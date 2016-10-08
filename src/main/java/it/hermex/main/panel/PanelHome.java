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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Sergio on 27/09/2016.
 */
public class PanelHome extends HttpServlet {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	public static final int ENTRY_PER_PAGE = 40;
	
	private PebbleTemplate compiledTemplate = new PebbleEngine.Builder().build().getTemplate("templates/home.peb");
	
	private Pattern pattern = Pattern.compile("^(?:.*\\.(?:png|jp?g|bmp|gif|svg))$");
	
	public PanelHome() throws PebbleException {
		super();
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (request.getSession().getAttribute("logged") == null || !(boolean) request.getSession().getAttribute("logged")) {
				response.sendRedirect("/panel/login");
				return;
			}
			
			if (request.getParameter("delete") != null) {
				File f = new File(HermexServer.config.getFolder(), request.getParameter("delete"));
				f.delete();
				response.sendRedirect("/panel?p=" + request.getParameter("p"));
				return;
			}
			
			if (request.getParameter("p") == null) {
				response.sendRedirect("/panel?p=0");
				return;
			}
			
			int currentPage = Integer.parseInt(request.getParameter("p"));
			if (currentPage < 0) {
				response.sendRedirect("/panel?p=0");
				return;
			}
			
			if (request.getParameter("search") != null && request.getParameter("search").isEmpty()) {
				response.sendRedirect("/panel?p=" + currentPage);
			}
			
			File uploadsDir = new File(HermexServer.config.getFolder());
			File[] folder;
			if (request.getParameter("search") != null) {
				FilenameFilter filenameFilter = (dir, name) -> name.toLowerCase().contains(request.getParameter("search").toLowerCase());
				folder = uploadsDir.listFiles(filenameFilter);
			} else {
				folder = uploadsDir.listFiles();
			}
			Arrays.sort(folder, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
			
			List<Map> files = new ArrayList<>();
			for (int i = currentPage; i < currentPage + ENTRY_PER_PAGE; i++) {
				if (i >= folder.length) {
					break;
				}
				int index = i;
				files.add(new HashMap<String, Object>() {{
					put("img", pattern.matcher(folder[index].getName()).matches());
					put("file", folder[index].getName());
				}});
			}
			
			Map<String, Object> context = new HashMap<>();
			
			context.put("page", currentPage);
			context.put("files", files);
			
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
			
			if (request.getParameter("search") != null && !request.getParameter("search").isEmpty()) {
				context.put("searchVal", request.getParameter("search"));
			}
			
			compiledTemplate.evaluate(response.getWriter(), context);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Cannot get writer.", e);
		} catch (PebbleException e) {
			logger.log(Level.ERROR, "Error in template.", e);
		}
	}
}
