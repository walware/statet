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

import org.junit.Test;

import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.RFactorDataImpl;


public class RValueValidator0AfactorTest extends AbstractRValueValidatorTest {
	
	
	public RValueValidator0AfactorTest() {
		super(new RFactorDataImpl(0, false, new String[] { "A", "B", null }));
	}
	
	
	@Override
	protected boolean isValidSource(final byte type) {
		return false;
	}
	
	@Override
	protected boolean isValidSourceNA(final byte type) {
		return (type == RStore.LOGICAL);
	}
	
	
	@Test
	public void parseLevel() {
		testValidate(true, "A");
		testValidate(true, "B");
		testValidate(false, "C");
		testValidate(true, "<NA>");
	}
	
	@Test
	public void parseLevelTrim() {
		testValidate(true, " A");
		testValidate(true, "B ");
		testValidate(false, "C ");
		testValidate(true, " <NA> ");
	}
	
}
