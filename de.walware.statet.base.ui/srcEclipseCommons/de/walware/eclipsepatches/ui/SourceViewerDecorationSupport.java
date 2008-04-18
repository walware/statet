/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsepatches.ui;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.texteditor.AnnotationPreference;


// Uses patch AnnotationPainter
public class SourceViewerDecorationSupport extends org.eclipse.ui.texteditor.SourceViewerDecorationSupport {
	
	
	/**
	 * Underline drawing strategy.
	 *
	 * @since 3.0
	 */
	private static final class UnderlineDrawingStrategy implements IDrawingStrategy {
		
		public void draw(final Annotation annotation, final GC gc, final StyledText textWidget, final int offset, final int length, final Color color) {
			if (gc != null) {
				Rectangle bounds;
				if (length > 0)
					bounds= textWidget.getTextBounds(offset, offset + length - 1);
				else {
					final Point loc= textWidget.getLocationAtOffset(offset);
					bounds= new Rectangle(loc.x, loc.y, 1, textWidget.getLineHeight(offset));
				}
				
				final int y= bounds.y + bounds.height - 1;
				
				gc.setForeground(color);
				gc.drawLine(bounds.x, y, bounds.x + bounds.width, y);
				
			} else {
				textWidget.redrawRange(offset, length, true);
			}
		}
	}
	
	/**
	 * Draws a box around a given range.
	 *
	 * @since 3.0
	 */
	private static class BoxDrawingStrategy implements IDrawingStrategy {
		
		public void draw(final Annotation annotation, final GC gc, final StyledText textWidget, final int offset, final int length, final Color color) {
			if (length == 0) {
				fgIBeamStrategy.draw(annotation, gc, textWidget, offset, length, color);
				return;
			}
			
			if (gc != null) {
				Rectangle bounds;
				if (length > 0)
					bounds= textWidget.getTextBounds(offset, offset + length - 1);
				else {
					final Point loc= textWidget.getLocationAtOffset(offset);
					bounds= new Rectangle(loc.x, loc.y, 1, textWidget.getLineHeight(offset));
				}
				
				drawBox(gc, textWidget, color, bounds);
				
			} else {
				textWidget.redrawRange(offset, length, true);
			}
		}
		
		protected void drawBox(final GC gc, final StyledText textWidget, final Color color, final Rectangle bounds) {
			gc.setForeground(color);
			gc.drawRectangle(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
		}
	}
	
	/**
	 * Dashed box drawing strategy.
	 *
	 * @since 3.3
	 */
	private static final class DashedBoxDrawingStrategy extends BoxDrawingStrategy {
		
		@Override
		protected void drawBox(final GC gc, final StyledText textWidget, final Color color, final Rectangle bounds) {
			//clean bg:
			gc.setForeground(textWidget.getBackground());
			gc.setLineStyle(SWT.LINE_SOLID);
			final int x= bounds.x;
			final int y= bounds.y;
			final int w= bounds.width - 1;
			final int h= bounds.height - 1;
			gc.drawRectangle(x, y, w, h);
			
			gc.setForeground(color);
			gc.setLineDash(new int[] { 3 });
			
			// gc.drawRectangle(x, y, w, h) is platform-dependent and can look "animated"
			gc.drawLine(x, y, x + w, y);
			gc.drawLine(x, y + h, x + w, y + h);
			gc.drawLine(x, y, x, y + h);
			gc.drawLine(x + w, y, x + w, y + h);
			
			//RESET (same GC is passed around!):
			gc.setLineStyle(SWT.LINE_SOLID);
		}
	}
	
	/**
	 * Draws an iBeam at the given offset, the length is ignored.
	 *
	 * @since 3.0
	 */
	private static final class IBeamStrategy implements IDrawingStrategy {
		
