/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.base.internal.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.walware.ecommons.workbench.ui.IWorkbenchPerspectiveElements;


public class StatetPerspectiveFactory implements IPerspectiveFactory, IWorkbenchPerspectiveElements {
	
	
	/**
	 * Constructs a new Default layout engine.
	 */
	public StatetPerspectiveFactory() {
		super();
	}
	
	@Override
	public void createInitialLayout(final IPageLayout layout) {
		final String editorArea = layout.getEditorArea();
		
		final IFolderLayout leftFolder = layout.createFolder(
				"left", IPageLayout.LEFT, 0.25f, editorArea); //$NON-NLS-1$
		leftFolder.addView(PROJECT_EXPLORER_VIEW);
		leftFolder.addPlaceholder(RESOURCE_NAVIGATOR_VIEW);
		
		final IFolderLayout outputfolder = layout.createFolder(
				"bottom", IPageLayout.BOTTOM, 0.70f, editorArea); //$NON-NLS-1$
		outputfolder.addView(CONSOLE_VIEW);
		outputfolder.addView(TASKS_VIEW);
		outputfolder.addPlaceholder(PROBLEM_VIEW);
		outputfolder.addPlaceholder(SEARCH_VIEW);
		outputfolder.addPlaceholder(BOOKMARKS_VIEW);
		outputfolder.addPlaceholder(PROGRESS_VIEW);
		
		final IFolderLayout editorAddFolder = layout.createFolder(
				"editor-additions", IPageLayout.RIGHT, 0.75f, editorArea); //$NON-NLS-1$
		editorAddFolder.addView(OUTLINE_VIEW);
		editorAddFolder.addPlaceholder(TEMPLATES_VIEW);
		editorAddFolder.addPlaceholder(FILTERS_VIEW);
		
		final IFolderLayout consoleAddFolder = layout.createFolder(
				"console-additions", IPageLayout.BOTTOM, 0.60f, "left"); //$NON-NLS-1$ //$NON-NLS-2$
		consoleAddFolder.addView(NICO_OBJECTBROWSER_VIEW);
		consoleAddFolder.addView(NICO_CMDHISTORY_VIEW);
		
//		layout.createPlaceholderFolder("console-additions", IPageLayout.BOTTOM, 0.80f, "left"); //$NON-NLS-1$
		
		layout.addActionSet(LAUNCH_ACTION_SET);
		layout.addActionSet(BREAKPOINT_ACTION_SET);
		layout.addActionSet(NAVIGATE_ACTION_SET);
		
		// views - search
		layout.addShowViewShortcut(SEARCH_VIEW);
		
		// views - standard workbench
		layout.addShowViewShortcut(PROJECT_EXPLORER_VIEW);
		layout.addShowViewShortcut(OUTLINE_VIEW);
		layout.addShowViewShortcut(TEMPLATES_VIEW);
		layout.addShowViewShortcut(TASKS_VIEW);
		layout.addShowViewShortcut(PROBLEM_VIEW);
		
		// views - debugging
		layout.addShowViewShortcut(CONSOLE_VIEW);
		layout.addShowViewShortcut(NICO_OBJECTBROWSER_VIEW);
		layout.addShowViewShortcut(NICO_CMDHISTORY_VIEW);
		
		// new actions
		layout.addNewWizardShortcut(NEW_FOLDER_WIZARD);
		layout.addNewWizardShortcut(NEW_TEXTFILE_WIZARD);
		layout.addNewWizardShortcut(NEW_UNTITLED_TEXTFILE_WIZARD);
		
		layout.addPerspectiveShortcut(DEBUG_PERSPECTIVE);
	}
	
}
