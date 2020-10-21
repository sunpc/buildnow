/**
 * 
 */
package com.sunpc.buildnow.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sunpc.buildnow.util.io.CodeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author PengChengSun
 *
 */
@Controller
@RequestMapping("/api/zip")
public class ZipController {

	@RequestMapping("/download")
	public Map<String, Object> downloadFiles(@RequestParam("uuid") String uuid, @RequestParam("name") String name, HttpServletRequest request, HttpServletResponse response) {

		CodeUtil codeUtil = new CodeUtil();
		codeUtil.downloadCodePack(uuid, name, request, response);
		
		return null;
	}
}
