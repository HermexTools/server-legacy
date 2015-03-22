import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * 
 * @author Flegyas
 *
 */
public class MainServer {

	private final ServerSocket serverSocket;

	private static LoadConfig config;

	// private final Users users;

	private static MainServer instance;

	public static MainServer getInstance() {
		return instance;
	}

	private MainServer() {

		try {
			config = new LoadConfig();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Al momento non interessa
		// users = Users.getLocalInstance();

		try {
			serverSocket = new ServerSocket(Constants.SERVER_PORT);
			log("Sono in ascolto dalla: " + Constants.SERVER_PORT);
			log("----------");
		} catch (IOException exc) {
			exc.printStackTrace();
			throw new IllegalArgumentException("Can't init serverSocket!");
		}
	}

	final static void log(String toPrint) {
		System.out.println(toPrint);
	}

	// Se ci sono args, il primo deve essere il serverDomain, il secondo il
	// serverID.
	public static void main(String[] args) throws Exception {
		log("----------");
		log("Bootstrap...");

		// if (!Constants.OWP_DIR.exists())
		// Constants.OWP_DIR.mkdirs();

		config = new LoadConfig();

		if (args.length != 0) {
			if (args.length == 3) {
				// Config.buildInstance(args[0], args[1]).saveLocal();
				config.changeConfig(args[0], args[1], args[2]);
			} else
				throw new IllegalArgumentException(
						"Correct args are: serverDomain, folder, id");
		} else {
			if (config.getDomain().equals("") || config.getFolder().equals("")
					|| config.getFolder().equals(""))
				throw new Exception("Error reading config properties.");
			else {
				log("Domain: " + config.getDomain());
				log("Folder: " + config.getFolder());
				log("Pass: " + config.getPass());

			}
		}

		// Controllo se esiste la directory dei file
		if (new File("./" + config.getFolder()).exists() == false)
			new File("./" + config.getFolder()).mkdir();

		new MainServer().start();

	}

	private void start() {
		while (true)
			try {
				new RequestHandler(serverSocket.accept()).run();
			} catch (IOException exc) {
				exc.printStackTrace();
			}
	}

	public void stop() {
		log("Saving...");
		// Al momento non interessa
		// users.save();
		// config.saveLocal();

		log("Server stopped!");
	}
}
