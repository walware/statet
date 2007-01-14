/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License 
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.rserve.internal.launchconfigs;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.DefaultBindSpec;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.ui.StatetImages;


public class RServeClientMainTab extends AbstractLaunchConfigurationTab {

	
	private ConnectionConfig fConnectionConfig;
	
	private Text fServerAddress;
	private Text fServerPort;
	
	
	public RServeClientMainTab() {
		
		fConnectionConfig = new ConnectionConfig();
	}
	
	public void createControl(Composite parent) {
		
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Realm realm = Realm.getDefault();
		if (realm == null) {
			realm = SWTObservables.getRealm(UIAccess.getDisplay());
			Realm.setDefault(realm);
		}
		final DataBindingContext dbc = new DataBindingContext(realm);
		mainComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dbc.dispose();
			}
		});
		
		Layouter main = new Layouter(mainComposite, new GridLayout());
		Layouter layouter = new Layouter(main.addGroup("Connection:"), 2);

		fServerAddress = layouter.addLabeledTextControl("Server Address:");
		dbc.bindValue(SWTObservables.observeText(fServerAddress, SWT.Modify), 
				BeansObservables.observeValue(fConnectionConfig, ConnectionConfig.PROP_SERVERADDRESS), 
				null);
		
		fServerPort = layouter.addLabeledTextControl("Server Port:");
		dbc.bindValue(SWTObservables.observeText(fServerPort, SWT.Modify), 
				BeansObservables.observeValue(fConnectionConfig, ConnectionConfig.PROP_SERVERPORT),
				new DefaultBindSpec()
						.setTargetValidator(new IValidator() {
							public IStatus validate(Object value) {
								try {
									int n = Integer.parseInt((String) value);
									if (n < 0 || n > 65535) {
										return ValidationStatus.error("The valid port range is 0-65535.");
									}
									return Status.OK_STATUS;
								} catch (Throwable t) {
									return ValidationStatus.error("Please enter a number specifing the port");
								}
							}
						})
//						.setTargetUpdatePolicy(DataBindingContext.TIME_EARLY)
				);

		dbc.getValidationStatus().addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				IStatus status = (IStatus) event.getObservableValue().getValue();
				if (!status.isOK()) {
					setErrorMessage(status.getMessage());
				}
				else {
					setErrorMessage(null);
				}
				updateLaunchConfigurationDialog();
			}
		});
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
		ConnectionConfig.writeDefaultsTo(configuration);
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		
		fConnectionConfig.readFrom(configuration);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		
		fConnectionConfig.writeTo(configuration);
	}

	public String getName() {
		
		return "&Main";
	}
	
	public Image getImage() {
		
		return StatetImages.getDefault().getImage(StatetImages.IMG_LAUNCHCONFIG_MAIN);
	}

	
}
