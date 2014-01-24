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

package de.walware.statet.rtm.base.ui.rexpr;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.text.StringParseInput;
import de.walware.ecommons.ui.components.ObjValueEvent;

import de.walware.statet.rtm.base.ui.RtModelUIPlugin;
import de.walware.statet.rtm.base.ui.rexpr.RExprWidget.TypeDef;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;

import de.walware.statet.r.core.model.RGraphicFunctions;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.ui.graphics.RFontFamilyChooser;


public class FontFamilyType extends TypeDef implements PaintListener, Listener {
	
	
	private Button fDetail;
	
	private String fCurrentValue;
	private String fCurrentFontName;
	private Font fCurrentSWTFont;
	
	private RFontFamilyChooser fFontChooser;
	
	private final RGraphicFunctions fRGraphicFunctions = RGraphicFunctions.DEFAULT;
	
	
	public FontFamilyType(final RExprTypeUIAdapter type) {
		super(type);
	}
	
	
	@Override
	public boolean hasDetail() {
		return true;
	}
	
	@Override
	protected Control createDetailControl(final Composite parent) {
		fDetail = new Button(parent, SWT.NONE);
		fDetail.addPaintListener(this);
		fDetail.addListener(SWT.Selection, this);
		fDetail.addListener(SWT.Dispose, this);
		
		return fDetail;
	}
	
	
	@Override
	public void valueAboutToChange(final ObjValueEvent<RTypedExpr> event) {
		final RTypedExpr newExpr = event.newValue;
		String newValue = null;
		try {
			if (newExpr != null && newExpr.getTypeKey() == RTypedExpr.R) {
				final RScanner scanner = new RScanner(new StringParseInput(newExpr.getExpr()),
						AstInfo.LEVEL_MODEL_DEFAULT );
				newValue = fRGraphicFunctions.parseFontFamily(scanner.scanExpr());
			}
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RtModelUIPlugin.PLUGIN_ID,
					"An error occurred when parsing the R font family value expression.", e ));
		}
		
		doSetValue(newValue);
		
		fDetail.redraw();
	}
	
	
	@Override
	public void paintControl(final PaintEvent e) {
		final GC gc = e.gc;
		final Point size = fDetail.getSize();
		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY));
		gc.drawRectangle(4, 4, size.x - 9, size.y - 9);
		if (fCurrentFontName != null && !fCurrentFontName.isEmpty()) {
			if (fCurrentSWTFont == null) {
				fCurrentSWTFont = RFontFamilyChooser.createFont(fDetail.getDisplay(), fCurrentFontName,
						fDetail.getFont().getFontData()[0].getHeight() );
				if (fCurrentSWTFont == null) {
					fCurrentFontName = ""; //$NON-NLS-1$
				}
			}
			RFontFamilyChooser.drawPreview(gc, 5, 5, size.x - 11, size.y - 11, fCurrentSWTFont);
		}
	}
	
	@Override
	public void handleEvent(final Event event) {
		switch (event.type) {
		case SWT.Selection:
			showColorChooser();
			return;
		case SWT.Dispose:
			if (fFontChooser != null) {
				fFontChooser.dispose();
			}
			if (fCurrentSWTFont != null) {
				fCurrentSWTFont.dispose();
				fCurrentSWTFont = null;
			}
			return;
		default:
			break;
		}
	}
	
	private void showColorChooser() {
		if (fFontChooser == null) {
			fFontChooser = new RFontFamilyChooser() {
				@Override
				protected void onOK() {
					FontFamilyType.this.setValue(getValue());
				}
			};
		}
		if (fFontChooser.isActive()) {
			fFontChooser.close();
			return;
		}
		final Rectangle bounds = fDetail.getBounds();
		{	final Point location = fDetail.getParent().toDisplay(new Point(bounds.x, bounds.y));
			bounds.x = location.x;
			bounds.y = location.y;
		}
		fFontChooser.open(fDetail.getShell(), bounds, fCurrentValue);
	}
	
	private void setValue(final String value) {
		if (value == null) {
			setExpr(""); //$NON-NLS-1$
			return;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append('"');
		sb.append(value);
		sb.append('"');
		
		doSetValue(value);
		
		fDetail.redraw();
		setExpr(sb.toString());
	}
	
	private void doSetValue(final String value) {
		final String fontName = RFontFamilyChooser.getFontName(value);
		if (fCurrentSWTFont != null && !fCurrentFontName.equals(fontName)) {
			fCurrentSWTFont.dispose();
			fCurrentSWTFont = null;
		}
		fCurrentValue = value;
		fCurrentFontName = fontName;
	}
	
}
