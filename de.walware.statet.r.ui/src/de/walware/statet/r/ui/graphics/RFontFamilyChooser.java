/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.graphics;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.ui.dialogs.ToolPopup;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.rj.eclient.graphics.RGraphics;


public class RFontFamilyChooser extends ToolPopup {
	
	
	private static final String GENERIC = "Generic"; //$NON-NLS-1$
	
	
	public static void drawPreview(final GC gc,
			final int x, final int y, final int width, final int height,
			final Font font ) {
		if (font != null) {
			gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(x, y, width + 1, height + 1);
	//		gc.setTextAntialias(SWT.ON);
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
			gc.setFont(font);
			final FontMetrics fontMetrics = gc.getFontMetrics();
			gc.setClipping(x, y, width, height);
			gc.drawText("Abc 12.0", x + 2, y + 1 - fontMetrics.getLeading() + Math.max(1 + (height - fontMetrics.getHeight()) / 2, 0), //$NON-NLS-1$
					SWT.DRAW_TRANSPARENT );
			gc.setClipping((Rectangle) null);
		}
	}
	
	public static String getFontName(final String value) {
		if (value != null) {
			if (value.equals("serif")) { //$NON-NLS-1$
				return Platform.getPreferencesService().getString(RGraphics.PREF_FONTS_QUALIFIER, RGraphics.PREF_FONTS_SERIF_FONTNAME_KEY, "", null); //$NON-NLS-1$
			}
			else if (value.equals("sansserif")) { //$NON-NLS-1$
				return Platform.getPreferencesService().getString(RGraphics.PREF_FONTS_QUALIFIER, RGraphics.PREF_FONTS_SANS_FONTNAME_KEY, "", null); //$NON-NLS-1$
			}
			else if (value.equals("mono")) { //$NON-NLS-1$
				return Platform.getPreferencesService().getString(RGraphics.PREF_FONTS_QUALIFIER, RGraphics.PREF_FONTS_MONO_FONTNAME_KEY, "", null); //$NON-NLS-1$
			}
		}
		return value;
	}
	
	public static Font createFont(final Device device, final String fontName, int height) {
		FontData fontData = getFontData(device, fontName);
		if (fontData == null) {
			return null;
		}
		if (height < 0) {
			height = JFaceResources.getDialogFont().getFontData()[0].getHeight();
		}
		if (fontData.getHeight() != height) {
			fontData = new FontData(fontData.getName(), height, fontData.getStyle());
		}
		return new Font(device, fontData);
	}
	
	private static FontData getFontData(final Device device, final String fontName) {
		if (fontName == null || fontName.isEmpty()) {
			return null;
		}
		FontData[] fontDatas = device.getFontList(fontName, true);
		if (fontDatas.length == 0) {
			fontDatas = device.getFontList(fontName, false);
		}
		if (fontDatas.length == 0) {
			return null;
		}
		for (int i = 0; i < fontDatas.length; i++) {
			if (fontDatas[i].getStyle() == SWT.NORMAL) {
				return fontDatas[i];
			}
		}
		for (int i = 0; i < fontDatas.length; i++) {
			if (fontDatas[i].getStyle() == SWT.ITALIC) {
				return fontDatas[i];
			}
		}
		return fontDatas[0];
	}
	
	
	protected static abstract class GenericTab extends ToolTab {
		
		
		private static final ConstList<String> VALUES = new ConstArrayList<String>(
				"serif", //$NON-NLS-1$
				"sansserif", //$NON-NLS-1$
				"mono" ); //$NON-NLS-1$
		
		private Button[] fButtons;
		
		
		GenericTab(final ToolPopup parent) {
			this(parent, "Standard", "Definition by generic R Standard Names"); //$NON-NLS-1$
		}
		
