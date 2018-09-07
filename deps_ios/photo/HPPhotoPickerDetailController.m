//
//  HPPhotoPickerDetailController.m
//  photo
//
//  Created by 雷建民 on 17/1/11.
//  Copyright © 2017年 雷建民. All rights reserved.
//


#define ScreenWidth [UIScreen mainScreen].bounds.size.width
#define ScreenHeight [UIScreen mainScreen].bounds.size.height

#import "HPPhotoPickerDetailController.h"
#import "UIImage+Extension.h"
@interface HPPhotoPickerDetailController ()<UIScrollViewDelegate>
{
    int _ovalClip;  //是否裁剪
}
@property (nonatomic, strong) UIScrollView * scrollView;
@property (nonatomic, strong) UIImageView * imageView;
@property (nonatomic, strong) UIImage * originalImage;

@end

@implementation HPPhotoPickerDetailController

- (BOOL) shouldAutorotate {
    return YES;
}
- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

//初始化裁剪界面
- (void)setUpSubViews
{
    self.view.backgroundColor = [UIColor blackColor];
    self.automaticallyAdjustsScrollViewInsets = NO;
    CGFloat distance = (ScreenWidth - ScreenHeight)/2.0;
    CGFloat radius = ScreenHeight/2.0;
    if (ScreenHeight > ScreenWidth) {
        distance = (ScreenHeight - ScreenWidth)/2.0;
        _scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, distance ,ScreenWidth,ScreenWidth)];
        radius = ScreenWidth/2.0;
    } else {
        _scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(distance, 0 ,ScreenHeight, ScreenHeight)];
    }
    [_scrollView autorelease];
    _scrollView.bouncesZoom = YES;
    _scrollView.minimumZoomScale = 1;
    _scrollView.maximumZoomScale = 3;
    _scrollView.zoomScale = 1;
    _scrollView.delegate = self;
    _scrollView.layer.masksToBounds = NO;
    _scrollView.showsHorizontalScrollIndicator = NO;
    _scrollView.showsVerticalScrollIndicator = NO;
    if (_ovalClip != 0) {
        //裁剪边框的划线宽度
        _scrollView.layer.borderWidth = 1.5;
    }
    //裁剪边框的划线颜色
    _scrollView.layer.borderColor = [UIColor whiteColor].CGColor;
    self.view.layer.masksToBounds = YES;
    if (_ovalClip == 2) {
        _scrollView.layer.cornerRadius = radius;
    }
    if (_originalImage) {
        //等比例缩放原图，通过imageView展示到裁剪界面
        _imageView = [[[UIImageView alloc] initWithImage:_originalImage] autorelease];
        CGFloat img_height = ScreenHeight;
        CGFloat img_width = _originalImage.size.width * (img_height/_originalImage.size.height);
        CGFloat img_x= (img_width - self.view.bounds.size.height)/2.0;
        CGFloat img_y= 0;
        if (ScreenHeight > ScreenWidth) {
            img_width = ScreenWidth;
            img_height = _originalImage.size.height * (img_width/_originalImage.size.width);
            img_y= (img_height - self.view.bounds.size.width)/2.0;
            img_x = 0;
        }
        
        _imageView.frame = CGRectMake(0,0, img_width, img_height);
        _imageView.userInteractionEnabled = YES;
        [_scrollView addSubview:_imageView];
        
        _scrollView.contentSize = CGSizeMake(img_width, img_height);
        _scrollView.contentOffset = CGPointMake(img_x, img_y);
        [self.view addSubview:_scrollView];
    }
    [self userInterface];
}

