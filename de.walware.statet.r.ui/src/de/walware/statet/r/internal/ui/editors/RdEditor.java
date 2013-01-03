/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RdSourceViewerConfiguration;
import de.walware.statet.r.ui.editors.RdSourceViewerConfigurator;


public class RdEditor extends SourceEditor1 {
	
	
	private RdSourceViewerConfigurator fRdConfig;
	
	
	public RdEditor() {
		super();
	}
	
	@Override
	public String getModelTypeId() {
		return "Rd"; // not yet a real type
	}
	
	@Override
	protected SourceEditorViewerConfigurator createConfiguration() {
		setDocumentProvider(RUIPlugin.getDefault().getRdDocumentProvider());
		
		fRdConfig = new RdSourceViewerConfigurator(null, new RdSourceViewerConfiguration(
				RUIPlugin.getDefault().getEditorPreferenceStore(),
				SharedUIResources.getColors()) );
		return fRdConfig;
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);
		
		fRdConfig.setTarget(this);
	}
	
	@Override
	protected void setupConfiguration(final IEditorInput newInput) {
		fRdConfig.setSource((RProject) getProject(newInput, RProject.NATURE_ID));
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
