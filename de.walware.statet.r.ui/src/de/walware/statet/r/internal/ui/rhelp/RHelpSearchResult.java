/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.rhelp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.workbench.search.ui.ElementMatchComparator;
import de.walware.ecommons.workbench.search.ui.ExtTextSearchResult;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IRPkgHelp;
import de.walware.statet.r.ui.RUI;


public class RHelpSearchResult extends ExtTextSearchResult<IRPkgHelp, RHelpSearchMatch> {
	
	
	public static final ElementMatchComparator<IRPkgHelp, RHelpSearchMatch> COMPARATOR= new ElementMatchComparator<>(
			new IRPkgHelp[0], null,
			new RHelpSearchMatch[0], null );
	
	
	private final RHelpSearchUIQuery query;
		
	private IREnv rEnv;
	
	
	public RHelpSearchResult(final RHelpSearchUIQuery query) {
		super(COMPARATOR);
		
		this.query= query;
	}
	
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return RUI.getImageDescriptor(RUI.IMG_OBJ_R_HELP_SEARCH);
	}
	
	@Override
	public String getLabel() {
		final String searchLabel= this.query.getSearchLabel();
		
		final String matchLabel;
		{	final Object[] data= new Object[3]; // count, pkg-count, renv
			
			data[2]= this.rEnv.getName();
			if (data[2] == null) {
				data[2]= "-"; //$NON-NLS-1$
			}
			
			int count;
			synchronized (this) {
				count= getMatchCount();
				data[0]= count;
				data[1]= getElementCount();
			}
			
			if (count == 1) {
				matchLabel= NLS.bind(Messages.Search_SingleMatch_label, data[2]);
			}
			else {
				matchLabel= NLS.bind(Messages.Search_MultipleMatches_label, data);
			}
		}
		
		return searchLabel + " – " + matchLabel; //$NON-NLS-1$
	}
	
	@Override
	public RHelpSearchUIQuery getQuery() {
		return this.query;
	}
	
	
	public void init(final IREnv renv) {
		this.rEnv= renv;
		removeAll();
	}
	
}
