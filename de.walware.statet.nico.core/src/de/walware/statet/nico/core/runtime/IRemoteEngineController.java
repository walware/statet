/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Interface for feature set to be implemented by tool controllers of remote engines.
 */
public interface IRemoteEngineController {
	
	
	public static final String FEATURE_SET_ID = "de.walware.statet.general/remote"; //$NON-NLS-1$
	
	public static final String LAUNCH_RECONNECT_ATTRIBUTE = "reconnect"; //$NON-NLS-1$
	
	
	public void disconnect(IProgressMonitor monitor) throws CoreException;
	
	public boolean isDisconnected();
	
	
}
