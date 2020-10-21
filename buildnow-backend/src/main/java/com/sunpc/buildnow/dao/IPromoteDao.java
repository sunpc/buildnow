package com.sunpc.buildnow.dao;

import java.util.List;
import java.util.Map;

public interface IPromoteDao {
    List<Map<String, Object>> getPromoteReq(String status, long USER_ID);

    Map<String, Object> insertRequest(String LOGON_USER_ID, String ENV_CODE, String PROMOTE_MEMBER, String BRANCH_NAME, String ORIGIN_NAME, String FEATURE_NAME, String JOB_LINK_NBR, String status);

    List<Map<String, Object>> getUserInfo(String envCode, String userName);

    Map<String, Object> saveUserInfo(String userName, String envCode,String envUserID, String envPassword, String gitURI);

    Map<String, Object> saveGit(String userName, String envCode, String gitURI);

    List<Map<String, Object>> getBusyLink(String envCode,String origin);

    Map<String, Object> updateStatusFromPromoteReq(String requestID, String status, String jobLinkNbr);

    List<Map<String, Object>> getEnvInfo(String envCode, String defaultName);

    Map<String, Object> insertPromoteStep(String requestID, String step_seq);

    Map<String, Object> updatePromoteStepStatus(String requestID,String stepId,String status);

    Map<String, Object> updatePromoteStepStatus(String envCode, String userName, String feature, String promoteMember, String branch, String origin, String status);

    Map<String, Object> getReqInfo();

    Map<String, Object> getCompInfo();

    Map<String, Object> getFeature(String envCode);

    Map<String, Object> addFeature(String envCode, String featureName, String featureDesc,  int lastActUserId);

    Map<String, Object> updateFeatureStatus(String envCode, String featureName, String apprStatus,  int lastActUserId);

    Map<String, Object> getStatusFeature(String envCode,String status);

}
