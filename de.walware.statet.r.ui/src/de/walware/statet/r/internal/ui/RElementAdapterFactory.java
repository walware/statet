/*******************************************************************************
 * Copyright (c) 2000-2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation for JDT
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.search.ui.ISearchPageScoreComputer;
import org.eclipse.ui.IContributorResourceAdapter;

import de.walware.ecommons.ltk.ISourceElement;
import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.IWorkspaceSourceUnit;

import de.walware.statet.r.core.model.IRElement;


/**
 * Implements basic UI support for R elements.
 */
public class RElementAdapterFactory implements IAdapterFactory, IContributorResourceAdapter {
	
	
	private static Class<?>[] PROPERTIES = new Class<?>[] {
		IResource.class,
		IContributorResourceAdapter.class,
	};
	
	
	/*
	 * Do not use real type since this would cause
	 * the Search plug-in to be loaded.
	 */
	private Class<?> fSearchPageScoreComputerClass;
	private Object fSearchPageScoreComputer;
	
	
	@Override
	public Class<?>[] getAdapterList() {
		updateLazyLoadedAdapters();
		return PROPERTIES;
	}
	
	@Override
	public Object getAdapter(final Object adaptableObject, final Class key) {
		updateLazyLoadedAdapters();
		final IRElement element = getRElement(adaptableObject);
//		if (IPropertySource.class.equals(key)) {
//			return getProperties(java);
//		}
		if (IResource.class.equals(key)) {
			return getResource(element);
		}
		if (fSearchPageScoreComputerClass != null && fSearchPageScoreComputerClass.equals(key)) {
			return fSearchPageScoreComputer;
		}
//		if (IWorkbenchAdapter.class.equals(key)) {
//			return getRWorkbenchAdapter();
//		}
//		if (IPersistableElement.class.equals(key)) {
//			return new PersistableRElementFactory(java);
//		}
		if (IContributorResourceAdapter.class.equals(key)) {
			return this;
		}
//		if (IContributorResourceAdapter2.class.equals(key)) {
//			return this;
//		}
//		if (ITaskListResourceAdapter.class.equals(key)) {
//			return getTaskListAdapter();
//		}
//		if (IContainmentAdapter.class.equals(key)) {
//			return getRElementContainmentAdapter();
//		}
		return null;
	}
	
	private IResource getResource(final IRElement element) {
		switch (element.getElementType() & IRElement.MASK_C1) {
		case IRElement.C1_CLASS:
		case IRElement.C1_METHOD:
			// top level types behave like the CU
			if (element instanceof ISourceStructElement) {
				final ISourceStructElement parent = ((ISourceStructElement)element).getSourceParent();
				if ((parent.getElementType() & IRElement.MASK_C1) == IRElement.C1_SOURCE) {
					final ISourceUnit su = parent.getSourceUnit();
					if (su instanceof IWorkspaceSourceUnit) {
						return ((IWorkspaceSourceUnit) su).getResource();
					}
				}
			}
			return null;
		case IRElement.C1_SOURCE:
			if (element instanceof ISourceElement) {
				final ISourceUnit su = ((ISourceElement) element).getSourceUnit();
				if (su instanceof IWorkspaceSourceUnit) {
					return ((IWorkspaceSourceUnit) su).getResource();
				}
			}
			return null;
		default:
			return null;
		}
	}
	
	@Override
	public IResource getAdaptedResource(final IAdaptable adaptable) {
		final IRElement element = getRElement(adaptable);
		if (element != null) {
			return getResource(element);
		}
		return null;
	}
	
//	public IResourceMapping getAdaptedResourceMapping(final IAdaptable adaptable) {
//		final IRElement element = getRElement(adaptable);
//		if (element != null) {
//			return
//		}
//		return null;
//	}
	
	private IRElement getRElement(final Object element) {
		if (element instanceof IRElement) {
			return (IRElement) element;
		}
		return null;
	}
	
	private synchronized void updateLazyLoadedAdapters() {
		if (fSearchPageScoreComputerClass == null && RUIPlugin.isSearchPlugInActivated()) {
			createSearchPageScoreComputer();
		}
	}
	
	private void createSearchPageScoreComputer() {
		fSearchPageScoreComputerClass = ISearchPageScoreComputer.class;
		fSearchPageScoreComputer = new RSearchPageScoreComputer();
		final Class<?>[] newProperties = new Class[PROPERTIES.length+1];
		System.arraycopy(PROPERTIES, 0, newProperties, 0, PROPERTIES.length);
		newProperties[PROPERTIES.length] = fSearchPageScoreComputerClass;
		PROPERTIES = newProperties;
	}
	
//	private RWorkbenchAdapter getRWorkbenchAdapter() {
//		if (fWorkbenchAdapter == null)
//			fWorkbenchAdapter =;
//		return fWorkbenchAdapter;
//	}
	
}
