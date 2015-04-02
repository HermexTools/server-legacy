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
    private long folderSize;
    private long maxFileSize;
	private int port;

    public LoadConfig(){
        InputStream inputStream = null;
        try {
            Properties prop = new Properties();
            
            if (!new File("server.properties").exists()) {
                prop.setProperty("folder", "files");
                prop.setProperty("web_url", "http://domain.com/");
                prop.setProperty("password", "pass");
                prop.setProperty("port", "4030");
                prop.setProperty("folder_size(MB)", "4096");
                prop.setProperty("max_file_size(MB)", "512");
                prop.store(new FileOutputStream("server.properties"), null);
            }   
            
            inputStream = new FileInputStream("server.properties");
            prop.load(inputStream);
            this.folder = prop.getProperty("folder");
            this.web_url = prop.getProperty("web_url");
            this.pass = prop.getProperty("password");
            this.port = Integer.parseInt(prop.getProperty("port"));
            this.folderSize = Long.parseLong(prop.getProperty("folder_size(MB)"))*1048576;
            this.maxFileSize = Long.parseLong(prop.getProperty("max_file_size(MB)"))*1048576;
            
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
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
    
    

	public boolean changeConfig(String folder, String web_url, String password, String port, String folderSize, String maxFileSize) {
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
        }
		return true;
	}

}