		GenericTab(final ToolPopup parent, final String name, final String tooltip) {
			super(GENERIC, parent, name, tooltip);
			
			final Composite composite = create();
			composite.setLayout(LayoutUtil.createTabGrid(3));
			
			{	final String[] labels = new String[] { "Serif", "Sans Serif", "Monospace" };
				final Listener listener = new Listener() {
					@Override
					public void handleEvent(final Event event) {
						switch (event.type) {
						case SWT.Selection:
							setValue((String) event.widget.getData());
							return;
						case SWT.MouseDoubleClick:
							event.widget.getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									performOK();
								}
							});
							return;
						}
					}
				};
				fButtons = new Button[3];
				for (int i = 0; i < 3; i++) {
					final Button button = new Button(composite, SWT.RADIO);
					button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
					button.setText(labels[i] + " ('" + VALUES.get(i) + "')"); //$NON-NLS-1$ //$NON-NLS-2$
					button.setData(VALUES.get(i));
					button.addListener(SWT.Selection, listener);
					button.addListener(SWT.MouseDoubleClick, listener);
					
					fButtons[i] = button;
				}
				fButtons[0].setSelection(true);
			}
			
			LayoutUtil.addGDDummy(composite, true, 3);
			
			{	final Link link = new Link(composite, SWT.NONE);
				link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
				link.setText("<a>Preferences for Standard Fonts in StatET...</a>");
				link.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						final ToolPopup popup = getParent();
						popup.beginIgnoreActivation();
						try {
							final PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(popup.getShell(),
									"de.walware.statet.r.preferencePages.RGraphicsPage", //$NON-NLS-1$
									new String[] { "de.walware.statet.r.preferencePages.RGraphicsPage" }, //$NON-NLS-1$
									null );
							dialog.open();
						}
						finally {
							popup.endIgnoreActivation();
						}
					}
				});
			}
		}
		
		@Override
		protected void activated() {
			final String value = getValue();
			final int idx = VALUES.indexOf(value);
			for (int i = 0; i < 3; i++) {
				fButtons[i].setSelection(i == idx);
			}
		}
		
		protected abstract String getValue();
		
		protected abstract void setValue(String value);
		
	}
	
	
	private String fInitialValue;
	private String fInitialFontName;
	private Font fInitialSWTFont;
	private String fCurrentFontName;
	private String fCurrentValue;
	private Font fCurrentSWTFont;
	
	private Composite fStatusControl;
	
	
	public RFontFamilyChooser() {
	}
	
	
	public void open(final Shell parent, final Rectangle position, final String initialValue) {
		fInitialValue = initialValue;
		doSetValue((initialValue != null) ? initialValue : "sansserif"); //$NON-NLS-1$
		
		super.open(parent, position);
	}
	
	@Override
	protected void addTabs(final CTabFolder tabFolder) {
		new GenericTab(this) {
			@Override
			protected String getValue() {
				return RFontFamilyChooser.this.getValue();
			}
			@Override
			protected void setValue(final String value) {
				RFontFamilyChooser.this.doSetValue(value);
			}
		};
	}
	
	@Override
	protected ToolTab getBestTab() {
		return getTab(GENERIC);
	}
	
	@Override
	protected void addStatusControls(final Composite composite) {
		fStatusControl = new PreviewCanvas(composite) {
			@Override
			protected void drawPreview(final GC gc, final int idx,
					final int x, final int y, final int width, final int height) {
				Font font = null;
				switch (idx) {
				case 0:
					if (fInitialValue != null) {
						if (fInitialSWTFont == null && fInitialFontName != "") { //$NON-NLS-1$
							if (fInitialFontName == null) {
								fInitialFontName = getFontName(fInitialValue);
							}
							fInitialSWTFont = createFont(gc.getDevice(), fInitialFontName, -1);
							if (fInitialSWTFont == null) {
								fInitialFontName = ""; //$NON-NLS-1$
							}
						}
						font = fInitialSWTFont;
					}
					break;
				case 1:
					if (fCurrentValue != null) {
						if (fCurrentSWTFont == null && fCurrentFontName != "") { //$NON-NLS-1$
							if (fCurrentFontName == null) {
								fCurrentFontName = getFontName(fCurrentValue);
							}
							fCurrentSWTFont = createFont(gc.getDevice(), fCurrentFontName, -1);
							if (fCurrentSWTFont == null) {
								fCurrentFontName = ""; //$NON-NLS-1$
							}
						}
						font = fCurrentSWTFont;
					}
					break;
				default:
					break;
				}
				RFontFamilyChooser.drawPreview(gc, x, y, width, height, font);
			}
		};
		updateStatus();
	}
	
	protected void doSetValue(final String value) {
		if (value == null || value.equals(fCurrentValue)) {
			return;
		}
		final String fontName = getFontName(value);
		if (fCurrentSWTFont != null && (fontName == null || !fontName.equals(fCurrentFontName))) {
			fCurrentSWTFont.dispose();
			fCurrentSWTFont = null;
		}
		fCurrentValue = value;
		fCurrentFontName = fontName;
		
		if (fStatusControl != null) {
			updateStatus();
		}
	}
	
	protected void updateStatus() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Previous: ");
		sb.append((fInitialValue != null) ? fInitialValue : "-"); //$NON-NLS-1$
		sb.append("\n");
		sb.append("Current: ");
		sb.append(fCurrentValue);
//		fStatusLabel.setText(info);
		fStatusControl.setToolTipText(sb.toString());
		fStatusControl.redraw();
	}
	
	public String getValue() {
		return fCurrentValue;
	}
	
	@Override
	protected void onDispose() {
		fStatusControl = null;
		super.onDispose();
		if (fInitialSWTFont != null) {
			fInitialSWTFont.dispose();
			fInitialFontName = null;
			fInitialSWTFont = null;
		}
		if (fCurrentSWTFont != null) {
			fCurrentSWTFont.dispose();
			fCurrentFontName = null;
			fCurrentSWTFont = null;
		}
	}
	
}
