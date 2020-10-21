/**
 *  Class: FtpService
 *  Description: FTP Service
 *  Created for zDevOps v1.0
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  1. 12/08/2018: Initial version. (V1.0)
 */
package com.sunpc.buildnow.util.git;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.SocketException;

/**
 * @author PengChengSun
 *
 */
@Service
public class FtpService {

	private FTPSClient ftpClient = null;
	
	public void connect(String hostname, int port, String username, String password) throws SocketException, IOException {
		// connect to FTP server
		ftpClient = new FTPSClient();
		System.out.println(hostname+port+username+password);
		ftpClient.connect(hostname, port);
		if (!ftpClient.login(username, password)) {
			throw new IOException("Invalid username or password");
		}
		ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
		ftpClient.setControlEncoding("utf-8");
	}
	
	public void disconnect() throws IOException {
		ftpClient.disconnect();
	}
	
	public void download(String remoteName, String localName) throws IOException {
		// retrieve the file
		OutputStream output;
        output = new FileOutputStream(localName);
		if (!ftpClient.retrieveFile(remoteName, output)) {
			throw new IOException("Unable to download " + remoteName);
		}
		output.close();
	}

	public void upload(String remoteName, String localName) throws IOException {
		// retrieve the file
		InputStream input;
		input = new FileInputStream(localName);
		if(!ftpClient.storeFile(remoteName, input)) {
			throw new IOException("Unable to upload " + remoteName);
		}
		input.close();
	}

}
