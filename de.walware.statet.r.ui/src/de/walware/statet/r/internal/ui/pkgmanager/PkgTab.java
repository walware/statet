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

import static de.walware.statet.r.internal.ui.pkgmanager.RPkgManagerDialog.NO_INPUT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.icu.text.DateFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.components.DropDownButton;
import de.walware.ecommons.ui.components.History;
import de.walware.ecommons.ui.components.SearchText;
import de.walware.ecommons.ui.content.IElementFilter;
import de.walware.ecommons.ui.content.ObservableSetBinding;
import de.walware.ecommons.ui.content.SearchTextBinding;
import de.walware.ecommons.ui.content.SetElementFilter;
import de.walware.ecommons.ui.content.TableFilterController;
import de.walware.ecommons.ui.content.TextElementFilter;
import de.walware.ecommons.ui.util.AutoCheckController;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.NestedServices;
import de.walware.ecommons.ui.util.ViewerUtil.CheckboxTableComposite;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;
import de.walware.ecommons.ui.util.ViewerUtil.TreeComposite;

import de.walware.rj.renv.IRPkg;

import de.walware.statet.r.core.pkgmanager.IRLibPaths;
import de.walware.statet.r.core.pkgmanager.IRLibPaths.Entry;
import de.walware.statet.r.core.pkgmanager.IRPkgData;
import de.walware.statet.r.core.pkgmanager.IRPkgInfo;
import de.walware.statet.r.core.pkgmanager.IRPkgInfoAndData;
import de.walware.statet.r.core.pkgmanager.IRPkgList;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.pkgmanager.IRView;
import de.walware.statet.r.core.pkgmanager.RPkgAction;
import de.walware.statet.r.core.pkgmanager.RPkgResolver;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;
import de.walware.statet.r.core.pkgmanager.RRepo;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.ui.REnvLabelProvider;
import de.walware.statet.r.ui.RUI;


public class PkgTab extends Composite {
	
	
	private static final int VERSION_CHARS = 8;
	
	private static final int AVAIL = 0;
	private static final int INST = 1;
	
	
	private class InstalledFilter implements IElementFilter, SelectionListener {
		
		private static final int INSTALLED = 0x1;
		private static final int NOT_INSTALLED = 0x2;
		
		private class Final implements IFinalFilter {
			
			private final int fState;
			
			public Final(final int state) {
				fState = state;
			}
			
			
			@Override
			public boolean select(final Object element) {
				final boolean installed = fPkgSet.getInstalled().containsByName((String) element);
				if ((fState & INSTALLED) != 0) {
					return installed;
				}
				else {
					return !installed;
				}
			}
			
			@Override
			public boolean isSubOf(final IFinalFilter other) {
				return (other == this|| other == null || ((other instanceof Final)
						&& fState == ((Final) other).fState ));
			}
			
			@Override
			public boolean isEqualTo(final IFinalFilter other) {
				return (other == this || ((other instanceof Final)
						&& fState == ((Final) other).fState ));
			}
			
		}
		
		
		private volatile int fState;
		
		private Final fFilter;
		
		
		public InstalledFilter() {
			fFilterInstButton.addSelectionListener(this);
			fFilterNotInstButton.addSelectionListener(this);
		}
		
		
		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (e.getSource() == fFilterInstButton) {
				fFilterNotInstButton.setSelection(false);
			}
			else if (e.getSource() == fFilterNotInstButton) {
				fFilterInstButton.setSelection(false);
			}
			int state;
			if (fFilterInstButton.getSelection()) {
				state = INSTALLED;
			}
			else if (fFilterNotInstButton.getSelection()) {
				state = NOT_INSTALLED;
			}
			else {
				state = 0;
			}
			if (fState != state) {
				fState = state;
				fFilterController.refresh(true);
			}
		}
		
