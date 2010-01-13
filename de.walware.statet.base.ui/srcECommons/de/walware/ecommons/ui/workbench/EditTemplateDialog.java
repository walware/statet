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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.walware.ecommons.internal.ui.Messages;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.dialogs.StatusInfo;
import de.walware.ecommons.ui.text.sourceediting.SnippetEditor;
import de.walware.ecommons.ui.text.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ui.text.sourceediting.TextViewerAction;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Dialog to edit a template.
 */
public class EditTemplateDialog extends ExtStatusDialog {
	
	private final Template fOriginalTemplate;
	private Template fNewTemplate;
	
	private SourceEditorViewerConfigurator fConfigurator;
	
	private Text fNameText;
	private Text fDescriptionText;
	private ComboViewer fContextCombo;
	private SnippetEditor fPatternEditor;
	private Button fInsertVariableButton;
	private Button fAutoInsertCheckbox;
	private boolean fIsNameModifiable;
	
	private StatusInfo fValidationStatus;
	private boolean fSuppressError = true;
	
	private ContextTypeRegistry fContextTypeRegistry;
	private final TemplateVariableProcessor fTemplateProcessor;
	
	
	/**
	 * Creates a new dialog.
	 * 
	 * @param parent the shell parent of the dialog
	 * @param template the template to edit
	 * @param edit whether this is a new template or an existing being edited
	 * @param isNameModifiable whether the name of the template may be modified
	 * @param registry the context type registry to use
	 */
	public EditTemplateDialog(final Shell parent, final Template template, final boolean edit, final boolean isNameModifiable,
			final SourceEditorViewerConfigurator configuration,
			final TemplateVariableProcessor processor,
			final ContextTypeRegistry registry) {
		super(parent);
		
		setTitle(edit ?
				Messages.EditTemplateDialog_title_Edit :
				Messages.EditTemplateDialog_title_New );
		
		fOriginalTemplate = template;
		fIsNameModifiable = isNameModifiable;
		
		fValidationStatus = new StatusInfo();
		
		fTemplateProcessor = processor;
		fContextTypeRegistry = registry;
		
		final TemplateContextType type = fContextTypeRegistry.getContextType(template.getContextTypeId());
		fTemplateProcessor.setContextType(type);
		fConfigurator = configuration;
		fPatternEditor = new SnippetEditor(fConfigurator, template.getPattern(), PlatformUI.getWorkbench());
	}
	
	
	/**
	 * Returns the created template.
	 * 
	 * @return the created template
	 * @since 3.1
	 */
	public Template getTemplate() {
		return fNewTemplate;
	}
	
	@Override
	public void create() {
		super.create();
		// update initial OK button to be disabled for new templates
		final boolean valid= fNameText == null || fNameText.getText().trim().length() != 0;
		if (!valid) {
			final StatusInfo status = new StatusInfo();
			status.setError(Messages.EditTemplateDialog_error_NoName);
			updateButtonsEnableState(status);
		}
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite dialogArea = new Composite(parent, SWT.NONE);
		dialogArea.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
		dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final ModifyListener listener= new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				fSuppressError = false;
				updateButtons();
			}
		};
		
		if (fIsNameModifiable) {
			createLabel(dialogArea, Messages.EditTemplateDialog_Name_label);
			
			final Composite composite = new Composite(dialogArea, SWT.NONE);
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 4));
			
			fNameText = createText(composite);
			fNameText.addModifyListener(listener);
			fNameText.addFocusListener(new FocusListener() {
				
				public void focusGained(final FocusEvent e) {
				}
				
				public void focusLost(final FocusEvent e) {
					if (fSuppressError) {
						fSuppressError = false;
						updateButtons();
					}
				}
			});
			
			createLabel(composite, Messages.EditTemplateDialog_Context_label);
			fContextCombo = new ComboViewer(composite, SWT.BORDER | SWT.READ_ONLY);
			fContextCombo.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(final Object element) {
					return ((TemplateContextType) element).getName();
				}
			});
			fContextCombo.setContentProvider(new ArrayContentProvider());
			final List<TemplateContextType> contextTypes = new ArrayList<TemplateContextType>();
			final Iterator iter = fContextTypeRegistry.contextTypes();
			while (iter.hasNext()) {
				final TemplateContextType contextType = (TemplateContextType) iter.next();
				contextTypes.add(contextType);
			}
			fContextCombo.setInput(contextTypes.toArray());
			
			fContextCombo.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(final SelectionChangedEvent event) {
					final StructuredSelection selection = (StructuredSelection) event.getSelection();
					doContextChanged(((TemplateContextType) selection.getFirstElement()));
				}
			});
			
			fAutoInsertCheckbox= createCheckbox(composite, Messages.EditTemplateDialog_AutoInsert_label);
			fAutoInsertCheckbox.setSelection(fOriginalTemplate.isAutoInsertable());
		}
		else {
			configureForContext(getContextType());
		}
		
		createLabel(dialogArea, Messages.EditTemplateDialog_Description_label);
		
		final int descFlags= fIsNameModifiable ? SWT.BORDER : SWT.BORDER | SWT.READ_ONLY;
		fDescriptionText= new Text(dialogArea, descFlags );
		fDescriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fDescriptionText.addModifyListener(listener);
		
		final Label patternLabel= createLabel(dialogArea, Messages.EditTemplateDialog_Pattern_label);
		patternLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		createEditor(dialogArea);
		
		final Label filler= new Label(dialogArea, SWT.NONE);
		filler.setLayoutData(new GridData());
		
		final Composite composite= new Composite(dialogArea, SWT.NONE);
		composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
		composite.setLayoutData(new GridData());
		
		fInsertVariableButton= new Button(composite, SWT.NONE);
		fInsertVariableButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fInsertVariableButton.setText(Messages.EditTemplateDialog_InsertVariable);
		fInsertVariableButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(final SelectionEvent e) {
				fPatternEditor.getSourceViewer().getTextWidget().setFocus();
				fPatternEditor.getSourceViewer().doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
			}
			
			public void widgetDefaultSelected(final SelectionEvent e) {}
		});
		
		fDescriptionText.setText(fOriginalTemplate.getDescription());
		if (fIsNameModifiable) {
			fNameText.setText(fOriginalTemplate.getName());
			fNameText.addModifyListener(listener);
			fContextCombo.setSelection(new StructuredSelection(fContextTypeRegistry.getContextType(fOriginalTemplate.getContextTypeId())));
		} else {
			fPatternEditor.getControl().setFocus();
		}
		
		final TextViewerAction assistAction = new TextViewerAction(fPatternEditor.getSourceViewer(), ISourceViewer.CONTENTASSIST_PROPOSALS);
		assistAction.setId("ContentAssistProposal"); //$NON-NLS-1$
		assistAction.setText(Messages.EditTemplateDialog_ContentAssist);
		assistAction.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		fPatternEditor.addAction(assistAction); 
		
		LayoutUtil.addSmallFiller(dialogArea, false);
		applyDialogFont(dialogArea);
		return composite;
	}
	
	protected SourceViewer getSourceViewer() {
		return fPatternEditor.getSourceViewer();
	}
	
	protected SourceEditorViewerConfigurator getSourceViewerConfigurator() {
		return fConfigurator;
	}
	
	
