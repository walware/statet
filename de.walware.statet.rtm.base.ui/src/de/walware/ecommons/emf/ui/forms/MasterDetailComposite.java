/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.ui.forms;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;


public class MasterDetailComposite extends SashForm {
	
	
	private class SashListener implements Listener {
		
		
		private boolean fHover;
		
		
		public SashListener(final Sash sash) {
			sash.addListener(SWT.MouseEnter, this);
			sash.addListener(SWT.MouseExit, this);
			sash.addListener(SWT.Paint, this);
			sash.addListener(SWT.Dispose, this);
		}
		
		
		@Override
		public void handleEvent(final Event event) {
			switch (event.type) {
			case SWT.MouseEnter:
				fHover = true;
				((Sash) event.widget).redraw();
				break;
			case SWT.MouseExit:
				fHover = false;
				((Sash) event.widget).redraw();
				break;
			case SWT.Paint:
				paintSash(event);
				break;
			case SWT.Dispose:
				fSashes.remove(event.widget);
				break;
			}
		}
		
		private void paintSash(final Event event) {
			final Sash sash = (Sash) event.widget;
			final IManagedForm form = fForm;
			final FormColors colors = form.getToolkit().getColors();
			final GC gc = event.gc;
			gc.setBackground(colors.getColor(IFormColors.TB_BG));
			gc.setForeground(colors.getColor(IFormColors.TB_BORDER));
			final Point size = sash.getSize();
			if ((sash.getStyle() & SWT.VERTICAL) != 0) {
				if (fHover) {
					gc.fillRectangle(0, 0, size.x, size.y);
				//else
					//gc.drawLine(1, 0, 1, size.y-1);
				}
			}
			else {
				if (fHover) {
					gc.fillRectangle(0, 0, size.x, size.y);
				//else
					//gc.drawLine(0, 1, size.x-1, 1);
				}
			}
		}
		
	};
	
	private final Composite fMaster;
	private final Composite fDetail;
	
	private boolean fCheckLayout;
	private final Set<Sash> fSashes = new HashSet<Sash>();
	
	private final IManagedForm fForm;
	
	
	public MasterDetailComposite(final Composite parent, final IManagedForm managedForm) {
		super(parent, SWT.HORIZONTAL);
		
		fForm = managedForm;
		
		final FormToolkit toolkit = managedForm.getToolkit();
		toolkit.adapt(this, false, false);
		
		fMaster = toolkit.createComposite(this);
		fMaster.setLayout(EFLayoutUtil.createMainSashLeftLayout(1));
		fDetail = toolkit.createComposite(this);
		fDetail.setLayout(EFLayoutUtil.createMainSashRightLayout(1));
		
		setWeights(new int[] { 40, 60 });
		
		fCheckLayout = true;
		
		final Listener listener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				checkSashes();
			}
		};
		fMaster.addListener(SWT.Resize, listener);
		fDetail.addListener(SWT.Resize, listener);
	}
	
	
	public Composite getMasterContainer() {
		return fMaster;
	}
	
	public Composite getDetailContainer() {
		return fDetail;
	}
	
	
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		if (fCheckLayout) {
			checkChildren();
		}
		return super.computeSize(wHint, hHint, changed);
	}
	
	@Override
	public void layout(final boolean changed, final boolean all) {
		if (fCheckLayout) {
			checkChildren();
		}
		super.layout(changed, all);
	}
	
	@Override
	public void layout(final Control[] changed, final int flags) {
		if (fCheckLayout) {
			checkChildren();
		}
		super.layout(changed, flags);
	}
	
	@Override
	public void setBounds(final int x, final int y, final int width, final int height) {
		if (fCheckLayout) {
			checkChildren();
		}
		super.setBounds(x, y, width, height);
	}
	
	
	public void checkChildren() {
		fCheckLayout = false;
		{	final Control[] children = fMaster.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i].getLayoutData() == null) {
//					children[i].setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
					children[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				}
			}
		}
		{	final Control[] children = fDetail.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i].getLayoutData() == null) {
					children[i].setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL));
//					children[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				}
			}
		}
	}
	
	private void checkSashes() {
		{	final Control [] children = getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof Sash) {
					final Sash sash = (Sash) children[i];
					if (fSashes.contains(sash)) {
						continue;
					}
					new SashListener(sash);
					fSashes.add(sash);
				}
			}
		}
	}
	
}
