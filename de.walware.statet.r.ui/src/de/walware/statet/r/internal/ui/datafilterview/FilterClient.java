/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.datafilterview;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.internal.ui.datafilter.IFilterListener;
import de.walware.statet.r.internal.ui.datafilter.VariableFilter;


public abstract class FilterClient extends Composite implements IFilterListener {
	
	
	protected static final String SELECT_ALL_COMMAND_ID = IWorkbenchCommandConstants.EDIT_SELECT_ALL;
	protected static final String REMOVE_COMMAND_ID = IWorkbenchCommandConstants.EDIT_DELETE;
	protected static final String REMOVE_ALL_HANDLER_COMMAND_ID = "RemoveAll"; //$NON-NLS-1$
	protected static final String REMOVE_UNCHECKED_HANDLER_ID = "RemoveUnchecked"; //$NON-NLS-1$
	
	
	private DataBindingSupport fDataBinding;
	
	private Listener fResizeListener;
	
	
	public FilterClient(final VariableComposite parent) {
		super(parent, SWT.NONE);
		
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				onDispose();
			}
		});
	}
	
	
	@Override
	public VariableComposite getParent() {
		return (VariableComposite) super.getParent();
	}
	
	public abstract VariableFilter getFilter();
	
	protected void init(final int numColumns) {
		getParent().setClient(this);
		
		setLayout(LayoutUtil.createCompositeGrid(numColumns));
		
		addWidgets();
		LayoutUtil.addSmallFiller(this, false);
		initActions(getParent().getContainer().getServiceLocator());
		initBindings();
		updateInput();
	}
	
	
	protected abstract void addWidgets();
	
	protected abstract void initActions(IServiceLocator serviceLocator);
	
	@Override
	public void filterChanged() {
		if (isDisposed()) {
			return;
		}
		updateInput();
	}
	
	protected abstract void updateInput();
	
	protected void initBindings() {
		fDataBinding = new DataBindingSupport(this);
		addBindings(fDataBinding);
		fDataBinding.getContext().updateTargets();
	}
	
	protected void addBindings(final DataBindingSupport db) {
	}
	
	protected DataBindingSupport getDataBinding() {
		return fDataBinding;
	}
	
	
	protected void installResizeListener() {
		if (fResizeListener == null) {
			fResizeListener = new Listener() {
				@Override
				public void handleEvent(final Event event) {
					if (updateLayout()) {
						getParent().layout(new Control[] { FilterClient.this });
					}
				}
			};
			getParent().getContainer().getVariableComposite().addListener(SWT.Resize, fResizeListener);
		}
	}
	
	protected boolean updateLayout() {
		return false;
	}
	
	protected boolean updateLayout(final TableViewer viewer, final int count) {
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, ((GridLayout) getLayout()).numColumns, 1);
		if (count == 0) {
			data.exclude = true;
		}
		else {
			data.heightHint = LayoutUtil.hintHeight(viewer.getTable(), count, false);
			final int max = Math.max(5 * viewer.getTable().getItemHeight(),
					getParent().getContainer().getVariableComposite().getClientArea().height
							- getMinHeightPadding() );
			if (data.heightHint > max) {
				data.heightHint = max;
			}
			installResizeListener();
		}
		
		{	final Control control = viewer.getControl();
			final GridData oldData = ((GridData) control.getLayoutData());
			if (oldData == null || oldData.exclude != data.exclude || oldData.heightHint != data.heightHint) {
				control.setVisible(!data.exclude);
				control.setLayoutData(data);
				return true;
			}
			return false;
		}
	}
	
	protected int getMinHeightPadding() {
		return 20 + 8 * LayoutUtil.defaultVSpacing();
	}
	
	@Override
	public void layout(boolean changed, final boolean all) {
		changed |= updateLayout();
		super.layout(changed, all);
	}
	
	@Override
	public void layout(final Control[] changed, int flags) {
		if (updateLayout()) {
			flags |= SWT.CHANGED;
		}
		super.layout(changed, flags);
	}
	
	
	protected void onDispose() {
	}
	
}