		@Override
		public IFinalFilter getFinal(final boolean newData) {
			final int state = fState;
			if (state == 0) {
				fFilter = null;
			}
			else if (fFilter == null || fFilter.fState != state) {
				fFilter = new Final(state);
			}
			return fFilter;
		}
		
		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
		}
		
	}
	
	
	private final RPkgManagerDialog fDialog;
	private final IRPkgManager.Ext fRPkgManager;
	
	private final TabItem fTab;
	
	private IRPkgSet.Ext fPkgSet;
	
	private String fSelectedPkgName;
	private int fSelectedPkgVersionGroup;
	private IRPkgData fSelectedPkgVersion;
	private final Map<String, String> fSelectedPkgVersions= new HashMap<>();
	
	private SearchText fFilterText;
	private Button fFilterInstButton;
	private Button fFilterNotInstButton;
	private CheckboxTableViewer fFilterPriorityTable;
	private WritableSet fFilterPrioritySet;
	private StackLayout fFilterViewsStack;
	private Link fFilterViewsMessage;
	private CheckboxTableViewer fFilterRViewsTable;
	private WritableSet fFilterRViewsSet;
	private TableFilterController fFilterController;
	
	private TableComposite fPkgTable;
	
	private NestedServices fServiceLocator;
	
	private IHandler2 fRefreshHandler;
	private final History<String> fPkgHistory = new de.walware.ecommons.ui.components.History<String>() {
		@Override
		protected void select(final String entry) {
			showPkg(entry);
		};
	};
	private ToolBarManager fToolBar;
	
	private TreeComposite fDetailTable;
	private Label fDetailLicense;
	private TreeViewer fDetailDepTable;
	private TreeViewer fDetailRevTable;
	
	private Button fInstallButton;
	private DropDownButton fUpdateButton;
	private Button fUninstallButton;
	private Button fLoadButton;
	
	
	PkgTab(final RPkgManagerDialog dialog, final TabItem tab, final Composite parent,
			final IRPkgManager.Ext rPkgManager) {
		super(parent, SWT.NONE);
		
		fDialog = dialog;
		fTab = tab;
		fRPkgManager = rPkgManager;
		
		setLayout(LayoutUtil.createTabGrid(3));
		createContent(this);
		updateButtons();
	}
	
	
	private void createContent(final Composite parent) {
		final Composite filterCol = createFilter(parent);
		filterCol.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		
		final Composite tableCol = createTable(parent);
		tableCol.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Composite detailCol = createDetail(parent);
		detailCol.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
	
	private Composite createFilter(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createGroupGrid(1));
		composite.setText("Filter");
		
		{	final SearchText text = new SearchText(composite);
			fFilterText = text;
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(text.getTextControl(), 20);
			text.setToolTipText("Name");
			text.setLayoutData(gd);
		}
		
		{	
//			Label label = new Label(composite, SWT.NONE);
//			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//			label.setText("State:");
			LayoutUtil.addSmallFiller(composite, false);
			
			{	final Button button = new Button(composite, SWT.CHECK);
				fFilterInstButton = button;
				button.setText("Installed");
				button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			}
	//		{	Button button = new Button(composite, SWT.CHECK);
	//			fPkgFilterInstUptButton = button;
	//			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	//			button.setText("With Update");
	//		}
			{	final Button button = new Button(composite, SWT.CHECK);
				fFilterNotInstButton = button;
				button.setText("Not Installed");
				button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			}
		}
		{	final Label label = new Label(composite, SWT.NONE);
			label.setText("Priorities:");
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			final CheckboxTableViewer viewer = new CheckboxTableViewer(new Table(composite,
					SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.NO_SCROLL ));
			fFilterPriorityTable = viewer;
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setLabelProvider(new LabelProvider());
			
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.heightHint = LayoutUtil.hintHeight(viewer.getTable(),
					IRPkgSet.Ext.DEFAULT_PRIORITIES.size(), false );
			viewer.getControl().setLayoutData(gd);
		}
		
		{	final Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			label.setText("Task Views:");
			
			final Composite views = new Composite(composite, SWT.NONE);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			views.setLayoutData(gd);
			fFilterViewsStack = new StackLayout();
			views.setLayout(fFilterViewsStack);
			
			final Link link = new Link(views, SWT.MULTI);
			fFilterViewsMessage = link;
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					showPkg(e.text);
				}
			});
			link.setText("");
			
			final CheckboxTableComposite table = new CheckboxTableComposite(views,
					SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION );
			fFilterRViewsTable = table.viewer;
			table.viewer.setContentProvider(new ArrayContentProvider());
			ColumnViewerToolTipSupport.enableFor(table.viewer, ToolTip.NO_RECREATE);
			
			final TableViewerColumn column = table.addColumn("", SWT.LEFT, new ColumnWeightData(100, false));
			column.setLabelProvider(new RViewLabelProvider());
			
			gd.widthHint = LayoutUtil.hintWidth(table.table, 20);
			fFilterViewsStack.topControl = table;
		}
		
		return composite;
	}
	
	private Composite createTable(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createCompositeGrid(2));
		
		{	final Label label = new Label(composite, SWT.NONE);
			label.setText("Packages:");
			label.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		}
		{	fToolBar = new ToolBarManager(SWT.HORIZONTAL | SWT.FLAT);
			final ToolBar toolBar = fToolBar.createControl(composite);
			toolBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
		}
		
		final TableComposite viewer = new TableComposite(composite,
				SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL);
		fPkgTable = viewer;
		viewer.viewer.setUseHashlookup(true);
		{	final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			gd.heightHint = LayoutUtil.hintHeight(viewer.table, 15);
			gd.widthHint = fDialog.hintWidthInChars(40);
			viewer.setLayoutData(gd);
		}
		ColumnViewerToolTipSupport.enableFor(viewer.viewer, ToolTip.NO_RECREATE);
		{
			final TableViewerColumn column = viewer.addColumn("Name", SWT.LEFT,
					new ColumnWeightData(50));
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final String name = (String) cell.getElement();
					cell.setImage(RUI.getImage((fPkgSet.getInstalled().containsByName(name)) ?
							RUI.IMG_OBJ_R_PACKAGE : RUI.IMG_OBJ_R_PACKAGE_NOTA));
					cell.setText(name);
				}
				@Override
				public String getToolTipText(final Object element) {
					final String name = (String) element;
					final IRPkgInfoAndData v = fPkgSet.getInstalled().getFirstByName(name);
					if (v != null) {
						return v.getTitle();
					}
					return null;
				}
			});
		}
		
		return composite;
	}
	
	private Composite createDetail(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createCompositeGrid(2));
		
		createDetailTable(composite);
		createDetailButtons(composite);
		createDetailInfo(composite);
		
		return composite;
	}
	
	private void createDetailTable(final Composite parent) {
		{	final Label label = new Label(parent, SWT.NONE);
			label.setText("&Versions:");
			label.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		}
		{	// for layout
			final Label label = new Label(parent, SWT.NONE);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
			gd.heightHint = fToolBar.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			label.setLayoutData(gd);
		}
		
		final TreeComposite composite = new TreeComposite(parent,
				SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		fDetailTable = composite;
		{	final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			gd.heightHint = LayoutUtil.hintHeight(composite.tree, 6);
			gd.widthHint = fDialog.hintWidthInChars(40);
			composite.setLayoutData(gd);
		}
		
		composite.viewer.setContentProvider(new DetailGroup.ContentProvider(2) {
			{	fGroups[AVAIL] = new DetailGroup(AVAIL, "Available");
				fGroups[INST] = new DetailGroup(INST, "Installed");
			}
			
			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
				if (newInput instanceof String) {
					final String name = (String) newInput;
					fGroups[AVAIL].setList(fPkgSet.getAvailable().getByName(name));
					fGroups[INST].setList(fPkgSet.getInstalled().getByName(name));
				}
				else {
					fGroups[AVAIL].clearList();
					fGroups[INST].clearList();
				}
			}
		});
		composite.viewer.setComparer(new IElementComparer() {
			@Override
			public int hashCode(final Object element) {
				if (element instanceof IRPkgData) {
					return element.hashCode() + ((IRPkgData) element).getRepoId().hashCode();
				}
				return element.hashCode();
			}
			@Override
			public boolean equals(final Object a, final Object b) {
				if (a == b) {
					return true;
				}
				if (!a.equals(b)) {
					return false;
				}
				if (a instanceof IRPkgData) {
					return ((IRPkgData) a).getRepoId().equals(((IRPkgData) b).getRepoId());
				}
				return false;
			}
		});
		composite.viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		
		{	final TreeViewerColumn column = composite.addColumn("Repository/Library", SWT.LEFT,
					new ColumnWeightData(50, fDialog.hintWidthInChars(20), true));
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final Object element = cell.getElement();
					if (element instanceof DetailGroup) {
						cell.setText(((DetailGroup) element).getLabel());
						return;
					}
					else if (element instanceof IRPkgInfo) {
						final IRLibraryLocation location = ((IRPkgInfo) element).getLibraryLocation();
						cell.setText(REnvLabelProvider.getSafeLabel(location));
						return;
					}
					else if (element instanceof IRPkgData) {
						final IRPkgData pkg = (IRPkgData) element;
						if (pkg.getRepoId() != null) {
							final RRepo repo = RPkgUtil.getRepoById(
									fDialog.fRepoTab.getAvailableRepos(), pkg.getRepoId());
							if (repo != null) {
								cell.setText(repo.getName());
								return;
							}
						}
						cell.setText("-");
						return;
					}
					throw new IllegalStateException();
				}
			});
		}
		{	final TreeViewerColumn column = composite.addColumn("Version", SWT.LEFT,
					new ColumnPixelData(fDialog.hintWidthInChars(VERSION_CHARS), true, true) );
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final Object element = cell.getElement();
					if (element instanceof IRPkgData) {
						cell.setText(((IRPkgData) element).getVersion().toString());
						return;
					}
					cell.setText(""); //$NON-NLS-1$
				}
				@Override
				public String getToolTipText(final Object element) {
					return (element instanceof IRPkgData) ? getDetailToolTipText((IRPkgData) element) : null;
				}
			});
		}
		
		ColumnViewerToolTipSupport.enableFor(composite.viewer);
		
		composite.viewer.setInput(RPkgManagerDialog.NO_INPUT);
	}
	
	private String getDetailToolTipText(final IRPkgData pkgData) {
		final StringBuilder sb = new StringBuilder(pkgData.getName());
		sb.append("\nVersion: ").append(pkgData.getVersion());
		if (pkgData instanceof IRPkgInfoAndData) {
			final IRPkgInfoAndData pkgDescr = (IRPkgInfoAndData) pkgData;
			sb.append("\nBuilt: ").append(((IRPkgInfoAndData) pkgData).getBuilt());
			sb.append("\nInstalled: ").append((pkgDescr.getInstallStamp() != 0) ?
					DateFormat.getDateTimeInstance().format(pkgDescr.getInstallStamp()) : "-" );
			final RRepo repo = fRPkgManager.getRepo(pkgDescr.getRepoId());
			if (repo != null) {
				sb.append("\nFrom: ").append(repo.getName());
			}
		}
		return sb.toString();
	}
	
	private void createDetailButtons(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		composite.setLayout(LayoutUtil.createCompositeGrid(1));
		
		{	final Button button = new Button(composite, SWT.PUSH);
			fInstallButton = button;
			button.setText("Install...");
			button.setLayoutData(LayoutUtil.createGD(button));
		}
		{	final SelectionListener defaultUpdate = new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					doUpdateLatest();
				}
			};
			final DropDownButton button = new DropDownButton(composite);
			final Menu menu = button.getDropDownMenu();
			{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText("&Update (default)...");
				item.addSelectionListener(defaultUpdate);
			}
			{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText("&Reinstall...");
				item.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						doReinstall(null);
					}
				});
			}
			fUpdateButton = button;
			button.setText("Update...");
			button.addSelectionListener(defaultUpdate);
			button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		{	final Button button = new Button(composite, SWT.PUSH);
			fUninstallButton = button;
			button.setText("Uninstall");
			button.setLayoutData(LayoutUtil.createGD(button));
		}
		LayoutUtil.addSmallFiller(composite, false);
		{	final Button button = new Button(composite, SWT.PUSH);
			fLoadButton = button;
			button.setText("Load");
			button.setLayoutData(LayoutUtil.createGD(button));
		}
		
		final SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (e.getSource() == fInstallButton) {
					doInstall();
				}
				else if (e.getSource() == fUpdateButton) {
					doUpdateLatest();
				}
				else if (e.getSource() == fUninstallButton) {
					doUninstall();
				}
				else if (e.getSource() == fLoadButton) {
					doLoad();
				}
			}
		};
		fInstallButton.addSelectionListener(listener);
		fUninstallButton.addSelectionListener(listener);
		fUpdateButton.addSelectionListener(listener);
		fLoadButton.addSelectionListener(listener);
	}
	
	private void createDetailInfo(final Composite parent) {
		final Group info = new Group(parent, SWT.NONE);
		info.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		info.setLayout(LayoutUtil.createGroupGrid(2, true));
		info.setText("Info:");
		
		{	final Composite properties = new Composite(info, SWT.NONE);
			properties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			properties.setLayout(LayoutUtil.createCompositeGrid(2));
			
			final Label label = new Label(properties, SWT.NONE);
			label.setText("License:");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			fDetailLicense = new Label(properties, SWT.NONE);
			fDetailLicense.setText("                ");
			fDetailLicense.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		{	final Composite col = new Composite(info, SWT.NONE);
			col.setLayout(LayoutUtil.createCompositeGrid(1));
			createDetailRef(col, 0);
			col.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		{	final Composite col = new Composite(info, SWT.NONE);
			col.setLayout(LayoutUtil.createCompositeGrid(1));
			createDetailRef(col, 1);
			col.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
	}
	
	private void createDetailRef(final Composite parent, final int type) {
		{	final Label label = new Label(parent, SWT.NONE);
			label.setText((type == 0) ? "Dependencies:" : "Reverse:");
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		}
		
		final TreeViewer viewer = new TreeViewer(parent, (SWT.BORDER | SWT.SINGLE) );
		if (type == 0) {
			fDetailDepTable = viewer;
		}
		else {
			fDetailRevTable = viewer;
		}
		{	final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd.heightHint = LayoutUtil.hintHeight(viewer.getTree(), 12);
			gd.widthHint = fDialog.hintWidthInChars(20);
			viewer.getControl().setLayoutData(gd);
		}
		
		viewer.setContentProvider(new DetailGroup.ContentProvider(5) {
			private static final int DEPENDS = 0;
			private static final int IMPORTS = 1;
			private static final int LINKINGTO = 2;
			private static final int SUGGESTS = 3;
			private static final int ENHANCES = 4;
			{	fGroups[DEPENDS] = new DetailGroup(0, "Depends");
				fGroups[IMPORTS] = new DetailGroup(1, "Imports");
				fGroups[LINKINGTO] = new DetailGroup(2, "Linking To");
				fGroups[SUGGESTS] = new DetailGroup(3, "Suggests");
				fGroups[ENHANCES] = new DetailGroup(4, "Enhances");
			}
			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
				if (newInput instanceof IRPkgData) {
					final IRPkgData pkg = (IRPkgData) newInput;
					fGroups[DEPENDS].setList(pkg.getDepends());
					fGroups[IMPORTS].setList(pkg.getImports());
					fGroups[LINKINGTO].setList(pkg.getLinkingTo());
					fGroups[SUGGESTS].setList(pkg.getSuggests());
					fGroups[ENHANCES].setList(pkg.getEnhances());
				}
				else {
					fGroups[DEPENDS].clearList();
					fGroups[IMPORTS].clearList();
					fGroups[LINKINGTO].clearList();
					fGroups[SUGGESTS].clearList();
					fGroups[ENHANCES].clearList();
				}
			}
		});
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		
		viewer.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final Object element = cell.getElement();
				if (element instanceof DetailGroup) {
					cell.setText(((DetailGroup) element).getLabel());
					cell.setStyleRanges(null);
					return;
				}
				else if (element instanceof IRPkg) {
					final IRPkg pkg = (IRPkg) element;
					final StyledString text = new StyledString();
					text.append(pkg.getName());
					final String version = pkg.getVersion().toString();
					if (!version.isEmpty()) {
						text.append(" (", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
						text.append(version, StyledString.QUALIFIER_STYLER);
						text.append(")", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
					}
					cell.setText(text.getString());
					cell.setStyleRanges(text.getStyleRanges());
					return;
				}
				throw new IllegalStateException();
			}
		});
		
		viewer.setInput(NO_INPUT);
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				final Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (element instanceof IRPkg) {
					showPkg(((IRPkg) element).getName());
				}
			}
		});
	}
	
	void createActions() {
		fServiceLocator = new NestedServices(fDialog.fServiceLocator.getLocator(), "Tab");
		fServiceLocator.bindTo(this);
		
		final IHandlerService handlerService = (IHandlerService) fServiceLocator.getLocator()
				.getService(IHandlerService.class);
		
		fRefreshHandler = new AbstractHandler() {
			@Override
			public void setEnabled(final Object evaluationContext) {
				final IStatus status = fDialog.fStatus;
				setBaseEnabled(status != null && status.isOK());
			}
			@Override
			public Object execute(final ExecutionEvent event) throws ExecutionException {
				final IStatus status = fDialog.fStatus;
				if (status != null && status.isOK()) {
					fDialog.doApply(true);
				}
				return null;
			}
		};
		handlerService.activateHandler(IWorkbenchCommandConstants.FILE_REFRESH, fRefreshHandler);
		fToolBar.add(new HandlerContributionItem(new CommandContributionItemParameter(
				fServiceLocator.getLocator(), null, IWorkbenchCommandConstants.FILE_REFRESH,
				HandlerContributionItem.STYLE_PUSH ), fRefreshHandler));
		fToolBar.add(new Separator());
		fPkgHistory.addActions(fToolBar, fServiceLocator.getLocator());
		fToolBar.update(true);
	}
	
	
	TabItem getTab() {
		return fTab;
	}
	
	void addBinding(final DataBindingSupport db) {
		fFilterPrioritySet = new WritableSet(db.getRealm(), Collections.EMPTY_SET, String.class);
		db.getContext().bindSet(
				ViewersObservables.observeCheckedElements(fFilterPriorityTable, String.class),
				fFilterPrioritySet );
		new AutoCheckController(fFilterPriorityTable, fFilterPrioritySet);
		
		fFilterRViewsSet = new WritableSet(db.getRealm(), Collections.EMPTY_SET, IRView.class);
		db.getContext().bindSet(
				ViewersObservables.observeCheckedElements(fFilterRViewsTable, IRView.class),
				fFilterRViewsSet );
		new AutoCheckController(fFilterRViewsTable, fFilterRViewsSet);
		
		fFilterController = new TableFilterController(fPkgTable.viewer);
		
		fFilterController.addFilter(new InstalledFilter());
		{	// Priority
			final SetElementFilter filter = new SetElementFilter() {
				@Override
				protected boolean select(final Collection<?> set, final Object element) {
					final String name = (String) element;
					if (Util.hasPkgPriority(fPkgSet.getAvailable(), name, set)) {
						return true;
					}
					if (Util.hasPkgPriority(fPkgSet.getInstalled(), name, set)) {
						return true;
					}
					return false;
				}
			};
			fFilterController.addFilter(filter);
			new ObservableSetBinding(fFilterController, fFilterPrioritySet, filter) {
				@Override
				protected java.util.Collection<?> getAll() {
					return fPkgSet.getPriorities();
				}
			};
		}
		{	// Task Views
			final SetElementFilter filter = new SetElementFilter();
			fFilterController.addFilter(filter);
			new ObservableSetBinding(fFilterController, fFilterRViewsSet, filter) {
				@Override
				protected Collection<?> createFilterSet(final Collection<?> set) {
					final Set<String> pkgNames= new HashSet<>(set.size() * 50);
					for (final IRView view : (Collection<? extends IRView>) set) {
						pkgNames.addAll(view.getPkgList());
					}
					return pkgNames;
				}
			};
		}
		{	final TextElementFilter filter = new TextElementFilter();
			fFilterController.addFilter(filter);
			new SearchTextBinding(fFilterText, fFilterController, filter);
		}
		
		fFilterController.addListener(new TableFilterController.Listener() {
			@Override
			public void inputUpdated(final boolean newInput) {
				if (newInput) {
					fSelectedPkgVersion = null;
					updateDetail();
				}
			}
		});
		
		fPkgTable.viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final List<?> list = ((IStructuredSelection) event.getSelection()).toList();
				doUpdateDetail(list.toArray(new String[list.size()]));
			}
		});
		
		fDetailTable.viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final ITreeSelection treeSelection = (ITreeSelection) event.getSelection();
				final Object element = treeSelection.getFirstElement();
				updateDetailDetail((element instanceof IRPkgData) ?
						treeSelection.getPaths()[0] : null );
			}
		});
	}
	
	private void clearFilter() {
		fFilterController.startUpdate();
		try {
			fFilterText.clearText();
			fFilterInstButton.setSelection(false);
			fFilterNotInstButton.setSelection(false);
			fFilterPrioritySet.clear();
			fFilterRViewsSet.clear();
		}
		finally {
			fFilterController.endUpdate();
		}
	}
	
	public void updateSettings(final IRPkgManager.Ext pkgManager) {
		fFilterController.startUpdate();
		try {
			fPkgSet = pkgManager.getExtRPkgSet();
			if (fPkgSet == null) {
				fPkgSet = IRPkgSet.DUMMY;
			}
			fSelectedPkgVersion = null;
			
			final List<String> priorities = fPkgSet.getPriorities();
			{	fFilterPriorityTable.setInput(priorities);
				fFilterPrioritySet.retainAll(priorities);
			}
			{	final List<? extends IRView> views = pkgManager.getRViews();
				final Control show;
				if (views != null) {
					fFilterRViewsTable.setInput(views);
					fFilterRViewsSet.retainAll(views);
					show = fFilterRViewsTable.getControl().getParent();
				}
				else {
					fFilterRViewsTable.setInput(Collections.EMPTY_LIST);
					fFilterRViewsSet.clear();
					if (fPkgSet.getAvailable().containsByName("ctv")
							&& !fPkgSet.getInstalled().containsByName("ctv") ) {
						fFilterViewsMessage.setText("Install CRAN Task Views (<a href=\"ctv\">ctv</a>) package to filter the packages by tasks.");
						show = fFilterViewsMessage;
					}
					else {
						show = fFilterRViewsTable.getControl().getParent();
					}
				}
				if (fFilterViewsStack.topControl != show) {
					fFilterViewsStack.topControl = show;
					show.getParent().layout(true);
				}
			}
			fFilterController.setInput(fPkgSet.getNames());
		}
		finally {
			fFilterController.endUpdate();
		}
	}
	
	private void updateDetail() {
		final List<?> list = ((IStructuredSelection) fPkgTable.viewer.getSelection()).toList();
		doUpdateDetail(list.toArray(new String[list.size()]));
	}
	
	private void doUpdateDetail(final String[] selection) {
		if (selection.length == 1) {
			final String name = selection[0];
			fSelectedPkgName = name;
			fDetailTable.viewer.setInput(name);
			ITreeSelection treeSelection = ((ITreeSelection) fDetailTable.viewer.getSelection());
			Object element = treeSelection.getFirstElement();
			if (!(element instanceof IRPkgData)) {
				IRPkgData pkg = null;
				if (pkg == null) {
					final String repoId = fSelectedPkgVersions.get(name);
					if (repoId != null) {
						pkg = Util.getPkgByRepo(fPkgSet.getAvailable(), name, repoId);
					}
				}
				if (pkg == null) {
					pkg = fPkgSet.getAvailable().getFirstByName(name);
				}
				if (pkg == null) {
					pkg = fPkgSet.getInstalled().getFirstByName(name);
				}
				if (pkg != null) {
					fDetailTable.viewer.setSelection(new StructuredSelection(pkg));
				}
			}
			if (fSelectedPkgVersion == null) {
				treeSelection = ((ITreeSelection) fDetailTable.viewer.getSelection());
				element = treeSelection.getFirstElement();
				if (element instanceof IRPkgData) {
					updateDetailDetail(treeSelection.getPaths()[0]);
				}
			}
			fPkgHistory.selected(name);
			fToolBar.update(true);
		}
		else {
			fSelectedPkgName = null;
			fDetailTable.viewer.setInput(NO_INPUT);
			fPkgHistory.selected(null);
		}
	}
	
	private void updateDetailDetail(final TreePath path) {
		if (fSelectedPkgName != null && path != null) {
			final String name = fSelectedPkgName;
			final int id = ((DetailGroup) path.getFirstSegment()).getId();
			final IRPkgData pkg = (IRPkgData) path.getLastSegment();
			fSelectedPkgVersionGroup = id;
			fSelectedPkgVersion = pkg;
			IRPkgData first;
			if (!pkg.getRepoId().isEmpty()
					&& ((first = fPkgSet.getAvailable().getFirstByName(name)) != null)
					&& !pkg.getRepoId().equals(first.getRepoId()) ) {
				fSelectedPkgVersions.put(name, pkg.getRepoId());
			}
			else {
				fSelectedPkgVersions.remove(name);
			}
			
			fDetailLicense.setText(pkg.getLicense());
			fDetailDepTable.setInput(pkg);
			fDetailRevTable.setInput(fPkgSet.getReverse(name));
		}
		else {
			fSelectedPkgVersionGroup = -1;
			fSelectedPkgVersion = null;
			
			fDetailLicense.setText(""); //$NON-NLS-1$
			fDetailDepTable.setInput(NO_INPUT);
			fDetailRevTable.setInput(NO_INPUT);
		}
		
		updateButtons();
	}
	
	private boolean isModifiable(final IRLibraryLocation libLoc) {
		final Entry entry = fRPkgManager.getRLibPaths().getEntryByLocation(libLoc);
		return (entry != null && (entry.getAccess() & IRLibPaths.WRITABLE) != 0);
	}
	
	private void updateButtons() {
		boolean available;
		boolean allInstalled;
		boolean allRemovable;
		if (fSelectedPkgName != null) {
			final String name = fSelectedPkgName;
			available = fPkgSet.getAvailable().containsByName(name);
			final IRPkgInfoAndData pkg = fPkgSet.getInstalled().getFirstByName(name);
			if (pkg != null) {
				allInstalled = true;
				allRemovable = isModifiable(pkg.getLibraryLocation());
			}
			else {
				allInstalled = false;
				allRemovable = false;
			}
		}
		else {
			final IStructuredSelection selection = (IStructuredSelection) fPkgTable.viewer.getSelection();
			if (selection.isEmpty()) {
				available = false;
				allInstalled = false;
				allRemovable = false;
			}
			else {
				available = false;
				allInstalled = true;
				allRemovable = true;
				final List<String> checkedLocations= new ArrayList<>(8);
				for (final Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
					final String name = (String) iterator.next();
					if (!available && fPkgSet.getAvailable().containsByName(name)) {
						available = true;
					}
					if (allInstalled) {
						final IRPkgInfoAndData pkg = fPkgSet.getInstalled().getFirstByName(name);
						if (pkg != null) {
							if (allRemovable) {
								final IRLibraryLocation libLoc = pkg.getLibraryLocation();
								if (!checkedLocations.contains(libLoc.getDirectoryPath())) {
									allRemovable = isModifiable(libLoc);
									checkedLocations.add(libLoc.getDirectoryPath());
								}
							}
						}
						else {
							allInstalled = false;
							allRemovable = false;
						}
					}
				}
			}
		}
		fInstallButton.setEnabled(available);
		fUpdateButton.setEnabled(available);
		fUninstallButton.setEnabled(allRemovable);
		fLoadButton.setEnabled(allInstalled);
	}
	
	void updateStatus(final IStatus status) {
		fRefreshHandler.setEnabled(null);
	}
	
	private void doInstall() {
		final Map<String, List<RPkgAction.Install>> pkgs = getFirstSelectedAsActions(
				false, new IGetPkgFilter[0] );
		if (pkgs == null) {
			return;
		}
		
		final RPkgResolver resolver = new RPkgResolver(fPkgSet, pkgs);
		resolver.setAddSuggested(fDialog.fOptionsTab.installSuggested());
		resolver.run();
		
		final InstallPkgsWizard wizard = new InstallPkgsWizard(fDialog.getTool(), fRPkgManager,
				InstallPkgsWizard.MODE_INSTALL, resolver );
		final WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.setBlockOnOpen(true);
		dialog.open();
		
		fPkgTable.viewer.refresh(true);
		updateButtons();
	}
	
	private void doUpdateLatest() {
		final Map<String, List<RPkgAction.Install>> pkgs = getFirstSelectedAsActions(
				false, new IGetPkgFilter[] {
						new RequireInstFilter(),
						new LibSourceFilter(),
						new LaterVersionFilter(),
		});
		if (pkgs == null) {
			return;
		}
		
		final RPkgResolver resolver = new RPkgResolver(fPkgSet, pkgs);
		resolver.setAddSuggested(false);
		resolver.run();
		
		final InstallPkgsWizard wizard = new InstallPkgsWizard(fDialog.getTool(), fRPkgManager,
				InstallPkgsWizard.MODE_UPDATE, resolver );
		final WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.setBlockOnOpen(true);
		dialog.open();
		
		fPkgTable.viewer.refresh(true);
		updateButtons();
	}
	
	private void doReinstall(final List<String> names) {
		final Map<String, List<RPkgAction.Install>> pkgs = getAllSelectedAsActions(names, true,
				new IGetPkgFilter[] {
						new RequireInstFilter(),
						new LibSourceFilter(),
						new NotOlderVersionFilter(),
						new ReadOnlyFilter(fRPkgManager.getRLibPaths()),
		});
		if (pkgs == null) {
			return;
		}
		
		final RPkgResolver resolver = new RPkgResolver(fPkgSet, pkgs);
		resolver.setAddRequired(false);
		resolver.run();
		
		final InstallPkgsWizard wizard = new InstallPkgsWizard(fDialog.getTool(), fRPkgManager,
				InstallPkgsWizard.MODE_REINSTALL, resolver );
		final WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.setBlockOnOpen(true);
		dialog.open();
		
		fPkgTable.viewer.refresh(true);
		updateButtons();
	}
	
	private void doUninstall() {
		final List<? extends IRPkgInfoAndData> pkgs = getSelectedInstalled(
				new IGetPkgFilter[] {
						new RequireInstFilter(),
						new LibSourceFilter(),
						new ReadOnlyFilter(fRPkgManager.getRLibPaths()),
		});
		if (pkgs == null) {
			return;
		}
		final List<RPkgAction> actions= new ArrayList<>(pkgs.size());
		for (final IRPkgInfoAndData pkg : pkgs) {
			actions.add(new RPkgAction.Uninstall(pkg));
		}
		fRPkgManager.perform(fDialog.getTool(), actions);
		
		fPkgTable.viewer.refresh(true);
		updateButtons();
	}
	
	private void doLoad() {
		final List<? extends IRPkgInfoAndData> pkgs = getSelectedInstalled(
				new IGetPkgFilter[] {
						new RequireInstFilter(),
		});
		if (pkgs == null) {
			return;
		}
		
		if (pkgs.size() == 1 && fSelectedPkgVersion != null) {
			fRPkgManager.loadPkgs(fDialog.getTool(), pkgs, true);
		}
		else {
			fRPkgManager.loadPkgs(fDialog.getTool(), pkgs, false);
		}
	}
	
