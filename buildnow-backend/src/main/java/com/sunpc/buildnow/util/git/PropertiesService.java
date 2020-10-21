/**
 *  Class: PropertiesService
 *  Description: Properties Service
 *  Created for zDevOps v1.0
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  1. 12/16/2018: Initial version. (V1.0)
 *  2. 01/03/2019: Add GIT URI. (V1.1)
 */
package com.sunpc.buildnow.util.git;

import com.sunpc.buildnow.config.EnvProperties;
import com.sunpc.buildnow.config.GitProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author PengChengSun
 *
 */
@Service
public class PropertiesService {

	public void saveProperties(EnvProperties envProperties, GitProperties gitProperties) throws IOException {
		
		Map<String, String> map = new HashMap<String, String>();

		map.put("zdevops.env.name", envProperties.getName());
		map.put("zdevops.env.host", envProperties.getHost());
		map.put("zdevops.env.port", String.valueOf(envProperties.getPort()));
		map.put("zdevops.env.username", envProperties.getUsername());
		map.put("zdevops.env.password", DesEncrypter.encrypt(envProperties.getPassword()));
		map.put("zdevops.git.path", gitProperties.getPath());
		map.put("zdevops.git.uri", gitProperties.getUri());
		
		PropertiesUtil.save(map);
		
	}
	
}
