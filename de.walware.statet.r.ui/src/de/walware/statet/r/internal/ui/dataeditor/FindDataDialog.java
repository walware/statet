/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.dataeditor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.dataeditor.RDataTableComposite;


public class FindDataDialog extends ExtStatusDialog {
	
	
	private static final Map<IWorkbenchWindow, FindDataDialog> gDialogs= new HashMap<>();
	
	
	public static FindDataDialog get(final IWorkbenchWindow window, final boolean create) {
		FindDataDialog dialog = gDialogs.get(window);
		if (dialog == null && create) {
			dialog = new FindDataDialog(window);
			dialog.create();
			gDialogs.put(window, dialog);
		}
		return dialog;
	}
	
	
	private static final int FIND_NEXT_ID = 101;
	private static final int FIND_PREVIOUS_ID = 102;
	
	
	private class PartListener implements IWindowListener, IPartListener {
		
		@Override
		public void windowOpened(final IWorkbenchWindow window) {
		}
		@Override
		public void windowClosed(final IWorkbenchWindow window) {
			if (FindDataDialog.this.fWindow == window) {
				close();
			}
		}
		@Override
		public void windowActivated(final IWorkbenchWindow window) {
		}
		@Override
		public void windowDeactivated(final IWorkbenchWindow window) {
		}
		
		@Override
		public void partOpened(final IWorkbenchPart part) {
		}
		@Override
		public void partClosed(final IWorkbenchPart part) {
			if (FindDataDialog.this.fPart == part) {
				FindDataDialog.this.fPart = null;
			}
		}
		@Override
		public void partActivated(final IWorkbenchPart part) {
			FindDataDialog.this.fPart = part;
			update(part);
		}
		@Override
		public void partDeactivated(final IWorkbenchPart part) {
		}
		@Override
		public void partBroughtToTop(final IWorkbenchPart part) {
		}
		
	}
	
	
	private final IWorkbenchWindow fWindow;
	private IWorkbenchPart fPart;
	
	private final PartListener fPartListener = new PartListener();
	
	private RDataTableComposite fTable;
	
	private Combo fTextControl;
	private Button fRExpressionModeControl;
	private Button fIsNaModeControl;;
	private Button fDirectionFirstInColumnControl;
	private Button fDirectionFirstInRowControl;
	private Button fSelectedOnlyControl;
	
	private final Map<String, String> fHistoryModeMap= new HashMap<>();
	
	
	private final IFindListener fFindListener = new IFindListener() {
		@Override
		public void handleFindEvent(final IFindListener.FindEvent event) {
			updateStatus(event.status);
		}
	};
	
	
	protected FindDataDialog(final IWorkbenchWindow window) {
		super(window.getShell());
		
		this.fWindow = window;
		this.fWindow.getWorkbench().addWindowListener(this.fPartListener);
		this.fWindow.getActivePage().addPartListener(this.fPartListener);
		this.fPart = this.fWindow.getActivePage().getActivePart();
		
		setTitle("Find");
		setShellStyle((getShellStyle() & ~SWT.APPLICATION_MODAL) | SWT.MODELESS);
		setBlockOnOpen(false);
		setStatusLineAboveButtons(false);
		setHelpAvailable(false);
	}
	
	
	protected IDialogSettings getDialogSettings() {
		return DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "RDataTable.FindDialog");
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return getDialogSettings();
	}
	
	@Override
	public void create() {
		super.create();
		
		loadSettings();
	}
	
	@Override
	public boolean close() {
		this.fWindow.getWorkbench().removeWindowListener(this.fPartListener);
		final IWorkbenchPage page = this.fWindow.getActivePage();
		if (page != null) {
			page.removePartListener(this.fPartListener);
		}
		
		gDialogs.remove(this.fWindow);
		saveSettings();
		
		return super.close();
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.createDialogGrid(2));
		
		final Composite textInput = createTextInput(composite);
		textInput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		final Composite additionalOptions = createAdditionalOptions(composite);
		additionalOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		final Composite directionOptions = createDirectionOptions(composite);
		directionOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
