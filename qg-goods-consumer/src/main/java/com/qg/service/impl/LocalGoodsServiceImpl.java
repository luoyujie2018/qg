package com.qg.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.qg.common.Constants;
import com.qg.dto.ReturnResult;
import com.qg.dto.ReturnResultUtils;
import com.qg.exception.GoodsException;
import com.qg.pojo.QgGoods;
import com.qg.pojo.QgGoodsTempStock;
import com.qg.pojo.QgOrder;
import com.qg.pojo.QgUser;
import com.qg.service.LocalGoodsService;
import com.qg.service.QgGoodsService;
import com.qg.service.QgGoodsTempStockService;
import com.qg.service.QgOrderService;
import com.qg.utils.*;
import com.qg.vo.GetGoodsMessage;
import com.qg.vo.GoodsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/***
 * 本地用户的业务处理
 */
@Component
public class LocalGoodsServiceImpl implements LocalGoodsService {


    @Reference
    private QgGoodsService qgGoodsService;

    @Reference
    private QgGoodsTempStockService qgGoodsTempStockService;

    @Reference
    private QgOrderService qgOrderService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtils activeMQUtils;



    /**
     * 需求：商品信息，获取库存信息
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public ReturnResult queryGoodsById(String id) throws Exception {
        GoodsVo goodsVo = new GoodsVo();
        //1.首先从redis中进行获取
        //2.redis中没有，则走数据库查询，并将结果写入到redis
        //3.redis中有，则走redis
        String goodsVoJson = redisUtil.getStr(Constants.goodsPrefix+id);
        if (EmptyUtils.isEmpty(goodsVoJson)){

            QgGoods qgGoods = qgGoodsService.getQgGoodsById(id);
            BeanUtils.copyProperties(qgGoods,goodsVo);
            //获取库存信息

            //1.获取临时库存表中，goods_id 为id的有效记录数-->用户已消费或待消费记录数
            Map<String,Object> param = new HashMap<>();
            param.put("goodsId",id);
            param.put("active",1);
            Integer activeCount = qgGoodsTempStockService.getQgGoodsTempStockCountByMap(param);
            //实际库存
            Integer currentCount = goodsVo.getStock()-activeCount;
            goodsVo.setCurrentStock(currentCount);
            //放在redis当中
            redisUtil.setStr(Constants.goodsPrefix+id,JSONObject.toJSONString(goodsVo));
        }else {
            goodsVo = JSONObject.parseObject(goodsVoJson,GoodsVo.class);
        }

        return ReturnResultUtils.returnSuccess(goodsVo);
    }

    /**
     *
     * @param token
     * @param goodsId
     * @return
     * @throws Exception
     */
    @Override
    public ReturnResult goodsGetMessage(String token, String goodsId) throws Exception {
//        for (int i = 2; i <=200; i++) {
//            String toke = "{\n" +
//                    "    \"createdTime\": 1513907646000,\n" +
//                    "    \"id\": \""+i+"\",\n" +
//                    "    \"password\": \"123456\",\n" +
//                    "    \"phone\": \"13366055111\",\n" +
//                    "    \"realName\": \"张杉\""+i+",\n" +
//                    "    \"updatedTime\": 1513994048000,\n" +
//                    "    \"wxUserId\": \"\"\n" +
//                    "}";
//
//            redisUtil.del(Constants.getGoodsPrefix+"1:"+i);
////            redisUtil.setStr(Constants.tokenPrefix+i,toke);
//
//        }

        //根据token知道是哪个用户 获取用户信息
        String userJson = redisUtil.getStr(token);
        QgUser qgUser = JSONObject.parseObject(userJson,QgUser.class);
        //将用户id 和 抢购的商品 id 写入消息中间件
        GetGoodsMessage getGoodsMessage  = new GetGoodsMessage();
        getGoodsMessage.setUserId(qgUser.getId());
        getGoodsMessage.setGoodsId(goodsId);
        activeMQUtils.sendQueueMesage(Constants.ActiveMQMassage.getMessage,getGoodsMessage);
        return ReturnResultUtils.returnSuccess();
    }


