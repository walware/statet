/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.templates;

import org.eclipse.osgi.util.NLS;


public final class TemplateMessages extends NLS {

	private static final String BUNDLE_NAME = TemplateMessages.class.getName();


	public static String TemplateVariableProposal_error_title;


	static {
		NLS.initializeMessages(BUNDLE_NAME, TemplateMessages.class);
	}
}