/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.preferences;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;
import de.walware.eclipsecommons.ui.dialogs.StatusInfo;
import de.walware.eclipsecommons.ui.util.DialogUtil;
import de.walware.eclipsecommons.ui.util.LayoutUtil;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.ui.sourceeditors.SourceViewerConfigurator;
import de.walware.statet.base.ui.sourceeditors.TextViewerAction;
import de.walware.statet.ext.ui.dialogs.SnippetEditor;


/**
 * Dialog to edit a template.
 */
public class EditTemplateDialog extends StatusDialog {
	
	private final Template fOriginalTemplate;
	private Template fNewTemplate;
	
	private SourceViewerConfigurator fConfigurator;
	
	private Text fNameText;
	private Text fDescriptionText;
	private Combo fContextCombo;
	private SnippetEditor fPatternEditor;
	private Button fInsertVariableButton;
	private Button fAutoInsertCheckbox;
	private boolean fIsNameModifiable;
	
	private StatusInfo fValidationStatus;
	private boolean fSuppressError = true;
	private String[][] fContextTypes;
	
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
			final SourceViewerConfigurator configuration,
			final TemplateVariableProcessor processor,
			final ContextTypeRegistry registry) {
		super(parent);
		
		final String title = edit ?
				Messages.EditTemplateDialog_title_Edit: Messages.EditTemplateDialog_title_New;
		setTitle(title);
		
		fOriginalTemplate = template;
		fIsNameModifiable = isNameModifiable;
		
		final List<String[]> contexts = new ArrayList<String[]>();
		for (final Iterator it = registry.contextTypes(); it.hasNext();) {
			final TemplateContextType type= (TemplateContextType) it.next();
			contexts.add(new String[] { type.getId(), type.getName() });
		}
		fContextTypes = contexts.toArray(new String[contexts.size()][]);
				
		fValidationStatus = new StatusInfo();
		
		fTemplateProcessor = processor;
		fContextTypeRegistry = registry;
		
		final TemplateContextType type = fContextTypeRegistry.getContextType(template.getContextTypeId());
		fTemplateProcessor.setContextType(type);
		fConfigurator = configuration;
		fPatternEditor = new SnippetEditor(fConfigurator, template.getPattern(), PlatformUI.getWorkbench());
	}
	
	@Override
	protected boolean isResizable() {
		return true;
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
				doTextWidgetChanged(e.widget);
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
						fSuppressError= false;
						updateButtons();
					}
				}
			});
			
			createLabel(composite, Messages.EditTemplateDialog_Context_label);
			fContextCombo= new Combo(composite, SWT.READ_ONLY);
			
			for (int i= 0; i < fContextTypes.length; i++) {
				fContextCombo.add(fContextTypes[i][1]);
			}
			
			fContextCombo.addModifyListener(listener);
			
			fAutoInsertCheckbox= createCheckbox(composite, Messages.EditTemplateDialog_AutoInsert_label);
			fAutoInsertCheckbox.setSelection(fOriginalTemplate.isAutoInsertable());
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
			fContextCombo.select(getIndex(fOriginalTemplate.getContextTypeId()));
		} else {
			fPatternEditor.getControl().setFocus();
		}
		
		final TextViewerAction assistAction = new TextViewerAction(fPatternEditor.getSourceViewer(), ISourceViewer.CONTENTASSIST_PROPOSALS);
		assistAction.setId("ContentAssistProposal");
		assistAction.setText(Messages.EditTemplateDialog_ContentAssist);
		assistAction.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		fPatternEditor.addAction(assistAction); 
		
		applyDialogFont(dialogArea);
		return composite;
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
	
	private void doTextWidgetChanged(final Widget w) {
		if (w == fNameText) {
			fSuppressError= false;
			updateButtons();
		} else if (w == fContextCombo) {
			final String contextId= getContextId();
			fTemplateProcessor.setContextType(fContextTypeRegistry.getContextType(contextId));
		} else if (w == fDescriptionText) {
			// oh, nothing
		}
	}
	
	private void doSourceChanged(final IDocument document) {
		final String text = document.get();
		fValidationStatus.setOK();
		final TemplateContextType contextType = fContextTypeRegistry.getContextType(getContextId());
		if (contextType != null) {
			try {
				contextType.validate(text);
			} catch (final TemplateException e) {
				fValidationStatus.setError(e.getLocalizedMessage());
			}
		}
		updateButtons();
	}
	
	/*
	 * @since 3.1
	 */
	@Override
	protected void okPressed() {
		final String name= fNameText == null ? fOriginalTemplate.getName() : fNameText.getText();
		final boolean isAutoInsertable= fAutoInsertCheckbox != null && fAutoInsertCheckbox.getSelection();
		fNewTemplate= new Template(name, fDescriptionText.getText(), getContextId(), fPatternEditor.getDocument().get(), isAutoInsertable);
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
	
	private String getContextId() {
		if (fContextCombo != null && !fContextCombo.isDisposed()) {
			final String name= fContextCombo.getText();
			for (int i= 0; i < fContextTypes.length; i++) {
				if (name.equals(fContextTypes[i][1])) {
					return fContextTypes[i][0];
				}
			}
		}
		
		return fOriginalTemplate.getContextTypeId();
	}
	
	private int getIndex(final String contextid) {
		
		if (contextid == null)
			return -1;
		
		for (int i= 0; i < fContextTypes.length; i++) {
			if (contextid.equals(fContextTypes[i][0])) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return DialogUtil.getDialogSettings(StatetUIPlugin.getDefault(), "TemplateEditDialog"); //$NON-NLS-1$
	}
	
}
