/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.preferences;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.ui.databinding.ColorSelectorObservableValue;
import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.preferences.OverlayStoreConfigurationBlock;
import de.walware.eclipsecommons.ui.preferences.OverlayStorePreference;
import de.walware.eclipsecommons.ui.preferences.PreferenceStoreBeanWrapper;
import de.walware.eclipsecommons.ui.preferences.RGBPref;
import de.walware.eclipsecommons.ui.util.ColorManager;
import de.walware.eclipsecommons.ui.util.LayoutUtil;
import de.walware.eclipsecommons.ui.util.PixelConverter;
import de.walware.eclipsecommons.ui.util.ViewerUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;
import de.walware.eclipsecommons.ui.util.ViewerUtil.Node;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.internal.ui.preferences.Messages;
import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.ext.ui.editors.SourceViewerUpdater;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.preferences.AbstractSyntaxColoringBlock.SyntaxNode.UseStyle;


/**
 * Common UI to configure the style of syntax tokens (tree, options, preview).
 */
public abstract class AbstractSyntaxColoringBlock extends OverlayStoreConfigurationBlock {

	
	/**
	 * Generic node of the tree.
	 * 
	 * Note: getter and setters in all nodes for easy DataBinding.
	 */
	protected static abstract class SyntaxNode extends Node {
		
		public static class UseStyle {
			
			private String fLabel;
			private String fRefRootKey;

			public UseStyle(String refRootKey, String label) {
				super();
				fRefRootKey = refRootKey;
				fLabel = label;
			}

			public String getLabel() {
				return fLabel;
			}
			
			public String getRefRootKey() {
				return fRefRootKey;
			}

		}

		public static UseStyle createUseCustomStyle() {
			return new UseStyle("", Messages.SyntaxColoring_Use_CustomStyle_label); //$NON-NLS-1$
		}
		
		public static UseStyle createUseNoExtraStyle(String parentKey) {
			return new UseStyle(parentKey, Messages.SyntaxColoring_Use_NoExtraStyle_label);
		}
		
		public static UseStyle createUseOtherStyle(String otherKey, String otherLabel) {
			return new UseStyle(otherKey, NLS.bind(Messages.SyntaxColoring_Use_OtherStyle_label, otherLabel));
		}
		
		
		public static final String PROP_USE = "useStyle"; //$NON-NLS-1$
		public static final String PROP_COLOR = "color"; //$NON-NLS-1$
		public static final String PROP_BOLD = "bold"; //$NON-NLS-1$
		public static final String PROP_ITALIC = "italic"; //$NON-NLS-1$
		public static final String PROP_STRIKETHROUGH = "strikethrough"; //$NON-NLS-1$
		public static final String PROP_UNDERLINE = "underline"; //$NON-NLS-1$
		
		private SyntaxNode(String name, SyntaxNode[] children) {
			super(name, children);
		}
		
		
		@Override
		public SyntaxNode[] getChildren() {
			return (SyntaxNode[]) super.getChildren();
		}
		
		public String getDescription() {
			return null;
		}
		
		
		/*-- Bean-Support --*/
		
		public void addPropertyChangeListener(PropertyChangeListener listener) {
		}

		public void addPropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
		}

