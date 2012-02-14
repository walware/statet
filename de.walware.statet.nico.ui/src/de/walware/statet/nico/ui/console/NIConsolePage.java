/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.console.actions.ClearOutputAction;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.texteditor.FindReplaceAction;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier.ChangeListener;
import de.walware.ecommons.text.ui.TextViewerAction;
import de.walware.ecommons.ui.ISettingsChangedHandler;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.actions.SimpleContributionItem;
import de.walware.ecommons.ui.util.DNDUtil;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStatus;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.core.util.IToolRetargetable;
import de.walware.statet.nico.internal.ui.LocalTaskTransfer;
import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.internal.ui.console.OutputViewer;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.actions.CancelHandler;
import de.walware.statet.nico.ui.util.ExportConsoleOutputWizard;
import de.walware.statet.nico.ui.util.NicoWizardDialog;
import de.walware.statet.nico.ui.util.OpenTrackingFilesContributionItem;


/**
 * A page for a <code>NIConsole</code>.
 * <p>
 * The page contains beside the usual output viewer
 * a separete input field with submit button.
 */
public abstract class NIConsolePage implements IPageBookViewPage,
		IAdaptable, IShowInSource, IShowInTargetList,
		IPropertyChangeListener, ScrollLockAction.Receiver, IToolProvider, ChangeListener {
	
	
	private static final String DIALOG_ID = "Console"; //$NON-NLS-1$
	private static final String SETTING_INPUTHEIGHT = "InputHeight"; //$NON-NLS-1$
	
	public static final String NICO_CONTROL_MENU_ID = "nico.control"; //$NON-NLS-1$
	
	
	private class FindReplaceUpdater implements IDocumentListener {
		
		private boolean wasEmpty = true;
		
		@Override
		public void documentAboutToBeChanged(final DocumentEvent event) {
		}
		
		@Override
		public void documentChanged(final DocumentEvent event) {
			final boolean isEmpty = (event.fDocument.getLength() == 0);
			if (isEmpty != wasEmpty) {
				fMultiActionHandler.updateEnabledState();
				wasEmpty = isEmpty;
			}
		}
		
	}
	
	private class PostUpdater implements IDocumentListener, Runnable {
		
		private volatile boolean fIsSheduled = false;
		
		@Override
		public void documentAboutToBeChanged(final DocumentEvent event) {
		}
		
		@Override
		public void documentChanged(final DocumentEvent event) {
			if (!fIsSheduled) {
				fIsSheduled = true;
				final Display display = UIAccess.getDisplay(getSite().getShell());
				display.asyncExec(this);
			}
		}
		
		@Override
		public void run() {
			// post change run
			fIsSheduled = false;
			fMultiActionHandler.updateEnabledState();
		}
		
	}
	
	private class SizeController implements Listener {
		
		private final Sash fSash;
		private final GridData fOutputGD;
		private final GridData fInputGD;
		private int fLastExplicit;
		
		public SizeController(final Sash sash, final GridData outputGD, final GridData inputGD) {
			fSash = sash;
			fOutputGD = outputGD;
			fInputGD = inputGD;
			fLastExplicit = -1;
		}
		
		@Override
		public void handleEvent(final Event event) {
			if (event.widget == fSash) {
				if (event.type == SWT.Selection && event.detail != SWT.DRAG) {
					final Rectangle bounds = fControl.getClientArea();
	//				System.out.println(bounds.height);
	//				Rectangle bounds2 = fInputGroup.getComposite().getBounds();
	//				System.out.println(bounds2.y+bounds2.height);
					
					setNewInputHeight(bounds.height - event.y - fSash.getSize().y, true);
				}
				return;
			}
			if (event.widget == fControl) {
				if (event.type == SWT.Resize) {
					setNewInputHeight(fInputGD.heightHint, false);
				}
			}
		}
		
		private void setNewInputHeight(int height, final boolean explicit) {
			if (!explicit) {
				height = fLastExplicit;
			}
			if (height == -1) {
				return;
			}
			final Rectangle bounds = fControl.getClientArea();
			final int max = bounds.height - fOutputGD.minimumHeight - fSash.getSize().y;
			if (height > max) {
				height = max;
			}
			if (height < fInputGD.minimumHeight) {
				height = -1;
			}
			if (explicit) {
				fLastExplicit = height;
			}
			
			if (fInputGD.heightHint == height) {
				return;
			}
			fInputGD.heightHint = height;
			fControl.layout(new Control[] { fInputGroup.getComposite() });
		}
		
		private void fontChanged() {
			fOutputGD.minimumHeight = LayoutUtil.hintHeight(fOutputViewer.getTextWidget(), 4);
			final ScrollBar bar = fOutputViewer.getTextWidget().getHorizontalBar();
			if (bar.isVisible()) {
				fOutputGD.minimumHeight += bar.getSize().y;
			}
			fInputGD.minimumHeight = fInputGroup.getComposite().computeSize(800, -1).y;
			if (fInputGD.heightHint != -1
					&& fInputGD.minimumHeight > fInputGD.heightHint) {
				fInputGD.heightHint = -1;
			}
		}
		
	}
	
	private class StatusListener implements IDebugEventSetListener {
		
		private boolean isProcessing = false;
		private boolean isTerminated = false;
		
		private int updateId = Integer.MIN_VALUE;
		private Prompt fNewPrompt = null;
		private boolean fCurrentBusy = false;
		private boolean fNewBusy = false;
		
		public void init() {
			final ToolController controller = getConsole().getProcess().getController();
			synchronized (StatusListener.this) {
				if (controller != null) {
					final ToolStatus status = controller.getStatus();
					isProcessing = (status == ToolStatus.STARTED_PROCESSING || status == ToolStatus.STARTING);
					isTerminated = (status == ToolStatus.TERMINATED);
					fCurrentBusy = fNewBusy = (isProcessing || isTerminated);
				}
				else {
					isProcessing = false;
					isTerminated = true;
					fCurrentBusy = fNewBusy = true;
				}
				fInputGroup.updatePrompt(null);
				fInputGroup.updateBusy(fCurrentBusy);
			}
		}
		
		@Override
		public void handleDebugEvents(final DebugEvent[] events) {
			final ToolProcess process = getConsole().getProcess();
			final ToolWorkspace data = process.getWorkspaceData();
			
			Prompt prompt = null;
			boolean match = false;
			
			for (final DebugEvent event : events) {
				final Object source = event.getSource();
				
				if (source == process) {
					switch (event.getKind()) {
					case DebugEvent.TERMINATE:
						match = true;
						isTerminated = true;
						onToolTerminated();
						break;
					case DebugEvent.MODEL_SPECIFIC:
						match = true;
						final int type = (event.getDetail() & ToolProcess.TYPE_MASK);
						if (type == ToolProcess.STATUS) {
							isProcessing = (event.getDetail() == ToolProcess.STATUS_PROCESS);
						}
						break;
					}
				}
				else if (source == data) {
					if (event.getKind() == DebugEvent.CHANGE
							&& event.getDetail() == ToolWorkspace.DETAIL_PROMPT) {
						match = true;
						prompt = (Prompt) event.getData();
					}
				}
			}
			if (!match) {
				return;
			}
			final int thisId;
			final long schedule;
			synchronized (StatusListener.this) {
				fNewBusy = isProcessing || isTerminated;
				schedule = (fNewBusy) ? (System.nanoTime() + 50000000L) : System.nanoTime();
				if (prompt != null) {
					fNewPrompt = prompt;
				}
				if (!fIsCreated || 
						(fNewBusy == fCurrentBusy && fNewPrompt == null)) {
					return;
				}
				thisId = ++updateId;
			}
			UIAccess.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					final long diff = (schedule - System.nanoTime()) / 1000000L;
					if (diff > 5) {
						Display.getCurrent().timerExec((int) diff, this);
						return;
					}
					if (!fIsCreated) {
						return;
					}
					synchronized (StatusListener.this) {
						if (thisId != updateId) {
							return;
						}
						if (fNewPrompt != null) {
							fInputGroup.updatePrompt(fNewPrompt);
							fNewPrompt = null;
						}
						if (fNewBusy != fCurrentBusy) {
							fInputGroup.updateBusy(fNewBusy);
							fCurrentBusy = fNewBusy;
						}
					}
				};
			});
		}
		
	}
	
	
	private final NIConsole fConsole;
	private final IConsoleView fConsoleView;
	private IPageSite fSite;
	private Composite fControl;
	private Clipboard fClipboard;
	
	private OutputViewer fOutputViewer;
	private ConsolePageEditor fInputGroup;
	private SizeController fResizer;
	private MenuManager fOutputMenuManager;
	private MenuManager fInputMenuManager;
	
	private volatile boolean fIsCreated = false;
	
	// Actions
	private MultiActionHandler fMultiActionHandler;
	private final ListenerList fToolActions = new ListenerList();
	ServiceLocator fInputServices;
	
	private FindReplaceUpdater fFindReplaceUpdater;
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
	private StatusListener fDebugListener;
	private ConsoleRemoveLaunchAction fRemoveAction;
	private ConsoleRemoveAllTerminatedAction fRemoveAllAction;
	private TerminateToolAction fTerminateAction;
	
	private CancelHandler fCancelCurrentHandler;
	private CancelHandler fCancelAllHandler;
	private CancelHandler fCancelPauseHandler;
	
	private final HandlerCollection fPageHandlers = new HandlerCollection();
	private final HandlerCollection fInputHandlers = new HandlerCollection();
	
	
	/**
	 * Constructs a console page for the given console in the given view.
	 * 
	 * @param console the console
	 * @param view the console view the page is contained in
	 */
	public NIConsolePage(final NIConsole console, final IConsoleView view) {
		fConsole = console;
		fConsoleView = view;
	}
	
	
	@Override
	public void init(final IPageSite site) throws PartInitException {
		fSite = site;
		fInputGroup = createInputGroup();
		
		fDebugListener = new StatusListener();
		DebugPlugin.getDefault().addDebugEventListener(fDebugListener);
	}
	
	protected ConsolePageEditor createInputGroup() {
		return new ConsolePageEditor(this);
	}
	
	protected ConsolePageEditor getInputGroup() {
		return fInputGroup;
	}
	
	@Override
	public void createControl(final Composite parent) {
		PreferencesUtil.getSettingsChangeNotifier().addChangeListener(this);
		fConsole.addPropertyChangeListener(this);
		
		fControl = new Composite(parent, SWT.NONE) {
			@Override
			public boolean setFocus() {
				NIConsolePage.this.setFocus();
				return true;
			}
			@Override
			public void setVisible(final boolean visible) {
				super.setVisible(visible);
				if (visible) {
					NIConsolePage.this.setFocus();
				}
			}
		};
		final GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		fControl.setLayout(layout);
		
		fOutputViewer = new OutputViewer(fControl, fConsole);
		final GridData outputGD = new GridData(SWT.FILL, SWT.FILL, true, true);
		fOutputViewer.getControl().setLayoutData(outputGD);
		
		fOutputViewer.getTextWidget().addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.doit
						&& (e.character > 0)
						&& (e.stateMask == SWT.NONE || e.stateMask == SWT.SHIFT)
						&& ((e.keyCode & SWT.KEYCODE_BIT) == 0) ) {
					final StyledText textWidget = fInputGroup.getViewer().getTextWidget();
					if (!UIAccess.isOkToUse(textWidget)) {
						return;
					}
					final int cType = Character.getType(e.character);
					if (cType > 0 && cType < Character.CONTROL && textWidget.getCharCount() == 0) {
						textWidget.replaceTextRange(0, 0, Character.toString(e.character));
						textWidget.setCaretOffset(textWidget.getCharCount());
					}
					else {
						Display.getCurrent().beep();
					}
					setFocus();
				}
			}
			@Override
			public void keyReleased(final KeyEvent e) {
			}
		});
		
		final Sash sash = new Sash(fControl, SWT.HORIZONTAL);
