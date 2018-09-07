//
//  sdkControl.h
//  sdkControl
//
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "sdkdef.h"
@interface sdkControl : NSObject

+ (void) init;
+ (void) initHandle:(int) handle;
+ (void) setEventHandler:(int) handle;
+ (void) setSchemeUrl:(NSString*) url;
+ (NSString*) getSchemeUrl;
+ (void) initConfig:(NSString*) data;
+ (void) login:(NSString*) params;
+ (void) pay:(NSString*) params;
+ (void) openBrowser:(NSString*)url;
+ (void) openWebView:(NSString*)url;
+ (void) share:(NSString*)params;
+ (BOOL) copyToClipboard:(NSString*) str;
+ (BOOL) initRecord:(NSString*) data;
+ (BOOL) startRecord:(NSString*) data;
+ (void) stopRecord;
+ (int) recordGetVolume;
+ (double) getAudioDuration:(NSString*) filename;
+ (void) initLocate;
+ (void) startLocate;
+ (void) stopLocate;
+ (double) getDistance:(double)alongitude :(double)alatitude :(double)blongitude :(double)blatitude;
+ (void) saveImageAlbum:(NSString*) str;
+ (void) pickImg:(NSString*) filename;
//+ (NSString*) getChannel;
//+ (NSString*) getExternInfo;
+ (NSString*) getVersionName;
+ (BOOL) vibrate:(long)milliseconds;
+ (NSString*) getIpAddress;
+ (NSString*) getUUID;
+ (NSString*) getDeviceName;
+ (NSString*) getMetaData:(NSString*)key;
+ (BOOL) isinstall:(NSString*) name;
//pay init
+ (void) iosPayInit:(id) dic;
+ (void) iosPayReq:(NSString*)pid :(NSString*)customid :(BOOL)force;
+ (void) iosPayStop;
//-------------------------
+ (BOOL) handleUrl:(NSURL*)url;
+ (BOOL) handleUrl:(NSURL*)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options;
+ (BOOL) handleUrl:(NSURL*)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation;

@end
