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

package de.walware.statet.nico.console;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.walware.eclipsecommon.ui.util.PixelConverter;
import de.walware.statet.nico.runtime.History;
import de.walware.statet.nico.runtime.IHistoryListener;
import de.walware.statet.nico.runtime.SubmitType;
import de.walware.statet.nico.runtime.ToolController;
import de.walware.statet.nico.runtime.History.Entry;


public class InputGroup {

	
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


	private NIConsole fConsole;
	private ToolController fController;
	private History.Entry fCurrentHistoryEntry;
	private IHistoryListener fHistoryListener;
	
	private Composite fComposite;
	private Label fPrefix;
	private InputSourceViewer fSourceViewer;
	private InputDocument fDocument;
	private Button fSubmitButton;
	

	public InputGroup(NIConsole console) {
		
		fConsole = console;
		fController = console.getController();
		
		fDocument = new InputDocument();
		
	}

	public Composite createControl(Composite parent) {
		
		fComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		fComposite.setLayout(layout);
		
		fPrefix = new Label(fComposite, SWT.LEFT);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		fPrefix.setLayoutData(gd);
		fPrefix.setFont(fComposite.getFont());
		fPrefix.setText("> ");
		
		fSourceViewer = new InputSourceViewer(fComposite);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fSourceViewer.getControl().setLayoutData(gd);
		fSourceViewer.setDocument(fDocument);
		fSourceViewer.getControl().addKeyListener(new ThisKeyListener());
		
		fSubmitButton = new Button(fComposite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalIndent = 3;
		gd.heightHint = new PixelConverter(fSubmitButton).convertHeightInCharsToPixels(1);
		fSubmitButton.setLayoutData(gd);
		fSubmitButton.setText("Submit");
		fSubmitButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				doSubmit();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				doSubmit();
			}
		});
		
		setFont(fConsole.getFont());
		
		fHistoryListener = new IHistoryListener() {
			public void entryAdded(Entry e) {
			}
			public void entryRemoved(Entry e) {
			}
			public void completeChange() {
				fCurrentHistoryEntry = null;
			}
		};
		fController.getHistory().addListener(fHistoryListener);
		
		return fComposite;
	}
	
	
	public void setFont(Font font) {
		
		fPrefix.setFont(font);
		fSourceViewer.getControl().setFont(font);
	}
	
	
	
	public void doHistoryNewer() {
		
		if (fCurrentHistoryEntry == null)
			return;
		
		fCurrentHistoryEntry = fCurrentHistoryEntry.getNewer();

		String text = (fCurrentHistoryEntry != null) ?
				fCurrentHistoryEntry.getCommand() : "";
		fDocument.set(text);
	}
	
	public void doHistoryOlder() {
		
		if (fCurrentHistoryEntry != null) {
			History.Entry next = fCurrentHistoryEntry.getOlder();
			if (next == null)
				return;
			fCurrentHistoryEntry = next;
		}
		else {
			fCurrentHistoryEntry = fController.getHistory().getNewest();
			if (fCurrentHistoryEntry == null)
				return;
		}
		
		fDocument.set(fCurrentHistoryEntry.getCommand());
	}
	
	public void doSubmit() {
		
		String content = fDocument.get();
		fDocument.set("");
		fCurrentHistoryEntry = null;
		
		fController.submit(content, SubmitType.CONSOLE);
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

	public void dispose() {
		
		fController.getHistory().removeListener(fHistoryListener);
		fHistoryListener = null;
		fCurrentHistoryEntry = null;
		
		fController = null;
		fConsole = null;
	}
	
}
