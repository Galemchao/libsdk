//
//  sdk.m
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//
#import "sdkControl.h"
#import "sdkdef.h"
#import "sdk.h"
#import "sysinfo.h"
#import "sdk_audio.h"
#import "sdk_amap.h"
#import "sdk_iospay.h"
#import "sdk_album.h"

@implementation sdkControl

+ (void) init
{
    [sdk init];
}

+ (void) initHandle:(int) handle
{
    [sdk init_handle:handle];
}

+ (void) setEventHandler:(int) handle
{
    [sdk setEventHandler:handle];
}

+ (void) initConfig:(NSString*) data
{
    [sdk config:data];
}

+ (void) setSchemeUrl:(NSString*) url
{
    [sdk setSchemeUrl:url];
}

+ (NSString*) getSchemeUrl
{
    return [sdk getSchemeUrl];
}

+ (void) login:(NSString*) params
{
    [sdk login:params];
}

+ (void) pay:(NSString*) params
{
    [sdk pay:params];
}

+ (void) openBrowser:(NSString*)urlstr
{
    [sdk openBrowser:urlstr];
}

+ (void) openWebView:(NSString*)urlstr
{
    [sdk openWebView:urlstr];
}

+ (void) share:(id)data;
{
    [sdk share:data];
}

+ (BOOL) copyToClipboard:(NSString*) str
{
    [sdk set_pasteboard:str];
    return YES;
}

+ (BOOL) initRecord:(NSString*) data
{
    return [sdk init_record:data];
}

+ (BOOL) startRecord:(NSString*) data
{
    return [sdk start_record:data];
}

+ (void) stopRecord
{
    [sdk stop_record];
}

+ (int) recordGetVolume
{
    return [sdk record_volume];
}

+ (double) getAudioDuration:(NSString*) filename;
{
    return [sdk get_audio_duration:filename];
}

+ (void) initLocate
{
    [sdk init_locate];
}

+ (void) startLocate;
{
    [sdk start_locate];
}

+ (void) stopLocate;
{
    [sdk stop_locate];
}

+ (double) getDistance:(double)alongitude :(double)alatitude :(double)blongitude :(double)blatitude
{
    return [sdk get_distance:alongitude :alatitude :blongitude :blatitude];
}

+ (void) saveImageAlbum:(NSString*) str
{
    [sdk save_image_album:str];
}

+ (BOOL) saveImgToSystemGallery:(NSString*)imgPath :(NSString*)fileName
{
    return NO;
}

+ (void) pickImg:(NSString*) parmas
{
    [sdk clippingImg:parmas];
}

//+ (NSString*) getChannel
//{
//    return [sysinfo getChannel];
//}

//+ (NSString*) getExternInfo
//{
//    return @"";
//}

+ (NSString*) getVersionName
{
    return [sysinfo appversion];
}

+ (BOOL) vibrate:(long)milliseconds
{
    [sdk start_vibrator:milliseconds];
    return YES;
}

+ (NSString*) getIpAddress
{
    return [sysinfo ipaddress];
}

+ (NSString*) getUUID
{
    return [sysinfo imei];
}

+ (NSString*) getDeviceName
{
    return [sysinfo deviceVersion];
}

+ (NSString*) getMetaData:(NSString*)key
{
    return [sysinfo metadata:key];
}

+ (BOOL) isinstall:(NSString*) name
{
    return [sysinfo isinstall:name];
}

//pay
+ (void) iosPayInit:(id) data;
{
    [sdk iospay_init:data];
}
+ (void) iosPayReq:(NSString*)pid :(NSString*)customid :(BOOL)force
{
    [sdk iospay_req:pid :customid :force];
}
+ (void) iosPayStop
{
     [sdk iospay_stop];
}

//-------------------------
+ (BOOL) handleUrl:(NSURL*)url
{
    return [sdk handle_url:url];
}

+ (BOOL) handleUrl:(NSURL*)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options
{
    return [sdk handle_url:url options:options];
}

+ (BOOL) handleUrl:(NSURL*)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
    return [sdk handle_url:url sourceApplication:sourceApplication annotation:annotation];
}

@end
