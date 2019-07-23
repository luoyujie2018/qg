package com.qg.config;
import com.qg.utils.TokenUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;;
import java.net.URLEncoder;

@Component
@ConfigurationProperties(prefix = "wx")
public class WxConfig {

    private String codeUrl;
    private String appId;
    private String redirectUri;
    private String responseType;
    private String scope;
    private String state;

    private String accessTokenUrl;
    private String secret;
    private String grantType;

    private String userInfoUrl;

    private String successUrl;


    public String getUserInfoUrl() {
        return userInfoUrl;
    }

    public void setUserInfoUrl(String userInfoUrl) {
        this.userInfoUrl = userInfoUrl;
    }

    public String getAccessTokenUrl() {
        return accessTokenUrl;
    }

    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = TokenUtils.createToken("wx","state");
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String reqCodeUrl() throws Exception {
        StringBuffer buffer = new StringBuffer(getCodeUrl());
        buffer.append("?").append("appid=").append(getAppId());
        buffer.append("&").append("redirect_uri=").append(URLEncoder.encode (getRedirectUri(),"utf-8"));
        buffer.append("&").append("response_type=").append(getResponseType());
        buffer.append("&").append("scope=").append(getScope());
        buffer.append("&").append("state=").append(getState());
        buffer.append("#wechat_redirect");
        return buffer.toString();
    }





    public String reqAccessTokenUrl(String code) throws Exception {
        StringBuffer buffer = new StringBuffer(getAccessTokenUrl());
        buffer.append("?").append("appid=").append(getAppId());
        buffer.append("&").append("secret=").append(getSecret());
        buffer.append("&").append("code=").append(code);
        buffer.append("&").append("grant_type=").append(getGrantType());
        return buffer.toString();
    }

    public String reqUserInfoUrl(String accessToken,String  openId) {
        StringBuffer buffer = new StringBuffer(getUserInfoUrl());
        buffer.append("?").append("access_token=").append(accessToken);
        buffer.append("&").append("openid=").append(openId);
        return buffer.toString();
    }
}
