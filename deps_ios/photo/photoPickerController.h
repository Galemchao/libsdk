//
//  photoPickerController.h
//  myGame
//
//  Created by jake on 2018/6/29.
//

#import <UIKit/UIKit.h>
#import "HPPhotoPickerDetailController.h"
@class photoPickerController;

@protocol photoPickerControllerDelegate <NSObject>

- (void)imagePickerController:(photoPickerController *)picker didFinishPickingWithImage:(UIImage *)image;

@end

@interface photoPickerController : UIViewController <UIImagePickerControllerDelegate,UINavigationControllerDelegate>
- (BOOL) shouldAutorotate;

@property (nonatomic) id <photoPickerControllerDelegate> delegate;
@property (nonatomic ,assign) NSMutableDictionary*  data;

- (instancetype)initWithDelegate:(id)delegate
                            data:(NSDictionary*)data;

- (UIInterfaceOrientationMask)supportedInterfaceOrientations;
@end


