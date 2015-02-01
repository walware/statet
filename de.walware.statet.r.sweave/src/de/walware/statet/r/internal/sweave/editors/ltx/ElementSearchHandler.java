/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.editors.ltx;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import de.walware.statet.r.internal.sweave.editors.ForwardHandler;
import de.walware.statet.r.sweave.text.LtxRweaveSwitch;
import de.walware.statet.r.ui.editors.RElementSearchHandler;


public class ElementSearchHandler extends ForwardHandler implements IExecutableExtension {
	
	
	private String commandId;
	
	
	public ElementSearchHandler() {
		super();
	}
	
	
	@Override
	public void setInitializationData(final IConfigurationElement config,
			final String propertyName, final Object data) throws CoreException {
		if (this.commandId != null) {
			return;
		}
		final String s= config.getAttribute("commandId"); //$NON-NLS-1$
		if (s != null && !s.isEmpty()) {
			this.commandId= s.intern();
		}
	}
	
	@Override
	protected IHandler2 createHandler(final LtxRweaveSwitch type) {
		switch (type) {
		case R:
			return new RElementSearchHandler(this.commandId);
		default:
			return null;
		}
	}
	
}
