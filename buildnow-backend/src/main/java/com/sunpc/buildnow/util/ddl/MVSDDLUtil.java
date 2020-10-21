package com.sunpc.buildnow.util.ddl;

import java.text.SimpleDateFormat;
import java.util.*;

public class MVSDDLUtil {
    Map<String, Object> code = new HashMap<String, Object>();

    String promoteMember = "";

    public Map<String, Object> getCNTN(String dbname, String tsname, String fileID) {
        String ddl = "";
        ddl += "EXEC 'TFMS.DD.CLIST(CONTENTN)' +\r\n";
        ddl += "'LOAD " + dbname + " " + tsname + " 1 20 CDA1'\r\n";
        code = putDB2UTIL(ddl, "CNTN" + fileID + ".sql", code);
        promoteMember += "CNTN" + fileID + "     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getCNTNP(String dbname, String tsname, String fileID) {
        String ddl = "";
        ddl += "EXEC 'TFMS.DD.CLIST(CONTENTN)' +\r\n";
        ddl += "'LOAD " + dbname + " " + tsname + " 1 20 CDA1 #PNO#'\r\n";
        code = putDB2UTIL(ddl, "CNTN" + fileID + "P.sql", code);
        promoteMember += "CNTN" + fileID + "P    DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getCNTNQ(String dbname, String tsname, String fileID, int partitionNum) {
        String ddl = "";
        for (int partIndc = 1; partIndc <= partitionNum; partIndc++) {
            String PN = getPartNum(partIndc);
            ddl += "EXEC 'TFMS.DD.CLIST(CONTENTN)' +\r\n";
            ddl += "'LOAD " + dbname + " " + tsname + " 1 20 CDA1 #PNO"+PN+"#'\r\n";
        }

        code = putDB2UTIL(ddl, "CNTN" + fileID + "Q.sql", code);
        promoteMember += "CNTN" + fileID + "Q    DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getCOM(String tableName, String fileID, List<Map<String, Object>> columnList) {
        String ddl = "";
        ddl += "--********************************************************************\r\n";
        ddl += "--* CREATE COMMENTS ON TABLE                                          \r\n";
        ddl += "--* This DDL applies comments to a table and to all columns within    \r\n";
        ddl += "--* the table                                                         \r\n";
        ddl += "--**----------------------------------------------------------------**\r\n";
        ddl += "--                                                                    \r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "-- Comment on table                                                   \r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "                                                                      \r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "--* Comment on columns                                                \r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "COMMENT ON " + tableName + "\r\n(\r\n";

        int i = 1;
        for (Map<String, Object> entry : columnList) {
            String colName = entry.get("COLNAME").toString().trim();
            ddl += "--** " + colName + "\r\n";
            if (i > 1) {
                ddl += ",";
            }
            ddl += colName + " IS\r\n";
            ddl += "   '" + colName + "'\r\n";

            i++;
        }

        ddl += ");\r\n";
        ddl += "COMMIT;\r\n";

        code = putDB2UTIL(ddl, "COM" + fileID + ".sql", code);
        promoteMember += "COM" + fileID + "      DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getCOPY(String dbname, String tsname, String fileID) {
        String ddl = "";
        ddl += "COPY TABLESPACE " + dbname + "." + tsname + " DSNUM ALL\r\n";

        code = putDB2UTIL(ddl, "COPY" + fileID + ".sql", code);
        promoteMember += "COPY" + fileID + "     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getFMSD(String dbname, String tsname, String fileID) {
        String ddl = "";
        ddl += "DROP TABLESPACE " + dbname + "." + tsname + ";\r\n";
        ddl += "COMMIT;\r\n";

        code = putDB2UTIL(ddl, "FMSD" + fileID + ".sql", code);
        promoteMember += "FMSD" + fileID + "     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getFMST(String dbname, String tsname, String tableName, String fileID,
                                       List<Map<String, Object>> columnList, List<Map<String, Object>> indexList, boolean triggerIndc, String partitionKeys) {

        boolean partitionFlag = false;
        int partitionNum = 1;
        List<String> partIdxList = new ArrayList<>();
        // EFFC_DATE:10;PART_AUX_ID:400
        if (partitionKeys != null && !"".equals(partitionKeys)) {
            partitionFlag = true;

            partitionNum = getPartitionNum(partitionKeys);


        }

        String ddl = "";
        ddl += "--********************************************************************\r\n";
        ddl += "--* FMST" + fileID + "- CREATE TABLESPACE/TABLE/INDEX/VIEW                       \r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "--* CREATE TABLESPACE\r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "CREATE TABLESPACE " + tsname + "\r\n";
        ddl += "     IN " + dbname + "\r\n";
        if (partitionFlag == false) {
            ddl += "   USING STOGROUP FMSG001\r\n";
            ddl += "         PRIQTY 480\r\n";
            ddl += "         SECQTY 8000\r\n";
            ddl += "         ERASE  NO\r\n";
            ddl += "     FREEPAGE   0\r\n";
            ddl += "     PCTFREE    10\r\n";
            ddl += "     BUFFERPOOL BP0\r\n";
            ddl += "     CCSID      UNICODE\r\n";
            ddl += "     LOCKSIZE   ANY\r\n";
            ddl += "     LOCKMAX    SYSTEM\r\n";
            ddl += "     COMPRESS   NO\r\n";
            ddl += "     CLOSE      YES\r\n";
            ddl += "     MAXPARTITIONS 64\r\n";
            ddl += "     SEGSIZE    32;\r\n";
        } else {
            ddl += "     NUMPARTS " + partitionNum + "\r\n";
//            ddl += "     (\r\n";
//            for (int i = 1; i <= partitionNum; i++) {
//                ddl += "          PART " + i + " USING STOGROUP FMSG001\r\n";
//                ddl += "               PRIQTY 50\r\n";
//                ddl += "               SECQTY 72000\r\n";
//                ddl += "               ERASE  NO\r\n";
//                if (i < partitionNum) {
//                    ddl += "          ,\r\n";
//                } else {
//                    ddl += "          )\r\n";
//                }
//
//            }
            ddl += "   USING STOGROUP FMSG001\r\n";
            ddl += "         PRIQTY 480\r\n";
            ddl += "         SECQTY 8000\r\n";
            ddl += "         ERASE  NO\r\n";
            ddl += "     FREEPAGE   0\r\n";
            ddl += "     PCTFREE    10\r\n";
            ddl += "     BUFFERPOOL BP32K\r\n";
            ddl += "     CCSID      UNICODE\r\n";
            ddl += "     LOCKSIZE   ANY\r\n";
            ddl += "     LOCKMAX    SYSTEM\r\n";
            ddl += "     COMPRESS   YES\r\n";
            ddl += "     CLOSE      YES;\r\n";
        }
        ddl += "COMMIT;\r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "--* CREATE TABLE\r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "CREATE TABLE " + tableName + "\r\n(\r\n";

        int i = 1;
        for (Map<String, Object> entry : columnList) {
            String colName = entry.get("COLNAME").toString().trim();
            String colType = entry.get("TYPENAME").toString().trim();
            String colLength = entry.get("LENGTH").toString().trim();
            String colNullable = entry.get("NULLS").toString().trim();
            String colScale = entry.get("SCALE").toString().trim();
            String colIdentity = entry.get("IDENTITY").toString().trim();
            String colDefault = entry.get("DEFAULT").toString().trim();

            if ("CHARACTER".equals(colType) && !"13".equals(colLength)) {
                colType = "CHAR(" + colLength + ") FOR MIXED DATA";
            } else if ("CHARACTER".equals(colType) && "13".equals(colLength)) {
                colType = "CHAR(" + colLength + ") FOR BIT DATA";
            } else if ("DECIMAL".equals(colType)) {
                colType = colType + "(" + colLength + "," + colScale + ")";
            } else if ("BIGINT".equals(colType) || "INTEGER".equals(colType) || "DATE".equals(colType)
                    || "TIMESTAMP".equals(colType)) {
            } else if ("VARCHAR".equals(colType)) {
                colType = "VARCHAR(" + colLength + ") FOR MIXED DATA";
            } else {
                colType = colType + "(" + colLength + ")";
            }

            if ("N".equals(colNullable)) {
                colNullable = "NOT NULL";
            } else {
                colNullable = "";
            }

            String autoGenerate = "";
            if ("Y".equals(colIdentity)) {
                autoGenerate += "\r\n";
                autoGenerate += "          GENERATED BY DEFAULT AS IDENTITY (\r\n";
                autoGenerate += "          START WITH + 1\r\n";
                autoGenerate += "          INCREMENT BY + 1\r\n";
                autoGenerate += "          MINVALUE + 1\r\n";
                autoGenerate += "          MAXVALUE + 9223372036854775807\r\n";
                autoGenerate += "          NO CYCLE\r\n";
                autoGenerate += "          CACHE 20\r\n";
                autoGenerate += "          NO ORDER )";
            }

            ddl += "  " + colName + " " + colType + " " + colNullable + "" + autoGenerate;

            if ("LAST_UPT_TIME".equals(colName)) {
                ddl += " GENERATED BY DEFAULT FOR EACH ROW\r\n   ON UPDATE AS ROW CHANGE TIMESTAMP";
            } else if ("CURRENT TIMESTAMP".equals(colDefault)) {
                ddl += "\r\n    WITH DEFAULT";
            }

            if (i < columnList.size()) {
                ddl += ",\r\n";
            }
            i++;
        }

        boolean pkFlag = true;
        int pkPostion = 1;
        List<Map<String, Object>> pkList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> entry : indexList) {
            String uniqueRule = entry.get("UNIQUERULE").toString().trim();
            if ("P".equals(uniqueRule)) {
                pkList.add(entry);
            }
        }

