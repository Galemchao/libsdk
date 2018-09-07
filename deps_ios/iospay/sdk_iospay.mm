//
//  sdk.m
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//
#import "sdk_iospay.h"
#import "IOSiAP_Bridge.h"

static std::map<std::string,std::string> sMap;

@implementation sdk (iospay)
+ (void) iap_init : (id)data
{
    sMap.clear();
    NSDictionary* dic = (NSDictionary*)data;
    for (NSString *key in dic) {
        std::string k = [key UTF8String];
        NSString*vv = dic[key];
        std::string v = [vv UTF8String];
        sMap[k] = v;
    }
}
+ (void) iap_req:(NSString*)pid :(NSString*)cid :(BOOL)force
{
    std::string ppid = [pid UTF8String];
    std::string pcid = [cid UTF8String];
    
    if(sMap.size()==0)
    {
        [sdk iap_notify:-1 :pid :cid :@"" :@"没有商品信息1"];
        return;
    }
    if(sMap.find(ppid)==sMap.end())
    {
        [sdk iap_notify:-2 :pid :cid :@"" :@"没有商品信息2"];
        return;
    }
    if(IOSiAP_Bridge::getInstance()->isBusy())
    {
        if(force)
        {
            IOSiAP_Bridge::delInstance();
        }
        else
        {
            [sdk iap_notify:-3 :pid :cid :@"" :@"正在购买，请稍后"];
            return;
        }
    }
    int flag = IOSiAP_Bridge::getInstance()->requestProducts(ppid,sMap,pcid);
    if(flag==1)
    {
        [sdk iap_notify:1 :pid :cid :@"" :@"正在购买，请稍后"];
    }
}

+ (void) iap_stop
{
    if(IOSiAP_Bridge::getInstance()->isBusy())
    {
        IOSiAP_Bridge::delInstance();
    }
}

+ (void) iap_notify:(int) error :(NSString*)pid :(NSString*)cid :(NSString*)identifer :(NSString*)info
{
    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
    [dic setValue:SDK_EVT_IOSPAY forKey:SDK_EVT];
    if(0==error){
        [dic setValue:[NSNumber numberWithInt:error] forKey:SDK_ERROR];
        [dic setValue:pid forKey:SDK_IOSPAY_PID];
        [dic setValue:cid forKey:SDK_IOSPAY_NUMB];
        [dic setValue:identifer forKey:SDK_IOSPAY_IDENTIFER];
        [dic setValue:info forKey:SDK_IOSPAY_INFO];
    }
    else{
        [dic setValue:[NSNumber numberWithInt:error] forKey:SDK_ERROR];
        [dic setValue:pid forKey:SDK_IOSPAY_PID];
        [dic setValue:cid forKey:SDK_IOSPAY_NUMB];
        [dic setValue:identifer forKey:SDK_IOSPAY_IDENTIFER];
        [dic setValue:info forKey:SDK_IOSPAY_INFO];
        NSLog(@"iospay_notify error：%d",error);
    }
    [sdk notifyEventByObject: dic];
}


@end
