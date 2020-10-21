package com.sunpc.buildnow.dao;

import java.util.List;
import java.util.Map;

public interface IDDLDao {

    List<Map<String, Object>> getColumnList(String tableName);

    List<Map<String, Object>> getIndexList(String tableName);

    List<String> getSysTableList(String queryTable);

    List<Map<String, Object>> getAllMvsTables();

    List<Map<String, Object>> getAllCodegenTables();

}
