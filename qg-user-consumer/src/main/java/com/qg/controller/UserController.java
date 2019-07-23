package com.qg.controller;

import com.qg.dto.ReturnResult;
import com.qg.service.LocalUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * 用户控制器
 */
@Controller
@RequestMapping("/api")
public class UserController {

    @Autowired
    private LocalUserService localUserService;

    /**
     * 用户登录的方法
     * @param phone
     * @param password
     * @return
     */
    @RequestMapping("/doLogin")
    @ResponseBody
    public ReturnResult doLogin(String phone, String password, HttpServletResponse httpServletResponse) throws Exception {
        //1.调用接口，实现用户名和密码的验证
      return   localUserService.validateToken(phone,password);
    }

    /***
     * 用户注销的方法
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/v/loginOut",method = RequestMethod.POST)
    @ResponseBody
    public ReturnResult loginOut(String token)throws Exception{
        return localUserService.removeToken(token);
    }



}
