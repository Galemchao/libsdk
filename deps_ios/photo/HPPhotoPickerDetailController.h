//
//  HPPhotoPickerDetailController.h
//  photo
//
//  Created by 雷建民 on 17/1/11.
//  Copyright © 2017年 雷建民. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol CropImageDelegate <NSObject>

- (void)cropImageDidFinishedWithImage:(UIImage *)image;
- (void) cancleDidFinishedWithImage;
@end

@interface HPPhotoPickerDetailController : UIViewController

@property (nonatomic) id <CropImageDelegate> delegate;
@property (nonatomic, assign) int type;      //1 拍照  2 相册
@property (nonatomic, assign) NSMutableDictionary*  data;
- (instancetype)initWithImage:(UIImage *)originalImage delegate:(id)delegate;


@end
