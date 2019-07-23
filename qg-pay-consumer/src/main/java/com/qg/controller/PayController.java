package com.qg.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.qg.common.Constants;
import com.qg.config.AlipayConfig;
import com.qg.dto.ReturnResult;
import com.qg.pojo.QgGoods;
import com.qg.pojo.QgOrder;
import com.qg.service.LocalPayService;
import com.qg.service.QgGoodsService;
import com.qg.service.QgOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class PayController {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private LocalPayService localPayService;

    /**
     *
     * @return
     */
    @RequestMapping("/getQgTradeById")
    @ResponseBody
    public ReturnResult getQgTradeById(String id) throws Exception {

        System.out.println("dfd");
        return localPayService.getQgTradeById(id);
    }


    /***
     * 支付
     * @throws Exception
     */
    @RequestMapping("/v/toPay")
    public void toPay(String orderId, HttpServletResponse response) throws Exception {
        //请求
        String result = localPayService.createAliForm(orderId);
        //输出
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(result);
        response.getWriter().flush();
        response.getWriter().close();
    }

    /***
     * 回调
     * @throws Exception
     */
    @RequestMapping("/callBack")
    public String callBack(HttpServletRequest request) throws Exception {
        //获取支付宝GET过来反馈信息
        boolean signVerified = localPayService.validateAliPay(request.getParameterMap()); //调用SDK验证签名
        String orderId =null;
        //——请在这里编写您的程序（以下代码仅作参考）——
        if(signVerified) {

            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //付款金额
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

            orderId = localPayService.dealPaySuccess(out_trade_no,trade_no,Constants.PayMethod.aliPay);
            return "redirect:"+alipayConfig.paySuccessUrl+"?orderId="+orderId;
//            out.println("trade_no:"+trade_no+"<br/>out_trade_no:"+out_trade_no+"<br/>total_amount:"+total_amount);
        }else {
            return "redirect:"+alipayConfig.payFailUrl+"?orderId="+orderId;
//            out.println("验签失败");
        }

    }

    /***
     * 异步通知
     * @throws Exception
     */
    @RequestMapping("/payNotify")
    @ResponseBody
    public void payNotify(HttpServletRequest request,HttpServletResponse response) throws Exception {
        boolean signVerified = localPayService.validateAliPay(request.getParameterMap()); //调用SDK验证签名
        if(signVerified) {//验证成功
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
          //查询交易记录表看是否执行了业务操作
            boolean flag = localPayService.validateDealPaySuccess(trade_no);
            if (!flag){
                String orderId = localPayService.dealPaySuccess(out_trade_no,trade_no,Constants.PayMethod.aliPay);
            }
            response.getWriter().println("success");
        }else {//验证失败
            response.getWriter().println("fail");
        }
        response.getWriter().flush();
        response.getWriter().close();
    }

}
