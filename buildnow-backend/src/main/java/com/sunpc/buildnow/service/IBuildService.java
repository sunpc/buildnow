package com.sunpc.buildnow.service;

import java.util.List;
import java.util.Map;

public interface IBuildService {
	
	public Map<String, Object> authenticate(String email, String password,String envcode);
	
	public Map<String, Object> getEnvs();
	
	public Map<String, Object> getEnv(String envCode);
	
	public Map<String, Object> getDefaultEnv(String userEmail);

	public Map<String, Object> saveEnv(String envCode, String envDesc, int lastActUserId);
	
	public Map<String, Object> getMembers(String envCode);
	
	public Map<String, Object> addMember(String envCode, String userName, String userEmail, String userRole, int lastActUserId);
	
	public Map<String, Object> updateMemberRole(String envCode, String userEmail, String userRole, int lastActUserId);
	
	public Map<String, Object> updateMemberStatus(String envCode, String userEmail, String apprStatus, int lastActUserId);
	
	public Map<String, Object> getDefaults(String envCode);

	public Map<String, Object> saveEnvDefaults(String envCode, List<Map<String, Object>> params, int lastActUserId);
	
	public Map<String, Object> saveJson(Map<String, Object>[] map);
	
	public Map<String, Object> buildSym(Map<String, Object> layout, String sortLib);
	
	public Map<String, Object> buildSymErw(Map<String, Object> orgInLayout, 
			                               Map<String, Object> rebInLayout, 
			                               String sortLib);
	 
	public Map<String, Object> buildSort(Map<String, Object> step, String sortLib);
	 
	public Map<String, Object> buildSplit(Map<String, Object> step, String sortLib);

	public Map<String, Object> buildJoin(Map<String, Object> step, String sortLib);

	public Map<String, Object> buildJob(List<Map<String, Object>> steps, Map<String, Object> properties,
			List<String> participants);
	
	public Map<String, Object> getObject(String objId, String uniqueKey);
	
	public Map<String, Object> getObjectHistory(String objId);
	
	public Map<String, Object> makeObjectCurrent(String objId, String uniqueKey, int userId);
	
	public Map<String, Object> searchJobs(String queryString, String envCode);
	
	public Map<String, Object> getRecentJobs(String envCode);

}
