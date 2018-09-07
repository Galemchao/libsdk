//
//  sdk.m
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//
#import "sdk.h"
#import "sdk_audio.h"
#import "sdkdef.h"
#import "sdk_amap.h"
#import "sdk_iospay.h"
#import "sdk_album.h"
#import "sdk_um.h"
//#import <sdk_wx.h>
#include "CCLuaBridge.h"
#include "CCLuaEngine.h"
#import <UIkit/UIWebView.h>
#import <UIKit/UIButton.h>
#import <WebKit/WKWebView.h>
#ifdef __IPHONE_9_0
#import <SafariServices/SFSafariViewController.h>
#endif
#import <AudioToolbox/AudioToolbox.h>
#import "WbViewController.h"
//#import "HPPhotoPickerController.h"
#import "photoPickerController.h"

static int evthandler = 0;
static NSString* schemeUrl = nil;
static NSMutableDictionary* gDic = nil;
static NSString* clippingImgPath = nil; //保存裁剪相册图片路径

@interface sdk ()
+ (void) notifyEventByObject: (id) object;
+ (void) notifyEvent: (NSString*) str;
+ (NSString*) toJsonStr:(id) object;
+ (NSDictionary *)toDictionay:(NSString *)jsonString;
@end

@implementation sdk

+ (UIViewController*) uivc;
{
//    key window 不可靠并不一定是gamewindow，alert关闭以后在0.3-0.4秒内会清除keywindow
//    在alert回调中立即添加view到keywindow，都会被清除
//    return [[[UIApplication sharedApplication] keyWindow] rootViewController];
//    ----------------------------------------------------------------------------
//    return [[[UIApplication sharedApplication].delegate window] rootViewController];
    return [[UIApplication sharedApplication].windows[0] rootViewController];
}
+ (NSString*) toJsonStr:(id) object
{
    NSString* jsonStr =nil;
    NSError * error = nil;
    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:object options:NSJSONWritingPrettyPrinted error:&error];
    if(!jsonData)
    {
        NSLog(@"toJsonStr error: %@", error);
    }
    jsonStr = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    return jsonStr;
}

+ (NSDictionary *)toDictionay:(NSString *)jsonString
{
    if (jsonString == nil) {
        return nil;
    }
    
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    if(jsonData == nil)
    {
        return nil;
    }
    
    NSError *err = nil;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&err];
    if(err)
    {
        NSLog(@"json解析失败：%@",err);
        return nil;
    }
    return dic;
}

+ (void) notifyEventByObject: (id) object
{
    NSString* str  = [sdk toJsonStr:object];
    if(!str)
    {
        str = @"";
    }
    [sdk notifyEvent:str];
    
}

+ (void) notifyEvent: (NSString*) str
{
    if(!evthandler)
    {
        return;
    }
    
    cocos2d::LuaBridge::pushLuaFunctionById(evthandler);
    cocos2d::LuaStack *stack = cocos2d::LuaBridge::getStack();
    stack->pushString([str UTF8String]);
    stack->executeFunction(evthandler);
    
}

+ (void) init
{
    
}

+ (void) setEventHandler:(int) handle
{
    if(evthandler)
    {
        cocos2d::LuaBridge::releaseLuaFunctionById(evthandler);
    }
    evthandler = handle;
    cocos2d::LuaBridge::retainLuaFunctionById(evthandler);
}

+ (void) init_handle:(int) handle
{
    [sdk setEventHandler:handle];
    if(gDic)
    {
        [sdk um_init:gDic];
//        [sdk loc_init:gDic];
        [sdk init_record:gDic];
    }
    else
    {
        NSLog(@"sdk init error, no config");
    }
    
    //    NSDictionary* dict = [[NSMutableDictionary alloc ] initWithCapacity:10];
    //    [dict setValue:[NSString stringWithFormat:@"%d",-1] forKey:SDK_ERROR];
    //    [dict setValue:@"sdk login jsonObj data resolution fail" forKey:SDK_ERROR_MSG];
    //    [sdk notifyEventByObject:dict];
}