/* GUI Methods ****************************************************************/
	
	private static Label createLabel(final Composite parent, final String name) {
		final Label label= new Label(parent, SWT.NULL);
		label.setText(name);
		label.setLayoutData(new GridData());
		
		return label;
	}
	
	private static Text createText(final Composite parent) {
		final Text text= new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return text;
	}
	
	private static Button createCheckbox(final Composite parent, final String name) {
		final Button button= new Button(parent, SWT.CHECK);
		button.setText(name);
		button.setLayoutData(new GridData());
		
		return button;
	}
	
	private void createEditor(final Composite parent) {
		int nLines = fPatternEditor.getDocument().getNumberOfLines();
		if (nLines < 6) {
			nLines= 6;
		} else if (nLines > 12) {
			nLines= 12;
		}
		
		fPatternEditor.create(parent, SnippetEditor.DEFAULT_MULTI_LINE_STYLE);
		final Control control= fPatternEditor.getControl();
		final GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint= convertWidthInCharsToPixels(80);
		data.heightHint= convertHeightInCharsToPixels(nLines);
		control.setLayoutData(data);
		
		fPatternEditor.getSourceViewer().addTextListener(new ITextListener() {
			public void textChanged(final TextEvent event) {
				if (event.getDocumentEvent() != null)
					doSourceChanged(event.getDocumentEvent().getDocument());
			}
		});
	}
	
	
/* Handlers *******************************************************************/
	
	
	protected void doContextChanged(final TemplateContextType contextType) {
		fTemplateProcessor.setContextType(contextType);
		configureForContext(contextType);
		final Document document = fPatternEditor.getDocument();
		doValidate(contextType, document);
		updateButtons();
	}
	
	private void doSourceChanged(final IDocument document) {
		final TemplateContextType contextType = getContextType();
		doValidate(contextType, document);
		updateButtons();
	}
	
	private void doValidate(final TemplateContextType contextType, final IDocument document) {
		final String text = document.get();
		fValidationStatus.setOK();
		if (contextType != null) {
			try {
				contextType.validate(text);
			}
			catch (final TemplateException e) {
				fValidationStatus.setError(e.getLocalizedMessage());
			}
		}
	}
	
	protected void configureForContext(final TemplateContextType contextType) {
	}
	
	@Override
	protected void okPressed() {
		final String name= fNameText == null ? fOriginalTemplate.getName() : fNameText.getText();
		final boolean isAutoInsertable= fAutoInsertCheckbox != null && fAutoInsertCheckbox.getSelection();
		fNewTemplate= new Template(name, fDescriptionText.getText(), getContextType().getId(), fPatternEditor.getDocument().get(), isAutoInsertable);
		super.okPressed();
	}
	
	private void updateButtons() {
		StatusInfo status;
		
		final boolean valid= fNameText == null || fNameText.getText().trim().length() != 0;
		if (!valid) {
			status = new StatusInfo();
			if (!fSuppressError) {
				status.setError(Messages.EditTemplateDialog_error_NoName);
			}
		} else {
			status= fValidationStatus;
		}
		updateStatus(status);
	}
	
	
/* ******/
	
	protected TemplateContextType getContextType() {
		if (fContextCombo != null) {
			final StructuredSelection selection = (StructuredSelection) fContextCombo.getSelection();
			return ((TemplateContextType) selection.getFirstElement());
		}
		else {
			return fContextTypeRegistry.getContextType(fOriginalTemplate.getContextTypeId());
		}
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return DialogUtil.getDialogSettings(StatetUIPlugin.getDefault(), "TemplateEditDialog"); //$NON-NLS-1$
	}
	
}
