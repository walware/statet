/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.datafilterview;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;


public class ConverterValidator implements IValidator {
	
	
	private final IConverter fConverter;
	
	
	public ConverterValidator(final IConverter converter) {
		fConverter = converter;
	}
	
	
	@Override
	public IStatus validate(final Object value) {
		try {
			fConverter.convert(value);
			return ValidationStatus.ok();
		}
		catch (final IllegalArgumentException e) {
			return ValidationStatus.error(e.getLocalizedMessage());
		}
	}
	
}