+ (void) config:(NSString*) data
{
    if(nil==gDic)
    {
        gDic = [[NSMutableDictionary alloc ] initWithCapacity:10];
    }
    [gDic removeAllObjects];
    NSDictionary* dic = [sdk toDictionay:data];
    if(!dic)
    {
        return;
    }
    for (NSString *key in dic) {
        [gDic setValue:dic[key] forKey:key];
    }
}

+ (void) login:(NSString*) params
{
    NSDictionary* dic = [sdk toDictionay:params];
    if(!dic)
    {
        return;
    }
    NSNumber* type = [dic valueForKey:SDK_LOGIN_TYPE];
    if(!type)
    {
        type = [NSNumber numberWithInt:SDK_TYPE_WX];
    }
    [sdk um_login:[type intValue]];
}

+ (void) pay:(NSString*) params
{
    NSDictionary* dic = [sdk toDictionay:params];
    if(!dic)
    {
        return;
    }
    //TODO
}

+ (void) openBrowser:(NSString*)urlstr
{
    NSURL* url = [NSURL URLWithString:urlstr];
    [[UIApplication sharedApplication] openURL:url];
}

+ (void) openWebView:(NSString*)urlstr
{
    float sysver = [[[UIDevice currentDevice] systemVersion] floatValue];
    if(sysver>=9.0)
    {
        NSURL* url = [NSURL URLWithString:urlstr];//创建URL
        SFSafariViewController* sfvc = [[SFSafariViewController alloc] initWithURL:url];
        if (@available(iOS 11.0, *)) {
            //            [sdk uivc] 使用window[0] 即gamewindow，alert是新的window，从alert返回后立即调用因w0还不是keywindow导致空白
            UIWindow * w0 = [UIApplication sharedApplication].windows[0];
            UIWindow * wk = [UIApplication sharedApplication].keyWindow;
            if(w0!=wk){
                dispatch_async(dispatch_get_main_queue(),^{
                    [w0 makeKeyWindow];
                });
            }
        }
        [[sdk uivc] presentViewController:sfvc animated:YES completion:nil];
    }
    else
    {
        WbViewController* wbvc =  [WbViewController create:urlstr];
        [[sdk uivc] presentViewController:wbvc animated:YES completion:nil];
    }
}

+ (void) open_uiwebview:(NSString*)urlstr
{
    WbViewController* wbvc =  [WbViewController create:urlstr];
    [[sdk uivc] presentViewController:wbvc animated:YES completion:nil];
}

+ (void) share:(NSString*) params
{
    NSDictionary* dic = [sdk toDictionay:params];
    if(!dic)
    {
        return;
    }
    NSNumber* type = [dic valueForKey:SDK_SHARE_TYPE];
    NSString* title = [dic valueForKey:SDK_SHARE_TITLE];
    NSString* text = [dic valueForKey:SDK_SHARE_TEXT];
    NSString* image = [dic valueForKey:SDK_SHARE_IMAGE];
    NSString* url = [dic valueForKey:SDK_SHARE_URL];
    NSNumber* alert = [dic valueForKey:SDK_SHARE_RESULT_ALERT];

    if(!type)   type = [NSNumber numberWithInt:-1];
    if(!title)  title=@"";
    if(!text)   text=@"";
    if(!image)  image=@"";
    if(!url)    url=@"";
    if(!alert)  alert = [NSNumber numberWithInt:0];

    [sdk um_set_sharealert:[alert intValue]];
    [sdk um_share:[type intValue] Title:title Text:text Img:image Url:url];
}

+ (void) save_image_album:(NSString*) str
{
    [sdk ab_save_image:str Name:NULL];
}

+ (NSString*) get_pasteboard
{
    UIPasteboard* pasteboard = [UIPasteboard generalPasteboard];
    NSString* ss = [pasteboard string];
    if(ss==nil)
    {
        return @"";
    }
    return ss;
}

+ (void) set_pasteboard:(NSString*) str
{
    if(str)
    {
        UIPasteboard* pasteboard = [UIPasteboard generalPasteboard];
        [pasteboard setString:str];
    }
}

+ (BOOL) init_record:(id) dic
{
    [[sdk_audio sharedSdkAudio] init_record:dic];
    return YES;
}
+ (BOOL) start_record:(NSString*) data
{
    NSDictionary* dic = [sdk toDictionay:data];
    if(!dic)
    {
        return NO;
    }
    NSString* filename = [dic valueForKey:SDK_RECORD_FILENAME];
    return [[sdk_audio sharedSdkAudio] start_record: filename];
}

