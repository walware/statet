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

import static de.walware.statet.r.core.model.RGraphicFunctions.COLORS_COLOR_DEF_TYPE;
import static de.walware.statet.r.core.model.RGraphicFunctions.PALETTE_COLOR_DEF_TYPE;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.graphics.core.ColorAlphaDef;
import de.walware.ecommons.graphics.core.ColorDef;
import de.walware.ecommons.graphics.core.HSVColorDef;
import de.walware.ecommons.ui.components.ColorPalette;
import de.walware.ecommons.ui.components.DoubleText;
import de.walware.ecommons.ui.components.HSVSelector;
import de.walware.ecommons.ui.components.IObjValueListener;
import de.walware.ecommons.ui.components.IntText;
import de.walware.ecommons.ui.components.ObjValueEvent;
import de.walware.ecommons.ui.components.RGBSelector;
import de.walware.ecommons.ui.dialogs.ToolPopup;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.model.RGraphicFunctions;


public class RColorChooser extends ToolPopup {
	
	
	private static final String R_COLORS = "RColors"; //$NON-NLS-1$
	private static final String R_PALETTE = "RPalette"; //$NON-NLS-1$
	private static final String RGB = "RGB"; //$NON-NLS-1$
	private static final String HSV = "HSV"; //$NON-NLS-1$
	
	
	public static void drawPreview(final GC gc,
			final int x, final int y, final int width, final int height,
			final Color color ) {
		if (color != null) {
			gc.setBackground(color);
			gc.fillRectangle(x, y, width + 1, height + 1);
		}
	}
	
	public static Color createPreviewColor(final Display display, final ColorDef colorDef) {
		return new Color(display, colorDef.getRed(), colorDef.getGreen(), colorDef.getBlue());
	}
	
	
	protected class PaletteTab extends ToolTab {
		
		ColorPalette fPalette;
		
		
		PaletteTab(final String key, final String name, final String tooltip, final List<? extends ColorDef> colors) {
			super(key, RColorChooser.this, name, "Palette of Named Colors in R");
			
			final Composite composite = create();
			composite.setLayout(LayoutUtil.createTabGrid(1));
			
			fPalette = new ColorPalette(composite);
			fPalette.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			fPalette.setColors(colors);
			fPalette.addValueListener(new IObjValueListener<ColorDef>() {
				@Override
				public void valueAboutToChange(final ObjValueEvent<ColorDef> event) {
				}
				@Override
				public void valueChanged(final ObjValueEvent<ColorDef> event) {
					if (event.newValue == null) {
						return;
					}
					RColorChooser.this.doSetColorValue(event.newValue);
					if ((event.flags & ObjValueEvent.DEFAULT_SELECTION) != 0) {
						performOK();
					}
				}
			});
			
		}
		
		@Override
		protected void activated() {
			final ColorDef value = doGetColorValue();
			fPalette.setValue(0, value);
			final ColorDef paletteColor = fPalette.getValue(0);
			if (paletteColor != null) {
				RColorChooser.this.doSetColorValue(paletteColor);
			}
		}
		
	}
	
	private static final int[] safeRGBIntArray(final ColorDef color) {
		final int[] rgb = new int[] { 255, 0, 0 };
		if (color != null) {
			rgb[0] = color.getRed();
			rgb[1] = color.getGreen();
			rgb[2] = color.getBlue();
		}
		return rgb;
	}
	
	protected class RGBTab extends ToolTab {
		
		
		private final RGBSelector fSelector;
		
		private final Button[] fRGBButton = new Button[3];
		private final IntText[] fRGBText = new IntText[3];
		
		private Text fHexText;
		
		private int fTextChange;
		
		
		RGBTab() {
			super(RGB, RColorChooser.this, "RGB", "Definition by Red-Green-Blue"); //$NON-NLS-1$
			
			final Composite composite = create();
			composite.setLayout(LayoutUtil.createTabGrid(4));
			
			fSelector = new RGBSelector(composite);
			fSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5));
			
