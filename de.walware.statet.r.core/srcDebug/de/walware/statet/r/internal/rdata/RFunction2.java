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

package de.walware.statet.r.internal.rdata;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.Collections;
import java.util.List;

import de.walware.ecommons.text.StringParseInput;

import de.walware.rj.data.RFunction;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.internal.core.sourcemodel.SourceAnalyzer;


public final class RFunction2 extends CombinedElement
		implements IRMethod, RFunction {
	
	
	private ArgsDefinition fArgs;
	
	
	public RFunction2(final RElementName name, final ArgsDefinition args) {
		fElementName = name;
		fArgs = args;
	}
	
	
	public RFunction2(final ObjectInput in, final int flags, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException, ClassNotFoundException {
		fParent = parent;
		fElementName = name;
		readExternal(in, flags, factory);
	}
	
	public void readExternal(final ObjectInput in, final int flags, final RObjectFactory factory) throws IOException, ClassNotFoundException {
		final int options = in.readInt();
		final String headerSource = in.readUTF();
		final RScanner scanner = new RScanner(new StringParseInput(headerSource), null);
		final FDef fDef = scanner.scanFDef();
		if (fDef != null) {
			fArgs = SourceAnalyzer.createMethodArgDef(fDef, null);
		}
	}
	
	public byte getRObjectType() {
		return RObject.TYPE_FUNCTION;
	}
	
	public String getRClassName() {
		return "function";
	}
	
	public int getLength() {
		return 0;
	}
	public String getHeaderSource() {
		return null;
	}
	
	public String getBodySource() {
		return null;
	}
	
	public RStore getData() {
		return null;
	}
	
	
	public int getElementType() {
		return IRElement.R_COMMON_FUNCTION;
	}
	
	public ArgsDefinition getArgsDefinition() {
		return fArgs;
	}
	
	
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		return Collections.EMPTY_LIST;
	}
	
}
