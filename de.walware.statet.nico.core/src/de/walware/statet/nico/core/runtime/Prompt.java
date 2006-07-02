/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;


/**
 * 
 */
public class Prompt {
	
	public static final Prompt NONE = new Prompt("", 0);
	public static final Prompt DEFAULT = new Prompt("> ", IToolRunnableControllerAdapter.META_PROMPT_DEFAULT);
	
	
	public final String text;
	public final int meta;
	
	public Prompt(String text) {
		
		this(text, 0);
	}
	
	public Prompt(String text, int meta) {
		
		this.text = text;
		this.meta = meta;
	}
}
