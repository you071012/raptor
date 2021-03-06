package com.mo9.raptor.service.impl;

import com.mo9.raptor.bean.req.PageReq;
import com.mo9.raptor.engine.enums.AuditResultEnum;
import com.mo9.raptor.engine.enums.StatusEnum;
import com.mo9.raptor.engine.state.action.impl.user.UserAuditAction;
import com.mo9.raptor.engine.state.event.impl.AuditLaunchEvent;
import com.mo9.raptor.engine.state.event.impl.AuditResponseEvent;
import com.mo9.raptor.engine.state.event.impl.user.BlackEvent;
import com.mo9.raptor.engine.state.launcher.IEventLauncher;
import com.mo9.raptor.entity.UserEntity;
import com.mo9.raptor.enums.BankAuthStatusEnum;
import com.mo9.raptor.enums.DictEnums;
import com.mo9.raptor.enums.SourceEnum;
import com.mo9.raptor.redis.RedisParams;
import com.mo9.raptor.redis.RedisServiceApi;
import com.mo9.raptor.repository.UserRepository;
import com.mo9.raptor.service.DictService;
import com.mo9.raptor.service.UserService;
import com.mo9.raptor.utils.DateUtils;
import com.mo9.risk.service.RiskAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zma
 * @date 2018/9/13
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IEventLauncher userEventLauncher;

    @Resource
    private RiskAuditService riskAuditService;

    @Resource
    private DictService dictService;

    @Resource
    private RedisServiceApi redisServiceApi;

    @Resource(name = "raptorRedis")
    private RedisTemplate redisTemplate;

    @Override
    public UserEntity findByUserCode(String userCode) {
        return userRepository.findByUserCode(userCode);
    }


    @Override
    public UserEntity findByUserCodeAndDeleted(String userCode, boolean isDelete) {
        if (StringUtils.isEmpty(userCode)){
            return null;
        }
        return userRepository.findByUserCodeAndDeleted(userCode,isDelete);
    }

    @Override
    public UserEntity findByMobileAndDeleted(String mobile, boolean isDelete) {
        return userRepository.findByMobileAndDeleted(mobile,isDelete);
    }

    @Override
    public UserEntity findByMobile(String mobile) {
        return userRepository.findByMobile(mobile);
    }

    @Override
    public UserEntity save(UserEntity userEntity) {
        userEntity.setUpdateTime(System.currentTimeMillis());
        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity findByUserCodeAndStatus(String userCode, StatusEnum status) {
        return userRepository.findByUserCodeAndStatus(userCode, status.name());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCallHistory(UserEntity userEntity, boolean b) throws Exception {
        userEntity.setCallHistory(b);
        if(b){
            userEntity.setCallHistoryTime(System.currentTimeMillis());
        }
        this.save(userEntity);
        checkAuditStatus(userEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCertifyInfo(UserEntity userEntity, boolean b) throws Exception {
        userEntity.setCertifyInfo(b);
        if(b){
            userEntity.setCertifyInfoTime(System.currentTimeMillis());
        }
        this.save(userEntity);
        checkAuditStatus(userEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMobileContacts(UserEntity userEntity, boolean b) throws Exception {
        userEntity.setMobileContacts(b);
        if(b){
            userEntity.setMobileContactsTime(System.currentTimeMillis());
        }
        this.save(userEntity);
        checkAuditStatus(userEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReceiveCallHistory(String userCode, boolean b) throws Exception {
        UserEntity userEntity = userRepository.findByUserCode(userCode);
        userEntity.setReceiveCallHistory(b);
        userEntity.setCallHistory(b);
        if(b){
            userEntity.setReceiveCallHistoryTime(System.currentTimeMillis());
        }
        this.save(userEntity);
        //如果b非true直接结束方法
        if(!b){
            logger.info("当前状态修改不是为true，方法结束userCode={}", userCode);
            return;
        }
        String status = userEntity.getStatus();
        if(StatusEnum.AUDITING.name().equals(status)){
            //通知风控
            logger.info("当前状态是AUDITING，直接通知风控，无需调用状态机再次修改状态userCode={}", userCode);
            new UserAuditAction(userCode, userEventLauncher, riskAuditService).run();
        }else{
            //调用状态机
            logger.info("当前状态不是AUDITING，需调用状态机修改状态userCode={}", userCode);
            checkAuditStatus(userEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBankAuthStatus(UserEntity userEntity, BankAuthStatusEnum statusEnum) throws Exception {
        logger.info("用户更新银行卡状态 : " + userEntity.getUserCode());
        userEntity.setBankAuthStatus(statusEnum.name());
        if (BankAuthStatusEnum.SUCCESS == statusEnum){
            userEntity.setBankCardSet(true);
            userEntity.setBankCardSetTime(System.currentTimeMillis());
        }else {
            userEntity.setBankCardSet(false);
        }
        userRepository.save(userEntity);
        checkAuditStatus(userEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class )
    public void checkAuditStatus(UserEntity userEntity) throws Exception {
        Boolean certifyInfo = userEntity.getCertifyInfo();
        Boolean mobileContacts = userEntity.getMobileContacts();
        Boolean callHistory = userEntity.getCallHistory();
        Boolean bankCardSet = userEntity.getBankCardSet();
        String status = userEntity.getStatus() ;
        if(StatusEnum.COLLECTING.name().equals(status)){
            if (callHistory && certifyInfo && mobileContacts && bankCardSet) {
                // 信息采集完成 发起审核
                userEntity.setAuthTime(System.currentTimeMillis());
                AuditLaunchEvent auditLaunchEvent = new AuditLaunchEvent(userEntity.getUserCode(),userEntity.getUserCode());
                userEventLauncher.launch(auditLaunchEvent);
            }
        }

    }

    @Override
    public boolean isaAllowNewUser() {
        String dictName = dictService.findDictName(DictEnums.REGISTER_NUM.getDictTypeNo(), DateUtils.formartDate(new Date()));
        if(org.apache.commons.lang.StringUtils.isBlank(dictName)){
            return false;
        }
        int num = Integer.valueOf(dictName);
        String key = RedisParams.ALLOW_NEW_USER_REGISTER_KEY + DateUtils.formartDate(new Date());
        Integer redisNum = (Integer) redisServiceApi.get(key, redisTemplate);
        if(redisNum == null){
            redisNum = 0;
        }
        if(redisNum < num){
            return true;
        }
        return false;
    }

    @Override
    public void addAllowNewUserNum(){
        String key = RedisParams.ALLOW_NEW_USER_REGISTER_KEY + DateUtils.formartDate(new Date());
        Integer num = (Integer) redisServiceApi.get(key, redisTemplate);
        if(num == null){
            num = 1;
            redisServiceApi.set(key, num, RedisParams.EXPIRE_1D, redisTemplate);
            return;
        }
        redisServiceApi.increment(key, 1L, redisTemplate);
    }

    @Override
    public List<UserEntity> findNoCallLogReports() throws Exception {
        return userRepository.findNoCallLogReports();
    }

    @Override
    public Page<Map<String,Object>> getRegisterUserNumber(String source, PageReq pageReq) {
        Pageable pageable = new PageRequest(pageReq.getPage() -1, pageReq.getSize(), pageReq.getDirection(), pageReq.getProperty());
        Page<Map<String,Object>> registerUserNumber = userRepository.findRegisterUserNumber(source, pageReq.getStartTime(), pageReq.getEndTime(), pageable);
        return registerUserNumber;
    }
    @Override
    public void toBlackUser(UserEntity userEntity, String desc) throws Exception {
        BlackEvent event = new BlackEvent(userEntity.getUserCode(), desc);
        userEventLauncher.launch(event);
    }

    @Override
    public List<UserEntity> findByMobiles(List<String> mobiles) {
        return userRepository.findByMobiles(mobiles);
    }

    @Override
    public List<UserEntity> findManualAuditUser(String source) {
        if (SourceEnum.NEW.name().equals(source)||SourceEnum.WHITE.name().equals(source)){
            return userRepository.findManualAuditUserNew(source);
        }
     return  userRepository.findManualAuditUser(source);
    }
    @Override
    public List<UserEntity> findManualAuditUserBuyOperateId(String source,String operateId) {
     return  userRepository.findManualAuditUserBuyOperateId(source,operateId);
    }

    @Override
    public List<Map<String, Object>> toAuditUserCount(String source) {
        return userRepository.toAuditUserCount(source);
    }

    @Override
    public List<Map<String, Object>> getChannelLoanCount(String source) {
        return userRepository.getChannelLoanCount(source);
    }

    @Override
    public Boolean backToCollecting(String userCode, String description) {
        AuditResponseEvent responseEvent = new AuditResponseEvent(userCode,description, AuditResultEnum.COLLECTING);
        try {
            userEventLauncher.launch(responseEvent);
        } catch (Exception e) {
            logger.error("用户审核，从审核中到信息采集中出现异常[{}],[{}]",userCode,description,e);
            return false;
        }
        return true;
    }

    @Override
    public Boolean directRejection(String userCode, String description) {
        AuditResponseEvent responseEvent = new AuditResponseEvent(userCode,description, AuditResultEnum.REJECTED);
        try {
            userEventLauncher.launch(responseEvent);
        } catch (Exception e) {
            logger.error("用户审核，直接拒绝服务出现异常[{}],[{}]",userCode,description,e);
            return false;
        }
        return true;
    }

    @Override
    public Long countByStatus(StatusEnum status) {
        Long total = userRepository.countByStatus(status.name());
        if (total == null){
            total =0L ;
        }
        return total;
    }

    @Override
    public List<UserEntity> findManualAuditUserBuyOperateId(String operateId) {
        return userRepository.findManualAuditUserBuyOperateId(operateId);
    }
}
