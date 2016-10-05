package it.hermex.main.uploaders.socket;

import it.hermex.main.HermexServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;

public class SocketListener extends Thread {
	
	private boolean listening;
	private Logger logger = Logger.getLogger(this.getClass());
	private ServerSocketChannel serverSocketChannel;
	
	public SocketListener() {
	}
	
	private void buildSocket() throws IOException {
		try {
			this.serverSocketChannel = ServerSocketChannel.open();
			this.serverSocketChannel.socket().bind(new InetSocketAddress(HermexServer.config.getPort()));
			
			this.logger.log(Level.INFO, "Socket listening on port " + HermexServer.config.getPort() + ".");
			
		} catch (IOException exc) {
			throw new IOException("Can't init the listening socket!");
		}
	}
	
	public void stopListen() {
		try {
			this.serverSocketChannel.close();
		} catch (IOException e) {
			this.logger.log(Level.WARN, "Cannot closing socket.", e);
		}
		this.interrupt();
		this.listening = false;
	}
	
	public void startListen() {
		try {
			this.buildSocket();
		} catch (IOException e) {
			this.logger.log(Level.ERROR, "Cannot build socket.", e);
			return;
		}
		this.start();
		this.listening = true;
	}
	
	public boolean isListening() {
		return this.listening;
	}
	
	public void run() {
		while (true) {
			try {
				new SocketRequestHandler(this.serverSocketChannel.accept()).start();
			} catch (ClosedChannelException e) {
				this.logger.log(Level.WARN, "Stopping listening...");
				break;
			} catch (IOException e) {
				this.logger.log(Level.ERROR, "Error during handling request", e);
			}
		}
	}
}
