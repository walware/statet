/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.workbench;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import de.walware.ecommons.ConstList;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.ui.dialogs.Layouter;
import de.walware.ecommons.ui.preferences.ColorSelectorObservableValue;
import de.walware.ecommons.ui.preferences.OverlayStoreConfigurationBlock;
import de.walware.ecommons.ui.preferences.OverlayStorePreference;
import de.walware.ecommons.ui.preferences.PreferenceStoreBeanWrapper;
import de.walware.ecommons.ui.preferences.RGBPref;
import de.walware.ecommons.ui.text.presentation.ITextPresentationConstants;
import de.walware.ecommons.ui.text.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.ui.text.sourceediting.SourceViewerJFaceUpdater;
import de.walware.ecommons.ui.util.ColorManager;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.MessageUtil;
import de.walware.ecommons.ui.util.PixelConverter;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.Node;
import de.walware.ecommons.ui.workbench.AbstractSyntaxColoringBlock.SyntaxNode.UseStyle;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.internal.ui.preferences.Messages;


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
			
			public UseStyle(final String refRootKey, final String label) {
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
		
		public static UseStyle createUseNoExtraStyle(final String parentKey) {
			return new UseStyle(parentKey, Messages.SyntaxColoring_Use_NoExtraStyle_label);
		}
		
		public static UseStyle createUseOtherStyle(final String otherKey, final String otherLabel) {
			return new UseStyle(otherKey, NLS.bind(Messages.SyntaxColoring_Use_OtherStyle_label, otherLabel));
		}
		
		
		public static final String PROP_USE = "useStyle"; //$NON-NLS-1$
		public static final String PROP_COLOR = "color"; //$NON-NLS-1$
		public static final String PROP_BOLD = "bold"; //$NON-NLS-1$
		public static final String PROP_ITALIC = "italic"; //$NON-NLS-1$
		public static final String PROP_STRIKETHROUGH = "strikethrough"; //$NON-NLS-1$
		public static final String PROP_UNDERLINE = "underline"; //$NON-NLS-1$
		
		private SyntaxNode(final String name, final SyntaxNode[] children) {
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
		
		public void addPropertyChangeListener(final PropertyChangeListener listener) {
		}
		
		public void addPropertyChangeListener(final String propertyName,
				final PropertyChangeListener listener) {
		}
		
		public void removePropertyChangeListener(final PropertyChangeListener listener) {
		}
		
		public void removePropertyChangeListener(final String propertyName,
				final PropertyChangeListener listener) {
		}
		
		
		/*-- Property-Access --*/
		public List<UseStyle> getAvailableUseStyles() {
			return Collections.emptyList();
		}
		
		public UseStyle getUseStyle() {
			return null;
		}
		public void setUseStyle(final UseStyle useStyle) {
		}
		
		public RGB getColor() {
			return null;
		}
		public void setColor(final RGB color) {
		}
		
		public boolean isBold() {
			return false;
		}
		public void setBold(final boolean enabled) {
		}
		
		public boolean isItalic() {
			return false;
		}
		public void setItalic(final boolean enabled) {
		}
		
		public boolean isStrikethrough() {
			return false;
		}
		public void setStrikethrough(final boolean enabled) {
		}
		
		public boolean isUnderline() {
			return false;
		}
		public void setUnderline(final boolean enabled) {
		}
	}
	
	/**
	 * Category Node without syntax style.
	 */
	protected static class CategoryNode extends SyntaxNode {
		
		public CategoryNode(final String name, final SyntaxNode[] children) {
			super(name, children);
		}
	}
	
	/**
	 * Style Node with syntax style, connected to overlay-preferencestory.
	 */
	protected static class StyleNode extends SyntaxNode {
		
		public class UseStylePref extends Preference<UseStyle> {
			UseStylePref(final String qualifier, final String key) {
				super(qualifier, key, Type.STRING);
			}
			@Override
			public Class<UseStyle> getUsageType() {
				return UseStyle.class;
			}
			@Override
			public UseStyle store2Usage(final Object obj) {
				return getUseStyle((String) obj);
			}
			@Override
			public Object usage2Store(final UseStyle obj) {
				return obj.getRefRootKey();
			}
		}
		
		private String fDescription;
		private String fRootKey;
		private List<UseStyle> fAvailableStyles;
		
		/** tuple { pref : Preference, beanProperty : String } */
		private Object[][] fPreferences;
		private IPreferenceStore fPreferenceStore;
		private PreferenceStoreBeanWrapper fBeanSupport;
		
		
		public StyleNode(final String name, final String description, final String rootKey, final UseStyle[] availableStyles, final SyntaxNode[] children) {
			super(name, children);
			assert (availableStyles != null && availableStyles.length > 0);
			fDescription = description;
			fRootKey = rootKey;
			fAvailableStyles = new ConstList<UseStyle>(availableStyles);
			
			final List<Object[]> prefs = new ArrayList<Object[]>();
			if (fAvailableStyles.size() > 1) {
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
			return fRootKey + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		}
		private String getColorKey() {
			return fRootKey + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		}
		private String getBoldKey() {
			return fRootKey + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		}
		private String getItalicKey() {
			return fRootKey + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		}
		private String getUnderlineKey() {
			return fRootKey + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		}
		private String getStrikethroughKey() {
			return fRootKey + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		}
		
		protected void gatherPreferenceKeys(final List<OverlayStorePreference> keys) {
			for (final Object[] pref : fPreferences) {
				keys.add(OverlayStorePreference.create((Preference) pref[0]));
			}
		}
		protected void connectPreferenceStore(final IPreferenceStore store) {
			fPreferenceStore = store;
			fBeanSupport = new PreferenceStoreBeanWrapper(store, this);
			for (final Object[] pref : fPreferences) {
				fBeanSupport.addPreference((String) pref[1], (Preference) pref[0]);
			}
		}
		
		
		/*-- Bean-Support --*/
		
		@Override
		public void addPropertyChangeListener(final PropertyChangeListener listener) {
			fBeanSupport.addPropertyChangeListener(listener);
		}
		
		@Override
		public void addPropertyChangeListener(final String propertyName,
				final PropertyChangeListener listener) {
			fBeanSupport.addPropertyChangeListener(propertyName, listener);
		}
		
		@Override
		public void removePropertyChangeListener(final PropertyChangeListener listener) {
			fBeanSupport.removePropertyChangeListener(listener);
		}
		
		@Override
		public void removePropertyChangeListener(final String propertyName,
				final PropertyChangeListener listener) {
			fBeanSupport.removePropertyChangeListener(propertyName, listener);
		}
		
		
		/*-- Property-Access --*/
		@Override
		public List<UseStyle> getAvailableUseStyles() {
			return fAvailableStyles;
		}
		
		@Override
		public void setUseStyle(final UseStyle useStyle) {
			if (useStyle != null) {
				fPreferenceStore.setValue(getUseKey(), useStyle.getRefRootKey());
			}
		}
		@Override
		public UseStyle getUseStyle() {
			return getUseStyle(fPreferenceStore.getString(getUseKey()));
		}
		private UseStyle getUseStyle(final String value) {
			for (final UseStyle style : fAvailableStyles) {
				if (style.getRefRootKey().equals(value)) {
					return style;
				}
			}
			return fAvailableStyles.get(0);
		}
		
		@Override
		public RGB getColor() {
			return PreferenceConverter.getColor(fPreferenceStore, getColorKey());
		}
		@Override
		public void setColor(final RGB color) {
			PreferenceConverter.setValue(fPreferenceStore, getColorKey(), color);
		}
		
		@Override
		public boolean isBold() {
			return fPreferenceStore.getBoolean(getBoldKey());
		}
		@Override
		public void setBold(final boolean enabled) {
			fPreferenceStore.setValue(getBoldKey(), enabled);
		}
		
		@Override
		public boolean isItalic() {
			return fPreferenceStore.getBoolean(getItalicKey());
		}
		@Override
		public void setItalic(final boolean enabled) {
			fPreferenceStore.setValue(getItalicKey(), enabled);
		}
		
		@Override
		public boolean isStrikethrough() {
			return fPreferenceStore.getBoolean(getStrikethroughKey());
		}
		@Override
		public void setStrikethrough(final boolean enabled) {
			fPreferenceStore.setValue(getStrikethroughKey(), enabled);
		}
		
		@Override
		public boolean isUnderline() {
			return fPreferenceStore.getBoolean(getUnderlineKey());
		}
		@Override
		public void setUnderline(final boolean enabled) {
			fPreferenceStore.setValue(getUnderlineKey(), enabled);
		}
	}
	
	
	private static class SyntaxNodeLabelProvider extends CellLabelProvider {
		
		@Override
		public boolean useNativeToolTip(final Object object) {
			return true;
		}
		@Override
		public String getToolTipText(final Object element) {
			if (element instanceof StyleNode) {
				return ((StyleNode) element).getDescription();
			}
			return null;
		}
		
		@Override
		public void update(final ViewerCell cell) {
			cell.setText(((Node) cell.getElement()).getName());
		}
	}
	
	private static class UseStyleLabelProvider extends LabelProvider {
		@Override
		public String getText(final Object element) {
			final UseStyle style = (UseStyle) element;
			return style.getLabel();
		}
	}
	
	private SyntaxNode[] fRootNodes;
	private DataBindingContext fDbc;
	
	private TreeViewer fSelectionViewer;
	private SourceEditorViewerConfiguration fConfiguration;
	private Set<String> fGroupIds;
	
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
	protected abstract String[] getSettingsGroups();
	
	
	@Override
	protected Set<String> getChangedGroups() {
		return fGroupIds;
	}
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		// Prepare model
		fRootNodes = createItems();
		final String[] groupIds = getSettingsGroups();
		fGroupIds = new HashSet<String>();
		fGroupIds.addAll(Arrays.asList(groupIds));
		final List<OverlayStorePreference> keys = new ArrayList<OverlayStorePreference>();
		collectKeys(keys, fRootNodes);
		setupOverlayStore(keys.toArray(new OverlayStorePreference[keys.size()]));
		connectStore(fRootNodes);
		
		addLinkHeader(pageComposite, Messages.SyntaxColoring_link);
		
		final Layouter content = new Layouter(new Composite(pageComposite, SWT.NONE), 2);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		content.composite.setLayoutData(gd);
		
		// Tree / Options
		content.addLabel(Messages.SyntaxColoring_List_label);
		final Layouter group = new Layouter(new Composite(content.composite, SWT.NONE), 2);
		group.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, content.fNumColumns, 1));
		final Control selectionControl = createTreeViewer(group);
		gd = new GridData(SWT.FILL, SWT.FILL, false, true);
		final Point size = ViewerUtil.calculateTreeSizeHint(fSelectionViewer.getControl(), fRootNodes, 9);
		gd.widthHint = size.x;
		gd.heightHint = size.y;
		selectionControl.setLayoutData(gd);
		
		final Control optionControl = createOptionsControl(group);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalIndent = 5;
		optionControl.setLayoutData(gd);
		
		// Previewer
		content.addLabel(Messages.SyntaxColoring_Preview);
		final Control previewerControl = createPreviewer(content.composite);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		final PixelConverter conv = new PixelConverter(previewerControl);
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
	
	private void collectKeys(final List<OverlayStorePreference> keys, final SyntaxNode[] nodes) {
		for (final SyntaxNode node : nodes) {
			if (node instanceof StyleNode) {
				((StyleNode) node).gatherPreferenceKeys(keys);
			}
			final SyntaxNode[] children = node.getChildren();
			if (children != null) {
				collectKeys(keys, children);
			}
		}
	}
	
	private void connectStore(final SyntaxNode[] nodes) {
		for (final SyntaxNode node: nodes) {
			if (node instanceof StyleNode) {
				((StyleNode) node).connectPreferenceStore(fOverlayStore);
			}
			final SyntaxNode[] children = node.getChildren();
			if (children != null) {
				connectStore(children);
			}
		}
	}
	
	
	public Control createTreeViewer(final Layouter parent) {
		fSelectionViewer = new TreeViewer(parent.composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		fSelectionViewer.setContentProvider(new ViewerUtil.NodeContentProvider());
		fSelectionViewer.setLabelProvider(new SyntaxNodeLabelProvider());
		ColumnViewerToolTipSupport.enableFor(fSelectionViewer);
		
		ViewerUtil.addDoubleClickExpansion(fSelectionViewer);
		
		return fSelectionViewer.getControl();
	}
	
	private Control createOptionsControl(final Layouter parent) {
		GridData gd;
		
		final Layouter options = new Layouter(new Composite(parent.composite, SWT.NONE), 2);
		fUseControl = new ComboViewer(options.composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		fUseControl.setLabelProvider(new UseStyleLabelProvider());
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 0);
		gd.widthHint = LayoutUtil.hintWidth(fUseControl.getCombo(),
				new String[] { "XXXXXXXXXXXXXXX", Messages.SyntaxColoring_Use_CustomStyle_label, Messages.SyntaxColoring_Use_NoExtraStyle_label }); 
		fUseControl.getControl().setLayoutData(gd);
		final int indent = LayoutUtil.defaultSmallIndent();
		options.addLabel(Messages.SyntaxColoring_Color, indent, 1);
		fForegroundColorEditor = new ColorSelector(options.composite);
		final Button foregroundColorButton = fForegroundColorEditor.getButton();
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		foregroundColorButton.setLayoutData(gd);
		fBoldCheckbox = options.addCheckBox(Messages.SyntaxColoring_Bold, indent);
		fItalicCheckbox = options.addCheckBox(Messages.SyntaxColoring_Italic, indent);
		fUnderlineCheckbox = options.addCheckBox(Messages.SyntaxColoring_Underline, indent);
		fStrikethroughCheckbox = options.addCheckBox(Messages.SyntaxColoring_Strikethrough, indent);
		
		return options.composite;
	}
	
	private Control createPreviewer(final Composite parent) {
		fColorManager = new ColorManager();
		
		final IPreferenceStore store = new ChainedPreferenceStore(new IPreferenceStore[] {
				fOverlayStore, EditorsUI.getPreferenceStore() });
		fPreviewViewer = new SourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		final Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
		fPreviewViewer.getTextWidget().setFont(font);
		fPreviewViewer.setEditable(false);
		fConfiguration = getSourceViewerConfiguration(fColorManager, store);
		fPreviewViewer.configure(fConfiguration);
		new SourceViewerJFaceUpdater(fPreviewViewer, fConfiguration, store);
		
		final String content = loadPreviewContentFromFile(getPreviewFileName());
		final IDocument document = new Document(content);
		getDocumentSetupParticipant().setup(document);
		fPreviewViewer.setDocument(document);
		
		return fPreviewViewer.getControl();
	}
	
	protected abstract String getPreviewFileName();
	
	protected abstract SourceEditorViewerConfiguration getSourceViewerConfiguration(ColorManager colorManager, IPreferenceStore store);
	
	protected abstract IDocumentSetupParticipant getDocumentSetupParticipant();
	
	private String loadPreviewContentFromFile(final String filename) {
		String line;
		final String separator = System.getProperty("line.separator"); //$NON-NLS-1$
		final StringBuffer buffer = new StringBuffer(512);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			while ((line= reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (final IOException io) {
			StatetUIPlugin.logUnexpectedError(io);
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (final IOException e) {}
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
		final IObservableValue selection = ViewersObservables.observeSingleSelection(fSelectionViewer);
		selection.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(final ValueChangeEvent event) {
				final SyntaxNode newNode = (SyntaxNode) event.diff.getNewValue();
				if (newNode != null) {
					updateEnablement(newNode, newNode.getUseStyle());
				}
			}
		});
		// Bind use style selection
		final IObservableList list = MasterDetailObservables.detailList(
				BeansObservables.observeDetailValue(selection, "availableUseStyles", List.class), //$NON-NLS-1$
				new IObservableFactory() {
					public IObservable createObservable(final Object target) {
						return Observables.staticObservableList(realm, (List) target);
					}
				}, null);
		fUseControl.setContentProvider(new ObservableListContentProvider());
		fUseControl.setInput(list);
		final IObservableValue useStyle = BeansObservables.observeDetailValue(selection, SyntaxNode.PROP_USE, UseStyle.class);
		useStyle.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(final ValueChangeEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) fSelectionViewer.getSelection();
				final UseStyle newUse = (UseStyle) event.diff.getNewValue();
				updateEnablement((SyntaxNode) selection.getFirstElement(), newUse);
			}
		});
		fDbc.bindValue(ViewersObservables.observeSingleSelection(fUseControl),
				useStyle,
				null, null);
		// Bind option widgets to the properties of the current selection.
		fDbc.bindValue(new ColorSelectorObservableValue(fForegroundColorEditor),
				BeansObservables.observeDetailValue(selection, SyntaxNode.PROP_COLOR, RGB.class),
				null, null);
		fDbc.bindValue(SWTObservables.observeSelection(fBoldCheckbox),
				BeansObservables.observeDetailValue(selection, SyntaxNode.PROP_BOLD, boolean.class),
				null, null);
		fDbc.bindValue(SWTObservables.observeSelection(fItalicCheckbox),
				BeansObservables.observeDetailValue(selection, SyntaxNode.PROP_ITALIC, boolean.class),
				null, null);
		fDbc.bindValue(SWTObservables.observeSelection(fStrikethroughCheckbox),
				BeansObservables.observeDetailValue(selection, SyntaxNode.PROP_STRIKETHROUGH, boolean.class),
				null, null);
		fDbc.bindValue(SWTObservables.observeSelection(fUnderlineCheckbox),
				BeansObservables.observeDetailValue(selection, SyntaxNode.PROP_UNDERLINE, boolean.class),
				null, null);
	}
	
	private void updateEnablement(final SyntaxNode node, final UseStyle useStyle) {
		boolean enableOptions;
		if (node instanceof StyleNode) {
			fUseControl.getControl().setEnabled(node.getAvailableUseStyles().size() > 1);
			enableOptions = useStyle != null && useStyle.getRefRootKey().equals(""); 
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
			final Map<String, Object> options = new HashMap<String, Object>();
			fConfiguration.handleSettingsChanged(fGroupIds, options);
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
	
	protected String addListToTooltip(final String tooltip, final String[] listItems) {
		final StringBuilder description = new StringBuilder(tooltip);
		final int end = Math.min(20, listItems.length);
		for (int i = 0; i < end; i++) {
			description.append("\n    "); 
			description.append(listItems[i]);
		}
		description.append("\n["+end+"/"+listItems.length+"]"); 
		return MessageUtil.escapeForTooltip(description);
	}
	
	protected String addExtraStyleNoteToTooltip(final String tooltip) {
		return NLS.bind(tooltip, Messages.SyntaxColoring_MindExtraStyle_tooltip);
	}
	
}
