package com.qg.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.qg.common.Constants;
import com.qg.config.WxConfig;
import com.qg.dto.ReturnResult;
import com.qg.pojo.QgUser;
import com.qg.service.LocalUserService;
import com.qg.service.QgUserService;
import com.qg.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class WxController {

    @Autowired
    private WxConfig wxConfig;

    @Autowired
    private KafkaUtil kafkaUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private LocalUserService localUserService;

    /**
     * 让用户进行授权
     * @return
     */
    @RequestMapping("/wx/toLogin")
    public  String toWxLogin() throws Exception {
        return "redirect:"+wxConfig.reqCodeUrl();
    }



    /***
     * 微信返回方法
     * @param code
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/wx/callBack")
    public String callBack(String code)throws Exception{
        //获取accessToken的json串
        String accessTokenjsonStr = UrlUtils.loadURL(wxConfig.reqAccessTokenUrl(code));

        //写入日志
        kafkaUtil.sendInfoMessage(accessTokenjsonStr);
        JSONObject accessTokenjson = JSONObject.parseObject(accessTokenjsonStr);
        String accessToken = accessTokenjson.getString("access_token");
        String openId = accessTokenjson.getString("openid");

        //获取用户信息
         String userInfoJsonStr = UrlUtils.loadURL(wxConfig.reqUserInfoUrl(accessToken,openId));
        kafkaUtil.sendInfoMessage(userInfoJsonStr);
        String token = localUserService.createWxUserToken(userInfoJsonStr);
        //跳转至前端首页
        return "redirect:"+wxConfig.getSuccessUrl()+"?token="+token;
    }
}
