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

package de.walware.statet.r.core.rhelp;

import java.util.List;

import de.walware.statet.r.core.renv.IREnv;


public interface IREnvHelp {
	
	
	IREnv getREnv();
	
	List<IRHelpKeyword.Group> getKeywords();
	
	List<IRPackageHelp> getRPackages();
	IRPackageHelp getRPackage(String packageName);
	
	IRHelpPage getPage(String packageName, String name);
	IRHelpPage getPageForTopic(String packageName, String topicName);
	List<IRHelpPage> getPagesForTopic(String topic);
	
	String getHtmlPage(IRHelpPage page);
	String getHtmlPage(String packageName, String pageName);
	String getHtmlPage(String packageName, String pageName,
			String queryString, String[] preTags, String[] postTags);
	
	
}
