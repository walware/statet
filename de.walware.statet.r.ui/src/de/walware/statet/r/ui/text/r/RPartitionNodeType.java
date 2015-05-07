/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.text.r;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import de.walware.ecommons.text.core.treepartitioner.AbstractPartitionNodeType;
import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNode;

import de.walware.statet.r.core.source.IRDocumentConstants;


public abstract class RPartitionNodeType extends AbstractPartitionNodeType {
	
	
	public static final RPartitionNodeType DEFAULT_ROOT= new RPartitionNodeType() {
		
		@Override
		public String getPartitionType() {
			return IRDocumentConstants.R_DEFAULT_CONTENT_TYPE;
		}
		
		@Override
		public byte getScannerState() {
			return RPartitionNodeScanner.S_DEFAULT;
		}
		
		@Override
		public boolean prefereAtBegin(final ITreePartitionNode node, final IDocument document) {
			return true;
		}
		
		@Override
		public boolean prefereAtEnd(final ITreePartitionNode node, final IDocument document) {
			return true;
		}
		
	};
	
	public static final RPartitionNodeType STRING_S= new RPartitionNodeType() {
		
		@Override
		public String getPartitionType() {
			return IRDocumentConstants.R_STRING_CONTENT_TYPE;
		}
		
		@Override
		public byte getScannerState() {
			return RPartitionNodeScanner.S_STRING_S;
		}
		
		@Override
		protected char getEndChar() {
			return '\'';
		}
		
		@Override
		public boolean prefereAtEnd(final ITreePartitionNode node, final IDocument document) {
			return !isClosed(node, document);
		}
		
	};
	
	public static final RPartitionNodeType STRING_D= new RPartitionNodeType() {
		
		@Override
		public String getPartitionType() {
			return IRDocumentConstants.R_STRING_CONTENT_TYPE;
		}
		
		@Override
		public byte getScannerState() {
			return RPartitionNodeScanner.S_STRING_D;
		}
		
		@Override
		protected char getEndChar() {
			return '\"';
		}
		
		@Override
		public boolean prefereAtEnd(final ITreePartitionNode node, final IDocument document) {
			return !isClosed(node, document);
		}
		
	};
	
	public static final RPartitionNodeType QUOTED_SYMBOL= new RPartitionNodeType() {
		
		@Override
		public String getPartitionType() {
			return IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE;
		}
		
		@Override
		protected char getEndChar() {
			return '`';
		}
		
		@Override
		public byte getScannerState() {
			return RPartitionNodeScanner.S_QUOTED_SYMBOL;
		}
		
		@Override
		public boolean prefereAtEnd(final ITreePartitionNode node, final IDocument document) {
			return !isClosed(node, document);
		}
		
	};
	
	public static final RPartitionNodeType INFIX_OPERATOR= new RPartitionNodeType() {
		
		@Override
		public String getPartitionType() {
			return IRDocumentConstants.R_INFIX_OPERATOR_CONTENT_TYPE;
		}
		
		@Override
		public byte getScannerState() {
			return RPartitionNodeScanner.S_INFIX_OPERATOR;
		}
		
		@Override
		protected char getEndChar() {
			return '%';
		}
		
		@Override
		public boolean prefereAtEnd(final ITreePartitionNode node, final IDocument document) {
			return !isClosed(node, document);
		}
		
	};
	
	public static abstract class Comment extends RPartitionNodeType {
		
		
		public Comment() {
		}
		
		
		@Override
		public boolean prefereAtEnd(final ITreePartitionNode node, final IDocument document) {
			return true;
		}
		
	}
	
	public static final Comment COMMENT= new Comment() {
		
		@Override
		public String getPartitionType() {
			return IRDocumentConstants.R_COMMENT_CONTENT_TYPE;
		}
		
		@Override
		public byte getScannerState() {
			return RPartitionNodeScanner.S_COMMENT;
		}
		
	};
	
	
	public static final Comment ROXYGEN= new Comment() {
		
		@Override
		public String getPartitionType() {
			return IRDocumentConstants.R_ROXYGEN_CONTENT_TYPE;
		}
		
		@Override
		public byte getScannerState() {
			return RPartitionNodeScanner.S_ROXYGEN;
		}
		
	};
	
	
	protected RPartitionNodeType() {
	}
	
	
	public abstract byte getScannerState();
	
	protected char getEndChar() {
		return 0;
	}
	
	
	protected final boolean isClosed(final ITreePartitionNode node, final IDocument document) {
		try {
			return (node.getLength() >= 2 
					&& document.getChar(node.getOffset() + node.getLength() - 1) == getEndChar() );
		}
		catch (final BadLocationException e) {
			return false;
		}
	}
	
	protected final boolean isEndingByLineDelimeter(final ITreePartitionNode node, final IDocument document) {
		try {
			final int c= document.getChar(node.getOffset() + node.getLength());
			return (c == '\n' || c == '\r');
		}
		catch (final BadLocationException e) {
			return false;
		}
	}
	
}
