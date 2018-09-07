//
//  sdk.m
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//
#import "sdk_album.h"

@implementation sdk (album)

+ (void) ab_init : (NSDictionary*)dic
{
}

+(UIImage*)ab_image_scale:(UIImage*)image scale:(float)scale
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

+ (void) ab_save_image: (NSString*)imgpath Name:(NSString*)name
{
//从文件读取,从后台返回情况下，图片一半黑屏，有可能跟图片名字没变化有关系
//从文件内容读取测试ok
//    UIImage *savedImage  = [UIImage imageNamed:imgpath];
    NSMutableData *data = [NSMutableData dataWithContentsOfFile: imgpath];
    UIImage *savedImage = [UIImage imageWithData: data];
    if(savedImage)
    {
//        savedImage = [sdk ab_image_scale: savedImage scale : 0.8f];
        UIImageWriteToSavedPhotosAlbum(savedImage, [sdk class], @selector(ab_save_image_callback:didFinishSavingWithError:contextInfo:), NULL);
    }
}

+ (void)ab_save_image_callback: (UIImage *) image didFinishSavingWithError: (NSError *) error contextInfo: (void *) contextInfo
{
    NSString *msg = nil ;
    if(error != NULL){
        msg = @"保存图片失败" ;
    }else{
        msg = @"保存图片成功" ;
    }
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"提示" message:msg preferredStyle:UIAlertControllerStyleAlert];
    [[sdk uivc] presentViewController:alert animated:NO completion:nil];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [alert dismissViewControllerAnimated:YES completion:^{

        }];
    });
    
    if(error != NULL){
        [sdk ab_save_nofity:1 Data:NULL];
    }else{
        [sdk ab_save_nofity:0 Data:NULL];
    }
}

+(void) ab_save_nofity:(int) error Data:(id)data
{
    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
    [dic setValue:SDK_EVT_SAVE_IMAGE forKey:SDK_EVT];
    [dic setValue:[NSNumber numberWithInt:error] forKey:SDK_ERROR];
    [sdk notifyEventByObject: dic];
}


+ (BOOL) ab_handle_url:(NSURL*)url
{
    return NO;
}

+ (BOOL) ab_handle_url:(NSURL*)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options
{
    return NO;
}

// 支持所有iOS系统
+ (BOOL) ab_handle_url:(NSURL*)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation

{
    return NO;
}

@end
