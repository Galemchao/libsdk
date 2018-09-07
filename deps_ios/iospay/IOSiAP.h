//
//  IOSiAP.h
//
//Created by zhanglin on 16-6-16.
//
//

#ifndef __iAP_JSBinding__IOSiAP__
#define __iAP_JSBinding__IOSiAP__
#import <Foundation/Foundation.h>
#import <StoreKit/StoreKit.h>

#include <iostream>
#include <vector>
#include <map>

class IOSProduct
{
public:
    std::string productIdentifier;
    std::string localizedTitle;
    std::string localizedDescription;
    std::string localizedPrice;// has be localed, just display it on UI.
    bool isValid;
    int index;//internal use : index of skProducts
};

typedef enum {
    IOSIAP_PAYMENT_PURCHASING,// just notify, UI do nothing
    IOSIAP_PAYMENT_PURCHAED,// need unlock App Functionality
    IOSIAP_PAYMENT_FAILED,// remove waiting on UI, tall user payment was failed
    IOSIAP_PAYMENT_RESTORED,// need unlock App Functionality, consumble payment No need to care about this.
    IOSIAP_PAYMENT_REMOVED,// remove waiting on UI
} IOSiAPPaymentEvent;

class IOSiAPDelegate
{
public:
    virtual ~IOSiAPDelegate() {}
    // for requestProduct
    virtual void onRequestProductsFinish(void) = 0;
    virtual void onRequestProductsError(int code) = 0;
    // for payment
    virtual void onPaymentEvent(std::string &identifier, IOSiAPPaymentEvent event, int quantity,SKPaymentTransaction *transaction) = 0;
};

@interface iAPTransactionObserver : NSObject<SKPaymentTransactionObserver,SKProductsRequestDelegate>
@end

class IOSiAP
{
public:
    IOSiAP();
    ~IOSiAP();
    void requestProducts(std::vector <std::string> &productIdentifiers);
    IOSProduct *iOSProductByIdentifier(std::string &identifier);
    void paymentWithProduct(IOSProduct *iosProduct, int quantity = 1);
    
    bool IphoneIsYueYu(); //判断手机是否越狱
    
    IOSiAPDelegate *delegate;
    // ===  internal use for object-c class ===
    NSArray *skProducts;// object-c SKProduct
    iAPTransactionObserver *skTransactionObserver;// object-c TransactionObserver
    std::vector<IOSProduct *> iOSProducts;
};

#endif /* defined(__iAP_JSBinding__IOSiAP__) */
