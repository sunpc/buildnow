package com.sunpc.buildnow.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BuildDao implements IBuildDao {

	@Autowired
	@Qualifier("primaryJdbcTemplate")
	private JdbcTemplate jdbcTemplate;	

	@Autowired
	@Qualifier("secondaryJdbcTemplate")
	private JdbcTemplate jdbcTemplateCodeGen;	

	@Override
	public Map<String, Object> getEnvs() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM IBD_O_ENV ORDER BY ENV_CODE");

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb.toString());

		resultMap.put("result", result);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getEnv(String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM IBD_O_ENV WHERE ENV_CODE = ?");

		Map<String, Object> result = jdbcTemplate.queryForMap(sb.toString(), new Object[] {envCode});

		resultMap.put("result", result);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getDefaultEnv(String userEmail) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT COALESCE(MAX(ENV_CODE), '') FROM IBD_O_USER_ENV AL1, IBD_O_USER AL2 ");
		sb.append("WHERE AL1.USER_ID = AL2.USER_ID AND AL1.DEFAULT_ENV_INDC = '1' ");
		sb.append("AND UPPER(AL2.USER_EMAIL) = ?");

		String envCode = jdbcTemplate.queryForObject(sb.toString(), new Object[] {userEmail.trim().toUpperCase()}, String.class);

		resultMap.put("envCode", envCode.trim());

		return resultMap;
	}
	
	@Override
	public Map<String, Object> updateDefaultEnv(long userId, String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// Update new default
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE IBD_O_USER_ENV SET DEFAULT_ENV_INDC = '1', ");
		sb.append("LAST_ACT_USER_ID = ?, LAST_UPT_TIME = CURRENT_TIMESTAMP ");
		sb.append("WHERE USER_ID = ? AND ENV_CODE = ? AND DEFAULT_ENV_INDC = '0'");

		jdbcTemplate.update(sb.toString(), new Object[] {userId, userId, envCode});
		
		// Update old default
		StringBuffer sb1 = new StringBuffer();
		sb1.append("UPDATE IBD_O_USER_ENV SET DEFAULT_ENV_INDC = '0', ");
		sb1.append("LAST_ACT_USER_ID = ?, LAST_UPT_TIME = CURRENT_TIMESTAMP ");
		sb1.append("WHERE USER_ID = ? AND ENV_CODE <> ? AND DEFAULT_ENV_INDC = '1'");

		jdbcTemplate.update(sb.toString(), new Object[] {userId, userId, envCode});

		resultMap.put("result", "success");

		return resultMap;
	}
	
	@Override
	public Map<String, Object> saveEnv(String envCode, String envDesc, int lastActUserId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE IBD_O_ENV SET ENV_DESP = ?, LAST_ACT_USER_ID = ?, LAST_UPT_TIME = CURRENT_TIMESTAMP WHERE ENV_CODE = ?");

		jdbcTemplate.update(sb.toString(), new Object[] { envDesc, lastActUserId, envCode });

		resultMap.put("result", "success");

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getMembers(String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT AL1.USER_ID, AL1.USER_EMAIL, AL1.USER_NAME, AL2.USER_ROLE, AL2.APPR_STATUS ");
		sb.append("FROM IBD_O_USER AL1,IBD_O_USER_ENV AL2 ");
		sb.append("WHERE AL1.USER_ID = AL2.USER_ID AND AL2.ENV_CODE = ?");

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb.toString(), new Object[] {envCode});

		resultMap.put("result", result);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getMember(String envCode, String userEmail) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT AL1.USER_ID, AL1.USER_NAME, AL2.USER_ROLE ");
		sb.append("FROM IBD_O_USER AL1,IBD_O_USER_ENV AL2 ");
		sb.append("WHERE AL1.USER_ID = AL2.USER_ID AND AL2.ENV_CODE = ? AND UPPER(AL1.USER_EMAIL) = ? AND AL2.APPR_STATUS = 'Approved'");

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb.toString(), new Object[] {envCode, userEmail.trim().toUpperCase()});

		resultMap.put("result", result);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> addMember(String envCode, String userName, String userEmail, String userRole, int lastActUserId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// Query if user exists
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT USER_ID FROM IBD_O_USER WHERE UPPER(USER_EMAIL) = ?");
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb.toString(), new Object[] {userEmail.trim().toUpperCase()});
		
		// Insert if not exists
		if (result.size() == 0) {
			StringBuffer sbIns = new StringBuffer();
			sbIns.append("INSERT INTO IBD_O_USER (USER_EMAIL, USER_NAME, LAST_ACT_USER_ID, LAST_ACT_SYS_CD) ");
			sbIns.append("VALUES (?, ?, ?, '') ");
			jdbcTemplate.update(sbIns.toString(), new Object[] { userEmail, userName, lastActUserId });
		}
		
		// Query if user exists
		StringBuffer sbEnv = new StringBuffer();
		sbEnv.append("SELECT AL1.USER_ID FROM IBD_O_USER_ENV AL1, IBD_O_USER AL2 WHERE AL1.USER_ID = AL2.USER_ID AND UPPER(AL2.USER_EMAIL) = ? AND AL1.ENV_CODE = ?");
		List<Map<String, Object>> resultEnv = jdbcTemplate.queryForList(sbEnv.toString(), new Object[] {userEmail.trim().toUpperCase(), envCode});
		
		// Insert if not exists
		if (resultEnv.size() == 0) {
			StringBuffer sbIns = new StringBuffer();
			sbIns.append("INSERT INTO IBD_O_USER_ENV (USER_ID, ENV_CODE, USER_ROLE, APPR_STATUS, DEFAULT_ENV_INDC, LAST_ACT_USER_ID, LAST_ACT_SYS_CD) ");
			sbIns.append("SELECT USER_ID, '" + envCode + "', '" + userRole + "', 'Approved', 0, " + lastActUserId + ", '' FROM IBD_O_USER WHERE UPPER(USER_EMAIL) = ?");
			jdbcTemplate.update(sbIns.toString(), new Object[] { userEmail.trim().toUpperCase() });
			resultMap.put("result", "success");
		} else {
			resultMap.put("result", "duplicate");
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> updateMemberRole(String envCode, String userEmail, String userRole, int lastActUserId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE IBD_O_USER_ENV SET USER_ROLE = ?, LAST_ACT_USER_ID = ? WHERE USER_ID IN (SELECT USER_ID FROM IBD_O_USER WHERE USER_EMAIL = ?) AND ENV_CODE = ?");
		jdbcTemplate.update(sb.toString(), new Object[] { userRole, lastActUserId, userEmail, envCode });
		
		resultMap.put("result", "success");
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> updateMemberStatus(String envCode, String userEmail, String apprStatus, int lastActUserId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE IBD_O_USER_ENV SET APPR_STATUS = ?, LAST_ACT_USER_ID = ? WHERE USER_ID IN (SELECT USER_ID FROM IBD_O_USER WHERE USER_EMAIL = ?) AND ENV_CODE = ?");
		jdbcTemplate.update(sb.toString(), new Object[] { apprStatus, lastActUserId, userEmail, envCode });
		
		resultMap.put("result", "success");
		
		return resultMap;
	}

	@Override
	public Map<String, Object> getDefaults(String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();

		sb.append("select OBJ_DEFAULT_NAME as DEFAULT_NAME,OBJ_DEFAULT_VALUE as DEFAULT_VALUE FROM IBD_O_OBJ_DEFAULT ");
		sb.append("union ");
		sb.append("select ENV_DEFAULT_NAME as DEFAULT_NAME,ENV_DEFAULT_VALUE as DEFAULT_VALUE FROM IBD_O_ENV_DEFAULT ");
		sb.append("where ENV_CODE = ? ");

		List<Map<String, Object>> datalist = jdbcTemplate.queryForList(sb.toString(), new Object[] { envCode });

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
		
		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM IBD_O_ENV_DEFAULT WHERE ENV_CODE = ?");
		jdbcTemplate.update(sb.toString(), new Object[] { envCode });
		
		for (Map<String, Object> param : params) {
			StringBuffer sbIns = new StringBuffer();
			sbIns.append("INSERT INTO IBD_O_ENV_DEFAULT (ENV_CODE, ENV_DEFAULT_NAME, ENV_DEFAULT_VALUE, LAST_ACT_USER_ID, LAST_ACT_SYS_CD) ");
			sbIns.append("VALUES(?, ?, ?, ?, '')");
			jdbcTemplate.update(sbIns.toString(), new Object[] { envCode, param.get("name").toString(), param.get("value").toString(), lastActUserId });
		}
		
		resultMap.put("result", "success");
		
		return resultMap;
	}

	@Override
	// public Map<String, Object> saveJson(int projectId, String uuid, String type,
	// String name, String json, int userId) {
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
			StringBuffer sb = new StringBuffer();
			sb.append("INSERT INTO IBD_O_OBJ (OBJ_ID, OBJ_TYPE_ID, OBJ_NAME, JSON_FILE, LAST_ACT_USER_ID) ");
			sb.append("VALUES (?,?,?,?,?) ");
			jdbcTemplate.update(sb.toString(), new Object[] { uuid, objTypeId, name.toUpperCase(), json, userId });
			// Update H
			String updateAction = "INSERT";
			StringBuffer sbH = new StringBuffer();
			sbH.append("INSERT INTO IBD_H_OBJ (OBJ_ID, OBJ_TYPE_ID, OBJ_NAME, JSON_FILE, LAST_ACT_USER_ID, UPDATE_ACTION) ");
			sbH.append("VALUES (?,?,?,?,?,?) ");
			jdbcTemplate.update(sbH.toString(), new Object[] { uuid, objTypeId, name.toUpperCase(), json, userId, updateAction });
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append("UPDATE IBD_O_OBJ ");
			sb.append("SET OBJ_NAME = ?, ");
			sb.append("JSON_FILE = ?, ");
			sb.append("LAST_ACT_USER_ID = ?, ");
			sb.append("LAST_UPT_TIME = CURRENT_TIMESTAMP ");
			sb.append("WHERE OBJ_ID = ? ");
			jdbcTemplate.update(sb.toString(), new Object[] { name.toUpperCase(), json, userId, uuid });
			// Update H
			String updateAction = "UPDATE";
			StringBuffer sbH = new StringBuffer();
			sbH.append("INSERT INTO IBD_H_OBJ (OBJ_ID, OBJ_TYPE_ID, OBJ_NAME, JSON_FILE, LAST_ACT_USER_ID, UPDATE_ACTION) ");
			sbH.append("VALUES (?,?,?,?,?,?) ");
			jdbcTemplate.update(sbH.toString(), new Object[] { uuid, objTypeId, name.toUpperCase(), json, userId, updateAction });
		}
		
		// Query UUID
		String sqlObjEnv = "SELECT COUNT(*) from IBD_O_OBJ_ENV WHERE OBJ_ID = ? AND ENV_CODE = ?";
		int objEnvCount = jdbcTemplate.queryForObject(sqlObjEnv, new Object[] { uuid, envCode }, Integer.class);
		
		// Update IBD_O_OBJ_ENV
		if (objEnvCount == 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("INSERT INTO IBD_O_OBJ_ENV (OBJ_ID, ENV_CODE, LAST_ACT_USER_ID) ");
			sb.append("VALUES (?,?,?) ");
			jdbcTemplate.update(sb.toString(), new Object[] { uuid, envCode, userId });
		}
		
		resultMap.put("result", "success");

		return resultMap;
	}

	@Override
	public Map<String, Object> getObject(String uuid, String uniqueKey) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		StringBuffer sb = new StringBuffer();
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

		StringBuffer sb = new StringBuffer();		
		// sb.append("SELECT 'v' || ROW_NUMBER() OVER(ORDER BY AL1.LAST_UPT_TIME) as VERSION, ");
		sb.append("SELECT 'v' || (select count(*) from IBD_H_OBJ H where H.OBJ_ID = AL1.OBJ_ID and H.LAST_UPT_TIME <= AL1.LAST_UPT_TIME ) as VERSION, ");
		sb.append("AL1.OBJ_NAME, strftime('%Y%m%d%H%M%S', AL1.LAST_UPT_TIME) as UNIQUE_KEY, ");
		sb.append("COALESCE(AL2.USER_NAME, 'Unknown') AS USER_NAME, ");
		sb.append("strftime('%Y-%m-%d %H:%M:%S', AL1.LAST_UPT_TIME) as LAST_UPT_TIME ");
		sb.append("FROM IBD_H_OBJ AL1 ");
		sb.append("LEFT OUTER JOIN IBD_O_USER AL2 ON AL1.LAST_ACT_USER_ID = AL2.USER_ID "); 
		sb.append("WHERE OBJ_ID = ? ");
		sb.append("ORDER BY AL1.LAST_UPT_TIME DESC ");
		sb.append("LIMIT 10");

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb.toString(), new Object[] { uuid });

		resultMap.put("result", result);

		return resultMap;
	}

	@Override
	public Map<String, Object> makeObjectCurrent(String uuid, String uniqueKey, int userId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE IBD_O_OBJ ");
		sb.append("SET (OBJ_NAME, JSON_FILE, LAST_ACT_USER_ID, LAST_UPT_TIME) =  ");
		sb.append("(SELECT OBJ_NAME, JSON_FILE, " + userId + ", CURRENT_TIMESTAMP FROM IBD_H_OBJ ");
		sb.append("WHERE OBJ_ID = ? AND strftime('%Y%m%d%H%M%S', last_upt_time) = ?) ");
		sb.append("WHERE OBJ_ID = ? ");

		jdbcTemplate.update(sb.toString(), new Object[] { uuid, uniqueKey, uuid });

		resultMap.put("result", "success");

		return resultMap;
	}

	@Override
	public Map<String, Object> searchJobs(String queryString, String envCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		sb.append("select A.OBJ_ID, B.OBJ_TYPE, A.OBJ_NAME ");
		sb.append("FROM IBD_O_OBJ A ");
		sb.append("inner join IBD_R_OBJ_TYPE B on A.OBJ_TYPE_ID = B.OBJ_TYPE_ID ");
		sb.append("inner join IBD_O_OBJ_ENV C on A.OBJ_ID = C.OBJ_ID ");
		sb.append("where B.OBJ_TYPE = 'JOB' and C.ENV_CODE = ? and A.OBJ_NAME like '%" + queryString.toUpperCase() + "%' ");

		List<Map<String, Object>> datalist = jdbcTemplate.queryForList(sb.toString(), new Object[] { envCode });
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		
		for (Map<String, Object> row : datalist) {
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
		StringBuffer sb = new StringBuffer();
		sb.append("select A.OBJ_ID, A.OBJ_NAME, strftime('%Y-%m-%d %H:%M:%S', A.LAST_UPT_TIME) as LAST_UPT_TIME ");
		sb.append("FROM IBD_O_OBJ A ");
		sb.append("inner join IBD_R_OBJ_TYPE B on A.OBJ_TYPE_ID = B.OBJ_TYPE_ID ");
		sb.append("inner join IBD_O_OBJ_ENV C on A.OBJ_ID = C.OBJ_ID ");
		sb.append("where B.OBJ_TYPE = 'JOB' and C.ENV_CODE = ? order by A.LAST_UPT_TIME desc ");
		sb.append("LIMIT 5");

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sb.toString(), new Object[] { envCode });

		resultMap.put("result", result);

		return resultMap;
	}

	//getLayoutList
	@Override
	public Map<String, Object> getLayoutList(String queryString) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		StringBuffer sq = new StringBuffer();
		sq.append("WITH TEMP AS(");
		sq.append(" SELECT distinct rtrim(CODE_NAME) as LAYOUT_NAME FROM IBD_O_COPYBOOK");
		sq.append(" WHERE CODE_NAME LIKE '%" + queryString + "%' "); 
		sq.append(") SELECT * from temp order by LAYOUT_NAME");
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sq.toString());
		
		List<Map<String, Object>> resultData = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> layoutElement : result)
		{    
			String leName  = (String) layoutElement.get("LAYOUT_NAME");

			Map<String, Object> resultElement = new HashMap<String, Object>();
			resultElement.put("name", leName);
			resultElement.put("value", leName);
			
			resultData.add(resultElement); 
		}
		
		resultMap.put("result", resultData);
		
		return resultMap;
	}


	//getLayout
	@Override
	public Map<String, Object> getLayout(String queryString) {
		Map<String, Object> resultMap  = new HashMap<String, Object>();
		List<Map<String, Object>> resultData = new ArrayList<Map<String, Object>>();
		StringBuffer sq = new StringBuffer();
		 
		sq.append("SELECT  FIELD_NAME as name ,FIELD_TYPE as type ,FIELD_LENGTH as length FROM IBD_O_COPYBOOK "); 
		sq.append("WHERE CODE_NAME = '" + queryString  + "' order by FIELD_START_POS asc"); 
		
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sq.toString());
 
		for (Map<String, Object> layoutElement : result)
		{    
			String leName  = (String) layoutElement.get("NAME");
			String leType  = (String) layoutElement.get("TYPE");
			long   leLenth = (Long) layoutElement.get("LENGTH");

			Map<String, Object> resultElement = new HashMap<String, Object>();
			resultElement.put("name", leName);
			resultElement.put("type", leType);
			resultElement.put("length", leLenth);
			
			resultData.add(resultElement); 
		}
		
		resultMap.put("name", queryString);
		resultMap.put("data", resultData);
		
		return resultMap;
	}
	
	//saveLayout
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> saveLayout(Map<String, Object> params) {
		// just ERWIN debug purpose
		//System.out.println("received layout in BUILDDAO = " + params.get("layout"));
		
		Map<String, Object> layOutMap = (Map<String, Object>) params.get("layout");
		String layOutName = (String) layOutMap.get("name");
		//System.out.println("Current LayoutName = " + layOutName);	
		
		List<Map<String, Object>> newLayout =  (List<Map<String, Object>>) layOutMap.get("data");
		//System.out.println("Current LayoutName data = " + newLayout);	
	 
		//delete existing records first before insert
		String deleteSQ =  "DELETE FROM IBD_O_COPYBOOK WHERE CODE_NAME = '" + layOutName + "'";
		//System.out.println("SQL for delete  = " + deleteSQ);	
		jdbcTemplate.execute(deleteSQ); 
 		 
		long   	fieldStartPos    = 1;
		long	priorFieldLength = 0;		
		for (Map<String, Object> entry : newLayout) {
			if (entry.isEmpty() != true) {
				String fieldName       = entry.get("name").toString().trim();
				       fieldStartPos   = fieldStartPos + priorFieldLength;
				int    fieldLength     = (int) entry.get("length");
				String fieldType       = entry.get("type").toString().trim();
				long   lastActUserId   = 0;
				
				String insertSQ =  "INSERT INTO IBD_O_COPYBOOK (CODE_NAME, FIELD_NAME, FIELD_START_POS, FIELD_LENGTH, FIELD_TYPE, LAST_ACT_USER_ID) "
						   + "VALUES ('" + layOutName + "','" 
				           + fieldName + "', " + fieldStartPos + "," + fieldLength 
				           + ",'" + fieldType + "'," + lastActUserId + ")";
				//System.out.println("SQL for insert  = " + insertSQ);	
				jdbcTemplate.execute(insertSQ); 
				//
				priorFieldLength       = fieldLength;				
			}
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		return resultMap;
      }

	// getTableList
	@Override
	public List<Map<String, Object>> getTableList(String schema, String queryTable) {
		StringBuffer sq = new StringBuffer();
		sq.append("SELECT DISTINCT TABNAME FROM SYSCAT.TABLES WHERE TABNAME LIKE '");
		sq.append(queryTable.trim().toUpperCase());
		sq.append("%' ORDER BY TABNAME FETCH FIRST 15 ROWS ONLY");
		List<Map<String, Object>> result = jdbcTemplateCodeGen.queryForList(sq.toString());

		List<Map<String, Object>> responseList = new ArrayList<Map<String, Object>>();

		for (Map<String, Object> entry : result) {
			Map<String, Object> tableName = new HashMap<String, Object>();
			tableName.put("value", entry.get("TABNAME").toString().trim());
			responseList.add(tableName);
		}

		return responseList;
	}

	// getColumnList
	@Override
	public List<Map<String, Object>> getTableColumnList(String schema, String tableName) {
		StringBuffer sq = new StringBuffer();
		sq.append("SELECT COLNAME as NAME,TYPENAME as COLTYPE,LENGTH,SCALE,NULLS FROM SYSCAT.COLUMNS WHERE TABNAME = '");
		sq.append(tableName.trim().toUpperCase());
        sq.append("' ORDER BY COLNO");
		List<Map<String, Object>> result = jdbcTemplateCodeGen.queryForList(sq.toString());

		return result;
	}

}
