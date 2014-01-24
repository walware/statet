/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.tool;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ts.ITool;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.eclient.AbstractRToolRunnable;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RGraphicCreator;
import de.walware.rj.services.RPlatform;

import de.walware.statet.r.core.RCore;


public class AbstractStatetRRunnable extends AbstractRToolRunnable {
	
	
	protected static void checkNewCommand(final IRConsoleService r,
			final IProgressMonitor monitor) throws CoreException {
		if (!r.acceptNewConsoleCommand()) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 0,
					"Operation cancelled because another command is already started" +
					"in the console.", null ));
		}
	}
	
	private static class RToolServiceWrapper implements IRConsoleService {
		
		
		private final IRToolService fService;
		
		
		public RToolServiceWrapper(final IRToolService service) {
			fService = service;
		}
		
		
		@Override
		public ITool getTool() {
			return fService.getTool();
		}
		
		@Override
		public RPlatform getPlatform() {
			return fService.getPlatform();
		}
		
		@Override
		public void evalVoid(final String expression,
				final IProgressMonitor monitor) throws CoreException {
			fService.evalVoid(expression, monitor);
		}
		
		@Override
		public RObject evalData(final String expression,
				final IProgressMonitor monitor) throws CoreException {
			return fService.evalData(expression, monitor);
		}
		
		@Override
		public RObject evalData(final String expression, final String factoryId, final int options, final int depth,
				final IProgressMonitor monitor) throws CoreException {
			return fService.evalData(expression, factoryId, options, depth, monitor);
		}
		
		@Override
		public RObject evalData(final RReference reference,
				final IProgressMonitor monitor) throws CoreException {
			return fService.evalData(reference, monitor);
		}
		
		@Override
		public RObject evalData(final RReference reference, final String factoryId, final int options, final int depth,
				final IProgressMonitor monitor) throws CoreException {
			return fService.evalData(reference, factoryId, options, depth, monitor);
		}
		
		@Override
		public void assignData(final String expression, final RObject data,
				final IProgressMonitor monitor) throws CoreException {
			fService.assignData(expression, data, monitor);
		}
		
		@Override
		public void uploadFile(final InputStream in, final long length, final String fileName, final int options,
				final IProgressMonitor monitor) throws CoreException {
			fService.uploadFile(in, length, fileName, options, monitor);
		}
		
		@Override
		public void downloadFile(final OutputStream out, final String fileName, final int options,
				final IProgressMonitor monitor) throws CoreException {
			fService.downloadFile(fileName, options, monitor);
		}
		
		@Override
		public byte[] downloadFile(final String fileName, final int options,
				final IProgressMonitor monitor) throws CoreException {
			return fService.downloadFile(fileName, options, monitor);
		}
		
		@Override
		public FunctionCall createFunctionCall(final String name) throws CoreException {
			return fService.createFunctionCall(name);
		}
		
		@Override
		public RGraphicCreator createRGraphicCreator(final int options) throws CoreException {
			return fService.createRGraphicCreator(options);
		}
		
		@Override
		public boolean acceptNewConsoleCommand() {
			return true;
		}
		
		@Override
		public void submitToConsole(final String input, final IProgressMonitor monitor) throws CoreException {
			fService.evalVoid("{\n" + input + "\n}", monitor); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		@Override
		public void briefAboutChange(final int o) {
		}
		
	}
	
	
	/**
	 * @param typeId
	 * @param label
	 */
	public AbstractStatetRRunnable(final String typeId, final String label) {
		super(typeId, label);
	}
	
	
	@Override
	protected void run(final IRToolService r,
			final IProgressMonitor monitor) throws CoreException {
		if (r instanceof IRConsoleService) {
			run((IRConsoleService) r, monitor);
		}
		else {
			run(new RToolServiceWrapper(r), monitor);
		}
	}
	
	protected void run(final IRConsoleService r,
			final IProgressMonitor monitor) throws CoreException {
	}
	
}
