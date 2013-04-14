
package de.walware.statet.rtm.base.internal.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.walware.ecommons.workbench.ui.IWorkbenchPerspectiveElements;


public class RGraphPerspectiveFactory implements IPerspectiveFactory, IWorkbenchPerspectiveElements {
	
	
	public RGraphPerspectiveFactory() {
	}
	
	
	@Override
	public void createInitialLayout(final IPageLayout layout) {
		final String editorArea = layout.getEditorArea();
		
//		layout.addFastView(PROJECT_EXPLORER_VIEW, 0.25f);
		final IFolderLayout leftFolder = layout.createFolder(
				"left", IPageLayout.LEFT, 0.20f, editorArea); //$NON-NLS-1$
		leftFolder.addView(PROJECT_EXPLORER_VIEW);
		leftFolder.addPlaceholder(RESOURCE_NAVIGATOR_VIEW);
		
		final IFolderLayout editorAddFolder = layout.createFolder(
				"editor-additions", IPageLayout.RIGHT, 0.50f, editorArea); //$NON-NLS-1$
		editorAddFolder.addView("de.walware.statet.r.views.RGraphic"); //$NON-NLS-1$
		editorAddFolder.addPlaceholder(OUTLINE_VIEW);
		editorAddFolder.addPlaceholder(TEMPLATES_VIEW);
		editorAddFolder.addPlaceholder(FILTERS_VIEW);
		
		final IFolderLayout propertiesFolder = layout.createFolder(
				"bottom", IPageLayout.BOTTOM, 0.75f, editorArea); //$NON-NLS-1$
		propertiesFolder.addView(PROPERTIES_VIEW);
		propertiesFolder.addView(TASKS_VIEW);
		propertiesFolder.addPlaceholder(PROBLEM_VIEW);
		propertiesFolder.addPlaceholder(SEARCH_VIEW);
		propertiesFolder.addPlaceholder(BOOKMARKS_VIEW);
		
		final IFolderLayout consoleFolder = layout.createFolder(
				"console", IPageLayout.BOTTOM, 0.75f, "editor-additions"); //$NON-NLS-1$ //$NON-NLS-2$
		consoleFolder.addView(CONSOLE_VIEW);
		consoleFolder.addPlaceholder(PROGRESS_VIEW);
		
		final IFolderLayout consoleAddFolder = layout.createFolder(
				"console-additions", IPageLayout.BOTTOM, 0.50f, "left"); //$NON-NLS-1$ //$NON-NLS-2$
		consoleAddFolder.addView(NICO_OBJECTBROWSER_VIEW);
//		layout.addFastView(NICO_CMDHISTORY_VIEW);
		
//		layout.createPlaceholderFolder("console-additions", IPageLayout.BOTTOM, 0.80f, "left"); //$NON-NLS-1$
		
		layout.addActionSet(LAUNCH_ACTION_SET);
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
		
		layout.addPerspectiveShortcut("de.walware.statet.base.perspectives.StatetPerspective"); //$NON-NLS-1$
	}
	
}
