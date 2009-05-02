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

package de.walware.ecommons.ui;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.walware.ecommons.ui.dialogs.SearchText;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public class SearchContributionItem extends ContributionItem {
	
	
	// E-3.5 remove workaround
	public static final String WORKAROUND = StatetUIPlugin.PLUGIN_ID + "/" + "searchbar-workaround";
	
	public static boolean requiresWorkaround() {
		return StatetUIPlugin.getDefault().getPluginPreferences().getBoolean(WORKAROUND);
	}
	
	
	private SearchText fControl;
	private ToolItem fTextItem;
	
	private String fToolTipText;
	
	private Composite fSizeControl;
	private Control fResultControl;
	
	private boolean fUpdateWhenTyping;
	
	
	public SearchContributionItem(final String id, final boolean updateWhenTyping) {
		super(id);
		fUpdateWhenTyping = updateWhenTyping;
	}
	
	
	public SearchText getSearchText() {
		return fControl;
	}
	
	/**
	 * Table or tree to select
	 */
	public void setResultControl(final Control control) {
		fResultControl = control;
	}
	
	public void setToolTip(final String text) {
		fToolTipText = text;
	}
	
	/**
	 * For views the control of the view
	 */
	public void setSizeControl(final Composite control) {
		fSizeControl = control;
		fSizeControl.addListener(SWT.Resize, new Listener() {
			public void handleEvent(final Event event) {
				resize();
			}
		});
	}
	
	public void resize() {
		if (fTextItem != null && !fTextItem.isDisposed() 
				&& fSizeControl != null) {
			final int viewWidth = fSizeControl.getClientArea().width;
			if (viewWidth <= 0) {
				return;
			}
			final ToolBar toolBar = fTextItem.getParent();
			final int toolBarWidth = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			final int minWidth = LayoutUtil.hintWidth(fControl.getTextControl(), 8);
			fTextItem.setWidth(Math.min(310,
					Math.max(minWidth, viewWidth - toolBarWidth + fTextItem.getWidth() - 26)));
			toolBar.layout(new Control[] { fControl, fControl.getTextControl() });
			toolBar.getParent().layout(true, true);
		}
	}
	
	@Override
	public void fill(final ToolBar parent, final int index) {
		fControl = new SearchText(parent) {
			@Override
			protected void okPressed() {
				SearchContributionItem.this.search();
			}
			@Override
			protected void downPressed() {
				SearchContributionItem.this.selectFirst();
			}
			@Override
			protected void textChanged() {
				if (fUpdateWhenTyping) {
					SearchContributionItem.this.search();
				}
			}
		};
		fControl.setToolTipText(fToolTipText);
		
		fTextItem = new ToolItem(parent, SWT.SEPARATOR, index);
		fTextItem.setControl(fControl);
		fTextItem.setToolTipText(fToolTipText);
		fTextItem.setWidth(310); // high value prevents that the toolbar is moved to tabs
	}
	
	public Control create(final Composite parent) {
		fControl = new SearchText(parent) {
			@Override
			protected void okPressed() {
				SearchContributionItem.this.search();
			}
			@Override
			protected void downPressed() {
				SearchContributionItem.this.selectFirst();
			}
			@Override
			protected void textChanged() {
				if (fUpdateWhenTyping) {
					SearchContributionItem.this.search();
				}
			}
		};
		fControl.setToolTipText(fToolTipText);
		return fControl;
	}
	
	protected void search() {
	}
	
	protected void selectFirst() {
		if (fResultControl instanceof Table) {
			final Table table = (Table) fResultControl;
			table.setFocus();
			if (table.getSelectionCount() == 0) {
				final int idx = table.getTopIndex();
				if (idx >= 0) {
					table.setSelection(idx);
				}
			}
		}
		else if (fResultControl instanceof Tree) {
			final Tree table = (Tree) fResultControl;
			table.setFocus();
			if (table.getSelectionCount() == 0) {
				final TreeItem item = table.getTopItem();
				if (item != null) {
					table.setSelection(item);
				}
			}
		}
	}
	
	public String getText() {
		return fControl.getText();
	}
	
	public void show() {
		if (!UIAccess.isOkToUse(fControl)) {
			return;
		}
		fControl.setFocus();
	}
	
}
