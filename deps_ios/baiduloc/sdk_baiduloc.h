
#ifndef __sdk_baidiuloc_H__
#define __sdk_baidiuloc_H__
//MARK::Location
#import "sdk.h"
@interface sdk (baiduloc)
+ (void) loc_init: (NSDictionary*)dic;
+ (void) loc_start;
+ (void) loc_stop;
+ (double) loc_get_distance:(double)ajd : (double)awd : (double)bjd :(double)bwd;
+ (void) loc_notify:(int)error : (double)ajd : (double)awd : (id)addr;
@end
#endif
