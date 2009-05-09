/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.core.runtime.CoreException;

import de.walware.ecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.ecommons.ui.text.sourceediting.AdvancedContentAssistConfigurationBlock;

import de.walware.statet.r.internal.ui.RUIPlugin;


public class AdvancedContentAssistConfigurationPage extends ConfigurationBlockPreferencePage<AdvancedContentAssistConfigurationBlock> {
	
	
	@Override
	protected AdvancedContentAssistConfigurationBlock createConfigurationBlock() throws CoreException {
		return new AdvancedContentAssistConfigurationBlock(
				RUIPlugin.getDefault().getREditorContentAssistRegistry(),
				"de.walware.statet.r.commands.SpecificContentAssist", //$NON-NLS-1$
				createStatusChangedListener());
	}
	
}
