package it.ksuploader.main;

import it.ksuploader.utils.Config;
import it.ksuploader.utils.LogFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class KSUploaderServer {
	public static Config config;
	public static Logger logger = Logger.getLogger("KSULogger");

	public static void main(String[] args) {

		// file handler
		try {
			FileHandler loggerFileHandler = new FileHandler("./KSULog.txt", 10000000, 1, true);
			loggerFileHandler.setFormatter(new LogFormatter());
			loggerFileHandler.setLevel(Level.FINEST);

			logger.addHandler(loggerFileHandler);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// console handler
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new LogFormatter());
		consoleHandler.setLevel(Level.FINEST);

		logger.addHandler(consoleHandler);

		logger.setLevel(Level.FINEST);

		/*
		* BOOTING
		*/
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("                               KSUploader Server                                ");
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Bootstrap...");

		try {
			bootstrap(args);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error reading configuration", e);
			System.exit(1);
		}
		System.out.println("--------------------------------------------------------------------------------");

		// listen connections
		try {
			Listener listener = new Listener();
			listener.start();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error setting up the listener", e);
			System.exit(2);
		}

		// open console
		console();

		System.exit(0);
	}

	private static void bootstrap(String[] args) throws Exception {
		config = new Config();
		if (args.length != 0) {
			if (args.length == 6) {
				KSUploaderServer.config.setPort(args[0]);
				KSUploaderServer.config.setPass(args[1]);
				KSUploaderServer.config.setFolder(args[2]);
				KSUploaderServer.config.setFolderSize(args[3]);
				KSUploaderServer.config.setMaxFileSize(args[4]);
				KSUploaderServer.config.setWeb_url(args[5]);
				logger.log(Level.FINE, "Config created!");
			} else {
				throw new IllegalArgumentException("Correct args are: port, password, folder, folder size, max file size, web url");
			}
		} else {
			if (
					KSUploaderServer.config.getFolder().equals("") ||
					KSUploaderServer.config.getPass().equals("") ||
					Integer.toString(KSUploaderServer.config.getPort()).equals("") ||
					KSUploaderServer.config.getWebUrl().equals(""))
			{
				throw new Exception("Error reading config properties.");
			} else {
				System.out.println("Password: " + KSUploaderServer.config.getPass());
				System.out.println("Port: " + KSUploaderServer.config.getPort());
				System.out.println("Folder: " + KSUploaderServer.config.getFolder());
				System.out.println("Folder Max size: " + KSUploaderServer.config.getFolderSize());
				System.out.println("File Max size: " + KSUploaderServer.config.getMaxFileSize());
				System.out.println("Web Url: " + KSUploaderServer.config.getWebUrl());
			}
		}
	}

	private static void console() {
		String cmd = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (!cmd.equals("stop")) {
			try {
				cmd = in.readLine();
				switch (cmd.split(" ")[0]) {
					case "changepassword":
						config.setPass(cmd.split(" ")[1]);
						break;
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Invalid command.");
			}
		}
	}
}
