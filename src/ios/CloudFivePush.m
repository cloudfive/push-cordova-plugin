#import "CloudFivePush.h"
#import <Cordova/CDV.h>

@implementation CloudFivePush : CDVPlugin

/* this method is called the moment the class is made known to the obj-c runtime,
 before app launch completes. */

- (id)initWithWebView:(UIWebView *)theWebView {
    if (self = [super initWithWebView:theWebView]) {

    }
    return self;
}

// Send a message back to the javascript which handles all the success/failure stuff
- (void)sendResult:(NSDictionary *)result
{
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:result
                                                       options: 0
                                                         error:nil];
    
    NSString *jsonResult = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *javascript = [NSString stringWithFormat:@"window.CloudFivePush._messageCallback(%@)", jsonResult];
    
    [self.webView stringByEvaluatingJavaScriptFromString:javascript];
}

-(void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)token
{
    NSLog(@"Got token: %@", token);
    _apsToken = [[[[token description] stringByReplacingOccurrencesOfString:@"<"  withString:@""]
                                       stringByReplacingOccurrencesOfString:@">"  withString:@""]
                                       stringByReplacingOccurrencesOfString: @" " withString:@""];
    [self notifyCloudFive];
    
    [self sendResult:@{@"event": @"registration", @"success": @YES, @"token": _apsToken}];
}
-(void) didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    NSLog(@"Error registering for push");
    [self sendResult:@{@"event": @"registration", @"success": @NO, @"error": [error localizedDescription]} ];
}

// plugin method
- (void)register:(CDVInvokedUrlCommand*)command
{
    _uniqueIdentifier = (NSString*)[command argumentAtIndex:0];
    
    UIApplication *application = [UIApplication sharedApplication];
    if ([application respondsToSelector:@selector(isRegisteredForRemoteNotifications)]) {
        UIUserNotificationSettings *settings = [UIUserNotificationSettings
                                                settingsForTypes:UIUserNotificationTypeAlert|UIUserNotificationTypeBadge|UIUserNotificationTypeSound
                                                categories:nil];
        [application registerUserNotificationSettings:settings];
        [application registerForRemoteNotifications];
    } else {
        [application registerForRemoteNotificationTypes:(UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound)];
    }
}

-(void)didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    NSDictionary *payload = [userInfo objectForKey:@"aps"];
    NSString *message = [userInfo objectForKey:@"message"];
    NSString *alert = [payload objectForKey:@"alert"];
    NSDictionary *customData = [userInfo objectForKey:@"data"];

    NSString *title = alert;
    NSString *detailButton = nil;
    if (customData) {
        detailButton = @"Details";
    }
    
    if (message == nil) {
        title = [[[NSBundle mainBundle] infoDictionary]  objectForKey:@"CFBundleName"];
        message = alert;
    }

    if (alert) {
        self.alertUserInfo = userInfo;
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:title
                                                        message:message
                                                       delegate:self
                                              cancelButtonTitle:@"Dismiss"
                                              otherButtonTitles:detailButton, nil];
        [alertView show];
    }
    
    if (customData) {
        [self sendResult:@{@"event": @"message", @"payload": userInfo} ];
    }
}

- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (buttonIndex == 1) {
        [self sendResult:@{@"event": @"interaction", @"payload": self.alertUserInfo} ];
        self.alertUserInfo = nil;
    }
}

-(void)notifyCloudFive
{
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:@"https://www.cloudfiveapp.com/push/register"]];
    request.HTTPMethod = @"POST";
    UIDevice *dev = [UIDevice currentDevice];
    NSString *postData = [NSString stringWithFormat:@"bundle_identifier=%@&device_token=%@&device_platform=ios&device_name=%@&device_model=%@&device_version=%@&app_version=%@",
                            [[NSBundle mainBundle] bundleIdentifier],
                            _apsToken,
                            dev.name,
                            dev.model,
                            dev.systemVersion,
                            [[NSBundle mainBundle] objectForInfoDictionaryKey:(NSString*)kCFBundleVersionKey]
                          
    ];
    if (_uniqueIdentifier != nil) {
        postData = [postData stringByAppendingFormat:@"&user_identifier=%@", _uniqueIdentifier];
    }

    request.HTTPBody = [postData dataUsingEncoding:NSUTF8StringEncoding];
    NSURLConnection *conn = [NSURLConnection connectionWithRequest:request delegate:self];
    [conn start];
}

-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
    NSLog(@"Error talking to cloudfive");
}

-(void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    NSHTTPURLResponse* httpResponse = (NSHTTPURLResponse*)response;
    if ([httpResponse statusCode] == 200) {
        NSLog(@"Successfully registered!");
    } else {
        NSLog(@"Couldn't register with cloudfive");
    }
}

// Accept self signed certificates
//- (BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace {
//    return [protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust];
//}
//
//- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge {
//    NSURLCredential *credential = [NSURLCredential credentialForTrust:challenge.protectionSpace.serverTrust];
//    [challenge.sender useCredential:credential forAuthenticationChallenge:challenge];
//}
@end