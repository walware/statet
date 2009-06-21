/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ui.refactoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public class RefactoringBasedStatus implements IStatus {
	
	
	private final RefactoringStatus fStatus;
	
	
	public RefactoringBasedStatus(final RefactoringStatus status) {
		fStatus = status;
	}
	
	
	public String getPlugin() {
		return StatetUIPlugin.PLUGIN_ID;
	}
	
	public int getSeverity() {
		return convertSeverity(fStatus.getSeverity());
	}
	
	public boolean isOK() {
		return (fStatus.getSeverity() == RefactoringStatus.OK);
	}
	
	public String getMessage() {
		return fStatus.getMessageMatchingSeverity(fStatus.getSeverity());
	}
	
	public int getCode() {
		return 0;
	}
	
	public Throwable getException() {
		return null;
	}
	
	public boolean isMultiStatus() {
		return false;
	}
	
	public IStatus[] getChildren() {
		return null;
	}
	
	public boolean matches(final int severityMask) {
		return (getSeverity() & severityMask) != 0;
	}
	
	
	public static int convertSeverity(final int severity) {
		switch (severity) {
		case RefactoringStatus.FATAL:
			return IStatus.ERROR;
		case RefactoringStatus.ERROR:
		case RefactoringStatus.WARNING:
			return IStatus.WARNING;
		case RefactoringStatus.INFO:
			return IStatus.INFO;
		default:
			return IStatus.OK;
		}
	}
	
}
