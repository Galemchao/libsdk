//
//  sdk.m
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//
#import "sdk_um.h"
#import <UMShare/UMSocialManager.h>
#import <UMShare/UMShare.h>
#import <UShareUI/UMSocialUIManager.h>
#import <UShareUI/UShareUI.h>
#import <UMCommon/UMConfigure.h>
#import <UMPush/UMessage.h>

static int share_alert=0;

@implementation sdk (um)
+ (void) um_set_sharealert : (int)flag
{
    share_alert = flag;
}

+ (BOOL) um_isinstall : (NSString*)name
{
    return [[UMSocialManager defaultManager]  isInstall:UMSocialPlatformType_WechatSession];
}

+ (void) um_init : (NSDictionary*)dic
{
    //打开调试日志
//    [[UMSocialManager defaultManager] openLog:YES];
//    [UMSocialGlobal shareInstance].isClearCacheWhenGetUserInfo = NO;
    [UMConfigure setLogEnabled:YES];
    //设置友盟appkey
//    [[UMSocialManager defaultManager] setUmSocialAppkey:[dic valueForKey:TOKEN_UM_APPKEY]];
    
    NSString* key = [dic valueForKey:TOKEN_UM_APPKEY];
    if(key==nil)
    {
        key = [[[NSBundle mainBundle] infoDictionary] objectForKey:TOKEN_UM_APPKEY];
        NSLog(@"sdk um_init %@ empty",TOKEN_UM_APPKEY);
        return;
    }
    NSString* channel = [dic valueForKey:TOKEN_UM_APPCHANNEL];
    if (channel == nil)
    {
        channel = @"um_ios";
    }
    //设置友盟appkey
//    [[UMSocialManager defaultManager] setUmSocialAppkey:key];
     [UMConfigure initWithAppkey:key channel:channel];
    
    // Push组件基本功能配置
    UMessageRegisterEntity * entity = [[UMessageRegisterEntity alloc] init];
    //type是对推送的几个参数的选择，可以选择一个或者多个。默认是三个全部打开，即：声音，弹窗，角标
    entity.types = UMessageAuthorizationOptionBadge|UMessageAuthorizationOptionSound|UMessageAuthorizationOptionAlert;
    [UNUserNotificationCenter currentNotificationCenter].delegate=self;
    [UMessage registerForRemoteNotificationsWithLaunchOptions:nil Entity:entity completionHandler:^(BOOL granted, NSError * _Nullable error) {
        if (granted) {
        }else{
        }
    }];
    
    // 获取友盟social版本号
    NSLog(@"UMeng social version: %@", [UMSocialGlobal umSocialSDKVersion]);
    
    //设置微信的appKey和appSecret
    [[UMSocialManager defaultManager] setPlaform:UMSocialPlatformType_WechatSession appKey:[dic valueForKey:TOKEN_WX_APPKEY] appSecret:[dic valueForKey:TOKEN_WX_APPSECRET] redirectURL:nil];
    
    //设置QQ的appKey和appSecret
    [[UMSocialManager defaultManager] setPlaform:UMSocialPlatformType_QQ appKey:[dic valueForKey:TOKEN_QQ_APPKEY] appSecret:[dic valueForKey:TOKEN_QQ_APPSECRET] redirectURL:nil];
    
    // 如果不想显示平台下的某些类型，可用以下接口设置
    [[UMSocialManager defaultManager] removePlatformProviderWithPlatformTypes:@[

                                                                                //@(UMSocialPlatformType_QQ),
                                                                                @(UMSocialPlatformType_Qzone),            @(UMSocialPlatformType_TencentWb),
                                                                                @(UMSocialPlatformType_Facebook),
                                                                                @(UMSocialPlatformType_WechatFavorite),
                                                                                @(UMSocialPlatformType_Sms),
                                                                                @(UMSocialPlatformType_Email),
                                                                                @(UMSocialPlatformType_Renren),
                                                                                @(UMSocialPlatformType_Douban),
                                                                                @(UMSocialPlatformType_Flickr),                                                                            @(UMSocialPlatformType_Twitter),
                                                                                @(UMSocialPlatformType_YixinTimeLine),
                                                                                @(UMSocialPlatformType_LaiWangTimeLine),
                                                                                @(UMSocialPlatformType_Linkedin),
                                                                                @(UMSocialPlatformType_AlipaySession)]
                                                                                ];
}

