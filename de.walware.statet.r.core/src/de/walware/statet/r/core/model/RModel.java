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
	
	
	public static IFrameInSource searchEnvir(RAstNode node) {
		while (node != null) {
			final Object[] attachments = node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof IFrame) {
					return (IFrameInSource) attachment;
				}
			}
			node = node.getRParent();
		}
		return null;
	}
	
	public static IFrameInSource[] createEnvirList(final IFrameInSource envir) {
		final ArrayList<IFrameInSource> list = new ArrayList<IFrameInSource>();
		int idx = 0;
		list.add(envir);
		while (idx < list.size()) {
			final List<? extends IFrameInSource> ps = list.get(idx++).getUnderneathEnvirs();
			for (final IFrameInSource p : ps) {
				if (!list.contains(p)) {
					list.add(p);
				}
			}
		}
		return list.toArray(new IFrameInSource[list.size()]);
	}
	
	
	private RModel() {}
	
}
