package de.walware.statet.rtm.base.util;

import de.walware.statet.rtm.rtdata.RtDataPackage;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


public class RExprType {
	
	
	public static final RExprType DATAFRAME_TYPE = new RExprType(
			RTypedExpr.R, RtDataPackage.RDATA_FRAME,
			"R e&xpression setting the dataframe" );
	
	public static final RExprType EXPR_VALUE_TYPE = new RExprType(
			RTypedExpr.R, -1,
			"R e&xpression setting the attribute value" );
	
	public static final RExprType EXPR_LABEL_VALUE_TYPE = new RExprType(
			RTypedExpr.R, RtDataPackage.RLABEL,
			"R e&xpression setting the attribute label" );
	
	public static final RExprType EXPR_COLOR_VALUE_TYPE = new RExprType(
			RTypedExpr.R, RtDataPackage.RCOLOR,
			"R e&xpression setting the attribute color value" );
	
	public static final RExprType EXPR_ALPHA_VALUE_TYPE = new RExprType(
			RTypedExpr.R, RtDataPackage.RALPHA,
			"R e&xpression setting the attribute alpha value" );
	
	public static final RExprType EXPR_FONT_FAMILY_VALUE_TYPE = new RExprType(
			RTypedExpr.R, RtDataPackage.RFONT_FAMILY,
			"R e&xpression setting the font family value" );
	
	public static final RExprType TEXT_VALUE_TYPE = new RExprType(
			RTypedExpr.CHAR, RtDataPackage.RTEXT,
			"&Text (automatically quoted for R)" );
	
	public static final RExprType DATAFRAME_COLUMN_TYPE = new RExprType(
			RTypedExpr.MAPPED, -1,
			"Dataframe variable &mapping from data to the attribute" );
	
	public static final RExprType EXPR_FUNCTION_TYPE = new RExprType(
			RTypedExpr.R, RtDataPackage.RFUNCTION,
			"R e&xpression setting the function" );
	
	
	private final String fTypeKey;
	private final int fRtDataTypeID;
	
	private final String fLabel;
	
	
	public RExprType(final String type, final int typeID, final String label) {
		fTypeKey = type;
		fRtDataTypeID = typeID;
		
		fLabel = label;
	}
	
	
	public String getTypeKey() {
		return fTypeKey;
	}
	
	public int getRtDataID() {
		return fRtDataTypeID;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	
	@Override
	public String toString() {
		return fTypeKey + " / " + fRtDataTypeID; //$NON-NLS-1$
	}
	
}
