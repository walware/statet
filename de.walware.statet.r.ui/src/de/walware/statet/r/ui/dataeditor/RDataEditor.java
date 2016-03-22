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

package de.walware.statet.r.ui.dataeditor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ts.ITool;

import de.walware.rj.data.RLanguage;
import de.walware.rj.data.defaultImpl.RLanguageImpl;
import de.walware.rj.eclient.FQRObjectRef;
import de.walware.rj.services.IFQRObjectRef;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.RUI;


public class RDataEditor {
	
	
	public static final String RDATA_EDITOR_ID= "de.walware.statet.r.editors.RData"; //$NON-NLS-1$
	
	
	private static FQRObjectRef createFQRef(final ITool tool, final RElementName elementName) {
		RElementName envName= elementName.getScope();
		RElementName objName= elementName;
		if (envName == null && RElementName.isScopeType(elementName.getType())) {
			envName= elementName;
			objName= elementName.getNextSegment();
		}
		if (envName == null) {
			throw new IllegalArgumentException("elementName not FQ.");
		}
		
		RElementName next= objName.getNextSegment();
		while (next != null) {
			if (next.getType() == RElementName.MAIN_DEFAULT) {
				objName= next;
			}
			next= next.getNextSegment();
		}
		
		envName= RElementName.create(envName, objName, true);
		objName= RElementName.create(objName, null, false);
		return new FQRObjectRef(tool,
				new RLanguageImpl(RLanguage.CALL, envName.getDisplayName(RElementName.DISPLAY_FQN | RElementName.DISPLAY_EXACT), null),
				new RLanguageImpl(RLanguage.CALL, objName.getDisplayName(RElementName.DISPLAY_EXACT), null) );
	}
	
	public static void open(final IWorkbenchPage page, final ITool tool,
			final RElementName elementName, final long[] indexes) {
		open(page, elementName, createFQRef(tool, elementName), indexes);
	}
	
	public static void open(final IWorkbenchPage page,
			final RElementName elementName, final IFQRObjectRef elementRef, final long[] indexes) {
		try {
			final IEditorPart editor= IDE.openEditor(page,
					new RLiveDataEditorInput(elementName, elementRef),
					RDATA_EDITOR_ID, true );
			
			final RDataTableComposite table= editor.getAdapter(RDataTableComposite.class);
			if (indexes != null && table != null) {
				switch (indexes.length) {
				case 1:
					table.setAnchorDataIdxs(0, indexes[0]);
					break;
				case 2:
					table.setAnchorDataIdxs(indexes[1], indexes[0]);
					break;
				default:
					break;
				}
			}
		}
		catch (final PartInitException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					"Failed to open the R element in the data editor.",
					e ));
		}
	}
	
}
