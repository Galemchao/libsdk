//
//  IOSiAP_Bridge.cpp
//
//Created by zhanglin on 16-6-16.
//
//

#include "IOSiAP_Bridge.h"
#import "sdk_iospay.h"
static IOSiAP_Bridge* s_iap = nullptr;
IOSiAP_Bridge* IOSiAP_Bridge::getInstance()
{
    if(!s_iap)
    {
        s_iap = new IOSiAP_Bridge();
    }
    return s_iap;
}
void IOSiAP_Bridge::delInstance()
{
    if(s_iap)
    {
        delete s_iap;
        s_iap = nullptr;
    }
}

IOSiAP_Bridge::IOSiAP_Bridge()
{
    m_productID = "";
    iap = new IOSiAP();
    iap->delegate = this;
    m_payNum = 0;
    m_customid = "";
    m_isPaying = false;
}

IOSiAP_Bridge::~IOSiAP_Bridge()
{
    delete iap;
}

bool IOSiAP_Bridge::isBusy()
{
    return m_isPaying;
}

int IOSiAP_Bridge::requestProducts(std::string& productId,std::map<std::string,std::string>& pmap,std::string& customid)
{
    if(m_isPaying)
    {
        return 1;
    }
    m_isPaying = true;
    m_productID  = productId;
    m_pmap = pmap;
    m_customid = customid;
    vector<std::string> product;
    for(auto t : m_pmap)
    {
        product.push_back(t.second);

    }
	//把需要付费的道具的所有product id都放到容器里面传进去
    iap->requestProducts(product);
    return 0;
}

void IOSiAP_Bridge::onRequestProductsFinish(void)
{
    std::string identifier = "";
    if(m_pmap.find(m_productID) == m_pmap.end())
    {
        NSString* pid = [NSString stringWithUTF8String:m_productID.c_str()];
        NSString* cid = [NSString stringWithUTF8String:m_customid.c_str()];
        [sdk iap_notify:-2 :pid :cid :@"" :@"没有商品信息3"];
        m_isPaying = false;
        return;
    }
    identifier = m_pmap[m_productID];
    //必须在onRequestProductsFinish后才能去请求iAP产品数据。
    IOSProduct *product = iap->iOSProductByIdentifier(identifier);
    if(product==nullptr)
    {
        NSString* pid = [NSString stringWithUTF8String:m_productID.c_str()];
        NSString* cid = [NSString stringWithUTF8String:m_customid.c_str()];
        [sdk iap_notify:3 :pid :cid :@"" :@"取不到商品信息"];
        m_isPaying = false;
        return;
    }
    // 然后可以发起付款请求。,第一个参数是由iOSProductByIdentifier获取的IOSProduct实例，第二个参数是购买数量
    iap->paymentWithProduct(product, 1);
}

void IOSiAP_Bridge::onRequestProductsError(int code)
{
    //这里requestProducts出错了，不能进行后面的所有操作。
    NSLog(@"请求商品信息失败 %d",code);
    NSString* pid = [NSString stringWithUTF8String:m_productID.c_str()];
    NSString* cid = [NSString stringWithUTF8String:m_customid.c_str()];
    [sdk iap_notify:2 :pid :cid :@"" :@"请求商品信息失败"];
    m_isPaying = false;
}

void IOSiAP_Bridge::onPaymentEvent(std::string &identifier, IOSiAPPaymentEvent event, int quantity,SKPaymentTransaction *transaction)
{
    NSString* pid = [NSString stringWithUTF8String:m_productID.c_str()];
    NSString* pidstr = [NSString stringWithUTF8String:identifier.c_str()];
    NSString* cid = [NSString stringWithUTF8String:m_customid.c_str()];

    if(m_isPaying==false)
    {
        return;
    }
    if (event == IOSIAP_PAYMENT_PURCHASING)
    {
        NSLog(@"付款进行中");
        [sdk iap_notify:1 :pid :cid :pidstr :@"付款进行中"];
        return;
    }
    else if (event == IOSIAP_PAYMENT_PURCHAED)
    {
        NSLog(@"付款成功");
        m_payNum++;
        NSData *receiptData;
        if (NSFoundationVersionNumber >= NSFoundationVersionNumber_iOS_7_0) {
            receiptData = [NSData dataWithContentsOfURL:[[NSBundle mainBundle] appStoreReceiptURL]];
        } else {
            receiptData = transaction.transactionReceipt;
        }
        if(!receiptData)
        {
            
        }
        else
        {
            [sdk iap_notify:0 :pid :cid :pidstr :@"付款成功"];
        }
    }
    else if (event == IOSIAP_PAYMENT_FAILED)
    {
        NSLog(@"付款失败");
        [sdk iap_notify:100 :pid :cid :pidstr :@"付款失败"];
    }
    else if (event == IOSIAP_PAYMENT_RESTORED)
    {
        NSLog(@"支付RESTORED");
        [sdk iap_notify:102 :pid :cid :pidstr :@"支付RESTORED"];
    }
    else if (event == IOSIAP_PAYMENT_REMOVED)
    {
        //用户取消支付
        NSLog(@"取消购买");
        [sdk iap_notify:101 :pid :cid :pidstr :@"取消购买"];
    }
    else
    {
        NSLog(@"购买失败Other");
        [sdk iap_notify:103 :pid :cid :pidstr :@"购买失败Other"];
    }
    m_isPaying = false;
    return;
}
