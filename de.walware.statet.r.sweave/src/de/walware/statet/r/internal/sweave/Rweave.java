/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import org.eclipse.jface.text.IDocument;

import de.walware.ecommons.ltk.text.IPartitionConstraint;
import de.walware.ecommons.ltk.text.PartitioningConfiguration;

import net.sourceforge.texlipse.editor.ITexDocumentConstants;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.sweave.text.CatPartitioner;
import de.walware.statet.r.sweave.text.MultiCatDocumentUtil;


/**
 * 
 */
public class Rweave {
	
	
	public static final String CHUNK_CONTROL_CONTENT_TYPE = "__sweave_chunk_control"; //$NON-NLS-1$
	public static final String CHUNK_COMMENT_CONTENT_TYPE = "__sweave_chunk_comment"; //$NON-NLS-1$
	
	public static final String TEX_DEFAULT_CONTENT_TYPE = IDocument.DEFAULT_CONTENT_TYPE;
	public static final String TEX_COMMENT_CONTENT_TYPE = ITexDocumentConstants.TEX_COMMENT_CONTENT_TYPE;
	public static final String TEX_MATH_CONTENT_TYPE = ITexDocumentConstants.TEX_MATH_CONTENT_TYPE;
	public static final String TEX_VERBATIM_CONTENT_TYPE = ITexDocumentConstants.TEX_VERBATIM;
	
	public static final String R_DEFAULT_CONTENT_TYPE = IRDocumentPartitions.R_DEFAULT_EXPL;
	
	
	/**
	 * Id of partitioning of Sweave (LaTeX) documents.
	 * Value: {@value}
	 */
	public static final String R_TEX_PARTITIONING = "__sweave_tex_partitioning"; //$NON-NLS-1$
	
	public static final String[] R_PARTITION_TYPES = new String[] {
		IRDocumentPartitions.R_DEFAULT_EXPL,
		IRDocumentPartitions.R_QUOTED_SYMBOL,
		IRDocumentPartitions.R_INFIX_OPERATOR,
		IRDocumentPartitions.R_STRING,
		IRDocumentPartitions.R_COMMENT,
		IRDocumentPartitions.R_ROXYGEN,
	};
	
	public static final String[] R_CHUNK_PARTITION_TYPES = new String[] {
		CHUNK_CONTROL_CONTENT_TYPE,
		CHUNK_COMMENT_CONTENT_TYPE,
		IRDocumentPartitions.R_DEFAULT_EXPL,
		IRDocumentPartitions.R_QUOTED_SYMBOL,
		IRDocumentPartitions.R_INFIX_OPERATOR,
		IRDocumentPartitions.R_STRING,
		IRDocumentPartitions.R_COMMENT,
		IRDocumentPartitions.R_ROXYGEN,
	};
	
	public static final String[] TEX_PARTITION_TYPES = new String[] {
		ITexDocumentConstants.TEX_DEFAULT_EXPL_CONTENT_TYPE,
		ITexDocumentConstants.TEX_MATH_CONTENT_TYPE,
		ITexDocumentConstants.TEX_VERBATIM,
		ITexDocumentConstants.TEX_COMMENT_CONTENT_TYPE,
	};
	
	/**
	 * Array with all partitions of Sweave LaTeX documents.
	 */
	public static final String[] ALL_PARTITION_TYPES = new String[] {
		IDocument.DEFAULT_CONTENT_TYPE,
//		ITexDocumentConstants.TEX_DEFAULT_EXPL_CONTENT_TYPE, automatically converted to DEFAULT_CONTENT_TYPE
		ITexDocumentConstants.TEX_MATH_CONTENT_TYPE,
		ITexDocumentConstants.TEX_VERBATIM,
		ITexDocumentConstants.TEX_COMMENT_CONTENT_TYPE,
		CHUNK_CONTROL_CONTENT_TYPE,
		CHUNK_COMMENT_CONTENT_TYPE,
		IRDocumentPartitions.R_DEFAULT_EXPL,
		IRDocumentPartitions.R_QUOTED_SYMBOL,
		IRDocumentPartitions.R_INFIX_OPERATOR,
		IRDocumentPartitions.R_STRING,
		IRDocumentPartitions.R_COMMENT,
		IRDocumentPartitions.R_ROXYGEN,
	};
	
	public static final String R_CAT = "r"; //$NON-NLS-1$
	public static final String TEX_CAT = "tex"; //$NON-NLS-1$
	public static final String CONTROL_CAT = CatPartitioner.CONTROL_CAT;
	
	public static final MultiCatDocumentUtil R_TEX_CAT_UTIL = new MultiCatDocumentUtil(
			R_TEX_PARTITIONING, new String[] { TEX_CAT, R_CAT });
	
	
	public static final IPartitionConstraint CHUNK_CONTROL_PARTITION_CONSTRAINT = new IPartitionConstraint() {
		public boolean matches(final String partitionType) {
			return (partitionType == Rweave.CHUNK_CONTROL_CONTENT_TYPE
					|| partitionType == Rweave.CHUNK_COMMENT_CONTENT_TYPE
					);
		}
	};
	
	public static final IPartitionConstraint R_PARTITION_CONSTRAINT = new IPartitionConstraint() {
		public boolean matches(final String partitionType) {
			return (partitionType == IRDocumentPartitions.R_DEFAULT_EXPL
					|| partitionType == IRDocumentPartitions.R_STRING
					|| partitionType == IRDocumentPartitions.R_COMMENT
					|| partitionType == IRDocumentPartitions.R_ROXYGEN
					|| partitionType == IRDocumentPartitions.R_INFIX_OPERATOR
					|| partitionType == IRDocumentPartitions.R_QUOTED_SYMBOL
					);
		}
	};
	
	public static final IPartitionConstraint TEX_PARTITION_CONSTRAINT = new IPartitionConstraint() {
		public boolean matches(final String partitionType) {
			return (partitionType == IDocument.DEFAULT_CONTENT_TYPE
					|| partitionType == ITexDocumentConstants.TEX_DEFAULT_EXPL_CONTENT_TYPE
					|| partitionType == ITexDocumentConstants.TEX_MATH_CONTENT_TYPE
					|| partitionType == ITexDocumentConstants.TEX_VERBATIM
					|| partitionType == ITexDocumentConstants.TEX_COMMENT_CONTENT_TYPE
					);
		}
	};
	
	public static final PartitioningConfiguration TEX_PARTITIONING_CONFIG = new PartitioningConfiguration(
			R_TEX_PARTITIONING, new IPartitionConstraint() {
				public boolean matches(final String partitionType) {
					return (partitionType == IDocument.DEFAULT_CONTENT_TYPE
							|| partitionType == ITexDocumentConstants.TEX_DEFAULT_EXPL_CONTENT_TYPE);
				}
			});
	
	public static final PartitioningConfiguration R_PARTITIONING_CONFIG = new PartitioningConfiguration(
			R_TEX_PARTITIONING, new IPartitionConstraint() {
				public boolean matches(final String partitionType) {
					return (partitionType == IRDocumentPartitions.R_DEFAULT_EXPL);
				}
			});
	
}
