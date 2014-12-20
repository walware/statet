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

import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.SourceDocumentRunnable;
import de.walware.ecommons.ltk.WorkingContext;
import de.walware.ecommons.ltk.core.impl.GenericFragmentSourceUnit2;
import de.walware.ecommons.text.ISourceFragment;
import de.walware.ecommons.text.core.sections.DocContentSections;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RSuModelContainer;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.source.RDocumentContentInfo;


public class RFragmentSourceUnit extends GenericFragmentSourceUnit2<RSuModelContainer> implements IRSourceUnit {
	
	
	public RFragmentSourceUnit(final String id, final ISourceFragment fragment) {
		super(id, fragment);
	}
	
	@Override
	protected RSuModelContainer createModelContainer() {
		return new RSuModelContainer(this);
	}
	
	
	@Override
	public WorkingContext getWorkingContext() {
		return LTK.EDITOR_CONTEXT;
	}
	
	@Override
	public String getModelTypeId() {
		return RModel.R_TYPE_ID;
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
	public void syncExec(final SourceDocumentRunnable runnable) throws InvocationTargetException {
		runnable.run();
	}
	
	@Override
	public IRCoreAccess getRCoreAccess() {
		return RCore.getWorkbenchAccess();
	}
	
	@Override
	public IREnv getREnv() {
		final IREnv rEnv = (IREnv) getFragment().getAdapter(IREnv.class);
		if (rEnv != null) {
			return rEnv;
		}
		return RCore.getREnvManager().getDefault();
	}
	
}
