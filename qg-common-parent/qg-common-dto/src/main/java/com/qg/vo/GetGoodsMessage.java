package com.qg.vo;

import java.io.Serializable;

/**
 * 抢购消息类
 */
public class GetGoodsMessage implements Serializable {

    private String userId;

    private String goodsId;

    private String createDate;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
