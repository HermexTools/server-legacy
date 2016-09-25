package it.ksuploader.main.sockets;

import it.ksuploader.main.KSUploaderServer;
import it.ksuploader.utils.FileHelper;
import it.ksuploader.utils.Messages;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import static it.ksuploader.utils.FileHelper.acceptedTypes;

class SocketRequestHandler extends Thread {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	private SocketChannel socketChannel;
	private DataInputStream input;
	private DataOutputStream output;
	
	
	SocketRequestHandler(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		this.logger.log(Level.INFO, "SocketRequestHandler initialized");
	}
	
	@Override
	public void run() {
		
		try {
			this.logger.log(Level.DEBUG, "Client connected (" + socketChannel.getRemoteAddress() + ")");
			
			// get streams
			this.input = new DataInputStream(socketChannel.socket().getInputStream());
			this.output = new DataOutputStream(socketChannel.socket().getOutputStream());
			
			// Syn string (password&fileLength&fileType)
			this.logger.log(Level.INFO, "Waiting SYN");
			String synInfo[] = input.readUTF().split("&");
			
			// check correct SYN
			if (synInfo.length != 3) {
				logger.log(Level.ERROR, "Bad syn string, missing SYN arguments");
				this.output.writeUTF(Messages.BAD_SYN_STRING.name());
				this.close();
				return;
			}
			
			this.logger.log(Level.INFO, "Auth received: " + synInfo[0]);
			this.logger.log(Level.INFO, "File length: " + synInfo[1]);
			this.logger.log(Level.INFO, "File type: " + synInfo[2]);
			
			// start controls
			String auth = synInfo[0];
			long flength = Long.parseLong(synInfo[1]);
			String ftype = synInfo[2];
			
			// check password
			if (!KSUploaderServer.config.getPass().equals(auth)) {
				logger.log(Level.INFO, "Client wrong password");
				this.output.writeUTF(Messages.WRONG_PASSWORD.name());
				this.close();
				return;
			}
			
			// check file length
			if (flength > KSUploaderServer.config.getMaxFileSize()) {
				logger.log(Level.INFO, "Incoming file too large");
				this.output.writeUTF(Messages.FILE_TOO_LARGE.name());
				this.close();
				return;
			}
			
			if (FileHelper.folderSize() + flength >= KSUploaderServer.config.getFolderSize()) {
				logger.log(Level.WARN, "Server full");
				this.output.writeUTF(Messages.SERVER_FULL.name());
				this.close();
				return;
			}
			
			if (!acceptedTypes.containsKey(ftype)) {
				logger.log(Level.INFO, "Incoming file not recognized");
				this.output.writeUTF(Messages.FILE_NOT_RECOGNIZED.name());
				this.close();
				return;
			}
			
			this.output.writeUTF(Messages.OK.name());
			
			File outFile = new File(KSUploaderServer.config.getFolder() + File.separator + FileHelper.generateName() + acceptedTypes.get(ftype));
			
			this.logger.log(Level.INFO, "Transfer started.");
			
			boolean ret = readFromSocket(outFile, flength);
			
			this.logger.log(Level.INFO, "Transfer ended.");
			
			// return URL
			if (ret) {
				this.output.writeUTF(KSUploaderServer.config.getWebUrl() + outFile.getName());
				this.logger.log(Level.INFO, "Returned link -> " + KSUploaderServer.config.getWebUrl() + outFile.getName());
			} else {
				this.output.writeUTF(Messages.UNKNOWN_ERROR.name());
			}
			this.close();
			
		} catch (Exception e) {
			this.logger.log(Level.FATAL, "Something went wrong", e);
		}
	}
	
	private boolean readFromSocket(File fileName, long fileLength) {
		try {
			RandomAccessFile aFile = new RandomAccessFile(fileName, "rw");
			
			FileChannel fileChannel = aFile.getChannel();
			fileChannel.transferFrom(socketChannel, 0, fileLength);
			fileChannel.close();
			aFile.close();
			
		} catch (IOException e) {
			this.logger.log(Level.ERROR, "IOException in readfromsocket", e);
			return false;
		}
		return true;
	}
	
	private void close() {
		try {
			output.close();
			input.close();
			socketChannel.close();
		} catch (IOException e) {
			// no real need, later thread dies
			logger.log(Level.ERROR, "Exception during streams closes", e);
		}
	}
}
