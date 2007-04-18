/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import org.eclipse.ui.IEditorInput;

import de.walware.eclipsecommons.ui.preferences.ICombinedPreferenceStore;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.editors.StatextEditor1;
import de.walware.statet.ext.ui.text.PairMatcher;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.IRDocumentPartitions;


public class RdEditor extends StatextEditor1<RProject> {

	private static final char[][] BRACKETS = { {'{', '}'} };

	
	public RdEditor() {
		super();
	}
		
	@Override
	protected void initializeEditor() {

		setDocumentProvider(RUIPlugin.getDefault().getRdDocumentProvider());
		initStatext(new PairMatcher(BRACKETS, IRDocumentPartitions.RDOC_DOCUMENT_PARTITIONING, IRDocumentPartitions.R_DEFAULT, '\\'));
		
		super.initializeEditor();
	}
	
	@Override
	protected RProject getProject(IEditorInput input) {
		
		return (RProject) getProject(input, RProject.NATURE_ID);
	}
	
	protected void setupConfiguration(RProject project) {
		
		ICombinedPreferenceStore preferenceStore = RSourceViewerConfiguration.createCombinedPreferenceStore(
				RUIPlugin.getDefault().getPreferenceStore(), project);
		setPreferenceStore(preferenceStore);
		setSourceViewerConfiguration(new RdSourceViewerConfiguration( 
				StatetPlugin.getDefault().getColorManager(), preferenceStore));
	}
	
	@Override
	protected String[] collectContextMenuPreferencePages() {
		
		String[] ids = super.collectContextMenuPreferencePages();
		String[] more = new String[ids.length + 1];
		more[0]= "de.walware.statet.r.ui.preferencePages.RdSyntaxColoring"; //$NON-NLS-1$
		System.arraycopy(ids, 0, more, 1, ids.length);
		return more;
	}

	@Override
	public void dispose() {
		
		super.dispose();
	}

}
