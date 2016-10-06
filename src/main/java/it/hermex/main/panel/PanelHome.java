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

			PebbleEngine engine = new PebbleEngine.Builder().build();
			PebbleTemplate compiledTemplate = engine.getTemplate("templates/home.peb");
			Map<String, Object> context = new HashMap<>();

			File f = new File(HermexServer.config.getFolder());


			File[] folder;
			if (request.getParameter("search") != null) {
				FilenameFilter filenameFilter = (dir, name) -> name.contains(request.getParameter("search"));
				folder = f.listFiles(filenameFilter);
			} else {
				folder = f.listFiles();
			}
			Arrays.sort(folder, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

			List<Map> files = new ArrayList<>();
			for (int i = currentPage; i < currentPage + ENTRY_PER_PAGE; i++) {
				if (i >= folder.length) {
					break;
				}
				int index = i;
				files.add(new HashMap<String, Object>() {{
					put("img", folder[index].getName().endsWith(".png") || folder[index].getName().endsWith(".jpg") || folder[index].getName().endsWith(".gif"));
					put("file", folder[index].getName());
				}});
			}
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

			compiledTemplate.evaluate(response.getWriter(), context);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Cannot get writer.", e);
		} catch (PebbleException e) {
			logger.log(Level.ERROR, "Error in template.", e);
		}
	}
}