//		sash.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		fInputGroup.createControl(fControl, createInputEditorConfigurator());
		final GridData inputGD = new GridData(SWT.FILL, SWT.FILL, true, false);
		fInputGroup.getComposite().setLayoutData(inputGD);
		
		fOutputViewer.getTextWidget().getHorizontalBar().setVisible(false);
		
		fResizer = new SizeController(sash, outputGD, inputGD);
		sash.addListener(SWT.Selection, fResizer);
		fControl.addListener(SWT.Resize, fResizer);
		
		fClipboard = new Clipboard(fControl.getDisplay());
		createActions();
		initActions(getSite(), fPageHandlers);
		fInputGroup.initActions(fInputServices, fInputHandlers);
		hookContextMenu();
		hookDND();
		contributeToActionBars(getSite(), getSite().getActionBars(), fPageHandlers);
		
		new ConsoleActivationNotifier();
		fIsCreated = true;
		fDebugListener.init();
		
		final IDialogSettings dialogSettings = DialogUtil.getDialogSettings(NicoUIPlugin.getDefault(), DIALOG_ID);
		try {
			final int height = dialogSettings.getInt(SETTING_INPUTHEIGHT);
			if (height > 0) {
				fResizer.fLastExplicit = height;
			}
		}
		catch (final NumberFormatException e) {
			// missing value
		}
		fResizer.fontChanged();
		
		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (fInputGroup != null && UIAccess.isOkToUse(fInputGroup.getViewer())) {
					fOutputViewer.revealEndOfDocument();
					if (fOutputViewer.getControl().isFocusControl()) {
						setFocus();
					}
				}
			}
		});
	}
	
	/**
	 * Creates the adapter to configure the input source viewer.
	 * Will be disposed automatically.
	 * 
	 * @return the adapter
	 */
	protected abstract SourceEditorViewerConfigurator createInputEditorConfigurator();
	
	
	private class ConsoleActivationNotifier implements Listener {
		private ConsoleActivationNotifier() {
			fControl.addListener(SWT.Activate, this);
			fControl.addListener(SWT.Dispose, this);
			if (fControl.isVisible()) {
				NicoUIPlugin.getDefault().getToolRegistry().consoleActivated(fConsoleView, fConsole);
			}
		}
		
		@Override
		public void handleEvent(final Event event) {
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
	
	private void createActions() {
		final Control outputControl = fOutputViewer.getControl();
		final SourceViewer inputViewer = fInputGroup.getViewer();
		final Control inputControl = inputViewer.getControl();
		
		final IServiceLocator pageServices = getSite();
		final IServiceLocatorCreator serviceCreator = (IServiceLocatorCreator) pageServices.getService(IServiceLocatorCreator.class);
		fInputServices = (ServiceLocator) serviceCreator.createServiceLocator(pageServices, null, new IDisposable() {
			@Override
			public void dispose() {
				fInputServices = null;
			}
		});
		// TODO: E-3.4 / E-3.5 bug #142226
		
		final IHandlerService pageCommands = (IHandlerService) pageServices.getService(IHandlerService.class);
		final IHandlerService inputCommands = (IHandlerService) fInputServices.getService(IHandlerService.class);
		final IContextService pageKeys = (IContextService) pageServices.getService(IContextService.class);
		final IContextService inputKeys = (IContextService) fInputServices.getService(IContextService.class);
		
		inputControl.addListener(SWT.FocusIn, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (fInputServices != null) {
					fInputServices.activate();
					getSite().getActionBars().updateActionBars();
				}
			}
		});
		inputControl.addListener(SWT.FocusOut, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (fInputServices != null) {
					fInputServices.deactivate();
					getSite().getActionBars().updateActionBars();
				}
			}
		});
		
		fMultiActionHandler = new MultiActionHandler();
		
		fRemoveAction = new ConsoleRemoveLaunchAction(fConsole.getProcess().getLaunch());
		fRemoveAllAction = new ConsoleRemoveAllTerminatedAction();
		fTerminateAction = new TerminateToolAction(fConsole.getProcess());
		fCancelCurrentHandler = new CancelHandler(this, ToolController.CANCEL_CURRENT);
		pageCommands.activateHandler(NicoUI.CANCEL_CURRENT_COMMAND_ID, fCancelCurrentHandler);
		fCancelAllHandler = new CancelHandler(this, ToolController.CANCEL_ALL);
		pageCommands.activateHandler(NicoUI.CANCEL_ALL_COMMAND_ID, fCancelAllHandler);
		fCancelPauseHandler = new CancelHandler(this, ToolController.CANCEL_CURRENT | ToolController.CANCEL_PAUSE);
		pageCommands.activateHandler(NicoUI.CANCEL_PAUSE_COMMAND_ID, fCancelPauseHandler);
