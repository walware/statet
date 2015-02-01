/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.objectbrowser;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import com.ibm.icu.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.RStore;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;


class SortByTypeComparator extends ViewerComparator implements Comparator<Object> {
	
	
	private final Collator classNameCollator = Collator.getInstance(Locale.ENGLISH);
	
	
	@Override
	public void sort(final Viewer viewer, final Object[] elements) {
		if (elements != null && elements.length > 0 && elements[0] instanceof ICombinedRElement) {
			final ICombinedRElement parent = ((ICombinedRElement) elements[0]).getModelParent();
			if (parent != null && parent.getRObjectType() == RObject.TYPE_ENV) {
				Arrays.sort(elements, this);
			}
		}
	}
	
	@Override
	public int compare(final Object o1, final Object o2) {
		return compare((ICombinedRElement) o1, (ICombinedRElement) o2);
	}
	
	public int compare(final ICombinedRElement e1, final ICombinedRElement e2) {
		{	// By type
			final int cat1 = category(e1);
			final int cat2 = category(e2);
			if (cat1 != cat2) {
				return cat1 - cat2;
			}
			if (cat1 == RObject.TYPE_VECTOR || cat1 == RObject.TYPE_ARRAY) {
				final int d1 = getDataOrder(e1.getData().getStoreType());
				final int d2 = getDataOrder(e2.getData().getStoreType());
				if (d1 != d2) {
					return d1 - d2;
				}
			}
		}
		{	// By classname
			final int diff = this.classNameCollator.compare(e1.getRClassName(), e2.getRClassName());
			if (diff != 0) {
				return diff;
			}
		}
		{	// By index
			final RElementName name1 = e1.getElementName();
			final RElementName name2 = e2.getElementName();
			if (name1 instanceof RElementName.IndexElementName
					&& name2 instanceof RElementName.IndexElementName) {
					// index
				return ((RElementName.IndexElementName) name1).getIndex() -
						((RElementName.IndexElementName) name2).getIndex();
			}
		}
		// By name
		return ObjectBrowserView.ELEMENTNAME_COMPARATOR.compare(e1, e2);
	}
	
	private int getDataOrder(final int dataType) {
		switch (dataType) {
		case RStore.NUMERIC:
			return 1;
		case RStore.COMPLEX:
			return 2;
		case RStore.LOGICAL:
			return 3;
		case RStore.INTEGER:
			return 4;
		case RStore.FACTOR:
			return 4;
		case RStore.CHARACTER:
			return 5;
		case RStore.RAW:
			return 6;
		default:
			return 7;
		}
	}
	
	public int category(final ICombinedRElement element) {
		byte objectType = element.getRObjectType();
		if (objectType == RObject.TYPE_REFERENCE) {
			final RObject realObject = ((RReference) element).getResolvedRObject();
			if (realObject != null) {
				objectType = realObject.getRObjectType();
			}
		}
		if (objectType == RObject.TYPE_ARRAY) {
			objectType = RObject.TYPE_VECTOR;
		}
		return objectType;
	}
	
}
