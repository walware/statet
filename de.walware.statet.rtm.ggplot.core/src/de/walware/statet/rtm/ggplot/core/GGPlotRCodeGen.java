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

package de.walware.statet.rtm.ggplot.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.collections.IntArrayMap;
import de.walware.ecommons.collections.IntMap;

import de.walware.statet.rtm.base.core.AbstractRCodeGenerator;
import de.walware.statet.rtm.base.util.RExprTypes;
import de.walware.statet.rtm.ggplot.FacetLayout;
import de.walware.statet.rtm.ggplot.GGPlot;
import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.GGPlotPackage.Literals;
import de.walware.statet.rtm.ggplot.GeomBarLayer;
import de.walware.statet.rtm.ggplot.GeomLineLayer;
import de.walware.statet.rtm.ggplot.GeomPointLayer;
import de.walware.statet.rtm.ggplot.GridFacetLayout;
import de.walware.statet.rtm.ggplot.Layer;
import de.walware.statet.rtm.ggplot.PropDataProvider;
import de.walware.statet.rtm.ggplot.PropStatProvider;
import de.walware.statet.rtm.ggplot.Stat;
import de.walware.statet.rtm.ggplot.SummaryStat;
import de.walware.statet.rtm.ggplot.TextStyle;
import de.walware.statet.rtm.ggplot.WrapFacetLayout;
import de.walware.statet.rtm.ggplot.core.GGPlotRCodeGen.E2R.Property;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


public class GGPlotRCodeGen extends AbstractRCodeGenerator {
	
	/**
	 * Static mapping
	 */
	static class E2R {
		
		static class  Property {
			
			private final EStructuralFeature fEFeature;
			private final String fRArg;
			private final boolean fAllowMapped;
			private final boolean fAllowDirect;
			
			public Property(final EStructuralFeature eFeature, final String rArg,
					final boolean allowMapped, final boolean allowDirect) {
				fEFeature = eFeature;
				fRArg = rArg;
				fAllowMapped = allowMapped;
				fAllowDirect = allowDirect;
			}
			
			
			public boolean allowMapped() {
				return fAllowMapped;
			}
			
			public boolean allowDirect() {
				return fAllowDirect;
			}
			
			public String getRArg() {
				return fRArg;
			}
			
			public EStructuralFeature getEFeature() {
				return fEFeature;
			}
			
		}
		
		private final EClass fEClass;
		private final String fRFun;
		private final List<Property> fProperties;
		
		
		public E2R(final EClass eClass, final String rFun) {
			fEClass = eClass;
			fRFun = rFun;
			
			final List<Property> list = new ArrayList<Property>();
			addProperty(list, Literals.PROP_XVAR_PROVIDER__XVAR, "x"); //$NON-NLS-1$
			addProperty(list, Literals.PROP_YVAR_PROVIDER__YVAR, "y"); //$NON-NLS-1$
			addProperty(list, Literals.GEOM_TEXT_LAYER__LABEL, "label"); //$NON-NLS-1$
			addProperty(list, Literals.GEOM_ABLINE_LAYER__INTERCEPT_VAR, "intercept"); //$NON-NLS-1$
			addProperty(list, Literals.GEOM_ABLINE_LAYER__SLOPE_VAR, "slope"); //$NON-NLS-1$
			addProperty(list, Literals.PROP_GROUP_VAR_PROVIDER__GROUP_VAR, "group"); //$NON-NLS-1$
			addProperty(list, Literals.PROP_SHAPE_PROVIDER__SHAPE, "shape"); //$NON-NLS-1$
			addProperty(list, Literals.PROP_LINE_TYPE_PROVIDER__LINE_TYPE, "linetype"); //$NON-NLS-1$
			addProperty(list, Literals.TEXT_STYLE__FONT_FAMILY, "family"); //$NON-NLS-1$
			addProperty(list, Literals.TEXT_STYLE__FONT_FACE, "face"); //$NON-NLS-1$
			addProperty(list, Literals.PROP_SIZE_PROVIDER__SIZE, "size"); //$NON-NLS-1$
			addProperty(list, Literals.PROP_COLOR_PROVIDER__COLOR, "colour"); //$NON-NLS-1$
			addProperty(list, Literals.PROP_FILL_PROVIDER__FILL, "fill"); //$NON-NLS-1$
			addProperty(list, Literals.TEXT_STYLE__HJUST, "hjust"); //$NON-NLS-1$
			addProperty(list, Literals.TEXT_STYLE__VJUST, "vjust"); //$NON-NLS-1$
			addProperty(list, Literals.TEXT_STYLE__ANGLE, "angle");  //$NON-NLS-1$
			
			fProperties = list;
		}
		
