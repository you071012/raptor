package com.mo9.raptor.engine.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 优惠券 Created by xzhang on 2018/9/28.
 */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "t_raptor_coupon")
public class CouponEntity extends AbstractStateEntity {

    /**
     * 优惠券号, 唯一编号
     */
    @Column(name = "coupon_id")
    private String couponId;

    /**
     * 优惠券类型
     */
    @Column(name = "coupon_type")
    private String couponType;

    /**
     * 绑定用户唯一标识
     */
    @Column(name = "owner_id")
    private String ownerId;

    /**
     * 绑定订单号, 绑定订单后有值
     */
    @Column(name = "bound_order_id")
    private String boundOrderId;

    /**
     * 使用特别限制
     */
    @Column(name = "condition")
    private String condition;

    /**
     * 可优惠金额, 绑定订单后有值
     */
    @Column(name = "apply_amount")
    private String applyAmount;

    /**
     * 已入账金额, 绑定订单后有值
     */
    @Column(name = "entry_amount")
    private String entryAmount;

    /**
     * 生效起始日期
     */
    @Column(name = "effective_date")
    private String effectiveDate;

    /**
     * 失效日期
     */
    @Column(name = "expire_date")
    private String expireDate;

    /**
     * 入账结束时间
     */
    @Column(name = "end_time")
    private String endTime;

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getCouponType() {
        return couponType;
    }

    public void setCouponType(String couponType) {
        this.couponType = couponType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getBoundOrderId() {
        return boundOrderId;
    }

    public void setBoundOrderId(String boundOrderId) {
        this.boundOrderId = boundOrderId;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getApplyAmount() {
        return applyAmount;
    }

    public void setApplyAmount(String applyAmount) {
        this.applyAmount = applyAmount;
    }

    public String getEntryAmount() {
        return entryAmount;
    }

    public void setEntryAmount(String entryAmount) {
        this.entryAmount = entryAmount;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
