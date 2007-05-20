/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.preferences;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;
import de.walware.eclipsecommons.ui.dialogs.StatusInfo;

import de.walware.statet.ext.ui.editors.SourceViewerConfigurator;


/**
 * Dialog to edit a template.
 */
public class EditTemplateDialog extends StatusDialog {

	private static class TextViewerAction extends Action implements IUpdate {
	
		private int fOperationCode = -1;
		private ITextOperationTarget fOperationTarget;
	
		/** 
		 * Creates a new action.
		 * 
		 * @param viewer the viewer
		 * @param operationCode the opcode
		 */
		public TextViewerAction(ITextViewer viewer, int operationCode) {
			fOperationCode = operationCode;
			fOperationTarget= viewer.getTextOperationTarget();
			update();
		}
	
		/**
		 * Updates the enabled state of the action.
		 * Fires a property change if the enabled state changes.
		 * 
		 * @see Action#firePropertyChange(String, Object, Object)
		 */
		public void update() {
	
			boolean wasEnabled= isEnabled();
			boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
			setEnabled(isEnabled);
	
			if (wasEnabled != isEnabled) {
				firePropertyChange(ENABLED, wasEnabled ? Boolean.TRUE : Boolean.FALSE, isEnabled ? Boolean.TRUE : Boolean.FALSE);
			}
		}
		
		/**
		 * @see Action#run()
		 */
		public void run() {
			if (fOperationCode != -1 && fOperationTarget != null) {
				fOperationTarget.doOperation(fOperationCode);
			}
		}
	}	


	private final Template fOriginalTemplate;
	private Template fNewTemplate;
	
	private SourceViewerConfigurator fConfigurator;
	
	private Text fNameText;
	private Text fDescriptionText;
	private Combo fContextCombo;
	private SourceViewer fPatternEditor;	
	private Button fInsertVariableButton;
	private Button fAutoInsertCheckbox;
	private boolean fIsNameModifiable;

