/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.databinding;

import java.text.NumberFormat;
import java.text.ParsePosition;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * Validator for integers.
 */
public class NumberValidator implements IValidator {

	
	private NumberFormat fFormatter;
	private int fMin;
	private int fMax;
	private String fMessage;
	

	public NumberValidator(int min, int max, String message) {
		fMin = min;
		fMax = max;
		fMessage = message;
		fFormatter = NumberFormat.getIntegerInstance();
		fFormatter.setParseIntegerOnly(true);
	}

	public IStatus validate(Object value) {
		String s = ((String) value).trim();
		ParsePosition result = new ParsePosition(0);
		Number number = fFormatter.parse(s, result);
		if (result.getIndex() == s.length() && result.getErrorIndex() < 0) {
			int n = number.intValue();
			if (n >= fMin && n <= fMax) {
				return Status.OK_STATUS;
			}
			// return range message
		}
		return ValidationStatus.error(fMessage);
	}

}
