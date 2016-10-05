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
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Sergio on 27/09/2016.
 */
public class PanelHome extends HttpServlet {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	public static final int ENTRY_PER_PAGE = 40;
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (request.getSession().getAttribute("logged") == null || !(boolean) request.getSession().getAttribute("logged")) {
				response.sendRedirect("/panel/login");
				return;
			}
			
			if (request.getParameter("page") == null) {
				response.sendRedirect(request.getRequestURI() + "?page=0");
				return;
			}
			
			int currentPage = Integer.parseInt(request.getParameter("page"));
			if (currentPage < 0) {
				response.sendRedirect(request.getRequestURI() + "?page=0");
				return;
			}
			
			if (request.getParameter("delete") != null) {
				File f = new File(HermexServer.config.getFolder(), request.getParameter("delete"));
				f.delete();
				response.sendRedirect(request.getRequestURI() + "?page=" + request.getParameter("page"));
				return;
			}
			
			PebbleEngine engine = new PebbleEngine.Builder().build();
			PebbleTemplate compiledTemplate = engine.getTemplate("templates/home.peb");
			Map<String, Object> context = new HashMap<>();
			
			File f = new File(HermexServer.config.getFolder());
			List<Map> files = new ArrayList<>();
			
			File[] folder = f.listFiles();
			Arrays.sort(folder, (b, a) -> Long.compare(a.lastModified(), b.lastModified()));
			
			for (int i = currentPage; i < currentPage + ENTRY_PER_PAGE; i++) {
				if (i >= folder.length) {
					break;
				}
				int finalI = i;
				files.add(new HashMap<String, Object>() {{
					put("img", folder[finalI].getName().endsWith(".png") || folder[finalI].getName().endsWith(".jpg") || folder[finalI].getName().endsWith(".gif"));
					put("file", folder[finalI].getName());
				}});
			}
			context.put("files", files);
			context.put("page", currentPage);
			
			if (currentPage + ENTRY_PER_PAGE < folder.length) {
				context.put("nextPage", currentPage + ENTRY_PER_PAGE);
			} else {
				context.put("nextPage", "");
			}
			
			if (currentPage >= ENTRY_PER_PAGE) {
				context.put("previousPage", currentPage - ENTRY_PER_PAGE);
			} else {
				context.put("previousPage", "");
			}
			
			compiledTemplate.evaluate(response.getWriter(), context);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Cannot get writer.", e);
		} catch (PebbleException e) {
			logger.log(Level.ERROR, "Error in template.", e);
		}
	}
}
