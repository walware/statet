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

package de.walware.statet.internal.ui.preferences;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;
import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.dialogs.groups.CategorizedItem;
import de.walware.eclipsecommons.ui.dialogs.groups.CategorizedOptionButtonsGroup;
import de.walware.eclipsecommons.ui.preferences.AbstractConfigurationBlock;
import de.walware.eclipsecommons.ui.util.PixelConverter;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.base.core.StatetProject;
import de.walware.statet.ext.ui.editors.SourceViewerUpdater;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.preferences.EditTemplateDialog;
import de.walware.statet.ext.ui.preferences.ICodeGenerationTemplatesCategory;
import de.walware.statet.ext.ui.preferences.TemplateViewerConfigurationProvider;


/**
 * The page to configure the codegenerator templates.
 */
public class CodeGenerationTemplatesConfigurationBlock extends AbstractConfigurationBlock {


	public static final String EXTENSION_POINT = "codeGenerationTemplatesCategory";
	public static final String ATT_ID = "id";
	public static final String ATT_NAME = "name";
	public static final String ATT_CLASS = "providerClass";

	private final static int IDX_EDIT = 0;
	private final static int IDX_IMPORT = 2;
	private final static int IDX_EXPORT = 3;
	private final static int IDX_EXPORTALL = 4;

	
	public class TemplateItem extends CategorizedItem {

		protected TemplatePersistenceData fData;
		
		public TemplateItem(TemplatePersistenceData data) {

			super(data.getTemplate().getDescription());
			fData = data;
		}
	}
	
	private class Group extends CategorizedOptionButtonsGroup<TemplateItem> {
		
		public Group() {
			super(new String[] {
					Messages.CodeTemplates_EditButton_label,
					null,
					Messages.CodeTemplates_ImportButton_label,
					Messages.CodeTemplates_ExportButton_label,
					Messages.CodeTemplates_ExportAllButton_label,
			});
		}
		

		@Override
		protected void handleDoubleClick(TemplateItem item) {
			
			if (item instanceof TemplateItem) {
				doEdit((TemplateItem) item);
			}
		}
		
		@Override
		public void handleListSelection() {
			
			IStructuredSelection selection = getSelectedItems();
			TemplateItem single = getSingleSelectedItem();
			
			fButtonGroup.enableButton(IDX_EDIT, (single != null));
			fButtonGroup.enableButton(IDX_EXPORT, selection.size() > 0);
			
			updateSourceViewerInput(single);
		}
		
		@Override
		public void handleButtonPressed(int buttonIdx) {

			switch (buttonIdx) {
			case IDX_EDIT:
				TemplateItem item = getSingleSelectedItem();
				if (item != null)
					doEdit(item);
				break;

			case IDX_EXPORT:
				IStructuredSelection selection = getSelectedItems();
				List<TemplatePersistenceData> datas = new ArrayList<TemplatePersistenceData>();
				for (Iterator iter = selection.iterator(); iter.hasNext();) {
					Object curr = iter.next();
					if (curr instanceof TemplateItem) {
						datas.add( ((TemplateItem) curr).fData );
					} else {
						int catIndex = getIndexOfCategory(curr);
						for (int j = 0; j < fCategoryChilds[catIndex].length; j++) {
							datas.add(fCategoryChilds[catIndex][j].fData );
						}
					}
				}
				doExport(datas.toArray(new TemplatePersistenceData[datas.size()]));
				break;

			case IDX_EXPORTALL:
				doExport(fTemplatesStore.getAllTemplateData());
				break;

			case IDX_IMPORT:
				doImport();
				break;
			}		
		}
		
	}
	

	protected IProject fProject;
	protected StatetProject fStatetProject;
	private String[] fCategoryIds;
	private ICodeGenerationTemplatesCategory[] fCategoryProvider;
	
	protected CodeGenerationTemplatesStore fTemplatesStore;
	
	protected Group fGroup;
	private SourceViewer fPatternViewer;
	private int fPatternViewerConfiguredCategory = -1;
	private SourceViewerUpdater fPatternViewerUpdater = null;
	
	private TemplateVariableProcessor fTemplateProcessor;
	
	
	public CodeGenerationTemplatesConfigurationBlock(IProject project) throws CoreException {
		
		fProject = project;
		if (fProject != null) {
			fStatetProject = (StatetProject) fProject.getNature(StatetProject.ID);
		}

		fGroup = new Group();
		loadRegisteredTemplates();
		fGroup.generateListModel();

		fTemplateProcessor = new TemplateVariableProcessor();
	}
	
