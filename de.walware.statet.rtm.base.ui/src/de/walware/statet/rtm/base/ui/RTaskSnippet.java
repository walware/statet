/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.rtm.base.ui;

import java.util.List;


public class RTaskSnippet {
	
	
	private final IRtDescriptor fDescriptor;
	
	private final String fLabel;
	
	private final List<String> fRequiredPkgs;
	
	private final String fRCode;
	
	
	public RTaskSnippet(final IRtDescriptor descriptor, final String label,
			final List<String> requiredPkgs, final String code) {
		fDescriptor = descriptor;
		fLabel = label;
		fRequiredPkgs = requiredPkgs;
		fRCode = code;
	}
	
	
	public String getLabel() {
		return fLabel;
	}
	
	public IRtDescriptor getDescriptor() {
		return fDescriptor;
	}
	
	public List<String> getRequiredPkgs() {
		return fRequiredPkgs;
	}
	
	public String getRCode() {
		return fRCode;
	}
	
}
