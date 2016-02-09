/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.preferences;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.IntegerValidator;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.ui.NicoUIPreferences;
import de.walware.statet.nico.ui.util.SubmitTypeSelectionComposite;


public class ConsolePreferencePage extends ConfigurationBlockPreferencePage {
	
	
	public ConsolePreferencePage() {
	}
	
	
	@Override
	protected ConfigurationBlock createConfigurationBlock() throws CoreException {
		return new ConsolePreferenceBlock(createStatusChangedListener());
	}
	
}


class ConsolePreferenceBlock extends ManagedConfigurationBlock {
	
	
	private SubmitTypeSelectionComposite inputSubmitTypeControl;
	
	private Text outputCharLimitControl;
	private SubmitTypeSelectionComposite outputSubmitTypeControl;
	
	
	public ConsolePreferenceBlock(final IStatusChangeListener statusListener) {
		super(null, statusListener);
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference<?>, String> prefs= new HashMap<>();
		
		prefs.put(ConsolePreferences.HISTORYNAVIGATION_SUBMIT_TYPES_PREF, ConsolePreferences.GROUP_ID);
		prefs.put(NicoUIPreferences.OUTPUT_CHARLIMIT_PREF, ConsolePreferences.GROUP_ID);
		prefs.put(NicoUIPreferences.OUTPUT_FILTER_SUBMITTYPES_INCLUDE_PREF, ConsolePreferences.GROUP_ID);
		
		setupPreferenceManager(prefs);
		
		final Composite appearance= createOutputOptions(pageComposite);
		appearance.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Composite input= createInputOptions(pageComposite);
		input.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		initBindings();
		updateControls();
	}
	
	private Composite createInputOptions(final Composite parent) {
		final Group group= new Group(parent, SWT.NONE);
		group.setText("Input" + ':');
		group.setLayout(LayoutUtil.createGroupGrid(1));
		
		{	final Label label= new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			label.setText("Include in &history navigation (up/down) input from:");
			
			this.inputSubmitTypeControl= new SubmitTypeSelectionComposite(group);
			this.inputSubmitTypeControl.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
		}
		
		return group;
	}
	
	private Composite createOutputOptions(final Composite parent) {
		final Group group= new Group(parent, SWT.NONE);
		group.setText("Output" + ':');
		group.setLayout(LayoutUtil.createGroupGrid(2));
		
		{	final Label label= new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("S&ize (characters):");
			final Text text= new Text(group, SWT.BORDER | SWT.RIGHT);
			final GridData gd= new GridData(SWT.LEFT, SWT.CENTER, true, false);
			gd.widthHint= LayoutUtil.hintWidth(text, 12);
			text.setLayoutData(gd);
			text.setTextLimit(20);
			this.outputCharLimitControl= text;
		}
		
		LayoutUtil.addSmallFiller(group, false);
		
		{	final Label label= new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			label.setText("&Print content from:");
			
			this.outputSubmitTypeControl= new SubmitTypeSelectionComposite(group);
			this.outputSubmitTypeControl.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 2, 1));
			this.outputSubmitTypeControl.setEditable(EnumSet.of(SubmitType.OTHER));
		}
		
		return group;
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		db.getContext().bindValue(
				WidgetProperties.text(SWT.Modify).observe(this.outputCharLimitControl),
				createObservable(NicoUIPreferences.OUTPUT_CHARLIMIT_PREF),
				new UpdateValueStrategy().setAfterGetValidator(
						new IntegerValidator(100000, 1000000000, "Invalid char limit specified (100000-1000000000).") ),
				null );
		db.getContext().bindValue(
				this.outputSubmitTypeControl.getObservable(),
				createObservable(NicoUIPreferences.OUTPUT_FILTER_SUBMITTYPES_INCLUDE_PREF) );
		
		db.getContext().bindValue(
				this.inputSubmitTypeControl.getObservable(),
				createObservable(ConsolePreferences.HISTORYNAVIGATION_SUBMIT_TYPES_PREF) );
	}
	
}
