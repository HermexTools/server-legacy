import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
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

	private final MainServer server = MainServer.getInstance();

	public RequestHandler(Socket socket) {
		this.socket = socket;
	}

	public static int getLastPush(String dir) {
		// Sistema schifoso, da cambiare
		return new File("./" + dir).listFiles().length + 1;
	}

	@Override
	public void run() {
		try {
			socket.setSoTimeout(10000);
			MainServer.log("Client connected from: " + socket.getInetAddress());

			// Prendere immagine
			DataInputStream dis = new DataInputStream(socket.getInputStream());

			// Leggo string
			BufferedReader stringIn = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			// Invio al client
			DataOutputStream os = new DataOutputStream(socket.getOutputStream());

			// leggo in ricezione
			MainServer.log("Attendo auth");
			String auth = stringIn.readLine();

			// check auth
			MainServer.log("Auth ricevuto: " + auth);
			// User user = Users.getLocalInstance().getIfValidAuth(auth);
			LoadConfig config = new LoadConfig();
			String pass = config.getPass();
			if (pass.equals(auth)) {
				os.writeBytes("OK\n");
				System.out.println("Client Authenticated");

				// transfer image
				int len = dis.readInt();
				System.out.println("Transfer started.");
				byte[] data = new byte[len];
				dis.readFully(data);
				System.out.println("Transfer ended.");

				// Al momento non interessa
				// File toWrite = new File(user.newPuushFile().getPath());
				int i = getLastPush(config.getFolder());

				String fileName = i + ".jpg";
				System.out.println("fileName: " + fileName);
				File toWrite = new File(config.getFolder() + "/" + fileName);
				ImageIO.write(ImageIO.read(new ByteArrayInputStream(data)),
						"jpg", toWrite);

				// return link
				// os.writeBytes("http://" + config.getDomain() + "/"+
				// toWrite.getParentFile().getName() + "/"+ toWrite.getName());
				os.writeBytes("http://" + config.getDomain() + "/"
						+ toWrite.getName());

				i++;

				os.close();
				dis.close();
				stringIn.close();
			} else {
				os.writeBytes("Invalid Id or Password");
				System.out.println("Invalid Id or Password");
				os.close();
				dis.close();
				stringIn.close();
			}

		} catch (IOException exc) {
			System.err.println(exc.toString());
		}
		System.out.println("----------");
	}
}
