/**
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 */

package de.walware.statet.rtm.ftable.ui.actions;

import de.walware.statet.rtm.base.ui.actions.AbstractNewRTaskFileWizard;
import de.walware.statet.rtm.ftable.ui.RtFTableDescriptor;


public class NewFTableFileWizard extends AbstractNewRTaskFileWizard {
	
	
	public NewFTableFileWizard() {
		super(RtFTableDescriptor.INSTANCE);
	}
	
	
}