+ (void) um_login:(int) p
{
    UMSocialPlatformType platformType = UMSocialPlatformType_QQ;
    if (p == 1) {
        platformType = UMSocialPlatformType_WechatSession;
    } else if (p == 3) {
        platformType = UMSocialPlatformType_QQ;
    }
    
    UIViewController* vc = [sdk uivc];
    [[UMSocialManager defaultManager]  getUserInfoWithPlatform:platformType currentViewController:vc completion:^(id result, NSError *error) {
        [sdk um_login_notify: error Platform:p Data:result];
    }];

}

+(void) um_login_notify:(NSError*) error Platform:(int)plat Data:(id) data
{
    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
    [dic setValue:SDK_EVT_LOGIN forKey:SDK_EVT];
    [dic setValue:[NSNumber numberWithInt:plat] forKey:SDK_SHARE_TYPE];
    if(nil==error){
        UMSocialUserInfoResponse *usinfo = data;
        [dic setValue:usinfo.openid forKey:SDK_OPENID];
        [dic setValue:usinfo.name forKey:SDK_NAME];
        [dic setValue:usinfo.iconurl forKey:SDK_ICONURL];
        [dic setValue:usinfo.gender forKey:SDK_GENDER];
        [dic setValue:usinfo.accessToken forKey:SDK_ACCESS_TOKEN];
        [dic setValue:usinfo.refreshToken forKey:SDK_REFRESH_TOKEN];        
        [dic setValue:[NSNumber numberWithInt:0] forKey:SDK_ERROR];
    }
    else{
        [dic setValue:[NSNumber numberWithInt:1] forKey:SDK_ERROR];
        NSLog(@"um_login_notify error：%@",error);
    }
    [sdk notifyEventByObject: dic];
}

+(UIImage*)um_image_scale:(UIImage*)image scale:(float)scale
{
    
    CGSize size = image.size;
    
    CGFloat width = size.width;
    
    CGFloat height=size.height;
    
    CGFloat scaledWidth = width*scale;
    
    CGFloat scaledHeight = height*scale;
    
    size.width = scaledWidth;
    size.height = scaledHeight;
    
    UIGraphicsBeginImageContext(size);//thiswillcrop
    
    [image drawInRect:CGRectMake(0,0,scaledWidth,scaledHeight)];
    
    UIImage*newImage=UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    return newImage;

}

