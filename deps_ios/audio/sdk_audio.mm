//
//  sdk_audio.mm
//  pdk
//
//  Created by Jeep on 16/12/15.
//
//

#import "sdk_audio.h"
#import "sdk.h"
#import<AVFoundation/AVFoundation.h>
#import "lame.h"




@interface sdk_audio()



- (BOOL)pcm_to_mp3:(NSString *)soucePath andDesPath:(NSString *)desPath;

@end



@implementation sdk_audio

+ (sdk_audio *)sharedSdkAudio
{
    static sdk_audio *instance = nil;
    static dispatch_once_t predicate;
    dispatch_once(&predicate, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

- (instancetype) init
{
    [super init];
    recordDuration = 0;
    recordResult = 0;
    convertSt = 0;
    limitDuration = 60.0;
    return self;
}

- (BOOL) init_record:(id)dic
{
    id key = [dic valueForKey:TOKEN_RECORD_DURATION_LIMIT];
    if(key!=nil && [key isKindOfClass:[NSNumber class]])
    {
        NSNumber* duration = key;
        limitDuration = [duration doubleValue];
        if(limitDuration<1.0)
        {
            limitDuration = 1.0;
        }
    }
    else
    {
        NSLog(@"sdk init_record %@ empty",TOKEN_RECORD_DURATION_LIMIT);
    }
    return YES;
}

- (BOOL) start_record:(NSString*)filename
{
    if(mRecordFileName){
        [mRecordFileName dealloc];
        mRecordFileName = nil;
    }
    mRecordFileName = [[NSString alloc] initWithString:filename];
    
    if(s_audiorec){
        if([s_audiorec isRecording]){
            return false;
        }
        if(convertSt>0){
            return false;
        }
        [s_audiorec dealloc];
        s_audiorec=nil;
    }
    
    if(!s_audio_session){
        NSError*errorSession =nil;
        s_audio_session = [AVAudioSession sharedInstance];//得到AVAudioSession单例对象
        [s_audio_session setCategory:AVAudioSessionCategoryPlayAndRecord error: &errorSession];//设置类别,表示该应用同时支持播放和录音
        [s_audio_session setActive:YES error: &errorSession];//启动音频会话管理,此时会阻断后台音乐的播放.
        
        [s_audio_session overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:nil];
    }
    
    NSError* error;
    NSDictionary* setting =@{
                             
                             AVFormatIDKey:@(kAudioFormatLinearPCM),//音频格式
                             
                             AVSampleRateKey:@44100.0f,//录音采样率(Hz)如：AVSampleRateKey==8000/44100/96000（影响音频的质量）
                             
                             AVNumberOfChannelsKey:@2,//音频通道数1或2
                             
                             AVEncoderBitDepthHintKey:@16,//线性音频的位深度8、16、24、32
                             
                             AVEncoderAudioQualityKey:@(AVAudioQualityLow)//录音的质量
                             
                             };
    
    NSString* path = [mRecordFileName stringByDeletingLastPathComponent];
    
    NSLog(@"path: %@", path);
    
    NSFileManager *manager = [NSFileManager defaultManager];
    
    if (![manager fileExistsAtPath:path]) {
        [manager createDirectoryAtPath:path
           withIntermediateDirectories:YES
                            attributes:nil
                                 error:nil];
    }
    
    NSString* baseName =[[mRecordFileName stringByDeletingPathExtension] lastPathComponent];
    NSLog(@"baseName: %@", baseName);
    
    mRecordCafFileName =[[path stringByDeletingPathExtension] stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.caf",baseName]];
    NSLog(@"pathcaf: %@", mRecordCafFileName);
    
    if ([manager fileExistsAtPath:mRecordCafFileName]) {
        [manager removeItemAtPath:mRecordCafFileName error:nil];
    }
    
    if ([manager fileExistsAtPath:mRecordFileName]) {
        [manager removeItemAtPath:mRecordFileName error:nil];
    }
    
    NSURL* url = [NSURL URLWithString:mRecordCafFileName];
    s_audiorec = [[AVAudioRecorder alloc] initWithURL:url settings:setting error:&error];
    if(!s_audiorec)
    {
        NSLog(@"Error: %@", [error localizedDescription]);
        return false;
    }
    
    [s_audiorec prepareToRecord];
    s_audiorec.delegate = self;
    s_audiorec.meteringEnabled=YES;
    
    //创建一个音频文件，并准备系统进行录制
    BOOL result = [s_audiorec record];
    if(!result){
        return false;
    }
    
    recordResult = 0;
    
    [self start_convert];
    
    return true;
}

- (void) stop_record
{
    if(s_audiorec)
    {
        if([s_audiorec isRecording]){
            recordDuration = [s_audiorec currentTime];
            NSLog(@"停止录音: %f",recordDuration);
            [s_audiorec stop];
        }
    }
}

/* 该方法确实会随环境音量变化而变化，但具体分贝值是否准确暂时没有研究 */
- (int)record_getVolume
{
    [s_audiorec updateMeters];
    
    float   level;                // The linear 0.0 .. 1.0 value we need.
    float   minDecibels = -80.0f; // Or use -60dB, which I measured in a silent room.
    float   decibels    = [s_audiorec averagePowerForChannel:0];
    
    if (decibels < minDecibels)
    {
        level = 0.0f;
    }
    else if (decibels >= 0.0f)
    {
        level = 1.0f;
    }
    else
    {
        float   root            = 2.0f;
        float   minAmp          = powf(10.0f, 0.05f * minDecibels);
        float   inverseAmpRange = 1.0f / (1.0f - minAmp);
        float   amp             = powf(10.0f, 0.05f * decibels);
        float   adjAmp          = (amp - minAmp) * inverseAmpRange;
        
        level = powf(adjAmp, 1.0f / root);
    }
    
    return level*2000;
}

- (double)record_getDuration:(NSString*)filename
{
    NSString* fullFilePath = [[NSString alloc] initWithString:filename];
    NSURL *pathToMp3File = [NSURL URLWithString:fullFilePath];
    NSError *error = nil;
    AVAudioPlayer *avAudioPlayer = [[AVAudioPlayer alloc]initWithContentsOfURL:pathToMp3File error:&error];
    double duration = avAudioPlayer.duration;
    avAudioPlayer = nil;
    NSLog(@"MP3文件时长:%f", duration);
    return duration;
}

- (BOOL)pcm_to_mp3:(NSString *)soucePath andDesPath:(NSString *)desPath
{
    NSLog(@"开始转换");
    @try {
        int read, write;
        
        FILE *pcm = fopen([soucePath cStringUsingEncoding:1],  "rb"); // source 被转换的音频文件位置
        fseek(pcm, 4 * 1024, SEEK_CUR); // skip file header
        FILE *mp3 = fopen([desPath cStringUsingEncoding:1], "wb"); // output 输出生成的Mp3文件位置
        
        const int PCM_SIZE = 8192;
        const int MP3_SIZE = 8192;
        short int pcm_buffer[PCM_SIZE * 2];
        unsigned char mp3_buffer[MP3_SIZE];
        
        lame_t lame = lame_init();
        lame_set_in_samplerate(lame, 44100);
//        lame_set_out_samplerate(lame, 44100);
//        lame_set_num_channels(lame, 1);
        lame_set_brate(lame, 32);
        lame_set_quality(lame,7);
        
        lame_set_VBR(lame, vbr_default);
        lame_init_params(lame);
        
        do {
            read = fread(pcm_buffer, 2 * sizeof(short int), PCM_SIZE, pcm);
            if (read == 0)
                write = lame_encode_flush(lame, mp3_buffer, MP3_SIZE);
            else
                write = lame_encode_buffer_interleaved(lame, pcm_buffer, read, mp3_buffer, MP3_SIZE);
            
            fwrite(mp3_buffer, write, 1, mp3);
            
        } while (read != 0);
        
        lame_close(lame);
        fclose(mp3);
        fclose(pcm);
    } @catch (NSException *exception) {
        NSLog(@"%@", [exception description]);
        return NO;
    } @finally {
        NSLog(@"MP3生成成功");
    }
    return YES;
}

//- (void)audioRecorderDidFinishRecording:(AVAudioRecorder *)recorder successfully:(BOOL)flag
//{
//    NSLog(@"录音完成 finish: %d", flag);
//
//    dispatch_async(dispatch_get_global_queue(0, 0),^{
//
//        //进入另一个线程
//        // sleep(time+1);
//        NSLog(@"录音完成 开始转换为mp3文件: %@", mRecordFileName);
//
//        NSFileManager *manager = [NSFileManager defaultManager];
//        if ([manager fileExistsAtPath:mRecordCafFileName]) {
//            [self pcm_to_mp3:mRecordCafFileName andDesPath:mRecordFileName];
//        }
//
//        dispatch_async(dispatch_get_main_queue(),^{
//            //返回主线程
//            NSNumber* error=[NSNumber numberWithInt:1];
//            if(flag){
//                NSFileManager *manager = [NSFileManager defaultManager];
//                if ([manager fileExistsAtPath:mRecordFileName]) {
//                    error=[NSNumber numberWithInt:0];
//                }
//            }
//
//            NSLog(@"录音完成 发送stop通知");
//            NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
//            [dic setValue:SDK_EVT_RECORD forKey:SDK_EVT];
//            [dic setValue:error forKey:SDK_ERROR];
//            [dic setValue:@"stoped" forKey:SDK_RECORD_STATE];
//            [dic setValue:mRecordFileName forKey:SDK_RECORD_FILENAME];
//            [dic setValue:[NSNumber numberWithDouble:recordDuration] forKey:SDK_RECORD_DURATION];
//
//
//            [sdk notifyEventByObject:dic];
//
//        });
//    });
//
//}
//
///* if an error occurs while encoding it will be reported to the delegate. */
//- (void)audioRecorderEncodeErrorDidOccur:(AVAudioRecorder *)recorder error:(NSError * __nullable)error
//{
//    NSLog(@"录音出错 encode error: %@", error);
//
//    NSNumber* ret_error= [NSNumber numberWithInt:error.code];
//    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
//    [dic setValue:SDK_EVT_RECORD forKey:SDK_EVT];
//    [dic setValue:ret_error forKey:SDK_ERROR];
//    [dic setValue:@"stoped" forKey:SDK_RECORD_STATE];
//    [dic setValue:mRecordFileName forKey:SDK_RECORD_FILENAME];
//    [dic setValue:[NSNumber numberWithDouble:0] forKey:SDK_RECORD_DURATION];
//
//    [sdk notifyEventByObject:dic];
//
//}


- (void)start_convert
{
    NSLog(@"开始转换线程");
    convertSt = 1;
    dispatch_async(dispatch_get_global_queue(0, 0),^{
        
        //进入另一个线程
        // sleep(time+1);
        NSLog(@"开始转换为mp3文件: %@", mRecordFileName);
        
        lame_t lame = lame_init();
        lame_set_in_samplerate(lame, 44100.0);
//        lame_set_out_samplerate(lame, 44100);
//        lame_set_num_channels(lame, 1);
        lame_set_brate(lame, 32);
        lame_set_quality(lame,7);
        
        lame_set_VBR(lame, vbr_default);
        lame_init_params(lame);
        
        FILE *pcm = fopen([mRecordCafFileName cStringUsingEncoding:1],  "rb"); // source 被转换的音频文件位置
        FILE *mp3 = fopen([mRecordFileName cStringUsingEncoding:1], "wb"); // output 输出生成的Mp3文件位置
        
        bool flag = pcm && mp3;
        
        if(!flag)
        {
            NSLog(@"转换失败，打开文件失败");
            dispatch_async(dispatch_get_main_queue(),^{
                //返回主线程
                [self stop_record];
                if(recordResult>=0)
                {
                    NSNumber* error=[NSNumber numberWithInt:1];
                    NSLog(@"转换失败 发送stop通知");
                    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
                    [dic setValue:SDK_EVT_RECORD forKey:SDK_EVT];
                    [dic setValue:error forKey:SDK_ERROR];
                    [dic setValue:@"stoped" forKey:SDK_RECORD_STATE];
                    [dic setValue:mRecordFileName forKey:SDK_RECORD_FILENAME];
                    [dic setValue:[NSNumber numberWithDouble:recordDuration] forKey:SDK_RECORD_DURATION];
                    [sdk notifyEventByObject:dic];
                }
                
            });
            convertSt = 0;
            return;
        }
        
        long skip = 0;
        const int PCM_HEAD_SKIP = 4 * 1024;
        const int PCM_SIZE = 8192;
        const int MP3_SIZE = 8192;
        short int pcm_buffer[PCM_SIZE * 2];
        unsigned char mp3_buffer[MP3_SIZE];
        while(true)
        {
            bool isstop = true;
            if([s_audiorec isRecording])
            {
                isstop = false;
                recordDuration = [s_audiorec currentTime];
                if(recordDuration>limitDuration)
                {
                    NSLog(@"录音超时 停止录音");
                    [self stop_record];
                    isstop = true;
                }
            }
            
            int read, write;
            fseek(pcm,0,SEEK_END);
            long length = ftell(pcm);
            if(length<=PCM_HEAD_SKIP)
            {
                if(isstop)
                {
                    break;
                }
                [NSThread sleepForTimeInterval:0.02f];
                continue;
            }
            if(skip==0)
            {
                skip = PCM_HEAD_SKIP;
            }
            if(length-skip < PCM_SIZE * 2 * sizeof(short int))
            {
                if(!isstop)
                {
                    [NSThread sleepForTimeInterval:0.05f];
                    continue;
                }
            }
            fseek(pcm,skip,SEEK_SET);
            read = (int)fread(pcm_buffer, 2 * sizeof(short int), PCM_SIZE, pcm);
            if(read>0)
            {
                write = lame_encode_buffer_interleaved(lame, pcm_buffer, read, mp3_buffer, MP3_SIZE);
                fwrite(mp3_buffer, write, 1, mp3);
                skip = ftell(pcm);
            }
            else if(isstop)
            {
               break;
            }
            
        }
        
        if(mp3)
        {
            int write = lame_encode_flush(lame, mp3_buffer, MP3_SIZE);
            fwrite(mp3_buffer, write, 1, mp3);
            fclose(mp3);
        }
        if(pcm)
        {
            fclose(pcm);
        }
        lame_close(lame);
        
        NSLog(@"结束转换");
        dispatch_async(dispatch_get_main_queue(),^{
            //返回主线程
            if(recordResult>=0)
            {
                NSNumber* error=[NSNumber numberWithInt:1];
                if(recordResult>=0){
                    NSFileManager *manager = [NSFileManager defaultManager];
                    if ([manager fileExistsAtPath:mRecordFileName]) {
                        error=[NSNumber numberWithInt:recordResult];
                    }
                }
                NSLog(@"转换完成 发送stop通知");
                NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
                [dic setValue:SDK_EVT_RECORD forKey:SDK_EVT];
                [dic setValue:error forKey:SDK_ERROR];
                [dic setValue:@"stoped" forKey:SDK_RECORD_STATE];
                [dic setValue:mRecordFileName forKey:SDK_RECORD_FILENAME];
                [dic setValue:[NSNumber numberWithDouble:recordDuration] forKey:SDK_RECORD_DURATION];
                [sdk notifyEventByObject:dic];
            }
            
            convertSt = 0;
            
        });
    });
    
}
- (void)audioRecorderDidFinishRecording:(AVAudioRecorder *)recorder successfully:(BOOL)flag
{
    NSLog(@"录音完成 finish: %d", flag);
    
    if(flag)
    {
        recordResult = 0;
    }
    else
    {
        recordResult = 1;
    }
    
}

/* if an error occurs while encoding it will be reported to the delegate. */
- (void)audioRecorderEncodeErrorDidOccur:(AVAudioRecorder *)recorder error:(NSError * __nullable)error
{
    NSLog(@"录音出错 encode error: %@", error);
    recordResult = -1;
    NSNumber* ret_error= [NSNumber numberWithInt:error.code];
    NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithCapacity:10];
    [dic setValue:SDK_EVT_RECORD forKey:SDK_EVT];
    [dic setValue:ret_error forKey:SDK_ERROR];
    [dic setValue:@"stoped" forKey:SDK_RECORD_STATE];
    [dic setValue:mRecordFileName forKey:SDK_RECORD_FILENAME];
    [dic setValue:[NSNumber numberWithDouble:0] forKey:SDK_RECORD_DURATION];
    
    [sdk notifyEventByObject:dic];
    
}

- (void)dealloc
{
    if(mRecordCafFileName){
        [mRecordCafFileName dealloc];
        mRecordCafFileName = nil;
    }
    
    if(s_audiorec){
        [s_audiorec dealloc];
        s_audiorec=nil;
    }

}

@end