- (void)userInterface {
    if (_ovalClip != 0) {
        //圆形裁剪界面
        CGRect cropframe = _scrollView.frame;
        UIBezierPath * path = [UIBezierPath bezierPathWithRoundedRect:self.view.bounds cornerRadius:0];
        UIBezierPath * cropPath = [UIBezierPath bezierPathWithRoundedRect:cropframe cornerRadius:0];
        if (_ovalClip == 2) {
            cropPath = [UIBezierPath bezierPathWithOvalInRect:cropframe];
        }
        [path appendPath:cropPath];
        CAShapeLayer * layer = [[[CAShapeLayer alloc] init] autorelease];
        layer.fillColor = [UIColor colorWithRed:.0 green:.0 blue:.0 alpha:0.5].CGColor;
        layer.fillRule=kCAFillRuleEvenOdd;
        layer.path = path.CGPath;
        [self.view.layer addSublayer:layer];
    }
    
    UIView * view = [[[UIView alloc] initWithFrame:CGRectMake(0, self.view.bounds.size.height - 46, self.view.bounds.size.width, 46)] autorelease];
    view.backgroundColor = [UIColor colorWithRed:30/255.0 green:30/255.0 blue:30/255.0 alpha:0.7];
    [self.view addSubview:view];
    
    UIButton * canncelBtn = [UIButton buttonWithType:UIButtonTypeSystem];
    canncelBtn.frame = CGRectMake(0, 0, 60, 44);
    canncelBtn.titleLabel.font = [UIFont systemFontOfSize:18];
    [canncelBtn setTitle:@"取 消" forState:UIControlStateNormal];
    [canncelBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [canncelBtn addTarget:self action:@selector(cancalAction) forControlEvents:UIControlEventTouchUpInside];
    [view addSubview:canncelBtn];

    UIButton * doneBtn = [UIButton buttonWithType:UIButtonTypeSystem];
    doneBtn.frame = CGRectMake([UIScreen mainScreen].bounds.size.width - 60, 0, 60, 44);
    doneBtn.titleLabel.font = [UIFont systemFontOfSize:18];
    [doneBtn setTitle:@"完 成" forState:UIControlStateNormal];
    [doneBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [doneBtn addTarget:self action:@selector(sureAction) forControlEvents:UIControlEventTouchUpInside];
    [view addSubview:doneBtn];
}
#pragma mark -- UIScrollViewDelegate
- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView {
    return self.imageView;
}
- (void)scrollViewDidZoom:(UIScrollView *)scrollView {
    [self centerContent];
}
//图片放大缩小重置imageView起始点
- (void)centerContent {
    CGRect imageViewFrame = _imageView.frame;
    CGRect scrollBounds = CGRectMake(0, 0, ScreenHeight, ScreenHeight);
    if (ScreenHeight > ScreenWidth) {
        scrollBounds = CGRectMake(0, 0, ScreenWidth, ScreenWidth);
    }
    CGSize imgSize = imageViewFrame.size;
    CGSize scrollSize = scrollBounds.size;
    
    if (imgSize.height > scrollSize.height) {
        imageViewFrame.origin.y = 0.0f;
    }else {
        imageViewFrame.origin.y = (scrollSize.height - imgSize.height) / 2.0;
    }
    if (imgSize.width < scrollSize.width) {
        imageViewFrame.origin.x = (scrollSize.width - imgSize.width) /2.0;
    }else {
        imageViewFrame.origin.x = 0.0f;
    }
    _imageView.frame = imageViewFrame;
}

//裁剪图片
- (UIImage *)cropImage {
    if (_ovalClip == 0) {
        return _originalImage;
    }
    CGPoint offset = _scrollView.contentOffset;
    CGFloat originalHeight = _originalImage.size.height;
    CGFloat originalWidth = _originalImage.size.width;
    CGFloat imgWidth = _imageView.frame.size.width;
    CGFloat imgHeight = _imageView.frame.size.height;
    //图片缩放比例
    CGFloat zoom = imgHeight/originalHeight;
    if (ScreenHeight > ScreenWidth) {
        zoom = imgWidth/originalWidth;
    }

    CGFloat width = _scrollView.frame.size.width;
    CGFloat height = _scrollView.frame.size.height;
    //防止黑边
    if (ScreenHeight > ScreenWidth) {
        if (imgHeight < height) {
            offset = CGPointMake(offset.x + (width - imgHeight)/2.0, 0);
            width = height = imgHeight;
        }
    } else {
        if (imgWidth < width) {
            offset = CGPointMake(0, offset.y + (height - imgWidth)/2.0);
            width = height = imgWidth;
        }
    }
    //竖屏拍照图片会顺时针旋转90度，需要旋转回来
    UIImage* img = [self fixOrientation:_originalImage];
    
    CGRect rec = CGRectMake(offset.x/zoom, offset.y/zoom,width/zoom,height/zoom);
    CGImageRef imageRef =CGImageCreateWithImageInRect([img CGImage],rec);
    UIImage * image = [[[UIImage alloc]initWithCGImage:imageRef] autorelease];
    CGImageRelease(imageRef);
    //获取图片大小并裁减
    NSNumber* outputX = [_data valueForKey:@"outputX"];
    NSNumber* outputY = [_data valueForKey:@"outputY"];
    int newWidth = [outputX intValue];
    int newHeight = [outputY intValue];
    CGSize size = CGSizeMake(newWidth/[UIScreen mainScreen].scale ,newHeight/[UIScreen mainScreen].scale);
    UIGraphicsBeginImageContextWithOptions(size, NO, [UIScreen mainScreen].scale);
    [image drawInRect:CGRectMake(0, 0, size.width,size.height)];
    UIImage * newimage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    //圆形裁剪
    if (_ovalClip == 2) {
        newimage = [newimage beginClip];
    }
    
    return newimage;
}

//uiImage图片还原
- (UIImage *)fixOrientation:(UIImage *)aImage {
    // No-op if the orientation is already correct
    if (aImage.imageOrientation == UIImageOrientationUp)
        return aImage;
    
    // We need to calculate the proper transformation to make the image upright.
    // We do it in 2 steps: Rotate if Left/Right/Down, and then flip if Mirrored.
    CGAffineTransform transform = CGAffineTransformIdentity;
    
    switch (aImage.imageOrientation) {
        case UIImageOrientationDown:
        case UIImageOrientationDownMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.width, aImage.size.height);
            transform = CGAffineTransformRotate(transform, M_PI);
            break;
            
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.width, 0);
            transform = CGAffineTransformRotate(transform, M_PI_2);
            break;
            
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            transform = CGAffineTransformTranslate(transform, 0, aImage.size.height);
            transform = CGAffineTransformRotate(transform, -M_PI_2);
            break;
        default:
            break;
    }
    
    switch (aImage.imageOrientation) {
        case UIImageOrientationUpMirrored:
        case UIImageOrientationDownMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.width, 0);
            transform = CGAffineTransformScale(transform, -1, 1);
            break;
            
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRightMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.height, 0);
            transform = CGAffineTransformScale(transform, -1, 1);
            break;
        default:
            break;
    }
    
    // Now we draw the underlying CGImage into a new context, applying the transform
    // calculated above.
    CGContextRef ctx = CGBitmapContextCreate(NULL, aImage.size.width, aImage.size.height,
                                             CGImageGetBitsPerComponent(aImage.CGImage), 0,
                                             CGImageGetColorSpace(aImage.CGImage),
                                             CGImageGetBitmapInfo(aImage.CGImage));
    CGContextConcatCTM(ctx, transform);
    switch (aImage.imageOrientation) {
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            // Grr...
            CGContextDrawImage(ctx, CGRectMake(0,0,aImage.size.height,aImage.size.width), aImage.CGImage);
            break;
            
        default:
            CGContextDrawImage(ctx, CGRectMake(0,0,aImage.size.width,aImage.size.height), aImage.CGImage);
            break;
    }
    
    // And now we just create a new UIImage from the drawing context
    CGImageRef cgimg = CGBitmapContextCreateImage(ctx);
    UIImage *img = [UIImage imageWithCGImage:cgimg];
    CGContextRelease(ctx);
    CGImageRelease(cgimg);
    return img;
}

#pragma mark - 初始化
- (instancetype)initWithImage:(UIImage *)originalImage delegate:(id)delegate {
    self = [super init];
    if (self) {
        _delegate = delegate;
        _originalImage = originalImage;
        NSNumber* ovalClip = [_data valueForKey:@"clip"];
        _ovalClip = [ovalClip intValue];
    }
    return self;
}
- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationController.navigationBar.hidden = YES;
    [self setUpSubViews];
}
- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
//    self.navigationController.navigationBar.hidden = NO;
}
#pragma mark - 点击事件
- (void)cancalAction
{
    if (_type == 1 ) {
        [_delegate cancleDidFinishedWithImage];
    } else {
        [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    }
}

- (void)sureAction
{
    if (_delegate && [_delegate respondsToSelector:@selector(cropImageDidFinishedWithImage:)]) {
        [_delegate cropImageDidFinishedWithImage:[self cropImage]];
    }
}

@end