	private void loadRegisteredTemplates() {
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(StatetPlugin.ID, EXTENSION_POINT);
		
		fCategoryIds = new String[elements.length];
		fCategoryProvider = new ICodeGenerationTemplatesCategory[elements.length];
		fGroup.fCategorys = new String[elements.length];
		fGroup.fCategoryChilds = new TemplateItem[elements.length][];
		TemplateStore[] templates = new TemplateStore[elements.length];

		for (int i = 0; i < elements.length; i++) {
			fCategoryIds[i] = elements[i].getAttribute(ATT_ID);
			Assert.isLegal(fCategoryIds[i] != null);
			
			fGroup.fCategorys[i] = elements[i].getAttribute(ATT_NAME);
			if (fGroup.fCategorys[i] == null) 
				fGroup.fCategorys[i] = fCategoryIds[i];
			
			try {
				fCategoryProvider[i] = (ICodeGenerationTemplatesCategory) elements[i].createExecutableExtension(ATT_CLASS);
			} catch (CoreException e) {
				throw new IllegalArgumentException("error loading templateprovider of category with id "+fCategoryIds[i]);
			}
			
			templates[i] = fCategoryProvider[i].getTemplateStore();
		}
		
		fTemplatesStore = new CodeGenerationTemplatesStore(fProject, templates);
		try {
			fTemplatesStore.load();
		} catch (IOException e) {
			StatetPlugin.logUnexpectedError(e);
		}
		for (int i = 0; i < fCategoryIds.length; i++) {
			TemplatePersistenceData[] datas = fTemplatesStore.getTemplateData(i);
			
			fGroup.fCategoryChilds[i] = new TemplateItem[datas.length];
			for (int j = 0; j < fGroup.fCategoryChilds[i].length; j++) {
				fGroup.fCategoryChilds[i][j] = new TemplateItem(datas[j]);
			}
		}
	}
	
	private void reloadTemplateData() {
		
		for (int i = 0; i < fCategoryIds.length; i++) {
			for (int j = 0; j < fGroup.fCategoryChilds[i].length; j++) {
				fGroup.fCategoryChilds[i][j].fData = fTemplatesStore.getTemplateData(
						i, fGroup.fCategoryChilds[i][j].fData.getId());
			}
		}
	}
	
	private void updateTemplate(TemplatePersistenceData data) {
		
		TemplatePersistenceData[] datas = fTemplatesStore.getAllTemplateData();
		for (int i = 0; i < datas.length; i++) {
			String id = datas[i].getId();
			if (id != null && id.equals(data.getId())) {
				datas[i].setTemplate(data.getTemplate());
				break;
			}
		}
	}
	
	@Override
	public void setUseProjectSpecificSettings(boolean enable) {

		super.setUseProjectSpecificSettings(enable);
		
		if (enable) {
			fGroup.fComposite.getDisplay().asyncExec(new Runnable() {
				public void run() {
					fGroup.handleListSelection();
				}
			});
		}
	}
	
/* GUI ************************************************************************/
	
	@Override
	public void createContents(Layouter layouter, IWorkbenchPreferenceContainer container, IPreferenceStore preferenceStore) {

		super.createContents(layouter, container, preferenceStore);

		layouter.addLabel(Messages.CodeTemplates_label);
		layouter.addGroup(fGroup);
		
		fGroup.initFields();
		
		fPatternViewer = createViewer(layouter);
	}

