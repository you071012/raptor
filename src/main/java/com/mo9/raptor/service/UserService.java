package com.mo9.raptor.service;

import com.mo9.raptor.bean.req.PageReq;
import com.mo9.raptor.engine.enums.StatusEnum;
import com.mo9.raptor.entity.UserEntity;
import com.mo9.raptor.enums.BankAuthStatusEnum;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author zma
 * @date 2018/9/13
 */
public interface UserService {


    UserEntity findByUserCode(String userCode);

    /**
     * 根据userCode查询是否禁用的用户
     *
     * @param userCode
     * @param isDelete
     * @return
     */
    UserEntity findByUserCodeAndDeleted(String userCode, boolean isDelete);
    /**
     * 根据mobile查询是否禁用的用户
     * @param mobile
     * @param isDelete
     * @return
     */
    UserEntity findByMobileAndDeleted(String mobile, boolean isDelete);

    /**
     * 根据绑定手机号查询用户是否存在
     * @param mobile
     * @return
     */
    UserEntity findByMobile(String mobile);

    UserEntity save (UserEntity userEntity);

    /**
     * 根据userCode和状态查询用户
     * @param userCode   用户
     * @param status     状态
     * @return
     */
    UserEntity findByUserCodeAndStatus(String userCode, StatusEnum status);

    /**
     * 修改通话记录状态
     * @param userEntity
     * @param b
     */
    void updateCallHistory(UserEntity userEntity, boolean b) throws Exception;

    /**
     * 修改身份信息状态
     * @param userEntity
     * @param b
     */
    void updateCertifyInfo(UserEntity userEntity, boolean b) throws Exception;

    /**
     * 修改通讯录状态
     * @param userEntity
     * @param b
     */
    void updateMobileContacts(UserEntity userEntity, boolean b) throws Exception;
    /**
     * 修改是否收到通讯录数据
     * @param userCode
     * @param b
     */
    void updateReceiveCallHistory(String userCode, boolean b) throws Exception;

    /**
     * 修改银行卡认证状态
     * @param userEntity
     * @param statusEnum
     */
    void updateBankAuthStatus(UserEntity userEntity, BankAuthStatusEnum statusEnum) throws Exception;

    /**
     * 检查身份基本信息认证，银行卡认证，手机通讯录认证，手机通话记录是否上传，是否收到通讯录数据
     * @param userEntity
     * @return
     */
    void checkAuditStatus(UserEntity userEntity) throws Exception;

    /**
     * 当天是否允许新用户进入
     * @return
     */
    boolean isaAllowNewUser();

    /**
     * 当天允许新用户注册增加计数
     */
    void addAllowNewUserNum();
    /**
     * 查找没有通话记录报告的用户
     * @return
     * @throws Exception
     */
    List<UserEntity> findNoCallLogReports() throws Exception;

    /**
     * 获取用户注册数量
     * @param source
     * @param pageReq
     * @return
     */
    Page<Map<String,Object>> getRegisterUserNumber(String source,PageReq pageReq);
    /**
     * 拉黑用户
     * @param userEntity
     * @param desc
     */
    void toBlackUser(UserEntity userEntity, String desc) throws Exception;

    /**
     * 根据批量手机号查询用户
     * @param mobiles
     * @return
     */
    List<UserEntity> findByMobiles(List<String> mobiles);

    /**
     * 查询需要人工审核的用户
     * @return
     */
    List<UserEntity> findManualAuditUser(String source);

    /**
     * 查询当前操作人待审核的用户
     * @param source
     * @param operateId
     * @return
     */
     List<UserEntity> findManualAuditUserBuyOperateId(String source,String operateId);
    /**
     * 查询填资料的用户数
     * @param source
     * @return
     */
    List<Map<String,Object>> toAuditUserCount(String source);

    /**
     * 查询去借款的用户数
     * @param source
     * @return
     */
    List<Map<String,Object>> getChannelLoanCount(String source);

    /**
     * 从审核中状态回退到信息采集中
     * @param userCode
     * @return
     */
    Boolean backToCollecting(String userCode,String description);

    /**
     * 审核中直接拒绝
     * @return
     */
    Boolean directRejection(String userCode,String description);

    Long countByStatus(StatusEnum status);

    List<UserEntity> findManualAuditUserBuyOperateId(String operateId);
}