//	private void revert() {
//		List<String> list;
//		if (fSelectedPkgName != null) {
//			list = Collections.singletonList(fSelectedPkgName);
//		}
//		else {
//			final IStructuredSelection selection = (IStructuredSelection) fPkgTable.viewer.getSelection();
//			list = selection.toList();
//		}
//		fPkgTable.viewer.refresh(true);
//		updateButtons();
//	}
	
	private List<IRPkgInfoAndData> getSelectedInstalled(final IGetPkgFilter[] filters) {
		if (fSelectedPkgName != null) {
			final String name = fSelectedPkgName;
			IRPkgInfoAndData inst;
			if (fSelectedPkgVersionGroup == INST) {
				inst = (IRPkgInfoAndData) fSelectedPkgVersion;
			}
			else {
				inst = fPkgSet.getInstalled().getFirstByName(name);
			}
			for (int j = 0; j < filters.length; j++) {
				if (filters[j].exclude(inst, null)) {
					return null;
				}
			}
			final List<IRPkgInfoAndData> list= new ArrayList<>(1);
			list.add(inst);
			return list;
		}
		else {
			final IStructuredSelection selection = (IStructuredSelection) fPkgTable.viewer.getSelection();
			final List<IRPkgInfoAndData> list= new ArrayList<>(selection.size());
			ITER_SELECTED: for (final Object element : selection.toList()) {
				final String name = (String) element;
				final IRPkgInfoAndData inst = fPkgSet.getInstalled().getFirstByName(name);
				for (int j = 0; j < filters.length; j++) {
					if (filters[j].exclude(inst, null)) {
						continue ITER_SELECTED;
					}
				}
				list.add(inst);
			}
			if (list.isEmpty()) {
				return null;
			}
			return list;
		}
	}
	
	private final Map<String, List<RPkgAction.Install>> getFirstSelectedAsActions(
			final boolean sameRepo, final IGetPkgFilter[] filters) {
		if (fSelectedPkgName != null) {
			final String name = fSelectedPkgName;
			IRPkgData avail;
			if (fSelectedPkgVersionGroup == AVAIL) {
				avail = fSelectedPkgVersion;
			}
			else {
				avail = fPkgSet.getAvailable().getFirstByName(name);
			}
			if (avail == null) {
				return null;
			}
			IRPkgInfoAndData inst;
			if (fSelectedPkgVersionGroup == INST) {
				inst = (IRPkgInfoAndData) fSelectedPkgVersion;
			}
			else {
				inst = fPkgSet.getInstalled().getFirstByName(name);
			}
			if (inst != null && sameRepo) {
				avail = getAvailSameRepo(inst, avail);
			}
			for (int j = 0; j < filters.length; j++) {
				if (filters[j].exclude(inst, avail)) {
					return null;
				}
			}
			final List<RPkgAction.Install> list= new ArrayList<>(1);
			list.add(new RPkgAction.Install(avail, null, inst));
			return Collections.singletonMap(name, list);
		}
		else {
			final IStructuredSelection selection = (IStructuredSelection) fPkgTable.viewer.getSelection();
			final Map<String, List<RPkgAction.Install>> map= new HashMap<>(selection.size());
			ITER_SELECTED: for (final Object element : selection.toList()) {
				final String name = (String) element;
				IRPkgData avail = fPkgSet.getAvailable().getFirstByName(name);
				if (avail == null) {
					continue;
				}
				final IRPkgInfoAndData inst = fPkgSet.getInstalled().getFirstByName(name);
				if (inst != null && sameRepo) {
					avail = getAvailSameRepo(inst, avail);
				}
				for (int j = 0; j < filters.length; j++) {
					if (filters[j].exclude(inst, avail)) {
						continue ITER_SELECTED;
					}
				}
				final List<RPkgAction.Install> list= new ArrayList<>(1);
				list.add(new RPkgAction.Install(avail, null, inst));
				map.put(name, list);
			}
			if (map.isEmpty()) {
				return null;
			}
			return map;
		}
	}
	
	private final Map<String, List<RPkgAction.Install>> getAllSelectedAsActions(
			List<String> names, final boolean sameRepo, final IGetPkgFilter[] filters) {
		if (names == null && fSelectedPkgName != null) {
			final String name = fSelectedPkgName;
			final IRPkgData avail;
			if (fSelectedPkgVersionGroup == AVAIL) {
				avail = fSelectedPkgVersion;
			}
			else {
				avail = fPkgSet.getAvailable().getFirstByName(name);
			}
			if (avail == null) {
				return null;
			}
			final List<? extends IRPkgInfoAndData> instList = fPkgSet.getInstalled().getByName(name);
			if (instList.isEmpty()) {
				return null;
			}
			final List<RPkgAction.Install> list= new ArrayList<>(instList.size());
			ITER_INST: for (final IRPkgInfoAndData inst : instList) {
				final IRPkgData instAvail = (sameRepo) ? getAvailSameRepo(inst, avail) : avail;
				for (int j = 0; j < filters.length; j++) {
					if (filters[j].exclude(inst, instAvail)) {
						continue ITER_INST;
					}
				}
				list.add(new RPkgAction.Install(instAvail, null, inst));
			}
			
			if (list.isEmpty()) {
				return null;
			}
			return Collections.singletonMap(name, list);
		}
		else {
			if (names == null) {
				names = ((IStructuredSelection) fPkgTable.viewer.getSelection()).toList();
			}
			final Map<String, List<RPkgAction.Install>> map= new HashMap<>(names.size());
			for (final String name : names) {
				final IRPkgData avail = fPkgSet.getAvailable().getFirstByName(name);
				if (avail == null) {
					continue;
				}
				final List<? extends IRPkgInfoAndData> instList = fPkgSet.getInstalled().getByName(name);
				if (instList.isEmpty()) {
					continue;
				}
				final List<RPkgAction.Install> list = new ArrayList<>(instList.size());
				ITER_INST: for (final IRPkgInfoAndData inst : instList) {
					final IRPkgData instAvail = (sameRepo) ? getAvailSameRepo(inst, avail) : avail;
					for (int j = 0; j < filters.length; j++) {
						if (filters[j].exclude(inst, instAvail)) {
							continue ITER_INST;
						}
					}
					list.add(new RPkgAction.Install(instAvail, null, inst));
				}
				
				if (list.isEmpty()) {
					continue;
				}
				map.put(name, list);
			}
			if (map.isEmpty()) {
				return null;
			}
			return map;
		}
	}
	
	private IRPkgData getAvailSameRepo(final IRPkgInfoAndData inst, final IRPkgData fallback) {
		IRPkgData pkg = null;
		if (!inst.getRepoId().isEmpty()) {
			final IRPkgList<? extends IRPkgData> repoList = fPkgSet.getAvailable().getBySource(inst.getRepoId());
			if (repoList != null) {
				pkg = repoList.get(inst.getName());
				if (pkg != null) {
					return pkg;
				}
			}
		}
		return fallback;
	}
	
	void showPkg(final String name) {
		if (name.equals("R")) {
			return;
		}
		fFilterController.setSelection(name);
	}
	
	IRPkgSet.Ext getPkgSet() {
		return fPkgSet;
	}
	
	void install(final List<String> pkgNames) {
		fPkgTable.viewer.setSelection(new StructuredSelection());
		clearFilter();
		fFilterController.schedule(new Runnable() {
			@Override
			public void run() {
				fFilterController.setSelection(pkgNames);
//				updateDetail();
				doInstall();
			}
		});
	}
	
	void reinstallAll() {
		doReinstall(fPkgSet.getNames());
	}
	
}
