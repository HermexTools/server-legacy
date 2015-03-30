package it.ksuploader.main;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import javax.imageio.ImageIO;

public class RequestHandler implements Runnable {

	private SocketChannel socketChannel;
	private DataInputStream dis;
	private String type = null;
	private LoadConfig config = null;

	public RequestHandler(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		System.out.println("RequestHandler initialized");
	}

	/*
	 * public static int getLastPush(String dir) { // Sistema schifoso, da
	 * cambiare return new File("./" + dir).listFiles().length + 1; }
	 */

	public void run() {

		try {
			config = new LoadConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {

			// socketChannel.socket().setSoTimeout(10000);
			MainServer.log("Client connected from: " + socketChannel);

			// Ricevo
			dis = new DataInputStream(socketChannel.socket().getInputStream());

			// Invio al client
			DataOutputStream dos = new DataOutputStream(socketChannel.socket().getOutputStream());

			// leggo in ricezione
			MainServer.log("Attendo auth");
			String auth = dis.readUTF();

			// check auth
			MainServer.log("Auth ricevuto: " + auth);

			String pass = config.getPass();
			if (pass.equals(auth)) {
				dos.writeUTF("OK");
				System.out.println("Client Authenticated");

				// Aspetto e leggo il type
				type = dis.readUTF();
				System.out.println("fileType: " + type);

				// Informo il client della ricezione e così parte l'upload
				dos.writeUTF(type);

				// Integer i = getLastPush(config.getFolder());

				String fileName = System.currentTimeMillis() / 1000 + "" + ((int) (Math.random() * 999));
				System.out.println("fileName: " + fileName);

				switch (type) {

				case "img":

					// transfer image
					int len = dis.readInt();
					System.out.println("Transfer started.");
					byte[] data = new byte[len];
					dis.readFully(data);
					System.out.println("Transfer ended.");

					File toWrite = new File(config.getFolder() + "/" + fileName + ".png");
					ImageIO.write(ImageIO.read(new ByteArrayInputStream(data)), "png", toWrite);

					dos.writeUTF(returnUrl(fileName, type));

					break;
				case "file":

					// transfer file
					System.out.println("Transfer started.");
					readFileFromSocket(config.getFolder() + "/" + fileName + ".zip");
					System.out.println("Transfer ended.");

					System.out.println("Sending link...");
					dos.writeUTF(returnUrl(fileName, type));

					break;
				case "txt":

					// transfer a txt
					System.out.println("Transfer started.");
					readFileFromSocket(config.getFolder() + "/" + fileName + ".txt");
					System.out.println("Transfer ended.");

					System.out.println("Sending link...");
					dos.writeUTF(returnUrl(fileName, type));

					break;
				default:

				}

				// i++;

				System.out.println("Chiudo");
				dos.close();
				dis.close();
			} else {
				dos.writeUTF("WRONG_PASS");
				System.out.println("Invalid Id or Password");
				dos.close();
				dis.close();
			}

			socketChannel.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		System.out.println("----------");
	}

	public void readFileFromSocket(String fileName) {
		try {
			RandomAccessFile aFile = null;
			FileChannel fileChannel;
			aFile = new RandomAccessFile(fileName, "rw");
			fileChannel = aFile.getChannel();
			try {

				long fileLength = dis.readLong();
				System.out.println("File length: " + fileLength);

				fileChannel.transferFrom(socketChannel, 0, fileLength);
				fileChannel.close();
				aFile.close();

				Thread.sleep(1000);
				fileChannel.close();
				System.out.println("End of file reached, closing channel");

			} catch (IOException | InterruptedException e) {
				try {
					e.printStackTrace();
					aFile.close();
					fileChannel.close();
					new File(fileName).delete();
					System.out.println("Transfer cancelled.");
					if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
						Runtime.getRuntime().exec("del /f /q " + fileName);
					System.out.println("Transfer cancelled.");
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}

	}

	private String returnUrl(String fileName, String type) {
		String urlToReturn = "";
		switch (type) {

		case "img":
			urlToReturn = config.getWebUrl() + "/" + fileName + ".png";
			break;
		case "file":
			urlToReturn = config.getWebUrl() + "/" + fileName + ".zip";
			break;
		case "txt":
			urlToReturn = config.getWebUrl() + "/" + fileName + ".txt";
			break;
		default:
			break;
		}

		return urlToReturn;
	}

}
