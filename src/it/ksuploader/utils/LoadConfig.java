package it.ksuploader.utils;

import it.ksuploader.main.MainServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class LoadConfig {

	private String folder;
	private String web_url;
	private String pass;
	private long folderSize;
	private long maxFileSize;
	private int port;

	public LoadConfig() {
		InputStream inputStream = null;
		try {
			Properties prop = new Properties();

			if (!new File("server.properties").exists()) {
				prop.store(new FileOutputStream("server.properties"), null);
			}

			inputStream = new FileInputStream("server.properties");
			prop.load(inputStream);
			inputStream.close();

			boolean correct_config = false;

			// Server address
			if ((this.folder = prop.getProperty("folder")) == null || prop.getProperty("folder").isEmpty()) {
				prop.setProperty("folder", "files");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default folder");
			}

			if ((this.web_url = prop.getProperty("web_url")) == null || prop.getProperty("web_url").isEmpty()) {
				prop.setProperty("web_url", "http://domain.com/");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default web_url");
			}

			if ((this.pass = prop.getProperty("password")) == null || prop.getProperty("password").isEmpty()) {
				prop.setProperty("password", "pass");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default password");
			}

			if (prop.getProperty("port") == null || prop.getProperty("port").isEmpty()) {
				prop.setProperty("port", "4030");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default port");
			} else {
				this.port = Integer.parseInt(prop.getProperty("port"));
			}

			if (prop.getProperty("folder_size(MB)") == null || prop.getProperty("folder_size(MB)").isEmpty()) {
				prop.setProperty("folder_size(MB)", "4096");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default folder_size(MB)");
			} else {
				this.folderSize = Long.parseLong(prop.getProperty("folder_size(MB)")) * 1048576;
			}

			if (prop.getProperty("max_file_size(MB)") == null || prop.getProperty("max_file_size(MB)").isEmpty()) {
				prop.setProperty("max_file_size(MB)", "512");
				correct_config = true;
				System.out.println("[LoadConfig] Setting default max_file_size(MB)");
			} else {
				this.maxFileSize = Long.parseLong(prop.getProperty("max_file_size(MB)")) * 1048576;
			}

			if (correct_config)
				prop.store(new FileOutputStream("server.properties"), null);

		} catch (IOException ex) {
			ex.printStackTrace();
			MainServer.err(Arrays.toString(ex.getStackTrace()).replace(",", "\n"));
		} finally {
			try {
				inputStream.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				MainServer.err(Arrays.toString(ex.getStackTrace()).replace(",", "\n"));
			}
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
		Properties prop = new Properties();
		prop.setProperty("folder", folder);
		prop.setProperty("web_url", web_url);
		prop.setProperty("password", password);
		prop.setProperty("port", port);
		prop.setProperty("folder_size(MB)", folderSize);
		prop.setProperty("max_file_size(MB)", maxFileSize);

		try {
			prop.store(new FileOutputStream("server.properties"), null);
		} catch (Exception e) {
			e.printStackTrace();
			MainServer.err(Arrays.toString(e.getStackTrace()).replace(",", "\n"));
		}
		return true;
	}

}
