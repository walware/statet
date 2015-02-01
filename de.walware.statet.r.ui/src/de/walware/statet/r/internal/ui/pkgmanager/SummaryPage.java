/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.pkgmanager;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;

import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.RPkgAction;
import de.walware.statet.r.core.pkgmanager.RPkgResolver;
import de.walware.statet.r.core.pkgmanager.RRepo;
import de.walware.statet.r.ui.REnvLabelProvider;


abstract class SummaryPage extends WizardPage {
	
	
	private final IRPkgManager.Ext fPkgManager;
	private final RPkgResolver fResolver;
	
	private TableComposite fTable;
	
	
	public SummaryPage(final IRPkgManager.Ext pkgManager, final RPkgResolver resolver,
			final String title) {
		super("InstallPkgsSummaryPage"); //$NON-NLS-1$
		fPkgManager = pkgManager;
		fResolver = resolver;
		
		setTitle(title);
		setDescription("Summary packages to install/update.");
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.createContentGrid(1));
		
		{	final Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label.setText("Packages to install:");
		}
		
		fTable = new TableComposite(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION
				| SWT.VIRTUAL );
		fTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fTable.viewer.setContentProvider(ArrayContentProvider.getInstance());
		{	final TableViewerColumn column = fTable.addColumn("Name", SWT.LEFT,
					new ColumnWeightData(60, true) );
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final RPkgAction action = (RPkgAction) cell.getElement();
					cell.setText(action.getPkg().getName());
				}
			});
		}
		{	final TableViewerColumn column = fTable.addColumn("", SWT.LEFT,
					new ColumnPixelData(LayoutUtil.hintColWidth(fTable.table, 10), true, true) );
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final RPkgAction action = (RPkgAction) cell.getElement();
					cell.setText(fResolver.getReason(action.getPkg()));
				}
			});
		}
		{	final TableViewerColumn column = fTable.addColumn("Version", SWT.LEFT,
					new ColumnPixelData(LayoutUtil.hintColWidth(fTable.table, 10), true, true) );
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final RPkgAction action = (RPkgAction) cell.getElement();
					cell.setText(action.getPkg().getVersion().toString());
				}
			});
		}
		{	final TableViewerColumn column = fTable.addColumn("From", SWT.LEFT,
					new ColumnWeightData(40, true) );
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final RPkgAction action = (RPkgAction) cell.getElement();
					final RRepo repo = fPkgManager.getRepo(action.getRepoId());
					if (repo.getPkgType() != null) {
						final StringBuilder sb = new StringBuilder(repo.getName());
						sb.append(" (");
						sb.append(repo.getPkgType().getLabel());
						sb.append(")");
						cell.setText(sb.toString());
					}
					else {
						cell.setText(repo.getName());
					}
				}
			});
		}
		{	final TableViewerColumn column = fTable.addColumn("To", SWT.LEFT,
					new ColumnWeightData(40, true) );
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					cell.setText(REnvLabelProvider.getSafeLabel(
							((RPkgAction) cell.getElement()).getLibraryLocation() ));
				}
			});
		}
		fTable.table.setHeaderVisible(true);
		fTable.table.setLinesVisible(true);
		
		Dialog.applyDialogFont(composite);
		setControl(composite);
	}
	
	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		if (visible) {
			updateInput();
		}
	}
	
	public abstract void updateInput();
	
	protected void setActions(final List<? extends RPkgAction> list) {
		fTable.viewer.setInput(list);
	}
	
}