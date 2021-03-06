package com.mo9.raptor.service.impl;

import com.mo9.raptor.entity.RiskTelYellowPage;
import com.mo9.raptor.repository.RiskTelYellowPageRepository;
import com.mo9.raptor.service.RiskTelYellowPageService;
import com.mo9.raptor.utils.log.Log;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wtwei .
 * @date 2018/10/8 .
 * @time 14:46 .
 */

@Service("riskTelYellowPageService")
public class RiskTelYellowPageServiceImpl implements RiskTelYellowPageService {
    private static Logger logger = Log.get();
    
    @Resource
    private RiskTelYellowPageRepository riskTelYellowPageRepository;
    
    @Override
    public RiskTelYellowPage saveOrUpdate(RiskTelYellowPage entity) {
        try {
            if (entity.getCreateTime() == null){
                entity.setCreateTime(System.currentTimeMillis());
            }
            return riskTelYellowPageRepository.save(entity);
        }catch (Exception e){
            logger.warn("保存黄页电话出错，tel: {}", entity.getFormatTel());
        }
        return null;
    }

}
