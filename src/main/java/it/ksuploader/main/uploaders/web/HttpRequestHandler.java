package it.ksuploader.main.uploaders.web;

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
import java.nio.file.Paths;
import java.text.MessageFormat;

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
			
			if (request.getPart("data") == null || request.getParameter("auth") == null) {
				response.getWriter().write("MISSING_OR_INVALID_PARAMETER");
				return;
			}
			
			String auth = request.getParameter("auth");
			long fileLength = request.getPart("data").getSize();
			String fileType = Paths.get(request.getPart("data").getSubmittedFileName()).getFileName().toString();
			
			if (!KSUploaderServer.config.getPass().equals(auth)) {
				logger.log(Level.INFO, "Client wrong password");
				response.getWriter().write(Messages.WRONG_PASSWORD.name());
				request.getPart("data").delete();
				return;
			}
			
			if (FileHelper.folderSize() + fileLength >= KSUploaderServer.config.getFolderSize()) {
				logger.log(Level.WARN, "Server full");
				response.getWriter().write(Messages.SERVER_FULL.name());
				request.getPart("data").delete();
				return;
			}
			
			File outFile;
			if (fileType.contains("{0}")) {
				outFile = new File(KSUploaderServer.config.getFolder() + File.separator + MessageFormat.format(fileType, FileHelper.generateName()));
			} else {
				outFile = new File(KSUploaderServer.config.getFolder() + File.separator + FileHelper.generateName(fileType));
			}
			
			try (InputStream is = request.getPart("data").getInputStream()) {
				Files.copy(is, outFile.toPath());
			}
			response.getWriter().write(KSUploaderServer.config.getWebUrl() + outFile.getName());
			request.getPart("data").delete();
			this.logger.log(Level.INFO, "Returned link -> " + KSUploaderServer.config.getWebUrl() + outFile.getName());
			
		} catch (IOException | ServletException e) {
			this.logger.log(Level.FATAL, "Something went wrong", e);
		}
	}
}
