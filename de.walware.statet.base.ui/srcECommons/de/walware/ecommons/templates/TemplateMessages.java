/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.templates;

import org.eclipse.osgi.util.NLS;


public final class TemplateMessages extends NLS {
	
	
	public static String TemplateVariableProposal_error_title;
	
	public static String TemplateEvaluation_error_description;
	
	
	static {
		NLS.initializeMessages(TemplateMessages.class.getName(), TemplateMessages.class);
	}
	private TemplateMessages() {}
	
}
