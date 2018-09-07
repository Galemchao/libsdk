//
//  sdk.h
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//

#import <Foundation/Foundation.h>
#import  "sdk.h"
@interface sdk (album)
+ (void) ab_init : (NSDictionary*)dic;
+ (void) ab_save_image:(NSString*)imgpath Name:(NSString*)name;
+ (void) ab_save_nofity:(int) error Data:(id)data;
+ (BOOL) ab_handle_url:(NSURL*)url;
+ (BOOL) ab_handle_url:(NSURL*)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options;
+ (BOOL) ab_handle_url:(NSURL*)url sourceApplication:(NSString *)sourceApplication annotation:(id) annotation;
@end
