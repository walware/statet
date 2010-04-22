/*******************************************************************************
 * Copyright (c) 2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rhelp;

import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;


public class RHelpSearchMatch implements IRHelpSearchMatch {
	
	
	public static class Fragment implements MatchFragment {
		
		
		private final String fField;
		private final String fText;
		
		
		public Fragment(final IRHelpSearchMatch match, final String field, final String text) {
			fField = field;
			fText = text;
		}
		
		
		public String getField() {
			return fField;
		}
		
		public String getFieldLabel() {
			if (fField == IREnvIndex.ALIAS_FIELD_NAME || fField == IREnvIndex.ALIAS_TXT_FIELD_NAME) {
				return "Topic";
			}
			if (fField == IREnvIndex.TITLE_TXT_FIELD_NAME) {
				return "Title";
			}
			if (fField == IREnvIndex.CONCEPT_TXT_FIELD_NAME) {
				return "Concept";
			}
			if (fField == IREnvIndex.DESCRIPTION_TXT_FIELD_NAME) {
				return "Description";
			}
			if (fField == IREnvIndex.AUTHORS_TXT_FIELD_NAME) {
				return "Author(s)";
			}
			if (fField == IREnvIndex.EXAMPLES_TXT_FIELD_NAME) {
				return "Examples";
			}
			return "";
		}
		
		public String getText() {
			return fText;
		}
		
		
		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(fField);
			sb.append(": "); //$NON-NLS-1$
			sb.append(fText);
			return sb.toString();
		}
		
	}
	
	
	private final IRHelpPage fPage;
	private final float fScore;
	private MatchFragment[] fBestFragments;
	private int fTotalMatches = -1;
	
	
	public RHelpSearchMatch(final IRHelpPage page, final float score) {
		fPage = page;
		fScore = score;
	}
	
	
	public void setBestFragments(final MatchFragment[] fragments) {
		fBestFragments = fragments;
	}
	
	public void setTotalMatches(final int totalMatches) {
		fTotalMatches = totalMatches;
	}
	
	public IRHelpPage getPage() {
		return fPage;
	}
	
	public float getScore() {
		return fScore;
	}
	
	public int getMatchesCount() {
		return fTotalMatches;
	}
	
	public MatchFragment[] getBestFragments() {
		return fBestFragments;
	}
	
	
	@Override
	public int hashCode() {
		return fPage.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof IRHelpSearchMatch
				&& fPage.equals(((IRHelpSearchMatch) obj).getPage()) );
	}
	
}
