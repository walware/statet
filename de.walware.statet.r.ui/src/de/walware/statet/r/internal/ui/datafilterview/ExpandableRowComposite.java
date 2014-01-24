/*=============================================================================#
 # Copyright (c) 2000-2014 IBM Corporation and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     IBM Corporation - initial API and implementation
 #     Kai Nacke - Fix for Bug 202382
 #     Bryan Hunt - Fix for Bug 245457
 #     Didier Villevalois - Fix for Bug 178534 
 #=============================================================================*/

package de.walware.statet.r.internal.ui.datafilterview;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.SizeCache;
import org.eclipse.ui.forms.widgets.Twistie;
import org.eclipse.ui.internal.forms.widgets.FormUtil;

import de.walware.ecommons.ui.SharedUIResources;


/**
 * This composite is capable of expanding or collapsing a single client that is
 * its direct child. The composite renders an expansion toggle affordance
 * (according to the chosen style), and a title that also acts as a hyperlink
 * (can be selected and is traversable). The client is layed out below the title
 * when expanded, or hidden when collapsed.
 * <p>
 * The widget can be instantiated as-is, or subclassed to modify some aspects of
 * it. *
 * <p>
 * Since 3.1, left/right arrow keys can be used to control the expansion state.
 * If several expandable composites are created in the same parent, up/down
 * arrow keys can be used to traverse between them. Expandable text accepts
 * mnemonics and mnemonic activation will toggle the expansion state.
 * 
 * <p>
 * While expandable composite recognize that different styles can be used to
 * render the title bar, and even defines the constants for these styles (<code>TITLE_BAR</code>
 * and <code>SHORT_TITLE_BAR</code> the actual painting is done in the
 * subclasses.
 * 
 * @see Section
 * @since 3.0
 */
public class ExpandableRowComposite extends Canvas {
	
	
	private static class Toggle extends Twistie {
		
		public Toggle(final Composite parent, final int style) {
			super(parent, style);
		}
		
		public void setHover(final boolean on) {
			hover = on;
		}
		
		public boolean getHover() {
			return hover;
		}
		
		@Override
		protected void paint(final PaintEvent e) {
			// don't paint focus
			final GC gc = e.gc;
			final Rectangle clientArea = getClientArea();
			if (clientArea.width == 0 || clientArea.height == 0) {
				return;
			}
			paintHyperlink(gc);
		}
		
	}
	
	
	/**
	 * If this style is used, a twistie will be used to render the expansion
	 * toggle.
	 */
	public static final int TWISTIE = 1 << 1;
	
	/**
	 * If this style is used, the title text will be rendered as a hyperlink
	 * that can individually accept focus. Otherwise, it will still act like a
	 * hyperlink, but only the toggle control will accept focus.
	 */
	public static final int FOCUS_TITLE = 1 << 3;
	
	/**
	 * If this style is used, the client origin will be vertically aligned with
	 * the title text. Otherwise, it will start at x = 0.
	 */
	public static final int CLIENT_INDENT = 1 << 4;
	
	/**
	 * If this style is used, computed size of the composite will take the
	 * client width into consideration only in the expanded state. Otherwise,
	 * client width will always be taken into acount.
	 */
	public static final int COMPACT = 1 << 5;
	
	/**
	 * If this style is used, the control will be created in the expanded state.
	 * This state can later be changed programmatically or by the user if
	 * TWISTIE or TREE_NODE style is used.
	 */
	public static final int EXPANDED = 1 << 6;
	
	/**
	 * If this style is used, title bar decoration will be painted behind the
	 * text.
	 */
	public static final int TITLE_BAR = 1 << 8;
	
	/**
	 * If this style is used, a short version of the title bar decoration will
	 * be painted behind the text. This style is useful when a more descrete
	 * option is needed for the title bar.
	 * 
	 * @since 3.1
	 */
	public static final int SHORT_TITLE_BAR = 1 << 9;
	
	/**
	 * By default, text client is right-aligned. If this style is used, it will
	 * be positioned after the text control and vertically centered with it.
	 */
	public static final int LEFT_TEXT_CLIENT_ALIGNMENT = 1 << 13;
	
	/**
	 * By default, a focus box is painted around the title when it receives focus.
	 * If this style is used, the focus box will not be painted.  This style does
	 * not apply when FOCUS_TITLE is used.
	 * @since 3.5
	 */
	public static final int NO_TITLE_FOCUS_BOX = 1 << 14;
	
	public static final int IMAGE = 1 << 30;
	
	/**
	 * Width of the margin that will be added around the control (default is 0).
	 */
	public int marginWidth = 0;
	
	/**
	 * Height of the margin that will be added around the control (default is
	 * 0).
	 */
	public int marginHeight = 0;
	
