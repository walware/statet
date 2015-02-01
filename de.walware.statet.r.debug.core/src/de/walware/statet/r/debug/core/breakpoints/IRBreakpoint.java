/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.debug.core.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;

import de.walware.statet.r.debug.core.IRDebugTarget;


public interface IRBreakpoint extends IBreakpoint {
	
	
	interface ITargetData extends IAdaptable {
		
		boolean isInstalled();
		
	}
	
	
	
	
	/**
	 * Returns the type of this breakpoint
	 * 
	 * @return the type id
	 */
	String getBreakpointType();
	
//	/**
//	 * Returns the hit count of this breakpoint
//	 * 
//	 * @return the hit count, or -1 if the breakpoint does not have a hit count.
//	 * @exception CoreException if unable to access the property from this breakpoint's underlying marker
//	 */
//	int getHitCount() throws CoreException;
//	
//	/**
//	 * Sets the hit count of this breakpoint.
//	 * If this breakpoint is currently disabled and the hit count
//	 * is set greater than -1, this breakpoint is automatically enabled.
//	 * 
//	 * @param count the new hit count
//	 * @exception CoreException if unable to set the property on this breakpoint's underlying marker
//	 */
//	void setHitCount(int count) throws CoreException;
	
	ITargetData registerTarget(IRDebugTarget target, ITargetData data);
	ITargetData unregisterTarget(IRDebugTarget target);
	ITargetData getTargetData(IRDebugTarget target);
	
	
	/**
	 * Returns if the breakpoint is installed in any R engine.
	 * 
	 * @return if the breakpoint is installed
	 * @throws CoreException 
	 */
	boolean isInstalled() throws CoreException;
	
}
