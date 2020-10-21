/**
 *  Class: GitProperties
 *  Description: GIT Properties
 *  Created for zDevOps v1.0
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  1. 12/08/2018: Initial version. (V1.0)
 *  2. 01/03/2019: Add GIT URI. (V1.1)
 */
package com.sunpc.buildnow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author PengChengSun
 *
 */
@Component
public class GitProperties {
	
	@Value("${zdevops.git.path}")
	private String path;
	
	@Value("${zdevops.git.uri}")
	private String uri;

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}	

}
