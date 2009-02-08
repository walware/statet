/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.SubMonitor;

import de.walware.ecommons.ltk.ECommonsLTK;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.IWorkingBuffer;
import de.walware.ecommons.ltk.SourceDocumentRunnable;
import de.walware.ecommons.ltk.WorkingContext;
import de.walware.ecommons.ltk.ui.FileBufferWorkingBuffer;

import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RManagedWorkingCopy;


public class REditorWorkingCopy extends RManagedWorkingCopy {
	
	
	public REditorWorkingCopy(final IRSourceUnit from) {
		super(from);
	}
	
	
	public WorkingContext getWorkingContext() {
		return ECommonsLTK.EDITOR_CONTEXT;
	}
	
	@Override
	protected IWorkingBuffer createWorkingBuffer(final SubMonitor progress) {
		return new FileBufferWorkingBuffer(this);
	}
	
	public void syncExec(final SourceDocumentRunnable runnable) throws InvocationTargetException {
		FileBufferWorkingBuffer.syncExec(runnable);
	}
	
	public IProblemRequestor getProblemRequestor() {
		return (IProblemRequestor) RUIPlugin.getDefault().getRDocumentProvider().getAnnotationModel(this);
	}
	
}
