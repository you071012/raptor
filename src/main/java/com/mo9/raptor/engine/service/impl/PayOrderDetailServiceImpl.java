package com.mo9.raptor.engine.service.impl;

import com.mo9.raptor.bean.res.RepayDetailRes;
import com.mo9.raptor.engine.entity.PayOrderDetailEntity;
import com.mo9.raptor.engine.repository.PayOrderDetailRepository;
import com.mo9.raptor.engine.service.IPayOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xzhang on 2018/9/17.
 */
@Service("payOrderDetailServiceImpl")
public class PayOrderDetailServiceImpl implements IPayOrderDetailService {

    @Autowired
    private PayOrderDetailRepository payOrderDetailRepository;

    @Override
    public List<PayOrderDetailEntity> listByOrderId(String orderId) {
        return payOrderDetailRepository.listByOrderId(orderId);
    }

    @Override
    public List<PayOrderDetailEntity> listByPayOrderId(String payOrderId) {
        return payOrderDetailRepository.listByPayOrderId(payOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PayOrderDetailEntity> saveItem(List<PayOrderDetailEntity> payOrderDetails) {
        if (payOrderDetails != null && payOrderDetails.size() > 0) {
            for (PayOrderDetailEntity payOrderDetail : payOrderDetails) {
                payOrderDetailRepository.save(payOrderDetail);
            }
        }
        return payOrderDetails;
    }

    @Override
    public List<RepayDetailRes> getRepayDetail(String payOrderId) {
        List<RepayDetailRes> resList = new ArrayList<RepayDetailRes>();
        List<PayOrderDetailEntity> payOrderDetailEntities = payOrderDetailRepository.listByPayOrderId(payOrderId);
        for (PayOrderDetailEntity payOrderDetailEntity : payOrderDetailEntities) {
            RepayDetailRes res = new RepayDetailRes();
            res.setFieldType(payOrderDetailEntity.getField());
            res.setNumber(payOrderDetailEntity.getPaid());
            resList.add(res);
        }
        return resList;
    }
}
