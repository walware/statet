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

package de.walware.statet.r.internal.debug.ui;

import static org.eclipse.debug.ui.IDebugUIConstants.PREF_DETAIL_PANE_FONT;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ui.util.InformationDispatchHandler;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.base.ui.StatetImages;

import de.walware.rj.data.RList;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.ui.RLabelProvider;


public class RElementInfoHoverCreator extends AbstractReusableInformationControlCreator {
	
	
	public static class Data {
		
		final Control control;
		
		final ICombinedRElement element;
		final RList elementAttr;
		final String detailTitle;
		final String detailInfo;
		
		public Data(final Control control, final ICombinedRElement element, final RList elementAttr,
				final String detailTitle, final String detailInfo) {
			this.control = control;
			this.element = element;
			this.elementAttr = elementAttr;
			this.detailTitle = detailTitle;
			this.detailInfo = detailInfo;
		}
		
	}
	
	
	private boolean fEnrich;
	
	
	public RElementInfoHoverCreator() {
	}
	
	RElementInfoHoverCreator(final boolean enrich) {
		fEnrich = enrich;
	}
	
	
	@Override
	protected IInformationControl doCreateInformationControl(final Shell parent) {
		return (fEnrich) ? 
				new RElementInfoControl(parent, true) :
				new RElementInfoControl(parent, "");
	}
	
}

class RElementInfoControl extends AbstractInformationControl implements IInformationControlExtension2, IPropertyChangeListener {
	
	
	private static final int MODE_SIMPLE = 1;
	private static final int MODE_FOCUS = 2;
	
	
	private final int fMode;
	
	private RLabelProvider fLabelProvider;
	
	private Composite fContentComposite;
	private Label fTitleImage;
	private StyledText fTitleText;
	private StyledText fInfoText;
	
	private boolean fLayoutWorkaround;
	private boolean fLayoutHint;
	
	private RElementInfoHoverCreator.Data fInput;
	private boolean fInputChanged;
	
	
	public RElementInfoControl(final Shell shell, final String message) {
		super(shell, message);
		fMode = MODE_SIMPLE;
		
		JFaceResources.getFontRegistry().addListener(this);
		create();
	}
	
	public RElementInfoControl(final Shell shell, final boolean rich) {
		super(shell, rich);
		fMode = MODE_FOCUS;
		
		create();
	}
	
	
	public void setInput(final Object input) {
		fInputChanged = true;
		if (input instanceof RElementInfoHoverCreator.Data) {
			fInput = (RElementInfoHoverCreator.Data) input;
		}
		else {
			fInput = null;
		}
	}
	
	public boolean hasContents() {
		return (fInput != null);
	}
	
