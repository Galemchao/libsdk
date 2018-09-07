//
//  sdk.h
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "sdk.h"
@interface sdk (um)
+ (void) um_set_sharealert : (int)flag;
+ (BOOL) um_isinstall : (NSString*)name;
+ (void) um_init : (NSDictionary*)dic;
+ (void) um_login:(int) type;
+ (void) um_share:(int) type Title:(NSString*)title Text:(NSString*)text Img:(NSString*)img Url:(NSString*)url;

+ (void) um_login_notify:(NSError*) error Platform:(int)plat Data:(id)data;
+ (void) um_share_nofity:(NSError*) error Platform:(int)plat Data:(id)data;
+(UIImage*)um_image_scale:(UIImage*)image scale:(float)scale;

+ (BOOL) um_handle_url:(NSURL*)url;
+ (BOOL) um_handle_url:(NSURL*)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options;
+ (BOOL) um_handle_url:(NSURL*)url sourceApplication:(NSString *)sourceApplication annotation:(id) annotation;
@end
