package com.sunpc.buildnow.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sunpc.buildnow.service.IDDLService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ddl")
public class DDLController {
    @Autowired
    private IDDLService ddlService;

    @PostMapping("/getDDL")
    public Map<String, Object> getDDL(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> tables = (List<Map<String, Object>>) params.get("tables");
        Map<String, Object> properties = (Map<String, Object>) params.get("properties");
        return ddlService.getDDL(tables, properties);
    }
    
    @PostMapping("/getTableList")
    public Map<String, Object> getTableList(@RequestBody Map<String, Object> params) {
        String queryTable = (String)params.get("query");
        return ddlService.getTableList(queryTable);
    }

    @PostMapping("/uploadFile")
    public Map<String,Map<String, Object>> uploadPDF(@RequestParam("file") MultipartFile file) {
        return ddlService.readPDF(file);
    }

}

