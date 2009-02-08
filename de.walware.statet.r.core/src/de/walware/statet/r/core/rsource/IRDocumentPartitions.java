/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource;

import org.eclipse.jface.text.IDocument;

import de.walware.ecommons.ltk.text.IPartitionConstraint;
import de.walware.ecommons.ltk.text.PartitioningConfiguration;


public interface IRDocumentPartitions {
	
/* R *************************************************************************/
	
	/**
	 * Id of partitioning of R-documents.
	 * Value: {@value}
	 */
	public static final String R_PARTITIONING = "__r_partitioning"; //$NON-NLS-1$
	
	
	/**
	 * The type of a default partition (R code) in R documents
	 * 
	 * Value: defined by {@link org.eclipse.jface.text.IDocument#DEFAULT_CONTENT_TYPE}
	 */
	public static final String R_DEFAULT = IDocument.DEFAULT_CONTENT_TYPE;
	
	/**
	 * The type of a explicit default partition (R code) in R documents
	 * 
	 * Value: {@value}
	 */
	public static final String R_DEFAULT_EXPL = "__r_default"; //$NON-NLS-1$
	
	/**
	 * The type of a symbol partition in R documents
	 * 
	 * Value: {@value}
	 */
	public static final String R_QUOTED_SYMBOL = "__r_symbol_quoted"; //$NON-NLS-1$
	
	/**
	 * The type of a infix operator partition in R documents
	 * 
	 * Value: {@value}
	 */
	public static final String R_INFIX_OPERATOR = "__r_op_infix"; //$NON-NLS-1$
	
	/**
	 * The type of a string partition in R documents
	 * 
	 * Value: {@value}
	 */
	public static final String R_STRING = "__r_string"; //$NON-NLS-1$
	
	/**
	 * The type of a comment partition in R documents.
	 * Value: {@value}
	 */
	public static final String R_COMMENT = "__r_comment"; //$NON-NLS-1$
	
	/**
	 * The type of a Roxygen comment partition in R documents.
	 * Value: {@value}
	 */
	public static final String R_ROXYGEN = "__r_roxygen"; //$NON-NLS-1$
	
	
	/**
	 * Array with partitions of R-documents.
	 */
	public static final String[] R_PARTITIONS = new String[] {
			R_DEFAULT, 
			R_QUOTED_SYMBOL, 
			R_INFIX_OPERATOR, 
			R_STRING, 
			R_COMMENT,
			R_ROXYGEN,
	};
	
	public static final PartitioningConfiguration R_PARTITIONING_CONFIG = new PartitioningConfiguration(
			R_PARTITIONING, new IPartitionConstraint() {
				public boolean matches(final String partitionType) {
					return (partitionType == R_DEFAULT
							|| partitionType == R_DEFAULT_EXPL);
				}
			});
	
	
/* Rd ************************************************************************/
	
	/**
	 * Id of partitioning of Rd-documents
	 * 
	 * Value: {@value}
	 */
	public static final String RDOC_DOCUMENT_PARTITIONING = "__rd_partitioning"; //$NON-NLS-1$
	
	
	/**
	 * The type of a default partition (Rd code) in Rd documents
	 * 
	 * Value: defined by {@link org.eclipse.jface.text.IDocument#DEFAULT_CONTENT_TYPE}
	 */
	public static final String RDOC_DEFAULT = IDocument.DEFAULT_CONTENT_TYPE;
	
	/**
	 * The type of a comment partition in Rd documents
	 * 
	 * Value: {@value}
	 */
	public static final String RDOC_COMMENT = "__rd_comment"; //$NON-NLS-1$
	
	/**
	 * The type of a platform instruction partition in Rd documents
	 * 
	 * Value: {@value}
	 */
	
	public static final String RDOC_PLATFORM_SPECIF = "__rd_platform"; //$NON-NLS-1$
	
	
	/**
	 * Array with partitions of Rd-documents.
	 */
	public static final String[] RDOC_PARTITIONS = new String[] {
			RDOC_DEFAULT, 
			RDOC_COMMENT, 
			RDOC_PLATFORM_SPECIF,
	};
	
	public static final PartitioningConfiguration RDOC_PARTITIONING_CONFIG = new PartitioningConfiguration(
			RDOC_DOCUMENT_PARTITIONING, new IPartitionConstraint() {
				public boolean matches(final String partitionType) {
					return (partitionType == RDOC_DEFAULT);
				}
			});
	
}
