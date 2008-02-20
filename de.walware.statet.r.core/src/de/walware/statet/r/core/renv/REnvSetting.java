/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.renv;

import java.util.regex.Pattern;

import de.walware.eclipsecommons.AbstractSettingsModelObject;

import de.walware.statet.r.core.RCore;


/**
 * Provides settings to link to a REnv configuration.
 */
public class REnvSetting extends AbstractSettingsModelObject {
	
	
	public static enum SettingsType {
		WORKBENCH, SPECIFIC,
	}
	
	
	private static Pattern RENV_SETTINGSSPLIT_PATTERN = Pattern.compile("\\:"); //$NON-NLS-1$
	
	public static REnvConfiguration resolveREnv(final REnvSetting setting) {
		if (setting != null && setting.fType != null) {
			switch (setting.fType) {
			case WORKBENCH:
				return RCore.getREnvManager().getDefault();
			case SPECIFIC:
				return RCore.getREnvManager().get(setting.fDetails[0], setting.fDetails[1]);
			}
		}
		return null;
	}
	
	public static String encodeREnv(final SettingsType type, final REnvConfiguration specified, final boolean strict) {
		boolean valid;
		if (type != null) {
			switch (type) {
			case WORKBENCH:
				return SettingsType.WORKBENCH.name();
			case SPECIFIC:
				valid = (specified != null);
				if (valid || !strict) {
					final StringBuilder s = new StringBuilder(SettingsType.SPECIFIC.name());
					if (valid) {
						s.append(':');
						s.append(specified.getId());
						s.append(':');
						s.append(specified.getName());
					}
					return s.toString();
				}
				break;
			}
		}
		return null;
	}
	
	public static REnvSetting decodeType(final String encodedSetting, final boolean strict) {
		final REnvSetting setting = new REnvSetting();
		if (encodedSetting != null && encodedSetting.length() > 0) {
			final String[] parts = RENV_SETTINGSSPLIT_PATTERN.split(encodedSetting, 3);
			final SettingsType type = SettingsType.valueOf(parts[0]);
			boolean valid;
			switch (type) {
			case WORKBENCH:
				setting.fType = SettingsType.WORKBENCH;
				break;
			
			case SPECIFIC:
				valid = (parts.length == 3);
				if (valid || !strict) {
					setting.fType = SettingsType.SPECIFIC;
					setting.fDetails = (valid) ? new String[] { parts[1], parts[2] } : new String[] { null, null };
				}
				break;
			}
		}
		return setting;
	}
	
	
/*-- Object ------------------------------------------------------------------*/
	
	
	private SettingsType fType;
	private String[] fDetails;
	
	
	private REnvSetting() {
	}
	
	
	public SettingsType getType() {
		return fType;
	}
	
	public String[] getDetails() {
		return fDetails;
	}
	
}
