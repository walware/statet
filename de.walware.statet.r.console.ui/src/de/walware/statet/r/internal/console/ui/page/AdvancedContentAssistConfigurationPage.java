/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.console.ui.page;

import org.eclipse.core.runtime.CoreException;

import de.walware.ecommons.ltk.ui.sourceediting.assist.AdvancedContentAssistConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;

import de.walware.statet.r.internal.ui.RUIPlugin;


public class AdvancedContentAssistConfigurationPage extends ConfigurationBlockPreferencePage {
	
	
	/**
	 * Created by extension point
	 */
	public AdvancedContentAssistConfigurationPage() {
	}
	
	
	@Override
	protected ConfigurationBlock createConfigurationBlock() throws CoreException {
		return new AdvancedContentAssistConfigurationBlock(
				RUIPlugin.getDefault().getRConsoleContentAssistRegistry(),
				createStatusChangedListener());
	}
	
}
