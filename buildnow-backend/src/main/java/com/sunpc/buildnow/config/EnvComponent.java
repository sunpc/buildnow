/**
 *  Class: EnvComponent
 *  Description: Env Component
 *  Created for zDevOps v1.0
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  1. 12/08/2018: Initial version. (V1.0)
 */
package com.sunpc.buildnow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author PengChengSun
 *
 */
@Component
@ConfigurationProperties(prefix = "zdevops.env")
@Validated
public class EnvComponent {
	
	@NotEmpty
	private String name;
	
	@NotEmpty
	private String host;
	
	@NotNull
	private int port;
	
	@NotEmpty
	private String username;
	
	@NotEmpty
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
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	

}
