/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.base.ui.util.ISettingsChangedHandler;


/**
 * AbstractJavaCommentScanner.java
 */
public class CommentScanner extends StatextTextScanner 
		implements ISettingsChangedHandler {

	private static class TaskTagDetector implements IWordDetector {

		public boolean isWordStart(char c) {
			return Character.isLetterOrDigit(c);
		}

		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c);
		}
	}

	private class TaskTagRule extends WordRule {

		private IToken fToken;
		
		public TaskTagRule(IToken token, IToken defaultToken) {
			super(new TaskTagDetector(), defaultToken);
			fToken = token;
		}
	
		public void clearTaskTags() {
			fWords.clear();
		}
	
		public void addTaskTags(String[] tags) {
			for (String tag : tags) {
				addWord(tag, fToken);
			}
		}
	}
	
	
	private TaskTagRule fTaskTagRule;
	
	private String fCommentTokenKey;
	private String fTaskTokenKey;
	
	public CommentScanner(ColorManager colorManager, IPreferenceStore preferenceStore, IPreferenceAccess corePrefs, 
			String commentTokenKey, String taskTokenKey) {
		super(colorManager, preferenceStore);

		fCommentTokenKey = commentTokenKey;
		fTaskTokenKey = taskTokenKey;
		
		initialize();
		loadTaskTags(corePrefs);
	}
	
	protected List<IRule> createRules() {
		List<IRule> list = new ArrayList<IRule>();
		
		IToken defaultToken = getToken(fCommentTokenKey);
		IToken taskToken = getToken(fTaskTokenKey);
		
		// Add rule for Task Tags.
		fTaskTagRule = new TaskTagRule(taskToken, defaultToken);
		list.add(fTaskTagRule);

		setDefaultReturnToken(defaultToken);

		return list;
	}
	
	public boolean handleSettingsChanged(Set<String> contexts, Object options) {
		if (contexts.contains(TaskTagsPreferences.CONTEXT_ID)) {
			IPreferenceAccess prefs = (IPreferenceAccess) options;
			loadTaskTags(prefs);
			return true;
		}
		return false;
	}

	public void loadTaskTags(IPreferenceAccess prefs) {
		fTaskTagRule.clearTaskTags();
		String[] tags = TaskTagsPreferences.loadTagsOnly(prefs);
		if (tags != null) {
			fTaskTagRule.addTaskTags(tags);
		}
	}
}
