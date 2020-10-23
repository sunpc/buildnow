package com.sunpc.buildnow.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CodeUtil {
	
	private String inFilePath = "C:\\temp\\ibuild\\";
	private String outFilePath = "C:\\temp\\";

	public boolean saveCode(String uuid, List<Map<String, Object>> codeList) {
		// empty the target folder if exists
		String targetFilePath = inFilePath + uuid;
		File fileTarget = new File(targetFilePath);
		if (fileTarget.isDirectory()) {
			String[] tempList = fileTarget.list();
			for (String tempFolder : tempList) {
				File tempF = new File(targetFilePath + File.separator + tempFolder);
				String[] tempFList = tempF.list();
				for (String tempFile : tempFList) {
					File temp = new File(tempF.getPath() + File.separator + tempFile);
					if (temp.isFile()) {
						temp.delete();
					}
				}
			}
		}
		// save the generated code
		for (Map<String, Object> codeObj : codeList) {
			String codeFilePath = inFilePath + uuid + File.separator + codeObj.get("library") + File.separator
					+ codeObj.get("name");
			File file = new File(codeFilePath);
			File fileParent = file.getParentFile();
			if (!fileParent.exists()) {
				// mkdirs if not exists
				fileParent.mkdirs();
			}
			// write files
			try {
				FileWriter fw = new FileWriter(codeFilePath);
				fw.write((String) codeObj.get("code"));
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	public void downloadCodePack(String uuid, String name, HttpServletRequest request, HttpServletResponse response) {
		String jobFilePath = inFilePath + uuid + File.separator;
		List<File> files = new ArrayList<File>();
		File allFile = new File(jobFilePath);
		if (allFile.exists()) {
			File[] fileArr = allFile.listFiles();
			for (File file2 : fileArr) {
				files.add(file2);
			}
		}
		
		// get system time
		Date day = new Date();    
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String currTime = df.format(day);

		//String fileName = UUID.randomUUID().toString() + ".zip";
		String fileName = name + "_" + currTime + ".zip";
		
		// 在服务器端创建打包下载的临时文件
		//String outFilePath = request.getSession().getServletContext().getRealPath("/") + "upload/";

		File fileZip = new File(outFilePath + fileName);
		// toClient.setEncoding("gbk");
		try {
			// 文件输出流
			FileOutputStream outStream = new FileOutputStream(fileZip);
			// 压缩流
			ZipOutputStream toClient = new ZipOutputStream(outStream);
			zipFile(jobFilePath, files, toClient);
			toClient.close();
			outStream.close();
			downloadFile(fileZip, response, true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}		
	}

	public void zipFile(String jobFilePath, List<File> folders, ZipOutputStream outputStream) throws IOException, ServletException {
		try {
			// 压缩列表中的文件
			for (int i = 0; i < folders.size(); i++) {
				File folder = (File) folders.get(i);
				String folderName = folder.getName() + File.separator;
				File allFile = new File(jobFilePath + folderName);
				if (allFile.exists()) {
					File[] fileArr = allFile.listFiles();
					for (File file2 : fileArr) {
						zipFile(folderName, file2, outputStream);
					}
				}
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public void zipFile(String folderName, File inputFile, ZipOutputStream outputstream) throws IOException, ServletException {
		try {
			if (inputFile.exists()) {
				if (inputFile.isFile()) {
					FileInputStream inStream = new FileInputStream(inputFile);
					BufferedInputStream bInStream = new BufferedInputStream(inStream);
					ZipEntry entry = new ZipEntry(folderName + inputFile.getName());
					outputstream.putNextEntry(entry);

					final int MAX_BYTE = 10 * 1024 * 1024; // 最大的流为10M
					long streamTotal = 0; // 接受流的容量
					int streamNum = 0; // 流需要分开的数量
					int leaveByte = 0; // 文件剩下的字符数
					byte[] inOutbyte; // byte数组接受文件的数据

					streamTotal = bInStream.available(); // 通过available方法取得流的最大字符数
					streamNum = (int) Math.floor(streamTotal / MAX_BYTE); // 取得流文件需要分开的数量
					leaveByte = (int) streamTotal % MAX_BYTE; // 分开文件之后,剩余的数量

					if (streamNum > 0) {
						for (int j = 0; j < streamNum; ++j) {
							inOutbyte = new byte[MAX_BYTE];
							// 读入流,保存在byte数组
							bInStream.read(inOutbyte, 0, MAX_BYTE);
							outputstream.write(inOutbyte, 0, MAX_BYTE); // 写出流
						}
					}
					// 写出剩下的流数据
					inOutbyte = new byte[leaveByte];
					bInStream.read(inOutbyte, 0, leaveByte);
					outputstream.write(inOutbyte);
					outputstream.closeEntry(); // Closes the current ZIP entry
					// and positions the stream for
					// writing the next entry
					bInStream.close(); // 关闭
					inStream.close();
				}
			} else {
				throw new ServletException("文件不存在！");
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public void downloadFile(File file, HttpServletResponse response, boolean isDelete) {
		try {
			// 以流的形式下载文件。
			BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file.getPath()));
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			fis.close();
			// 清空response
			response.reset();
			OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition",
					"attachment;filename=" + new String(file.getName().getBytes("UTF-8"), "ISO-8859-1"));
			toClient.write(buffer);
			toClient.flush();
			toClient.close();
			if (isDelete) {
				file.delete(); // 是否将生成的服务器端文件删除
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
}
