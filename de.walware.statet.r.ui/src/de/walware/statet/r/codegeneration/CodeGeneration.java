/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.codegeneration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.templates.Template;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.ext.templates.TemplatesUtil;
import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.ui.internal.RUIPlugin;


/**
 * Class that offers access to the code templates contained.
 * 
 * @author Stephan Wahlbrink
 */
public class CodeGeneration {

	
	/**
	 * @param cu The code unit to create the source for. The unit does not need to exist.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getNewRFileContent(RResourceUnit cu, String lineDelimiter) throws CoreException {
		
		Template template = RUIPlugin.getDefault().getRCodeGenerationTemplateStore().findTemplate(RCodeTemplatesContextType.NEW_RSCRIPTFILE);
		if (template == null) {
			return null;
		}
		
		StatetProject project = cu.getStatetProject();
		RCodeTemplatesContext context = new RCodeTemplatesContext(RCodeTemplatesContextType.NEW_RSCIRPTFILE_CONTEXTTYPE, project, lineDelimiter);
		context.setCodeUnitVariables(cu);

		return TemplatesUtil.evaluateTemplate(context, template);
	}
	
	/**
	 * @param cu The code unit to create the source for. The unit does not need to exist.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException Thrown when the evaluation of the code template fails.
	 */
	public static String getNewRdFileContent(RResourceUnit cu, String lineDelimiter) throws CoreException {
		
		Template template = RUIPlugin.getDefault().getRdCodeGenerationTemplateStore().findTemplate(RdCodeTemplatesContextType.NEW_RDOCFILE);
		if (template == null) {
			return null;
		}
		
		StatetProject project = cu.getStatetProject();
		RdCodeTemplatesContext context = new RdCodeTemplatesContext(RdCodeTemplatesContextType.NEW_RDOCFILE_CONTEXTTYPE, project, lineDelimiter);
		context.setCodeUnitVariables(cu);

		return TemplatesUtil.evaluateTemplate(context, template);
	}
}
