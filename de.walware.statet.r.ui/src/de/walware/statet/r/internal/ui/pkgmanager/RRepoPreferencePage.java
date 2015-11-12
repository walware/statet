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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.NotEmptyValidator;
import de.walware.ecommons.databinding.URLValidator;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.components.ButtonGroup.IActions;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.util.ComparatorViewerComparator;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;

import de.walware.rj.renv.RPkgType;

import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.RRepo;


public class RRepoPreferencePage extends ConfigurationBlockPreferencePage {
	
	
	public RRepoPreferencePage() {
	}
	
	
	@Override
	protected ConfigurationBlock createConfigurationBlock() throws CoreException {
		return new RRepoConfigurationBlock(createStatusChangedListener());
	}
	
}


class EditRepoDialog extends ExtStatusDialog {
	
	
	private static final String DEFAULT_TYPE = "Default"; //$NON-NLS-1$
	
	
	private final RRepo fRepo;
	private final boolean fIsNew;
	
	private Text fNameControl;
	private Text fURLControl;
	private ComboViewer fTypeControl;
	
	
	public EditRepoDialog(final Shell parent, final RRepo repo, final boolean isNew) {
		super(parent, (isNew) ? WITH_DATABINDING_CONTEXT :
				(WITH_DATABINDING_CONTEXT | SHOW_INITIAL_STATUS));
		fRepo = repo;
		fIsNew = isNew;
		
		setTitle(isNew ? "Add Repository" : "Edit Repository");
	}
	
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		area.setLayout(LayoutUtil.createDialogGrid(2));
		
		{	final Label label = new Label(area, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("Name:");
		}
		{	final Text text = new Text(area, SWT.BORDER);
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fNameControl = text;
		}
		{	final Label label = new Label(area, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("URL:");
		}
		{	final Text text = new Text(area, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(fNameControl, 60);
			text.setLayoutData(gd);
			fURLControl = text;
		}
		{	final Label label = new Label(area, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("Type:");
		}
		{	final ComboViewer viewer = new ComboViewer(area, SWT.READ_ONLY | SWT.BORDER | SWT.DROP_DOWN);
			final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(viewer.getCombo(), 10);
			viewer.getCombo().setLayoutData(gd);
			viewer.setContentProvider(ArrayContentProvider.getInstance());
			viewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(final Object element) {
					if (element instanceof RPkgType) {
						return ((RPkgType) element).getLabel();
					}
					return super.getText(element);
				}
			});
			viewer.setInput(new Object[] {
				DEFAULT_TYPE, RPkgType.SOURCE, RPkgType.BINARY
			});
			viewer.setSelection(new StructuredSelection(DEFAULT_TYPE));
			fTypeControl = viewer;
		}
		
		LayoutUtil.addSmallFiller(area, true);
		
		applyDialogFont(area);
		
		return area;
	}
	
	@Override
	protected void addBindings(final DataBindingSupport databinding) {
		final DataBindingContext dbc = databinding.getContext();
		dbc.bindValue(SWTObservables.observeText(fNameControl, SWT.Modify),
				PojoObservables.observeValue(fRepo, "name"), //$NON-NLS-1$
				null,
				null );
		dbc.bindValue(SWTObservables.observeText(fURLControl, SWT.Modify),
				PojoObservables.observeValue(fRepo, "URL"), //$NON-NLS-1$
				new UpdateValueStrategy().setAfterGetValidator(
						new NotEmptyValidator("URL", new URLValidator("URL")) ),
				null );
		dbc.bindValue(ViewersObservables.observeSingleSelection(fTypeControl),
				PojoObservables.observeValue(fRepo, "pkgType"), //$NON-NLS-1$
				new UpdateValueStrategy().setConverter(new Converter(RPkgType.class, RPkgType.class) {
					@Override
					public Object convert(final Object fromObject) {
						return (fromObject != DEFAULT_TYPE) ? fromObject : null;
					}
				}),
				new UpdateValueStrategy().setConverter(new Converter(RPkgType.class, RPkgType.class) {
					@Override
					public Object convert(final Object fromObject) {
						return (fromObject != null) ? fromObject : DEFAULT_TYPE;
					}
				}) );
	}
	
}

class RRepoConfigurationBlock extends ManagedConfigurationBlock implements IActions<RRepo> {
	
	
	private static final int R_SIZE = 3;
	
	private static final int REPO = 0;
	private static final int CRAN = 1;
	private static final int BIOC = 2;
	
	private static final ImList<Preference<List<RRepo>>> PREFS= ImCollections.newList(
			IRPkgManager.CUSTOM_REPO_PREF,
			IRPkgManager.CUSTOM_CRAN_MIRROR_PREF,
			IRPkgManager.CUSTOM_BIOC_MIRROR_PREF );
	
