/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


public class StatusInfo extends Status {

	public StatusInfo() {
		this(IStatus.OK, ""); //$NON-NLS-1$
	}
	
	public StatusInfo(int severity, String message) {
		super(severity, "no supported", IStatus.OK, message, null); //$NON-NLS-1$
	}

	public void setOK() {
		
		setSeverity(IStatus.OK);
		setMessage(""); //$NON-NLS-1$
	}
	
	public void setWarning(String message) {
		
		setSeverity(IStatus.WARNING);
		setMessage(message);
	}

	public void setError(String message) {
		
		setSeverity(IStatus.ERROR);
		setMessage(message);
	}

}
