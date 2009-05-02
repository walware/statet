/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.base.ui.StatetImages;


/**
 * Search text custom widget (with clear button)
 * 
 * E-3.5 use optional the new native support
 */
public class SearchText extends Composite {
	// org.eclipse.ui.dialogs.FilteredTree
	
	private Text fTextControl;
	private Label fClearButtonControl;
	
	
	public SearchText(final Composite parent) {
		super(parent, SWT.BORDER);
		
		setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		final GridLayout layout = LayoutUtil.applyCompositeDefaults(new GridLayout(), 2);
		layout.horizontalSpacing = 0;
		setLayout(layout);
		
		fTextControl = new Text(this, SWT.LEFT | SWT.SINGLE);
		fTextControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fTextControl.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					downPressed();
				}
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					okPressed();
				}
				if (e.keyCode == SWT.ESC) {
					setText(null);
				}
			}
		});
		fTextControl.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				textChanged0();
			}
		});
		
		createClearTextNew(this);
	}
	
	private void textChanged0() {
		textChanged();
	}
	
	
	@Override
	public boolean setFocus() {
		return fTextControl.setFocus();
	}
	
	@Override
	public void setToolTipText(final String text) {
		fTextControl.setToolTipText(text);
	}
	
	public void setText(final String text) {
		fTextControl.setText((text != null) ? text : "");
	}
	
	public String getText() {
		return fTextControl.getText();
	}
	
	public Text getTextControl() {
		return fTextControl;
	}
	
	public void clearText() {
		setText(null);
		fTextControl.setFocus();
	}
	
	protected void okPressed() {
	}
	
	protected void downPressed() {
	}
	
	protected void textChanged() {
	}
	
	
	/**
	 * Create the button that clears the text.
	 * 
	 * @param parent parent <code>Composite</code> of toolbar button
	 */
	private void createClearTextNew(final Composite parent) {
		// only create the button if the text widget doesn't support one natively
//		if ((filterText.getStyle() & SWT.ICON_CANCEL) == 0) {
			final Image activeImage = StatetImages.getImage(StatetImages.LOCTOOL_CLEARSEARCH);
			final Image inactiveImage = StatetImages.getImage(StatetImages.LOCTOOLD_CLEARSEARCH);
			final Image pressedImage = new Image(Display.getCurrent(), activeImage, SWT.IMAGE_GRAY);
			
			final Label clearButton= new Label(parent, SWT.NONE);
			clearButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			clearButton.setImage(inactiveImage);
			clearButton.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			clearButton.setToolTipText("Clear");
			clearButton.addMouseListener(new MouseAdapter() {
				private MouseMoveListener fMoveListener;
				
				@Override
				public void mouseDown(final MouseEvent e) {
					clearButton.setImage(pressedImage);
					fMoveListener= new MouseMoveListener() {
						private boolean fMouseInButton= true;
						
						public void mouseMove(final MouseEvent e) {
							final boolean mouseInButton= isMouseInButton(e);
							if (mouseInButton != fMouseInButton) {
								fMouseInButton= mouseInButton;
								clearButton.setImage(mouseInButton ? pressedImage : inactiveImage);
							}
						}
					};
					clearButton.addMouseMoveListener(fMoveListener);
				}
				
				@Override
				public void mouseUp(final MouseEvent e) {
					if (fMoveListener != null) {
						clearButton.removeMouseMoveListener(fMoveListener);
						fMoveListener= null;
						final boolean mouseInButton= isMouseInButton(e);
						clearButton.setImage(mouseInButton ? activeImage : inactiveImage);
						if (mouseInButton) {
							clearText();
						}
					}
				}
				
				private boolean isMouseInButton(final MouseEvent e) {
					final Point buttonSize = clearButton.getSize();
					return 0 <= e.x && e.x < buttonSize.x && 0 <= e.y && e.y < buttonSize.y;
				}
			});
			clearButton.addMouseTrackListener(new MouseTrackListener() {
				public void mouseEnter(final MouseEvent e) {
					clearButton.setImage(activeImage);
				}
				public void mouseExit(final MouseEvent e) {
					clearButton.setImage(inactiveImage);
				}
				public void mouseHover(final MouseEvent e) {
				}
			});
			clearButton.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(final DisposeEvent e) {
					pressedImage.dispose();
				}
			});
			clearButton.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {
					@Override
					public void getName(final AccessibleEvent e) {
						e.result = "Clear filter field";
					}
			});
			clearButton.getAccessible().addAccessibleControlListener(
				new AccessibleControlAdapter() {
					@Override
					public void getRole(final AccessibleControlEvent e) {
						e.detail = ACC.ROLE_PUSHBUTTON;
					}
			});
			fClearButtonControl= clearButton;
//		}
	}

}
