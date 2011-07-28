/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.sourcelookup;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;

import de.walware.ecommons.ts.ITool;
import de.walware.statet.r.debug.core.sourcelookup.IRSourceLookupMatch;
import de.walware.statet.r.debug.core.sourcelookup.RRuntimeSourceFragment;
import de.walware.statet.r.internal.debug.ui.RDebugUIPlugin;


public class RSourceLookupAdapterFactory implements IAdapterFactory,
		IWorkbenchAdapter, IContributorResourceAdapter {
	
	
	private static final Class[] ADAPTERS = new Class[] {
		IWorkbenchAdapter.class,
		IContributorResourceAdapter.class,
	};
	
	
	/** Created via extension point */
	public RSourceLookupAdapterFactory() {
	}
	
	
	public Class[] getAdapterList() {
		return ADAPTERS;
	}
	
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType == IWorkbenchAdapter.class
				|| adapterType == IContributorResourceAdapter.class) {
			return this;
		}
		return null;
	}
	
	
	public Object[] getChildren(final Object o) {
		return null;
	}
	
	public Object getParent(final Object o) {
		return null;
	}
	
	public ImageDescriptor getImageDescriptor(final Object obj) {
		if (obj instanceof IRSourceLookupMatch) {
			final Object element = ((IRSourceLookupMatch) obj).getElement();
			if (element instanceof RRuntimeSourceFragment) {
				return RDebugUIPlugin.getDefault().getImageRegistry().getDescriptor(
						RDebugUIPlugin.IMG_OBJ_R_SOURCE_FROM_RUNTIME );
			}
			if (element instanceof IAdaptable) {
				final IWorkbenchAdapter adapter = (IWorkbenchAdapter) Platform.getAdapterManager()
						.getAdapter(element, IWorkbenchAdapter.class);
				if (adapter != null) {
					return adapter.getImageDescriptor(element);
				}
			}
		}
		return null;
	}
	
	public String getLabel(final Object obj) {
		if (obj instanceof IRSourceLookupMatch) {
			final Object element = ((IRSourceLookupMatch) obj).getElement();
			if (element instanceof RRuntimeSourceFragment) {
				final RRuntimeSourceFragment fragment = (RRuntimeSourceFragment) element;
				return fragment.getName() + "  \u2012  " + fragment.getProcess().getLabel(ITool.DEFAULT_LABEL);
			}
			if (element instanceof IAdaptable) {
				final IWorkbenchAdapter adapter = (IWorkbenchAdapter) Platform.getAdapterManager()
						.getAdapter(element, IWorkbenchAdapter.class);
				if (adapter != null) {
					return adapter.getLabel(element);
				}
			}
			return element.toString();
		}
		return obj.toString();
	}
	
	public IResource getAdaptedResource(final IAdaptable obj) {
		if (obj instanceof IRSourceLookupMatch) {
			final Object element = ((IRSourceLookupMatch) obj).getElement();
			if (element instanceof IResource) {
				return (IResource) element;
			}
		}
		return null;
	}
	
}
