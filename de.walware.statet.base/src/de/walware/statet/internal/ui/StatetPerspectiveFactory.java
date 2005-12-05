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

package de.walware.statet.internal.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.progress.IProgressConstants;


public class StatetPerspectiveFactory implements IPerspectiveFactory {
	
	
	private static final String ID_SEARCH_VIEW = "org.eclipse.search.ui.views.SearchView"; // NewSearchUI.SEARCH_VIEW_ID)  //$NON-NLS-1$
	private static final String ID_CONSOLE_VIEW = "org.eclipse.ui.console.ConsoleView"; // IConsoleConstants.ID_CONSOLE_VIEW //$NON-NLS-1$
	
	/**
	 * Constructs a new Default layout engine.
	 */
	public StatetPerspectiveFactory() {
		super();
	}

	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT, 0.25f, editorArea); //$NON-NLS-1$
		folder.addView(IPageLayout.ID_RES_NAV);
		
		IFolderLayout outputfolder = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.75f, editorArea); //$NON-NLS-1$
//		outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		outputfolder.addView(IPageLayout.ID_TASK_LIST);
		outputfolder.addPlaceholder(ID_SEARCH_VIEW);
		outputfolder.addPlaceholder(ID_CONSOLE_VIEW);
		outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
		
		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.75f, editorArea);
		
//		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
//		layout.addActionSet(JavaUI.ID_ACTION_SET);
//		layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		// views - search
		layout.addShowViewShortcut(ID_SEARCH_VIEW); 
		
		// views - debugging
		layout.addShowViewShortcut(ID_CONSOLE_VIEW); 

		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		
		// new actions
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$
	}
}
