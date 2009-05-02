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
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ui.util.InformationDispatchHandler;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.base.ui.StatetImages;

import de.walware.rj.data.RList;

import de.walware.statet.r.core.data.ICombinedRElement;


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
	
	
	private static final String INFO_FONT = "org.eclipse.debug.ui.DetailPaneFont"; // E-3.5 -> IDebugUIConstants
	
	
	private static Point gScrollbarSize;
	
	
	private CombinedLabelProvider fLabelProvider;
	
	private int fMode;
	
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
		fMode = 1;
		
		JFaceResources.getFontRegistry().addListener(this);
		create();
	}
	
	public RElementInfoControl(final Shell shell, final boolean rich) {
		super(shell, rich);
		fMode = 2;
		
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
		fContentComposite.setBackground(parent.getBackground());
		
		final GridLayout gridLayout = LayoutUtil.applyCompositeDefaults(new GridLayout(), 2);
		gridLayout.horizontalSpacing = (int) (((double) gridLayout.horizontalSpacing) / 1.5);
		fContentComposite.setLayout(gridLayout);
		
		final int vIndent = Math.max(1, LayoutUtil.defaultVSpacing() / 4);
		final int hIndent = Math.max(2, LayoutUtil.defaultHSpacing() / 3);
		
		{	// Title image
			fTitleImage = new Label(fContentComposite, SWT.NULL);
			fTitleImage.setForeground(fContentComposite.getForeground());
			fTitleImage.setBackground(fContentComposite.getBackground());
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
			fTitleText.setForeground(fContentComposite.getForeground());
			fTitleText.setBackground(fContentComposite.getBackground());
			
			fTitleText.setFont(JFaceResources.getDialogFont());
			final GC gc = new GC(fTitleText);
			final FontMetrics fontMetrics = gc.getFontMetrics();
			final GridData imageGd = new GridData(SWT.FILL, SWT.TOP, false, false);
			imageGd.horizontalIndent = hIndent;
			final int textHeight = fontMetrics.getAscent() + fontMetrics.getLeading();
			final int imageHeight = image.getBounds().height;
			final int shift = Math.max(3, (int) (((double) fontMetrics.getDescent()) / 1.5));
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
		
		int style = SWT.MULTI | SWT.READ_ONLY;
		if (fMode == 2) {
			style |= SWT.V_SCROLL | SWT.H_SCROLL;
		}
		fInfoText = new StyledText(fContentComposite, style);
		fInfoText.setForeground(fContentComposite.getForeground());
		fInfoText.setBackground(fContentComposite.getBackground());
		fInfoText.setIndent(hIndent);
		fInfoText.setFont(JFaceResources.getFont(INFO_FONT));
		
		if (gScrollbarSize == null) {
			computeScrollbarSize(fContentComposite);
		}
		
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
//		gd.widthHint = LayoutUtil.hintWidth(fInfoText, INFO_FONT, 50);
		fInfoText.setLayoutData(gd);
		updateInput();
	}
	
	private void computeScrollbarSize(final Composite parent) {
		final StyledText test = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		try {
			if (test instanceof Scrollable) {
				final Scrollable scroll = (Scrollable) test;
				final ScrollBar horizontalBar = scroll.getHorizontalBar();
				final ScrollBar verticalBar = scroll.getVerticalBar();
				if (horizontalBar != null && verticalBar != null) {
					gScrollbarSize = new Point(verticalBar.getSize().x, horizontalBar.getSize().y);
					return;
				}
			}
			gScrollbarSize = new Point(0, 0);
			return;
		}
		finally {
			test.dispose();
		}
	}
	
	@Override
	public Rectangle computeTrim() {
		final Rectangle trim = super.computeTrim();
		if (fMode == 2) {
			trim.width += gScrollbarSize.x;
			trim.height += gScrollbarSize.y;
		}
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
		final int widthMax2 = LayoutUtil.hintWidth(fInfoText, INFO_FONT, 80);
		final int widthMax = ((sizeConstraints != null && sizeConstraints.x != SWT.DEFAULT) ?
				sizeConstraints.x : widthMax2) - trim.x;
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
		if (widthHint < titleHint && widthHint + gScrollbarSize.x >= titleHint) {
			widthHint = titleHint;
		}
		
		final int heightMax = ((sizeConstraints != null && sizeConstraints.y != SWT.DEFAULT) ?
				sizeConstraints.y :
				fInfoText.getLineHeight()*12) - trim.height;
		
		final Point size = fContentComposite.computeSize(widthHint, SWT.DEFAULT, true);
		size.y += LayoutUtil.defaultVSpacing() + fInfoText.getLineHeight();
		size.x = Math.max(Math.min(size.x, widthMax), 200);
		size.y = Math.max(Math.min(size.y, heightMax), 100);
		return size;
	}
	
	@Override
	public Point computeSizeConstraints(final int widthInChars, final int heightInChars) {
		final GC gc= new GC(fContentComposite);
		gc.setFont(JFaceResources.getDialogFont());
		final int titleWidth = gc.getFontMetrics().getAverageCharWidth() * widthInChars;
		final int titleHeight = fTitleText.getLineHeight();
		gc.dispose();
		final int infoWidth = LayoutUtil.hintWidth(fInfoText, INFO_FONT, widthInChars);
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
			fLabelProvider = new CombinedLabelProvider(true, true);
		}
		if (fInput != null) {
			final Image image = fLabelProvider.getImage(fInput.element);
			fTitleImage.setImage((image != null) ? image : StatetImages.getImage(StatetImages.OBJ_PLACEHOLDER));
			final StyledString styleString = fLabelProvider.getStyleString(fInput.element, fInput.elementAttr);
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
		if (fMode == 1) {
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
		if (property.equals(INFO_FONT) || property.equals(JFaceResources.DEFAULT_FONT)) {
			dispose();
		}
	}
	
	@Override
	public void dispose() {
		JFaceResources.getFontRegistry().removeListener(this);
		super.dispose();
	}
	
}