		private void addProperty(final List<Property> list, final EStructuralFeature eFeature, final String rArg) {
			if (!fEClass.getEAllStructuralFeatures().contains(eFeature)) {
				return;
			}
			final RExprTypes types = GGPlotExprTypesProvider.INSTANCE.getTypes(fEClass, eFeature);
			final boolean allowMapped = types.contains(RTypedExpr.MAPPED);
			final boolean allowDirect = types.contains(RTypedExpr.R) || types.contains(RTypedExpr.CHAR);
			list.add(new Property(eFeature, rArg, allowMapped, allowDirect));
		}
		
		public EClass getEClass() {
			return fEClass;
		}
		
		public String getRFun() {
			return fRFun;
		}
		
		public List<Property> getProperties() {
			return fProperties;
		}
		
	}
	
	private static final IntMap<E2R> E2R_PROPERTIES;
	
	static {
		final List<E2R> list = new ArrayList<GGPlotRCodeGen.E2R>();
		list.add(new E2R(Literals.GG_PLOT, "ggplot")); //$NON-NLS-1$
		list.add(new E2R(Literals.GEOM_ABLINE_LAYER, "geom_abline")); //$NON-NLS-1$
		list.add(new E2R(Literals.GEOM_BAR_LAYER, "geom_bar")); //$NON-NLS-1$
		list.add(new E2R(Literals.GEOM_BOXPLOT_LAYER, "geom_boxplot")); //$NON-NLS-1$
		list.add(new E2R(Literals.GEOM_HISTOGRAM_LAYER, "geom_histogram")); //$NON-NLS-1$
		list.add(new E2R(Literals.GEOM_LINE_LAYER, "geom_line")); //$NON-NLS-1$
		list.add(new E2R(Literals.GEOM_POINT_LAYER, "geom_point")); //$NON-NLS-1$
		list.add(new E2R(Literals.GEOM_SMOOTH_LAYER, "geom_smooth")); //$NON-NLS-1$
		list.add(new E2R(Literals.GEOM_TEXT_LAYER, "geom_text")); //$NON-NLS-1$
		list.add(new E2R(Literals.GEOM_TILE_LAYER, "geom_tile")); //$NON-NLS-1$
		list.add(new E2R(Literals.GEOM_VIOLIN_LAYER, "geom_violin")); //$NON-NLS-1$
		list.add(new E2R(Literals.TEXT_STYLE, "text_theme")); //$NON-NLS-1$
		list.add(new E2R(Literals.GRID_FACET_LAYOUT, "facet_grid")); //$NON-NLS-1$
		list.add(new E2R(Literals.WRAP_FACET_LAYOUT, "facet_wrap")); //$NON-NLS-1$
		
		E2R_PROPERTIES = new IntArrayMap<GGPlotRCodeGen.E2R>();
		for (final E2R e2r : list) {
			final int id = e2r.getEClass().getClassifierID();
			E2R_PROPERTIES.put(id, e2r);
		}
	}
	
	private static void appendMappedProperties(final FunBuilder fun, final EObject obj, final List<Property> properties) {
		for (final Property property : properties) {
			if (property.allowMapped()) {
				final Object value = obj.eGet(property.getEFeature());
				fun.appendExpr(property.getRArg(), (RTypedExpr) value, RTypedExpr.MAPPED);
			}
		}
	}
	
	private static final ConstList<String> DIRECT_TYPES = new ConstArrayList<String>(
			RTypedExpr.R,
			RTypedExpr.CHAR );
	
