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

package de.walware.eclipsecommons.ui.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import de.walware.eclipsecommons.ui.dialogs.Layouter;


public class OverlayStoreConfigurationBlock extends AbstractConfigurationBlock {

	
	protected class FieldManager {

		protected Layouter fLayouter;
		
		/** Maps CheckBoxes to associated PreferenceKey */
		protected Map<Button, String> fCheckBoxes = new HashMap<Button, String>();
		/** Listens on changes of CheckBoxes */
		protected SelectionListener fCheckBoxListener = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				fOverlayStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
			}
		};

		/** Maps TextFields to associated PreferenceKey */
		protected Map<Text, String> fTextFields = new HashMap<Text, String>();
		
		/** Listens on changes of TextFields */
		protected ModifyListener fSimpleTextFieldListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text = (Text) e.widget;
				fOverlayStore.setValue((String) fTextFields.get(text), text.getText());
			}
		};
		
		
		public FieldManager() {
		}
		
		public void setLayouter(Layouter layouter) {
			
			fLayouter = layouter;
		}
		
		public Button addCheckBox(String text, String key) {

			Button button = fLayouter.addCheckBox(text);
			fCheckBoxes.put(button, key);
			button.addSelectionListener(fCheckBoxListener);
			return button;
		}
	}
	
	
	protected OverlayPreferenceStore fOverlayStore;
	private FieldManager fFieldManager;
	
		
/* Managing methods ***********************************************************/
		
	
	public OverlayStoreConfigurationBlock() {

		super();
	}

	protected void setupPreferenceManager(IPreferenceStore store, PreferenceKey[] keys) {
		
		fOverlayStore = new OverlayPreferenceStore(store, keys);
		fOverlayStore.load();
		fOverlayStore.start();
	}
	
	public FieldManager getManager(Layouter layouter) {
		
		if (fFieldManager == null)
			fFieldManager = new FieldManager();
		fFieldManager.setLayouter(layouter);
		return fFieldManager;
	}
	
	@Override
	public void dispose() {
		
		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore= null;
		}
		super.dispose();
	}
	
	
	@Override
	public void performApply() {

		performOk();
	}
	
	@Override
	public boolean performOk() {
		
		if (fOverlayStore != null) {
			fOverlayStore.propagate();
			return true;
		}
		return false;
	}

	public void performDefaults() {
		
		if (fOverlayStore != null) {
			fOverlayStore.loadDefaults();
			updateControls();
		}
	}

	
/* */

	protected void updateControls() {

		if (fFieldManager != null) {
			for (Button b : fFieldManager.fCheckBoxes.keySet()) {
				b.setSelection(fOverlayStore.getBoolean(fFieldManager.fCheckBoxes.get(b)) );
			}
			
			for (Text t : fFieldManager.fTextFields.keySet()) {
				t.setText(fOverlayStore.getString( fFieldManager.fTextFields.get(t)) );
			}
		}		
	}
	
}