	/**
	 * Vertical spacing between the title area and the composite client control
	 * (default is 3).
	 */
	public int clientVerticalSpacing = 3;
	
	/**
	 * Vertical spacing between the title area and the description control
	 * (default is 0). The description control is normally placed at the new
	 * line as defined in the font used to render it. This value will be added
	 * to it.
	 * 
	 * @since 3.3
	 */
	public int descriptionVerticalSpacing = 0;
	
	/**
	 * Horizontal margin around the inside of the title bar area when TITLE_BAR
	 * or SHORT_TITLE_BAR style is used. This variable is not used otherwise.
	 * 
	 * @since 3.3
	 */
	public int titleBarTextMarginWidth = 6;
	
	/**
	 * The toggle widget used to expand the composite.
	 */
	protected Toggle toggle;
	
	/**
	 * The text label for the title.
	 */
	protected Control textLabel;
	
	private static final int IGAP = 4;
	private static final int IVGAP = 3;
	
	private static final Point NULL_SIZE = new Point(0, 0);
	
	private static final int VSPACE = 3;
	
	private static final int SEPARATOR_HEIGHT = 2;
	
	private int expansionStyle = TWISTIE | FOCUS_TITLE | EXPANDED;
	
	private boolean expanded;
	
	private Label imageLabel;
	
	private Control textClient;
	
	private Control client;
	
	private final ListenerList listeners = new ListenerList();
	
	private Color titleBarForeground;
	
	private final Rectangle titleHeaderRegion = new Rectangle(0, 0, 0, 0);
	
	private class ExpandableLayout extends Layout implements ILayoutExtension {
		
		private final SizeCache toggleCache = new SizeCache();
		
		private final SizeCache textClientCache = new SizeCache();
		
		private final SizeCache textLabelCache = new SizeCache();
		
		private final SizeCache descriptionCache = new SizeCache();
		
		private final SizeCache clientCache = new SizeCache();
		
		private void initCache(final boolean shouldFlush) {
			toggleCache.setControl(toggle);
			textClientCache.setControl(textClient);
			textLabelCache.setControl(textLabel);
			descriptionCache.setControl(getDescriptionControl());
			clientCache.setControl(client);
			
			if (shouldFlush) {
				toggleCache.flush();
				textClientCache.flush();
				textLabelCache.flush();
				descriptionCache.flush();
				clientCache.flush();
			}
		}
		
