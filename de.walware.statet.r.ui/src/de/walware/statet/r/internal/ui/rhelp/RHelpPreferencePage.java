/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.rhelp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.IntegerValidator;
import de.walware.ecommons.databinding.jface.AbstractSWTObservableValue;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.rhelp.IRHelpManager;


public class RHelpPreferencePage extends ConfigurationBlockPreferencePage {
	
	
	public RHelpPreferencePage() {
	}
	
	
	@Override
	protected ConfigurationBlock createConfigurationBlock() throws CoreException {
		return new RHelpConfigurationBlock(createStatusChangedListener());
	}
	
}


class RHelpConfigurationBlock extends ManagedConfigurationBlock {
	
	
	class HomeObservable extends AbstractSWTObservableValue
			implements SelectionListener, ModifyListener {
		
		
		private boolean fIsCustom;
		private String fUrl;
		private String fLastCustom = "http://"; //$NON-NLS-1$
		
		private boolean fIsUpdating;
		
		
		public HomeObservable(final Realm realm) {
			super(realm, fHomeUrlControl);
			fHomeUrlControl.addModifyListener(this);
			fHomeBlankControl.addSelectionListener(this);
			fHomeREnvControl.addSelectionListener(this);
			fHomeCustomControl.addSelectionListener(this);
		}
		
		@Override
		public Object getValueType() {
			return String.class;
		}
		
		@Override
		protected Object doGetValue() {
			if (!fIsUpdating) {
				fIsUpdating = true;
				try {
					updateUrl();
				}
				finally {
					fIsUpdating = false;
				}
			}
			return fUrl;
		}
		
		@Override
		protected void doSetValue(final Object value) {
			fIsUpdating = true;
			fUrl = (String) value;
			if (fUrl == null) {
				fUrl = "about:blank"; //$NON-NLS-1$
			}
			try {
				if (fUrl.equals("about:blank")) { //$NON-NLS-1$
					fHomeBlankControl.setSelection(true);
					fHomeREnvControl.setSelection(false);
					fHomeCustomControl.setSelection(false);
					fIsCustom = false;
				}
				else if (fUrl.equals(IRHelpManager.PORTABLE_DEFAULT_RENV_BROWSE_URL)) {
					fHomeBlankControl.setSelection(false);
					fHomeREnvControl.setSelection(true);
					fHomeCustomControl.setSelection(false);
					fIsCustom = false;
				}
				else {
					fHomeBlankControl.setSelection(false);
					fHomeREnvControl.setSelection(false);
					fHomeCustomControl.setSelection(true);
					fIsCustom = true;
					fLastCustom = fUrl;
				}
				fHomeUrlControl.setText(fUrl);
			}
			finally {
				fIsUpdating = false;
			}
		}
		
		private void updateUrl() {
			final String oldUrl = fUrl;
			if (fHomeBlankControl.getSelection()) {
				if (fIsCustom) {
					fLastCustom = fHomeUrlControl.getText();
				}
				fIsCustom = false;
				fUrl = "about:blank"; //$NON-NLS-1$
			}
			else if (fHomeREnvControl.getSelection()) {
				if (fIsCustom) {
					fLastCustom = fHomeUrlControl.getText();
				}
				fIsCustom = false;
				fUrl = IRHelpManager.PORTABLE_DEFAULT_RENV_BROWSE_URL;
			}
			else {
				fIsCustom = true;
				fUrl = (fLastCustom != null) ? fLastCustom : ""; //$NON-NLS-1$
			}
			if (!fUrl.equals(fHomeUrlControl.getText())) {
				fHomeUrlControl.setText(fUrl);
			}
			if (!fUrl.equals(oldUrl)) {
				fireValueChange(Diffs.createValueDiff(oldUrl, fUrl));
			}
		}
		
		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (fIsUpdating) {
				return;
			}
			fIsUpdating = true;
			try {
				updateUrl();
			}
			finally {
				fIsUpdating = false;
			}
		}
		
		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
		}
		
		@Override
		public void modifyText(final ModifyEvent e) {
			if (fIsUpdating) {
				return;
			}
			fIsUpdating = true;
			try {
				fHomeBlankControl.setSelection(false);
				fHomeREnvControl.setSelection(false);
				fHomeCustomControl.setSelection(true);
				fLastCustom = fHomeUrlControl.getText();
				updateUrl();
			}
			finally {
				fIsUpdating = false;
			}
		}
		
	}
	
	
	private Button fHomeBlankControl;
	private Button fHomeREnvControl;
	private Button fHomeCustomControl;
	private Text fHomeUrlControl;
	
	private Button fSearchReusePageControl;
	private Text fSearchMaxFragmentsControl;
	
	
	protected RHelpConfigurationBlock(final IStatusChangeListener statusListener) {
		super(null, statusListener);
	}
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference<?>, String> prefs= new HashMap<>();
		
		prefs.put(RHelpPreferences.HOMEPAGE_URL_PREF, null);
		prefs.put(RHelpPreferences.SEARCH_REUSE_PAGE_ENABLED_PREF, null);
		prefs.put(RHelpPreferences.SEARCH_PREVIEW_FRAGMENTS_MAX_PREF, null);
		
		setupPreferenceManager(prefs);
		
		final Composite appearanceOptions = createAppearanceOptions(pageComposite);
		appearanceOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Composite homeOptions = createHomeOptions(pageComposite);
		homeOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Composite searchOptions = createSearchOptions(pageComposite);
		searchOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		initBindings();
		updateControls();
	}
	
	private Composite createAppearanceOptions(final Composite pageComposite) {
		final Group group = new Group(pageComposite, SWT.NONE);
		group.setText("Appearance" + ':');
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 3));
		
		final Link control = addLinkControl(group,
				"See also settings for StatET > Documentation in <a href=\"org.eclipse.ui.preferencePages.ColorsAndFonts\">Color and Fonts</a>.");
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		return group;
	}
		
	private Composite createHomeOptions(final Composite pageComposite) {
		final Group group = new Group(pageComposite, SWT.NONE);
		group.setText("Home Page" + ':');
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 3));
		
		{	final Button button = new Button(group, SWT.RADIO);
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			button.setText("&Blank page");
			fHomeBlankControl = button;
		}
		{	final Button button = new Button(group, SWT.RADIO);
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			button.setText("Default &R environment");
			fHomeREnvControl = button;
		}
		{	final Button button = new Button(group, SWT.RADIO);
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			button.setText("C&ustom");
			fHomeCustomControl = button;
		}
		{	final Text text = new Text(group, SWT.BORDER);
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			text.setText(""); //$NON-NLS-1$
			fHomeUrlControl = text;
		}
		
		return group;
	}
	
	private Composite createSearchOptions(final Composite pageComposite) {
		final Group group = new Group(pageComposite, SWT.NONE);
		group.setText("Search" + ':');
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		
		fSearchReusePageControl = new Button(group, SWT.CHECK);
		fSearchReusePageControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		fSearchReusePageControl.setText("Reuse &page in R Help view to show matches.");
		
		{	final Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("Maximum preview &fragments:");
			
			final Text text = new Text(group, SWT.BORDER | SWT.RIGHT);
			final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.widthHint = LayoutUtil.hintWidth(text, 5);
			text.setLayoutData(gd);
			
			fSearchMaxFragmentsControl = text;
		}
		
		return group;
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		db.getContext().bindValue(
				new HomeObservable(db.getRealm()),
				createObservable(RHelpPreferences.HOMEPAGE_URL_PREF) );
		db.getContext().bindValue(
				SWTObservables.observeSelection(fSearchReusePageControl),
				createObservable(RHelpPreferences.SEARCH_REUSE_PAGE_ENABLED_PREF) );
		db.getContext().bindValue(
				SWTObservables.observeText(fSearchMaxFragmentsControl, SWT.Modify),
				createObservable(RHelpPreferences.SEARCH_PREVIEW_FRAGMENTS_MAX_PREF),
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 1000,
						"Invalid maximum for preview fragments specified (1-1000).")),
				null );
	}
	
}
