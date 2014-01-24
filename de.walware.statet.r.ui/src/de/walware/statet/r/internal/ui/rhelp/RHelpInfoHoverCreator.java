/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.rhelp;

import static de.walware.ecommons.ltk.ui.sourceediting.assist.IInfoHover.MODE_FOCUS;
import static org.eclipse.debug.ui.IDebugUIConstants.PREF_DETAIL_PANE_FONT;

import java.net.URI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.actions.SimpleContributionItem;
import de.walware.ecommons.ui.mpbv.BrowserHandler.IBrowserProvider;
import de.walware.ecommons.ui.mpbv.BrowserHandler.NavigateBackHandler;
import de.walware.ecommons.ui.mpbv.BrowserHandler.NavigateForwardHandler;
import de.walware.ecommons.ui.util.InformationDispatchHandler;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.PixelConverter;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.ui.RUI;


public class RHelpInfoHoverCreator extends AbstractReusableInformationControlCreator {
	
	
	public static final boolean isAvailable(final Composite parent) {
		return BrowserInformationControl.isAvailable(parent);
	}
	
	
	public static class Data {
		
		final Control control;
		
		final Object helpObject;
		final String httpUrl;
		
		public Data(final Control control, final Object helpObject, final String httpUrl) {
			this.control = control;
			this.helpObject = helpObject;
			this.httpUrl = httpUrl;
		}
		
	}
	
	
	private final int mode;
	
	
	public RHelpInfoHoverCreator(final int mode) {
		this.mode = mode;
	}
	
	
	@Override
	protected IInformationControl doCreateInformationControl(final Shell parent) {
		return ((this.mode & MODE_FOCUS) != 0) ?
				new RHelpInfoControl(parent, this.mode, true) :
				new RHelpInfoControl(parent, this.mode);
	}
	
}

