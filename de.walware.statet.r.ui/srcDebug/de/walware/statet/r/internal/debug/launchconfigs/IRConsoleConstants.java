/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.launchconfigs;

import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;


public interface IRConsoleConstants {

	String ROOT = "de.walware.statet.r.rcmd.";
	
	String ID_RCMD_LAUNCHCONFIG = "de.walware.statet.r.launching.RCmdConfigurationType";

	String ATTR_R_LOCATION = IExternalToolConstants.ATTR_LOCATION; //ROOT + "location";
	String ATTR_R_CMD = ROOT + "cmd";
	String ATTR_WORKING_DIRECTORY = IExternalToolConstants.ATTR_WORKING_DIRECTORY; // ROOT + "workingdirectory";
	String ATTR_CMD_ARGUMENTS = IExternalToolConstants.ATTR_TOOL_ARGUMENTS; // "cmdarguments";

	
}