        if (pkList.size() != 0) {
            for (Map<String, Object> pkEntry : pkList) {
                String colName = pkEntry.get("COLNAME").toString().trim();
                if (pkFlag) {
                    ddl += ",\r\nPRIMARY KEY (\r\n";
                    pkFlag = false;
                }
                ddl += colName;

                partIdxList.add(colName);


                if (pkPostion == pkList.size()) {
                    ddl += "\r\n)";
                } else {
                    ddl += ",\r\n";
                }

                pkPostion++;
            }

        }

        ddl += "\r\n)\r\n";

        ddl += "IN " + dbname + "." + tsname;

        ddl += "\r\n";

        if (partitionFlag == false) {
            ddl += ";\r\n";
        } else {
            ddl += " PARTITION BY (";
            String[] partitionKey = partitionKeys.split(";");
            String partitionName;
            for (int j = 0; j < partitionKey.length; j++) {
                String[] partition = partitionKey[j].split(":");
                partitionName = partition[0];
                ddl += partitionName;
                partIdxList.add(partitionName);
                if (j < partitionKey.length - 1) {
                    ddl += ",";
                } else {
                    ddl += ")\r\n";
                }

            }
            ddl += "(\r\n";

            String currentYear = getCurrentYear();

            ArrayList<ArrayList<String>> list = new ArrayList<>();
            ArrayList<String> partitionList;

            for (int k = 1; k <= partitionNum; k++) {
                list.add(new ArrayList());
            }

            int partitionRange = partitionNum;
            int partitionCycle = partitionNum;
            int partitionDetail;
            String insertString;

            for (int k = 0; k < partitionKey.length; k++) {
                String[] partition = partitionKey[k].split(":");
                partitionName = partition[0];
                if (!partition[1].contains("(")) {
                    partitionDetail = Integer.parseInt(partition[1]);

                    if ("EFFC_DATE".equals(partitionName)) {
                        partitionDetail = partitionDetail * 2;
                    }

                    partitionCycle = partitionNum / partitionRange;
                    partitionRange = partitionRange / partitionDetail;
                    for (int m = 0; m < partitionNum; m++) {
                        for (int n = 1; n <= partitionDetail; n++) {
                            for (int p = 0; p < partitionCycle; p++) {
                                if ((m - partitionDetail * p) / partitionRange + 1 == n) {
                                    partitionList = list.get(m);
                                    if ("EFFC_DATE".equals(partitionName)) {
                                        if (n % 2 == 1) {
                                            insertString = "'" + (Integer.parseInt(currentYear) + n / 2) + "-01-01'";
                                        } else {
                                            insertString = "'" + (Integer.parseInt(currentYear) + (n - 1) / 2) + "-07-01'";
                                        }
                                    } else {
                                        insertString = Integer.toString(n);
                                    }
                                    partitionList.add(insertString);
                                }
                            }

                        }
                    }
                }
            }

            for (int x = 0; x < list.size(); x++) {
                ddl += "    PARTITION " + (x + 1) + " ENDING AT (";
                for (int y = 0; y < partitionKey.length; y++) {
                    ddl += list.get(x).get(y);
                    if (y < partitionKey.length - 1) {
                        ddl += ",";
                    } else {
                        ddl += ")";
                    }
                }
                if (x < list.size() - 1) {
                    ddl += ",\r\n";
                } else {
                    ddl += "\r\n);";
                }
            }

        }


        ddl += "\r\nCOMMIT;\r\n";

        ddl += "--********************************************************************\r\n";
        ddl += "--* CREATE INDEXES\r\n";
        ddl += "--********************************************************************\r\n";

