//
//  amap.h
//  myGame-mobile
//
//  Created by jake on 2018/7/3.
//

#import <AMapFoundationKit/AMapFoundationKit.h>
#import <AMapSearchKit/AMapSearchKit.h>
#import <AMapLocationKit/AMapLocationKit.h>
#import <MAMapKit/MAGeometry.h>

@interface amap : NSObject <AMapSearchDelegate, AMapLocationManagerDelegate>
{
    bool _isruning;
    bool _isinit;
}
/// 位置信息，尚未定位成功，则该值为nil
@property (retain,nonatomic,strong) CLLocation *location;
@property (strong, nonatomic) AMapLocationManager *locationManager;
@property (strong, nonatomic) AMapSearchAPI *search;

- (instancetype) init;
- (void) init :(NSString*)key;
- (void) start;
- (void) stop;
- (void) dealloc;
- (double) getDistance:(double)ajd : (double)awd : (double)bjd :(double)bwd;
+ (amap*) sharedAmap;

@end
