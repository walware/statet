/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.text.FixInterningStringCache;
import de.walware.ecommons.text.IStringCache;
import de.walware.ecommons.text.PartialStringParseInput;
import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.text.StringParseInput;

import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.SpecialParseContent;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstInfo;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.core.rsource.ast.RoxygenScanner;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


/**
 * Worker for r model manager
 */
public class RReconciler {
	
	
	private static final boolean LOG_TIME = false;
	
	protected static class Data {
		
		public final IManagableRUnit su;
		public final SourceContent content;
		
		public SourceParseInput parseInput;
		public int parseOffset;
		
		public RAstInfo ast;
		
		public IRModelInfo oldModel;
		public IRModelInfo newModel;
		
		public Data(final IManagableRUnit su, final IProgressMonitor monitor) {
			this.su = su;
			this.content = su.getParseContent(monitor);
		}
		
	}
	
	
	private final RModelManager fManager;
	
	private final Object fAstLock = new Object();
	private final IStringCache f1AstStringCache;
	private final RoxygenScanner f1RoxygenScanner;
	
	private final Object fModelLock = new Object();
	private final SourceAnalyzer f2ScopeAnalyzer;
	private final SyntaxProblemReporter f2SyntaxReporter;
	
	protected boolean fStop = false;
	
	
	public RReconciler(final RModelManager manager) {
		fManager = manager;
		f1AstStringCache = new FixInterningStringCache();
		f1RoxygenScanner = new RoxygenScanner(f1AstStringCache);
		f2ScopeAnalyzer = new SourceAnalyzer();
		f2SyntaxReporter = new SyntaxProblemReporter();
	}
	
	
	/** for editor reconciling */
	public IRModelInfo reconcile(final IManagableRUnit su, final int level, final boolean reconciler, final IProgressMonitor monitor) {
		final int type = (su.getModelTypeId().equals(RModel.TYPE_ID) ? su.getElementType() : 0);
		if (type == 0) {
			return null;
		}
		final Data data = new Data(su, monitor);
		if (data.content == null) {
			return null;
		}
		synchronized (fAstLock) {
			if (fStop) {
				return null;
			}
			updateAst(data, monitor);
		}
		if (level <= IModelManager.AST) {
			return null;
		}
		
		synchronized (fModelLock) {
			if (fStop) {
				return null;
			}
			updateModel(data);
			
			if (fStop) {
				return null;
			}
			// Report problems
			if (reconciler) {
				final IProblemRequestor problemRequestor = su.getProblemRequestor();
				if (problemRequestor != null) {
					problemRequestor.beginReportingSequence();
					f2SyntaxReporter.run(su, data.ast, problemRequestor);
					problemRequestor.endReportingSequence();
				}
			}
			
			if (fStop) {
				return null;
			}
			// Throw event
			if (data.newModel != null && (data.oldModel == null || data.oldModel.getStamp() != data.newModel.getStamp())) {
				fManager.getEventJob().addUpdate(su, data.oldModel, data.newModel);
			}
		}
		return data.newModel;
	}
	
	protected void initParseInput(final Data data) {
		if (data.parseInput == null) {
			if (data.content instanceof SpecialParseContent) {
				final SpecialParseContent parseContent = (SpecialParseContent) data.content;
				data.parseInput = new PartialStringParseInput(data.content.text, parseContent.offset);
				data.parseOffset = parseContent.offset;
			}
			else {
				data.parseInput = new StringParseInput(data.content.text);
				data.parseOffset = 0;
			}
		}
	}
	
	protected final void updateAst(final Data data, final IProgressMonitor monitor) {
		final RAstInfo old = data.su.getCurrentRAst();
		
		if (old == null || old.stamp != data.content.stamp) {
			final long startAst;
			final long stopAst;
			startAst = System.nanoTime();
			
			initParseInput(data);
			final RAstInfo2 ast = new RAstInfo2(RAst.LEVEL_MODEL_DEFAULT, data.content.stamp);
			final RScanner scanner = new RScanner(data.parseInput, ast, f1AstStringCache);
			scanner.setCommentLevel(100);
			final SourceComponent sourceComponent = scanner.scanSourceRange(null, data.parseOffset, data.content.text.length());
			ast.set(sourceComponent, scanner.getLineOffsets());
			
			stopAst = System.nanoTime();
			
			f1RoxygenScanner.init(data.parseInput);
			f1RoxygenScanner.update(ast.root);
			
			if (LOG_TIME) {
				System.out.println(f1AstStringCache.toString());
				System.out.println("RReconciler/createAST   : " + DecimalFormat.getInstance().format(stopAst-startAst)); //$NON-NLS-1$
			}
			
			synchronized (data.su.getModelLockObject()) {
				data.su.setRAst(ast);
			}
			data.ast = ast;
		}
		else {
			data.ast = old;
		}
	}
	
	protected final void updateModel(final Data data) {
		// Update Model
		IRModelInfo oldModel = null;
		IRModelInfo newModel = isUpToDate(data.su, data.ast.stamp);
		if (newModel == null) {
			final long startModel;
			final long stopModel;
			startModel = System.nanoTime();
			
			newModel = f2ScopeAnalyzer.createModel(data.su, data.ast);
			final boolean isOK = (newModel != null);
			
			stopModel = System.nanoTime();
			
			if (LOG_TIME) {
				System.out.println("RReconciler/createMODEL : " + DecimalFormat.getInstance().format(stopModel-startModel)); 
			}
			
			synchronized (data.su.getModelLockObject()) {
				if (isOK) {
					oldModel = data.su.getCurrentRModel();
					final RAstInfo oldAst = data.su.getCurrentRAst();
					if (oldAst == null || oldAst.stamp == newModel.getStamp()) {
						// otherwise, the ast is probably newer
						data.su.setRAst((RAstInfo) newModel.getAst());
					}
					data.su.setRModel(newModel);
				}
			}
		}
		data.oldModel = oldModel;
		data.newModel = newModel;
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
