//
//  sdk.m
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//
#import "sysinfo.h"
#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import <UIKit/UIKit.h>
#include <memory>
#include <mutex>
#include <sys/socket.h>
#include <sys/sysctl.h>
#include <net/if.h>
#include <net/if_dl.h>
#include "Reachability.h"
#import "getipaddr.h"

#import "SimulateIDFA.h"
#import "GameUUID.h"

#import "sys/utsname.h"

@interface sysinfo ()
+ (NSString*) getbundle: (NSString*) key;
@end

@implementation sysinfo


+ (NSString*) getbundle: (NSString*) key
{
    NSString* value = [[[NSBundle mainBundle] infoDictionary] objectForKey:key];
    if(value){
        return value;
    }else{
        return @"";
    }
}


//0 no
//1 4g
//2 wifi
+ (int) netstate
{
    int state = 0;
    Reachability * r = [Reachability reachabilityWithHostName : @"www.apple.com"];
    switch ([r currentReachabilityStatus])
    {
        case NotReachable:
            state = 0;
            break;
        case ReachableViaWWAN:
            state = 1;
            break;
        case ReachableViaWiFi:
            state = 2;
            break;
    }
    return state;
}

+ (int) batteryinfo
{
    [UIDevice currentDevice].batteryMonitoringEnabled = YES;
    return 100*[UIDevice currentDevice].batteryLevel;
}

+ (NSString*) imsi
{
    return @"0000";
}

+ (NSString*) imei
{
//    NSString *simulateIDFA = [SimulateIDFA createSimulateIDFA];
//    return simulateIDFA;
    NSString* uuid = [GameUUID uniqueAppId];
    return uuid;
}

+ (NSString*) ipaddress
{
    return [getipaddr ip];
}

+ (NSString*) macaddress
{
    int                 mib[6];
    size_t              len;
    char                *buf;
    unsigned char       *ptr;
    struct if_msghdr    *ifm;
    struct sockaddr_dl  *sdl;
    
    mib[0] = CTL_NET;
    mib[1] = AF_ROUTE;
    mib[2] = 0;
    mib[3] = AF_LINK;
    mib[4] = NET_RT_IFLIST;
    
    if ((mib[5] = if_nametoindex("en0")) == 0) {
        printf("Error: if_nametoindex error\n");
        return NULL;
    }
    
    if (sysctl(mib, 6, NULL, &len, NULL, 0) < 0) {
        printf("Error: sysctl, take 1\n");
        return NULL;
    }
    
    if ((buf = (char*)malloc(len)) == NULL)
    {
        printf("Could not allocate memory. error!\n");
        return NULL;
    }
    
    if (sysctl(mib, 6, buf, &len, NULL, 0) < 0) {
        printf("Error: sysctl, take 2");
        free(buf);
        return NULL;
    }
    
    ifm = (struct if_msghdr *)buf;
    sdl = (struct sockaddr_dl *)(ifm + 1);
    ptr = (unsigned char *)LLADDR(sdl);
    
    char tmp[64] = { 0 };
    sprintf(tmp, "%02X:%02X:%02X:%02X:%02X:%02X", *ptr, *(ptr + 1), *(ptr + 2), *(ptr + 3), *(ptr + 4), *(ptr + 5));
    
    free(buf);
    
    return [NSString stringWithUTF8String:tmp];
    
}

+ (NSString*) mobilemodel
{
    NSString* model = [sysinfo deviceVersion];
    if(model)
    {
        return model;
    }
    return [[UIDevice currentDevice] model];
}

+ (NSString*) systemversion
{
    return [[UIDevice currentDevice] systemVersion];
}

+ (NSString*) packagename
{
    return [sysinfo getbundle:@"CFBundleIdentifier"];
}


+ (NSString*) appname
{
    return [sysinfo getbundle:@"CFBundleDisplayName"];
}

+ (NSString*) appversion
{
    return [sysinfo getbundle:@"CFBundleVersion"];
}

+ (NSString*) country
{
    NSString *countryCode = [[NSLocale currentLocale] objectForKey: NSLocaleCountryCode];
    return countryCode;
}

+ (NSString*) downloadurl
{
    return @"http://www.apple.com";
}

+ (BOOL) isinstall:(NSString*) name
{
    NSURL* url = [NSURL URLWithString:name];//@"weixin://"
    return [[UIApplication sharedApplication] canOpenURL:url];
}

+ (void) openapp:(NSString*) name
{
    //open safari only need a httpurl @"http:/www.bing.cn//"
    NSURL* url = [NSURL URLWithString:name];//@"weixin://"
    if ([[UIApplication sharedApplication] canOpenURL:url]) {
        [[UIApplication sharedApplication] openURL:url];
    }
}


static unsigned long long  us_since_boot() {
    struct timeval boottime;
    int mib[2] = {CTL_KERN, KERN_BOOTTIME};
    size_t size = sizeof(boottime);
    int rc = sysctl(mib, 2, &boottime, &size, NULL, 0);
    if (rc != 0) {
        return 0;
    }
    return boottime.tv_sec;
}

