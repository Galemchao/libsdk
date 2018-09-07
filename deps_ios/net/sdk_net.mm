//
//  sdk.m
//  sdk
//
//  Created by Jeep on 16/11/17.
//  Copyright © 2016年 Jeep. All rights reserved.
//
#import "sdk_net.h"
#import "AFNetworking.h"
@implementation sdk (net)

+ (void) http_get:(NSString*) url params:(NSDictionary*) param callback:(void (^)(int,id)) callback
{
    
    NSURL *URL = [NSURL URLWithString:url];
    AFHTTPSessionManager *session = [AFHTTPSessionManager manager];
    [session.securityPolicy setAllowInvalidCertificates:YES];
    NSSet* accpet = [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript",@"text/html",@"text/plain", @"*/*",nil];//-1006
    session.requestSerializer = [AFHTTPRequestSerializer serializer];// 请求//-3480
    session.responseSerializer = [AFHTTPResponseSerializer serializer];// 响应
    [session.responseSerializer setAcceptableContentTypes:accpet];
    [session
        GET:URL.absoluteString
        parameters:param
        progress:^(NSProgress *downloadProgress){
        
        }
        success:^(NSURLSessionDataTask *task, id responseObject) {
            NSLog(@"成功");
            if(callback){
                callback(0,responseObject);
            }
        }
        failure:^(NSURLSessionDataTask *task, NSError *error) {
            NSLog(@"失败=%d",(int)error.code);
            NSLog(@"失败=%@",error);
            if(callback){
                callback(-1,nil);
            }
        }
    ];
}

+ (void) http_post:(NSString*) url params:(NSDictionary*) param callback:(void (^)(int,id)) callback
{
    NSURL *URL = [NSURL URLWithString:url];
    AFHTTPSessionManager *session = [AFHTTPSessionManager manager];
    [session.securityPolicy setAllowInvalidCertificates:YES];
    NSSet* accpet = [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript",@"text/html",@"text/plain", @"*/*",nil];
    session.requestSerializer = [AFHTTPRequestSerializer serializer];// 请求
    session.responseSerializer = [AFHTTPResponseSerializer serializer];// 响应
    [session.responseSerializer setAcceptableContentTypes:accpet];
    [session
     POST:URL.absoluteString
     parameters:param
     progress:^(NSProgress *downloadProgress){
         
     }
     success:^(NSURLSessionDataTask *task, id responseObject) {
         NSLog(@"成功");
         if(callback){
             callback(0,responseObject);
         }
     }
     failure:^(NSURLSessionDataTask *task, NSError *error) {
         NSLog(@"失败=%@",error);
         if(callback){
             callback(-1,nil);
         }
     }
     ];
}

+ (void) http_notify:(int) result data:(id) data
{
    
}

@end
