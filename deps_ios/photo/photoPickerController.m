//
//  photoPickerController.m
//  myGame
//
//  Created by jake on 2018/6/29.
//

#import "photoPickerController.h"
#import <AVFoundation/AVFoundation.h>
#import <MediaPlayer/MediaPlayer.h>

@implementation photoPickerController

- (void)viewDidLoad {
    [self.view setBackgroundColor:[UIColor colorWithRed:1 green:1 blue:1 alpha:1]];
    [super viewDidLoad];
    //延迟执行
    [NSTimer scheduledTimerWithTimeInterval:0.1 target:self selector:@selector(delayMethod) userInfo:nil repeats:NO];
}

- (void)delayMethod {
    NSLog(@"delayMethodEnd");
    int _type = [[_data valueForKey:@"type"] intValue];
    if (_type == 0) {
        [self positiveButtonClick];
    } else if (_type == 1) {
        [self openCamera];
    }
    else if (_type == 2) {
        [self openPhotoLibrary];
    }
}

- (BOOL) shouldAutorotate {
    return YES;
}
- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

- (void)positiveButtonClick{
    UIActionSheet * actionSheet = [[UIActionSheet alloc]initWithTitle:nil delegate:self cancelButtonTitle:@"取消" destructiveButtonTitle:nil otherButtonTitles:@"相机",@"从相册选择", nil];
    [actionSheet autorelease];
    actionSheet.actionSheetStyle = UIActionSheetStyleBlackTranslucent;

    [actionSheet showInView:self.view];
}
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    NSLog(@"buttonIndex-----%zi",buttonIndex);
    
    if (buttonIndex==0) {
        
        NSLog(@"拍照");
        
        [self openCamera];
        
    }else if (buttonIndex==1){
        NSLog(@"从相册选择");
        [self openPhotoLibrary];
    } else if (buttonIndex==2){
        [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    }
    
    
    // button 点击处理（然后从上往下增加从0开始，注意与alertView 不一样！）
}

/**
 
 *  调用照相机
 
 */

- (void)openCamera
{
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    [picker autorelease];
    picker.delegate = self;
    picker.allowsEditing = NO; //可编辑
//    picker.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
    //判断是否可以打开照相机
    if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
    {
        //摄像头
        picker.sourceType = UIImagePickerControllerSourceTypeCamera;
        [self presentViewController:picker animated:NO completion:nil];
    }
    
    else
        
    {
        
        NSLog(@"没有摄像头");
        
    }
    
}


/**
 
 *  打开相册
 
 */

-(void)openPhotoLibrary

{
    // 进入相册
    
    if([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypePhotoLibrary])
        
    {
        
        UIImagePickerController *imagePicker = [[UIImagePickerController alloc]init];
        
        imagePicker.allowsEditing = NO;
        
        imagePicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
        
        imagePicker.delegate = self;
        
        [self presentViewController:imagePicker animated:YES completion:^{
            
            NSLog(@"打开相册");
            
        }];
        
    }
    
    else
        
    {
        
        NSLog(@"不能打开相册");
        
    }
    
}

#pragma mark - 初始化
- (instancetype)initWithDelegate:(id)delegate
                            data:(NSDictionary*)data
{
    _delegate = delegate;
    _data = [[NSMutableDictionary alloc]initWithDictionary:data];
    return self;
}

#pragma mark - CropImageDelegate
//图片完成回调
- (void)cropImageDidFinishedWithImage:(UIImage *)image
{
    if (self.delegate && [self.delegate respondsToSelector:@selector(imagePickerController:didFinishPickingWithImage:)]) {
        [self.delegate imagePickerController:self didFinishPickingWithImage:image];
    }
    [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
}

-(void)cancleDidFinishedWithImage
{
    [self.presentingViewController dismissViewControllerAnimated:NO completion:nil];
//    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
//    [picker autorelease];
//    picker.delegate = self;
//    picker.allowsEditing = NO; //可编辑
//    //判断是否可以打开照相机
//    if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
//    {
//        //摄像头
//        picker.sourceType = UIImagePickerControllerSourceTypeCamera;
//        UIViewController *topRootViewController = [[UIApplication  sharedApplication] keyWindow].rootViewController;
//        // 在这里加一个这个样式的循环
//        while (topRootViewController.presentedViewController)
//        {
//            // 这里固定写法
//            topRootViewController = topRootViewController.presentedViewController;
//        }
//        [topRootViewController presentViewController:picker animated:YES completion:^{
//            NSLog(@"返回拍摄界面");
//        }];
//    }
//    else
//    {
//
//        NSLog(@"没有摄像头");
//
//    }
}

#pragma mark - UIImagePickerControllerDelegate

// 拍照完成回调

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info
{
    NSLog(@"finish..");
    int type = 1;
//    if(picker.sourceType == UIImagePickerControllerSourceTypeCamera)
//    {
//
//
//    }
    if(picker.sourceType == UIImagePickerControllerSourceTypePhotoLibrary)
    {
        type = 2;
    }
    
    HPPhotoPickerDetailController *detailVC = [[HPPhotoPickerDetailController alloc] autorelease];
    detailVC.data = _data;
    detailVC.type = type;
    [detailVC initWithImage:info[UIImagePickerControllerOriginalImage] delegate:self];
    [picker presentViewController:detailVC animated:YES completion:^{
        NSLog(@"裁剪");
    }];
}

//进入拍摄页面点击取消按钮

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
    [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
