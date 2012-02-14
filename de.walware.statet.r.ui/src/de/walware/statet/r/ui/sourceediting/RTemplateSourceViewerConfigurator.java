/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.sourceediting;

import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.preferences.RTemplateSourceViewerConfiguration;


public class RTemplateSourceViewerConfigurator extends RSourceViewerConfigurator {
	
	public RTemplateSourceViewerConfigurator(
			final IRCoreAccess rCoreAccess,
			final TemplateVariableProcessor processor) {
		super(rCoreAccess);
		setConfiguration(new RTemplateSourceViewerConfiguration(
				processor,
				this,
				RUIPlugin.getDefault().getEditorPreferenceStore(),
				SharedUIResources.getColors()));
	}
	
}
