/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import net.sourceforge.nattable.data.convert.IDisplayConverter;


public class RDataFormatterConverter implements IDisplayConverter {
	
	
	public static class RowHeader extends RDataFormatterConverter {
		
		public RowHeader(final AbstractRDataProvider<?> dataProvider) {
			super(dataProvider);
		}
		
		@Override
		protected RDataFormatter getFormatter(final RDataTableContentDescription description) {
			return description.rowHeaderColumn.getDefaultFormat();
		}
		
	}
	
	public static class DataColumn extends RDataFormatterConverter {
		
		private final int fColumnIndex;
		
		public DataColumn(final AbstractRDataProvider<?> dataProvider, final int columnIndex) {
			super(dataProvider);
			fColumnIndex = columnIndex;
		}
		
		@Override
		protected RDataFormatter getFormatter(final RDataTableContentDescription description) {
			if (fColumnIndex >= 0 && fColumnIndex < description.dataColumns.length) {
				return description.dataColumns[fColumnIndex].getDefaultFormat();
			}
			return description.defaultDataFormatter;
		}
		
	}
	
	
	private final AbstractRDataProvider<?> fDataProvider;
	
	
	RDataFormatter fFallbackFormatter;
	
	
	public RDataFormatterConverter(final AbstractRDataProvider<?> dataProvider) {
		fDataProvider = dataProvider;
	}
	
	
	private RDataFormatter getFormatter() {
		final RDataTableContentDescription description = fDataProvider.getDescription();
		if (description != null) {
			final RDataFormatter formatter = getFormatter(description);
			if (formatter != null) {
				return formatter;
			}
		}
		if (fFallbackFormatter == null) {
			fFallbackFormatter = new RDataFormatter();
		}
		return fFallbackFormatter;
	}
	
	protected RDataFormatter getFormatter(final RDataTableContentDescription description) {
		return null;
	}
	
	
	public Object canonicalToDisplayValue(final Object canonicalValue) {
		return getFormatter().modelToDisplayValue(canonicalValue);
	}
	
	public Object displayToCanonicalValue(final Object displayValue) {
		throw new UnsupportedOperationException();
	}
	
}
