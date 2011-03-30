/*******************************************************************************
 * Copyright (c) 2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.renv;


public class REnvUtil {
	
	
	public static String encode(final IREnv rEnv) {
		if (rEnv != null) {
			final String name = rEnv.getName();
			if (name != null) {
				return rEnv.getId() + ';' + name;
			}
			else {
				return rEnv.getId() + ';';
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	public static IREnv decode(final String encodedSetting, final IREnvManager manager) {
		if (encodedSetting != null) {
			final int idx = encodedSetting.indexOf(';');
			final IREnv rEnv;
			if (idx >= 0) {
				rEnv = manager.get(encodedSetting.substring(0, idx), encodedSetting.substring(idx+1));
			}
			else {
				rEnv = manager.get(encodedSetting, null);
			}
			if (rEnv != null) {
				return rEnv;
			}
		}
		return null;
	}
	
	
	private REnvUtil() {
	}
	
}
