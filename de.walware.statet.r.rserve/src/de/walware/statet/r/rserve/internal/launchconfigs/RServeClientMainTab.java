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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.beans.BeanObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Integer;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.validation.RegexStringValidator;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersBindingFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.statet.ui.StatetImages;


public class RServeClientMainTab extends AbstractLaunchConfigurationTab {

	
	private ConnectionConfig fConnectionConfig;
	
	private Text fServerAddress;
	private Text fServerPort;
	private Text fSocketTimeout;
	
	
	public RServeClientMainTab() {
		
		fConnectionConfig = new ConnectionConfig();
	}
	
	public void createControl(Composite parent) {
		
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final DataBindingContext dbc = new DataBindingContext();
		dbc.addObservableFactory(new DefaultObservableFactory(dbc));
		dbc.addObservableFactory(new BeanObservableFactory(dbc, null, new Class[]{Widget.class}));
		dbc.addObservableFactory(new SWTObservableFactory());
		dbc.addBindingFactory(new DefaultBindingFactory());
		dbc.addBindingFactory(new ViewersBindingFactory());
		dbc.addBindSupportFactory(new DefaultBindSupportFactory());
		mainComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dbc.dispose();
			}
		});
		
		Layouter main = new Layouter(mainComposite, new GridLayout());
		Layouter layouter = new Layouter(main.addGroup("Connection:"), 2);

		fServerAddress = layouter.addLabeledTextControl("Server Address:");
		dbc.bind(fServerAddress, 
				new Property(fConnectionConfig, ConnectionConfig.PROP_SERVERADDRESS), 
				null);
		
		fServerPort = layouter.addLabeledTextControl("Server Port:");
		dbc.bind(fServerPort, 
				new Property(fConnectionConfig,	ConnectionConfig.PROP_SERVERPORT), 
		        new BindSpec(null, 
		        		new ConvertString2Integer(), 
		        		new RegexStringValidator("^[0-9]*$", "^[0-9]+$", "Please enter a number"), 
		        		null));		

		fSocketTimeout = layouter.addLabeledTextControl("Socket Timeout (ms):");
		dbc.bind(fSocketTimeout, 
				new Property(fConnectionConfig, ConnectionConfig.PROP_SOCKETTIMEOUT), 
		        new BindSpec(null, 
		        		new ConvertString2Integer(),
		        		new RegexStringValidator("^[0-9]*$", "^[0-9]+$", "Please enter a number"),
		        		null));
		
		fConnectionConfig.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				updateLaunchConfigurationDialog();
			};
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
