package com.qg.exception;
/***
 * 支付项目异常
 */
public enum PayException {
    WX_PAY_BUZY(1401,"微信支付繁忙,请使用其他的支付方式");
    private Integer code;
    private String message;

    PayException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
