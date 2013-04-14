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

package de.walware.ecommons.emf.core.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;


public class EFeatureReference {
	
	
	private final EObject fEObject;
	private final EStructuralFeature fEFeature;
	
	
	public EFeatureReference(final EObject eObject, final EStructuralFeature eFeature) {
		fEObject = eObject;
		fEFeature = eFeature;
	}
	
	
	public EObject getEObject() {
		return fEObject;
	}
	
	public EStructuralFeature getEFeature() {
		return fEFeature;
	}
	
	public Object getValue() {
		return fEObject.eGet(fEFeature);
	}
	
	
	@Override
	public int hashCode() {
		return fEObject.hashCode() + fEFeature.hashCode() * 17;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EFeatureReference)) {
			return false;
		}
		final EFeatureReference other = (EFeatureReference) obj;
		return (fEObject == other.fEObject
				&& fEFeature == other.fEFeature);
	}
	
	@Override
	public String toString() {
		return fEObject.toString() + " # " + fEFeature.toString(); //$NON-NLS-1$
	}
	
}
