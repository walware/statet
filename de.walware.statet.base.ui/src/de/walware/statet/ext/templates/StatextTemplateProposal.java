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

import java.util.Comparator;

import com.ibm.icu.text.Collator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;


public class StatextTemplateProposal extends TemplateProposal {
	
	public static class TemplateComparator implements Comparator<StatextTemplateProposal> {
		
		private final Collator fgCollator = Collator.getInstance();
		
		public int compare(final StatextTemplateProposal arg0, final StatextTemplateProposal arg1) {
			final int result = fgCollator.compare(arg0.getTemplate().getName(), arg1.getTemplate().getName());
			if (result != 0) {
				return result;
			}
			return fgCollator.compare(arg0.getDisplayString(), arg1.getDisplayString());
		}
		
	}
	
	
	public StatextTemplateProposal(final Template template, final TemplateContext context,
			final IRegion region, final Image image) {
		super(template, context, region, image);
	}
	
	public StatextTemplateProposal(final Template template, final TemplateContext context,
			final IRegion region, final Image image, final int relevance) {
		super(template, context, region, image, relevance);
	}
	
	
	@Override
	public String getAdditionalProposalInfo() {
		try {
			final TemplateContext context = getContext();
			context.setReadOnly(true);
			if (context instanceof IExtTemplateContext) {
				return ((IExtTemplateContext) context).evaluateInfo(getTemplate());
			}
				
			final TemplateBuffer templateBuffer = context.evaluate(getTemplate());
			if (templateBuffer != null) {
				return templateBuffer.toString();
			}
		}
		catch (final TemplateException e) { }
		catch (final BadLocationException e) { }
		return null;
	}
	
}
