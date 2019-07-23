package com.qg.config;

import com.github.wxpay.sdk.IWXPayDomain;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@ConfigurationProperties(prefix = "wxPayConfig")
@Component
public class QgWxPayConfig extends WXPayConfig {

    private String appID;

    private String mchID;

    private String key;

    private String notifyUrl;
    @Override
    public String getAppID() {
        return this.appID;
    }

    @Override
    public String getMchID() {
        return this.mchID;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public IWXPayDomain getWXPayDomain() {
        return new IWXPayDomain() {
            @Override
            public void report(String s, long l, Exception e) {

            }

            @Override
            public DomainInfo getDomain(WXPayConfig wxPayConfig) {
                return new IWXPayDomain.DomainInfo(WXPayConstants.DOMAIN_API,true);
            }
        };
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public void setMchID(String mchID) {
        this.mchID = mchID;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
}
