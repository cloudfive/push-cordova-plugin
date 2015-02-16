#import <Cordova/CDV.h>

@interface CloudFivePush : CDVPlugin <NSURLConnectionDataDelegate, UIAlertViewDelegate>
-(void)register:(CDVInvokedUrlCommand*)command;
-(void)finish:(CDVInvokedUrlCommand*)command;

@property (nonatomic, strong) NSString* callbackId;
@property NSString* uniqueIdentifier;
@property NSString* apsToken;
@end