	private static void appendDirectProperties(final FunBuilder fun, final EObject obj, final List<Property> properties) {
		for (final Property property : properties) {
			if (property.allowDirect()) {
				final Object value = obj.eGet(property.getEFeature());
				fun.appendExpr(property.getRArg(), (RTypedExpr) value, DIRECT_TYPES);
			}
		}
	}
	
	
	private final String fPlotVar = "p"; //$NON-NLS-1$
	
	
	@Override
	public void generate(final EObject root) {
		reset();
		addRequirePackage("ggplot2"); //$NON-NLS-1$
		if (root == null) {
			return;
		}
		if (root.eClass().getClassifierID() != GGPlotPackage.GG_PLOT) {
			throw new IllegalArgumentException("root: " + root.eClass().getName()); //$NON-NLS-1$
		}
		appendNewLine();
		genRCode((GGPlot) root);
		
		appendNewLine();
		final FunBuilder printFun = appendFun("print"); //$NON-NLS-1$
		printFun.append(null, fPlotVar);
		printFun.close();
		appendNewLine();
	}
	
	public void genRCode(final GGPlot plot) {
		appendAssign(fPlotVar);
		{	final E2R e2r = E2R_PROPERTIES.get(GGPlotPackage.GG_PLOT);
			final FunBuilder fun = appendFun(e2r.getRFun());
			appendData(fun, plot);
			appendAes(fun, plot, e2r);
			fun.close();
			appendNewLine();
			
			addFacet(plot.getFacet());
			
			addLab("title", plot.getMainTitle(), null, null); //$NON-NLS-1$
			addTheme("plot.title", plot.getMainTitleStyle()); //$NON-NLS-1$
			addLab("x", plot.getAxXLabel(), null, null); //$NON-NLS-1$
			addTheme("axis.title.x ", plot.getAxXLabelStyle()); //$NON-NLS-1$
			addTheme("axis.text.x ", plot.getAxXTextStyle()); //$NON-NLS-1$
			addLab("y", plot.getAxYLabel(), null, null); //$NON-NLS-1$
			addTheme("axis.title.y ", plot.getAxYLabelStyle()); //$NON-NLS-1$
			addTheme("axis.text.y ", plot.getAxYTextStyle()); //$NON-NLS-1$
		}
		
		final EList<Layer> layers = plot.getLayers();
		for (final Layer layer : layers) {
			appendAssign(fPlotVar);
			fBuilder.append(fPlotVar);
			fBuilder.append(" + "); //$NON-NLS-1$
			final E2R e2r = E2R_PROPERTIES.get(layer.eClass().getClassifierID());
			final FunBuilder fun = appendFun(e2r.getRFun());
			appendData(fun, layer);
			appendAes(fun, layer, e2r);
			switch (layer.eClass().getClassifierID()) {
			case GGPlotPackage.GEOM_BAR_LAYER:
				appendStat(fun, (GeomBarLayer) layer);
				break;
			case GGPlotPackage.GEOM_LINE_LAYER:
				appendStat(fun, (GeomLineLayer) layer);
				break;
			case GGPlotPackage.GEOM_POINT_LAYER:
				appendPosition(fun, (GeomPointLayer) layer);
				break;
			}
			fun.close();
			appendNewLine();
		}
	}
	
	private void appendData(final FunBuilder fun, final PropDataProvider obj) {
		if (obj.getData() != null) {
			String expr = obj.getData().getExpr();
			if (obj instanceof GGPlot) {
				final RTypedExpr dataFilter = ((GGPlot) obj).getDataFilter();
				if (dataFilter != null) {
					expr += dataFilter.getExpr();
				}
			}
			fun.append("data", expr); //$NON-NLS-1$
		}
	}
	
	private void appendAes(final FunBuilder fun, final EObject obj, final E2R e2r) {
		final List<Property> aesProperties = e2r.getProperties();
		
		final FunBuilder aesFun = fun.appendFun(null, "aes"); //$NON-NLS-1$
		appendMappedProperties(aesFun, obj, aesProperties);
		aesFun.closeOrRemove();
		
		appendDirectProperties(fun, obj, aesProperties);
	}
	
