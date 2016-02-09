/*=============================================================================#
 # Copyright (c) 2011-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.rtool;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Control;

import de.walware.ecommons.text.TextUtil;
import de.walware.ecommons.ts.ISystemReadRunnable;
import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.ui.NicoUITools;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RLanguage;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RReference;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.data.defaultImpl.RLanguageImpl;
import de.walware.rj.data.defaultImpl.RListImpl;
import de.walware.rj.data.defaultImpl.RReferenceImpl;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RService;

import de.walware.statet.r.console.core.AbstractRDataRunnable;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.console.core.util.LoadReferenceRunnable;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.nico.impl.RjsController;


public class RElementInfoTask extends AbstractRDataRunnable implements ISystemReadRunnable {
	
	
	public static class RElementInfoData {
		
		
		private Control control;
		
		private ICombinedRElement element;
		private RElementName elementName;
		private boolean isActiveBinding;
		private RList elementAttr;
		private String detailTitle;
		private String detailInfo;
		
		
		private RElementInfoData() {
		}
		
		
		public Control getControl() {
			return this.control;
		}
		
		public ICombinedRElement getElement() {
			return this.element;
		}
		
		public RElementName getElementName() {
			return this.elementName;
		}
		
		public boolean isElementOfActiveBinding() {
			return this.isActiveBinding;
		}
		
		public RList getElementAttr() {
			return this.elementAttr;
		}
		
		public boolean hasDetail() {
			return (this.detailTitle != null);
		}
		
		public String getDetailTitle() {
			return this.detailTitle;
		}
		
		public String getDetailInfo() {
			return this.detailInfo;
		}
		
	}
	
	
	private final RElementName elementName;
	
	private int status;
	
	private RElementInfoData data;
	
	
	public RElementInfoTask(final RElementName name) {
		super("reditor/hover", "Collecting Element Detail for Hover"); //$NON-NLS-1$
		
		this.elementName= RElementName.normalize(name);
	}
	
	
	public boolean preCheck() {
		if (this.elementName == null) {
			return false;
		}
		if (this.elementName.getType() != RElementName.MAIN_DEFAULT) {
			return false;
		}
		return true;
	}
	
	public RElementInfoData load(final ITool tool, final Control control) {
		if (!NicoUITools.isToolReady(RConsoleTool.TYPE, RConsoleTool.R_DATA_FEATURESET_ID, tool)) {
			return null;
		}
		try {
			synchronized (this) {
				final IStatus status= tool.getQueue().addHot(this);
				if (status.getSeverity() >= IStatus.ERROR) {
					return null;
				}
				while (this.status == 0) {
					if (Thread.interrupted()) {
						this.status= -1;
					}
					wait(200);
				}
			}
		}
		catch (final InterruptedException e) {
			this.status= -1;
		}
		if (this.status != 1) {
			tool.getQueue().removeHot(this);
			return null;
		}
		
		final RElementInfoData data= this.data;
		if (data != null && data.element != null) {
			data.control= control;
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
			this.status= -1;
			synchronized (this) {
				notifyAll();
			}
			break;
		case FINISHING_OK:
			this.status= 1;
			synchronized (this) {
				notifyAll();
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
		if (this.status != 0 || monitor.isCanceled()) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if (!(r instanceof RjsController) || !preCheck()) {
			return; // TODO
		}
		
		this.data= new RElementInfoData();
		final ICombinedREnvironment environment= getEnv(r, monitor);
		
		final String name;
		final RReference envir;
		if (environment != null) {
			name= RElementName.createDisplayName(this.elementName, RElementName.DISPLAY_EXACT);
			envir= new RReferenceImpl(environment.getHandle(), RObject.TYPE_ENV, null);
		}
		else if (this.data.elementName != null && this.data.elementName.getScope() != null
				&& this.data.elementName.getScope().getType() == RElementName.SCOPE_NS) {
			name= RElementName.createDisplayName(this.elementName, RElementName.DISPLAY_EXACT | RElementName.DISPLAY_FQN);
			envir= null;
		}
		else {
			return;
		}
		
		if (this.data.element == null || this.data.element.getRObjectType() == RObject.TYPE_MISSING) {
			try {
				this.data.element= ((RjsController) r).evalCombinedStruct(name, envir,
						0, RService.DEPTH_ONE, this.elementName, monitor );
				if (this.data.element == null) {
					return;
				}
			}
			catch (final CoreException e) {
				if (this.data.element == null) {
					throw e;
				}
				// RObject.TYPE_MISSING
				final String message= e.getMessage();
				final int idxBegin= message.indexOf('<');
				final int idxEnd= message.lastIndexOf('>');
				if (idxBegin >= 0 && idxEnd > idxBegin + 1) {
					this.data.detailTitle= "error";
					this.data.detailInfo= message.substring(idxBegin + 1, idxEnd);
				}
			}
		}
		
		if (envir != null) {
			try {
				final FunctionCall fcall= r.createFunctionCall("base::bindingIsActive"); //$NON-NLS-1$
				fcall.addChar("sym", this.elementName.getSegmentName()); //$NON-NLS-1$
				fcall.add("env", envir); //$NON-NLS-1$
				final RObject data= fcall.evalData(monitor);
				this.data.isActiveBinding= RDataUtil.checkSingleLogiValue(data);
			}
			catch (final CoreException | UnexpectedRDataException e) {
			}
		}
		
		if (this.data.element.getRObjectType() != RObject.TYPE_PROMISE
				&& this.data.element.getRObjectType() != RObject.TYPE_MISSING) {
			final String cmd= "class("+name+")";
			
			final RObject robject= ((RjsController) r).evalData(cmd, envir,
					null, 0, RService.DEPTH_INFINITE, monitor );
			if (robject != null) {
				this.data.elementAttr= new RListImpl(new RObject[] { robject }, null, new String[] { "class" });
			}
		}
		if (this.data.element.getRObjectType() != RObject.TYPE_MISSING) {
			final String title= "str";
			final StringBuilder cmd= new StringBuilder("rj::.statet.captureStr(");
			if (this.data.element.getRObjectType() == RObject.TYPE_PROMISE) {
				cmd.append("substitute(");
				cmd.append(name);
				cmd.append(")");
			}
			else {
				cmd.append(name);
			}
			cmd.append(")");
			
			final RObject robject= ((RjsController) r).evalData(cmd.toString(), envir,
					null, 0, RService.DEPTH_INFINITE, monitor );
			
			try {
				final RCharacterStore data= RDataUtil.checkRCharVector(robject).getData();
				final StringBuilder sb= new StringBuilder((int) data.getLength()*30);
				final String ln= TextUtil.getPlatformLineDelimiter();
				for (int i= 0; i < data.getLength(); i++) {
					if (!data.isNA(i)) {
						sb.append(data.getChar(i));
						sb.append(ln);
					}
				}
				if (sb.length() > 0) {
					sb.setLength(sb.length()-ln.length());
				}
				this.data.detailTitle= title;
				this.data.detailInfo= sb.toString();
			}
			catch (final UnexpectedRDataException e) {}
		}
	}
	
	private ICombinedREnvironment getEnv(final IRDataAdapter r,
			final IProgressMonitor monitor) throws CoreException {
		RElementName envName= this.elementName.getScope();
		boolean inherits= false;
		if (envName == null) {
			final int position= getFramePosition(r, monitor);
			if (position > 0) {
				envName= RElementName.create(RElementName.SCOPE_SYSFRAME, Integer.toString(position));
			}
			else {
				envName= RElementName.create(RElementName.SCOPE_SEARCH_ENV, ".GlobalEnv");
			}
			inherits= true;
		}
		
		if (envName == null
				|| !LoadReferenceRunnable.isAccessAllowed(envName, r.getWorkspaceData()) ) {
			return null;
		}
		
		if (envName.getType() == RElementName.SCOPE_NS) {
			this.data.elementName= this.elementName;
			return null;
		}
		if (envName.getType() == RElementName.SCOPE_NS_INT) {
			final ICombinedRElement element= r.getWorkspaceData().resolve(envName,
					RWorkspace.RESOLVE_UPTODATE, 0, monitor );
			if (element instanceof ICombinedREnvironment) {
				final ICombinedREnvironment env= (ICombinedREnvironment) element;
				if (env.getNames() != null && env.getNames().contains(this.elementName.getSegmentName())) {
					this.data.elementName= this.elementName;
					if (this.elementName.getNextSegment() == null) {
						this.data.element= env.get(this.elementName.getSegmentName());
					}
					return env;
				}
			}
			return null;
		}
		
		final String name= envName.getDisplayName(RElementName.DISPLAY_FQN);
		if (name == null) {
			return null;
		}
		
		final RElementName mainName= RElementName.cloneSegment(this.elementName);
		
		final int depth= (this.elementName.getNextSegment() != null) ?
				RService.DEPTH_REFERENCE : RService.DEPTH_ONE;
		final RObject[] data= ((RjsController) r).findData(mainName.getSegmentName(),
				new RLanguageImpl(RLanguage.CALL, name, null), inherits,
				"combined", RObjectFactory.F_ONLY_STRUCT, depth, monitor );
		if (data == null) {
			return null;
		}
		final ICombinedREnvironment foundEnv= (ICombinedREnvironment) data[1];
		if (!updateName(foundEnv.getElementName())) {
			final ICombinedRElement altElement= r.getWorkspaceData().resolve(new RReferenceImpl(
					foundEnv.getHandle(), RObject.TYPE_ENV, null ), 0 );
			if (!(altElement instanceof ICombinedREnvironment)
					|| !updateName(altElement.getElementName()) ) {
				this.data.elementName= this.elementName;
			}
		}
		if (depth == RService.DEPTH_ONE) {
			this.data.element= (ICombinedRElement) data[0];
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
		final List<RElementName> segments= new ArrayList<>();
		segments.add(envName);
		RElementName a= this.elementName;
		while (a != null) {
			segments.add(a);
			a= a.getNextSegment();
		}
		final RElementName name= RElementName.create(segments);
		if (name.getScope() == null) {
			return false;
		}
		
		this.data.elementName= name;
		return true;
	}
	
	protected int getFramePosition(final IRDataAdapter r,
			final IProgressMonitor monitor) throws CoreException {
		return 0;
	}
	
}
