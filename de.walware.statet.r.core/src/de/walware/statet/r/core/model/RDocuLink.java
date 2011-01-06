/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import de.walware.statet.r.core.rsource.ast.DocuComment;
import de.walware.statet.r.internal.core.sourcemodel.IRLangSourceElement;


public class RDocuLink {
	
	
	private final IRLangSourceElement fElement;
	private final DocuComment fDocuNode;
	
	
	public RDocuLink(final IRLangSourceElement element, final DocuComment docuNode) {
		fElement = element;
		fDocuNode = docuNode;
	}
	
	
	public IRLangSourceElement getModelElement() {
		return fElement;
	}
	
	public DocuComment getDocuCommentNode() {
		return fDocuNode;
	}
	
}
