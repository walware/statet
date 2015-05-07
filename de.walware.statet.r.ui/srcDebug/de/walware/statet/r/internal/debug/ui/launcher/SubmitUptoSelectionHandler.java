/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import de.walware.ecommons.ltk.core.model.ISourceStructElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.ui.util.LTKSelectionUtil;
import de.walware.ecommons.ltk.ui.util.LTKWorkbenchUIUtil;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.core.refactoring.RRefactoringAdapter;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.ICodeSubmitContentHandler;
import de.walware.statet.r.launching.RCodeLaunching;


/**
 * Launch shortcut, which submits 
 *  - the current line/selection directly to R (text editor)
 *  - code of selected model element (outline etc.)
 * and does not change the focus by default.
 * 
 * Low requirements: ITextSelection is sufficient
 */
public class SubmitUptoSelectionHandler extends AbstractHandler implements IElementUpdater {
	
	
	private final boolean fGotoConsole;
	private RRefactoringAdapter fModelUtil;
	
	
	public SubmitUptoSelectionHandler() {
		this(false); 
	}
	
	protected SubmitUptoSelectionHandler(final boolean gotoConsole) {
		fGotoConsole = gotoConsole;
	}
	
	
	@Override
	public void updateElement(final UIElement element, final Map parameters) {
//		element.setText(appendVariant(RLaunchingMessages.SubmitCode_UptoElementSelection_label));
	}
	
	protected String appendVariant(final String label) {
		return label;
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		
		try {
			final IProgressMonitor progress = null;
			
			// text selection
			if (selection instanceof ITextSelection) {
				final ITextSelection textSelection = (ITextSelection) selection;
				final IWorkbenchPart part = HandlerUtil.getActivePart(event);
				List<String> lines = null;
				
				final AbstractDocument document = LTKWorkbenchUIUtil.getDocument(part);
				if (document != null) {
					final ICodeSubmitContentHandler contentHandler = RCodeLaunching
							.getCodeSubmitContentHandler(LTKWorkbenchUIUtil.getContentTypeId(part));
					lines = contentHandler.getCodeLines(document, 0, textSelection.getOffset());
				}
				else {
					lines = LaunchShortcutUtil.getSelectedCodeLines(event);
				}
				if (lines != null) {
					RCodeLaunching.runRCodeDirect(lines, fGotoConsole, progress);
				}
				return null;
			}
			else
			// selection of model elements
			if (selection instanceof IStructuredSelection) {
				final ISourceStructElement[] elements = LTKSelectionUtil.getSelectedSourceStructElements(selection);
				if (elements != null && elements.length == 1) {
					final List<String> lines = getCodeLinesUpto(elements[0], progress);
					
					RCodeLaunching.runRCodeDirect(lines, fGotoConsole, progress);
					return null;
				}
			}
		}
		catch (final Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RSelectionLaunch_error_message, event);
			return null;
		}
		
		LaunchShortcutUtil.handleUnsupportedExecution(event);
		return null;
	}
	
	protected List<String> getCodeLinesUpto(final ISourceStructElement element, final IProgressMonitor monitor)
			throws BadLocationException, BadPartitioningException, CoreException {
		final ISourceUnit su = element.getSourceUnit();
		if (su == null) {
			return null;
		}
		if (fModelUtil == null) {
			fModelUtil = new RSourceCodeAdapter();
		}
		
		su.connect(monitor);
		try {
			final AbstractDocument document = su.getDocument(monitor);
			final RHeuristicTokenScanner scanner = fModelUtil.getScanner(su);
			final IRegion range = fModelUtil.expandElementRange(element, document, scanner);
			
			if (range == null) {
				return null;
			}
			
			final ICodeSubmitContentHandler contentHandler = RCodeLaunching
					.getCodeSubmitContentHandler(su.getContentTypeId() );
			return contentHandler.getCodeLines(document, 0, range.getOffset());
		}
		finally {
			su.disconnect(monitor);
		}
	}
	
}
