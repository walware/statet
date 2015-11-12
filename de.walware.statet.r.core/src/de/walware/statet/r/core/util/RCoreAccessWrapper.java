/*=============================================================================#
 # Copyright (c) 2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.util;

import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.preferences.core.IPreferenceAccess;
import de.walware.ecommons.preferences.core.util.PreferenceAccessWrapper;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.renv.IREnv;


public class RCoreAccessWrapper extends PreferenceAccessWrapper
		implements IRCoreAccess {
	
	
	private IRCoreAccess parent;
	
	
	public RCoreAccessWrapper(final IRCoreAccess rCoreAccess) {
		if (rCoreAccess == null) {
			throw new NullPointerException("rCoreAccess"); //$NON-NLS-1$
		}
		
		updateParent(null, rCoreAccess);
	}
	
	
	public synchronized IRCoreAccess getParent() {
		return this.parent;
	}
	
	public synchronized boolean setParent(final IRCoreAccess rCoreAccess) {
		if (rCoreAccess == null) {
			throw new NullPointerException("rCoreAccess"); //$NON-NLS-1$
		}
		if (rCoreAccess != this.parent) {
			updateParent(this.parent, rCoreAccess);
			return true;
		}
		return false;
	}
	
	protected void updateParent(final IRCoreAccess oldParent, final IRCoreAccess newParent) {
		this.parent= newParent;
		
		super.setPreferenceContexts(newParent.getPrefs().getPreferenceContexts());
	}
	
	@Override
	public void setPreferenceContexts(final ImList<IScopeContext> contexts) {
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public IPreferenceAccess getPrefs() {
		return this;
	}
	
	@Override
	public IREnv getREnv() {
		return this.parent.getREnv();
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return this.parent.getRCodeStyle();
	}
	
}
