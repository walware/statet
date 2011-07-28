/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.sourcelookup;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.databinding.jface.DatabindingSupport;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.r.debug.core.sourcelookup.RLibrarySourceContainer;


public class RLibrarySourceContainerBrowser extends
		AbstractSourceContainerBrowser {
	
	
	private static class EditContainerDialog extends ExtStatusDialog {
		
		
		private ResourceInputComposite fResourceControl;
		
		private final WritableValue fResourceValue;
		
		
		public EditContainerDialog(final Shell parent, final String location) {
			super(parent);
			setTitle((location == null) ? Messages.RLibrarySourceContainerBrowser_Add_title :
					Messages.RLibrarySourceContainerBrowser_Edit_title );
			
			fResourceValue = new WritableValue(location, String.class);
		}
		
		
		@Override
		protected Control createContents(final Composite parent) {
			final Control control = super.createContents(parent);
			initBindings();
			return control;
		}
		
		@Override
		protected Control createDialogArea(final Composite parent) {
			final Composite dialogArea = new Composite(parent, SWT.NONE);
			dialogArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
			dialogArea.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 1));
			
			final Composite composite = dialogArea;
			{	final Label label = new Label(dialogArea, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				label.setText(Messages.RLibrarySourceContainerBrowser_Directory_label+':');
				
				fResourceControl = new ResourceInputComposite(composite, ResourceInputComposite.STYLE_TEXT,
						ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN,
						Messages.RLibrarySourceContainerBrowser_Directory_label);
				fResourceControl.setShowInsertVariable(false, DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS, null);
				final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
				gd.widthHint = LayoutUtil.hintWidth((Text) fResourceControl.getTextControl(), 60);
				fResourceControl.setLayoutData(gd);
			}
			return dialogArea;
		}
		
		@Override
		protected void addBindings(final DatabindingSupport db) {
			db.getContext().bindValue(fResourceControl.getObservable(), fResourceValue,
					new UpdateValueStrategy().setAfterGetValidator(fResourceControl.getValidator()), null);
		}
		
		public String getResult() {
			return (String) fResourceValue.getValue();
		}
	}
	
	
	/** Created via extension point */
	public RLibrarySourceContainerBrowser() {
	}
	
	
	@Override
	public ISourceContainer[] addSourceContainers(final Shell shell,
			final ISourceLookupDirector director) {
		final EditContainerDialog dialog = new EditContainerDialog(shell, null);
		if (dialog.open() == Dialog.OK) {
			final String location = dialog.getResult();
			if (location != null) {
				return new ISourceContainer[] { new RLibrarySourceContainer(location) };
			}
		}
		return new ISourceContainer[0];
	}
	
	@Override
	public boolean canEditSourceContainers(final ISourceLookupDirector director,
			final ISourceContainer[] containers) {
		return (containers.length == 1);
	}
	
	@Override
	public ISourceContainer[] editSourceContainers(final Shell shell,
			final ISourceLookupDirector director, final ISourceContainer[] containers) {
		final EditContainerDialog dialog = new EditContainerDialog(shell,
				((RLibrarySourceContainer) containers[0]).getLocationPath());
		if (dialog.open() == Dialog.OK) {
			final String location = dialog.getResult();
			if (location != null) {
				return new ISourceContainer[] { new RLibrarySourceContainer(location) };
			}
		}
		return containers;
	}
	
}
