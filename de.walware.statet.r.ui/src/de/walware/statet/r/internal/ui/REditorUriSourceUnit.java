/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.SourceDocumentRunnable;
import de.walware.ecommons.ltk.WorkingContext;
import de.walware.ecommons.ltk.core.impl.GenericUriSourceUnit;
import de.walware.ecommons.ltk.core.impl.IWorkingBuffer;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.FileBufferWorkingBuffer;
import de.walware.ecommons.text.core.sections.DocContentSections;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RSuModelContainer;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.source.RDocumentContentInfo;


public class REditorUriSourceUnit extends GenericUriSourceUnit implements IRSourceUnit {
	
	
	private final RSuModelContainer fModel = new RUISuModelContainer(this);
	
	
	public REditorUriSourceUnit(final String id, final IFileStore store) {
		super(id, store);
	}
	
	
	@Override
	public WorkingContext getWorkingContext() {
		return LTK.EDITOR_CONTEXT;
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
	public DocContentSections getDocumentContentInfo() {
		return RDocumentContentInfo.INSTANCE;
	}
	
	@Override
	public int getElementType() {
		return R_OTHER_SU;
	}
	
	
	@Override
	protected IWorkingBuffer createWorkingBuffer(final SubMonitor progress) {
		return new FileBufferWorkingBuffer(this);
	}
	
	@Override
	protected void register() {
		super.register();
		RCore.getRModelManager().registerDependentUnit(this);
	}
	
	@Override
	protected void unregister() {
		super.unregister();
		RCore.getRModelManager().deregisterDependentUnit(this);
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
		FileBufferWorkingBuffer.syncExec(runnable);
	}
	
	@Override
	public IRCoreAccess getRCoreAccess() {
		return RCore.getWorkbenchAccess();
	}
	
	@Override
	public IREnv getREnv() {
		return RCore.getREnvManager().getDefault();
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (RSuModelContainer.class.equals(required)) {
			return fModel;
		}
		return super.getAdapter(required);
	}
	
}
