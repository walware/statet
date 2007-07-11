/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 *
 */
public class LayoutUtil {
	
	private static class DialogValues {
		
		int defaultEntryFieldWidth;
		
		int defaultHSpacing;
		int defaultVSpacing;
		int defaultIndent;
		int defaultSmallIndent;
		
		public DialogValues() {

			GC gc = new GC(Display.getCurrent());
			gc.setFont(JFaceResources.getDialogFont());
			FontMetrics fontMetrics = gc.getFontMetrics();
			
			defaultHSpacing = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.HORIZONTAL_SPACING);
			defaultVSpacing = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_SPACING);
			defaultEntryFieldWidth = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.ENTRY_FIELD_WIDTH);
			defaultIndent = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.INDENT);
			defaultSmallIndent = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.SMALL_INDENT);
			
			gc.dispose();
		}
	}
	
	private static DialogValues gDialogValues;
	
	private static DialogValues getDialogValues() {
		if (gDialogValues == null) {
			JFaceResources.getFontRegistry().addListener(new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (JFaceResources.DIALOG_FONT.equals(event.getProperty())) {
						UIAccess.getDisplay().asyncExec(new Runnable() {
							public void run() {
								gDialogValues = new DialogValues();
							}
						});
					}
				}
			});
			gDialogValues = new DialogValues();
		}
		return gDialogValues;
	}

	
	public static Point defaultSpacing() {
		return new Point(getDialogValues().defaultHSpacing, getDialogValues().defaultVSpacing);
	}
	
	public static int defaultHSpacing() {
		return getDialogValues().defaultHSpacing;
	}
	
	public static int defaultVSpacing() {
		return getDialogValues().defaultVSpacing;
	}
	
	public static int defaultIndent() {
		return getDialogValues().defaultIndent;
	}
	
	public static int defaultSmallIndent() {
		return getDialogValues().defaultSmallIndent;
	}

	public static int hintWidth(Button button) {
		button.setFont(JFaceResources.getFontRegistry().get(JFaceResources.DIALOG_FONT));
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}
	
	public static int hintWidth(Text text, int numChars) {
		return hintWidth(text, JFaceResources.DIALOG_FONT, numChars);
	}
	public static int hintWidth(Text text, String symbolicName, int numChars) {
		text.setFont(JFaceResources.getFontRegistry().get(symbolicName));
		if (numChars == -1) {
			return getDialogValues().defaultEntryFieldWidth;
		}
		PixelConverter converter = new PixelConverter(text);
		int widthHint = converter.convertWidthInCharsToPixels(numChars);
		return widthHint;
	}
	
	public static int hintWidth(Combo combo, String[] items) {
		combo.setFont(JFaceResources.getFontRegistry().get(JFaceResources.DIALOG_FONT));
		if (items == null || items.length == 0) {
			return getDialogValues().defaultEntryFieldWidth;
		}
		int max = 0;
		for (String s : items) {
			max = Math.max(max, s.length());
		}
		PixelConverter converter = new PixelConverter(combo);
		int widthHint = (int) (converter.convertWidthInCharsToPixels(max+2) * 1.05);
		return widthHint;
	}


	public static GridLayout applyGroupDefaults(GridLayout gl, int numColumns) {
		gl.numColumns = numColumns;
		gl.horizontalSpacing = defaultHSpacing();
		gl.verticalSpacing = defaultVSpacing();
		gl.marginTop = defaultVSpacing() / 2;
		return gl;
	}
	
	public static GridLayout applyCompositeDefaults(GridLayout gl, int numColumns) {
		gl.numColumns = numColumns;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.horizontalSpacing = defaultHSpacing();
		gl.verticalSpacing = defaultVSpacing();
		return gl;
	}
		
	public static void addGDDummy(Composite composite) {
		Label dummy = new Label(composite, SWT.NONE);
		dummy.setVisible(false);
		dummy.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
	}

	public static void addSmallFiller(Composite composite, boolean grab) {
		Label filler = new Label(composite, SWT.NONE);
		Layout layout = composite.getLayout();
		if (layout instanceof GridLayout) {
			GridData gd = new GridData(SWT.FILL, SWT.FILL, false, grab);
			gd.horizontalSpan = ((GridLayout) layout).numColumns;
			gd.heightHint = defaultVSpacing() / 2;
			filler.setLayoutData(gd);
		}
	}

	public static IDialogSettings createDialogBoundSettings(String dialogId, AbstractUIPlugin plugin) {
		String sectionName = dialogId + "_dialogBounds"; //$NON-NLS-1$
		IDialogSettings settings = plugin.getDialogSettings();
		IDialogSettings section = settings.getSection(sectionName);
		if (section == null)
			section = settings.addNewSection(sectionName);
		return section;
	}


	private LayoutUtil() {
	}

}
