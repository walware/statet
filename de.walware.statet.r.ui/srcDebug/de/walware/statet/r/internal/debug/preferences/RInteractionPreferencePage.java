/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.preferences;

import static de.walware.statet.r.launching.RCodeLaunchRegistry.PREF_R_CONNECTOR;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommon.preferences.Preference;
import de.walware.eclipsecommon.ui.dialogs.Layouter;
import de.walware.eclipsecommon.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommon.ui.util.PixelConverter;
import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


public class RInteractionPreferencePage extends ConfigurationBlockPreferencePage<RInteractionConfigurationBlock> {

	
	public RInteractionPreferencePage() {
		
		setPreferenceStore(StatetPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.RInteraction_description);
	}
	
	@Override
	protected RInteractionConfigurationBlock createConfigurationBlock() {

		return new RInteractionConfigurationBlock();
	}
	
}

class RInteractionConfigurationBlock extends ManagedConfigurationBlock {


	private RCodeLaunchRegistry.ConnectorConfig[] fConnectors;
	private Combo fConnectorsSelector;
	private Link fConnectorsDescription;
	

	RInteractionConfigurationBlock () {

		super(null);
	}
	
	@Override
	public void createContents(Layouter layouter, IWorkbenchPreferenceContainer container, IPreferenceStore preferenceStore) {

		super.createContents(layouter, container, preferenceStore);
		
		layouter.addFiller();
		createConnectorComponent(layouter, container);
		
		loadValues();
	}

	private void createConnectorComponent(Layouter parent, IWorkbenchPreferenceContainer container) {
		
		fConnectors = RCodeLaunchRegistry.getAvailableConnectors();
		setupPreferenceManager(container, new Preference[] {
				PREF_R_CONNECTOR,
		} );

		String[] connectorLabels = new String[fConnectors.length];
		for (int i = 0; i < fConnectors.length; i++) {
			connectorLabels[i] = fConnectors[i].fName;
		}

		Group group = parent.addGroup(Messages.RInteraction_RConnector);
		Layouter layouter = new Layouter(group, 2);
		
		fConnectorsSelector = layouter.addComboControl(connectorLabels, 2);

		// Description
		layouter.addLabel(Messages.RInteraction_RConnector_Description_label, 0, 1);
		
		ScrolledComposite scrolled = new ScrolledComposite(layouter.fComposite, SWT.V_SCROLL);
		fConnectorsDescription = addLinkControl(scrolled, "");
		scrolled.addControlListener(new ControlListener() {
			public void controlMoved(org.eclipse.swt.events.ControlEvent e) {};
			public void controlResized(org.eclipse.swt.events.ControlEvent e) {
				updateDescriptionSize();
			};
		});

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		PixelConverter pixelConverter = new PixelConverter(fConnectorsDescription);
		gd.horizontalSpan = 1;
		gd.widthHint = pixelConverter.convertWidthInCharsToPixels(40);
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(5);
		scrolled.setLayoutData(gd);
		scrolled.setContent(fConnectorsDescription);
		
		fConnectorsSelector.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				int idx = fConnectorsSelector.getSelectionIndex();
				if (idx >= 0) {
					setPrefValue(PREF_R_CONNECTOR, fConnectors[idx].fId);
					String description = fConnectors[idx].fDescription;
					if (description == null) {
						description = "";
					}
					fConnectorsDescription.setText(description);
					updateDescriptionSize();
				}
			};
		});
	}
	
	private void updateDescriptionSize() {

		Composite scroller = fConnectorsDescription.getParent();
		int widthHint = fConnectorsDescription.getParent().getClientArea().width;
		if (!scroller.getVerticalBar().isVisible())
			widthHint -= scroller.getVerticalBar().getSize().x;
		fConnectorsDescription.setSize(fConnectorsDescription.computeSize(
				widthHint, SWT.DEFAULT));
		
	}
	
	@Override
	protected void updateControls() {
		
		loadValues();
	}
		
	private void loadValues() {

		String selectedConnector = getPreferenceValue(PREF_R_CONNECTOR);

		for (int i = 0; i < fConnectors.length; i++) {
			if (selectedConnector.equals(fConnectors[i].fId))
				fConnectorsSelector.select(i);
		}
	}

}
