/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;

import de.walware.eclipsecommons.FastList;
import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.ElementChangedEvent;
import de.walware.eclipsecommons.ltk.IElementChangedListener;
import de.walware.eclipsecommons.ltk.IModelElementDelta;
import de.walware.eclipsecommons.ltk.SourceContent;
import de.walware.eclipsecommons.ltk.WorkingContext;
import de.walware.eclipsecommons.ltk.text.SourceParseInput;
import de.walware.eclipsecommons.ltk.text.StringParseInput;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.internal.core.RCorePlugin;



/**
 *
 */
public class RModelManager {
	
	private class ContextItem {
		final WorkingContext context;
		final HashMap<String, IRSourceUnit> copies;
		final FastList<IElementChangedListener> modelListeners;
		
		public ContextItem(WorkingContext context) {
			this.context = context;
			this.copies = new HashMap<String, IRSourceUnit>();
			this.modelListeners = new FastList<IElementChangedListener>(IElementChangedListener.class, FastList.IDENTITY);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ContextItem) {
				return ( ((ContextItem) obj).context == this.context);
			}
			return false;
		}
	}
	
	
	private FastList<ContextItem> fContexts = new FastList<ContextItem>(ContextItem.class, FastList.EQUALITY);
	

	public RModelManager() {
		getContextItem(RCore.PERSISTENCE_CONTEXT, true);
		getContextItem(RCore.PRIMARY_WORKING_CONTEXT, true);
	}
	
	
	public void addElementChangedListener(IElementChangedListener listener, WorkingContext context) {
		ContextItem item = getContextItem(context, true);
		item.modelListeners.add(listener);
	}
	
	public void removeElementChangedListener(IElementChangedListener listener, WorkingContext context) {
		ContextItem item = getContextItem(context, false);
		if (item == null) {
			return;
		}
		item.modelListeners.remove(listener);
	}
	
	
	private ContextItem getContextItem(WorkingContext context, boolean create) {
		ContextItem[] contexts = fContexts.toArray();
		for (ContextItem item : contexts) {
			if (item.context == context) {
				return item;
			}
		}
		if (!create) {
			return null;
		}
		fContexts.add(new ContextItem(context));
		return getContextItem(context, true);
	}

	
	public void registerWorkingCopy(IRSourceUnit copy) {
		ContextItem item = getContextItem(copy.getWorkingContext(), true);
		synchronized (item) {
			item.copies.put(copy.getId(), copy);
		}
	}
	
	public void removeWorkingCopy(IRSourceUnit copy) {
		ContextItem item = getContextItem(copy.getWorkingContext(), true);
		synchronized (item) {
			item.copies.remove(copy.getId());
		}
	}
	
	public IRSourceUnit getWorkingCopy(String id, WorkingContext context) {
		ContextItem item = getContextItem(context, false);
		if (item == null) {
			return null;
		}
		synchronized (item) {
			return item.copies.get(id);
		}
	}
	
	public IRSourceUnit[] getWorkingCopies(String id) {
		ArrayList<IRSourceUnit> copies = new ArrayList<IRSourceUnit>();
		ContextItem[] contexts = fContexts.toArray();
		for (ContextItem item : contexts) {
			synchronized (item) {
				IRSourceUnit copy = item.copies.get(id);
				if (copy != null) {
					copies.add(copy);
				}
			}
		}
		return copies.toArray(new IRSourceUnit[copies.size()]);
	}
	
	public IRSourceUnit[] getWorkingCopies(WorkingContext context) {
		ContextItem item = getContextItem(context, false);
		if (item == null) {
			return new IRSourceUnit[0];
		}
		synchronized (item) {
			Collection<IRSourceUnit> units = item.copies.values();
			return units.toArray(new IRSourceUnit[units.size()]);
		}
	}
	
	
	
	public AstInfo<RAstNode> reconcile(RSourceUnitWorkingCopy u, int level, IProgressMonitor monitor) {
		ModelDelta delta = null;
		synchronized (u.fAstLock) {
			SourceContent content = u.getContent();
			AstInfo<RAstNode> old = u.fAst;
			if (old == null || old.stamp != content.stamp) {
				SourceParseInput input = new StringParseInput(content.text);
				AstInfo<RAstNode> ast = new AstInfo<RAstNode>(level, content.stamp);
				ast.root = new RScanner(input, ast).scanSourceUnit();
				u.fAst = ast;
				delta = new ModelDelta(u, old, ast);
			}
			else {
				return old;
			}
		}
		fireDelta(delta, u.getWorkingContext());
		return delta.getNewAst();
	}
	
	private class SafeRunnable implements ISafeRunnable {
		
		final ElementChangedEvent fEvent;
		IElementChangedListener fListener;
	
		public SafeRunnable(ElementChangedEvent event) {
			fEvent = event;
		}
		
		public void run() {
			fListener.elementChanged(fEvent);
		}

		public void handleException(Throwable exception) {
			RCorePlugin.log(new Status(Status.ERROR, RCore.PLUGIN_ID, -1, "An error occured while notifying an ElementChangedListener.", exception)); //$NON-NLS-1$
		}
		
	}
	void fireDelta(final IModelElementDelta delta, final WorkingContext context) {
		ElementChangedEvent event = new ElementChangedEvent(delta, context);
		SafeRunnable runnable = new SafeRunnable(event);
		ContextItem item = getContextItem(context, true);
		IElementChangedListener[] listeners = item.modelListeners.toArray();
		for (int i = 0; i < listeners.length; i++) {
			runnable.fListener = listeners[i];
			SafeRunner.run(runnable);
		}
	}
	
}
