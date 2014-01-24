/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
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
import org.eclipse.jface.text.ISynchronizable;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.SourceDocumentRunnable;

import de.walware.statet.nico.ui.console.GenericConsoleSourceUnit;
import de.walware.statet.nico.ui.console.InputDocument;

import de.walware.statet.r.console.ui.RConsole;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RSuModelContainer;
import de.walware.statet.r.core.model.SpecialParseContent;
import de.walware.statet.r.core.renv.IREnv;


public class RConsoleSourceUnit extends GenericConsoleSourceUnit implements IRSourceUnit {
	
	
	private final RConsole fRConsole;
	
	private final RSuModelContainer fModel = new RSuModelContainer(this) {
		
		@Override
		public SourceContent getParseContent(final IProgressMonitor monitor) {
			Object lock = null;
			if (fDocument instanceof ISynchronizable) {
				lock = ((ISynchronizable) fDocument).getLockObject();
			}
			if (lock == null) {
				lock = new Object();
			}
			synchronized (lock) {
				return new SpecialParseContent(
						fDocument.getModificationStamp(),
						fDocument.getMasterDocument().get(),
						-fDocument.getOffsetInMasterDocument() );
			}
		}
	};
	
	
	public RConsoleSourceUnit(final RConsolePage page, final InputDocument document) {
		super(page.toString(), document);
		fRConsole = page.getConsole();
	}
	
	
	@Override
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	@Override
	public String getContentTypeId() {
		return R_CONTENT;
	}
	
	
	@Override
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
		RCore.getRModelManager().reconcile(fModel, (reconcileLevel | IModelManager.RECONCILER),
				monitor );
	}
	
	@Override
	public AstInfo getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			return fModel.getAstInfo(ensureSync, monitor);
		}
		return null;
	}
	
	@Override
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			return fModel.getModelInfo(syncLevel, monitor);
		}
		return null;
	}
	
	@Override
	public void syncExec(final SourceDocumentRunnable runnable) throws InvocationTargetException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IRCoreAccess getRCoreAccess() {
		return fRConsole;
	}
	
	@Override
	public IREnv getREnv() {
		final IREnv rEnv = (IREnv) fRConsole.getProcess().getAdapter(IREnv.class);
		return (rEnv != null) ? rEnv : RCore.getREnvManager().getDefault();
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
