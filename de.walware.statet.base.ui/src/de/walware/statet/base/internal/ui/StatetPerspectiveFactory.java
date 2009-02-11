/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.ui;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.texteditor.templates.TemplatesView;


public class StatetPerspectiveFactory implements IPerspectiveFactory {
	
	
	private static final String ID_SEARCH_VIEW = "org.eclipse.search.ui.views.SearchView"; // NewSearchUI.SEARCH_VIEW_ID)  //$NON-NLS-1$
	private static final String ID_CONSOLE_VIEW = "org.eclipse.ui.console.ConsoleView"; // IConsoleConstants.ID_CONSOLE_VIEW //$NON-NLS-1$
	
	
	/**
	 * Constructs a new Default layout engine.
	 */
	public StatetPerspectiveFactory() {
		super();
	}
	
	public void createInitialLayout(final IPageLayout layout) {
		final String editorArea = layout.getEditorArea();
		
		final IFolderLayout leftFolder = layout.createFolder("left", IPageLayout.LEFT, 0.25f, editorArea); //$NON-NLS-1$
		leftFolder.addView(ProjectExplorer.VIEW_ID);
		leftFolder.addPlaceholder(IPageLayout.ID_RES_NAV);
		
		final IFolderLayout outputfolder = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.75f, editorArea); //$NON-NLS-1$
		outputfolder.addView(IPageLayout.ID_TASK_LIST);
		outputfolder.addView(ID_CONSOLE_VIEW);
		outputfolder.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
		outputfolder.addPlaceholder(ID_SEARCH_VIEW);
		outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
		
		final IFolderLayout editorAddonFolder = layout.createFolder("editor-additions", IPageLayout.RIGHT, 0.75f, editorArea); //$NON-NLS-1$
		editorAddonFolder.addView(IPageLayout.ID_OUTLINE);
		editorAddonFolder.addPlaceholder(TemplatesView.ID);
		
//		layout.createPlaceholderFolder("console-additions", IPageLayout.BOTTOM, 0.80f, "left"); //$NON-NLS-1$
		
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		// views - search
		layout.addShowViewShortcut(ID_SEARCH_VIEW);
		
		// views - debugging
		layout.addShowViewShortcut(ID_CONSOLE_VIEW);
		
		// views - standard workbench
		layout.addShowViewShortcut(ProjectExplorer.VIEW_ID);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(TemplatesView.ID);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		
		// new actions
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$
	}
	
}
