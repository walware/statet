/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.pkgmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RService;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.core.RCorePlugin;


class RViewTasks {
	
	
	private static class CRANViewHandler extends DefaultHandler {
		
		private static final int S_VIEW = 1;
		private static final int S_NAME = 2;
		private static final int S_TOPIC = 3;
		private static final int S_PKGLIST = 4;
		private static final int S_PKGLIST_PKG = 5;
		private static final int S_SKIP = 9;
		
		
		private final List<RView> fViewList;
		
		private RView fView;
		
		private final StringBuilder fString = new StringBuilder();
		
		private int fState = -1;
		private int fDepth;
		private final int[] fStateDepth = new int[10];
		private int fSkipState;
		
		
		public CRANViewHandler(final List<RView> viewList) {
			fViewList = viewList;
		}
		
		
		@Override
		public void startDocument() throws SAXException {
			fView = null;
			fState = 0;
		}
		
		@Override
		public void endDocument() throws SAXException {
			fState = -1;
		}
		
		private void enter(final int newState) {
			fState = newState;
			fStateDepth[newState] = fDepth;
		}
		
		private void exit(final int newState) {
			fState = newState;
		}
		
		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
				throws SAXException {
			fDepth++;
			switch (fState) {
			case 0:
				if (localName.equals("CRANTaskView")) { //$NON-NLS-1$
					enter(S_VIEW);
					fView = null;
					return;
				}
				break;
			case S_VIEW:
				if (localName.equals("name")) { //$NON-NLS-1$
					if (fView == null) {
						enter(S_NAME);
						fString.setLength(0);
						return;
					}
				}
				else if (localName.equals("topic")) { //$NON-NLS-1$
					if (fView != null) {
						enter(S_TOPIC);
						fString.setLength(0);
						return;
					}
				}
				else if (localName.equals("packagelist")) { //$NON-NLS-1$
					if (fView != null) {
						enter(S_PKGLIST);
						return;
					}
				}
				break;
			case S_PKGLIST:
				if (localName.equals("pkg")) { //$NON-NLS-1$
					enter(S_PKGLIST_PKG);
					fString.setLength(0);
					return;
				}
				break;
			case S_SKIP:
				return;
			}
			
			fSkipState = fState;
			enter(S_SKIP);
		}
		
		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			if (fState > 0 && fStateDepth[fState] == fDepth) {
				switch (fState) {
				case S_VIEW:
					if (fView != null && !fView.getPkgList().isEmpty()) {
						fViewList.add(fView);
						fView = null;
					}
					exit(0);
					break;
				case S_PKGLIST:
					exit(S_VIEW);
					break;
				case S_NAME:
					if (fString.length() > 0) {
						fView = new RView(fString.toString());
					}
					exit(S_VIEW);
					break;
				case S_TOPIC:
					if (fString.length() > 0) {
						fView.setTopic(fString.toString());
					}
					exit(S_VIEW);
					break;
				case S_PKGLIST_PKG:
					if (fString.length() > 0) {
						fView.getPkgList().add(fString.toString().intern());
					}
					exit(S_PKGLIST);
					break;
				case S_SKIP:
					exit(fSkipState);
					break;
				}
			}
			fDepth--;
		}
		
		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			switch (fState) {
			case S_NAME:
			case S_TOPIC:
			case S_PKGLIST_PKG:
				fString.append(ch, start, length);
				break;
			}
		}
	}
	
	
	static List<RView> loadRViews(final RService r, final IProgressMonitor monitor) {
		Exception error = null;
		monitor.subTask("Loading R task views...");
		try {
			final String dir;
			{	final FunctionCall call = r.createFunctionCall("system.file"); //$NON-NLS-1$
				call.addChar("ctv"); //$NON-NLS-1$
				call.addChar("package", "ctv"); //$NON-NLS-1$ //$NON-NLS-2$
				dir = RDataUtil.checkSingleChar(call.evalData(monitor));
			}
			if (dir == null || dir.equals("")) { //$NON-NLS-1$
				return null;
			}
			
			final String[] files ;
			{	final FunctionCall call = r.createFunctionCall("dir"); //$NON-NLS-1$
				call.addChar("path", dir); //$NON-NLS-1$
				final RCharacterStore data = RDataUtil.checkRCharVector(call.evalData(monitor)).getData();
				files = data.toArray();
			}
			
			final List<RView> views = new ArrayList<RView>(files.length);
			{	final String sep = r.getPlatform().getFileSep();
				final CRANViewHandler handler = new CRANViewHandler(views);
				final XMLReader reader = XMLReaderFactory.createXMLReader();
				reader.setContentHandler(handler);
				for (int i = 0; i < files.length; i++) {
					if (files[i].endsWith(".ctv")) { //$NON-NLS-1$
						final byte[] file = r.downloadFile(dir + sep + files[i], 0, monitor);
						if (file.length > 0) {
							reader.parse(new InputSource(new ByteArrayInputStream(file)));
						}
					}
				}
			}
			
			return views;
		}
		catch (final UnexpectedRDataException e) {
			error = e;
		}
		catch (final IOException e) {
			error = e;
		}
		catch (final SAXException e) {
			error = e;
		}
		catch (final CoreException e) {
			error = e;
		}
		RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
				"An error occurred when loading R task views.", error));
		return null;
	}
	
	//	private void loadBioCViews(final RService r, final ISelectedRepos repoSettings,
	//	final AvailableRPkgs pkgs, final Change event,
	//	final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException, IOException {
	//	final IRPkgInfo pkg = getInstalled(pkgs, "biocViews"); //$NON-NLS-1$
	//	if (pkg == null || (pkg.getVersion().equals(fBioCViewsVersion)
	//				&& Math.abs(event.stamp - fBioCViewsStamp) < MIRROR_CHECK ) ) {
	//		return;
	//	}
	//	
	//	Collection<RRepo> repos = repoSettings.getRepos();
	//	r.evalVoid("data(biocViewsVocab, package= 'biocViews', envir= rj::.rj.tmp)", monitor);
	//	for (RRepo repo : repos) {
	//		if (repo.getId().startsWith(RRepo.BIOC_ID_PREFIX)) {
	//			try {
	//				RObject data = r.evalData("biocViews:::getBiocViews('" + repo.getURL() + "', " +
	//						"rj::.rj.tmp$biocViewsVocab, 'NoViewProvided')", monitor );
	//				System.out.println(data);
	//			}
	//			catch (CoreException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}
	//	final String dir;
	//	{	final FunctionCall call = r.createFunctionCall("system.file"); //$NON-NLS-1$
	//		call.addChar("ctv"); //$NON-NLS-1$
	//		call.addChar("package", "ctv"); //$NON-NLS-1$
	//		dir = RDataUtil.checkSingleChar(call.evalData(monitor));
	//	}
	//	if (dir == null || dir.equals("")) { //$NON-NLS-1$
	//		return;
	//	}
	//	
	//	final String[] files ;
	//	{	final FunctionCall call = r.createFunctionCall("dir"); //$NON-NLS-1$
	//		call.addChar("path", dir); //$NON-NLS-1$
	//		final RCharacterStore data = RDataUtil.checkRCharVector(call.evalData(monitor)).getData();
	//		files = data.toArray();
	//	}
	//	
	//	fBioCViews = views;
	//	fBioCViewsVersion = pkg.getVersion();
	//	fBioCViewsStamp = event.stamp;
	//	event.fViews = true;
	//}
	
}
