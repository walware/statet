/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.preferences;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.base.core.StatetCore;


public abstract class AbstractConfigurationBlock {
	
	
	public static void scheduleChangeNotification(final IWorkbenchPreferenceContainer container, final String[] groupIds, final boolean directly) {
		if (groupIds != null) {
			final String source = (directly) ? null : container.toString();
			final Job job = StatetCore.getSettingsChangeNotifier().getNotifyJob(source, groupIds);
			if (job == null) {
				return;
			}
			if (directly) {
				job.schedule();
			}
			else {
				container.registerUpdateJob(job);
			}
		}
	}
	
	
	private Shell fShell;
	private IWorkbenchPreferenceContainer fContainer;
	protected boolean fUseProjectSettings = true;
	
	
	protected AbstractConfigurationBlock() {
	}
	
	
	public void createContents(final Composite pageComposite, final IWorkbenchPreferenceContainer container,
			final IPreferenceStore preferenceStore) {
		fShell = pageComposite.getShell();
		fContainer = container;
	}
	
	public void dispose() {
	}
	
	public void performApply() {
		performOk();
	}
	
	public abstract boolean performOk();
	
	public abstract void performDefaults();
	
	public void performCancel() {
	}
	
	public void setUseProjectSpecificSettings(final boolean enable) {
		fUseProjectSettings = enable;
	}
	
	protected Shell getShell() {
		return fShell;
	}
	
	protected void addLinkHeader(final Composite pageComposite, final String text) {
		final Link link = addLinkControl(pageComposite, text);
		final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.widthHint = 150; // only expand further if anyone else requires it
		link.setLayoutData(gridData);
		LayoutUtil.addSmallFiller(pageComposite, false);
	}
	
	protected Link addLinkControl(final Composite composite, final String text) {
		final Link link = new Link(composite, SWT.NONE);
		link.setText(text);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(getShell(), e.text, null, null);
			}
		});
		return link;
	}
	
	protected void scheduleChangeNotification(final Set<String> groupIds, final boolean directly) {
		scheduleChangeNotification(fContainer, groupIds.toArray(new String[groupIds.size()]), directly);
	}
	
	protected void logSaveError(final BackingStoreException e) {
		StatusManager.getManager().handle(new Status(IStatus.ERROR,
				"org.osgi.service.prefs", ICommonStatusConstants.INTERNAL_PREF_PERSISTENCE, //$NON-NLS-1$
				"An error occurred when saving preferences to backing store.", e)); //$NON-NLS-1$
	}
	
}
