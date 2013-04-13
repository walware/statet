/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.pkgmanager;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.components.StatusInfo;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.NestedServices;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.ui.util.ToolDialog;
import de.walware.statet.nico.ui.util.ToolInfoGroup;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.pkgmanager.ISelectedRepos;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.pkgmanager.StartAction;


public class RPkgManagerDialog extends ToolDialog implements IChangeListener, IRPkgManager.Listener {
	
	static final int APPLY_ID = 10;
	static final int INSTFILE_ID = 12;
	
	static final Object[] NO_INPUT = new Object[0];
	
	
	private final IRPkgManager.Ext fRPkgManager;
	
	Display fDisplay;
	
	IStatus fStatus;
	
	private TabFolder fTabFolder;
	PkgTab fPkgTab;
	RepoTab fRepoTab;
	OptionsTab fOptionsTab;
	
	private Button fApplyButton;
	
	private final Object fUpdateLock = new Object();
	private boolean fUpdateRepos;
	private boolean fUpdatePkgs;
	private boolean fUpdateState;
	private int fUpdatePage;
	
	private StartAction fStartAction;
	
	NestedServices fServiceLocator;
	
	
	public RPkgManagerDialog(final IRPkgManager.Ext rPkgManager, final RProcess rProcess,
			final Shell parentShell) {
		super(rProcess, parentShell, null, "R Package Manager", ToolInfoGroup.WIDE);
		
		fRPkgManager = rPkgManager;
	}
	
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "pkgmanager/MainDialog"); //$NON-NLS-1$
	}
	
	
	@Override
	protected RProcess getTool() {
		return (RProcess) super.getTool();
	}
	
	@Override
	protected void setShellStyle(final int newShellStyle) {
		super.setShellStyle((newShellStyle & ~SWT.APPLICATION_MODAL) | SWT.MIN | SWT.MAX);
	}
	
	@Override
	protected Control createContents(final Composite parent) {
		final Control control = super.createContents(parent);
		
		setTitle(NLS.bind("R Package Manager for ''{0}''", fRPkgManager.getREnv().getName()));
		
		setTabFocus();
		
		if (!fRPkgManager.getReposStatus(null).isOK()) {
			activateTab(fRepoTab.getTab());
		}
		else {
			onTabSelected(fPkgTab.getTab());
		}
		
		fDisplay = parent.getDisplay();
		fRPkgManager.addListener(this);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				fRPkgManager.removeListener(RPkgManagerDialog.this);
			}
		});
		
		fServiceLocator = new NestedServices.Dialog(getShell());
		fPkgTab.createActions();
		
		updateStatus();
		
		return control;
	}
	
	@Override
	protected Control createDialogContent(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createCompositeGrid(1));
		
		fTabFolder = new TabFolder(composite, SWT.TOP);
		fTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		{	final TabItem tabItem = new TabItem(fTabFolder, SWT.NONE);
			tabItem.setText("&Packages");
			fPkgTab = new PkgTab(this, tabItem, fTabFolder, fRPkgManager);
			tabItem.setControl(fPkgTab);
		}
		{	final TabItem tabItem = new TabItem(fTabFolder, SWT.NONE);
			tabItem.setText("&Repositories");
			fRepoTab = new RepoTab(this, tabItem, fTabFolder);
			tabItem.setControl(fRepoTab);
		}
		{	final TabItem tabItem = new TabItem(fTabFolder, SWT.NONE);
			tabItem.setText("&Options");
			fOptionsTab = new OptionsTab(this, tabItem, fTabFolder);
			tabItem.setControl(fOptionsTab);
		}
		
		applyDialogFont(composite);
		
		final DataBindingSupport databinding = new DataBindingSupport(parent);
		addBindings(databinding);
		
		fUpdateRepos = fUpdatePkgs = fUpdateState = true;
		update();
		fRepoTab.init();
		
		fTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				onTabSelected((TabItem) e.item);
			}
		});
		
		return composite;
	}
	
	public void activateTab(final TabItem tab) {
		fTabFolder.setSelection(tab);
		onTabSelected(tab);
	}
	
	private void onTabSelected(final TabItem tab) {
		fApplyButton.setVisible(fRepoTab.getTab() == tab);
		checkAction();
	}
	
	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, INSTFILE_ID, "Install from file...", false);
		createButton(parent, 99, "", false).setVisible(false); //$NON-NLS-1$
		fApplyButton = createButton(parent, APPLY_ID, "&Apply", false);
