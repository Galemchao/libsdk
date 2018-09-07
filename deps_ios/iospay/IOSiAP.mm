#import "IOSiAP.h"

@interface iAPTransactionObserver()
@property (nonatomic, assign) IOSiAP *iosiap;
@end

@implementation iAPTransactionObserver
//SKPaymentTransactionObserver
- (void)paymentQueue:(SKPaymentQueue *)queue updatedTransactions:(NSArray *)transactions
{
    for (SKPaymentTransaction *transaction in transactions) {
        std::string identifier([transaction.payment.productIdentifier UTF8String]);
        IOSiAPPaymentEvent event;
        switch (transaction.transactionState) {
            case SKPaymentTransactionStatePurchasing:
                event = IOSIAP_PAYMENT_PURCHASING;
                return;
            case SKPaymentTransactionStatePurchased:
                event = IOSIAP_PAYMENT_PURCHAED;
                break;
            case SKPaymentTransactionStateFailed:
                event = IOSIAP_PAYMENT_FAILED;
                NSLog(@"==ios payment error:%@", transaction.error);
                break;
            case SKPaymentTransactionStateRestored:
                // NOTE: consumble payment is NOT restorable
                event = IOSIAP_PAYMENT_RESTORED;
                break;
        }
        if(_iosiap && _iosiap->delegate)
        {
            _iosiap->delegate->onPaymentEvent(identifier, event, transaction.payment.quantity, transaction);
        }
        if (event != IOSIAP_PAYMENT_PURCHASING) {
            [[SKPaymentQueue defaultQueue] finishTransaction: transaction];
        }
    }
}

- (void)paymentQueue:(SKPaymentQueue *)queue removedTransactions:(NSArray *)transactions
{
    for (SKPaymentTransaction *transaction in transactions) {
        std::string identifier([transaction.payment.productIdentifier UTF8String]);
        if(_iosiap && _iosiap->delegate)
        {
            _iosiap->delegate->onPaymentEvent(identifier, IOSIAP_PAYMENT_REMOVED, transaction.payment.quantity,transaction);
        }
    }
}
//SKProductsRequestDelegate

- (void)productsRequest:(SKProductsRequest *)request
     didReceiveResponse:(SKProductsResponse *)response
{
    if(!_iosiap)
    {
        NSLog(@"requestProducts has been canceld %@",self);
        return;
    }
    // release old
    if (_iosiap->skProducts) {
        [_iosiap->skProducts release];
    }
    // record new product
    _iosiap->skProducts = [response.products retain];
    
    for (int index = 0; index < [response.products count]; index++) {
        SKProduct *skProduct = [response.products objectAtIndex:index];
        
        // check is valid
        bool isValid = true;
        for (NSString *invalidIdentifier in response.invalidProductIdentifiers) {
            NSLog(@"invalidIdentifier:%@", invalidIdentifier);
            if (skProduct.productIdentifier==nil || [skProduct.productIdentifier isEqualToString:invalidIdentifier]) {
                isValid = false;
                break;
            }
        }
        
        IOSProduct *iosProduct = new IOSProduct;
        if(skProduct.productIdentifier!=nil)
        {
            iosProduct->productIdentifier = std::string([skProduct.productIdentifier UTF8String]);
        }
        if(skProduct.localizedTitle!=nil)
        {
            iosProduct->localizedTitle = std::string([skProduct.localizedTitle UTF8String]);
        }
        if(skProduct.localizedDescription!=nil)
        {
            iosProduct->localizedDescription = std::string([skProduct.localizedDescription UTF8String]);
        }
    
        // locale price to string
        NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
        [formatter setFormatterBehavior:NSNumberFormatterBehavior10_4];
        [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
        [formatter setLocale:skProduct.priceLocale];
        NSString *priceStr = [formatter stringFromNumber:skProduct.price];
        [formatter release];
        iosProduct->localizedPrice = std::string([priceStr UTF8String]);
        
        iosProduct->index = index;
        iosProduct->isValid = isValid;
        _iosiap->iOSProducts.push_back(iosProduct);
    }
}

- (void)requestDidFinish:(SKRequest *)request
{
    if(_iosiap && _iosiap->delegate)
    {
        _iosiap->delegate->onRequestProductsFinish();
    }
    [request.delegate release];
    [request release];
}

- (void)request:(SKRequest *)request didFailWithError:(NSError *)error
{
    NSLog(@"%@", error);
    if(_iosiap && _iosiap->delegate)
    {
        _iosiap->delegate->onRequestProductsError([error code]);
    }
}
@end

IOSiAP::IOSiAP()
{
    skProducts = nil;
    delegate = nullptr;
    skTransactionObserver = [[iAPTransactionObserver alloc] init];
    skTransactionObserver.iosiap = this;
    [[SKPaymentQueue defaultQueue] addTransactionObserver:(iAPTransactionObserver *)skTransactionObserver];
}

IOSiAP::~IOSiAP()
{
    if (skProducts) {
        [skProducts release];
    }
    
    std::vector <IOSProduct *>::iterator iterator;
    for (iterator = iOSProducts.begin(); iterator != iOSProducts.end(); iterator++) {
        IOSProduct *iosProduct = *iterator;
        delete iosProduct;
    }
    delegate = nullptr;
    //设置为null SKProductsRequest中还retain它,SKProductsRequestDelegate两个函数中做了判断
    skTransactionObserver.iosiap = nullptr;
    [[SKPaymentQueue defaultQueue] removeTransactionObserver:(iAPTransactionObserver *)skTransactionObserver];
    [skTransactionObserver release];
}

IOSProduct *IOSiAP::iOSProductByIdentifier(std::string &identifier)
{
    std::vector <IOSProduct *>::iterator iterator;
    for (iterator = iOSProducts.begin(); iterator != iOSProducts.end(); iterator++)
    {
        IOSProduct *iosProduct = *iterator;
        if (iosProduct->productIdentifier == identifier)
        {
            return iosProduct;
        }
    }

    return nullptr;
}

void IOSiAP::requestProducts(std::vector <std::string> &productIdentifiers)
{
    NSMutableSet *set = [NSMutableSet setWithCapacity:productIdentifiers.size()];
    std::vector <std::string>::iterator iterator;
    for (iterator = productIdentifiers.begin(); iterator != productIdentifiers.end(); iterator++) {
        [set addObject:[NSString stringWithUTF8String:(*iterator).c_str()]];
       
    }
    SKProductsRequest *productsRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:set];
    productsRequest.delegate = skTransactionObserver;
    [productsRequest.delegate retain];
    [productsRequest start];
}
bool IOSiAP::IphoneIsYueYu()
{
    NSURL* url = [NSURL URLWithString:@"cydia://package/com.example.package"];
    //是否越狱
    if ([[UIApplication sharedApplication] canOpenURL:url])
    {
        return true;
    }
    return false;
}
void IOSiAP::paymentWithProduct(IOSProduct *iosProduct, int quantity)
{
    SKProduct *skProduct = [(NSArray *)(skProducts) objectAtIndex:iosProduct->index];
    SKMutablePayment *payment = [SKMutablePayment paymentWithProduct:skProduct];
    payment.quantity = quantity;
    
    [[SKPaymentQueue defaultQueue] addPayment:payment];
}
