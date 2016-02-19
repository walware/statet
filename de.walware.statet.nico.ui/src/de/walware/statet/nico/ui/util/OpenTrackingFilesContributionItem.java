/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ui.actions.SimpleContributionItem;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.ITrack;
import de.walware.statet.nico.core.runtime.ToolProcess;


/**
 * Contribution item creating menu items to open track files.
 */
public class OpenTrackingFilesContributionItem extends CompoundContributionItem {
	
	
	private static class RefreshFileJob extends Job {
		
		
		private final IFile fFile;
		
		
		public RefreshFileJob(final String name, final IFile file) {
			super("Refresh track file of '"+name+"'");
			
			fFile = file;
			
			setSystem(true);
			setUser(false);
			final IResourceRuleFactory ruleFactory = file.getWorkspace().getRuleFactory();
			setRule(ruleFactory.refreshRule(file));
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			try {
				fFile.refreshLocal(IResource.DEPTH_ONE, monitor);
			}
			catch (final CoreException e) {
			}
			return Status.OK_STATUS;
		}
		
	}
	
	public static void open(final String name, final IFileStore fileStore) {
		try {
			final IFile workspaceFile = FileUtil.getAsWorkspaceFile(fileStore.toURI());
			if (workspaceFile != null) {
				new RefreshFileJob(name, workspaceFile).schedule();
				try {
					Thread.sleep(50);
				}
				catch (final InterruptedException e) {}
				IDE.openEditor(UIAccess.getActiveWorkbenchPage(true), workspaceFile);
			}
			else {
				IDE.openEditorOnFileStore(UIAccess.getActiveWorkbenchPage(true), fileStore);
			}
		}
		catch (final PartInitException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1,
					"An error occurred when opening the editor on the track file of "+name+".", e));
		}
	}
	
	
	private final ToolProcess fTool;
	
	
	public OpenTrackingFilesContributionItem(final ToolProcess tool) {
		if (tool == null) {
			throw new NullPointerException();
		}
		fTool = tool;
	}
	
	
	@Override
	protected IContributionItem[] getContributionItems() {
		final List<? extends ITrack> tracks = fTool.getTracks();
		final List<IContributionItem> items= new ArrayList<>(tracks.size());
		for (int i = 0; i < tracks.size(); i++) {
			final ITrack track = tracks.get(i);
			final IFileStore file = track.getFile();
			if (file != null) {
				final SimpleContributionItem item = new SimpleContributionItem(
						track.getName(), null) {
					@Override
					protected void execute() throws ExecutionException {
						track.flush();
						open(track.getName(), track.getFile());
					}
				};
				items.add(item);
			}
		}
		return items.toArray(new IContributionItem[items.size()]);
	}
}
