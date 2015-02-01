/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.ui.preferences;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String SweaveEditorOptions_RAndLatexRef_note;
	public static String SweaveEditorOptions_SyntaxColoring_note;
	
	public static String SweaveEditorOptions_MarkOccurrences_Enable_label;
	
	public static String SweaveEditorOptions_SpellChecking_Enable_label;
	public static String SweaveEditorOptions_SpellChecking_note;
	
	public static String SweaveEditorOptions_AnnotationAppearance_info;
	
	public static String DocTemplates_title;
	public static String DocTemplates_LtxRweave_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
