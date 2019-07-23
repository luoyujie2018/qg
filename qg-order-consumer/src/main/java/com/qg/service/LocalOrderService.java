package com.qg.service;

import com.qg.dto.ReturnResult;
import com.qg.pojo.QgOrder;
import com.qg.vo.OrderVo;

import java.util.List;

/**
 * created by admin
 *
 * 本地订单接口
 */
public interface LocalOrderService {
    /**
     *查询订单列表
     * @param token
     * @return
     * @throws Exception
     */
    public ReturnResult<List<OrderVo>> queryOrderList(String token) throws Exception;
    /***
     * 根据订单id查询订单
     * @param orderId
     * @param token
     * @return
     * @throws Exception
     */
    public ReturnResult<QgOrder> queryOrderById(String orderId,String token) throws Exception;
}
