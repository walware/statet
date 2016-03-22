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

package de.walware.statet.nico.core.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ibm.icu.text.DateFormat;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.debug.core.model.AbstractProcess;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;
import de.walware.statet.nico.core.runtime.ToolWorkspace.Listener;


/**
 * Provides <code>IProcess</code> for a <code>ToolController</code>.
 */
public class ToolProcess extends AbstractProcess implements IProcess, ITool, IToolStatusListener {
	
	public static final String PROCESS_TYPE_SUFFIX = ".nico"; //$NON-NLS-1$
	
	
	public static final int EXITCODE_DISCONNECTED = 101;
	
	
	private final String fMainType;
	private final Set<String> fFeatureSets = new HashSet<>();
	private final String fToolLabelShort;
	private final String fToolLabelLong;
	private String fToolLabelTrimmedWD;
	private String fToolLabelStatus;
	
	private final String fAddress;
	private final long fConnectionTimestamp;
	private long fStartupTimestamp;
	private String fStartupWD;
	Map<String, Object> fInitData;
	
	private ToolController fController;
	private final Queue fQueue;
	private final History fHistory;
	private ToolWorkspace fWorkspaceData;
	
	private final Object fDisposeLock = new Object();
	private int fRetain;
	private boolean fIsDisposed;
	
	private final boolean fCaptureOutput;
	
	private volatile ToolStatus fStatus = ToolStatus.STARTING;
	
	private ImList<? extends ITrack> fTracks= ImCollections.emptyList();
	
	
	public ToolProcess(final ILaunch launch, final String mainType,
			final String labelPrefix, final String name,
			final String address, final String wd, final long timestamp) {
		super(launch, name);
		fMainType = mainType;
		fAddress = address;
		fStartupWD = wd;
		fStartupTimestamp = timestamp;
		fConnectionTimestamp = timestamp;
		
		fToolLabelShort = labelPrefix;
		fToolLabelLong = labelPrefix + ' ' + name;
		fToolLabelStatus = ToolStatus.STARTING.getMarkedLabel();
		fToolLabelTrimmedWD = trimPath(wd);
		
//		final String captureOutput = launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT);
//		fCaptureOutput = !("false".equals(captureOutput)
//				&& !captureLogOnly(launch.getLaunchConfiguration())); //$NON-NLS-1$
		fCaptureOutput = false;
		
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new ILaunchesListener() {
			@Override
			public void launchesAdded(final ILaunch[] launches) {
			}
			@Override
			public void launchesChanged(final ILaunch[] launches) {
			}
			@Override
			public void launchesRemoved(final ILaunch[] launches) {
				for (final ILaunch launch : launches) {
					if (getLaunch() == launch) {
						final DebugPlugin plugin = DebugPlugin.getDefault();
						if (plugin != null) {
							plugin.getLaunchManager().removeLaunchListener(this);
							dispose();
						}
					}
				}
			}
		});
		
