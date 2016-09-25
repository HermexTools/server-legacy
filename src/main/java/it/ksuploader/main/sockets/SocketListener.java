package it.ksuploader.main.sockets;

import it.ksuploader.main.KSUploaderServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class SocketListener extends Thread {
	private boolean listen;
	private Logger logger = Logger.getLogger(this.getClass());
	private ServerSocketChannel serverSocketChannel;

	public SocketListener() throws IOException {

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

			this.logger.log(Level.INFO, "Listening on port " + KSUploaderServer.config.getPort() + ".");

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
			} catch (IOException e) {
				this.logger.log(Level.ERROR, "Error during handling request", e);
			}
		}
	}
}
