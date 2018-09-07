//
//  bdlocate.h
//  bdlocate
//
//  Created by Jeep on 2017/5/13.
//  Copyright © 2017年 Jeep. All rights reserved.
//
//MARK::Location
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <BaiduMapAPI_Base/BMKMapManager.h>
#import <BaiduMapAPI_Map/BMKMapComponent.h>
#import <BaiduMapAPI_Location/BMKLocationComponent.h>
#import <BaiduMapAPI_Utils/BMKUtilsComponent.h>
#import <BaiduMapAPI_Search/BMKGeocodeSearch.h>

@interface bdlocate : NSObject <BMKGeneralDelegate,BMKMapViewDelegate,BMKLocationServiceDelegate,BMKGeoCodeSearchDelegate>{
    BMKMapManager* _mapmng;
    BMKLocationService* _locService;
    BMKGeoCodeSearch* _searcher;
    
    //网络状态和权限,ok以后才能发送地址查询成功
    bool _network;
    bool _permission;
    bool _isruning;
    bool _isinit;
}
/// 位置信息，尚未定位成功，则该值为nil
@property (retain,nonatomic,strong) CLLocation *location;

/// heading信息，尚未定位成功，则该值为nil
@property (retain,nonatomic, strong) CLHeading *heading;

/// 定位标注点要显示的标题信息
@property (retain,strong, nonatomic) NSString *title;

/// 定位标注点要显示的子标题信息.
@property (copy, nonatomic) NSString *subtitle;

/// 定位标注点要显示的子标题信息.
@property (strong, nonatomic) BMKReverseGeoCodeResult *address;

- (instancetype) init;
- (void) init :(NSString*)key;
- (void) start;
- (void) stop;
- (void) dealloc;
+ (bdlocate*) sharedBdLocate;
+ (double) getDistance: (double)ajd :(double)awd :(double)bjd :(double)bwd;
- (Boolean) searchReverse: (double)jd :(double)wd;
- (Boolean) searchAddress: (double)jd :(double)wd;
- (NSString*) getMyAdress;

@end
