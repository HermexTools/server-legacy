import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class MainServer {

	private ServerSocketChannel serverSocketChannel;
	private static LoadConfig config;
	private static MainServer instance;

	public static MainServer getInstance() {
		return instance;
	}

	// SocketChannel socketChannel = null;
	// ServerSocketChannel serverSocketChannel = null;

	private MainServer() {

		try {
			config = new LoadConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Al momento non interessa
		// users = Users.getLocalInstance();

		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(config.getPort()));

			log("Listening images from: " + config.getPort());
			// socketChannel = createServerSocketChannel(config.getFilePort());
			// log("Listening files from: " + config.getFilePort());
			log("----------");
		} catch (IOException exc) {
			exc.printStackTrace();
			throw new IllegalArgumentException("Can't init serverSocket!");
		}
	}

	final static void log(String toPrint) {
		System.out.println(toPrint);
	}

	// Se ci sono args, il primo deve essere il serverDomain, il secondo la
	// cartella, il terzo l'id
	public static void main(String[] args) throws Exception {
		log("----------");
		log("Bootstrap...");

		// if (!Constants.OWP_DIR.exists())
		// Constants.OWP_DIR.mkdirs();

		config = new LoadConfig();

		if (args.length != 0) {
			if (args.length == 5) {
				config.changeConfig(args[0], args[1], args[2], args[3], args[4]);
			} else
				throw new IllegalArgumentException("Correct args are: serverDomain, folder, pass, port, filePort");
		} else {
			if (config.getDomain().equals("") || config.getFolder().equals("") || config.getPass().equals("")
					|| Integer.toString(config.getPort()).equals("")
					|| Integer.toString(config.getFilePort()).equals(""))
				throw new Exception("Error reading config properties.");
			else {
				log("Domain: " + config.getDomain());
				log("Folder: " + config.getFolder());
				log("Pass: " + config.getPass());
				log("Port: " + config.getPort());
				log("FilePort: " + config.getFilePort());
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
				SocketChannel clientSocket = serverSocketChannel.accept();
				RequestHandler requestHandler = new RequestHandler(clientSocket);
				new Thread(requestHandler).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void stop() {
		log("Server stopped!");
	}

}
