/*******************************************************************************
 * Copyright (c) 2005-2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.walware.eclipsecommons.ui.preferences.ICombinedPreferenceStore;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.editors.EditorMessages;
import de.walware.statet.ext.ui.editors.StatextEditor1;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.ui.internal.RUIPlugin;
import de.walware.statet.r.ui.text.r.RBracketPairMatcher;


public class REditor extends StatextEditor1<RProject> {


	public REditor() {
		super();
	}
	
	@Override
	protected void initializeEditor() {
		
		setDocumentProvider(RUIPlugin.getDefault().getRDocumentProvider());
		initStatext(new RBracketPairMatcher());

		super.initializeEditor();
	}
	
	@Override
	protected RProject getProject(IEditorInput input) {
		
		return (RProject) getProject(input, RProject.ID);
	}

	@Override
	protected void setupConfiguration(RProject project) {
		
		ICombinedPreferenceStore preferenceStore = RSourceViewerConfiguration.createCombinedPreferenceStore(
				RUIPlugin.getDefault().getPreferenceStore(), project);
		setPreferenceStore(preferenceStore);
		setSourceViewerConfiguration(new RSourceViewerConfiguration(this, 
				StatetPlugin.getDefault().getColorManager(), preferenceStore));
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		
		setKeyBindingScopes(new String[] { "de.walware.statet.r.ui.contexts.REditorScope" }); //$NON-NLS-1$
	}
	
	@Override
	protected String[] collectContextMenuPreferencePages() {
		
		String[] ids = super.collectContextMenuPreferencePages();
		String[] more = new String[ids.length + 2];
		more[0]= "de.walware.statet.r.ui.preferencePages.RSyntaxColoring"; //$NON-NLS-1$
		more[1]= "de.walware.statet.r.ui.preferencePages.REditorTemplates"; //$NON-NLS-1$
		System.arraycopy(ids, 0, more, 2, ids.length);
		return more;
	}
	
	@Override
	protected void createActions() {

		super.createActions();
		
		Action action = new ContentAssistAction(
				EditorMessages.getCompatibilityBundle(), "ContentAssistProposal_", this); //$NON-NLS-1$
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", action); //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		
		super.dispose();
	}

}
