package com.mo9.raptor.repository;

import com.mo9.raptor.entity.CashAccountLogEntity;
import com.mo9.raptor.enums.BalanceTypeEnum;
import com.mo9.raptor.enums.BusinessTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by xtgu on 2018/9/12.
 * @author xtgu
 */
public interface CashAccountLogRepository extends JpaRepository<CashAccountLogEntity,Long> , JpaSpecificationExecutor<CashAccountLogEntity> {

    /**
     * 根据 业务流水号 和业务类型查询
     * @param businessNo
     * @param repay
     * @return
     */
    CashAccountLogEntity findByBusinessNoAndBusinessType(String businessNo, BusinessTypeEnum repay);

    /**
     * 根据 业务流水号 和业务类型 和 金额出入账类型查询
     * @param businessNo
     * @param businessTypeEnum
     * @param balanceType
     * @return
     */
    CashAccountLogEntity findByBusinessNoAndBusinessTypeAndBalanceType(String businessNo, BusinessTypeEnum businessTypeEnum, BalanceTypeEnum balanceType);
}
