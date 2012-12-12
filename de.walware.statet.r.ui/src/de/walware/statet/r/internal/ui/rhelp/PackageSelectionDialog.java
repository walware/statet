/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rhelp;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;

import de.walware.ecommons.ui.components.SearchText;
import de.walware.ecommons.ui.content.SearchTextBinding;
import de.walware.ecommons.ui.content.TableFilterController;
import de.walware.ecommons.ui.content.TextFilterProvider;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.CheckboxTableComposite;

import de.walware.statet.r.core.rhelp.IRPackageHelp;


public class PackageSelectionDialog extends SelectionDialog {
	
	
	private final List<IRPackageHelp> fInput;
	
	private SearchText fFilterText;
	private CheckboxTableViewer fViewer;
	private TableFilterController fFilterController;
	
	private final List<IRPackageHelp> fSelection;
	
	
	
	protected PackageSelectionDialog(final Shell parentShell,
			final List<IRPackageHelp> packages, final List<IRPackageHelp> initialSelection) {
		super(parentShell);
		fInput = packages;
		fSelection = initialSelection;
		setTitle(Messages.PackageSelection_title);
		setMessage(Messages.PackageSelection_message);
	}
	
	
	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.LIST_SELECTION_DIALOG);
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.createDialogGrid(1));
		
		initializeDialogUnits(composite);
		
		createMessageArea(composite);
		
		fFilterText = new SearchText(composite);
		fFilterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fFilterText.setToolTipText("Filter");
		
		final CheckboxTableComposite tableComposite = new CheckboxTableComposite(composite,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.VIRTUAL);
		final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = LayoutUtil.hintHeight(tableComposite.table, 10);
		data.widthHint = LayoutUtil.hintWidth(tableComposite.table, 40);
		tableComposite.setLayoutData(data);
		fViewer = tableComposite.viewer;
		
		final TableViewerColumn column = tableComposite.addColumn("Name", SWT.LEFT, new ColumnWeightData(1));
		column.setLabelProvider(new RHelpLabelProvider());
		ColumnViewerToolTipSupport.enableFor(tableComposite.viewer);
		
		fViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(final CheckStateChangedEvent event) {
				final Object element = event.getElement();
				if (element instanceof IRPackageHelp) {
					final IRPackageHelp pkg = (IRPackageHelp) element;
					if (!fSelection.remove(pkg)) {
						fSelection.add(pkg);
					}
				}
			}
		});
		fViewer.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(final Object element) {
				return false;
			}
			@Override
			public boolean isChecked(final Object element) {
				return fSelection.contains(element);
			}
		});
		
		fFilterController = new TableFilterController(fViewer);
		
		{	final TextFilterProvider filter = new TextFilterProvider();
			fFilterController.setFilter(0, filter);
			new SearchTextBinding(fFilterText, fFilterController, filter);
		}
		fFilterController.setInput(fInput);
		
		ViewerUtil.installSearchTextNavigation(fViewer, fFilterText, true);
		
		final Button clearAllControl = new Button(composite, SWT.PUSH);
		final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(clearAllControl);
		clearAllControl.setLayoutData(gd);
		clearAllControl.setText(Messages.PackageSelection_ClearAll_label);
		clearAllControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				fSelection.clear();
				fViewer.refresh();
			}
		});
		
		Dialog.applyDialogFont(composite);
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
		setSelectionResult(fSelection.toArray(new IRPackageHelp[fSelection.size()]));
	}
	
	@Override
	public IRPackageHelp[] getResult() {
		return (IRPackageHelp[]) super.getResult();
	}
	
}
