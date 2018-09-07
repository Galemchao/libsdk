//
//  sdk.h
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "sdkdef.h"
@interface sdk : NSObject
+ (UIViewController*) uivc;
+ (void) notifyEventByObject: (id) object;
+ (void) notifyEvent: (NSString*) str;
+ (void) setEventHandler:(int) handle;
+ (void) setSchemeUrl:(NSString*) url;
+ (NSString*) getSchemeUrl;

+ (void) config:(NSString*)data;
+ (void) init;
+ (void) init_handle:(int) handle;
+ (void) login:(NSString*)data;
+ (void) share:(NSString*)data;
+ (void) pay:(NSString*)data;
+ (void) openBrowser:(NSString*)url;
+ (void) openWebView:(NSString*)url;
+ (void) open_uiwebview:(NSString*)urlstr;

+ (BOOL) init_record:(id) dic;
+ (BOOL) start_record:(NSString*)data;
+ (void) stop_record;
+ (int) record_volume;
+ (double) get_audio_duration:(NSString*) filename;

+ (void) init_locate;
+ (void) start_locate;
+ (void) stop_locate;
+ (double) get_distance:(double)alongitude :(double)alatitude :(double)blongitude :(double) blatitude;
//+ (double) get_distance:(id)array;
+ (NSString*) get_pasteboard;
+ (void) set_pasteboard:(NSString*) str;
//
+ (void) start_vibrator:(long)milliseconds;
+ (void) stop_vibrator;
//save image
+ (void) save_image_album:(NSString*) str;
+ (void)saveImage:(UIImage *)image;
//Clipping image
+ (void) clippingImg:(NSString*) filename;


//pay init
+ (void) iospay_init:(id) dic;
+ (void) iospay_req:(NSString*)pid :(NSString*)numb :(BOOL)force;
+ (void) iospay_stop;
//-------------------------
+ (BOOL) handle_url:(NSURL*)url;
+ (BOOL) handle_url:(NSURL*)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options;
+ (BOOL) handle_url:(NSURL*)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation;

@end
