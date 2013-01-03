/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.SimpleContributionItem;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.workbench.AbstractEditorOutlinePage;

import de.walware.rj.data.RFactorStore;
import de.walware.rj.data.RStore;

import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.IRDataTableListener;
import de.walware.statet.r.ui.dataeditor.IRDataTableVariable;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RDataEditorOutlinePage extends AbstractEditorOutlinePage {
	
	
	static abstract class VariablePropertyItem {
		
		
		protected final IRDataTableVariable fVariable;
		
		
		public VariablePropertyItem(final IRDataTableVariable column) {
			fVariable = column;
		}
		
		
		public Object getParent() {
			return fVariable;
		}
		
		public boolean hasChildren() {
			return false;
		}
		
		public Object[] getChildren() {
			return new Object[0];
		}
		
		public abstract String getName();
		
		public int getCount() {
			return -1;
		}
		
		
		@Override
		public int hashCode() {
			return getName().hashCode() * fVariable.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof VariablePropertyItem)) {
				return false;
			}
			final VariablePropertyItem other = (VariablePropertyItem) obj;
			return (getName() == other.getName()
					&& fVariable.equals(other.fVariable));
		}
		
	}
	
	private static class FactorLevels extends VariablePropertyItem {
		
		
		public FactorLevels(final RDataTableColumn column) {
			super(column);
		}
		
		
		@Override
		public boolean hasChildren() {
			return true;
		}
		
		@Override
		public Object[] getChildren() {
			final RFactorStore data = (RFactorStore) ((RDataTableColumn) fVariable).getDataStore();
			return data.getLevels().toArray();
		}
		
		@Override
		public String getName() {
			return "Factor Levels";
		}
		
		@Override
		public int getCount() {
			final RFactorStore data = (RFactorStore) ((RDataTableColumn) fVariable).getDataStore();
			return data.getLevelCount();
		}
		
	}
	
	private static class FTableFactorLevels extends VariablePropertyItem {
		
		
		public FTableFactorLevels(final FTableVariable variable) {
			super(variable);
		}
		
		
		@Override
		public boolean hasChildren() {
			return true;
		}
		
		@Override
		public Object[] getChildren() {
			final RStore data = ((FTableVariable) fVariable).getLevelStore();
			return data.toArray();
		}
		
		@Override
		public String getName() {
			return "Levels";
		}
		
		@Override
		public int getCount() {
			final RStore data = ((FTableVariable) fVariable).getLevelStore();
			return data.getLength();
		}
		
	}
	
	
	private class RDataContentProvider implements ITreeContentProvider {
		
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		}
		
		@Override
		public Object[] getElements(final Object inputElement) {
			if (fDescription != null) {
				return new Object[] { fDescription };
			}
			return new Object[0];
		}
		
		@Override
		public Object getParent(final Object element) {
			if (element instanceof RDataTableColumn) {
				return fDescription;
			}
			if (element instanceof VariablePropertyItem) {
				return ((VariablePropertyItem) element).getParent();
			}
			return null;
		}
		
		@Override
		public boolean hasChildren(final Object element) {
			if (element == fDescription) {
				return (fDescription.getVariables().length > 0);
			}
			if (element instanceof RDataTableColumn) {
				final RDataTableColumn column = (RDataTableColumn) element;
				return (column.getVarType() == IRDataTableVariable.FACTOR);
			}
			else if (element instanceof FTableVariable) {
				return true;
			}
			else if (element instanceof VariablePropertyItem) {
				final VariablePropertyItem item = (VariablePropertyItem) element;
				return item.hasChildren();
			}
			return false;
		}
		
		@Override
		public Object[] getChildren(final Object parentElement) {
			{	Object[] columns;
				if (parentElement == fDescription && (columns = fDescription.getVariables()).length <= 2500) {
					return columns;
				}
			}
			if (parentElement instanceof RDataTableColumn) {
				final RDataTableColumn column = (RDataTableColumn) parentElement;
				if (column.getVarType() == IRDataTableVariable.FACTOR) {
					return new Object[] { new FactorLevels(column) };
				}
			}
			else if (parentElement instanceof FTableVariable) {
				return new Object[] { new FTableFactorLevels((FTableVariable) parentElement) };
			}
			else if (parentElement instanceof VariablePropertyItem) {
				final VariablePropertyItem item = (VariablePropertyItem) parentElement;
				return item.getChildren();
			}
			return new Object[0];
		}
		
		@Override
		public void dispose() {
		}
		
	}
	
	private final RDataEditor fEditor;
	
	private RDataTableContentDescription fDescription;
	
	
	public RDataEditorOutlinePage(final RDataEditor editor) {
		super("de.walware.r.menu.RDataOutlineViewContextMenu"); //$NON-NLS-1$
		fEditor = editor;
	}
	
	
	public RDataEditor getDataEditor() {
		return fEditor;
	}
	
	@Override
	protected IDialogSettings getDialogSettings() {
		return DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "RDataOutlineView");
	}
	
	@Override
	protected void configureViewer(final TreeViewer viewer) {
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new RDataContentProvider());
		viewer.setLabelProvider(new RDataLabelProvider());
		viewer.setInput(this);
	}
	
	@Override
	protected void init() {
		super.init();
		fEditor.getRDataTable().addTableListener(new IRDataTableListener() {
			@Override
			public void inputChanged(final IRDataTableInput input, final RDataTableContentDescription description) {
				final boolean isNew = (description != null
						&& (fDescription == null
								|| fDescription.getVariables().length != description.getVariables().length ));
				fDescription = description;
				
				final TreeViewer viewer = getViewer();
				if (UIAccess.isOkToUse(viewer)) {
					viewer.refresh();
					if (isNew && fDescription != null) {
//						viewer.setExpandedTreePaths(new TreePath[] { new TreePath(new Object[] { fDescription }) });
						viewer.expandToLevel(3);
					}
				}
			}
		});
	}
	
	@Override
	protected void selectInEditor(final ISelection selection) {
		if (selection.isEmpty()) {
			return;
		}
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.size() == 1) {
				final Object element = structuredSelection.getFirstElement();
				if (element instanceof RDataTableColumn) {
					fEditor.getRDataTable().revealColumn(((RDataTableColumn) element).getIndex());
				}
			}
		}
	}
	
	@Override
	protected void contextMenuAboutToShow(final IMenuManager m) {
		final IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		for (final Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			final Object element = iterator.next();
			if (!(element instanceof RDataTableColumn)) {
				return;
			}
		}
		
		m.add(new SimpleContributionItem("Select Column", "S") {
			@Override
			protected void execute() throws ExecutionException {
				final IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
				final List<Integer> columnIndexes = new ArrayList<Integer>();
				for (final Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
					final Object element = iterator.next();
					if (element instanceof RDataTableColumn) {
						columnIndexes.add(((RDataTableColumn) element).getIndex());
					}
					else {
						return;
					}
				}
				fEditor.getRDataTable().selectColumns(columnIndexes);
			}
		});
		
		m.add(new Separator());
		if (selection.size() == 1) {
			m.add(new SimpleContributionItem(
					SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_SORT_ALPHA_IMAGE_ID), null,
					"Sort Increasing by Column", "I") {
				@Override
				protected void execute() throws ExecutionException {
					final IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
					final Object element = selection.getFirstElement();
					if (selection.size() == 1 && element instanceof RDataTableColumn) {
						final RDataTableColumn column = (RDataTableColumn) element;
						fEditor.getRDataTable().sortByColumn(column.getIndex(), true);
					}
				}
			});
			m.add(new SimpleContributionItem(
					"Sort Decreasing by Column", "I") {
				@Override
				protected void execute() throws ExecutionException {
					final IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
					final Object element = selection.getFirstElement();
					if (selection.size() == 1 && element instanceof RDataTableColumn) {
						final RDataTableColumn column = (RDataTableColumn) element;
						fEditor.getRDataTable().sortByColumn(column.getIndex(), false);
					}
				}
			});
		}
		m.add(new SimpleContributionItem("Clear All Sorting", "O") {
			@Override
			protected void execute() throws ExecutionException {
				fEditor.getRDataTable().clearSorting();
			}
		});
	}
	
}
