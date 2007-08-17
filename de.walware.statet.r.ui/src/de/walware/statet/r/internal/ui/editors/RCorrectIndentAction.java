/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.IStatetUICommandIds;
import de.walware.statet.ext.ui.editors.IEditorAdapter;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.RSourceIndenter;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RDocumentProvider;
import de.walware.statet.r.ui.editors.REditor;


/**
 *
 */
public class RCorrectIndentAction extends Action implements IUpdate {

	
	private REditor fEditor;
	private IEditorAdapter fEditorAdapter;
	private RSourceIndenter fIndenter;
	
	
	public RCorrectIndentAction(REditor editor) {
		setId("de.walware.statet.r.actions.RCorrectIndent"); //$NON-NLS-1$
		setActionDefinitionId(IStatetUICommandIds.CORRECT_INDENT);
		fEditor = editor;
		fEditorAdapter = (IEditorAdapter) editor.getAdapter(IEditorAdapter.class);
	}
	
	
	public void update() {
		setEnabled(fEditorAdapter.isEditable(false));
	}
	
	@Override
	public void run() {
		if (!fEditorAdapter.isEditable(true)) {
			return;
		}
		try {
			final ITextSelection selection = (ITextSelection) fEditor.getSelectionProvider().getSelection();

			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						RDocumentProvider documentProvider = RUIPlugin.getDefault().getRDocumentProvider();
						IRSourceUnit unit = documentProvider.getWorkingCopy(fEditor.getEditorInput());
						doCorrection(unit, selection, monitor);
					}
					catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
					finally {
					}
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
	}
	
	private void doCorrection(IRSourceUnit unit, ITextSelection selection, IProgressMonitor monitor)
			throws CoreException {
		monitor.subTask("Updating document structure...");
		AbstractDocument document = unit.getDocument();
		AstInfo<RAstNode> ast = unit.getAstInfo(true, monitor);
		
		if (monitor.isCanceled()) {
			return;
		}
		
		monitor.subTask("Indenting lines...");
		
		if (fIndenter == null) {
			fIndenter = new RSourceIndenter();
		}
		int startLine = selection.getStartLine(); // save before change
//		if (length > 0 && fDocument.getLineOffset(fLastLine) == start+length) {
//			fLastLine--;
//		}
		fIndenter.indent(document, ast, startLine, selection.getEndLine(),
				fEditor.getRCoreAccess(), unit.getWorkingContext());
		if (selection.getLength() == 0) {
			final int newPos = fIndenter.getNewIndentOffset(startLine);
			if (newPos >= 0) {
				UIAccess.getDisplay().syncExec(new Runnable() {
					public void run() {
						if (UIAccess.isOkToUse(fEditorAdapter.getSourceViewer())) {
							fEditor.selectAndReveal(newPos, 0);
						}
					}
				});
			}
		}
	}
	
}
