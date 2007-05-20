/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RSourceViewerConfiguration;
import de.walware.statet.r.ui.editors.RSourceViewerConfigurator;


public class RInputConfiguration extends RSourceViewerConfigurator {

	
	RInputConfiguration(IRCoreAccess rCoreAccess) {
		super(rCoreAccess, RUIPlugin.getDefault().getEditorPreferenceStore());
		setConfiguration(new RSourceViewerConfiguration(this, getPreferenceStore(), StatetUIServices.getSharedColorManager()));
	}
	
}