        String currentName = "";
        int indexNum = 1;
        boolean clusterFlag = true;
        if (indexList.size() != 0) {

            boolean isPadded = false;
            for (Map<String, Object> entry : indexList) {
                String indName = entry.get("INDNAME").toString().trim();
                String colName = entry.get("COLNAME").toString().trim();
                String uniqueRule = entry.get("UNIQUERULE").toString().trim();
                String colType = entry.get("TYPENAME").toString().trim();

                if ("VARCHAR".equals(colType)) {
                    isPadded = true;
                }
                if ((!"".equals(currentName) && !currentName.equals(indName))) {
                    ddl += "\r\n)\r\n";
                    if (clusterFlag == true) {
                        ddl += "     CLUSTER ";
                        clusterFlag = false;
                    } else {
                        ddl += "     NOT CLUSTER ";
                    }

                    if (isPadded == true) {
                        ddl += "NOT PADDED";
                        isPadded = false;
                    }

//                    if (partitionFlag == false) {
                        ddl += "\r\n";
                    if(partitionFlag == true){
                        ddl += "PARTITIONED\r\n";
                    }
                        ddl += "       USING STOGROUP FMSG001\r\n";
                        ddl += "             PRIQTY 480\r\n";
                        ddl += "             SECQTY 8000\r\n";
                        ddl += "             ERASE  NO\r\n";

//                    }
//                    else {
//                        ddl += "     PARTITIONED\r\n";
//                        ddl += "     (\r\n";
//                        for (int z = 1; z <= partitionNum; z++) {
//                            ddl += "          PART " + z + " USING STOGROUP FMSG001\r\n";
//                            ddl += "               PRIQTY 50\r\n";
//                            ddl += "               SECQTY 72000\r\n";
//                            ddl += "               ERASE  NO\r\n";
//                            if (z < partitionNum) {
//                                ddl += "          ,\r\n";
//                            } else {
//                                ddl += "          )\r\n";
//                            }
//
//                        }
//                    }
                    ddl += "     FREEPAGE   0\r\n";
                    ddl += "     PCTFREE    10\r\n";
                    ddl += "     BUFFERPOOL BP1\r\n";
                    ddl += "     CLOSE      YES;\r\n\r\n";
                    ddl += "COMMIT;\r\n\r\n";

                    indexNum++;
                } else if (currentName.equals(indName)) {
                    ddl += ",\r\n";

                }
                if (!currentName.equals(indName)) {
                    if ("P".equals(uniqueRule) || "U".equals(uniqueRule)) {
                        ddl += "CREATE UNIQUE INDEX FMSX" + indexNum + fileID + "\r\n";
                    } else {
                        ddl += "CREATE INDEX FMSX" + indexNum + fileID + "\r\n";
                    }
                    ddl += "     ON " + tableName + "\r\n(\r\n";
                    ddl += "  " + colName;
                } else {
                    ddl += "  " + colName;
                }

                currentName = indName;


            }

            ddl += "\r\n)\r\n";
            if (clusterFlag == true) {
                ddl += "     CLUSTER ";
                clusterFlag = false;
            } else {
                ddl += "     NOT CLUSTER ";
            }

            if (isPadded == true) {
                ddl += "NOT PADDED";
                isPadded = false;
            }
            ddl += "\r\n";
            ddl += "       USING STOGROUP FMSG001\r\n";
            ddl += "             PRIQTY 480\r\n";
            ddl += "             SECQTY 8000\r\n";
            ddl += "             ERASE  NO\r\n";
            ddl += "     FREEPAGE   0\r\n";
            ddl += "     PCTFREE    10\r\n";
            ddl += "     BUFFERPOOL BP1\r\n";
            ddl += "     CLOSE      YES;\r\n\r\n";
            ddl += "COMMIT;\r\n\r\n";

        }

        if(partitionFlag == true) {
            indexNum++;
            ddl += "CREATE UNIQUE INDEX FMSX" + indexNum + fileID + "\r\n";
            ddl += "     ON " + tableName + "\r\n(\r\n";
            for (int partIdxIndc = 0; partIdxIndc < partIdxList.size(); partIdxIndc++) {
                ddl += "    " + partIdxList.get(partIdxIndc) + "    ASC";
                if (partIdxIndc < partIdxList.size() - 1) {
                    ddl += ",\r\n";
                } else {
                    ddl += "\r\n)\r\n";
                }
            }
            if(clusterFlag == true) {
                ddl += "CLUSTER\r\n";
            }
            ddl += "PARTITIONED\r\n";
            ddl += "       USING STOGROUP FMSG001\r\n";
            ddl += "             PRIQTY 480\r\n";
            ddl += "             SECQTY 8000\r\n";
            ddl += "             ERASE  NO\r\n";
            ddl += "     FREEPAGE   0\r\n";
            ddl += "     PCTFREE    10\r\n";
            ddl += "     BUFFERPOOL BP1\r\n";
            ddl += "     CLOSE      YES;\r\n\r\n";
            ddl += "COMMIT;\r\n\r\n";
        }



        ddl += "--********************************************************************\r\n";
        ddl += "--* CREATE DEFAULT VIEW\r\n";
        ddl += "--********************************************************************\r\n";
        String v0ViewName = "";
        if (tableName.contains("FMST")) {
            v0ViewName = tableName.replace("FMST", "FMSV0");
        } else {
            v0ViewName = "FMSV0_O_" + tableName;
        }
        ddl += "CREATE VIEW " + v0ViewName + " AS\r\n";
        ddl += "       SELECT * FROM " + tableName + ";\r\n";
        ddl += "COMMIT;\r\n\r\n";

        if (triggerIndc == true) {
            if (!tableName.contains("FMST_H")) {
                ddl += "--********************************************************************\r\n";
                ddl += "--* CREATE TRIGGERS \r\n";
                ddl += "--********************************************************************\r\n";
                ddl += "--#SET TERMINATOR #  \r\n";
                if (tableName.contains("FMST_O") || tableName.contains("FMST_R")) {
                    ddl += "--********************************************************************\r\n";
                    ddl += "--* CREATE TRIGGER  " + tableName + "_I  \r\n";
                    ddl += "--********************************************************************\r\n";
                    ddl += "CREATE TRIGGER " + tableName + "_I\r\n";
                    ddl += "AFTER INSERT ON  " + tableName + "\r\n";
                    ddl += "REFERENCING NEW AS I \r\n";
                    ddl += "FOR EACH ROW MODE DB2SQL \r\n";
                    if (tableName.contains("FMST_C_")) {
                        ddl += "BEGIN ATOMIC INSERT INTO " + tableName.replace("FMST_C_", "FMST_H_") + "  \r\n VALUES (\r\n";
                    } else if (tableName.contains("FMST_O_")) {
                        ddl += "BEGIN ATOMIC INSERT INTO " + tableName.replace("FMST_O_", "FMST_H_") + "  \r\n VALUES (\r\n";
                    } else if (tableName.contains("FMST_R_")) {
                        ddl += "BEGIN ATOMIC INSERT INTO " + tableName.replace("FMST_R_", "FMST_H_") + "  \r\n VALUES (\r\n";
                    }
                    ddl += "GENERATE_UNIQUE(),\r\n";
                    for (Map<String, Object> entry : columnList) {
                        String colName = entry.get("COLNAME").toString().trim();
                        ddl += "    I." + colName + ",\r\n";
                    }
                    ddl += "    'INSERT'\r\n); ";

                    ddl += "END#\r\n";
                    ddl += "COMMIT#\r\n";
                }


                ddl += "--********************************************************************\r\n";
                ddl += "--* CREATE TRIGGER  " + tableName + "_U  \r\n";
                ddl += "--********************************************************************\r\n";
                ddl += "CREATE TRIGGER " + tableName + "_U\r\n";
                ddl += "AFTER UPDATE ON  " + tableName + "\r\n";
                ddl += "REFERENCING NEW AS I \r\n";
                ddl += "FOR EACH ROW MODE DB2SQL \r\n";
                if (tableName.contains("FMST_C_")) {
                    ddl += "BEGIN ATOMIC INSERT INTO " + tableName.replace("FMST_C_", "FMST_H_") + "  \r\n(\r\n";
                } else if (tableName.contains("FMST_O_")) {
                    ddl += "BEGIN ATOMIC INSERT INTO " + tableName.replace("FMST_O_", "FMST_H_") + "  \r\n(\r\n";
                } else if (tableName.contains("FMST_R_")) {
                    ddl += "BEGIN ATOMIC INSERT INTO " + tableName.replace("FMST_R_", "FMST_H_") + "  \r\n(\r\n";
                }
                ddl += "UNIQUE_ID,\r\n";
                for (Map<String, Object> entry : columnList) {
                    String colName = entry.get("COLNAME").toString().trim();
                    ddl += colName + ",\r\n";
                }
                ddl += "LAST_ACT\r\n)\r\n";
                ddl += "VALUES\r\n(\r\n";
                ddl += "    GENERATE_UNIQUE(),\r\n";
                for (Map<String, Object> entry : columnList) {
                    String colName = entry.get("COLNAME").toString().trim();
                    ddl += "    I." + colName + ",\r\n";
                }
                ddl += "    'UPDATE'\r\n);\r\n";

                ddl += "END#\r\n";
                ddl += "COMMIT#\r\n";
            }

            if (tableName.contains("FMST_O") || tableName.contains("FMST_R")) {
                ddl += "--********************************************************************\r\n";
                ddl += "--* CREATE TRIGGER  " + tableName + "_D  \r\n";
                ddl += "--********************************************************************\r\n";
                ddl += "CREATE TRIGGER " + tableName + "_D\r\n";
                ddl += "AFTER DELETE ON  " + tableName + "\r\n";
                ddl += "REFERENCING OLD AS I \r\n";
                ddl += "FOR EACH ROW MODE DB2SQL \r\n";
                if (tableName.contains("FMST_C_")) {
                    ddl += "BEGIN ATOMIC INSERT INTO " + tableName.replace("FMST_C_", "FMST_H_") + "  \r\n VALUES (\r\n";
                } else if (tableName.contains("FMST_O_")) {
                    ddl += "BEGIN ATOMIC INSERT INTO " + tableName.replace("FMST_O_", "FMST_H_") + "  \r\n VALUES (\r\n";
                } else if (tableName.contains("FMST_R_")) {
                    ddl += "BEGIN ATOMIC INSERT INTO " + tableName.replace("FMST_R_", "FMST_H_") + "  \r\n VALUES (\r\n";
                }
                ddl += "GENERATE_UNIQUE(),\r\n";
                for (Map<String, Object> entry : columnList) {
                    String colName = entry.get("COLNAME").toString().trim();
                    ddl += "    I." + colName + ",\r\n";
                }
                ddl += "    'DELETE'\r\n); ";

                ddl += "END#\r\n";
                ddl += "COMMIT#\r\n";
            }
        }
        ddl += "--#SET TERMINATOR ;\r\n";

