package it.ksuploader.main;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Listener extends Thread {
	private boolean listen;
	private Logger logger = KSUploaderServer.logger;
	private ServerSocketChannel serverSocketChannel;

	public Listener() throws IOException {

		if (!new File("." + File.separator + KSUploaderServer.config.getFolder()).exists()) {
			new File("./" + File.separator + KSUploaderServer.config.getFolder()).mkdir();
		}
		this.listen = true;
		this.buildSocket();
	}

	private void buildSocket() throws IOException {
		try {
			this.serverSocketChannel = ServerSocketChannel.open();
			this.serverSocketChannel.socket().bind(new InetSocketAddress(KSUploaderServer.config.getPort()));

			this.logger.log(Level.FINE, "Listening on port " + KSUploaderServer.config.getPort() + ".");

		} catch (IOException exc) {
			throw new IOException("Can't init the listening socket!");
		}
	}

	public void setListen(boolean listen) {
		this.listen = listen;
	}

	public void run() {
		while (this.listen) {
			try {
				new RequestHandler(this.serverSocketChannel.accept()).start();
				System.out.println("--------------------------------------------------------------------------------");
			} catch (IOException e) {
				this.logger.log(Level.WARNING, "Error during handling request", e);
			}
		}
	}
}
