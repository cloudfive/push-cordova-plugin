#import <Cordova/CDV.h>

@interface CloudFivePush : CDVPlugin <NSURLConnectionDataDelegate>
-(void)alert:(CDVInvokedUrlCommand*)command;
-(void)register:(CDVInvokedUrlCommand*)command;
-(void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)token;
-(void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
-(void)didReceiveRemoteNotification:(NSDictionary *)userInfo;


@property NSString* registrationCallbackId;
@property NSString* uniqueIdentifier;
@property NSString* apsToken;

@end
