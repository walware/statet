/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.rtool;

import static de.walware.ecommons.ltk.ui.sourceediting.assist.IInfoHover.MODE_FOCUS;

import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.swt.widgets.Shell;

import de.walware.statet.r.internal.debug.ui.assist.RElementInfoControl;


public class RElementInfoHoverCreator extends AbstractReusableInformationControlCreator {
	
	
	private final int mode;
	
	
	public RElementInfoHoverCreator(final int mode) {
		this.mode= mode;
	}
	
	
	@Override
	protected IInformationControl doCreateInformationControl(final Shell parent) {
		return ((this.mode & MODE_FOCUS) != 0) ? 
				new RElementInfoControl(parent, this.mode, true) :
				new RElementInfoControl(parent, this.mode);
	}
	
}
