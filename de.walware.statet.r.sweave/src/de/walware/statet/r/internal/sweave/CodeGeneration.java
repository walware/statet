/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.templates.TemplatesUtil;
import de.walware.ecommons.ltk.ui.templates.TemplatesUtil.EvaluatedTemplate;
import de.walware.ecommons.templates.TemplateMessages;

import de.walware.statet.r.ui.RUI;


/**
 * Class that offers access to the code templates contained.
 */
public class CodeGeneration {
	
	
	/**
	 * Generates initial content for a new Sweave (R/LaTeX) script file.
	 * 
	 * @param su the source unit to create the source for. The unit does not need to exist.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException thrown when the evaluation of the code template fails.
	 */
	public static EvaluatedTemplate getNewRweaveTexDocContent(final ISourceUnit su, final String lineDelimiter) throws CoreException {
		final Template template = SweavePlugin.getDefault().getRweaveTexGenerationTemplateStore().findTemplateById(RweaveTexTemplatesContextType.NEW_SWEAVEDOC_ID);
		if (template == null) {
			return null;
		}
		
		final RweaveTexTemplatesContext context = new RweaveTexTemplatesContext(
				RweaveTexTemplatesContextType.NEW_RWEAVETEX_CONTEXTTYPE, su, lineDelimiter);
		
		try {
			final TemplateBuffer buffer = context.evaluate(template);
			if (buffer == null) {
				return null;
			}
			return new TemplatesUtil.EvaluatedTemplate(buffer, lineDelimiter);
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, NLS.bind(
					TemplateMessages.TemplateEvaluation_error_description, template.getDescription()), e));
		}
	}
	
}