    /**
     * 处理抢购请求的方法
     * @param getGoodsMessage
     * @throws Exception
     */
    @JmsListener(destination = Constants.ActiveMQMassage.getMessage)
    private void getGoods(GetGoodsMessage getGoodsMessage) throws Exception {
        String userId = getGoodsMessage.getUserId();
        String goodsId = getGoodsMessage.getGoodsId();

        //1.查看用户是否已经抢购过该商品，如果用户有抢购成功未支付的或已经成功支付的记录，则不能抢
        while (!redisUtil.lock(Constants.lockPrefix+goodsId,Constants.lockExpire)){
            Thread.sleep(3);
        }

        String getFlag = redisUtil.getStr(Constants.getGoodsPrefix+goodsId+":"+userId);

        //判断用户是否已经抢购成功
        if (EmptyUtils.isNotEmpty(getFlag) && getFlag.equals(Constants.getGoodsStatus.getSuccess)){
            redisUtil.unLock(Constants.lockPrefix+goodsId);
            return;
        }
        //2.判断库存是否大于0，如果大于0则进入抢购成功，如果已经抢购过，则跳过
        String goodsVoJson =  redisUtil.getStr(Constants.goodsPrefix+goodsId);
        GoodsVo goodsVo = JSONObject.parseObject(goodsVoJson,GoodsVo.class);
        if (goodsVo.getCurrentStock()<=0){
            redisUtil.unLock(Constants.lockPrefix+goodsId);
            redisUtil.setStr(Constants.getGoodsPrefix+goodsId+":"+userId,Constants.getGoodsStatus.getFail);
            return;
        }
        //3.更新Redis库存
        goodsVo.setCurrentStock(goodsVo.getCurrentStock()-1);//当前库存减一
        redisUtil.setStr(Constants.goodsPrefix+goodsId,JSONObject.toJSONString(goodsVo));
        //4.记录用户购买数据
        String stockId = saveQgGoodsTempStock (userId,goodsId);
        //5.生成订单
        saveOrder(stockId,goodsVo.getId(),userId,goodsVo.getPrice());
        //6.在redis中，增加用户已抢购到商品的标识
        redisUtil.setStr(Constants.getGoodsPrefix+goodsId+":"+userId,Constants.getGoodsStatus.getSuccess);
        redisUtil.unLock(Constants.lockPrefix+goodsId);
        //TODO
        //7.返回执行结果
    }

    private void saveOrder(String stockId,String goodsId,String userId,double amount) throws Exception {
        QgOrder qgOrder = new QgOrder();
        qgOrder.setId(IdWorker.getId());
        qgOrder.setCreatedTime(new Date());
        qgOrder.setGoodsId(goodsId);
        qgOrder.setStatus(Constants.OrderStatus.toPay);
        qgOrder.setUpdatedTime(new Date());
        qgOrder.setUserId(userId);
        qgOrder.setAmount(amount);
        qgOrder.setNum(1);
        qgOrder.setOrderNo(IdWorker.getId());
        qgOrder.setStockId(stockId);
        qgOrderService.qdtxAddQgOrder(qgOrder);
    }

    /**
     * 保存抢购信息到临时库存记录表
     * @param userId
     * @param goodsId
     * @throws Exception
     */
    private String saveQgGoodsTempStock (String userId,String goodsId)throws Exception{
        QgGoodsTempStock qgGoodsTempStock = new QgGoodsTempStock();
        qgGoodsTempStock.setId(IdWorker.getId());
        qgGoodsTempStock.setUserId(userId);
        qgGoodsTempStock.setCreatedTime(new Date());
        qgGoodsTempStock.setGoodsId(goodsId);
        qgGoodsTempStock.setStatus(Constants.StockStatus.lock);
        qgGoodsTempStock.setUpdatedTime(new Date());
        qgGoodsTempStockService.qdtxAddQgGoodsTempStock(qgGoodsTempStock);

        return qgGoodsTempStock.getId();
    }

    /**
     * 在redis中查询用户的抢购状态
     * @param token
     * @param goodsId
     * @return
     * @throws Exception
     */
    @Override
    public ReturnResult flushGetGoodsStatus(String token, String goodsId) throws Exception {

        //1.获取用户信息
        String userJson = redisUtil.getStr(token);
        QgUser qgUser = JSONObject.parseObject(userJson,QgUser.class);
        //2.根据用户信息和商品信息 查询 抢购的状态
        String getFlag = redisUtil.getStr(Constants.getGoodsPrefix+goodsId+":"+qgUser.getId());
        //3.根据抢购状态，返回制定信息
        if (EmptyUtils.isEmpty(getFlag)){
            //正在排队
            return ReturnResultUtils.returnFail(GoodsException.GOODS_IS_GETTING.getCode(),GoodsException.GOODS_IS_GETTING.getMessage());
        }else if (getFlag.equals("0")){
            return ReturnResultUtils.returnFail(GoodsException.GOODS_IS_CLEAR.getCode(),GoodsException.GOODS_IS_CLEAR.getMessage());
        }else {
            return  ReturnResultUtils.returnSuccess();
        }
    }
}
