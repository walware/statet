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

package de.walware.statet.r.sweave;

import de.walware.ecommons.preferences.IPreferenceAccess;

import de.walware.docmlet.tex.core.ITexCoreAccess;
import de.walware.docmlet.tex.core.TexCodeStyleSettings;
import de.walware.docmlet.tex.core.commands.TexCommandSet;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;


public class TexRweaveCoreAccess implements ITexRweaveCoreAccess {
	
	
	private final ITexCoreAccess fTexAccess;
	private final IRCoreAccess fRAccess;
	
	
	public TexRweaveCoreAccess(final ITexCoreAccess tex, final IRCoreAccess r) {
		if (tex == null) {
			throw new NullPointerException("tex");
		}
		if (r == null) {
			throw new NullPointerException("r");
		}
		fTexAccess = tex;
		fRAccess = r;
	}
	
	
	@Override
	public IPreferenceAccess getPrefs() {
		return fRAccess.getPrefs(); // TODO
	}
	
	@Override
	public TexCommandSet getTexCommandSet() {
		return fTexAccess.getTexCommandSet();
	}
	
	@Override
	public TexCodeStyleSettings getTexCodeStyle() {
		return fTexAccess.getTexCodeStyle();
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return fRAccess.getRCodeStyle();
	}
	
}
