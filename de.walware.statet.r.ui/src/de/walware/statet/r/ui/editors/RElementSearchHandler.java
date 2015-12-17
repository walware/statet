/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.ui.LTKUI;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.util.LTKWorkbenchUIUtil;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.refactoring.RElementSearchProcessor;
import de.walware.statet.r.core.refactoring.RElementSearchProcessor.Mode;
import de.walware.statet.r.core.refactoring.RRefactoringAdapter;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.ui.search.RElementSearch;
import de.walware.statet.r.internal.ui.search.RElementSearchQuery;
import de.walware.statet.r.ui.RUI;


public class RElementSearchHandler extends AbstractHandler implements IExecutableExtension {
	
	
	public static RElementSearchProcessor.Mode parScope2Mode(final String par) {
		if (par == LTKUI.SEARCH_SCOPE_WORKSPACE_PARAMETER_VALUE) {
			return Mode.WORKSPACE;
		}
		if (par == LTKUI.SEARCH_SCOPE_PROJECT_PARAMETER_VALUE) {
			return Mode.CURRENT_PROJECT;
		}
		if (par == LTKUI.SEARCH_SCOPE_FILE_PARAMETER_VALUE) {
			return Mode.CURRENT_FILE;
		}
		return null;
	}
	
	
	private String commandId;
	
	private final RRefactoringAdapter ltkAdapter= new RRefactoringAdapter();
	
	
	public RElementSearchHandler(final String commandId) {
		this.commandId= commandId;
	}
	
	public RElementSearchHandler() {
	}
	
	
	@Override
	public void setInitializationData(final IConfigurationElement config,
			final String propertyName, final Object data) throws CoreException {
		if (this.commandId != null) {
			return;
		}
		final String s= config.getAttribute("commandId"); //$NON-NLS-1$
		if (s != null && !s.isEmpty()) {
			this.commandId= s.intern();
		}
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part= HandlerUtil.getActivePart(event);
		final Mode mode= parScope2Mode(event.getParameter(LTKUI.SEARCH_SCOPE_PARAMETER_NAME));
		if (part == null || mode == null) {
			return null;
		}
		final ISelection selection= WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		IStatus status= null;
		if (selection instanceof ITextSelection) {
			final ITextSelection textSelection= (ITextSelection) selection;
			final ISourceEditor editor= (ISourceEditor) part.getAdapter(ISourceEditor.class);
			if (editor != null) {
				final ISourceUnit su= editor.getSourceUnit();
				if (su instanceof IRSourceUnit) {
					final IProgressMonitor monitor= new NullProgressMonitor();
					
					final RAstNode node= this.ltkAdapter.searchPotentialNameNode(su,
							new Region(textSelection.getOffset(), textSelection.getLength()),
							false, monitor );
					if (node != null) {
						final RElementAccess mainAccess= RElementAccess.getMainElementAccessOfNameNode(node);
						final RElementAccess subAccess= RElementAccess.getElementAccessOfNameNode(node);
						if (mainAccess != null && subAccess != null) {
							final RElementName name= RElementName.create(mainAccess, subAccess.getNextSegment(), false);
							status= startSearch(name, (IRSourceUnit) su, mainAccess, mode);
						}
					}
				}
			}
		}
		
		if (status == null) {
			status= new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					"The operation is unavailable on the current selection." );
		}
		if (status.getSeverity() == IStatus.ERROR) {
			LTKWorkbenchUIUtil.indicateStatus(status, event);
		}
		return status;
	}
	
	
	protected IStatus startSearch(final RElementName name,
			final IRSourceUnit sourceUnit, final RElementAccess mainAccess,
			final Mode mode) {
		final RElementSearch searchProcessor= new RElementSearch(name, sourceUnit, mainAccess,
				mode, (this.commandId == LTKUI.SEARCH_WRITE_ELEMENT_ACCESS_COMMAND_ID) );
		if (searchProcessor.getStatus().getSeverity() >= IStatus.ERROR
				|| searchProcessor.getMode() == Mode.LOCAL_FRAME) {
			return null; // default error message
		}
		if (searchProcessor.getMode() != mode && searchProcessor.getMode() == Mode.CURRENT_FILE) {
			return new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					"The search scope is not available for the current selection." );
		}
		final RElementSearchQuery query= new RElementSearchQuery(searchProcessor);
		NewSearchUI.runQueryInBackground(query);
		return Status.OK_STATUS;
	}
	
}
