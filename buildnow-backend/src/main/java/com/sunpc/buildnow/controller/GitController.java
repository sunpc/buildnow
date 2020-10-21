package com.sunpc.buildnow.controller;

import com.sunpc.buildnow.service.IPromoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/git")
public class GitController {
    @Autowired
    private IPromoteService promoteService;

    @PostMapping("/promote")
    public Map<String, Object> promote(@RequestBody Map<String, Object> params) {
        Map<String, Object> properties = (Map<String, Object>) params.get("properties");
        return promoteService.receiveReq(properties);
    }

    @PostMapping("/setEnv")
    public Map<String, Object> setEnvs(@RequestBody Map<String, Object> params) {
        return promoteService.setEnv(params);
    }

    @PostMapping("/getEnv")
    public Map<String, Object> getEnvs(@RequestBody Map<String, Object> params) {
        return promoteService.getEnv(params);
    }

    @PostMapping("/setGit")
    public Map<String, Object> setGit(@RequestBody Map<String, Object> params) {
        return promoteService.setGit(params);
    }

    @PostMapping("/getReqInfo")
    public Map<String, Object> getReqInfo(@RequestBody Map<String, Object> params) {
        return promoteService.getReqInfo(params);
    }

    @PostMapping("/getCompInfo")
    public Map<String, Object> getCompInfo(@RequestBody Map<String, Object> params) {
        return promoteService.getCompInfo(params);
    }

    @PostMapping("/getFullList")
    public Map<String, Object> getFullList(@RequestBody Map<String, Object> params) {
        return promoteService.getFullList(params);
    }

    @PostMapping("/getUniqueList")
    public Map<String, Object> getUniqueList(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        return promoteService.getUniqueList(params,request);
    }

    @PostMapping("/uploadUniqueList")
    public Map<String, Object> uploadUniqueList(@RequestBody Map<String, Object> params, HttpSession session) {
        return promoteService.uploadUniqueList(params,session);
    }

    @PostMapping("/getFeatures")
    public Map<String, Object> getFeatures(@RequestBody Map<String, Object> params) {
        String envCode = (String) params.get("envCode");
        return promoteService.getFeatures(envCode);
    }

    @PostMapping("/addFeature")
    public Map<String, Object> addFeatures(@RequestBody Map<String, Object> params) {
        return promoteService.addFeature(params);
    }

    @PostMapping("/updateFeatureStatus")
    public Map<String, Object> updateFeatureStatus(@RequestBody Map<String, Object> params) {
        return promoteService.updateFeatureStatus(params);
    }

    @PostMapping("/getApproveFeatures")
    public Map<String, Object> getApproveFeatures(@RequestBody Map<String, Object> params) {
        String envCode = (String) params.get("envCode");
        return promoteService.getApproveFeatures(envCode);
    }

    @PostMapping("/handleStopRequest")
    public Map<String, Object> handleStopRequest(@RequestBody Map<String, Object> params) {
        return promoteService.handleStopRequest(params);
    }


}

