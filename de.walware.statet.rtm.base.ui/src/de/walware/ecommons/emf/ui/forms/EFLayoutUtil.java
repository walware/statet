/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.ui.forms;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.walware.ecommons.ui.util.LayoutUtil;


public class EFLayoutUtil {
	
	
	public static final int MAIN_H_SPACING = 20;
	public static final int MAIN_V_SPACING = 17;
	
	public static final int SECTION_HEADER_V_SPACING = 6;
	
	// SECTION CLIENT
	public static final int SECTION_CLIENT_MARGIN_TOP = 5;
	public static final int SECTION_CLIENT_MARGIN_BOTTOM = SECTION_CLIENT_MARGIN_TOP;
	// Should be 6; but, we minus 4 because the section automatically pads the
	// left margin by that amount
	public static final int SECTION_CLIENT_MARGIN_LEFT = 2;
	// Should be 6; but, we minus 4 because the section automatically pads the
	// right margin by that amount	
	public static final int SECTION_CLIENT_MARGIN_RIGHT = 2;
	public static final int SECTION_CLIENT_H_SPACING = 5;
	public static final int SECTION_CLIENT_V_SPACING = SECTION_CLIENT_H_SPACING;
	
	public static final int PROP_DEFAULT_NUMCOLUMNS = 3;
	
	
	public static TableWrapLayout createBodyTableLayout(final int numColumns) {
		final TableWrapLayout layout = new TableWrapLayout();
		
		layout.topMargin = 12;
		layout.bottomMargin = 12;
		layout.leftMargin = 6;
		layout.rightMargin = 6;
		
		layout.horizontalSpacing = MAIN_H_SPACING;
		layout.verticalSpacing = MAIN_V_SPACING;
		
		layout.numColumns = numColumns;
		
		return layout;
	}
	
	public static TableWrapLayout createPropertiesTableLayout(final int numColumns) {
		final TableWrapLayout layout = new TableWrapLayout();
		
		layout.topMargin = 6;
		layout.bottomMargin = 6;
		layout.leftMargin = 6;
		layout.rightMargin = 6;
		
		layout.horizontalSpacing = MAIN_H_SPACING;
		layout.verticalSpacing = MAIN_V_SPACING;
		
		layout.numColumns = numColumns;
		
		return layout;
	}
	
	public static ColumnLayout createMainColumnLayout() {
		final ColumnLayout layout = new ColumnLayout();
		
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		
		layout.horizontalSpacing = MAIN_H_SPACING;
		layout.verticalSpacing = MAIN_V_SPACING;
		
		return layout;
	}
	
	public static TableWrapLayout createMainTableLayout(final int numColumns) {
		final TableWrapLayout layout = new TableWrapLayout();
		
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		
		layout.horizontalSpacing = MAIN_H_SPACING;
		layout.verticalSpacing = MAIN_V_SPACING;
		
		layout.numColumns = numColumns;
		
		return layout;
	}
	
	public static Layout createMainSashLeftLayout(final int numColumns) {
		final GridLayout layout = new GridLayout();
		
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginRight = (MAIN_H_SPACING - 2) / 2;
		
		layout.horizontalSpacing = MAIN_H_SPACING;
		layout.verticalSpacing = MAIN_V_SPACING;
		
		layout.numColumns = numColumns;
		
		return layout;
	}
	
	public static Layout createMainSashRightLayout(final int numColumns) {
		final TableWrapLayout layout = new TableWrapLayout();
		
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.leftMargin = (MAIN_H_SPACING - 2) / 2;
		layout.rightMargin = 0;
		
		layout.horizontalSpacing = MAIN_H_SPACING;
		layout.verticalSpacing = MAIN_V_SPACING;
		
		layout.numColumns = numColumns;
		
		return layout;
	}
	
//	public static GridLayout createMainSashLeftLayout(final int numColumns) {
//		final GridLayout layout = new GridLayout();
//		
//		layout.marginHeight = 0;
//		layout.marginWidth = 0;
//		
//		layout.marginTop = 0;
//		layout.marginBottom = 0;
//		layout.marginLeft = 0;
//		layout.marginRight = (MAIN_H_SPACING - 2) / 2;
//		
//		layout.horizontalSpacing = MAIN_H_SPACING;
//		layout.verticalSpacing = MAIN_V_SPACING;
//		
//		layout.numColumns = numColumns;
//		
//		return layout;
//	}
//	
//	public static GridLayout createMainSashRightLayout(final int numColumns) {
//		final GridLayout layout = new GridLayout();
//		
//		layout.marginHeight = 0;
//		layout.marginWidth = 0;
//		
//		layout.marginTop = 0;
//		layout.marginBottom = 0;
//		layout.marginLeft = (MAIN_H_SPACING - 2) / 2;
//		layout.marginRight = 0;
//		
//		layout.horizontalSpacing = MAIN_H_SPACING;
//		layout.verticalSpacing = MAIN_V_SPACING;
//		
//		layout.numColumns = numColumns;
//		
//		return layout;
//	}
//	
	public static GridLayout createSectionPropGridLayout() {
		return createSectionPropGridLayout(PROP_DEFAULT_NUMCOLUMNS);
	}
	
	public static GridLayout createSectionPropGridLayout(final int numColumns) {
		final GridLayout layout = new GridLayout();
		
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		layout.marginTop = SECTION_CLIENT_MARGIN_TOP;
		layout.marginBottom = SECTION_CLIENT_MARGIN_BOTTOM;
		layout.marginLeft = SECTION_CLIENT_MARGIN_LEFT;
		layout.marginRight = SECTION_CLIENT_MARGIN_RIGHT;
		
		layout.horizontalSpacing = LayoutUtil.defaultHSpacing();
		layout.verticalSpacing = LayoutUtil.defaultVSpacing();
		
		layout.numColumns = numColumns;
		
		return layout;
	}
	
	public static GridLayout createCompositePropGridLayout() {
		return createCompositePropGridLayout(PROP_DEFAULT_NUMCOLUMNS);
	}
	
	public static GridLayout createCompositePropGridLayout(final int numColumns) {
		final GridLayout layout = new GridLayout();
		
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		
		layout.horizontalSpacing = LayoutUtil.defaultHSpacing();
		layout.verticalSpacing = LayoutUtil.defaultVSpacing();
		
		layout.numColumns = numColumns;
		
		return layout;
	}
	
//	public static TableWrapLayout createSectionClientTableWrapLayout(int numColumns) {
//		TableWrapLayout layout = new TableWrapLayout();
//		
//		layout.topMargin = SECTION_CLIENT_MARGIN_TOP;
//		layout.bottomMargin = SECTION_CLIENT_MARGIN_BOTTOM;
//		layout.leftMargin = SECTION_CLIENT_MARGIN_LEFT;
//		layout.rightMargin = SECTION_CLIENT_MARGIN_RIGHT;
//		
//		layout.horizontalSpacing = SECTION_CLIENT_HORIZONTAL_SPACING;
//		layout.verticalSpacing = SECTION_CLIENT_VERTICAL_SPACING;
//		
//		layout.numColumns = numColumns;
//		
//		return layout;
//	}
	
	public static GridLayout createCompositeColumnGridLayout(final int numColumns) {
		final GridLayout layout = new GridLayout();
		
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		
		layout.horizontalSpacing = 20;
		layout.verticalSpacing = LayoutUtil.defaultVSpacing();
		
		layout.numColumns = numColumns;
		
		return layout;
	}
	
}