	private static final Comparator<RRepo> COMPARATOR = new Comparator<RRepo>() {
		
		@Override
		public int compare(final RRepo o1, final RRepo o2) {
			final int diff = o1.getName().compareTo(o2.getName());
			if (diff != 0) {
				return diff;
			}
			return o1.getId().compareTo(o2.getId());
		}
		
	};
	
	
	private final TableComposite[] fTables = new TableComposite[R_SIZE];
	private final ButtonGroup<RRepo>[] fButtonsGroups = new ButtonGroup[R_SIZE];
	
	private final WritableList[] fLists = new WritableList[R_SIZE];
	
	private final Set<String> fIds= new HashSet<>();
	
	
	public RRepoConfigurationBlock(final IStatusChangeListener statusListener) {
		super(null, "R Custom Repositories", statusListener);
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference<?>, String> prefs= new HashMap<>();
		
		prefs.put(IRPkgManager.CUSTOM_REPO_PREF, null);
		prefs.put(IRPkgManager.CUSTOM_CRAN_MIRROR_PREF, null);
		prefs.put(IRPkgManager.CUSTOM_BIOC_MIRROR_PREF, null);
		
		setupPreferenceManager(prefs);
		
		addLinkHeader(pageComposite, "Additional custom repositories for R packages. " +
				"There is no need to add the default R repositories or mirrors.");
		
		final Composite composite = new Composite(pageComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.createCompositeGrid(2));
		
		createTable(composite, REPO, "R&epositories");
		createTable(composite, CRAN, "CR&AN Mirrors");
		createTable(composite, BIOC, "&Bioconductor Mirrors");
		
		initBindings();
		
		updateControls();
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		for (int i = 0; i < R_SIZE; i++) {
			fLists[i] = new WritableList(db.getRealm(), new ArrayList<RRepo>(), RRepo.class);
			fTables[i].viewer.setContentProvider(new ArrayContentProvider());
			fTables[i].viewer.setInput(fLists[i]);
			fButtonsGroups[i].connectTo(fTables[i].viewer, fLists[i], null);
		}
	}
	
	
	private void createTable(final Composite parent, final int r, final String s) {
		{	final Label label = new Label(parent, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			label.setText(s+':');
		}
		{	final TableComposite table = new TableComposite(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
//			table.table.setHeaderVisible(true);
			{	final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
				gd.heightHint = LayoutUtil.hintHeight(table.table, (r == REPO) ? 9 : 3);
				table.setLayoutData(gd);
			}
			{	final TableViewerColumn column = table.addColumn("Repository", SWT.LEFT, new ColumnWeightData(100));
				column.setLabelProvider(new RRepoLabelProvider());
			}
//			{	final TableViewerColumn column = table.addColumn("Name", SWT.LEFT, new ColumnWeightData(40));
//				column.setLabelProvider(new RepoLabelProvider());
//			}
//			{	final TableViewerColumn column = table.addColumn("URL", SWT.LEFT, new ColumnWeightData(60));
//				column.setLabelProvider(new RepoLabelProvider());
//			}
			table.viewer.setComparator(new ComparatorViewerComparator(COMPARATOR));
			fTables[r] = table;
			
			ViewerUtil.scheduleStandardSelection(table.viewer);
		}
		{	final ButtonGroup<RRepo> buttons= new ButtonGroup<>(parent, this, false);
			buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
			
			buttons.addAddButton(null);
			buttons.addEditButton(null);
			buttons.addDeleteButton(null);
			
			fButtonsGroups[r] = buttons;
		}
	}
	
	@Override
	public RRepo edit(final int command, final RRepo item, final Object parent) {
		final RRepo repo = new RRepo(((command & ButtonGroup.ADD_ANY) != 0) ? newId() : item.getId());
		if (item != null) {
			repo.set(item);
		}
		final Dialog dialog = new EditRepoDialog(getShell(), repo, item == null);
		if (dialog.open() == Dialog.OK) {
			if (repo.getName().isEmpty()) {
				repo.setName(RRepo.hintName(repo));
			}
			return repo;
		}
		return null;
	}
	
	private String newId() {
		String id;
		do {
			id = RRepo.CUSTOM_PREFIX + System.currentTimeMillis();
		}
		while (fIds.contains(id));
		return id;
	}
	
	@Override
	public void updateState(final IStructuredSelection selection) {
	}
	
	@Override
	protected void updateControls() {
		super.updateControls();
		for (int i = 0; i < R_SIZE; i++) {
			fLists[i].clear();
			final List<RRepo> repos= getPreferenceValue(PREFS.get(i));
			for (final RRepo repo : repos) {
				fIds.add(repo.getId());
			}
			fLists[i].addAll(repos);
			fButtonsGroups[i].refresh();
		}
	}
	
	@Override
	protected void updatePreferences() {
		for (int i = 0; i < R_SIZE; i++) {
			final RRepo[] array = (RRepo[]) fLists[i].toArray(new RRepo[fLists[i].size()]);
			Arrays.sort(array, COMPARATOR);
			setPrefValue(PREFS.get(i), ImCollections.newList(array));
		}
		super.updatePreferences();
	}
	
}
