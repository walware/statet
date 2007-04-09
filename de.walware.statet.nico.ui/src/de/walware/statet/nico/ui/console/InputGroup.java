/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.console;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.util.PixelConverter;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.editors.GotoMatchingBracketAction;
import de.walware.statet.ext.ui.editors.IEditorAdapter;
import de.walware.statet.ext.ui.editors.IEditorConfiguration;
import de.walware.statet.ext.ui.editors.SourceViewerUpdater;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.text.PairMatcher;
import de.walware.statet.nico.core.runtime.History;
import de.walware.statet.nico.core.runtime.IHistoryListener;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.History.Entry;
import de.walware.statet.nico.ui.internal.Messages;
import de.walware.statet.ui.IStatextEditorActionDefinitionIds;
import de.walware.statet.ui.StatetUiPreferenceConstants;


public class InputGroup {

	
	private class EditorAdapter implements IEditorAdapter {

		private boolean fMessageSetted;
		
		public ISourceViewer getSourceViewer() {
			
			return fSourceViewer;
		}
		
		public void setStatusLineErrorMessage(String message) {
			
			IStatusLineManager manager = fConsolePage.getSite().getActionBars().getStatusLineManager();
			if (manager != null) {
				fMessageSetted = true;
				manager.setErrorMessage(message);
			}
		}
		
		protected void cleanStatusLine() {
			
			if (fMessageSetted) {
				IStatusLineManager manager = fConsolePage.getSite().getActionBars().getStatusLineManager();
				if (manager != null) {
					manager.setErrorMessage(null);
					fMessageSetted = false;
				}
				
			}
		}
	}
	
	private class ThisKeyListener implements KeyListener {

		public void keyPressed(KeyEvent e) {

			if (e.stateMask == SWT.NONE) {
				if (e.keyCode == SWT.ARROW_UP) {
					doHistoryOlder();
				} else if (e.keyCode == SWT.ARROW_DOWN)
					doHistoryNewer();
				else if (e.keyCode == '\r')
					doSubmit();
			}
		}

		public void keyReleased(KeyEvent e) {
			
		}
	}


	private NIConsolePage fConsolePage;
	private ToolProcess fProcess;
	private History.Entry fCurrentHistoryEntry;
	private IHistoryListener fHistoryListener;
	
	private Composite fComposite;
	private Label fPrefix;
	private InputSourceViewer fSourceViewer;
	protected InputDocument fDocument;
	private Button fSubmitButton;
	
	EditorAdapter fEditorAdapter = new EditorAdapter();
	private SourceViewerDecorationSupport fSourceViewerDecorationSupport;
	private PairMatcher fPairMatcher;
	
	private boolean fIsHistoryCompoundChangeOpen = false; // for undo manager
	/** Indicates that the document is change by a history action */
	private boolean fInHistoryChange = false;
	

	public InputGroup(NIConsolePage page) {
		
		fConsolePage = page;
		fProcess = page.getConsole().getProcess();
		
		fDocument = new InputDocument();
	}

