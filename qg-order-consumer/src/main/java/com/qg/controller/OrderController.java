package com.qg.controller;

import com.qg.dto.ReturnResult;
import com.qg.service.LocalOrderService;
import com.qg.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private LocalOrderService localOrderService;

    /**
     *查询订单
     * @return
     */
    @RequestMapping("/v/queryOrderList")
    @ResponseBody
    public ReturnResult<List<OrderVo>> queryOrderList(String token) throws Exception {
        return localOrderService.queryOrderList(token);
    }


    @RequestMapping("/v/prepay")
    @ResponseBody
    public ReturnResult prepay(String orderId,String token) throws Exception {
        return localOrderService.queryOrderById(orderId,token);
    }

}
