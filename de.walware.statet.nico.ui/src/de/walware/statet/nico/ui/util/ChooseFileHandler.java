/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.databinding.jface.DatabindingSupport;
import de.walware.ecommons.ts.util.ToolCommandHandlerUtil;
import de.walware.ecommons.ui.dialogs.TitleAreaStatusUpdater;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.nico.core.runtime.IConsoleService;
import de.walware.statet.nico.core.runtime.IProgressInfo;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.AbstractConsoleCommandHandler;
import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;


/**
 * TODO: Better support for remote engines (resource mapping); disable OK button on errors?
 */
public class ChooseFileHandler extends AbstractConsoleCommandHandler {
	
	
	public static final String CHOOSE_FILE_ID = "common/chooseFile"; //$NON-NLS-1$
	
	
	private static class ChooseFileDialog extends ToolDialog {
		
		private final int fMode;
		
		private final String fMessage;
		
		private ResourceInputComposite fLocationGroup;
		private WritableValue fNewLocationString;
		private final String fHistoryId;
		
		
		public ChooseFileDialog(final Shell shell, final ToolProcess tool, final String message, final boolean newFile) {
			super(tool, shell, null, Messages.Util_ChooseFile_Dialog_title);
			
			fMode = newFile ? (ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_SAVE)
					: (ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_OPEN);
			fMessage = message;
			fHistoryId = "statet:"+getTool().getMainType()+":location.commonfile"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		
		@Override
		protected IDialogSettings getDialogSettings() {
			return DialogUtil.getDialogSettings(NicoUIPlugin.getDefault(), "tools/ChooseFileDialog"); //$NON-NLS-1$
		}
		
		@Override
		protected Control createDialogContent(final Composite parent) {
			setTitle(fMessage);
			
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
			
			final Composite inputComposite = new Composite(composite, SWT.NONE);
			inputComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			inputComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			fLocationGroup = new ResourceInputComposite(inputComposite,
					ResourceInputComposite.STYLE_COMBO,
					fMode,
					Messages.Util_ChooseFile_File_label);
			fLocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			final String[] history = getDialogSettings().getArray(fHistoryId);
			fLocationGroup.setHistory(history);
			
			final DatabindingSupport databinding = new DatabindingSupport(composite);
			addBindings(databinding);
			
			fNewLocationString.setValue((history != null && history.length > 0) ? history[0] : ""); //$NON-NLS-1$
			
			databinding.installStatusListener(new TitleAreaStatusUpdater(this, fMessage));
			
			return composite;
		}
		
		protected void addBindings(final DatabindingSupport db) {
			final IFileStore current = getTool().getWorkspaceData().getWorkspaceDir();
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
		
		@Override
		protected void okPressed() {
			saveSettings();
			super.okPressed();
		}
		
		public void saveSettings() {
			final IDialogSettings settings = getDialogSettings();
			DialogUtil.saveHistorySettings(settings, fHistoryId, (String) fNewLocationString.getValue());
		}
		
		public IFileStore getResource() {
			return fLocationGroup.getResourceAsFileStore();
		}
		
	}
	
	
	@Override
	public IStatus execute(final String id, final IConsoleService service, final Map<String, Object> data, final IProgressMonitor monitor) {
		final String message;
		{	String s = ToolCommandHandlerUtil.getCheckedData(data, "message", String.class, false);  //$NON-NLS-1$
			if (s == null) {
				final IProgressInfo progressInfo = service.getController().getProgressInfo();
				s = NLS.bind("Choose file (asked by {0}):", progressInfo.getLabel());
			}
			message = s;
		}
		final Boolean newFile = ToolCommandHandlerUtil.getCheckedData(data, "newResource", Boolean.class, true); //$NON-NLS-1$
		final AtomicReference<IFileStore> file = new AtomicReference<IFileStore>();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				final ChooseFileDialog dialog = new ChooseFileDialog(UIAccess.getActiveWorkbenchShell(true),
						service.getTool(), message, newFile.booleanValue());
				dialog.setBlockOnOpen(true);
				if (dialog.open() == Dialog.OK) {
					file.set(dialog.getResource());
				}
			}
		};
		UIAccess.getDisplay().syncExec(runnable);
		if (file.get() == null) {
			return Status.CANCEL_STATUS;
		}
		{	final String fileName = file.get().toString();
			data.put("filename", fileName); //$NON-NLS-1$
			data.put("fileName", fileName); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
	}
	
}
