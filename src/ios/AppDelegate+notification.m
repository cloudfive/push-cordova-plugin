//
//  AppDelegate+notification.m
//  pushtest
//
//  Created by Robert Easterday on 10/26/12.
//
//

#import "AppDelegate+notification.h"
#import "CloudFivePush.h"

@implementation AppDelegate (notification)

- (id) getCommandInstance:(NSString*)className
{
  return [self.viewController getCommandInstance:className];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    CloudFivePush *cloudFive = [self getCommandInstance:@"CloudFivePush"];
    [cloudFive didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    CloudFivePush *cloudFive = [self getCommandInstance:@"CloudFivePush"];
    [cloudFive didFailToRegisterForRemoteNotificationsWithError:error];
}


- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void(^)(UIBackgroundFetchResult result))completionHandler
{
    NSLog(@"- CloudFivePush didReceiveRemoteNotification");
    
    void (^safeHandler)(UIBackgroundFetchResult) = ^(UIBackgroundFetchResult result){
        dispatch_async(dispatch_get_main_queue(), ^{
            completionHandler(result);
        });
    };
    NSMutableDictionary* params = [NSMutableDictionary dictionaryWithCapacity:2];
    [params setObject:safeHandler forKey:@"handler"];
    [params setObject:userInfo forKey:@"userInfo"];
    [[NSNotificationCenter defaultCenter] postNotificationName:@"CloudFivePushDidReceiveRemoteNotification" object:params];
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    
    NSLog(@"active");
    
//    //zero badge
//    application.applicationIconBadgeNumber = 0;
    /*
    if (self.launchNotification) {
        CloudFivePush *cloudFive = [self getCommandInstance:@"CloudFivePush"];
        [cloudFive performSelectorOnMainThread:@selector(didReceiveRemoteNotification:) withObject:self.launchNotification waitUntilDone:NO];
        self.launchNotification = nil;
    }
     */
}

@end