	private void addFacet(final FacetLayout obj) {
		if (obj == null) {
			return;
		}
		
		appendAssign(fPlotVar);
		fBuilder.append(fPlotVar);
		fBuilder.append(" + "); //$NON-NLS-1$
		
		FunBuilder facetFun;
		switch (obj.eClass().getClassifierID()) {
		case GGPlotPackage.GRID_FACET_LAYOUT: {
			final GridFacetLayout layout = (GridFacetLayout) obj;
			facetFun = appendFun("facet_grid"); //$NON-NLS-1$
			appendExprList(layout.getRowVars(), " + ", "."); //$NON-NLS-1$ //$NON-NLS-2$
			fBuilder.append(" ~ "); //$NON-NLS-1$
			appendExprList(layout.getColVars(), " + ", "."); //$NON-NLS-1$ //$NON-NLS-2$
			break; }
		case GGPlotPackage.WRAP_FACET_LAYOUT: {
			final WrapFacetLayout layout = (WrapFacetLayout) obj;
			facetFun = appendFun("facet_wrap"); //$NON-NLS-1$
			fBuilder.append("~ "); //$NON-NLS-1$
			appendExprList(layout.getColVars(), " + ", "."); //$NON-NLS-1$ //$NON-NLS-2$
			facetFun.appendExpr("ncol", ((WrapFacetLayout) obj).getColNum()); //$NON-NLS-1$
			break; }
		default:
			throw new UnsupportedOperationException(obj.eClass().getName());
		}
		facetFun.close();
		appendNewLine();
	}
	
	private void appendPosition(final FunBuilder fun, final GeomPointLayer obj) {
		RTypedExpr xJitter = obj.getPositionXJitter();
		RTypedExpr yJitter = obj.getPositionYJitter();
		if ((xJitter == null && yJitter == null) ) {
			return;
		}
		if (xJitter == null) {
			xJitter = R_NUM_ZERO_EXPR;
		}
		if (yJitter == null) {
			yJitter = R_NUM_ZERO_EXPR;
		}
		final FunBuilder jitterFun = fun.appendFun("position", "position_jitter"); //$NON-NLS-1$ //$NON-NLS-2$
		jitterFun.appendExpr("w", xJitter); //$NON-NLS-1$
		jitterFun.appendExpr("h", yJitter); //$NON-NLS-1$
		jitterFun.close();
	}
	
	private void appendStat(final FunBuilder fun, final PropStatProvider obj) {
		final Stat stat = obj.getStat();
		if (stat == null) {
			return;
		}
		switch (stat.eClass().getClassifierID()) {
		case GGPlotPackage.IDENTITY_STAT:
			fun.append("stat", "\"identity\""); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		case GGPlotPackage.SUMMARY_STAT:
			fun.append("stat", "\"summary\""); //$NON-NLS-1$ //$NON-NLS-2$
			fun.appendExpr("fun.y", ((SummaryStat) stat).getYFun()); //$NON-NLS-1$
			return;
		}
	}
	
	private void addLab(final String lab, final RTypedExpr text, final String lab2, final RTypedExpr text2) {
		if (text == null && text2 == null) {
			return;
		}
		appendAssign(fPlotVar);
		fBuilder.append(fPlotVar);
		fBuilder.append(" + "); //$NON-NLS-1$
		final FunBuilder labsFun = appendFun("labs"); //$NON-NLS-1$
		labsFun.appendExpr(lab, text, DIRECT_TYPES);
		if (lab2 != null) {
			labsFun.appendExpr(lab2, text2, DIRECT_TYPES);
		}
		labsFun.close();
		appendNewLine();
	}
	
	private void addTheme(final String text, final TextStyle obj) {
		if (obj.getFontFamily() == null && obj.getFontFace() == null
				&& obj.getSize() == null && obj.getColor() == null
				&& obj.getHJust() == null && obj.getVJust() == null && obj.getAngle() == null) {
			return;
		}
		final E2R e2r = E2R_PROPERTIES.get(obj.eClass().getClassifierID());
		
		appendAssign(fPlotVar);
		fBuilder.append(fPlotVar);
		fBuilder.append(" + "); //$NON-NLS-1$
		final FunBuilder themeFun = appendFun("theme"); //$NON-NLS-1$
		final FunBuilder textFun = themeFun.appendFun(text, "element_text"); //$NON-NLS-1$
		appendDirectProperties(textFun, obj, e2r.getProperties());
		textFun.close();
		themeFun.close();
		appendNewLine();
	}
	
}
