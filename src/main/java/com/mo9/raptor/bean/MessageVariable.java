package com.mo9.raptor.bean;

/**
 * 发送邮件或短信消息所需要的变量
 * @author zma
 * @date 2018/7/25
 */
public interface MessageVariable {
    /**
     * 产品名称
     */
    String  RAPTOR = "超级飞鼠";
    /**
     * 系统码
     */
    String  SYSTEM_CODE = "RAPTOR";
    /**
     * 主题
     */
    String SUBJECT = "subject";
    /**
     * 短信签名
     */
    String SIGN = "sign";
    /**
     * 验证码
     */
    String CAPTCHA = "captcha";
    /**
     * 币种
     */
    String CURRENCY = "currency";
    /**
     * 金额
     */
    String AMOUNT = "amount";
    /**
     * 同一个模版含有两个相同变量用A或B区分
     */
    String AMOUNT_A = "amountA";
    /**
     * 同一个模版含有两个相同变量用A或B区分
     */
    String AMOUNT_B = "amountB";
    /**
     * 时间
     */
    String TIME = "time";

}
