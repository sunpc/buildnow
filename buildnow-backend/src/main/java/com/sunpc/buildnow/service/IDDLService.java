package com.sunpc.buildnow.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IDDLService {

	Map<String, Object> getDDL(List<Map<String, Object>> tables, Map<String, Object> properties);

	Map<String, Object> getTableList(String queryTable);

	Map<String,Map<String, Object>> readPDF(MultipartFile file);

}
