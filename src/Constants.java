import java.io.File;

/**
 * 
 * @author Flegyas
 *
 */
public class Constants {

	/**
	 * Cartella home di default dell'utente, specifica dell'OS.
	 */
	public static final File USER_HOME = new File(
			System.getProperty("user.home"));

	public static final File OWP_DIR = new File(USER_HOME, ".ownPuush");

	public static final File CONFIG_FILE = new File(OWP_DIR, "config.owp");

	// public static final File USERS_FILE = new File(OWP_DIR, "users.owp");

	public static final int SERVER_PORT = 4030;
}
