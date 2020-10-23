package com.sunpc.buildnow.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BuildDao implements IBuildDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Map<String, Object> authenticate(String email, String password, String envCode) {
		Map<String, Object> resultMap;

		String sql = "SELECT A.USER_ID, A.USER_NAME, B.USER_ROLE " +
				"FROM IBD_O_USER A, IBD_O_USER_ENV B " +
				"WHERE A.USER_ID = B.USER_ID AND B.APPR_STATUS = 'Approved' " +
				"AND A.USER_EMAIL = ? AND A.USER_PASS = ? AND B.ENV_CODE = ?";

		try {
			resultMap = jdbcTemplate.queryForMap(sql, email, password, envCode);
		} catch (EmptyResultDataAccessException e) {
			resultMap = null;
		}

		return resultMap;
	}

	@Override
	public Map<String, Object> getEnvs() {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT * FROM IBD_O_ENV ORDER BY ENV_CODE");

		resultMap.put("result", result);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getEnv(String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		Map<String, Object> result = jdbcTemplate.queryForMap("SELECT * FROM IBD_O_ENV WHERE ENV_CODE = ?", envCode);

		resultMap.put("result", result);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getDefaultEnv(String userEmail) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String sb = "SELECT COALESCE(MAX(ENV_CODE), '') FROM IBD_O_USER_ENV AL1, IBD_O_USER AL2 " +
				"WHERE AL1.USER_ID = AL2.USER_ID AND AL1.DEFAULT_ENV_INDC = '1' " +
				"AND UPPER(AL2.USER_EMAIL) = ?";
		String envCode = jdbcTemplate.queryForObject(sb, new Object[] {userEmail.trim().toUpperCase()}, String.class);

		resultMap.put("envCode", envCode);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> updateDefaultEnv(long userId, String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// Update new default
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE IBD_O_USER_ENV SET DEFAULT_ENV_INDC = '1', ");
		sb.append("LAST_ACT_USER_ID = ?, LAST_UPT_TIME = CURRENT_TIMESTAMP ");
		sb.append("WHERE USER_ID = ? AND ENV_CODE = ? AND DEFAULT_ENV_INDC = '0'");

		jdbcTemplate.update(sb.toString(), userId, userId, envCode);
		
		// Update old default
		StringBuilder sb1 = new StringBuilder();
		sb1.append("UPDATE IBD_O_USER_ENV SET DEFAULT_ENV_INDC = '0', ");
		sb1.append("LAST_ACT_USER_ID = ?, LAST_UPT_TIME = CURRENT_TIMESTAMP ");
		sb1.append("WHERE USER_ID = ? AND ENV_CODE <> ? AND DEFAULT_ENV_INDC = '1'");

		jdbcTemplate.update(sb.toString(), userId, userId, envCode);

		resultMap.put("result", "success");

		return resultMap;
	}
	
	@Override
	public Map<String, Object> saveEnv(String envCode, String envDesc, int lastActUserId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		jdbcTemplate.update("UPDATE IBD_O_ENV SET ENV_DESP = ?, LAST_ACT_USER_ID = ?, LAST_UPT_TIME = CURRENT_TIMESTAMP WHERE ENV_CODE = ?", new Object[] { envDesc, lastActUserId, envCode });

		resultMap.put("result", "success");

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getMembers(String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String sb = "SELECT AL1.USER_ID, AL1.USER_EMAIL, AL1.USER_NAME, AL2.USER_ROLE, AL2.APPR_STATUS " +
				"FROM IBD_O_USER AL1,IBD_O_USER_ENV AL2 " +
				"WHERE AL1.USER_ID = AL2.USER_ID AND AL2.ENV_CODE = ?";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb, envCode);

		resultMap.put("result", result);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getMember(String envCode, String userEmail) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String sb = "SELECT AL1.USER_ID, AL1.USER_NAME, AL2.USER_ROLE " +
				"FROM IBD_O_USER AL1,IBD_O_USER_ENV AL2 " +
				"WHERE AL1.USER_ID = AL2.USER_ID AND AL2.ENV_CODE = ? AND UPPER(AL1.USER_EMAIL) = ? AND AL2.APPR_STATUS = 'Approved'";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb, envCode, userEmail.trim().toUpperCase());

		resultMap.put("result", result);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> addMember(String envCode, String userName, String userEmail, String userRole, int lastActUserId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// Query if user exists
		List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT USER_ID FROM IBD_O_USER WHERE UPPER(USER_EMAIL) = ?", new Object[] {userEmail.trim().toUpperCase()});
		
		// Insert if not exists
		if (result.size() == 0) {
			String sbIns = "INSERT INTO IBD_O_USER (USER_EMAIL, USER_NAME, LAST_ACT_USER_ID, LAST_ACT_SYS_CD) " +
					"VALUES (?, ?, ?, '') ";
			jdbcTemplate.update(sbIns, userEmail, userName, lastActUserId);
		}
		
		// Query if user exists
		List<Map<String, Object>> resultEnv = jdbcTemplate.queryForList("SELECT AL1.USER_ID FROM IBD_O_USER_ENV AL1, IBD_O_USER AL2 WHERE AL1.USER_ID = AL2.USER_ID AND UPPER(AL2.USER_EMAIL) = ? AND AL1.ENV_CODE = ?", new Object[] {userEmail.trim().toUpperCase(), envCode});
		
		// Insert if not exists
		if (resultEnv.size() == 0) {
			String sbIns = "INSERT INTO IBD_O_USER_ENV (USER_ID, ENV_CODE, USER_ROLE, APPR_STATUS, DEFAULT_ENV_INDC, LAST_ACT_USER_ID, LAST_ACT_SYS_CD) " +
					"SELECT USER_ID, '" + envCode + "', '" + userRole + "', 'Approved', 0, " + lastActUserId + ", '' FROM IBD_O_USER WHERE UPPER(USER_EMAIL) = ?";
			jdbcTemplate.update(sbIns, userEmail.trim().toUpperCase());
			resultMap.put("result", "success");
		} else {
			resultMap.put("result", "duplicate");
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> updateMemberRole(String envCode, String userEmail, String userRole, int lastActUserId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		jdbcTemplate.update("UPDATE IBD_O_USER_ENV SET USER_ROLE = ?, LAST_ACT_USER_ID = ? WHERE USER_ID IN (SELECT USER_ID FROM IBD_O_USER WHERE USER_EMAIL = ?) AND ENV_CODE = ?", new Object[] { userRole, lastActUserId, userEmail, envCode });
		
		resultMap.put("result", "success");
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> updateMemberStatus(String envCode, String userEmail, String apprStatus, int lastActUserId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		jdbcTemplate.update("UPDATE IBD_O_USER_ENV SET APPR_STATUS = ?, LAST_ACT_USER_ID = ? WHERE USER_ID IN (SELECT USER_ID FROM IBD_O_USER WHERE USER_EMAIL = ?) AND ENV_CODE = ?", apprStatus, lastActUserId, userEmail, envCode);
		
		resultMap.put("result", "success");
		
		return resultMap;
	}

	@Override
	public Map<String, Object> getDefaults(String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String sb = "select OBJ_DEFAULT_NAME as DEFAULT_NAME,OBJ_DEFAULT_VALUE as DEFAULT_VALUE FROM IBD_O_OBJ_DEFAULT " +
				"union " +
				"select ENV_DEFAULT_NAME as DEFAULT_NAME,ENV_DEFAULT_VALUE as DEFAULT_VALUE FROM IBD_O_ENV_DEFAULT " +
				"where ENV_CODE = ? ";
		List<Map<String, Object>> datalist = jdbcTemplate.queryForList(sb, envCode);

		Map<String, Object> result = new HashMap<String, Object>();
		for (Map<String, Object> entry : datalist) {
			String defaultName = entry.get("DEFAULT_NAME").toString().trim();
			String defaultValue = entry.get("DEFAULT_VALUE").toString().trim();
			result.put(defaultName, defaultValue);
		}

		resultMap.put("result", result);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> saveEnvDefaults(String envCode, List<Map<String, Object>> params, int lastActUserId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		jdbcTemplate.update("DELETE FROM IBD_O_ENV_DEFAULT WHERE ENV_CODE = ?", envCode);
		
		for (Map<String, Object> param : params) {
			String sbIns = "INSERT INTO IBD_O_ENV_DEFAULT (ENV_CODE, ENV_DEFAULT_NAME, ENV_DEFAULT_VALUE, LAST_ACT_USER_ID, LAST_ACT_SYS_CD) " +
					"VALUES(?, ?, ?, ?, '')";
			jdbcTemplate.update(sbIns, envCode, param.get("name").toString(), param.get("value").toString(), lastActUserId);
		}
		
		resultMap.put("result", "success");
		
		return resultMap;
	}

	@Override
	public Map<String, Object> saveJson(String uuid, String type, String name, String json, String envCode, int userId) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// Query OBJ_TYPE_ID
		String sqlTypeId = "SELECT OBJ_TYPE_ID from IBD_R_OBJ_TYPE WHERE OBJ_TYPE = ?";
		int objTypeId = jdbcTemplate.queryForObject(sqlTypeId, new Object[] { type.toUpperCase() }, Integer.class);
		
		// Query UUID
		String sqlObjId = "SELECT COUNT(*) from IBD_O_OBJ WHERE OBJ_ID = ?";
		int objCount = jdbcTemplate.queryForObject(sqlObjId, new Object[] { uuid }, Integer.class);
		
		// Update IBD_O_OBJ
		if (objCount == 0) {
			String sb = "INSERT INTO IBD_O_OBJ (OBJ_ID, OBJ_TYPE_ID, OBJ_NAME, JSON_FILE, LAST_ACT_USER_ID) " +
					"VALUES (?,?,?,?,?) ";
			jdbcTemplate.update(sb, uuid, objTypeId, name.toUpperCase(), json, userId);
			// Update H
			String updateAction = "INSERT";
			String sbH = "INSERT INTO IBD_H_OBJ (OBJ_ID, OBJ_TYPE_ID, OBJ_NAME, JSON_FILE, LAST_ACT_USER_ID, UPDATE_ACTION) " +
					"VALUES (?,?,?,?,?,?) ";
			jdbcTemplate.update(sbH, uuid, objTypeId, name.toUpperCase(), json, userId, updateAction);
		} else {
			String sb = "UPDATE IBD_O_OBJ " +
					"SET OBJ_NAME = ?, " +
					"JSON_FILE = ?, " +
					"LAST_ACT_USER_ID = ?, " +
					"LAST_UPT_TIME = CURRENT_TIMESTAMP " +
					"WHERE OBJ_ID = ? ";
			jdbcTemplate.update(sb, name.toUpperCase(), json, userId, uuid);
			// Update H
			String updateAction = "UPDATE";
			String sbH = "INSERT INTO IBD_H_OBJ (OBJ_ID, OBJ_TYPE_ID, OBJ_NAME, JSON_FILE, LAST_ACT_USER_ID, UPDATE_ACTION) " +
					"VALUES (?,?,?,?,?,?) ";
			jdbcTemplate.update(sbH, uuid, objTypeId, name.toUpperCase(), json, userId, updateAction);
		}
		
		// Query UUID
		String sqlObjEnv = "SELECT COUNT(*) from IBD_O_OBJ_ENV WHERE OBJ_ID = ? AND ENV_CODE = ?";
		int objEnvCount = jdbcTemplate.queryForObject(sqlObjEnv, new Object[] { uuid, envCode }, Integer.class);
		
		// Update IBD_O_OBJ_ENV
		if (objEnvCount == 0) {
			String sb = "INSERT INTO IBD_O_OBJ_ENV (OBJ_ID, ENV_CODE, LAST_ACT_USER_ID) " +
					"VALUES (?,?,?) ";
			jdbcTemplate.update(sb, uuid, envCode, userId);
		}
		
		resultMap.put("result", "success");

		return resultMap;
	}

	@Override
	public Map<String, Object> getObject(String uuid, String uniqueKey) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		StringBuilder sb = new StringBuilder();
		Object[] objects = new Object[] {};
		if (uniqueKey == null) {
			sb.append("SELECT * FROM IBD_O_OBJ ");
			sb.append("WHERE OBJ_ID = ? ");
			objects = new Object[] { uuid };
		} else {
			sb.append("WITH TMP AS (SELECT strftime('%Y%m%d%H%M%S', last_upt_time) as UNIQUE_KEY,");
			sb.append("OBJ_ID, OBJ_TYPE_ID, OBJ_NAME, LAST_ACT_USER_ID, CREATE_TIMESTAMP, LAST_UPT_TIME, JSON_FILE, OBJ_CONTENT ");
			sb.append("FROM IBD_H_OBJ) ");
			sb.append("SELECT * FROM TMP WHERE OBJ_ID = ? AND UNIQUE_KEY = ? ");
			objects = new Object[] { uuid, uniqueKey };
		}

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb.toString(), objects);

		resultMap.put("result", result);

		return resultMap;
	}

	@Override
	public Map<String, Object> getObjectHistory(String uuid) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		// sb.append("SELECT 'v' || ROW_NUMBER() OVER(ORDER BY AL1.LAST_UPT_TIME) as VERSION, ");

		String sb = "SELECT 'v' || (select count(*) from IBD_H_OBJ H where H.OBJ_ID = AL1.OBJ_ID and H.LAST_UPT_TIME <= AL1.LAST_UPT_TIME ) as VERSION, " +
				"AL1.OBJ_NAME, strftime('%Y%m%d%H%M%S', AL1.LAST_UPT_TIME) as UNIQUE_KEY, " +
				"COALESCE(AL2.USER_NAME, 'Unknown') AS USER_NAME, " +
				"strftime('%Y-%m-%d %H:%M:%S', AL1.LAST_UPT_TIME) as LAST_UPT_TIME " +
				"FROM IBD_H_OBJ AL1 " +
				"LEFT OUTER JOIN IBD_O_USER AL2 ON AL1.LAST_ACT_USER_ID = AL2.USER_ID " +
				"WHERE OBJ_ID = ? " +
				"ORDER BY AL1.LAST_UPT_TIME DESC " +
				"LIMIT 10";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb, uuid);

		resultMap.put("result", result);

		return resultMap;
	}

	@Override
	public Map<String, Object> makeObjectCurrent(String uuid, String uniqueKey, int userId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String sb = "UPDATE IBD_O_OBJ " +
				"SET (OBJ_NAME, JSON_FILE, LAST_ACT_USER_ID, LAST_UPT_TIME) =  " +
				"(SELECT OBJ_NAME, JSON_FILE, " + userId + ", CURRENT_TIMESTAMP FROM IBD_H_OBJ " +
				"WHERE OBJ_ID = ? AND strftime('%Y%m%d%H%M%S', last_upt_time) = ?) " +
				"WHERE OBJ_ID = ? ";
		jdbcTemplate.update(sb, uuid, uniqueKey, uuid);

		resultMap.put("result", "success");

		return resultMap;
	}

	@Override
	public Map<String, Object> searchJobs(String queryString, String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String sb = "select A.OBJ_ID, B.OBJ_TYPE, A.OBJ_NAME " +
				"FROM IBD_O_OBJ A " +
				"inner join IBD_R_OBJ_TYPE B on A.OBJ_TYPE_ID = B.OBJ_TYPE_ID " +
				"inner join IBD_O_OBJ_ENV C on A.OBJ_ID = C.OBJ_ID " +
				"where B.OBJ_TYPE = 'JOB' and C.ENV_CODE = ? and A.OBJ_NAME like '%" + queryString.toUpperCase() + "%' ";
		List<Map<String, Object>> dataList = jdbcTemplate.queryForList(sb, envCode);
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		
		for (Map<String, Object> row : dataList) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", row.get("OBJ_NAME"));
			map.put("value", row.get("OBJ_NAME"));
			map.put("text", row.get("OBJ_TYPE"));
			map.put("uuid", row.get("OBJ_ID"));
			result.add(map);
		}

		resultMap.put("result", result);

		return resultMap;
	}

	@Override
	public Map<String, Object> getRecentJobs(String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String sb = "select A.OBJ_ID, A.OBJ_NAME, strftime('%Y-%m-%d %H:%M:%S', A.LAST_UPT_TIME) as LAST_UPT_TIME " +
				"FROM IBD_O_OBJ A " +
				"inner join IBD_R_OBJ_TYPE B on A.OBJ_TYPE_ID = B.OBJ_TYPE_ID " +
				"inner join IBD_O_OBJ_ENV C on A.OBJ_ID = C.OBJ_ID " +
				"where B.OBJ_TYPE = 'JOB' and C.ENV_CODE = ? order by A.LAST_UPT_TIME desc " +
				"LIMIT 5";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb, envCode);

		resultMap.put("result", result);

		return resultMap;
	}

}
