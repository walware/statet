/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.sweave.text;

import org.eclipse.jface.text.IDocument;

import de.walware.ecommons.text.IPartitionConstraint;
import de.walware.ecommons.text.PartitioningConfiguration;

import de.walware.docmlet.tex.core.source.ITexDocumentConstants;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;


/**
 * 
 */
public class Rweave {
	
	
	public static final String CHUNK_CONTROL_CONTENT_TYPE = "sweave_chunk_control"; //$NON-NLS-1$
	public static final String CHUNK_COMMENT_CONTENT_TYPE = "sweave_chunk_comment"; //$NON-NLS-1$
	
	public static final String LTX_DEFAULT_CONTENT_TYPE = ITexDocumentConstants.LTX_DEFAULT_CONTENT_TYPE;
	
	public static final String R_DEFAULT_CONTENT_TYPE = IRDocumentPartitions.R_DEFAULT_EXPL;
	
	
	/**
	 * Id of partitioning of Sweave (LaTeX) documents.
	 * Value: {@value}
	 */
	public static final String LTX_R_PARTITIONING = "ltxrweave_walware"; //$NON-NLS-1$
	
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
	
	public static final String[] LTX_PARTITION_TYPES = new String[] {
		ITexDocumentConstants.LTX_DEFAULT_EXPL_CONTENT_TYPE,
		ITexDocumentConstants.LTX_MATH_CONTENT_TYPE,
		ITexDocumentConstants.LTX_VERBATIM_CONTENT_TYPE,
		ITexDocumentConstants.LTX_COMMENT_CONTENT_TYPE,
		ITexDocumentConstants.LTX_MATHCOMMENT_CONTENT_TYPE,
	};
	
	/**
	 * Array with all partitions of Sweave LaTeX documents.
	 */
	public static final String[] ALL_PARTITION_TYPES = new String[] {
		IDocument.DEFAULT_CONTENT_TYPE,
//		ITexDocumentConstants.TEX_DEFAULT_EXPL_CONTENT_TYPE, automatically converted to DEFAULT_CONTENT_TYPE
		ITexDocumentConstants.LTX_MATH_CONTENT_TYPE,
		ITexDocumentConstants.LTX_VERBATIM_CONTENT_TYPE,
		ITexDocumentConstants.LTX_COMMENT_CONTENT_TYPE,
		ITexDocumentConstants.LTX_MATHCOMMENT_CONTENT_TYPE,
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
			LTX_R_PARTITIONING, new String[] { TEX_CAT, R_CAT });
	
	
	public static final IPartitionConstraint CHUNK_CONTROL_PARTITION_CONSTRAINT = new IPartitionConstraint() {
		@Override
		public boolean matches(final String partitionType) {
			return (partitionType == Rweave.CHUNK_CONTROL_CONTENT_TYPE
					|| partitionType == Rweave.CHUNK_COMMENT_CONTENT_TYPE
					);
		}
	};
	
	public static final IPartitionConstraint R_PARTITION_CONSTRAINT = new IPartitionConstraint() {
		@Override
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
	
	public static final IPartitionConstraint LTX_PARTITION_CONSTRAINT = new IPartitionConstraint() {
		@Override
		public boolean matches(final String partitionType) {
			return (partitionType == IDocument.DEFAULT_CONTENT_TYPE
					|| partitionType == ITexDocumentConstants.LTX_DEFAULT_EXPL_CONTENT_TYPE
					|| partitionType == ITexDocumentConstants.LTX_MATH_CONTENT_TYPE
					|| partitionType == ITexDocumentConstants.LTX_COMMENT_CONTENT_TYPE
					|| partitionType == ITexDocumentConstants.LTX_MATHCOMMENT_CONTENT_TYPE
					|| partitionType == ITexDocumentConstants.LTX_VERBATIM_CONTENT_TYPE
					);
		}
	};
	
	public static final PartitioningConfiguration LTX_PARTITIONING_CONFIG = new PartitioningConfiguration(
			LTX_R_PARTITIONING, new IPartitionConstraint() {
				@Override
				public boolean matches(final String partitionType) {
					return (partitionType == IDocument.DEFAULT_CONTENT_TYPE
							|| partitionType == ITexDocumentConstants.LTX_DEFAULT_EXPL_CONTENT_TYPE);
				}
			});
	
	public static final PartitioningConfiguration R_PARTITIONING_CONFIG = new PartitioningConfiguration(
			LTX_R_PARTITIONING, new IPartitionConstraint() {
				@Override
				public boolean matches(final String partitionType) {
					return (partitionType == IRDocumentPartitions.R_DEFAULT_EXPL);
				}
			});
	
}
