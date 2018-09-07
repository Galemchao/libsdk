//
//  sdk.h
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface sysinfo : NSObject
//0 no
//1 4g
//2 wifi
+ (int) netstate;//网络状态
+ (int) batteryinfo;//电池电量0-100
+ (NSString*) imsi;//获取imsi
+ (NSString*) imei;//获取imei
+ (NSString*) ipaddress;//ip地址
+ (NSString*) macaddress;//获取mac地址
+ (NSString*) mobilemodel;//获取手机型号
+ (NSString*) systemversion;//获取系统版本号
+ (NSString*) packagename;//获取应用包名
+ (NSString*) appname;//获取应用名称
+ (NSString*) appversion;//获取应用版本
+ (NSString*) country;//国家
+ (NSString*) downloadurl;//下载地址
+ (BOOL) isinstall:(NSString*) name;//是否安装了某个app
+ (void) openapp:(NSString*) name;//打开app
+ (unsigned long) elapsedtime;//开机时间秒
+ (NSString*) metadata:(NSString*)key;//bund info
+ (NSString*)deviceVersion;//详细到手机型号

@end
