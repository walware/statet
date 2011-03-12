/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.ecommons.ltk.ISourceElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.IWorkspaceSourceUnit;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.util.WorkbenchUIUtil;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstInfo;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.ui.RLabelProvider;


public class ROpenDeclarationHandler extends AbstractHandler {
	
	
	public static void open(final ISourceElement element, final boolean activate) throws PartInitException  {
		final ISourceUnit su = element.getSourceUnit();
		if (su instanceof IWorkspaceSourceUnit) {
			final IResource resource = ((IWorkspaceSourceUnit) su).getResource();
			if (resource.getType() == IResource.FILE) {
				open((IFile) resource, activate, element.getNameSourceRange());
				return;
			}
		}
	}
	
	public static void open(final IFile file, final boolean activate, final IRegion region) throws PartInitException  {
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
		final IEditorDescriptor editorDescriptor = IDE.getEditorDescriptor(file, true);
		final FileEditorInput input = new FileEditorInput(file);
		IEditorPart editorPart = page.findEditor(input);
		if (editorPart == null || !(editorPart instanceof ITextEditor)) {
			editorPart = page.openEditor(input, editorDescriptor.getId(), activate);
		}
		else if (activate) {
			page.activate(editorPart);
		}
		if (editorPart instanceof ITextEditor) {
			((ITextEditor) editorPart).selectAndReveal(region.getOffset(), region.getLength());
		}
	}
	
	public static RElementAccess searchAccess(final ISourceEditor editor, final IRegion region) {
		try {
			final IDocument document = editor.getViewer().getDocument();
			final ITypedRegion partition = TextUtilities.getPartition(document, IRDocumentPartitions.R_PARTITIONING, region.getOffset(), false);
			final ISourceUnit su = editor.getSourceUnit();
			if (su instanceof IRSourceUnit && region.getOffset() < document.getLength()
					&& ( (editor.getPartitioning().getDefaultPartitionConstraint().matches(partition.getType())
							&& !RTokens.isRobustSeparator(document.getChar(region.getOffset()), false) )
						|| partition.getType() == IRDocumentPartitions.R_QUOTED_SYMBOL
						|| partition.getType() == IRDocumentPartitions.R_STRING )) {
				
				final IRModelInfo info = (IRModelInfo) su.getModelInfo(RModel.TYPE_ID, IRModelManager.MODEL_FILE, new NullProgressMonitor());
				if (info != null) {
					final RAstInfo astInfo = info.getAst();
					final AstSelection selection = AstSelection.search(astInfo.root, region.getOffset(), region.getOffset()+region.getLength(), AstSelection.MODE_COVERING_SAME_LAST);
					final IAstNode covering = selection.getCovering();
					if (covering instanceof RAstNode) {
						final RAstNode node = (RAstNode) covering;
						if (node.getNodeType() == NodeType.SYMBOL || node.getNodeType() == NodeType.STRING_CONST) {
							RAstNode current = node;
							do {
								final Object[] attachments = current.getAttachments();
								for (int i = 0; i < attachments.length; i++) {
									if (attachments[i] instanceof RElementAccess) {
										final RElementAccess access = (RElementAccess) attachments[i];
										if (access.getNameNode() == node) {
											return access;
										}
									}
								}
								current = current.getRParent();
							} while (current != null);
						}
					}
				}
			}
		}
		catch (final BadLocationException e) {
		}
		return null;
	}
	
	
	private static class ElementSelectionDialog extends ListDialog {
		
		public ElementSelectionDialog(final Shell parent, final List<ISourceElement> list) {
			super(parent);
		}
		
	}
	public static ISourceElement selectElement(final List<ISourceElement> list, final IWorkbenchPart part) {
		if (list.isEmpty()) {
			return null;
		}
		else if (list.size() == 1) {
			return list.get(0);
		}
		else {
			final ListDialog dialog = new ListDialog(part != null ? part.getSite().getShell() : UIAccess.getActiveWorkbenchShell(true));
			dialog.setTitle("Open Declaration");
			dialog.setMessage("Select the appropriate declaration:");
			dialog.setHelpAvailable(false);
			dialog.setContentProvider(new ArrayContentProvider());
			dialog.setLabelProvider(new RLabelProvider(RLabelProvider.NAMESPACE));
			dialog.setInput(list);
			dialog.setInitialSelections(new Object[] { list.get(0) });
			
			if (dialog.open() == Dialog.OK) {
				return (ISourceElement) dialog.getResult()[0];
			}
			return null;
		}
	}
	
	
	public ROpenDeclarationHandler() {
	}
	
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart activePart = WorkbenchUIUtil.getActivePart(event.getApplicationContext());
		final ISourceEditor editor = (ISourceEditor) activePart.getAdapter(ISourceEditor.class);
		if (editor != null) {
			final ITextSelection selection = (ITextSelection) editor.getViewer().getSelection();
			final RElementAccess access = searchAccess(editor, new Region(selection.getOffset(), selection.getLength()));
			if (access != null) {
				try {
					final List<ISourceElement> list = RModel.searchDeclaration(access, (IRSourceUnit) editor.getSourceUnit());
					final ISourceElement element = selectElement(list, editor.getWorkbenchPart());
					if (element != null) {
						ROpenDeclarationHandler.open(element, true);
						return null;
					}
				}
				catch (final PartInitException e) {
					Display.getCurrent().beep();
					StatusManager.getManager().handle(new Status(IStatus.INFO, SharedUIResources.PLUGIN_ID, -1,
							NLS.bind("An error occurred when opening editor for the declaration of ''{0}''", access.getDisplayName()), e));
				}
			}
		}
		Display.getCurrent().beep();
		return null;
	}
	
}
