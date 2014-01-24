/**
 * Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 */

package de.walware.statet.rtm.ggplot.ui.actions;

import de.walware.statet.rtm.base.ui.actions.AbstractNewRTaskFileWizard;
import de.walware.statet.rtm.ggplot.ui.RtGGPlotDescriptor;


public class NewGGPlotFileWizard extends AbstractNewRTaskFileWizard {
	
	
	public NewGGPlotFileWizard() {
		super(RtGGPlotDescriptor.INSTANCE);
	}
	
	
}
