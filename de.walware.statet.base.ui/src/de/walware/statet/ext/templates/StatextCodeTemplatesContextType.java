/*=============================================================================#
 # Copyright (c) 2005-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.ext.templates;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.templates.SourceEditorContextType;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.core.StatetProject;
import de.walware.statet.base.core.preferences.TaskTagsPreferences;


public class StatextCodeTemplatesContextType extends SourceEditorContextType {
	
	public static String getTodoTaskTag(final StatetProject project) {
		final TaskTagsPreferences taskPrefs = (project != null) ?
				new TaskTagsPreferences(project) :
				new TaskTagsPreferences(PreferencesUtil.getInstancePrefs());
		
		final String[] markers = taskPrefs.getTags();
		
		if (markers == null || markers.length == 0) {
			return null;
		}
		return markers[0];
	}
	
	/**
	 * Resolver for ToDo-tags.
	 */
	protected static class StatetTodo extends Todo {
		
		public StatetTodo() {
		}
		
		@Override
		protected String getTag(final ISourceUnit su) {
			final StatetProject project = StatetCore.getStatetProject(su);
			return getTodoTaskTag(project);
		}
		
	}
	
	
	public StatextCodeTemplatesContextType(final String id, final String name) {
		super(id, name);
	}
	
	public StatextCodeTemplatesContextType(final String id) {
		super(id);
	}
	
	
	@Override
	protected void addCommonVariables() {
		super.addCommonVariables();
		addResolver(new Todo());
	}
	
}
