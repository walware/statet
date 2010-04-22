/*******************************************************************************
 * Copyright (c) 2000-2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation for JDT
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.search.ui.ISearchPageScoreComputer;


/**
 * Implements basic UI support for R editor input.
 */
public class REditorInputAdapterFactory implements IAdapterFactory {
	
	
	private static Class<?>[] PROPERTIES = new Class[] {
//		IRElement.class,
	};
	
	/*
	 * Do not use real type since this would cause
	 * the Search plug-in to be loaded.
	 */
	private Class<?> fSearchPageScoreComputerClass;
	private Object fSearchPageScoreComputer;
	
	
	public REditorInputAdapterFactory() {
	}
	
	
	public Class<?>[] getAdapterList() {
		updateLazyLoadedAdapters();
		return PROPERTIES;
	}
	
	public Object getAdapter(final Object adaptableObject, final Class key) {
		updateLazyLoadedAdapters();
		if (fSearchPageScoreComputerClass != null && fSearchPageScoreComputerClass.equals(key)) {
			return fSearchPageScoreComputer;
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
	
}
