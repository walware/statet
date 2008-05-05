/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;

import de.walware.eclipsecommons.ltk.ISourceUnit;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.ext.templates.StatextCodeTemplatesContext;


public class RweaveTexTemplatesContext extends StatextCodeTemplatesContext {
	
	
	public RweaveTexTemplatesContext(final String contextTypeName, final StatetProject project, final String lineDelim) {
		super(
				SweavePlugin.getDefault().getRweaveTexGenerationTemplateContextRegistry().getContextType(contextTypeName),
				project,
				lineDelim);
	}
	
	
	@Override
	public TemplateBuffer evaluate(final Template template) throws BadLocationException, TemplateException {
		return super.evaluate(template);
	}
	
	public void setCodeUnitVariables(final ISourceUnit u) {
		setVariable(RweaveTexTemplatesContextType.FILENAME, u.getElementName());
	}
	
}
