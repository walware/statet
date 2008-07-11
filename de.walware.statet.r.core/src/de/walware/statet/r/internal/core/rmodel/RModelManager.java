/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
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
import de.walware.eclipsecommons.ltk.IModelManager;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.SourceContent;
import de.walware.eclipsecommons.ltk.WorkingContext;
import de.walware.eclipsecommons.ltk.text.SourceParseInput;
import de.walware.eclipsecommons.ltk.text.StringParseInput;

import de.walware.statet.base.core.StatetCore;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RManagedWorkingCopy;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * 
 */
public class RModelManager implements IModelManager {
	
	
	private class ContextItem {
		final WorkingContext context;
		final HashMap<String, IRSourceUnit> copies;
		final HashMap<String, ISourceUnit> worksheets;
		final FastList<IElementChangedListener> modelListeners;
		
		public ContextItem(final WorkingContext context) {
			this.context = context;
			this.copies = new HashMap<String, IRSourceUnit>();
			this.worksheets = new HashMap<String, ISourceUnit>();
			this.modelListeners = new FastList<IElementChangedListener>(IElementChangedListener.class, FastList.IDENTITY);
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof ContextItem) {
				return ( ((ContextItem) obj).context == this.context);
			}
			return false;
		}
	}
	
	
	private FastList<ContextItem> fContexts = new FastList<ContextItem>(ContextItem.class, FastList.EQUALITY);
	private RModelJob fJob = new RModelJob(this);
	
	
	public RModelManager() {
		getContextItem(StatetCore.PERSISTENCE_CONTEXT, true);
		getContextItem(StatetCore.EDITOR_CONTEXT, true);
	}
	
	
	public void addElementChangedListener(final IElementChangedListener listener, final WorkingContext context) {
		final ContextItem item = getContextItem(context, true);
		item.modelListeners.add(listener);
	}
	
	public void removeElementChangedListener(final IElementChangedListener listener, final WorkingContext context) {
		final ContextItem item = getContextItem(context, false);
		if (item == null) {
			return;
		}
		item.modelListeners.remove(listener);
	}
	
	
	private ContextItem getContextItem(final WorkingContext context, final boolean create) {
		final ContextItem[] contexts = fContexts.toArray();
		for (final ContextItem item : contexts) {
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
	
	
	public void registerWorkingCopy(final IRSourceUnit copy) {
		final ContextItem item = getContextItem(copy.getWorkingContext(), true);
		synchronized (item) {
			item.copies.put(copy.getId(), copy);
		}
	}
	
	public void registerWorksheetCopy(final ISourceUnit copy) {
		final ContextItem item = getContextItem(copy.getWorkingContext(), true);
		synchronized (item) {
			item.worksheets.put(copy.getId()+'+'+copy.getModelTypeId(), copy);
		}
	}
	
	public void removeWorkingCopy(final IRSourceUnit copy) {
		final ContextItem item = getContextItem(copy.getWorkingContext(), true);
		synchronized (item) {
			item.copies.remove(copy.getId());
		}
	}
	
	public void removeWorksheetCopy(final ISourceUnit copy) {
		final ContextItem item = getContextItem(copy.getWorkingContext(), true);
		synchronized (item) {
			item.worksheets.remove(copy.getId()+'+'+copy.getModelTypeId());
		}
	}
	
	public IRSourceUnit getWorkingCopy(final String id, final WorkingContext context) {
		final ContextItem item = getContextItem(context, false);
		if (item == null) {
			return null;
		}
		synchronized (item) {
			return item.copies.get(id);
		}
	}
	
	public ISourceUnit getWorksheetCopy(final String type, final String id, final WorkingContext context) {
		final ContextItem item = getContextItem(context, false);
		if (item == null) {
			return null;
		}
		synchronized (item) {
			return item.worksheets.get(id+'+'+type);
		}
	}
	
	public IRSourceUnit[] getWorkingCopies(final String id) {
		final ArrayList<IRSourceUnit> copies = new ArrayList<IRSourceUnit>();
		final ContextItem[] contexts = fContexts.toArray();
		for (final ContextItem item : contexts) {
			synchronized (item) {
				final IRSourceUnit copy = item.copies.get(id);
				if (copy != null) {
					copies.add(copy);
				}
			}
		}
		return copies.toArray(new IRSourceUnit[copies.size()]);
	}
	
	public IRSourceUnit[] getWorkingCopies(final WorkingContext context) {
		final ContextItem item = getContextItem(context, false);
		if (item == null) {
			return new IRSourceUnit[0];
		}
		synchronized (item) {
			final Collection<IRSourceUnit> units = item.copies.values();
			return units.toArray(new IRSourceUnit[units.size()]);
		}
	}
	
	/**
	 * Refresh reuses existing ast
	 */
	public void refresh(final WorkingContext context) {
		final IRSourceUnit[] units = getWorkingCopies(context);
		for (int i = 0; i < units.length; i++) {
			if (units[i] instanceof RManagedWorkingCopy) {
				final RManagedWorkingCopy u = (RManagedWorkingCopy) units[i];
				synchronized (u.getModelLockObject()) {
					final AstInfo<RAstNode> ast = u.getCurrentRAst();
					if (ast != null) {
						fJob.addReconcile(u, ast, 0);
					}
				}
			}
		}
		
	}
	
	
	public AstInfo<RAstNode> reconcile(final IManagableRUnit u, final int level, final int waitLevel, final IProgressMonitor monitor) {
		synchronized (u.getModelLockObject()) {
			final SourceContent content = u.getContent(monitor);
			final AstInfo<RAstNode> old = u.getCurrentRAst();
			if (old == null || old.stamp != content.stamp) {
				final SourceParseInput input = new StringParseInput(content.text);
				final AstInfo<RAstNode> ast = new AstInfo<RAstNode>(RAst.LEVEL_MODEL_DEFAULT, content.stamp);
				ast.root = new RScanner(input, ast).scanSourceUnit();
				u.setRAst(ast);
				if (monitor.isCanceled()) {
					return null;
				}
				fJob.addReconcile(u, ast, waitLevel);
				return ast;
			}
			else {
				return old;
			}
		}
	}
	
	
	private class SafeRunnable implements ISafeRunnable {
		
		final ElementChangedEvent fEvent;
		IElementChangedListener fListener;
		
		public SafeRunnable(final ElementChangedEvent event) {
			fEvent = event;
		}
		
		public void run() {
			fListener.elementChanged(fEvent);
		}
		
		public void handleException(final Throwable exception) {
			RCorePlugin.log(new Status(Status.ERROR, RCore.PLUGIN_ID, -1, "An error occured while notifying an ElementChangedListener.", exception)); //$NON-NLS-1$
		}
		
	}
	
	void fireDelta(final IModelElementDelta delta, final WorkingContext context) {
		final ElementChangedEvent event = new ElementChangedEvent(delta, context);
		final SafeRunnable runnable = new SafeRunnable(event);
		final ContextItem item = getContextItem(context, true);
		final IElementChangedListener[] listeners = item.modelListeners.toArray();
		for (int i = 0; i < listeners.length; i++) {
			runnable.fListener = listeners[i];
			SafeRunner.run(runnable);
		}
	}
	
}
