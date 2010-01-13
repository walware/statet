/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.text.InternalAccessor;
import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenKeeperExtension;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.ECommonsUI;


/**
 * Hover InformationControl manager for a ColumnViewer (instead of TextViewer).
 */
public abstract class ColumnHoverManager extends AbstractHoverInformationControlManager
		implements IWidgetTokenKeeper, IWidgetTokenKeeperExtension {
	
	
	private static final Anchor[] TREE_ANCHORS = new Anchor[] {
		ANCHOR_RIGHT, ANCHOR_LEFT,
	};
	
	/**
	 * Priority of the hovers managed by this manager.
	 * Value: {@value}
	 * @since 3.0
	 */
	public final static int WIDGET_PRIORITY = 0;
	
	
	private Composite fSubjectControl;
	
	/** The vertical margin when laying out the information control */
	private int fMarginY= 5;
	
	/** The horizontal margin when laying out the information control */
	private int fMarginX= 5;
	
	private TreeViewer fTreeViewer;
	private ColumnWidgetTokenOwner fColumnOwner;
	
	/** The hover information computation thread */
	private Thread fThread;
	/** The stopper of the computation thread */
//	private ITextListener fStopper;
	/** Internal monitor */
	private final Object fMutex = new Object();
	
	private volatile Object fTextHover; // not used
	
	
	protected ColumnHoverManager(final TreeViewer treeViewer, final ColumnWidgetTokenOwner tokenOwner, final IInformationControlCreator creator) {
		super(creator);
		
		fTreeViewer = treeViewer;
		fColumnOwner = tokenOwner;
		fSubjectControl = treeViewer.getTree();
		
		fSubjectControl.setToolTipText("");
		
		setAnchor(ANCHOR_LEFT);
		setFallbackAnchors(TREE_ANCHORS);
	}
	
	
	@Override
	protected void computeInformation() {
//		if (!fProcessMouseHoverEvent) {
//			setInformation(null, null);
//			return;
//		}
		
		final Point location = getHoverEventLocation();
		final ViewerCell cell = computeCellAtLocation(location);
		if (cell == null) {
			setInformation(null, null);
			return;
		}
		final Object element = prepareHoverInformation(cell);
		if (element == null) {
			setInformation(null, null);
			return;
		}
		final Rectangle textArea = cell.getTextBounds();
		final Rectangle imageArea = cell.getImageBounds();
		final Rectangle clientArea = fSubjectControl.getClientArea();
		final int x = Math.max(0, Math.min(textArea.x, imageArea.x));
		int x2 = (clientArea.width + location.x) / 2;
		x2 = Math.min(x2, location.x + 120);
//		final int x2 = clientArea.width;
		final Rectangle area = new Rectangle(x, textArea.y, x2 - x, textArea.height);
		
		synchronized (fMutex) {
			if (fThread != null) {
				setInfo(null, null);
				return;
			}
			
			fThread = new Thread("Structured Viewer Hover Presenter") { //$NON-NLS-1$
				@Override
				public void run() {
					boolean ok = false;
					try {
						if (fThread == null) {
							return;
						}
						final Object information = getHoverInformation(element);
						
	//					if (hover instanceof ITextHoverExtension)
	//						setCustomInformationControlCreator(((ITextHoverExtension) hover).getHoverControlCreator());
	//					else
	//						setCustomInformationControlCreator(null);
						
						synchronized (fMutex) {
							if (fThread != null && information != null) {
								setInfo(information, area);
								fThread = null;
								ok = true;
							}
						}
					}
					catch (final RuntimeException e) {
						StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID,
								IStatus.OK, "Unexpected runtime error while computing a element hover", e)); //$NON-NLS-1$
					}
					finally {
						if (!ok) {
							synchronized (fMutex) {
								setInfo(null, null);
								fThread = null;
							}
						}
					}
				}
			};
			
			fThread.setDaemon(true);
			fThread.setPriority(Thread.MIN_PRIORITY);
			fThread.start();
		}
	}
	
	public void stop() {
		synchronized (fMutex) {
			if (fThread != null) {
				fThread.interrupt();
				fThread = null;
			}
		}
	}
	
	private ViewerCell computeCellAtLocation(final Point location) {
		return fTreeViewer.getCell(location);
	}
	
	protected void setInfo(final Object information, final Rectangle subjectArea) {
		super.setInformation(information, subjectArea);
	}
	
	protected abstract Object prepareHoverInformation(ViewerCell cell);
	
	protected abstract Object getHoverInformation(final Object element);
	
	/**
	 * As computation is done in the background, this method is
	 * also called in the background thread. Delegates the control
	 * flow back into the UI thread, in order to allow displaying the
	 * information in the information control.
	 */
	@Override
	protected void presentInformation() {
		if (fSubjectControl == null)
			return;
		
		final Control control = fSubjectControl;
		if (control != null && !control.isDisposed()) {
			final Display display = control.getDisplay();
			if (display == null)
				return;
			
			display.asyncExec(new Runnable() {
				public void run() {
					ColumnHoverManager.super.presentInformation();
				}
			});
		}
	}
	
	/**
	 * Computes the area available for an information control given an anchor and the subject area
	 * within <code>bounds</code>.
	 * 
	 * @param subjectArea the subject area
	 * @param bounds the bounds
	 * @param anchor the anchor at the subject area
	 * @return the area available at the given anchor relative to the subject area, confined to the
	 *         monitor's client area
	 * @since 3.3
	 */
	@Override
	protected Rectangle computeAvailableArea(final Rectangle subjectArea, final Rectangle bounds, final Anchor anchor) {
		Rectangle area;
		if (anchor == ANCHOR_RIGHT) {
			final int x = (getHoverEventLocation().x - subjectArea.x) + fMarginX;
			area = new Rectangle(x, bounds.y, bounds.x + bounds.width - x, bounds.height);
			area.intersect(bounds);
			return area;
		}
		return super.computeAvailableArea(subjectArea, bounds, anchor);
	}
	
	
	@Override
	protected void showInformationControl(final Rectangle subjectArea) {
		if (fColumnOwner != null && fColumnOwner.requestWidgetToken(this, WIDGET_PRIORITY)) {
			super.showInformationControl(subjectArea);
		} else {
			if (DEBUG)
				System.out.println("TextViewerHoverManager#showInformationControl(..) did not get widget token"); //$NON-NLS-1$
		}
	}
	
	@Override
	protected void hideInformationControl() {
		try {
			fTextHover = null;
			super.hideInformationControl();
		}
		finally {
			if (fColumnOwner != null)
				fColumnOwner.releaseWidgetToken(this);
		}
	}
	
	@Override
	protected void handleInformationControlDisposed() {
		try {
			super.handleInformationControlDisposed();
		}
		finally {
			if (fColumnOwner != null)
				fColumnOwner.releaseWidgetToken(this);
		}
	}
	
	public boolean requestWidgetToken(final IWidgetTokenOwner owner) {
		fTextHover = null;
		super.hideInformationControl();
		return true;
	}
	
	public boolean requestWidgetToken(final IWidgetTokenOwner owner, final int priority) {
		if (priority > WIDGET_PRIORITY) {
			fTextHover = null;
			super.hideInformationControl();
			return true;
		}
		return false;
	}
	
	public boolean setFocus(final IWidgetTokenOwner owner) {
		final InternalAccessor accessor = getInternalAccessor();
		if (accessor.getInformationControlReplacer() == null) {
			return false;
		}
		final IInformationControl iControl = accessor.getCurrentInformationControl();
		if (accessor.canReplace(iControl)) {
//			if (cancelReplacingDelay()) {
				accessor.replaceInformationControl(true);
//			}
			return true;
		}
		
		return false;
	}
	
	
	@Override
	public void dispose() {
		stop();
		
		super.dispose();
	}
	
}
