/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.emf.ui.forms;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Control;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.emf.core.util.IEMFEditContext;


public class EFDataBindingSupport extends DataBindingSupport implements IEMFEditContext {
	
	
	private final EFEditor fEditor;
	
	private final WritableValue fRootObject;
	
	private final IAdapterFactory fAdapterFactory;
	
	
	public EFDataBindingSupport(final EFEditor editor, final IAdapterFactory adapterFactory) {
		super((Control) editor.getAdapter(Control.class));
		fEditor = editor;
		
		final Resource resource = fEditor.getEditingDomain().getResourceSet().getResources().get(0);
		fRootObject = new WritableValue(getRealm(), getRootObject(resource), EObject.class);
		resource.eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(final Notification notification) {
				if (notification.getFeatureID(Resource.class) == Resource.RESOURCE__IS_LOADED
						&& notification.getNewBooleanValue()) {
					getRealm().exec(new Runnable() {
						@Override
						public void run() {
							final EObject rootObject = getRootObject((Resource) notification.getNotifier());
							fRootObject.setValue(rootObject);
						}
					});
				}
			}
		});
		
		fAdapterFactory = adapterFactory;
	}
	
	@Override
	protected DataBindingContext createContext(final Realm realm) {
		return new EMFDataBindingContext(realm);
	}
	
	
	protected EObject getRootObject(final Resource resource) {
		return resource.getContents().get(0);
	}
	
	@Override
	public EditingDomain getEditingDomain() {
		return fEditor.getEditingDomain();
	}
	
	public IObservableValue getRootObservable() {
		return fRootObject;
	}
	
	@Override
	public DataBindingContext getDataBindingContext() {
		return getContext();
	}
	
	@Override
	public IObservableValue getBaseObservable() {
		return getRootObservable();
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.isInstance(this)) {
			return this;
		}
		if (required.equals(IShellProvider.class)) {
			return fEditor.getSite();
		}
		if (required.equals(EFEditor.class)) {
			return fEditor;
		}
		if (fAdapterFactory != null) {
			return fAdapterFactory.getAdapter(this, required);
		}
		return null;
	}
	
}
