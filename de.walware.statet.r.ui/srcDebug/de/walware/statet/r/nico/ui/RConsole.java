/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.nico.core.ConsoleInstanceScope;
import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsoleColorAdapter;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.debug.ui.launchconfigs.RErrorLineTracker;


public class RConsole extends NIConsole implements IRCoreAccess {

	
	private IPreferenceAccess fPrefs;
	
	
	public RConsole(ToolProcess process, NIConsoleColorAdapter adapter) {
		super(process, adapter);
		
		RErrorLineTracker lineMatcher = new RErrorLineTracker(process);
		addPatternMatchListener(lineMatcher);
		fPrefs = NicoCore.getDefaultConsolePreferences();
	}

	@Override
	public IPageBookViewPage createPage(IConsoleView view) {
		return new RConsolePage(this, view);
	}

	public IPreferenceAccess getPrefs() {
		return fPrefs;
	}
	
	public RCodeStyleSettings getRCodeStyle() {
		return RCore.getWorkbenchAccess().getRCodeStyle();
	}
	
}
