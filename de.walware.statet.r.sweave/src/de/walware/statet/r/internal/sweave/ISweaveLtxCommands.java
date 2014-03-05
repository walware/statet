/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave;

import de.walware.ecommons.collections.ConstArrayList;

import de.walware.docmlet.tex.core.commands.Argument;
import de.walware.docmlet.tex.core.commands.TexCommand;
import de.walware.docmlet.tex.core.commands.TexEmbedCommand;

import de.walware.statet.r.core.model.RModel;


public interface ISweaveLtxCommands {
	
	
	TexCommand SWEAVE_SweaveOpts_COMMANDS= new TexCommand(0,
			"SweaveOpts", false, new ConstArrayList<>( //$NON-NLS-1$
					new Argument("options", Argument.REQUIRED, Argument.NONE) //$NON-NLS-1$
			), Messages.LtxCommand_SweaveOpts_description);
	
	TexEmbedCommand SWEAVE_Sexpr_COMMANDS= new TexEmbedCommand(0, RModel.TYPE_ID,
			"Sexpr", false, new ConstArrayList<>( //$NON-NLS-1$
					new Argument("expression", Argument.REQUIRED, Argument.EMBEDDED) //$NON-NLS-1$
			), Messages.LtxCommand_Sexpr_description);
	
}
