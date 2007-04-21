/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import java.util.EnumSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.progress.WorkbenchJob;

import de.walware.eclipsecommons.ui.dialogs.ShortedLabel;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.nico.core.runtime.IProgressInfo;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStatus;
import de.walware.statet.nico.ui.NicoUITools;


/**
 * UI Component showing the progress information of a NICO tool.
 */
public class ToolProgressGroup {

	
	private static final IProgressInfo DUMMY_INFO = new IProgressInfo() {
		public String getLabel() {
			return ""; //$NON-NLS-1$
		}
		public String getSubLabel() {
			return ""; //$NON-NLS-1$
		}
		public int getWorked() {
			return 0;
		}
		public IToolRunnable getRunnable() {
			return null;
		}
	};
	
	
	private static final int SCHEDULE_ON_EVENT = 50;
	private static final int SCHEDULE_DEFAULT = 150;
	
	private static final EnumSet<ToolStatus> gFreshStates = EnumSet.complementOf(
			EnumSet.of(ToolStatus.STARTED_IDLING, ToolStatus.STARTED_PAUSED, ToolStatus.TERMINATED));
	
	
	private class RefreshJob extends WorkbenchJob {
		
		RefreshJob() {
			
			super("ToolProgress Refresh"); //$NON-NLS-1$
			setSystem(true);
		}
		
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			
			internalRefresh();
			if (fTool.fScheduleRefresh) {
				schedule(SCHEDULE_DEFAULT);
			}
			
			return Status.OK_STATUS;
		}
	}
	
	private class DebugEventListener implements IDebugEventSetListener {
		
		public void handleDebugEvents(DebugEvent[] events) {
			ToolInfo tool = fTool;
			for (DebugEvent event : events) {
				if (tool.fProcess == event.getSource()) {
					ToolStatus status = ToolProcess.getChangedToolStatus(event);
					if (status != null) {
						tool.fScheduleRefresh = gFreshStates.contains(status);
						fRefreshJob.schedule(SCHEDULE_ON_EVENT);
					}
				}
			}
		}
	}
	
	private class ToolInfo {
		
		ToolProcess fProcess;
		ImageDescriptor fImageCache;
		boolean fScheduleRefresh = false;
		
		ToolInfo(ToolProcess process) {
			
			fProcess = process;
			if (process != null) {
				fScheduleRefresh = gFreshStates.contains(process.getToolStatus());
			}
			else {
				fScheduleRefresh = false;
			}

		}
	}
	
	private DebugEventListener fDebugEventListener;
	private Job fRefreshJob;
	
	private Composite fComposite;
	private Label fImageLabel;
	private ShortedLabel fMainLabel;
	private ProgressBar fProgressBar;
	private ShortedLabel fSubLabel;
	
	private ToolInfo fTool = new ToolInfo(null);
	
	
	/**
	 * 
	 */
	public ToolProgressGroup(Composite parent) {
		
		fRefreshJob = new RefreshJob();
		fDebugEventListener = new DebugEventListener();
		
		createControls(parent);
		
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.addDebugEventListener(fDebugEventListener);
		}
	}
	
	
	private void createControls(Composite parent) {
		
		fComposite = new Composite(parent, SWT.NONE) {
			@Override
			public void dispose() {
				ToolProgressGroup.this.dispose();
				super.dispose();
			}
		};
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 2;
		layout.verticalSpacing = 2;
		fComposite.setLayout(layout);
		
		fImageLabel = new Label(fComposite, SWT.NONE);
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.verticalSpan = 2;
		gd.verticalIndent = 2;
		gd.widthHint = 16;
		gd.heightHint = 16;
		fImageLabel.setLayoutData(gd);
		
		fMainLabel = new ShortedLabel(fComposite, SWT.NONE);
		fMainLabel.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fSubLabel = new ShortedLabel(fComposite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fSubLabel.getControl().setLayoutData(gd);
		
		fProgressBar = new ProgressBar(fComposite, SWT.HORIZONTAL);
		fProgressBar.setMinimum(0);
		fProgressBar.setMaximum(IToolRunnable.TOTAL_WORK);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;
		fProgressBar.setLayoutData(gd);
	}
	
	public Control getControl() {
		
		return fComposite;
	}
	
	
	public void setTool(ToolProcess tool, boolean directRefresh) {
		
		fTool = new ToolInfo(tool);
		refresh(directRefresh);
	}
	
	/**
	 * Refreshes the components.
	 * 
	 * If <code>directRefresh</code> is requested, you have to be in UI thread.
	 * 
	 * @param directRefresh refresh is directly executed instead of scheduled.
	 */
	public void refresh(boolean directRefresh) {
		
		if (directRefresh) {
			fRefreshJob.cancel();
			internalRefresh();
			fRefreshJob.schedule(SCHEDULE_DEFAULT);
		}
		else {
			fRefreshJob.schedule(SCHEDULE_ON_EVENT);
		}
	}
	
	private void internalRefresh() {
		
		if (!UIAccess.isOkToUse(fComposite)) {
			return;
		}
		ToolInfo tool = fTool;
		ToolController controller = (tool.fProcess != null) ? tool.fProcess.getController() : null;
		IProgressInfo info = (controller != null) ? controller.getProgressInfo() : DUMMY_INFO;
		Image image = null;
		IToolRunnable runnable = info.getRunnable();
		if (runnable != null) {
			ImageDescriptor imageDescr = NicoUITools.getImageDescriptor(runnable);
			if (imageDescr != null) {
				image = StatetImages.getCachedImage(imageDescr);
			}
		}
		if (image == null && tool.fProcess != null) {
			image = getToolImage(tool);
		}
		if (image == null) {
			image = StatetImages.getImage(StatetImages.OBJ_COMMAND_DUMMY);
		}
		if (!(image.equals(fImageLabel.getImage()))) {
			fImageLabel.setImage(image);
		}
		fMainLabel.setText(info.getLabel());
		fSubLabel.setText(info.getSubLabel());
		fProgressBar.setSelection(info.getWorked());
	}
	
	private Image getToolImage(ToolInfo tool) {
		
		if (tool.fImageCache == null) {
			tool.fImageCache = NicoUITools.getImageDescriptor(tool.fProcess);
		}
		if (tool.fImageCache != null) {
			return StatetImages.getCachedImage(tool.fImageCache);
		}
		return null;
	}
	
	private void dispose() {
		
		fTool = new ToolInfo(null);
		fRefreshJob.cancel();
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.removeDebugEventListener(fDebugEventListener);
		}
	}
}
