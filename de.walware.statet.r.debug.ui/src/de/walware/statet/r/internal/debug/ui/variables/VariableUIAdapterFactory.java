/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.variables;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;

import de.walware.statet.r.debug.core.IRVariable;
import de.walware.statet.r.internal.debug.ui.actions.WatchHandler;


public class VariableUIAdapterFactory implements IAdapterFactory {
	
	
	private static final Class<?>[] ADAPTERS= new Class<?>[] {
		IElementLabelProvider.class,
		IElementEditor.class,
		IWatchExpressionFactoryAdapter.class,
	};
	
	
	private final IElementLabelProvider labelProvider= new RVariableLabelProvider();
	private final IElementEditor variableEditor= new RVariableEditor();
	
	private final IWatchExpressionFactoryAdapter watchExprFactory= new WatchHandler();
	
	
	public VariableUIAdapterFactory() {
	}
	
	
	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTERS;
	}
	
	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType == IElementLabelProvider.class) {
			if (adaptableObject instanceof IRVariable) {
				return this.labelProvider;
			}
		}
		else if (adapterType == IElementEditor.class) {
			if (adaptableObject instanceof IRVariable) {
				return this.variableEditor;
			}
		}
		else if (adapterType == IWatchExpressionFactoryAdapter.class) {
			if (adaptableObject instanceof IRVariable) {
				return this.watchExprFactory;
			}
		}
		return null;
	}
	
}
