/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ui.dialogs.DatabindingSupport;
import de.walware.ecommons.ui.dialogs.TitleAreaStatusUpdater;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.nico.core.runtime.IProgressInfo;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.ToolEventHandlerUtil;
import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;


/**
 * TODO: Test, Better support for remote engines, define constants, ...
 */
public class SelectFileHandler implements IToolEventHandler {
	
	
	private static class SelectFileDialog extends TitleAreaDialog {
		
		private ToolProcess fTool;
		private int fMode;
		
		private ResourceInputComposite fLocationGroup;
		private WritableValue fNewLocationString;
		private final String fHistoryId;
		
		
		public SelectFileDialog(final Shell shell, final ToolProcess tool, final String message, final boolean newFile) {
			super(shell);
			
			setTitle(message);
			setMessage(message);
			fTool = tool;
			fMode = newFile ? (ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_SAVE)
					: (ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_OPEN);
			fHistoryId = "statet:"+fTool.getMainType()+":location.commonfile"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		@Override
		protected void configureShell(final Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(Messages.Util_SelectFile_Dialog_title);
		}
		
		protected IDialogSettings getDialogSettings() {
			return DialogUtil.getDialogSettings(NicoUIPlugin.getDefault(), IToolEventHandler.SELECTFILE_EVENT_ID+"-Wizard");
		}
		
		@Override
		protected Control createDialogArea(Composite dialogArea) {
			dialogArea = (Composite) super.createDialogArea(dialogArea);
			
			LayoutUtil.addGDDummy(dialogArea);
			final Composite inputComposite = new Composite(dialogArea, SWT.NONE);
			inputComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			inputComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			fLocationGroup = new ResourceInputComposite(inputComposite,
					ResourceInputComposite.STYLE_COMBO,
					fMode,
					Messages.Util_SelectFile_File_label);
			fLocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fLocationGroup.setHistory(getDialogSettings().getArray(fHistoryId));
			
			LayoutUtil.addSmallFiller(dialogArea, true);
			
			final ToolInfoGroup info = new ToolInfoGroup(dialogArea, fTool);
			info.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			final DatabindingSupport databinding = new DatabindingSupport(dialogArea);
			addBindings(databinding);
			databinding.installStatusListener(new TitleAreaStatusUpdater(this));
			
			return dialogArea;
		}
		
		protected void addBindings(final DatabindingSupport db) {
			final IFileStore current = fTool.getWorkspaceData().getWorkspaceDir();
			String dir = ""; //$NON-NLS-1$
			if (current != null) {
				final IPath path = URIUtil.toPath(current.toURI());
				if (path != null) {
					dir = path.toOSString();
				}
			}
			fNewLocationString = new WritableValue(dir, String.class);
			db.getContext().bindValue(fLocationGroup.getObservable(), fNewLocationString,
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
	
	
	public int handle(final String id, final IToolRunnableControllerAdapter tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		final String message;
		{	String s = ToolEventHandlerUtil.getCheckedData(data, LOGIN_MESSAGE_DATA_KEY, String.class, false); 
			if (s == null) {
				final IProgressInfo progressInfo = tools.getController().getProgressInfo();
				s = NLS.bind("Select file (in {0}):", progressInfo.getLabel());
			}
			message = s;
		}
		final Boolean newFile = ToolEventHandlerUtil.getCheckedData(data, "newResource", Boolean.class, true); //$NON-NLS-1$
		final ToolProcess tool = tools.getController().getProcess();
		final AtomicReference<IFileStore> file = new AtomicReference<IFileStore>();
		final Runnable runnable = new Runnable() {
			public void run() {
				final SelectFileDialog dialog = new SelectFileDialog(UIAccess.getActiveWorkbenchShell(true),
						tool, message, newFile.booleanValue());
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
		data.put("filename", file.get().toURI().toString()); //$NON-NLS-1$
		return OK;
	}
	
}
