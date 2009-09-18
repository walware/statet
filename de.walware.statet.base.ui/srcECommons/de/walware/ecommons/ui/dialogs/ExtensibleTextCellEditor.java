/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation (TextCellEditor)
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


/**
 * A cell editor that manages a text entry field with tools button.
 * The cell editor's value is the text string itself.
 * <p>
 */
public abstract class ExtensibleTextCellEditor extends CellEditor {
	
	
	public static abstract class FocusGroup implements Listener {
		
		private final List<Widget> fControls = new ArrayList<Widget>();
		
		private Widget fWidget;
		
		private int fIgnore;
		
		public void handleEvent(final Event event) {
			Widget control;
			switch (event.type) {
			case SWT.Activate:
				control = Display.getCurrent().getFocusControl();
				fWidget = (event.widget != control && fControls.contains(control)) ? control : null;
				return;
			case SWT.FocusIn:
				fWidget = null;
				return;
			case SWT.FocusOut:
				control = fWidget;
				fWidget = null;
				if (fIgnore == 0 && event.widget != control) {
					focusLost();
				}
				return;
			}
			
		}
		
		protected abstract void focusLost();
		
		
		public void add(final Control control) {
			fControls.add(control);
			control.addListener(SWT.Activate, this);
			control.addListener(SWT.FocusIn, this);
			control.addListener(SWT.FocusOut, this);
		}
		
		public void addRecursivly(final Control control) {
			if (control instanceof Composite) {
				final Control[] children = ((Composite) control).getChildren();
				for (final Control child : children) {
					addRecursivly(child);
				}
			}
			else {
				add(control);
			}
		}
		
		public void discontinueTracking() {
			fIgnore++;
		}
		
		public void continueTracking() {
			fIgnore--;
		}
		
	}
	
	
	/**
	 * Default TextCellEditor style
	 * specify no borders on text widget as cell outline in table already
	 * provides the look of a border.
	 */
	protected static final int DEFAULT_STYLE = SWT.SINGLE;
	
	
	/**
	 * The text control; initially <code>null</code>.
	 */
	protected Text fText;
	
	private ModifyListener fModifyListener;
	
	/**
	 * State information for updating action enablement
	 */
	private boolean fIsSelection = false;
	
	private boolean fIsDeleteable = false;
	
	private boolean fIsSelectable = false;
	
	private FocusGroup fFocusGroup;
	
	
	/**
	 * Creates a new text string cell editor parented under the given control.
	 * The cell editor value is the string itself, which is initially the empty string. 
	 * Initially, the cell editor has no cell validator.
	 *
	 * @param parent the parent control
	 */
	public ExtensibleTextCellEditor(final Composite parent) {
		super(parent);
	}
	
	/**
	 * Checks to see if the "deletable" state (can delete/
	 * nothing to delete) has changed and if so fire an
	 * enablement changed notification.
	 */
	private void checkDeleteable() {
		final boolean oldIsDeleteable = fIsDeleteable;
		fIsDeleteable = isDeleteEnabled();
		if (oldIsDeleteable != fIsDeleteable) {
			fireEnablementChanged(DELETE);
		}
	}
	
	/**
	 * Checks to see if the "selectable" state (can select)
	 * has changed and if so fire an enablement changed notification.
	 */
	private void checkSelectable() {
		final boolean oldIsSelectable = fIsSelectable;
		fIsSelectable = isSelectAllEnabled();
		if (oldIsSelectable != fIsSelectable) {
			fireEnablementChanged(SELECT_ALL);
		}
	}
	
