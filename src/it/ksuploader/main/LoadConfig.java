package it.ksuploader.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadConfig {

	private String folder;
	private String web_url;
	private String pass;
	private int port;

	public LoadConfig() throws IOException {
		Properties prop = new Properties();

		if (!new File("server.properties").exists()) {
			prop.setProperty("folder", "files");
			prop.setProperty("web_url", "http://domain.com/noFinalSlash");
			prop.setProperty("password", "pass");
			prop.setProperty("port", "4030");
			prop.store(new FileOutputStream("server.properties"), null);
		}
		InputStream inputStream = new FileInputStream("server.properties");
		prop.load(inputStream);

		this.folder = prop.getProperty("folder");
		this.web_url = prop.getProperty("web_url");
		this.pass = prop.getProperty("password");
		this.port = Integer.parseInt(prop.getProperty("port"));
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

	public boolean changeConfig(String folder, String web_url, String password, String port) {
		Properties prop = new Properties();
		prop.setProperty("folder", folder);
		prop.setProperty("web_url", web_url);
		prop.setProperty("password", password);
		prop.setProperty("port", port.toString());

		try {
			if (!new File("server.properties").exists()) {
				prop.store(new FileOutputStream("server.properties"), null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}
