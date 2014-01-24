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

package de.walware.ecommons.emf.core.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;


public class RuleSet {
	
	
	public static final String PARENT_FEATURES_ID_SUFFIX = ".parent.features"; //$NON-NLS-1$
	public static final String DISJOINT_FEATURES_ID = "disjoint.features"; //$NON-NLS-1$
	
	
	public Object get(final EObject obj, final EStructuralFeature eFeature, final String ruleType) {
		return null;
	}
	
}
