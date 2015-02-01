/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.internal.ui.ToolSourceProvider;


public abstract class AbstractToolHandler extends AbstractHandler {
	
	
	private final String fRequiredMainType;
	private final String fRequiredFeatureSet;
	
	
	protected AbstractToolHandler(final String mainType) {
		this(mainType, null);
	}
	
	protected AbstractToolHandler(final String mainType, final String featureSet) {
		fRequiredMainType = mainType;
		fRequiredFeatureSet = featureSet;
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final ITool tool = (ITool) HandlerUtil.getVariable(evaluationContext, ToolSourceProvider.ACTIVE_TOOL_NAME);
		setBaseEnabled(isValid(tool));
	}
	
	protected boolean isValid(final ITool tool) {
		return (tool != null
				&& (fRequiredMainType == null || tool.getMainType() == fRequiredMainType)
				&& (fRequiredFeatureSet == null || tool.isProvidingFeatureSet(fRequiredFeatureSet))
				&& isSupported(tool) );
	}
	
	protected boolean isSupported(final ITool tool) {
		return !tool.isTerminated();
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ITool tool = (ITool) HandlerUtil.getVariable(event, ToolSourceProvider.ACTIVE_TOOL_NAME);
		if (!isValid(tool)) {
			return null;
		}
		return execute(tool, event);
	}
	
	protected abstract Object execute(ITool tool, ExecutionEvent event) throws ExecutionException;
	
}
