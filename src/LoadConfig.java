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

	public LoadConfig() throws IOException {
		Properties prop = new Properties();

		if (!new File("config.properties").exists()) {
			prop.setProperty("domain", "localhost");
			prop.setProperty("folder", "files");
			prop.setProperty("password", "pass");
			prop.store(new FileOutputStream("config.properties"), null);
		}
		InputStream inputStream = new FileInputStream("config.properties");
		prop.load(inputStream);

		this.domain = prop.getProperty("domain");
		this.folder = prop.getProperty("folder");
		this.pass = prop.getProperty("password");
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

	public boolean changeConfig(String domain, String folder, String password) {
		Properties prop = new Properties();
		prop.setProperty("domain", domain);
		prop.setProperty("folder", folder);
		prop.setProperty("password", password);

		try {
			if (!new File("config.properties").exists()) {
				prop.store(new FileOutputStream("config.properties"), null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}