	/**
	 * Checks to see if the selection state (selection /
	 * no selection) has changed and if so fire an
	 * enablement changed notification.
	 */
	private void checkSelection() {
		final boolean oldIsSelection = fIsSelection;
		fIsSelection = fText.getSelectionCount() > 0;
		if (oldIsSelection != fIsSelection) {
			fireEnablementChanged(COPY);
			fireEnablementChanged(CUT);
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on CellEditor.
	 */
	@Override
	protected Control createControl(final Composite parent) {
		final Control control = createCustomControl(parent);
		
		fText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				handleDefaultSelection(e);
			}
		});
		fText.addKeyListener(new KeyAdapter() {
			// hook key pressed - see PR 14201  
			@Override
			public void keyPressed(final KeyEvent e) {
				keyReleaseOccured(e);
				
				// as a result of processing the above call, clients may have
				// disposed this cell editor
				if ((getControl() == null) || getControl().isDisposed()) {
					return;
				}
				checkSelection(); // see explanation below
				checkDeleteable();
				checkSelectable();
			}
		});
		fText.addTraverseListener(new TraverseListener() {
			public void keyTraversed(final TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE
						|| e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
				}
			}
		});
		// We really want a selection listener but it is not supported so we
		// use a key listener and a mouse listener to know when selection changes
		// may have occurred
		fText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(final MouseEvent e) {
				checkSelection();
				checkDeleteable();
				checkSelectable();
			}
		});
		fText.setFont(parent.getFont());
		fText.setBackground(parent.getBackground());
		fText.setText("");//$NON-NLS-1$
		fText.addModifyListener(getModifyListener());
		fText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		fFocusGroup = new FocusGroup() {
			@Override
			protected void focusLost() {
				ExtensibleTextCellEditor.this.focusLost();
			}
		};
		fFocusGroup.addRecursivly(control);
		
		return control;
	}
	
	protected FocusGroup getFocusGroup() {
		return fFocusGroup;
	}
	
	protected abstract Control createCustomControl(final Composite parent);
	
	/**
	 * The <code>TextCellEditor</code> implementation of
	 * this <code>CellEditor</code> framework method returns
	 * the text string.
	 *
	 * @return the text string
	 */
	@Override
	protected Object doGetValue() {
		return fText.getText();
	}
	
	/* (non-Javadoc)
	 * Method declared on CellEditor.
	 */
	@Override
	protected void doSetFocus() {
		if (fText != null) {
			fText.selectAll();
			fText.setFocus();
			checkSelection();
			checkDeleteable();
			checkSelectable();
		}
	}
	
	/**
	 * The <code>TextCellEditor</code> implementation of
	 * this <code>CellEditor</code> framework method accepts
	 * a text string (type <code>String</code>).
	 *
	 * @param value a text string (type <code>String</code>)
	 */
	@Override
	protected void doSetValue(final Object value) {
		Assert.isTrue(fText != null && (value instanceof String));
		fText.removeModifyListener(getModifyListener());
		fText.setText((String) value);
		fText.addModifyListener(getModifyListener());
	}
	
	/**
	 * Processes a modify event that occurred in this text cell editor.
	 * This framework method performs validation and sets the error message
	 * accordingly, and then reports a change via <code>fireEditorValueChanged</code>.
	 * Subclasses should call this method at appropriate times. Subclasses
	 * may extend or reimplement.
	 *
	 * @param e the SWT modify event
	 */
	protected void editOccured(final ModifyEvent e) {
		String value = fText.getText();
		if (value == null) {
			value = "";//$NON-NLS-1$
		}
		final Object typedValue = value;
		final boolean oldValidState = isValueValid();
		final boolean newValidState = isCorrect(typedValue);
		if (typedValue == null && newValidState) {
			Assert.isTrue(false,
					"Validator isn't limiting the cell editor's type range");//$NON-NLS-1$
		}
		if (!newValidState) {
			// try to insert the current value into the error message.
			setErrorMessage(MessageFormat.format(getErrorMessage(),
					new Object[] { value }));
		}
		valueChanged(oldValidState, newValidState);
	}
	
	/**
	 * Since a text editor field is scrollable we don't
	 * set a minimumSize.
	 */
	@Override
	public LayoutData getLayoutData() {
		return new LayoutData();
	}
	
	/**
	 * Return the modify listener.
	 */
	private ModifyListener getModifyListener() {
		if (fModifyListener == null) {
			fModifyListener = new ModifyListener() {
				public void modifyText(final ModifyEvent e) {
					editOccured(e);
				}
			};
		}
		return fModifyListener;
	}
	
	/**
	 * Handles a default selection event from the text control by applying the editor
	 * value and deactivating this cell editor.
	 * 
	 * @param event the selection event
	 * 
	 * @since 3.0
	 */
	protected void handleDefaultSelection(final SelectionEvent event) {
		// same with enter-key handling code in keyReleaseOccured(e);
		fireApplyEditorValue();
		deactivate();
	}

	/**
	 * The <code>TextCellEditor</code>  implementation of this 
	 * <code>CellEditor</code> method returns <code>true</code> if 
	 * the current selection is not empty.
	 */
	@Override
	public boolean isCopyEnabled() {
		if (fText == null || fText.isDisposed()) {
			return false;
		}
		return fText.getSelectionCount() > 0;
	}

	/**
	 * The <code>TextCellEditor</code>  implementation of this 
	 * <code>CellEditor</code> method returns <code>true</code> if 
	 * the current selection is not empty.
	 */
	@Override
	public boolean isCutEnabled() {
		if (fText == null || fText.isDisposed()) {
			return false;
		}
		return fText.getSelectionCount() > 0;
	}
	
	/**
	 * The <code>TextCellEditor</code>  implementation of this 
	 * <code>CellEditor</code> method returns <code>true</code>
	 * if there is a selection or if the caret is not positioned 
	 * at the end of the text.
	 */
	@Override
	public boolean isDeleteEnabled() {
		if (fText == null || fText.isDisposed()) {
			return false;
		}
		return fText.getSelectionCount() > 0
				|| fText.getCaretPosition() < fText.getCharCount();
	}
	
	/**
	 * The <code>TextCellEditor</code>  implementation of this 
	 * <code>CellEditor</code> method always returns <code>true</code>.
	 */
	@Override
	public boolean isPasteEnabled() {
		if (fText == null || fText.isDisposed()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Check if save all is enabled
	 * @return true if it is 
	 */
	public boolean isSaveAllEnabled() {
		if (fText == null || fText.isDisposed()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns <code>true</code> if this cell editor is
	 * able to perform the select all action.
	 * <p>
	 * This default implementation always returns 
	 * <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 * @return <code>true</code> if select all is possible,
	 *  <code>false</code> otherwise
	 */
	@Override
	public boolean isSelectAllEnabled() {
		if (fText == null || fText.isDisposed()) {
			return false;
		}
		return fText.getCharCount() > 0;
	}
	
	/**
	 * Processes a key release event that occurred in this cell editor.
	 * <p>
	 * The <code>TextCellEditor</code> implementation of this framework method 
	 * ignores when the RETURN key is pressed since this is handled in 
	 * <code>handleDefaultSelection</code>.
	 * An exception is made for Ctrl+Enter for multi-line texts, since
	 * a default selection event is not sent in this case. 
	 * </p>
	 *
	 * @param keyEvent the key event
	 */
	@Override
	protected void keyReleaseOccured(final KeyEvent keyEvent) {
		if (keyEvent.character == '\r') { // Return key
			// Enter is handled in handleDefaultSelection.
			// Do not apply the editor value in response to an Enter key event
			// since this can be received from the IME when the intent is -not-
			// to apply the value.  
			// See bug 39074 [CellEditors] [DBCS] canna input mode fires bogus event from Text Control
			//
			// An exception is made for Ctrl+Enter for multi-line texts, since
			// a default selection event is not sent in this case. 
			if (fText != null && !fText.isDisposed()
					&& (fText.getStyle() & SWT.MULTI) != 0) {
				if ((keyEvent.stateMask & SWT.CTRL) != 0) {
					super.keyReleaseOccured(keyEvent);
				}
			}
			return;
		}
		super.keyReleaseOccured(keyEvent);
	}
	
	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method copies the
	 * current selection to the clipboard. 
	 */
	@Override
	public void performCopy() {
		fText.copy();
	}
	
	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method cuts the
	 * current selection to the clipboard. 
	 */
	@Override
	public void performCut() {
		fText.cut();
		checkSelection();
		checkDeleteable();
		checkSelectable();
	}
	
	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method deletes the
	 * current selection or, if there is no selection,
	 * the character next character from the current position. 
	 */
	@Override
	public void performDelete() {
		if (fText.getSelectionCount() > 0) {
			// remove the contents of the current selection
			fText.insert(""); //$NON-NLS-1$
		} else {
			// remove the next character
			final int pos = fText.getCaretPosition();
			if (pos < fText.getCharCount()) {
				fText.setSelection(pos, pos + 1);
				fText.insert(""); //$NON-NLS-1$
			}
		}
		checkSelection();
		checkDeleteable();
		checkSelectable();
	}
	
	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method pastes the
	 * the clipboard contents over the current selection. 
	 */
	@Override
	public void performPaste() {
		fText.paste();
		checkSelection();
		checkDeleteable();
		checkSelectable();
	}
	
	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method selects all of the
	 * current text. 
	 */
	@Override
	public void performSelectAll() {
		fText.selectAll();
		checkSelection();
		checkDeleteable();
	}
	
	@Override
	protected boolean dependsOnExternalFocusListener() {
		return false;
	}
	
	
	protected void fillToolsMenu(final Menu menu) {
	}
	
}