		fQueue = new Queue(this);
		fHistory = new History(this);
	}
	
	public void init(final ToolController controller) {
		fController = controller;
		fWorkspaceData = fController.getWorkspaceData();
		fWorkspaceData.addPropertyListener(new Listener() {
			@Override
			public void propertyChanged(final ToolWorkspace workspace, final Map<String, Object> properties) {
				final Map<String, String> attributes= getAttributes(true);
				final DebugEvent nameEvent;
				synchronized (attributes) {
					fToolLabelTrimmedWD = null;
					nameEvent = doSet(attributes, IProcess.ATTR_PROCESS_LABEL, computeConsoleLabel());
				}
				if (nameEvent != null) {
					fireEvent(nameEvent);
				}
			}
		});
		
		fToolLabelTrimmedWD = null;
		final Map<String, String> attributes= getAttributes(true);
		doSet(attributes, IProcess.ATTR_PROCESS_LABEL, computeConsoleLabel());
		doSet(attributes, IProcess.ATTR_PROCESS_TYPE, (fMainType+PROCESS_TYPE_SUFFIX).intern());
		
		fHistory.init();
		
		created();
	}
	
	
	public void registerFeatureSet(final String featureSetID) {
		fFeatureSets.add(featureSetID);
	}
	
	@Override
	public boolean isProvidingFeatureSet(final String featureSetID) {
		return fFeatureSets.contains(featureSetID);
	}
	
	private String computeConsoleLabel() {
		String wd = fToolLabelTrimmedWD;
		if (wd == null) {
			wd = fToolLabelTrimmedWD = trimPath(FileUtil.toString(fWorkspaceData.getWorkspaceDir()));
		}
		return fToolLabelShort + ' ' + getLabel() + "  ∙  " + wd + "   \t " + fToolLabelStatus; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private String trimPath(final String path) {
		if (path == null) {
			return "–"; //$NON-NLS-1$
		}
		if (path.length() < 30) {
			return path;
		}
		final int post1 = prevPathSeperator(path, path.length() - 1);
		if (post1 < 25) {
			return path;
		}
		final int post2 = prevPathSeperator(path, post1 - 1);
		if (post2 < 20) {
			return path;
		}
		final int pre1 = nextPathSeperator(path, 0);
		int pre2 = nextPathSeperator(path, pre1 + 1);
		if (pre2 > 12) {
			pre2 = 10;
		}
		else {
			pre2++;
		}
		if (post2 - pre2 < 10) {
			return path;
		}
		return path.substring(0, pre2) + " ... " + path.substring(post2, path.length()); //$NON-NLS-1$
	}
	
	private int prevPathSeperator(final String path, final int offset) {
		final int idx1 = path.lastIndexOf('/', offset);
		final int idx2 = path.lastIndexOf('\\', offset);
		return (idx1 > idx2) ?
				idx1 : idx2;
	}
	
	private int nextPathSeperator(final String path, final int offset) {
		final int idx1 = path.indexOf('/', offset);
		final int idx2 = path.indexOf('\\', offset);
		return (idx1 >= 0 && idx1 < idx2) ?
				idx1 : idx2;
	}
	
	@Override
	public final String getMainType() {
		return fMainType;
	}
	
	/**
	 * Returns the name of the process, usually with a timestamp.
	 * <p>
	 * For example: <code>R-2.11.1 / RJ (29.11.2010 12:56:37)</code>
	 * 
	 * @return
	 */
//	@Override
//	public String getLabel() {
//		return super.getLabel();
//	}
	
	/**
	 * Returns a label of the tool, usually based on the launch configuration.
	 * <p>
	 * {@link ITool#DEFAULT_LABEL DEFAULT_LABEL}: &lt;name of launch config&gt; [&lt;name of launch type&gt;]<br/>
	 * For example: <code>RJ-2.11 [R Console]</code>
	 * </p><p>
	 * {@link ITool#LONG_LABEL LONG_LABEL}: &lt;short label&gt; &lt;name&gt;<br/>
	 * For example: <code>RJ-2.11 [R Console] R-2.11.1 / RJ (29.11.2010 12:49:51)</code>
	 * </p>
	 * 
	 * @param config allows to configure the information to include in the label
	 * @return the label
	 */
	@Override
	public String getLabel(final int config) {
		if ((config & LONG_LABEL) != 0) {
			return fToolLabelLong;
		}
		return fToolLabelShort;
	}
	
	public ToolController getController() {
		return fController;
	}
	
	public History getHistory() {
		return fHistory;
	}
	
	@Override
	public Queue getQueue() {
		return fQueue;
	}
	
	@Override
	public IStreamsProxy getStreamsProxy() {
		return (fCaptureOutput && fController != null) ? fController.getStreams() : null;
	}
	
	
	public ToolWorkspace getWorkspaceData() {
		return fWorkspaceData;
	}
	
	public ToolStatus getToolStatus() {
		return fStatus;
	}
	
	
	void setExitValue(final int value) {
		doSetExitValue(value);
	}
	
	@Override
	public boolean canTerminate() {
		return (!isTerminated());
	}
	
	@Override
	public void terminate() throws DebugException {
		final ToolController controller = fController;
		if (controller != null) {
			controller.scheduleQuit();
		}
	}
	
	@Override
	public boolean isTerminated() {
		return fStatus == ToolStatus.TERMINATED;
	}
	
	
	/** Called by Controller */
	@Override
	public void controllerStatusChanged(final ToolStatus oldStatus, final ToolStatus newStatus,
			final List<DebugEvent> eventCollection) {
		fStatus = newStatus;
		
		if (newStatus == ToolStatus.TERMINATED) {
			fController = null;
			eventCollection.add(new DebugEvent(ToolProcess.this, DebugEvent.TERMINATE));
		}
		
		final Map<String, String> attributes= getAttributes(true);
		final DebugEvent nameEvent;
		synchronized (attributes) {
			fToolLabelStatus = newStatus.getMarkedLabel();
			nameEvent = doSet(attributes, IProcess.ATTR_PROCESS_LABEL, computeConsoleLabel());
		}
		if (nameEvent != null) {
			eventCollection.add(nameEvent);
		}
	}
	
//	public void controllerBusyChanged(final boolean isBusy, final List<DebugEvent> eventCollection) {
//		eventCollection.add(new DebugEvent(this, DebugEvent.MODEL_SPECIFIC, 
//				isBusy ? (ToolProcess.BUSY | 0x1) : (ToolProcess.BUSY | 0x0)));
//	}
//	
	private final void dispose() {
		synchronized (fDisposeLock) {
			fIsDisposed = true;
			if (fRetain > 0) {
				return;
			}
		}
		doDispose();
	}
	
	public void prepareRestart(final Map<String, Object> data) {
		if (fStatus != ToolStatus.TERMINATED) {
			throw new IllegalStateException();
		}
		if (data == null) {
			throw new NullPointerException();
		}
		data.put("process", this);
		data.put("processDispose", poseponeDispose());
		data.put("address", fAddress);
		data.put("initData", fInitData);
	}
	
	public void restartCompleted(final Map<String, Object> data) {
		if (data == null) {
			throw new NullPointerException();
		}
		if (data.get("process") != this) {
			throw new IllegalArgumentException();
		}
		approveDispose(data.get("processDispose"));
	}
	
	/**
	 * Prevents to dispose the resources so you can still access the tool
	 * and its queue.
	 * It is important to call #approveDispose later to release the resources.
	 * 
	 * @return ticket to approve the disposal
	 */
	private final Object poseponeDispose() {
		synchronized (fDisposeLock) {
			if (fRetain <= 0 && fIsDisposed) {
				return null;
			}
			fRetain++;
		}
		return new AtomicBoolean(true);
	}
	
	/**
	 * @see #poseponeDispone
	 */
	private final void approveDispose(final Object ticket) {
		if (ticket instanceof AtomicBoolean && ((AtomicBoolean) ticket).getAndSet(false)) {
			synchronized (fDisposeLock) {
				fRetain--;
				if (fRetain > 0 || !fIsDisposed) {
					return;
				}
			}
			doDispose();
		}
	}
	
	protected void doDispose() {
		if (fQueue != null) {
			fQueue.dispose();
		}
		if (fHistory != null) {
			fHistory.dispose();
		}
	}
	
	
	void setTracks(final List<? extends ITrack> tracks) {
		fTracks= ImCollections.toList(tracks);
	}
	
	public ImList<? extends ITrack> getTracks() {
		return fTracks;
	}
	
	void setStartupTimestamp(final long timestamp) {
		fStartupTimestamp = timestamp;
	}
	
	public long getStartupTimestamp() {
		return fStartupTimestamp;
	}
	
	public long getConnectionTimestamp() {
		return fConnectionTimestamp;
	}
	
	public String createTimestampComment(final long timestamp) {
		return DateFormat.getDateTimeInstance().format(timestamp) + '\n';
	}
	
	void setStartupWD(final String wd) {
		fStartupWD = wd;
	}
	
	public String getStartupWD() {
		return fStartupWD;
	}
	
	
	@Override
	public String toString() {
		return fToolLabelLong;
	}
	
}
