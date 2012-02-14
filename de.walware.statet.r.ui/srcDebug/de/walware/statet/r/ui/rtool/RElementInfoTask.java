/*******************************************************************************
 * Copyright (c) 2011-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.rtool;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Control;

import de.walware.ecommons.text.TextUtil;
import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.ui.NicoUITools;

import de.walware.rj.data.RLanguage;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RReference;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.RLanguageImpl;
import de.walware.rj.data.defaultImpl.RListImpl;
import de.walware.rj.data.defaultImpl.RReferenceImpl;
import de.walware.rj.services.RService;

import de.walware.statet.r.console.core.AbstractRDataRunnable;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.console.core.RTool;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.nico.impl.RjsController;


public class RElementInfoTask extends AbstractRDataRunnable {
	
	
	public static class RElementInfoData {
		
		
		private Control fControl;
		
		private ICombinedRElement fElement;
		private RElementName fElementName;
		private RList fElementAttr;
		private String fDetailTitle;
		private String fDetailInfo;
		
		
		private RElementInfoData() {
		}
		
		
		public Control getControl() {
			return fControl;
		}
		
		public ICombinedRElement getElement() {
			return fElement;
		}
		
		public RElementName getElementName() {
			return fElementName;
		}
		
		public RList getElementAttr() {
			return fElementAttr;
		}
		
		public boolean hasDetail() {
			return (fDetailTitle != null);
		}
		
		public String getDetailTitle() {
			return fDetailTitle;
		}
		
		public String getDetailInfo() {
			return fDetailInfo;
		}
		
	}
	
	
	private final RElementName fElementName;
	private int fStatus;
	
	private RElementInfoData fData;
	
	
	public RElementInfoTask(final RElementName name) {
		super("reditor/hover", "Collecting Element Detail for Hover"); //$NON-NLS-1$
		fElementName = name;
	}
	
	
	public RElementInfoData load(final ITool tool, final Control control) {
		if (!NicoUITools.isToolReady(RTool.TYPE, RTool.R_DATA_FEATURESET_ID, tool)) {
			return null;
		}
		try {
			synchronized (this) {
				final IStatus status = tool.getQueue().addHot(this);
				if (status.getSeverity() >= IStatus.ERROR) {
					return null;
				}
				while (fStatus == 0) {
					if (Thread.interrupted()) {
						fStatus = -1;
					}
					wait(200);
				}
			}
		}
		catch (final InterruptedException e) {
			fStatus = -1;
		}
		if (fStatus != 1) {
			tool.getQueue().removeHot(this);
			return null;
		}
		
		final RElementInfoData data = fData;
		if (data != null && data.fElement != null) {
			data.fControl = control;
			return data;
		}
		return null;
	}
	
	
	public SubmitType getSubmitType() {
		return SubmitType.OTHER;
	}
	
	@Override
	public boolean changed(final int event, final ITool tool) {
		switch (event) {
		case MOVING_FROM:
			return false;
		case REMOVING_FROM:
		case BEING_ABANDONED:
		case FINISHING_CANCEL:
		case FINISHING_ERROR:
			fStatus = -1;
			synchronized (this) {
				this.notifyAll();
			}
			break;
		case FINISHING_OK:
			fStatus = 1;
			synchronized (this) {
				this.notifyAll();
			}
			break;
		default:
			break;
		}
		return true;
	}
	
	@Override
	protected void run(final IRDataAdapter r,
			final IProgressMonitor monitor) throws CoreException {
		if (fStatus != 0 || monitor.isCanceled()) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if (!(r instanceof RjsController)) {
			return; // TODO
		}
		
		fData = new RElementInfoData();
		final ICombinedREnvironment environment = getEnv(r, monitor);
		if (environment == null) {
			return;
		}
		final String name = RElementName.createDisplayName(fElementName, RElementName.DISPLAY_EXACT);
		final RReference envir = new RReferenceImpl(environment.getHandle(), RObject.TYPE_ENV, null);
		if (fData.fElement == null) {
			final String cmd = name;
			fData.fElement = ((RjsController) r).evalCombinedStruct(cmd, envir,
					0, RService.DEPTH_ONE, fElementName, monitor );
			if (fData.fElement == null) {
				return;
			}
		}
		
		if (fData.fElement.getRObjectType() != RObject.TYPE_PROMISE
				&& fData.fElement.getRObjectType() != RObject.TYPE_MISSING) {
			final String cmd = "class("+name+")";
			
			final RObject robject = ((RjsController) r).evalData(cmd, envir,
					null, 0, RService.DEPTH_INFINITE, monitor );
			if (robject != null) {
				fData.fElementAttr = new RListImpl(new RObject[] { robject }, null, new String[] { "class" });
			}
		}
		if (fData.fElement.getRObjectType() != RObject.TYPE_MISSING) {
			final String title = "str";
			final StringBuilder cmd = new StringBuilder("rj::.statet.captureStr(");
			if (fData.fElement.getRObjectType() == RObject.TYPE_PROMISE) {
				cmd.append("substitute(");
				cmd.append(name);
				cmd.append(")");
			}
			else {
				cmd.append(name);
			}
			cmd.append(")");
			
			final RObject robject = ((RjsController) r).evalData(cmd.toString(), envir,
					null, 0, RService.DEPTH_INFINITE, monitor );
			
			if (robject != null && robject.getRObjectType() == RObject.TYPE_VECTOR
					&& robject.getData().getStoreType() == RStore.CHARACTER ) {
				final RStore data = robject.getData();
				final StringBuilder sb = new StringBuilder(data.getLength()*30);
				final String ln = TextUtil.getPlatformLineDelimiter();
				for (int i = 0; i < data.getLength(); i++) {
					if (!data.isNA(i)) {
						sb.append(data.getChar(i));
						sb.append(ln);
					}
				}
				if (sb.length() > 0) {
					sb.setLength(sb.length()-ln.length());
				}
				fData.fDetailTitle = title;
				fData.fDetailInfo = sb.toString();
			}
		}
	}
	
	private ICombinedREnvironment getEnv(final IRDataAdapter r,
			final IProgressMonitor monitor) throws CoreException {
		boolean inherits;
		RElementName envName;
		if (fElementName.getNamespace() != null) {
			inherits = false;
			envName = fElementName.getNamespace();
		}
		else {
			inherits = true;
			final int position = getFramePosition(r, monitor);
			if (position > 0) {
				envName = RElementName.create(RElementName.MAIN_SYSFRAME, Integer.toString(position));
			}
			else {
				envName = RElementName.create(RElementName.MAIN_SEARCH_ENV, ".GlobalEnv");
			}
		}
		if (envName == null) {
			return null;
		}
		final String name = envName.getDisplayName(RElementName.DISPLAY_NS_PREFIX);
		if (name == null) {
			return null;
		}
		final RElementName mainName = RElementName.cloneSegment(fElementName);
		if (mainName.getType() != RElementName.MAIN_DEFAULT) {
			return null;
		}
		
		final int depth = (fElementName.getNextSegment() != null) ?
				RService.DEPTH_REFERENCE : RService.DEPTH_ONE;
		final RObject[] data = ((RjsController) r).findData(mainName.getSegmentName(),
				new RLanguageImpl(RLanguage.CALL, name, null), inherits,
				"combined", RObjectFactory.F_ONLY_STRUCT, depth, monitor );
		if (data == null) {
			return null;
		}
		final ICombinedREnvironment foundEnv = (ICombinedREnvironment) data[1];
		if (!updateName(foundEnv.getElementName())) {
			final ICombinedRElement altElement = r.getWorkspaceData().resolve(new RReferenceImpl(
					foundEnv.getHandle(), RObject.TYPE_ENV, null ), false );
			if (!(altElement instanceof ICombinedREnvironment)
					|| !updateName(altElement.getElementName()) ) {
				fData.fElementName = fElementName;
			}
		}
		if (depth == RService.DEPTH_ONE) {
			fData.fElement = (ICombinedRElement) data[0];
			return (ICombinedREnvironment) data[1];
		}
		else {
			if (!(data[0] instanceof RReference) 
					|| ((RReference) data[0]).getReferencedRObjectType() == RObject.TYPE_PROMISE
					|| ((RReference) data[0]).getReferencedRObjectType() == RObject.TYPE_MISSING) {
				return null;
			}
			return (ICombinedREnvironment) data[1];
		}
	}
	
	private boolean updateName(final RElementName envName) {
		if (envName == null || envName.getSegmentName() == null
				|| envName.getSegmentName().isEmpty()) {
			return false;
		}
		final List<RElementName> segments = new ArrayList<RElementName>();
		segments.add(envName);
		RElementName a = fElementName;
		while (a != null) {
			segments.add(a);
			a = a.getNextSegment();
		}
		final RElementName name = RElementName.concat(segments);
		if (name.getNamespace() == null) {
			return false;
		}
		
		fData.fElementName = name;
		return true;
	}
	
	protected int getFramePosition(final IRDataAdapter r,
			final IProgressMonitor monitor) throws CoreException {
		return 0;
	}
	
}