			{	final String[] fieldLabels = new String[] { "&Red", "&Green", "&Blue" };
				final Listener buttonListener = new Listener() {
					@Override
					public void handleEvent(final Event event) {
						fSelector.setPrimary(indexOf((Button) event.widget));
					}
				};
				final IObjValueListener<Integer> textListener = new IObjValueListener<Integer>() {
					@Override
					public void valueAboutToChange(final ObjValueEvent<Integer> event) {
					}
					@Override
					public void valueChanged(final ObjValueEvent<Integer> event) {
						if (event.newValue == null) {
							return;
						}
						final ColorDef oldColor = fSelector.getValue(0);
						final int[] rgb = safeRGBIntArray(oldColor);
						fTextChange++;
						try {
							rgb[indexOf((IntText) event.getSource())] = event.newValue;
							final ColorDef newColor = new ColorDef(rgb[0], rgb[1], rgb[2]);
							if (!newColor.equals(oldColor)) {
								fSelector.setValue(0, newColor);
								RColorChooser.this.doSetColorValue(newColor);
							}
						}
						catch (final NumberFormatException e) {}
						catch (final IllegalArgumentException e) {}
						finally {
							fTextChange--;
						}
						return;
					}
				};
				for (int i = 0; i < 3; i++) {
					final Button button = new Button(composite, SWT.RADIO);
					button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
					button.setText(fieldLabels[i]);
					button.addListener(SWT.Selection, buttonListener);
					fRGBButton[i] = button;
					
					final IntText text = new IntText(composite, SWT.BORDER);
					final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
					gd.widthHint = LayoutUtil.hintWidth(text.getControl(), 4);
					text.getControl().setLayoutData(gd);
					text.addValueListener(textListener);
					
					text.setIncrement(1);
					text.setMinMax(0, 255);
					fRGBText[i] = text;
				}
				fRGBButton[0].setSelection(true);
			}
			{	final Label label = new Label(composite, SWT.NONE);
				final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1);
				label.setLayoutData(gd);
			}
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
				label.setText("Hex:");
				
				final Text text = new Text(composite, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
				final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
				gd.widthHint = LayoutUtil.hintWidth(text, 9);
				text.setLayoutData(gd);
				text.addListener(SWT.Modify, new Listener() {
					@Override
					public void handleEvent(final Event event) {
						final String s = fHexText.getText();
						if (s.length() == 7 && s.charAt(0) == '#') {
							final ColorDef newColor = ColorDef.parseRGBHex(s.substring(1));
							if (newColor != null && !newColor.equals(fSelector.getValue(0))) {
								fSelector.setValue(0, newColor);
							}
						}
					}
				});
				fHexText = text;
			}
			
			fSelector.addValueListener(new IObjValueListener<ColorDef>() {
				@Override
				public void valueAboutToChange(final ObjValueEvent<ColorDef> event) {
				}
				@Override
				public void valueChanged(final ObjValueEvent<ColorDef> event) {
					final ColorDef value = event.newValue;
					if (fTextChange == 0) {
						final Integer[] rgb = new Integer[3];
						rgb[0] = value.getRed();
						rgb[1] = value.getGreen();
						rgb[2] = value.getBlue();
						for (int i = 0; i < 3; i++) {
							fRGBText[i].setValue(0, rgb[i]);
						}
						
						RColorChooser.this.doSetColorValue(value);
					}
					
					final StringBuilder sb = new StringBuilder(7);
					sb.append('#');
					value.printRGBHex(sb);
					final Point selection = fHexText.getSelection();
					fHexText.setText(sb.toString());
					fHexText.setSelection(selection);
				}
			});
		}
		
		private int indexOf(final IntText source) {
			for (int i = 0; i < 3; i++) {
				if (fRGBText[i] == source) {
					return i;
				}
			}
			return -1;
		}
		
		private int indexOf(final Button source) {
			for (int i = 0; i < 3; i++) {
				if (fRGBButton[i] == source) {
					return i;
				}
			}
			return -1;
		}
		
		@Override
		protected void activated() {
			final ColorDef value = doGetColorValue();
			fSelector.setValue(0, value);
		}
	}
	
	private final static double[] safeHSVDoubleArray(final HSVColorDef color) {
		final double[] hsv = new double[] { 0.0, 1.0, 1.0 };
		if (color != null) {
			hsv[0] = color.getHue();
			hsv[1] = color.getSaturation();
			hsv[2] = color.getValue();
		}
		return hsv;
	}
	
	protected class HSVTab extends ToolTab {
		
		
		private final HSVSelector fSelector;
		
		private final DoubleText[] fHSVText = new DoubleText[3];
		
		private int fTextChange;
		
		
		HSVTab() {
			super(HSV, RColorChooser.this, "HSV", "Definition by Hue-Saturation-Value"); //$NON-NLS-1$
			
			final Composite composite = create();
			composite.setLayout(LayoutUtil.createTabGrid(3));
			
			fSelector = new HSVSelector(composite);
			fSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
			
			{	final String[] fieldLabels = new String[] { "&Hue:", "&Saturation:", "&Value:" };
				final IObjValueListener<Double> textListener = new IObjValueListener<Double>() {
					@Override
					public void valueAboutToChange(final ObjValueEvent<Double> event) {
					}
					@Override
					public void valueChanged(final ObjValueEvent<Double> event) {
						if (event.newValue == null) {
							return;
						}
						final HSVColorDef oldValue = fSelector.getValue(0);
						final double[] hsv = safeHSVDoubleArray(oldValue);
						fTextChange++;
						try {
							hsv[indexOf((DoubleText) event.getSource())] = event.newValue;
							final HSVColorDef value = new HSVColorDef((float) hsv[0], (float) hsv[1], (float) hsv[2]);
							if (!value.equals(oldValue)) {
								fSelector.setValue(0, value);
								RColorChooser.this.doSetColorValue(value);
							}
						}
						catch (final NumberFormatException e) {}
						catch (final IllegalArgumentException e) {}
						finally {
							fTextChange--;
						}
						return;
					}
				};
				for (int i = 0; i < 3; i++) {
					final Label label = new Label(composite, SWT.NONE);
					label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
					label.setText(fieldLabels[i]);
					
					final DoubleText text = new DoubleText(composite, SWT.BORDER);
					final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
					gd.widthHint = LayoutUtil.hintWidth(text.getControl(), 6);
					text.getControl().setLayoutData(gd);
					text.addValueListener(textListener);
					
					text.setIncrement(0.001);
					text.setMinMax(0.0, 1.0);
					text.setFormat(DoubleText.createFormat(3));
					
					fHSVText[i] = text;
				}
			}
			{	final Label label = new Label(composite, SWT.NONE);
				final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1);
				label.setLayoutData(gd);
			}
			
			fSelector.addValueListener(new IObjValueListener<ColorDef>() {
				@Override
				public void valueAboutToChange(final ObjValueEvent<ColorDef> event) {
				}
				@Override
				public void valueChanged(final ObjValueEvent<ColorDef> event) {
					if (fTextChange == 0) {
						final HSVColorDef value = (HSVColorDef) event.newValue;
						final Double[] hsv = new Double[3];
						hsv[0] = Double.valueOf(value.getHue());
						hsv[1] = Double.valueOf(value.getSaturation());
						hsv[2] = Double.valueOf(value.getValue());
						for (int i = 0; i < 3; i++) {
							fHSVText[i].setValue(0, hsv[i]);
						}
						RColorChooser.this.doSetColorValue(value);
					}
				}
			});
		}
		
		private int indexOf(final DoubleText source) {
			for (int i = 0; i < 3; i++) {
				if (fHSVText[i] == source) {
					return i;
				}
			}
			return -1;
		}
		
		@Override
		protected void activated() {
			final ColorDef value = doGetColorValue();
			fSelector.setValue(0, value);
		}
	}
	
	
	private ColorDef fInitialValue;
	private Color fInitialSWTColor;
	private ColorDef fCurrentValue;
	private Color fCurrentSWTColor;
	
	private Composite fStatusControl;
	
	
	public RColorChooser() {
	}
	
	
	public void open(final Shell parent, final Rectangle position, final ColorDef initialValue) {
		fInitialValue = initialValue;
		doSetValue((initialValue != null) ? initialValue :
				RGraphicFunctions.DEFAULT.colorsMap.get("black") ); //$NON-NLS-1$
		
		super.open(parent, position);
	}
	
	@Override
	protected void addTabs(final CTabFolder tabFolder) {
		new PaletteTab(R_COLORS, "R Colors", "Definition by R color names", RGraphicFunctions.DEFAULT.colorsList);
		new RGBTab();
		new HSVTab();
		new PaletteTab(R_PALETTE, "R Palette", "Definition by R palette index", RGraphicFunctions.DEFAULT.defaultPalette);
		new RAlphaChooser.AlphaTab(this, "+ Alpha", "Add transparency by Alpha [0, 1]") {
			@Override
			protected void setValue(final Float value) {
				RColorChooser.this.doSetAlphaValue(value);
			}
			@Override
			protected Float getValue() {
				return RColorChooser.this.doGetAlphaValue();
			}
			@Override
			protected ColorDef getBaseColor() {
				return RColorChooser.this.doGetColorValue();
			}
		};
	}
	
	@Override
	protected ToolTab getBestTab() {
		if (fCurrentValue.getType() == "hsv") { //$NON-NLS-1$
			return getTab(HSV);
		}
		else if (fCurrentValue.getType() == COLORS_COLOR_DEF_TYPE) {
			return getTab(R_COLORS);
		}
		else if (fCurrentValue.getType() == PALETTE_COLOR_DEF_TYPE) {
			return getTab(R_PALETTE);
		}
		else {
			return getTab(RGB);
		}
	}
	
	@Override
	protected void addStatusControls(final Composite composite) {
		fStatusControl = new PreviewCanvas(composite) {
			@Override
			protected void drawPreview(final GC gc, final int idx,
					final int x, final int y, final int width, final int height) {
				Color color = null;
				boolean alpha = false;
				switch (idx) {
				case 0:
					if (fInitialValue != null) {
						alpha = (fInitialValue instanceof ColorAlphaDef);
						if (fInitialSWTColor == null) {
							fInitialSWTColor = (alpha) ?
									RAlphaChooser.createPreviewColor(getDisplay(), ((ColorAlphaDef) fInitialValue).getAlpha255(), fInitialValue) :
									createPreviewColor(getDisplay(), fInitialValue);
						}
						color = fInitialSWTColor;
					}
					break;
				case 1:
					if (fCurrentValue != null) {
						alpha = (fCurrentValue instanceof ColorAlphaDef);
						if (fCurrentSWTColor == null) {
							fCurrentSWTColor = (alpha) ?
									RAlphaChooser.createPreviewColor(getDisplay(), ((ColorAlphaDef) fCurrentValue).getAlpha255(), fCurrentValue) :
									createPreviewColor(getDisplay(), fCurrentValue);
						}
						color = fCurrentSWTColor;
					}
					break;
				default:
					break;
				}
				if (alpha) {
					RAlphaChooser.drawPreview(gc, x, y, width, height, color);
				}
				else {
					RColorChooser.drawPreview(gc, x, y, width, height, color);
				}
			}
		};
		updateStatus();
	}
	
	private void doSetColorValue(final ColorDef value) {
		final Float oldAlpha = doGetAlphaValue();
		doSetValue((oldAlpha.floatValue() != 1f) ?
				new ColorAlphaDef(value, oldAlpha.floatValue()) :
				value );
	}
	
	private void doSetAlphaValue(final Float value) {
		final ColorDef oldValue = doGetColorValue();
		doSetValue((value.floatValue() == 1f) ? oldValue :
				new ColorAlphaDef(oldValue, value.floatValue()) );
	}
	
	private void doSetValue(final ColorDef value) {
		if (value == null || value == fCurrentValue) {
			return;
		}
		if (fCurrentSWTColor != null && !value.equals(fCurrentValue)) {
			fCurrentSWTColor.dispose();
			fCurrentSWTColor = null;
		}
		fCurrentValue = value;
		
		if (fStatusControl != null) {
			updateStatus();
		}
	}
	
	protected void updateStatus() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Previous: ");
		sb.append((fInitialValue != null) ? fInitialValue.toString() : "-"); //$NON-NLS-1$
		sb.append("\n");
		sb.append("Current: ");
		sb.append(fCurrentValue.toString());
//		fStatusLabel.setText(info);
		fStatusControl.setToolTipText(sb.toString());
		fStatusControl.redraw();
	}
	
	private ColorDef doGetColorValue() {
		if (fCurrentValue instanceof ColorAlphaDef) {
			return ((ColorAlphaDef) fCurrentValue).getRef();
		}
		return fCurrentValue;
	}
	
	private Float doGetAlphaValue() {
		if (fCurrentValue instanceof ColorAlphaDef) {
			return Float.valueOf(((ColorAlphaDef) fCurrentValue).getAlpha());
		}
		return Float.valueOf(1f);
	}
	
	public ColorDef getValue() {
		return fCurrentValue;
	}
	
	@Override
	protected void onDispose() {
		fStatusControl = null;
		super.onDispose();
		if (fInitialSWTColor != null) {
			fInitialSWTColor.dispose();
			fInitialSWTColor = null;
		}
		if (fCurrentSWTColor != null) {
			fCurrentSWTColor.dispose();
			fCurrentSWTColor = null;
		}
	}
	
}
