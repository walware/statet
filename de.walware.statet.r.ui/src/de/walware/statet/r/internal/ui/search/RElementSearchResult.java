/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;

import de.walware.ecommons.ltk.IExtContentTypeManager;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.IModelTypeDescriptor;
import de.walware.ecommons.ltk.core.model.ISourceElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.util.ElementComparator;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.workbench.search.ui.ElementMatchComparator;
import de.walware.ecommons.workbench.search.ui.ExtTextSearchResult;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.ui.RUI;


public class RElementSearchResult extends ExtTextSearchResult<IRSourceUnit, RElementMatch> 
		implements IFileMatchAdapter, IEditorMatchAdapter {
	
	
	private static ISourceUnit getSourceUnit(final IEditorPart editor) {
		final ISourceEditor sourceEditor= (ISourceEditor) editor.getAdapter(ISourceEditor.class);
		if (sourceEditor != null) {
			return sourceEditor.getSourceUnit();
		}
		return null;
	}
	
	
	public static final ElementMatchComparator<IRSourceUnit, RElementMatch> COMPARATOR= new ElementMatchComparator<>(
			new IRSourceUnit[0], new ElementComparator(RSymbolComparator.R_NAMES_COLLATOR),
			new RElementMatch[0], new DefaultMatchComparator<>() );
	
	
	private final RElementSearchQuery query;
	
	
	public RElementSearchResult(final RElementSearchQuery query) {
		super(COMPARATOR);
		
		this.query= query;
	}
	
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return RUI.getImageDescriptor(RUI.IMG_OBJ_R_SCRIPT);
	}
	
	@Override
	public String getLabel() {
		final StringBuilder sb= new StringBuilder(this.query.getSearchLabel());
		
		sb.append(" - "); //$NON-NLS-1$
		
		{	final int count= getMatchCount();
			sb.append(count);
			sb.append(' ');
			sb.append(this.query.getMatchLabel(count));
			sb.append(' ');
			sb.append("in");
			sb.append(' ');
			sb.append(this.query.getScopeLabel());
		}
		
		return sb.toString();
	}
	
	@Override
	public String getTooltip() {
		return getLabel();
	}
	
	@Override
	public ISearchQuery getQuery() {
		return this.query;
	}
	
	
	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}
	
	@Override
	public IFile getFile(Object element) {
		if (element instanceof ISourceElement) {
			element= ((ISourceElement) element).getSourceUnit();
		}
		if (element instanceof ISourceUnit) {
			element= ((ISourceUnit) element).getResource();
		}
		if (element instanceof IFile) {
			return (IFile) element;
		}
		return null;
	}
	
	@Override
	public Match[] computeContainedMatches(final AbstractTextSearchResult result,
			final IFile file) {
		final IContentType contentType= IDE.guessContentType(file);
		if (contentType != null) {
			final IExtContentTypeManager typeManager= LTK.getExtContentTypeManager();
			final IModelTypeDescriptor modelType= typeManager.getModelTypeForContentType(contentType.getId());
			if (modelType != null && (modelType.getId() == RModel.TYPE_ID
					|| modelType.getSecondaryTypeIds().contains(RModel.TYPE_ID) )) {
				final ISourceUnit su= LTK.getSourceUnitManager().getSourceUnit(modelType.getId(),
						LTK.PERSISTENCE_CONTEXT, file, false, null);
				if (su != null) {
					su.disconnect(null);
					return getMatches(su);
				}
			}
		}
		return getComparator().getMatch0();
	}
	
	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}
	
	@Override
	public boolean isShownInEditor(final Match match, final IEditorPart editor) {
		ISourceUnit su= getSourceUnit(editor);
		while (su != null) {
			if (su.equals(match.getElement())) {
				return true;
			}
			su= su.getUnderlyingUnit();
		}
		return false;
	}
	
	@Override
	public Match[] computeContainedMatches(final AbstractTextSearchResult result,
			final IEditorPart editor) {
		ISourceUnit su= getSourceUnit(editor);
		while (su != null) {
			final RElementMatch[] matches= getMatches(su);
			if (matches.length > 0) {
				return matches;
			}
			su= su.getUnderlyingUnit();
		}
		return getComparator().getMatch0();
	}
	
}
