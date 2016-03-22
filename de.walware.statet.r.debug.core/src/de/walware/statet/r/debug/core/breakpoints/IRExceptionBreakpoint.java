/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.debug.core.breakpoints;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.annotation.NonNull;


public interface IRExceptionBreakpoint extends IRBreakpoint {
	
	
	@NonNull String getExceptionId() throws DebugException;
	
}
