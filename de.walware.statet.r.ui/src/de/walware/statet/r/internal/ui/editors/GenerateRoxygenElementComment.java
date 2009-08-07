/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.SourceDocumentRunnable;
import de.walware.ecommons.ltk.ui.LTKSelectionUtil;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditorAssociated;
import de.walware.ecommons.ui.util.WorkbenchUIUtil;

import de.walware.statet.ext.templates.TemplatesUtil;
import de.walware.statet.ext.templates.TemplatesUtil.EvaluatedTemplate;

import de.walware.statet.r.codegeneration.CodeGeneration;
import de.walware.statet.r.core.model.IRClass;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.refactoring.RRefactoring;
import de.walware.statet.r.core.rsource.RIndentUtil;
import de.walware.statet.r.internal.ui.RUIMessages;


public class GenerateRoxygenElementComment extends AbstractHandler implements IElementUpdater {
	
	
	public GenerateRoxygenElementComment() {
	}
	
	
	public void updateElement(final UIElement element, final Map parameters) {
		element.setText(RUIMessages.GenerateRoxygenElementComment_label);
	}
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (!(event.getApplicationContext() instanceof IEvaluationContext)) {
			return null;
		}
		
		final IProgressMonitor monitor = new NullProgressMonitor();
		final SubMonitor progress = SubMonitor.convert(monitor, 2);
		
		ISourceUnit su = null;
		ISourceStructElement[] elements = null;
		ISourceUnitModelInfo info = null;
		
		final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
		final ISelection selection = WorkbenchUIUtil.getCurrentSelection(context);
		final IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		final IWorkbenchPart part = (IWorkbenchPart) context.getVariable(ISources.ACTIVE_PART_NAME);
		final ISourceEditor editor;
		if (selection instanceof IStructuredSelection) {
			elements = LTKSelectionUtil.getSelectedSourceStructElements((IStructuredSelection) selection);
			if (elements != null && elements.length > 0) {
				su = elements[0].getSourceUnit();
				for (int i = 0; i < elements.length; i++) {
					if (elements[i].getSourceUnit() != su) {
						// should be filtered by enablement
						return null;
					}
				}
				info = su.getModelInfo(RModel.TYPE_ID, IModelManager.MODEL_FILE, progress.newChild(1));
				// update elements if necessary
			}
			final ISourceEditorAssociated associated = (ISourceEditorAssociated) part.getAdapter(ISourceEditorAssociated.class);
			editor = (associated != null) ? associated.getSourceEditor() : null;
		}
		else if (selection instanceof ITextSelection) {
			editor = (ISourceEditor) part.getAdapter(ISourceEditor.class);
			if (editor != null) {
				su = editor.getSourceUnit();
				if (su != null) {
					info = su.getModelInfo(RModel.TYPE_ID, IModelManager.MODEL_FILE, progress.newChild(1));
					elements = LTKSelectionUtil.getSelectedSourceStructElement(info, (ITextSelection) selection);
				}
			}
		}
		else {
			editor = null;
		}
		
		if (su == null || elements == null || elements.length == 0 || !(su instanceof IRSourceUnit)) {
			return null;
		}
		
		if (!su.checkState(true, new NullProgressMonitor())) {
			return false;
		}
		final IRSourceUnit rsu = (IRSourceUnit) su;
		try {
			final AbstractDocument doc = su.getDocument(null);
			final EvaluatedTemplate[] templates = new EvaluatedTemplate[elements.length];
			final String lineDelimiter = doc.getDefaultLineDelimiter();
			Arrays.sort(elements, RRefactoring.getFactory().createAdapter().getModelElementComparator());
			ITER_ELEMENTS: for (int i = 0; i < elements.length; i++) {
				switch (elements[i].getElementType() & IRElement.MASK_C1) {
				case IRElement.C1_CLASS:
					templates[i] = CodeGeneration.getClassRoxygenComment((IRClass) elements[i], lineDelimiter);
					continue ITER_ELEMENTS;
				case IRElement.C1_METHOD:
					switch (elements[i].getElementType() & IRElement.MASK_C1) {
					case IRElement.R_S4METHOD:
						templates[i] = CodeGeneration.getMethodRoxygenComment((IRMethod) elements[i], lineDelimiter);
						continue ITER_ELEMENTS;
					default:
						templates[i] = CodeGeneration.getCommonFunctionRoxygenComment((IRMethod) elements[i], lineDelimiter);
						continue ITER_ELEMENTS;
					}
				}
			}
			
			final RIndentUtil indentUtil = new RIndentUtil(doc, rsu.getRCoreAccess().getRCodeStyle());
			final MultiTextEdit multi = new MultiTextEdit();
			int selectionStart = 0;
			int selectionLength = 0;
			boolean first = true;
			for (int i = 0; i < elements.length; i++) {
				if (templates[i] == null) {
					continue;
				}
				final int line = doc.getLineOfOffset(elements[i].getSourceRange().getOffset());
				final String lineIndent = indentUtil.copyLineIndent(line);
				final AbstractDocument templateDoc = templates[i].startPostEdit();
				TemplatesUtil.indentTemplateDocument(templateDoc, lineIndent);
				templates[i].finishPostEdit();
				final int lineOffset = doc.getLineOffset(line);
				multi.addChild(new InsertEdit(lineOffset, templates[i].getContent()));
				
				// select offset for first comment
				if (first) {
					first = false;
					selectionStart = doc.getLineOffset(line);
					final IRegion templateSelection = templates[i].getRegionToSelect();
					if (templateSelection != null) {
						selectionStart += templateSelection.getOffset();
						selectionLength = templateSelection.getLength();
					}
					else {
						selectionStart += lineIndent.length();
					}
				}
			}
			
			if (first) {
				return null;
			}
			
			final IFile resource = (IFile) su.getResource();
			final IRegion initialSelection = new Region(selectionStart, selectionLength);
			su.syncExec(new SourceDocumentRunnable(doc, info.getStamp(), DocumentRewriteSessionType.SEQUENTIAL) {
				@Override
				public void run() throws InvocationTargetException {
					try {
						multi.apply(getDocument(), TextEdit.NONE);
						
						if (editor != null) {
							editor.getViewer().setSelectedRange(initialSelection.getOffset(), initialSelection.getLength());
						}
						else if (resource != null) {
							WorkbenchUIUtil.openEditor(workbenchWindow.getActivePage(), resource, initialSelection);
						}
					}
					catch (final MalformedTreeException e) {
						throw new InvocationTargetException(e);
					}
					catch (final BadLocationException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		}
		catch (final InvocationTargetException e) {
			throw new ExecutionException(RUIMessages.GenerateRoxygenElementComment_error_message, e.getCause());
		}
		catch (final BadLocationException e) {
			throw new ExecutionException(RUIMessages.GenerateRoxygenElementComment_error_message, e);
		}
		catch (final CoreException e) {
			throw new ExecutionException(RUIMessages.GenerateRoxygenElementComment_error_message, e);
		}
		
		return null;
	}
	
}
