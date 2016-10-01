package it.ksuploader.main;


import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import it.ksuploader.main.panel.PanelHome;
import it.ksuploader.main.panel.PanelLogin;
import it.ksuploader.main.uploaders.web.HttpRequestHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.servlet.MultipartConfigElement;
import java.nio.file.Paths;

import static io.undertow.Handlers.resource;
import static it.ksuploader.main.KSUploaderServer.config;

/**
 * Created by Sergio on 25/09/2016.
 */
public class Webserver {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	private boolean listening;
	private Undertow server;
	
	public Webserver() {
		try {
			DeploymentInfo uploadServlet = Servlets.deployment()
					.setClassLoader(Webserver.class.getClassLoader())
					.setDeploymentName("HttpRequestHandler")
					.setContextPath("/")
					.addServlets(
							Servlets.servlet(
									"HttpRequestHandler",
									HttpRequestHandler.class)
									.addMapping("/")
									.setMultipartConfig(new MultipartConfigElement(System.getProperty("java.io.tmpdir"), config.getMaxFileSize(), config.getMaxFileSize() * 1024, 0))
					);
			DeploymentInfo panelServlet = Servlets.deployment()
					.setClassLoader(Webserver.class.getClassLoader())
					.setDeploymentName("Panel")
					.setContextPath("/")
					.addServlets(
							Servlets.servlet("PanelLogin", PanelLogin.class).addMapping("/login"),
							Servlets.servlet("Panel", PanelHome.class).addMapping("/")
					);
			
			DeploymentManager upload = Servlets.defaultContainer().addDeployment(uploadServlet);
			upload.deploy();
			
			DeploymentManager panel = Servlets.defaultContainer().addDeployment(panelServlet);
			panel.deploy();
			
			PathHandler path = Handlers.path(
					resource(new PathResourceManager(Paths.get(KSUploaderServer.config.getFolder()), 0))
							.setDirectoryListingEnabled(false)
			);
			path.addPrefixPath("/upload", upload.start());
			path.addPrefixPath("/panel", panel.start());
			path.addPrefixPath("/assets",
					resource(new ClassPathResourceManager(this.getClass().getClassLoader(), "assets/"))
							.setDirectoryListingEnabled(false)
			);
			
			
			this.server = Undertow.builder()
					.addHttpListener(KSUploaderServer.config.getWebPort(), "0.0.0.0")
					.setHandler(path)
					.build();
		} catch (Exception e) {
			logger.log(Level.ERROR, "Unknown error", e);
		}
	}
	
	public void startListen() {
		try {
			this.server.start();
			logger.log(Level.INFO, "Webserver listening on port " + config.getWebPort() + ".");
			this.listening = true;
		} catch (Exception e) {
			logger.log(Level.ERROR, "Cannot start webserver.", e);
		}
	}
	
	public void stopListen() {
		try {
			this.server.stop();
			this.logger.log(Level.WARN, "Stopping listening...");
			this.listening = false;
		} catch (Exception e) {
			logger.log(Level.ERROR, "Cannot stop webserver.", e);
		}
	}
	
	public boolean isListening() {
		return this.listening;
	}
}
