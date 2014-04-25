/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.rhelp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import de.walware.ecommons.collections.CollectionUtils;
import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IREnvHelp;
import de.walware.statet.r.core.rhelp.IRHelpKeyword;
import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.core.rhelp.IRPkgHelp;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery.Compiled;
import de.walware.statet.r.internal.ui.REnvSelectionComposite;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;


public class RHelpSearchInputPage extends DialogPage implements ISearchPage {
	
	
	private static final String PAGE_ID = "RHelpSearchPage"; //$NON-NLS-1$
	
	
	private static String prettyList(final List<String> list) {
		if (list.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		final StringBuilder sb = new StringBuilder(list.size() * 10);
		for (final String s : list) {
			sb.append(s);
			sb.append(", "); //$NON-NLS-1$
		}
		return sb.substring(0, sb.length()-2);
	}
	
	private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[\\,\\;\\s]+"); //$NON-NLS-1$
	
	private static List<String> toList(final String input) {
		final String[] array = SEPARATOR_PATTERN.split(input);
		if (array.length == 1 && array[0].isEmpty()) {
			return CollectionUtils.emptyConstList();
		}
		return new ConstArrayList<String>(array);
	}
	
	private static String[] notNull(final String[] array) {
		return (array != null) ? array : new String[0];
	}
	
	private RHelpSearchQuery loadQuery(final IDialogSettings settings) {
		final int type = settings.getInt("type"); //$NON-NLS-1$
		final String text = settings.get("text"); //$NON-NLS-1$
		final List<String> fields = new ConstArrayList<String>(settings.getArray("fields")); //$NON-NLS-1$
		final List<String> keywords = new ConstArrayList<String>(settings.getArray("keywords")); //$NON-NLS-1$
		final List<String> packages = new ConstArrayList<String>(settings.getArray("packages")); //$NON-NLS-1$
		return new RHelpSearchQuery(type, text, fields, keywords, packages, null);
	}
	
	private void saveQuery(final RHelpSearchQuery query, final IDialogSettings settings) {
		settings.put("type", query.getSearchType()); //$NON-NLS-1$
		settings.put("text", query.getSearchString()); //$NON-NLS-1$
		final List<String> fields = query.getEnabledFields();
		settings.put("fields", fields.toArray(new String[fields.size()])); //$NON-NLS-1$
		final List<String> keywords = query.getKeywords();
		settings.put("keywords", keywords.toArray(new String[keywords.size()])); //$NON-NLS-1$
		final List<String> packages = query.getPackages();
		settings.put("packages", packages.toArray(new String[packages.size()])); //$NON-NLS-1$
	}
	
	
	private ISearchPageContainer fContainer;
	
	private IDialogSettings fDialogSettings;
	private final LinkedHashMap<String, RHelpSearchQuery> fQueryHistory = new LinkedHashMap<String, RHelpSearchQuery>(25);
	
	private Combo fSearchTextControl;
	
	private Button fTypeTopicsControl;
	private Button fTypeFieldsControl;
	private Button fTypeDocControl;
	
	private Button fFieldTitleControl;
	private Button fFieldConceptsControl;
	private Button fFieldAliasControl;
	
	private Combo fKeywordsInputControl;
	private Combo fPackagesInputControl;
	
	private REnvSelectionComposite fREnvControl;
	
	
	public RHelpSearchInputPage() {
	}
	
	
	private IDialogSettings getDialogSettings() {
		if (fDialogSettings == null) {
			fDialogSettings = DialogUtil.getDialogSettings(RUIPlugin.getDefault(), PAGE_ID);
		}
		return fDialogSettings;
	}
	
	@Override
	public void setContainer(final ISearchPageContainer container) {
		fContainer = container;
	}
	
	@Override
	public void createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createTabGrid(1));
		
		final Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		label.setText("Search S&tring:");
		
