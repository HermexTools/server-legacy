package it.hermex.main.uploaders.socket;

import it.hermex.main.HermexServer;
import it.hermex.utils.FileHelper;
import it.hermex.utils.Messages;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;

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
			
			// Syn string (password & fileLength & fileName)
			this.logger.log(Level.INFO, "Waiting SYN");
			String synInfo[] = input.readUTF().split("&");
			
			// check correct SYN
			if (synInfo.length != 3) {
				logger.log(Level.ERROR, "Bad syn string, missing SYN arguments");
				this.output.writeUTF(Messages.BAD_SYN_STRING.name());
				this.close();
				return;
			}
			
			// start controls
			String auth = synInfo[0];
			long fileLength = Long.parseLong(synInfo[1]);
			String fileName = synInfo[2];
			
			this.logger.log(Level.INFO, MessageFormat.format("Auth, fileName, fileLenght: {0}, {1}, {2} KB", auth, fileName, fileLength / 1024));
			
			// check password
			if (!HermexServer.config.getPass().equals(auth)) {
				logger.log(Level.INFO, "Client wrong password");
				this.output.writeUTF(Messages.WRONG_PASSWORD.name());
				this.close();
				return;
			}
			
			// check file length
			if (fileLength > HermexServer.config.getMaxFileSize()) {
				logger.log(Level.INFO, "Incoming file too large");
				this.output.writeUTF(Messages.FILE_TOO_LARGE.name());
				this.close();
				return;
			}
			
			if (FileHelper.folderSize() + fileLength >= HermexServer.config.getFolderSize()) {
				logger.log(Level.WARN, "Server full");
				this.output.writeUTF(Messages.SERVER_FULL.name());
				this.close();
				return;
			}
			
			this.output.writeUTF(Messages.OK.name());
			
			File outFile;
			if (fileName.contains("{0}")) {
				outFile = new File(HermexServer.config.getFolder() + File.separator + MessageFormat.format(fileName, FileHelper.generateName()));
			} else {
				outFile = new File(HermexServer.config.getFolder() + File.separator + FileHelper.generateName(fileName));
			}
			
			this.logger.log(Level.INFO, "Transfer started.");
			
			boolean ret = readFromSocket(outFile, fileLength);
			
			this.logger.log(Level.INFO, "Transfer ended.");
			
			// return URL
			if (ret) {
				this.output.writeUTF(HermexServer.config.getWebUrl() + outFile.getName());
				this.logger.log(Level.INFO, "Returned link -> " + HermexServer.config.getWebUrl() + outFile.getName());
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
