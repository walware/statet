/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rmodel;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.IWorkingBuffer;
import de.walware.eclipsecommons.ltk.SourceContent;
import de.walware.eclipsecommons.ltk.WorkingContext;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 *
 */
public class RSourceUnitWorkingCopy implements IRSourceUnit {
	
	
	private WorkingContext fContext;
	private IRSourceUnit fFrom;
	private IWorkingBuffer fBuffer;
	
	AstInfo<RAstNode> fAst;
	Object fAstLock = new Object();
	private int fCounter = 0;
	
	
	public RSourceUnitWorkingCopy(IRSourceUnit from, WorkingContext context) {
		fContext = context;
		fFrom = from;
	}
	
	public WorkingContext getWorkingContext() {
		return fContext;
	}
	
	public synchronized IRSourceUnit getWorkingCopy(WorkingContext context, boolean create) {
		String id = getId();
		IRSourceUnit workingCopy = RCorePlugin.getDefault().getRModelManager().getWorkingCopy(id, context);
		if (workingCopy == null && create) {
			workingCopy = new RSourceUnitWorkingCopy(this, context);
		}
		workingCopy.connect();
		return workingCopy;
	}
	
	public IRSourceUnit getUnderlyingUnit() {
		return fFrom;
	}

	public ISourceUnit getSourceUnit() {
		return this;
	}
	public String getElementName() {
		return fFrom.getElementName();
	}
	public String getId() {
		return fFrom.getId();
	}
	public RProject getRProject() {
		return fFrom.getRProject();
	}
	public StatetProject getStatetProject() {
		return fFrom.getStatetProject();
	}
	public IPath getPath() {
		return fFrom.getPath();
	}
	public IResource getResource() {
		return fFrom.getResource();
	}
	
	public AbstractDocument getDocument() {
		return fBuffer.getDocument();
	}
	
	public SourceContent getContent() {
		return fBuffer.getContent();
	}
	
	public IPreferenceAccess getPrefs() {
		return fFrom.getPrefs();
	}
	public RCodeStyleSettings getRCodeStyle() {
		return fFrom.getRCodeStyle();
	}
	
	
	public IModelElement getParent() {
		return null; // directory
	}
	
	public boolean hasChildren(Object filter) {
		return true;
	}

	public IModelElement[] getChildren(Object filter) {
		return new IModelElement[0];
	}

	public synchronized final void connect() {
		fCounter++;
		if (fCounter == 1) {
			if (fBuffer == null) {
				fBuffer = fContext.createWorkingBuffer(this);
			}
			RCorePlugin.getDefault().getRModelManager().registerWorkingCopy(this);
			fFrom.connect();
		}
	}
	
	public synchronized final void disconnect() {
		fCounter--;
		if (fCounter == 0) {
			fBuffer.releaseDocument();
			RCorePlugin.getDefault().getRModelManager().removeWorkingCopy(this);
			fFrom.disconnect();
		}
	}
	
	public void reconcile(int level, IProgressMonitor monitor) {
		RCorePlugin.getDefault().getRModelManager().reconcile(this, level, monitor);
		// ast field is updated in model manager
	}

	public AstInfo<RAstNode> getAstInfo(boolean ensureSync, IProgressMonitor monitor) {
		if (ensureSync) {
			reconcile(0, monitor);
		}
		return fAst;
	}

}
