package com.sunpc.buildnow.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PromoteDao implements IPromoteDao {

    @Autowired
    @Qualifier("primaryJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getPromoteReq(String status, long USER_ID) {
        String sql = "SELECT REQUEST_ID, USER_ENV.URI AS URI, REQ.ENV_CODE AS ENV_CODE, USER_ENV.LOGON_USER_ID AS LOGON_USER_ID, USER_ENV.LOGON_USER_PWD AS LOGON_USER_PWD, ";
        sql += "PROMOTE_MEMBER, BRANCH_NAME, ORIGIN_NAME, FEATURE_NAME, JOB_LINK_NBR FROM IBD_O_PROMOTE_REQUEST REQ ";
        sql += "JOIN IBD_O_USER_ENV USER_ENV ON REQ.USER_ID = USER_ENV.USER_ID AND REQ.ENV_CODE = USER_ENV.ENV_CODE ";
        sql += "WHERE REQ.STATUS = '"+status+"' AND REQ.USER_ID = "+ USER_ID +" LIMIT 1";

        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> insertRequest(String USER_EMAIL, String ENV_CODE, String PROMOTE_MEMBER, String BRANCH_NAME, String ORIGIN_NAME, String FEATURE_NAME, String JOB_LINK_NBR, String status) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //System.out.println(PROMOTE_MEMBER + BRANCH_NAME + ORIGIN_NAME + FEATURE_NAME + JOB_LINK_NBR + USER_EMAIL + ENV_CODE);

        String sql = "INSERT INTO IBD_O_PROMOTE_REQUEST ";
        sql += "(USER_ID, ENV_CODE, PROMOTE_MEMBER, BRANCH_NAME, ORIGIN_NAME, FEATURE_NAME, JOB_LINK_NBR, STATUS, LAST_ACT_USER_ID, LAST_ACT_SYS_CD, CREATE_TIMESTAMP, LAST_UPT_TIME) ";
        sql += "SELECT USER_ID, '" + ENV_CODE + "', '" + PROMOTE_MEMBER + "','" + BRANCH_NAME + "' ,'" + ORIGIN_NAME + "','" + FEATURE_NAME + "','" + JOB_LINK_NBR + "','"+status+"', 0,'IBUILD',  CURRENT TIMESTAMP, CURRENT TIMESTAMP " +
                "FROM IBD_O_USER WHERE USER_EMAIL = '" + USER_EMAIL + "'";

        jdbcTemplate.update(sql);

        resultMap.put("result", "success");

        return resultMap;
    }

    @Override
    public Map<String, Object> saveUserInfo(String userName, String envCode, String envUserID, String envPassword, String gitURI) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE IBD_O_USER_ENV SET LOGON_USER_ID = ?, LOGON_USER_PWD = ?, URI = ?, ");
        sb.append("LAST_UPT_TIME = CURRENT TIMESTAMP ");
        sb.append("WHERE USER_ID IN (SELECT USER_ID FROM IBD_O_USER WHERE USER_EMAIL = ?) AND ENV_CODE = ?");

        jdbcTemplate.update(sb.toString(), new Object[]{envUserID, envPassword, gitURI, userName, envCode});

        resultMap.put("result", "success");

        return resultMap;
    }


    @Override
    public Map<String, Object> saveGit(String userName, String envCode, String gitURI) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE IBD_O_USER_ENV SET URI = ?, ");
        sb.append("LAST_UPT_TIME = CURRENT TIMESTAMP ");
        sb.append("WHERE USER_ID IN (SELECT USER_ID FROM IBD_O_USER WHERE USER_EMAIL = ?) AND ENV_CODE = ?");

        jdbcTemplate.update(sb.toString(), new Object[]{gitURI, userName, envCode});

        resultMap.put("result", "success");

        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getBusyLink(String evnCode, String originName) {
        String sql = "SELECT DISTINCT JOB_LINK_NBR FROM IBD_O_PROMOTE_REQUEST ";
        sql += "WHERE JOB_LINK_NBR IS NOT NULL AND ENV_CODE = '"+ evnCode +"' AND ORIGIN_NAME = '"+ originName +"' ORDER BY JOB_LINK_NBR";

        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> updateStatusFromPromoteReq(String requestID, String status, String jobLinkNbr) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE IBD_O_PROMOTE_REQUEST SET STATUS = ?, JOB_LINK_NBR = ?, ");
        sb.append("LAST_UPT_TIME = CURRENT TIMESTAMP ");
        sb.append("WHERE REQUEST_ID = ? ");

        jdbcTemplate.update(sb.toString(), new Object[]{status, jobLinkNbr, Integer.parseInt(requestID)});

        resultMap.put("result", "success");

        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getEnvInfo(String envCode, String defaultName) {
        String sql = "SELECT ENV_DEFAULT_VALUE ";
        sql += "FROM IBD_O_ENV_DEFAULT ";
        sql += "WHERE ENV_CODE = '" + envCode + "' AND ENV_DEFAULT_NAME = '" + defaultName + "'";

        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> getUserInfo(String envCode, String userName) {
        String sql = "SELECT LOGON_USER_ID, LOGON_USER_PWD, URI ";
        sql += "FROM IBD_O_USER_ENV AS USER_ENV ";
        sql += "JOIN IBD_O_USER USER ON USER_ENV.USER_ID = USER.USER_ID ";
        sql += "WHERE USER.USER_EMAIL = '" + userName + "' AND USER_ENV.ENV_CODE = '" + envCode + "'";

        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> insertPromoteStep(String requestID, String step_seq) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String sql = "INSERT INTO IBD_O_PROMOTE_STEP ";
        sql += "(REQUEST_ID, STEP_SEQ, STATUS, LAST_ACT_USER_ID, LAST_ACT_SYS_CD, CREATE_TIMESTAMP, LAST_UPT_TIME) ";
        sql += "VALUES (?, ?, 'PROCESSING', 0, '', CURRENT TIMESTAMP, CURRENT TIMESTAMP)";

        jdbcTemplate.update(sql, new Object[]{Integer.parseInt(requestID), Integer.parseInt(step_seq)});

        resultMap.put("result", "success");

        return resultMap;
    }

    @Override
    public Map<String, Object> updatePromoteStepStatus(String requestID, String stepSeq, String status) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE IBD_O_PROMOTE_STEP SET STATUS = ?, ");
        sb.append("LAST_UPT_TIME = CURRENT TIMESTAMP ");
        sb.append("WHERE REQUEST_ID = ? AND STEP_SEQ = ? ");

        jdbcTemplate.update(sb.toString(), new Object[]{status, Integer.parseInt(requestID), Integer.parseInt(stepSeq)});

        resultMap.put("result", "success");

        return resultMap;
    }

    @Override
    public Map<String, Object> updatePromoteStepStatus(String envCode, String userName, String feature, String promoteMember, String branch, String origin, String status){
        Map<String, Object> resultMap = new HashMap<String, Object>();

        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE IBD_O_PROMOTE_STEP SET STATUS = ?, ");
        sb.append("LAST_UPT_TIME = CURRENT TIMESTAMP ");
        sb.append("WHERE REQUEST_ID IN (SELECT REQUEST_ID FROM IBD_O_PROMOTE_REQUEST ");
        sb.append("WHERE ENV_CODE = ? AND FEATURE_NAME = ? AND PROMOTE_MEMBER = ? AND BRANCH_NAME = ?");



        resultMap.put("result", "success");

        return resultMap;
    }

    @Override
    public Map<String, Object> getReqInfo() {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        String sql = "SELECT USER.USER_NAME AS USER_NAME,USER.USER_EMAIL AS USER_EMAIL, REQ.PROMOTE_MEMBER AS PROMOTE_MEMBER, " +
                "REQ.BRANCH_NAME AS BRANCH_NAME,REQ.ORIGIN_NAME AS ORIGIN_NAME,REQ.ENV_CODE AS ENV_CODE, REQ.FEATURE_NAME AS FEATURE_NAME, CASE WHEN CURRENT_STEP_ID IS NULL THEN 0 ELSE CURRENT_STEP_ID*100/12 END AS PERCENTAGE, " +
                "CASE WHEN REQ.STATUS = 'ERROR' THEN 'exception' WHEN REQ.STATUS = 'COMPLETE' THEN 'success' ELSE 'text' END AS STATUS "+
                "FROM IBD_O_PROMOTE_REQUEST REQ " +
                "JOIN IBD_O_USER USER ON REQ.USER_ID = USER.USER_ID " +
                "LEFT JOIN (SELECT REQUEST_ID,MAX(STEP_SEQ) AS CURRENT_STEP_ID FROM IBD_O_PROMOTE_STEP GROUP BY REQUEST_ID) STEP ON REQ.REQUEST_ID = STEP.REQUEST_ID " +
                "WHERE REQ.STATUS IN ('AVAILABLE','PROCESSING')";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

        resultMap.put("result", result);

        return resultMap;
    }

    @Override
    public Map<String, Object> getCompInfo() {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        String sql = "SELECT USER.USER_NAME AS USER_NAME,USER.USER_EMAIL AS USER_EMAIL, REQ.PROMOTE_MEMBER AS PROMOTE_MEMBER, " +
                "REQ.BRANCH_NAME AS BRANCH_NAME,REQ.ORIGIN_NAME AS ORIGIN_NAME,REQ.ENV_CODE AS ENV_CODE,REQ.FEATURE_NAME AS FEATURE_NAME, CURRENT_STEP_ID*100/12 AS PERCENTAGE, " +
                "CASE WHEN REQ.STATUS = 'ERROR' THEN 'exception' WHEN REQ.STATUS = 'COMPLETE' THEN 'success' ELSE 'text' END AS STATUS "+
                "FROM IBD_O_PROMOTE_REQUEST REQ " +
                "JOIN IBD_O_USER USER ON REQ.USER_ID = USER.USER_ID " +
                "JOIN (SELECT REQUEST_ID,MAX(STEP_SEQ) AS CURRENT_STEP_ID FROM IBD_O_PROMOTE_STEP GROUP BY REQUEST_ID) STEP ON REQ.REQUEST_ID = STEP.REQUEST_ID " +
                "WHERE REQ.STATUS IN ('ERROR','COMPLETE')";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

        resultMap.put("result", result);

        return resultMap;
    }

    @Override
    public Map<String, Object> getFeature(String envCode) {
        Map<String, Object> resultMap = new HashMap<>();

        String sql = "SELECT FEATURE_NAME, FEATURE_DESC, APPR_STATUS FROM IBD_R_FEATURE ";
        sql += "WHERE ENV_CODE = ?";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, new Object[] {envCode});

        resultMap.put("result", result);

        return resultMap;
    }

    @Override
    public Map<String, Object> addFeature(String envCode, String featureName, String featureDesc, int lastActUserId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        // Query if user exists
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT FEATURE_NAME, FEATURE_DESC FROM IBD_R_FEATURE WHERE UPPER(FEATURE_NAME) = ? AND ENV_CODE = ?");
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sb.toString(), new Object[] {featureName.trim().toUpperCase(), envCode.trim()});

        // Insert if not exists
        if (result.size() == 0) {
            StringBuffer sbIns = new StringBuffer();
            sbIns.append("INSERT INTO IBD_R_FEATURE (FEATURE_NAME, FEATURE_DESC, APPR_STATUS, LAST_ACT_USER_ID,LAST_ACT_SYS_CD,ENV_CODE) ");
            sbIns.append("VALUES (?, ?, 'Approved', ?, '',?)");
            jdbcTemplate.update(sbIns.toString(), new Object[] { featureName, featureDesc, lastActUserId, envCode });
        }

        resultMap.put("result", "success");

        return resultMap;
    }

    @Override
    public Map<String, Object> updateFeatureStatus(String envCode, String featureName, String apprStatus, int lastActUserId) {
        Map<String, Object> resultMap = new HashMap<>();

        StringBuffer sb = new StringBuffer();

        sb.append("UPDATE IBD_R_FEATURE SET APPR_STATUS = ?, LAST_ACT_USER_ID = ? WHERE FEATURE_NAME = ? AND ENV_CODE = ?");

        jdbcTemplate.update(sb.toString(), new Object[] { apprStatus, lastActUserId, featureName, envCode });

        resultMap.put("result", "success");

        return resultMap;
    }

    @Override
    public Map<String, Object> getStatusFeature(String envCode, String status) {
        Map<String, Object> resultMap = new HashMap<>();

        String sql = "SELECT DISTINCT FEATURE_NAME,FEATURE_DESC FROM IBD_R_FEATURE ";
        sql += "WHERE ENV_CODE = ? AND APPR_STATUS = ?";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, new Object[] {envCode, status});

        resultMap.put("result", result);

        return resultMap;
    }

}
