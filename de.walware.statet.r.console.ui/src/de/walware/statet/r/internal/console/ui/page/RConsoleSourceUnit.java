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

package de.walware.statet.r.internal.console.ui.page;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.SourceDocumentRunnable;
import de.walware.ecommons.ltk.core.SourceContent;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.text.core.sections.IDocContentSections;
import de.walware.ecommons.text.core.util.AbstractFragmentDocument;
import de.walware.ecommons.text.core.util.TextUtils;

import de.walware.statet.nico.ui.console.GenericConsoleSourceUnit;

import de.walware.statet.r.console.ui.RConsole;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RSuModelContainer;
import de.walware.statet.r.core.source.RDocumentContentInfo;


public class RConsoleSourceUnit extends GenericConsoleSourceUnit implements IRSourceUnit {
	
	
	private final RConsole rConsole;
	
	private final RSuModelContainer model = new RSuModelContainer(this) {
		
		@Override
		public SourceContent getParseContent(final IProgressMonitor monitor) {
			final AbstractFragmentDocument doc= getDocument();
			synchronized (TextUtils.getLockObject(doc)) {
				return new SourceContent(doc.getModificationStamp(),
						doc.getMasterDocument().get(), 
						-doc.getOffsetInMasterDocument() );
			}
		}
		
	};
	
	
	public RConsoleSourceUnit(final RConsolePage page, final AbstractFragmentDocument document) {
		super(page.toString(), document);
		this.rConsole = page.getConsole();
	}
	
	
	@Override
	public String getModelTypeId() {
		return RModel.R_TYPE_ID;
	}
	
	@Override
	public IDocContentSections getDocumentContentInfo() {
		return RDocumentContentInfo.INSTANCE;
	}
	
	
	@Override
	public AstInfo getAstInfo(final String type, final boolean ensureSync,
			final IProgressMonitor monitor) {
		if (type == null || this.model.isContainerFor(type)) {
			return this.model.getAstInfo(ensureSync, monitor);
		}
		return null;
	}
	
	@Override
	public ISourceUnitModelInfo getModelInfo(final String type, final int flags,
			final IProgressMonitor monitor) {
		if (type == null || this.model.isContainerFor(type)) {
			return this.model.getModelInfo(flags, monitor);
		}
		return null;
	}
	
	@Override
	public void syncExec(final SourceDocumentRunnable runnable) throws InvocationTargetException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IRCoreAccess getRCoreAccess() {
		return this.rConsole;
	}
	
	@Override
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	@Override
	public List<? extends IModelElement> getModelChildren(final Filter filter) {
		return null;
	}
	
}
