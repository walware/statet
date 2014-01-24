/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.rhelp;

import java.util.List;

import de.walware.statet.r.core.renv.IREnv;


public interface IREnvHelp {
	
	
	IREnv getREnv();
	
	List<IRHelpKeyword.Group> getKeywords();
	
	List<IRPackageHelp> getRPackages();
	IRPackageHelp getRPackage(String packageName);
	
	/**
	 * Looks for the specified help page in the specified package.
	 * 
	 * @param packageName the name of the package
	 * @param name the name of the page
	 * @return the page if exists, otherwise <code>null</code>
	 */
	IRHelpPage getPage(String packageName, String name);
	
	/**
	 * Looks for the help page for the specified topic in the specified package.
	 * 
	 * @param packageName the name of the package
	 * @param topic the topic
	 * @return the page if exists, otherwise <code>null</code> 
	 */
	IRHelpPage getPageForTopic(String packageName, String topic);
	
	/**
	 * Looks for help pages for the specified topic in all packages.
	 * 
	 * @param topic the topic
	 * @return list with all pages
	 */
	List<IRHelpPage> getPagesForTopic(String topic);
	
	String getHtmlPage(IRHelpPage page);
	String getHtmlPage(String packageName, String pageName);
	String getHtmlPage(String packageName, String pageName,
			String queryString, String[] preTags, String[] postTags);
	
	void unlock();
	
}
