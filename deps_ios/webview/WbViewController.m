//
//  WbViewController.m
//  pdk
//
//  Created by Jeep on 16/12/14.
//
//

#import "WbViewController.h"
#import <UIkit/UIWebView.h>
#import <UIKit/UIButton.h>

@interface WbViewController ()

@end

@implementation WbViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    [self loadUrl:_mUrl];
    
    CGRect rect = [[UIScreen mainScreen] bounds];
    CGRect rect_lb =[_mTopLable bounds];
    rect_lb.size.width = rect.size.width;
    [_mTopLable setFrame:rect_lb];
    
    CGRect rect_wb =[_mWebView bounds];
    rect_wb.size.width = rect.size.width;
    rect_wb.size.height = rect.size.height-rect_lb.size.height;
    [_mWebView setFrame:rect_wb];

}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (void)dealloc {
    [_mWebView release];
    [_mBtnBack release];
    [_mTopLable release];
    [super dealloc];
}

- (IBAction)goBack:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)loadUrl:(NSString*)urlstr{
    
    NSURL* url = [NSURL URLWithString:urlstr];//创建URL
    NSURLRequest* request = [NSURLRequest requestWithURL:url];//创建NSURLRequest
    if(_mWebView && urlstr)
    {
        [_mWebView loadRequest:request];//加载
    }
}

+ (id)create:(NSString*)urlstr{
    WbViewController *vc=[[WbViewController alloc]initWithNibName:@"WbViewController" bundle:nil];
    vc.mUrl = urlstr;
    return vc;
}

@end
