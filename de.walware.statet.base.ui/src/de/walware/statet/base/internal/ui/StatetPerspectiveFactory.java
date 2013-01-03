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

package de.walware.statet.base.internal.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.texteditor.templates.TemplatesView;

import de.walware.statet.base.ui.contentfilter.FilterView;


public class StatetPerspectiveFactory implements IPerspectiveFactory {
	
	private static final String ID_SEARCH_VIEW = "org.eclipse.search.ui.views.SearchView"; // NewSearchUI.SEARCH_VIEW_ID)  //$NON-NLS-1$
	private static final String ID_CONSOLE_VIEW = "org.eclipse.ui.console.ConsoleView"; // IConsoleConstants.ID_CONSOLE_VIEW //$NON-NLS-1$
	
	private static final String ID_NICO_CMDHISTORY_VIEW = "de.walware.statet.nico.views.HistoryView"; //$NON-NLS-1$
	private static final String ID_NICO_OBJECTBROWSER_VIEW = "de.walware.statet.nico.views.ObjectBrowser"; //$NON-NLS-1$
	
	
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
		leftFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);
		leftFolder.addPlaceholder(IPageLayout.ID_RES_NAV);
		
		final IFolderLayout outputfolder = layout.createFolder(
				"bottom", IPageLayout.BOTTOM, 0.70f, editorArea); //$NON-NLS-1$
		outputfolder.addView(ID_CONSOLE_VIEW);
		outputfolder.addView(IPageLayout.ID_TASK_LIST);
		outputfolder.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
		outputfolder.addPlaceholder(ID_SEARCH_VIEW);
		outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
		
		final IFolderLayout editorAddFolder = layout.createFolder(
				"editor-additions", IPageLayout.RIGHT, 0.75f, editorArea); //$NON-NLS-1$
		editorAddFolder.addView(IPageLayout.ID_OUTLINE);
		editorAddFolder.addPlaceholder(TemplatesView.ID);
		editorAddFolder.addPlaceholder(FilterView.VIEW_ID);
		
		final IFolderLayout consoleAddFolder = layout.createFolder(
				"console-additions", IPageLayout.BOTTOM, 0.60f, "left"); //$NON-NLS-1$ //$NON-NLS-2$
		consoleAddFolder.addView(ID_NICO_OBJECTBROWSER_VIEW);
		consoleAddFolder.addView(ID_NICO_CMDHISTORY_VIEW);
		
		//		layout.createPlaceholderFolder("console-additions", IPageLayout.BOTTOM, 0.80f, "left"); //$NON-NLS-1$
		
		layout.addActionSet("org.eclipse.debug.ui.launchActionSet"); //$NON-NLS-1$
		layout.addActionSet("org.eclipse.debug.ui.breakpointActionSet"); //$NON-NLS-1$
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		// views - search
		layout.addShowViewShortcut(ID_SEARCH_VIEW);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(TemplatesView.ID);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		
		// views - debugging
		layout.addShowViewShortcut(ID_CONSOLE_VIEW);
		layout.addShowViewShortcut(ID_NICO_OBJECTBROWSER_VIEW);
		layout.addShowViewShortcut(ID_NICO_CMDHISTORY_VIEW);
		
		// new actions
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard"); //$NON-NLS-1$
		
		layout.addPerspectiveShortcut("org.eclipse.debug.ui.DebugPerspective"); //$NON-NLS-1$
	}
	
}
