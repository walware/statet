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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabItem;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil.CheckboxTableComposite;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;

import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.ISelectedRepos;
import de.walware.statet.r.core.pkgmanager.RRepo;
import de.walware.statet.r.core.pkgmanager.SelectedRepos;


public class RepoTab extends Composite {
	
	
	private final RPkgManagerDialog fDialog;
	
	private final TabItem fTab;
	
	private List<RRepo> fAvailableRepos;
	
	private WritableSet fSelectedRepos;
	private WritableValue fSelectedCRAN;
	private String fBioCVersion;
	private WritableValue fSelectedBioC;
	
	private CheckboxTableViewer fRepoTable;
	private TableViewer fCRANTable;
	private Label fBioCLabel;
	private TableViewer fBioCTable;
	
	
	RepoTab(final RPkgManagerDialog dialog, final TabItem tab, final Composite parent) {
		super(parent, SWT.NONE);
		
		fDialog = dialog;
		fTab = tab;
		
		setLayout(LayoutUtil.createTabGrid(2, true));
		createContent(this);
	}
	
	
	private void createContent(final Composite parent) {
		// Column 1
		{
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.createCompositeGrid(1));
			
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				label.setText("R&epositories:");
			}
			final CheckboxTableComposite table = new CheckboxTableComposite(composite,
					SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION );
			fRepoTable = table.viewer;
			{	final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
				gd.heightHint = LayoutUtil.hintHeight(table.table, 15);
				gd.widthHint = LayoutUtil.hintWidth(table.table, 40);
				table.setLayoutData(gd);
			}
			table.addColumn("Repository", SWT.LEFT, new ColumnWeightData(100, false));
			
			table.viewer.setLabelProvider(new RRepoLabelProvider());
			table.viewer.setContentProvider(new ArrayContentProvider());
		}
		// Column 2
		{
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.createCompositeGrid(1));
			
			{
				final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				label.setText("CR&AN Mirror:");
			}
			fCRANTable = createMirrorTable(composite);
			{
				final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				label.setText("&Bioconductor Mirror:");
				fBioCLabel = label;
			}
			fBioCTable = createMirrorTable(composite);
		}
		
		{	final Link link = new Link(parent, SWT.NONE);
			link.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			link.setText("Custom repositories and mirrors can be configured in the "
					+ "<a href=\"de.walware.statet.r.preferencePages.RRepositories\">preferences</a>.");
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					fDialog.openPrefPage(e.text);
				}
			});
		}
	}
	
	private TableViewer createMirrorTable(final Composite parent) {
		final TableComposite table = new TableComposite(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		{
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = LayoutUtil.hintHeight(table.table, 5);
			gd.widthHint = LayoutUtil.hintWidth(table.table, 40);
			table.setLayoutData(gd);
		}
		table.addColumn("Mirror", SWT.LEFT, new ColumnWeightData(100, false));
		
		table.viewer.setLabelProvider(new RRepoLabelProvider());
		table.viewer.setContentProvider(new ArrayContentProvider());
		
		return table.viewer;
	}
	
	
	ISelectedRepos createRepoSettings() {
		final List<RRepo> list = new ArrayList<RRepo>(fSelectedRepos.size());
		for (final RRepo repo : fAvailableRepos) {
			if (fSelectedRepos.contains(repo)) {
				list.add(repo);
			}
		}
		return new SelectedRepos(list, (RRepo) fSelectedCRAN.getValue(), fBioCVersion,
				(RRepo) fSelectedBioC.getValue());
	}
	
	
	TabItem getTab() {
		return fTab;
	}
	
	List<RRepo> getAvailableRepos() {
		return fAvailableRepos;
	}
	
	void addBindings(final DataBindingSupport db) {
		fSelectedRepos = new WritableSet(db.getRealm(), Collections.EMPTY_SET, RRepo.class);
		fSelectedCRAN = new WritableValue(db.getRealm(), null, RRepo.class);
		fSelectedBioC = new WritableValue(db.getRealm(), null, RRepo.class);
		
		db.getContext().bindSet(
				ViewersObservables.observeCheckedElements(fRepoTable, RRepo.class),
				fSelectedRepos );
		db.getContext().bindValue(ViewersObservables.observeSingleSelection(fCRANTable),
				fSelectedCRAN );
		db.getContext().bindValue(ViewersObservables.observeSingleSelection(fBioCTable),
				fSelectedBioC );
		
		fSelectedRepos.addChangeListener(fDialog);
		fSelectedCRAN.addChangeListener(fDialog);
		fSelectedBioC.addChangeListener(fDialog);
	}
	
	void init() {
		fRepoTable.getTable().setSelection(0);
	}
	
	void updateSettings(final IRPkgManager.Ext pkgManager) {
		fAvailableRepos = pkgManager.getAvailableRepos();
		final ISelectedRepos repoSettings = pkgManager.getSelectedRepos();
		fRepoTable.setInput(fAvailableRepos);
		if (fSelectedRepos.isEmpty()) {
			fSelectedRepos.addAll(repoSettings.getRepos());
		}
		else {
			fSelectedRepos.retainAll(fAvailableRepos);
		}
		
		fCRANTable.setInput(pkgManager.getAvailableCRANMirrors());
		fSelectedCRAN.setValue(repoSettings.getCRANMirror());
		
		fBioCVersion = repoSettings.getBioCVersion();
		fBioCLabel.setText("&Bioconductor " + " (" + fBioCVersion + ") Mirror:");
		fBioCTable.setInput(pkgManager.getAvailableBioCMirrors());
		fSelectedBioC.setValue(repoSettings.getBioCMirror());
	}
	
}
