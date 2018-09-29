package com.mo9.raptor.engine.service;

import com.mo9.raptor.engine.entity.CouponEntity;

import java.util.List;

/**
 * 优惠券service
 * Created by xzhang on 2018/9/28.
 */
public interface CouponService {

    /** 获取订单有效的优惠券 */
    CouponEntity getEffectiveByLoanOrderId (String loanOrderId);
    /**
     * 获取用户最新的一张未绑定的订单
     * @param userCode
     * @return
     */
    CouponEntity getUserUnboundCoupon(String userCode);

    /**
     * 优惠券号获取优惠券
     * @param couponId
     * @return
     */
    CouponEntity getByCouponId(String couponId);

}