// Conflict with binding CTRL+Z (in console EOF)
//		pageKeys.activateContext("org.eclipse.debug.ui.console");  //$NON-NLS-1$
		
		fOutputCopyAction = TextViewerAction.createCopyAction(fOutputViewer);
		fMultiActionHandler.addGlobalAction(outputControl, ActionFactory.COPY.getId(), fOutputCopyAction);
		fOutputPasteAction = new SubmitPasteAction(this);
		fOutputPasteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);
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
		
		final ResourceBundle bundle = SharedMessages.getCompatibilityBundle();
		fFindReplaceAction = new FindReplaceAction(bundle, "FindReplaceAction_", fConsoleView);  //$NON-NLS-1$
		fFindReplaceAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
		fMultiActionHandler.addGlobalAction(outputControl, ActionFactory.FIND.getId(), fFindReplaceAction);
		fMultiActionHandler.addGlobalAction(inputControl, ActionFactory.FIND.getId(), fFindReplaceAction);
		fFindReplaceUpdater = new FindReplaceUpdater();
		fConsole.getDocument().addDocumentListener(fFindReplaceUpdater);
		inputViewer.getDocument().addDocumentListener(new PostUpdater());
		
		inputViewer.addSelectionChangedListener(fMultiActionHandler);
		fOutputViewer.addSelectionChangedListener(fMultiActionHandler);
	}
	
	protected void initActions(final IServiceLocator serviceLocator, final HandlerCollection handlers) {
	}
	
	private void hookContextMenu() {
		String id = NIConsole.NICONSOLE_TYPE + "#OutputContextMenu"; //$NON-NLS-1$
		fOutputMenuManager = new MenuManager("ContextMenu", id); //$NON-NLS-1$
		fOutputMenuManager.setRemoveAllWhenShown(true);
		fOutputMenuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				fillOutputContextMenu(manager);
			}
		});
		Control control = fOutputViewer.getControl();
		Menu menu = fOutputMenuManager.createContextMenu(control);
		control.setMenu(menu);
		getSite().registerContextMenu(id, fOutputMenuManager, fOutputViewer);
		
		id = NIConsole.NICONSOLE_TYPE + "#InputContextMenu"; //$NON-NLS-1$
		fInputMenuManager = new MenuManager("ContextMenu", id); //$NON-NLS-1$
		fInputMenuManager.setRemoveAllWhenShown(true);
		fInputMenuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				fillInputContextMenu(manager);
			}
		});
		control = fInputGroup.getViewer().getControl();
		menu = fInputMenuManager.createContextMenu(control);
		control.setMenu(menu);
		getSite().registerContextMenu(id, fInputMenuManager, fInputGroup.getViewer());
	}
	
	protected void hookDND() {
		DNDUtil.addDropSupport(fOutputViewer.getControl(),
				new SubmitDropAdapter(this),
				new Transfer[] { 
					TextTransfer.getInstance(), 
					LocalTaskTransfer.getTransfer()
				} );
	}
	
	protected void contributeToActionBars(final IServiceLocator serviceLocator,
			final IActionBars actionBars, final HandlerCollection handlers) {
		fMultiActionHandler.registerActions(actionBars);
		
		final IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fOutputClearAllAction);
		toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fOutputScrollLockAction);
		
		toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, new HandlerContributionItem(
				new CommandContributionItemParameter(getSite(), CancelHandler.MENU_ID,
						NicoUI.CANCEL_CURRENT_COMMAND_ID, null,
						null, null, null,
						Messages.CancelAction_name, null, Messages.CancelAction_tooltip,
						CommandContributionItem.STYLE_PULLDOWN, null, false), fCancelCurrentHandler));
		toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fTerminateAction);
		toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemoveAction);
		toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemoveAllAction);
		
		final IMenuManager menuManager = actionBars.getMenuManager();
		menuManager.add(new Separator(NICO_CONTROL_MENU_ID));
		menuManager.add(new Separator(SharedUIResources.ADDITIONS_MENU_ID));
		
		menuManager.add(new Separator("tracking")); //$NON-NLS-1$
		final MenuManager trackingMenu= new MenuManager("Open In Editor") {
			@Override
			public boolean isVisible() {
				return !getTool().getTracks().isEmpty();
			}
		};
		trackingMenu.add(new OpenTrackingFilesContributionItem(getTool()));
		menuManager.add(trackingMenu);
		menuManager.add(new SimpleContributionItem("Export Console Output...", null) {
			@Override
			protected void execute() throws ExecutionException {
				final ToolProcess tool = getTool();
				if (tool == null) {
					return;
				}
				final ExportConsoleOutputWizard wizard = new ExportConsoleOutputWizard(NIConsolePage.this);
				final WizardDialog dialog = new NicoWizardDialog(getSite().getShell(), wizard);
				dialog.setBlockOnOpen(false);
				dialog.open();
			}
		});
		
		menuManager.add(new Separator("settings")); //$NON-NLS-1$
		menuManager.add(new SimpleContributionItem("Preferences...", "P") {
			@Override
			protected void execute() throws ExecutionException {
				final Shell shell = getSite().getShell();
				final List<String> pageIds = new ArrayList<String>();
				NIConsolePage.this.collectContextMenuPreferencePages(pageIds);
				if (!pageIds.isEmpty() && (shell == null || !shell.isDisposed())) {
					org.eclipse.ui.dialogs.PreferencesUtil.createPreferenceDialogOn(shell,
							pageIds.get(0), pageIds.toArray(new String[pageIds.size()]), null)
							.open();
				}
			}
		});
		
		menuManager.add(new Separator());
	}
	
	protected void fillInputContextMenu(final IMenuManager manager) {
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
	
	protected void fillOutputContextMenu(final IMenuManager manager) {
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
	
	@Override
	public void dispose() {
		fConsole.removePropertyChangeListener(this);
		PreferencesUtil.getSettingsChangeNotifier().removeChangeListener(this);
		if (fDebugListener != null) {
			final DebugPlugin debug = DebugPlugin.getDefault();
			if (debug != null) {
				debug.removeDebugEventListener(fDebugListener);
			}
			fDebugListener = null;
		}
		
		if (fIsCreated) { // control created
			fIsCreated = false;
			
			try {
				fConsole.getDocument().removeDocumentListener(fFindReplaceUpdater);
				fOutputViewer.removeSelectionChangedListener(fMultiActionHandler);
				fInputGroup.getViewer().removeSelectionChangedListener(fMultiActionHandler);
			}
			catch (final Exception e) {
				NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, Messages.Console_error_UnexpectedException_message, e);
			}
			
			fPageHandlers.dispose();
			fInputHandlers.dispose();
			
			fMultiActionHandler.dispose();
			fMultiActionHandler = null;
			fInputServices.dispose();
			fInputServices = null;
			
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
	
	
	@Override
	public IPageSite getSite() {
		return fSite;
	}
	
	public IConsoleView getView() {
		return fConsoleView;
	}
	
	@Override
	public Control getControl() {
		return fControl;
	}
	
	public NIConsole getConsole() {
		return fConsole;
	}
	
	public Clipboard getClipboard() {
		return fClipboard;
	}
	
	@Override
	public ToolProcess getTool() {
		return fConsole.getProcess();
	}
	
	@Override
	public void addToolRetargetable(final IToolRetargetable listener) {
		fToolActions.add(listener);
	}
	
	@Override
	public void removeToolRetargetable(final IToolRetargetable listener) {
		fToolActions.remove(listener);
	}
	
	public TextConsoleViewer getOutputViewer() {
		return fOutputViewer;
	}
	
	public IMenuManager getOutputContextMenuManager() {
		return fOutputMenuManager;
	}
	
	public IMenuManager getInputContextMenuManager() {
		return fInputMenuManager;
	}
	
	/**
	 * Return the text in the input line.
	 * 
	 * @return
	 */
	public String getInput() {
		return fInputGroup.fDocument.get();
	}
	
	/**
	 * Clear the input line (e.g. after successful submit).
	 */
	public void clearInput() {
		fInputGroup.clear();
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (fInputGroup != null) {
			if (Widget.class.equals(required)) {
				if (fOutputViewer.getControl().isFocusControl()) {
					return fOutputViewer.getTextWidget();
				}
				return fInputGroup.getViewer().getTextWidget();
			}
			if (IFindReplaceTarget.class.equals(required)) {
				if (fInputGroup.getViewer().getControl().isFocusControl()) {
					return fInputGroup.getViewer().getFindReplaceTarget();
				}
				return fOutputViewer.getFindReplaceTarget();
			}
		}
		if (ISourceEditor.class.equals(required)) {
			return fInputGroup;
		}
		if (IShowInSource.class.equals(required)) {
			return this;
		}
		if (IShowInTargetList.class.equals(required)) {
			return this;
		}
		return fConsole.getAdapter(required);
	}
	
	@Override
	public ShowInContext getShowInContext() {
		final IProcess process = fConsole.getProcess();
		if (process == null) {
			return null;
		}
		final IDebugTarget target = (IDebugTarget) process.getAdapter(IDebugTarget.class);
		ISelection selection = null;
		if (target == null) {
			selection = new TreeSelection(new TreePath(new Object[]{
					DebugPlugin.getDefault().getLaunchManager(),
					process.getLaunch(),
					process}));
		} else {
			selection = new TreeSelection(new TreePath(new Object[]{
					DebugPlugin.getDefault().getLaunchManager(),
					target.getLaunch(),
					target}));
		}
		return new ShowInContext(null, selection);
	}
	
	@Override
	public String[] getShowInTargetIds() {
		return new String[] { IDebugUIConstants.ID_DEBUG_VIEW };
	}
	
	
	@Override
	public void setActionBars(final IActionBars actionBars) {
//		fOutputViewer.setActionBars(actionBars);
	}
	
	@Override
	public void setFocus() {
		fInputGroup.getViewer().getControl().setFocus();
	}
	
	
	protected void onToolTerminated() {
		if (fIsCreated) {
			fTerminateAction.update();
			for (final Object action : fToolActions.getListeners()) {
				((IToolRetargetable) action).toolTerminated();
			}
			fOutputPasteAction.setEnabled(false);
			final Button button = fInputGroup.getSubmitButton();
			UIAccess.getDisplay(getSite().getShell()).asyncExec(new Runnable() {
				@Override
				public void run() {
					if (UIAccess.isOkToUse(button)) {
						button.setEnabled(false);
					}
				}
			});
			final IDialogSettings dialogSettings = DialogUtil.getDialogSettings(NicoUIPlugin.getDefault(), DIALOG_ID);
			dialogSettings.put(SETTING_INPUTHEIGHT, fResizer.fLastExplicit);
		}
	}
	
	@Override
	public void setAutoScroll(final boolean enabled) {
		fOutputViewer.setAutoScroll(enabled);
		fOutputScrollLockAction.setChecked(!enabled);
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		if (UIAccess.isOkToUse(fControl) ) {
			final Object source = event.getSource();
			final String property = event.getProperty();
			
			if (source.equals(fConsole) && IConsoleConstants.P_FONT.equals(property)) {
				final Font font = fConsole.getFont();
				fOutputViewer.setFont(font);
				fInputGroup.setFont(font);
				fResizer.fontChanged();
				fControl.layout();
			}
			else if (IConsoleConstants.P_FONT_STYLE.equals(property)) {
				fControl.redraw();
			}
			else if (property.equals(IConsoleConstants.P_STREAM_COLOR)) {
				fOutputViewer.getTextWidget().redraw();
			}
//			else if (source.equals(fConsole) && property.equals(IConsoleConstants.P_TAB_SIZE)) {
//				int tabSize = ((Integer) event.getNewValue()).intValue();
//				fOutputViewer.setTabWidth(tabSize);
//				fInputGroup.getSourceViewer().setTabWidth(tabSize);
//			}
			else if (source.equals(fConsole) && property.equals(IConsoleConstants.P_CONSOLE_WIDTH)) {
				fOutputViewer.setConsoleWidth(fConsole.getConsoleWidth());
			}
		}
	}
	
	@Override
	public void settingsChanged(final Set<String> groupIds) {
		final Map<String, Object> options = new HashMap<String, Object>();
		UIAccess.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				handleSettingsChanged(groupIds, options);
			}
		});
	}
	
	/**
	 * @see ISettingsChangedHandler#handleSettingsChanged(Set, Map)
	 */
	protected void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		if (fInputGroup != null && UIAccess.isOkToUse(fControl)) {
			fInputGroup.handleSettingsChanged(groupIds, options);
		}
	}
	
	protected void collectContextMenuPreferencePages(final List<String> pageIds) {
		pageIds.add("de.walware.statet.nico.preferencePages.Console"); //$NON-NLS-1$
	}
	
}
