/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rhelp;

import java.util.List;

import org.eclipse.core.runtime.CoreException;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.internal.core.rhelp.IREnvIndex;
import de.walware.statet.r.internal.core.rhelp.REnvIndexReader;


public class RHelpSearchQuery {
	
	
	public static final int TOPIC_SEARCH = 1;
	public static final int FIELD_SEARCH = 2;
	public static final int DOC_SEARCH = 3;
	
	public static final String TOPICS_FIELD = IREnvIndex.ALIAS_TXT_FIELD_NAME;
	public static final String TITLE_FIELD = IREnvIndex.TITLE_TXT_FIELD_NAME;
	public static final String CONCEPTS_FIELD = IREnvIndex.CONCEPT_TXT_FIELD_NAME;
	
	
	public static class Compiled extends RHelpSearchQuery {
		
		private final Object fCompiled;
		
		public Compiled(final RHelpSearchQuery org, final Object compiled) {
			super(org.getSearchType(),
					org.getSearchString(),
					org.getEnabledFields(),
					org.getKeywords(),
					org.getPackages(),
					org.getREnv().resolve() );
			fCompiled = compiled;
		}
		
		@Override
		public Compiled compile() {
			return this;
		}
		
		public Object compiled() {
			return fCompiled;
		}
		
	}
	
	
	private final int fSearchType;
	private final String fSearchText;
	private final List<String> fFields;
	private final List<String> fKeywords;
	private final List<String> fPackages;
	
	private final IREnv fREnv;
	
	
	/**
	 * @param type
	 * @param text
	 * @param fields
	 * @param keywords
	 * @param packages
	 */
	public RHelpSearchQuery(final int type, final String text, final List<String> fields,
			final List<String> keywords, final List<String> packages, final IREnv rEnv) {
		fSearchType = type;
		fSearchText = text;
		fFields = fields;
		fKeywords = keywords;
		fPackages = packages;
		fREnv = rEnv;
	}
	
	
	public IREnv getREnv() {
		return fREnv;
	}
	
	public int getSearchType() {
		return fSearchType;
	}
	
	public String getSearchString() {
		return fSearchText;
	}
	
	public List<String> getEnabledFields() {
		return fFields;
	}
	
	public List<String> getKeywords() {
		return fKeywords;
	}
	
	public List<String> getPackages() {
		return fPackages;
	}
	
	public RHelpSearchQuery.Compiled compile() throws CoreException {
		final Object compiled = REnvIndexReader.compile(this);
		return new RHelpSearchQuery.Compiled(this, compiled);
	}
	
	
	@Override
	public String toString() {
		return fSearchText;
	}
	
}
