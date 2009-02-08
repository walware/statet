/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - concepts in JDT
 *     Stephan Wahlbrink - initial API in StatET
 *******************************************************************************/

package de.walware.ecommons.ltk.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import de.walware.ecommons.ltk.IProblem;
import de.walware.ecommons.ltk.IProblemRequestor;


/**
 * Abstract annotation model dealing with marker annotations and temporary problems.
 * Also acts as problem requester for its source unit.
 */
public abstract class SourceAnnotationModel extends ResourceMarkerAnnotationModel
		implements IProblemRequestor {
	
	private static class ProblemRequestorState {
		List<IProblem> reportedProblems = new ArrayList<IProblem>();
		int sequenceCount = 0;
	}
	
	private final ThreadLocal<ProblemRequestorState> fProblemRequestorState = new ThreadLocal<ProblemRequestorState>();
	
	private final List<SourceProblemAnnotation> fProblemAnnotations = new ArrayList<SourceProblemAnnotation>();
	
//	private ReverseMap fReverseMap= new ReverseMap();
//	private List fPreviouslyOverlaid= null;
//	private List fCurrentlyOverlaid= new ArrayList();
	
	
	public SourceAnnotationModel(final IResource resource) {
		super(resource);
	}
	
	protected abstract boolean isHandlingTemporaryProblems();
	
//	@Override
//	protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
//		if (JavaMarkerAnnotation.isJavaAnnotation(marker))
//			return new JavaMarkerAnnotation(marker);
//		return super.createMarkerAnnotation(marker);
//	}
	
//	@Override
//	protected AnnotationModelEvent createAnnotationModelEvent() {
//		return new CompilationUnitAnnotationModelEvent(this, getResource());
//	}
	
	public void beginReportingSequence() {
		ProblemRequestorState state = fProblemRequestorState.get();
		if (state == null) {
			state = new ProblemRequestorState();
			fProblemRequestorState.set(state);
		}
		state.sequenceCount++;
	}
	
	public void endReportingSequence() {
		final ProblemRequestorState state = fProblemRequestorState.get();
		if (state == null) {
			return;
		}
		state.sequenceCount--;
		if (state.sequenceCount == 0) {
			reportProblems(state.reportedProblems);
			fProblemRequestorState.remove();
		}
	}
	
	public void acceptProblems(final String type, final List<IProblem> problems) {
		if (isHandlingTemporaryProblems()) {
			final ProblemRequestorState state = fProblemRequestorState.get();
			if (state != null) {
				state.reportedProblems.addAll(problems);
			}
		}
	}
	
	public void clearProblems(final String type) {
		synchronized (getLockObject()) {
			if (fProblemAnnotations.size() > 0) {
				removeAnnotations(fProblemAnnotations, true, true);
				fProblemAnnotations.clear();
			}
		}
	}
	
	private void reportProblems(final List<IProblem> reportedProblems) {
		boolean reportedProblemsChanged = false;
		
		synchronized (getLockObject()) {
//			fPreviouslyOverlaid = fCurrentlyOverlaid;
//			fCurrentlyOverlaid = new ArrayList();
			
			if (fProblemAnnotations.size() > 0) {
				reportedProblemsChanged = true;
				removeAnnotations(fProblemAnnotations, false, true);
				fProblemAnnotations.clear();
			}
			
			if (reportedProblems != null && reportedProblems.size() > 0) {
				for (final IProblem problem : reportedProblems) {
					final Position position = createPosition(problem);
					if (position != null) {
						try {
							final SourceProblemAnnotation annotation = createAnnotation(problem);
//							overlayMarkers(position, annotation);
							if (annotation != null) {
								addAnnotation(annotation, position, false);
								fProblemAnnotations.add(annotation);
								reportedProblemsChanged = true;
							}
						} catch (final BadLocationException x) {
							// ignore invalid position
						}
					}
				}
			}
			
//			removeMarkerOverlays(isCanceled);
//			fPreviouslyOverlaid= null;
		}
		
		if (reportedProblemsChanged) {
			fireModelChanged();
		}
	}
	
//	private void overlayMarkers(Position position, ProblemAnnotation problemAnnotation) {
//		Object value= getAnnotations(position);
//		if (value instanceof List) {
//			List list= (List) value;
//			for (Iterator e = list.iterator(); e.hasNext();)
//				setOverlay(e.next(), problemAnnotation);
//		} else {
//			setOverlay(value, problemAnnotation);
//		}
//	}
//
//	private void setOverlay(Object value, ProblemAnnotation problemAnnotation) {
//		if (value instanceof  JavaMarkerAnnotation) {
//			JavaMarkerAnnotation annotation= (JavaMarkerAnnotation) value;
//			if (annotation.isProblem()) {
//				annotation.setOverlay(problemAnnotation);
//				fPreviouslyOverlaid.remove(annotation);
//				fCurrentlyOverlaid.add(annotation);
//			}
//		} else {
//		}
//	}
//
//	private void removeMarkerOverlays(boolean isCanceled) {
//		if (isCanceled) {
//			fCurrentlyOverlaid.addAll(fPreviouslyOverlaid);
//		} else if (fPreviouslyOverlaid != null) {
//			Iterator e= fPreviouslyOverlaid.iterator();
//			while (e.hasNext()) {
//				JavaMarkerAnnotation annotation= (JavaMarkerAnnotation) e.next();
//				annotation.setOverlay(null);
//			}
//		}
//	}
	
	protected Position createPosition(final IProblem problem) {
		final int start = problem.getSourceStartOffset();
		final int end = problem.getSourceStopOffset();
		if (start < 0 && end < 0) {
			assert (start >= 0 && end >= 0);
		}
		return new Position(start, end-start);
	}
	
	protected SourceProblemAnnotation createAnnotation(final IProblem problem) {
		return null;
	}
	
//	@Override
//	protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged) throws BadLocationException {
//		super.addAnnotation(annotation, position, fireModelChanged);
//
//		synchronized (getLockObject()) {
//			Object cached = fReverseMap.get(position);
//			if (cached == null)
//				fReverseMap.put(position, annotation);
//			else if (cached instanceof List) {
//				List list= (List) cached;
//				list.add(annotation);
//			} else if (cached instanceof Annotation) {
//				List list= new ArrayList(2);
//				list.add(cached);
//				list.add(annotation);
//				fReverseMap.put(position, list);
//			}
//		}
//	}
//
//	@Override
//	protected void removeAllAnnotations(boolean fireModelChanged) {
//		super.removeAllAnnotations(fireModelChanged);
//		synchronized (getLockObject()) {
//			fReverseMap.clear();
//		}
//	}
//
//	@Override
//	protected void removeAnnotation(Annotation annotation, boolean fireModelChanged) {
//		Position position = getPosition(annotation);
//		synchronized (getLockObject()) {
//			Object cached= fReverseMap.get(position);
//			if (cached instanceof List) {
//				List list= (List) cached;
//				list.remove(annotation);
//				if (list.size() == 1) {
//					fReverseMap.put(position, list.get(0));
//					list.clear();
//				}
//			} else if (cached instanceof Annotation) {
//				fReverseMap.remove(position);
//			}
//		}
//		super.removeAnnotation(annotation, fireModelChanged);
//	}
	
}
