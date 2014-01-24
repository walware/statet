/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.ggplot.internal.ui.editors;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.TableWrapData;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.databinding.DetailContext;
import de.walware.ecommons.emf.ui.forms.EFPropertySection;

import de.walware.statet.rtm.ggplot.GGPlotPackage.Literals;
import de.walware.statet.rtm.ggplot.TextStyle;


public abstract class TextStylePropertySection extends EFPropertySection {
	
	
	public static class MainTitle extends TextStylePropertySection {
		
		public MainTitle() {
		}
		
		@Override
		protected EStructuralFeature getFeature() {
			return Literals.GG_PLOT__MAIN_TITLE_STYLE;
		}
		
		@Override
		protected String getLabel() {
			return "the Main Title";
		}
		
	}
	
	public static class AxXLabel extends TextStylePropertySection {
		
		public AxXLabel() {
		}
		
		@Override
		protected EStructuralFeature getFeature() {
			return Literals.GG_PLOT__AX_XLABEL_STYLE;
		}
		
		@Override
		protected String getLabel() {
			return "the Label of the x-Axis";
		}
		
	}
	
	public static class AxYLabel extends TextStylePropertySection {
		
		public AxYLabel() {
		}
		
		@Override
		protected EStructuralFeature getFeature() {
			return Literals.GG_PLOT__AX_YLABEL_STYLE;
		}
		
		@Override
		protected String getLabel() {
			return "the Label of the y-Axis";
		}
		
	}
	
	public static class AxXText extends TextStylePropertySection {
		
		public AxXText() {
		}
		
		@Override
		protected EStructuralFeature getFeature() {
			return Literals.GG_PLOT__AX_XTEXT_STYLE;
		}
		
		@Override
		protected String getLabel() {
			return "the Text of the x-Axis";
		}
		
	}
	
	public static class AxYText extends TextStylePropertySection {
		
		public AxYText() {
		}
		
		@Override
		protected EStructuralFeature getFeature() {
			return Literals.GG_PLOT__AX_YTEXT_STYLE;
		}
		
		@Override
		protected String getLabel() {
			return "the Text of the y-Axis";
		}
		
	}
	
	public static class LayerText extends TextStylePropertySection {
		
		public LayerText() {
		}
		
		@Override
		protected IFilter getFilter() {
			return LayerTextStyleFilter.INSTANCE;
		}
		
		@Override
		protected String getLabel() {
			return "the Layer";
		}
		
	}
	
	
	private TextStyleSection fFormSection;
	
	private IObservableValue fValue;
	
	
	protected TextStylePropertySection() {
	}
	
	
	protected EStructuralFeature getFeature() {
		return null;
	}
	
	protected IFilter getFilter() {
		return null;
	}
	
	protected abstract String getLabel();
	
	@Override
	protected void createContent(final Composite parent) {
		fFormSection = new TextStyleSection(this, parent, getLabel());
//		fFormSection.getSection().setLayoutData(new ColumnLayoutData(300));
		fFormSection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL));
	}
	
	@Override
	protected void initBindings() {
		final IEMFEditContext context = createContext();
		
		fFormSection.addBindings(context);
	}
	
	protected IEMFEditContext createContext() {
		final IEMFEditContext rootContext = getRootContext();
		final IObservableValue observable;
		if (getFeature() != null) {
			observable = EMFProperties.value(getFeature()).observeDetail(rootContext.getBaseObservable());
		}
		else {
			observable = new WritableValue(getEditor().getDataBinding().getRealm(), null, TextStyle.class);
			observable.setValue(getEObject(getSelection()));
			fValue = observable;
		}
		return new DetailContext(rootContext, observable);
	}
	
	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		
		if (fValue != null) {
			fValue.setValue(getEObject(getSelection()));
		}
	}
	
	protected TextStyle getEObject(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			for (final Object element : ((IStructuredSelection) selection).toList()) {
				if (element instanceof TextStyle) {
					if (getFilter() == null || getFilter().select(element)) {
						return (TextStyle) element;
					}
				}
			}
		}
		return null;
	}
	
}
