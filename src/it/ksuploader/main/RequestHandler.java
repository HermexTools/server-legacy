package it.ksuploader.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Random;

public class RequestHandler implements Runnable {

	private SocketChannel socketChannel;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String type = null;

	public RequestHandler(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		MainServer.log("RequestHandler initialized");
	}

	public void run() {

		try {
			MainServer.log("Client connected from: " + socketChannel);

			// Ricevo
			dis = new DataInputStream(socketChannel.socket().getInputStream());

			// Invio al client
			dos = new DataOutputStream(socketChannel.socket().getOutputStream());

			// leggo in ricezione
			MainServer.log("Attendo auth");
			String auth = dis.readUTF();

			// check auth
			MainServer.log("Auth received: " + auth);

			String pass = MainServer.config.getPass();
			if (pass.equals(auth)) {
				dos.writeUTF("OK");
				MainServer.log("Client Authenticated");

				// Aspetto e leggo il type
				type = dis.readUTF();
				MainServer.log("fileType: " + type);

				// Informo il client della ricezione e così parte l'upload
				dos.writeUTF(type);

				String fileName = System.currentTimeMillis() / 1000 + "" + new Random().nextInt(999);
				MainServer.log("fileName: " + fileName);
				String returnValue;
				switch (type) {

				case "img":
					// transfer image
					MainServer.log("Transfer started.");
					returnValue = readFileFromSocket(MainServer.config.getFolder() + "/" + fileName + ".png");
					MainServer.log("Transfer ended.");

					if (returnValue.equals("OK")) {
						MainServer.log("Sending link...");
						dos.writeUTF(returnUrl(fileName, type));
					}

					break;

				case "file":

					// transfer file
					MainServer.log("Transfer started.");
					returnValue = readFileFromSocket(MainServer.config.getFolder() + "/" + fileName + ".zip");
					MainServer.log("Transfer ended.");

					if (returnValue.equals("OK")) {
						MainServer.log("Sending link...");
						dos.writeUTF(returnUrl(fileName, type));
					}

					break;

				case "txt":

					// transfer a txt
					MainServer.log("Transfer started.");
					returnValue = readFileFromSocket(MainServer.config.getFolder() + "/" + fileName + ".txt");
					MainServer.log("Transfer ended.");

					if (returnValue.equals("OK")) {
						MainServer.log("Sending link...");
						dos.writeUTF(returnUrl(fileName, type));
					}

					break;

				default:
					MainServer.log("File type not recognized!");
				}

				MainServer.log("Closing..");
				dos.close();
				dis.close();
			} else {
				dos.writeUTF("WRONG_PASS");
				MainServer.log("Invalid Id or Password");
				dos.close();
				dis.close();
			}

			socketChannel.close();

		} catch (Exception exc) {
			exc.printStackTrace();
			MainServer.err(Arrays.toString(exc.getStackTrace()).replace(",", "\n"));
		}
		MainServer.log("----------");
	}

	private String readFileFromSocket(String fileName) {
		try {
			RandomAccessFile aFile = new RandomAccessFile(fileName, "rw");
			FileChannel fileChannel = aFile.getChannel();

			long fileLength = dis.readLong();

			if (fileLength + folderSize() <= MainServer.config.getFolderSize()) {
				if (fileLength <= MainServer.config.getMaxFileSize()) {
					dos.writeUTF("START_TRANSFER");

					MainServer.log("File length: " + fileLength);

					fileChannel.transferFrom(socketChannel, 0, fileLength);
					fileChannel.close();
					aFile.close();

					MainServer.log("End of file reached, closing channel");
					if (fileLength != new File(fileName).length()) {
						MainServer.log("File invalid, deleting...");
						new File(fileName).delete();
					}

				} else {
					MainServer.log("File too large!");
					aFile.close();
					dos.writeUTF("FILE_TOO_LARGE");
					return "FILE_TOO_LARGE";
				}

			} else {
				MainServer.log("Server full !");
				aFile.close();
				dos.writeUTF("SERVER_FULL");
				return "SERVER_FULL";
			}

		} catch (IOException ex) {
			ex.printStackTrace();
			MainServer.err(Arrays.toString(ex.getStackTrace()).replace(",", "\n"));
		}
		return "OK";
	}

	public long folderSize() {
		File dir = new File(MainServer.config.getFolder());
		long length = 0;
		for (File file : dir.listFiles()) {
			if (file.isFile())
				length += file.length();
		}
		return length;
	}

	private String returnUrl(String fileName, String type) {
		String urlToReturn = "";
		switch (type) {

		case "img":
			urlToReturn = MainServer.config.getWebUrl() + fileName + ".png";
			break;
		case "file":
			urlToReturn = MainServer.config.getWebUrl() + fileName + ".zip";
			break;
		case "txt":
			urlToReturn = MainServer.config.getWebUrl() + fileName + ".txt";
			break;
		default:
			break;
		}

		return urlToReturn;
	}

}
