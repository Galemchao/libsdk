//
//  bdlocate.m
//  bdlocate
//
//  Created by Jeep on 2017/5/13.
//  Copyright © 2017年 Jeep. All rights reserved.
//
//MARK::Location
#include "sdk.h"
#include "sdk_baiduloc.h"
#import "bdlocate.h"
#define BMK_KEY @"XWjj1bdRo0M7xgB4XewAdWTH8a9pGX2G"
static bool s_lcoation_init = false;
@implementation sdk (um)
+ (void) loc_init: (NSDictionary*)dic
{
    if(s_lcoation_init)
    {
        NSLog(@"sdk loc_init already");
        return;
    }
    s_lcoation_init = true;
    NSString* key = [dic valueForKey:TOKEN_BD_LOCKEY];
    if(key==nil)
    {
        key = [[[NSBundle mainBundle] infoDictionary] objectForKey:TOKEN_BD_LOCKEY];
        NSLog(@"sdk loc_init %@ empty",TOKEN_BD_LOCKEY);
    }
    if(key==nil)
    {
        key = BMK_KEY;
        NSLog(@"sdk loc_init bundle:%@ empty",TOKEN_BD_LOCKEY);
    }
    [[bdlocate sharedBdLocate] init:key];
}

+ (void) loc_start
{
    [[bdlocate sharedBdLocate] stop];
    [[bdlocate sharedBdLocate] start];
}
+ (void) loc_stop
{
    [[bdlocate sharedBdLocate] stop];
}
+ (double) loc_get_distance:(double)ajd : (double)awd : (double)bjd :(double)bwd
{
    return [bdlocate getDistance:ajd :awd :bjd :bwd];
}
+ (void) loc_notify:(int)error : (double)ajd : (double)awd : (id)result
{
    [[bdlocate sharedBdLocate] stop];
    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
    [dic setValue:SDK_EVT_LOCATION forKey:SDK_EVT];
    [dic setValue:[NSNumber numberWithInt:error] forKey:SDK_ERROR];
    [dic setValue:[NSNumber numberWithDouble:ajd] forKey:SDK_LOCATION_LONGITUDE];
    [dic setValue:[NSNumber numberWithDouble:awd] forKey:SDK_LOCATION_LATITUDE];
    if(result){
        BMKReverseGeoCodeResult* addr = (BMKReverseGeoCodeResult*)result;
        [dic setValue:[addr address] forKey:SDK_LOCATION_ADDRESS];
        [dic setValue:[[addr addressDetail] country] forKey:SDK_LOCATION_ADDRESS_COUNTY];
        [dic setValue:[[addr addressDetail] province] forKey:SDK_LOCATION_ADDRESS_PROVINCE];
        [dic setValue:[[addr addressDetail] city] forKey:SDK_LOCATION_ADDRESS_CITY];
        [dic setValue:[[addr addressDetail] district] forKey:SDK_LOCATION_ADDRESS_DISTRICT];
        [dic setValue:[[addr addressDetail] streetName] forKey:SDK_LOCATION_ADDRESS_STREET];
        [dic setValue:[[addr addressDetail] streetNumber] forKey:SDK_LOCATION_ADDRESS_STREETNUMBER];
        [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS_DETAIL];
        [dic setValue:[addr sematicDescription] forKey:SDK_LOCATION_ADDRESS_DESCRIBE];
    }
    else{
        [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS];
        [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS_COUNTY];
        [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS_PROVINCE];
        [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS_CITY];
        [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS_DISTRICT];
        [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS_STREET];
        [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS_STREETNUMBER];
        [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS_DETAIL];
        [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS_DESCRIBE];
    }
    [sdk notifyEventByObject:dic];
}

@end


