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

import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;


public class RValueValidator06rawTest extends AbstractRValueValidatorTest {
	
	
	public RValueValidator06rawTest() {
		super(RObjectFactoryImpl.RAW_STRUCT_DUMMY);
	}
	
	
	@Override
	protected boolean isValidSource(final byte type) {
		return (type == RStore.LOGICAL);
	}
	
	@Override
	protected boolean isValidSourceNA(final byte type) {
		return false;
	}
	
	
	@Override
	public void parseInt() {
		testValidate(true, "0L");
		testValidate(true, "255L");
		testValidate(false, "-1L");
		testValidate(false, "256L");
		testValidate(false, "NA_integer_");
	}
	
	
	@Override
	public void parseNum() {
		testValidate(true, "0");
		testValidate(true, "a0");
		testValidate(true, "FF");
		testValidate(false, "100");
		testValidate(false, "255");
		testValidate(false, "-1");
		testValidate(true, "0x00");
		testValidate(true, "0x7F");
		testValidate(false, "0x100");
		testValidate(false, "0x7FFFFFF");
		testValidate(false, "0x7FFFFFFFFL");
		testValidate(false, "-1.55");
		testValidate(false, "-1.0e-5");
		testValidate(false, "NA_real_");
	}
	
	
}
