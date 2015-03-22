import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Flegyas
 *
 */
public class Config {

	private Config() {
	}

	public Config(String domain, String id) {
		setDomain(domain);
		setID(id);
	}

	private String domain, id;

	public static Config getLocalInstance() {
		/*
		 * if (Constants.CONFIG_FILE.exists()) try { return new Gson().fromJson(
		 * new FileReader(Constants.CONFIG_FILE), Config.class); } catch
		 * (JsonSyntaxException | JsonIOException | FileNotFoundException exc) {
		 * exc.printStackTrace(); }
		 */

		return new Config();
	}

	public boolean writeToFile(File file) {
		/*
		 * try { FileWriter writer = new FileWriter(file); writer.write(new
		 * GsonBuilder().setPrettyPrinting().create() .toJson(this));
		 * writer.flush(); writer.close();
		 * 
		 * return true; } catch (IOException exc) { exc.printStackTrace();
		 * return false; }
		 */
		return false;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		if (domain == null || domain.isEmpty())
			throw new IllegalArgumentException("Invalid domain!");

		this.domain = domain;
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		if (id == null)
			throw new IllegalArgumentException("Invalid id!");

		this.id = id;
	}

	public static Config buildInstance(String domain, String string2)
			throws IOException {

		return null;
	}

	public void saveLocal() {
		// TODO Auto-generated method stub

	}
}
