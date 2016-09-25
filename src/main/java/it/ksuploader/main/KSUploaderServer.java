package it.ksuploader.main;


import it.ksuploader.main.sockets.SocketListener;
import it.ksuploader.main.web.UndertowServer;
import it.ksuploader.utils.Configuration;
import org.apache.log4j.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class KSUploaderServer {
	public static Configuration config;
	public static Logger logger = Logger.getLogger("KSULogger");
	
	public static void main(String[] args) {
		
		setupLogger();

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
			logger.log(Level.FATAL, "Error reading configuration", e);
			System.exit(1);
		}
		System.out.println("--------------------------------------------------------------------------------");
		
		// listen connections
		try {
			SocketListener socketListener = new SocketListener();
			socketListener.start();
			
			UndertowServer w = new UndertowServer();
			w.start();
		} catch (IOException e) {
			logger.log(Level.FATAL, "Error setting up the listener", e);
			System.exit(2);
		}
		
		// open console
		console();
		
		System.exit(0);
	}
	
	private static void bootstrap(String[] args) throws Exception {
		config = new Configuration();
		if (args.length != 0) {
			if (args.length == 6) {
				KSUploaderServer.config.setPort(args[0]);
				KSUploaderServer.config.setPass(args[1]);
				KSUploaderServer.config.setFolder(args[2]);
				KSUploaderServer.config.setFolderSize(args[3]);
				KSUploaderServer.config.setMaxFileSize(args[4]);
				KSUploaderServer.config.setWeb_url(args[5]);
				logger.log(Level.INFO, "Configuration created!");
			} else {
				throw new IllegalArgumentException("Correct args are: port, password, folder, folder size, max file size, web url");
			}
		} else {
			if (
					KSUploaderServer.config.getFolder().equals("") ||
							KSUploaderServer.config.getPass().equals("") ||
							Integer.toString(KSUploaderServer.config.getPort()).equals("") ||
							KSUploaderServer.config.getWebUrl().equals("")) {
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
	
	public static void setupLogger() {
		PatternLayout layout = new PatternLayout("%d [%p|%C{1}] %m%n");
		
		ConsoleAppender logConsole = new ConsoleAppender();
		logConsole.setLayout(layout);
		logConsole.setThreshold(Level.INFO);
		logConsole.activateOptions();
		Logger.getRootLogger().addAppender(logConsole);
		
		FileAppender logFile = new FileAppender();
		logFile.setName("KSUploaderFileLogger");
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		logFile.setFile(MessageFormat.format("logs" + File.separator + "KSUploader_server-{0}.log", date.format(Calendar.getInstance().getTime())));
		logFile.setLayout(layout);
		logFile.setThreshold(Level.INFO);
		logFile.activateOptions();
		Logger.getRootLogger().addAppender(logFile);
	}
	
	private static void console() {
		String cmd = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (!cmd.equals("shutdown")) {
			try {
				cmd = in.readLine();
				switch (cmd.split(" ")[0]) {
					case "change-password":
						if (config.setPass(cmd.split(" ")[1])) {
							System.out.println("Password changed.");
						} else {
							System.out.println("Error.");
						}
						break;
					case "get-password":
						System.out.println(config.getPass());
						break;
					case "help":
					case "?":
						System.out.println("change-password <pass> - Change the server password");
						System.out.println("get-password - Print current password");
						System.out.println("shutdown - Stop the server");
						break;
					case "shutdown":
						System.out.println("Shutting down...");
						break;
					default:
						System.out.println("Invalid command.");
						break;
				}
			} catch (IOException e) {
				logger.log(Level.WARN, "Invalid command.");
			}
		}
	}
}
