/*******************************************************************************
 * Copyright (c) 2005-2007 StatET-Project (www.walware.de/goto/statet).
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
import java.util.List;

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.IBindingListener;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.conversion.ConvertString2Boolean;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
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

import de.walware.eclipsecommons.preferences.Preference.Type;
import de.walware.eclipsecommons.ui.databinding.ColorSelectorObservableValue;
import de.walware.eclipsecommons.ui.databinding.ConvertString2RGB;
import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.preferences.OverlayStoreConfigurationBlock;
import de.walware.eclipsecommons.ui.preferences.PreferenceKey;
import de.walware.eclipsecommons.ui.preferences.PreferenceStoreBeanWrapper;
import de.walware.eclipsecommons.ui.util.ColorManager;
import de.walware.eclipsecommons.ui.util.PixelConverter;
import de.walware.eclipsecommons.ui.util.TreeUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;
import de.walware.eclipsecommons.ui.util.TreeUtil.Node;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.editors.SourceViewerUpdater;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ui.StatetUiPreferenceConstants;


public abstract class AbstractSyntaxColoringBlock extends OverlayStoreConfigurationBlock {

	
	protected static abstract class SyntaxNode extends Node {
		
		protected SyntaxNode(String name, SyntaxNode[] children) {
			
			super(name, children);
		}
		
		public SyntaxNode[] getChildren() {
			
			return (SyntaxNode[]) super.getChildren();
		}
		
		public String getDescription() {
			
			return null;
		}
		

		public RGB getColor() {
			return null;
		}
		public void setColor(RGB color) {
		}
		
		public boolean isEnabled() {
			return false;
		}
		public void setEnabled() {
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
		
//		-- Bean-Support
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
	}

	protected static class CategoryNode extends SyntaxNode {
		
		public CategoryNode(String name, SyntaxNode[] children) {
			
			super(name, children);
		}
	}
	
	protected static class StyleNode extends CategoryNode {
		
		private static IConverter gBoolConverter = new ConvertString2Boolean();
		private static IConverter gRGBConverter = new ConvertString2RGB();
		
		private String fDescription;
		private String fRootKey;
		private boolean fAllowActivation;
		
		private Object[][] fPreferences; 
		private IPreferenceStore fPreferenceStore;
		private PreferenceStoreBeanWrapper fBeanSupport;
		
		
		public StyleNode(String name, String description, String rootKey, boolean allowActivation, SyntaxNode[] children) {
			
			super(name, children);
			fDescription = description;
			fRootKey = rootKey;
			
			fAllowActivation = allowActivation;
			List<Object[]> prefs = new ArrayList<Object[]>();
			if (fAllowActivation) {
				prefs.add(new Object[] { new PreferenceKey(getEnableKey(), Type.BOOLEAN), "enabled", gBoolConverter }); 
			}
			prefs.add(new Object[] { new PreferenceKey(getColorKey(), Type.STRING), "color", gRGBConverter });
			prefs.add(new Object[] { new PreferenceKey(getBoldKey(), Type.BOOLEAN), "bold", gBoolConverter });
			prefs.add(new Object[] { new PreferenceKey(getItalicKey(), Type.BOOLEAN), "italic", gBoolConverter });
			prefs.add(new Object[] { new PreferenceKey(getUnderlineKey(), Type.BOOLEAN), "underline", gBoolConverter });
			prefs.add(new Object[] { new PreferenceKey(getStrikethroughKey(), Type.BOOLEAN), "strikethrough", gBoolConverter });
			fPreferences = prefs.toArray(new Object[prefs.size()][]);
		}
		
		public StyleNode(String name, String description, String rootKey, boolean allowActivation) {
		
			this(name, description, rootKey, allowActivation, null);
		}

		public String getDescription() {

			return fDescription;
		}

		
		private String getEnableKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_ENABLE_SUFFIX;
		}
		private String getColorKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_COLOR_SUFFIX;
		}
		private String getBoldKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_BOLD_SUFFIX;
		}
		private String getItalicKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_ITALIC_SUFFIX;
		}
		private String getUnderlineKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_UNDERLINE_SUFFIX;
		}
		private String getStrikethroughKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_STRIKETHROUGH_SUFFIX;
		}

		protected void addPreferenceKeys(List<PreferenceKey> keys) {
			
			for (Object[] pref : fPreferences) {
				keys.add((PreferenceKey) pref[0]);
			}
		}
		protected void connectPreferenceStore(IPreferenceStore store) {
			
			fPreferenceStore = store;
			fBeanSupport = new PreferenceStoreBeanWrapper(store, this);
			for (Object[] pref : fPreferences) {
				fBeanSupport.addPreference(((PreferenceKey) pref[0]).fKey, (String) pref[1], (IConverter) pref[2]);
			}
		}

		
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			
			fBeanSupport.addPropertyChangeListener(listener);
		}

		public void addPropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			
			fBeanSupport.addPropertyChangeListener(propertyName, listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			
			fBeanSupport.removePropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			
			fBeanSupport.removePropertyChangeListener(propertyName, listener);
		}

		
		public boolean isEnabled() {
			if (fAllowActivation) {
				return fPreferenceStore.getBoolean(getEnableKey());
			}
			return true;
		}
		public void setEnabled(boolean enabled) {
			if (fAllowActivation) {
				fPreferenceStore.setValue(getEnableKey(), enabled);
			}
		}
		
		public RGB getColor() {
			return PreferenceConverter.getColor(fPreferenceStore, getColorKey());
		}
		public void setColor(RGB color) {
			PreferenceConverter.setValue(fPreferenceStore, getColorKey(), color);
		}
		
		public boolean isBold() {
			return fPreferenceStore.getBoolean(getBoldKey());
		}
		public void setBold(boolean enabled) {
			fPreferenceStore.setValue(getBoldKey(), enabled);
		}
		
		public boolean isItalic() {
			return fPreferenceStore.getBoolean(getItalicKey());
		}
		public void setItalic(boolean enabled) {
			fPreferenceStore.setValue(getItalicKey(), enabled);
		}
		
		public boolean isStrikethrough() {
			return fPreferenceStore.getBoolean(getStrikethroughKey());
		}
		public void setStrikethrough(boolean enabled) {
			fPreferenceStore.setValue(getStrikethroughKey(), enabled);
		}
		
		public boolean isUnderline() {
			return fPreferenceStore.getBoolean(getUnderlineKey());
		}
		public void setUnderline(boolean enabled) {
			fPreferenceStore.setValue(getUnderlineKey(), enabled);
		}
	}
		
	private class SyntaxNodeLabelProvider extends CellLabelProvider {

		public String getText(Object element) {
			return ((Node) element).getName();
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
			cell.setText(getText(cell.getElement()));
		}
	}


	private SyntaxNode[] fRootNodes;
	private DataBindingContext fDbc;
	
	private TreeViewer fSelectionViewer;
	
	private Button fEnableCheckbox;
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
	

	@Override
	public void createContents(Layouter block, IWorkbenchPreferenceContainer container, IPreferenceStore preferenceStore) {
	
		super.createContents(block, container, preferenceStore);
		
		// Prepare model
		fRootNodes = createItems();
		List<PreferenceKey> keys = new ArrayList<PreferenceKey>();
		collectKeys(keys, fRootNodes);
		setupPreferenceManager(preferenceStore, keys.toArray(new PreferenceKey[keys.size()]));
		connectStore(fRootNodes);

		addLinkHeader(block, Messages.SyntaxColoring_link);

		Layouter content = new Layouter(new Composite(block.fComposite, SWT.NONE), 2);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = block.fNumColumns;
		content.fComposite.setLayoutData(gd);
		
		// Tree / Options
		content.addLabel(Messages.SyntaxColoring_List_label);
		Layouter group = new Layouter(new Composite(content.fComposite, SWT.NONE), 2);
		group.fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, content.fNumColumns, 1));
		Control selectionControl = createTreeViewer(group);
		gd = new GridData(SWT.FILL, SWT.FILL, false, true);
		Point size = TreeUtil.calculateTreeSizeHint(fSelectionViewer.getControl(), fRootNodes, 9);
		gd.widthHint = size.x;
		gd.heightHint = size.y;
		selectionControl.setLayoutData(gd);
		
		Control optionControl = createOptionsControl(group);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		optionControl.setLayoutData(gd);

		// Previewer
		content.addLabel(Messages.SyntaxColoring_Preview);
		Control previewerControl = createPreviewer(content.fComposite);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		PixelConverter conv = new PixelConverter(previewerControl);
		gd.widthHint = conv.convertWidthInCharsToPixels(20);
		gd.heightHint = conv.convertHeightInCharsToPixels(5);
		previewerControl.setLayoutData(gd);
		
		initFields();
		initBindings();
		updateControls();

		UIAccess.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (Layouter.isOkToUse(fSelectionViewer)) {
					fSelectionViewer.setSelection(new StructuredSelection(fRootNodes[0]));
				}
			}
		});

	}
	
	private void collectKeys(List<PreferenceKey> keys, SyntaxNode[] nodes) {
		for (SyntaxNode node : nodes) {
			if (node instanceof StyleNode) {
				((StyleNode) node).addPreferenceKeys(keys);
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
		
		fSelectionViewer = new TreeViewer(parent.fComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		fSelectionViewer.setContentProvider(new TreeUtil.NodeContentProvider());
		fSelectionViewer.setLabelProvider(new SyntaxNodeLabelProvider());
		fSelectionViewer.activateCustomTooltips();
		
		TreeUtil.addDoubleClickExpansion(fSelectionViewer);
		
		return fSelectionViewer.getControl();
	}
	
	private Control createOptionsControl(Layouter parent) {
		
		Layouter options = new Layouter(new Composite(parent.fComposite, SWT.NONE), 2);
		fEnableCheckbox = options.addCheckBox(Messages.SyntaxColoring_Enable, 0, 2);
		final int indent = 20;
		options.addLabel(Messages.SyntaxColoring_Color, indent, 1);
		fForegroundColorEditor = new ColorSelector(options.fComposite);
		Button foregroundColorButton = fForegroundColorEditor.getButton();
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		foregroundColorButton.setLayoutData(gd);
		fBoldCheckbox = options.addCheckBox(Messages.SyntaxColoring_Bold, indent);
		fItalicCheckbox = options.addCheckBox(Messages.SyntaxColoring_Italic, indent);
		fUnderlineCheckbox = options.addCheckBox(Messages.SyntaxColoring_Underline, indent);
		fStrikethroughCheckbox = options.addCheckBox(Messages.SyntaxColoring_Strikethrough, indent);
		
		return options.fComposite;
	}
	
	private Control createPreviewer(Composite parent) {
		
		fColorManager = new ColorManager(false);

		IPreferenceStore store = new ChainedPreferenceStore(new IPreferenceStore[] { 
				fOverlayStore, EditorsUI.getPreferenceStore() });
		fPreviewViewer = new SourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
		fPreviewViewer.getTextWidget().setFont(font);
		fPreviewViewer.setEditable(false);
		StatextSourceViewerConfiguration configuration = getSourceViewerConfiguration(fColorManager, store);
		fPreviewViewer.configure(configuration);
		new SourceViewerUpdater(fPreviewViewer, configuration, store);
		
		String content = loadPreviewContentFromFile(getPreviewFileName()); //$NON-NLS-1$
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
			StatetPlugin.logUnexpectedError(io);
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException e) {}
			}
		}
		return buffer.toString();
	}
	
	
//	@Override
//	protected void updateControls() {
//		super.updateControls();
//
//		fGroup.handleListSelection();
//	}


	public void initFields() {

		fSelectionViewer.setInput(fRootNodes);
	// TODO: check
//		fSelectionViewer.refresh();
//		handleListSelection(); // -> deaktiveren
	}
	
	private void initBindings() {
	
// Binding listiner below is enough
//		fSelectionViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//			public void selectionChanged(SelectionChangedEvent event) {
//				updateEnablement((IStructuredSelection) event.getSelection());
//			}
//		});

		Realm realm = Realm.getDefault();
		if (realm == null) {
			realm = SWTObservables.getRealm(UIAccess.getDisplay());
			Realm.setDefault(realm);
		}
		fDbc = new DataBindingContext(realm);

		// Observe changes in selection.
		IObservableValue selection = 
			ViewersObservables.observeSingleSelection(fSelectionViewer);

		// Bind option widgets to the properties of the current selection.
		Binding enablement = fDbc.bindValue(SWTObservables.observeSelection(fEnableCheckbox), 
				BeansObservables.observeDetailValue(realm, selection, 
						"enabled", boolean.class), null);
		enablement.addBindingEventListener(new IBindingListener() {
			public IStatus bindingEvent(BindingEvent e) {
				if (e.pipelinePosition == BindingEvent.PIPELINE_AFTER_CHANGE) {
					updateEnablement((IStructuredSelection) fSelectionViewer.getSelection());
				}
				return Status.OK_STATUS; 
			}
		});
		fDbc.bindValue(new ColorSelectorObservableValue(fForegroundColorEditor), 
				BeansObservables.observeDetailValue(realm, selection, 
						"color", RGB.class), null);
		fDbc.bindValue(SWTObservables.observeSelection(fBoldCheckbox), 
				BeansObservables.observeDetailValue(realm, selection, 
						"bold", boolean.class), null);
		fDbc.bindValue(SWTObservables.observeSelection(fItalicCheckbox), 
				BeansObservables.observeDetailValue(realm, selection, 
						"italic", boolean.class), new BindSpec());
		fDbc.bindValue(SWTObservables.observeSelection(fStrikethroughCheckbox), 
				BeansObservables.observeDetailValue(realm, selection, 
						"strikethrough", boolean.class), new BindSpec());
		fDbc.bindValue(SWTObservables.observeSelection(fUnderlineCheckbox), 
				BeansObservables.observeDetailValue(realm, selection, 
						"underline", boolean.class), new BindSpec());
	}
	
	
	private void updateEnablement(IStructuredSelection selection) {
		
		Object obj = selection.getFirstElement();
		boolean enableOptions;
		if (obj != null && obj instanceof StyleNode) {
			StyleNode style = (StyleNode) obj;
			fEnableCheckbox.setEnabled(style.fAllowActivation);
			enableOptions = style.fAllowActivation ? style.isEnabled() : true;
		}
		else {
			fEnableCheckbox.setEnabled(false);
			enableOptions = false;
		}
		fForegroundColorEditor.setEnabled(enableOptions);
		fBoldCheckbox.setEnabled(enableOptions);
		fItalicCheckbox.setEnabled(enableOptions);
		fStrikethroughCheckbox.setEnabled(enableOptions);
		fUnderlineCheckbox.setEnabled(enableOptions);
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
	
}