/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import de.walware.ecommons.collections.ConstList;

import de.walware.docmlet.tex.core.commands.Argument;
import de.walware.docmlet.tex.core.commands.TexCommand;


public interface ISweaveLtxCommands {
	
	
	public static final TexCommand SWEAVE_SweaveOpts_COMMANDS = new TexCommand(0,
			"SweaveOpts", false, new ConstList<Argument>( //$NON-NLS-1$
					new Argument("options", Argument.REQUIRED, Argument.NONE)
			), "Modifies the Sweave options for the rest of the document");
	
	public static final TexCommand SWEAVE_Sexpr_COMMANDS = new TexCommand(0,
			"Sexpr", false, new ConstList<Argument>( //$NON-NLS-1$
					new Argument("expression", Argument.REQUIRED, Argument.NONE)
			), "Prints the value of a scalar in R");
	
}
