import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.imageio.ImageIO;

/**
 *
 * @author Sergio
 */
public class Main {

	public static int getLastPush(String dir) {
		File[] folder = new File("./" + dir).listFiles();
                int[] folderInt = new int[folder.length];
                for (int i = 0; i < folder.length; i++) {
                    folderInt[i] = Integer.parseInt(folder[i].getName());
                }
		try {
			return folderInt[folderInt.length-1];
		} catch (ArrayIndexOutOfBoundsException ex) {
			return 0;
		}
	}

	public static void init(String test1, String dir, String test2) {
		if (new File("./" + dir).exists() == false)
			new File("./" + dir).mkdir();
	}

	public static void main(String[] args) throws IOException {
		System.out.println("--------------------");
		System.out.println("Bootstrap...");
		try {
			init(args[0], args[1], args[2]);
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.err.println("Correct args: java -jar jarName.jar domain.com Password");
		}

		int i = getLastPush(args[1]);
		Socket socket;
		ServerSocket server = new ServerSocket(4030);
		System.out.println("Bootstrap Completed");
		System.out.println("--------------------");

		while (true) {
			try {
				socket = server.accept();
				socket.setSoTimeout(10000);
				System.out.println("Client connected from: " + socket);

				// Prendere immagine
				DataInputStream dis = new DataInputStream(
						socket.getInputStream());

				// Leggo string
				BufferedReader stringIn = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));

				// Invio al client
				DataOutputStream os = new DataOutputStream(
						socket.getOutputStream());

				// leggo in ricezione
				System.out.println("Attendo auth");
				String auth = "";
				auth = stringIn.readLine();

				// check auth
				System.out.println("Da linea: " + args[2]);
				System.out.println("Auth ricevuto: " + auth);
				if (auth.equals(args[2])) {
					os.writeBytes("OK\n");
					System.out.println("Client Authenticated");
					// transfer image
					int len = dis.readInt();
					System.out.println("Transfer started.");
					byte[] data = new byte[len];
					dis.readFully(data);
					System.out.println("Transfer ended.");
					ImageIO.write(ImageIO.read(new ByteArrayInputStream(data)),
							"jpg", new File("./" + args[1] + "/" + i + ".jpg"));

					// return link
					os.writeBytes("http://" + args[0] + "/" + args[1] + "/" + i
							+ ".jpg");

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

			} catch (SocketTimeoutException | SocketException s) {
				System.err.println(s.toString());
			}
			System.out.println("--------------------");
		}
	}
}