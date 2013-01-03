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

package de.walware.statet.r.internal.ui.datafilterview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;


public class ScrolledPageComposite extends SharedScrolledComposite {
	
	
	private static FormToolkit gDialogsFormToolkit;
	
	private static FormToolkit getViewFormToolkit() {
		if (gDialogsFormToolkit == null) {
			final FormColors colors = new FormColors(Display.getCurrent());
			gDialogsFormToolkit = new FormToolkit(colors);
		}
		return gDialogsFormToolkit;
	}
	
	
	private final FormToolkit fToolkit;
	
	private IExpansionListener fExpansionListener;
	
	private int fDelayReflowCounter;
	
	
	public ScrolledPageComposite(final Composite parent) {
		this(parent, SWT.H_SCROLL | SWT.V_SCROLL);
	}
	
	public ScrolledPageComposite(final Composite parent, final int style) {
		super(parent, style);
		
		if ((style & SWT.H_SCROLL) == 0) {
			setExpandHorizontal(true);
		}
		
		setFont(parent.getFont());
		
		fToolkit = getViewFormToolkit();
		
		setExpandHorizontal(true);
		setExpandVertical(true);
		
		final Composite body = new Composite(this, SWT.NONE);
		setContent(body);
		
		setFont(parent.getFont());
		setBackground(fToolkit.getColors().getBackground());
	}
	
	
	@Override
	public Composite getContent() {
		return (Composite) super.getContent();
	}
	
	@Override
	public void setDelayedReflow(final boolean delayedReflow) {
		if (delayedReflow) {
			fDelayReflowCounter++;
		}
		else {
			fDelayReflowCounter--;
		}
		super.setDelayedReflow(fDelayReflowCounter > 0);
	}
	
	public void adaptChild(final Control childControl) {
		if (childControl instanceof ExpandableRowComposite) {
			fToolkit.adapt(childControl, true, false);
			if (fExpansionListener == null) {
				fExpansionListener = new ExpansionAdapter() {
					@Override
					public void expansionStateChanged(final ExpansionEvent e) {
						expandedStateChanged();
					}
				};
			}
			((ExpandableRowComposite) childControl).addExpansionListener(fExpansionListener);
		}
		else if (childControl instanceof Composite) {
			fToolkit.adapt(childControl, false, false);
			final Control[] children = ((Composite) childControl).getChildren();
			for (int i = 0; i < children.length; i++) {
				fToolkit.adapt(children[i], true, false);
			}
		}
		else {
			fToolkit.adapt(childControl, true, false);
		}
	}
	
	protected void expandedStateChanged() {
		reflow(true);
	}
	
}
