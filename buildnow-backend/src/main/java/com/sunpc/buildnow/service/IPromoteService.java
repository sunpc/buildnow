package com.sunpc.buildnow.service;

import org.springframework.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public interface IPromoteService {

    Map<String, Object> receiveReq(Map<String, Object> properties);

    Map<String, Object> getEnv(Map<String, Object> properties);

    Map<String, Object> setEnv(Map<String, Object> properties);

    Map<String, Object> setGit(Map<String, Object> properties);

    Map<String, Object> getReqInfo(Map<String, Object> properties);

    Map<String, Object> getCompInfo(Map<String, Object> properties);

    Map<String, Object> getUniqueList(Map<String, Object> properties, HttpServletRequest request);

    Map<String, Object> uploadUniqueList(Map<String, Object> properties, HttpSession session);

    Map<String, Object> getFeatures(String envCode);

    Map<String, Object> addFeature(Map<String, Object> properties);

    Map<String, Object> updateFeatureStatus(Map<String, Object> properties);

    Map<String, Object> getApproveFeatures(String envCode);

    Map<String, Object> getFullList(Map<String, Object> properties);

    Map<String, Object> handleStopRequest(Map<String, Object> properties);

    Map<String, Object> promote(String requestID, String gitPath, String gitURI,
                                String envCode, String envHost, String envPort, String envUserID, String envPassword,
                                String promoteMmeber, String branch,String origin, String feature);
}
