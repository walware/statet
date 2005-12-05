/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.editors.StatextEditor1;
import de.walware.statet.ext.ui.text.PairMatcher;
import de.walware.statet.r.ui.IRDocumentPartitions;
import de.walware.statet.r.ui.RUiPlugin;


public class RdEditor extends StatextEditor1 {

	private static final char[][] BRACKETS = { {'{', '}'} };

	
	public RdEditor() {
		super();
		
		setDocumentProvider(RUiPlugin.getDefault().getRdDocumentProvider());
		initStatext(
				new PairMatcher(BRACKETS, IRDocumentPartitions.RDOC_DOCUMENT_PARTITIONING, IRDocumentPartitions.R_DEFAULT, '\\')
		);
	}
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		IPreferenceStore preferenceStore = createCombinedPreferenceStore(); 
		setPreferenceStore(preferenceStore);
		
		setSourceViewerConfiguration(new RdSourceViewerConfiguration(
				StatetPlugin.getDefault().getColorManager(), preferenceStore));
	}
	
	private IPreferenceStore createCombinedPreferenceStore() {
		List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>(3);
		
		stores.add(RUiPlugin.getDefault().getPreferenceStore());
		stores.add(StatetPlugin.getDefault().getPreferenceStore());
		stores.add(EditorsUI.getPreferenceStore());
		
		return new ChainedPreferenceStore((IPreferenceStore[]) stores.toArray(new IPreferenceStore[stores.size()]));
	}
	
	
	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected String[] collectContextMenuPreferencePages() {
		String[] ids = super.collectContextMenuPreferencePages();
		String[] more = new String[ids.length + 1];
		more[0]= "de.walware.statet.r.ui.preferences.RdSyntaxColoringPreferencePage"; //$NON-NLS-1$
		System.arraycopy(ids, 0, more, 1, ids.length);
		return more;
	}
	
}