	private SourceViewer createViewer(Layouter layouter) {
		
		layouter.addLabel(Messages.CodeTemplates_Preview_label);
		
		SourceViewer viewer = new SourceViewer(layouter.fComposite, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setEditable(false);
		viewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

		IDocument document = new Document();
		viewer.setDocument(document);
		
		Control control = viewer.getControl();
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = layouter.fNumColumns;
		data.heightHint = new PixelConverter(control).convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
		
		return viewer;
	}
	
	protected void updateSourceViewerInput(TemplateItem item) {
		
		if (fPatternViewer == null || !(Layouter.isOkToUse(fPatternViewer.getControl())) )
			return;

		if (item != null) {
			TemplatePersistenceData data = item.fData;
			ICodeGenerationTemplatesCategory category = fCategoryProvider[item.fCategoryIndex];
			Template template = data.getTemplate();
			
			TemplateContextType type = category.getContextTypeRegistry().getContextType(template.getContextTypeId());
			fTemplateProcessor.setContextType(type);
			TemplateViewerConfigurationProvider prov = category.getEditTemplateDialogConfiguation(fTemplateProcessor, fStatetProject);

			if (item.fCategoryIndex != fPatternViewerConfiguredCategory) {
				fPatternViewerConfiguredCategory = item.fCategoryIndex;
				
				if (fPatternViewerUpdater != null) {
					fPatternViewerUpdater.unregister();
					fPatternViewerUpdater = null;
				}
				fPatternViewer.unconfigure();
				
				StatextSourceViewerConfiguration configuration = prov.getSourceViewerConfiguration();
				fPatternViewer.configure(configuration);
				fPatternViewerUpdater = new SourceViewerUpdater(fPatternViewer, configuration, prov.getPreferenceStore());
				
				IDocument document = new Document(template.getPattern());
				prov.getDocumentSetupParticipant().setup(document);
				fPatternViewer.setDocument(document);
			} 
			else {
				fPatternViewer.getDocument().set(template.getPattern());
			}
			
		} else {
			fPatternViewer.getDocument().set(""); //$NON-NLS-1$
		}		
	}


/* Execute Actions ************************************************************/

	public void doEdit(TemplateItem item) {

		EditTemplateDialog dialog = new EditTemplateDialog(
				getShell(), item.fData.getTemplate(), true, false, 
				fCategoryProvider[item.fCategoryIndex].getEditTemplateDialogConfiguation(fTemplateProcessor, fStatetProject),
				fTemplateProcessor,
				fCategoryProvider[item.fCategoryIndex].getContextTypeRegistry());
		if (dialog.open() == Window.OK) {
			// changed
			item.fData.setTemplate(dialog.getTemplate());
			fGroup.fSelectionViewer.refresh(item);
			fGroup.fSelectionViewer.setSelection(new StructuredSelection(item));
		}
	}
	
	public void doImport() {
		
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		dialog.setText(Messages.CodeTemplates_Import_title); 
		dialog.setFilterExtensions(new String[] { Messages.CodeTemplates_Import_extension }); 
		String path = dialog.open();
		
		if (path == null)
			return;
		
		try {
			TemplateReaderWriter reader = new TemplateReaderWriter();
			File file = new File(path);
			if (file.exists()) {
				InputStream input = new BufferedInputStream(new FileInputStream(file));
				try {
					TemplatePersistenceData[] datas = reader.read(input, null);
					for (int i= 0; i < datas.length; i++) {
						updateTemplate(datas[i]);
					}
				} finally {
					try {
						input.close();
					} catch (IOException x) {
					}
				}
			}

			fGroup.fSelectionViewer.refresh();
			updateSourceViewerInput(fGroup.getSingleSelectedItem());

		} catch (FileNotFoundException e) {
			openReadErrorDialog(e);
		} catch (IOException e) {
			openReadErrorDialog(e);
		}
		
	}

	public void doExport(TemplatePersistenceData[] templates) {

		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(NLS.bind(Messages.CodeTemplates_Export_title, String.valueOf(templates.length))); 
		dialog.setFilterExtensions(new String[] { Messages.CodeTemplates_Export_extension }); 
		dialog.setFileName(Messages.CodeTemplates_Export_filename); 
		String path = dialog.open();

		if (path == null)
			return;
		
		File file = new File(path);		

		if (file.isHidden()) {
			MessageDialog.openError(getShell(), 
					Messages.CodeTemplates_Export_Error_title, 
					NLS.bind(Messages.CodeTemplates_Export_Error_Hidden_message, file.getAbsolutePath()) );
			return;
		}
		
		if (file.exists() && !file.canWrite()) {
			MessageDialog.openError(getShell(), 
					Messages.CodeTemplates_Export_Error_title,
					NLS.bind(Messages.CodeTemplates_Export_Error_CanNotWrite_message, file.getAbsolutePath()) );
			return;
		}

		if (!file.exists() || confirmOverwrite(file)) {
			OutputStream output= null;
			try {
				output= new BufferedOutputStream(new FileOutputStream(file));
				TemplateReaderWriter writer= new TemplateReaderWriter();
				writer.save(templates, output);
				output.close();
			} catch (IOException e) {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e2) {
						// ignore 
					}
				}
				openWriteErrorDialog(e);
			}
		}
		
	}

	private boolean confirmOverwrite(File file) {
		return MessageDialog.openQuestion(getShell(),
			Messages.CodeTemplates_Export_Exists_title, 
			NLS.bind(Messages.CodeTemplates_Export_Exists_message, file.getAbsolutePath()) ); 
	}
	
	
	
/* IConfigurationBlock ********************************************************/	
	
	public void performDefaults() {
		fTemplatesStore.restoreDefaults();
		reloadTemplateData();
		
		// refresh
		fGroup.fSelectionViewer.refresh();
		updateSourceViewerInput(fGroup.getSingleSelectedItem());
	}
	
	public boolean performOk() {
		if (fProject != null) {
			TemplatePersistenceData[] templateData = fTemplatesStore.getAllTemplateData();
			for (int i = 0; i < templateData.length; i++) {
				fTemplatesStore.setProjectSpecific(templateData[i].getId(), fUseProjectSettings);
			}
		}
		try {
			fTemplatesStore.save();
		} catch (IOException e) {
			StatetPlugin.logUnexpectedError(e);
			openWriteErrorDialog(e);
		}
		return true;
	}
	
	public void performCancel() {
		try {
			fTemplatesStore.revertChanges();
		} catch (IOException e) {
			openReadErrorDialog(e);
		}
	}
	

/* Error Dialogs **************************************************************/
	
	private void openReadErrorDialog(Exception e) {
		
		String title = Messages.CodeTemplates_error_title;
		String message = e.getLocalizedMessage();
		if (message != null)
			message = NLS.bind(Messages.CodeTemplates_error_Parse_message, message); //$NON-NLS-1$
		else
			message = Messages.CodeTemplates_error_Read_message;
		MessageDialog.openError(getShell(), title, message);
	}
	
	private void openWriteErrorDialog(Exception e) {
		
		String title = Messages.CodeTemplates_error_title;
		String message = Messages.CodeTemplates_error_Write_message;
		MessageDialog.openError(getShell(), title, message);
	}
	
}