		fSearchTextControl = new Combo(composite, SWT.DROP_DOWN);
		fSearchTextControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fSearchTextControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final int selectionIdx;
				if (!fSearchTextControl.getListVisible()
						&& (selectionIdx = fSearchTextControl.getSelectionIndex()) >= 0) {
					loadPattern(fQueryHistory.get(fSearchTextControl.getItem(selectionIdx)));
				}
			}
		});
		
		final Composite searchInGroup = createSearchInGroup(composite);
		searchInGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Composite restrictToGroup = createRestrictToGroup(composite);
		restrictToGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Composite scopeGroup = createScopeGroup(composite);
		scopeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IRUIHelpContextIds.R_HELP_SEARCH_PAGE);
		
		loadSettings();
		initSettings();
		fREnvControl.setSetting(RCore.getREnvManager().getDefault());
		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (UIAccess.isOkToUse(fSearchTextControl)) {
					fSearchTextControl.setFocus();
				}
				updateState();
			}
		});
	}
	
	
	private Composite createSearchInGroup(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
		{	final Group group = new Group(composite, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			group.setText("Search in:");
			final GridLayout layout = LayoutUtil.applyGroupDefaults(new GridLayout(), 1);
			group.setLayout(layout);
			
			{	final Button button = new Button(group, SWT.RADIO);
				button.setText("&Topics/Alias (strict)");
				button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				fTypeTopicsControl = button;
			}
			{	final Button button = new Button(group, SWT.RADIO);
				button.setText("Selected &Fields");
				button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				button.setSelection(true);
				fTypeFieldsControl = button;
			}
			{	final Button button = new Button(group, SWT.RADIO);
				button.setText("Complete &Document");
				button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				fTypeDocControl = button;
			}
		}
		
		{	final Group group = new Group(composite, SWT.NONE);
			group.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));
			group.setText("Fields:");
			final GridLayout layout = LayoutUtil.applyGroupDefaults(new GridLayout(), 1);
			group.setLayout(layout);
			{	final Button button = new Button(group, SWT.CHECK);
				button.setText("T&itle");
				button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				button.setSelection(true);
				fFieldTitleControl = button;
			}
			{	final Button button = new Button(group, SWT.CHECK);
				button.setText("Topics/&Alias");
				button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				button.setSelection(true);
				fFieldAliasControl = button;
			}
			{	final Button button = new Button(group, SWT.CHECK);
				button.setText("C&oncepts");
				button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				button.setSelection(true);
				fFieldConceptsControl = button;
			}
		}
		
		fTypeFieldsControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateFieldsState();
			}
		});
		
		return composite;
	}
	
	private void updateFieldsState() {
		final boolean enable = fTypeFieldsControl.getSelection();
		fFieldTitleControl.setEnabled(enable);
		fFieldAliasControl.setEnabled(enable);
		fFieldConceptsControl.setEnabled(enable);
	}
	
	private Composite createRestrictToGroup(final Composite parent) {
		final Group group = new Group(parent, SWT.NONE);
		group.setText("Restrict to:");
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 3));
		
		{	final Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("&Keyword:");
		}
		fKeywordsInputControl = new Combo(group, SWT.DROP_DOWN);
		fKeywordsInputControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		{	final Button button = new Button(group, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			button.setText("Select...");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					List<IRHelpKeyword.Group> keywords = null;
					IREnv rEnv = fREnvControl.getSelection();
					final IRHelpManager rHelpManager = RCore.getRHelpManager();
					if (rEnv != null) {
						final IREnvHelp help = rHelpManager.getHelp(rEnv);
						if (help != null) {
							try {
								keywords = help.getKeywords();
							}
							finally {
								help.unlock();
							}
						}
					}
					if (keywords == null) {
						rEnv = RCore.getREnvManager().getDefault();
						final IREnvHelp help = rHelpManager.getHelp(rEnv);
						if (help != null) {
							try {
								keywords = help.getKeywords();
							}
							finally {
								help.unlock();
							}
						}
					}
					if (keywords != null) {
						final KeywordSelectionDialog dialog = new KeywordSelectionDialog(
								getControl().getShell(), keywords);
						if (dialog.open() == Dialog.OK) {
							final StringBuilder input = new StringBuilder();
							final Object[] result = dialog.getResult();
							for (final Object selectionItem : result) {
								if (selectionItem instanceof IRHelpKeyword) {
									input.append(((IRHelpKeyword) selectionItem).getKeyword());
									break;
								}
							}
							fKeywordsInputControl.setText(input.toString());
						}
					}
				}
			});
		}
		
		{	final Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("&Package:");
		}
		fPackagesInputControl = new Combo(group, SWT.DROP_DOWN);
		fPackagesInputControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		{	final Button button = new Button(group, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			button.setText("Select...");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					List<IRPkgHelp> packages = null;
					IREnv rEnv = fREnvControl.getSelection();
					final IRHelpManager rHelpManager = RCore.getRHelpManager();
					if (rEnv != null) {
						final IREnvHelp help = rHelpManager.getHelp(rEnv);
						if (help != null) {
							packages = help.getRPackages();
							help.unlock();
						}
					}
					if (packages == null) {
						rEnv = RCore.getREnvManager().getDefault();
						final IREnvHelp help = rHelpManager.getHelp(rEnv);
						if (help != null) {
							packages = help.getRPackages();
							help.unlock();
						}
					}
					if (packages != null) {
						final List<String> currentNames = toList(fPackagesInputControl.getText());
						final List<IRPkgHelp> currentPkgs = new ArrayList<IRPkgHelp>(currentNames.size());
						for (final String name : currentNames) {
							for (final IRPkgHelp pkg : packages) {
								if (pkg.getName().equals(name)) {
									currentPkgs.add(pkg);
								}
							}
						}
						final PackageSelectionDialog dialog = new PackageSelectionDialog(
								getControl().getShell(), packages, currentPkgs);
						if (dialog.open() == Dialog.OK) {
							final StringBuilder input = new StringBuilder();
							final IRPkgHelp[] result = dialog.getResult();
							if (result.length > 0) {
								Arrays.sort(result);
								for (final IRPkgHelp pkg : result) {
									input.append(pkg.getName());
									input.append(", "); //$NON-NLS-1$
								}
								fPackagesInputControl.setText(input.substring(0, input.length()-2));
							}
							else {
								fPackagesInputControl.setText(""); //$NON-NLS-1$
							}
						}
					}
				}
			});
		}
		
		return group;
	}
	
	private Composite createScopeGroup(final Composite parent) {
		final Group group = new Group(parent, SWT.NONE);
		group.setText("Scope:");
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		fREnvControl = new REnvSelectionComposite(group);
		fREnvControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fREnvControl.addChangeListener(new REnvSelectionComposite.ChangeListener() {
			@Override
			public void settingChanged(final REnvSelectionComposite source, final String oldValue,
					final String newValue, final IREnv newREnv) {
				updateState();
			}
		});
		
		return group;
	}
	
	private void updateState() {
		final IREnv rEnv = fREnvControl.getSelection();
		if (rEnv == null || rEnv.getConfig() == null
				|| !RCore.getRHelpManager().hasHelp(rEnv)) {
			fContainer.setPerformActionEnabled(false);
			return;
		}
		fContainer.setPerformActionEnabled(true);
	}
	
	private void loadSettings() {
		final IDialogSettings dialogSettings = getDialogSettings();
		
		fKeywordsInputControl.setItems(notNull(dialogSettings.getArray("keywords"))); //$NON-NLS-1$
		fPackagesInputControl.setItems(notNull(dialogSettings.getArray("packages"))); //$NON-NLS-1$
		
		int num = 0;
		final List<String> texts = new ArrayList<String>();
		while (true) {
			final IDialogSettings section = dialogSettings.getSection("searchhist"+(num++)); //$NON-NLS-1$
			if (section != null) {
				final RHelpSearchQuery hist = loadQuery(section);
				texts.add(hist.getSearchString());
				fQueryHistory.put(hist.getSearchString(), hist);
			}
			else {
				break;
			}
		}
		if (!fQueryHistory.isEmpty()) {
			fSearchTextControl.setItems(texts.toArray(new String[texts.size()]));
		}
	}
	
	private void initSettings() {
		final ISelection selection = fContainer.getSelection();
		if (selection instanceof ITextSelection) {
			fSearchTextControl.setText(((ITextSelection) selection).getText());
			return;
		}
		if (selection instanceof IStructuredSelection) {
			final Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement instanceof IModelElement) {
				final IModelElement element = (IModelElement) firstElement;
				if (element.getModelTypeId() == RModel.TYPE_ID
						&& element.getElementName().getSegmentName() != null) {
					fSearchTextControl.setText(element.getElementName().getDisplayName());
					return;
				}
			}
		}
		
		if (!fQueryHistory.isEmpty()) {
			loadPattern(fQueryHistory.values().iterator().next());
			return;
		}
	}
	
	private void saveSettings(final RHelpSearchQuery query) {
		final IDialogSettings dialogSettings = getDialogSettings();
		
		dialogSettings.put("keywords", DialogUtil.combineHistoryItems( //$NON-NLS-1$
				fKeywordsInputControl.getItems(),
				prettyList(query.getKeywords()) ));
		dialogSettings.put("packages", DialogUtil.combineHistoryItems( //$NON-NLS-1$
				fPackagesInputControl.getItems(),
				prettyList(query.getPackages()) ));
		
		fQueryHistory.remove(query.getSearchString());
		int num = 0;
		saveQuery(query, dialogSettings.addNewSection("searchhist"+(num++))); //$NON-NLS-1$
		for (final RHelpSearchQuery hist : fQueryHistory.values()) {
			saveQuery(hist, dialogSettings.addNewSection("searchhist"+(num++))); //$NON-NLS-1$
			if (num >= DialogUtil.HISTORY_MAX) {
				break;
			}
		}
	}
	
	private void loadPattern(final RHelpSearchQuery query) {
		if (query == null) {
			return;
		}
		fSearchTextControl.setText(query.getSearchString());
		fTypeTopicsControl.setSelection(false);
		fTypeFieldsControl.setSelection(false);
		fTypeDocControl.setSelection(false);
		switch (query.getSearchType()) {
		case RHelpSearchQuery.TOPIC_SEARCH:
			fTypeTopicsControl.setSelection(true);
			break;
		case RHelpSearchQuery.FIELD_SEARCH:
			fTypeFieldsControl.setSelection(true);
			break;
		case RHelpSearchQuery.DOC_SEARCH:
			fTypeDocControl.setSelection(true);
			break;
		}
		fFieldAliasControl.setSelection(query.getEnabledFields().contains(RHelpSearchQuery.TOPICS_FIELD));
		fFieldTitleControl.setSelection(query.getEnabledFields().contains(RHelpSearchQuery.TITLE_FIELD));
		fFieldConceptsControl.setSelection(query.getEnabledFields().contains(RHelpSearchQuery.CONCEPTS_FIELD));
		updateFieldsState();
		fKeywordsInputControl.setText(prettyList(query.getKeywords()));
		fPackagesInputControl.setText(prettyList(query.getPackages()));
	}
	
	private RHelpSearchQuery createPattern() {
		int type = 0;
		if (fTypeTopicsControl.getSelection()) {
			type = RHelpSearchQuery.TOPIC_SEARCH;
		}
		else if (fTypeFieldsControl.getSelection()) {
			type = RHelpSearchQuery.FIELD_SEARCH;
		}
		else if (fTypeDocControl.getSelection()) {
			type = RHelpSearchQuery.DOC_SEARCH;
		}
		final String text = fSearchTextControl.getText();
		final List<String> fields = new ArrayList<String>(3);
		if (fFieldAliasControl.getSelection()) {
			fields.add(RHelpSearchQuery.TOPICS_FIELD);
		}
		if (fFieldTitleControl.getSelection()) {
			fields.add(RHelpSearchQuery.TITLE_FIELD);
		}
		if (fFieldConceptsControl.getSelection()) {
			fields.add(RHelpSearchQuery.CONCEPTS_FIELD);
		}
		final List<String> keywords = toList(fKeywordsInputControl.getText());
		final List<String> packages = toList(fPackagesInputControl.getText());
		final IREnv renv = fREnvControl.getSelection();
		return new RHelpSearchQuery(type, text, fields, keywords, packages, renv);
	}
	
	
	@Override
	public boolean performAction() {
		final RHelpSearchQuery query = createPattern();
		if (query.getREnv() == null || query.getREnv().getConfig() == null) {
			return false;
		}
		try {
			final Compiled preparedQuery = query.compile(); // let us show errors directly
			
			saveSettings(query);
			final RHelpSearchUIQuery uiQuery = new RHelpSearchUIQuery(preparedQuery);
			NewSearchUI.runQueryInBackground(uiQuery);
			return true;
		}
		catch (final CoreException e) {
			setErrorMessage(e.getLocalizedMessage());
			return false;
		}
	}
	
}
