/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.templates.CodeGenerationTemplateContext;
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
	public static EvaluatedTemplate getNewSweaveDocContent(final ISourceUnit su, final String lineDelimiter,
			final Template template) throws CoreException {
		if (template == null) {
			return null;
		}
		
		final CodeGenerationTemplateContext context = new CodeGenerationTemplateContext(
				SweavePlugin.getDefault().getSweaveDocTemplateContextRegistry().getContextType(
						LtxRweaveTemplatesContextType.NEW_RWEAVETEX_CONTEXTTYPE ),
				lineDelimiter );
		
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
