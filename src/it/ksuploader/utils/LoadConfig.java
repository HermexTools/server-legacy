package it.ksuploader.utils;

import it.ksuploader.main.MainServer;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

public class LoadConfig extends Properties {

	private String folder;
	private String web_url;
	private String pass;
	private long folderSize;
	private long maxFileSize;
	private int port;

	public LoadConfig() {
		InputStream inputStream;
		try {

			if (!new File("server.properties").exists()) {
				this.store(new FileOutputStream("server.properties"), null);
			}

			inputStream = new FileInputStream("server.properties");
			this.load(inputStream);
			inputStream.close();

			boolean correct_config = false;

			// Server address
			if (this.getProperty("folder") == null || this.getProperty("folder").isEmpty()) {
				this.setProperty("folder", "files");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default folder");
			}
			this.folder = this.getProperty("folder");

			// Web url
			if (this.getProperty("web_url") == null || this.getProperty("web_url").isEmpty()) {
				this.setProperty("web_url", "http://domain.com/");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default web_url");
			}
			this.web_url = this.getProperty("web_url");

			// Password
			if (this.getProperty("password") == null || this.getProperty("password").isEmpty()) {
				this.setProperty("password", "pass");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default password");
			}
			this.pass = this.getProperty("password");

			// Port
			if (this.getProperty("port") == null || this.getProperty("port").isEmpty()) {
				this.setProperty("port", "4030");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default port");
			}
			this.port = Integer.parseInt(this.getProperty("port"));

			// Folder size
			if (this.getProperty("folder_size(MB)") == null || this.getProperty("folder_size(MB)").isEmpty()) {
				this.setProperty("folder_size(MB)", "4096");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default folder_size(MB)");
			}
			this.folderSize = Long.parseLong(this.getProperty("folder_size(MB)")) * 1048576;

			// File size
			if (this.getProperty("max_file_size(MB)") == null || this.getProperty("max_file_size(MB)").isEmpty()) {
				this.setProperty("max_file_size(MB)", "512");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default max_file_size(MB)");
			}
			this.maxFileSize = Long.parseLong(this.getProperty("max_file_size(MB)")) * 1048576;

			if (correct_config)
				this.store(new FileOutputStream("server.properties"), null);

		} catch (IOException ex) {
			ex.printStackTrace();
			MainServer.err(Arrays.toString(ex.getStackTrace()).replace(",", "\n"));
		}
	}

	public String getFolder() {
		return folder;
	}

	public String getPass() {
		return pass;
	}

	public String getWebUrl() {
		return web_url;
	}

	public int getPort() {
		return port;
	}

	public long getFolderSize() {
		return folderSize;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public boolean changeConfig(String folder, String web_url, String password, String port, String folderSize,
			String maxFileSize) {
		this.setProperty("folder", folder);
		this.setProperty("web_url", web_url);
		this.setProperty("password", password);
		this.setProperty("port", port);
		this.setProperty("folder_size(MB)", folderSize);
		this.setProperty("max_file_size(MB)", maxFileSize);

		try {
			this.store(new FileOutputStream("server.properties"), null);
		} catch (Exception e) {
			e.printStackTrace();
			MainServer.err(Arrays.toString(e.getStackTrace()).replace(",", "\n"));
		}
		return true;
	}

}
