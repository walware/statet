/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IElementAccess;
import de.walware.statet.r.core.model.IEnvirInSource;
import de.walware.statet.r.core.model.IRClass;
import de.walware.statet.r.core.model.IRClassExtension;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRPackageLoad;
import de.walware.statet.r.core.model.IRSlot;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.RAst;


abstract class RSourceElementByElementAccess extends AbstractRModelElement
		implements IRLangElement, IRLangElementWithSource {
	
	
	static class RPkgImport extends RSourceElementByElementAccess implements IRPackageLoad {
		
		
		public RPkgImport(final AbstractRModelElement parent, final IElementAccess access) {
			super(parent, IRLangElement.R_PACKAGE_LOAD, access);
		}
		
	}
	
	static class RMethod extends RSourceElementByElementAccess implements IRMethod {
		
		
		private Envir fEnvir;
		
		private FDef fFDefNode;
		private ArgsDefinition fArgs;
		
		
		public RMethod(final AbstractRModelElement parent, final Envir envir, final FDef fdefNode) {
			super(parent, IRLangElement.R_COMMON_FUNCTION, null);
			fEnvir = envir;
			fFDefNode = fdefNode;
		}
		
		void complete(final int type, final IElementAccess defAccess, final ArgsDefinition args) {
			fType = type;
			fAccess = defAccess;
			fArgs = args;
		}
		
		
		public RMethod(final AbstractRModelElement parent, final int type, final IElementAccess access, final Envir envir) {
			super(parent, type, access);
			fFDefNode = null;
			fEnvir = envir;
		}
		
		void complete(final ArgsDefinition args) {
			fArgs = args;
		}
		
		
		public FDef getFDefNode() {
			return fFDefNode;
		}
		
		public ArgsDefinition getArgsDefinition() {
			return fArgs;
		}
		
		@Override
		public Object getAdapter(final Class required) {
			if (IEnvirInSource.class.equals(required)) {
				return fEnvir;
			}
			return super.getAdapter(required);
		}
		
	}
	
	static class RClass extends RSourceElementByElementAccess implements IRClass {
		
		
		private static final List<String> NO_PARENTS = Arrays.asList(new String[0]);
		
		
		private Envir fEnvir;
		
		private List<String> fSuperClassesTypeNames = NO_PARENTS;
		private List<String> fSuperClassesTypeNamesProtected = NO_PARENTS;
		
		
		public RClass(final AbstractRModelElement parent, final ElementAccess defAccess, final Envir envir) {
			super(parent, IRLangElement.R_S4CLASS, defAccess);
			fEnvir = envir;
		}
		
		
		void addSuperClasses(final String[] typeNames) {
			if (fSuperClassesTypeNames == NO_PARENTS) {
				int count = 0;
				for (final String name : typeNames) {
					if (name != null) {
						count++;
					}
				}
				if (count == 0) {
					return;
				}
				fSuperClassesTypeNames = new ArrayList<String>(count);
				fSuperClassesTypeNamesProtected = Collections.unmodifiableList(fSuperClassesTypeNames);
			}
			for (final String name : typeNames) {
				if (name != null && !fSuperClassesTypeNames.contains(name)) {
					fSuperClassesTypeNames.add(name);
				}
			}
		}
		
		
		public List<String> getExtendedClassNames() {
			return fSuperClassesTypeNamesProtected;
		}
		
		@Override
		public Object getAdapter(final Class required) {
			if (IEnvirInSource.class.equals(required)) {
				return fEnvir;
			}
			return super.getAdapter(required);
		}
		
	}
	
	static class RClassExt extends RSourceElementByElementAccess implements IRClassExtension {
		
		
		private IEnvirInSource fEnvir;
		
		private String fExtCommand;
		private String fExtTypeName;
		
		
		public RClassExt(final AbstractRModelElement parent, 
				final ElementAccess defAccess, final IEnvirInSource envir, final String command) {
			super(parent, IRLangElement.R_S4CLASS_EXTENSION, defAccess);
			fExtCommand = command;
			fEnvir = envir;
		}
		
		void complete(final String extTypeName) {
			fExtTypeName = extTypeName;
		}
		
		public String getExtCommand() {
			return fExtCommand;
		}
		
		public String getExtTypeName() {
			return fExtTypeName;
		}
		
		@Override
		public Object getAdapter(final Class required) {
			if (IEnvirInSource.class.equals(required)) {
				return fEnvir;
			}
			return super.getAdapter(required);
		}
		
	}
	
	static class RVariable extends RSourceElementByElementAccess {
		
		
		public RVariable(final AbstractRModelElement parent, final int elementType, final ElementAccess defAccess) {
			super(parent, elementType, defAccess);
		}
		
	}
	
	static class RSlot extends RSourceElementByElementAccess implements IRSlot {
		
		
		private String fTypeName;
		private String fPrototypeCode;
		
		
		public RSlot(final AbstractRModelElement parent, final ElementAccess defAccess) {
			super(parent, IRLangElement.R_S4SLOT, defAccess);
		}
		
		void completeType(final String name) {
			fTypeName = name;
		}
		
		public String getTypeName() {
			return fTypeName;
		}
		
	}
	
	
	private final AbstractRModelElement fParent;
	protected IElementAccess fAccess;
	int fType;
	int fOccurenceCount;
	
	
	public RSourceElementByElementAccess(final AbstractRModelElement parent, final int elementType, final IElementAccess defAccess) {
		fParent = parent;
		fType = elementType;
		fAccess = defAccess;
	}
	
	
	public IElementAccess getAccess() {
		return fAccess;
	}
	
	public IModelElement getParent() {
		return fParent;
	}
	
	public ISourceUnit getSourceUnit() {
		return fParent.getSourceUnit();
	}
	
	public int getElementType() {
		return fType;
	}
	
	public IElementName getElementName() {
		return fAccess;
	}
	
	public String getId() {
		final String name = getElementName().getDisplayName();
		final StringBuilder sb = new StringBuilder(name.length() + 10);
		sb.append(Integer.toHexString(fType & MASK_C2));
		sb.append(':');
		sb.append(name);
		sb.append('#');
		sb.append(fOccurenceCount);
		return sb.toString();
	}
	
	public boolean exists() {
		return fParent.exists();
	}
	
	public boolean isReadOnly() {
		return fParent.isReadOnly();
	}
	
	
	public IRegion getNameSourceRange() {
		IElementAccess access = fAccess;
		while (access.getNextSegment() != null) {
			access = access.getNextSegment();
		}
		if (access.getNameNode() != null) {
			return RAst.getElementNameRegion(access.getNameNode());
		}
		else {
			return new Region(access.getNode().getOffset(), 0);
		}
	}
	
	public IRegion getSourceRange() {
		return fAccess.getNode();
	}
	
	
	public Object getAdapter(final Class required) {
		if (IAstNode.class.equals(required)) {
			return fAccess.getNode();
		}
		if (IElementAccess.class.equals(required)) {
			return fAccess;
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return (fType & MASK_C2) * getElementName().hashCode() + fOccurenceCount;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RSourceElementByElementAccess)) {
			return false;
		}
		final RSourceElementByElementAccess other = (RSourceElementByElementAccess) obj;
		return ((fType & MASK_C2) == (other.fType & MASK_C2))
				&& (fOccurenceCount == other.fOccurenceCount)
				&& ( ((fType & MASK_C1) == C1_SOURCE) || (getParent().equals(other.getParent())) )
				&& (getElementName().equals(other.getElementName()));
	}
	
}
