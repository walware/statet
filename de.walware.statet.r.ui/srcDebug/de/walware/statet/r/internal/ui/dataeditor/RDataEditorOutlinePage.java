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
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.SimpleContributionItem;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.workbench.AbstractEditorOutlinePage;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RFactorStore;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.IRDataTableListener;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;
import de.walware.statet.r.ui.dataeditor.RProcessDataTableInput;


public class RDataEditorOutlinePage extends AbstractEditorOutlinePage {
	
	
	private static abstract class ColumnPropertyItem {
		
		
		protected final RDataTableColumn fColumn;
		
		
		public ColumnPropertyItem(final RDataTableColumn column) {
			fColumn = column;
		}
		
		
		public Object getParent() {
			return fColumn;
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
			return getName().hashCode() * fColumn.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ColumnPropertyItem)) {
				return false;
			}
			final ColumnPropertyItem other = (ColumnPropertyItem) obj;
			return (getName() == other.getName()
					&& fColumn.equals(other.fColumn));
		}
		
	}
	
	private static class FactorLevels extends ColumnPropertyItem {
		
		
		public FactorLevels(final RDataTableColumn column) {
			super(column);
		}
		
		
		@Override
		public boolean hasChildren() {
			return true;
		}
		
		@Override
		public Object[] getChildren() {
			final RStore data = fColumn.getDataStore();
			if (data instanceof RFactorStore) {
				return ((RFactorStore) data).getLevels().toArray();
			}
			return super.getChildren();
		}
		
		@Override
		public String getName() {
			return "Factor Levels";
		}
		
		@Override
		public int getCount() {
			final RStore data = fColumn.getDataStore();
			if (data instanceof RFactorStore) {
				return ((RFactorStore) data).getLevelCount();
			}
			return -1;
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
			if (element instanceof ColumnPropertyItem) {
				return ((ColumnPropertyItem) element).getParent();
			}
			return null;
		}
		
		@Override
		public boolean hasChildren(final Object element) {
			if (element == fDescription) {
				return (fDescription.dataColumns.length > 0);
			}
			if (element instanceof RDataTableColumn) {
				final RDataTableColumn column = (RDataTableColumn) element;
				return (column.getColumnType() == RDataTableColumn.FACTOR);
			}
			if (element instanceof ColumnPropertyItem) {
				final ColumnPropertyItem item = (ColumnPropertyItem) element;
				return item.hasChildren();
			}
			return false;
		}
		
		@Override
		public Object[] getChildren(final Object parentElement) {
			if (parentElement == fDescription && fDescription.dataColumns.length <= 2500) {
				return fDescription.dataColumns;
			}
			if (parentElement instanceof RDataTableColumn) {
				final RDataTableColumn column = (RDataTableColumn) parentElement;
				if (column.getColumnType() == RDataTableColumn.FACTOR) {
					return new Object[] { new FactorLevels(column) };
				}
			}
			if (parentElement instanceof ColumnPropertyItem) {
				final ColumnPropertyItem item = (ColumnPropertyItem) parentElement;
				return item.getChildren();
			}
			return new Object[0];
		}
		
		@Override
		public void dispose() {
		}
		
	}
	
	private class RDataLabelProvider extends StyledCellLabelProvider {
		
		
		private final RLabelProvider fRLabelProvider = new RLabelProvider(
				RLabelProvider.NO_STORE_TYPE | RLabelProvider.COUNT | RLabelProvider.NAMESPACE);
		
		
		public RDataLabelProvider() {
		}
		
		
		@Override
		public void update(final ViewerCell cell) {
			Image image;
			final StyledString text = new StyledString();
			final Object element = cell.getElement();
			if (element == fDescription) {
				if (fDescription.struct instanceof ICombinedRElement) {
					fRLabelProvider.update(cell, (IModelElement) fDescription.struct);
					super.update(cell);
					return;
				}
				switch (fDescription.struct.getRObjectType()) {
				case RObject.TYPE_VECTOR:
					image = RUI.getImage(RUI.IMG_OBJ_VECTOR);
					break;
				case RObject.TYPE_ARRAY:
					image = RUI.getImage(RUI.IMG_OBJ_VECTOR);
					break;
				case RObject.TYPE_DATAFRAME:
					image = RUI.getImage(RUI.IMG_OBJ_VECTOR);
					break;
				default:
					image = null;
					break;
				}
				text.append(fRootElementName.toString());
			}
			else if (element instanceof RDataTableColumn) {
				final RDataTableColumn column = (RDataTableColumn) element;
				switch (column.getColumnType()) {
				case RDataTableColumn.LOGI:
					image = RUI.getImage(RUI.IMG_OBJ_COL_LOGI);
					break;
				case RDataTableColumn.INT:
					image = RUI.getImage(RUI.IMG_OBJ_COL_INT);
					break;
				case RDataTableColumn.NUM:
					image = RUI.getImage(RUI.IMG_OBJ_COL_NUM);
					break;
				case RDataTableColumn.CPLX:
					image = RUI.getImage(RUI.IMG_OBJ_COL_CPLX);
					break;
				case RDataTableColumn.CHAR:
					image = RUI.getImage(RUI.IMG_OBJ_COL_CHAR);
					break;
				case RDataTableColumn.RAW:
					image = RUI.getImage(RUI.IMG_OBJ_COL_RAW);
					break;
				case RDataTableColumn.FACTOR:
					image = RUI.getImage(RUI.IMG_OBJ_COL_FACTOR);
					break;
				case RDataTableColumn.DATE:
					image = RUI.getImage(RUI.IMG_OBJ_COL_DATE);
					break;
				case RDataTableColumn.DATETIME:
					image = RUI.getImage(RUI.IMG_OBJ_COL_DATETIME);
					break;
				default:
					image = null;
				}
				text.append(column.getName());
				
				text.append(" : ", StyledString.DECORATIONS_STYLER);
				final List<String> classNames = column.getClassNames();
				text.append(classNames.get(0), StyledString.DECORATIONS_STYLER);
				for (int i = 1; i < classNames.size(); i++) {
					text.append(", ", StyledString.DECORATIONS_STYLER);
					text.append(classNames.get(i), StyledString.DECORATIONS_STYLER);
				}
				if (!classNames.contains(RDataUtil.getStoreClass(column.getDataStore()))) {
					text.append(" (", StyledString.DECORATIONS_STYLER);
					text.append(RDataUtil.getStoreAbbr(column.getDataStore()), StyledString.DECORATIONS_STYLER);
					text.append(")", StyledString.DECORATIONS_STYLER);
				}
			}
			else if (element instanceof ColumnPropertyItem) {
				final ColumnPropertyItem item = (ColumnPropertyItem) element;
				image = null;
				text.append(item.getName());
				final int count = item.getCount();
				if (count >= 0) {
					text.append(" (", StyledString.COUNTER_STYLER);
					text.append(Integer.toString(count), StyledString.COUNTER_STYLER);
					text.append(")", StyledString.COUNTER_STYLER);
				}
			}
			else {
				image = null;
				text.append(element.toString());
			}
			
			cell.setText(text.getString());
			cell.setStyleRanges(text.getStyleRanges());
			cell.setImage(image);
			
			super.update(cell);
		}
		
		@Override
		public void dispose() {
			super.dispose();
			
			fRLabelProvider.dispose();
		}
		
	}
	
	
	private final RDataEditor fEditor;
	
	private RDataTableContentDescription fDescription;
	private RElementName fRootElementName;
	
	
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
								|| fDescription.dataColumns.length != description.dataColumns.length ));
				fDescription = description;
				fRootElementName = ((RProcessDataTableInput) input).getElementName();
				
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
