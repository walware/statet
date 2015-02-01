/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp;

import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;
import de.walware.statet.r.internal.core.rhelp.index.IREnvIndex;


public class RHelpSearchMatch implements IRHelpSearchMatch {
	
	
	public static class Fragment implements MatchFragment {
		
		
		private final String field;
		private final String text;
		
		
		public Fragment(final IRHelpSearchMatch match, final String field, final String text) {
			this.field= field;
			this.text= text;
		}
		
		
		@Override
		public String getField() {
			return this.field;
		}
		
		@Override
		public String getFieldLabel() {
			if (this.field == IREnvIndex.ALIAS_FIELD_NAME || this.field == IREnvIndex.ALIAS_TXT_FIELD_NAME) {
				return "Topic";
			}
			if (this.field == IREnvIndex.TITLE_TXT_FIELD_NAME) {
				return "Title";
			}
			if (this.field == IREnvIndex.CONCEPT_TXT_FIELD_NAME) {
				return "Concept";
			}
			if (this.field == IREnvIndex.DESCRIPTION_TXT_FIELD_NAME) {
				return "Description";
			}
			if (this.field == IREnvIndex.AUTHORS_TXT_FIELD_NAME) {
				return "Author(s)";
			}
			if (this.field == IREnvIndex.EXAMPLES_TXT_FIELD_NAME) {
				return "Examples";
			}
			return null;
		}
		
		@Override
		public String getText() {
			return this.text;
		}
		
		
		@Override
		public String toString() {
			final StringBuilder sb= new StringBuilder();
			sb.append(this.field);
			sb.append(": "); //$NON-NLS-1$
			sb.append(this.text);
			return sb.toString();
		}
		
	}
	
	
	private final IRHelpPage page;
	private final float score;
	private MatchFragment[] bestFragments;
	private int totalMatches= -1;
	
	
	public RHelpSearchMatch(final IRHelpPage page, final float score) {
		this.page= page;
		this.score= score;
	}
	
	
	public void setBestFragments(final MatchFragment[] fragments) {
		this.bestFragments= fragments;
	}
	
	public void setTotalMatches(final int totalMatches) {
		this.totalMatches= totalMatches;
	}
	
	@Override
	public IRHelpPage getPage() {
		return this.page;
	}
	
	@Override
	public float getScore() {
		return this.score;
	}
	
	@Override
	public int getMatchesCount() {
		return this.totalMatches;
	}
	
	@Override
	public MatchFragment[] getBestFragments() {
		return this.bestFragments;
	}
	
	
	@Override
	public int hashCode() {
		return this.page.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof IRHelpSearchMatch
				&& this.page.equals(((IRHelpSearchMatch) obj).getPage()) );
	}
	
	@Override
	public String toString() {
		final StringBuilder sb= new StringBuilder();
		sb.append(getPage());
		sb.append(" (score= ").append(getScore()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		return sb.toString();
	}
	
}
