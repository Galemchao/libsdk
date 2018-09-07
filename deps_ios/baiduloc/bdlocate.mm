//
//  bdlocate.m
//  bdlocate
//
//  Created by Jeep on 2017/5/13.
//  Copyright © 2017年 Jeep. All rights reserved.
//
//MARK::Location

#import "sdk_baiduloc.h"
#import "bdlocate.h"

@implementation bdlocate
+ (bdlocate*) sharedBdLocate
{
    static bdlocate* s_loc = nil;
    if(!s_loc)
    {
        s_loc = [[bdlocate alloc] init];
    }
    return s_loc;
    
}

+ (double) getDistance: (double)ajd :(double)awd :(double)bjd :(double)bwd
{
    BMKMapPoint point1 = BMKMapPointForCoordinate(CLLocationCoordinate2DMake(awd,ajd));
    BMKMapPoint point2 = BMKMapPointForCoordinate(CLLocationCoordinate2DMake(bwd,bjd));
    CLLocationDistance distance = BMKMetersBetweenMapPoints(point1,point2);
    return distance;
}
- (id) init
{
    self = [super init];
    _isinit = NO;
    _network = NO;
    _permission = NO;
    _isruning = NO;
    
    _mapmng = nil;
    _searcher = nil;
    _locService = nil;
    
    self.location = nil;
    self.heading = nil;
    self.title = nil;
    self.subtitle = nil;
    self.address = nil;
    return self;
}

- (void) init :(NSString*)key
{
    _mapmng = [[BMKMapManager alloc] init];
    [_mapmng start:key generalDelegate:self];
    
    _searcher = [[BMKGeoCodeSearch alloc]init];
    _searcher.delegate = self;
    
    _locService = [[BMKLocationService alloc]init];
    _locService.delegate = self;
    
    
    _network = NO;
    _permission = NO;
    _isruning = NO;

    
    self.location = nil;
    self.heading = nil;
    self.title = nil;
    self.subtitle = nil;
    self.address = nil;
    
    _isinit = YES;
    
}
- (void) start
{
    if(!_isinit)
    {
        [sdk loc_notify:-1 :0 :0 :nil];
        return;
    }
    _isruning = YES;
    [_locService startUserLocationService];
}
- (void) stop
{
    if(!_isinit) return;
    _isruning = NO;
    [_locService stopUserLocationService];
}


- (Boolean) searchReverse: (double)jd :(double)wd;
{
    if(!_isinit) return NO;
    CLLocationCoordinate2D pt = (CLLocationCoordinate2D){wd, jd};
    BMKReverseGeoCodeOption *reverseGeoCodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
    reverseGeoCodeSearchOption.reverseGeoPoint = pt;
    BOOL flag = [_searcher reverseGeoCode:reverseGeoCodeSearchOption];
    [reverseGeoCodeSearchOption release];
    if(flag)
    {	
        NSLog(@"反geo检索发送成功");
        return YES;
    }
    else
    {
        NSLog(@"反geo检索发送失败");
        return NO;
    }
}

- (Boolean) searchAddress: (double)jd :(double)wd;
{
    if(!_isinit) return NO;
    if(!(_network && _permission))
    {
        NSLog(@"state no ok");
        return NO;
    }
    return [self searchReverse:jd:wd];
}

- (NSString*) getMyAdress
{
    if([self address])
    {
        return [[self address] address];
    }
    return nil;
}

/**
 *在地图View将要启动定位时，会调用此函数
 */
- (void)willStartLocatingUser
{
    NSLog(@"start locate");
}

/**
 *用户方向更新后，会调用此函数
 *@param userLocation 新的用户位置
 */
- (void)didUpdateUserHeading:(BMKUserLocation *)userLocation
{
    NSLog(@"heading is %@",userLocation.heading);
}

/**
 *用户位置更新后，会调用此函数
 *@param userLocation 新的用户位置
 */
- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation
{
    if(!_isinit) return;
    if(!_isruning)
    {
        return;
    }
    if(userLocation.location)
    {
        self.location   =   userLocation.location;
        self.heading    =   userLocation.heading;
        self.title      =   userLocation.title;
        self.subtitle   =   userLocation.subtitle;
        Boolean ret = [self searchAddress: self.location.coordinate.longitude : self.location.coordinate.latitude];
        if(!ret)
        {
            [sdk loc_notify:1 :0 :0 :nil];
        }
    }
    else
    {
        [sdk loc_notify:2 :0 :0 :nil];
    }

}

/**
 *在地图View停止定位后，会调用此函数
 */
- (void)didStopLocatingUser
{
    NSLog(@"stop locate");
}

/**
 *定位失败后，会调用此函数
 *@param error 错误号，参考CLError.h中定义的错误号
 */
- (void)didFailToLocateUserWithError:(NSError *)error
{
    [sdk loc_notify:4 :0 :0 :nil];
    NSLog(@"location error");
}

//实现Deleage处理回调结果
//接收正向编码结果
- (void)onGetGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error{
    if (error == BMK_SEARCH_NO_ERROR) {
        NSLog(@"geo search，true");
    }
    else {
        NSLog(@"geo search，fail");
    }
}

/**
 接收反向地理编码结果

 @return
 */
-(void) onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:
(BMKReverseGeoCodeResult *)result
                        errorCode:(BMKSearchErrorCode)error{
    
    if(!_isruning)
    {
        return;
    }
    if (error == BMK_SEARCH_NO_ERROR) {
        [self setAddress:result];
        auto location = [result location];
        double jd = location.longitude;
        double wd = location.latitude;
        NSString* addr = [result address];
        [sdk loc_notify:0 :jd :wd :result];
        NSLog(@"geo reverse search，true");
    }
    else {
        [sdk loc_notify:3 :0 : 0 :nil];
        NSLog(@"geo reverse search，false");
    }

}

- (void)onGetNetworkState:(int)iError
{
    _network = iError==0;
    NSLog(@"map network state %d",iError);
}

/**
 *返回授权验证错误
 *@param iError 错误号 : 为0时验证通过，具体参加BMKPermissionCheckResultCode
 */
- (void)onGetPermissionState:(int)iError
{
    _permission = iError==0;
    NSLog(@"map permission state %d",iError);
}


- (void)dealloc {
    if (_mapmng) {
        [_mapmng dealloc];
    }
    if (_locService) {
        [_locService dealloc];
    }
    if (_searcher) {
        [_searcher dealloc];
    }
    self.location = nil;
    self.heading = nil;
    self.title = nil;
    self.subtitle = nil;
    [super dealloc];
}
@end
