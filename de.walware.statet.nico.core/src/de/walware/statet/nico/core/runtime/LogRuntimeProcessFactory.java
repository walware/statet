/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core.runtime;

import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;


public class LogRuntimeProcessFactory implements IProcessFactory {
	
	
	public static final String FACTORY_ID = "de.walware.statet.nico.LogRuntimeProcessFactory"; //$NON-NLS-1$
	
	
	public LogRuntimeProcessFactory() {
	}
	
	
	@Override
	public IProcess newProcess(final ILaunch launch, final Process process, final String label,
			final Map attributes) {
		return new LogRuntimeProcess(launch, process, label, attributes);
	}
	
}
