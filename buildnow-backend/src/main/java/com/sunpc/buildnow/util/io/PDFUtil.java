package com.sunpc.buildnow.util.io;


import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class PDFUtil {

    public ArrayList<String> getAllTable(File file) {
        ArrayList<String> resultList = new ArrayList<String>();
        try (PDDocument document = PDDocument.load(file)) {

            document.getClass();
            if (!document.isEncrypted()) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);
                PDFTextStripper tStripper = new PDFTextStripper();

                String tableName = "";
                String pdfFileInText = tStripper.getText(document);
                String[] lines = pdfFileInText.split("\\r?\\n");
                for (String line : lines) {
                    if (line.contains("FMST_")) {
                            String[] tableStrings = line.split("\\s+");
                            for (int i = 0; i < tableStrings.length; i++) {
                                if (tableStrings[i].contains("FMST_")) {
                                    //start with FMST
                                    int tableIndc = tableStrings[i].indexOf("FMST_");
                                    tableName = tableStrings[i].substring(tableIndc);

                                    resultList.add(tableName);
                                }
                            }
                    }
                }

                HashSet<String> set = new HashSet(resultList);

                resultList = new ArrayList(set);

                Collections.sort(resultList);
            }
        } catch (InvalidPasswordException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public ArrayList<String> getNewTable(ArrayList<String> inputTableList,List<Map<String, Object>> codegenTableList,List<Map<String, Object>> mvsTableList) {
        ArrayList<String> newTableList = new ArrayList<String>();

        boolean codegenFoundFlag;
        boolean mvsFoundFlag;

        // loop input List
        for(int inputIndc = 0; inputIndc < inputTableList.size(); inputIndc ++){
            codegenFoundFlag = false;
            mvsFoundFlag = false;
            // loop codegen table list
            for(int codegenIndc = 0; codegenIndc < codegenTableList.size(); codegenIndc ++){
                // if the table exists in codegen
                if(inputTableList.get(inputIndc).trim().equals(codegenTableList.get(codegenIndc).get("TABNAME").toString().trim())){
                    codegenFoundFlag = true;
                    // loop mvs table list
                    for(int mvsIndc = 0; mvsIndc < mvsTableList.size(); mvsIndc ++){
                        // if the table exists in mvs
                        if(inputTableList.get(inputIndc).trim().equals(mvsTableList.get(mvsIndc).get("TABNAME").toString().trim())){
                            mvsFoundFlag = true;
                            break;
                        }
                    }

                    break;
                }

            }

            if(codegenFoundFlag && !mvsFoundFlag){
                newTableList.add(inputTableList.get(inputIndc).trim());
            }


        }



        return newTableList;
    }

}
