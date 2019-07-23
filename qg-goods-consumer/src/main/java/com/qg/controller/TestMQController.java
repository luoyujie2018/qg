package com.qg.controller;

import com.qg.utils.ActiveMQUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test")
public class TestMQController {

    @Autowired
    private ActiveMQUtils activeMQUtils;


    /**
     * 发送消息的方法
     * @return
     */
    @RequestMapping("/sendMessage")
    @ResponseBody
    public String sendMessage(){
        activeMQUtils.sendQueueMesage("qg:test","I AM TISTER");
        return "success";
    }

    /**
     * 监听并接收消息的方法
     * @param message
     * @return
     */
    @JmsListener(destination = "qg:test")
    public void receiveMessage(String message){
        System.out.println(message);
    }
}
