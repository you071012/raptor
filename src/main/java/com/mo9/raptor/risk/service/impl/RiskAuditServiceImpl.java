package com.mo9.raptor.risk.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mo9.raptor.engine.state.event.impl.AuditResponseEvent;
import com.mo9.raptor.entity.UserCertifyInfoEntity;
import com.mo9.raptor.entity.UserContactsEntity;
import com.mo9.raptor.entity.UserEntity;
import com.mo9.raptor.enums.SourceEnum;
import com.mo9.raptor.risk.entity.TRiskCallLog;
import com.mo9.raptor.risk.repo.RiskCallLogRepository;
import com.mo9.raptor.risk.service.LinkFaceService;
import com.mo9.raptor.risk.service.RiskAuditService;
import com.mo9.raptor.risk.service.RiskRuleEngineService;
import com.mo9.raptor.risk.service.RiskWordService;
import com.mo9.raptor.service.RuleLogService;
import com.mo9.raptor.service.UserCertifyInfoService;
import com.mo9.raptor.service.UserContactsService;
import com.mo9.raptor.service.UserService;
import com.mo9.raptor.utils.IdCardUtils;
import com.mo9.raptor.utils.MobileUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author yngong
 */
@Service("riskAuditService")
public class RiskAuditServiceImpl implements RiskAuditService {

    @Value("${raptor.sockpuppet}")
    private String sockpuppet;

    @Value("${raptor.oss.read-endpoint}")
    private String readEndpoint;

    @Value("${raptor.oss.catalog.callLogReport}")
    private String secondDomain;

    @Value("${risk.calllog.limit}")
    private int callLogLimit;

    @Resource
    private UserContactsService userContactsService;

    private static final Logger logger = LoggerFactory.getLogger(RiskAuditServiceImpl.class);

    @Resource
    private RiskCallLogRepository riskCallLogRepository;

    @Resource
    private LinkFaceService linkFaceService;

    @Resource
    private UserCertifyInfoService userCertifyInfoService;

    @Resource
    private UserService userService;

    @Resource
    private RuleLogService ruleLogService;

    @Resource
    private RiskWordService riskWordService;
    
    @Resource
    private RiskRuleEngineService riskRuleEngineService;

    private static final String WHITE_LIST = "WHITE";

    private static final String ORIGN_CALL = "主叫";