//		final Composite scopeOptions = createScopeOptions(composite);
//		scopeOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		
		LayoutUtil.addSmallFiller(composite, false);
		final Composite navigateButtons = createNavigateButtons(composite);
		navigateButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		
		applyDialogFont(composite);
		return composite;
	}
	
	protected Composite createTextInput(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createCompositeGrid(2));
		
		final Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText("Find:");
		
		this.fTextControl = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
		this.fTextControl.setLayoutData(LayoutUtil.hintWidth(
				new GridData(SWT.FILL, SWT.CENTER, true, false), this.fTextControl, 25 ));
		
		this.fTextControl.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				updateStatus(null);
				updateState();
			}
		});
		this.fTextControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final int selectionIdx;
				if (!FindDataDialog.this.fTextControl.getListVisible()
						&& (selectionIdx = FindDataDialog.this.fTextControl.getSelectionIndex()) >= 0) {
					loadQuery(FindDataDialog.this.fTextControl.getItem(selectionIdx));
				}
			}
		});
		
		return composite;
	}
	
	protected Composite createDirectionOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setText("Direction");
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(0, true), 2));
		
		{	this.fDirectionFirstInColumnControl = new Button(composite, SWT.RADIO);
			this.fDirectionFirstInColumnControl.setText("&Column, Row");
			this.fDirectionFirstInColumnControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			this.fDirectionFirstInColumnControl.setSelection(true);
		}
		{	this.fDirectionFirstInRowControl = new Button(composite, SWT.RADIO);
			this.fDirectionFirstInRowControl.setText("&Row, Column");
			this.fDirectionFirstInRowControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		{	this.fSelectedOnlyControl = new Button(composite, SWT.CHECK);
			this.fSelectedOnlyControl.setText("Only Selec&ted Cells");
			this.fSelectedOnlyControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}
		
		return composite;
	}
	
	protected Composite createScopeOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setText("Scope");
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		{	final Button button = new Button(composite, SWT.RADIO);
			button.setText("A&ll");
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			button.setSelection(true);
		}
		{	this.fSelectedOnlyControl = new Button(composite, SWT.CHECK);
			this.fSelectedOnlyControl.setText("Selec&ted Cells");
			this.fSelectedOnlyControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		return composite;
	}
	
	protected Composite createAdditionalOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setText("Options");
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(0, true), 2));
		
		{	this.fRExpressionModeControl = new Button(composite, SWT.CHECK);
			this.fRExpressionModeControl.setText("R expression");
			this.fRExpressionModeControl.setToolTipText("Use x to reference the object itself, e.g. 'x >= 1'");
			this.fRExpressionModeControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			this.fRExpressionModeControl.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					if (FindDataDialog.this.fRExpressionModeControl.getSelection()) {
						FindDataDialog.this.fIsNaModeControl.setSelection(false);
					}
				}
			});
		}
		
		{	this.fIsNaModeControl = new Button(composite, SWT.CHECK);
			this.fIsNaModeControl.setText("Is NA");
			this.fIsNaModeControl.setToolTipText("The search expression is ignored");
			this.fIsNaModeControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			this.fIsNaModeControl.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					if (FindDataDialog.this.fIsNaModeControl.getSelection()) {
						FindDataDialog.this.fRExpressionModeControl.setSelection(false);
					}
				}
			});
		}
		
		return composite;
	}
	
	protected Composite createNavigateButtons(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 0));
		
		createButton(composite, FIND_PREVIOUS_ID, "&Previous", false);
		createButton(composite, FIND_NEXT_ID, "&Next", true);
		
		return composite;
	}
	
	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
	}
	
	@Override
	protected void buttonPressed(final int buttonId) {
		switch (buttonId) {
		case IDialogConstants.CLOSE_ID:
			close();
			return;
		case FIND_NEXT_ID:
			doFind(true);
			return;
		case FIND_PREVIOUS_ID:
			doFind(false);
			return;
		}
	}
	
	private void loadQuery(final String text) {
		if (text == null || text.isEmpty()) {
			return;
		}
		
		final String mode = this.fHistoryModeMap.get(text);
		if (mode != null) {
			if ("r_expression".equals(mode)) {
				this.fRExpressionModeControl.setSelection(true);
				this.fIsNaModeControl.setSelection(false);
			}
			else {
				this.fRExpressionModeControl.setSelection(false);
			}
		}
	}
	
	private void doFind(final boolean forward) {
		final String expression = getExpression();
		if (!expression.equals("is.na(x)")) {
			final String text = this.fTextControl.getText();
			final int idx = this.fTextControl.indexOf(text);
			if (idx != 0) {
				if (idx > 0) {
					this.fTextControl.remove(idx);
				}
				this.fTextControl.add(text, 0);
				this.fTextControl.setText(text);
			}
			this.fHistoryModeMap.put(text,
					this.fRExpressionModeControl.getSelection() ? "r_expression" : "default");
		}
		this.fTable.find(expression,
				this.fSelectedOnlyControl.getSelection(),
				this.fDirectionFirstInRowControl.getSelection(),
				forward);
	}
	
	protected String getExpression() {
		if (this.fRExpressionModeControl.getSelection()) {
			return this.fTextControl.getText();
		}
		else if (this.fIsNaModeControl.getSelection()) {
			return "is.na(x)";
		}
		else {
			return "x == " + this.fTextControl.getText();
		}
	}
	
	protected void loadSettings() {
		final IDialogSettings settings = getDialogSettings();
		final String[] history = DialogUtil.noNull(settings.getArray("SearchText.history"));
		this.fTextControl.setItems(history);
		final String[] historyModes = DialogUtil.noNull(settings.getArray("SearchMode.history"));
		if (history.length == historyModes.length) {
			for (int i = 0; i < history.length; i++) {
				this.fHistoryModeMap.put(history[i], historyModes[i]);
			}
		}
		if (history.length > 0) {
			this.fTextControl.setText(history[0]);
			loadQuery(history[0]);
		}
		if (settings.getBoolean("Direction.firstInRow")) {
			this.fDirectionFirstInColumnControl.setSelection(false);
			this.fDirectionFirstInRowControl.setSelection(true);
		}
		else {
			this.fDirectionFirstInColumnControl.setSelection(true);
			this.fDirectionFirstInRowControl.setSelection(false);
		}
	}
	
	protected void saveSettings() {
		final IDialogSettings settings = getDialogSettings();
		final String[] history = DialogUtil.combineHistoryItems(this.fTextControl.getItems(), null);
		settings.put("SearchText.history", history);
		final String[] historyModes = new String[history.length];
		for (int i = 0; i < history.length; i++) {
			String mode = this.fHistoryModeMap.get(history[i]);
			if (mode == null) {
				mode = "default";
			}
			historyModes[i] = mode;
		}
		settings.put("SearchMode.history", historyModes);
		settings.put("Direction.firstInRow", this.fDirectionFirstInRowControl.getSelection());
	}
	
	
	public void update(final IWorkbenchPart part) {
		if (part != null && this.fPart == part) {
			final RDataTableComposite table = (RDataTableComposite) part.getAdapter(RDataTableComposite.class);
			if (this.fTable != null && this.fTable != table) {
				this.fTable.removeFindListener(this.fFindListener);
				updateStatus(null);
			}
			
			this.fTable = table;
			if (this.fTable != null) {
				table.addFindListener(this.fFindListener);
			}
			updateState();
		}
	}
	
	private void updateState() {
		final boolean enabled = (this.fTable != null && this.fTextControl.getText().length() > 0);
		getButton(FIND_NEXT_ID).setEnabled(enabled);
		getButton(FIND_PREVIOUS_ID).setEnabled(enabled);
	}
	
}
