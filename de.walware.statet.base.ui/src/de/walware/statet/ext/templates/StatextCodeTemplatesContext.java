/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.templates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import de.walware.statet.base.core.StatetProject;


public class StatextCodeTemplatesContext extends TemplateContext implements IStatetContext {
	
	private String fLineDelimiter;
	private StatetProject fProject;
	Position fSelectionStart;
	Position fSelectionEnd;
	
	
	public StatextCodeTemplatesContext(
			final TemplateContextType contextType,
			final StatetProject project,
			final String lineDelim) {
		super(contextType);
		fLineDelimiter = lineDelim;
		fProject = project;
	}
	
	public StatetProject getStatetProject() {
		return fProject;
	}
	
	public String evaluateInfo(final Template template) throws BadLocationException, TemplateException {
		final TemplateBuffer buffer = evaluate(template);
		if (buffer != null) {
			buffer.getString();
		}
		return null;
	}
	
	@Override
	public TemplateBuffer evaluate(final Template template) throws BadLocationException, TemplateException {
		// test that all variables are defined
		final Iterator iterator = getContextType().resolvers();
		while (iterator.hasNext()) {
			final TemplateVariableResolver var = (TemplateVariableResolver) iterator.next();
			if (var instanceof StatextCodeTemplatesContextType.CodeTemplatesVariableResolver) {
				Assert.isNotNull(getVariable(var.getType()), "Variable " + var.getType() + "not defined"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		if (!canEvaluate(template))
			return null;
			
		final String pattern = changeLineDelimiter(template.getPattern(), fLineDelimiter);
		
		final TemplateTranslator translator= new TemplateTranslator();
		final TemplateBuffer buffer= translator.translate(pattern);
		getContextType().resolve(buffer, this);
		return buffer;
	}
	
	private static String changeLineDelimiter(final String code, final String lineDelim) {
		try {
			final ILineTracker tracker= new DefaultLineTracker();
			tracker.set(code);
			final int nLines= tracker.getNumberOfLines();
			if (nLines == 1) {
				return code;
			}
			
			final StringBuffer buf= new StringBuffer();
			for (int i= 0; i < nLines; i++) {
				if (i != 0) {
					buf.append(lineDelim);
				}
				final IRegion region = tracker.getLineInformation(i);
				final String line= code.substring(region.getOffset(), region.getOffset() + region.getLength());
				buf.append(line);
			}
			return buf.toString();
		} catch (final BadLocationException e) {
			// can not happen
			return code;
		}
	}
	
	@Override
	public boolean canEvaluate(final Template template) {
		return true;
	}
	
	public int getSelectionStart() {
		if (fSelectionStart != null) {
			return fSelectionStart.offset;
		}
		return -1;
	}
	
	public int getSelectionEnd() {
		if (fSelectionEnd != null) {
			return fSelectionEnd.offset;
		}
		return -1;
	}
	
	public final Position[] getPositions() {
		final List<Position> list = new ArrayList<Position>();
		addPositions(list);
		return list.toArray(new Position[list.size()]);
	}
	
	protected void addPositions(final List<Position> list) {
		if (fSelectionStart != null) {
			list.add(fSelectionStart);
		}
		if (fSelectionEnd != null) {
			list.add(fSelectionEnd);
		}
	}
	
}
