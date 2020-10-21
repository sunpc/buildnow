package com.sunpc.buildnow.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class DDLDao implements IDDLDao {

    @Autowired
    @Qualifier("primaryJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    private JdbcTemplate jdbcTemplateCodeGen;

    // getColumnList
    @Override
    public List<Map<String, Object>> getColumnList(String tableName) {
        StringBuffer sq = new StringBuffer();
        sq.append("SELECT COLNAME,TYPENAME,LENGTH,SCALE,NULLS,IDENTITY,CASE WHEN DEFAULT IS NULL THEN '' ELSE DEFAULT END AS DEFAULT FROM SYSCAT.COLUMNS WHERE TABNAME = '");
        sq.append(tableName);
        sq.append("' ORDER BY COLNO");
        List<Map<String, Object>> result = jdbcTemplateCodeGen.queryForList(sq.toString());

        return result;
    }

    // getIndexList
    @Override
    public List<Map<String, Object>> getIndexList(String tableName) {
        StringBuffer sq = new StringBuffer();
        sq.append("SELECT IX.INDNAME,KEY.COLNAME,IX.UNIQUERULE,COL.TYPENAME FROM SYSCAT.INDEXES IX " +
                "JOIN SYSCAT.INDEXCOLUSE KEY ON IX.INDNAME = KEY.INDNAME " +
                "JOIN SYSCAT.COLUMNS COL ON KEY.COLNAME = COL.COLNAME AND IX.TABNAME = COL.TABNAME " +
                " WHERE IX.TABNAME = '");
        sq.append(tableName);
        sq.append("' ORDER BY IX.INDNAME,KEY.COLSEQ");
        List<Map<String, Object>> result = jdbcTemplateCodeGen.queryForList(sq.toString());

        return result;
    }

    // getTableList
    @Override
    public List<String> getSysTableList(String queryTable) {
        StringBuffer sq = new StringBuffer();
        sq.append("SELECT DISTINCT TABNAME FROM SYSCAT.TABLES WHERE TABNAME LIKE '");
        sq.append(queryTable.trim().toUpperCase());
        sq.append("%' ORDER BY TABNAME FETCH FIRST 15 ROWS ONLY");
        List<Map<String, Object>> result = jdbcTemplateCodeGen.queryForList(sq.toString());

        List<String> responseList = new ArrayList<String>();

        for (Map<String, Object> entry : result) {
            String tableName = entry.get("TABNAME").toString().trim();
            responseList.add(tableName);
        }

        return responseList;
    }


    // getColumnList
    @Override
    public List<Map<String, Object>> getAllMvsTables() {
        StringBuffer sq = new StringBuffer();
        //sq.append("SELECT DISTINCT NAME AS TABNAME FROM SYSIBM.SYSTABLES WHERE TYPE = 'T' AND NAME LIKE 'FMST_%' ORDER BY NAME");
        sq.append("SELECT DISTINCT TABNAME AS TABNAME FROM SYSCAT.TABLES WHERE TYPE = 'T' AND TABNAME LIKE 'FMST_%' ORDER BY TABNAME");
        List<Map<String, Object>> result = jdbcTemplateCodeGen.queryForList(sq.toString());

        return result;
    }

    // getColumnList
    @Override
    public List<Map<String, Object>> getAllCodegenTables() {
        StringBuffer sq = new StringBuffer();
        sq.append("SELECT DISTINCT TABNAME AS TABNAME FROM SYSCAT.TABLES WHERE TYPE = 'T' AND TABNAME LIKE 'FMST_%' ORDER BY TABNAME");
        List<Map<String, Object>> result = jdbcTemplateCodeGen.queryForList(sq.toString());

        return result;
    }
}
