/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ui.refactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.CoreUtility;
import de.walware.ecommons.ltk.internal.ui.refactoring.ECommonsRefactoring;
import de.walware.ecommons.ltk.internal.ui.refactoring.Messages;
import de.walware.ecommons.ltk.ui.EditorUtility;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Helper to save dirty editors prior to starting a refactoring.
 * 
 * @see PreferenceConstants#REFACTOR_SAVE_ALL_EDITORS
 * @since 3.5
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefactoringSaveHelper {
	
	
	/**
	 * Save mode to not save any editors.
	 */
	public static final int SAVE_NOTHING = 0;
	
	/**
	 * Save mode to save all dirty editors.
	 */
	public static final int SAVE_ALL = 1;
	
	/**
	 * Save mode to save all editors that are known to cause trouble for Java refactorings, e.g.
	 * editors on compilation units that are not in working copy mode.
	 */
	public static final int SAVE_REFACTORING = 2;
	
	
	public static final int EXCLUDE_ACTIVE_EDITOR = 0x10;
	
	public static final int OPTIONAL = 0x100;
	public static final int ASK_ALWAYS = 0x200;
	
	
	private boolean fFilesSaved;
	private final int fSaveMode;
	
	
	/**
	 * Creates a refactoring save helper with the given save mode.
	 * 
	 * @param saveMode one of the SAVE_* constants
	 */
	public RefactoringSaveHelper(final int saveMode) {
		fSaveMode = saveMode;
	}
	
	
	/**
	 * Saves all editors. Depending on the {@link PreferenceConstants#REFACTOR_SAVE_ALL_EDITORS}
	 * preference, the user is asked to save affected dirty editors.
	 * 
	 * @param shell the parent shell for the confirmation dialog
	 * @return <code>true</code> if save was successful and refactoring can proceed;
	 *     false if the refactoring must be cancelled
	 */
	public boolean saveEditors(final Shell shell) {
		final List<IEditorPart> dirtyEditors;
		switch (fSaveMode & 0xf) {
			case SAVE_ALL:
				dirtyEditors = EditorUtility.getDirtyEditors(true);
				break;
			case SAVE_REFACTORING:
//				dirtyEditors = EditorUtility.getDirtyEditorsToSave(false); // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=175495
				dirtyEditors = EditorUtility.getDirtyEditors(true);
				break;
			case SAVE_NOTHING:
				return true;
			default:
				throw new IllegalStateException(Integer.toString(fSaveMode));
		}
		if ((fSaveMode & EXCLUDE_ACTIVE_EDITOR) != 0) {
			final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
			dirtyEditors.remove(page.getActiveEditor());
		}
		if (dirtyEditors.isEmpty()) {
			return true;
		}
		if (!askSaveAllDirtyEditors(shell, dirtyEditors)) {
			return false;
		}
		
		try {
			// Save isn't cancelable.
			final boolean autoBuild = CoreUtility.setAutoBuilding(false);
			try {
				if ((fSaveMode & 0xf) == SAVE_ALL
						|| ECommonsRefactoring.getSaveAllEditors()) {
					if (!PlatformUI.getWorkbench().saveAllEditors(false))
						return false;
				}
				else {
					final IRunnableWithProgress runnable = new IRunnableWithProgress() {
						public void run(final IProgressMonitor pm) throws InterruptedException {
							final int count = dirtyEditors.size();
							pm.beginTask("", count); //$NON-NLS-1$
							for (int i = 0; i < count; i++) {
								final IEditorPart editor = dirtyEditors.get(i);
								editor.doSave(new SubProgressMonitor(pm, 1));
								if (pm.isCanceled())
									throw new InterruptedException();
							}
							pm.done();
						}
					};
					try {
						PlatformUI.getWorkbench().getProgressService().runInUI(UIAccess.getActiveWorkbenchWindow(true), runnable, null);
					}
					catch (final InterruptedException e) {
						return false;
					}
					catch (final InvocationTargetException e) {
						StatusManager.getManager().handle(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
								Messages.RefactoringStarter_UnexpectedException, e.getCause()) );
						return false;
					}
				}
				fFilesSaved = true;
			}
			finally {
				CoreUtility.setAutoBuilding(autoBuild);
			}
			return true;
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
					Messages.RefactoringStarter_UnexpectedException, e) );
			return false;
		}
	}
	
	
	/**
	 * Triggers an incremental build if this save helper did save files before.
	 */
	public void triggerBuild() {
		if (fFilesSaved) {
			new GlobalBuildAction(UIAccess.getActiveWorkbenchWindow(true), IncrementalProjectBuilder.INCREMENTAL_BUILD).run();
		}
	}
	
	/**
	 * Triggers an incremental build if this save helper did save files before.
	 */
	public void triggerAutoBuild() {
		if (fFilesSaved && ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			new GlobalBuildAction(UIAccess.getActiveWorkbenchWindow(true), IncrementalProjectBuilder.INCREMENTAL_BUILD).run();
		}
	}
	
	
	/**
	 * Returns whether this save helper did actually save any files.
	 * 
	 * @return <code>true</code> iff files have been saved
	 */
	public boolean didSaveFiles() {
		return fFilesSaved;
	}
	
	private boolean askSaveAllDirtyEditors(final Shell shell, final List<IEditorPart> dirtyEditors) {
		final boolean canSaveAutomatically = (fSaveMode & ASK_ALWAYS) == 0;
		if (canSaveAutomatically && ECommonsRefactoring.getSaveAllEditors()) //must save everything
			return true;
		final ListDialog dialog = new ListDialog(shell) {
			{
				setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
			}
			@Override
			protected Control createDialogArea(final Composite parent) {
				final Composite result = (Composite) super.createDialogArea(parent);
				if (canSaveAutomatically) {
					final Button check = new Button(result, SWT.CHECK);
					check.setText(Messages.RefactoringStarter_ConfirmSave_Always_message);
					check.setSelection(ECommonsRefactoring.getSaveAllEditors());
					check.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							ECommonsRefactoring.setSaveAllEditors(check.getSelection());
						}
					});
					applyDialogFont(result);
				}
				return result;
			}
		};
		dialog.setTitle(Messages.RefactoringStarter_ConfirmSave_title);
		dialog.setMessage(Messages.RefactoringStarter_ConfirmSave_message);
		dialog.setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(final Object element) {
				return ((IEditorPart) element).getTitleImage();
			}
			@Override
			public String getText(final Object element) {
				return ((IEditorPart) element).getTitle();
			}
		});
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setInput(dirtyEditors);
		
		return (dialog.open() == Dialog.OK);
	}
	
}
