package it.ksuploader.main;

import it.ksuploader.utils.LoadConfig;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MainServer {
    public static LoadConfig config = new LoadConfig();
    public static Logger logger = Logger.getLogger("KSUserverLog");

    static void log(String toPrint) {
        System.out.println(toPrint);
        logger.info(toPrint);
    }

    public static void err(String s) {
        logger.severe(s);
    }

    public static void main(String[] args) throws Exception {
        FileHandler fh = new FileHandler("./KSULog.txt");
        fh.setFormatter(new SimpleFormatter());
        logger.addHandler(fh);
        logger.setUseParentHandlers(false);
        System.out.println("--------------------------------------------------------------------------------");
        log("Bootstrap...");

        if (args.length != 0) {
            if (args.length == 6) {
                config.changeConfig(args[0], args[1], args[2], args[3], args[4], args[5]);
                log("Restart for load the new config...");
            } else {
                throw new IllegalArgumentException("Correct args are: folder, web_url, pass, port, folder size, max file size");
            }
        } else {
            if (config.getFolder().equals("") || config.getPass().equals("")
                    || Integer.toString(config.getPort()).equals("") || config.getWebUrl().equals("")) {
                throw new Exception("Error reading config properties.");
            } else {
                log("Folder: " + config.getFolder());
                log("Folder Max size: " + config.getFolderSize());
                log("Max file size: " + config.getMaxFileSize());
                log("WebUrl: " + config.getWebUrl());
                log("Pass: " + config.getPass());
                log("Port: " + config.getPort());
            }
        }

        // Controllo se esiste la directory dei file
        if (!new File("./" + config.getFolder()).exists())
            new File("./" + config.getFolder()).mkdir();

        ServerSocketChannel serverSocketChannel;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(config.getPort()));

            log("Listening images from: " + config.getPort());
            log("--------------------------------------------------------------------------------");
        } catch (IOException exc) {
            exc.printStackTrace();
            err(Arrays.toString(exc.getStackTrace()).replace(",", "\n"));
            throw new IllegalArgumentException("Can't init serverSocket!");
        }

        while (true) {
            try {
                new RequestHandler(serverSocketChannel.accept()).start();
            } catch (IOException e) {
                e.printStackTrace();
                err(Arrays.toString(e.getStackTrace()).replace(",", "\n"));
            }
        }
    }
}
