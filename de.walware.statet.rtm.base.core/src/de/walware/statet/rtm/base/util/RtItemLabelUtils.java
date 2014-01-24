/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.walware.statet.rtm.rtdata.types.RExpr;


public class RtItemLabelUtils {
	
	
	public static class LabelBuilder {
		
		
		private final StringBuilder fBuilder;
		private int fDetail = -1;
		
		public LabelBuilder(final String label) {
			fBuilder = new StringBuilder(label);
		}
		
		
		public void appendDetail(final String label, final RExpr value) {
			if (label != null && value != null) {
				if (fDetail < 0) {
					fDetail = 1;
					fBuilder.append(" ("); //$NON-NLS-1$
				}
				else {
					fBuilder.append(", "); //$NON-NLS-1$
				}
				fBuilder.append(label);
				fBuilder.append(": "); //$NON-NLS-1$
				fBuilder.append(value.getExpr());
			}
		}
		
		public void closeDetail() {
			if (fDetail > 0) {
				fBuilder.append(")"); //$NON-NLS-1$
			}
		}
		
		@Override
		public String toString() {
			return fBuilder.toString();
		}
		
	}
	
	public static class LabelGenerator {
		
		
		private static class Detail {
			
			private final EStructuralFeature fEFeature;
			private final String fLabel;
			
			public Detail(final EStructuralFeature eFeature, final String label) {
				fEFeature = eFeature;
				fLabel = label;
			}
			
		}
		
		
		private final String fBaseLabel;
		
		private final Detail[] fDetails;
		
		
		public LabelGenerator(final ResourceLocator resourceLocator, final EClass eClass, final String[] names) {
			fBaseLabel = resourceLocator.getString(getLabelKey(eClass), true);
			final List<Detail> details = new ArrayList<Detail>();
			for (final String name : names) {
				final EStructuralFeature eFeature = eClass.getEStructuralFeature(name);
				if (eFeature == null) {
					continue;
				}
				final String label = resourceLocator.getString(getLabelKey(eFeature));
				details.add(new Detail(eFeature, label));
			}
			fDetails = details.toArray(new Detail[details.size()]);
		}
		
		
		public String createLabel(final EObject eObj) {
			final LabelBuilder sb = new LabelBuilder(fBaseLabel);
			for (int i = 0; i < fDetails.length; i++) {
				final Object value = eObj.eGet(fDetails[i].fEFeature);
				if (value != null) {
					sb.appendDetail(fDetails[i].fLabel, (RExpr) value);
				}
			}
			sb.closeDetail();
			return sb.toString();
		}
		
	}
	
	public static String getLabelKey(final EClass eClass) {
		return "_UI_" + eClass.getName() + "_type"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static String getLabelKey(final EStructuralFeature eFeature) {
		return "_UI_" + eFeature.getEContainingClass().getName() + //$NON-NLS-1$
				"_" + eFeature.getName() + "_feature"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
