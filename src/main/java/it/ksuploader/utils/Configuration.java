package it.ksuploader.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class Configuration extends Properties {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	private String folder;
	private String web_url;
	private String pass;
	private long folderSize;
	private long maxFileSize;
	private int port;
	private int web_port;

	public Configuration() {
		InputStream inputStream;
		try {

			if (!new File("server.properties").exists()) {
				this.store(new FileOutputStream("server.properties"), null);
			}

			inputStream = new FileInputStream("server.properties");
			this.load(inputStream);
			inputStream.close();

			boolean correct_config = false;

			// old address
			if (this.getProperty("folder") == null || this.getProperty("folder").isEmpty()) {
				this.setProperty("folder", "files");
				correct_config = true;
				logger.log(Level.INFO, "[Configuration] Setting default folder");
			}
			this.folder = this.getProperty("folder");

			// Web url
			if (this.getProperty("web_url") == null || this.getProperty("web_url").isEmpty()) {
				this.setProperty("web_url", "http://domain.com/");
				correct_config = true;
				logger.log(Level.INFO, "[Configuration] Setting default web_url");
			}
			this.web_url = this.getProperty("web_url");

			// Password
			if (this.getProperty("password") == null || this.getProperty("password").isEmpty()) {
				this.setProperty("password", "pass");
				correct_config = true;
				logger.log(Level.INFO, "[Configuration] Setting default password");
			}
			this.pass = this.getProperty("password");

			// Port
			if (this.getProperty("port") == null || this.getProperty("port").isEmpty()) {
				this.setProperty("port", "4030");
				correct_config = true;
				logger.log(Level.INFO, "[Configuration] Setting default port");
			}
			this.port = Integer.parseInt(this.getProperty("port"));
			
			// Webserver Port
			if (this.getProperty("web_port") == null || this.getProperty("web_port").isEmpty()) {
				this.setProperty("web_port", "4040");
				correct_config = true;
				logger.log(Level.INFO, "[Configuration] Setting default webserver port");
			}
			this.web_port = Integer.parseInt(this.getProperty("web_port"));

			// Folder size
			if (this.getProperty("folder_size(MB)") == null || this.getProperty("folder_size(MB)").isEmpty()) {
				this.setProperty("folder_size(MB)", "4096");
				correct_config = true;
				logger.log(Level.INFO, "Setting default folder_size(MB)");
			}
			this.folderSize = Long.parseLong(this.getProperty("folder_size(MB)")) * 1048576;

			// File size
			if (this.getProperty("max_file_size(MB)") == null || this.getProperty("max_file_size(MB)").isEmpty()) {
				this.setProperty("max_file_size(MB)", "512");
				correct_config = true;
				logger.log(Level.INFO, "[Configuration] Setting default max_file_size(MB)");
			}
			this.maxFileSize = Long.parseLong(this.getProperty("max_file_size(MB)")) * 1048576;

			if (correct_config)
				this.store(new FileOutputStream("server.properties"), null);

		} catch (IOException ex) {
			this.logger.log(Level.ERROR, "Error parsing config", ex);
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
	
	public int getWebPort() {
		return web_port;
	}
	
	public long getFolderSize() {
		return folderSize;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	private boolean save() {
		try {
			this.store(new FileOutputStream("server.properties"), null);
		} catch (Exception e) {
			logger.log(Level.ERROR, "Can't save config", e);
			return false;
		}
		logger.log(Level.INFO, "Configuration saved");
		return true;
	}

	public boolean setPort(String port) {
		this.port = Integer.parseInt(this.getProperty("port"));
		this.setProperty("port", port);
		return this.save();
	}

	public boolean setFolder(String folder) {
		this.folder = folder;
		this.setProperty("folder", folder);
		return this.save();
	}

	public boolean setWeb_url(String web_url) {
		this.web_url = web_url;
		this.setProperty("web_url", web_url);
		return this.save();
	}

	public boolean setPass(String pass) {
		this.pass = pass;
		this.setProperty("password", pass);
		return this.save();
	}

	public boolean setFolderSize(String folderSize) {
		this.folderSize = Long.parseLong(this.getProperty("folder_size(MB)")) * 1048576;
		this.setProperty("folder_size(MB)", folderSize);
		return this.save();
	}

	public boolean setMaxFileSize(String maxFileSize) {
		this.maxFileSize = Long.parseLong(this.getProperty("max_file_size(MB)")) * 1048576;
		this.setProperty("max_file_size(MB)", maxFileSize);
		return this.save();
	}


}
