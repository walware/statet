/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import de.walware.statet.r.ui.RUI;


public class RElementSearchQuery implements ISearchQuery {
	
	
	private final RElementSearch searchProcessor;
	
	private final String displayName;
	
	private RElementSearchResult result;
	
	
	public RElementSearchQuery(final RElementSearch searchProcessor) {
		this.searchProcessor= searchProcessor;
		this.displayName= searchProcessor.getElementName().getDisplayName();
	}
	
	
	@Override
	public String getLabel() {
		return Messages.Search_Query_label;
	}
	
	
	public String getSearchLabel() {
		return "'" + this.displayName + "'"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getScopeLabel() {
		return this.searchProcessor.getModeLabel();
	}
	
	public String getMatchLabel(final int count) {
		if (this.searchProcessor.searchWrite()) {
			return (count == 1) ?
					Messages.Search_WriteOccurrence_sing_label :
					Messages.Search_WriteOccurrence_plural_label;
		}
		return (count == 1) ?
				Messages.Search_Occurrence_sing_label :
				Messages.Search_Occurrence_plural_label;
	}
	
	
	@Override
	public IStatus run(final IProgressMonitor monitor) throws OperationCanceledException {
		synchronized (this) {
			if (this.result == null) {
				this.result= new RElementSearchResult(this);
			}
			this.searchProcessor.result= this.result;
		}
		
		try {
			this.searchProcessor.run(SubMonitor.convert(monitor));
			return Status.OK_STATUS;
		}
		catch (final CoreException e) {
			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
				throw new OperationCanceledException();
			}
			return new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					NLS.bind(Messages.Search_error_RunFailed_message, getSearchLabel()),
					e );
		}
	}
	
	@Override
	public boolean canRerun() {
		return true;
	}
	
	@Override
	public boolean canRunInBackground() {
		return true;
	}
	
	@Override
	public ISearchResult getSearchResult() {
		synchronized (this) {
			if (this.result == null) {
				this.result= new RElementSearchResult(this);
			}
			return this.result;
		}
	}
	
}
