/*=============================================================================#
 # Copyright (c) 2006-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core.runtime;


/**
 * 
 */
public class Prompt {
	
	public static final Prompt NONE = new Prompt("", 0); //$NON-NLS-1$
	public static final Prompt DEFAULT = new Prompt("> ", IConsoleService.META_PROMPT_DEFAULT); //$NON-NLS-1$
	
	
	public final String text;
	public final int meta;
	
	
	public Prompt(final String text, final int meta) {
		assert (text != null);
		this.text = text;
		this.meta = meta;
	}
	
}
