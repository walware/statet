/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rhelp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

import de.walware.rj.renv.IRPackageDescription;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.renv.REnvConfiguration;
import de.walware.statet.r.internal.core.rhelp.REnvHelp;
import de.walware.statet.r.internal.core.rhelp.RHelpTopicEntry;
import de.walware.statet.r.internal.core.rhelp.RHelpWebapp;
import de.walware.statet.r.internal.core.rhelp.RHelpWebapp.ContentInfo;


/**
 * Abstract R help servlet.
 */
public abstract class RHelpServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	
	private static final String PACKAGE_INDEX_PAGE_NAME = "00Index"; //$NON-NLS-1$
	
	private static final String ATTR_RENV_ID = "rhelp.renv.id"; //$NON-NLS-1$
	private static final String ATTR_RENV_RESOLVED = "rhelp.renv.resolved"; //$NON-NLS-1$
	private static final String ATTR_RENV_HELP = "rhelp.renv.help"; //$NON-NLS-1$
	
	private static final String[][] MANUALS = new String[][] {
			{ "manual/R-intro.html", "An Introduction to R" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "manual/R-data.html", "R Data Import/Export" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "manual/R-lang.html", "The R Language Definition" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "manual/R-exts.html", "Writing R Extensions" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "manual/R-admin.html", "R Installation and Administration" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "manual/R-ints.html", "R Internals" }, //$NON-NLS-1$ //$NON-NLS-2$
	};
	private static final String[][] MISCS = new String[][] {
			{ "html/about.html", "About R" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "COPYING", "License" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "AUTHORS", "Authors" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "THANKS", "Thanks" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "html/resources.html", "Resources" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "manual/R-FAQ.html", "Frequently Asked Questions" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "html/rw-FAQ.html", "FAQ for Windows port" }, //$NON-NLS-1$ //$NON-NLS-2$
	};
	
	private static void printSaveHtml(final Writer writer, final String s) throws IOException {
		final int length = s.length();
		int next = 0;
		for (int i = 0; i < length; ) {
			final char c = s.charAt(i);
			switch (c) {
			case '"':
				if (i > next) {
					writer.write(s, next, i - next);
				}
				writer.write("&quot;"); //$NON-NLS-1$
				next = ++i;
				continue;
			case '&':
				if (i > next) {
					writer.write(s, next, i - next);
				}
				writer.write("&amp;"); //$NON-NLS-1$
				next = ++i;
				continue;
			case '\'':
				if (i > next) {
					writer.write(s, next, i - next);
				}
				// &apos; see http://www.w3.org/TR/xhtml1/#C_16
				writer.write("&#39;"); //$NON-NLS-1$
				next = ++i;
				continue;
			case '<':
				if (i > next) {
					writer.write(s, next, i - next);
				}
				writer.write("&lt;"); //$NON-NLS-1$
				next = ++i;
				continue;
			case '>':
				if (i > next) {
					writer.write(s, next, i - next);
				}
				writer.write("&gt;"); //$NON-NLS-1$
				next = ++i;
				continue;
			default:
				i++;
				continue;
			}
		}
		if (length > next) {
			writer.write(s, next, length - next);
		}
	}
	
	
	private RCorePlugin fPlugin;
	
	
	public RHelpServlet() {
	}
	
	
	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		
		fPlugin = RCorePlugin.getDefault();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		fPlugin = null;
	}
	
	
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final String path = req.getPathInfo();
		try {
			if (path != null) {
				if (path.endsWith("/R.css")) { //$NON-NLS-1$
					processCss(req, resp);
					return;
				}
				final ContentInfo info = RHelpWebapp.extractContent(path);
				if (info != null) {
					if (!checkREnv(req, resp, info.rEnvId)) {
						return;
					}
					if (info.cat == null) {
						printEnvIndex(req, resp);
						return;
					}
					else if (info.cat == RHelpWebapp.CAT_LIBRARY) {
						if (info.command == RHelpWebapp.COMMAND_IDX) {
							processPackageIndex(req, resp, info.packageName);
							return;
						}
						else if (info.command == RHelpWebapp.COMMAND_HTML_PAGE) {
							processHelpPage(req, resp, info.packageName, info.detail);
							return;
						}
						else if (info.command == RHelpWebapp.COMMAND_HELP_TOPIC) {
							processTopic(req, resp, info.packageName, info.detail);
							return;
						}
					}
					else if (info.cat == RHelpWebapp.CAT_DOC) {
						processDoc(req, resp, info.detail);
						return;
					}
					
				}
			}
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		finally {
			final IREnvHelp help = (IREnvHelp) req.getAttribute(ATTR_RENV_HELP);
			if (help != null) {
				help.unlock();
			}
		}
	}
	
	private boolean checkREnv(final HttpServletRequest req, final HttpServletResponse resp,
			final String id) throws IOException {
		final IREnvManager rEnvManager = fPlugin.getREnvManager();
		IREnv rEnv = rEnvManager.get(id, null);
		if (rEnv != null) {
			rEnv = rEnv.resolve();
		}
		if (rEnv != null && rEnv.getConfig() != null) {
			req.setAttribute(ATTR_RENV_ID, id);
			req.setAttribute(ATTR_RENV_RESOLVED, rEnv);
			final IRHelpManager rHelpManager = fPlugin.getRHelpManager();
			final IREnvHelp help = rHelpManager.getHelp(rEnv);
			if (help != null) {
				req.setAttribute(ATTR_RENV_HELP, help);
				return true;
			}
			resp.sendError(HttpServletResponse.SC_NOT_FOUND,
					"The R library of the requested R environment <code>" + rEnv.getName() + 
					"</code> is not yet indexed. Please run the indexer first to enable R help support.");
			return false;
		}
		else {
			if (id.equals(IREnv.DEFAULT_WORKBENCH_ENV_ID)) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND,
						"The requested default R environment is missing. " +
						"Please configure an environment as default.");
				return false;
			}
			resp.sendError(HttpServletResponse.SC_NOT_FOUND,
					"The requested R environment doesn't exist. " +
					"Please change the environment.");
			return false;
		}
	}
	
	private void processHelpPage(final HttpServletRequest req, final HttpServletResponse resp,
			final String packageName, final String detail) throws IOException {
		if (detail != null && detail.equalsIgnoreCase(PACKAGE_INDEX_PAGE_NAME)) {
			final String rEnvId = (String) req.getAttribute(ATTR_RENV_ID);
			resp.sendRedirect(req.getContextPath() + req.getServletPath() + '/'+rEnvId +
					'/'+RHelpWebapp.CAT_LIBRARY + '/'+packageName + '/');
			return;
		}
		
		final IREnvHelp help = (IREnvHelp) req.getAttribute(ATTR_RENV_HELP);
		final String qs = req.getParameter(RHelpWebapp.PAR_QUERY_STING);
		final String html = help.getHtmlPage(packageName, detail, qs,
				getHightlightPreTags(), getHightlightPostTags());
		if (html != null) {
			printHtmlPage(req, resp, html);
			return;
		}
		final IRHelpPage page = help.getPageForTopic(packageName, detail);
		if (page != null) {
			redirect(req, resp, page);
			return;
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND,
				"Help page <code>" + detail + "</code> {<code>" + packageName + "</code>} not found.");
		return;
	}
	
	private void processPackageIndex(final HttpServletRequest req, final HttpServletResponse resp,
			final String packageName) throws IOException {
		final REnvHelp help = (REnvHelp) req.getAttribute(ATTR_RENV_HELP);
		final IRPackageHelp packageHelp = help.getRPackage(packageName);
		if (packageHelp != null) {
			final List<RHelpTopicEntry> packageTopics = help.getPackageTopics(packageHelp);
			if (packageTopics != null) {
				printPackageIndex(req, resp, packageHelp, packageTopics);
				return;
			}
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND,
				"Help for package <code>" + packageName + "</code> not found.");
		return;
	}
	
	private void processTopic(final HttpServletRequest req, final HttpServletResponse resp,
			final String packageName, final String detail) throws IOException {
		final IREnvHelp help = (IREnvHelp) req.getAttribute(ATTR_RENV_HELP);
		final IRHelpPage page = help.getPageForTopic(packageName, detail);
		if (page != null) {
			redirect(req, resp, page);
			return;
		}
		final List<IRHelpPage> pages = help.getPagesForTopic(detail);
		if (pages.size() == 1) {
			redirect(req, resp, pages.get(0));
			return;
		}
		else {
			printTopicList(req, resp, detail, pages);
			return;
		}
	}
	
	private void processCss(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException {
		final PrintWriter writer = createCssDoc(req, resp);
		
		writer.println("span.acronym { font-size: small }\n" + //$NON-NLS-1$
				"span.env { font-family: monospace }\n" + //$NON-NLS-1$
				"span.file { font-family: monospace }\n" + //$NON-NLS-1$
				"span.option { font-family: monospace }\n" + //$NON-NLS-1$
				"span.pkg { font-weight: bold }\n" + //$NON-NLS-1$
				"span.samp { font-family: monospace }"); //$NON-NLS-1$
		
		writer.println("body { line-height: 125%; margin: 1em; padding: 0; color: black; }"); //$NON-NLS-1$
		writer.println("table { margin: 0.4em 0 0.4em 0; border-collapse:collapse; border:0px; font-size: 100% }"); //$NON-NLS-1$
		writer.println("td { padding: 0.2em 0.8em 0.2em 0; border:0px; }"); //$NON-NLS-1$
		writer.println("h2 { font-size: 120%; font-weight: bold; margin: 0 0 0.6em 0; }"); //$NON-NLS-1$
		writer.println("h3 { font-size: 110%; font-weight: bold; letter-spacing: 0.05em; margin: 1.0em 0 0.6em 0; }"); //$NON-NLS-1$
		writer.println("p, pre { margin: 0.6em 0 0.6em 0; }"); //$NON-NLS-1$
		writer.println("td { vertical-align: top; }"); //$NON-NLS-1$
		writer.println("hr { margin-top: 0.8em; clear: both; }"); //$NON-NLS-1$
		
		writer.println("div.toc { display: none; font-size: 80%; line-height: 125%; padding: 0.2em 0.8em 0.4em; }"); //$NON-NLS-1$
		writer.println("div.toc ul { list-style: none; padding: 0; margin: 0 }"); //$NON-NLS-1$
		writer.println("div.toc pre { margin: 0 0 0.4em; }"); //$NON-NLS-1$
		writer.println("div.toc a { text-decoration: none; color: black; }"); //$NON-NLS-1$
		writer.println("div.toc a:visited { text-decoration: none; color: black; }"); //$NON-NLS-1$
		
		customizeCss(writer);
	}
	
	private IFileStore getDocDirectory(final REnvHelp envHelp) {
		final String docDir = envHelp.getDocDir();
		final IREnvConfiguration config = envHelp.getREnv().getConfig();
		if (docDir != null && config instanceof REnvConfiguration) {
			final IFileStore docDirectory = ((REnvConfiguration) config).resolvePath(docDir);
			if (docDirectory != null && EFS.getLocalFileSystem().equals(docDirectory.getFileSystem())) {
				return docDirectory;
			}
		}
		return null;
	}
	
	private void processDoc(final HttpServletRequest req, final HttpServletResponse resp, String path)
			throws IOException {
		final REnvHelp envHelp = (REnvHelp) req.getAttribute(ATTR_RENV_HELP);
		final IFileStore docDirectory = getDocDirectory(envHelp);
		if (docDirectory != null) {
			if (path.startsWith("/")) { //$NON-NLS-1$
				path = path.substring(1);
			}
			final IFileStore file = docDirectory.getFileStore(new Path(path));
			InputStream in = null;
			try {
				if (file.fetchInfo().exists()) {
					in = file.openInputStream(EFS.NONE, null);
					in.available();
					final byte[] buffer = new byte[1024];
					
					if (file.getName().endsWith(".html")) { //$NON-NLS-1$
						resp.setContentType("text/html; charset=UTF-8"); //$NON-NLS-1$
						resp.setHeader("Cache-Control", "max-age=600, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else if (file.getName().indexOf(".") < 0) { //$NON-NLS-1$
						resp.setContentType("text/plain; charset=US-ASCII"); //$NON-NLS-1$
					}
					final ServletOutputStream outputStream = resp.getOutputStream();
					
					int n = 0;
					while ((n = in.read(buffer, 0, 1024)) >= 0) {
						outputStream.write(buffer, 0, n);
					}
					return;
				}
			}
			catch (final CoreException e) {
			}
			finally {
				if (in != null) {
					in.close();
				}
			}
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, file.toString());
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, "R doc directory");
	}
	
	private PrintWriter createHtmlDoc(final HttpServletRequest req, final HttpServletResponse resp,
			final String title) throws IOException {
		resp.setContentType("text/html;charset=UTF-8"); //$NON-NLS-1$
		resp.setHeader("Cache-Control", "max-age=30, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$
		final PrintWriter writer = resp.getWriter();
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
		writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"); //$NON-NLS-1$
		
		writer.println("<html><head>"); //$NON-NLS-1$
		writer.write("<title>"); //$NON-NLS-1$
		printSaveHtml(writer, title);
		writer.write("</title>"); //$NON-NLS-1$
		writer.write("<link rel=\"stylesheet\" type=\"text/css\" href=\""); //$NON-NLS-1$
		writer.write(req.getContextPath());
		writer.write(req.getServletPath());
		writer.println("/R.css\"/>"); //$NON-NLS-1$
		return writer;
	}
	
	private void redirect(final HttpServletRequest req, final HttpServletResponse resp,
			final IRHelpPage page) throws IOException {
		final String location = RHelpWebapp.CONTEXT_PATH + req.getServletPath() +
				'/'+req.getAttribute(ATTR_RENV_ID) + '/'+RHelpWebapp.CAT_LIBRARY +
				'/'+page.getPackage().getName() + '/'+RHelpWebapp.COMMAND_HTML_PAGE +
				'/'+page.getName() + ".html"; //$NON-NLS-1$
		resp.sendRedirect(location);
	}
	
	private PrintWriter createCssDoc(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/css;charset=UTF-8"); //$NON-NLS-1$
		final PrintWriter writer = resp.getWriter();
		writer.println("@charset \"UTF-8\";"); //$NON-NLS-1$
		return writer;
	}

//	private void redirect() {
//		final StringBuilder sb = new StringBuilder();
//		sb.append(req.getServletPath());
//		sb.append(SERVLET_ID);
//		final String pathInfo = req.getPathInfo();
//		sb.append(pathInfo.substring(1, pathInfo.indexOf('/', 1)));
//		final String queryString = req.getQueryString();
//		sb.append("?renv=fallback-renv");
//		if (queryString != null) {
//			sb.append('&');
//			sb.append(queryString);
//		}
//		resp.sendRedirect(sb.toString());
//	}
	
	private void printHtmlPage(final HttpServletRequest req, final HttpServletResponse resp,
			final String html) throws IOException {
		resp.setContentType("text/html;charset=UTF-8"); //$NON-NLS-1$
		resp.setHeader("Cache-Control", "max-age=30, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$
		final PrintWriter writer = resp.getWriter();
		final int idxHead = html.indexOf("</head>"); //$NON-NLS-1$
		if (idxHead > 0) {
			writer.write(html, 0, idxHead);
			customizePageHtmlHeader(req, writer);
			int idxEndExamples = html.lastIndexOf(RHelpWebapp.HTML_END_EXAMPLES); 
			if (idxEndExamples > 0) {
				final int idxBeginExamples = html.lastIndexOf(RHelpWebapp.HTML_BEGIN_EXAMPLES, idxEndExamples); 
				writer.write(html, idxHead, idxBeginExamples-idxHead);
				customizeExamples(writer, html.substring(
						idxBeginExamples+RHelpWebapp.HTML_BEGIN_EXAMPLES.length(), idxEndExamples));
				idxEndExamples += RHelpWebapp.HTML_END_EXAMPLES.length();
				writer.write(html, idxEndExamples, html.length()-idxEndExamples);
			}
			else {
				writer.write(html, idxHead, html.length()-idxHead);
			}
		}
		else {
			writer.write(html);
		}
	}
	
	private void printTopicList(final HttpServletRequest req, final HttpServletResponse resp,
			final String topic, final List<IRHelpPage> pages) throws IOException {
		final PrintWriter writer = createHtmlDoc(req, resp, 
				NLS.bind("Help on topic {0}", '\''+topic+'\''));
		final String codeTopic = "<code>" + topic + "</code>"; //$NON-NLS-1$ //$NON-NLS-2$
		customizeIndexHtmlHeader(req, writer);
		writer.println("</head><body>"); //$NON-NLS-1$
		writer.write("<h2>"); //$NON-NLS-1$
		writer.write(NLS.bind("Help on topic {0}", codeTopic));
		writer.write("</h2>"); //$NON-NLS-1$
		
		if (pages != null && !pages.isEmpty()) {
			writer.write("<p>"); //$NON-NLS-1$
			writer.write(NLS.bind("Help on topic {0} was found in the following pages:", codeTopic));
			writer.println("</p>"); //$NON-NLS-1$
			
			Collections.sort(pages);
			writer.write("<table>"); //$NON-NLS-1$
			for (final IRHelpPage page : pages) {
				writer.write("<tr><td style=\"white-space: nowrap;\">"); //$NON-NLS-1$
				writer.write("<a href=\""); //$NON-NLS-1$
				writer.write(getRelativePageUrl(page));
				writer.write("\"><code>"); //$NON-NLS-1$
				writer.write(page.getName());
				writer.write("</code></a> {"); //$NON-NLS-1$
				writer.write("<a href=\""); //$NON-NLS-1$
				writer.write(getRelativeIndexUrl(page.getPackage()));
				writer.write("\" title=\""); //$NON-NLS-1$
				writer.write(page.getPackage().getName());
				writer.write(" ["); //$NON-NLS-1$
				writer.write(page.getPackage().getVersion());
				writer.write("]\n"); //$NON-NLS-1$
				printSaveHtml(writer, page.getPackage().getTitle());
				writer.write("\"><code>"); //$NON-NLS-1$
				writer.write(page.getPackage().getName());
				writer.write("</code></a>}</td>"); //$NON-NLS-1$
				writer.write("<td>"); //$NON-NLS-1$
				printSaveHtml(writer, page.getTitle());
				writer.write("</td></tr>"); //$NON-NLS-1$
			}
			writer.write("</table>"); //$NON-NLS-1$
		}
		else {
			writer.write(NLS.bind("No help found on topic {0} in any package in the R library.", codeTopic));
		}
		writer.println("</body></html>"); //$NON-NLS-1$
	}
	
	private void printPackageIndex(final HttpServletRequest req, final HttpServletResponse resp,
			final IRPackageHelp packageHelp, final List<RHelpTopicEntry> packageTopics) throws IOException {
		final IRPackageDescription packageDescription = packageHelp.getPackageDescription();
		final PrintWriter writer = createHtmlDoc(req, resp,
				NLS.bind("Package {0} - {1}", '\''+packageHelp.getName()+'\'', packageHelp.getTitle()) );
		customizeIndexHtmlHeader(req, writer);
		writer.println("</head><body>"); //$NON-NLS-1$
		writer.write("<table class=\"header\"><tr><td>"); //$NON-NLS-1$
		writer.write(packageHelp.getName());
		writer.write(" ["); //$NON-NLS-1$
		writer.write(packageHelp.getVersion());
		writer.write("]"); //$NON-NLS-1$
		writer.println("</td></tr></table>"); //$NON-NLS-1$
		
		writer.println("<div class=\"toc\"><ul>"); //$NON-NLS-1$
//		writer.println("<li><a href=\"#description\">Description</a></li>");
		writer.write("<li><a href=\"#topics\">Help Topics</a><pre>"); //$NON-NLS-1$
		TOC: for (int i = 'A', j = 0; i <= 'Z'; i++) {
			if ((i-'A') % 7 == 0) {
				writer.println();
			}
			writer.print(' ');
			String name;
			while (j < packageTopics.size() &&
					(name = packageTopics.get(j).getAlias()) != null && name.length() > 0) {
				final char c = Character.toUpperCase(name.charAt(0));
				if (c >= 'A' && c <= 'Z') {
					if (c > i) {
						break;
					}
					if (c == i) {
						writer.write("<a href=\"#idx"); //$NON-NLS-1$
						writer.print((char) (32 + c)); // lowercase
						writer.write("\" class=\"mnemonic\">"); //$NON-NLS-1$
						writer.print(c);
						writer.write("</a>"); //$NON-NLS-1$
						continue TOC;
					}
				}
				j++;
			}
			writer.print((char) i);
		}
		writer.println("</pre></li>"); //$NON-NLS-1$
		if (packageDescription != null) {
			if (packageDescription.getAuthor() != null && packageDescription.getAuthor().length() > 0) {
				writer.println("<li><a href=\"#authors\">Author(s)</a></li>"); //$NON-NLS-1$
			}
			if (packageDescription.getMaintainer() != null && packageDescription.getMaintainer().length() > 0) {
				writer.println("<li><a href=\"#maintainer\">Maintainer</a></li>"); //$NON-NLS-1$
			}
		}
		writer.println("</ul></div>"); //$NON-NLS-1$
		
		writer.write("<h2>"); //$NON-NLS-1$
		printSaveHtml(writer, packageHelp.getTitle());
		writer.write("</h2>"); //$NON-NLS-1$
		
		if (packageDescription != null) {
			final String description = packageDescription.getDescription();
			if (description.length() > 0) {
				writer.write("<h3 id=\"description\">Description</h3>"); //$NON-NLS-1$
				writer.write("<p>"); //$NON-NLS-1$
				printSaveHtml(writer, description);
				if (description.charAt(description.length()-1) != '.') {
					writer.print('.');
				}
				writer.write("</p>"); //$NON-NLS-1$
			}
		}
		
		writer.write("<h3 id=\"topics\">Help Topics</h3>"); //$NON-NLS-1$
		writer.write("<table>"); //$NON-NLS-1$
		final String basePath = req.getContextPath() + req.getServletPath() + '/' + packageHelp.getREnv().getId() +
				'/' + RHelpWebapp.CAT_LIBRARY + '/' + packageHelp.getName() + '/' + RHelpWebapp.COMMAND_HTML_PAGE + '/';
		int lastChar = 0;
		for (final RHelpTopicEntry topic : packageTopics) {
			final IRHelpPage page = topic.getPage();
			final String alias = topic.getAlias();
			writer.write("<tr><td style=\"white-space: nowrap;\">"); //$NON-NLS-1$
			writer.write("<a href=\""); //$NON-NLS-1$
			writer.write(basePath);
			writer.write(page.getName());
			writer.write(".html"); //$NON-NLS-1$
			writer.print('"');
			if (alias.length() > 0) {
				final char c = Character.toUpperCase(alias.charAt(0));
				if (c >= 'A' && c <= 'Z' && c > lastChar) {
					lastChar = c;
					writer.write(" id=\"idx"); //$NON-NLS-1$
					writer.print((char) (32 + c)); // lowercase
					writer.print('"');
				}
			}
			writer.write(" title=\""); //$NON-NLS-1$
			writer.write(page.getName());
			writer.write(" {"); //$NON-NLS-1$
			writer.write(packageHelp.getName());
			writer.write("}\n"); //$NON-NLS-1$
			printSaveHtml(writer, page.getTitle());
			writer.write("\"><code>"); //$NON-NLS-1$
			writer.write(alias);
			writer.write("</code></a>"); //$NON-NLS-1$
			writer.write("</td><td>"); //$NON-NLS-1$
			printSaveHtml(writer, page.getTitle());
			writer.write("</td></tr>"); //$NON-NLS-1$
		}
		writer.write("</table>"); //$NON-NLS-1$
		
		if (packageDescription != null) {
			if (packageDescription.getAuthor() != null && packageDescription.getAuthor().length() > 0) {
				writer.write("<h3 id=\"authors\">Author(s)</h3>"); //$NON-NLS-1$
				writer.write("<p>"); //$NON-NLS-1$
				printSaveHtml(writer, packageDescription.getAuthor());
				writer.write("</p>"); //$NON-NLS-1$
			}
			if (packageDescription.getMaintainer() != null && packageDescription.getMaintainer().length() > 0) {
				writer.write("<h3 id=\"maintainer\">Maintainer</h3>"); //$NON-NLS-1$
				writer.write("<p>"); //$NON-NLS-1$
				printSaveHtml(writer, packageDescription.getMaintainer());
				writer.write("</p>"); //$NON-NLS-1$
			}
			if (packageDescription.getUrl() != null && packageDescription.getUrl().length() > 0) {
				writer.write("<h3 id=\"url\">URL</h3>"); //$NON-NLS-1$
				writer.write("<p><a href=\""); //$NON-NLS-1$
				printSaveHtml(writer, packageDescription.getUrl());
				writer.write("\"><code>"); //$NON-NLS-1$
				printSaveHtml(writer, packageDescription.getUrl());
				writer.write("</code></a></p>"); //$NON-NLS-1$
			}
		}
		writer.println("</body></html>"); //$NON-NLS-1$
	}
	
	private void printEnvIndex(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException {
		final REnvHelp envHelp = (REnvHelp) req.getAttribute(ATTR_RENV_HELP);
		final IREnv rEnv = envHelp.getREnv();
		final IFileStore docDirectory = getDocDirectory(envHelp);
		final List<IRPackageHelp> packages = envHelp.getRPackages();
		final PrintWriter writer = createHtmlDoc(req, resp,
				NLS.bind("R Environment {0}", '\''+rEnv.getName()+'\'') );
		final String baseDocPath = req.getContextPath() + req.getServletPath() + '/'+rEnv.getId() +
				'/'+RHelpWebapp.CAT_DOC + '/';
		final String baseLibPath = req.getContextPath() + req.getServletPath() + '/'+rEnv.getId() +
				'/'+RHelpWebapp.CAT_LIBRARY +'/';
		customizeIndexHtmlHeader(req, writer);
		writer.println("</head><body>"); //$NON-NLS-1$
		
		writer.println("<div class=\"toc\"><ul>"); //$NON-NLS-1$
		writer.write("<li><a href=\"#manuals\">Manuals</a></li>"); //$NON-NLS-1$
		writer.write("<li><a href=\"#packages\">Packages</a><pre>"); //$NON-NLS-1$
		TOC: for (int i = 'A', j = 0; i <= 'Z'; i++) {
			if ((i-'A') % 7 == 0) {
				writer.println();
			}
			writer.print(' ');
			String name;
			while (j < packages.size() &&
					(name = packages.get(j).getName()) != null && name.length() > 0) {
				final char c = Character.toUpperCase(name.charAt(0));
				if (c >= 'A' && c <= 'Z') {
					if (c > i) {
						break;
					}
					if (c == i) {
						writer.write("<a href=\"#idx"); //$NON-NLS-1$
						writer.print((char) (32 + c)); // lowercase
						writer.write("\" class=\"mnemonic\">"); //$NON-NLS-1$
						writer.print(c);
						writer.write("</a>"); //$NON-NLS-1$
						continue TOC;
					}
				}
				j++;
			}
			writer.print((char) i);
		}
		writer.println("</pre></li>"); //$NON-NLS-1$
		if (docDirectory != null) {
			writer.write("<li><a href=\"#misc\">Misc. Material</a></li>"); //$NON-NLS-1$
		}
		writer.println("</ul></div>"); //$NON-NLS-1$
		
		writer.write("<h2>"); //$NON-NLS-1$
		writer.write(rEnv.getName());
		writer.write("</h2>"); //$NON-NLS-1$
		
		writer.write("<h3 id=\"manuals\">Manuals</h3>"); //$NON-NLS-1$
		if (docDirectory != null) {
			writer.write("<table>"); //$NON-NLS-1$
			for (int i = 0; i < MANUALS.length; i++) {
				final String link = MANUALS[i][0];
				final IFileStore file = docDirectory.getFileStore(new Path(link));
				if (file.fetchInfo().exists()) {
					writer.write("<tr><td>"); //$NON-NLS-1$
					writer.write("<a href=\""); //$NON-NLS-1$
					writer.write(baseDocPath);
					writer.write(link);
					writer.write("\">"); //$NON-NLS-1$
					writer.write(MANUALS[i][1]);
					writer.write("</a>"); //$NON-NLS-1$
					if (link.endsWith(".html")) { //$NON-NLS-1$
						final IFileStore pdfFile = file.getParent().getChild(
								file.getName().substring(0, file.getName().length()-5) + ".pdf"); //$NON-NLS-1$
						if (pdfFile.fetchInfo().exists()) {
							writer.write(" [<a"); //$NON-NLS-1$
							printOpenFileLink(writer, pdfFile);
							writer.write(" title=\"Open in PDF Viewer\">PDF</a>]");
						}
					}
					writer.write("</td></tr>"); //$NON-NLS-1$
				}
			}
			writer.write("</table>"); //$NON-NLS-1$
		}
		else {
			writer.write("<p>Manuals not available, because R home documentation folder is not found.</p>");
		}
		
		writer.write("<h3 id=\"packages\">Packages</h3>"); //$NON-NLS-1$
		writer.write("<table>"); //$NON-NLS-1$
		char lastChar = 0;
		for (final IRPackageHelp packageHelp : packages) {
			final String name = packageHelp.getName();
			writer.write("<tr><td>"); //$NON-NLS-1$
			writer.write("<a href=\""); //$NON-NLS-1$
			writer.write(baseLibPath);
			writer.write(name);
			writer.write("/\" title=\""); //$NON-NLS-1$
			writer.write(name);
			writer.write(" ["); //$NON-NLS-1$
			printSaveHtml(writer, packageHelp.getVersion());
			writer.print(']');
			writer.print('"');
			if (name.length() > 0) {
				final char c = Character.toUpperCase(name.charAt(0));
				if (c >= 'A' && c <= 'Z' && c > lastChar) {
					lastChar = c;
					writer.write(" id=\"idx"); //$NON-NLS-1$
					writer.print((char) (32 + c)); // lowercase
					writer.print('"');
				}
			}
			writer.write("><code>"); //$NON-NLS-1$
			writer.write(packageHelp.getName());
			writer.write("</code></a>"); //$NON-NLS-1$
			writer.write("</td><td>"); //$NON-NLS-1$
			printSaveHtml(writer, packageHelp.getTitle());
			writer.write("</td></tr>"); //$NON-NLS-1$
		}
		writer.write("</table>"); //$NON-NLS-1$
		
		if (docDirectory != null) {
			writer.write("<h3 id=\"misc\">Miscellaneous Material</h3>"); //$NON-NLS-1$
			writer.write("<table>"); //$NON-NLS-1$
			for (int i = 0; i < MISCS.length; i++) {
				final String link = MISCS[i][0];
				final IFileStore file = docDirectory.getFileStore(new Path(link));
				if (file.fetchInfo().exists()) {
					writer.write("<tr><td>"); //$NON-NLS-1$
					writer.write("<a href=\""); //$NON-NLS-1$
					writer.write(baseDocPath);
					writer.write(link);
					writer.write("\">"); //$NON-NLS-1$
					writer.write(MISCS[i][1]);
					writer.write("</a>"); //$NON-NLS-1$
					if (link.endsWith(".html")) { //$NON-NLS-1$
						final IFileStore pdfFile = file.getParent().getChild(
								file.getName().substring(0, file.getName().length()-5) + ".pdf"); //$NON-NLS-1$
						if (pdfFile.fetchInfo().exists()) {
							writer.write(" [<a"); //$NON-NLS-1$
							printOpenFileLink(writer, pdfFile);
							writer.write(" title=\"Open in PDF Viewer\">PDF</a>]");
						}
					}
					writer.write("</td></tr>"); //$NON-NLS-1$
				}
			}
			writer.write("</table>"); //$NON-NLS-1$
		}
		
		writer.write("<hr/>"); //$NON-NLS-1$
		
		writer.println("</body></html>"); //$NON-NLS-1$
	}
	
	private String getRelativePageUrl(final IRHelpPage page) {
		final StringBuilder sb = new StringBuilder(25);
		sb.append("../../"); //$NON-NLS-1$
		sb.append(page.getPackage().getName());
		sb.append('/');
		sb.append(RHelpWebapp.COMMAND_HTML_PAGE);
		sb.append('/');
		sb.append(page.getName());
		sb.append(".html"); //$NON-NLS-1$
		return sb.toString();
	}
	
	private String getRelativeIndexUrl(final IRPackageHelp pkg) {
		final StringBuilder sb = new StringBuilder(25);
		sb.append("../../"); //$NON-NLS-1$
		sb.append(pkg.getName());
		sb.append('/');
		return sb.toString();
	}
	
	
	private void customizeExamples(final PrintWriter writer, final String html) {
		int idx = 0;
		while (idx < html.length()) {
			int begin = html.indexOf("<pre", idx); //$NON-NLS-1$
			if (begin >= 0) {
				begin = html.indexOf('>', begin+4);
				if (begin >= 0) {
					begin ++;
					final int end = html.indexOf("</pre", begin); //$NON-NLS-1$
					if (end >= 0) {
						writer.write(html, idx, begin-idx);
						printRCode(writer, html.substring(begin, end));
						idx = end;
						continue;
					}
				}
			}
			break;
		}
		writer.write(html, idx, html.length()-idx);
	}
	
	protected String[] getHightlightPreTags() {
		return new String[] { "<b>" }; //$NON-NLS-1$
	}
	
	protected String[] getHightlightPostTags() {
		return new String[] { "</b>" }; //$NON-NLS-1$
	}
	
	protected void customizeCss(final PrintWriter writer) {
	}
	
	protected void customizePageHtmlHeader(final HttpServletRequest req, final PrintWriter writer) {
	}
	
	protected void customizeIndexHtmlHeader(final HttpServletRequest req, final PrintWriter writer) {
	}
	
	protected void printOpenFileLink(final PrintWriter writer, final IFileStore fileStore) {
		// linking from http: to file: usually doesn't work
		writer.write(" href=\""); //$NON-NLS-1$
		writer.write(fileStore.toURI().toString());
		writer.write("\""); //$NON-NLS-1$
	}
	
	protected void printRCode(final PrintWriter writer, final String html) {
		writer.write(html);
	}
	
}
