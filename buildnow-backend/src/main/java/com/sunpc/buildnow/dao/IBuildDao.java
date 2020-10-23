package com.sunpc.buildnow.dao;

import java.util.List;
import java.util.Map;

public interface IBuildDao {

	public Map<String, Object> authenticate(String email, String password, String envCode);

	public Map<String, Object> getEnvs();

	public Map<String, Object> getEnv(String envCode);

	public Map<String, Object> getDefaultEnv(String userEmail);

	public Map<String, Object> updateDefaultEnv(long userId, String envCode);

	public Map<String, Object> saveEnv(String envCode, String envDesc, int lastActUserId);
	
	public Map<String, Object> getMembers(String envCode);
	
	public Map<String, Object> getMember(String envCode, String userEmail);
	
	public Map<String, Object> addMember(String envCode, String userName, String userEmail, String userRole, int lastActUserId);
	
	public Map<String, Object> updateMemberRole(String envCode, String userEmail, String userRole, int lastActUserId);
	
	public Map<String, Object> updateMemberStatus(String envCode, String userEmail, String apprStatus, int lastActUserId);

	public Map<String, Object> getDefaults(String envCode);

	public Map<String, Object> saveEnvDefaults(String envCode, List<Map<String, Object>> params, int lastActUserId);

	public Map<String, Object> saveJson(String uuid, String type, String name, String json, String envCode, int userId);
	
	public Map<String, Object> getObject(String uuid, String uniqueKey);
	
	public Map<String, Object> getObjectHistory(String uuid);
	
	public Map<String, Object> makeObjectCurrent(String uuid, String uniqueKey, int userId);
	
	public Map<String, Object> searchJobs(String queryString, String envCode);
	
	public Map<String, Object> getRecentJobs(String envCode);

}
