package it.ksuploader.main;


import it.ksuploader.main.uploaders.socket.SocketListener;
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
	public static Webserver webServer;
	
	public static void main(String[] args) {
		
		setupLogger();

		/*
		* BOOTING
		*/
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("                               KSUploader Server                                ");
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Bootstrap...");
		bootstrap();
		System.out.println("--------------------------------------------------------------------------------");
		
		socketListener = new SocketListener();
		webServer = new Webserver();
		
		if (config.isSocketEnabled()) {
			socketListener.startListen();
		}
		
		if (config.isWebserverEnabled()) {
			webServer.startListen();
		}
		
		// open consoleLoop
		System.out.println("Type 'help' or '?' for commands help.");
		consoleLoop();
	}
	
	private static void bootstrap() {
		config = new Configuration();
		
		System.out.println("Port: " + KSUploaderServer.config.getPort());
		System.out.println("Web Port: " + KSUploaderServer.config.getWebPort());
		System.out.println("Web Url: " + KSUploaderServer.config.getWebUrl());
		System.out.println("Folder: " + KSUploaderServer.config.getFolder());
		System.out.println("Folder Max size: " + KSUploaderServer.config.getFolderSize() / (1024 * 1024) + " MB");
		System.out.println("File Max size: " + KSUploaderServer.config.getMaxFileSize() / (1024 * 1024) + " MB");
		
		File uploadFoder = new File(config.getFolder());
		if (!uploadFoder.isDirectory()) {
			if (!uploadFoder.mkdirs()) {
				logger.log(Level.FATAL, "Cannot find or create upload folder.");
				System.exit(1);
			}
		}
	}
	
	private static void setupLogger() {
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
						if (cmd.length == 1) {
							System.out.println("Invalid command.\nRight syntax: password [get|change]");
							break;
						}
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
						if (cmd.length == 1) {
							System.out.println("Invalid command.\nRight syntax: socket [start|stop|status]");
							break;
						}
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
						if (cmd.length == 1) {
							System.out.println("Invalid command.\nRight syntax: web [start|stop|status]");
							break;
						}
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