static unsigned long long us_uptime()
{
    unsigned long long before_now;
    unsigned long long after_now;
    struct timeval now;
    
    after_now = us_since_boot();
    do {
        before_now = after_now;
        gettimeofday(&now, NULL);
        after_now = us_since_boot();
    } while (after_now != before_now);
    
    return (now.tv_sec - before_now);
}

+ (unsigned long) elapsedtime
{
    return us_uptime();
}

+ (NSString*) metadata:(NSString*)key
{
    NSString* value = [[[NSBundle mainBundle] infoDictionary] objectForKey:key];
    if(value){
        return value;
    }else{
        return nil;
    }
}

/**
 *  设备版本
 *
 *  @return e.g. iPhone 5S
 */
+ (NSString*)deviceVersion
{
    // 需要#import "sys/utsname.h"
    struct utsname systemInfo;
    uname(&systemInfo);
    NSString *deviceString = [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
    if(nil == deviceString)
    {
        return nil;
    }
    NSDictionary *dic =
    @{
         //iPhone
         @"iPhone1,1" : @"iPhone 1G",
         @"iPhone1,2" : @"iPhone 3G",
         @"iPhone2,1" : @"iPhone 3GS",
         @"iPhone3,1" : @"iPhone 4",
         @"iPhone3,2" : @"Verizon iPhone 4",
         @"iPhone4,1" : @"iPhone 4S",
         @"iPhone5,1" : @"iPhone 5",
         @"iPhone5,2" : @"iPhone 5",
         @"iPhone5,3" : @"iPhone 5C",
         @"iPhone5,4" : @"iPhone 5C",
         @"iPhone6,1" : @"iPhone 5S",
         @"iPhone6,2" : @"iPhone 5S",
         @"iPhone7,1" : @"iPhone 6 Plus",
         @"iPhone7,2" : @"iPhone 6",
         @"iPhone8,1" : @"iPhone 6s",
         @"iPhone8,2" : @"iPhone 6s Plus",
         @"iPhone8,4" : @"iPhone SE",
         @"iPhone9,1" : @"iPhone 7",
         @"iPhone9,3" : @"iPhone 7",
         @"iPhone9,2" : @"iPhone 7 Plus",
         @"iPhone9,4" : @"iPhone 7 Plus",
         @"iPhone10,1" : @"iPhone 8",
         @"iPhone10,4" : @"iPhone 8",
         @"iPhone10,2" : @"iPhone 8 Plus",
         @"iPhone10,5" : @"iPhone 8 Plus",
         @"iPhone10,3" : @"iPhone X",
         @"iPhone10,6" : @"iPhone X",
         
         //iPad
         @"iPad1,1" : @"iPad",
         @"iPad2,1" : @"iPad 2 (WiFi)",
         @"iPad2,2" : @"iPad 2 (GSM)",
         @"iPad2,3" : @"iPad 2 (CDMA)",
         @"iPad2,4" : @"iPad 2 (32nm)",
         @"iPad2,5" : @"iPad mini (WiFi)",
         @"iPad2,6" : @"iPad mini (GSM)",
         @"iPad2,7" : @"iPad mini (CDMA)",
         
         @"iPad3,1" : @"iPad 3(WiFi)",
         @"iPad3,2" : @"iPad 3(CDMA)",
         @"iPad3,3" : @"iPad 3(4G)",
         @"iPad3,4" : @"iPad 4 (WiFi)",
         @"iPad3,5" : @"iPad 4 (4G)",
         @"iPad3,6" : @"iPad 4 (CDMA)",
         
         @"iPad4,1" : @"iPad Air",
         @"iPad4,2" : @"iPad Air",
         @"iPad4,3" : @"iPad Air",
         @"iPad5,3" : @"iPad Air 2",
         @"iPad5,4" : @"iPad Air 2",
         @"iPad6,7" : @"iPad Pro (12.9-inch)",
         @"iPad6,8" : @"iPad Pro (12.9-inch)",
         @"iPad6,3" : @"iPad Pro (9.7-inch)",
         @"iPad6,4" : @"iPad Pro (9.7-inch)",
         
         @"iPad6,11" : @"iPad (5th generation)",
         @"iPad6,12" : @"iPad (5th generation)",
         
         @"iPad7,1" : @"iPad Pro (12.9-inch, 2nd generation)",
         @"iPad7,2" : @"iPad Pro (12.9-inch, 2nd generation)",
         @"iPad7,3" : @"iPad Pro (10.5-inch)",
         @"iPad7,4" : @"iPad Pro (10.5-inch)",
         
         @"i386" : @"Simulator",
         @"x86_64" : @"Simulator",
         
         //iPad min
         @"iPad4,4" : @"iPad mini 2",
         @"iPad4,5" : @"iPad mini 2",
         @"iPad4,6" : @"iPad mini 2",
         @"iPad4,7" : @"iPad mini 3",
         @"iPad4,8" : @"iPad mini 3",
         @"iPad4,9" : @"iPad mini 3",
         
         @"iPad5,1" : @"iPad mini 4",
         @"iPad5,2" : @"iPad mini 4",

    };
    
    for (NSString *key in dic)
    {
        if ([deviceString isEqualToString:key])
        {
            return dic[key];
        }
    }

    return deviceString;
}

@end
