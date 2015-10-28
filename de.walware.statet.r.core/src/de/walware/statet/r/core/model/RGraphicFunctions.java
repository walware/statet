/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.graphics.core.ColorAlphaDef;
import de.walware.ecommons.graphics.core.ColorDef;
import de.walware.ecommons.graphics.core.HSVColorDef;
import de.walware.ecommons.graphics.core.NamedColorDef;
import de.walware.ecommons.graphics.core.NumberedRefColorDef;

import de.walware.statet.r.core.rsource.IRSourceConstants;
import de.walware.statet.r.core.rsource.ast.FCall;
import de.walware.statet.r.core.rsource.ast.NSGet;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAst.ReadedFCallArgs;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public class RGraphicFunctions {
	
	
	public static final RGraphicFunctions DEFAULT = new RGraphicFunctions();
	
	
	public static final String RGB_NAME = "rgb"; //$NON-NLS-1$
	public final ArgsDefinition RGB_args;
	
	public static final String HSV_NAME = "hsv"; //$NON-NLS-1$
	public final ArgsDefinition HSV_args;
	
	public static final String COLORS_NAME = "colors"; //$NON-NLS-1$
	public static final String COLORS_COLOR_DEF_TYPE = "rgb-rcolors"; //$NON-NLS-1$
	
	public static final String ADJUST_COLOR_NAME = "adjustcolor"; //$NON-NLS-1$
	public final ArgsDefinition ADJUST_COLOR_args;
	
	public static final String PALETTE_NAME = "palette"; //$NON-NLS-1$
	public static final String PALETTE_COLOR_DEF_TYPE = "rgb-rpalette"; //$NON-NLS-1$
	
	
	public static final class RColorsDef extends NamedColorDef {
		
		public RColorsDef(final String name, final int red, final int green, final int blue) {
			super(name, red, green, blue);
		}
		
		@Override
		public String getType() {
			return COLORS_COLOR_DEF_TYPE;
		}
		
	}
	
	public static final class RPaletteDef extends NumberedRefColorDef {
		
		public RPaletteDef(final int number, final ColorDef ref) {
			super(number, ref);
		}
		
		@Override
		public String getType() {
			return PALETTE_COLOR_DEF_TYPE;
		}
		
	}
	
	
	public final Map<String, ? extends NamedColorDef> colorsMap;
	public final List<? extends NamedColorDef> colorsList;
	
	public final List<? extends ColorDef> defaultPalette;
	
	
	protected RGraphicFunctions() {
		this.RGB_args= createRGB();
		this.HSV_args= createHSV();
		this.ADJUST_COLOR_args= createAdjustColor();
		
		{	final Map<String, RColorsDef> map= new LinkedHashMap<>();
			addNamedColors(map);
			this.colorsMap= Collections.unmodifiableMap(map);
			this.colorsList= ImCollections.toList(colorsMap.values());
		}
		{	final List<ColorDef> list= new ArrayList<>();
			addDefaultPaletteColors(list);
			this.defaultPalette= ImCollections.toList(list);
		}
	}
	
	
	protected ArgsDefinition createRGB() {
		return new ArgsDefinition(
				"red", "green", "blue", "alpha", "names", "maxColorValue"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}
	
	protected ArgsDefinition createHSV() {
		return new ArgsDefinition(
				"h", "s", "v", "alpha"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	protected ArgsDefinition createAdjustColor() {
		return new ArgsDefinition(
				"col", "alpha.f", "red.f", "green.f", "blue.f", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"offset", "transform"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
/*
cc <- colors()
for (i in cc) {
	cat("colors.put(\"")
	cat(i)
	cat("\", new NamedRGB(\"")
	cat(i)
	cat("\", ")
	crgb <- col2rgb(i)
	cat(crgb[1])
	cat(", ")
	cat(crgb[2])
	cat(", ")
	cat(crgb[3])
	cat("));\n")
}
*/
	protected void addNamedColors(final Map<String, RColorsDef> colors) {
		colors.put("white", new RColorsDef("white", 255, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("aliceblue", new RColorsDef("aliceblue", 240, 248, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("antiquewhite", new RColorsDef("antiquewhite", 250, 235, 215)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("antiquewhite1", new RColorsDef("antiquewhite1", 255, 239, 219)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("antiquewhite2", new RColorsDef("antiquewhite2", 238, 223, 204)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("antiquewhite3", new RColorsDef("antiquewhite3", 205, 192, 176)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("antiquewhite4", new RColorsDef("antiquewhite4", 139, 131, 120)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("aquamarine", new RColorsDef("aquamarine", 127, 255, 212)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("aquamarine1", new RColorsDef("aquamarine1", 127, 255, 212)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("aquamarine2", new RColorsDef("aquamarine2", 118, 238, 198)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("aquamarine3", new RColorsDef("aquamarine3", 102, 205, 170)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("aquamarine4", new RColorsDef("aquamarine4", 69, 139, 116)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("azure", new RColorsDef("azure", 240, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("azure1", new RColorsDef("azure1", 240, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("azure2", new RColorsDef("azure2", 224, 238, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("azure3", new RColorsDef("azure3", 193, 205, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("azure4", new RColorsDef("azure4", 131, 139, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("beige", new RColorsDef("beige", 245, 245, 220)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("bisque", new RColorsDef("bisque", 255, 228, 196)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("bisque1", new RColorsDef("bisque1", 255, 228, 196)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("bisque2", new RColorsDef("bisque2", 238, 213, 183)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("bisque3", new RColorsDef("bisque3", 205, 183, 158)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("bisque4", new RColorsDef("bisque4", 139, 125, 107)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("black", new RColorsDef("black", 0, 0, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("blanchedalmond", new RColorsDef("blanchedalmond", 255, 235, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("blue", new RColorsDef("blue", 0, 0, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("blue1", new RColorsDef("blue1", 0, 0, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("blue2", new RColorsDef("blue2", 0, 0, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("blue3", new RColorsDef("blue3", 0, 0, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("blue4", new RColorsDef("blue4", 0, 0, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("blueviolet", new RColorsDef("blueviolet", 138, 43, 226)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("brown", new RColorsDef("brown", 165, 42, 42)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("brown1", new RColorsDef("brown1", 255, 64, 64)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("brown2", new RColorsDef("brown2", 238, 59, 59)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("brown3", new RColorsDef("brown3", 205, 51, 51)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("brown4", new RColorsDef("brown4", 139, 35, 35)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("burlywood", new RColorsDef("burlywood", 222, 184, 135)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("burlywood1", new RColorsDef("burlywood1", 255, 211, 155)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("burlywood2", new RColorsDef("burlywood2", 238, 197, 145)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("burlywood3", new RColorsDef("burlywood3", 205, 170, 125)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("burlywood4", new RColorsDef("burlywood4", 139, 115, 85)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cadetblue", new RColorsDef("cadetblue", 95, 158, 160)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cadetblue1", new RColorsDef("cadetblue1", 152, 245, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cadetblue2", new RColorsDef("cadetblue2", 142, 229, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cadetblue3", new RColorsDef("cadetblue3", 122, 197, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cadetblue4", new RColorsDef("cadetblue4", 83, 134, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("chartreuse", new RColorsDef("chartreuse", 127, 255, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("chartreuse1", new RColorsDef("chartreuse1", 127, 255, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("chartreuse2", new RColorsDef("chartreuse2", 118, 238, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("chartreuse3", new RColorsDef("chartreuse3", 102, 205, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("chartreuse4", new RColorsDef("chartreuse4", 69, 139, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("chocolate", new RColorsDef("chocolate", 210, 105, 30)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("chocolate1", new RColorsDef("chocolate1", 255, 127, 36)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("chocolate2", new RColorsDef("chocolate2", 238, 118, 33)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("chocolate3", new RColorsDef("chocolate3", 205, 102, 29)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("chocolate4", new RColorsDef("chocolate4", 139, 69, 19)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("coral", new RColorsDef("coral", 255, 127, 80)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("coral1", new RColorsDef("coral1", 255, 114, 86)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("coral2", new RColorsDef("coral2", 238, 106, 80)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("coral3", new RColorsDef("coral3", 205, 91, 69)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("coral4", new RColorsDef("coral4", 139, 62, 47)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cornflowerblue", new RColorsDef("cornflowerblue", 100, 149, 237)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cornsilk", new RColorsDef("cornsilk", 255, 248, 220)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cornsilk1", new RColorsDef("cornsilk1", 255, 248, 220)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cornsilk2", new RColorsDef("cornsilk2", 238, 232, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cornsilk3", new RColorsDef("cornsilk3", 205, 200, 177)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cornsilk4", new RColorsDef("cornsilk4", 139, 136, 120)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cyan", new RColorsDef("cyan", 0, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cyan1", new RColorsDef("cyan1", 0, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cyan2", new RColorsDef("cyan2", 0, 238, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cyan3", new RColorsDef("cyan3", 0, 205, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("cyan4", new RColorsDef("cyan4", 0, 139, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkblue", new RColorsDef("darkblue", 0, 0, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkcyan", new RColorsDef("darkcyan", 0, 139, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkgoldenrod", new RColorsDef("darkgoldenrod", 184, 134, 11)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkgoldenrod1", new RColorsDef("darkgoldenrod1", 255, 185, 15)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkgoldenrod2", new RColorsDef("darkgoldenrod2", 238, 173, 14)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkgoldenrod3", new RColorsDef("darkgoldenrod3", 205, 149, 12)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkgoldenrod4", new RColorsDef("darkgoldenrod4", 139, 101, 8)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkgray", new RColorsDef("darkgray", 169, 169, 169)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkgreen", new RColorsDef("darkgreen", 0, 100, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkgrey", new RColorsDef("darkgrey", 169, 169, 169)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkkhaki", new RColorsDef("darkkhaki", 189, 183, 107)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkmagenta", new RColorsDef("darkmagenta", 139, 0, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkolivegreen", new RColorsDef("darkolivegreen", 85, 107, 47)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkolivegreen1", new RColorsDef("darkolivegreen1", 202, 255, 112)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkolivegreen2", new RColorsDef("darkolivegreen2", 188, 238, 104)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkolivegreen3", new RColorsDef("darkolivegreen3", 162, 205, 90)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkolivegreen4", new RColorsDef("darkolivegreen4", 110, 139, 61)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkorange", new RColorsDef("darkorange", 255, 140, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkorange1", new RColorsDef("darkorange1", 255, 127, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkorange2", new RColorsDef("darkorange2", 238, 118, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkorange3", new RColorsDef("darkorange3", 205, 102, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkorange4", new RColorsDef("darkorange4", 139, 69, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkorchid", new RColorsDef("darkorchid", 153, 50, 204)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkorchid1", new RColorsDef("darkorchid1", 191, 62, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkorchid2", new RColorsDef("darkorchid2", 178, 58, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkorchid3", new RColorsDef("darkorchid3", 154, 50, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkorchid4", new RColorsDef("darkorchid4", 104, 34, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkred", new RColorsDef("darkred", 139, 0, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darksalmon", new RColorsDef("darksalmon", 233, 150, 122)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkseagreen", new RColorsDef("darkseagreen", 143, 188, 143)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkseagreen1", new RColorsDef("darkseagreen1", 193, 255, 193)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkseagreen2", new RColorsDef("darkseagreen2", 180, 238, 180)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkseagreen3", new RColorsDef("darkseagreen3", 155, 205, 155)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkseagreen4", new RColorsDef("darkseagreen4", 105, 139, 105)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkslateblue", new RColorsDef("darkslateblue", 72, 61, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkslategray", new RColorsDef("darkslategray", 47, 79, 79)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkslategray1", new RColorsDef("darkslategray1", 151, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkslategray2", new RColorsDef("darkslategray2", 141, 238, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkslategray3", new RColorsDef("darkslategray3", 121, 205, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkslategray4", new RColorsDef("darkslategray4", 82, 139, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkslategrey", new RColorsDef("darkslategrey", 47, 79, 79)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkturquoise", new RColorsDef("darkturquoise", 0, 206, 209)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("darkviolet", new RColorsDef("darkviolet", 148, 0, 211)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("deeppink", new RColorsDef("deeppink", 255, 20, 147)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("deeppink1", new RColorsDef("deeppink1", 255, 20, 147)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("deeppink2", new RColorsDef("deeppink2", 238, 18, 137)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("deeppink3", new RColorsDef("deeppink3", 205, 16, 118)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("deeppink4", new RColorsDef("deeppink4", 139, 10, 80)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("deepskyblue", new RColorsDef("deepskyblue", 0, 191, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("deepskyblue1", new RColorsDef("deepskyblue1", 0, 191, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("deepskyblue2", new RColorsDef("deepskyblue2", 0, 178, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("deepskyblue3", new RColorsDef("deepskyblue3", 0, 154, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("deepskyblue4", new RColorsDef("deepskyblue4", 0, 104, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("dimgray", new RColorsDef("dimgray", 105, 105, 105)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("dimgrey", new RColorsDef("dimgrey", 105, 105, 105)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("dodgerblue", new RColorsDef("dodgerblue", 30, 144, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("dodgerblue1", new RColorsDef("dodgerblue1", 30, 144, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("dodgerblue2", new RColorsDef("dodgerblue2", 28, 134, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("dodgerblue3", new RColorsDef("dodgerblue3", 24, 116, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("dodgerblue4", new RColorsDef("dodgerblue4", 16, 78, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("firebrick", new RColorsDef("firebrick", 178, 34, 34)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("firebrick1", new RColorsDef("firebrick1", 255, 48, 48)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("firebrick2", new RColorsDef("firebrick2", 238, 44, 44)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("firebrick3", new RColorsDef("firebrick3", 205, 38, 38)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("firebrick4", new RColorsDef("firebrick4", 139, 26, 26)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("floralwhite", new RColorsDef("floralwhite", 255, 250, 240)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("forestgreen", new RColorsDef("forestgreen", 34, 139, 34)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gainsboro", new RColorsDef("gainsboro", 220, 220, 220)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("ghostwhite", new RColorsDef("ghostwhite", 248, 248, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gold", new RColorsDef("gold", 255, 215, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gold1", new RColorsDef("gold1", 255, 215, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gold2", new RColorsDef("gold2", 238, 201, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gold3", new RColorsDef("gold3", 205, 173, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gold4", new RColorsDef("gold4", 139, 117, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("goldenrod", new RColorsDef("goldenrod", 218, 165, 32)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("goldenrod1", new RColorsDef("goldenrod1", 255, 193, 37)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("goldenrod2", new RColorsDef("goldenrod2", 238, 180, 34)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("goldenrod3", new RColorsDef("goldenrod3", 205, 155, 29)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("goldenrod4", new RColorsDef("goldenrod4", 139, 105, 20)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray", new RColorsDef("gray", 190, 190, 190)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray0", new RColorsDef("gray0", 0, 0, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray1", new RColorsDef("gray1", 3, 3, 3)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray2", new RColorsDef("gray2", 5, 5, 5)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray3", new RColorsDef("gray3", 8, 8, 8)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray4", new RColorsDef("gray4", 10, 10, 10)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray5", new RColorsDef("gray5", 13, 13, 13)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray6", new RColorsDef("gray6", 15, 15, 15)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray7", new RColorsDef("gray7", 18, 18, 18)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray8", new RColorsDef("gray8", 20, 20, 20)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray9", new RColorsDef("gray9", 23, 23, 23)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray10", new RColorsDef("gray10", 26, 26, 26)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray11", new RColorsDef("gray11", 28, 28, 28)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray12", new RColorsDef("gray12", 31, 31, 31)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray13", new RColorsDef("gray13", 33, 33, 33)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray14", new RColorsDef("gray14", 36, 36, 36)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray15", new RColorsDef("gray15", 38, 38, 38)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray16", new RColorsDef("gray16", 41, 41, 41)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray17", new RColorsDef("gray17", 43, 43, 43)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray18", new RColorsDef("gray18", 46, 46, 46)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray19", new RColorsDef("gray19", 48, 48, 48)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray20", new RColorsDef("gray20", 51, 51, 51)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray21", new RColorsDef("gray21", 54, 54, 54)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray22", new RColorsDef("gray22", 56, 56, 56)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray23", new RColorsDef("gray23", 59, 59, 59)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray24", new RColorsDef("gray24", 61, 61, 61)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray25", new RColorsDef("gray25", 64, 64, 64)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray26", new RColorsDef("gray26", 66, 66, 66)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray27", new RColorsDef("gray27", 69, 69, 69)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray28", new RColorsDef("gray28", 71, 71, 71)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray29", new RColorsDef("gray29", 74, 74, 74)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray30", new RColorsDef("gray30", 77, 77, 77)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray31", new RColorsDef("gray31", 79, 79, 79)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray32", new RColorsDef("gray32", 82, 82, 82)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray33", new RColorsDef("gray33", 84, 84, 84)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray34", new RColorsDef("gray34", 87, 87, 87)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray35", new RColorsDef("gray35", 89, 89, 89)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray36", new RColorsDef("gray36", 92, 92, 92)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray37", new RColorsDef("gray37", 94, 94, 94)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray38", new RColorsDef("gray38", 97, 97, 97)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray39", new RColorsDef("gray39", 99, 99, 99)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray40", new RColorsDef("gray40", 102, 102, 102)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray41", new RColorsDef("gray41", 105, 105, 105)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray42", new RColorsDef("gray42", 107, 107, 107)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray43", new RColorsDef("gray43", 110, 110, 110)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray44", new RColorsDef("gray44", 112, 112, 112)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray45", new RColorsDef("gray45", 115, 115, 115)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray46", new RColorsDef("gray46", 117, 117, 117)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray47", new RColorsDef("gray47", 120, 120, 120)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray48", new RColorsDef("gray48", 122, 122, 122)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray49", new RColorsDef("gray49", 125, 125, 125)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray50", new RColorsDef("gray50", 127, 127, 127)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray51", new RColorsDef("gray51", 130, 130, 130)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray52", new RColorsDef("gray52", 133, 133, 133)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray53", new RColorsDef("gray53", 135, 135, 135)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray54", new RColorsDef("gray54", 138, 138, 138)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray55", new RColorsDef("gray55", 140, 140, 140)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray56", new RColorsDef("gray56", 143, 143, 143)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray57", new RColorsDef("gray57", 145, 145, 145)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray58", new RColorsDef("gray58", 148, 148, 148)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray59", new RColorsDef("gray59", 150, 150, 150)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray60", new RColorsDef("gray60", 153, 153, 153)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray61", new RColorsDef("gray61", 156, 156, 156)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray62", new RColorsDef("gray62", 158, 158, 158)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray63", new RColorsDef("gray63", 161, 161, 161)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray64", new RColorsDef("gray64", 163, 163, 163)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray65", new RColorsDef("gray65", 166, 166, 166)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray66", new RColorsDef("gray66", 168, 168, 168)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray67", new RColorsDef("gray67", 171, 171, 171)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray68", new RColorsDef("gray68", 173, 173, 173)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray69", new RColorsDef("gray69", 176, 176, 176)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray70", new RColorsDef("gray70", 179, 179, 179)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray71", new RColorsDef("gray71", 181, 181, 181)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray72", new RColorsDef("gray72", 184, 184, 184)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray73", new RColorsDef("gray73", 186, 186, 186)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray74", new RColorsDef("gray74", 189, 189, 189)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray75", new RColorsDef("gray75", 191, 191, 191)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray76", new RColorsDef("gray76", 194, 194, 194)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray77", new RColorsDef("gray77", 196, 196, 196)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray78", new RColorsDef("gray78", 199, 199, 199)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray79", new RColorsDef("gray79", 201, 201, 201)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray80", new RColorsDef("gray80", 204, 204, 204)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray81", new RColorsDef("gray81", 207, 207, 207)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray82", new RColorsDef("gray82", 209, 209, 209)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray83", new RColorsDef("gray83", 212, 212, 212)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray84", new RColorsDef("gray84", 214, 214, 214)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray85", new RColorsDef("gray85", 217, 217, 217)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray86", new RColorsDef("gray86", 219, 219, 219)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray87", new RColorsDef("gray87", 222, 222, 222)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray88", new RColorsDef("gray88", 224, 224, 224)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray89", new RColorsDef("gray89", 227, 227, 227)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray90", new RColorsDef("gray90", 229, 229, 229)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray91", new RColorsDef("gray91", 232, 232, 232)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray92", new RColorsDef("gray92", 235, 235, 235)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray93", new RColorsDef("gray93", 237, 237, 237)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray94", new RColorsDef("gray94", 240, 240, 240)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray95", new RColorsDef("gray95", 242, 242, 242)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray96", new RColorsDef("gray96", 245, 245, 245)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray97", new RColorsDef("gray97", 247, 247, 247)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray98", new RColorsDef("gray98", 250, 250, 250)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray99", new RColorsDef("gray99", 252, 252, 252)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("gray100", new RColorsDef("gray100", 255, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("green", new RColorsDef("green", 0, 255, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("green1", new RColorsDef("green1", 0, 255, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("green2", new RColorsDef("green2", 0, 238, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("green3", new RColorsDef("green3", 0, 205, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("green4", new RColorsDef("green4", 0, 139, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("greenyellow", new RColorsDef("greenyellow", 173, 255, 47)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey", new RColorsDef("grey", 190, 190, 190)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey0", new RColorsDef("grey0", 0, 0, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey1", new RColorsDef("grey1", 3, 3, 3)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey2", new RColorsDef("grey2", 5, 5, 5)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey3", new RColorsDef("grey3", 8, 8, 8)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey4", new RColorsDef("grey4", 10, 10, 10)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey5", new RColorsDef("grey5", 13, 13, 13)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey6", new RColorsDef("grey6", 15, 15, 15)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey7", new RColorsDef("grey7", 18, 18, 18)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey8", new RColorsDef("grey8", 20, 20, 20)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey9", new RColorsDef("grey9", 23, 23, 23)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey10", new RColorsDef("grey10", 26, 26, 26)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey11", new RColorsDef("grey11", 28, 28, 28)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey12", new RColorsDef("grey12", 31, 31, 31)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey13", new RColorsDef("grey13", 33, 33, 33)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey14", new RColorsDef("grey14", 36, 36, 36)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey15", new RColorsDef("grey15", 38, 38, 38)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey16", new RColorsDef("grey16", 41, 41, 41)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey17", new RColorsDef("grey17", 43, 43, 43)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey18", new RColorsDef("grey18", 46, 46, 46)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey19", new RColorsDef("grey19", 48, 48, 48)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey20", new RColorsDef("grey20", 51, 51, 51)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey21", new RColorsDef("grey21", 54, 54, 54)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey22", new RColorsDef("grey22", 56, 56, 56)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey23", new RColorsDef("grey23", 59, 59, 59)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey24", new RColorsDef("grey24", 61, 61, 61)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey25", new RColorsDef("grey25", 64, 64, 64)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey26", new RColorsDef("grey26", 66, 66, 66)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey27", new RColorsDef("grey27", 69, 69, 69)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey28", new RColorsDef("grey28", 71, 71, 71)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey29", new RColorsDef("grey29", 74, 74, 74)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey30", new RColorsDef("grey30", 77, 77, 77)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey31", new RColorsDef("grey31", 79, 79, 79)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey32", new RColorsDef("grey32", 82, 82, 82)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey33", new RColorsDef("grey33", 84, 84, 84)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey34", new RColorsDef("grey34", 87, 87, 87)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey35", new RColorsDef("grey35", 89, 89, 89)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey36", new RColorsDef("grey36", 92, 92, 92)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey37", new RColorsDef("grey37", 94, 94, 94)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey38", new RColorsDef("grey38", 97, 97, 97)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey39", new RColorsDef("grey39", 99, 99, 99)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey40", new RColorsDef("grey40", 102, 102, 102)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey41", new RColorsDef("grey41", 105, 105, 105)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey42", new RColorsDef("grey42", 107, 107, 107)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey43", new RColorsDef("grey43", 110, 110, 110)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey44", new RColorsDef("grey44", 112, 112, 112)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey45", new RColorsDef("grey45", 115, 115, 115)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey46", new RColorsDef("grey46", 117, 117, 117)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey47", new RColorsDef("grey47", 120, 120, 120)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey48", new RColorsDef("grey48", 122, 122, 122)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey49", new RColorsDef("grey49", 125, 125, 125)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey50", new RColorsDef("grey50", 127, 127, 127)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey51", new RColorsDef("grey51", 130, 130, 130)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey52", new RColorsDef("grey52", 133, 133, 133)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey53", new RColorsDef("grey53", 135, 135, 135)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey54", new RColorsDef("grey54", 138, 138, 138)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey55", new RColorsDef("grey55", 140, 140, 140)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey56", new RColorsDef("grey56", 143, 143, 143)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey57", new RColorsDef("grey57", 145, 145, 145)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey58", new RColorsDef("grey58", 148, 148, 148)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey59", new RColorsDef("grey59", 150, 150, 150)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey60", new RColorsDef("grey60", 153, 153, 153)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey61", new RColorsDef("grey61", 156, 156, 156)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey62", new RColorsDef("grey62", 158, 158, 158)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey63", new RColorsDef("grey63", 161, 161, 161)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey64", new RColorsDef("grey64", 163, 163, 163)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey65", new RColorsDef("grey65", 166, 166, 166)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey66", new RColorsDef("grey66", 168, 168, 168)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey67", new RColorsDef("grey67", 171, 171, 171)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey68", new RColorsDef("grey68", 173, 173, 173)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey69", new RColorsDef("grey69", 176, 176, 176)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey70", new RColorsDef("grey70", 179, 179, 179)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey71", new RColorsDef("grey71", 181, 181, 181)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey72", new RColorsDef("grey72", 184, 184, 184)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey73", new RColorsDef("grey73", 186, 186, 186)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey74", new RColorsDef("grey74", 189, 189, 189)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey75", new RColorsDef("grey75", 191, 191, 191)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey76", new RColorsDef("grey76", 194, 194, 194)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey77", new RColorsDef("grey77", 196, 196, 196)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey78", new RColorsDef("grey78", 199, 199, 199)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey79", new RColorsDef("grey79", 201, 201, 201)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey80", new RColorsDef("grey80", 204, 204, 204)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey81", new RColorsDef("grey81", 207, 207, 207)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey82", new RColorsDef("grey82", 209, 209, 209)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey83", new RColorsDef("grey83", 212, 212, 212)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey84", new RColorsDef("grey84", 214, 214, 214)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey85", new RColorsDef("grey85", 217, 217, 217)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey86", new RColorsDef("grey86", 219, 219, 219)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey87", new RColorsDef("grey87", 222, 222, 222)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey88", new RColorsDef("grey88", 224, 224, 224)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey89", new RColorsDef("grey89", 227, 227, 227)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey90", new RColorsDef("grey90", 229, 229, 229)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey91", new RColorsDef("grey91", 232, 232, 232)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey92", new RColorsDef("grey92", 235, 235, 235)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey93", new RColorsDef("grey93", 237, 237, 237)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey94", new RColorsDef("grey94", 240, 240, 240)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey95", new RColorsDef("grey95", 242, 242, 242)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey96", new RColorsDef("grey96", 245, 245, 245)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey97", new RColorsDef("grey97", 247, 247, 247)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey98", new RColorsDef("grey98", 250, 250, 250)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey99", new RColorsDef("grey99", 252, 252, 252)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("grey100", new RColorsDef("grey100", 255, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("honeydew", new RColorsDef("honeydew", 240, 255, 240)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("honeydew1", new RColorsDef("honeydew1", 240, 255, 240)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("honeydew2", new RColorsDef("honeydew2", 224, 238, 224)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("honeydew3", new RColorsDef("honeydew3", 193, 205, 193)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("honeydew4", new RColorsDef("honeydew4", 131, 139, 131)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("hotpink", new RColorsDef("hotpink", 255, 105, 180)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("hotpink1", new RColorsDef("hotpink1", 255, 110, 180)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("hotpink2", new RColorsDef("hotpink2", 238, 106, 167)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("hotpink3", new RColorsDef("hotpink3", 205, 96, 144)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("hotpink4", new RColorsDef("hotpink4", 139, 58, 98)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("indianred", new RColorsDef("indianred", 205, 92, 92)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("indianred1", new RColorsDef("indianred1", 255, 106, 106)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("indianred2", new RColorsDef("indianred2", 238, 99, 99)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("indianred3", new RColorsDef("indianred3", 205, 85, 85)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("indianred4", new RColorsDef("indianred4", 139, 58, 58)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("ivory", new RColorsDef("ivory", 255, 255, 240)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("ivory1", new RColorsDef("ivory1", 255, 255, 240)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("ivory2", new RColorsDef("ivory2", 238, 238, 224)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("ivory3", new RColorsDef("ivory3", 205, 205, 193)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("ivory4", new RColorsDef("ivory4", 139, 139, 131)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("khaki", new RColorsDef("khaki", 240, 230, 140)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("khaki1", new RColorsDef("khaki1", 255, 246, 143)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("khaki2", new RColorsDef("khaki2", 238, 230, 133)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("khaki3", new RColorsDef("khaki3", 205, 198, 115)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("khaki4", new RColorsDef("khaki4", 139, 134, 78)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lavender", new RColorsDef("lavender", 230, 230, 250)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lavenderblush", new RColorsDef("lavenderblush", 255, 240, 245)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lavenderblush1", new RColorsDef("lavenderblush1", 255, 240, 245)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lavenderblush2", new RColorsDef("lavenderblush2", 238, 224, 229)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lavenderblush3", new RColorsDef("lavenderblush3", 205, 193, 197)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lavenderblush4", new RColorsDef("lavenderblush4", 139, 131, 134)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lawngreen", new RColorsDef("lawngreen", 124, 252, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lemonchiffon", new RColorsDef("lemonchiffon", 255, 250, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lemonchiffon1", new RColorsDef("lemonchiffon1", 255, 250, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lemonchiffon2", new RColorsDef("lemonchiffon2", 238, 233, 191)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lemonchiffon3", new RColorsDef("lemonchiffon3", 205, 201, 165)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lemonchiffon4", new RColorsDef("lemonchiffon4", 139, 137, 112)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightblue", new RColorsDef("lightblue", 173, 216, 230)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightblue1", new RColorsDef("lightblue1", 191, 239, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightblue2", new RColorsDef("lightblue2", 178, 223, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightblue3", new RColorsDef("lightblue3", 154, 192, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightblue4", new RColorsDef("lightblue4", 104, 131, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightcoral", new RColorsDef("lightcoral", 240, 128, 128)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightcyan", new RColorsDef("lightcyan", 224, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightcyan1", new RColorsDef("lightcyan1", 224, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightcyan2", new RColorsDef("lightcyan2", 209, 238, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightcyan3", new RColorsDef("lightcyan3", 180, 205, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightcyan4", new RColorsDef("lightcyan4", 122, 139, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightgoldenrod", new RColorsDef("lightgoldenrod", 238, 221, 130)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightgoldenrod1", new RColorsDef("lightgoldenrod1", 255, 236, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightgoldenrod2", new RColorsDef("lightgoldenrod2", 238, 220, 130)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightgoldenrod3", new RColorsDef("lightgoldenrod3", 205, 190, 112)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightgoldenrod4", new RColorsDef("lightgoldenrod4", 139, 129, 76)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightgoldenrodyellow", new RColorsDef("lightgoldenrodyellow", 250, 250, 210)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightgray", new RColorsDef("lightgray", 211, 211, 211)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightgreen", new RColorsDef("lightgreen", 144, 238, 144)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightgrey", new RColorsDef("lightgrey", 211, 211, 211)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightpink", new RColorsDef("lightpink", 255, 182, 193)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightpink1", new RColorsDef("lightpink1", 255, 174, 185)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightpink2", new RColorsDef("lightpink2", 238, 162, 173)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightpink3", new RColorsDef("lightpink3", 205, 140, 149)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightpink4", new RColorsDef("lightpink4", 139, 95, 101)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightsalmon", new RColorsDef("lightsalmon", 255, 160, 122)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightsalmon1", new RColorsDef("lightsalmon1", 255, 160, 122)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightsalmon2", new RColorsDef("lightsalmon2", 238, 149, 114)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightsalmon3", new RColorsDef("lightsalmon3", 205, 129, 98)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightsalmon4", new RColorsDef("lightsalmon4", 139, 87, 66)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightseagreen", new RColorsDef("lightseagreen", 32, 178, 170)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightskyblue", new RColorsDef("lightskyblue", 135, 206, 250)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightskyblue1", new RColorsDef("lightskyblue1", 176, 226, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightskyblue2", new RColorsDef("lightskyblue2", 164, 211, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightskyblue3", new RColorsDef("lightskyblue3", 141, 182, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightskyblue4", new RColorsDef("lightskyblue4", 96, 123, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightslateblue", new RColorsDef("lightslateblue", 132, 112, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightslategray", new RColorsDef("lightslategray", 119, 136, 153)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightslategrey", new RColorsDef("lightslategrey", 119, 136, 153)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightsteelblue", new RColorsDef("lightsteelblue", 176, 196, 222)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightsteelblue1", new RColorsDef("lightsteelblue1", 202, 225, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightsteelblue2", new RColorsDef("lightsteelblue2", 188, 210, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightsteelblue3", new RColorsDef("lightsteelblue3", 162, 181, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightsteelblue4", new RColorsDef("lightsteelblue4", 110, 123, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightyellow", new RColorsDef("lightyellow", 255, 255, 224)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightyellow1", new RColorsDef("lightyellow1", 255, 255, 224)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightyellow2", new RColorsDef("lightyellow2", 238, 238, 209)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightyellow3", new RColorsDef("lightyellow3", 205, 205, 180)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("lightyellow4", new RColorsDef("lightyellow4", 139, 139, 122)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("limegreen", new RColorsDef("limegreen", 50, 205, 50)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("linen", new RColorsDef("linen", 250, 240, 230)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("magenta", new RColorsDef("magenta", 255, 0, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("magenta1", new RColorsDef("magenta1", 255, 0, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("magenta2", new RColorsDef("magenta2", 238, 0, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("magenta3", new RColorsDef("magenta3", 205, 0, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("magenta4", new RColorsDef("magenta4", 139, 0, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("maroon", new RColorsDef("maroon", 176, 48, 96)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("maroon1", new RColorsDef("maroon1", 255, 52, 179)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("maroon2", new RColorsDef("maroon2", 238, 48, 167)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("maroon3", new RColorsDef("maroon3", 205, 41, 144)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("maroon4", new RColorsDef("maroon4", 139, 28, 98)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumaquamarine", new RColorsDef("mediumaquamarine", 102, 205, 170)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumblue", new RColorsDef("mediumblue", 0, 0, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumorchid", new RColorsDef("mediumorchid", 186, 85, 211)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumorchid1", new RColorsDef("mediumorchid1", 224, 102, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumorchid2", new RColorsDef("mediumorchid2", 209, 95, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumorchid3", new RColorsDef("mediumorchid3", 180, 82, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumorchid4", new RColorsDef("mediumorchid4", 122, 55, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumpurple", new RColorsDef("mediumpurple", 147, 112, 219)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumpurple1", new RColorsDef("mediumpurple1", 171, 130, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumpurple2", new RColorsDef("mediumpurple2", 159, 121, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumpurple3", new RColorsDef("mediumpurple3", 137, 104, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumpurple4", new RColorsDef("mediumpurple4", 93, 71, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumseagreen", new RColorsDef("mediumseagreen", 60, 179, 113)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumslateblue", new RColorsDef("mediumslateblue", 123, 104, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumspringgreen", new RColorsDef("mediumspringgreen", 0, 250, 154)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumturquoise", new RColorsDef("mediumturquoise", 72, 209, 204)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mediumvioletred", new RColorsDef("mediumvioletred", 199, 21, 133)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("midnightblue", new RColorsDef("midnightblue", 25, 25, 112)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mintcream", new RColorsDef("mintcream", 245, 255, 250)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mistyrose", new RColorsDef("mistyrose", 255, 228, 225)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mistyrose1", new RColorsDef("mistyrose1", 255, 228, 225)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mistyrose2", new RColorsDef("mistyrose2", 238, 213, 210)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mistyrose3", new RColorsDef("mistyrose3", 205, 183, 181)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("mistyrose4", new RColorsDef("mistyrose4", 139, 125, 123)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("moccasin", new RColorsDef("moccasin", 255, 228, 181)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("navajowhite", new RColorsDef("navajowhite", 255, 222, 173)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("navajowhite1", new RColorsDef("navajowhite1", 255, 222, 173)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("navajowhite2", new RColorsDef("navajowhite2", 238, 207, 161)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("navajowhite3", new RColorsDef("navajowhite3", 205, 179, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("navajowhite4", new RColorsDef("navajowhite4", 139, 121, 94)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("navy", new RColorsDef("navy", 0, 0, 128)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("navyblue", new RColorsDef("navyblue", 0, 0, 128)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("oldlace", new RColorsDef("oldlace", 253, 245, 230)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("olivedrab", new RColorsDef("olivedrab", 107, 142, 35)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("olivedrab1", new RColorsDef("olivedrab1", 192, 255, 62)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("olivedrab2", new RColorsDef("olivedrab2", 179, 238, 58)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("olivedrab3", new RColorsDef("olivedrab3", 154, 205, 50)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("olivedrab4", new RColorsDef("olivedrab4", 105, 139, 34)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orange", new RColorsDef("orange", 255, 165, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orange1", new RColorsDef("orange1", 255, 165, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orange2", new RColorsDef("orange2", 238, 154, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orange3", new RColorsDef("orange3", 205, 133, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orange4", new RColorsDef("orange4", 139, 90, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orangered", new RColorsDef("orangered", 255, 69, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orangered1", new RColorsDef("orangered1", 255, 69, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orangered2", new RColorsDef("orangered2", 238, 64, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orangered3", new RColorsDef("orangered3", 205, 55, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orangered4", new RColorsDef("orangered4", 139, 37, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orchid", new RColorsDef("orchid", 218, 112, 214)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orchid1", new RColorsDef("orchid1", 255, 131, 250)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orchid2", new RColorsDef("orchid2", 238, 122, 233)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orchid3", new RColorsDef("orchid3", 205, 105, 201)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("orchid4", new RColorsDef("orchid4", 139, 71, 137)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palegoldenrod", new RColorsDef("palegoldenrod", 238, 232, 170)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palegreen", new RColorsDef("palegreen", 152, 251, 152)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palegreen1", new RColorsDef("palegreen1", 154, 255, 154)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palegreen2", new RColorsDef("palegreen2", 144, 238, 144)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palegreen3", new RColorsDef("palegreen3", 124, 205, 124)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palegreen4", new RColorsDef("palegreen4", 84, 139, 84)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("paleturquoise", new RColorsDef("paleturquoise", 175, 238, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("paleturquoise1", new RColorsDef("paleturquoise1", 187, 255, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("paleturquoise2", new RColorsDef("paleturquoise2", 174, 238, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("paleturquoise3", new RColorsDef("paleturquoise3", 150, 205, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("paleturquoise4", new RColorsDef("paleturquoise4", 102, 139, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palevioletred", new RColorsDef("palevioletred", 219, 112, 147)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palevioletred1", new RColorsDef("palevioletred1", 255, 130, 171)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palevioletred2", new RColorsDef("palevioletred2", 238, 121, 159)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palevioletred3", new RColorsDef("palevioletred3", 205, 104, 137)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("palevioletred4", new RColorsDef("palevioletred4", 139, 71, 93)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("papayawhip", new RColorsDef("papayawhip", 255, 239, 213)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("peachpuff", new RColorsDef("peachpuff", 255, 218, 185)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("peachpuff1", new RColorsDef("peachpuff1", 255, 218, 185)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("peachpuff2", new RColorsDef("peachpuff2", 238, 203, 173)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("peachpuff3", new RColorsDef("peachpuff3", 205, 175, 149)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("peachpuff4", new RColorsDef("peachpuff4", 139, 119, 101)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("peru", new RColorsDef("peru", 205, 133, 63)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("pink", new RColorsDef("pink", 255, 192, 203)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("pink1", new RColorsDef("pink1", 255, 181, 197)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("pink2", new RColorsDef("pink2", 238, 169, 184)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("pink3", new RColorsDef("pink3", 205, 145, 158)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("pink4", new RColorsDef("pink4", 139, 99, 108)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("plum", new RColorsDef("plum", 221, 160, 221)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("plum1", new RColorsDef("plum1", 255, 187, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("plum2", new RColorsDef("plum2", 238, 174, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("plum3", new RColorsDef("plum3", 205, 150, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("plum4", new RColorsDef("plum4", 139, 102, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("powderblue", new RColorsDef("powderblue", 176, 224, 230)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("purple", new RColorsDef("purple", 160, 32, 240)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("purple1", new RColorsDef("purple1", 155, 48, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("purple2", new RColorsDef("purple2", 145, 44, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("purple3", new RColorsDef("purple3", 125, 38, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("purple4", new RColorsDef("purple4", 85, 26, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("red", new RColorsDef("red", 255, 0, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("red1", new RColorsDef("red1", 255, 0, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("red2", new RColorsDef("red2", 238, 0, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("red3", new RColorsDef("red3", 205, 0, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("red4", new RColorsDef("red4", 139, 0, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("rosybrown", new RColorsDef("rosybrown", 188, 143, 143)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("rosybrown1", new RColorsDef("rosybrown1", 255, 193, 193)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("rosybrown2", new RColorsDef("rosybrown2", 238, 180, 180)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("rosybrown3", new RColorsDef("rosybrown3", 205, 155, 155)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("rosybrown4", new RColorsDef("rosybrown4", 139, 105, 105)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("royalblue", new RColorsDef("royalblue", 65, 105, 225)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("royalblue1", new RColorsDef("royalblue1", 72, 118, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("royalblue2", new RColorsDef("royalblue2", 67, 110, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("royalblue3", new RColorsDef("royalblue3", 58, 95, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("royalblue4", new RColorsDef("royalblue4", 39, 64, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("saddlebrown", new RColorsDef("saddlebrown", 139, 69, 19)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("salmon", new RColorsDef("salmon", 250, 128, 114)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("salmon1", new RColorsDef("salmon1", 255, 140, 105)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("salmon2", new RColorsDef("salmon2", 238, 130, 98)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("salmon3", new RColorsDef("salmon3", 205, 112, 84)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("salmon4", new RColorsDef("salmon4", 139, 76, 57)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("sandybrown", new RColorsDef("sandybrown", 244, 164, 96)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("seagreen", new RColorsDef("seagreen", 46, 139, 87)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("seagreen1", new RColorsDef("seagreen1", 84, 255, 159)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("seagreen2", new RColorsDef("seagreen2", 78, 238, 148)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("seagreen3", new RColorsDef("seagreen3", 67, 205, 128)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("seagreen4", new RColorsDef("seagreen4", 46, 139, 87)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("seashell", new RColorsDef("seashell", 255, 245, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("seashell1", new RColorsDef("seashell1", 255, 245, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("seashell2", new RColorsDef("seashell2", 238, 229, 222)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("seashell3", new RColorsDef("seashell3", 205, 197, 191)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("seashell4", new RColorsDef("seashell4", 139, 134, 130)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("sienna", new RColorsDef("sienna", 160, 82, 45)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("sienna1", new RColorsDef("sienna1", 255, 130, 71)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("sienna2", new RColorsDef("sienna2", 238, 121, 66)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("sienna3", new RColorsDef("sienna3", 205, 104, 57)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("sienna4", new RColorsDef("sienna4", 139, 71, 38)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("skyblue", new RColorsDef("skyblue", 135, 206, 235)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("skyblue1", new RColorsDef("skyblue1", 135, 206, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("skyblue2", new RColorsDef("skyblue2", 126, 192, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("skyblue3", new RColorsDef("skyblue3", 108, 166, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("skyblue4", new RColorsDef("skyblue4", 74, 112, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slateblue", new RColorsDef("slateblue", 106, 90, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slateblue1", new RColorsDef("slateblue1", 131, 111, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slateblue2", new RColorsDef("slateblue2", 122, 103, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slateblue3", new RColorsDef("slateblue3", 105, 89, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slateblue4", new RColorsDef("slateblue4", 71, 60, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slategray", new RColorsDef("slategray", 112, 128, 144)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slategray1", new RColorsDef("slategray1", 198, 226, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slategray2", new RColorsDef("slategray2", 185, 211, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slategray3", new RColorsDef("slategray3", 159, 182, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slategray4", new RColorsDef("slategray4", 108, 123, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("slategrey", new RColorsDef("slategrey", 112, 128, 144)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("snow", new RColorsDef("snow", 255, 250, 250)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("snow1", new RColorsDef("snow1", 255, 250, 250)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("snow2", new RColorsDef("snow2", 238, 233, 233)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("snow3", new RColorsDef("snow3", 205, 201, 201)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("snow4", new RColorsDef("snow4", 139, 137, 137)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("springgreen", new RColorsDef("springgreen", 0, 255, 127)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("springgreen1", new RColorsDef("springgreen1", 0, 255, 127)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("springgreen2", new RColorsDef("springgreen2", 0, 238, 118)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("springgreen3", new RColorsDef("springgreen3", 0, 205, 102)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("springgreen4", new RColorsDef("springgreen4", 0, 139, 69)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("steelblue", new RColorsDef("steelblue", 70, 130, 180)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("steelblue1", new RColorsDef("steelblue1", 99, 184, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("steelblue2", new RColorsDef("steelblue2", 92, 172, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("steelblue3", new RColorsDef("steelblue3", 79, 148, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("steelblue4", new RColorsDef("steelblue4", 54, 100, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("tan", new RColorsDef("tan", 210, 180, 140)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("tan1", new RColorsDef("tan1", 255, 165, 79)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("tan2", new RColorsDef("tan2", 238, 154, 73)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("tan3", new RColorsDef("tan3", 205, 133, 63)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("tan4", new RColorsDef("tan4", 139, 90, 43)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("thistle", new RColorsDef("thistle", 216, 191, 216)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("thistle1", new RColorsDef("thistle1", 255, 225, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("thistle2", new RColorsDef("thistle2", 238, 210, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("thistle3", new RColorsDef("thistle3", 205, 181, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("thistle4", new RColorsDef("thistle4", 139, 123, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("tomato", new RColorsDef("tomato", 255, 99, 71)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("tomato1", new RColorsDef("tomato1", 255, 99, 71)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("tomato2", new RColorsDef("tomato2", 238, 92, 66)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("tomato3", new RColorsDef("tomato3", 205, 79, 57)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("tomato4", new RColorsDef("tomato4", 139, 54, 38)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("turquoise", new RColorsDef("turquoise", 64, 224, 208)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("turquoise1", new RColorsDef("turquoise1", 0, 245, 255)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("turquoise2", new RColorsDef("turquoise2", 0, 229, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("turquoise3", new RColorsDef("turquoise3", 0, 197, 205)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("turquoise4", new RColorsDef("turquoise4", 0, 134, 139)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("violet", new RColorsDef("violet", 238, 130, 238)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("violetred", new RColorsDef("violetred", 208, 32, 144)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("violetred1", new RColorsDef("violetred1", 255, 62, 150)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("violetred2", new RColorsDef("violetred2", 238, 58, 140)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("violetred3", new RColorsDef("violetred3", 205, 50, 120)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("violetred4", new RColorsDef("violetred4", 139, 34, 82)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("wheat", new RColorsDef("wheat", 245, 222, 179)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("wheat1", new RColorsDef("wheat1", 255, 231, 186)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("wheat2", new RColorsDef("wheat2", 238, 216, 174)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("wheat3", new RColorsDef("wheat3", 205, 186, 150)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("wheat4", new RColorsDef("wheat4", 139, 126, 102)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("whitesmoke", new RColorsDef("whitesmoke", 245, 245, 245)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("yellow", new RColorsDef("yellow", 255, 255, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("yellow1", new RColorsDef("yellow1", 255, 255, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("yellow2", new RColorsDef("yellow2", 238, 238, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("yellow3", new RColorsDef("yellow3", 205, 205, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("yellow4", new RColorsDef("yellow4", 139, 139, 0)); //$NON-NLS-1$ //$NON-NLS-2$
		colors.put("yellowgreen", new RColorsDef("yellowgreen", 154, 205, 50)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
/*
cc <- palette("default")
for (i in cc) {
	cat("colors.add(new RPaletteDef(number++, colorMap.get(\"")
	cat(i)
	cat("\")));\n")
}
*/
	protected void addDefaultPaletteColors(final List<ColorDef> colors) {
		int number = 1;
		colors.add(new RPaletteDef(number++, colorsMap.get("black"))); //$NON-NLS-1$
		colors.add(new RPaletteDef(number++, colorsMap.get("red"))); //$NON-NLS-1$
		colors.add(new RPaletteDef(number++, colorsMap.get("green3"))); //$NON-NLS-1$
		colors.add(new RPaletteDef(number++, colorsMap.get("blue"))); //$NON-NLS-1$
		colors.add(new RPaletteDef(number++, colorsMap.get("cyan"))); //$NON-NLS-1$
		colors.add(new RPaletteDef(number++, colorsMap.get("magenta"))); //$NON-NLS-1$
		colors.add(new RPaletteDef(number++, colorsMap.get("yellow"))); //$NON-NLS-1$
		colors.add(new RPaletteDef(number++, colorsMap.get("gray"))); //$NON-NLS-1$
	}
	
	
	public ColorDef parseColorDef(final RAstNode node) {
		if (node != null) {
			switch (node.getNodeType()) {
			case F_CALL:
				return analyzeColorCall((FCall) node);
			case STRING_CONST:
				return analyzeColorString(node.getText());
			case NUM_CONST:
				return analyzeColorNum(node);
			default:
				break;
			}
		}
		return null;
	}
	
	private ColorDef analyzeColorCall(final FCall node) {
		final String fName = resolveElementName(node.getRefChild());
		if (fName != null && !fName.isEmpty()) {
			if (fName.equals(RGraphicFunctions.RGB_NAME)) {
				final ReadedFCallArgs args = RAst.readArgs(node.getArgsChild(), RGB_args);
				final Integer red = RAst.toJavaInt(args.getArgValueNode(0));
				final Integer green = RAst.toJavaInt(args.getArgValueNode(1));
				final Integer blue = RAst.toJavaInt(args.getArgValueNode(2));
				final Integer alpha = RAst.toJavaInt(args.getArgValueNode(3));
				if (red != null && green != null && blue != null
						&& (args.getArgValueNode(3) == null || alpha != null)) {
					try {
						final ColorDef color = new ColorDef(red, green, blue);
						return (alpha != null) ?
								new ColorAlphaDef(color, alpha.intValue()) :
								color;
					}
					catch (final IllegalArgumentException e) {}
				}
				return null;
			}
			if (fName.equals(RGraphicFunctions.HSV_NAME)) {
				final ReadedFCallArgs args = RAst.readArgs(node.getArgsChild(), HSV_args);
				final Float hue = RAst.toJavaFloat(args.getArgValueNode(0));
				final Float saturation = RAst.toJavaFloat(args.getArgValueNode(1));
				final Float value = RAst.toJavaFloat(args.getArgValueNode(2));
				final Float alpha = RAst.toJavaFloat(args.getArgValueNode(3));
				if (hue != null && saturation != null && value != null) {
					try {
						final ColorDef color = new HSVColorDef(hue, saturation, value);
						return (alpha != null) ?
								new ColorAlphaDef(color, alpha.floatValue()) :
								color;
					}
					catch (final IllegalArgumentException e) {}
				}
				return null;
			}
			if (fName.equals(RGraphicFunctions.ADJUST_COLOR_NAME)) {
				final ReadedFCallArgs args = RAst.readArgs(node.getArgsChild(), ADJUST_COLOR_args);
				final RAstNode colorArg = args.getArgValueNode(0);
				if (colorArg != null && !hasMoreArgs(args, 2)) {
					try {
						final ColorDef color = parseColorDef(colorArg);
						if (color != null) {
							final Float alpha = RAst.toJavaFloat(args.getArgValueNode(1));
							return (alpha != null) ?
									new ColorAlphaDef(color, alpha.floatValue()) :
									color;
						}
					}
					catch (final IllegalArgumentException e) {}
				}
			}
		}
		return null;
	}
	
	private boolean hasMoreArgs(final ReadedFCallArgs args, int first) {
		final int size = args.argsDef.size();
		while (first < size) {
			if (args.getArgValueNode(first++) != null) {
				return true;
			}
		}
		return false;
	}
	
	private ColorDef analyzeColorString(final String s) {
		if (s != null && !s.isEmpty()) {
			if (s.length() == 7 && s.charAt(0) == '#') {
				try {
					final int red = Integer.parseInt(s.substring(1, 3), 16);
					final int green = Integer.parseInt(s.substring(3, 5), 16);
					final int blue = Integer.parseInt(s.substring(5, 7), 16);
					return new ColorDef(red, green, blue);
				}
				catch (final NumberFormatException e) {}
				catch (final IllegalArgumentException e) {}
				return null;
			}
			if (s.length() == 9 && s.charAt(0) == '#') {
				try {
					final int red = Integer.parseInt(s.substring(1, 3), 16);
					final int green = Integer.parseInt(s.substring(3, 5), 16);
					final int blue = Integer.parseInt(s.substring(5, 7), 16);
					final int alpha = Integer.parseInt(s.substring(7, 9), 16);
					return new ColorAlphaDef(new ColorDef(red, green, blue), alpha);
				}
				catch (final NumberFormatException e) {}
				catch (final IllegalArgumentException e) {}
				return null;
			}
			return colorsMap.get(s);
		}
		return null;
	}
	
	private ColorDef analyzeColorNum(final RAstNode node) {
		final Integer num = RAst.toJavaInt(node);
		if (num != null) {
			final int idx = num.intValue() - 1;
			final List<? extends ColorDef> palette = defaultPalette;
			if (idx >= 0 && idx < palette.size()) {
				return palette.get(idx);
			}
		}
		return null;
	}
	
	public Float parseAlpha(final RAstNode node) {
		if (node != null) {
			switch (node.getNodeType()) {
			case NUM_CONST:
				return analyzeAlphaNum(node);
			default:
				break;
			}
		}
		return null;
	}
	
	private Float analyzeAlphaNum(final RAstNode node) {
		final Float num = RAst.toJavaFloat(node);
		if (num != null) {
			final float v = num.floatValue();
			if (v >= 0 && v <= 1f) {
				return num;
			}
		}
		return null;
	}
	
	public String parseFontFamily(final RAstNode node) {
		if (node != null) {
			switch (node.getNodeType()) {
			case STRING_CONST:
				return node.getText();
			default:
				break;
			}
		}
		return null;
	}
	
	private String resolveElementName(final RAstNode node) {
		if ((node.getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) != 0) {
			return null;
		}
		switch (node.getNodeType()) {
		case SYMBOL:
		case STRING_CONST:
			return node.getText();
		case NS_GET: {
			final NSGet ns = (NSGet) node;
			if (ns.getNamespaceChild().getNodeType() == NodeType.SYMBOL
					&& (ns.getNamespaceChild().getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0
					&& ns.getElementChild().getNodeType() == NodeType.SYMBOL
					&& (ns.getElementChild().getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0) {
				final String namespace = ns.getNamespaceChild().getText();
				if (namespace.equals("base") //$NON-NLS-1$
						|| namespace.equals("utils") //$NON-NLS-1$
						|| namespace.equals("grDevices") ) { //$NON-NLS-1$
					return ns.getElementChild().getText();
				}
			}
			return null; }
		default:
			return null;
		}
	}
	
}