	private StatusInfo fValidationStatus;
	private boolean fSuppressError= true; // #4354	
	private Map<String, Action> fGlobalActions= new HashMap<String, Action>(10);
	private List<String> fSelectionActions = new ArrayList<String>(3);
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
	public EditTemplateDialog(Shell parent, Template template, boolean edit, boolean isNameModifiable, 
			SourceViewerConfigurator configuration,
			TemplateVariableProcessor processor,
			ContextTypeRegistry registry) {
		
		super(parent);
		
		fConfigurator = configuration;

		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
		String title = edit ? 
				Messages.EditTemplateDialog_title_Edit: Messages.EditTemplateDialog_title_New; 
		setTitle(title);

		fOriginalTemplate = template;
		fIsNameModifiable = isNameModifiable;

		List<String[]> contexts = new ArrayList<String[]>();
		for (Iterator it = registry.contextTypes(); it.hasNext();) {
			TemplateContextType type= (TemplateContextType) it.next();
			contexts.add(new String[] { type.getId(), type.getName() });
		}
		fContextTypes = contexts.toArray(new String[contexts.size()][]);
				
		fValidationStatus = new StatusInfo();
		
		fTemplateProcessor = processor;
		fContextTypeRegistry = registry;
		
		TemplateContextType type = fContextTypeRegistry.getContextType(template.getContextTypeId());
		fTemplateProcessor.setContextType(type);
		
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

	public void create() {
		super.create();
		// update initial OK button to be disabled for new templates
		boolean valid= fNameText == null || fNameText.getText().trim().length() != 0;
		if (!valid) {
			StatusInfo status = new StatusInfo();
			status.setError(Messages.EditTemplateDialog_error_NoName);
			updateButtonsEnableState(status);
 		}
	}
	
	protected Control createDialogArea(Composite ancestor) {
		Composite parent= new Composite(ancestor, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		ModifyListener listener= new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doTextWidgetChanged(e.widget);
			}
		};
		
		if (fIsNameModifiable) {
			createLabel(parent, Messages.EditTemplateDialog_Name_label);

			Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			layout= new GridLayout();
			layout.numColumns= 4;
			layout.marginWidth= 0;
			layout.marginHeight= 0;
			composite.setLayout(layout);

			fNameText= createText(composite);
			fNameText.addModifyListener(listener);
			fNameText.addFocusListener(new FocusListener() {

				public void focusGained(FocusEvent e) {
				}

				public void focusLost(FocusEvent e) {
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

		createLabel(parent, Messages.EditTemplateDialog_Description_label);

		int descFlags= fIsNameModifiable ? SWT.BORDER : SWT.BORDER | SWT.READ_ONLY;
		fDescriptionText= new Text(parent, descFlags );
		fDescriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fDescriptionText.addModifyListener(listener);

		Label patternLabel= createLabel(parent, Messages.EditTemplateDialog_Pattern_label);
		patternLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		fPatternEditor= createEditor(parent, fOriginalTemplate.getPattern());

		Label filler= new Label(parent, SWT.NONE);
		filler.setLayoutData(new GridData());

		Composite composite= new Composite(parent, SWT.NONE);
		layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData());

		fInsertVariableButton= new Button(composite, SWT.NONE);
		fInsertVariableButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fInsertVariableButton.setText(Messages.EditTemplateDialog_InsertVariable);
		fInsertVariableButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fPatternEditor.getTextWidget().setFocus();
				fPatternEditor.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		fDescriptionText.setText(fOriginalTemplate.getDescription());
		if (fIsNameModifiable) {
			fNameText.setText(fOriginalTemplate.getName());
			fNameText.addModifyListener(listener);
			fContextCombo.select(getIndex(fOriginalTemplate.getContextTypeId()));
		} else {
			fPatternEditor.getControl().setFocus();
		}
		initializeActions();

		applyDialogFont(parent);
		return composite;
	}

/* GUI Methods ****************************************************************/	
	
	private static Label createLabel(Composite parent, String name) {
		Label label= new Label(parent, SWT.NULL);
		label.setText(name);
		label.setLayoutData(new GridData());

		return label;
	}

	private static Text createText(Composite parent) {
		Text text= new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return text;
	}

	private static Button createCheckbox(Composite parent, String name) {
		Button button= new Button(parent, SWT.CHECK);
		button.setText(name);
		button.setLayoutData(new GridData());
		
		return button;
	}
	
	private SourceViewer createEditor(Composite parent, String pattern) {
		SourceViewer viewer= createViewer(parent);

		IDocument document= new Document(pattern);
		fConfigurator.getDocumentSetupParticipant().setup(document);
		viewer.setEditable(true);
		viewer.setDocument(document);


		int nLines= document.getNumberOfLines();
		if (nLines < 6) {
			nLines= 6;
		} else if (nLines > 12) {
			nLines= 12;
		}

		Control control= viewer.getControl();
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint= convertWidthInCharsToPixels(80);
		data.heightHint= convertHeightInCharsToPixels(nLines);
		control.setLayoutData(data);

		viewer.addTextListener(new ITextListener() {
			public void textChanged(TextEvent event) {
				if (event .getDocumentEvent() != null)
					doSourceChanged(event.getDocumentEvent().getDocument());
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelectionDependentActions();
			}
		});

	 	viewer.prependVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				handleVerifyKeyPressed(event);
			}
		});

		return viewer;
	}

	/**
	 * Creates the viewer to be used to display the pattern. Subclasses may override.
	 *
	 * @param parent the parent composite of the viewer
	 * @return a configured <code>SourceViewer</code>
	 */
	protected SourceViewer createViewer(Composite parent) {
		
		SourceViewer viewer= new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		// CHANGED
		viewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		fConfigurator.setTarget(viewer, true);
		
		return viewer;
	}

	
/* Handlers *******************************************************************/
	
	private void doTextWidgetChanged(Widget w) {
		if (w == fNameText) {
			fSuppressError= false;
			updateButtons();
		} else if (w == fContextCombo) {
			String contextId= getContextId();
			fTemplateProcessor.setContextType(fContextTypeRegistry.getContextType(contextId));
		} else if (w == fDescriptionText) {
			// oh, nothing
		}
	}

	private void doSourceChanged(IDocument document) {
		
		String text = document.get();
		fValidationStatus.setOK();
		TemplateContextType contextType = fContextTypeRegistry.getContextType(getContextId());
		if (contextType != null) {
			try {
				contextType.validate(text);
			} catch (TemplateException e) {
				fValidationStatus.setError(e.getLocalizedMessage());
			}
		}

		updateUndoAction();
		updateButtons();
	}

	/*
	 * @since 3.1
	 */
	protected void okPressed() {
		String name= fNameText == null ? fOriginalTemplate.getName() : fNameText.getText();
		boolean isAutoInsertable= fAutoInsertCheckbox != null && fAutoInsertCheckbox.getSelection();
		fNewTemplate= new Template(name, fDescriptionText.getText(), getContextId(), fPatternEditor.getDocument().get(), isAutoInsertable);
		super.okPressed();
	}

