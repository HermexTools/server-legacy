package it.ksuploader.utils;

import it.ksuploader.main.KSUploaderServer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Sergio on 25/09/2016.
 */
public class FileHelper {
	
	public final static HashMap<String, String> acceptedTypes = new HashMap<String, String>() {{
		put("img", ".png");
		put("file", ".zip");
		put("txt", ".txt");
	}};
	
	
	public static long folderSize() {
		File dir = new File(KSUploaderServer.config.getFolder());
		long length = 0;
		for (File file : dir.listFiles()) {
			if (file.isFile())
				length += file.length();
		}
		return length;
	}
	
	public static String generateName() {
		String fileName = new SimpleDateFormat("ddMMyy-HHmmssSS").format(Calendar.getInstance().getTime());
		return new Random().nextInt(9999) + "-" + fileName;
	}
}
