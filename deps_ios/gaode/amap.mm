//
//  amap.m
//  myGame-mobile
//
//  Created by yange on 2018/7/3.
//

#import <Foundation/Foundation.h>
#import "amap.h"
#import "sdk_amap.h"

@implementation amap

+ (amap*) sharedAmap
{
    static amap* s_loc = nil;
    if(!s_loc)
    {
        s_loc = [[amap alloc] init];
    }
    return s_loc;
}

- (id) init
{
    self = [super init];
    _isinit = NO;
    _isruning = NO;
    
    self.location = nil;
    self.locationManager = nil;
    self.search = nil;
    return self;
}

- (void) init :(NSString*)key
{
    [AMapServices sharedServices].enableHTTPS = YES;
    [AMapServices sharedServices].apiKey = key;
    self.locationManager = [[AMapLocationManager alloc] init];
    [self.locationManager setDelegate:self];
    //设置定位最小更新距离方法如下，单位米。当两次定位距离满足设置的最小更新距离时，SDK会返回符合要求的定位结果。
    self.locationManager.distanceFilter = 200;
    
    self.search = [[AMapSearchAPI alloc] init];
    self.search.delegate = self;
    
    _isinit = YES;
}

- (void) start
{
    if(!_isinit)
    {
        [sdk loc_notify:-1 :0 :0 :nil];
        NSLog(@"高德地图没有初始化");
        return;
    }
    if (_isruning) {
        return;
    }
    _isruning = YES;
    [self startSerialLocation];
}

- (void) stop
{
    if(!_isinit) return;
    _isruning = NO;
    [self stopSerialLocation];
}

- (double) getDistance:(double)ajd : (double)awd : (double)bjd :(double)bwd
{
    MAMapPoint point1 = MAMapPointForCoordinate(CLLocationCoordinate2DMake(awd, ajd));
    MAMapPoint point2 = MAMapPointForCoordinate(CLLocationCoordinate2DMake(bwd, bjd));
    //2.计算距离
    CLLocationDistance distance = MAMetersBetweenMapPoints(point1, point2);
    return (double)distance;
}


- (void)startSerialLocation
{
    //开始定位
//    [self.locationManager setLocatingWithReGeocode:YES];
    [self.locationManager startUpdatingLocation];
}

- (void)stopSerialLocation
{
    //停止定位
    [self.locationManager stopUpdatingLocation];
}

- (void)amapLocationManager:(AMapLocationManager *)manager didFailWithError:(NSError *)error
{
    //定位错误
    _isruning = NO;
    NSLog(@"%s, amapLocationManager = %@, error = %@", __func__, [manager class], error);
    [sdk loc_notify:-2 :0 :0 :nil];
}

//- (void)amapLocationManager:(AMapLocationManager *)manager didUpdateLocation:(CLLocation *)location reGeocode:(AMapLocationReGeocode *)reGeocode
//{
//    NSLog(@"location:{lat:%f; lon:%f; accuracy:%f}", location.coordinate.latitude, location.coordinate.longitude, location.horizontalAccuracy);
//    if (reGeocode)
//    {
//        NSLog(@"reGeocode:%@", reGeocode);
//        [sdk loc_notify:0 :location.coordinate.latitude :location.coordinate.longitude :reGeocode];
//    } else {
//        self.location = location;
//        [self ReGoecodeSearch];
//    }
//}

- (void)ReGoecodeSearch
{
    AMapReGeocodeSearchRequest *regeo = [[AMapReGeocodeSearchRequest alloc] init];
    regeo.location                    = [AMapGeoPoint locationWithLatitude:self.location.coordinate.latitude longitude:self.location.coordinate.longitude];
    regeo.requireExtension            = YES;
    [self.search AMapReGoecodeSearch:regeo];
}

/* 逆地理编码回调. */
- (void)onReGeocodeSearchDone:(AMapReGeocodeSearchRequest *)request response:(AMapReGeocodeSearchResponse *)response
{
    if (!_isruning) {
        return;
    }
    if (response.regeocode != nil)
    {
        //解析response获取地址描述，具体解析见 Demo
        NSLog(@"reGeocode:%@", response.regeocode);
        [sdk loc_notify:0 :self.location .coordinate.latitude :self.location .coordinate.longitude :response.regeocode];
        [self stop];
    }
}

- (void)amapLocationManager:(AMapLocationManager *)manager didUpdateLocation:(CLLocation *)location
{
    if (!_isruning) {
        return;
    }
    //定位结果
    NSLog(@"location:{lat:%f; lon:%f; accuracy:%f}", location.coordinate.latitude, location.coordinate.longitude, location.horizontalAccuracy);
    self.location = location;
    [self ReGoecodeSearch];
}

- (void) dealloc
{
    if (self.locationManager) {
        [self.locationManager dealloc];
        self.locationManager = nil;
    }

    [super dealloc];
}

@end