	private void handleVerifyKeyPressed(VerifyEvent event) {
		if (!event.doit)
			return;

		if (event.stateMask != SWT.MOD1)
			return;

		switch (event.character) {
			case ' ':
				fPatternEditor.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				event.doit= false;
				break;

			// CTRL-Z
			case 'z' - 'a' + 1:
				fPatternEditor.doOperation(ITextOperationTarget.UNDO);
				event.doit= false;
				break;
		}
	}

	private void updateButtons() {
		StatusInfo status;

		boolean valid= fNameText == null || fNameText.getText().trim().length() != 0;
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
	
	
/* Menu / Actions *************************************************************/
	
	private void initializeActions() {
		TextViewerAction action= new TextViewerAction(fPatternEditor, ITextOperationTarget.UNDO);
		action.setText(Messages.EditTemplateDialog_Undo);
		fGlobalActions.put(ITextEditorActionConstants.UNDO, action);

		action= new TextViewerAction(fPatternEditor, ITextOperationTarget.CUT);
		action.setText(Messages.EditTemplateDialog_Cut);
		fGlobalActions.put(ITextEditorActionConstants.CUT, action);

		action= new TextViewerAction(fPatternEditor, ITextOperationTarget.COPY);
		action.setText(Messages.EditTemplateDialog_Copy);
		fGlobalActions.put(ITextEditorActionConstants.COPY, action);

		action= new TextViewerAction(fPatternEditor, ITextOperationTarget.PASTE);
		action.setText(Messages.EditTemplateDialog_Paste);
		fGlobalActions.put(ITextEditorActionConstants.PASTE, action);

		action= new TextViewerAction(fPatternEditor, ITextOperationTarget.SELECT_ALL);
		action.setText(Messages.EditTemplateDialog_SelectAll);
		fGlobalActions.put(ITextEditorActionConstants.SELECT_ALL, action);

		action= new TextViewerAction(fPatternEditor, ISourceViewer.CONTENTASSIST_PROPOSALS);
		action.setText(Messages.EditTemplateDialog_ContentAssist);
		fGlobalActions.put("ContentAssistProposal", action); //$NON-NLS-1$

		fSelectionActions.add(ITextEditorActionConstants.CUT);
		fSelectionActions.add(ITextEditorActionConstants.COPY);
		fSelectionActions.add(ITextEditorActionConstants.PASTE);

		// create context menu
		MenuManager manager= new MenuManager(null, null);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});

		StyledText text= fPatternEditor.getTextWidget();
		Menu menu= manager.createContextMenu(text);
		text.setMenu(menu);
	}

	private void fillContextMenu(IMenuManager menu) {
		menu.add(new GroupMarker(ITextEditorActionConstants.GROUP_UNDO));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_UNDO, (IAction) fGlobalActions.get(ITextEditorActionConstants.UNDO));

		menu.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) fGlobalActions.get(ITextEditorActionConstants.CUT));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) fGlobalActions.get(ITextEditorActionConstants.COPY));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) fGlobalActions.get(ITextEditorActionConstants.PASTE));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) fGlobalActions.get(ITextEditorActionConstants.SELECT_ALL));

		menu.add(new Separator("templates")); //$NON-NLS-1$
		menu.appendToGroup("templates", (IAction) fGlobalActions.get("ContentAssistProposal")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void updateAction(String actionId) {
		IAction action= (IAction) fGlobalActions.get(actionId);
		if (action instanceof IUpdate)
			((IUpdate) action).update();
	}

	private void updateSelectionDependentActions() {
		Iterator iterator= fSelectionActions.iterator();
		while (iterator.hasNext())
			updateAction((String)iterator.next());
	}

	private void updateUndoAction() {
		IAction action= (IAction) fGlobalActions.get(ITextEditorActionConstants.UNDO);
		if (action instanceof IUpdate)
			((IUpdate) action).update();
	}
	
/* ******/
	
	private String getContextId() {
		if (fContextCombo != null && !fContextCombo.isDisposed()) {
			String name= fContextCombo.getText();
			for (int i= 0; i < fContextTypes.length; i++) {
				if (name.equals(fContextTypes[i][1])) {
					return fContextTypes[i][0];
				}
			}
		}

		return fOriginalTemplate.getContextTypeId();
	}
	
	private int getIndex(String contextid) {

		if (contextid == null)
			return -1;

		for (int i= 0; i < fContextTypes.length; i++) {
			if (contextid.equals(fContextTypes[i][0])) {
				return i;
			}
		}
		return -1;
	}
	
	
}
