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

package de.walware.statet.rtm.ggplot.internal.ui.editors;

import org.eclipse.emf.ecore.EClass;

import de.walware.ecommons.emf.ui.forms.DetailStack;
import de.walware.ecommons.emf.ui.forms.MainDetail;


public abstract class LayerDetail extends MainDetail {
	
	
	private final EClass fEClass;
	
	
	public LayerDetail(final DetailStack parent, final EClass eClass) {
		super(parent);
		
		fEClass = eClass;
		
		createContent();
	}
	
	
	protected EClass getEClass() {
		return fEClass;
	}
	
}
