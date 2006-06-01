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

package de.walware.statet.r.ui.editors.templates;

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


public class RTemplateProposal extends TemplateProposal {
	
	public static class RTemplateComparator implements Comparator<RTemplateProposal> {
		
		private final Collator fgCollator = Collator.getInstance();
		
		public int compare(RTemplateProposal arg0, RTemplateProposal arg1) {
			
			int result = fgCollator.compare(arg0.getTemplate().getName(), arg1.getTemplate().getName());
			if (result != 0)
				return result; 
			return fgCollator.compare(arg0.getDisplayString(), arg1.getDisplayString());
		}
	}
	

	public RTemplateProposal(Template template, TemplateContext context,
			IRegion region, Image image) {
		super(template, context, region, image);
	}

	public RTemplateProposal(Template template, TemplateContext context,
			IRegion region, Image image, int relevance) {
		super(template, context, region, image, relevance);
	}

	@Override
	public String getAdditionalProposalInfo() {
	    try {
	    	TemplateContext context = getContext();
		    context.setReadOnly(true);
			if (context instanceof REditorContext)
				return ((REditorContext) context).getInfo(getTemplate());
				
			TemplateBuffer templateBuffer = context.evaluate(getTemplate());
			if (templateBuffer != null)
				return templateBuffer.toString();
		} 
	    catch (TemplateException e) { }
	    catch (BadLocationException e) { }
	    
	    return null;
	}
}
