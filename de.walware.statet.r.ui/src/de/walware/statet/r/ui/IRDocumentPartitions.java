/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui;

import org.eclipse.jface.text.IDocument;


public interface IRDocumentPartitions {

/* R *************************************************************************/

	/**
	 * Id of partitioning of R-documents.
	 * Value: {@value}
	 */
	String R_DOCUMENT_PARTITIONING = "__r_partitioning"; //$NON-NLS-1$
	
	
	/**
	 * The name of a default partition (R code) in R documents.
	 * Value: defined by {@link org.eclipse.jface.text.IDocument#DEFAULT_CONTENT_TYPE}
	 */
	String R_DEFAULT = IDocument.DEFAULT_CONTENT_TYPE;

	/**
	 * The name of a infix operator partition in R documents.
	 * Value: {@value}
	 */
	String R_INFIX_OPERATOR = "__r_op_infix"; //$NON-NLS-1$

	/**
	 * The name of a string partition in R documents.
	 * Value: {@value}
	 */
	String R_STRING = "__r_string"; //$NON-NLS-1$

	/**
	 * The name of a comment partition in R documents.
	 * Value: {@value}
	 */
	String R_COMMENT = "__r_comment"; //$NON-NLS-1$

	
	/**
	 * Array with partitions of R-documents.
	 */
	String[] R_PARTITIONS = new String[] {
			R_DEFAULT, R_INFIX_OPERATOR, R_STRING, R_COMMENT
	};
	
	
	
/* Rd ************************************************************************/

	/**
	 * Id of partitioning of Rd-documents.
	 * Value: {@value}
	 */
	String RDOC_DOCUMENT_PARTITIONING = "__rd_partitioning"; //$NON-NLS-1$


	/**
	 * The name of a default partition (Rd code) in Rd documents.
	 * Value: defined by {@link org.eclipse.jface.text.IDocument#DEFAULT_CONTENT_TYPE}
	 */
	String RDOC_DEFAULT = IDocument.DEFAULT_CONTENT_TYPE;

	/**
	 * The name of a comment partition in Rd documents.
	 * Value: {@value}
	 */
	String RDOC_COMMENT = "__rd_comment"; //$NON-NLS-1$
	
	/**
	 * The name of a platform instruction partition in Rd documents.
	 * Value: {@value}
	 */

	String RDOC_PLATFORM_SPECIF = "__rd_platform"; //$NON-NLS-1$
	

	/**
	 * Array with partitions of Rd-documents.
	 */
	String[] RDOC_PARTITIONS = new String[] {
			RDOC_DEFAULT, RDOC_COMMENT, RDOC_PLATFORM_SPECIF, 
	};
	
}
