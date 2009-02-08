/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import com.ibm.icu.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.text.FixInterningStringCache;
import de.walware.ecommons.text.IStringCache;
import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.text.StringParseInput;

import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;


/**
 * Worker for r model manager
 */
public class RReconciler {
	
	
	private static final boolean LOG_TIME = false;
	
	
	private final RModelManager fManager;
	
	private final Object fAstLock = new Object();
	private final IStringCache fAstStringCache1;
	
	private final Object fModelLock = new Object();
	private final SourceAnalyzer fScopeAnalyzer;
	private final SyntaxProblemReporter fSyntaxReporter;
	
	private boolean fStop = false;
	
	
	public RReconciler(final RModelManager manager) {
		fManager = manager;
		fScopeAnalyzer = new SourceAnalyzer();
		fSyntaxReporter = new SyntaxProblemReporter();
		fAstStringCache1 = new FixInterningStringCache();
	}
	
	
	void reconcile(final IManagableRUnit u, final int level, final boolean reconciler, final IProgressMonitor monitor) {
		// Update AST
		final AstInfo<RAstNode> ast;
		final int type = (u.getModelTypeId().equals(RModel.TYPE_ID) ? u.getElementType() : 0);
		if (type == 0) {
			return;
		}
		synchronized (fAstLock) {
			if (fStop) {
				return;
			}
			final SourceContent content = u.getContent(monitor);
			final AstInfo<RAstNode> old = u.getCurrentRAst();
			
			if (old == null || old.stamp != content.stamp) {
				long startAst;
				if (LOG_TIME) {
					startAst = System.nanoTime();
				}
				
				final SourceParseInput input = new StringParseInput(content.text);
				ast = new AstInfo<RAstNode>(RAst.LEVEL_MODEL_DEFAULT, content.stamp);
				ast.root = new RScanner(input, ast, fAstStringCache1).scanSourceUnit();
				
				if (LOG_TIME) {
					System.out.println(fAstStringCache1.toString());
					final long stopAst = System.nanoTime();
					System.out.println("RReconciler/createAST   : " + DecimalFormat.getInstance().format(stopAst-startAst)); //$NON-NLS-1$
				}
				
				synchronized (u.getModelLockObject()) {
					u.setRAst(ast);
				}
			}
			else {
				ast = old;
			}
		}
		if (level <= IModelManager.AST) {
			return;
		}
		
		synchronized (fModelLock) {
			if (fStop) {
				return;
			}
			// Update Model
			IRModelInfo oldModel = null;
			IRModelInfo newModel = isUpToDate(u, ast.stamp);
			if (newModel == null) {
				long startModel;
				if (LOG_TIME) {
					startModel = System.nanoTime();
				}
				
				newModel = fScopeAnalyzer.update(u, ast);
				final boolean isOK = (newModel != null);
				
				if (LOG_TIME) {
					final long stopModel = System.nanoTime();
					System.out.println("RReconciler/createMODEL : " + DecimalFormat.getInstance().format(stopModel-startModel)); //$NON-NLS-1$
				}
				
				synchronized (u.getModelLockObject()) {
					if (isOK) {
						oldModel = u.getCurrentRModel();
						final AstInfo<RAstNode> oldAst = u.getCurrentRAst();
						if (oldAst == null || oldAst.stamp == newModel.getStamp()) {
							// otherwise, the ast is probably newer
							u.setRAst(newModel.getAst());
						}
						u.setRModel(newModel);
					}
				}
				
			}
			
			if (fStop) {
				return;
			}
			// Report problems
			if (reconciler) {
				final IProblemRequestor problemRequestor = u.getProblemRequestor();
				if (problemRequestor != null) {
					problemRequestor.beginReportingSequence();
					fSyntaxReporter.run(u, ast, problemRequestor);
					problemRequestor.endReportingSequence();
				}
			}
			
			if (fStop) {
				return;
			}
			// Throw event
			if (newModel != null && (oldModel == null || oldModel.getStamp() != newModel.getStamp())) {
				fManager.getEventJob().addUpdate(u, oldModel, newModel);
			}
		}
	}
	
	private IRModelInfo isUpToDate(final IManagableRUnit u, final long stamp) {
		final IRModelInfo currentInfo = u.getCurrentRModel();
		if (currentInfo != null && currentInfo.getStamp() == stamp) {
			return currentInfo;
		}
		return null;
	}
	
	void stop() {
		fStop = true;
	}
	
}
