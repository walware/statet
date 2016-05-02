/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.buildpaths.core.BuildpathElement;
import de.walware.ecommons.ltk.buildpaths.core.BuildpathElementType;
import de.walware.ecommons.ltk.buildpaths.core.BuildpathsUtils;
import de.walware.ecommons.ltk.buildpaths.core.IBuildpathAttribute;
import de.walware.ecommons.ltk.buildpaths.core.IBuildpathElement;

import de.walware.statet.r.core.RBuildpaths;


public class RBuildpathPrefs {
	
	
	public static final String STAMP_KEY= "stamp"; //$NON-NLS-1$
	private static final String VERSION_KEY= "version"; //$NON-NLS-1$
	
	private static final String PATH_PREF_KEY= "path"; //$NON-NLS-1$
	
	private static final ImList<IPath> DEFAULT_INCLUSION_PATTERNS= ImCollections.newList();
	private static final ImList<IPath> DEFAULT_EXCLUSION_PATTERNS= ImCollections.<IPath>newList(
			new Path(".Rcheck/") ); //$NON-NLS-1$
	
	
	private final IEclipsePreferences rootNode;
	
	private final IProject project;
	
	
	public RBuildpathPrefs(final IScopeContext context, final String qualifier,
				final IProject project) {
		this.rootNode= context.getNode(qualifier);
		this.project= project;
	}
	
	
	public ImList<IBuildpathElement> load() {
		final List<IBuildpathElement> elements= new ArrayList<>();
		
		{	final String childName= IBuildpathElement.SOURCE + ".0";
			final Preferences entryNode= this.rootNode.node(childName);
			final BuildpathElementType type= RBuildpaths.R_SOURCE_TYPE;
			
			final IPath path= this.project.getFullPath();
			ImList<IPath> inclusionPatterns= BuildpathsUtils.decodePatterns(
					entryNode.get(IBuildpathAttribute.FILTER_INCLUSIONS, null) );
			if (inclusionPatterns == null) {
				inclusionPatterns= DEFAULT_INCLUSION_PATTERNS;
			}
			ImList<IPath> exclusionPatterns= BuildpathsUtils.decodePatterns(
					entryNode.get(IBuildpathAttribute.FILTER_EXCLUSIONS, null) );
			if (exclusionPatterns == null) {
				exclusionPatterns= DEFAULT_EXCLUSION_PATTERNS;
			}
			
			elements.add(new BuildpathElement(type, path,
					inclusionPatterns, exclusionPatterns,
					null, null, null, null, true, null ));
		}
		
		return ImCollections.toList(elements);
	}
	
	public void save(final List<IBuildpathElement> elements, final boolean flush) {
		try {
			for (final IBuildpathElement element : elements) {
				if (element.getType() == RBuildpaths.R_SOURCE_TYPE
						&& element.getPath().equals(this.project.getFullPath()) ) {
					final String childName= IBuildpathElement.SOURCE + ".0";
					final Preferences entryNode= this.rootNode.node(childName);
					
					IPath path= element.getPath();
					if (path.isAbsolute() && this.project.getFullPath().isPrefixOf(path)) {
						path= path.removeFirstSegments(1).makeRelative();
					}
					else {
						continue;
					}
					entryNode.put(PATH_PREF_KEY, path.toPortableString());
					entryNode.put(IBuildpathAttribute.FILTER_INCLUSIONS,
							BuildpathsUtils.encodePatterns(element.getInclusionPatterns()) );
					entryNode.put(IBuildpathAttribute.FILTER_EXCLUSIONS,
							BuildpathsUtils.encodePatterns(element.getExclusionPatterns()) );
					
					this.rootNode.putInt(VERSION_KEY, 1);
					this.rootNode.putLong(STAMP_KEY, System.currentTimeMillis());
					
					if (flush) {
						this.rootNode.flush();
					}
				}
			}
		}
		catch (final BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
