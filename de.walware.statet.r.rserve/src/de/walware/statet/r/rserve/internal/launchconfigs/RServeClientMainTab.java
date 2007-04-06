/*******************************************************************************
 * Copyright (c) 2005-2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License 
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.rserve.internal.launchconfigs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParsePosition;

import com.ibm.icu.text.NumberFormat;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.walware.eclipsecommons.ui.dialogs.Layouter;

import de.walware.statet.ui.StatetImages;


/**
 * Tab to configure the connection to the RServe-server.
 */
public class RServeClientMainTab extends AbstractLaunchConfigurationTab {


	private static class PortValidator implements IValidator {
		
		private NumberFormat fFormatter;

		public PortValidator() {
			NumberFormat fFormatter = NumberFormat.getIntegerInstance();
			fFormatter.setParseIntegerOnly(true);
		}
		public IStatus validate(Object value) {
			String s = ((String) value).trim();
			ParsePosition result = new ParsePosition(0);
			int n = fFormatter.parse(s, result).intValue();
			if (result.getIndex() == s.length() && result.getErrorIndex() < 0) {
				if (n >= 0 && n <= 65535) {
					return Status.OK_STATUS;
				}
				return ValidationStatus.error("The valid port range is 0-65535.");
			}
			return ValidationStatus.error("Please enter a integer specifing the port");
		}
	}
	
	
	private ConnectionConfig fConnectionConfig;
	
	private Text fServerAddress;
	private Text fServerPort;
	
	private DataBindingContext fDbc;
	private AggregateValidationStatus fAggregateStatus;
	private IStatus fCurrentStatus;
	
	
	public RServeClientMainTab() {
		
		fConnectionConfig = new ConnectionConfig();
	}
	
	public String getName() {
		
		return "&Main";
	}
	
	public Image getImage() {
		
		return StatetImages.getDefault().getImage(StatetImages.IMG_LAUNCHCONFIG_MAIN);
	}

	public void createControl(Composite parent) {
		
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Layouter main = new Layouter(mainComposite, new GridLayout());
		Layouter layouter = new Layouter(main.addGroup("Connection:"), 2);

		fServerAddress = layouter.addLabeledTextControl("Server Address:");
		fServerPort = layouter.addLabeledTextControl("Server Port:");

		initBindings();

		fConnectionConfig.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	private void initBindings() {

		Realm realm = Realm.getDefault();
		fDbc = new DataBindingContext(realm);

		fDbc.bindValue(SWTObservables.observeText(fServerAddress, SWT.Modify), 
				BeansObservables.observeValue(fConnectionConfig, ConnectionConfig.PROP_SERVERADDRESS), 
				null, null);

		fDbc.bindValue(SWTObservables.observeText(fServerPort, SWT.Modify), 
				BeansObservables.observeValue(fConnectionConfig, ConnectionConfig.PROP_SERVERPORT),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE)
						.setAfterGetValidator(new PortValidator()),
				null);
		
		fAggregateStatus = new AggregateValidationStatus(fDbc.getBindings(),
				AggregateValidationStatus.MAX_SEVERITY);
		fAggregateStatus.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				fCurrentStatus = (IStatus) event.diff.getNewValue();
				if (!fCurrentStatus.isOK()) {
					setErrorMessage(fCurrentStatus.getMessage());
				}
				else {
					setErrorMessage(null);
				}
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	@Override
	public void dispose() {
		
		super.dispose();
		
		if (fAggregateStatus != null) {
			fAggregateStatus.dispose();
			fAggregateStatus = null;
		}
		if (fDbc != null) {
			fDbc.dispose();
			fDbc = null;
		}
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


	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		
		return (fCurrentStatus == null || fCurrentStatus.isOK());
	}
	
	@Override
	public boolean canSave() {

		return (fCurrentStatus != null && fCurrentStatus.isOK());
	}
}
