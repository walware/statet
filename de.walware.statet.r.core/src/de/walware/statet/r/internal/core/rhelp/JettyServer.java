/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rhelp;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import de.walware.statet.r.internal.core.RCorePlugin;


public class JettyServer {
	
	
	private static final String WEBAPP_NAME = "rhelp"; //$NON-NLS-1$
	private static final String OTHER_INFO = "de.walware.statet.r.rhelp"; //$NON-NLS-1$
	
	private static final String LOCALHOST = "127.0.0.1"; //$NON-NLS-1$
	
	
	private String fHost;
	private int fPort;
	
	
	public JettyServer() {
		fHost = loadHost();
		fPort = -1;
	}
	
	
	protected String loadHost() {
		String serverProperty = RCorePlugin.getDefault().getBundle().getBundleContext().getProperty("server_host"); //$NON-NLS-1$
		if (serverProperty != null && (serverProperty = serverProperty.trim()).length() > 0) {
			return serverProperty;
		}
		return LOCALHOST;
	}
	
	public void startServer() throws Exception {
		final Bundle bundle = Platform.getBundle("org.eclipse.equinox.http.registry"); //$NON-NLS-1$
		if (bundle == null) {
			throw new IllegalStateException("bundle 'org.eclipse.equinox.http.registry' is missing.");
		}
		
		final Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put("http.host", fHost);
		dict.put("http.port", Integer.valueOf((fPort == -1) ? 0 : fPort)); //$NON-NLS-1$
		
		// set the base URL
		dict.put("context.path", "/rhelp"); //$NON-NLS-1$ //$NON-NLS-2$
		dict.put("other.info", OTHER_INFO); //$NON-NLS-1$ 
		
		// suppress Jetty INFO/DEBUG messages to stderr
		Logger.getLogger("org.mortbay").setLevel(Level.WARNING); //$NON-NLS-1$	
		
		JettyConfigurator.startServer(WEBAPP_NAME, dict);
		
		if (bundle.getState() == Bundle.RESOLVED) {
			bundle.start(Bundle.START_TRANSIENT);
		}
		if (fPort == -1) {
			// Jetty selected a port number for us
			final ServiceReference<?>[] reference = bundle.getBundleContext().getServiceReferences(
					"org.osgi.service.http.HttpService", "(other.info="+OTHER_INFO+")"); //$NON-NLS-1$ //$NON-NLS-2$
			final Object assignedPort = reference[0].getProperty("http.port"); //$NON-NLS-1$
			fPort = Integer.parseInt((String)assignedPort);
		}
	}
	
	public void stopServer() throws Exception {
		JettyConfigurator.stopServer(WEBAPP_NAME);
		fPort = -1;
	}
	
	public String getHost() {
		return fHost;
	}
	
	public int getPort() {
		return fPort;
	}
	
}