		public void draw(final Annotation annotation, final GC gc, final StyledText textWidget, final int offset, int length, final Color color) {
			if (gc != null) {
				final Point left= textWidget.getLocationAtOffset(offset);
				final int x1= left.x;
				final int y1= left.y;
				
				gc.setForeground(color);
				gc.drawLine(x1, y1, x1, left.y + textWidget.getLineHeight(offset) - 1);
				
			} else {
				/*
				 * The length for IBeam's is always 0, which causes no redraw to occur in
				 * StyledText#redraw(int, int, boolean). We try to normally redraw at length of one,
				 * and up to the line start of the next line if offset is at the end of line. If at
				 * the end of the document, we redraw the entire document as the offset is behind
				 * any content.
				 */
				final int contentLength= textWidget.getCharCount();
				if (offset >= contentLength) {
					textWidget.redraw();
					return;
				}
				
				final char ch= textWidget.getTextRange(offset, 1).charAt(0);
				if (ch == '\r' || ch == '\n') {
					// at the end of a line, redraw up to the next line start
					final int nextLine= textWidget.getLineAtOffset(offset) + 1;
					if (nextLine >= textWidget.getLineCount()) {
						/*
						 * Panic code: should not happen, as offset is not the last offset,
						 * and there is a delimiter character at offset.
						 */
						textWidget.redraw();
						return;
					}
					
					final int nextLineOffset= textWidget.getOffsetAtLine(nextLine);
					length= nextLineOffset - offset;
				} else {
					length= 1;
				}
				
				textWidget.redrawRange(offset, length, true);
			}
		}
	}
	
	/**
	 * The box drawing strategy.
	 * @since 3.0
	 */
	private static IDrawingStrategy fgBoxStrategy= new BoxDrawingStrategy();
	
	/**
	 * The dashed box drawing strategy.
	 * @since 3.3
	 */
	private static IDrawingStrategy fgDashedBoxStrategy= new DashedBoxDrawingStrategy();
	
	/**
	 * The null drawing strategy.
	 * @since 3.0
	 */
	private static IDrawingStrategy fgNullStrategy= new AnnotationPainter.NullStrategy();
	
	/**
	 * The underline drawing strategy.
	 * @since 3.0
	 */
	private static IDrawingStrategy fgUnderlineStrategy= new UnderlineDrawingStrategy();
	
	/**
	 * The iBeam drawing strategy.
	 * @since 3.0
	 */
	private static IDrawingStrategy fgIBeamStrategy= new IBeamStrategy();
	
	/**
	 * The squiggles drawing strategy.
	 * @since 3.0
	 */
	private static IDrawingStrategy fgSquigglesStrategy= new AnnotationPainter.SquigglesStrategy();
	
	
	/** The viewer */
	private ISourceViewer fSourceViewer;
	/** The annotation access */
	private IAnnotationAccess fAnnotationAccess;
	
	
	/**
	 * Creates a new decoration support for the given viewer.
	 *
	 * @param sourceViewer the source viewer
	 * @param overviewRuler the viewer's overview ruler
	 * @param annotationAccess the annotation access
	 * @param sharedTextColors the shared text color manager
	 */
	public SourceViewerDecorationSupport(final ISourceViewer sourceViewer, final IOverviewRuler overviewRuler, final IAnnotationAccess annotationAccess, final ISharedTextColors sharedTextColors) {
		super(sourceViewer, overviewRuler, annotationAccess, sharedTextColors);
		fSourceViewer= sourceViewer;
		fAnnotationAccess= annotationAccess;
	}
	
	@Override
	protected AnnotationPainter createAnnotationPainter() {
		final AnnotationPainter painter= new de.walware.eclipsepatches.ui.AnnotationPainter(fSourceViewer, fAnnotationAccess);
		
		painter.addDrawingStrategy(AnnotationPreference.STYLE_BOX, fgBoxStrategy);
		painter.addDrawingStrategy(AnnotationPreference.STYLE_DASHED_BOX, fgDashedBoxStrategy);
		painter.addDrawingStrategy(AnnotationPreference.STYLE_NONE, fgNullStrategy);
		painter.addDrawingStrategy(AnnotationPreference.STYLE_SQUIGGLES, fgSquigglesStrategy);
		painter.addDrawingStrategy(AnnotationPreference.STYLE_UNDERLINE, fgUnderlineStrategy);
		painter.addDrawingStrategy(AnnotationPreference.STYLE_IBEAM, fgIBeamStrategy);
		
		return painter;
	}
	
}