        code = putDB2UTIL(ddl, "FMST" + fileID + ".sql", code);
        promoteMember += "FMST" + fileID + "     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getFMSV0(String tableName, String fileID, List<Map<String, Object>> columnList) {
        String ddl = "";
        String v0ViewName = "";
        if (tableName.contains("FMST")) {
            v0ViewName = tableName.replace("FMST", "FMSV0");
        } else {
            v0ViewName = "FMSV0_O_" + tableName;
        }
        ddl += " /*********************************************************************/\r\n";
        ddl += " /* DCLGEN TABLE(\"" + v0ViewName + "\")                     */\r\n";
        ddl += " /*        LIBRARY(TFMS.F.DD.FMSDDL.TMP(FMSVN$$$))                    */\r\n";
        ddl += " /*        ACTION(REPLACE)                                            */\r\n";
        ddl += " /*        LANGUAGE(PLI)                                              */\r\n";
        ddl += " /*        APOST                                                      */\r\n";
        ddl += " /*        LABEL(YES)                                                 */\r\n";
        ddl += " /* ... IS THE DCLGEN COMMAND THAT MADE THE FOLLOWING STATEMENTS      */\r\n";
        ddl += " /*********************************************************************/\r\n";
        ddl += " EXEC SQL DECLARE \"" + v0ViewName + "\" TABLE\r\n";
        ddl += " (\r\n";
        int i = 1;
        for (Map<String, Object> entry : columnList) {
            String colName = entry.get("COLNAME").toString().trim();
            String colType = entry.get("TYPENAME").toString().trim();
            String colLength = entry.get("LENGTH").toString().trim();
            String colNullable = entry.get("NULLS").toString().trim();
            String colScale = entry.get("SCALE").toString().trim();

            if ("CHARACTER".equals(colType)) {
                colType = "CHAR(" + colLength + ")";
            } else if ("DECIMAL".equals(colType)) {
                colType = colType + "(" + colLength + "," + colScale + ")";
            } else if ("BIGINT".equals(colType) || "INTEGER".equals(colType) || "DATE".equals(colType)
                    || "TIMESTAMP".equals(colType)) {
            } else if ("VARCHAR".equals(colType)) {
                colType = "VARCHAR(" + colLength + ")";
            } else {
                colType = colType + "(" + colLength + ")";
            }

            if ("N".equals(colNullable)) {
                colNullable = "NOT NULL";
            } else {
                colNullable = "";
            }

            ddl += "    " + colName + " " + colType + " " + colNullable;
            if (i == columnList.size()) {
                ddl += "\r\n );\r\n";
            } else {
                ddl += ",\r\n";
            }

            i++;
        }

        ddl += " /*********************************************************************/\r\n";
        ddl += " /* PLI DECLARATION FOR TABLE \"" + v0ViewName + "\"         */\r\n";
        ddl += " /*********************************************************************/\r\n";
        ddl += " DCL 1 DCL" + v0ViewName + ",\r\n";
        i = 1;
        for (Map<String, Object> entry : columnList) {
            String colName = entry.get("COLNAME").toString().trim();
            String colType = entry.get("TYPENAME").toString().trim();
            String colLength = entry.get("LENGTH").toString().trim();
            String colScale = entry.get("SCALE").toString().trim();

            if ("CHARACTER".equals(colType)) {
                colType = "CHAR(" + colLength + ")";
            } else if ("DECIMAL".equals(colType)) {
                colType = "DEC FIXED" + "(" + colLength + "," + colScale + ")";
            } else if ("BIGINT".equals(colType)) {
                colType = "BIN FIXED(63)";
            } else if ("INTEGER".equals(colType)) {
                colType = "BIN FIXED(31)";
            } else if ("TIMESTAMP".equals(colType)) {
                colType = "CHAR(26)";
            } else if ("DATE".equals(colType)) {
                colType = "CHAR(10)";
            } else if ("VARCHAR".equals(colType)) {
                colType = "CHAR(" + colLength + ") VAR";
            } else {
                colType = colType + "(" + colLength + ")";
            }

            ddl += "      5 " + colName + " " + colType;

            if (i == columnList.size()) {
                ddl += ";\r\n";
            } else {
                ddl += ",\r\n";
            }

            i++;
        }

        ddl += " /*********************************************************************/\r\n";
        ddl += " /* THE NUMBER OF COLUMNS DESCRIBED BY THIS DECLARATION IS 13         */\r\n";
        ddl += " /*********************************************************************/\r\n";

        code = putPLINCL(ddl, "FMSV0" + fileID + ".inc", code);

