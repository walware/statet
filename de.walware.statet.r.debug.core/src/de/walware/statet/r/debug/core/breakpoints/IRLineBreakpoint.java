/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
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
import org.eclipse.debug.core.model.ILineBreakpoint;


public interface IRLineBreakpoint extends IRBreakpoint, ILineBreakpoint {
	
	int R_COMMON_FUNCTION_ELEMENT_TYPE = 1;
	int R_S4_METHOD_ELEMENT_TYPE = 2;
	
	int R_TOPLEVEL_COMMAND_ELEMENT_TYPE = 9;
	
	
	void setConditionEnabled(boolean enabled) throws CoreException;
	boolean isConditionEnabled() throws CoreException;
	void setConditionExpr(String code) throws CoreException;
	String getConditionExpr() throws CoreException;
	
	int getElementType() throws CoreException;
//	String getElementId() throws CoreException;
	String getElementLabel() throws CoreException;
	String getSubLabel() throws CoreException;
	
}
