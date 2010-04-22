/*******************************************************************************
 * Copyright (c) 2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rhelp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.statet.r.core.renv.IREnv;


/**
 * 
 */
public interface IRHelpManager {
	
	
	String PORTABLE_URL_SCHEME = "erhelp"; //$NON-NLS-1$
	String PORTABLE_URL_PREFIX = PORTABLE_URL_SCHEME + ':';
	
	String PORTABLE_DEFAULT_RENV_BROWSE_URL = PORTABLE_URL_PREFIX + "/browse/" + IREnv.DEFAULT_WORKBENCH_ENV_ID + '/'; //$NON-NLS-1$
	
	
	String getPackageHttpUrl(IRPackageHelp packageHelp, String target);
	
	String getPageHttpUrl(IRHelpPage page, String target);
	String getPageHttpUrl(String packageName, String pageName,
			IREnv rEnv, String target);
	
	String getTopicHttpUrl(String topic, String packageName,
			IREnv rEnv, String target);
	
	String toHttpUrl(Object object, String target);
	String toHttpUrl(String url, IREnv rEnv, String target);
	Object getContentOfUrl(String url);
	
	boolean ensureIsRunning();
	boolean isDynamic(URI url);
	URI toHttpUrl(URI url) throws URISyntaxException;
	URI toPortableUrl(URI url) throws URISyntaxException;
	
	List<IREnv> getREnvWithHelp();
	
	boolean hasHelp(IREnv rEnv);
	
	IREnvHelp getHelp(IREnv rEnv);
	
	
	void search(RHelpSearchQuery query, IRHelpSearchRequestor requestor,
			IProgressMonitor monitor) throws CoreException;
	
}
