/*=============================================================================#
 # Copyright (c) 2006-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.progress.WorkbenchJob;

import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ui.components.ShortedLabel;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IProgressInfo;
import de.walware.statet.nico.core.runtime.Queue;
import de.walware.statet.nico.core.runtime.Queue.StateDelta;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;


/**
 * UI Component showing the progress information of a NICO tool.
 */
public class ToolProgressGroup {
	
	
	private static final IProgressInfo DUMMY_INFO= new IProgressInfo() {
		@Override
		public String getLabel() {
			return ""; //$NON-NLS-1$
		}
		@Override
		public String getSubLabel() {
			return ""; //$NON-NLS-1$
		}
		@Override
		public int getWorked() {
			return 0;
		}
		@Override
		public IToolRunnable getRunnable() {
			return null;
		}
	};
	
	
	private static final int SCHEDULE_ON_EVENT= 50;
	private static final int SCHEDULE_DEFAULT= 150;
	
	
	private class RefreshJob extends WorkbenchJob {
		
		RefreshJob() {
			super("ToolProgress Refresh"); //$NON-NLS-1$
			setSystem(true);
		}
		
		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			internalRefresh();
			if (ToolProgressGroup.this.toolInfo.scheduleRefresh) {
				schedule(SCHEDULE_DEFAULT);
			}
			
			return Status.OK_STATUS;
		}
	}
	
	private class DebugEventListener implements IDebugEventSetListener {
		
		@Override
		public void handleDebugEvents(final DebugEvent[] events) {
			final ToolInfo tool= ToolProgressGroup.this.toolInfo;
			for (final DebugEvent event : events) {
				if (event.getSource() == tool.process.getQueue()) {
					if (Queue.isStateChange(event)) {
						final StateDelta delta= ((Queue.StateDelta) event.getData());
						tool.scheduleRefresh= (delta.newState == Queue.PROCESSING_STATE);
						ToolProgressGroup.this.refreshJob.schedule(SCHEDULE_ON_EVENT);
					}
				}
			}
		}
		
	}
	
	private static class ToolInfo {
		
		final ToolProcess process;
		
		Image imageCache;
		
		boolean scheduleRefresh= false;
		
		ToolInfo(final ToolProcess process) {
			this.process= process;
			if (process != null) {
				this.scheduleRefresh= process.getToolStatus().isRunning();
			}
			else {
				this.scheduleRefresh= false;
			}
		}
		
	}
	
	
	private final DebugEventListener debugEventListener;
	private final Job refreshJob;
	
	private Composite composite;
	private Label imageLabel;
	private ShortedLabel mainLabel;
	private ProgressBar progressBar;
	private ShortedLabel subLabel;
	
	private ToolInfo toolInfo= new ToolInfo(null);
	
	
	/**
	 * Creates a new group
	 * 
	 * @param parent the parent composite
	 */
	public ToolProgressGroup(final Composite parent) {
		this.debugEventListener= new DebugEventListener();
		this.refreshJob= new RefreshJob();
		
		createControls(parent);
		
		final DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.addDebugEventListener(this.debugEventListener);
		}
	}
	
	
	private void createControls(final Composite parent) {
		this.composite= new Composite(parent, SWT.NONE);
		this.composite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				ToolProgressGroup.this.dispose();
			}
		});
		final GridLayout layout= new GridLayout(2, false);
		layout.marginHeight= 0;
		layout.marginWidth= 2;
		layout.verticalSpacing= 2;
		this.composite.setLayout(layout);
		
		this.imageLabel= new Label(this.composite, SWT.NONE);
		GridData gd= new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.verticalSpan= 2;
		gd.verticalIndent= 2;
		gd.widthHint= 16;
		gd.heightHint= 16;
		this.imageLabel.setLayoutData(gd);
		
		this.mainLabel= new ShortedLabel(this.composite, SWT.NONE);
		this.mainLabel.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		this.subLabel= new ShortedLabel(this.composite, SWT.NONE);
		gd= new GridData(SWT.FILL, SWT.CENTER, true, false);
		this.subLabel.getControl().setLayoutData(gd);
		
		this.progressBar= new ProgressBar(this.composite, SWT.HORIZONTAL);
		this.progressBar.setMinimum(0);
		this.progressBar.setMaximum(IToolRunnable.TOTAL_WORK);
		gd= new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan= 2;
		this.progressBar.setLayoutData(gd);
	}
	
	public Control getControl() {
		return this.composite;
	}
	
	
	public void setTool(final ToolProcess tool, final boolean directRefresh) {
		this.toolInfo= new ToolInfo(tool);
		refresh(directRefresh);
	}
	
	/**
	 * Refreshes the components.
	 * 
	 * If <code>directRefresh</code> is requested, you have to be in UI thread.
	 * 
	 * @param directRefresh refresh is directly executed instead of scheduled.
	 */
	public void refresh(final boolean directRefresh) {
		if (directRefresh) {
			this.refreshJob.cancel();
			internalRefresh();
			this.refreshJob.schedule(SCHEDULE_DEFAULT);
		}
		else {
			this.refreshJob.schedule(SCHEDULE_ON_EVENT);
		}
	}
	
	private void internalRefresh() {
		if (!UIAccess.isOkToUse(this.composite)) {
			return;
		}
		final ToolInfo tool= this.toolInfo;
		final ToolController controller= (tool.process != null) ? tool.process.getController() : null;
		final IProgressInfo info= (controller != null) ? controller.getProgressInfo() : DUMMY_INFO;
		Image image= null;
		final IToolRunnable runnable= info.getRunnable();
		if (runnable != null) {
			image= NicoUITools.getImage(runnable);
		}
		if (image == null && tool.process != null) {
			image= getToolImage(tool);
		}
		if (image == null) {
			image= NicoUIPlugin.getDefault().getImageRegistry().get(NicoUI.OBJ_TASK_DUMMY_IMAGE_ID);
		}
		if (!(image.equals(this.imageLabel.getImage()))) {
			this.imageLabel.setImage(image);
		}
		this.mainLabel.setText(info.getLabel());
		this.subLabel.setText(info.getSubLabel());
		this.progressBar.setSelection(info.getWorked());
	}
	
	private Image getToolImage(final ToolInfo tool) {
		if (tool.imageCache == null) {
			tool.imageCache= NicoUITools.getImage(tool.process);
		}
		return tool.imageCache;
	}
	
	private void dispose() {
		this.toolInfo= new ToolInfo(null);
		this.refreshJob.cancel();
		final DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.removeDebugEventListener(this.debugEventListener);
		}
	}
	
}
