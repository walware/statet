/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import java.util.ArrayList;
import java.util.List;

import de.walware.statet.r.core.rsource.ast.RAstNode;


/**
 * 
 */
public class RModel {
	
	
	public static final String TYPE_ID = "r"; //$NON-NLS-1$
	
	
	public static IRFrameInSource searchEnvir(RAstNode node) {
		while (node != null) {
			final Object[] attachments = node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof IRFrameInSource) {
					return (IRFrameInSource) attachment;
				}
			}
			node = node.getRParent();
		}
		return null;
	}
	
	public static List<IRFrame> createEnvirList(final IRFrame envir) {
		final ArrayList<IRFrame> list = new ArrayList<IRFrame>();
		int idx = 0;
		list.add(envir);
		while (idx < list.size()) {
			final List<? extends IRFrame> ps = list.get(idx++).getPotentialParents();
			for (final IRFrame p : ps) {
				if (!list.contains(p)) {
					list.add(p);
				}
			}
		}
		return list;
	}
	
	
	private RModel() {}
	
}
