/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.dataeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ts.util.ToolCommandHandlerUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.eclient.AbstractRToolCommandHandler;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.RService;

import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;


public class ShowElementCommandHandler extends AbstractRToolCommandHandler {
	
	
	private static class CheckRunnable implements ISystemRunnable {
		
		
		private final ToolProcess tool;
		
		private final RElementName elementName;
		
		
		public CheckRunnable(final ToolProcess tool, final RElementName elementName) {
			this.tool= tool;
			this.elementName= elementName;
		}
		
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == this.tool);
		}
		
		@Override
		public String getTypeId() {
			return "r/dataeditor/open"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return "Show R Element";
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case MOVING_FROM:
			case MOVING_TO:
				return false;
			default:
				return true;
			}
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			final RObject[] data= ((IRDataAdapter) service).findData(this.elementName.getSegmentName(),
					null, true, "combined", RObjectFactory.F_ONLY_STRUCT, RService.DEPTH_REFERENCE,
					monitor );
			if (data == null) {
				return;
			}
			final ICombinedRElement foundEnv= (ICombinedRElement) data[1];
			final List<RElementName> segments= new ArrayList<>();
			segments.add(foundEnv.getElementName());
			RElementName.addSegments(segments, this.elementName);
			doOpen(this.tool, RElementName.create(segments));
		}
		
	}
	
	private static void doOpen(final ToolProcess tool, final RElementName name) {
		UIAccess.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final IWorkbenchPage page= UIAccess.getActiveWorkbenchPage(true);
				if (page == null) {
					return;
				}
				RDataEditor.open(page, tool, name, null);
			}
		});
	}
	
	
	public static final String SHOW_ELEMENT_COMMAND_ID= "showElement"; //$NON-NLS-1$
	
	
	public ShowElementCommandHandler() {
	}
	
	
	@Override
	protected IStatus execute(final String id, final IRToolService r, final Map<String, Object> data,
			final IProgressMonitor monitor) throws CoreException {
		if (id.equals(SHOW_ELEMENT_COMMAND_ID)) {
			final ToolProcess tool= (ToolProcess) r.getTool();
			final String elementName= ToolCommandHandlerUtil.getCheckedData(data, "elementName",
					String.class, true); 
			final RElementName rElementName= RElementName.parseDefault(elementName);
			if (rElementName != null) {
				if (rElementName.getScope() != null) {
					doOpen(tool, rElementName);
				}
				else {
					tool.getQueue().addHot(new CheckRunnable(tool, rElementName));
				}
			}
			return Status.OK_STATUS;
		}
		return null;
	}
	
}
