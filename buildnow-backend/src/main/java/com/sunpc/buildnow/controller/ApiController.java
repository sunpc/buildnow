package com.sunpc.buildnow.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sunpc.buildnow.service.IBuildService;

@RestController
@RequestMapping("/api/rest")
public class ApiController {

	@Autowired
	private IBuildService buildService;

	@PostMapping("/auth")
	public Map<String, Object> authenticate(@RequestBody Map<String, Object> params) {
		String username = (String) params.get("username");
		String password = (String) params.get("password");
		String envcode = (String) params.get("envcode");
		return buildService.authenticate(username, password, envcode);
	}
	
	@PostMapping("/searchBluePages")
	public Map<String, Object> searchBluePages(@RequestBody Map<String, Object> params) {
		String email = (String) params.get("email");
		return buildService.searchBluePages(email);
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/build")
	public Map<String, Object> build(@RequestBody Map<String, Object> params) {
		List<Map<String, Object>> steps = (List<Map<String, Object>>) params.get("steps");
		Map<String, Object> properties = (Map<String, Object>) params.get("properties");
		List<String> participants = (List<String>) params.get("participants");
		return buildService.buildJob(steps, properties, participants);
	}

	@PostMapping("/getEnvs")
	public Map<String, Object> getEnvs(@RequestBody Map<String, Object> params) {
		return buildService.getEnvs();
	}

	@PostMapping("/getEnv")
	public Map<String, Object> getEnv(@RequestBody Map<String, Object> params) {
		String envCode = (String) params.get("envCode");
		return buildService.getEnv(envCode);
	}

	@PostMapping("/getDefaultEnv")
	public Map<String, Object> getDefaultEnv(@RequestBody Map<String, Object> params) {
		String userEmail = (String) params.get("userEmail");
		return buildService.getDefaultEnv(userEmail);
	}
	
	@PostMapping("/saveEnv")
	public Map<String, Object> saveEnv(@RequestBody Map<String, Object> params) {
		String envCode = (String) params.get("envCode");
		String envDesc = (String) params.get("envDesc");
		int lastActUserId = (int) params.get("lastActUserId");
		return buildService.saveEnv(envCode, envDesc, lastActUserId);
	}

	@PostMapping("/getMembers")
	public Map<String, Object> getMembers(@RequestBody Map<String, Object> params) {
		String envCode = (String) params.get("envCode");
		return buildService.getMembers(envCode);
	}
	
	@PostMapping("/addMember")
	public Map<String, Object> addMember(@RequestBody Map<String, Object> params) {
		String envCode = (String) params.get("envCode");
		String userName = (String) params.get("userName");
		String userEmail = (String) params.get("userEmail");
		String userRole = (String) params.get("userRole");
		int lastActUserId = (int) params.get("lastActUserId");
		return buildService.addMember(envCode, userName, userEmail, userRole, lastActUserId);
	}
	
	@PostMapping("/updateMemberRole")
	public Map<String, Object> updateMemberRole(@RequestBody Map<String, Object> params) {
		String envCode = (String) params.get("envCode");
		String userEmail = (String) params.get("userEmail");
		String userRole = (String) params.get("userRole");
		int lastActUserId = (int) params.get("lastActUserId");
		return buildService.updateMemberRole(envCode, userEmail, userRole, lastActUserId);
	}
	
	@PostMapping("/updateMemberStatus")
	public Map<String, Object> updateMemberStatus(@RequestBody Map<String, Object> params) {
		String envCode = (String) params.get("envCode");
		String userEmail = (String) params.get("userEmail");
		String apprStatus = (String) params.get("apprStatus");
		int lastActUserId = (int) params.get("lastActUserId");
		return buildService.updateMemberStatus(envCode, userEmail, apprStatus, lastActUserId);
	}

	@PostMapping("/getDefaults")
	public Map<String, Object> getDefaults(@RequestBody Map<String, Object> params) {
		String envCode = (String) params.get("envCode");
		return buildService.getDefaults(envCode);
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping("/saveEnvDefaults")
	public Map<String, Object> saveEnvDefaults(@RequestBody Map<String, Object> params) {
		String envCode = (String) params.get("envCode");
		List<Map<String, Object>> defaults = (List<Map<String, Object>>) params.get("defaults");
		int lastActUserId = (int) params.get("lastActUserId");
		return buildService.saveEnvDefaults(envCode, defaults, lastActUserId);
	}

	@PostMapping("/saveJson")
	public Map<String, Object> saveJson(@RequestBody Map<String, Object>[] params) {
		return buildService.saveJson(params);
	}

	@PostMapping("/getObject")
	public Map<String, Object> getObject(@RequestBody Map<String, Object> params) {
		String objId = (String) params.get("objId");
		String uniqueKey = (String) params.get("uniqueKey");
		return buildService.getObject(objId, uniqueKey);
	}

	@PostMapping("/getObjectHistory")
	public Map<String, Object> getObjectHistory(@RequestBody Map<String, Object> params) {
		String objId = (String) params.get("objId");
		return buildService.getObjectHistory(objId);
	}

	@PostMapping("/makeObjectCurrent")
	public Map<String, Object> makeObjectCurrent(@RequestBody Map<String, Object> params) {
		String objId = (String) params.get("objId");
		String uniqueKey = (String) params.get("uniqueKey");
		int userId = (int) params.get("userId");
		return buildService.makeObjectCurrent(objId, uniqueKey, userId);
	}

	@PostMapping("/searchJobs")
	public Map<String, Object> searchJobs(@RequestBody Map<String, Object> params) {
		String queryString = (String) params.get("queryString");
		String envCode = (String) params.get("envCode");
		return buildService.searchJobs(queryString, envCode);
	}

	@PostMapping("/getRecentJobs")
	public Map<String, Object> getRecentJobs(@RequestBody Map<String, Object> params) {
		String envCode = (String) params.get("envCode");
		return buildService.getRecentJobs(envCode);
	}

	//getLayoutList
	@PostMapping("/getLayoutList")
	public Map<String, Object> getLayoutList(@RequestBody Map<String, Object> params) {
 		String layoutName = (String) params.get("queryString");
 		return buildService.getLayoutList(layoutName);
	}

	//getLayout
 	@PostMapping("/getLayout")
	public Map<String, Object> getLayout(@RequestBody Map<String, Object> params) {
 		String layoutName = (String) params.get("queryString"); 		
		return buildService.getLayout(layoutName);
	}
 	
 	//saveLayout
 	@PostMapping("/saveLayout")
	public Map<String, Object> saveLayout(@RequestBody Map<String, Object> params) {
		return buildService.saveLayout(params);
	}
    
    @PostMapping("/getTableList")
    public Map<String, Object> getTableList(@RequestBody Map<String, Object> params) {
        String querySchema = (String) params.get("querySchema");
        String queryTable = (String) params.get("queryTable");
        return buildService.getTableList(querySchema, queryTable);
    }
    
    @PostMapping("/getTableColumnList")
    public Map<String, Object> getTableColumnList(@RequestBody Map<String, Object> params) {
        String querySchema = (String) params.get("querySchema");
        String queryTable = (String) params.get("queryTable");
        return buildService.getTableColumnList(querySchema, queryTable);
    }
	

}
