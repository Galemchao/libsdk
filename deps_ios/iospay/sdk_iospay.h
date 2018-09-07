//
//  sdk.h
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "sdk.h"
@interface sdk (iospay)
+ (void) iap_init : (id)data;
//force --如果正在进行的支付流程,强行终止
+ (void) iap_req:(NSString*)pid :(NSString*)customid :(BOOL)force;
+ (void) iap_stop;
//-3 busy繁忙
//-2 没有支付信息
//-1 未初始化
//以上<0都未提交支付流程
//0 购买成功
//1 支付ing
//2 支付请求失败
//3 支付请求到支付信息不存在
//100 支付失败
//101 用户取消
//102 RESTORED
//103 其他
+ (void) iap_notify:(int) error :(NSString*)pid :(NSString*)customid :(NSString*)identifer :(NSString*)info;
@end
