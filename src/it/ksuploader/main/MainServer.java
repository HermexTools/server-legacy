package it.ksuploader.main;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class MainServer {

	private ServerSocketChannel serverSocketChannel;
	public static LoadConfig config = new LoadConfig();
	private static MainServer instance;

	public static MainServer getInstance() {
		return instance;
	}

	// SocketChannel socketChannel = null;
	// ServerSocketChannel serverSocketChannel = null;

	private MainServer() {

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

	public static void main(String[] args) throws Exception {
		log("----------");
		log("Bootstrap...");

		// if (!Constants.OWP_DIR.exists())
		// Constants.OWP_DIR.mkdirs();


		if (args.length != 0) {
			if (args.length == 6) {
				config.changeConfig(args[0], args[1], args[2], args[3], args[4], args[5]);
                log("Restart for load the new config...");
			} else
				throw new IllegalArgumentException("Correct args are: folder, web_url, pass, port, folder size, max file size");
		} else {
			if (config.getFolder().equals("") || config.getPass().equals("") || Integer.toString(config.getPort()).equals("") || config.getWebUrl().equals(""))
				throw new Exception("Error reading config properties.");
			else {
				log("Folder: " + config.getFolder());
                log("Folder Max size: " + config.getFolderSize());
                log("Max file size: "+config.getMaxFileSize());
				log("WebUrl: " + config.getWebUrl());
				log("Pass: " + config.getPass());
				log("Port: " + config.getPort()); 
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
