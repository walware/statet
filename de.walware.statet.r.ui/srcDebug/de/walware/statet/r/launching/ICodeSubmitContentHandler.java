/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.launching;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


public interface ICodeSubmitContentHandler {
	
	
	void setup(IDocument document);
	
	List<String> getCodeLines(IDocument document)
			throws CoreException, BadLocationException;
	
	List<String> getCodeLines(IDocument document, int offset, int length)
			throws CoreException, BadLocationException;
	
}
