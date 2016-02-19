/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.console.ui.launching;


public final class RConsoleType {
	
	
	private final String name;
	private final String id;
	private final boolean requireJRE;
	private final boolean isDebugSupported;
	private final boolean isJDebugSupported;
	
	
	public RConsoleType(final String name, final String id,
			final boolean requireJRE,
			final boolean isDebugSupported, final boolean isJDebugSupported) {
		this.name= name;
		this.id= id;
		this.requireJRE= requireJRE;
		this.isDebugSupported= isDebugSupported;
		this.isJDebugSupported= isJDebugSupported;
	}
	
	
	public String getName() {
		return this.name;
	}
	
	public String getId() {
		return this.id;
	}
	
	public boolean requireJRE() {
		return this.requireJRE;
	}
	
	public boolean isDebugSupported() {
		return this.isDebugSupported;
	}
	
	public boolean isJDebugSupported() {
		return this.isJDebugSupported;
	}
	
}
