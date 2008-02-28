/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.ltk.text.StringParseInput;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.Preference.StringArrayPref;
import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.util.LayoutUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;
import de.walware.eclipsecommons.ui.util.ViewerUtil;
import de.walware.eclipsecommons.ui.util.ViewerUtil.TableComposite;

import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;
import de.walware.statet.r.core.RNamesComparator;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.RSourceTokenLexer;
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
		Set<String> fSet = new HashSet<String>();
		
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
			setPrefValue(fPref, fSet.toArray(new String[fSet.size()]));
		}
		
		@Override
		public String toString() {
			return fLabel;
		}
		
	}
	
	private class WordEditing extends EditingSupport {
		
		private TextCellEditor fCellEditor;
		private Object fLast;
		
		public WordEditing() {
			super(fWordList);
			fCellEditor = new TextCellEditor(fWordList.getTable()) {
				@Override
				public void deactivate() {
					super.deactivate();
					if (fLast == "") { //$NON-NLS-1$
						fWordList.remove(""); //$NON-NLS-1$
						fWordListComposite.layout(false);
					}
					fLast = null;
				}
			};
			fCellEditor.addListener(new ICellEditorListener() {
				public void editorValueChanged(final boolean oldValidState, final boolean newValidState) {
					if (!newValidState) {
						fStatusListener.statusChanged(new Status(Status.ERROR, RUI.PLUGIN_ID, fCellEditor.getErrorMessage()));
					}
					else {
						fStatusListener.statusChanged(Status.OK_STATUS);
					}
				}
				public void applyEditorValue() {
					fStatusListener.statusChanged(Status.OK_STATUS);
				}
				public void cancelEditor() {
					fStatusListener.statusChanged(Status.OK_STATUS);
				}
			});
			fCellEditor.setValidator(new ICellEditorValidator() {
				private RSourceTokenLexer fLexer = new RSourceTokenLexer();
				public String isValid(final Object value) {
					if (value == null || value.equals("")) { //$NON-NLS-1$
						return Messages.RIdentifiers_Identifier_error_Empty_message;
					}
					if (!value.equals(fLast) && fActiveCategory.fSet.contains(value)) {
						return Messages.RIdentifiers_Identifier_error_AlreadyExistingInSameGroup_message;
					}
					for (int i = 0; i < fCategories.length; i++) {
						if (fCategories[i] != fActiveCategory && fCategories[i].fSet.contains(value)) {
							return NLS.bind(Messages.RIdentifiers_Identifier_error_AlreadyExistingInOtherGroup_message, fCategories[i].fLabel);
						}
					}
					fLexer.reset(new StringParseInput((String) value), null);
					if (fLexer.nextToken().getTokenType() != RTerminal.SYMBOL
							|| fLexer.nextToken().getTokenType() != RTerminal.EOF) {
						return Messages.RIdentifiers_Identifier_error_Invalid_message;
					}
					return null;
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
				editWord(element, value);
			}
		}
		
	}
	
	
	private TableViewer fCategoryList;
	private TableViewer fWordList;
	private ViewerUtil.TableComposite fWordListComposite;
	
	private Category[] fCategories;
	private Category fActiveCategory;
	
	private IStatusChangeListener fStatusListener;
	
	
	public RIdentifiersBlock(final IStatusChangeListener statusListener) {
		super(null, null);
		fStatusListener = statusListener;
	}
	
	
	@Override
	protected String[] getChangedGroups() {
		return new String[] {
				RIdentifierGroups.GROUP_ID,
				RUIPreferenceConstants.R.TS_GROUP_ID,
		};
	}
	
	@Override
	public void createContents(final Composite pageComposite, final IWorkbenchPreferenceContainer container, final IPreferenceStore preferenceStore) {
		super.createContents(pageComposite, container, preferenceStore);
		
		// Preferences
		final List<Preference> prefs = new ArrayList<Preference>();
		fCategories = new Category[] {
				new Category(Messages.RSyntaxColoring_Identifier_Assignment_label, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_ASSIGNMENT_ITEMS),
				new Category(Messages.RSyntaxColoring_Identifier_Flowcontrol_label, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_FLOWCONTROL_ITEMS),
				new Category(Messages.RSyntaxColoring_Identifier_Logical_label, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_LOGICAL_ITEMS),
				new Category(Messages.RSyntaxColoring_Identifier_Custom1_label, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM1_ITEMS),
				new Category(Messages.RSyntaxColoring_Identifier_Custom2_label, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM2_ITEMS),
		};
		for (int i = 0; i < fCategories.length; i++) {
			prefs.add(fCategories[i].fPref);
		}
		setupPreferenceManager(container, prefs.toArray(new Preference[prefs.size()]));
		
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
		
		final Composite wordComposite = new Composite(pageComposite, SWT.NONE);
		wordComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		wordComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(),2));
		
		fWordListComposite = new ViewerUtil.TableComposite(wordComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		fWordListComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fWordList = fWordListComposite.viewer;
		fWordList.setContentProvider(new ArrayContentProvider());
		fWordList.setComparator(new ViewerComparator(new RNamesComparator()) {
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2) {
				return getComparator().compare(e1, e2);
			}
		});
		fWordListComposite.table.setFont(JFaceResources.getTextFont());
		fWordListComposite.table.setLinesVisible(true);
		
		column = new TableViewerColumn(fWordList, SWT.NONE);
		column.setLabelProvider(new ColumnLabelProvider());
		column.setEditingSupport(new WordEditing());
		fWordListComposite.layout.setColumnData(column.getColumn(), new ColumnWeightData(100));
		
		final Composite buttonComposite = new Composite(wordComposite, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		buttonComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(),1));
		
		Button button = new Button(buttonComposite, SWT.PUSH);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = LayoutUtil.hintWidth(button);
		button.setLayoutData(gd);
		
		button.setText(Messages.RIdentifiers_AddAction_label);
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
			public void widgetSelected(final SelectionEvent e) {
				addNewWord();
			}
		});
		
		button = new Button(buttonComposite, SWT.PUSH);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = LayoutUtil.hintWidth(button);
		button.setLayoutData(gd);
		
		button.setText(Messages.RIdentifiers_EditAction_label);
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = (IStructuredSelection) fWordList.getSelection();
				if (selection.size() == 1) {
					fWordList.setSelection(selection, true);
					fWordList.editElement(selection.getFirstElement(), 0);
				}
			}
		});
		
		button = new Button(buttonComposite, SWT.PUSH);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = LayoutUtil.hintWidth(button);
		button.setLayoutData(gd);
		
		button.setText(Messages.RIdentifiers_RemoveAction_label);
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = (IStructuredSelection) fWordList.getSelection();
				deleteWords(selection.toArray());
			}
		});
		
		
		// Binding
		fCategoryList.setInput(fCategories);
		fCategoryList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final Category cat = (Category) ((IStructuredSelection) event.getSelection()).getFirstElement();
				fWordList.setInput(cat.fSet);
				fActiveCategory = cat;
				fWordListComposite.layout(false);
			}
		});
		
		// Init
		fActiveCategory = fCategories[0];
		updateControls();
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				if (UIAccess.isOkToUse(fCategoryList)) {
					fCategoryList.setSelection(new StructuredSelection(fActiveCategory));
				}
			}
		});
	}
	
	
	void addNewWord() {
		fWordList.add(""); //$NON-NLS-1$
		fWordList.setSelection(new StructuredSelection(""), true); //$NON-NLS-1$
		fWordList.editElement("", 0); //$NON-NLS-1$
		fWordListComposite.layout(false);
	}
	
	void deleteWords(final Object[] words) {
		fActiveCategory.fSet.removeAll(Arrays.asList(words));
		fWordList.remove(words);
		fWordListComposite.layout(false);
	}
	
	void editWord(final Object old, final Object word) {
		if (old.equals(word)) {
			return;
		}
		fActiveCategory.fSet.remove(old);
		fWordList.remove(old);
		fActiveCategory.fSet.add((String) word);
		fWordList.add(word);
		fWordList.setSelection(new StructuredSelection(word), true);
	}
	
	
	@Override
	public void onBeforeSave() {
		for (int i = 0; i < fCategories.length; i++) {
			fCategories[i].save();
		}
		super.onBeforeSave();
	}
	
	@Override
	protected void updateControls() {
		for (int i = 0; i < fCategories.length; i++) {
			fCategories[i].load();
		}
		fWordList.refresh(false);
		fWordListComposite.layout(false);
	}
	
}
