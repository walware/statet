/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.walware.ecommons.ltk.AbstractSourceModelInfo;
import de.walware.ecommons.ltk.AstInfo;

import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRModelInfo;


public class RSourceModelInfo extends AbstractSourceModelInfo implements IRModelInfo {
	
	
	final BuildSourceFrame fTopFrame;
	final LinkedHashMap<String, BuildSourceFrame> fLocalFrames;
	private final Map<String, BuildSourceFrame> fProtectedLocalFrames;
	
	final PackageReferences fPackageRefs;
	final Map<String, BuildSourceFrame> fNamespaceFrames;
	private final Map<String, BuildSourceFrame> fProtectedNamespaceFrames;
	
	private final IRLangSourceElement fSourceElement;
	
	
	RSourceModelInfo(final AstInfo ast,
			final LinkedHashMap<String, BuildSourceFrame> localFrames,
			final BuildSourceFrame topFrame,
			final PackageReferences packageRefs,
			final Map<String, BuildSourceFrame> namespaceFrames,
			final IRLangSourceElement unitElement) {
		super(ast);
		fTopFrame = topFrame;
		fLocalFrames = localFrames;
		fProtectedLocalFrames = Collections.unmodifiableMap(localFrames);
		fPackageRefs = packageRefs;
		fNamespaceFrames = namespaceFrames;
		fProtectedNamespaceFrames = Collections.unmodifiableMap(namespaceFrames);
		fSourceElement = unitElement;
	}
	
	
	@Override
	public IRLangSourceElement getSourceElement() {
		return fSourceElement;
	}
	
	@Override
	public BuildSourceFrame getTopFrame() {
		return fTopFrame;
	}
	
	@Override
	public final Map<String, ? extends IRFrameInSource> getSourceFrames() {
		return fProtectedLocalFrames;
	}
	
	@Override
	public PackageReferences getReferencedPackages() {
		return fPackageRefs;
	}
	
	@Override
	public final Map<String, ? extends IRFrame> getReferencedFrames() {
		return fProtectedNamespaceFrames;
	}
	
}
