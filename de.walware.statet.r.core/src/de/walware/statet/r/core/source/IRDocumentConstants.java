/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.source;

import org.eclipse.jface.text.IDocument;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.text.core.IPartitionConstraint;


public interface IRDocumentConstants {
	
/* R *************************************************************************/
	
	/**
	 * The id of partitioning of R documents.
	 * 
	 * Value: {@value}
	 */
	String R_PARTITIONING= "R_walware"; //$NON-NLS-1$
	
	
	/**
	 * The type of a default partition (R code) in R documents
	 * 
	 * Value: {@value}
	 */
	String R_DEFAULT_CONTENT_TYPE= "R.Default"; //$NON-NLS-1$
	
	/**
	 * The type of a symbol partition in R documents
	 * 
	 * Value: {@value}
	 */
	String R_QUOTED_SYMBOL_CONTENT_TYPE= "R.QuotedSymbol"; //$NON-NLS-1$
	
	/**
	 * The type of a infix operator partition in R documents
	 * 
	 * Value: {@value}
	 */
	String R_INFIX_OPERATOR_CONTENT_TYPE= "R.Op.Infix"; //$NON-NLS-1$
	
	/**
	 * The type of a string partition in R documents
	 * 
	 * Value: {@value}
	 */
	String R_STRING_CONTENT_TYPE= "R.String"; //$NON-NLS-1$
	
	/**
	 * The type of a comment partition in R documents.
	 * Value: {@value}
	 */
	String R_COMMENT_CONTENT_TYPE= "R.Comment"; //$NON-NLS-1$
	
	/**
	 * The type of a Roxygen comment partition in R documents.
	 * Value: {@value}
	 */
	String R_ROXYGEN_CONTENT_TYPE= "R.Roxygen"; //$NON-NLS-1$
	
	
	/**
	 * List with all partition content types of R documents.
	 */
	ImList<String> R_CONTENT_TYPES= ImCollections.newList(
			R_DEFAULT_CONTENT_TYPE, 
			R_QUOTED_SYMBOL_CONTENT_TYPE, 
			R_INFIX_OPERATOR_CONTENT_TYPE, 
			R_STRING_CONTENT_TYPE, 
			R_COMMENT_CONTENT_TYPE,
			R_ROXYGEN_CONTENT_TYPE );
	
	
	IPartitionConstraint R_DEFAULT_CONTENT_CONSTRAINT= new IPartitionConstraint() {
		@Override
		public boolean matches(final String partitionType) {
			return (partitionType == R_DEFAULT_CONTENT_TYPE);
		}
	};
	
	IPartitionConstraint R_CODE_CONTENT_CONSTRAINT= new IPartitionConstraint() {
		@Override
		public boolean matches(final String partitionType) {
			return (partitionType == R_DEFAULT_CONTENT_TYPE
					|| partitionType == IRDocumentConstants.R_STRING_CONTENT_TYPE
					|| partitionType == IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE
					|| partitionType == IRDocumentConstants.R_INFIX_OPERATOR_CONTENT_TYPE );
		}
	};
	
	IPartitionConstraint R_ANY_CONTENT_CONSTRAINT= new IPartitionConstraint() {
		@Override
		public boolean matches(final String partitionType) {
			return (partitionType == IRDocumentConstants.R_DEFAULT_CONTENT_TYPE
					|| partitionType == IRDocumentConstants.R_STRING_CONTENT_TYPE
					|| partitionType == IRDocumentConstants.R_COMMENT_CONTENT_TYPE
					|| partitionType == IRDocumentConstants.R_ROXYGEN_CONTENT_TYPE
					|| partitionType == IRDocumentConstants.R_INFIX_OPERATOR_CONTENT_TYPE
					|| partitionType == IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE );
		}
	};
	
	
/* Rd ************************************************************************/
	
	/**
	 * The Id of partitioning of Rd documents.
	 * 
	 * Value: {@value}
	 */
	String RDOC_PARTITIONING= "Rd_walware"; //$NON-NLS-1$
	
	
	/**
	 * The type of a default partition (Rd code) in Rd documents
	 * 
	 * Value: defined by {@link org.eclipse.jface.text.IDocument#DEFAULT_CONTENT_TYPE}
	 */
	String RDOC_DEFAULT_CONTENT_TYPE= IDocument.DEFAULT_CONTENT_TYPE;
	
	/**
	 * The type of a comment partition in Rd documents
	 * 
	 * Value: {@value}
	 */
	String RDOC_COMMENT_CONTENT_TYPE= "Rd.Comment"; //$NON-NLS-1$
	
	/**
	 * The type of a platform instruction partition in Rd documents
	 * 
	 * Value: {@value}
	 */
	
	String RDOC_PLATFORM_SPECIF= "__rd_platform"; //$NON-NLS-1$
	
	
	/**
	 * List with all partition content types of Rd documents.
	 */
	ImList<String> RDOC_CONTENT_TYPES= ImCollections.newList(
			RDOC_DEFAULT_CONTENT_TYPE, 
			RDOC_COMMENT_CONTENT_TYPE, 
			RDOC_PLATFORM_SPECIF );
	
}
