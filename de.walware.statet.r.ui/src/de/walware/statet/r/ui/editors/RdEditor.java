/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.ext.ui.editors.SourceViewerConfigurator;
import de.walware.statet.ext.ui.editors.StatextEditor1;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class RdEditor extends StatextEditor1<RProject> {
	
	
	RdSourceViewerConfigurator fRdConfig;
	
	
	public RdEditor() {
		super();
	}
	
	@Override
	protected SourceViewerConfigurator createConfiguration() {
		configureStatetProjectNatureId(RProject.NATURE_ID);
		setDocumentProvider(RUIPlugin.getDefault().getRdDocumentProvider());
		
		final IPreferenceStore store = RUIPlugin.getDefault().getEditorPreferenceStore();
		fRdConfig = new RdSourceViewerConfigurator(null, store);
		fRdConfig.setConfiguration(new RdSourceViewerConfiguration(
				fRdConfig, store, StatetUIServices.getSharedColorManager()));
		return fRdConfig;
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);
		
		fRdConfig.setTarget(this);
	}
	
	@Override
	protected void setupConfiguration(final RProject prevProject, final RProject newProject, final IEditorInput newInput) {
		fRdConfig.setSource(newProject);
	}
	
	@Override
	protected String[] collectContextMenuPreferencePages() {
		final String[] ids = super.collectContextMenuPreferencePages();
		final String[] more = new String[ids.length + 1];
		more[0]= "de.walware.statet.r.preferencePages.RdSyntaxColoring"; //$NON-NLS-1$
		System.arraycopy(ids, 0, more, 1, ids.length);
		return more;
	}
	
}
