/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import de.walware.eclipsecommon.ui.dialogs.Layouter;
import de.walware.eclipsecommon.ui.dialogs.groups.CategorizedItem;
import de.walware.eclipsecommon.ui.dialogs.groups.CategorizedOptionsGroup;
import de.walware.eclipsecommon.ui.preferences.OverlayStoreConfigurationBlock;
import de.walware.eclipsecommon.ui.preferences.PreferenceKey;
import de.walware.eclipsecommon.ui.preferences.PreferenceKey.Type;
import de.walware.eclipsecommon.ui.util.ColorManager;
import de.walware.eclipsecommon.ui.util.PixelConverter;
import de.walware.statet.base.StatetPlugin;
import de.walware.statet.base.StatetPreferenceConstants;
import de.walware.statet.ext.ui.editors.SourceViewerUpdater;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ui.StatetUiPreferenceConstants;


public abstract class AbstractSyntaxColoringBlock extends OverlayStoreConfigurationBlock {

	public static class SyntaxItem extends CategorizedItem {
		
		private String fRootKey;
		private String fDescription;
		
		public SyntaxItem(String label, String description, String rootKey) {
			
			super(label);
			fRootKey = rootKey;
			fDescription = description;
		}
		
		public String getEnableKey() {
			return null;
		}
		public String getColorKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_COLOR_SUFFIX;
		}
		public String getBoldKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_BOLD_SUFFIX;
		}
		public String getItalicKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_ITALIC_SUFFIX;
		}
		public String getUnderlineKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_UNDERLINE_SUFFIX;
		}
		public String getStrikethroughKey() {
			return fRootKey + StatetUiPreferenceConstants.TS_STRIKETHROUGH_SUFFIX;
		}
	}
	
	protected class SyntaxColoringGroup extends CategorizedOptionsGroup<SyntaxItem> {
		
		Button fEnableCheckbox;
		ColorSelector fForegroundColorEditor;
		Label fForegroundColorEditorLabel;
		Button fBoldCheckbox;
		Button fItalicCheckbox;
		Button fStrikethroughCheckbox;
		Button fUnderlineCheckbox;
		
		Label fDescriptionLabel;

		Composite fStylesComposite;
	
		protected SyntaxColoringGroup() {
			super(false, false);
		}
		
		@Override
		protected Control createOptionsControl(Composite parent, GridData gd) {
			
			fStylesComposite = new Composite(parent, SWT.NONE);
			return fStylesComposite;
		}
		
		
		@Override
		public void handleListSelection() {
			
			SyntaxItem item = getSingleSelectedItem();
			boolean enableOptions = true;
			if (item == null) {
				fEnableCheckbox.setEnabled(false);
				enableOptions = false;

				fDescriptionLabel.setEnabled(false);
				fDescriptionLabel.setToolTipText(null);
			} else {
//				if () {
//					fEnableCheckbox.setEnabled(true);
//					boolean enable = fOverlayStore.getBoolean(((SemanticHighlightingColorListItem) item).getEnableKey());
//					fGroup.fEnableCheckbox.setSelection(enable);
//					enableOptions = enable;
//				} else
					fEnableCheckbox.setEnabled(false);
				RGB rgb = PreferenceConverter.getColor(fOverlayStore, item.getColorKey());
				fForegroundColorEditor.setColorValue(rgb);		
				fBoldCheckbox.setSelection(fOverlayStore.getBoolean(item.getBoldKey()));
				fItalicCheckbox.setSelection(fOverlayStore.getBoolean(item.getItalicKey()));
				fStrikethroughCheckbox.setSelection(fOverlayStore.getBoolean(item.getStrikethroughKey()));
				fUnderlineCheckbox.setSelection(fOverlayStore.getBoolean(item.getUnderlineKey()));
				
				fDescriptionLabel.setEnabled(true);
				fDescriptionLabel.setToolTipText(item.fDescription);
			}

			fGroup.fForegroundColorEditor.getButton().setEnabled(enableOptions);
			fGroup.fForegroundColorEditorLabel.setEnabled(enableOptions);
			fGroup.fBoldCheckbox.setEnabled(enableOptions);
			fGroup.fItalicCheckbox.setEnabled(enableOptions);
			fGroup.fStrikethroughCheckbox.setEnabled(enableOptions);
			fGroup.fUnderlineCheckbox.setEnabled(enableOptions);
		}
	}
	
	
	private ColorManager fColorManager;
	
	protected SyntaxColoringGroup fGroup;
	protected SourceViewer fPreviewViewer;
	
	
	public AbstractSyntaxColoringBlock() {
		
		fGroup = new SyntaxColoringGroup();
	}

	public void setupItems(String[] categorys, SyntaxItem[][] children) {
		
		fGroup.fCategorys = categorys;
		fGroup.fCategoryChilds = children;
		fGroup.generateListModel();
	}
	
	@Override
	public void createContents(Layouter layouter, IWorkbenchPreferenceContainer container, IPreferenceStore preferenceStore) {
	
		fColorManager = new ColorManager(false);

		super.createContents(layouter, container, preferenceStore);
		
		List<PreferenceKey> keys = new ArrayList<PreferenceKey>();
		
		for (SyntaxItem item : fGroup.fSelectionModel) {
			keys.add(new PreferenceKey(item.getColorKey(), Type.STRING));
			keys.add(new PreferenceKey(item.getBoldKey(), Type.BOOLEAN));
			keys.add(new PreferenceKey(item.getItalicKey(), Type.BOOLEAN));
			keys.add(new PreferenceKey(item.getUnderlineKey(), Type.BOOLEAN));
			keys.add(new PreferenceKey(item.getStrikethroughKey(), Type.BOOLEAN));
		}
		setupPreferenceManager(preferenceStore, keys.toArray(new PreferenceKey[keys.size()]));

		addLinkHeader(layouter, Messages.SyntaxColoring_link);

		Layouter content = new Layouter(new Composite(layouter.fComposite, SWT.NONE), 2);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = layouter.fNumColumns;
		content.fComposite.setLayoutData(gd);
		
		createColorSection(content);
		
		fGroup.initFields();
		updateControls();
	}
	
	@Override
	public void dispose() {

		fColorManager.dispose();
		fColorManager = null;
		
		super.dispose();
	}
	
	private void createColorSection(Layouter layouter) {
		
		layouter.addLabel(Messages.SyntaxColoring_List_label);
		
		layouter.addGroup(fGroup);
		
		Layouter stylesLayouter = new Layouter(fGroup.fStylesComposite, 2);
		fGroup.fEnableCheckbox = stylesLayouter.addCheckBox(
				Messages.SyntaxColoring_Enable, 0, 1);
		
//		fEnableCheckbox.addSelectionListener(new SelectionListener() {
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// do nothing
//			}
//			public void widgetSelected(SelectionEvent e) {
//				HighlightingColorListItem item= getHighlightingColorListItem();
//				if (item instanceof SemanticHighlightingColorListItem) {
//					boolean enable= fEnableCheckbox.getSelection();
//					fOverlayStore.setValue(((SemanticHighlightingColorListItem) item).getEnableKey(), enable);
//					fEnableCheckbox.setSelection(enable);
//					fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
//					fColorEditorLabel.setEnabled(enable);
//					fBoldCheckBox.setEnabled(enable);
//					fItalicCheckBox.setEnabled(enable);
//					fStrikethroughCheckBox.setEnabled(enable);
//					fUnderlineCheckBox.setEnabled(enable);
//					uninstallSemanticHighlighting();
//					installSemanticHighlighting();
//				}
//			}
//		});

		fGroup.fDescriptionLabel = new Label(stylesLayouter.fComposite, SWT.RIGHT);
		fGroup.fDescriptionLabel.setText(Messages.SyntaxColoring_ItemInfo);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 1;
		fGroup.fDescriptionLabel.setLayoutData(gd);
		
		
		final int indent = 20;
		
		fGroup.fForegroundColorEditorLabel = stylesLayouter.addLabel(Messages.SyntaxColoring_Color, indent, 1);
		fGroup.fForegroundColorEditor = new ColorSelector(stylesLayouter.fComposite);
		Button foregroundColorButton = fGroup.fForegroundColorEditor.getButton();
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		foregroundColorButton.setLayoutData(gd);
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				SyntaxItem item = fGroup.getSingleSelectedItem();
				PreferenceConverter.setValue(fOverlayStore, item.getColorKey(), fGroup.fForegroundColorEditor.getColorValue());
			}
		});
		
		fGroup.fBoldCheckbox = stylesLayouter.addCheckBox(Messages.SyntaxColoring_Bold, indent);
		fGroup.fBoldCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				SyntaxItem item = fGroup.getSingleSelectedItem();
				fOverlayStore.setValue(item.getBoldKey(), fGroup.fBoldCheckbox.getSelection());
			}
		});
		
		fGroup.fItalicCheckbox = stylesLayouter.addCheckBox(Messages.SyntaxColoring_Italic, indent);
		fGroup.fItalicCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				SyntaxItem item = fGroup.getSingleSelectedItem();
				fOverlayStore.setValue(item.getItalicKey(), fGroup.fItalicCheckbox.getSelection());
			}
		});
		
		fGroup.fUnderlineCheckbox = stylesLayouter.addCheckBox(Messages.SyntaxColoring_Underline, indent);
		fGroup.fUnderlineCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				SyntaxItem item = fGroup.getSingleSelectedItem();
				fOverlayStore.setValue(item.getUnderlineKey(), fGroup.fUnderlineCheckbox.getSelection());
			}
		});
		
		fGroup.fStrikethroughCheckbox = stylesLayouter.addCheckBox(Messages.SyntaxColoring_Strikethrough, indent);
		fGroup.fStrikethroughCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				SyntaxItem item = fGroup.getSingleSelectedItem();
				fOverlayStore.setValue(item.getStrikethroughKey(), fGroup.fStrikethroughCheckbox.getSelection());
			}
		});
		
		
		

		// Previewer
		layouter.addLabel(Messages.SyntaxColoring_Preview);
		
		Control previewer = createPreviewer(layouter.fComposite);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		PixelConverter conv = new PixelConverter(previewer);
		gd.widthHint = conv.convertWidthInCharsToPixels(20);
		gd.heightHint = conv.convertHeightInCharsToPixels(5);
		previewer.setLayoutData(gd);
		
	}
	
	private Control createPreviewer(Composite parent) {
		
		IPreferenceStore additionalStore = new PreferenceStore();
		additionalStore.setValue(StatetPreferenceConstants.TASK_TAGS, "TODO"); //$NON-NLS-1$
		IPreferenceStore store = new ChainedPreferenceStore(new IPreferenceStore[] { 
				fOverlayStore, additionalStore, EditorsUI.getPreferenceStore() });
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
	
	
	@Override
	protected void updateControls() {
		super.updateControls();

		fGroup.handleListSelection();
	}

}