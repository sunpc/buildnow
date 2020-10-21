/**
 *  Class: EnvProperties
 *  Description: Env Properties
 *  Created for zDevOps v1.0
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  1. 12/08/2018: Initial version. (V1.0)
 */
package com.sunpc.buildnow.config;

import com.sunpc.buildnow.util.git.DesEncrypter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author PengChengSun
 *
 */
@Component
public class EnvProperties {
	
	@Value("${zdevops.env.name}")
	private String name;
	
	@Value("${zdevops.env.host}")
	private String host;
	
	@Value("${zdevops.env.port}")
	private int port;
	
	@Value("${zdevops.env.username}")
	private String username;
	
	@Value("${zdevops.env.password}")
	private String password;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return DesEncrypter.decrypt(password);
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = DesEncrypter.encrypt(password);
	}

}
