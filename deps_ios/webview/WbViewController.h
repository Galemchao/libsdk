//
//  WbViewController.h
//  pdk
//
//  Created by Jeep on 16/12/14.
//
//

#import <UIKit/UIKit.h>

@interface WbViewController : UIViewController
@property (retain, nonatomic) IBOutlet UILabel *mTopLable;
@property (retain, nonatomic) IBOutlet UIButton *mBtnBack;
@property (retain, nonatomic) IBOutlet UIWebView *mWebView;
- (IBAction)goBack:(id)sender;
- (void)loadUrl:(NSString*)url;

@property (retain) NSString *mUrl;
+ (id)create:(NSString*)urlstr;
@end
