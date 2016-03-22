/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.data;

import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;


public class RValueValidator01logiTest extends AbstractRValueValidatorTest {
	
	
	public RValueValidator01logiTest() {
		super(RObjectFactoryImpl.LOGI_STRUCT_DUMMY);
	}
	
	
	@Override
	public void parseNum() {
		testValidate(true, "0");
		testValidate(true, "1");
		testValidate(false, "0x7FFFFFF");
		testValidate(false, "0x7FFFFFFFFL");
		testValidate(false, "-1.55");
		testValidate(false, "-1.0e-5");
		testValidate(false, "NA_real_");
	}
	
	
}
