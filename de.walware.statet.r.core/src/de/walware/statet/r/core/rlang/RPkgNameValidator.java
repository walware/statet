/*******************************************************************************
 * Copyright (c) 2011-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rlang;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.r.internal.core.Messages;


public class RPkgNameValidator implements IValidator {
	
	
	private static final String TRANSLATION_PREFIX = "Translation-"; //$NON-NLS-1$
	private static final int TRANSLATION_PREFIX_LENGTH = 12;
	
	
	private boolean fRequired;
	private int fErrorVersion = 14;
	
	
	public RPkgNameValidator() {
		fRequired = true;
	}
	
	
	public void setRequired(final boolean isRequired) {
		fRequired = isRequired;
	}
	
	public void setBackwardCompatible(final int errorVersion) {
		fErrorVersion = errorVersion;
	}
	
	
	@Override
	public IStatus validate(final Object value) {
		final String name = (String) value;
		if (name == null || name.isEmpty()) {
			if (fRequired) {
				return ValidationStatus.error(Messages.RPkgName_Validation_error_Empty_message);
			}
			else {
				return ValidationStatus.ok();
			}
		}
		{	final char c = name.charAt(0);
			if (!Character.isLetter(c)) {
				return ValidationStatus.error(
						NLS.bind(Messages.RPkgName_Validation_error_InvalidFirstChar_message, c) );
			}
			if (c > 127) {
				return ValidationStatus.error(
						NLS.bind(Messages.RPkgName_Validation_error_InvalidNoAscii_message, c) );
			}
		}
		final boolean translation = name.startsWith(TRANSLATION_PREFIX);
		for (int i = translation ? TRANSLATION_PREFIX_LENGTH : 1; i < name.length(); i++) {
			final char c = name.charAt(i);
			if (!(Character.isLetterOrDigit(c) || c == '.')) {
				return ValidationStatus.error(
						NLS.bind(Messages.RPkgName_Validation_error_InvalidChar_message, c) );
			}
			if (c > 127) {
				return ValidationStatus.error(
						NLS.bind(Messages.RPkgName_Validation_error_InvalidNoAscii_message, c) );
			}
		}
		{	final char c = name.charAt(name.length()-1);
			if (c == '.') {
				return ValidationStatus.error(
						NLS.bind(Messages.RPkgName_Validation_error_InvalidDotAtEnd_message, c) );
			}
		}
		if (translation) {
			if (name.length() == TRANSLATION_PREFIX_LENGTH) {
				return ValidationStatus.error(Messages.RPkgName_Validation_error_IncompleteTranslation_message);
			}
		}
		else {
			if (name.length() == 1) {
				return (fErrorVersion >= 14) ?
						ValidationStatus.error(Messages.RPkgName_Validation_error_InvalidSingleChar_message) :
						ValidationStatus.warning(Messages.RPkgName_Validation_error_InvalidSingleChar_message);
			}
		}
		return ValidationStatus.ok();
	}
	
}
