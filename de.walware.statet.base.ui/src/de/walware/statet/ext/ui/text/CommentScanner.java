/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.ext.ui.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.text.ui.presentation.AbstractRuleBasedScanner;
import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;
import de.walware.ecommons.ui.ColorManager;
import de.walware.ecommons.ui.ISettingsChangedHandler;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;


/**
 * Scanner for comments. Provides support for task tags.
 */
public class CommentScanner extends AbstractRuleBasedScanner 
		implements ISettingsChangedHandler {
	
	private static class TaskTagDetector implements IWordDetector {
		
		@Override
		public boolean isWordStart(final char c) {
			return Character.isLetterOrDigit(c);
		}
		
		@Override
		public boolean isWordPart(final char c) {
			return Character.isLetterOrDigit(c);
		}
	}
	
	private static class TaskTagRule extends WordRule {
		
		private final IToken fToken;
		
		public TaskTagRule(final IToken token, final IToken defaultToken) {
			super(new TaskTagDetector(), defaultToken);
			fToken = token;
		}
		
		public void clearTaskTags() {
			fWords.clear();
		}
		
		public void addTaskTags(final String[] tags) {
			for (final String tag : tags) {
				addWord(tag, fToken);
			}
		}
	}
	
	
	private TaskTagRule fTaskTagRule;
	
	private final String fCommentTokenKey;
	private final String fTaskTokenKey;
	
	
	public CommentScanner(final ColorManager colorManager, final IPreferenceStore preferenceStore, final IPreferenceAccess corePrefs,
			final String stylesGroupId,
			final String commentTokenKey, final String taskTokenKey) {
		super(colorManager, preferenceStore, stylesGroupId);
		
		fCommentTokenKey = commentTokenKey;
		fTaskTokenKey = taskTokenKey;
		
		initialize();
		loadTaskTags(corePrefs);
	}
	
	
	@Override
	protected List<IRule> createRules() {
		final List<IRule> list = new ArrayList<IRule>();
		
		final IToken defaultToken = getToken(fCommentTokenKey);
		final IToken taskToken = getToken(fTaskTokenKey);
		
		// Add rule for Task Tags.
		fTaskTagRule = new TaskTagRule(taskToken, defaultToken);
		list.add(fTaskTagRule);
		
		setDefaultReturnToken(defaultToken);
		
		return list;
	}
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		super.handleSettingsChanged(groupIds, options);
		if (groupIds.contains(TaskTagsPreferences.GROUP_ID)) {
			final IPreferenceAccess prefs = (IPreferenceAccess) options.get(ISettingsChangedHandler.PREFERENCEACCESS_KEY);
			loadTaskTags(prefs);
			options.put(ITextPresentationConstants.SETTINGSCHANGE_AFFECTSPRESENTATION_KEY, Boolean.TRUE);
		}
	}
	
	public void loadTaskTags(final IPreferenceAccess prefs) {
		fTaskTagRule.clearTaskTags();
		final String[] tags = TaskTagsPreferences.loadTagsOnly(prefs);
		if (tags != null) {
			fTaskTagRule.addTaskTags(tags);
		}
	}
	
}
