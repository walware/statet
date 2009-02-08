/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import de.walware.ecommons.ltk.IModelElement;


public interface IRLangElement extends IModelElement {
	
	
	public static final int R_S4CLASS =                C1_CLASS    | 0x80;
	public static final int R_S4CLASS_EXTENSION =      C1_CLASS    | 0x90;
	
	public static final int R_PACKAGE_LOAD =           C1_IMPORT   | 0x10;
	
	public static final int R_COMMON_FUNCTION =        C1_METHOD   | 0x10;
	public static final int R_COMMON_LOCAL_FUNCTION =  C1_METHOD   | 0x11;
	public static final int R_GENERIC_FUNCTION =       C1_METHOD   | 0x20;
	public static final int R_S4METHOD =               C1_METHOD   | 0x80;
	
	public static final int R_GENERAL_VARIABLE =       C1_VARIABLE | 0x10;
	public static final int R_GENERAL_LOCAL_VARIABLE = C1_VARIABLE | 0x11;
	public static final int R_ARGUMENT =               C1_VARIABLE | 0x31;
	public static final int R_S4SLOT =                 C1_VARIABLE | 0x80;
	
	
	public static final Filter R_S4SLOT_FILTER = new Filter() {
		public boolean include(final IModelElement element) {
			return (element.getElementType() == R_S4SLOT);
		}
	};
	
}