    /**
     * 审核方法
     *
     * @param userCode
     * @return
     */
    @Override
    public AuditResponseEvent audit(String userCode) {
        UserEntity user = userService.findByUserCodeAndDeleted(userCode, false);
        //没收到通话记录则先跳过
        if (!user.getReceiveCallHistory()) {
            return null;
        }

        AuditResponseEvent finalResult = null;
        AuditResponseEvent res;

        //不在白名单内需要多运行一个通话记录规则
        if (!SourceEnum.WHITE.equals(user.getSource())) {
            logger.info(userCode + "开始运行规则[ContactsRule]");
            res = contactsRule(userCode);
            //这里的HIT字段有点歧义，这里的hit为true的话是通过，false为未通过
            ruleLogService.create(userCode, "ContactsRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "ContactsRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[OpenDateRule]");
            res = riskRuleEngineService.openDateRule(userCode);
            ruleLogService.create(userCode, "OpenDateRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "OpenDateRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[MergencyCallTimesRule]");
            res = riskRuleEngineService.mergencyCallTimesRule(userCode);
            ruleLogService.create(userCode, "MergencyCallTimesRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "MergencyCallTimesRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[MergencyHadNoDoneOrderRule]");
            res = riskRuleEngineService.mergencyHadNoDoneOrderRule(userCode);
            ruleLogService.create(userCode, "MergencyHadNoDoneOrderRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "MergencyHadNoDoneOrderRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[MergencyInJHJJBlackListRule]");
            res = riskRuleEngineService.mergencyInJHJJBlackListRule(userCode);
            ruleLogService.create(userCode, "MergencyInJHJJBlackListRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "MergencyInJHJJBlackListRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[CalledTimesByOneLoanCompanyRule]");
            res = riskRuleEngineService.calledTimesByOneLoanCompanyRule(userCode);
            ruleLogService.create(userCode, "CalledTimesByOneLoanCompanyRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "CalledTimesByOneLoanCompanyRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[CalledTimesByDifferentLoanCompanyRule]");
            res = riskRuleEngineService.calledTimesByDifferentLoanCompanyRule(userCode);
            ruleLogService.create(userCode, "CalledTimesByDifferentLoanCompanyRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "CalledTimesByDifferentLoanCompanyRule", null, false, "");
        }
        
        
        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[IdCardRule]");
            res = idCardRule(userCode);
            ruleLogService.create(userCode, "IdCardRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "IdCardRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[RiskWordRule]");
            res = riskWordRule(userCode);
            ruleLogService.create(userCode, "RiskWordRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "RiskWordRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[AgeRule]");
            res = ageRule(userCode);
            ruleLogService.create(userCode, "AgeRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "AgeRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[CallLogRule]");
            res = callLogRule(userCode);
            ruleLogService.create(userCode, "CallLogRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "CallLogRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[ThreeElementCheck]");
            res = threeElementCheck(userCode);
            ruleLogService.create(userCode, "ThreeElementCheck", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "ThreeElementCheck", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[AntiHackRule]");
            res = antiHackRule(userCode);
            ruleLogService.create(userCode, "AntiHackRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "AntiHackRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[LivePicCompareRule]");
            res = livePicCompareRule(userCode);
            ruleLogService.create(userCode, "LivePicCompareRule", res.isPass(), true, res.getExplanation());
            if (!res.isPass()) {
                finalResult = res;
            }
        } else {
            ruleLogService.create(userCode, "LivePicCompareRule", null, false, "");
        }

        if (finalResult == null) {
            logger.info(userCode + "开始运行规则[IdPicCompareRule]");
            res = idPicCompareRule(userCode);
            ruleLogService.create(userCode, "IdPicCompareRule", res.isPass(), true, res.getExplanation());
            finalResult = res;
        } else {
            ruleLogService.create(userCode, "IdPicCompareRule", null, false, "");
        }
        return finalResult;
    }

    private static final int HTTP_OK = 200;
    private static final int ERROR_SCORE_CODE = -1;

    AuditResponseEvent riskWordRule(String userCode) {
        int limit = 15;
        UserContactsEntity userContacts = userContactsService.getByUserCode(userCode);
        String json = userContacts.getContactsList();
        JSONArray jsonArray;
        //有2种JSON格式...
        if (json.startsWith("{")) {
            jsonArray = JSON.parseObject(json).getJSONArray("contact");
        } else {
            jsonArray = JSON.parseArray(json);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < jsonArray.size(); i++) {
            String name = MobileUtil.processMobile(jsonArray.getJSONObject(i).getString("contact_name"));
            stringBuilder.append(name + "|");
        }
        int hitCount = riskWordService.filter(stringBuilder.toString());
        return new AuditResponseEvent(userCode, hitCount <= limit, !(hitCount <= limit) ? "通信录风险词大于15" : "");

    }

    AuditResponseEvent ageRule(String userCode) {
        UserEntity user = userService.findByUserCode(userCode);
        String idCard = user.getIdCard();
        if (idCard == null) {
            return new AuditResponseEvent(userCode, false, "获取身份证失败！");
        }
        if (idCard.length() == 15) {
            idCard = IdCardUtils.conver15CardTo18(idCard);
        }
        int age = IdCardUtils.getAgeByIdCard(idCard);
        boolean pass = age >= 18 && age <= 30;
        return new AuditResponseEvent(userCode, pass, !pass ? "年龄大于30或者小于18" : "");

    }

    AuditResponseEvent idCardRule(String userCode) {
        UserEntity user = userService.findByUserCode(userCode);
        String idCard = user.getIdCard();
        if (idCard == null) {
            return new AuditResponseEvent(userCode, false, "获取身份证失败！");
        }
        if (idCard.length() == 15) {
            idCard = IdCardUtils.conver15CardTo18(idCard);
        }
        try {
            String province = IdCardUtils.getProvinceByIdCard(idCard);
            if ("新疆".equals(province) || "西藏".equals(province)) {
                return new AuditResponseEvent(userCode, false, "西藏-新疆-年龄规则");
            }
            return new AuditResponseEvent(userCode, true, "");
        } catch (Exception e) {
            return new AuditResponseEvent(userCode, false, "身份证非法");
        }
    }


    /**
     * 通讯录规则【通讯录数量大于30条，180天内主动拨打通讯录中电话10次及以上】
     *
     * @param userCode
     * @return
     */
    AuditResponseEvent contactsRule(String userCode) {
        int contactsLimit = 30;
        int orignCallLimit = 10;
        Long days180ts = 180 * 24 * 60 * 60 * 1000L;
        long currentTimeMillis = System.currentTimeMillis();
        UserEntity user = userService.findByUserCode(userCode);
        UserContactsEntity userContacts = userContactsService.getByUserCode(userCode);
        String json = userContacts.getContactsList();
        try {
            JSONArray jsonArray;
            //有2种JSON格式...
            if (json.startsWith("{")) {
                jsonArray = JSON.parseObject(json).getJSONArray("contact");
            } else {
                jsonArray = JSON.parseArray(json);
            }
            //通讯录电话HASHSET
            HashSet<String> allMobileSet = new HashSet<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                String mobile = MobileUtil.processMobile(jsonArray.getJSONObject(i).getString("contact_mobile"));
                if (StringUtils.isNotBlank(mobile)) {
                    allMobileSet.add(mobile);
                }
            }
            if (allMobileSet.size() < contactsLimit) {
                return new AuditResponseEvent(userCode, false, "通讯录数量小于30个");
            }
            int count = 0;
            HashSet<String> inListMobiles = new HashSet<>();
            //MYCAI限制1000条 所以这边有个分页
            List<TRiskCallLog> allCallLog = new ArrayList<>();
            Long lastId = 0L;
            while (true) {
                List<TRiskCallLog> list = riskCallLogRepository.getCallLogByMobileAfterTimestamp(user.getMobile(), (currentTimeMillis - days180ts) / 1000, ORIGN_CALL, lastId);
                allCallLog.addAll(list);
                if (list.size() == 0) {
                    break;
                } else {
                    lastId = list.get(list.size() - 1).getId();
                }
            }
            for (TRiskCallLog tRiskCallLog : allCallLog) {
                //主叫 && 在通讯录内
                if (ORIGN_CALL.equals(tRiskCallLog.getCallMethod()) && allMobileSet.contains(tRiskCallLog.getCallTel())) {
                    count++;
                    inListMobiles.add(tRiskCallLog.getCallTel());
                }
            }
            StringBuilder stringBuilder = new StringBuilder(userCode + "," + user.getMobile() + "在主叫列表里[");
            for (String inListMobile : inListMobiles) {
                stringBuilder.append(inListMobile + ",");
            }
            stringBuilder.append("]");
            logger.info(stringBuilder.toString());
            return new AuditResponseEvent(userCode, count >= orignCallLimit, count >= orignCallLimit ? "" : "180天主动拨打通讯录号码小于10次(共" + count + "次)");
        } catch (Exception e) {
            logger.error(userCode + "解析联系人出错", e);
            logger.info(userCode + json);
            return new AuditResponseEvent(userCode, false, "解析联系人出错！");
        }
    }

    /**
     * 身份证照片与公安照比对
     *
     * @param userCode
     * @return
     */
    AuditResponseEvent idPicCompareRule(String userCode) {
        UserCertifyInfoEntity userCertifyInfo = userCertifyInfoService.findByUserCode(userCode);
        double limit = 0.7;
        if (userCertifyInfo != null) {
            String url = userCertifyInfo.getAccountOcr();
            double score = linkFaceService.judgeOnePerson(userCode, url, userCertifyInfo.getIdCard(), userCertifyInfo.getRealName());
            if (score == ERROR_SCORE_CODE) {
                return new AuditResponseEvent(userCode, false, "调用LINKFACE出错");
            } else {
                return new AuditResponseEvent(userCode, score >= limit, score >= limit ? "" : "活体照与公安库身份证照比对,相似度低于" + limit);
            }
        }
        return new AuditResponseEvent(userCode, false, "查询不到用户" + userCode + "数据");
    }

    /**
     * 活体照与公安照片对比
     *
     * @param userCode
     * @return
     */
    AuditResponseEvent livePicCompareRule(String userCode) {
        UserCertifyInfoEntity userCertifyInfo = userCertifyInfoService.findByUserCode(userCode);
        double limit = 0.7;
        if (userCertifyInfo != null) {
            String url = userCertifyInfo.getAccountFrontImg();
            double score = linkFaceService.judgeIdCardPolice(userCode, url, userCertifyInfo.getIdCard(), userCertifyInfo.getRealName());
            if (score == ERROR_SCORE_CODE) {
                return new AuditResponseEvent(userCode, false, "调用LINKFACE出错");
            } else {
                return new AuditResponseEvent(userCode, score >= limit, score >= limit ? "" : "身份证正面与公安照正面对比,相似度低于" + limit);
            }
        }
        return new AuditResponseEvent(userCode, false, "查询不到用户" + userCode + "数据");
    }

    /**
     * 照片防HACK
     *
     * @param userCode
     * @return
     */
    AuditResponseEvent antiHackRule(String userCode) {
        UserCertifyInfoEntity userCertifyInfo = userCertifyInfoService.findByUserCode(userCode);
        double limit = 0.98;
        if (userCertifyInfo != null) {
            String url = userCertifyInfo.getAccountOcr();
            double score = linkFaceService.preventHack(userCode, url);
            if (score == ERROR_SCORE_CODE) {
                return new AuditResponseEvent(userCode, false, "调用LINKFACE出错");
            } else {
                return new AuditResponseEvent(userCode, score <= limit, score <= limit ? "" : "验证用户照片是否为活体照片,HACK可能性高于" + limit);
            }
        }
        return new AuditResponseEvent(userCode, false, "查询不到用户" + userCode + "数据");
    }

    /**
     * 通话记录规则 【用户最近180天通话记录大于100条】
     *
     * @param userCode
     * @return
     */
    AuditResponseEvent callLogRule(String userCode) {
        Long days180ts = 180 * 24 * 60 * 60 * 1000L;
        long currentTimeMillis = System.currentTimeMillis();
        UserEntity user = userService.findByUserCode(userCode);
        int count = riskCallLogRepository.getCallLogCountAfterTimestamp(user.getMobile(), (currentTimeMillis - days180ts) / 1000);
        int limit = callLogLimit;
        return new AuditResponseEvent(userCode, count >= limit, count >= limit ? "" : "用户最近180天通话记录少于" + limit);
    }

    AuditResponseEvent threeElementCheck(String userCode) {
        UserEntity user = userService.findByUserCode(userCode);
        try {
            if (user != null && StringUtils.isNotBlank(user.getMobile())) {
                String url = readEndpoint + "/" + secondDomain + "/" + sockpuppet + "-" + user.getMobile() + "-report.json";

                //String url = ossFileUpload.buildFileURL(sockpuppet + "-" + user.getMobile() + "-report.json");
                logger.info("读取报告" + url);
                OkHttpClient okHttpClient = new OkHttpClient();
                Response response = okHttpClient.newCall(new Request.Builder().get().url(url).build()).execute();
                if (response.code() == HTTP_OK) {
                    String json = response.body().string();
                    JSONObject jsonObject = JSON.parseObject(json);
                    Integer status = jsonObject.getInteger("status");
                    Integer checkResult = null;
                    String reportIdcard = null;
                    if (status == 0) {
                        if (jsonObject.containsKey("data")) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            if (data.containsKey("user_info")) {
                                JSONObject userInfo = data.getJSONObject("user_info");
                                if (userInfo.containsKey("conclusion_of_3_elements_check")) {
                                    checkResult = userInfo.getInteger("conclusion_of_3_elements_check");
                                }
                                if (userInfo.containsKey("user_idcard")) {
                                    reportIdcard = userInfo.getString("user_idcard");
                                }
                            }
                        }
                    }
                    if (checkResult == null) {
                        return new AuditResponseEvent(userCode, false, userCode + "[" + user.getMobile() + "]报告出错");
                    } else {
                        //1代表匹配
                        if (checkResult == 1) {
                            if (user.getIdCard() == null || reportIdcard == null || !user.getIdCard().toLowerCase().equals(reportIdcard.toLowerCase())) {
                                return new AuditResponseEvent(userCode, false, "用户填的身份证号码与报告匹配不上");
                            }
                        }
                        return new AuditResponseEvent(userCode, checkResult == 1 || checkResult == 5 || checkResult == 6, (checkResult == 1 || checkResult == 5 || checkResult == 6) ? "" : "实名三要素未通过");
                    }
                } else {
                    return new AuditResponseEvent(userCode, false, "报告不存在");
                }
            } else {
                return new AuditResponseEvent(userCode, false, "查询不到该用户，或者该用户手机号为空");
            }
        } catch (Exception e) {
            logger.error(userCode + "检查报告出错", e);
            return new AuditResponseEvent(userCode, false, "致命问题！！检查运营商报告出错");
        }
    }
}
