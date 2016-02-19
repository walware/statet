/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.rdata;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.text.core.input.StringParserInput;

import de.walware.rj.data.RFunction;
import de.walware.rj.data.RJIO;
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
	
	
	public RFunction2(final ArgsDefinition args,
			final CombinedElement parent, final RElementName name) {
		super(parent, name);
		
		fArgs = args;
	}
	
	
	public RFunction2(final RJIO io, final RObjectFactory factory,
			final CombinedElement parent, final RElementName name) throws IOException {
		super(parent, name);
		
		/*final int options =*/ io.readInt();
		final String headerSource = io.readString();
		if (headerSource != null && headerSource.length() > 0) {
			final RScanner scanner= new RScanner(AstInfo.LEVEL_MODEL_DEFAULT);
			final FDef fDef= scanner.scanFDef(new StringParserInput(headerSource).init());
			if (fDef != null) {
				fArgs = SourceAnalyzer.createMethodArgDef(fDef, null);
			}
		}
	}
	
	@Override
	public byte getRObjectType() {
		return TYPE_FUNCTION;
	}
	
	@Override
	public String getRClassName() {
		return "function";
	}
	
	
	@Override
	public long getLength() {
		return 0;
	}
	
	@Override
	public String getHeaderSource() {
		return null;
	}
	
	@Override
	public String getBodySource() {
		return null;
	}
	
	@Override
	public RStore getData() {
		return null;
	}
	
	
	@Override
	public int getElementType() {
		return IRElement.R_COMMON_FUNCTION;
	}
	
	@Override
	public ArgsDefinition getArgsDefinition() {
		return fArgs;
	}
	
	
	@Override
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	@Override
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		return Collections.emptyList();
	}
	
}
