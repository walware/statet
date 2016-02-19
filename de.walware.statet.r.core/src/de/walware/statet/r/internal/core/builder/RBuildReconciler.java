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

package de.walware.statet.r.internal.core.builder;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.text.core.ILineInformation;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RSuModelContainer;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.SourceComponent;
import de.walware.statet.r.internal.core.sourcemodel.RModelManager;
import de.walware.statet.r.internal.core.sourcemodel.RReconciler;


public class RBuildReconciler extends RReconciler {
	
	
	private final RTaskMarkerHandler taskScanner;
	
	private MultiStatus statusCollector;
	
	
	public RBuildReconciler(final RModelManager manager) {
		super(manager);
		
		this.taskScanner= new RTaskMarkerHandler();
	}
	
	
	public void init(final IRProject project, final MultiStatus status) throws CoreException {
		this.taskScanner.init(project);
		this.statusCollector= status;
	}
	
	/** for file build 
	 * @throws CoreException
	 **/
	public IRModelInfo build(final RSuModelContainer adapter, final IProgressMonitor monitor) {
		final IRSourceUnit su= adapter.getSourceUnit();
		final int type= (su.getModelTypeId().equals(RModel.TYPE_ID) ? su.getElementType() : 0);
		if (type == 0) {
			return null;
		}
		if (this.stop || monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		final Data data= new Data(adapter, monitor);
		if (data.content == null) {
			return null;
		}
		
		if (this.stop || monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		updateAst(data, monitor);
		
		if (this.stop || monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		updateModel(data);
		
		if (this.stop || monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		
//		final IProblemRequestor problemRequestor= su.getProblemRequestor();
//		if (problemRequestor != null) {
//			problemRequestor.beginReportingSequence();
			try {
				final List<RAstNode> comments= ((SourceComponent) data.ast.root).getComments();
				this.taskScanner.setup((IResource) su.getResource());
				final ILineInformation lines= data.content.getLines();
				for (final RAstNode comment : comments) {
						final int offset= comment.getOffset()+1;
						this.taskScanner.checkForTasks(data.content.getText().substring(
								offset, offset+comment.getLength()-1 ), offset, lines );
				}
			}
			catch (final Exception e) {
				this.statusCollector.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
						ICommonStatusConstants.BUILD_ERROR, "Failed to create task marker(s).", e));
			}
//			f2SyntaxReporter.run(su, ast, problemRequestor);
//			problemRequestor.endReportingSequence();
//		}
		
		if (this.stop || monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		return data.newModel;
	}
	
}
