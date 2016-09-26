package it.ksuploader.main;


import it.ksuploader.main.sockets.SocketListener;
import it.ksuploader.main.web.WebListener;
import it.ksuploader.utils.Configuration;
import org.apache.log4j.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class KSUploaderServer {
	public static Configuration config;
	public static Logger logger = Logger.getLogger("KSULogger");
	
	public static SocketListener socketListener;
	public static WebListener webServer;
	
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
		
		socketListener = new SocketListener();
		webServer = new WebListener();
		
		if (config.isSocketEnabled()) {
			socketListener.startListen();
		}
		
		if (config.isWebserverEnabled()) {
			webServer.startListen();
		}
		
		// open consoleLoop
		consoleLoop();
	}
	
	private static void bootstrap(String[] args) throws Exception {
		config = new Configuration();
		if (args.length != 0) {
			if (args.length == 6) {
				KSUploaderServer.config.setPort(args[0]);
				KSUploaderServer.config.setPort(args[2]);
				KSUploaderServer.config.setPass(args[3]);
				KSUploaderServer.config.setFolder(args[4]);
				KSUploaderServer.config.setFolderSize(args[5]);
				KSUploaderServer.config.setMaxFileSize(args[6]);
				KSUploaderServer.config.setWeb_url(args[7]);
				logger.log(Level.INFO, "Configuration created!");
			} else {
				throw new IllegalArgumentException("Correct args are: port, web_port, password, folder, folder size, max file size, web url");
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
				System.out.println("Web Port: " + KSUploaderServer.config.getWebPort());
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
	
	private static void consoleLoop() {
		String[] cmd;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				cmd = in.readLine().split(" ");
				switch (cmd[0]) {
					case "password":
						switch (cmd[1]) {
							case "get":
								System.out.println(config.getPass());
								break;
							case "change":
								if (cmd.length == 3 && config.setPass(cmd[2])) {
									System.out.println("Password changed.");
								} else {
									System.out.println("Error. Usage: 'password change <pass>pas'");
								}
								break;
						}
						break;
					case "socket":
						switch (cmd[1]) {
							case "stop":
								if (socketListener.isListening()) {
									socketListener.stopListen();
								}
								break;
							case "start":
								if (socketListener.isListening()) {
									socketListener.stopListen();
								}
								socketListener = new SocketListener();
								socketListener.startListen();
								break;
							case "status":
								System.out.println("Socket listening: " + socketListener.isListening());
								break;
						}
						break;
					case "web":
						switch (cmd[1]) {
							case "start":
								if (webServer.isListening()) {
									webServer.stopListen();
								}
								webServer.startListen();
								break;
							case "stop":
								if (webServer.isListening()) {
									webServer.stopListen();
								}
								break;
							case "status":
								System.out.println("Webserver listening: " + webServer.isListening());
								break;
						}
						break;
					case "help":
					case "?":
						System.out.println("password [get|change] - Print or change current password.");
						System.out.println("socket [start|stop|status] - SocketListener operations.");
						System.out.println("web [start|stop|status] - Webserver operations.");
						System.out.println("shutdown - Stop the server");
						break;
					case "shutdown":
						if (socketListener.isListening()) {
							socketListener.stopListen();
						}
						if (webServer.isListening()) {
							webServer.stopListen();
						}
						System.out.println("Bye!");
						System.exit(0);
						break;
					default:
						System.out.println("Invalid command.");
						break;
				}
			} catch (Exception e) {
				logger.log(Level.WARN, "Command error: Invalid.", e);
			}
		}
	}
}
