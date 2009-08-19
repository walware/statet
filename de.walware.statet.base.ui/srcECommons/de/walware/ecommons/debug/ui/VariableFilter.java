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

package de.walware.ecommons.debug.ui;

import org.eclipse.core.variables.IStringVariable;


public interface VariableFilter {
	
	
	public static VariableFilter EXCLUDE_INTERACTIVE_FILTER = new VariableFilter() {
		public boolean exclude(final IStringVariable variable) {
			final String variableName = variable.getName();
			return (variableName.startsWith("selected_") //$NON-NLS-1$
					|| variableName.endsWith("_prompt") ); //$NON-NLS-1$
		}
	};
	
	public static VariableFilter EXCLUDE_JAVA_FILTER = new VariableFilter() {
		public boolean exclude(final IStringVariable variable) {
			final String variableName = variable.getName();
			return (variableName.startsWith("java_") //$NON-NLS-1$
					|| variableName.startsWith("target_home") //$NON-NLS-1$
					|| variableName.startsWith("tptp_junit") ); //$NON-NLS-1$
		}
	};
	
	public static VariableFilter EXCLUDE_BUILD_FILTER = new VariableFilter() {
		public boolean exclude(final IStringVariable variable) {
			final String variableName = variable.getName();
			return (variableName.startsWith("build_")); //$NON-NLS-1$
		}
	};
	
	
	boolean exclude(IStringVariable variable);
	
}