//		super.createButtonsForButtonBar(parent);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
	}
	
	private void setTabFocus() {
		final Display display = Display.getCurrent();
		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (display.getFocusControl() == fTabFolder) {
					final int idx = fTabFolder.getSelectionIndex();
					if (idx >= 0) {
						fTabFolder.getItem(idx).getControl().setFocus();
					}
				}
			}
		});
	}
	
	protected void initBindings(final Control control) {
		// databinding.installStatusListener(new StatusUpdater());
	}
	
	
	int hintWidthInChars(final int chars) {
		return convertWidthInCharsToPixels(chars);
	}
	
	void openPrefPage(final String pageId) {
		final Control content = getContents();
		final PreferenceDialog dialog = org.eclipse.ui.dialogs.PreferencesUtil
				.createPreferenceDialogOn((content != null) ? content.getShell() : null,
						pageId, null, null);
		dialog.open();
		if (content != null) {
			content.setFocus();
		}
	}
	
	
	protected void addBindings(final DataBindingSupport db) {
		fRepoTab.addBindings(db);
		
		fPkgTab.addBinding(db);
	}
	
	private void update() {
		boolean updateRepos;
		boolean updatePkgs;
		boolean updateState;
		synchronized (fUpdateLock) {
			updateRepos = fUpdateRepos;
			fUpdateRepos = false;
			updatePkgs = fUpdatePkgs;
			fUpdatePkgs = false;
			updateState = fUpdateState;
			fUpdateState = false;
			if (!updateRepos && updatePkgs && !updateState) {
				return;
			}
		}
		
		fRPkgManager.getReadLock().lock();
		try {
			if (updateRepos) {
				fRepoTab.updateSettings(fRPkgManager);
			}
			
			if (updatePkgs) {
				fPkgTab.updateSettings(fRPkgManager);
				checkAction();
			}
			updateStatus();
		}
		finally {
			fRPkgManager.getReadLock().unlock();
		}
	}
	
	@Override
	// data binding
	public void handleChange(final ChangeEvent event) {
		updateStatus();
	}
	
	@Override
	// core pkg manager
	public void handleChange(final IRPkgManager.Event event) {
		synchronized (fUpdateLock) {
			fUpdateRepos |= (event.reposChanged() > 0);
			fUpdatePkgs |= (event.pkgsChanged() > 0);
			fUpdatePkgs |= (event.viewsChanged() > 0);
			fUpdateState |= true;
		}
		if (!fDisplay.isDisposed()) {
			fDisplay.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (UIAccess.isOkToUse(getContents())) {
						update();
					}
				}
			});
		}
	}
	
	private void updateStatus() {
		final ISelectedRepos repoSettings = fRepoTab.createRepoSettings();
		final IStatus status = fRPkgManager.getReposStatus(repoSettings);
		if (!status.isOK()) {
			setStatus(status, fRepoTab.getTab());
			return;
		}
		setStatus(new StatusInfo(IStatus.OK, "Install and Update R Packages"), fPkgTab.getTab());
	}
	
	private void setStatus(final IStatus status, final TabItem tab) {
		fStatus = status;
		if (!UIAccess.isOkToUse(getButtonBar())) {
			return;
		}
		StatusInfo.applyToStatusLine(this, status);
		final boolean apply = (status.getSeverity() != IStatus.ERROR);
		getButton(IDialogConstants.OK_ID).setEnabled(apply);
		getButton(APPLY_ID).setEnabled(apply);
		
		fPkgTab.updateStatus(status);
		
		if (tab != null && fUpdatePage >= 0 && fUpdatePage == fTabFolder.getSelectionIndex()
				&& fTabFolder.getItem(fUpdatePage) != tab) {
			activateTab(tab);
		}
		fUpdatePage = -1;
	}
	
	@Override
	protected void buttonPressed(final int buttonId) {
		switch (buttonId) {
		case APPLY_ID:
			doApply(true);
			break;
		case INSTFILE_ID:
			doInstFile();
			break;
		}
		super.buttonPressed(buttonId);
	}
	
	void doApply(final boolean forceRefresh) {
		fRPkgManager.getWriteLock().lock();
		try {
			final ISelectedRepos repoSettings = fRepoTab.createRepoSettings();
			fRPkgManager.setSelectedRepos(repoSettings);
			if (forceRefresh) {
				fRPkgManager.refreshPkgs();
			}
			
			final int page = fTabFolder.getSelectionIndex();
			fDisplay.asyncExec(new Runnable() {
				@Override
				public void run() {
					fUpdatePage = page;
				}
			});
			fRPkgManager.apply(getTool());
		}
		finally {
			fRPkgManager.getWriteLock().unlock();
		}
	}
	
	void doInstFile() {
		final IREnvConfiguration config = fRPkgManager.getREnv().getConfig();
		if (config == null) {
			return;
		}
		final InstallPkgFileWizard wizard = new InstallPkgFileWizard(getTool(), fRPkgManager);
		final WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.setBlockOnOpen(true);
		dialog.open();
	}
	
	@Override
	protected void okPressed() {
		doApply(false);
		super.okPressed();
	}
	
	public void start(final StartAction action) {
		fStartAction = action;
		if (fPkgTab != null && fTabFolder.getSelectionIndex() >= 0) {
			checkAction();
		}
	}
	
	private void checkAction() {
		if (fStartAction != null) {
			if (fStatus.getSeverity() == IStatus.OK
					&& fPkgTab.getPkgSet() != IRPkgSet.DUMMY) {
				final StartAction action = fStartAction;
				fStartAction = null;
				switch (action.getAction()) {
				case StartAction.INSTALL:
					fPkgTab.install(action.getPkgNames());
					break;
				case StartAction.REINSTALL:
					fPkgTab.reinstallAll();
					break;
				}
			}
		}
	}
	
}