	@Override
	protected void createContent(final Composite parent) {
		fContentComposite = new Composite(parent, SWT.NONE) {
			@Override
			public Point computeSize(final int width, final int height, final boolean changed) {
				return super.computeSize(width, height, changed || width != getSize().x);
			}
		};
		fContentComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		final GridLayout gridLayout = LayoutUtil.applyCompositeDefaults(new GridLayout(), 2);
		gridLayout.horizontalSpacing = (int) ((gridLayout.horizontalSpacing) / 1.5);
		fContentComposite.setLayout(gridLayout);
		
		final int vIndent = Math.max(1, LayoutUtil.defaultVSpacing() / 4);
		final int hIndent = Math.max(2, LayoutUtil.defaultHSpacing() / 3);
		
		{	// Title image
			fTitleImage = new Label(fContentComposite, SWT.NULL);
			final Image image = StatetImages.getImage(StatetImages.OBJ_PLACEHOLDER);
			fTitleImage.setImage(image);
			
			final GridData textGd = new GridData(SWT.FILL, SWT.TOP, false, false);
			fTitleText = new StyledText(fContentComposite, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP) {
				@Override
				public Point computeSize(int width, final int height, final boolean changed) {
					if (!fLayoutHint && width <= 0 && fContentComposite.getSize().x > 0) {
						width = fContentComposite.getSize().x -
								LayoutUtil.defaultHMargin() - fTitleImage.getSize().x - LayoutUtil.defaultHSpacing() - 10;
					}
					
					final Point size = super.computeSize(width, -1, true);
//					if (width >= 0) {
//						size.x = Math.min(size.x, width);
//					}
					return size;
				}
			};
			
			fTitleText.setFont(JFaceResources.getDialogFont());
			final GC gc = new GC(fTitleText);
			final FontMetrics fontMetrics = gc.getFontMetrics();
			final GridData imageGd = new GridData(SWT.FILL, SWT.TOP, false, false);
			imageGd.horizontalIndent = hIndent;
			final int textHeight = fontMetrics.getAscent() + fontMetrics.getLeading();
			final int imageHeight = image.getBounds().height;
			final int shift = Math.max(3, (int) ((fontMetrics.getDescent()) / 1.5));
			if (textHeight+shift < imageHeight) {
				imageGd.verticalIndent = vIndent+shift;
				textGd.verticalIndent = vIndent+(imageHeight-textHeight);
			}
			else {
				imageGd.verticalIndent = vIndent+(textHeight-imageHeight)+shift;
				textGd.verticalIndent = vIndent;
			}
			fTitleImage.setLayoutData(imageGd);
			fTitleText.setLayoutData(textGd);
			fLayoutWorkaround = true;
			
			gc.dispose();
		}
		
		fInfoText = new StyledText(fContentComposite, fMode == MODE_FOCUS ?
				(SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL) : (SWT.MULTI | SWT.READ_ONLY));
		fInfoText.setIndent(hIndent);
		fInfoText.setFont(JFaceResources.getFont(PREF_DETAIL_PANE_FONT));
		
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
//		gd.widthHint = LayoutUtil.hintWidth(fInfoText, INFO_FONT, 50);
		fInfoText.setLayoutData(gd);
		
		setBackgroundColor(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		setForegroundColor(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		
		updateInput();
	}
	
	@Override
	public void setBackgroundColor(final Color background) {
		super.setBackgroundColor(background);
		fContentComposite.setBackground(background);
	}
	
	@Override
	public void setForegroundColor(final Color foreground) {
		super.setForegroundColor(foreground);
		fContentComposite.setForeground(foreground);
		fTitleText.setForeground(foreground);
		fInfoText.setForeground(foreground);
	}
	
	
	@Override
	public Rectangle computeTrim() {
		final Rectangle trim = super.computeTrim();
		
		final Rectangle textTrim = fInfoText.computeTrim(0, 0, 0, 0);
		trim.x += textTrim.x;
		trim.y += textTrim.y;
		trim.width += textTrim.width;
		trim.height += textTrim.height;
		
		return trim;
	}
	
	@Override
	public Point computeSizeHint() {
		updateInput();
		final Point sizeConstraints = getSizeConstraints();
		final Rectangle trim = computeTrim();
		
//		int charWidth = 20;
//		if (fInput.detailInfo != null) {
//			final int count = Math.min(6, fInfoText.getLineCount());
//			for (int i = 0; i < count; i++) {
//				charWidth = Math.max(charWidth, fInfoText.getLine(i).length());
//			}
//		}
		int widthHint = fInfoText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x + LayoutUtil.defaultHSpacing();
		final int widthMax2 = LayoutUtil.hintWidth(fInfoText, PREF_DETAIL_PANE_FONT, 80);
		final int widthMax = ((sizeConstraints != null && sizeConstraints.x != SWT.DEFAULT) ?
				sizeConstraints.x : widthMax2) - trim.width;
		fLayoutHint = true;
		final int titleHint = LayoutUtil.defaultHMargin() + fTitleImage.getSize().x + LayoutUtil.defaultHSpacing() + fTitleText.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		fLayoutHint = false;
		if (titleHint > widthHint && widthMax2 > widthHint) {
			widthHint = Math.min(widthMax2, titleHint);
		}
		if (widthMax < widthHint) {
			widthHint = widthMax;
		}
		// avoid change of wrapping caused by scrollbar
		if (widthHint < titleHint && widthHint + fInfoText.computeTrim(0, 0, 0, 0).width >= titleHint) {
			widthHint = titleHint;
		}
		
		final int heightMax = ((sizeConstraints != null && sizeConstraints.y != SWT.DEFAULT) ?
				sizeConstraints.y :
				fInfoText.getLineHeight()*12) - trim.height;
		
		final Point size = fContentComposite.computeSize(widthHint, SWT.DEFAULT, true);
		size.y += LayoutUtil.defaultVSpacing();
		size.x = Math.max(Math.min(size.x, widthMax), 200) + trim.width;
		size.y = Math.max(Math.min(size.y, heightMax), 100) + trim.height;
		return size;
	}
	
	@Override
	public Point computeSizeConstraints(final int widthInChars, final int heightInChars) {
		final GC gc= new GC(fContentComposite);
		gc.setFont(JFaceResources.getDialogFont());
		final int titleWidth = gc.getFontMetrics().getAverageCharWidth() * widthInChars;
		final int titleHeight = fTitleText.getLineHeight();
		gc.dispose();
		
		final int infoWidth = LayoutUtil.hintWidth(fInfoText, PREF_DETAIL_PANE_FONT, widthInChars);
		final int infoHeight = fInfoText.getLineHeight() * (heightInChars);
		
		return new Point(Math.max(titleWidth, infoWidth), titleHeight + LayoutUtil.defaultVSpacing() + infoHeight);
	}
	
	@Override
	public void setVisible(final boolean visible) {
		if (visible) {
			updateInput();
			
			if (fLayoutWorkaround) {
				fContentComposite.layout(true, true);
				fLayoutWorkaround = false;
			}
		}
		super.setVisible(visible);
	}
	
	@Override
	public void setFocus() {
		fInfoText.setFocus();
	}
	
	private void updateInput() {
		if (fInfoText == null || !fInputChanged) {
			return;
		}
		if (fLabelProvider == null) {
			fLabelProvider = new RLabelProvider(RLabelProvider.LONG | RLabelProvider.TITLE | RLabelProvider.NAMESPACE);
		}
		if (fInput != null) {
			final Image image = fLabelProvider.getImage(fInput.element);
			fTitleImage.setImage((image != null) ? image : StatetImages.getImage(StatetImages.OBJ_PLACEHOLDER));
			final StyledString styleString = fLabelProvider.getStyledText(fInput.element, fInput.element.getElementName(), fInput.elementAttr);
			fTitleText.setText(styleString.getString());
			fTitleText.setStyleRanges(styleString.getStyleRanges());
			if (fInput.detailTitle != null) {
				fInfoText.setText(fInput.detailTitle + '\n' + ((fInput.detailInfo != null) ? fInput.detailInfo : "")); //$NON-NLS-1$
				final StyleRange title = new StyleRange(0, fInput.detailTitle.length(), null, null);
				title.underline = true;
				fInfoText.setStyleRange(title);
			}
			else {
				fInfoText.setText(""); //$NON-NLS-1$
			}
		}
		else {
			fTitleImage.setImage(StatetImages.getImage(StatetImages.OBJ_PLACEHOLDER));
			fTitleText.setText(""); //$NON-NLS-1$
			fInfoText.setText(""); //$NON-NLS-1$
		}
		if (fMode == MODE_SIMPLE) {
			setStatusText((fInput.control != null && fInput.control.isFocusControl()) ?
					InformationDispatchHandler.getTooltipAffordanceString() : ""); //$NON-NLS-1$
		}
		fInputChanged = false;
	}
	
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		// enriched mode
		return new RElementInfoHoverCreator(true);
	}
	
	public void propertyChange(final PropertyChangeEvent event) {
		final String property = event.getProperty();
		if (property.equals(PREF_DETAIL_PANE_FONT) || property.equals(JFaceResources.DEFAULT_FONT)) {
			dispose();
		}
	}
	
	@Override
	public void dispose() {
		JFaceResources.getFontRegistry().removeListener(this);
		super.dispose();
	}
	
}