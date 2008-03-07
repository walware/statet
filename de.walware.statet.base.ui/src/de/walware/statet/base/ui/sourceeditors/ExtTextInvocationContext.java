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

import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.TextInvocationContext;

import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.ast.AstSelection;


public class ExtTextInvocationContext extends TextInvocationContext {
	
	private StatextEditor1 fEditor;
	private ISourceUnit fSourceUnit;
	private AstSelection fAstSelection;
	
	
	public ExtTextInvocationContext(final StatextEditor1 editor, final IQuickAssistInvocationContext context) {
		super(context.getSourceViewer(), context.getOffset(), context.getLength());
		if (editor != null) {
			fEditor = editor;
			fSourceUnit = editor.getSourceUnit();
		}
	}
	
	public StatextEditor1 getEditor() {
		return fEditor;
	}
	
	public ISourceUnit getSourceUnit() {
		return fSourceUnit;
	}
	
	public AstSelection getAstSelection() {
		return fAstSelection;
	}
	
}
