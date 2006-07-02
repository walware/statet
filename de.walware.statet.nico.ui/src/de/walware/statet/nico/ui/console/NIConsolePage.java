/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.console;

import java.util.ResourceBundle;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.console.ConsoleRemoveAllTerminatedAction;
import org.eclipse.debug.internal.ui.views.console.ConsoleRemoveLaunchAction;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.actions.ClearOutputAction;
import org.eclipse.ui.internal.console.IOConsoleViewer;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

import de.walware.eclipsecommon.ui.SharedMessages;
import de.walware.eclipsecommon.ui.dialogs.Layouter;
import de.walware.eclipsecommon.ui.util.UIAccess;

import de.walware.statet.ext.ui.editors.IEditorConfiguration;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.ui.internal.NicoUIPlugin;
import de.walware.statet.ui.TextViewerAction;
import de.walware.statet.ui.util.DNDUtil;


/**
 * A page for a <code>NIConsole</code>.
 * <p>
 * The page contains beside the usual output viewer 
 * a separete input field with submit button.
 */
public class NIConsolePage implements IPageBookViewPage, 
		IAdaptable, IShowInSource, IShowInTargetList, 
		IPropertyChangeListener, ScrollLockAction.Receiver {

	
	private class FindReplaceUpdater implements IDocumentListener {
		
		private boolean wasEmpty = true;
		
		public void documentAboutToBeChanged(DocumentEvent event) {
		}

		public void documentChanged(DocumentEvent event) {
			
			boolean isEmpty = (event.fDocument.getLength() == 0);
			if (isEmpty != wasEmpty) {
				fMultiActionHandler.updateEnabledState();
				wasEmpty = isEmpty;
			}
		}
	}
	private class PostUpdater implements IDocumentListener, Runnable {
		
		private volatile boolean fIsSheduled = false;
		
		public void documentAboutToBeChanged(DocumentEvent event) {
		}

		public void documentChanged(DocumentEvent event) {

			if (!fIsSheduled) {
				fIsSheduled = true;
				Display display = UIAccess.getDisplay(getSite().getShell());
				display.asyncExec(this);
			}
		}

		public void run() {
			// post change run
			fIsSheduled = false;
			fMultiActionHandler.updateEnabledState();
		}
	}

	private NIConsole fConsole;
	private IConsoleView fConsoleView;
	private IPageSite fSite;
	private Composite fControl;
	private Clipboard fClipboard;
	
	private IOConsoleViewer fOutputViewer;
	private InputGroup fInputGroup;
	
	private volatile boolean fIsCreated = false;
	
	// Actions
	private MultiActionHandler fMultiActionHandler;
	
	private FindReplaceAction fFindReplaceAction;
	
	// Output viewer actions
	private TextViewerAction fOutputCopyAction;
	private SubmitPasteAction fOutputPasteAction;
	private TextViewerAction fOutputSelectAllAction;
	private ClearOutputAction fOutputClearAllAction;
	private Action fOutputScrollLockAction;
	
	// Input viewer actions
	private TextViewerAction fInputDeleteAction;
	private TextViewerAction fInputCutAction;
	private TextViewerAction fInputCopyAction;
	private TextViewerAction fInputPasteAction;
	private TextViewerAction fInputSelectAllAction;
	private TextViewerAction fInputUndoAction;
	private TextViewerAction fInputRedoAction;
	
	// Process control actions
	private IDebugEventSetListener fDebugListener;
	private ConsoleRemoveLaunchAction fRemoveAction;
	private ConsoleRemoveAllTerminatedAction fRemoveAllAction;
	private TerminateToolAction fTerminateAction;


	/**
	 * Constructs a console page for the given console in the given view.
	 * 
	 * @param console the console
	 * @param view the console view the page is contained in
	 */
	public NIConsolePage(NIConsole console, IConsoleView view) {
		
		fConsole = console;
		fConsoleView = view;
	}
	
	
	public void init(IPageSite site) throws PartInitException {
		
		fSite = site;
		fInputGroup = createInputGroup();

		fDebugListener = new IDebugEventSetListener() {
			public void handleDebugEvents(DebugEvent[] events) {
				ToolProcess process = getConsole().getProcess();
				ToolWorkspace data = process.getWorkspaceData();
				for (DebugEvent event : events) {
					Object source = event.getSource();
					
					if (source == process) {
						switch (event.getKind()) {
						case DebugEvent.TERMINATE:
							onToolTerminated();
							break;
						}
					}
					else if (source == data) {
						if (event.getKind() == DebugEvent.CHANGE 
								&& event.getDetail() == ToolWorkspace.DETAIL_PROMPT && fIsCreated) {
							Prompt prompt = (Prompt) event.getData();
							fInputGroup.updatePrompt(prompt);
						}
					}
				}
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(fDebugListener);
	}
	
	protected InputGroup createInputGroup() {
		
		return new InputGroup(this);
	}

	public void createControl(Composite parent) {
		
		fControl = new Composite(parent, SWT.NONE) {
			@Override
			public boolean setFocus() {
				try {
					return fInputGroup.getSourceViewer().getControl().setFocus();
				}
				catch (NullPointerException e) {
					return super.setFocus();
				}
			}
		};
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.verticalSpacing = 3;
		layout.marginWidth = 0;
		layout.marginBottom = 3;
		fControl.setLayout(layout);
		
		fOutputViewer = new IOConsoleViewer(fControl, fConsole);
		fOutputViewer.setReadOnly();
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		fOutputViewer.getControl().setLayoutData(gd);
		
		fInputGroup.createControl(fControl, getInputEditorConfiguration());
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		fInputGroup.getComposite().setLayoutData(gd);
		
		fConsole.addPropertyChangeListener(this);

		fClipboard = new Clipboard(fControl.getDisplay());
		createActions();
		hookContextMenu();
		hookDND();
		contributeToActionBars();
		
		new ConsoleActivationNotifier();
		fIsCreated = true;
		fInputGroup.updatePrompt(null);
	}
	
	/**
	 * Creates the adapter to configure the input source viewer.
	 * Will be disposed automatically.
	 * 
	 * @return the adapter
	 */
	protected IEditorConfiguration getInputEditorConfiguration() {
		
		return null;
	}


	private class ConsoleActivationNotifier implements Listener {

		private ConsoleActivationNotifier() {
			
			fControl.addListener(SWT.Activate, this);
			fControl.addListener(SWT.Dispose, this);
			if (fControl.isVisible()) {
				NicoUIPlugin.getDefault().getToolRegistry().consoleActivated(fConsoleView, fConsole);
			}
		}
		
		public void handleEvent(Event event) {
			switch (event.type) {
			
			case SWT.Activate:
				NicoUIPlugin.getDefault().getToolRegistry().consoleActivated(fConsoleView, fConsole);
				break;
			
			case SWT.Dispose:
				fControl.removeListener(SWT.Activate, this);
				fControl.removeListener(SWT.Dispose, this);
				break;

			}
		}
	}

	protected void createActions() {
		
		Control outputControl = fOutputViewer.getControl();
		SourceViewer inputViewer = fInputGroup.getSourceViewer();
		Control inputControl = inputViewer.getControl();
		
		fMultiActionHandler = new MultiActionHandler(fConsoleView.getViewSite());

		fOutputCopyAction = TextViewerAction.createCopyAction(fOutputViewer);
		fMultiActionHandler.addGlobalAction(outputControl, ActionFactory.COPY.getId(), fOutputCopyAction);
	
		fOutputPasteAction = new SubmitPasteAction(this);
		fOutputPasteAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.PASTE);
		fMultiActionHandler.addGlobalAction(outputControl, ActionFactory.PASTE.getId(), fOutputPasteAction);
		
		fOutputSelectAllAction = TextViewerAction.createSelectAllAction(fOutputViewer);
		fMultiActionHandler.addGlobalAction(outputControl, ActionFactory.SELECT_ALL.getId(), fOutputSelectAllAction);
		
		fOutputClearAllAction = new ClearOutputAction(fConsole);
		fOutputScrollLockAction = new ScrollLockAction(this, false);
		
		fInputDeleteAction = TextViewerAction.createDeleteAction(inputViewer);
		fMultiActionHandler.addGlobalAction(inputControl, ActionFactory.DELETE.getId(), fInputDeleteAction);
		
		fInputCutAction = TextViewerAction.createCutAction(inputViewer);
		fMultiActionHandler.addGlobalAction(inputControl, ActionFactory.CUT.getId(), fInputCutAction);
		
		fInputCopyAction = TextViewerAction.createCopyAction(inputViewer);
		fMultiActionHandler.addGlobalAction(inputControl, ActionFactory.COPY.getId(), fInputCopyAction);
		
		fInputPasteAction = TextViewerAction.createPasteAction(inputViewer);
		fMultiActionHandler.addGlobalAction(inputControl, ActionFactory.PASTE.getId(), fInputPasteAction);

		fInputSelectAllAction = TextViewerAction.createSelectAllAction(inputViewer);
		fMultiActionHandler.addGlobalAction(inputControl, ActionFactory.SELECT_ALL.getId(), fInputSelectAllAction);
		
		fInputUndoAction = TextViewerAction.createUndoAction(inputViewer);
		fMultiActionHandler.addGlobalAction(inputControl, ActionFactory.UNDO.getId(), fInputUndoAction);
		fInputRedoAction = TextViewerAction.createRedoAction(inputViewer);
		fMultiActionHandler.addGlobalAction(inputControl, ActionFactory.REDO.getId(), fInputRedoAction);

        ResourceBundle bundle = SharedMessages.getCompatibilityBundle();
		fFindReplaceAction = new FindReplaceAction(bundle, "FindReplaceAction_", fConsoleView); //$NON-NLS-1$
		fFindReplaceAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_REPLACE);
		fMultiActionHandler.addGlobalAction(outputControl, ActionFactory.FIND.getId(), fFindReplaceAction);
		fMultiActionHandler.addGlobalAction(inputControl, ActionFactory.FIND.getId(), fFindReplaceAction);
		fOutputViewer.getDocument().addDocumentListener(new FindReplaceUpdater());
		((InputDocument) inputViewer.getDocument()).addDocumentListener(new PostUpdater());
		fInputGroup.createActions(fMultiActionHandler);

		inputViewer.addSelectionChangedListener(fMultiActionHandler);
		fOutputViewer.addSelectionChangedListener(fMultiActionHandler);
		
		fRemoveAction = new ConsoleRemoveLaunchAction(fConsole.getProcess().getLaunch());
		fRemoveAllAction = new ConsoleRemoveAllTerminatedAction();
        fTerminateAction = new TerminateToolAction(fConsole.getProcess());
	}
	
	private void hookContextMenu() {
		
		String id = NIConsole.NICONSOLE_TYPE + "#OutputContextMenu"; //$NON-NLS-1$
		MenuManager menuMgr = new MenuManager("ContextMenu", id); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillOutputContextMenu(manager);
			}
		});
		Control control = fOutputViewer.getControl();
		Menu menu = menuMgr.createContextMenu(control);
		control.setMenu(menu);
		getSite().registerContextMenu(id, menuMgr, fOutputViewer);
		
		id = NIConsole.NICONSOLE_TYPE + "#InputContextMenu"; //$NON-NLS-1$
		menuMgr = new MenuManager("ContextMenu", id); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillInputContextMenu(manager);
			}
		});
		control = fInputGroup.getSourceViewer().getControl();
		menu = menuMgr.createContextMenu(control);
		control.setMenu(menu);
		getSite().registerContextMenu(id, menuMgr, fInputGroup.getSourceViewer());
	}
	
	protected void hookDND() {
		
		DNDUtil.addDropSupport(fOutputViewer.getControl(), 
				new SubmitDropAdapter(this), 
				new Transfer[] { TextTransfer.getInstance() } );
	}
	
	protected void contributeToActionBars() {
		
		IActionBars bars = getSite().getActionBars();
		
		fMultiActionHandler.registerActions(bars);
		
		IToolBarManager toolBar = bars.getToolBarManager();
		toolBar.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fOutputClearAllAction);
		toolBar.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fOutputScrollLockAction);

		toolBar.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fTerminateAction);
		toolBar.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemoveAction);
		toolBar.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemoveAllAction);
	}
	
	protected void fillInputContextMenu(IMenuManager manager) {

		manager.add(fInputCutAction);
		manager.add(fInputCopyAction);
		manager.add(fInputPasteAction);
        manager.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));

		manager.add(new Separator());
		manager.add(fInputUndoAction);
		manager.add(fInputRedoAction);
        manager.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	protected void fillOutputContextMenu(IMenuManager manager) {

		manager.add(fOutputCopyAction);
		manager.add(fOutputSelectAllAction);
		
		manager.add(new Separator("more")); //$NON-NLS-1$
		manager.add(fFindReplaceAction);
//		manager.add(new FollowHyperlinkAction(fViewer));

		manager.add(new Separator("submit")); //$NON-NLS-1$
		manager.add(fOutputPasteAction);
		
		manager.add(new Separator("view")); //$NON-NLS-1$
		manager.add(fOutputClearAllAction);
		manager.add(fOutputScrollLockAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public void dispose() {

		fConsole.removePropertyChangeListener(this);
		
		if (fIsCreated) { // control created
			fIsCreated = false;

			fOutputViewer.removeSelectionChangedListener(fMultiActionHandler);
			fInputGroup.getSourceViewer().removeSelectionChangedListener(fMultiActionHandler);
			fMultiActionHandler.dispose();

			fMultiActionHandler = null;
			
			fFindReplaceAction = null;
			
			fOutputCopyAction = null;
			fOutputPasteAction = null;
			fOutputSelectAllAction = null;
			fOutputClearAllAction = null;
			
			fInputDeleteAction = null;
			fInputCutAction = null;
			fInputCopyAction = null;
			fInputPasteAction = null;
			fInputSelectAllAction = null;
			fInputUndoAction = null;
			
			DebugPlugin debug = DebugPlugin.getDefault();
			if (debug != null) {
				debug.removeDebugEventListener(fDebugListener);
			}
			fDebugListener = null;
			fRemoveAction.dispose();
			fRemoveAction = null;
			fRemoveAllAction.dispose();
			fRemoveAllAction = null;
			fTerminateAction.dispose();
			fTerminateAction = null;

			fOutputViewer = null;
		}
		
		if (fInputGroup != null) {
			fInputGroup.dispose();
			fInputGroup = null;
		}
	}

	

	public IPageSite getSite() {
		
		return fSite;
	}
	
	public Control getControl() {
		
		return fControl;
	}
	
	public NIConsole getConsole() {
		
		return fConsole;
	}
	
	public Clipboard getClipboard() {
		
		return fClipboard;
	}
	
    public Object getAdapter(Class required) {
    	
		if (Widget.class.equals(required)) {
			if (fOutputViewer.getControl().isFocusControl())
				return fOutputViewer.getTextWidget();
			return fInputGroup.getSourceViewer().getTextWidget();
		}
   		if (IFindReplaceTarget.class.equals(required)) {
    		if (fInputGroup.getSourceViewer().getControl().isFocusControl())
    			return fInputGroup.getSourceViewer().getFindReplaceTarget();
   			return fOutputViewer.getFindReplaceTarget();
   		}
        if (IShowInSource.class.equals(required)) {
            return this;
        }
        if (IShowInTargetList.class.equals(required)) {
            return this; 
        }
        return null;
    }

    public ShowInContext getShowInContext() {
    	
        IProcess process = fConsole.getProcess();
        if (process == null) {
            return null;
        } 
        IDebugTarget target = (IDebugTarget) process.getAdapter(IDebugTarget.class);
        ISelection selection = null;
        if (target == null) {
            selection = new StructuredSelection(process);
        } else {
            selection = new StructuredSelection(target);
        }
        return new ShowInContext(null, selection);
    }

    public String[] getShowInTargetIds() {
    	
        return new String[] { IDebugUIConstants.ID_DEBUG_VIEW };
    }

    
	public void setActionBars(IActionBars actionBars) {
		
//		fOutputViewer.setActionBars(actionBars);
	}

	public void setFocus() {
		
		fInputGroup.getSourceViewer().getControl().setFocus();
	}
	
	
	protected void onToolTerminated() {
		
		if (fIsCreated) {
			fTerminateAction.update();
			fOutputPasteAction.setEnabled(false);
			final Button button = fInputGroup.getSubmitButton();
			UIAccess.getDisplay(getSite().getShell()).asyncExec(new Runnable() {
				public void run() {
					if (Layouter.isOkToUse(button)) {
						button.setEnabled(false);
					}
				}
			});
		}
	}
	
	public void setAutoScroll(boolean enabled) {
		
		fOutputViewer.setAutoScroll(enabled);
		fOutputScrollLockAction.setChecked(!enabled);
	}
	
    public void propertyChange(PropertyChangeEvent event) {
    	
        if (Layouter.isOkToUse(fControl) ) {
			Object source = event.getSource();
			String property = event.getProperty();
			
			if (source.equals(fConsole) && IConsoleConstants.P_FONT.equals(property)) {
				Font font = fConsole.getFont();
				fOutputViewer.setFont(font);
				fInputGroup.setFont(font);
				fControl.layout();
			} 
			else if (IConsoleConstants.P_FONT_STYLE.equals(property)) {
				fControl.redraw();
			}
			else if (property.equals(IConsoleConstants.P_STREAM_COLOR)) {
				fOutputViewer.getTextWidget().redraw();
			} 
			else if (source.equals(fConsole) && property.equals(IConsoleConstants.P_TAB_SIZE)) {
			    int tabSize = ((Integer) event.getNewValue()).intValue();
			    fOutputViewer.setTabWidth(tabSize);
			    fInputGroup.getSourceViewer().setTabWidth(tabSize);
			} 
			else if (source.equals(fConsole) && property.equals(IConsoleConstants.P_CONSOLE_WIDTH)) {
				fOutputViewer.setConsoleWidth(fConsole.getConsoleWidth());
			}
		} 
	}
    
}