+ (void) stop_record
{
    [[sdk_audio sharedSdkAudio] stop_record];
}

+ (int) record_volume
{
   return [[sdk_audio sharedSdkAudio] record_getVolume];
}

+ (double) get_audio_duration:(NSString*) filename
{
    return [[sdk_audio sharedSdkAudio] record_getDuration:filename];
}

+ (void) init_locate
{
    if(gDic)
    {
        [sdk loc_init:gDic];
    }
}

+ (void) start_locate;
{
    [sdk loc_start];
}

+ (void) stop_locate;
{
    [sdk loc_stop];
}

+ (double) get_distance:(double)alongitude :(double)alatitude :(double)blongitude :(double)blatitude
{
    return [sdk loc_get_distance:alongitude :alatitude :blongitude :blatitude];
}

+ (void) start_vibrator:(long)milliseconds
{
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
}

+ (void) stop_vibrator
{
    
}

+(void) setSchemeUrl:(NSString*)url
{
    if (schemeUrl) {
        [schemeUrl release];
    }
    schemeUrl = [[NSString alloc]initWithString:url];
}

+(NSString*) getSchemeUrl
{
    return schemeUrl;
}

+ (void)saveImage:(UIImage *)image {
    BOOL result =[UIImagePNGRepresentation(image)writeToFile:clippingImgPath atomically:YES]; // 保存成功会返回YES
    if (result == YES) {
        NSLog(@"保存成功");
        NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
        [dic setValue:SDK_EVT_PICKIMG forKey:SDK_EVT];
        [dic setValue:[NSNumber numberWithInt:0] forKey:SDK_ERROR];
        [dic setValue:clippingImgPath forKey:SDK_RECORD_FILENAME];
        [sdk notifyEventByObject: dic];
    }
    else {
        NSLog(@"保存图片失败");
        NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
        [dic setValue:SDK_EVT_PICKIMG forKey:SDK_EVT];
        [dic setValue:[NSNumber numberWithInt:-1] forKey:SDK_ERROR];
        [sdk notifyEventByObject: dic];
    }
    if (clippingImgPath) {
        [clippingImgPath  release];
    }
}

+ (void) clippingImg:(NSString*) parmas
{
    NSDictionary *dic = [sdk toDictionay:parmas];
    if(!dic)
    {
        return;
    }
    NSString* filename = [dic valueForKey:SDK_RECORD_FILENAME];
    clippingImgPath = [[NSString alloc]initWithString:filename];
    
    photoPickerController *pickerVC = [[photoPickerController alloc] autorelease];
    UIViewController* last = [sdk uivc];
//    last.definesPresentationContext = NO;
//    pickerVC.view.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0];
//    pickerVC.modalPresentationStyle = UIModalPresentationOverCurrentContext;
    [pickerVC initWithDelegate:[sdk class] data:dic];
//    UINavigationController *navi = [[[UINavigationController alloc]initWithRootViewController:pickerVC] autorelease];
//    [last presentViewController:navi animated:NO completion:nil];
    [last presentViewController:pickerVC animated:NO completion:nil];
}

+ (void)imagePickerController:(photoPickerController *)picker didFinishPickingWithImage:(UIImage *)image
{
    NSLog(@"////////%@",image);
    [sdk saveImage:image];
}

+ (void) iospay_init:(id) dic
{
    [sdk iap_init:dic];
}

+ (void) iospay_req:(NSString*)pid :(NSString*)cid :(BOOL)force
{
    [sdk iap_req:pid :cid :force];
}

+ (void) iospay_stop
{
    [sdk iap_stop];
}

//-------------------------
+ (BOOL) handle_url:(NSURL*)url
{
    return [sdk um_handle_url:url];
}

+ (BOOL) handle_url:(NSURL*)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options
{
   return [sdk um_handle_url:url options:options];
}

+ (BOOL) handle_url:(NSURL*)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
    return [sdk um_handle_url:url sourceApplication:sourceApplication annotation:annotation];
}

@end
