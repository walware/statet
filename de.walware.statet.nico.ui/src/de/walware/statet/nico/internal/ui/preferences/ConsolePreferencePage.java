/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.IntegerValidator;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.ui.ColorSelectorObservableValue;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.nico.ui.util.SubmitTypeSelectionComposite;


public class ConsolePreferencePage extends ConfigurationBlockPreferencePage<ConsolePreferenceBlock> {
	
	
	public ConsolePreferencePage() {
	}
	
	
	@Override
	protected ConsolePreferenceBlock createConfigurationBlock() throws CoreException {
		return new ConsolePreferenceBlock(createStatusChangedListener());
	}
	
}

class ConsolePreferenceBlock extends ManagedConfigurationBlock {
	
	
	private Text fCharLimitControl;
	private ColorSelector fColorInfoControl;
	private ColorSelector fColorInputControl;
	private ColorSelector fColorOutputControl;
	private ColorSelector fColorErrorControl;
	
	private SubmitTypeSelectionComposite fSubmitTypeControl;
	
	
	public ConsolePreferenceBlock(final IStatusChangeListener statusListener) {
		super(null, statusListener);
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference<?>, String> prefs = new HashMap<Preference<?>, String>();
		
		prefs.put(ConsolePreferences.PREF_HISTORYNAVIGATION_SUBMIT_TYPES, ConsolePreferences.GROUP_ID);
		prefs.put(ConsolePreferences.PREF_CHARLIMIT, ConsolePreferences.GROUP_ID);
		prefs.put(ConsolePreferences.PREF_COLOR_INFO, ConsolePreferences.GROUP_ID);
		prefs.put(ConsolePreferences.PREF_COLOR_INPUT, ConsolePreferences.GROUP_ID);
		prefs.put(ConsolePreferences.PREF_COLOR_OUTPUT, ConsolePreferences.GROUP_ID);
		prefs.put(ConsolePreferences.PREF_COLOR_ERROR, ConsolePreferences.GROUP_ID);
		
		setupPreferenceManager(prefs);
		
		final Composite appearance = createAppearance(pageComposite);
		appearance.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Composite input = createInputOptions(pageComposite);
		input.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		initBindings();
		updateControls();
	}
	
	private Composite createInputOptions(final Composite pageComposite) {
		final Group group = new Group(pageComposite, SWT.NONE);
		group.setText("Input" + ':');
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		{	final Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			label.setText("Include in history navigation (up/down):");
			
			fSubmitTypeControl = new SubmitTypeSelectionComposite(group);
			fSubmitTypeControl.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
		}
		
		return group;
	}
	
	private Composite createAppearance(final Composite pageComposite) {
		final Group group = new Group(pageComposite, SWT.NONE);
		group.setText("Output" + ':');
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		
		{	final Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			label.setText("Size (characters):");
			final Text text = new Text(group, SWT.BORDER | SWT.RIGHT);
			final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(text, 12);
			text.setLayoutData(gd);
			text.setTextLimit(20);
			fCharLimitControl = text;
		}
		{	final Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			label.setText("Prompt/Info:");
			final ColorSelector selector = new ColorSelector(group);
			selector.getButton().setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
			fColorInfoControl = selector;
		}
		{	final Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			label.setText("Input:");
			final ColorSelector selector = new ColorSelector(group);
			selector.getButton().setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
			fColorInputControl = selector;
		}
		{	final Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			label.setText("Output:");
			final ColorSelector selector = new ColorSelector(group);
			selector.getButton().setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
			fColorOutputControl = selector;
		}
		{	final Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			label.setText("Error:");
			final ColorSelector selector = new ColorSelector(group);
			selector.getButton().setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
			fColorErrorControl = selector;
		}
		
		return group;
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		db.getContext().bindValue(
				fSubmitTypeControl.getObservable(),
				createObservable(ConsolePreferences.PREF_HISTORYNAVIGATION_SUBMIT_TYPES) );
		
		db.getContext().bindValue(
				SWTObservables.observeText(fCharLimitControl, SWT.Modify),
				createObservable(ConsolePreferences.PREF_CHARLIMIT),
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(10000, 1000000000, "Invalid char limit specified (10000-1000000000).")),
				null );
		db.getContext().bindValue(
				new ColorSelectorObservableValue(fColorInfoControl),
				createObservable(ConsolePreferences.PREF_COLOR_INFO) );
		db.getContext().bindValue(
				new ColorSelectorObservableValue(fColorInputControl),
				createObservable(ConsolePreferences.PREF_COLOR_INPUT) );
		db.getContext().bindValue(
				new ColorSelectorObservableValue(fColorOutputControl),
				createObservable(ConsolePreferences.PREF_COLOR_OUTPUT) );
		db.getContext().bindValue(
				new ColorSelectorObservableValue(fColorErrorControl),
				createObservable(ConsolePreferences.PREF_COLOR_ERROR) );
	}
	
}
