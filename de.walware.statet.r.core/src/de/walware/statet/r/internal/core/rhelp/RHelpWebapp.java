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

package de.walware.statet.r.internal.core.rhelp;


public class RHelpWebapp {
	
	public static final String CONTEXT_PATH = "/rhelp"; //$NON-NLS-1$
	
	public static final String CAT_LIBRARY = "library"; //$NON-NLS-1$
	public static final String CAT_DOC = "doc"; //$NON-NLS-1$
	
	public static final String COMMAND_HTML_PAGE = "html"; //$NON-NLS-1$
	public static final String COMMAND_HELP_TOPIC = "help"; //$NON-NLS-1$
	public static final String COMMAND_IDX = "idx"; //$NON-NLS-1$
	
	public static final String HTML_BEGIN_EXAMPLES = "<!-- BEGIN-EXAMPLES -->"; //$NON-NLS-1$
	public static final String HTML_END_EXAMPLES = "<!-- END-EXAMPLES -->"; //$NON-NLS-1$
	
	public static final String PAR_QUERY_STING = "qs"; //$NON-NLS-1$
	
	public static class ContentInfo {
		
		public String rEnvId;
		public String cat;
		public String command;
		public String packageName;
		public String detail;
		
	}
	
	public static ContentInfo extractContent(final String path) {
		if (path != null) {
			final int idx1 = path.indexOf('/', 1); // rEnvId
			if (idx1 >= 2) {
				final ContentInfo content = new ContentInfo();
				content.rEnvId = path.substring(1, idx1);
				
				final int idx2 = path.indexOf('/', idx1+1); // cat
				if (idx2 >= idx1+1) {
					content.cat = path.substring(idx1+1, idx2);
					
					if (content.cat.equals(CAT_DOC)) {
						content.cat = CAT_DOC;
						content.detail = path.substring(idx2+1);
						return content; // doc page
					}
					else if (content.cat.equals(CAT_LIBRARY)) {
						content.cat = CAT_LIBRARY;
						
						final int idx3 = path.indexOf('/', idx2+1); // package
						if (idx3 >= idx2+2) {
							content.packageName = path.substring(idx2+1, idx3);
							
							final int idx4 = path.indexOf('/', idx3+1); // command
							if (idx4 >= idx3+2) {
								content.command = path.substring(idx3+1, idx4);
								content.detail = path.substring(idx4+1);
								if (content.detail.endsWith(".html")) { //$NON-NLS-1$
									content.detail = content.detail.substring(0, content.detail.length()-5);
								}
								
								if (content.command.equals(COMMAND_HTML_PAGE)) {
									content.command = COMMAND_HTML_PAGE;
									return content; // help page
								}
								else if (content.command.equals(COMMAND_HELP_TOPIC)) {
									content.command = COMMAND_HELP_TOPIC;
									return content; // help topic search
								}
								else {
									content.command = null;
								}
							}
							else if (idx4 < 0) {
								content.command = COMMAND_IDX;
								return content; // package idx
							}
						}
					}
				}
				content.cat = null;
				return content;
			}
		}
		return null;
	}
	
}
