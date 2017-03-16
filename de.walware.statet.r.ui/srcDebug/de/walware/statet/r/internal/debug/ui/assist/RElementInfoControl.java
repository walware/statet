/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.assist;

import static de.walware.ecommons.ltk.ui.sourceediting.assist.IInfoHover.MODE_FOCUS;
import static org.eclipse.debug.ui.IDebugUIConstants.PREF_DETAIL_PANE_FONT;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractInformationControl;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.SimpleContributionItem;
import de.walware.ecommons.ui.util.InformationDispatchHandler;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.dataeditor.RDataEditor;
import de.walware.statet.r.ui.dataeditor.RLiveDataEditorInput;
import de.walware.statet.r.ui.rtool.RElementInfoHoverCreator;
import de.walware.statet.r.ui.rtool.RElementInfoTask.RElementInfoData;


public class RElementInfoControl extends AbstractInformationControl implements IInformationControlExtension2,
		IPropertyChangeListener {
	
	
	private class OpenInEditorItem extends SimpleContributionItem {
		
		
		public OpenInEditorItem() {
			super(PlatformUI.getWorkbench().getEditorRegistry().findEditor(RDataEditor.RDATA_EDITOR_ID)
							.getImageDescriptor(), null,
					Messages.RElementInfo_OpenDataViewer_label, null );
		}
		
		
		@Override
		public boolean isEnabled() {
			final RElementInfoData input= getInput();
			if (input != null) {
				final ICombinedRElement rElement= input.getElement();
				return (rElement != null
					&& RLiveDataEditorInput.isSupported(rElement)
					&& input.getTool() instanceof ToolProcess);
			}
			return false;
		}
		
		@Override
		protected void execute() throws ExecutionException {
			final RElementInfoData input= getInput();
			if (input != null) {
				final ToolProcess tool= (ToolProcess) input.getTool();
				final RElementName elementName= input.getElementName();
				
				RDataEditor.open(input.getWorkbenchPart().getSite().getPage(),
						tool, elementName, null );
				return;
			}
		}
		
	}
	
	
	private final int mode;
	
	private RLabelProvider labelProvider;
	
	private Composite contentComposite;
	private Label titleImage;
	private StyledText titleText;
	private StyledText infoText;
	
	private boolean layoutWorkaround;
	private boolean layoutHint;
	
	private RElementInfoData input;
	private boolean inputChanged;
	
	
	public RElementInfoControl(final Shell shell, final int mode) {
		super(shell, ""); //$NON-NLS-1$
		assert ((mode & MODE_FOCUS) == 0);
		this.mode= mode;
		
		JFaceResources.getFontRegistry().addListener(this);
		create();
	}
	
	public RElementInfoControl(final Shell shell, final int mode, final boolean dummy) {
		super(shell, new ToolBarManager(SWT.FLAT));
		assert ((mode & MODE_FOCUS) != 0);
		this.mode= mode;
		
		create();
	}
	
	
	@Override
	public void setInput(final Object input) {
		this.inputChanged= true;
		if (input instanceof RElementInfoData) {
			this.input= (RElementInfoData) input;
		}
		else {
			this.input= null;
		}
	}
	
	public RElementInfoData getInput() {
		return this.input;
	}
	
	@Override
	public boolean hasContents() {
		return (this.input != null);
	}
	
	@Override
	protected void createContent(final Composite parent) {
		this.contentComposite= new Composite(parent, SWT.NONE) {
			@Override
			public Point computeSize(final int width, final int height, final boolean changed) {
				return super.computeSize(width, height, changed || width != getSize().x);
			}
		};
		this.contentComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		final GridLayout gridLayout= LayoutUtil.createCompositeGrid(2);
		gridLayout.horizontalSpacing= (int) ((gridLayout.horizontalSpacing) / 1.5);
		this.contentComposite.setLayout(gridLayout);
		
		final int vIndent= Math.max(1, LayoutUtil.defaultVSpacing() / 4);
		final int hIndent= Math.max(3, LayoutUtil.defaultHSpacing() / 2);
		
		{	// Title image
			this.titleImage= new Label(this.contentComposite, SWT.NULL);
			final Image image= SharedUIResources.getImages().get(SharedUIResources.PLACEHOLDER_IMAGE_ID);
			this.titleImage.setImage(image);
			
			final GridData textGd= new GridData(SWT.FILL, SWT.TOP, false, false);
			this.titleText= new StyledText(this.contentComposite, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP) {
				@Override
				public Point computeSize(int width, final int height, final boolean changed) {
					if (!RElementInfoControl.this.layoutHint && width <= 0 && RElementInfoControl.this.contentComposite.getSize().x > 0) {
						width= RElementInfoControl.this.contentComposite.getSize().x -
								LayoutUtil.defaultHMargin() - RElementInfoControl.this.titleImage.getSize().x - LayoutUtil.defaultHSpacing() - 10;
					}
					
					final Point size= super.computeSize(width, -1, true);
//					if (width >= 0) {
//						size.x= Math.min(size.x, width);
//					}
					return size;
				}
			};
			
			this.titleText.setFont(JFaceResources.getDialogFont());
			final GC gc= new GC(this.titleText);
			final FontMetrics fontMetrics= gc.getFontMetrics();
			final GridData imageGd= new GridData(SWT.FILL, SWT.TOP, false, false);
			imageGd.horizontalIndent= hIndent;
			final int textHeight= fontMetrics.getAscent() + fontMetrics.getLeading();
			final int imageHeight= image.getBounds().height;
			final int shift= Math.max(3, (int) ((fontMetrics.getDescent()) / 1.5));
			if (textHeight+shift < imageHeight) {
				imageGd.verticalIndent= vIndent+shift;
				textGd.verticalIndent= vIndent+(imageHeight-textHeight);
			}
			else {
				imageGd.verticalIndent= vIndent+(textHeight-imageHeight)+shift;
				textGd.verticalIndent= vIndent;
			}
			this.titleImage.setLayoutData(imageGd);
			this.titleText.setLayoutData(textGd);
			this.layoutWorkaround= true;
			
			gc.dispose();
		}
		
		this.infoText= new StyledText(this.contentComposite, ((this.mode & MODE_FOCUS) != 0) ?
				(SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL) :
				(SWT.MULTI | SWT.READ_ONLY));
		this.infoText.setIndent(hIndent);
		this.infoText.setFont(JFaceResources.getFont(PREF_DETAIL_PANE_FONT));
		
		final GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
//		gd.widthHint= LayoutUtil.hintWidth(fInfoText, INFO_FONT, 50);
		this.infoText.setLayoutData(gd);
		
		setBackgroundColor(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		setForegroundColor(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		
		if ((this.mode & MODE_FOCUS) != 0) {
			final ToolBarManager toolBarManager= getToolBarManager();
			contributeToActionBars(PlatformUI.getWorkbench(), toolBarManager, null);
		}
		
		updateInput();
	}
	
	protected void contributeToActionBars(final IServiceLocator serviceLocator,
			final ToolBarManager toolBarManager, final HandlerCollection handlers) {
		toolBarManager.add(new OpenInEditorItem());
	}
	
	
	@Override
	public void setBackgroundColor(final Color background) {
		super.setBackgroundColor(background);
		this.contentComposite.setBackground(background);
	}
	
	@Override
	public void setForegroundColor(final Color foreground) {
		super.setForegroundColor(foreground);
		this.contentComposite.setForeground(foreground);
		this.titleText.setForeground(foreground);
		this.infoText.setForeground(foreground);
	}
	
	
	@Override
	public Rectangle computeTrim() {
		final Rectangle trim= super.computeTrim();
		
		final Rectangle textTrim= this.infoText.computeTrim(0, 0, 0, 0);
		trim.x += textTrim.x;
		trim.y += textTrim.y;
		trim.width += textTrim.width;
		trim.height += textTrim.height;
		
		return trim;
	}
	
	@Override
	public Point computeSizeHint() {
		updateInput();
		final Point sizeConstraints= getSizeConstraints();
		final Rectangle trim= computeTrim();
		
//		int charWidth= 20;
//		if (fInput.detailInfo != null) {
//			final int count= Math.min(6, fInfoText.getLineCount());
//			for (int i= 0; i < count; i++) {
//				charWidth= Math.max(charWidth, fInfoText.getLine(i).length());
//			}
//		}
		int widthHint= this.infoText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x + LayoutUtil.defaultHSpacing();
		final int widthMax2= LayoutUtil.hintWidth(this.infoText, PREF_DETAIL_PANE_FONT, 80);
		final int widthMax= ((sizeConstraints != null && sizeConstraints.x != SWT.DEFAULT) ?
				sizeConstraints.x : widthMax2) - trim.width;
		this.layoutHint= true;
		final int titleHint= LayoutUtil.defaultHMargin() + this.titleImage.getSize().x + LayoutUtil.defaultHSpacing() + this.titleText.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		this.layoutHint= false;
		if (titleHint > widthHint && widthMax2 > widthHint) {
			widthHint= Math.min(widthMax2, titleHint);
		}
		if (widthMax < widthHint) {
			widthHint= widthMax;
		}
		// avoid change of wrapping caused by scrollbar
		if (widthHint < titleHint && widthHint + this.infoText.computeTrim(0, 0, 0, 0).width >= titleHint) {
			widthHint= titleHint;
		}
		
		final int heightMax= ((sizeConstraints != null && sizeConstraints.y != SWT.DEFAULT) ?
				sizeConstraints.y :
				this.infoText.getLineHeight()*12) - trim.height;
		
		final Point size= this.contentComposite.computeSize(widthHint, SWT.DEFAULT, true);
		size.y += LayoutUtil.defaultVSpacing();
		size.x= Math.max(Math.min(size.x, widthMax), 200) + trim.width;
		size.y= Math.max(Math.min(size.y, heightMax), 100) + trim.height;
		return size;
	}
	
	@Override
	public Point computeSizeConstraints(final int widthInChars, final int heightInChars) {
		final int titleWidth= LayoutUtil.hintWidth(this.titleText, JFaceResources.DIALOG_FONT, widthInChars);
		final int titleHeight= this.titleText.getLineHeight();
		final int infoWidth= LayoutUtil.hintWidth(this.infoText, PREF_DETAIL_PANE_FONT, widthInChars);
		final int infoHeight= this.infoText.getLineHeight() * (heightInChars);
		
		return new Point(Math.max(titleWidth, infoWidth),
				titleHeight + LayoutUtil.defaultVSpacing() + infoHeight );
	}
	
	@Override
	public void setVisible(final boolean visible) {
		if (visible) {
			updateInput();
			
			if (this.layoutWorkaround) {
				this.contentComposite.layout(true, true);
				this.layoutWorkaround= false;
			}
			
			if (Platform.WS_WIN32.equals(SWT.getPlatform())) {
				final Shell shell= getShell();
				if (shell != null) {
					shell.moveAbove(null);
				}
			}
		}
		super.setVisible(visible);
	}
	
	@Override
	public void setFocus() {
		this.infoText.setFocus();
	}
	
	private void updateInput() {
		if (this.infoText == null || !this.inputChanged) {
			return;
		}
		if (this.labelProvider == null) {
			this.labelProvider= new RLabelProvider(RLabelProvider.LONG | RLabelProvider.HEADER | RLabelProvider.NAMESPACE);
		}
		if (this.input != null) {
			final Image image= this.labelProvider.getImage(this.input.getElement());
			this.titleImage.setImage((image != null) ? image : SharedUIResources.getImages().get(SharedUIResources.PLACEHOLDER_IMAGE_ID));
			final StyledString styleString= this.labelProvider.getStyledText(this.input.getElement(), this.input.getElementName(), this.input.getElementAttr());
			if (this.input.isElementOfActiveBinding()) {
				styleString.append(" (active binding)", StyledString.QUALIFIER_STYLER);
			}
			this.titleText.setText(styleString.getString());
			this.titleText.setStyleRanges(styleString.getStyleRanges());
			if (this.input.hasDetail()) {
				this.infoText.setText(this.input.getDetailTitle() + '\n' + ((this.input.getDetailInfo() != null) ? this.input.getDetailInfo() : "")); //$NON-NLS-1$
				final StyleRange title= new StyleRange(0, this.input.getDetailTitle().length(), null, null);
				title.underline= true;
				this.infoText.setStyleRange(title);
			}
			else {
				this.infoText.setText(""); //$NON-NLS-1$
			}
		}
		else {
			this.titleImage.setImage(SharedUIResources.getImages().get(SharedUIResources.PLACEHOLDER_IMAGE_ID));
			this.titleText.setText(""); //$NON-NLS-1$
			this.infoText.setText(""); //$NON-NLS-1$
		}
		if ((this.mode & MODE_FOCUS) != 0) {
			getToolBarManager().update(true);
		}
		else {
			setStatusText((this.input.getControl() != null
							&& this.input.getControl().isFocusControl() ) ?
					InformationDispatchHandler.getTooltipAffordanceString() : "" ); //$NON-NLS-1$
		}
		this.inputChanged= false;
	}
	
	
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		// enriched mode
		return new RElementInfoHoverCreator(this.mode | MODE_FOCUS);
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		final String property= event.getProperty();
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
