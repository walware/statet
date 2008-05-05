/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.templates;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.base.ui.sourceeditors.StatextEditor1;


/**
 * Can extend {@link org.eclipse.jface.text.templates.TemplateContext} 
 * and similar contexts.
 */
public interface IExtTemplateContext {
	
	
	public StatetProject getStatetProject();
	
	public StatextEditor1 getEditor();
	
	public String evaluateInfo(Template template) throws BadLocationException, TemplateException;
	
}
