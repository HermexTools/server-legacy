import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadConfig {

	private String domain;
	private String folder;
	private String pass;
	private int port;
	private int filePort;

	public LoadConfig() throws IOException {
		Properties prop = new Properties();

		if (!new File("server.properties").exists()) {
			prop.setProperty("domain", "localhost");
			prop.setProperty("folder", "files");
			prop.setProperty("password", "pass");
			prop.setProperty("port", "4030");
			prop.setProperty("fileport", "4031");
			prop.store(new FileOutputStream("server.properties"), null);
		}
		InputStream inputStream = new FileInputStream("server.properties");
		prop.load(inputStream);

		this.domain = prop.getProperty("domain");
		this.folder = prop.getProperty("folder");
		this.pass = prop.getProperty("password");
		this.port = Integer.parseInt(prop.getProperty("port"));
		this.filePort = Integer.parseInt(prop.getProperty("fileport"));
	}

	public String getFolder() {
		return folder;
	}

	public String getPass() {
		return pass;
	}

	public String getDomain() {
		return domain;
	}

	public int getPort() {
		return port;
	}

	public int getFilePort() {
		return filePort;
	}

	public boolean changeConfig(String domain, String folder, String password, String port, String filePort) {
		Properties prop = new Properties();
		prop.setProperty("domain", domain);
		prop.setProperty("folder", folder);
		prop.setProperty("password", password);
		prop.setProperty("port", port.toString());
		prop.setProperty("fileport", filePort.toString());

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
