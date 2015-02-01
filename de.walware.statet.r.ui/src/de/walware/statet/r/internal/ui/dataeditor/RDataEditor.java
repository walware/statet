/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.dataeditor;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.base.ui.contentfilter.IFilterPage;

import de.walware.statet.r.internal.ui.datafilterview.RDataFilterPage;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.dataeditor.IRDataEditorInput;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.RDataTableComposite;
import de.walware.statet.r.ui.dataeditor.RDataTableSelection;
import de.walware.statet.r.ui.dataeditor.RLiveDataEditorInput;


public class RDataEditor extends EditorPart { // INavigationLocationProvider ?
	
	
	private class ActivationListener implements IPartListener {
		
		@Override
		public void partActivated(final IWorkbenchPart part) {
			if (part == RDataEditor.this) {
				updateStatusLine();
			}
		}
		
		@Override
		public void partBroughtToTop(final IWorkbenchPart part) {
		}
		
		@Override
		public void partClosed(final IWorkbenchPart part) {
		}
		
		@Override
		public void partOpened(final IWorkbenchPart part) {
		}
		
		@Override
		public void partDeactivated(final IWorkbenchPart part) {
		}
		
	}
	
	
	private RDataTableComposite fTable;
	
	private final HandlerCollection fEditorHandlers = new HandlerCollection();
	
	private final ActivationListener fActivationListener = new ActivationListener();
	
	private RDataEditorOutlinePage fOutlinePage;
	
	private RDataFilterPage fFilterPage;
	
	private final IRDataTableInput.StateListener fInputStateListener = new IRDataTableInput.StateListener() {
		@Override
		public void tableUnavailable() {
			close(false);
		}
	};
	
	
	public RDataEditor() {
	}
	
	
	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		setSite(site);
		
