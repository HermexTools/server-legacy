import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import javax.imageio.ImageIO;

public class RequestHandler implements Runnable {

	private SocketChannel socketChannel;

	BufferedReader stringIn;

	public RequestHandler(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		// this.serverSocketChannel = socketChannel;
		System.out.println("RequestHandler initialized");
	}

	public static int getLastPush(String dir) {
		// Sistema schifoso, da cambiare
		return new File("./" + dir).listFiles().length + 1;
	}

	public void run() {

		LoadConfig config = null;
		try {
			config = new LoadConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String type = null;

		try {

			socketChannel.socket().setSoTimeout(10000);
			MainServer.log("Client connected from: " + socketChannel);

			// Prendere immagine
			DataInputStream dis = new DataInputStream(socketChannel.socket().getInputStream());

			// Leggo string
			stringIn = new BufferedReader(new InputStreamReader(socketChannel.socket().getInputStream()));

			// Invio al client
			DataOutputStream dos = new DataOutputStream(socketChannel.socket().getOutputStream());

			// leggo in ricezione
			MainServer.log("Attendo auth");
			String auth = stringIn.readLine();

			// check auth
			MainServer.log("Auth ricevuto: " + auth);

			String pass = config.getPass();
			if (pass.equals(auth)) {
				dos.writeBytes("OK\n");
				System.out.println("Client Authenticated");

				// Aspetto e leggo il type
				type = stringIn.readLine();
				System.out.println("fileType: " + type);

				// Informo il client della ricezione e così parte l'upload
				dos.writeBytes(type + "\n");

				Integer i = getLastPush(config.getFolder());

				String fileName = i.toString();
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

					dos.writeBytes("http://" + config.getDomain() + "/" + toWrite.getName());

					break;
				case "file":

					// transfer file
					System.out.println("Transfer started.");

					readFileFromSocket(config.getFolder() + "/" + fileName + ".zip");
					System.out.println("Transfer ended.");

					System.out.println("Sending link...");
					dos.writeBytes("http://" + config.getDomain() + "/" + fileName + ".zip");

					break;
				default:

				}

				i++;

				System.out.println("Chiudo");
				dos.close();
				dis.close();
				stringIn.close();
			} else {
				dos.writeBytes("Invalid Id or Password");
				System.out.println("Invalid Id or Password");
				dos.close();
				dis.close();
				stringIn.close();
			}

			socketChannel.close();

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		System.out.println("----------");
	}

	public void readFileFromSocket(String fileName) {
		RandomAccessFile aFile = null;
		try {
			aFile = new RandomAccessFile(fileName, "rw");
			FileChannel fileChannel = aFile.getChannel();

			long fileLength = Long.parseLong(stringIn.readLine());
			System.out.println("File length: " + fileLength);

			fileChannel.transferFrom(socketChannel, 0, fileLength);
			fileChannel.close();

			Thread.sleep(1000);
			fileChannel.close();
			System.out.println("End of file reached, closing channel");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
