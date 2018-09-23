package com.mo9.raptor.task;

import com.mo9.raptor.engine.entity.PayOrderEntity;
import com.mo9.raptor.engine.enums.StatusEnum;
import com.mo9.raptor.engine.service.IPayOrderService;
import com.mo9.raptor.engine.service.impl.PayOrderServiceImpl;
import com.mo9.raptor.entity.PayOrderLogEntity;
import com.mo9.raptor.service.PayOrderLogService;
import com.mo9.raptor.utils.GatewayUtils;
import com.mo9.raptor.utils.log.Log;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by xtgu on 2018/9/22.
 * @author xtgu
 * 还款订单定时器
 */
@Component("payOrderTask")
public class PayOrderTask {

    private static Logger logger = Log.get();

    @Autowired
    private PayOrderLogService payOrderLogService ;

    @Autowired
    private IPayOrderService payOrderService ;

    @Autowired
    private GatewayUtils gatewayUtils ;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void notSuccessOrderTask(){
        List<PayOrderEntity> list = payOrderService.findByStatus(StatusEnum.DEDUCTING) ;
        for(PayOrderEntity payOrderEntity : list){
            PayOrderLogEntity payOrderLogEntity = payOrderLogService.getByPayOrderId(payOrderEntity.getOrderId());
            if(!StringUtils.isBlank(payOrderLogEntity.getDealCode())){
                gatewayUtils.gatewayMqPush(payOrderLogEntity.getDealCode());
            }
        }
    }
}
