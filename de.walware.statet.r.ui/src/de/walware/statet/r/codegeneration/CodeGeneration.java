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

package de.walware.statet.r.codegeneration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.osgi.util.NLS;

import de.walware.eclipsecommons.templates.TemplateMessages;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.core.StatetProject;
import de.walware.statet.ext.templates.TemplatesUtil;
import de.walware.statet.ext.templates.TemplatesUtil.EvaluatedTemplate;

import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


/**
 * Class that offers access to the code templates contained.
 */
public class CodeGeneration {
	
	
	/**
	 * Generates initial content for a new R script file.
	 * 
	 * @param su the R source unit to create the source for. The unit does not need to exist.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException thrown when the evaluation of the code template fails.
	 */
	public static EvaluatedTemplate getNewRFileContent(final RResourceUnit su, final String lineDelimiter) throws CoreException {
		final Template template = RUIPlugin.getDefault().getRCodeGenerationTemplateStore().findTemplate(RCodeTemplatesContextType.NEW_RSCRIPTFILE);
		if (template == null) {
			return null;
		}
		
		final StatetProject project = StatetCore.getStatetProject(su);
		final RCodeTemplatesContext context = new RCodeTemplatesContext(
				RCodeTemplatesContextType.NEW_RSCRIPTFILE_CONTEXTTYPE, project, lineDelimiter);
		context.setCodeUnitVariables(su);
		
		try {
			final TemplateBuffer buffer = context.evaluate(template);
			if (buffer == null) {
				return null;
			}
			return new TemplatesUtil.EvaluatedTemplate(buffer);
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, NLS.bind(
					TemplateMessages.TemplateEvaluation_error_description, template.getDescription()), e));
		}
	}
	
	/**
	 * Generates initial content for a new Rd file.
	 * 
	 * @param su the Rd source unit to create the source for. The unit does not need to exist.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException thrown when the evaluation of the code template fails.
	 */
	public static EvaluatedTemplate getNewRdFileContent(final RResourceUnit su, final String lineDelimiter) throws CoreException {
		final Template template = RUIPlugin.getDefault().getRdCodeGenerationTemplateStore().findTemplate(RdCodeTemplatesContextType.NEW_RDOCFILE);
		if (template == null) {
			return null;
		}
		
		final StatetProject project = StatetCore.getStatetProject(su);
		final RdCodeTemplatesContext context = new RdCodeTemplatesContext(
				RdCodeTemplatesContextType.NEW_RDOCFILE_CONTEXTTYPE, project, lineDelimiter);
		context.setCodeUnitVariables(su);
		
		try {
			final TemplateBuffer buffer = context.evaluate(template);
			if (buffer == null) {
				return null;
			}
			return new TemplatesUtil.EvaluatedTemplate(buffer);
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, NLS.bind(
					TemplateMessages.TemplateEvaluation_error_description, template.getDescription()), e));
		}
	}
	
}
