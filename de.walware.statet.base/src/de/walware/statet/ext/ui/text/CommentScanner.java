/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;

import de.walware.eclipsecommon.preferences.CombinedPreferenceStore;
import de.walware.eclipsecommon.ui.util.ColorManager;
import de.walware.statet.base.core.preferences.TaskTagsPreferences;


/**
 * AbstractJavaCommentScanner.java
 */
public class CommentScanner extends StatextTextScanner {

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
	
	public CommentScanner(ColorManager colorManager, CombinedPreferenceStore preferenceStore, 
			String commentTokenKey, String taskTokenKey) {
		
		super(colorManager, preferenceStore);

		fCommentTokenKey = commentTokenKey;
		fTaskTokenKey = taskTokenKey;
		
		initialize();
	}
	
	protected List<IRule> createRules() {
		
		List<IRule> list = new ArrayList<IRule>();
		
		IToken defaultToken = getToken(fCommentTokenKey);
		IToken taskToken = getToken(fTaskTokenKey);
		
		// Add rule for Task Tags.
		fTaskTagRule = new TaskTagRule(taskToken, defaultToken);
		list.add(fTaskTagRule);
		loadTaskTags();

		setDefaultReturnToken(defaultToken);

		return list;
	}
	
	@Override
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		
		super.adaptToPreferenceChange(event);
		
		if (event.getProperty().equals(TaskTagsPreferences.PREF_TAGS.getKey())) {
			loadTaskTags();
		}
	}

	public void loadTaskTags() {
		
		fTaskTagRule.clearTaskTags();
		
		String[] tags = TaskTagsPreferences.loadTagsOnly(fPreferenceStore.getCorePreferences());
		if (tags != null) {
			fTaskTagRule.addTaskTags(tags);
		}
	}
}