typedef void(^UM_SHARE)(UMSocialPlatformType platformType, NSDictionary *userInfo);
+ (void) um_share:(int) type Title:(NSString*)title Text:(NSString*)text Img:(NSString*)img Url:(NSString*)url
{
    NSLog(@"分享地址为：%@",url);
//    if(!url || [url length] == 0)
//    {
//        url = APPSTORE_URL;
//    }
    
    UM_SHARE toshare = ^(UMSocialPlatformType platformType, NSDictionary *userInfo) {
        //创建分享消息对象
        UMSocialMessageObject *messageObject = [UMSocialMessageObject messageObject];
        
        //设置文本
        messageObject.text = text;
        
        if([url length]>0)
        {
            UMShareWebpageObject* shareUrlobj =[[UMShareWebpageObject alloc] init];
            shareUrlobj.webpageUrl = url;
            shareUrlobj.title = title;
            shareUrlobj.descr = text;
            shareUrlobj.thumbImage = [UIImage imageNamed:@"Icon.png"];
            messageObject.shareObject = shareUrlobj;
        }
        else if([img length]>0)
        {   //创建图片内容对象
            UMShareImageObject *shareObject = [[UMShareImageObject alloc] init];
            shareObject.shareImage = [UIImage imageWithContentsOfFile:img];
            //如果有缩略图，则设置缩略图
//            shareObject.thumbImage = [UIImage imageNamed:@"Icon.png"];
//            shareObject.thumbImage = [sdk um_image_scale:shareObject.shareImage scale:0.5];
            shareObject.thumbImage = [UIImage imageWithContentsOfFile:img];
            
            shareObject.title = title;
            shareObject.descr = text;
            
            //分享消息对象设置分享内容对象
            messageObject.shareObject = shareObject;
        }
        else
        {
            UMShareObject *shareObject = [[UMShareObject alloc] init];
            shareObject.thumbImage = [UIImage imageNamed:@"Icon.png"];
            shareObject.title = title;
            shareObject.descr = text;
            messageObject.shareObject = shareObject;
        }
        //调用分享接口
        UIViewController* vc= [sdk uivc];
        [[UMSocialManager defaultManager] shareToPlatform:platformType messageObject:messageObject currentViewController:vc completion: ^(id data, NSError *error) {
            [sdk um_share_nofity:error Platform:platformType Data:data];
        }];
    };

    if(type<0)
    {
        [UMSocialUIManager showShareMenuViewInWindowWithPlatformSelectionBlock:toshare];
    }
    else
    {
        UMSocialPlatformType pp = UMSocialPlatformType_WechatSession;
        if (type == 1) {
            pp = UMSocialPlatformType_WechatSession;
        } else if (type == 2) {
            pp = UMSocialPlatformType_WechatTimeLine;
        } else if (type == 3) {
            pp = UMSocialPlatformType_QQ;
        }
        toshare(pp,nil);
    }
}

+(void) um_share_nofity:(NSError*) error Platform:(int)plat Data:(id)data
{
    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
    [dic setValue:SDK_EVT_SHARE forKey:SDK_EVT];
    [dic setValue:[NSNumber numberWithInt:plat] forKey:SDK_SHARE_TYPE];
    if(nil==error){
        [dic setValue:[NSNumber numberWithInt:0] forKey:SDK_ERROR];
    }
    else{
        int err = 1;
        if(error.code == UMSocialPlatformErrorType_Cancel)
        {
            err=2;
        }
        [dic setValue:[NSNumber numberWithInt:err] forKey:SDK_ERROR];
    }
    [sdk notifyEventByObject: dic];
    if(share_alert>0)
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [sdk um_share_alert:error];
        });
    }
}

+ (void)um_share_alert:(NSError *)error
{
    NSString *result = nil;
    if (!error) {
        result = [NSString stringWithFormat:@"分享成功"];
    }
    else{
        NSMutableString *str = [NSMutableString string];
        if (error.userInfo) {
            for (NSString *key in error.userInfo) {
                [str appendFormat:@"%@ = %@\n", key, error.userInfo[key]];
            }
        }
        if (error) {
            result = [NSString stringWithFormat:@"分享失败: %d\n%@",(int)error.code, str];
        }
        else{
            result = [NSString stringWithFormat:@"分享失败"];
        }
    }
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"分享" message:result preferredStyle:UIAlertControllerStyleAlert];
    [[sdk uivc] presentViewController:alert animated:NO completion:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [alert dismissViewControllerAnimated:YES completion:^{
            
        }];
    });
}

+ (BOOL) um_handle_url:(NSURL*)url
{
    BOOL result = [[UMSocialManager defaultManager] handleOpenURL:url];
    if (!result) {
        // 其他如支付等SDK的回调
    }
    return result;
}

+ (BOOL) um_handle_url:(NSURL*)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options
{
    BOOL result = [[UMSocialManager defaultManager] handleOpenURL:url options:options];
    if (!result) {
        // 其他如支付等SDK的回调
    }
    return result;
}

// 支持所有iOS系统
+ (BOOL) um_handle_url:(NSURL*)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation

{
    BOOL result = [[UMSocialManager defaultManager] handleOpenURL:url sourceApplication:sourceApplication annotation:annotation];
    if (!result) {
        // 其他如支付等SDK的回调
    }
    return result;
}

@end
