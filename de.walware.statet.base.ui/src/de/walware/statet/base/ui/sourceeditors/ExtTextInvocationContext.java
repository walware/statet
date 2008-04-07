/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.TextInvocationContext;

import de.walware.eclipsecommons.ltk.IModelManager;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.ISourceUnitModelInfo;
import de.walware.eclipsecommons.ltk.ast.AstSelection;


public class ExtTextInvocationContext extends TextInvocationContext {
	
	private StatextEditor1 fEditor;
	private ISourceUnit fSourceUnit;
	private ISourceUnitModelInfo fModelInfo;
	private AstSelection fAstSelection;
	
	
	public ExtTextInvocationContext(final StatextEditor1 editor, final IQuickAssistInvocationContext context) {
		super(context.getSourceViewer(), context.getOffset(), context.getLength());
		if (editor != null) {
			fEditor = editor;
			fSourceUnit = editor.getSourceUnit();
			if (fSourceUnit != null) {
				// later check, if synch is really necessary or causes delay
				fModelInfo = fSourceUnit.getModelInfo(null, IModelManager.MODEL_FILE, new NullProgressMonitor());
				if (fModelInfo != null) {
					fAstSelection = AstSelection.search(fModelInfo.getAst().root, getOffset(), getOffset(), AstSelection.MODE_COVERING_SAME_LAST);
				}
			}
		}
	}
	
	
	public StatextEditor1 getEditor() {
		return fEditor;
	}
	
	public ISourceUnit getSourceUnit() {
		return fSourceUnit;
	}
	
	public ISourceUnitModelInfo getModelInfo() {
		return fModelInfo;
	}
	
	public AstSelection getAstSelection() {
		return fAstSelection;
	}
	
}
