/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.preferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.StringArrayPref;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.components.EditableTextList;
import de.walware.ecommons.ui.util.ComparatorViewerComparator;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.internal.ui.RIdentifierCellValidator;
import de.walware.statet.r.internal.ui.RIdentifierGroups;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.RUIPreferenceConstants;


/**
 * Preference page for 'R Editor Options'
 */
public class RIdentifiersPreferencePage extends ConfigurationBlockPreferencePage<RIdentifiersBlock> {
	
	
	public RIdentifiersPreferencePage() {
	}
	
	@Override
	protected RIdentifiersBlock createConfigurationBlock() {
		return new RIdentifiersBlock(createStatusChangedListener());
	}
	
}


class RIdentifiersBlock extends ManagedConfigurationBlock {
	
	
	private class Category {
		
		String fLabel;
		StringArrayPref fPref;
		IObservableSet fSet = new WritableSet();
		
		public Category(final String label, final String prefKey) {
			fLabel = label;
			fPref = new StringArrayPref(RUI.PLUGIN_ID, prefKey);
		}
		
		void load() {
			fSet.clear();
			final String[] words = getPreferenceValue(fPref);
			if (words != null) {
				fSet.addAll(Arrays.asList(words));
			}
		}
		
		void save() {
			setPrefValue(fPref, (String[]) fSet.toArray(new String[fSet.size()]));
		}
		
		@Override
		public String toString() {
			return fLabel;
		}
		
	}
	
	private class WordEditing extends EditingSupport {
		
		private final TextCellEditor fCellEditor;
		
		private final EditableTextList fList;
		
		private Object fLast;
		
		public WordEditing(final EditableTextList list) {
			super(list.getViewer());
			fList = list;
			fCellEditor = new TextCellEditor(list.getViewer().getTable());
			fCellEditor.addListener(new ICellEditorListener() {
				@Override
				public void editorValueChanged(final boolean oldValidState, final boolean newValidState) {
					if (!newValidState) {
						fStatusListener.statusChanged(new Status(Status.ERROR, RUI.PLUGIN_ID, fCellEditor.getErrorMessage()));
					}
					else {
						fStatusListener.statusChanged(Status.OK_STATUS);
					}
				}
				@Override
				public void applyEditorValue() {
					fLast = null;
					fStatusListener.statusChanged(Status.OK_STATUS);
				}
				@Override
				public void cancelEditor() {
					if (fLast == "") { //$NON-NLS-1$
						fList.applyChange("", null); //$NON-NLS-1$
					}
					fStatusListener.statusChanged(Status.OK_STATUS);
				}
			});
			fCellEditor.setValidator(new RIdentifierCellValidator() {
				@Override
				public String isValid(final Object value) {
					final String valid = super.isValid(value);
					if (valid == null) {
						if (!value.equals(fLast) && fActiveCategory.fSet.contains(value)) {
							return Messages.RIdentifiers_Identifier_error_AlreadyExistingInSameGroup_message;
						}
						for (int i = 0; i < fCategories.length; i++) {
							if (fCategories[i] != fActiveCategory && fCategories[i].fSet.contains(value)) {
								return NLS.bind(Messages.RIdentifiers_Identifier_error_AlreadyExistingInOtherGroup_message, fCategories[i].fLabel);
							}
						}
					}
					return valid;
				}
			});
		}
		
		@Override
		protected boolean canEdit(final Object element) {
			return true;
		}
		
		@Override
		protected CellEditor getCellEditor(final Object element) {
			return fCellEditor;
		}
		
		@Override
		protected Object getValue(final Object element) {
			fLast = element;
			return element;
		}
		
		@Override
		protected void setValue(final Object element, final Object value) {
			if (value != null) {
				fList.applyChange(element, (value != "") ? value : null); //$NON-NLS-1$
			}
		}
		
	}
	
	private TableViewer fCategoryList;
	private EditableTextList fWordList;
	
	private Category[] fCategories;
	private Category fActiveCategory;
	
	private final IStatusChangeListener fStatusListener;
	
	
	public RIdentifiersBlock(final IStatusChangeListener statusListener) {
		super(null, null);
		fStatusListener = statusListener;
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		fCategories = new Category[] {
				new Category(Messages.RSyntaxColoring_Identifier_Assignment_label, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_ASSIGNMENT_ITEMS),
				new Category(Messages.RSyntaxColoring_Identifier_Flowcontrol_label, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_FLOWCONTROL_ITEMS),
				new Category(Messages.RSyntaxColoring_Identifier_Logical_label, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_LOGICAL_ITEMS),
				new Category(Messages.RSyntaxColoring_Identifier_Custom1_label, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM1_ITEMS),
				new Category(Messages.RSyntaxColoring_Identifier_Custom2_label, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM2_ITEMS),
		};
		
		final Map<Preference<?>, String> prefs = new HashMap<Preference<?>, String>();
		
		for (int i = 0; i < fCategories.length; i++) {
			prefs.put(fCategories[i].fPref, RIdentifierGroups.GROUP_ID);
		}
		
		setupPreferenceManager(prefs);
		
		// Controls
		GridData gd;
		Label label;
		TableViewerColumn column;
		
		label = new Label(pageComposite, SWT.NONE);
		label.setText(Messages.RIdentifiers_GroupsList_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final TableComposite categoryComposite = new ViewerUtil.TableComposite(pageComposite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint = LayoutUtil.hintHeight(categoryComposite.table, 4);
		categoryComposite.setLayoutData(gd);
		fCategoryList = categoryComposite.viewer;
		fCategoryList.setContentProvider(new ArrayContentProvider());
		
		column = new TableViewerColumn(fCategoryList, SWT.NONE);
		column.setLabelProvider(new ColumnLabelProvider());
		categoryComposite.layout.setColumnData(column.getColumn(), new ColumnWeightData(100));
		
		label = new Label(pageComposite, SWT.NONE);
		label.setText(Messages.RIdentifiers_IdentifiersList_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fWordList = new EditableTextList();
		{	final Control control = fWordList.create(pageComposite,
					new ComparatorViewerComparator(new RSymbolComparator()) );
			control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		fWordList.getColumn().setEditingSupport(new WordEditing(fWordList));
		
		// Binding
		fCategoryList.setInput(fCategories);
		fCategoryList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final Category cat = (Category) ((IStructuredSelection) event.getSelection()).getFirstElement();
				fWordList.setInput(cat.fSet);
				fActiveCategory = cat;
			}
		});
		
		// Init
		fActiveCategory = fCategories[0];
		updateControls();
		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (UIAccess.isOkToUse(fCategoryList)) {
					fCategoryList.setSelection(new StructuredSelection(fActiveCategory));
				}
			}
		});
	}
	
	
	@Override
	public void updatePreferences() {
		for (int i = 0; i < fCategories.length; i++) {
			fCategories[i].save();
		}
		super.updatePreferences();
	}
	
	@Override
	protected void updateControls() {
		for (int i = 0; i < fCategories.length; i++) {
			fCategories[i].load();
		}
		fWordList.refresh();
	}
	
}
