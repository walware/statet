/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.databinding;

import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.swt.widgets.Control;

import de.walware.ecommons.ui.util.DialogUtil;


public class SWTMultiEnabledObservable extends AbstractSWTObservableValue {
	
	
	private final Control[] fControls;
	private final List<? extends Control> fExceptions;
	
	
	public SWTMultiEnabledObservable(final Realm realm, final Control[] controls, final List<? extends Control> exceptions) {
		super(realm, controls[0]);
		fControls = controls;
		fExceptions = exceptions;
	}
	
	
	@Override
	protected void doSetValue(final Object value) {
		if (value instanceof Boolean) {
			DialogUtil.setEnabled(fControls, fExceptions, ((Boolean) value).booleanValue());
		}
	}
	
	@Override
	protected Object doGetValue() {
		return null;
	}
	
	public Object getValueType() {
		return null;
	}
	
}
