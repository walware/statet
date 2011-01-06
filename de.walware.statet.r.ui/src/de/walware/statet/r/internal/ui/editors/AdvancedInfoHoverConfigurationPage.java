/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.ui.sourceediting.AdvancedInfoHoverConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;

import de.walware.statet.r.internal.ui.RUIPlugin;


public class AdvancedInfoHoverConfigurationPage {
	
	
	public static class ForREditor extends ConfigurationBlockPreferencePage<AdvancedInfoHoverConfigurationBlock> {
		
		public ForREditor() {
		}
		
		@Override
		protected AdvancedInfoHoverConfigurationBlock createConfigurationBlock() throws CoreException {
			return new AdvancedInfoHoverConfigurationBlock(
					RUIPlugin.getDefault().getREditorInfoHoverRegistry(),
					createStatusChangedListener());
		}
		
	}
	
	
}
