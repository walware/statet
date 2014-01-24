/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.ftable.internal.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RObject;

import de.walware.statet.rtm.base.ui.RtModelUIPlugin;
import de.walware.statet.rtm.base.ui.rexpr.DefaultRExprTypeUIAdapters;
import de.walware.statet.rtm.base.ui.rexpr.IRExprTypesUIProvider;
import de.walware.statet.rtm.base.ui.rexpr.RExprTypeUIAdapter;
import de.walware.statet.rtm.base.util.RExprType;
import de.walware.statet.rtm.base.util.RExprTypes;
import de.walware.statet.rtm.ftable.core.FTableExprTypesProvider;


public class FTableExprTypesUIProvider extends FTableExprTypesProvider
		implements IRExprTypesUIProvider {
	
	
	private static final DefaultRExprTypeUIAdapters ADAPTERS = new DefaultRExprTypeUIAdapters();
	
	
	private static final RExprTypeUIAdapter FTABLE_DATA_ADAPTER = new DefaultRExprTypeUIAdapters
			.RObjectTypeAdapter(RExprType.DATAFRAME_TYPE,
					RtModelUIPlugin.getDefault().getImageRegistry().get(RtModelUIPlugin.OBJ_DATAFRAME_TYPE_IMAGE_ID) ) {
		
		@Override
		protected boolean isValidRObject(final RObject rObject) {
			switch (rObject.getRObjectType()) {
			case RObject.TYPE_DATAFRAME:
				return true;
			case RObject.TYPE_ARRAY:
				return (((RArray<?>) rObject).getDim().getLength() >= 2);
			default:
				return false;
			}
		}
		
	};
	
	
	@Override
	public List<RExprTypeUIAdapter> getUIAdapters(final RExprTypes types,
			final EClass eClass, final EStructuralFeature eFeature) {
		final List<RExprTypeUIAdapter> uiAdapters = new ArrayList<RExprTypeUIAdapter>();
		for (final RExprType type : types.getTypes()) {
			RExprTypeUIAdapter uiAdapter = null;
			if (type == RExprType.DATAFRAME_TYPE) {
				uiAdapter = FTABLE_DATA_ADAPTER;
			}
			else {
				uiAdapter = ADAPTERS.getUIAdapter(type, eFeature);
			}
			if (uiAdapter != null) {
				uiAdapters.add(uiAdapter);
			}
		}
		return uiAdapters;
	}
	
}
