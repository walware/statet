/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core;

import de.walware.ecommons.preferences.PreferencesManageListener;
import de.walware.ecommons.preferences.core.IPreferenceAccess;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.renv.IREnv;


final class RCoreAccess implements IRCoreAccess {
	
	
	private boolean isDisposed;
	
	private final IPreferenceAccess prefs;
	
	private volatile RCodeStyleSettings codeStyle;
	private PreferencesManageListener codeStyleListener;
	
	private final IREnv rEnv;
	
	
	RCoreAccess(final IPreferenceAccess prefs, final IREnv rEnv) {
		this.prefs= prefs;
		this.rEnv= rEnv;
	}
	
	
	@Override
	public IPreferenceAccess getPrefs() {
		return this.prefs;
	}
	
	@Override
	public IREnv getREnv() {
		return this.rEnv;
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		RCodeStyleSettings codeStyle= this.codeStyle;
		if (codeStyle == null) {
			synchronized (this) {
				codeStyle= this.codeStyle;
				if (codeStyle == null) {
					codeStyle= new RCodeStyleSettings(1);
					if (!this.isDisposed) {
						this.codeStyleListener= new PreferencesManageListener(codeStyle,
								this.prefs, RCodeStyleSettings.ALL_GROUP_IDS );
					}
					codeStyle.load(this.prefs);
					codeStyle.resetDirty();
					this.codeStyle= codeStyle;
				}
			}
		}
		return codeStyle;
	};
	
	public synchronized void dispose() {
		this.isDisposed= true;
		
		if (this.codeStyleListener != null) {
			this.codeStyleListener.dispose();
			this.codeStyleListener= null;
		}
	}
	
}
