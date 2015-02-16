//
//  AppDelegate+notification.m
//  pushtest
//
//  Created by Robert Easterday on 10/26/12.
//
//

#import "AppDelegate+notification.h"

@implementation AppDelegate (notification)

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

@end