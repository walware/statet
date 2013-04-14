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

package de.walware.statet.rtm.ftable.internal.ui.editors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.walware.ecommons.emf.ui.forms.EFProperty;

import de.walware.statet.rtm.base.ui.rexpr.RExprListProperty;
import de.walware.statet.rtm.base.ui.rexpr.RExprValueProperty;


public class FTableProperties {
	
	
	private static FTableExprTypesUIProvider UI_ADAPTER_FACTORY = new FTableExprTypesUIProvider();
	
	
	public static EFProperty createProperty(
			final EClass eClass, final EStructuralFeature eFeature,
			final String label, final String tooltip) {
		if (!eClass.getEAllStructuralFeatures().contains(eFeature)) {
			return null;
		}
		if (eFeature.getUpperBound() == 1) {
			return new RExprValueProperty(label, tooltip, eClass, eFeature,
					UI_ADAPTER_FACTORY );
		}
		return new RExprListProperty(label, tooltip, eClass, eFeature,
				UI_ADAPTER_FACTORY );
	}
	
}
