//
//  AppDelegate+notification.h
//  pushtest
//
//  Created by Robert Easterday on 10/26/12.
//  Refactored by Chris Scott <chris@transistorsoft.com>
//
//

#import "AppDelegate.h"

@interface AppDelegate (notification)
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult result))handler;
@end