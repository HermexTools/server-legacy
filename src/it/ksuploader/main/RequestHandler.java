package it.ksuploader.main;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Random;

import javax.imageio.ImageIO;

public class RequestHandler implements Runnable {
    
    private SocketChannel socketChannel;
    private DataInputStream dis;
    private String type = null;
    
    public RequestHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        MainServer.log("RequestHandler initialized");
    }
    
    public void run() {
        
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
            MainServer.log("Auth received: " + auth);
            
            String pass = MainServer.config.getPass();
            if (pass.equals(auth)) {
                dos.writeUTF("OK");
                MainServer.log("Client Authenticated");
                
                // Aspetto e leggo il type
                type = dis.readUTF();
                MainServer.log("fileType: " + type);
                
                // Informo il client della ricezione e così parte l'upload
                dos.writeUTF(type);
                
                // Integer i = getLastPush(config.getFolder());
                
                String fileName = System.currentTimeMillis() / 1000 + "" + new Random().nextInt(999);
                MainServer.log("fileName: " + fileName);
                String returnValue;
                switch (type) {
                    
                    case "img":

                        // transfer image
                        readImageFromSocket(MainServer.config.getFolder() + "/" + fileName + ".png");
                        
                        MainServer.log("Sending link...");
                        dos.writeUTF(returnUrl(fileName, type));
                        
                        break;
                        
                    case "file":
                        
                        // transfer file
                        MainServer.log("Transfer started.");
                        returnValue = readFileFromSocket(MainServer.config.getFolder() + "/" + fileName + ".zip");
                        MainServer.log("Transfer ended.");
                        
                        MainServer.log("Sending link...");
                        if(returnValue.equals("OK"))
                            dos.writeUTF(returnUrl(fileName, type));
                        else
                            dos.writeUTF(returnValue);
                        
                        break;
                        
                    case "txt":
                        
                        // transfer a txt
                        MainServer.log("Transfer started.");
                        returnValue = readFileFromSocket(MainServer.config.getFolder() + "/" + fileName + ".txt");
                        MainServer.log("Transfer ended.");
                        
                        MainServer.log("Sending link...");
                        if(returnValue.equals("OK"))
                            dos.writeUTF(returnUrl(fileName, type));
                        else
                            dos.writeUTF(returnValue);
                        
                        break;
                        
                    default:
                        MainServer.log("File type not recognized!");    
                }
                
                MainServer.log("Closing..");
                dos.close();
                dis.close();
            } else {
                dos.writeUTF("WRONG_PASS");
                MainServer.log("Invalid Id or Password");
                dos.close();
                dis.close();
            }
            
            socketChannel.close();
            
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        System.out.println("----------");
    }
    
    private String readFileFromSocket(String fileName) {
        try {
            RandomAccessFile aFile = null;
            FileChannel fileChannel;
            aFile = new RandomAccessFile(fileName, "rw");
            fileChannel = aFile.getChannel();
            
            long fileLength = dis.readLong();

            if (fileLength+folderSize() <= MainServer.config.getFolderSize()){
                if(fileLength <= MainServer.config.getMaxFileSize()){
                    
                    MainServer.log("File length: " + fileLength);
                    
                    fileChannel.transferFrom(socketChannel, 0, fileLength);
                    fileChannel.close();
                    aFile.close();
                    
                    MainServer.log("End of file reached, closing channel");
                    if(fileLength !=  new File(fileName).length()){
                        System.out.println("File invalid, deleting...");
                        new File(fileName).delete();
                    }
                    
                } else {
                    MainServer.log("File too large!");
                    return "FILE_TOO_LARGE";  
                }
                
            } else {
                MainServer.log("Server full !");
                return "SERVER_FULL " ;
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "OK";
    }
    
    private boolean readImageFromSocket(String fileName){
        try {
            int len = dis.readInt();
            if (len+folderSize() <= MainServer.config.getFolderSize()){
                MainServer.log("Transfer started.");
                byte[] data = new byte[len];
                dis.readFully(data);
                MainServer.log("Transfer ended.");
                ImageIO.write(ImageIO.read(new ByteArrayInputStream(data)), "png", new File(fileName));
            } else {
                MainServer.log("Server full!");
                return false;
            }  
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
        
    }
    
    public long folderSize() {
        File dir = new File(MainServer.config.getFolder());
        long length = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                length += file.length();
        }
        return length;
    }
    
    private String returnUrl(String fileName, String type) {
        String urlToReturn = "";
        switch (type) {
            
            case "img":
                urlToReturn = MainServer.config.getWebUrl() + fileName + ".png";
                break;
            case "file":
                urlToReturn = MainServer.config.getWebUrl() + fileName + ".zip";
                break;
            case "txt":
                urlToReturn = MainServer.config.getWebUrl() + fileName + ".txt";
                break;
            default:
                break;
        }
        
        return urlToReturn;
    }
    
}
