/*=============================================================================#
 # Copyright (c) 2014-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.core.SourceContent;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.text.core.ILineInformation;
import de.walware.ecommons.workbench.search.ui.LineElement;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.refactoring.RElementSearchProcessor;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public class RElementSearch extends RElementSearchProcessor {
	
	
	private final boolean searchWrite;
	
	RElementSearchResult result;
	
	private final StringBuilder sb= new StringBuilder();
	
	
	public RElementSearch(final RElementName name,
			final IRSourceUnit sourceUnit, final RElementAccess mainAccess,
			final Mode mode, final boolean searchWrite) {
		super(name, sourceUnit, mainAccess, mode, ALLOW_SUB_NAMEDPART);
		
		this.searchWrite= searchWrite;
	}
	
	
	public final boolean searchWrite() {
		return this.searchWrite;
	}
	
	@Override
	protected void begin(final SubMonitor progress) {
		super.begin(progress);
		
		this.result.removeAll();
	}
	
	@Override
	protected void process(final IRProject project, final List<ISourceUnit> sus,
			final SubMonitor progress) throws BadLocationException {
		if (sus != null) {
			int remaining= sus.size();
			for (final ISourceUnit su : sus) {
				progress.setWorkRemaining(remaining--);
				
				process((IRSourceUnit) su, progress.newChild(1));
			}
		}
	}
	
	protected void process(final IRSourceUnit sourceUnit, final SubMonitor progress) throws BadLocationException {
		progress.setWorkRemaining(10);
		sourceUnit.connect(progress.newChild(1));
		try {
			IRSourceUnit bestUnit= sourceUnit;
			if (bestUnit.getUnderlyingUnit() != null && bestUnit.isSynchronized()) {
				bestUnit= sourceUnit;
			}
			
			final IRModelInfo modelInfo= (IRModelInfo) sourceUnit.getModelInfo(RModel.R_TYPE_ID,
					IRModelManager.MODEL_FILE, progress.newChild(1) );
			final SourceContent content= sourceUnit.getContent(progress.newChild(1));
			
			final List<List<? extends RElementAccess>> allFrameAccess= new ArrayList<>();
			for (final String frameId : this.definitionFrameIds) {
				final IRFrame frame;
				if (frameId == null) {
					frame= modelInfo.getTopFrame();
				}
				else {
					frame= modelInfo.getReferencedFrames().get(frameId);
				}
				if (frame instanceof IRFrameInSource) {
					final List<? extends RElementAccess> allAccess= ((IRFrameInSource) frame).getAllAccessOf(
							this.mainName.getSegmentName(), true );
					if (allAccess != null && allAccess.size() > 0) {
						allFrameAccess.add(allAccess);
					}
				}
			}
			
			final String contentText= content.getText();
			final ILineInformation lineInformation= content.getLines();
			final Map<Integer, LineElement<IRSourceUnit>> lineElements= new HashMap<>();
			
			for (final List<? extends RElementAccess> allAccess : allFrameAccess) {
				for (RElementAccess access : allAccess) {
					access= include(access);
					if (access != null) {
						final RAstNode nameNode= access.getNameNode();
						final IRegion nameRegion= RAst.getElementNameRegion(nameNode);
						
						final Integer lineNumber= Integer.valueOf(
								lineInformation.getLineOfOffset(nameRegion.getOffset()) );
						LineElement<IRSourceUnit> lineElement= lineElements.get(lineNumber);
						if (lineElement == null) {
							final int lineOffset= lineInformation.getLineOffset(lineNumber);
							lineElement= new LineElement<>(bestUnit, lineNumber, lineOffset,
									getContent(contentText, lineOffset, lineOffset + lineInformation.getLineLength(lineNumber)) );
							lineElements.put(lineNumber, lineElement);
						}
						
						this.result.addMatch(new RElementMatch(lineElement,
								nameRegion.getOffset(), nameRegion.getLength(),
								(access.isWriteAccess() && access.getNextSegment() == null) ));
					}
				}
			}
			progress.setWorkRemaining(1);
		}
		finally {
			sourceUnit.disconnect(progress.newChild(1));
		}
	}
	
	private RElementAccess include(RElementAccess access) {
		access= searchMatch(access);
		return (access != null && access.isMaster()
						&& (!searchWrite() || access.isWriteAccess()) ) ?
				access : null;
	}
	
	private String getContent(final String text, final int start, final int end) {
		this.sb.setLength(0);
		for (int idx= start; idx < end; idx++) {
			final char c= text.charAt(idx);
			if (Character.isWhitespace(c) || Character.isISOControl(c)) {
				this.sb.append(' ');
			} else {
				this.sb.append(c);
			}
		}
		return this.sb.toString();
	}
	
}
