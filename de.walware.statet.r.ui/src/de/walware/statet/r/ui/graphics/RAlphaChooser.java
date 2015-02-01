/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.graphics;

import com.ibm.icu.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.graphics.core.ColorAlphaDef;
import de.walware.ecommons.graphics.core.ColorDef;
import de.walware.ecommons.ui.components.AlphaSelector;
import de.walware.ecommons.ui.components.DoubleText;
import de.walware.ecommons.ui.components.IObjValueListener;
import de.walware.ecommons.ui.components.ObjValueEvent;
import de.walware.ecommons.ui.dialogs.ToolPopup;
import de.walware.ecommons.ui.util.LayoutUtil;


public class RAlphaChooser extends ToolPopup {
	
	
	private static final String ALPHA = "Alpha"; //$NON-NLS-1$
	
	
	public static void drawPreview(final GC gc,
			final int x, final int y, final int width, final int height,
			final Color color ) {
		if (color != null) {
			gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(x, y, width + 1, height + 1);
			gc.setBackground(color);
			final int b = 1 + height / 4;
			gc.fillRectangle(x, y + b, width + 1, height + 1 - 2 * b);
		}
	}
	
	public static Color createPreviewColor(final Display display, final ColorAlphaDef baseColor) {
		return createPreviewColor(display, baseColor.getAlpha255(), baseColor);
	}
	
	public static Color createPreviewColor(final Display display, final int alpha255, final ColorDef baseColor) {
		if (baseColor == null) {
			return new Color(display, alpha255, alpha255, alpha255);
		}
		final int white = 255 - alpha255;
		return new Color(display, 
				white + (baseColor.getRed() * alpha255) / 255,
				white + (baseColor.getGreen() * alpha255) / 255,
				white + (baseColor.getBlue() * alpha255) / 255 );
	}
	
	
	protected static abstract class AlphaTab extends ToolTab {
		
		
		private final AlphaSelector fSelector;
		
		private DoubleText fText;
		
		private int fTextChange;
		
		private final DecimalFormat fFormat = DoubleText.createFormat(3);
		
		
		AlphaTab(final ToolPopup parent) {
			this(parent, "Alpha", "Definition by Alpha [0, 1]"); //$NON-NLS-1$
		}
		
