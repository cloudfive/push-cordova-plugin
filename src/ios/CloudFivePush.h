#import <Cordova/CDV.h>

@interface CloudFivePush : CDVPlugin <NSURLConnectionDataDelegate, UIAlertViewDelegate>
-(void)register:(CDVInvokedUrlCommand*)command;
-(void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)token;
-(void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;

//-(void)didReceiveRemoteNotification:(NSDictionary *)userInfo;
-(void)sendResult:(NSDictionary *)result;

@property (nonatomic, strong) NSString* callbackId;
@property NSString* uniqueIdentifier;
@property NSString* apsToken;
@property NSDictionary* alertUserInfo;
@end
