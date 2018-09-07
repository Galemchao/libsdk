//
//  bdlocate.m
//  bdlocate
//
//  Created by Jeep on 2017/5/13.
//  Copyright © 2017年 Jeep. All rights reserved.
//
//MARK::Location
#import "sdk_amap.h"
#import "amap.h"
#define BMK_KEY @"74c6f5cee48b37e53789e6ccee1f7872"
static bool s_lcoation_init = false;
@implementation sdk (amap)
+ (void) loc_init: (NSDictionary*)dic
{
    if(s_lcoation_init)
    {
        NSLog(@"sdk loc_init already");
        return;
    }
    s_lcoation_init = true;
    NSString* key = [dic valueForKey:TOKEN_AMAP_KEY];
    if(key==nil)
    {
        key = [[[NSBundle mainBundle] infoDictionary] objectForKey:TOKEN_AMAP_KEY];
        NSLog(@"sdk loc_init %@ empty",TOKEN_AMAP_KEY);
    }
    if(key==nil)
    {
        key = BMK_KEY;
        NSLog(@"sdk loc_init bundle:%@ empty",TOKEN_AMAP_KEY);
    }
    [[amap sharedAmap] init:key];
}

+ (void) loc_start
{
    [[amap sharedAmap] stop];
    [[amap sharedAmap] start];
}
+ (void) loc_stop
{
    [[amap sharedAmap] stop];
}
+ (double) loc_get_distance:(double)ajd : (double)awd : (double)bjd :(double)bwd
{
    return [[amap sharedAmap] getDistance:ajd :awd :bjd :bwd];
}
+ (void) loc_notify:(int)error : (double)ajd : (double)awd : (id)result
{
    [[amap sharedAmap] stop];
    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
    [dic setValue:SDK_EVT_LOCATION forKey:SDK_EVT];
    [dic setValue:[NSNumber numberWithInt:error] forKey:SDK_ERROR];
    [dic setValue:[NSNumber numberWithDouble:ajd] forKey:SDK_LOCATION_LONGITUDE];
    [dic setValue:[NSNumber numberWithDouble:awd] forKey:SDK_LOCATION_LATITUDE];
    if(result){
        AMapReGeocode* addr = (AMapReGeocode*)result;
        [dic setValue:[addr formattedAddress] forKey:SDK_LOCATION_ADDRESS];
        [dic setValue:[[addr addressComponent] country] forKey:SDK_LOCATION_ADDRESS_COUNTY];
        [dic setValue:[[addr addressComponent] province] forKey:SDK_LOCATION_ADDRESS_PROVINCE];
        [dic setValue:[[addr addressComponent] city] forKey:SDK_LOCATION_ADDRESS_CITY];
        [dic setValue:[[addr addressComponent] district] forKey:SDK_LOCATION_ADDRESS_DISTRICT];
        [dic setValue:[[[addr addressComponent] streetNumber] street] forKey:SDK_LOCATION_ADDRESS_STREET];
        [dic setValue:[[[addr addressComponent] streetNumber] number] forKey:SDK_LOCATION_ADDRESS_STREETNUMBER];
        [dic setValue:[[addr addressComponent] building] forKey:SDK_LOCATION_ADDRESS_DESCRIBE];
        if ([addr pois].count > 0) {
            [dic setValue:[[addr pois][0] name] forKey:SDK_LOCATION_ADDRESS_DETAIL];
        } else {
            [dic setValue:@"" forKey:SDK_LOCATION_ADDRESS_DETAIL];
        }
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


