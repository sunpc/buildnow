package com.sunpc.buildnow.service;


import com.sunpc.buildnow.dao.IPromoteDao;
import com.sunpc.buildnow.util.git.DesEncrypter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Component
@Configurable
@EnableScheduling
@EnableAsync
@Service
public class ScheduledService {
    @Autowired
    private IPromoteDao promoteDao;

    @Autowired
    private PromoteService promoteService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Scheduled(cron = "0 */1 *  * * * ")
    public void getPromoteReq() {
        BuildService buildService = new BuildService();
        long USER_ID = buildService.USER_ID;

        List<Map<String, Object>> availableReq = promoteDao.getPromoteReq("AVAILABLE",USER_ID);

        if (availableReq.size() != 0) {
            String requestID = availableReq.get(0).get("REQUEST_ID").toString().trim();
            String uri = availableReq.get(0).get("URI").toString().trim();
            String envCode = availableReq.get(0).get("ENV_CODE").toString().trim();
            String logonUserID = availableReq.get(0).get("LOGON_USER_ID").toString().trim();
            String logonUserPW = DesEncrypter.decrypt(availableReq.get(0).get("LOGON_USER_PWD").toString().trim());
            String promoteMember = availableReq.get(0).get("PROMOTE_MEMBER").toString().trim();
            String branchName = availableReq.get(0).get("BRANCH_NAME").toString().trim();
            String originName = availableReq.get(0).get("ORIGIN_NAME").toString().trim();
            String feature = availableReq.get(0).get("FEATURE_NAME").toString().trim();
            String jobLinkNbr = availableReq.get(0).get("JOB_LINK_NBR").toString().trim();

            logger.info("promote request " + requestID);

            promoteDao.updateStatusFromPromoteReq(requestID,"PROCESSING", jobLinkNbr);

            String envHost = promoteDao.getEnvInfo(envCode,"Host").get(0).get("ENV_DEFAULT_VALUE").toString().trim();
            String envPort = promoteDao.getEnvInfo(envCode,"SFTP Port").get(0).get("ENV_DEFAULT_VALUE").toString().trim();

            promoteService.promote(requestID,jobLinkNbr,uri,envCode,envHost,envPort,logonUserID,logonUserPW,promoteMember,branchName,originName,feature);

        }
    }

}
