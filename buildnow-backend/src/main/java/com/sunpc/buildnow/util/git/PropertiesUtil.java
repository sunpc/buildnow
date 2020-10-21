/**
 *  Class: PropertiesUtil
 *  Created for zDevOps v1.0
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  1. 12/16/2018: Initial version. (V1.0)
 */
package com.sunpc.buildnow.util.git;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * @author PengChengSun
 *
 */
public class PropertiesUtil {

	public static void save(Map<String, String> map) throws IOException {
		Properties prop = new Properties(); 
		FileOutputStream oFile = new FileOutputStream("application.properties");
		for (Entry<String, String> entry : map.entrySet()) {
			prop.setProperty(entry.getKey(), entry.getValue());
		}
		prop.store(oFile, "zDevOps application properties");
		oFile.close();
	}
	
}
