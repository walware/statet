/*=============================================================================#
 # Copyright (c) 2011-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.sweave.text.LtxRweaveSwitch;


public class ForwardHandler extends AbstractHandler {
	
	private static final IHandler2 NO_HANDLER= new AbstractHandler() {
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			return null;
		}
		@Override
		public void dispose() {
		}
	};
	
	
	private final IHandler2[] handlers;
	
	private ISourceEditor setEditor;
	
	
	public ForwardHandler(final ISourceEditor editor,
			final IHandler2 docHandler, final IHandler2 rHandler) {
		this.handlers= new IHandler2[3];
		
		this.setEditor= editor;
		
		this.handlers[LtxRweaveSwitch.LTX.ordinal()]= docHandler;
		this.handlers[LtxRweaveSwitch.CHUNK_CONTROL.ordinal()]= NO_HANDLER;
		this.handlers[LtxRweaveSwitch.R.ordinal()]= rHandler;
	}
	
	public ForwardHandler() {
		this.handlers= new IHandler2[3];
	}
	
	
	@Override
	public void dispose() {
		super.dispose();
		
		for (int i= 0; i < this.handlers.length; i++) {
			if (this.handlers[i] != null) {
				this.handlers[i].dispose();
			}
		}
	}
	
	
	protected ISourceEditor getEditor(final Object applicationContext) {
		if (this.setEditor != null) {
			return this.setEditor;
		}
		final IWorkbenchPart activePart= WorkbenchUIUtil.getActivePart(applicationContext);
		if (activePart instanceof ISourceEditor) {
			return (ISourceEditor) activePart;
		}
		return null;
	}
	
	protected IHandler2 createHandler(final LtxRweaveSwitch type) {
		return null;
	}
	
	protected IHandler2 getHandler(final LtxRweaveSwitch type) {
		final int i= type.ordinal();
		if (this.handlers[i] == null) {
			this.handlers[i]= NO_HANDLER;
			final IHandler2 handler= createHandler(type);
			if (handler != null) {
				this.handlers[i]= handler;
			}
		}
		return (this.handlers[i] != NO_HANDLER) ? this.handlers[i] : null;
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISourceEditor editor= getEditor(event.getApplicationContext());
		if (editor == null) {
			return null;
		}
		final SourceViewer viewer= editor.getViewer();
		if (viewer == null) {
			return null;
		}
		final IHandler2 handler= getHandler(
				LtxRweaveSwitch.get(viewer.getDocument(), viewer.getSelectedRange().x) );
		if (handler != null) {
			return handler.execute(event);
		}
		return null;
	}
	
}