        return code;

    }

    public Map<String, Object> getFMSV1(String tableName, String fileID, List<Map<String, Object>> columnList) {
        String ddl = (String) getFMSV0(tableName, fileID, columnList).get("code");
        String changeDDL = ddl.replaceAll("FMSV0", "FMSV1");
        code = putPLINCL(changeDDL, "FMSV1" + fileID + ".inc", code);
        promoteMember += "FMSV0" + fileID + "    PLINCL     NMLMBR\r\n";
        promoteMember += "FMSV1" + fileID + "    PLINCL     NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getFMSV(String tableName, String tsname, String fileID) {
        String ddl = "";
        String v1ViewName = "";
        if (tableName.contains("FMST")) {
            v1ViewName = tableName.replace("FMST", "FMSV1");
        } else {
            v1ViewName = "FMSV1_O_" + tableName;
        }
        ddl += "--********************************************************************\r\n";
        ddl += "--* FMSV" + fileID + " - CREATE SECURITY VIEW /   BATCH VIEW/\r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "CREATE VIEW " + v1ViewName + " AS\r\n";
        ddl += "  SELECT *\r\n";
        ddl += "  FROM " + tableName + ";\r\n";
        ddl += "COMMIT;\r\n";
        code = putDB2UTIL(ddl, "FMSV" + fileID + ".sql", code);
        promoteMember += "FMSV" + fileID + "     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getFMSW(String tableName, String tsname, String fileID) {
        String ddl = "";
        String vsViewName = "";
        if (tableName.contains("FMST")) {
            vsViewName = tableName.replace("FMST", "FMSVS");
        } else {
            vsViewName = "FMSVS_O_" + tableName;
        }
        ddl += "--********************************************************************\r\n";
        ddl += "--* CREATE SECURITY VIEW FOR TABLE " + tableName + "\r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "--*\r\n";
        ddl += "--* IBM SECURITY VIEW\r\n";
        ddl += "--*\r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "CREATE VIEW " + vsViewName + " AS\r\n";
        ddl += "  SELECT *\r\n";
        ddl += "  FROM " + tableName + ";\r\n";
        ddl += "COMMIT;\r\n";
        code = putDB2UTIL(ddl, "FMSW" + fileID + ".sql", code);
        promoteMember += "FMSW" + fileID + "     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getGRANT(String tableName, String fileID) {
        String ddl = "";
        String v0View = "";
        String v1View = "";
        String vsView = "";
        if (tableName.contains("FMST")) {
            v0View = tableName.replace("FMST", "FMSV0");
            v1View = tableName.replace("FMST", "FMSV1");
            vsView = tableName.replace("FMST", "FMSVS");
        } else {
            v0View = "FMSV0_O_" + tableName;
            v1View = "FMSV1_O_" + tableName;
            vsView = "FMSVS_O_" + tableName;
        }
        ddl += "--********************************************************************\r\n";
        ddl += "--* GRANTCKM - GRANT ACCESS ON " + tableName + "\r\n";
        ddl += "--********************************************************************\r\n";
        ddl += "GRANT DELETE ON " + v1View + " TO FMSWEBDD;\r\n";
        ddl += "GRANT INSERT ON " + v1View + " TO FMSWEBDD;\r\n";
        ddl += "GRANT UPDATE ON " + v1View + " TO FMSWEBDD;\r\n\r\n";

        ddl += "GRANT SELECT ON " + tableName + " TO DBDFMSDS;\r\n";
        ddl += "GRANT SELECT ON " + tableName + " TO DMDAC;\r\n";
        ddl += "GRANT SELECT ON " + tableName + " TO DMDBA;\r\n";
        ddl += "GRANT SELECT ON " + tableName + " TO DMDPS;\r\n\r\n";

        ddl += "GRANT SELECT ON " + v0View + " TO DBDFMSDS;\r\n";
        ddl += "GRANT SELECT ON " + v0View + " TO DMDAC;\r\n";
        ddl += "GRANT SELECT ON " + v0View + " TO DMDBA;\r\n";
        ddl += "GRANT SELECT ON " + v0View + " TO DMDPS;\r\n\r\n";

        ddl += "GRANT SELECT ON " + v1View + " TO DBDFMSDS;\r\n";
        ddl += "GRANT SELECT ON " + v1View + " TO FMSWEBDD;\r\n";
        ddl += "GRANT SELECT ON " + v1View + " TO FMSCMDDD;\r\n";
        ddl += "GRANT SELECT ON " + v1View + " TO DMDGCRLD;\r\n";
        ddl += "GRANT SELECT ON " + v1View + " TO DMDAC;\r\n";
        ddl += "GRANT SELECT ON " + v1View + " TO DMDBA;\r\n";
        ddl += "GRANT SELECT ON " + v1View + " TO DMDPS;\r\n\r\n";

        ddl += "GRANT SELECT ON " + vsView + " TO DBDFMSDS;\r\n";
        ddl += "GRANT SELECT ON " + vsView + " TO DMDAC;\r\n";
        ddl += "GRANT SELECT ON " + vsView + " TO DMDBA;\r\n";
        ddl += "GRANT SELECT ON " + vsView + " TO DMDPS;\r\n";
        ddl += "GRANT SELECT ON " + vsView + " TO FMSRDB2;\r\n\r\n";

        code = putDB2UTIL(ddl, "GRANT" + fileID + ".sql", code);
        promoteMember += "GRANT" + fileID + "    DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getLOD1(String dbname, String tsname, String tableName, String schema, String fileID,
                                       List<Map<String, Object>> columnList) {
        String ddl = "";
        ddl += "LOAD DATA INDDN SYSREC LOG NO  DISCARDS 1 REPLACE\r\n";
        ddl += "  UNICODE CCSID(00367,01208,01200)\r\n";
        ddl += "  INTO TABLE \"" + schema + "\".\"" + tableName + "\"\r\n(\r\n";
        int currentLength = 1;
        int i = 1;
        for (Map<String, Object> entry : columnList) {
            String colName = entry.get("COLNAME").toString().trim();
            String colType = entry.get("TYPENAME").toString().trim();
            int colLength = Integer.parseInt(entry.get("LENGTH").toString().trim());
            String colNullable = entry.get("NULLS").toString().trim();

            if ("CHARACTER".equals(colType)) {
                colType = "CHAR(" + colLength + ")";
            } else if ("DATE".equals(colType)) {
                colType = "DATE EXTERNAL(10)";
                colLength = 10;
            } else if ("TIMESTAMP".equals(colType)) {
                colType = "TIMESTAMP EXTERNAL(26)";
                colLength = 26;
            } else if ("BIGINT".equals(colType)) {
                colLength = 8;
            } else if ("INTEGER".equals(colType)) {
                colLength = 4;
            } else if ("DECIMAL".equals(colType)) {
                colLength = (colLength + 1) / 2;
            } else if ("VARCHAR".equals(colType)) {
                colLength = colLength + 2;
            } else {
                colType = colType + "(" + colLength + ")";
            }

            ddl += colName + "  POSITION(" + currentLength + ")\r\n";
            ddl += "  " + colType;

            currentLength += colLength;

            if ("Y".equals(colNullable)) {
                ddl += "\r\n";
                ddl += "  NULLIF(" + currentLength + ")='?'";
                currentLength++;
            }

            if (i == columnList.size()) {
                ddl += "\r\n)\r\n";
            } else {
                ddl += ",\r\n";
            }

            i++;
        }
        ddl += " REPAIR OBJECT LOG NO\r\n";
        ddl += " SET TABLESPACE " + dbname + "." + tsname + "\r\n";
        ddl += " NOCOPYPEND\r\n";
        code = putDB2UTIL(ddl, "LOD1" + fileID + ".sql", code);

        return code;
    }

    public Map<String, Object> getLOD(String dbname, String tsname, String tableName, String schema, String fileID,
                                      List<Map<String, Object>> columnList) {
        String ddl = (String) getLOD1(dbname, tsname, tableName, schema, fileID, columnList).get("code");
        String changeDDL = ddl.replaceAll("UNICODE", "EBCDIC");
        changeDDL = changeDDL.replaceAll("00367,01208,01200", "00037,00037,00037");
        code = putDB2UTIL(changeDDL, "LOD" + fileID + ".sql", code);
        promoteMember += "LOD1" + fileID + "     DB2UTIL    NMLMBR\r\n";
        promoteMember += "LOD" + fileID + "      DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getLODP(String dbname, String tsname, String tableName, String schema, String fileID,
                                       List<Map<String, Object>> columnList) {
        System.out.println("getLODP");
        String ddl = "";
        ddl += "LOAD DATA INDDN SYSREC LOG NO  DISCARDS 1 SORTDEVT\r\n";
        ddl += " SYSDA SORTNUM 10 REUSE";
        ddl += "  EBCDIC  CCSID(00037,00037,00037)\r\n";
        ddl += "  INTO TABLE \"" + schema + "\".\"" + tableName + "\" PART #PNO# REPLACE \r\n(\r\n";
        int currentLength = 1;
        int i = 1;
        for (Map<String, Object> entry : columnList) {
            String colName = entry.get("COLNAME").toString().trim();
            String colType = entry.get("TYPENAME").toString().trim();
            int colLength = Integer.parseInt(entry.get("LENGTH").toString().trim());
            String colNullable = entry.get("NULLS").toString().trim();

            if ("CHARACTER".equals(colType)) {
                colType = "CHAR(" + colLength + ")";
            } else if ("DATE".equals(colType)) {
                colType = "DATE EXTERNAL(10)";
                colLength = 10;
            } else if ("TIMESTAMP".equals(colType)) {
                colType = "TIMESTAMP EXTERNAL(26)";
                colLength = 26;
            } else if ("BIGINT".equals(colType)) {
                colLength = 8;
            } else if ("INTEGER".equals(colType)) {
                colLength = 4;
            } else if ("DECIMAL".equals(colType)) {
                colLength = (colLength + 1) / 2;
            } else if ("VARCHAR".equals(colType)) {
                colLength = colLength + 2;
            } else {
                colType = colType + "(" + colLength + ")";
            }

            ddl += colName + "  POSITION(" + currentLength + ")\r\n";
            ddl += "  " + colType;

            currentLength += colLength;

            if ("Y".equals(colNullable)) {
                ddl += "\r\n";
                ddl += "  NULLIF(" + currentLength + ")='?'";
                currentLength++;
            }

            if (i == columnList.size()) {
                ddl += "\r\n)\r\n";
            } else {
                ddl += ",\r\n";
            }

            i++;
        }

        code = putDB2UTIL(ddl, "LOD" + fileID + "P.sql", code);

        return code;
    }

    public Map<String, Object> getLODQ(String dbname, String tsname, String tableName, String schema, String fileID,
                                       List<Map<String, Object>> columnList, int partitionNum) {
        System.out.println("getLODQ");
        String ddl = "";
        ddl += "LOAD DATA LOG NO\r\n";
        ddl += " NOCOPYPEND\r\n";
        ddl += " SORTDEVT SYSDA SORTNUM 12 DISCARDS 1\r\n";
        for (int partIndc = 1; partIndc <= partitionNum; partIndc++) {
            String PN = getPartNum(partIndc);
            System.out.println(partIndc);

            ddl += "  INTO TABLE \"" + schema + "\".\"" + tableName + "\" PART #PNO" + PN + "#\r\n";
            ddl += "  INDDN SYS" + PN + " DISCARDDN DIS" + PN + " REPLACE\r\n(\r\n";
            int currentLength = 1;
            int i = 1;
            for (Map<String, Object> entry : columnList) {
                String colName = entry.get("COLNAME").toString().trim();
                String colType = entry.get("TYPENAME").toString().trim();
                int colLength = Integer.parseInt(entry.get("LENGTH").toString().trim());
                String colNullable = entry.get("NULLS").toString().trim();

                if ("CHARACTER".equals(colType)) {
                    colType = "CHAR(" + colLength + ")";
                } else if ("DATE".equals(colType)) {
                    colType = "DATE EXTERNAL(10)";
                    colLength = 10;
                } else if ("TIMESTAMP".equals(colType)) {
                    colType = "TIMESTAMP EXTERNAL(26)";
                    colLength = 26;
                } else if ("BIGINT".equals(colType)) {
                    colLength = 8;
                } else if ("INTEGER".equals(colType)) {
                    colLength = 4;
                } else if ("DECIMAL".equals(colType)) {
                    colLength = (colLength + 1) / 2;
                } else if ("VARCHAR".equals(colType)) {
                    colLength = colLength + 2;
                } else {
                    colType = colType + "(" + colLength + ")";
                }

                ddl += colName + "  POSITION(" + currentLength + ")\r\n";
                ddl += "  " + colType;

                currentLength += colLength;

                if ("Y".equals(colNullable)) {
                    ddl += "\r\n";
                    ddl += "  NULLIF(" + currentLength + ")='?'";
                    currentLength++;
                }

                if (i == columnList.size()) {
                    ddl += "\r\n)\r\n";
                } else {
                    ddl += ",\r\n";
                }

                i++;
            }
        }
        code = putDB2UTIL(ddl, "LOD" + fileID + "Q.sql", code);

        return code;
    }

    public Map<String, Object> getREO(String dbname, String tsname, String fileID) {
        String ddl = "";
        ddl += "REORG TABLESPACE (" + dbname + "." + tsname + ")\r\n";
        ddl += "  LOG NO\r\n";
        ddl += "  UNLDDN (SYSREC)\r\n";
        ddl += "  WORKDDN (SYSUT1)\r\n";
        ddl += "  SORTDEVT (SYSDA)\r\n";
        ddl += "  SORTNUM (12)\r\n";
        ddl += "  SORTKEYS\r\n";
        ddl += "  UNLOAD (CONTINUE)\r\n";
        code = putDB2UTIL(ddl, "REO" + fileID + ".sql", code);
        promoteMember += "REO" + fileID + "      DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getRUN(String dbname, String tsname, String fileID) {
        String ddl = "";
        ddl += "RUNSTATS TABLESPACE " + dbname + "." + tsname + "\r\n";
        ddl += "  TABLE ALL\r\n";
        ddl += "  INDEX ALL\r\n";
        ddl += "  SHRLEVEL REFERENCE\r\n";
        code = putDB2UTIL(ddl, "RUN" + fileID + ".sql", code);
        promoteMember += "RUN" + fileID + "      DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getRUNP(String dbname, String tsname, String tableName, String schema, String fileID) {
        System.out.println("getRUNP");
        String ddl = "";
        ddl += "RUNSTATS TABLESPACE " + dbname + "." + tsname + " PART #PNO#\r\n";
        ddl += "  TABLE ("+schema+"."+tableName+")\r\n";
        ddl += "  SHRLEVEL REFERENCE\r\n";
        code = putDB2UTIL(ddl, "RUN" + fileID + "P.sql", code);
        promoteMember += "RUN" + fileID + "P     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getRUNQ(String dbname, String tsname, String tableName, String schema, String fileID, int partitionNum) {
        System.out.println("getRUNQ");
        String ddl = "";
        for (int partIndc = 1; partIndc <= partitionNum; partIndc++) {
            String PN = getPartNum(partIndc);
            ddl += "RUNSTATS TABLESPACE " + dbname + "." + tsname + " PART #PNO" +PN+"#\r\n";
            ddl += "  TABLE (" + schema + "." + tableName + ")\r\n";
            ddl += "  SHRLEVEL REFERENCE\r\n";
        }
        code = putDB2UTIL(ddl, "RUN" + fileID + "Q.sql", code);
        promoteMember += "RUN" + fileID + "Q     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getSTRW(String dbname, String tsname, String fileID) {
        String ddl = "";
        ddl += "  -START DATABASE (" + dbname + ") SPACENAM (" + tsname + ") ACCESS (RW)\r\n";
        ddl += "  -DISPLAY DATABASE (" + dbname + ") SPACENAM (" + tsname + ")\r\n";
        ddl += "END\r\n";
        code = putDB2UTIL(ddl, "STRW" + fileID + ".sql", code);
        promoteMember += "STRW" + fileID + "     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getSTRWP(String dbname, String tsname, String fileID) {
        System.out.println("getSTRWP");
        String ddl = "";
        ddl += "  -START DATABASE (" + dbname + ") SPACENAM (" + tsname + ") PART(#PNO#) ACCESS (RW)\r\n";
        ddl += "END\r\n";
        code = putDB2UTIL(ddl, "STRW" + fileID + "P.sql", code);
        promoteMember += "STRW" + fileID + "P    DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getSTRWQ(String dbname, String tsname, String fileID, int partitionNum) {
        System.out.println("getSTRWQ");
        String ddl = "";
        for (int partIndc = 1; partIndc <= partitionNum; partIndc++) {
            String PN = getPartNum(partIndc);
            ddl += "  -START DATABASE (" + dbname + ") SPACENAM (" + tsname + ") PART(#PNO"+PN+"#) ACCESS (RW)\r\n";
        }
        ddl += "END\r\n";
        code = putDB2UTIL(ddl, "STRW" + fileID + "Q.sql", code);
        promoteMember += "STRW" + fileID + "Q    DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getSTUT(String dbname, String tsname, String fileID) {
        String ddl = "";
        ddl += "  -START DATABASE (" + dbname + ") SPACENAM (" + tsname + ") ACCESS (UT)\r\n";
        ddl += "  -DISPLAY DATABASE (" + dbname + ") SPACENAM (" + tsname + ")\r\n";
        ddl += "END\r\n";
        code = putDB2UTIL(ddl, "STUT" + fileID + ".sql", code);
        promoteMember += "STUT" + fileID + "     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getSTUTP(String dbname, String tsname, String fileID) {
        System.out.println("getSTUTP");
        String ddl = "";
        ddl += "  -START DATABASE (" + dbname + ") SPACENAM (" + tsname + ") PART(#PNO#) ACCESS (UT)\r\n";
        ddl += "END\r\n";
        code = putDB2UTIL(ddl, "STUT" + fileID + "P.sql", code);
        promoteMember += "STUT" + fileID + "P    DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getSTUTQ(String dbname, String tsname, String fileID, int partitionNum) {
        System.out.println("getSTUTQ");
        String ddl = "";
        for (int partIndc = 1; partIndc <= partitionNum; partIndc++) {
            String PN = getPartNum(partIndc);
            ddl += "  -START DATABASE (" + dbname + ") SPACENAM (" + tsname + ") PART(#PNO"+PN+"#) ACCESS (UT)\r\n";
        }
        ddl += "END\r\n";
        code = putDB2UTIL(ddl, "STUT" + fileID + "Q.sql", code);
        promoteMember += "STUT" + fileID + "Q    DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getTER(String fileID) {
        String ddl = "";
        ddl += "-DIS  UTIL(*)\r\n";
        ddl += "-TERM UTIL(FMSLT" + fileID + "#INST#)\r\n";
        ddl += "-DIS  UTIL(*)\r\n";
        code = putDB2UTIL(ddl, "TER" + fileID + ".sql", code);
        promoteMember += "TER" + fileID + "      DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getTERP(String fileID) {
        System.out.println("getTERP");
        String ddl = "";
        ddl += "-DIS  UTIL(*)\r\n";
        ddl += "-TERM UTIL(FMSLT" + fileID + "#JOBSEQ##INST#)\r\n";
        ddl += "-DIS  UTIL(*)\r\n";
        code = putDB2UTIL(ddl, "TER" + fileID + "P.sql", code);
        promoteMember += "TER" + fileID + "P     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getTERQ(String fileID) {
        System.out.println("getTERQ");
        String ddl = "";
        ddl += "-DIS  UTIL(*)\r\n";
        ddl += "-TERM UTIL(FMSLT" + fileID + "#JOBSEQ##INST#)\r\n";
        ddl += "-DIS  UTIL(*)\r\n";
        code = putDB2UTIL(ddl, "TER" + fileID + "Q.sql", code);
        promoteMember += "TER" + fileID + "Q     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getTERS(String fileID) {
        String ddl = "";
        ddl += "-DIS  UTIL(*)\r\n";
        ddl += "-TERM UTIL(FMSRS" + fileID + "#INST#)\r\n";
        ddl += "-DIS  UTIL(*)\r\n";
        code = putDB2UTIL(ddl, "TERS" + fileID + ".sql", code);
        promoteMember += "TERS" + fileID + "     DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getTERSP(String fileID) {
        System.out.println("getTERSP");
        String ddl = "";
        ddl += "-DIS  UTIL(*)\r\n";
        ddl += "-TERM UTIL(FMSRS" + fileID + "#JOBSEQ##INST#)\r\n";
        ddl += "-DIS  UTIL(*)\r\n";
        code = putDB2UTIL(ddl, "TERS" + fileID + "P.sql", code);
        promoteMember += "TERS" + fileID + "P    DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public Map<String, Object> getTERSQ(String fileID) {
        System.out.println("getTERSQ");
        String ddl = "";
        ddl += "-DIS  UTIL(*)\r\n";
        ddl += "-TERM UTIL(FMSRS" + fileID + "#JOBSEQ##INST#)\r\n";
        ddl += "-DIS  UTIL(*)\r\n";
        code = putDB2UTIL(ddl, "TERS" + fileID + "Q.sql", code);
        promoteMember += "TERS" + fileID + "Q    DB2UTIL    NMLMBR\r\n";
        return code;
    }

    public String getDMD(String tableName, String tableNameH, String tableNameC, String fileID, String fileIDH,
                         String fileIDC, String ddl) {

        if (!"".equals(tableNameH)) {
            ddl += "//*-------------------------------------------------------------------\r\n";
            ddl += "//* CREATE TABLE\r\n";
            ddl += "//* " + tableNameH + "       " + fileIDH + "\r\n";
            ddl += "//*-------------------------------------------------------------------\r\n";
            ddl += "//*\r\n";
            ddl += "//* " + tableNameH + "       " + fileIDH + "\r\n";
            ddl += "//*FMSD" + fileIDH + "  EXEC FMS$TEP3,MBR=FMSD" + fileIDH + "\r\n";
            ddl += "//FMST" + fileIDH + "  EXEC FMS$TEP3,MBR=FMST" + fileIDH + "\r\n";
            ddl += "//COM" + fileIDH + "  EXEC FMS$TEP3,MBR=COM" + fileIDH + "\r\n";
            ddl += "//FMSV" + fileIDH + "  EXEC FMS$TEP3,MBR=FMSV" + fileIDH + "\r\n";
            ddl += "//FMSW" + fileIDH + "  EXEC FMS$TEP3,MBR=FMSW" + fileIDH + "\r\n";
            ddl += "//GRANT" + fileIDH + "  EXEC FMS$TEP3,MBR=GRANT" + fileIDH + "\r\n";
            ddl += "//*\r\n";
        }
        ddl += "//*-------------------------------------------------------------------\r\n";
        ddl += "//* CREATE TABLE\r\n";
        ddl += "//* " + tableName + "       " + fileID + "\r\n";
        ddl += "//*-------------------------------------------------------------------\r\n";
        ddl += "//*\r\n";
        ddl += "//* " + tableName + "       " + fileID + "\r\n";
        ddl += "//*FMSD" + fileID + "  EXEC FMS$TEP3,MBR=FMSD" + fileID + "\r\n";
        ddl += "//FMST" + fileID + "  EXEC FMS$TEP3,MBR=FMST" + fileID + "\r\n";
        ddl += "//COM" + fileID + "  EXEC FMS$TEP3,MBR=COM" + fileID + "\r\n";
        ddl += "//FMSV" + fileID + "  EXEC FMS$TEP3,MBR=FMSV" + fileID + "\r\n";
        ddl += "//FMSW" + fileID + "  EXEC FMS$TEP3,MBR=FMSW" + fileID + "\r\n";
        ddl += "//GRANT" + fileID + "  EXEC FMS$TEP3,MBR=GRANT" + fileID + "\r\n";
        ddl += "//*\r\n";

        if (!"".equals(tableNameC)) {
            ddl += "//*-------------------------------------------------------------------\r\n";
            ddl += "//* CREATE TABLE\r\n";
            ddl += "//* " + tableNameC + "       " + fileIDC + "\r\n";
            ddl += "//*-------------------------------------------------------------------\r\n";
            ddl += "//*\r\n";
            ddl += "//* " + tableNameC + "       " + fileIDC + "\r\n";
            ddl += "//*FMSD" + fileIDC + "  EXEC FMS$TEP3,MBR=FMSD" + fileIDC + "\r\n";
            ddl += "//FMST" + fileIDC + "  EXEC FMS$TEP3,MBR=FMST" + fileIDC + "\r\n";
            ddl += "//COM" + fileIDC + "  EXEC FMS$TEP3,MBR=COM" + fileIDC + "\r\n";
            ddl += "//FMSV" + fileIDC + "  EXEC FMS$TEP3,MBR=FMSV" + fileIDC + "\r\n";
            ddl += "//FMSW" + fileIDC + "  EXEC FMS$TEP3,MBR=FMSW" + fileIDC + "\r\n";
            ddl += "//GRANT" + fileIDC + "  EXEC FMS$TEP3,MBR=GRANT" + fileIDC + "\r\n";
            ddl += "//*\r\n";
        }

        return ddl;
    }

    public String getDMDHeader(String jobName, String jobUser, String jobMessage, String jobTime, String jobLib,
                               String jobIncludesName) {
        String header = "";
        header += "//" + jobName + " JOB (@INF,FF31),'DB2CREATE',\r\n";
        header += "//      USER=" + jobUser + ",MSGCLASS=" + jobMessage + ",TIME=" + jobTime + ",REGION=0M\r\n";
        header += "//     JCLLIB ORDER=(TFMS.DD.PROCLIB,TFMS.F.DD.PROCLIB)\r\n";
        header += "//*\r\n";
        header += "//*\r\n";
        header += "//FMSSYM   INCLUDE MEMBER=" + jobIncludesName + "\r\n";
        header += "//FMSYY    INCLUDE MEMBER=FMS#YY               * ARCHIVE YEARS\r\n";
        header += "//         SET JOBNAME=" + jobName + "                * JOBNAME\r\n";
        header += "//*\r\n";

        return header;
    }


    public Map<String, Object> createDMD(String jobName, String ddl) {
        ddl += "//*-------------------------------------------------------------------\r\n";
        ddl += "//*     NOTIFY OPC \r\n";
        ddl += "//*-------------------------------------------------------------------\r\n";
        ddl += "//*\r\n";
        ddl += "//FMS$UTO EXEC FMS$UTO,OPCMSG='" + jobName + "',\r\n";
        ddl += "//         MAXCC=4\r\n";
        ddl += "//*\r\n";

        code = new HashMap<String, Object>();

        code.put("library", "TFMS.DD.INSTJCL");
        code.put("code", ddl);
        code.put("name", jobName + ".jcl");

        promoteMember += jobName + "    INSTJCL    NMLMBR\r\n";

        return code;
    }

    public void getPromoteHeader(String promoteName) {
        promoteMember += "* " + promoteName + " \r\n";
        promoteMember += "*\r\n";
        promoteMember += "* MEMBER * ** TYPE **  ** MEMBER TYPE **\r\n";
    }

    public Map<String, Object> getPromoteMmeber(String promoteName, String jobName) {
        promoteMember += "*ENVT* *GEO* *JOB TYPE* *JOB NAME*  *PRODONLY*\r\n";
        promoteMember += "%WW    WW    INSTJCL    " + jobName + "\r\n";
        promoteMember += "*INSTALLATION SEQUENCE\r\n";
        promoteMember += "%*1 AG AP JP EU WW\r\n";

        code = new HashMap<String, Object>();

        code.put("library", "TFMS.DD.PROMOTE");
        code.put("code", promoteMember);
        code.put("name", promoteName);

        return code;
    }

    public Map<String, Object> putDB2UTIL(String ddl, String fileName, Map<String, Object> code) {
        code = new HashMap<String, Object>();
        code.put("library", "TFMS.DD.DB2UTIL");
        code.put("code", ddl);
        code.put("name", fileName);
        return code;
    }

    public Map<String, Object> putPLINCL(String ddl, String fileName, Map<String, Object> code) {
        code = new HashMap<String, Object>();
        code.put("library", "TFMS.DD.PLINCL");
        code.put("code", ddl);
        code.put("name", fileName);
        return code;
    }

    public static String getCurrentYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date date = new Date();
        return sdf.format(date);
    }

    public static Integer getPartitionNum(String partitionKeys) {
        int partitionNum = 1;
        String[] partitionKey = partitionKeys.split(";");
        for (int i = 0; i < partitionKey.length; i++) {
            String[] partition = partitionKey[i].split(":");
            String partitionName = partition[0];
            String partitionDetail = partition[1];
            if (!partitionDetail.contains("(")) {
                if ("EFFC_DATE".equals(partitionName)) {
                    partitionNum = partitionNum * Integer.parseInt(partitionDetail) * 2;
                } else {
                    partitionNum = partitionNum * Integer.parseInt(partitionDetail);
                }
            }

        }
        return partitionNum;
    }

    public static String getPartNum(int PartitionNum) {
        if (PartitionNum < 10) {
            return "00" + PartitionNum;
        } else if (PartitionNum >= 10 && PartitionNum < 100) {
            return "0" + PartitionNum;
        } else {
            return "" + PartitionNum;
        }
    }

}
