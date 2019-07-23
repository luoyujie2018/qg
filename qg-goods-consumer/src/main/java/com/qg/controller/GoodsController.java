package com.qg.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.qg.common.Constants;
import com.qg.dto.ReturnResult;
import com.qg.dto.ReturnResultUtils;
import com.qg.exception.GoodsException;
import com.qg.pojo.QgGoodsTempStock;
import com.qg.pojo.QgUser;
import com.qg.service.LocalGoodsService;
import com.qg.service.QgGoodsTempStockService;
import com.qg.utils.IdWorker;
import com.qg.utils.KafkaUtil;
import com.qg.utils.RedisUtil;
import com.qg.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.qg.exception.GoodsException.USER_REPEAT_GET;


/**
 * 商品控制器
 */

@Controller
@RequestMapping("/api")
public class GoodsController {

    @Autowired
    private LocalGoodsService GoodsService;

    @Autowired
    private KafkaUtil kafkaUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Reference
    private QgGoodsTempStockService qgGoodsTempStockService;


    /**
     * 需求：商品信息，获取库存信息
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/v/queryGoodsById",method = RequestMethod.POST)
    @ResponseBody
    public ReturnResult queryGoodsById(String id) throws Exception {
        return GoodsService.queryGoodsById(id);
    }

    /**
     * 抢购商品的方法
     * @param token
     * @param goodsId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/v/getGoods",method = RequestMethod.POST)
    @ResponseBody
    public ReturnResult getGoods(String token , String goodsId) throws Exception {
        return GoodsService.goodsGetMessage(token,goodsId);
    }


    @RequestMapping(value = "/v/flushResult",method = RequestMethod.POST)
    @ResponseBody
    public ReturnResult flushResult(String token , String goodsId) throws Exception {
        return GoodsService.flushGetGoodsStatus(token,goodsId);
    }




}
