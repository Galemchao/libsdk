//
//  IOSiAP_Bridge.h
//
//  Created by zhanglin on 16-6-16.
//
//
#ifndef __iAP_Bridge__IOSiAP__
#define __iAP_Bridge__IOSiAP__
#pragma once
#import "IOSiAP.h"
using namespace std;
class IOSiAP_Bridge : public IOSiAPDelegate
{
public:
    static IOSiAP_Bridge* getInstance();
    static void delInstance();
    IOSiAP_Bridge();
    ~IOSiAP_Bridge();
    IOSiAP *iap;
    bool m_isPaying;
    int requestProducts(std::string& productId,std::map<std::string,std::string>& product, std::string& customid);
    virtual void onRequestProductsFinish(void);
    virtual void onRequestProductsError(int code);
    virtual void onPaymentEvent(std::string &identifier, IOSiAPPaymentEvent event, int quantity,SKPaymentTransaction *transaction);
    void    sharPayCallBack(int result);
    bool isBusy();
private:
    std::string m_productID;
    std::map<std::string,std::string> m_pmap;
    int m_payNum;
    std::string m_customid;
};
#endif
