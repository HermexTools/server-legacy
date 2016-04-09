package it.ksuploader.main;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

class RequestHandler extends Thread {

	private Logger logger = KSUploaderServer.logger;

	private SocketChannel socketChannel;
	private DataInputStream input;
	private DataOutputStream output;

	private final static HashMap<String, String> acceptedTypes = new HashMap<>();

	static {
		acceptedTypes.put("img", ".png");
		acceptedTypes.put("file", ".zip");
		acceptedTypes.put("txt", ".txt");
	}

	private enum Messages {
		OK,
		BAD_SYN_STRING,
		WRONG_PASSWORD,
		FILE_NOT_RECOGNIZED,
		FILE_TOO_LARGE,
		SERVER_FULL,
		UNKNOWN_ERROR
	}

	RequestHandler(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		this.logger.log(Level.INFO, "RequestHandler initialized");
	}

	@Override
	public void run() {

		try {
			this.logger.log(Level.FINE, "Client connected (" + socketChannel.getRemoteAddress() + ")");

			// get streams
			this.input = new DataInputStream(socketChannel.socket().getInputStream());
			this.output = new DataOutputStream(socketChannel.socket().getOutputStream());

			// Syn string (password&fileLength&fileType)
			this.logger.log(Level.INFO, "Waiting SYN");
			String synInfo[] = input.readUTF().split("&");

			// check correct SYN
			if (synInfo.length != 3) {
				logger.log(Level.SEVERE, "Bad syn string, missing SYN arguments");
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

			if (this.folderSize() + flength >= KSUploaderServer.config.getFolderSize()) {
				logger.log(Level.WARNING, "Server full");
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

			// set file name
			String fileName = new SimpleDateFormat("ddMMyy-HHmmssSS").format(Calendar.getInstance().getTime());
			fileName = new Random().nextInt(9999) + "-" + fileName;

			this.logger.log(Level.FINE, "Transfer started.");

			// set file format
			String format = acceptedTypes.get(ftype);

			boolean ret = readFromSocket(new File(KSUploaderServer.config.getFolder() + File.separator + fileName + format), flength);

			this.logger.log(Level.FINE, "Transfer ended.");

			// return URL
			if (ret) {
				this.output.writeUTF(KSUploaderServer.config.getWebUrl() + fileName + format);
				this.logger.log(Level.FINE, "Returned link -> " + KSUploaderServer.config.getWebUrl() + fileName + format);
			} else {
				this.output.writeUTF(Messages.UNKNOWN_ERROR.name());
			}
			this.close();

		} catch (Exception e) {
			this.logger.log(Level.SEVERE, "Something went wrong", e);
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
			this.logger.log(Level.WARNING, "IOException in readfromsocket", e);
			return false;
		}
		return true;
	}

	private long folderSize() {
		File dir = new File(KSUploaderServer.config.getFolder());
		long length = 0;
		for (File file : dir.listFiles()) {
			if (file.isFile())
				length += file.length();
		}
		return length;
	}

	private void close() {
		try {
			output.close();
			input.close();
			socketChannel.close();
		} catch (IOException e) {
			// no real need, later thread dies
			logger.log(Level.WARNING, "Exception during streams closes", e);
		}
	}
}
