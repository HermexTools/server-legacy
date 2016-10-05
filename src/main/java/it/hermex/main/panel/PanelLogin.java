package it.hermex.main.panel;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import it.hermex.main.HermexServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sergio on 27/09/2016.
 */
public class PanelLogin extends HttpServlet {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			
			if (request.getSession().getAttribute("logged") != null && request.getParameter("logout").equals("")) {
				request.getSession().invalidate();
				response.sendRedirect("/panel/login");
				return;
			}
			
			if (request.getSession().getAttribute("logged") != null && (boolean) request.getSession().getAttribute("logged")) {
				response.sendRedirect("/panel");
				return;
			}
			
			PebbleEngine engine = new PebbleEngine.Builder().build();
			PebbleTemplate compiledTemplate = engine.getTemplate("templates/login.peb");
			Map<String, Object> context = new HashMap<>();
			
			compiledTemplate.evaluate(response.getWriter(), context);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Cannot get writer.", e);
		} catch (PebbleException e) {
			logger.log(Level.ERROR, "Error in template.", e);
		}
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (request.getParameter("password") != null && request.getParameter("password").equals(HermexServer.config.getPass())) {
				request.getSession().setAttribute("logged", true);
				response.sendRedirect("/panel");
			} else {
				response.sendRedirect("/panel/login");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
