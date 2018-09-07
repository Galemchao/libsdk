//
//  sdk.h
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "sdk.h"
@interface sdk (net)
+ (void) http_get:(NSString*) url params:(NSDictionary*) param callback:(void (^)(int,id)) callback;
+ (void) http_post:(NSString*) url params:(NSDictionary*) param callback:(void (^)(int,id)) callback;
+ (void) http_notify:(int) result data:(id) data;

@end