		try {
			doSetInput(input);
		}
		catch (final CoreException e) {
			throw new PartInitException("The R data editor could not be initialized.");
		}
	}
	
	@Override
	public void dispose() {
		getEditorSite().getWorkbenchWindow().getPartService().removePartListener(fActivationListener);
		fEditorHandlers.dispose();
		
		disposeTableInput();
		
		super.dispose();
	}
	
	
	@Override
	protected void setInput(final IEditorInput input) {
		setInputWithNotify(input);
	}
	
	@Override
	protected void setInputWithNotify(final IEditorInput input) {
		try {
			doSetInput(input);
			firePropertyChange(PROP_INPUT);
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(
					IStatus.ERROR, RUI.PLUGIN_ID, "An error occurred when opening the element.", e));
		}
	}
	
	protected void doSetInput(final IEditorInput input) throws CoreException {
		if (input == null) {
			throw new NullPointerException("input");
		}
		if (!(input instanceof RLiveDataEditorInput)) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					NLS.bind("The element ''{0}'' is not supported by the R data editor.", input.getName()), null));
		}
		
		super.setInput(input);
		
		setPartName(input.getName());
		setTitleToolTip(input.getToolTipText());
	}
	
	public RDataTableComposite getRDataTable() {
		return fTable;
	}
	
	
	@Override
	public boolean isDirty() {
		return false;
	}
	
	@Override
	public void doSave(final IProgressMonitor monitor) {
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void doSaveAs() {
	}
	
	
	@Override
	public void createPartControl(final Composite parent) {
		fTable = new RDataTableComposite(parent, new Runnable() {
			@Override
			public void run() {
				close(false);
			}
		});
		getEditorSite().getWorkbenchWindow().getPartService().addPartListener(fActivationListener);
		
		initActions(getSite(), fEditorHandlers);
		
		initStatusLine();
		initTableInput();
	}
	
	private void initTableInput() {
		final IRDataEditorInput editorInput = (IRDataEditorInput) getEditorInput();
		if (editorInput != null) {
			final IRDataTableInput tableInput = editorInput.getRDataTableInput();
			if (tableInput != null) {
				tableInput.addStateListener(fInputStateListener);
				if (tableInput.isAvailable()) {
					fTable.setInput(tableInput);
				}
				else {
					close(false);
				}
			}
		}
	}
	
	private void disposeTableInput() {
		final IRDataEditorInput editorInput = (IRDataEditorInput) getEditorInput();
		if (editorInput != null) {
			final IRDataTableInput tableInput = editorInput.getRDataTableInput();
			if (tableInput != null) {
				tableInput.removeStateListener(fInputStateListener);
			}
		}
	}
	
	protected void initActions(final IServiceLocator serviceLocator, final HandlerCollection handlers) {
		WorkbenchUIUtil.activateContext(serviceLocator, "de.walware.statet.r.contexts.RDataEditor"); //$NON-NLS-1$
		
		final IHandlerService handlerService = (IHandlerService) serviceLocator.getService(IHandlerService.class);
		
		{	final IHandler2 handler = new RefreshHandler(fTable);
			handlers.add(IWorkbenchCommandConstants.FILE_REFRESH, handler);
			handlerService.activateHandler(IWorkbenchCommandConstants.FILE_REFRESH, handler);
		}
		{	final IHandler2 handler = new SelectAllHandler(fTable);
			handlers.add(IWorkbenchCommandConstants.EDIT_SELECT_ALL, handler);
			handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_SELECT_ALL, handler);
		}
//		{	final IHandler2 handler = new CopyDataHandler(fTable);
//			handlers.add(IWorkbenchCommandConstants.EDIT_COPY, handler);
//			handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_COPY, handler);
//		}
		{	final IHandler2 handler = new FindDialogHandler(this);
			handlers.add(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE, handler);
			handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE, handler);
		}
		{	final IHandler2 handler = new GotoCellHandler(fTable);
			handlers.add(ITextEditorActionDefinitionIds.LINE_GOTO, handler);
			handlerService.activateHandler(ITextEditorActionDefinitionIds.LINE_GOTO, handler);
		}
	}
	
	protected void initStatusLine() {
		fTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStatusLineManager manager = getEditorSite().getActionBars().getStatusLineManager();
				final RDataTableSelection selection = (RDataTableSelection) event.getSelection();
				updateStatusLine();
				if (selection.getAnchorRowLabel() != null) {
					final StringBuilder sb = new StringBuilder();
					sb.append(selection.getAnchorRowLabel());
					if (!selection.getAnchorRowLabel().isEmpty()
							&& !selection.getAnchorColumnLabel().isEmpty()) {
						sb.append(", ");
					}
					sb.append(selection.getAnchorColumnLabel());
					if (selection.getLastSelectedCellRowLabel() != null) {
						sb.append(" (");
						sb.append(selection.getLastSelectedCellRowLabel());
						if (!selection.getLastSelectedCellRowLabel().isEmpty()
								&& !selection.getLastSelectedCellColumnLabel().isEmpty()) {
							sb.append(", ");
						}
						sb.append(selection.getLastSelectedCellColumnLabel());
						sb.append(")");
					}
					manager.setMessage(sb.toString());
				}
				else {
					manager.setMessage(null);
				}
			}
		});
	}
	
	private void updateStatusLine() {
		final IStatusLineManager manager = getEditorSite().getActionBars().getStatusLineManager();
		final IContributionItem dimItem = manager.find("data.dimension");
		final long[] dimension = fTable.getTableDimension();
		if (dimItem != null) {
			((StatusLineContributionItem) dimItem).setText((dimension != null) ?
					("Dim: " + dimension[0] + " × " + dimension[1]) : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	@Override
	public void setFocus() {
		fTable.setFocus();
	}
	
	public void close(final boolean save) {
		final Display display = getSite().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				getSite().getPage().closeEditor(RDataEditor.this, save);
			}
		});
	}
	
	
	protected RDataEditorOutlinePage createOutlinePage() {
		return new RDataEditorOutlinePage(this);
	}
	
	protected RDataFilterPage createFilterPage() {
		return new RDataFilterPage(this);
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (RDataTableComposite.class.equals(required)) {
			return fTable;
		}
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage = createOutlinePage();
			}
			return fOutlinePage;
		}
		if (IFilterPage.class.equals(required)) {
			if (fFilterPage == null) {
				fFilterPage = createFilterPage();
			}
			return fFilterPage;
		}
		return super.getAdapter(required);
	}
	
}