	public Composite createControl(Composite parent, IEditorConfiguration editorConfig) {
		
		fComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		fComposite.setLayout(layout);
		
		fPrefix = new Label(fComposite, SWT.LEFT);
		GridData gd = new GridData(SWT.LEFT, SWT.FILL, false, true);
		fPrefix.setLayoutData(gd);
		float[] hsb = fPrefix.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB().getHSB();
		fPrefix.setBackground(StatetPlugin.getDefault().getColorManager().getColor(new RGB(hsb[0], hsb[1], 0.925f)));
		fPrefix.setText("> "); //$NON-NLS-1$
		
		createSourceViewer(editorConfig);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fSourceViewer.getControl().setLayoutData(gd);
		fSourceViewer.getControl().addKeyListener(new ThisKeyListener());
		
		fSubmitButton = new Button(fComposite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalIndent = 3;
		gd.heightHint = new PixelConverter(fSubmitButton).convertHeightInCharsToPixels(1);
		fSubmitButton.setLayoutData(gd);
		fSubmitButton.setText(Messages.Console_SubmitButton_label);
		fSubmitButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				doSubmit();
				fSourceViewer.getControl().setFocus();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		setFont(fConsolePage.getConsole().getFont());
		
		fHistoryListener = new IHistoryListener() {
			public void entryAdded(Entry e) {
			}
			public void entryRemoved(Entry e) {
			}
			public void completeChange() {
				fCurrentHistoryEntry = null;
			}
		};
		fProcess.getHistory().addListener(fHistoryListener);
		
		fDocument.addPrenotifiedDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
				if (fIsHistoryCompoundChangeOpen && !fInHistoryChange) {
					fIsHistoryCompoundChangeOpen = false;
					fSourceViewer.getUndoManager().endCompoundChange();
				}
			}
			public void documentChanged(DocumentEvent event) {
			}
		});
		
		return fComposite;
	}
	
	protected void createSourceViewer(IEditorConfiguration editorConfig) {
		
		fSourceViewer = new InputSourceViewer(fComposite);
		if (editorConfig != null) {
			StatextSourceViewerConfiguration sourceViewerConfig = editorConfig.getSourceViewerConfiguration();
			fSourceViewer.configure(sourceViewerConfig);
			
			fSourceViewerDecorationSupport = new SourceViewerDecorationSupport(
					fSourceViewer, null, null, EditorsUI.getSharedTextColors()); 

			fPairMatcher = editorConfig.getPairMatcher();
			if (fPairMatcher != null) {
				fSourceViewerDecorationSupport.setCharacterPairMatcher(fPairMatcher);
				fSourceViewerDecorationSupport.setMatchingCharacterPainterPreferenceKeys(
						StatetUiPreferenceConstants.EDITOR_MATCHING_BRACKETS, 
						StatetUiPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR);
			}
			
			editorConfig.configureSourceViewerDecorationSupport(fSourceViewerDecorationSupport);
			fSourceViewerDecorationSupport.install(sourceViewerConfig.getPreferences());
			
			IDocumentSetupParticipant docuSetup = editorConfig.getDocumentSetupParticipant();
			if (docuSetup != null) {
				docuSetup.setup(fDocument.getMasterDocument());
			}
			
			new SourceViewerUpdater(fSourceViewer, sourceViewerConfig);
		}
		fSourceViewer.setDocument(fDocument);
		
		fSourceViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fEditorAdapter.cleanStatusLine();
			}
		});
		fSourceViewer.getTextWidget().addListener(SWT.FocusOut, new Listener() {
			public void handleEvent(Event event) {
				fEditorAdapter.cleanStatusLine();
			}
		});
	}
	
	public void createActions(MultiActionHandler multiActionHandler) {
		
		Widget control = fSourceViewer.getTextWidget();
		if (fPairMatcher != null) {
			GotoMatchingBracketAction fPairMatcherGotoAction =
				new GotoMatchingBracketAction(fPairMatcher, fEditorAdapter);
			multiActionHandler.addGlobalAction(control, 
					fPairMatcherGotoAction.getId(), fPairMatcherGotoAction);
		}
	}

	public void configureServices(IHandlerService commands, IContextService keys) {
		
		keys.activateContext("de.walware.statet.ui.contexts.TextEditorScope");
	}
	
	
	public void setFont(Font font) {
		
		fPrefix.setFont(font);
		fSourceViewer.getControl().setFont(font);
	}
	
	/**
	 * @param prompt new prompt or null.
	 */
	public void updatePrompt(final Prompt prompt) {
		
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (Layouter.isOkToUse(fPrefix)) {
					Prompt p = (prompt != null) ? prompt : fProcess.getWorkspaceData().getPrompt();
					fPrefix.setText(p.text);
					getComposite().layout(new Control[] { fPrefix });
					onPromptUpdate(p);
				}
			}
		});
	}
	
	protected void onPromptUpdate(Prompt prompt) {
	}
	
	public void doHistoryNewer() {
		
		if (fCurrentHistoryEntry == null)
			return;
		
		fCurrentHistoryEntry = fCurrentHistoryEntry.getNewer();

		String text = (fCurrentHistoryEntry != null) ?
				fCurrentHistoryEntry.getCommand() : ""; //$NON-NLS-1$
		
		if (!fIsHistoryCompoundChangeOpen) {
			fIsHistoryCompoundChangeOpen = true;
			fSourceViewer.getUndoManager().beginCompoundChange();
		}
		fInHistoryChange = true;
		fDocument.set(text);
		fInHistoryChange = false;
	}
	
	public void doHistoryOlder() {
		
		if (fCurrentHistoryEntry != null) {
			History.Entry next = fCurrentHistoryEntry.getOlder();
			if (next == null)
				return;
			fCurrentHistoryEntry = next;
		}
		else {
			fCurrentHistoryEntry = fProcess.getHistory().getNewest();
			if (fCurrentHistoryEntry == null)
				return;
		}
		if (!fIsHistoryCompoundChangeOpen) {
			fIsHistoryCompoundChangeOpen = true;
			fSourceViewer.getUndoManager().beginCompoundChange();
		}
		fInHistoryChange = true;
		fDocument.set(fCurrentHistoryEntry.getCommand());
		fInHistoryChange = false;
	}
	
	public void doSubmit() {
		
		String content = fDocument.get();
		ToolController controller = fProcess.getController();
		
		if (controller != null && controller.submit(content, SubmitType.CONSOLE)) {
			clear();
		}
	}
	
	public void clear() {
		
		fDocument.set(""); //$NON-NLS-1$
		fCurrentHistoryEntry = null;
		fSourceViewer.getUndoManager().reset();
	}

	public Composite getComposite() {

		return fComposite;
	}
	
	public InputSourceViewer getSourceViewer() {
		
		return fSourceViewer;
	}
	
	public Button getSubmitButton() {
		
		return fSubmitButton;
	}
	
	protected NIConsolePage getConsolePage() {
		
		return fConsolePage;
	}

	public void dispose() {
		
		fProcess.getHistory().removeListener(fHistoryListener);
		fHistoryListener = null;
		fCurrentHistoryEntry = null;
		
		if (fSourceViewerDecorationSupport != null) {
			fSourceViewerDecorationSupport.dispose();
			fSourceViewerDecorationSupport = null;
		}
		if (fPairMatcher != null) {
			fPairMatcher.dispose();
			fPairMatcher = null;
		}
		fProcess = null;
		fConsolePage = null;
	}
	
}