		AlphaTab(final ToolPopup parent, final String name, final String tooltip) {
			super(ALPHA, parent, name, tooltip);
			
			final Composite composite = create();
			composite.setLayout(LayoutUtil.createTabGrid(3));
			
			fSelector = new AlphaSelector(composite);
			fSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
			
			{	final IObjValueListener<Double> textListener = new IObjValueListener<Double>() {
					@Override
					public void valueAboutToChange(final ObjValueEvent<Double> event) {
					}
					@Override
					public void valueChanged(final ObjValueEvent<Double> event) {
						if (event.newValue == null) {
							return;
						}
						final Float oldValue = fSelector.getValue(0);
						fTextChange++;
						try {
							final Float value = Float.valueOf(event.newValue.floatValue());
							if (!value.equals(oldValue)) {
								fSelector.setValue(0, value);
								setValue(value);
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
				{	final Label label = new Label(composite, SWT.NONE);
					label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
					label.setText("     &Alpha (opaque):");
					
					final DoubleText text = new DoubleText(composite, SWT.BORDER);
					final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
					gd.widthHint = LayoutUtil.hintWidth(text.getControl(), 6);
					text.getControl().setLayoutData(gd);
					text.addValueListener(textListener);
					
					text.setIncrement(0.001);
					text.setMinMax(0.0, 1.0);
					text.setFormat(fFormat);
					
					fText = text;
				}
			}
			{	final Label label = new Label(composite, SWT.NONE);
				final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1);
				label.setLayoutData(gd);
			}
			
			fSelector.addValueListener(new IObjValueListener<Float>() {
				@Override
				public void valueAboutToChange(final ObjValueEvent<Float> event) {
				}
				@Override
				public void valueChanged(final ObjValueEvent<Float> event) {
					if (fTextChange == 0) {
						final Float value = event.newValue;
						fText.setValue(0, Double.valueOf(value.doubleValue()));
						setValue(value);
					}
				}
			});
		}
		
		public DecimalFormat getFormat() {
			return fFormat;
		}
		
		@Override
		protected void activated() {
			fSelector.setBaseColor(getBaseColor());
			final Float value = getValue();
			fSelector.setValue(0, value);
		}
		
		protected abstract Float getValue();
		
		protected abstract void setValue(Float value);
		
		protected ColorDef getBaseColor() {
			return null;
		}
		
	}
	
	
	private Float fInitialValue;
	private int fInitial255 = -1;
	private Color fInitialSWTColor;
	private Float fCurrentValue;
	private int fCurrent255 = -1;
	private Color fCurrentSWTColor;
	
	private ColorDef fBaseColor;
	
	private Composite fStatusControl;
	
	
	public RAlphaChooser() {
	}
	
	
	public void open(final Shell parent, final Rectangle position, final Float initialValue) {
		fInitialValue = initialValue;
		if (initialValue != null) {
			fInitial255 = 255 - Math.round(initialValue * 255);
		}
		doSetValue((initialValue != null) ? initialValue : Float.valueOf(1f));
		
		super.open(parent, position);
	}
	
	@Override
	protected void addTabs(final CTabFolder tabFolder) {
		new AlphaTab(this) {
			@Override
			protected Float getValue() {
				return RAlphaChooser.this.getValue();
			}
			@Override
			protected void setValue(final Float value) {
				RAlphaChooser.this.doSetValue(value);
			}
		};
	}
	
	@Override
	protected ToolTab getBestTab() {
		return getTab(ALPHA);
	}
	
	@Override
	protected void addStatusControls(final Composite composite) {
		fStatusControl = new PreviewCanvas(composite) {
			@Override
			protected void drawPreview(final GC gc, final int idx,
					final int x, final int y, final int width, final int height) {
				Color color = null;
				switch (idx) {
				case 0:
					if (fInitialValue != null) {
						if (fInitialSWTColor == null) {
							fInitialSWTColor = createPreviewColor(getDisplay(), fInitial255, fBaseColor);
						}
						color = fInitialSWTColor;
					}
					break;
				case 1:
					if (fCurrentValue != null) {
						if (fCurrentSWTColor == null) {
							fCurrentSWTColor = createPreviewColor(getDisplay(), fCurrent255, fBaseColor);
						}
						color = fCurrentSWTColor;
					}
					break;
				default:
					break;
				}
				RAlphaChooser.drawPreview(gc, x, y, width, height, color);
			}
		};
		updateStatus();
	}
	
	protected void doSetValue(final Float value) {
		if (value == null || value.equals(fCurrentValue)) {
			return;
		}
		final int v255 = 255 - Math.round(value * 255);
		if (fCurrentSWTColor != null && v255 != fCurrent255) {
			fCurrentSWTColor.dispose();
			fCurrentSWTColor = null;
		}
		fCurrentValue = value;
		fCurrent255 = v255;
		
		if (fStatusControl != null) {
			updateStatus();
		}
	}
	
	protected void updateStatus() {
		final StringBuilder sb = new StringBuilder();
		final DecimalFormat format = ((AlphaTab) getTab(ALPHA)).getFormat();
		sb.append("Previous: ");
		sb.append((fInitialValue != null) ? format.format(fInitialValue) : "-"); //$NON-NLS-1$
		sb.append("\n");
		sb.append("Current: ");
		sb.append(format.format(fCurrentValue));
//		fStatusLabel.setText(info);
		fStatusControl.setToolTipText(sb.toString());
		fStatusControl.redraw();
	}
	
	public Float getValue() {
		return fCurrentValue;
	}
	
	@Override
	protected void onDispose() {
		fStatusControl = null;
		super.onDispose();
		if (fInitialSWTColor != null) {
			fInitialSWTColor.dispose();
			fInitial255 = -1;
			fInitialSWTColor = null;
		}
		if (fCurrentSWTColor != null) {
			fCurrentSWTColor.dispose();
			fCurrent255 = -1;
			fCurrentSWTColor = null;
		}
	}
	
}
