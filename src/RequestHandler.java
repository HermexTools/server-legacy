import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.imageio.ImageIO;

/**
 * 
 * @author Flegyas
 *
 */
public class RequestHandler implements Runnable {

	private final Socket socket;
	private FileOutputStream fos = null;

	// private final MainServer server = MainServer.getInstance();

	public RequestHandler(Socket socket) {
		this.socket = socket;
	}

	public static int getLastPush(String dir) {
		// Sistema schifoso, da cambiare
		return new File("./" + dir).listFiles().length + 1;
	}

	@Override
	public void run() {

		String type = "";

		try {

			socket.setSoTimeout(10000);
			MainServer.log("Client connected from: " + socket);

			// Prendere immagine
			DataInputStream dis = new DataInputStream(socket.getInputStream());

			// Leggo string
			BufferedReader stringIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Invio al client
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

			// leggo in ricezione
			MainServer.log("Attendo auth");
			String auth = stringIn.readLine();

			// check auth
			MainServer.log("Auth ricevuto: " + auth);

			LoadConfig config = new LoadConfig();
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

					// transfer image
					int len2 = dis.readInt();
					System.out.println("Transfer started.");
					byte[] data2 = new byte[len2];
					dis.readFully(data2);
					System.out.println("Transfer ended.");

					fos = new FileOutputStream(config.getFolder() + "/" + fileName + ".zip");
					fos.write(data2);
					fos.close();

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

			socket.close();

		} catch (Exception exc) {
			System.err.println(exc.toString());
		}
		System.out.println("----------");
	}
}
