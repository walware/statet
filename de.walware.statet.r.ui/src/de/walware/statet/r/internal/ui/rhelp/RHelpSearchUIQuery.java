/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rhelp;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;
import de.walware.statet.r.core.rhelp.IRHelpSearchRequestor;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.ui.RUI;


public class RHelpSearchUIQuery implements ISearchQuery {
	
	
	private final RHelpSearchQuery fQuery;
	
	private RHelpSearchUIResult fResult;
	
	
	public RHelpSearchUIQuery(final RHelpSearchQuery.Compiled coreQuery) {
		if (coreQuery == null) {
			throw new NullPointerException();
		}
		fQuery = coreQuery;
	}
	
	
	@Override
	public IStatus run(final IProgressMonitor monitor) throws OperationCanceledException {
		synchronized (this) {
			if (fResult == null) {
				fResult = new RHelpSearchUIResult(this);
			}
			fResult.init(fQuery.getREnv());
		}
		
		final IRHelpSearchRequestor requestor = new IRHelpSearchRequestor() {
			@Override
			public int maxFragments() {
				return PreferencesUtil.getInstancePrefs().getPreferenceValue(
						RHelpPreferences.SEARCH_PREVIEW_FRAGMENTS_MAX_PREF);
			}
			@Override
			public void matchFound(final IRHelpSearchMatch match) {
				fResult.addMatch(new RHelpSearchUIMatch(match));
			}
		};
		try {
			RCore.getRHelpManager().search(fQuery, requestor, monitor);
			return Status.OK_STATUS;
		}
		catch (final CoreException e) {
			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
				throw new OperationCanceledException();
			}
			return new Status(IStatus.ERROR, RUI.PLUGIN_ID, "An error occurred when performing R help search: " + getLongLabel());
		}
	}
	
	@Override
	public String getLabel() {
		return Messages.Search_Query_label;
	}
	
	public String getLongLabel() {
		final String searchString = fQuery.getSearchString();
		if (searchString.length() > 0) {
			switch (fQuery.getSearchType()) {
			case RHelpSearchQuery.TOPIC_SEARCH:
				return NLS.bind(Messages.Search_PatternInTopics_label, searchString);
			case RHelpSearchQuery.FIELD_SEARCH:
				return NLS.bind(Messages.Search_PatternInFields_label, searchString);
			}
		}
		return NLS.bind(Messages.Search_Pattern_label, searchString);
	}
	
	@Override
	public boolean canRerun() {
		return fQuery.getREnv().getConfig() != null;
	}
	
	@Override
	public boolean canRunInBackground() {
		return true;
	}
	
	@Override
	public ISearchResult getSearchResult() {
		synchronized (this) {
			if (fResult == null) {
				fResult = new RHelpSearchUIResult(this);
				fResult.init(fQuery.getREnv());
			}
		}
		return fResult;
	}
	
	
	public RHelpSearchQuery getRHelpQuery() {
		return fQuery;
	}
	
}
