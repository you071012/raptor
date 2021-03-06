package com.mo9.raptor.service;

import com.mo9.raptor.entity.LoanProductEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by xtgu on 2018/9/16.
 * @author xtgu
 */
public interface LoanProductService {

    /**
     * 查询正在使用的
     * @return
     */
    List<LoanProductEntity> findNotDelete();

    /**
     * 根据金额查询
     * @return
     */
    List<LoanProductEntity> findByAmount(BigDecimal amount);

    /**
     * 根据金额及期限查询唯一产品
     * @param amount    借款金额
     * @param period    周期
     * @return
     */
    LoanProductEntity findByAmountAndPeriod(BigDecimal amount, Integer period);

}
