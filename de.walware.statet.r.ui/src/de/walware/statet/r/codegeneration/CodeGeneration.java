/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.codegeneration;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.templates.TemplatesUtil;
import de.walware.ecommons.ltk.ui.templates.TemplatesUtil.EvaluatedTemplate;
import de.walware.ecommons.templates.TemplateMessages;

import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRClass;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRSlot;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


/**
 * Class that offers access to the code templates contained.
 */
public class CodeGeneration {
	
	
	/**
	 * Generates initial content for a new R script file.
	 * 
	 * @param su the R source unit to create the source for. The unit does not need to exist
	 * @param lineDelimiter the line delimiter to be used
	 * @return the new content or <code>null</code> if the template is undefined or empty
	 * @throws CoreException thrown when the evaluation of the code template fails
	 */
	public static EvaluatedTemplate getNewRFileContent(final RResourceUnit su, final String lineDelimiter) throws CoreException {
		final Template template = RUIPlugin.getDefault().getRCodeGenerationTemplateStore().findTemplate(RCodeTemplatesContextType.NEW_RSCRIPTFILE);
		if (template == null) {
			return null;
		}
		
		final RCodeTemplatesContext context = new RCodeTemplatesContext(
				RCodeTemplatesContextType.NEW_RSCRIPTFILE_CONTEXTTYPE, su, lineDelimiter);
		
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
	
	/**
	 * Generates content for the Roxygen comment for the given function definition
	 * @param rMethod function element
	 * @param lineDelimiter the line delimiter to be used
	 * @return 
	 * @throws CoreException thrown when the evaluation of the code template fails
	 */
	public static EvaluatedTemplate getCommonFunctionRoxygenComment(final IRMethod rMethod, final String lineDelimiter) throws CoreException {
		final Template template = RUIPlugin.getDefault().getRCodeGenerationTemplateStore().findTemplate(RCodeTemplatesContextType.ROXYGEN_COMMONFUNCTION_TEMPLATE);
		if (template == null) {
			return null;
		}
		
		final ISourceUnit su = rMethod.getSourceUnit();
		final RCodeTemplatesContext context = new RCodeTemplatesContext(
				RCodeTemplatesContextType.ROXYGEN_COMMONFUNCTION_CONTEXTTYPE, su, lineDelimiter);
		context.setRElement(rMethod);
		
		try {
			final TemplateBuffer buffer = context.evaluate(template);
			if (buffer == null) {
				return null;
			}
			final EvaluatedTemplate data = new EvaluatedTemplate(buffer, lineDelimiter);
			
			final AbstractDocument content = data.startPostEdit();
			final StringBuilder tagBuffer = new StringBuilder(64);
			final TemplateVariable paramVariable = TemplatesUtil.findVariable(buffer, RCodeTemplatesContextType.ROXYGEN_PARAM_TAGS_VARIABLE);
			final Position[] paramPositions = new Position[(paramVariable != null) ? paramVariable.getOffsets().length : 0];
			for (int i = 0; i < paramPositions.length; i++) {
				paramPositions[i] = new Position(paramVariable.getOffsets()[i], paramVariable.getLength());
				content.addPosition(paramPositions[i]);
			}
			
			if (paramPositions.length > 0) {
				String[] tags = null;
				final ArgsDefinition args = rMethod.getArgsDefinition();
				if (args != null) {
					final int count = args.size();
					tags = new String[count];
					for (int i = 0; i < count; i++) {
						tagBuffer.append("@param "); //$NON-NLS-1$
						tagBuffer.append(args.get(i).name);
						tagBuffer.append(" "); //$NON-NLS-1$
						tags[i] = tagBuffer.toString();
						tagBuffer.setLength(0);
					}
				}
				for (final Position pos : paramPositions) {
					insertRoxygen(content, pos, tags);
				}
			}
			
			data.finishPostEdit();
			return data;
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, NLS.bind(
					TemplateMessages.TemplateEvaluation_error_description, template.getDescription()), e));
		}
	}
	
	/**
	 * Generates content for the Roxygen comment for the given class definition
	 * @param rClass class element
	 * @param lineDelimiter the line delimiter to be used
	 * @return 
	 * @throws CoreException thrown when the evaluation of the code template fails
	 */
	public static EvaluatedTemplate getClassRoxygenComment(final IRClass rClass, final String lineDelimiter) throws CoreException {
		final Template template = RUIPlugin.getDefault().getRCodeGenerationTemplateStore().findTemplate(RCodeTemplatesContextType.ROXYGEN_S4CLASS_TEMPLATE);
		if (template == null) {
			return null;
		}
		
		final ISourceUnit su = rClass.getSourceUnit();
		final RCodeTemplatesContext context = new RCodeTemplatesContext(
				RCodeTemplatesContextType.ROXYGEN_CLASS_CONTEXTTYPE, su, lineDelimiter);
		context.setRElement(rClass);
		
		try {
			final TemplateBuffer buffer = context.evaluate(template);
			if (buffer == null) {
				return null;
			}
			final EvaluatedTemplate data = new EvaluatedTemplate(buffer, lineDelimiter);
			
			final AbstractDocument content = data.startPostEdit();
			final StringBuilder tagBuffer = new StringBuilder(64);
			final TemplateVariable slotVariable = TemplatesUtil.findVariable(buffer, RCodeTemplatesContextType.ROXYGEN_SLOT_TAGS_VARIABLE);
			final Position[] slotPositions = new Position[(slotVariable != null) ? slotVariable.getOffsets().length : 0];
			for (int i = 0; i < slotPositions.length; i++) {
				slotPositions[i] = new Position(slotVariable.getOffsets()[i], slotVariable.getLength());
				content.addPosition(slotPositions[i]);
			}
			
			if (slotPositions.length > 0) {
				String[] tags = null;
				final List<? extends IModelElement> slots = rClass.getModelChildren(IRElement.R_S4SLOT_FILTER);
				final int count = slots.size();
				tags = new String[count];
				for (int i = 0; i < count; i++) {
					final IRSlot slot = (IRSlot) slots.get(i);
					tagBuffer.append("@slot "); //$NON-NLS-1$
					tagBuffer.append(slot.getElementName().getDisplayName());
					tagBuffer.append(" "); //$NON-NLS-1$
					tags[i] = tagBuffer.toString();
					tagBuffer.setLength(0);
				}
				for (final Position pos : slotPositions) {
					insertRoxygen(content, pos, tags);
				}
			}
			
			data.finishPostEdit();
			return data;
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, NLS.bind(
					TemplateMessages.TemplateEvaluation_error_description, template.getDescription()), e));
		}
	}
	
	/**
	 * Generates content for the Roxygen comment for the given method definition
	 * @param rMethod function element
	 * @param lineDelimiter the line delimiter to be used
	 * @return 
	 * @throws CoreException thrown when the evaluation of the code template fails
	 */
	public static EvaluatedTemplate getMethodRoxygenComment(final IRMethod rMethod, final String lineDelimiter) throws CoreException {
		final Template template = RUIPlugin.getDefault().getRCodeGenerationTemplateStore().findTemplate(RCodeTemplatesContextType.ROXYGEN_S4METHOD_TEMPLATE);
		if (template == null) {
			return null;
		}
		
		final ISourceUnit su = rMethod.getSourceUnit();
		final RCodeTemplatesContext context = new RCodeTemplatesContext(
				RCodeTemplatesContextType.ROXYGEN_METHOD_CONTEXTTYPE, su, lineDelimiter);
		context.setRElement(rMethod);
		
		try {
			final TemplateBuffer buffer = context.evaluate(template);
			if (buffer == null) {
				return null;
			}
			final EvaluatedTemplate data = new EvaluatedTemplate(buffer, lineDelimiter);
			
			final AbstractDocument content = data.startPostEdit();
			final StringBuilder tagBuffer = new StringBuilder(64);
			final TemplateVariable paramVariable = TemplatesUtil.findVariable(buffer, RCodeTemplatesContextType.ROXYGEN_PARAM_TAGS_VARIABLE);
			final Position[] paramPositions = new Position[(paramVariable != null) ? paramVariable.getOffsets().length : 0];
			for (int i = 0; i < paramPositions.length; i++) {
				paramPositions[i] = new Position(paramVariable.getOffsets()[i], paramVariable.getLength());
				content.addPosition(paramPositions[i]);
			}
			
			if (paramPositions.length > 0) {
				String[] tags = null;
				final ArgsDefinition args = rMethod.getArgsDefinition();
				if (args != null) {
					final int count = args.size();
					tags = new String[count];
					for (int i = 0; i < count; i++) {
						tagBuffer.append("@param "); //$NON-NLS-1$
						tagBuffer.append(args.get(i).name);
						tagBuffer.append(" "); //$NON-NLS-1$
						tags[i] = tagBuffer.toString();
						tagBuffer.setLength(0);
					}
				}
				for (final Position pos : paramPositions) {
					insertRoxygen(content, pos, tags);
				}
			}
			
			data.finishPostEdit();
			return data;
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, NLS.bind(
					TemplateMessages.TemplateEvaluation_error_description, template.getDescription()), e));
		}
	}
	
	/**
	 * Generates initial content for a new Rd file.
	 * 
	 * @param su the Rd source unit to create the source for. The unit does not need to exist
	 * @param lineDelimiter the line delimiter to be used
	 * @return the new content or <code>null</code> if the template is undefined or empty
	 * @throws CoreException thrown when the evaluation of the code template fails
	 */
	public static EvaluatedTemplate getNewRdFileContent(final RResourceUnit su, final String lineDelimiter) throws CoreException {
		final Template template = RUIPlugin.getDefault().getRdCodeGenerationTemplateStore().findTemplate(RdCodeTemplatesContextType.NEW_RDOCFILE);
		if (template == null) {
			return null;
		}
		
		final RdCodeTemplatesContext context = new RdCodeTemplatesContext(
				RdCodeTemplatesContextType.NEW_RDOCFILE_CONTEXTTYPE, su, lineDelimiter);
		
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
	
	private static void insertRoxygen(final AbstractDocument doc, final Position pos, final String[] tags) throws BadLocationException {
		final int line = doc.getLineOfOffset(pos.getOffset());
		final int lineOffset = doc.getLineOffset(line);
		final int lineLength = doc.getLineLength(line);
		
		final String orgLine = doc.get(lineOffset, lineLength);
		final String prefix = orgLine.substring(0, pos.getOffset()-lineOffset); // can be replaced by more intelligent search
		if (tags == null) {
			return;
		}
		if (tags.length == 0) {
			if (onlyWhitespace(orgLine.substring(prefix.length(), pos.getOffset()-lineOffset))
					&& onlyWhitespace(orgLine.substring(pos.getOffset()-lineOffset+pos.getLength()))) {
				doc.replace(lineOffset, lineLength, ""); //$NON-NLS-1$
				return;
			}
			else {
				doc.replace(pos.getOffset(), pos.getLength(), ""); //$NON-NLS-1$
				return;
			}
		}
		final StringBuilder sb = new StringBuilder(tags.length * 16);
		sb.append(tags[0]);
		for (int i = 1; i < tags.length; i++) {
			sb.append(doc.getDefaultLineDelimiter());
			sb.append(prefix);
			sb.append(tags[i]);
		}
		doc.replace(pos.getOffset(), pos.getLength(), sb.toString());
	}
	
	private static boolean onlyWhitespace(final String s) {
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(0);
			if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
				return false;
			}
		}
		return true;
	}
	
	
	private CodeGeneration() {}
	
}
