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

package de.walware.statet.rtm.ggplot.internal.ui.editors;

import org.eclipse.swt.widgets.Layout;

import de.walware.ecommons.emf.ui.forms.Detail;
import de.walware.ecommons.emf.ui.forms.DetailStack;
import de.walware.ecommons.emf.ui.forms.EFLayoutUtil;


public abstract class FacetDetail extends Detail {
	
	
	public FacetDetail(final DetailStack parent) {
		super(parent);
	}
	
	
	@Override
	protected Layout createContentLayout() {
		return EFLayoutUtil.createCompositeColumnGridLayout(3);
	}
	
}
