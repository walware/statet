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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.databinding.DataBindingSubContext;
import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.databinding.DetailContext;


public class DetailStack extends Composite {
	
	
	private static class EmptyDetail extends Detail {
		
		public EmptyDetail(final DetailStack parent) {
			super(parent);
			
			createContent();
		}
		
		@Override
		protected Composite createComposite(final DetailStack parent) {
			return new Composite(parent, SWT.NONE);
		}
		
		@Override
		protected void createContent(final Composite composite) {
		}
		
	}
	
	private class DetailEntry {
		
		private final Object key;
		private final Detail detail;
		
		private IEMFEditContext context;
		private DataBindingSubContext dbc;
		
		public DetailEntry(final Object key, final Detail detail) {
			this.key = key;
			this.detail = detail;
		}
		
	}
	
	
	private final IEFFormPage fPage;
	
	private final StackLayout fLayout;
	
	private final List<DetailEntry> fDetails;
	
	private final int fLimit = 10;
	
	private IEMFEditContext fContext;
	
	private DetailEntry fCurrentDetail;
	private EObject fCurrentValue;
	
	
	public DetailStack(final IEFFormPage page, final Composite parent) {
		super(parent, SWT.NONE);
		
		fPage = page;
		
		fLayout = new StackLayout();
		setLayout(fLayout);
		
		fDetails = new ArrayList<DetailEntry>(fLimit + 1);
	}
	
	
	public IEFFormPage getPage() {
		return fPage;
	}
	
	protected void dispose(final DetailEntry entry) {
		{	final Composite composite = entry.detail.getComposite();
			if (composite != null && !composite.isDisposed()) {
				composite.dispose();
			}
		}
		if (entry.dbc != null) {
			entry.dbc.dispose();
			entry.dbc = null;
		}
	}
	
	public void showDetail(final EObject value) {
		int index = 0;
		final Object key = getKey(value);
		DetailEntry entry = null;
		for (; index < fDetails.size(); index++) {
			final DetailEntry detail = fDetails.get(index);
			if (key == detail.key) {
				entry = detail;
				break;
			}
		}
		if (entry == null) {
			final Detail detail = createDetail(value);
			entry = new DetailEntry(key, detail);
			fDetails.add(0, entry);
			initDetailBindings(entry, value);
		}
		else {
			if (entry.dbc == null) {
				initDetailBindings(entry, value);
			}
			if (index != 0) {
				fDetails.remove(index);
				fDetails.add(0, entry);
			}
		}
		
		fCurrentDetail = fDetails.get(0);
		fCurrentValue = value;
		fLayout.topControl = fCurrentDetail.detail.getComposite();
		layout();
		getParent().layout(new Control[] { this });
		getPage().reflow(true);
		
		if (fDetails.size() > fLimit) {
			dispose(fDetails.remove(fDetails.size() - 1));
		}
	}
	
	protected Object getKey(final EObject value) {
		return value;
	}
	
	
	protected Detail createDetail(final EObject value) {
		return new EmptyDetail(this);
	}
	
	protected void createDetailContent(final Composite composite, final EObject value) {
		if (value == null) {
			return;
		}
		final Label label = new Label(composite, SWT.NONE);
		label.setText(value.toString());
	}
	
	public void setContext(final IEMFEditContext context) {
		fContext = context;
		
		final IObservableValue baseValue = context.getBaseObservable();
		baseValue.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				showDetail((EObject) event.diff.getNewValue());
			}
		});
		showDetail((EObject) baseValue.getValue());
	}
	
	protected IEMFEditContext createDetailContext(final IEMFEditContext parent,
			final IObservableValue detailValue) {
		return new DetailContext(parent, detailValue);
	}
	
	private void initDetailBindings(final DetailEntry entry, final EObject value) {
		final IEMFEditContext context = fContext;
		if (context == null || entry.dbc != null) {
			if (entry.context != null) {
				entry.context.getBaseObservable().setValue(value);
			}
			return;
		}
		final AtomicReference<RuntimeException> error = new AtomicReference<RuntimeException>();
		
		entry.dbc = new DataBindingSubContext(context.getDataBindingContext());
		entry.dbc.run(new Runnable() {
			@Override
			public void run() {
				try {
					final WritableValue contextValue = new WritableValue(context.getRealm(),
							value, EObject.class );
					entry.context = createDetailContext(context, contextValue);
					entry.detail.addBindings(entry.context);
				}
				catch (final RuntimeException e) {
					error.set(e);
				}
			}
		});
		
		if (error.get() != null) {
			throw error.get();
		}
	}
	
//	@Override
//	public Point computeSize(int wHint, int hHint, boolean changed) {
//		Point size = super.computeSize(wHint, hHint, changed);
//		System.out.println("" + wHint + ", " + hHint + " -> " + size);
//		return size;
//	}
	
}
