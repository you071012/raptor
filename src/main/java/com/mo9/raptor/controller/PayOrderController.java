package com.mo9.raptor.controller;

import com.alibaba.fastjson.JSONObject;
import com.mo9.raptor.bean.BaseResponse;
import com.mo9.raptor.bean.ReqHeaderParams;
import com.mo9.raptor.bean.req.CashRenewalReq;
import com.mo9.raptor.bean.req.CashRepayReq;
import com.mo9.raptor.bean.res.ChannelDetailRes;
import com.mo9.raptor.bean.res.PayOderChannelRes;
import com.mo9.raptor.engine.entity.PayOrderEntity;
import com.mo9.raptor.engine.enums.StatusEnum;
import com.mo9.raptor.engine.service.IPayOrderService;
import com.mo9.raptor.entity.PayOrderLogEntity;
import com.mo9.raptor.enums.*;
import com.mo9.raptor.utils.IDWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 还款
 * Created by xzhang on 2018/9/13.
 */
@RestController()
@RequestMapping("/cash")
public class PayOrderController {

    private static final Logger logger = LoggerFactory.getLogger(PayOrderController.class);

    @Autowired
    private IDWorker idWorker;

    @Autowired
    private IPayOrderService payOrderService;

    @Value("${raptor.sockpuppet}")
    private String sockpuppet;

    /**
     * 还清
     * @param req
     * @return
     */
    @PostMapping("/repay")
    public BaseResponse<JSONObject> repay(@Valid @RequestBody CashRepayReq req, HttpServletRequest request) {
        BaseResponse<JSONObject> response = new BaseResponse<JSONObject>();
        String userCode = request.getHeader(ReqHeaderParams.ACCOUNT_CODE);
        // TODO: 检查用户

        RepayChannelTypeEnum repayChannelTypeEnum = RepayChannelTypeEnum.getByChannelType(req.getChannelType());
        if (repayChannelTypeEnum == null) {
            return response.buildFailureResponse(ResCodeEnum.NO_REPAY_CHANNEL);
        }

        // 获得订单
        String loanOrderId = req.getOrderId();

        String orderId = sockpuppet + "-" + String.valueOf(idWorker.nextId());
        PayOrderEntity payOrder = new PayOrderEntity();
        payOrder.setOrderId(orderId);
        payOrder.setStatus(StatusEnum.PENDING.name());
        payOrder.setOwnerId(userCode);
        payOrder.setType(PayTypeEnum.REPAY_IN_ADVANCE.name());
        payOrder.setApplyNumber(new BigDecimal(100));
        payOrder.setPostponeDays(0);
        payOrder.setPayCurrency(CurrencyEnum.getDefaultCurrency().name());
        payOrder.setLoanOrderId(loanOrderId);
        payOrder.setChannel(repayChannelTypeEnum.name());
        payOrder.create();
        PayOrderLogEntity payOrderLog = new PayOrderLogEntity();
        payOrderLog.setOrderId(payOrder.getLoanOrderId());
        payOrderLog.setPayOrderId(payOrder.getOrderId());
        payOrderLog.setBankCard(req.getBankCard());
        payOrderLog.setBankMobile(req.getBankMobile());
        payOrderLog.setIdCard(req.getIdCard());
        payOrderLog.setUserName(req.getUserName());
        payOrderLog.setChannel(repayChannelTypeEnum.name());
        payOrderLog.create();
        payOrderService.savePayOrderAndLog(payOrder, payOrderLog);


        PayOderChannelRes res = new PayOderChannelRes();
        JSONObject data = new JSONObject();
        data.put("entities", res);
        return response.buildSuccessResponse(data);
    }

    /**
     * 续期
     * @param req
     * @return
     */
    @PostMapping("/renewal")
    public BaseResponse<JSONObject> renewal(@Valid @RequestBody CashRenewalReq req, HttpServletRequest request) {
        BaseResponse<JSONObject> response = new BaseResponse<JSONObject>();
        String userCode = request.getHeader(ReqHeaderParams.ACCOUNT_CODE);

        RepayChannelTypeEnum repayChannelTypeEnum = RepayChannelTypeEnum.getByChannelType(req.getChannelType());
        if (repayChannelTypeEnum == null) {
            return response.buildFailureResponse(ResCodeEnum.NO_REPAY_CHANNEL);
        }

        Boolean checkRenewableDays = RenewableDaysEnum.checkRenewableDays(req.getPeriod());
        if (checkRenewableDays) {
            return response.buildFailureResponse(ResCodeEnum.INVALID_RENEWAL_DAYS);
        }

        // 获得订单
        String loanOrderId = req.getOrderId();

        String orderId = sockpuppet + "-" + String.valueOf(idWorker.nextId());
        PayOrderEntity payOrder = new PayOrderEntity();
        payOrder.setOrderId(orderId);
        payOrder.setStatus(StatusEnum.PENDING.name());
        payOrder.setOwnerId(userCode);
        payOrder.setType(PayTypeEnum.REPAY_POSTPONE.name());
//        payOrder.setApplyNumber();
        payOrder.setPostponeDays(req.getPeriod());
        payOrder.setPayCurrency(CurrencyEnum.getDefaultCurrency().name());
        payOrder.setLoanOrderId(loanOrderId);
        payOrder.setChannel(repayChannelTypeEnum.name());
        payOrder.create();

        PayOrderLogEntity payOrderLog = new PayOrderLogEntity();
        payOrderLog.setOrderId(payOrder.getLoanOrderId());
        payOrderLog.setPayOrderId(payOrder.getOrderId());
        payOrderLog.setBankCard(req.getBankCard());
        payOrderLog.setBankMobile(req.getBankMobile());
        payOrderLog.setIdCard(req.getIdCard());
        payOrderLog.setUserName(req.getUserName());
        payOrderLog.setChannel(repayChannelTypeEnum.name());
        payOrderLog.create();
        payOrderService.savePayOrderAndLog(payOrder, payOrderLog);

        PayOderChannelRes res = new PayOderChannelRes();
        JSONObject data = new JSONObject();
        data.put("entities", res);
        return response.buildSuccessResponse(data);
    }

    /**
     * 获取渠道列表
     * @return
     */
    @GetMapping("/get_repay_channels")
    public BaseResponse<JSONObject> getRepayChannels () {
        BaseResponse<JSONObject> response = new BaseResponse<JSONObject>();
        List<ChannelDetailRes> channels = new ArrayList<ChannelDetailRes>();
        for (RepayChannelTypeEnum channelType : RepayChannelTypeEnum.values()) {
            ChannelDetailRes res = new ChannelDetailRes();
            res.setChannelName(channelType.getChannelName());
            res.setChannelType(channelType.getChannelType());
            res.setUseType(channelType.getChannelUseType().getDesc());
            channels.add(res);
        }
        JSONObject data = new JSONObject();
        data.put("entities", channels);
        return response.buildSuccessResponse(data);
    }

}