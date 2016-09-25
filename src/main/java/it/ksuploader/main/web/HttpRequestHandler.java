package it.ksuploader.main.web;

import it.ksuploader.main.KSUploaderServer;
import it.ksuploader.utils.FileHelper;
import it.ksuploader.utils.Messages;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static it.ksuploader.utils.FileHelper.acceptedTypes;

/**
 * Created by Sergio on 25/09/2016.
 */
@MultipartConfig
public class HttpRequestHandler extends HttpServlet {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(403);
		try {
			response.getWriter().write("Forbidden.");
		} catch (IOException e) {
			logger.log(Level.ERROR, "Cannot get writer", e);
		}
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			
			if (request.getPart("data") == null) {
				response.getWriter().write("FIELD_DATA_EMPTY");
				return;
			}
			
			String auth = request.getParameter("auth");
			long flength = request.getPart("data").getSize();
			String ftype = request.getParameter("filetype");
			
			if (ftype == null) {
				if (request.getPart("data").getContentType().contains("png")) {
					ftype = "img";
				} else if (request.getPart("data").getContentType().contains("text/plain")) {
					ftype = "txt";
				} else if (request.getPart("data").getContentType().contains("zip")) {
					ftype = "file";
				} else {
					response.getWriter().write(Messages.FILE_NOT_RECOGNIZED.name());
					return;
				}
			}
			
			if (!acceptedTypes.containsKey(ftype)) {
				logger.log(Level.INFO, "Incoming file not recognized");
				response.getWriter().write(Messages.FILE_NOT_RECOGNIZED.name());
				return;
			}
			
			if (!KSUploaderServer.config.getPass().equals(auth)) {
				logger.log(Level.INFO, "Client wrong password");
				response.getWriter().write(Messages.WRONG_PASSWORD.name());
				return;
			}
			
			if (FileHelper.folderSize() + flength >= KSUploaderServer.config.getFolderSize()) {
				logger.log(Level.WARN, "Server full");
				response.getWriter().write(Messages.SERVER_FULL.name());
				return;
			}
			
			File outfile = new File(KSUploaderServer.config.getFolder() + File.separator + FileHelper.generateName() + acceptedTypes.get(ftype));
			
			try (InputStream is = request.getPart("data").getInputStream()) {
				Files.copy(is, outfile.toPath());
			}
			
			response.getWriter().write(KSUploaderServer.config.getWebUrl() + outfile.getName());
			this.logger.log(Level.INFO, "Returned link -> " + KSUploaderServer.config.getWebUrl() + outfile.getName());
			
		} catch (IOException | ServletException e) {
			this.logger.log(Level.FATAL, "Something went wrong", e);
		}
	}
}
