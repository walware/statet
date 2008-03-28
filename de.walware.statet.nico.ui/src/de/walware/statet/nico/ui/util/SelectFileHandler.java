/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.ui.databinding.TitleAreaDialogWithDbc;
import de.walware.eclipsecommons.ui.dialogs.ChooseResourceComposite;
import de.walware.eclipsecommons.ui.util.DialogUtil;
import de.walware.eclipsecommons.ui.util.LayoutUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IProgressInfo;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;


/**
 * TODO: Test
 */
public class SelectFileHandler implements IToolEventHandler {
	
	
	private class SelectFileDialog extends TitleAreaDialogWithDbc {
		
		private ToolProcess fTool;
		private int fMode;
		
		private ChooseResourceComposite fLocationGroup;
		private WritableValue fNewLocationString;
		private final String fHistoryId;
		
		
		public SelectFileDialog(final Shell shell, final ToolProcess tool, final String message, final boolean newFile) {
			super(shell);
			
			setTitle(message);
			setMessage(message);
			setDialogSettings(DialogUtil.getDialogSettings(NicoUIPlugin.getDefault(), IToolEventHandler.SELECTFILE_EVENT_ID+"-Wizard")); //$NON-NLS-1$
			fTool = tool;
			fMode = newFile ? (ChooseResourceComposite.MODE_FILE | ChooseResourceComposite.MODE_SAVE)
					: (ChooseResourceComposite.MODE_FILE | ChooseResourceComposite.MODE_OPEN);
			fHistoryId = "statet:"+fTool.getMainType()+":location.commonfile"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		@Override
		protected void configureShell(final Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(Messages.Util_SelectFile_Dialog_title);
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			parent = (Composite) super.createDialogArea(parent);
			
			LayoutUtil.addGDDummy(parent);
			final Composite inputComposite = new Composite(parent, SWT.NONE);
			inputComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			inputComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			fLocationGroup = new ChooseResourceComposite(inputComposite,
					ChooseResourceComposite.STYLE_COMBO,
					fMode,
					Messages.Util_SelectFile_File_label);
			fLocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fLocationGroup.setHistory(getDialogSettings().getArray(fHistoryId));
			
			LayoutUtil.addSmallFiller(parent, true);
			
			final ToolInfoGroup info = new ToolInfoGroup(parent, fTool);
			info.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			initBindings();
			return parent;
		}
		
		@Override
		protected void addBindings(final DataBindingContext dbc, final Realm realm) {
			final IFileStore current = fTool.getWorkspaceData().getWorkspaceDir();
			String dir = ""; //$NON-NLS-1$
			if (current != null) {
				final IPath path = URIUtil.toPath(current.toURI());
				if (path != null) {
					dir = path.toOSString();
				}
			}
			fNewLocationString = new WritableValue(dir, String.class);
			dbc.bindValue(fLocationGroup.createObservable(), fNewLocationString,
					new UpdateValueStrategy().setAfterGetValidator(fLocationGroup.getValidator()), null);
		}
		
//		@Override
//		protected void okPressed() {
//			super.okPressed();
//		}
		
		public void saveSettings() {
			final IDialogSettings settings = getDialogSettings();
			DialogUtil.saveHistorySettings(settings, fHistoryId, (String) fNewLocationString.getValue());
		}
		
		public IFileStore getResource() {
			return fLocationGroup.getResourceAsFileStore();
		}
		
	}
	
	
	public int handle(final IToolRunnableControllerAdapter tools, final Object contextData) {
		final SelectFileEventData data = (SelectFileEventData) contextData;
		if (data.message == null) {
			final IProgressInfo progressInfo = tools.getController().getProgressInfo();
			data.message = NLS.bind("Select file (in {0}):", progressInfo.getLabel());
		}
		final ToolProcess tool = tools.getController().getProcess();
		final AtomicReference<IFileStore> file = new AtomicReference<IFileStore>();
		final Runnable runnable = new Runnable() {
			public void run() {
				final SelectFileDialog dialog = new SelectFileDialog(UIAccess.getActiveWorkbenchShell(true),
						tool, data.message, data.newFile);
				dialog.setBlockOnOpen(true);
				if (dialog.open() == Dialog.OK) {
					file.set(dialog.getResource());
				}
			}
		};
		UIAccess.getDisplay().syncExec(runnable);
		if (file.get() == null) {
			return CANCEL;
		}
		data.filename = file.get().toURI().toString();
		return OK;
	}
	
	protected int handleError(final IStatus status) {
		StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
		return ERROR;
	}
	
}