		public void removePropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
		}

		
		/*-- Property-Access --*/
		public UseStyle[] getAvailableUseStyles() {
			return new UseStyle[0];
		}
		
		public UseStyle getUseStyle() {
			return null;
		}
		public void setUseStyle(UseStyle useStyle) {
		}
		
		public RGB getColor() {
			return null;
		}
		public void setColor(RGB color) {
		}
		
		public boolean isBold() {
			return false;
		}
		public void setBold(boolean enabled) {
		}
		
		public boolean isItalic() {
			return false;
		}
		public void setItalic(boolean enabled) {
		}
		
		public boolean isStrikethrough() {
			return false;
		}
		public void setStrikethrough(boolean enabled) {
		}
		
		public boolean isUnderline() {
			return false;
		}
		public void setUnderline(boolean enabled) {
		}
	}

	/**
	 * Category Node without syntax style.
	 */
	protected static class CategoryNode extends SyntaxNode {
		
		public CategoryNode(String name, SyntaxNode[] children) {
			super(name, children);
		}
	}
	
	/**
	 * Style Node with syntax style, connected to overlay-preferencestory.
	 */
	protected static class StyleNode extends SyntaxNode {
		
		public class UseStylePref extends Preference<UseStyle> {
			UseStylePref(String qualifier, String key) {
				super(qualifier, key, Type.STRING);
			}
			@Override
			public Class<UseStyle> getUsageType() {
				return UseStyle.class;
			}
			@Override
			public UseStyle store2Usage(Object obj) {
				return getUseStyle((String) obj);
			}
			@Override
			public Object usage2Store(UseStyle obj) {
				return obj.getRefRootKey();
			}
		}

		private String fDescription;
		private String fRootKey;
		private UseStyle[] fAvailableStyles;
		
		/** tuple { pref : Preference, beanProperty : String } */
		private Object[][] fPreferences;
		private IPreferenceStore fPreferenceStore;
		private PreferenceStoreBeanWrapper fBeanSupport;
		
		
		public StyleNode(String name, String description, String rootKey, UseStyle[] availableStyles, SyntaxNode[] children) {
			
			super(name, children);
			fDescription = description;
			fRootKey = rootKey;
			fAvailableStyles = availableStyles;
			
			List<Object[]> prefs = new ArrayList<Object[]>();
			assert (fAvailableStyles != null && fAvailableStyles.length > 0);
			if (fAvailableStyles.length > 1) {
				prefs.add(new Object[] { new UseStylePref(null, getUseKey()), PROP_USE });
			}
			prefs.add(new Object[] { new RGBPref(null, getColorKey()), PROP_COLOR });
			prefs.add(new Object[] { new Preference.BooleanPref(null, getBoldKey()), PROP_BOLD });
			prefs.add(new Object[] { new Preference.BooleanPref(null, getItalicKey()), PROP_ITALIC });
			prefs.add(new Object[] { new Preference.BooleanPref(null, getUnderlineKey()), PROP_UNDERLINE });
			prefs.add(new Object[] { new Preference.BooleanPref(null, getStrikethroughKey()), PROP_STRIKETHROUGH });
			fPreferences = prefs.toArray(new Object[prefs.size()][]);
		}
		
		@Override
		public String getDescription() {
			return fDescription;
		}


		private String getUseKey() {
			return fRootKey + IStatetUIPreferenceConstants.TS_USE_SUFFIX;
		}
		private String getColorKey() {
			return fRootKey + IStatetUIPreferenceConstants.TS_COLOR_SUFFIX;
		}
		private String getBoldKey() {
			return fRootKey + IStatetUIPreferenceConstants.TS_BOLD_SUFFIX;
		}
		private String getItalicKey() {
			return fRootKey + IStatetUIPreferenceConstants.TS_ITALIC_SUFFIX;
		}
		private String getUnderlineKey() {
			return fRootKey + IStatetUIPreferenceConstants.TS_UNDERLINE_SUFFIX;
		}
		private String getStrikethroughKey() {
			return fRootKey + IStatetUIPreferenceConstants.TS_STRIKETHROUGH_SUFFIX;
		}

		protected void gatherPreferenceKeys(List<OverlayStorePreference> keys) {
			for (Object[] pref : fPreferences) {
				keys.add(OverlayStorePreference.create((Preference) pref[0]));
			}
		}
		protected void connectPreferenceStore(IPreferenceStore store) {
			fPreferenceStore = store;
			fBeanSupport = new PreferenceStoreBeanWrapper(store, this);
			for (Object[] pref : fPreferences) {
				fBeanSupport.addPreference((String) pref[1], (Preference) pref[0]);
			}
		}

		
		/*-- Bean-Support --*/
		
		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			fBeanSupport.addPropertyChangeListener(listener);
		}

		@Override
		public void addPropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			fBeanSupport.addPropertyChangeListener(propertyName, listener);
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			fBeanSupport.removePropertyChangeListener(listener);
		}

		@Override
		public void removePropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			fBeanSupport.removePropertyChangeListener(propertyName, listener);
		}

		
		/*-- Property-Access --*/
		@Override
		public UseStyle[] getAvailableUseStyles() {
			return fAvailableStyles;
		}

		@Override
		public void setUseStyle(UseStyle useStyle) {
			if (useStyle != null) {
				fPreferenceStore.setValue(getUseKey(), useStyle.getRefRootKey());
			}
		}
		@Override
		public UseStyle getUseStyle() {
			return getUseStyle(fPreferenceStore.getString(getUseKey()));
		}
		private UseStyle getUseStyle(String value) {
			for (UseStyle style : fAvailableStyles) {
				if (style.getRefRootKey().equals(value)) {
					return style;
				}
			}
			return fAvailableStyles[0];
		}
		
		@Override
		public RGB getColor() {
			return PreferenceConverter.getColor(fPreferenceStore, getColorKey());
		}
		@Override
		public void setColor(RGB color) {
			PreferenceConverter.setValue(fPreferenceStore, getColorKey(), color);
		}
		
		@Override
		public boolean isBold() {
			return fPreferenceStore.getBoolean(getBoldKey());
		}
		@Override
		public void setBold(boolean enabled) {
			fPreferenceStore.setValue(getBoldKey(), enabled);
		}
		
		@Override
		public boolean isItalic() {
			return fPreferenceStore.getBoolean(getItalicKey());
		}
		@Override
		public void setItalic(boolean enabled) {
			fPreferenceStore.setValue(getItalicKey(), enabled);
		}
		
		@Override
		public boolean isStrikethrough() {
			return fPreferenceStore.getBoolean(getStrikethroughKey());
		}
		@Override
		public void setStrikethrough(boolean enabled) {
			fPreferenceStore.setValue(getStrikethroughKey(), enabled);
		}
		
		@Override
		public boolean isUnderline() {
			return fPreferenceStore.getBoolean(getUnderlineKey());
		}
		@Override
		public void setUnderline(boolean enabled) {
			fPreferenceStore.setValue(getUnderlineKey(), enabled);
		}
	}
	
	
	private class SyntaxNodeLabelProvider extends CellLabelProvider {

		@Override
		public boolean useNativeToolTip(Object object) {
			return true;
		}
		@Override
		public String getToolTipText(Object element) {
			if (element instanceof StyleNode) {
				return ((StyleNode) element).getDescription();
			}
			return null;
		}
		
		@Override
		public void update(ViewerCell cell) {
			cell.setText(((Node) cell.getElement()).getName());
		}
	}

	private class UseStyleLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			UseStyle style = (UseStyle) element;
			return style.getLabel();
		}
	}

	private SyntaxNode[] fRootNodes;
	private DataBindingContext fDbc;
	
	private TreeViewer fSelectionViewer;
	private StatextSourceViewerConfiguration fConfiguration;
	private Set<String> fContexts;
	
	private ComboViewer fUseControl;
	private ColorSelector fForegroundColorEditor;
	private Button fBoldCheckbox;
	private Button fItalicCheckbox;
	private Button fStrikethroughCheckbox;
	private Button fUnderlineCheckbox;
	
	private ColorManager fColorManager;
	protected SourceViewer fPreviewViewer;
	
	
	public AbstractSyntaxColoringBlock() {
	}

	protected abstract SyntaxNode[] createItems();
	protected abstract String[] getSettingsContexts();
	
	@Override
	protected String[] getChangedContexts() {
		return fContexts.toArray(new String[fContexts.size()]);
	}

	@Override
	public void createContents(Composite pageComposite, IWorkbenchPreferenceContainer container,
			IPreferenceStore preferenceStore) {
		super.createContents(pageComposite, container, preferenceStore);
		// Prepare model
		fRootNodes = createItems();
		String[] settingsContexts = getSettingsContexts();
		fContexts = new HashSet<String>();
		fContexts.addAll(Arrays.asList(settingsContexts));
		List<OverlayStorePreference> keys = new ArrayList<OverlayStorePreference>();
		collectKeys(keys, fRootNodes);
		setupOverlayStore(preferenceStore, keys.toArray(new OverlayStorePreference[keys.size()]));
		connectStore(fRootNodes);

		addLinkHeader(pageComposite, Messages.SyntaxColoring_link);

		Layouter content = new Layouter(new Composite(pageComposite, SWT.NONE), 2);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		content.composite.setLayoutData(gd);
		
		// Tree / Options
		content.addLabel(Messages.SyntaxColoring_List_label);
		Layouter group = new Layouter(new Composite(content.composite, SWT.NONE), 2);
		group.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, content.fNumColumns, 1));
		Control selectionControl = createTreeViewer(group);
		gd = new GridData(SWT.FILL, SWT.FILL, false, true);
		Point size = ViewerUtil.calculateTreeSizeHint(fSelectionViewer.getControl(), fRootNodes, 9);
		gd.widthHint = size.x;
		gd.heightHint = size.y;
		selectionControl.setLayoutData(gd);
		
		Control optionControl = createOptionsControl(group);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalIndent = 5;
		optionControl.setLayoutData(gd);

		// Previewer
		content.addLabel(Messages.SyntaxColoring_Preview);
		Control previewerControl = createPreviewer(content.composite);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		PixelConverter conv = new PixelConverter(previewerControl);
		gd.widthHint = conv.convertWidthInCharsToPixels(20);
		gd.heightHint = conv.convertHeightInCharsToPixels(5);
		previewerControl.setLayoutData(gd);
		
		initFields();
		initBindings();

		UIAccess.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (UIAccess.isOkToUse(fSelectionViewer)) {
					fSelectionViewer.setSelection(new StructuredSelection(fRootNodes[0]));
				}
			}
		});

	}
	
	private void collectKeys(List<OverlayStorePreference> keys, SyntaxNode[] nodes) {
		for (SyntaxNode node : nodes) {
			if (node instanceof StyleNode) {
				((StyleNode) node).gatherPreferenceKeys(keys);
			}
			SyntaxNode[] children = node.getChildren();
			if (children != null) {
				collectKeys(keys, children);
			}
		}
	}
	
	private void connectStore(SyntaxNode[] nodes) {
		for (SyntaxNode node: nodes) {
			if (node instanceof StyleNode) {
				((StyleNode) node).connectPreferenceStore(fOverlayStore);
			}
			SyntaxNode[] children = node.getChildren();
			if (children != null) {
				connectStore(children);
			}
		}
	}
	
	
	public Control createTreeViewer(Layouter parent) {
		fSelectionViewer = new TreeViewer(parent.composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		fSelectionViewer.setContentProvider(new ViewerUtil.NodeContentProvider());
		fSelectionViewer.setLabelProvider(new SyntaxNodeLabelProvider());
		ColumnViewerToolTipSupport.enableFor(fSelectionViewer);
		
		ViewerUtil.addDoubleClickExpansion(fSelectionViewer);
		
		return fSelectionViewer.getControl();
	}
	
	private Control createOptionsControl(Layouter parent) {
		GridData gd;
		
		Layouter options = new Layouter(new Composite(parent.composite, SWT.NONE), 2);
		fUseControl = new ComboViewer(options.composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		fUseControl.setLabelProvider(new UseStyleLabelProvider());
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 0);
		gd.widthHint = LayoutUtil.hintWidth(fUseControl.getCombo(),
				new String[] { "XXXXXXXXXXXXXXX", Messages.SyntaxColoring_Use_CustomStyle_label, Messages.SyntaxColoring_Use_NoExtraStyle_label }); //$NON-NLS-1$
		fUseControl.getControl().setLayoutData(gd);
		final int indent = LayoutUtil.defaultSmallIndent();
		options.addLabel(Messages.SyntaxColoring_Color, indent, 1);
		fForegroundColorEditor = new ColorSelector(options.composite);
		Button foregroundColorButton = fForegroundColorEditor.getButton();
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		foregroundColorButton.setLayoutData(gd);
		fBoldCheckbox = options.addCheckBox(Messages.SyntaxColoring_Bold, indent);
		fItalicCheckbox = options.addCheckBox(Messages.SyntaxColoring_Italic, indent);
		fUnderlineCheckbox = options.addCheckBox(Messages.SyntaxColoring_Underline, indent);
		fStrikethroughCheckbox = options.addCheckBox(Messages.SyntaxColoring_Strikethrough, indent);
		
		return options.composite;
	}
	
	private Control createPreviewer(Composite parent) {
		fColorManager = new ColorManager();

		IPreferenceStore store = new ChainedPreferenceStore(new IPreferenceStore[] {
				fOverlayStore, EditorsUI.getPreferenceStore() });
		fPreviewViewer = new SourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
		fPreviewViewer.getTextWidget().setFont(font);
		fPreviewViewer.setEditable(false);
		fConfiguration = getSourceViewerConfiguration(fColorManager, store);
		fPreviewViewer.configure(fConfiguration);
		new SourceViewerUpdater(fPreviewViewer, fConfiguration, store);
		
		String content = loadPreviewContentFromFile(getPreviewFileName());
		IDocument document = new Document(content);
		getDocumentSetupParticipant().setup(document);
		fPreviewViewer.setDocument(document);

		return fPreviewViewer.getControl();
	}
	
	protected abstract String getPreviewFileName();
	
	protected abstract StatextSourceViewerConfiguration getSourceViewerConfiguration(ColorManager colorManager, IPreferenceStore store);
	
	protected abstract IDocumentSetupParticipant getDocumentSetupParticipant();

	private String loadPreviewContentFromFile(String filename) {
		String line;
		String separator = System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer = new StringBuffer(512);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			while ((line= reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			StatetUIPlugin.logUnexpectedError(io);
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException e) {}
			}
		}
		return buffer.toString();
	}
	
	
	public void initFields() {
		fSelectionViewer.setInput(fRootNodes);
	}
	
	private void initBindings() {
		final Realm realm = Realm.getDefault();
		fDbc = new DataBindingContext(realm);

		// Observe changes in selection.
		IObservableValue selection = ViewersObservables.observeSingleSelection(fSelectionViewer);
		selection.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				SyntaxNode newNode = (SyntaxNode) event.diff.getNewValue();
				if (newNode != null) {
					updateEnablement(newNode, newNode.getUseStyle());
				}
			}
		});
		// Bind use style selection
		IObservableList list = MasterDetailObservables.detailList(
				BeansObservables.observeDetailValue(realm, selection, "availableUseStyles", UseStyle[].class), //$NON-NLS-1$
				new IObservableFactory() {
					public IObservable createObservable(Object target) {
						return Observables.staticObservableList(realm, Arrays.asList((UseStyle[]) target));
					}
				}, null);
		fUseControl.setContentProvider(new ObservableListContentProvider());
		fUseControl.setInput(list);
		IObservableValue useStyle = BeansObservables.observeDetailValue(realm, selection, SyntaxNode.PROP_USE, UseStyle.class);
		useStyle.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				IStructuredSelection selection = (IStructuredSelection) fSelectionViewer.getSelection();
				UseStyle newUse = (UseStyle) event.diff.getNewValue();
				updateEnablement((SyntaxNode) selection.getFirstElement(), newUse);
			}
		});
		fDbc.bindValue(ViewersObservables.observeSingleSelection(fUseControl),
				useStyle,
				null, null);
		// Bind option widgets to the properties of the current selection.
		fDbc.bindValue(new ColorSelectorObservableValue(fForegroundColorEditor),
				BeansObservables.observeDetailValue(realm, selection, SyntaxNode.PROP_COLOR, RGB.class),
				null, null);
		fDbc.bindValue(SWTObservables.observeSelection(fBoldCheckbox),
				BeansObservables.observeDetailValue(realm, selection, SyntaxNode.PROP_BOLD, boolean.class),
				null, null);
		fDbc.bindValue(SWTObservables.observeSelection(fItalicCheckbox),
				BeansObservables.observeDetailValue(realm, selection, SyntaxNode.PROP_ITALIC, boolean.class),
				null, null);
		fDbc.bindValue(SWTObservables.observeSelection(fStrikethroughCheckbox),
				BeansObservables.observeDetailValue(realm, selection, SyntaxNode.PROP_STRIKETHROUGH, boolean.class),
				null, null);
		fDbc.bindValue(SWTObservables.observeSelection(fUnderlineCheckbox),
				BeansObservables.observeDetailValue(realm, selection, SyntaxNode.PROP_UNDERLINE, boolean.class),
				null, null);
	}
	
	private void updateEnablement(SyntaxNode node, UseStyle useStyle) {
		boolean enableOptions;
		if (node instanceof StyleNode) {
			fUseControl.getControl().setEnabled(node.getAvailableUseStyles().length > 1);
			enableOptions = useStyle != null && useStyle.getRefRootKey().equals(""); //$NON-NLS-1$
		}
		else {
			fUseControl.getControl().setEnabled(false);
			enableOptions = false;
		}
		fForegroundColorEditor.setEnabled(enableOptions);
		fBoldCheckbox.setEnabled(enableOptions);
		fItalicCheckbox.setEnabled(enableOptions);
		fStrikethroughCheckbox.setEnabled(enableOptions);
		fUnderlineCheckbox.setEnabled(enableOptions);
	}
	
	@Override
	protected void handlePropertyChange() {
		if (UIAccess.isOkToUse(fPreviewViewer)) {
			fConfiguration.handleSettingsChanged(fContexts, null);
			fPreviewViewer.invalidateTextPresentation();
		}
	}
	
	@Override
	public void dispose() {
		if (fDbc != null) {
			fDbc.dispose();
			fDbc = null;
		}
		if (fColorManager != null) {
			fColorManager.dispose();
			fColorManager = null;
		}
		super.dispose();
	}
	
	protected String addListToTooltip(String tooltip, String[] listItems) {
		StringBuilder description = new StringBuilder(tooltip);
		int end = Math.min(20, listItems.length);
		for (int i = 0; i < end; i++) {
			description.append("\n    "); //$NON-NLS-1$
			description.append(listItems[i]);
		}
		description.append("\n["+end+"/"+listItems.length+"]");
		return description.toString();
	}
	
	protected String addExtraStyleNoteToTooltip(String tooltip) {
		return NLS.bind(tooltip, Messages.SyntaxColoring_MindExtraStyle_tooltip);
	}
	
}