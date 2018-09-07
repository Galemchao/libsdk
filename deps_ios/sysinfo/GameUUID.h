
#ifndef diamond_GameUUID_h
#define diamond_GameUUID_h
#import <Foundation/Foundation.h>

@interface KeychainHelper : NSObject

+ (void) save:(NSString *)service data:(id)data;
+ (id)   load:(NSString *)service;
+ (void) deleteData:(NSString *)service;

@end

@interface GameUUID : NSObject
+ (NSString *)packageNameString;
+ (id)userDefaultValueForKey:(NSString *)key;
+ (void)saveUserDefaultValue:(id)value forKey:(NSString *)key;
+ (NSString *)uniqueAppId;
@end

#endif