		@Override
		protected void layout(final Composite parent, final boolean changed) {
			initCache(changed);
			
			final Rectangle clientArea = parent.getClientArea();
			int thmargin = 0;
			int tvmargin = 0;
			
			if (hasTitleBar()) {
				thmargin = titleBarTextMarginWidth;
				tvmargin = IVGAP;
			}
			int x = marginWidth + thmargin;
			int y = marginHeight + tvmargin;
			Point tsize = NULL_SIZE;
			Point tcsize = NULL_SIZE;
			if (toggle != null) {
				tsize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
			Point isize = NULL_SIZE;
			if (imageLabel != null) {
				isize = imageLabel.computeSize(16, 16);
			}
			int twidth = clientArea.width - marginWidth - marginWidth - thmargin - thmargin;
			if (tsize.x > 0) {
				twidth -= tsize.x + IGAP;
			}
			if (isize.x > 0) {
				twidth -= isize.x;
			}
			if (textClient != null) {
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
			Point size = NULL_SIZE;
			if (textLabel != null) {
				if (tcsize.x > 0 && FormUtil.isWrapControl(textClient)) {
					size = textLabelCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					if (twidth < size.x + IGAP + tcsize.x) {
						twidth -= IGAP;
						if (textLabel instanceof Label) {
							final GC gc = new GC(textLabel);
							size = FormUtil.computeWrapSize(gc, ((Label)textLabel).getText(), Math.round(twidth*(size.x/(float)(size.x+tcsize.x))));
							gc.dispose();
						}
						else {
							size = textLabelCache.computeSize(Math.round(twidth*(size.x/(float)(size.x+tcsize.x))), SWT.DEFAULT);
						}
						tcsize = textClientCache.computeSize(twidth-size.x, SWT.DEFAULT);
					}
				}
				else {
					if (tcsize.x > 0) {
						twidth -= tcsize.x + IGAP;
					}
					size = textLabelCache.computeSize(twidth, SWT.DEFAULT);
				}
			}
			if (textLabel instanceof Label) {
				final Point defSize = textLabelCache.computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				if (defSize.y == size.y) {
					// One line - pick the smaller of the two widths
					size.x = Math.min(defSize.x, size.x);
				}
			}
			int theaderHeight;
			{	final GC gc = new GC(ExpandableRowComposite.this);
				gc.setFont(getFont());
				final FontMetrics fm = gc.getFontMetrics();
				theaderHeight = fm.getHeight();
				gc.dispose();
				
				if (isize.y > theaderHeight) {
					theaderHeight = isize.y;
				}
			}
			if (toggle != null) {
				if (textClient != null
						&& (expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0) {
					theaderHeight = Math.max(theaderHeight, tcsize.y);
				}
				int ty = theaderHeight / 2 - tsize.y / 2 + 1;
				ty = Math.max(ty, 0);
				ty += marginHeight + tvmargin;
				toggle.setLocation(x, ty);
				toggle.setSize(tsize);
				x += tsize.x + IGAP;
			}
			if (imageLabel != null) {
				final int iy = theaderHeight / 2 - isize.y / 2;
				imageLabel.setBounds(x, iy, isize.x, isize.y);
			}
			if (textLabel != null) {
				int ty = theaderHeight / 2 - size.y / 2;
				int tx = x;
				if (imageLabel != null) {
					tx += isize.x + IGAP;
				}
				if (textClient != null
						&& (expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0) {
					if (size.y < tcsize.y) {
						ty = tcsize.y / 2 - size.y / 2 + marginHeight
								+ tvmargin;
					}
				}
				final String os = System.getProperty("os.name"); //$NON-NLS-1$
				if (Constants.OS_LINUX.equalsIgnoreCase(os)) {
					size.x += 1; // See Bug 342610
				}
				
				textLabelCache.setBounds(tx, ty, size.x, size.y);
			}
			if (textClient != null) {
				int tcx;
				if ((expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0) {
					tcx = x + size.x + IGAP;
				} else {
					tcx = clientArea.width - tcsize.x - marginWidth - thmargin;
				}
				textClientCache.setBounds(tcx, y, tcsize.x, tcsize.y);
			}
			int tbarHeight = theaderHeight;
			if (size.y > tbarHeight) {
				tbarHeight = size.y;
			}
			if (tcsize.y > tbarHeight) {
				tbarHeight = tcsize.y;
			}
			y += tbarHeight;
			if (hasTitleBar()) {
				y += tvmargin;
			}
			if (getSeparatorControl() != null) {
				y += VSPACE;
				getSeparatorControl().setBounds(marginWidth, y,
						clientArea.width - marginWidth - marginWidth,
						SEPARATOR_HEIGHT);
				y += SEPARATOR_HEIGHT;
				if (expanded) {
					y += VSPACE;
				}
			}
			if (expanded) {
				int areaWidth = clientArea.width - marginWidth - thmargin;
				int cx = marginWidth + thmargin;
				if ((expansionStyle & CLIENT_INDENT) != 0) {
					cx = x;
				}
				areaWidth -= cx;
				if (client != null) {
					Point dsize = null;
					final Control desc = getDescriptionControl();
					if (desc != null) {
						dsize = descriptionCache.computeSize(areaWidth,
								SWT.DEFAULT);
						y += descriptionVerticalSpacing;
						descriptionCache.setBounds(cx, y, areaWidth, dsize.y);
						y += dsize.y + clientVerticalSpacing;
					} else {
						y += clientVerticalSpacing;
						if (getSeparatorControl() != null) {
							y -= VSPACE;
						}
					}
					final int cwidth = areaWidth;
					final int cheight = clientArea.height - marginHeight
							- marginHeight - y;
					clientCache.setBounds(cx, y, cwidth, cheight);
				}
			}
			
			titleHeaderRegion.x = marginWidth;
			titleHeaderRegion.y = marginHeight;
			titleHeaderRegion.width = clientArea.width - marginWidth - marginWidth;
			if (tcsize.x > 0) {
				titleHeaderRegion.width -= tcsize.x + IGAP;
			}
			titleHeaderRegion.height = theaderHeight;
		}
		
		@Override
		protected Point computeSize(final Composite parent, final int wHint, final int hHint,
				final boolean changed) {
			initCache(changed);
			
			int width = 0, height = 0;
			Point tsize = NULL_SIZE;
			int twidth = 0;
			if (toggle != null) {
				tsize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				twidth = tsize.x + IGAP;
			}
			int thmargin = 0;
			int tvmargin = 0;
			
			Point isize = NULL_SIZE;
			if (imageLabel != null) {
				isize = imageLabel.computeSize(16, 16);
			}
			
			if (hasTitleBar()) {
				thmargin = titleBarTextMarginWidth;
				tvmargin = IVGAP;
			}
			int innerwHint = wHint;
			if (innerwHint != SWT.DEFAULT) {
				innerwHint -= twidth + marginWidth + marginWidth + thmargin + thmargin;
				if (isize.x > 0) {
					innerwHint -= isize.x + IGAP;
				}
			}
			
			int innertHint = innerwHint;
			
			Point tcsize = NULL_SIZE;
			if (textClient != null) {
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
			Point size = NULL_SIZE;
			
			if (textLabel != null) {
				if (tcsize.x > 0 && FormUtil.isWrapControl(textClient)) {
					size = textLabelCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					if (innertHint != SWT.DEFAULT && innertHint < size.x + IGAP + tcsize.x) {
						innertHint -= IGAP;
						if (textLabel instanceof Label) {
							final GC gc = new GC(textLabel);
							size = FormUtil.computeWrapSize(gc, ((Label)textLabel).getText(), Math.round(innertHint*(size.x/(float)(size.x+tcsize.x))));
							gc.dispose();
						}
						else {
							size = textLabelCache.computeSize(Math.round(innertHint*(size.x/(float)(size.x+tcsize.x))), SWT.DEFAULT);
						}
						tcsize = textClientCache.computeSize(innertHint-size.x, SWT.DEFAULT);
					}
				} else {
					if (innertHint != SWT.DEFAULT && tcsize.x > 0) {
						innertHint -= IGAP + tcsize.x;
					}
					size = textLabelCache.computeSize(innertHint, SWT.DEFAULT);
				}
			}
			if (textLabel instanceof Label) {
				final Point defSize = textLabelCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (defSize.y == size.y) {
					// One line - pick the smaller of the two widths
					size.x = Math.min(defSize.x, size.x);
				}
			}
			
			if (twidth > 0) {
				width += twidth;
			}
			if (isize.x > 0) {
				width += isize.x + IGAP;
			}
			if (size.x > 0) {
				width += size.x;
			}
			if (tcsize.x > 0) {
				width += tcsize.x + IGAP;
			}
			if (tsize.y > height) {
				height = tsize.y;
			}
			if (isize.y > height) {
				height = isize.y;
			}
			if (size.y > height) {
				height = size.y;
			}
			if (tcsize.y > height) {
				height = tcsize.y;
			}
			if (getSeparatorControl() != null) {
				height += VSPACE + SEPARATOR_HEIGHT;
				if (expanded && client != null) {
					height += VSPACE;
				}
			}
			
			if (wHint < width && size.x > 50) {
				width -= size.x - 50;
			}
			
			// if (hasTitleBar())
			// height += VSPACE;
			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				int cwHint = wHint;
				int clientIndent = 0;
				if ((expansionStyle & CLIENT_INDENT) != 0) {
					clientIndent = twidth;
				}
				
				if (cwHint != SWT.DEFAULT) {
					cwHint -= marginWidth + marginWidth + thmargin + thmargin;
					if ((expansionStyle & CLIENT_INDENT) != 0) {
						if (tcsize.x > 0) {
							cwHint -= twidth;
						}
					}
				}
				Point dsize = null;
				final Point csize = clientCache.computeSize(FormUtil.getWidthHint(cwHint, client), SWT.DEFAULT);
				if (getDescriptionControl() != null) {
					int dwHint = cwHint;
					if (dwHint == SWT.DEFAULT) {
						dwHint = csize.x;
						if ((expansionStyle & CLIENT_INDENT) != 0) {
							dwHint -= twidth;
						}
					}
					dsize = descriptionCache.computeSize(dwHint, SWT.DEFAULT);
				}
				if (dsize != null) {
					width = Math.max(width, dsize.x + clientIndent);
					if (expanded) {
						height += descriptionVerticalSpacing + dsize.y + clientVerticalSpacing;
					}
				} else {
					height += clientVerticalSpacing;
					if (getSeparatorControl() != null) {
						height -= VSPACE;
					}
				}
				width = Math.max(width, csize.x + clientIndent);
				if (expanded) {
					height += csize.y;
				}
			}
			
			final Point result = new Point(width + marginWidth + marginWidth
					+ thmargin + thmargin, height + marginHeight + marginHeight
					+ tvmargin + tvmargin);
			return result;
		}
		
		@Override
		public int computeMinimumWidth(final Composite parent, final boolean changed) {
			return computeSize(parent, 0, SWT.DEFAULT, changed).x;
		}
		
		@Override
		public int computeMaximumWidth(final Composite parent, final boolean changed) {
			return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
		}
	}
	
	/**
	 * Creates an expandable composite using a TWISTIE toggle.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            SWT style bits
	 */
	public ExpandableRowComposite(final Composite parent, final int style) {
		this(parent, style, TWISTIE);
	}
	
	/**
	 * Creates the expandable composite in the provided parent.
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the control style (as expected by SWT subclass)
	 * @param expansionStyle
	 *            the style of the expansion widget (TREE_NODE, TWISTIE,
	 *            CLIENT_INDENT, COMPACT, FOCUS_TITLE,
	 *            LEFT_TEXT_CLIENT_ALIGNMENT, NO_TITLE)
	 */
	public ExpandableRowComposite(final Composite parent, final int style, final int expansionStyle) {
		super(parent, style);
		this.expansionStyle = expansionStyle;
		if ((expansionStyle & TITLE_BAR) != 0) {
			setBackgroundMode(SWT.INHERIT_DEFAULT);
		}
		super.setLayout(new ExpandableLayout());
		if (hasTitleBar()) {
			addPaintListener(new PaintListener() {
				@Override
				public void paintControl(final PaintEvent e) {
					onPaint(e);
				}
			});
		}
		if ((expansionStyle & TWISTIE) != 0) {
			toggle = new Toggle(this, SWT.NULL);
		}
		else {
			expanded = true;
		}
		if ((expansionStyle & EXPANDED) != 0) {
			expanded = true;
		}
		if (toggle != null) {
			toggle.setExpanded(expanded);
			toggle.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(final HyperlinkEvent e) {
					toggleState();
				}
			});
			toggle.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(final KeyEvent e) {
					if (e.keyCode == SWT.ARROW_UP) {
						verticalMove(false);
						e.doit = false;
					} else if (e.keyCode == SWT.ARROW_DOWN) {
						verticalMove(true);
						e.doit = false;
					}
				}
			});
			if ((getExpansionStyle() & FOCUS_TITLE)==0) {
				toggle.addFocusListener(new FocusListener() {
					@Override
					public void focusGained(final FocusEvent e) {
						if (textLabel != null) {
						    textLabel.redraw();
						} 
					}

					@Override
					public void focusLost(final FocusEvent e) {
						if (textLabel != null) {
						    textLabel.redraw();
						} 
					}
				});
			}
		}
		if ((expansionStyle & IMAGE) != 0) {
			imageLabel = new Label(this, SWT.NONE);
		}
		if ((expansionStyle & FOCUS_TITLE) != 0) {
			final Hyperlink link = new Hyperlink(this, SWT.NONE);
			link.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(final HyperlinkEvent e) {
					programmaticToggleState();
				}
			});
			textLabel = link;
		} else {
			final Label label = new Label(this, SWT.NONE);
			if (!isFixedStyle()) {
				final Listener listener = new Listener() {
					@Override
					public void handleEvent(final Event e) {
						switch (e.type) {
						case SWT.Paint:
							if (toggle != null && (getExpansionStyle() & NO_TITLE_FOCUS_BOX) == 0) {
								paintTitleFocus(e.gc);
							}
							break;
						case SWT.Resize:
							updateLabelTrim();
							break;
						}
					}
				};
				label.addListener(SWT.Paint, listener);
				label.addListener(SWT.Resize, listener);
			}
			textLabel = label;
		}
		if (textLabel != null) {
			textLabel.setMenu(getMenu());
			textLabel.addTraverseListener(new TraverseListener() {
				@Override
				public void keyTraversed(final TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_MNEMONIC) {
						// steal the mnemonic
						if (!isVisible() || !isEnabled()) {
							return;
						}
						if (FormUtil.mnemonicMatch(getText(), e.character)) {
							e.doit = false;
							if (!isFixedStyle()) {
							    programmaticToggleState();
							}
							setFocus();
						}
					}
				}
			});
		}
		{	// Complete Title header
			final Listener listener = new Listener() {
				@Override
				public void handleEvent(final Event event) {
					int x = event.x;
					int y = event.y;
					if (event.widget != ExpandableRowComposite.this) {
						final Point p = ((Control) event.widget).getLocation();
						x += p.x;
						y += p.y;
					}
					switch (event.type) {
					case SWT.MouseDown:
						if (titleHeaderRegion.contains(x, y)) {
							toggle.setFocus();
						}
						break;
					case SWT.MouseUp:
						if (titleHeaderRegion.contains(x, y) && event.button == 1) {
//							textLabel.setCursor(FormsResources.getBusyCursor());
							programmaticToggleState();
//							textLabel.setCursor(null);
						}
						break;
					case SWT.MouseEnter:
					case SWT.MouseMove:
					case SWT.MouseExit:
						if (toggle.getHover()) {
							if (!titleHeaderRegion.contains(x, y)) {
								toggle.setHover(false);
								toggle.redraw();
							}
						}
						else { // !toggle.getHover()
							if (titleHeaderRegion.contains(x, y)) {
								toggle.setHover(true);
								toggle.redraw();
							}
						}
						break;
//					case SWT.Paint:
//						if (toggle != null && (getExpansionStyle() & NO_TITLE_FOCUS_BOX) == 0) {
//							paintTitleFocus(event.gc);
//						}
//						break;
					}
				}
			};
			addListener(SWT.MouseDown, listener);
			addListener(SWT.MouseUp, listener);
			addListener(SWT.MouseEnter, listener);
			addListener(SWT.MouseMove, listener);
			addListener(SWT.MouseExit, listener);
			if (imageLabel != null) {
				imageLabel.addListener(SWT.MouseDown, listener);
				imageLabel.addListener(SWT.MouseUp, listener);
				imageLabel.addListener(SWT.MouseEnter, listener);
				imageLabel.addListener(SWT.MouseExit, listener);
			}
			textLabel.addListener(SWT.MouseDown, listener);
			textLabel.addListener(SWT.MouseUp, listener);
			textLabel.addListener(SWT.MouseEnter, listener);
			textLabel.addListener(SWT.MouseExit, listener);
		}
	}
	
	@Override
	public boolean forceFocus() {
		return false;
	}
	
	/**
	 * Overrides 'super' to pass the menu to the text label.
	 * 
	 * @param menu
	 *            the menu from the parent to attach to this control.
	 */
	@Override
	public void setMenu(final Menu menu) {
		if (toggle != null) {
			toggle.setMenu(menu);
		}
		if (imageLabel != null) {
			imageLabel.setMenu(menu);
		}
		if (textLabel != null) {
			textLabel.setMenu(menu);
		}
		super.setMenu(menu);
	}
	
	/**
	 * Prevents assignment of the layout manager - expandable composite uses its
	 * own layout.
	 */
	@Override
	public final void setLayout(final Layout layout) {
	}
	
	/**
	 * Sets the background of all the custom controls in the expandable.
	 */
	@Override
	public void setBackground(final Color bg) {
		super.setBackground(bg);
		if ((getExpansionStyle() & TITLE_BAR) == 0) {
			if (toggle != null) {
				toggle.setBackground(bg);
				toggle.setHoverDecorationColor(computeActiveToggleColor());
			}
			if (imageLabel != null) {
				imageLabel.setBackground(bg);
			}
			if (textLabel != null) {
				textLabel.setBackground(bg);
			}
		}
	}
	
	protected Color computeActiveToggleColor() {
		final Display display = getDisplay();
		final RGB selection = display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
		final RGB background = getBackground().getRGB();
		return SharedUIResources.getColors().getColor(new RGB(
				(63 + 3 * selection.red + 2 * background.red) / 6,
				(63 + 3 * selection.green + 2 *background.green) / 6,
				(63 + 3 * selection.blue + 2 * background.blue) / 6 ));
	}
	
	/**
	 * Sets the foreground of all the custom controls in the expandable.
	 */
	@Override
	public void setForeground(final Color fg) {
		super.setForeground(fg);
		if (textLabel != null) {
			textLabel.setForeground(fg);
		}
		if (toggle != null) {
			toggle.setForeground(fg);
		}
	}
	
	/**
	 * Sets the color of the toggle control.
	 * 
	 * @param c
	 *            the color object
	 */
	public void setToggleColor(final Color c) {
		if (toggle != null) {
			toggle.setDecorationColor(c);
		}
	}
	
	/**
	 * Sets the active color of the toggle control (when the mouse enters the
	 * toggle area).
	 * 
	 * @param c
	 *            the active color object
	 */
	public void setActiveToggleColor(final Color c) {
		if (toggle != null) {
			toggle.setHoverDecorationColor(c);
		}
	}
	
	/**
	 * Sets the fonts of all the custom controls in the expandable.
	 */
	@Override
	public void setFont(final Font font) {
		super.setFont(font);
		if (textLabel != null) {
			textLabel.setFont(font);
		}
		if (toggle != null) {
			toggle.setFont(font);
		}
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		if (textLabel != null) {
			textLabel.setEnabled(enabled);
		}
		if (toggle != null) {
			toggle.setEnabled(enabled);
		}
		super.setEnabled(enabled);
	}
	
	/**
	 * Sets the client of this expandable composite. The client must not be
	 * <samp>null </samp> and must be a direct child of this container.
	 * 
	 * @param client
	 *            the client that will be expanded or collapsed
	 */
	public void setClient(final Control client) {
		Assert.isTrue(client != null && client.getParent().equals(this));
		this.client = client;
	}
	
	/**
	 * Returns the current expandable client.
	 * 
	 * @return the client control
	 */
	public Control getClient() {
		return client;
	}
	
	public void setImage(final Image image) {
		if (imageLabel != null) {
			imageLabel.setImage(image);
		}
	}
	
	public Image getImage() {
		if (imageLabel != null) {
			return imageLabel.getImage();
		}
		return null;
	}
	
	/**
	 * Sets the title of the expandable composite. The title will act as a
	 * hyperlink and activating it will toggle the client between expanded and
	 * collapsed state.
	 * 
	 * @param title
	 *            the new title string
	 * @see #getText()
	 */
	public void setText(final String title) {
		if (textLabel instanceof Label) {
			((Label) textLabel).setText(title);
			updateLabelTrim();
		} else if (textLabel instanceof Hyperlink) {
			((Hyperlink) textLabel).setText(title);
		} else {
			return;
		}
		layout();
	}
	
	/**
	 * Returns the title string.
	 * 
	 * @return the title string
	 * @see #setText(String)
	 */
	public String getText() {
		if (textLabel instanceof Label) {
			return ((Label) textLabel).getText();
		}
		else if (textLabel instanceof Hyperlink) {
			return ((Hyperlink) textLabel).getText();
		}
		else {
			return ""; //$NON-NLS-1$
		}
	}
	
	/**
	 * Tests the expanded state of the composite.
	 * 
	 * @return <samp>true </samp> if expanded, <samp>false </samp> if collapsed.
	 */
	public boolean isExpanded() {
		return expanded;
	}
	
	/**
	 * Returns the bitwise-ORed style bits for the expansion control.
	 * 
	 * @return the bitwise-ORed style bits for the expansion control
	 */
	public int getExpansionStyle() {
		return expansionStyle;
	}
	
	/**
	 * Programmatically changes expanded state.
	 * 
	 * @param expanded
	 *            the new expanded state
	 */
	public void setExpanded(final boolean expanded) {
		internalSetExpanded(expanded);
		if (toggle != null) {
			toggle.setExpanded(expanded);
		}
	}
	
	/**
	 * Performs the expansion state change for the expandable control.
	 * 
	 * @param expanded
	 *            the expansion state
	 */
	protected void internalSetExpanded(final boolean expanded) {
		if (this.expanded != expanded) {
			this.expanded = expanded;
			if (getDescriptionControl() != null) {
				getDescriptionControl().setVisible(expanded);
			}
			if (client != null) {
				client.setVisible(expanded);
			}
			layout();
		}
	}
	
	/**
	 * Adds the listener that will be notified when the expansion state changes.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addExpansionListener(final IExpansionListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes the expansion listener.
	 * 
	 * @param listener
	 *            the listner to remove
	 */
	public void removeExpansionListener(final IExpansionListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * If TITLE_BAR or SHORT_TITLE_BAR style is used, title bar decoration will
	 * be painted behind the text in this method. The default implementation
	 * does nothing - subclasses are responsible for rendering the title area.
	 * 
	 * @param e
	 *            the paint event
	 */
	protected void onPaint(final PaintEvent e) {
	}

	/**
	 * Returns description control that will be placed under the title if
	 * present.
	 * 
	 * @return the description control or <samp>null </samp> if not used.
	 */
	protected Control getDescriptionControl() {
		return null;
	}
	
	/**
	 * Returns the separator control that will be placed between the title and
	 * the description if present.
	 * 
	 * @return the separator control or <samp>null </samp> if not used.
	 */
	protected Control getSeparatorControl() {
		return null;
	}
	
	/**
	 * Computes the size of the expandable composite.
	 * 
	 * @see org.eclipse.swt.widgets.Composite#computeSize
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		Point size;
		final ExpandableLayout layout = (ExpandableLayout) getLayout();
		if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
			size = layout.computeSize(this, wHint, hHint, changed);
		} else {
			size = new Point(wHint, hHint);
		}
		final Rectangle trim = computeTrim(0, 0, size.x, size.y);
		return new Point(trim.width, trim.height);
	}
	
	/**
	 * Returns <samp>true </samp> if the composite is fixed i.e. cannot be
	 * expanded or collapsed. Fixed control will still contain the title,
	 * separator and description (if present) as well as the client, but will be
	 * in the permanent expanded state and the toggle affordance will not be
	 * shown.
	 * 
	 * @return <samp>true </samp> if the control is fixed in the expanded state,
	 *         <samp>false </samp> if it can be collapsed.
	 */
	protected boolean isFixedStyle() {
		return (expansionStyle & TWISTIE) == 0;
	}
	
	/**
	 * Returns the text client control.
	 * 
	 * @return Returns the text client control if specified, or
	 *         <code>null</code> if not.
	 */
	public Control getTextClient() {
		return textClient;
	}
	
	/**
	 * Sets the text client control. Text client is a control that is a child of
	 * the expandable composite and is placed to the right of the text. It can
	 * be used to place small image hyperlinks. If more than one control is
	 * needed, use Composite to hold them. Care should be taken that the height
	 * of the control is comparable to the height of the text.
	 * 
	 * @param textClient
	 *            the textClient to set or <code>null</code> if not needed any
	 *            more.
	 */
	public void setTextClient(final Control textClient) {
		if (this.textClient != null) {
			this.textClient.dispose();
		}
		this.textClient = textClient;
	}
	
	/**
	 * Returns the difference in height between the text and the text client (if
	 * set). This difference can cause vertical alignment problems when two
	 * expandable composites are placed side by side, one with and one without
	 * the text client. Use this method obtain the value to add to either
	 * <code>descriptionVerticalSpacing</code> (if you have description) or
	 * <code>clientVerticalSpacing</code> to correct the alignment of the
	 * expandable without the text client.
	 * 
	 * @return the difference in height between the text and the text client or
	 *         0 if no corrective action is needed.
	 * @since 3.3
	 */
	public int getTextClientHeightDifference() {
		if (textClient == null || textLabel == null) {
			return 0;
		}
		final int theight = textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		final int tcheight = textClient.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		return Math.max(tcheight - theight, 0);
	}
	
	/**
	 * Tests if this expandable composite renders a title bar around the text.
	 * 
	 * @return <code>true</code> for <code>TITLE_BAR</code> or
	 *         <code>SHORT_TITLE_BAR</code> styles, <code>false</code>
	 *         otherwise.
	 */
	protected boolean hasTitleBar() {
		return (getExpansionStyle() & TITLE_BAR) != 0
				|| (getExpansionStyle() & SHORT_TITLE_BAR) != 0;
	}
	
	/**
	 * Sets the color of the title bar foreground when TITLE_BAR style is used.
	 * 
	 * @param color
	 *            the title bar foreground
	 */
	public void setTitleBarForeground(final Color color) {
		if (hasTitleBar()) {
			titleBarForeground = color;
		}
		if (textLabel != null) {
			textLabel.setForeground(color);
		}
	}
	
	/**
	 * Returns the title bar foreground when TITLE_BAR style is used.
	 * 
	 * @return the title bar foreground
	 */
	public Color getTitleBarForeground() {
		return titleBarForeground;
	}
	
	// end of APIs
	
	private void toggleState() {
		final boolean newState = !isExpanded();
		fireExpanding(newState, true);
		internalSetExpanded(newState);
		fireExpanding(newState, false);
		if (newState) {
			FormUtil.ensureVisible(this);
		}
	}
	
	private void fireExpanding(final boolean state, final boolean before) {
		final int size = listeners.size();
		if (size == 0) {
			return;
		}
		final ExpansionEvent e = new ExpansionEvent(this, state);
		final Object [] listenerList = listeners.getListeners();
		for (int i = 0; i < size; i++) {
			final IExpansionListener listener = (IExpansionListener) listenerList[i];
			if (before) {
				listener.expansionStateChanging(e);
			}
			else {
				listener.expansionStateChanged(e);
			}
		}
	}
	
	private void verticalMove(final boolean down) {
		final Composite parent = getParent();
		final Control[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			final Control child = children[i];
			if (child == this) {
				final ExpandableRowComposite sibling = getSibling(children, i, down);
				if (sibling != null && sibling.toggle != null) {
					sibling.setFocus();
				}
				break;
			}
		}
	}
	
	private ExpandableRowComposite getSibling(final Control[] children, final int index,
			final boolean down) {
		int loc = down ? index + 1 : index - 1;
		while (loc >= 0 && loc < children.length) {
			final Control c = children[loc];
			if (c instanceof ExpandableRowComposite && c.isVisible()) {
				return (ExpandableRowComposite) c;
			}
			loc = down ? loc + 1 : loc - 1;
		}
		return null;
	}
	
	private void programmaticToggleState() {
		if (toggle != null) {
			toggle.setExpanded(!toggle.isExpanded());
		}
		toggleState();
	}
	
	private void paintTitleFocus(final GC gc) {
		final Point size = textLabel.getSize();
		gc.setBackground(textLabel.getBackground());
		gc.setForeground(textLabel.getForeground());
		if (toggle.isFocusControl()) {
			gc.drawFocus(0, 0, size.x, size.y);
		}
	}
	
	private void updateLabelTrim() {
//		if (textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x > textLabel.getSize().x) {
//			textLabel.setToolTipText(((Label) textLabel).getText());
//		}
//		else {
//			textLabel.setToolTipText(null);
//		}
	}
	
}
