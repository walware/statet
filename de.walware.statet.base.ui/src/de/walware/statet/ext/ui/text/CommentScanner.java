/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.ext.ui.text;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;

import de.walware.ecommons.preferences.core.IPreferenceAccess;
import de.walware.ecommons.text.ui.presentation.AbstractRuleBasedScanner;
import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;
import de.walware.ecommons.text.ui.settings.TextStyleManager;
import de.walware.ecommons.ui.ISettingsChangedHandler;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;


/**
 * Scanner for comments. Provides support for task tags.
 */
public class CommentScanner extends AbstractRuleBasedScanner implements ISettingsChangedHandler {
	
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
			this.fToken= token;
		}
		
		public void clearTaskTags() {
			this.fWords.clear();
		}
		
		public void addTaskTags(final String[] tags) {
			for (final String tag : tags) {
				addWord(tag, this.fToken);
			}
		}
	}
	
	
	private TaskTagRule taskTagRule;
	
	private final String commentTokenKey;
	private final String taskTokenKey;
	
	
	public CommentScanner(final TextStyleManager textStyles, final String commentTokenKey,
			final String taskTokenKey,
			final IPreferenceAccess corePrefs) {
		super(textStyles);
		
		this.commentTokenKey= commentTokenKey;
		this.taskTokenKey= taskTokenKey;
		
		initRules();
		loadTaskTags(corePrefs);
	}
	
	
	@Override
	protected void createRules(final List<IRule> rules) {
		final IToken defaultToken= getToken(this.commentTokenKey);
		final IToken taskToken= getToken(this.taskTokenKey);
		
		setDefaultReturnToken(defaultToken);
		
		// Add rule for Task Tags.
		this.taskTagRule= new TaskTagRule(taskToken, defaultToken);
		rules.add(this.taskTagRule);
	}
	
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		if (groupIds.contains(TaskTagsPreferences.GROUP_ID)) {
			final IPreferenceAccess prefs= (IPreferenceAccess) options.get(ISettingsChangedHandler.PREFERENCEACCESS_KEY);
			loadTaskTags(prefs);
			options.put(ITextPresentationConstants.SETTINGSCHANGE_AFFECTSPRESENTATION_KEY, Boolean.TRUE);
		}
	}
	
	public void loadTaskTags(final IPreferenceAccess prefs) {
		this.taskTagRule.clearTaskTags();
		final String[] tags= TaskTagsPreferences.loadTagsOnly(prefs);
		if (tags != null) {
			this.taskTagRule.addTaskTags(tags);
		}
	}
	
}
