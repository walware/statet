/*******************************************************************************
 * Copyright (c) 2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.rtm.ggplot.internal.ui.editors;

import org.eclipse.jface.viewers.IFilter;

import de.walware.statet.rtm.ggplot.GGPlot;
import de.walware.statet.rtm.ggplot.Layer;
import de.walware.statet.rtm.ggplot.TextStyle;


public class LayerTextStyleFilter implements IFilter {
	
	
	public static final IFilter INSTANCE = new LayerTextStyleFilter();
	
	
	public LayerTextStyleFilter() {
	}
	
	
	@Override
	public boolean select(final Object toTest) {
		return (toTest instanceof GGPlot
				|| (toTest instanceof Layer && toTest instanceof TextStyle) );
	}
	
}
