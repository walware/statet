/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.rhelp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import de.walware.ecommons.ui.util.UIAccess;


public class ExecServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	
	public ExecServlet() {
	}
	
	
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req, resp);
	}
	
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req, resp);
	}
	
	private void handleRequest(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException {
		try {
			final URI referer = new URI(req.getHeader("referer")); //$NON-NLS-1$
			if (!referer.getHost().equals(req.getServerName())
					|| referer.getPort() != req.getServerPort()
					|| !referer.getPath().startsWith(req.getContextPath())) {
				throw new Exception();
			}
		}
		catch (final Exception e) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		final String command = req.getPathInfo();
		if (command != null && command.equals("/openFile")) { //$NON-NLS-1$
			execOpenFile(req, resp);
			resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		
		resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported command: " + command); //$NON-NLS-1$
		return;
	}
	
	protected void execOpenFile(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException {
		final String path = req.getParameter("path"); //$NON-NLS-1$
		
		if (path == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		try {
			final IFileStore fileStore = EFS.getLocalFileSystem().getStore(new URI(path));
			UIAccess.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					openExtern(fileStore);
				}
			});
		}
		catch (final URISyntaxException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}
	
	protected void openExtern(final IFileStore fileStore) {
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
		try {
			IDE.openEditorOnFileStore(page, fileStore);
		}
		catch (final PartInitException e) {
//			final String fileName = FileUtil.toString(fileStore);
//			Program.launch(fileName);
		}
	}
	
}
