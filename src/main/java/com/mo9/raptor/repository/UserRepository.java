package com.mo9.raptor.repository;

import com.mo9.raptor.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

/**
 * @author zma
 * @date 2018/9/13
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

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
     * 根据绑定手机号查询所有用户是否存在
     *
     * @param mobile
     * @return
     */
    UserEntity findByMobile(String mobile);

    /**
     * 根据状态查找用户
     *
     * @param status
     * @return
     */
    @Query(value = "select t from UserEntity t where t.status = ?1")
    List<UserEntity> findByStatus(String status);

    /**
     * 根据用户手机号和是否删除 查询
     *
     * @param mobile
     * @param isDelete
     * @return
     */
    UserEntity findByMobileAndDeleted(String mobile, boolean isDelete);

    /**
     * 根据userCode和状态查询用户
     *
     * @param userCode 用户
     * @param status   状态
     * @return
     */
    @Query(value = "select t from UserEntity t where t.userCode = ?1 and t.status = ?2 and t.deleted = 0")
    UserEntity findByUserCodeAndStatus(String userCode, String status);


    /**
     * 查询没有通话记录报告的用户
     *
     * @return
     */
    @Query(value = "select t from UserEntity t where t.status='AUDITING' and t.receiveCallHistory = 0 and t.callHistory=1 and t.bankCardSet=1 and t.certifyInfo=1 and t.mobileContacts=1 and t.deleted = 0")
    List<UserEntity> findNoCallLogReports();

    @Query(value = "select COUNT(*) num,FROM_UNIXTIME(create_time/1000,'%Y-%m-%d ') date ,sub_source,source from t_raptor_user  where source = ?1 and FROM_UNIXTIME(create_time/1000,'%Y-%m-%d ')" +
            " >= FROM_UNIXTIME(?2/1000,'%Y-%m-%d ') and  FROM_UNIXTIME(create_time/1000,'%Y-%m-%d ')  <=  FROM_UNIXTIME(?3/1000,'%Y-%m-%d ') and sub_source IS NOT NULL and sub_source != ''" +
            " GROUP BY FROM_UNIXTIME(create_time/1000,'%Y-%m-%d '),sub_source",
            countQuery = "select COUNT(*) from (select COUNT(*) num ,FROM_UNIXTIME(create_time/1000,'%Y-%m-%d ') date ,sub_source from t_raptor_user  where source = ?1 and " +
                    " FROM_UNIXTIME(create_time/1000,'%Y-%m-%d ') >= FROM_UNIXTIME(?2/1000,'%Y-%m-%d ') and  FROM_UNIXTIME(create_time/1000,'%Y-%m-%d ')  <= " +
                    " FROM_UNIXTIME(?3/1000,'%Y-%m-%d ') and sub_source IS NOT NULL and sub_source != '' GROUP BY FROM_UNIXTIME(create_time/1000,'%Y-%m-%d '),sub_source) t",
            nativeQuery = true)
    Page<Map<String, Object>> findRegisterUserNumber(String source, Long startTime, Long endTime, Pageable pageable);

    @Query(value = "SELECT  count(*)>0 from t_raptor_user where `status` = 'BLACK' and (mobile = ?1 or id_card = ?1)", nativeQuery = true)
    Integer inBlackList(String value);

    @Query(value = "SELECT  * from t_raptor_user where mobile in ?1 and deleted = 0", nativeQuery = true)
    List<UserEntity> findByMobiles(List<String> mobiles);

    @Query(value = "SELECT  * from t_raptor_user where source = ?1 and deleted = 0 and status in('MANUAL','PASSED','REJECTED')", nativeQuery = true)
    List<UserEntity> findManualAuditUser(String source);

    @Query(value = "SELECT  * from t_raptor_user where source = ?1 and deleted = 0 and status = 'MANUAL'", nativeQuery = true)
    List<UserEntity> findManualAuditUserNew(String source);

    @Query(value = "select u.* from t_audit_operation_record r JOIN t_raptor_user u ON  r.user_code = u.user_code where u.source = ?1 and r.operate_id = ?2 and u.status = 'MANUAL'", nativeQuery = true)
    List<UserEntity> findManualAuditUserBuyOperateId(String source,String operateId);
    @Query(value = "select u.* from t_audit_operation_record r JOIN t_raptor_user u ON  r.user_code = u.user_code where r.operate_id = ?1 and u.status = 'MANUAL' ORDER BY u.auth_time asc", nativeQuery = true)
    List<UserEntity> findManualAuditUserBuyOperateId(String operateId);

    @Query(value = "SELECT  COUNT(*) audit_num,source,sub_source,FROM_UNIXTIME(create_time / 1000,'%Y-%m-%d ') date from t_raptor_user where source = ?1 and deleted = 0 and status not in('BLACK','COLLECTING')GROUP BY FROM_UNIXTIME(create_time / 1000,'%Y-%m-%d '),sub_source order by id asc", nativeQuery = true)
    List<Map<String,Object>> toAuditUserCount(String source);

    @Query(value = "SELECT count(*) loan_num,source,sub_source,FROM_UNIXTIME(u.create_time / 1000,'%Y-%m-%d ') date from t_raptor_loan_order o  LEFT JOIN t_raptor_user u on o.owner_id = u.user_code where o.`status` in ('LENT','PAYOFF') and u.source = ?1  GROUP BY FROM_UNIXTIME(u.create_time / 1000,'%Y-%m-%d '),sub_source", nativeQuery = true)
    List<Map<String,Object>> getChannelLoanCount(String source);


    Long countByStatus(String name);
}