class RHelpInfoControl extends AbstractInformationControl implements IInformationControlExtension2,
		IPropertyChangeListener, OpenWindowListener, LocationListener, ProgressListener, TitleListener,
		IBrowserProvider {
	
	
	/** Action id (command) to navigate one page back. */
	protected static final String NAVIGATE_BACK_ID = IWorkbenchCommandConstants.NAVIGATE_BACK;
	/** Action id (command) to navigate one page forward. */
	protected static final String NAVIGATE_FORWARD_ID = IWorkbenchCommandConstants.NAVIGATE_FORWARD;
	
	
	/**
	 * Cached scroll bar width and height
	 */
	private static Point gScrollBarSize;
	
	
	private final int fMode;
	
	private RHelpLabelProvider fLabelProvider;
	
	private Composite fContentComposite;
	private Label fTitleImage;
	private StyledText fTitleText;
	private Browser fInfoBrowser;
	
	private final HandlerCollection fHandlerCollection = new HandlerCollection();
	
	private boolean fLayoutWorkaround;
	private boolean fLayoutHint;
	
	private RHelpInfoHoverCreator.Data fInput;
	private boolean fInputChanged;
	
	private boolean fLoadingCompleted;
	private String fBrowserTitle;
	
	private boolean fHide;
	
	
	RHelpInfoControl(final Shell shell, final int mode) {
		super(shell, ""); //$NON-NLS-1$
		assert ((mode & MODE_FOCUS) == 0);
		fMode = mode;
		
		JFaceResources.getFontRegistry().addListener(this);
		create();
	}
	
	RHelpInfoControl(final Shell shell, final int mode, final boolean dummy) {
		super(shell, new ToolBarManager(SWT.FLAT));
		assert ((mode & MODE_FOCUS) != 0);
		fMode = mode;
		
		create();
	}
	
	
	@Override
	public void setInput(final Object input) {
		fInputChanged = true;
		if (input instanceof RHelpInfoHoverCreator.Data) {
			fInput = (RHelpInfoHoverCreator.Data) input;
		}
		else {
			fInput = null;
		}
	}
	
	@Override
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
		
		final GridLayout gridLayout = LayoutUtil.createCompositeGrid(2);
		gridLayout.horizontalSpacing = (int) ((gridLayout.horizontalSpacing) / 1.5);
		fContentComposite.setLayout(gridLayout);
		
		final int vIndent = Math.max(1, LayoutUtil.defaultVSpacing() / 4);
		final int hIndent = Math.max(3, LayoutUtil.defaultHSpacing() / 3);
		
		{	// Title image
			fTitleImage = new Label(fContentComposite, SWT.NULL);
			final Image image = SharedUIResources.getImages().get(SharedUIResources.PLACEHOLDER_IMAGE_ID);
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
			imageGd.horizontalIndent = hIndent - 2;
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
		
		fInfoBrowser = new Browser(fContentComposite, SWT.NONE);
		
		fInfoBrowser.addOpenWindowListener(this);
		fInfoBrowser.addLocationListener(this);
		fInfoBrowser.addProgressListener(this);
		fInfoBrowser.addTitleListener(this);
		
		fInfoBrowser.addOpenWindowListener(new OpenWindowListener() {
			@Override
			public void open(final WindowEvent event) {
				event.required = true;
			}
		});
		// Disable context menu
		fInfoBrowser.setMenu(new Menu(getShell(), SWT.NONE));
		
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
//		gd.widthHint = LayoutUtil.hintWidth(fInfoText, INFO_FONT, 50);
		fInfoBrowser.setLayoutData(gd);
		
		fInfoBrowser.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(final KeyEvent e)  {
				if (e.character == SWT.ESC) {
					dispose();
				}
			}
			@Override
			public void keyReleased(final KeyEvent e) {}
		});
		
		setBackgroundColor(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		setForegroundColor(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		
		if ((fMode & MODE_FOCUS) != 0) {
			initActions(fHandlerCollection);
			final ToolBarManager toolBarManager = getToolBarManager();
			contributeToActionBars(PlatformUI.getWorkbench(), toolBarManager, fHandlerCollection);
			toolBarManager.update(true);
		}
		updateInput();
	}
	
	protected void initActions(final HandlerCollection handlers) {
		{	final IHandler2 handler = new NavigateBackHandler(this);
			handlers.add(NAVIGATE_BACK_ID, handler);
//			handlerService.activateHandler(NAVIGATE_BACK_ID, handler);
//			handlerService.activateHandler(IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY, handler);
		}
		{	final IHandler2 handler = new NavigateForwardHandler(this);
			handlers.add(NAVIGATE_FORWARD_ID, handler);
//			handlerService.activateHandler(NAVIGATE_FORWARD_ID, handler);
//			handlerService.activateHandler(IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY, handler);
		}
	}
	
	protected void contributeToActionBars(final IServiceLocator serviceLocator,
			final ToolBarManager toolBarManager, final HandlerCollection handlers) {
		toolBarManager.add(
				new HandlerContributionItem(new CommandContributionItemParameter(
						serviceLocator, null, NAVIGATE_BACK_ID, HandlerContributionItem.STYLE_PUSH),
						handlers.get(NAVIGATE_BACK_ID)));
		toolBarManager.add(
				new HandlerContributionItem(new CommandContributionItemParameter(
						serviceLocator, null, NAVIGATE_FORWARD_ID, HandlerContributionItem.STYLE_PUSH),
						handlers.get(NAVIGATE_FORWARD_ID)));
		toolBarManager.add(new SimpleContributionItem(
				RUI.getImageDescriptor(RUI.IMG_OBJ_R_HELP_SEARCH), null,
				"Show in R Help View", "V") {
			@Override
			protected void execute() throws ExecutionException {
				if (UIAccess.isOkToUse(fInfoBrowser)) {
					try {
						String url = fInfoBrowser.getUrl();
						final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
						final RHelpView view = (RHelpView) page.showView(RUI.R_HELP_VIEW_ID);
						final String browseUrl = RCore.getRHelpManager().toHttpUrl(url, null, RHelpUIServlet.BROWSE_TARGET);
						if (browseUrl != null) {
							url = browseUrl;
						}
						view.openUrl(url, null);
					}
					catch (final Exception e) {}
				}
			}
		});
	}
	
	
	@Override
	public Browser getBrowser() {
		return fInfoBrowser;
	}
	
	@Override
	public void showMessage(final int severity, final String message) {
	}
	
	@Override
	public void changing(final LocationEvent event) {
		if (event.location.startsWith("http://")) { //$NON-NLS-1$
			try {
				if (RCore.getRHelpManager().isDynamic(new URI(event.location))) {
					return;
				}
			}
			catch (final Exception e) {}
		}
		if (event.location.equals("about:blank")) { //$NON-NLS-1$
			return;
		}
		event.doit = false;
	}
	
	@Override
	public void changed(final LocationEvent event) {
		if (!event.top) {
			return;
		}
		final String url = fInfoBrowser.getUrl();
		final Object obj = RCore.getRHelpManager().getContentOfUrl(url);
		updateTitle(obj, fBrowserTitle);
	}
	
	@Override
	public void changed(final ProgressEvent event) {
		fHandlerCollection.update(null);
	}
	
	@Override
	public void completed(final ProgressEvent event) {
		fLoadingCompleted = true;
		fHandlerCollection.update(null);
	}
	
	@Override
	public void changed(final TitleEvent event) {
		String title = event.title;
		if (title == null) {
			title = ""; //$NON-NLS-1$
		}
		else if (title.startsWith("http://")) { //$NON-NLS-1$
			final int idx = title.lastIndexOf('/');
			if (idx >= 0) {
				title = title.substring(idx+1);
			}
		}
		fBrowserTitle = title;
	}
	
	@Override
	public void open(final WindowEvent event) {
		event.required = true;
	}
	
	@Override
	public void setBackgroundColor(final Color background) {
		super.setBackgroundColor(background);
		fContentComposite.setBackground(background);
		fInfoBrowser.setBackground(background);
	}
	
	@Override
	public void setForegroundColor(final Color foreground) {
		super.setForegroundColor(foreground);
		fContentComposite.setForeground(foreground);
		fTitleText.setForeground(foreground);
		fInfoBrowser.setForeground(foreground);
	}
	
	@Override
	public void setSize(final int width, final int height) {
		fInfoBrowser.setRedraw(false); // avoid flickering
		try {
			super.setSize(width, height);
		}
		finally {
			fInfoBrowser.setRedraw(true);
		}
	}
	
	
	@Override
	public Rectangle computeTrim() {
		final Rectangle trim = super.computeTrim();
		
		final Rectangle textTrim = fInfoBrowser.computeTrim(0, 0, 0, 0);
		if ((fMode & MODE_FOCUS) != 0 && textTrim.width == 0) {
			if (gScrollBarSize == null) {
				final Text text = new Text(fContentComposite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
				gScrollBarSize = new Point(
						text.getVerticalBar().getSize().x,
						text.getHorizontalBar().getSize().y);
				text.dispose();
			}
			textTrim.x = 0;
			textTrim.y = 0;
			textTrim.width = gScrollBarSize.x;
			textTrim.height = gScrollBarSize.y;
		}
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
		
		fTitleText.setFont(JFaceResources.getDialogFont());
		final PixelConverter converter = new PixelConverter(fTitleText);
		int widthHint = converter.convertWidthInCharsToPixels(60);
		final GC gc = new GC(fContentComposite);
		gc.setFont(JFaceResources.getTextFont());
		widthHint = Math.max(widthHint, gc.getFontMetrics().getAverageCharWidth() * 60);
		gc.dispose();
		
		final int heightHint = fTitleText.getLineHeight() * 12;
		
		final int widthMax = ((sizeConstraints != null && sizeConstraints.x != SWT.DEFAULT) ?
				sizeConstraints.x : widthHint+100) - trim.width;
		final int heightMax = ((sizeConstraints != null && sizeConstraints.y != SWT.DEFAULT) ?
				sizeConstraints.y : fTitleText.getLineHeight()*12) - trim.height;
		
		final Point size = new Point(widthHint, heightHint);
		size.y += LayoutUtil.defaultVSpacing();
		size.x = Math.max(Math.min(size.x, widthMax), 200) + trim.width;
		size.y = Math.max(Math.min(size.y, heightMax), 100) + trim.height;
		return size;
	}
	
	@Override
	public Point computeSizeConstraints(final int widthInChars, final int heightInChars) {
		final int width = LayoutUtil.hintWidth(fTitleText, JFaceResources.DIALOG_FONT, widthInChars);
		final int lineHeight = fTitleText.getLineHeight();
		
		return new Point(width, lineHeight*heightInChars + LayoutUtil.defaultVSpacing());
	}
	
	@Override
	public void setVisible(final boolean visible) {
		if (visible) {
			fHide = false;
			updateInput();
			
			final Display display = Display.getCurrent();
			display.timerExec(200, new Runnable() {
				@Override
				public void run() {
					fLoadingCompleted = true;
				}
			});
			while (!fLoadingCompleted) {
				// Drive the event loop to process the events required to load the browser widget's contents:
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			
			if (fHide) {
				return;
			}
			
			if (fLayoutWorkaround) {
				fContentComposite.layout(true, true);
				fLayoutWorkaround = false;
			}
			
			if (Platform.OS_WIN32.equals(SWT.getPlatform())) {
				final Shell shell = getShell();
				if (shell != null) {
					shell.moveAbove(null);
				}
			}
		}
		else {
			fHide = true;
		}
		super.setVisible(visible);
	}
	
	@Override
	public void setFocus() {
		fInfoBrowser.setFocus();
	}
	
	private void updateInput() {
		if (fInfoBrowser == null || !fInputChanged) {
			return;
		}
		if (fLabelProvider == null) {
			fLabelProvider = new RHelpLabelProvider(RHelpLabelProvider.WITH_TITLE | RHelpLabelProvider.WITH_QUALIFIER | RHelpLabelProvider.HEADER);
		}
		fLoadingCompleted = false;
		fInputChanged = false;
		fBrowserTitle = null;
		updateTitle(fInput.helpObject, null);
		if (fInput != null && fInput.httpUrl != null) {
			String url = fInput.httpUrl;
			if ((fMode & MODE_FOCUS) == 0) { // disable scrollbars
				url += "?style=hover"; //$NON-NLS-1$
			}
			fInfoBrowser.setUrl(url);
		}
		else {
			fInfoBrowser.setUrl("about:blank"); //$NON-NLS-1$
		}
		if ((fMode & MODE_FOCUS) == 0) {
			setStatusText((fInput.control != null && fInput.control.isFocusControl()) ?
					InformationDispatchHandler.getAffordanceString(fMode) : ""); //$NON-NLS-1$
		}
	}
	
	private void updateTitle(final Object helpObject, final String alt) {
		if (helpObject != null) {
			final Image image = fLabelProvider.getImage(helpObject);
			fTitleImage.setImage((image != null) ? image : SharedUIResources.getImages().get(SharedUIResources.PLACEHOLDER_IMAGE_ID));
			final StyledString styleString = fLabelProvider.getStyledText(helpObject);
			fTitleText.setText(styleString.getString());
			fTitleText.setStyleRanges(styleString.getStyleRanges());
		}
		else {
			fTitleImage.setImage(SharedUIResources.getImages().get(SharedUIResources.PLACEHOLDER_IMAGE_ID));
			fTitleText.setText((alt != null) ? alt : ""); //$NON-NLS-1$
		}
	}
	
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		// enriched mode
		return new RHelpInfoHoverCreator(fMode | MODE_FOCUS);
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		final String property = event.getProperty();
		if (property.equals(PREF_DETAIL_PANE_FONT) || property.equals(JFaceResources.DEFAULT_FONT)) {
			dispose();
		}
	}
	
	
	@Override
	public void dispose() {
		JFaceResources.getFontRegistry().removeListener(this);
		fHandlerCollection.dispose();
		super.dispose();
	}
	
}
