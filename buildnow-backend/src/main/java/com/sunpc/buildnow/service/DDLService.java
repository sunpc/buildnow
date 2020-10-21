package com.sunpc.buildnow.service;

import com.sunpc.buildnow.dao.IDDLDao;
import com.sunpc.buildnow.util.ddl.MVSDDLUtil;
import com.sunpc.buildnow.util.io.CodeUtil;
import com.sunpc.buildnow.util.io.PDFUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class DDLService implements IDDLService {

    @Autowired
    private IDDLDao DDLDao;
    
    private String uploadPath = "C:/temp/zbuildUpload/";

    @Override
    public Map<String, Object> getDDL(List<Map<String, Object>> tables, Map<String, Object> properties) {
        MVSDDLUtil util = new MVSDDLUtil();
        Map<String, Object> buildResult = new HashMap<String, Object>();

        List<Map<String, Object>> codes = new ArrayList<Map<String, Object>>();

        String jobName = (String) properties.get("jobName");
        String jobUser = (String) properties.get("jobUser");
        String jobMessage = (String) properties.get("jobMessage");
        String jobTime = (String) properties.get("jobTime");
        String jobLib = (String) properties.get("jobLib");
        List<Map<String, Object>> jobIncludes = (ArrayList<Map<String, Object>>) properties.get("jobIncludes");
        String jobIncludesName = (String) jobIncludes.get(0).get("name");
        Map<String, Object> db2Properties = (HashMap<String, Object>) properties.get("db2Properties");
        String defaultSchema = (String) db2Properties.get("defaultSchema");
        String promoteName = (String) properties.get("promoteName");

        String tableName = "";
        String tableNameH = "";
        String tableNameC = "";
        String fileID = "";
        String fileIDH = "";
        String fileIDC = "";
        String instJCLCode = "";

        if (!"".equals(jobName)) {
            // generate instjcl job header
            instJCLCode = util.getDMDHeader(jobName, jobUser, jobMessage, jobTime, jobLib, jobIncludesName);
        }

        if (!"".equals(promoteName)) {
            // generate promote job header
            util.getPromoteHeader(promoteName);
        }


        for (int tableIndex = 0; tableIndex < tables.size() - 1; tableIndex++) {
            Map<String, Object> table = tables.get(tableIndex);

            tableName = (String) table.get("table");
            if (tableName != "" && tableName != null) {
                String dbname = (String) table.get("dbname");
                String tsname = (String) table.get("tsname");
                String tsnameH = (String) table.get("tsnameH");
                String tsnameC = (String) table.get("tsnameC");
                String partitionKeys = (String) table.get("partitions");
                String repeats = (String) table.get("repeats");

                fileID = tsname.substring(tsname.length() - 3);

                List<Map<String, Object>> columnList = DDLDao.getColumnList(tableName);

                List<Map<String, Object>> columnListH = new ArrayList<Map<String, Object>>();

                if (tsnameH != null && !"".equals(tsnameH)) {
                    if (tableName.contains("FMST_O")) {
                        tableNameH = tableName.replace("FMST_O", "FMST_H");
                    } else if (tableName.contains("FMST_R")) {
                        tableNameH = tableName.replace("FMST_R", "FMST_H");
                    }
                    fileIDH = tsnameH.substring(tsnameH.length() - 3);

                    columnListH = DDLDao.getColumnList(tableNameH);
                }
                boolean triggerIndc = true;
                if (tsnameH == null || "".equals(tsnameH)) {
                    triggerIndc = false;
                }

                List<Map<String, Object>> columnListC = new ArrayList<Map<String, Object>>();

                if (tsnameC != null && !"".equals(tsnameC)) {
                    if (tableName.contains("FMST_O")) {
                        tableNameC = tableName.replace("FMST_O", "FMST_C");
                    } else if (tableName.contains("FMST_R")) {
                        tableNameC = tableName.replace("FMST_R", "FMST_C");
                    }
                    fileIDC = tsnameC.substring(tsnameC.length() - 3);

                    columnListC = DDLDao.getColumnList(tableNameC);
                }


                if (columnList.size() != 0) {

                    List<Map<String, Object>> indexList = DDLDao.getIndexList(tableName);

                    codes.add(util.getCNTN(dbname, tsname, fileID));
                    codes.add(util.getCOM(tableName, fileID, columnList));
                    codes.add(util.getCOPY(dbname, tsname, fileID));
                    codes.add(util.getFMSD(dbname, tsname, fileID));
                    codes.add(util.getFMST(dbname, tsname, tableName, fileID, columnList, indexList, triggerIndc, partitionKeys));
                    codes.add(util.getFMSV0(tableName, fileID, columnList));
                    codes.add(util.getFMSV1(tableName, fileID, columnList));
                    codes.add(util.getFMSV(tableName, tsname, fileID));
                    codes.add(util.getFMSW(tableName, tsname, fileID));
                    codes.add(util.getGRANT(tableName, fileID));
                    codes.add(util.getLOD1(dbname, tsname, tableName, defaultSchema, fileID, columnList));
                    codes.add(util.getLOD(dbname, tsname, tableName, defaultSchema, fileID, columnList));
                    codes.add(util.getREO(dbname, tsname, fileID));
                    codes.add(util.getRUN(dbname, tsname, fileID));
                    codes.add(util.getSTRW(dbname, tsname, fileID));
                    codes.add(util.getSTUT(dbname, tsname, fileID));
                    codes.add(util.getTER(fileID));
                    codes.add(util.getTERS(fileID));
                    if (partitionKeys != null && !"".equals(partitionKeys)) {
                        int partitionNum = MVSDDLUtil.getPartitionNum(partitionKeys);

                        int loadCard = 1;

                        if (repeats != null && !"".equals(repeats)) {
                            loadCard = Integer.parseInt(repeats);
                        } else {
                            loadCard = partitionNum;
                        }

                        codes.add(util.getLODP(dbname, tsname, tableName, defaultSchema, fileID, columnList));
                        codes.add(util.getLODQ(dbname, tsname, tableName, defaultSchema, fileID, columnList, loadCard));
                        codes.add(util.getRUNP(dbname, tsname, tableName, defaultSchema, fileID));
                        codes.add(util.getRUNQ(dbname, tsname, tableName, defaultSchema, fileID, loadCard));
                        codes.add(util.getSTRWP(dbname, tsname, fileID));
                        codes.add(util.getSTRWQ(dbname, tsname, fileID, loadCard));
                        codes.add(util.getSTUTP(dbname, tsname, fileID));
                        codes.add(util.getSTUTQ(dbname, tsname, fileID, loadCard));
                        codes.add(util.getTERP(fileID));
                        codes.add(util.getTERQ(fileID));
                        codes.add(util.getTERSP(fileID));
                        codes.add(util.getTERSQ(fileID));
                        codes.add(util.getCNTNP(dbname, tsname, fileID));
                        codes.add(util.getCNTNQ(dbname, tsname, fileID, loadCard));
                    }

                    if (tsnameH != null && !"".equals(tsnameH)) {
                        List<Map<String, Object>> indexListH = DDLDao.getIndexList(tableNameH);
                        codes.add(util.getCNTN(dbname, tsnameH, fileIDH));
                        codes.add(util.getCOM(tableNameH, fileIDH, columnListH));
                        codes.add(util.getCOPY(dbname, tsnameH, fileIDH));
                        codes.add(util.getFMSD(dbname, tsnameH, fileIDH));
                        codes.add(util.getFMST(dbname, tsnameH, tableNameH, fileIDH, columnListH, indexListH, triggerIndc, ""));
                        codes.add(util.getFMSV0(tableNameH, fileIDH, columnListH));
                        codes.add(util.getFMSV1(tableNameH, fileIDH, columnListH));
                        codes.add(util.getFMSV(tableNameH, tsnameH, fileIDH));
                        codes.add(util.getFMSW(tableNameH, tsnameH, fileIDH));
                        codes.add(util.getGRANT(tableNameH, fileIDH));
                        codes.add(util.getLOD1(dbname, tsnameH, tableNameH, defaultSchema, fileIDH, columnListH));
                        codes.add(util.getLOD(dbname, tsnameH, tableNameH, defaultSchema, fileIDH, columnListH));
                        codes.add(util.getREO(dbname, tsnameH, fileIDH));
                        codes.add(util.getRUN(dbname, tsnameH, fileIDH));
                        codes.add(util.getSTRW(dbname, tsnameH, fileIDH));
                        codes.add(util.getSTUT(dbname, tsnameH, fileIDH));
                        codes.add(util.getTER(fileIDH));
                        codes.add(util.getTERS(fileIDH));
                    }
                    if (tsnameC != null && !"".equals(tsnameC)) {
                        List<Map<String, Object>> indexListC = DDLDao.getIndexList(tableNameC);
                        codes.add(util.getCNTN(dbname, tsnameC, fileIDC));
                        codes.add(util.getCOM(tableNameC, fileIDC, columnListC));
                        codes.add(util.getCOPY(dbname, tsnameC, fileIDC));
                        codes.add(util.getFMSD(dbname, tsnameC, fileIDC));
                        codes.add(util.getFMST(dbname, tsnameC, tableNameC, fileIDC, columnListC, indexListC, triggerIndc, ""));
                        codes.add(util.getFMSV0(tableNameC, fileIDC, columnListC));
                        codes.add(util.getFMSV1(tableNameC, fileIDC, columnListC));
                        codes.add(util.getFMSV(tableNameC, tsnameC, fileIDC));
                        codes.add(util.getFMSW(tableNameC, tsnameC, fileIDC));
                        codes.add(util.getGRANT(tableNameC, fileIDC));
                        codes.add(util.getLOD1(dbname, tsnameC, tableNameC, defaultSchema, fileIDC, columnListC));
                        codes.add(util.getLOD(dbname, tsnameC, tableNameC, defaultSchema, fileIDC, columnListC));
                        codes.add(util.getREO(dbname, tsnameC, fileIDC));
                        codes.add(util.getRUN(dbname, tsnameC, fileIDC));
                        codes.add(util.getSTRW(dbname, tsnameC, fileIDC));
                        codes.add(util.getSTUT(dbname, tsnameC, fileIDC));
                        codes.add(util.getTER(fileIDC));
                        codes.add(util.getTERS(fileIDC));
                    }

                    instJCLCode = util.getDMD(tableName, tableNameH,
                            tableNameC, fileID, fileIDH, fileIDC, instJCLCode);

                }
            }
        }

        if (!"".equals(jobName)) {
            // generate instjcl job header
            codes.add(util.createDMD(jobName, instJCLCode));
        }

        if (!"".equals(promoteName)) {
            // generate instjcl job header
            codes.add(util.getPromoteMmeber(promoteName, jobName));
        }

        // save the generated code
        CodeUtil codeUtil = new CodeUtil();
        codeUtil.saveCode((String) properties.get("uuid"), codes);

        // return build result
        buildResult.put("code", codes);
        buildResult.put("status", "success");

        return buildResult;
    }

    @Override
    public Map<String, Object> getTableList(String queryTable) {
        Map<String, Object> data = new HashMap<String, Object>();

        List<String> tableList = DDLDao.getSysTableList(queryTable);

        data.put("data", tableList);

        return data;

    }

    @Override
    public Map<String,Map<String, Object>> readPDF(MultipartFile file) {
        Map<String,Map<String, Object>> resultMap = new HashMap<String,Map<String, Object>>();

        //String Path = (String.valueOf(Thread.currentThread().getContextClassLoader().getResource(""))).replaceAll("/target/classes/", "/tempUpload/").replaceAll("file:/", "").replaceAll("%20", " ").trim();
        //if (Path.indexOf(":") != 1) {
        //    Path = File.separator + Path;
        //}

        String fileName = file.getOriginalFilename();
        String filePath = uploadPath + "PDF";
        File pdfFile = new File(filePath, fileName);
        try {
            if (!pdfFile.exists()) {
                if (!pdfFile.getParentFile().exists()) {
                    pdfFile.getParentFile().mkdirs();
                }
                pdfFile.createNewFile();
            }
            file.transferTo(pdfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Object> data = new HashMap();

        PDFUtil readPDFObj = new PDFUtil();

        ArrayList<String> allTableList = readPDFObj.getAllTable(pdfFile);

        List<Map<String, Object>> codegenTableList = DDLDao.getAllCodegenTables();
        List<Map<String, Object>> mvsTableList = DDLDao.getAllMvsTables();


        List<String> newTableList = readPDFObj.getNewTable(allTableList,codegenTableList,mvsTableList);

        ArrayList<Map<String, Object>> allTableResultList = new ArrayList();

        ArrayList<Map<String, Object>> newTableResultList = new ArrayList();

        for (int i = 0; i < allTableList.size(); i++) {
            Map<String, Object> tableMap = new HashMap();
            tableMap.put("table", allTableList.get(i));
            allTableResultList.add(tableMap);
        }

        data.put("All", allTableResultList);

        for (int i = 0; i < newTableList.size(); i++) {
            Map<String, Object> tableMap = new HashMap();
            tableMap.put("table", newTableList.get(i));
            newTableResultList.add(tableMap);
        }

        data.put("New", newTableResultList);

        resultMap.put("data",data);

        return resultMap;

    }

}
