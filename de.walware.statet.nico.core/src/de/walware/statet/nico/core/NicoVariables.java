/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core;

import org.eclipse.core.variables.IDynamicVariable;

import de.walware.ecommons.variables.core.DateVariable;
import de.walware.ecommons.variables.core.DynamicVariable;
import de.walware.ecommons.variables.core.TimeVariable;

import de.walware.statet.nico.internal.core.Messages;


public class NicoVariables {
	
	public static final String SESSION_STARTUP_DATE_VARNAME= "session_startup_date"; //$NON-NLS-1$
	public static final IDynamicVariable SESSION_STARTUP_DATE_VARIABLE= new DateVariable(
			SESSION_STARTUP_DATE_VARNAME, Messages.SessionVariables_StartupDate_description );
	
	public static final String SESSION_STARTUP_TIME_VARNAME= "session_startup_time"; //$NON-NLS-1$
	public static final IDynamicVariable SESSION_STARTUP_TIME_VARIABLE= new TimeVariable(
			SESSION_STARTUP_TIME_VARNAME, Messages.SessionVariables_StartupTime_description );
	
	public static final String SESSION_CONNECTION_DATE_VARNAME= "session_connection_date"; //$NON-NLS-1$
	public static final IDynamicVariable SESSION_CONNECTION_DATE_VARIABLE= new DateVariable(
			SESSION_CONNECTION_DATE_VARNAME, Messages.SessionVariables_ConnectionDate_description );
	
	public static final String SESSION_CONNECTION_TIME_VARNAME= "session_connection_time"; //$NON-NLS-1$
	public static final IDynamicVariable SESSION_CONNECTION_TIME_VARIABLE= new TimeVariable(
			SESSION_CONNECTION_TIME_VARNAME, Messages.SessionVariables_ConnectionTime_description );
	
	public static final String SESSION_STARTUP_WD_VARNAME= "session_startup_wd"; //$NON-NLS-1$
	public static final IDynamicVariable SESSION_STARTUP_WD_VARIABLE= new DynamicVariable(
			SESSION_STARTUP_WD_VARNAME, Messages.SessionVariables_StartupWD_description, false );
	
}
