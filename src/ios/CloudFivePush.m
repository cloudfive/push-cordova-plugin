#import "CloudFivePush.h"
#import <Cordova/CDV.h>

@implementation CloudFivePush : CDVPlugin

/* this method is called the moment the class is made known to the obj-c runtime,
 before app launch completes. */

- (id)initWithWebView:(UIWebView *)theWebView {
    if (self = [super initWithWebView:theWebView]) {
       [[UIApplication sharedApplication] registerForRemoteNotificationTypes:(UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound)];
    }
    return self;
}

- (void)alert:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];

    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)token
{
    NSLog(@"Got token: %@", token);
    _apsToken = [[[[token description] stringByReplacingOccurrencesOfString:@"<"  withString:@""]
                                       stringByReplacingOccurrencesOfString:@">"  withString:@""]
                                       stringByReplacingOccurrencesOfString: @" " withString:@""];
    [self notifyCloudFive];
    
    if (_registrationCallbackId) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[token description]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_registrationCallbackId];
    }
}
-(void) didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    NSLog(@"Error registering for push");
}

- (void)register:(CDVInvokedUrlCommand*)command
{
    _uniqueIdentifier = (NSString*)[command argumentAtIndex:0];
    [[UIApplication sharedApplication] registerForRemoteNotificationTypes:UIRemoteNotificationTypeAlert | UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound];
}

-(void)didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    NSDictionary *payload = [userInfo objectForKey:@"aps"];
    NSString *alert = [payload objectForKey:@"alert"];
    if (alert) {
        [self showAlertWithMessage:alert];
    }
}

-(void)notifyCloudFive
{
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:@"https://www.cloudfiveapp.com/push/register"]];
    request.HTTPMethod = @"POST";
    UIDevice *dev = [UIDevice currentDevice];
    NSString *postData = [NSString stringWithFormat:@"bundle_identifier=%@&device_token=%@&device_platform=ios&user_identifier=%@&device_name=%@&device_model=%@&device_version=%@&app_version=%@",
                             [[NSBundle mainBundle] bundleIdentifier],
                             _apsToken,
                             _uniqueIdentifier,
                            dev.name,
                            dev.model,
                            dev.systemVersion,
                            [[NSBundle mainBundle] objectForInfoDictionaryKey:(NSString*)kCFBundleVersionKey]
                          
     ];

    request.HTTPBody = [postData dataUsingEncoding:NSUTF8StringEncoding];
    NSURLConnection *conn = [NSURLConnection connectionWithRequest:request delegate:self];
    [conn start];
}


-(void)showAlertWithMessage:(NSString*)message
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Alert"
                                                    message:message
                                                   delegate:nil
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
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
- (BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace {
    return [protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust];
}

- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge {
    NSURLCredential *credential = [NSURLCredential credentialForTrust:challenge.protectionSpace.serverTrust];
    [challenge.sender useCredential:credential forAuthenticationChallenge:challenge];
}
@end