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

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRClass;
import de.walware.statet.r.core.model.IRClassExtension;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRPackageLoad;
import de.walware.statet.r.core.model.IRSlot;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.DocuComment;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.RAst;


abstract class RSourceElementByElementAccess
		implements IRElement, IRLangSourceElement, IModelElement.Filter<IModelElement> {
	
	
	static final class RPkgImport extends RSourceElementByElementAccess implements IRPackageLoad {
		
		
		public RPkgImport(final IRLangSourceElement parent, final ElementAccess access) {
			super(parent, IRElement.R_PACKAGE_LOAD, access);
		}
		
		
		public IRegion getDocumentationRange() {
			return null;
		}
		
		
		public boolean hasModelChildren(final Filter filter) {
			return false;
		}
		
		public final List<? extends IRLangSourceElement> getModelChildren(final Filter filter) {
			return NO_R_SOURCE_CHILDREN;
		}
		
		public boolean hasSourceChildren(final Filter filter) {
			return false;
		}
		
		public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
			return NO_R_SOURCE_CHILDREN;
		}
		
	}
	
	static final class RMethod extends RSourceElementByElementAccess implements IRMethod, IBuildSourceFrameElement {
		
		
		private List<? extends IRLangSourceElement> fSourceChildrenProtected = NO_R_SOURCE_CHILDREN;
		private List<? extends IRLangSourceElement> fModelChildrenProtected;
		private final BuildSourceFrame fEnvir;
		
		private FDef fFDefNode;
		private ArgsDefinition fArgs;
		
		DocuComment fDocu;
		
		
		public RMethod(final IRLangSourceElement parent, final BuildSourceFrame envir, final FDef fdefNode) {
			super(parent, IRElement.R_COMMON_FUNCTION, null);
			fEnvir = envir;
			fFDefNode = fdefNode;
		}
		
		void complete(final int type, final ElementAccess defAccess, final ArgsDefinition args) {
			fType = type;
			setAccess(defAccess);
			fArgs = args;
		}
		
		
		public RMethod(final IRLangSourceElement parent, final int type, final ElementAccess access, final BuildSourceFrame envir) {
			super(parent, type, access);
			fEnvir = envir;
			fFDefNode = null;
		}
		
		public void complete(final ArgsDefinition args) {
			fArgs = args;
		}
		
		public void setSourceChildren(final List<? extends IRLangSourceElement> children) {
			fSourceChildrenProtected = children;
		}
		
		public BuildSourceFrame getBuildFrame() {
			return fEnvir;
		}
		
		
		public IRegion getDocumentationRange() {
			return fDocu;
		}
		
		public FDef getFDefNode() {
			return fFDefNode;
		}
		
		public ArgsDefinition getArgsDefinition() {
			return fArgs;
		}
		
		
		public boolean hasModelChildren(final Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.hasChildren(fModelChildrenProtected, filter);
		}
		
		public List<? extends IRLangSourceElement> getModelChildren(final Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.getChildren(fModelChildrenProtected, filter);
		}
		
		public boolean hasSourceChildren(final Filter filter) {
			return RSourceElements.hasChildren(fSourceChildrenProtected, filter);
		}
		
		public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
			return RSourceElements.getChildren(fSourceChildrenProtected, filter);
		}
		
		
		@Override
		public Object getAdapter(final Class required) {
			if (IRFrame.class.equals(required)) {
				return fEnvir;
			}
			if (FDef.class.equals(required)) {
				return fFDefNode;
			}
			return super.getAdapter(required);
		}
		
	}
	
	static final class RClass extends RSourceElementByElementAccess implements IRClass, IBuildSourceFrameElement {
		
		
		private static final List<String> NO_PARENTS = Arrays.asList(new String[0]);
		
		
		private List<? extends IRLangSourceElement> fSourceChildrenProtected = NO_R_SOURCE_CHILDREN;
		private List<? extends IRLangSourceElement> fModelChildrenProtected;
		private final BuildSourceFrame fEnvir;
		
		private List<String> fSuperClassesTypeNames = NO_PARENTS;
		private List<String> fSuperClassesTypeNamesProtected = NO_PARENTS;
		
		DocuComment fDocu;
		
		
		public RClass(final IRLangSourceElement parent, final ElementAccess defAccess, final BuildSourceFrame envir) {
			super(parent, IRElement.R_S4CLASS, defAccess);
			fEnvir = envir;
		}
		
		public void addSuperClasses(final String[] typeNames) {
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
		
		public void setSourceChildren(final List<? extends IRLangSourceElement> children) {
			fSourceChildrenProtected = children;
		}
		
		public BuildSourceFrame getBuildFrame() {
			return fEnvir;
		}
		
		
		public DocuComment getDocumentationRange() {
			return fDocu;
		}
		
		public List<String> getExtendedClassNames() {
			return fSuperClassesTypeNamesProtected;
		}
		
		
		public boolean hasModelChildren(final Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.hasChildren(fModelChildrenProtected, filter);
		}
		
		public List<? extends IRLangSourceElement> getModelChildren(final Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.getChildren(fModelChildrenProtected, filter);
		}
		
		public boolean hasSourceChildren(final Filter filter) {
			return RSourceElements.hasChildren(fSourceChildrenProtected, filter);
		}
		
		public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
			return RSourceElements.getChildren(fSourceChildrenProtected, filter);
		}
		
		
		@Override
		public Object getAdapter(final Class required) {
			if (IRFrame.class.equals(required)) {
				return fEnvir;
			}
			return super.getAdapter(required);
		}
		
	}
	
	static final class RClassExt extends RSourceElementByElementAccess implements IRClassExtension, IBuildSourceFrameElement {
		
		
		private List<? extends IRLangSourceElement> fSourceChildrenProtected = NO_R_SOURCE_CHILDREN;
		private List<? extends IRLangSourceElement> fModelChildrenProtected;
		private final BuildSourceFrame fEnvir;
		
		private String fExtCommand;
		private String fExtTypeName;
		
		
		public RClassExt(final IRLangSourceElement parent, 
				final ElementAccess defAccess, final BuildSourceFrame envir, final String command) {
			super(parent, IRElement.R_S4CLASS_EXTENSION, defAccess);
			fEnvir = envir;
			fExtCommand = command;
		}
		
		public void complete(final String extTypeName) {
			fExtTypeName = extTypeName;
		}
		
		public void setSourceChildren(final List<? extends IRLangSourceElement> children) {
			fSourceChildrenProtected = children;
		}
		
		public BuildSourceFrame getBuildFrame() {
			return fEnvir;
		}
		
		
		public String getExtCommand() {
			return fExtCommand;
		}
		
		public String getExtTypeName() {
			return fExtTypeName;
		}
		
		public IRegion getDocumentationRange() {
			return null;
		}
		
		
		public boolean hasModelChildren(final Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.hasChildren(fModelChildrenProtected, filter);
		}
		
		public List<? extends IRLangSourceElement> getModelChildren(final Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.getChildren(fModelChildrenProtected, filter);
		}
		
		public boolean hasSourceChildren(final Filter filter) {
			return RSourceElements.hasChildren(fSourceChildrenProtected, filter);
		}
		
		public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
			return RSourceElements.getChildren(fSourceChildrenProtected, filter);
		}
		
		
		@Override
		public Object getAdapter(final Class required) {
			if (IRFrame.class.equals(required)) {
				return fEnvir;
			}
			return super.getAdapter(required);
		}
		
	}
	
	static final class RVariable extends RSourceElementByElementAccess {
		
		
		DocuComment fDocu;
		
		public RVariable(final IRLangSourceElement parent, final int elementType, final ElementAccess defAccess) {
			super(parent, elementType, defAccess);
		}
		
		
		public DocuComment getDocumentationRange() {
			return fDocu;
		}
		
		
		public boolean hasModelChildren(final Filter filter) {
			return false;
		}
		
		public final List<? extends IRLangSourceElement> getModelChildren(final Filter filter) {
			return NO_R_SOURCE_CHILDREN;
		}
		
		public boolean hasSourceChildren(final Filter filter) {
			return false;
		}
		
		public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
			return NO_R_SOURCE_CHILDREN;
		}
		
	}
	
	static final class RSlot extends RSourceElementByElementAccess implements IRSlot {
		
		
		private String fTypeName;
		private String fPrototypeCode;
		
		
		public RSlot(final IRLangSourceElement parent, final ElementAccess defAccess) {
			super(parent, IRElement.R_S4SLOT, defAccess);
		}
		
		void completeType(final String name) {
			fTypeName = name;
		}
		
		
		public String getTypeName() {
			return fTypeName;
		}
		
		public IRegion getDocumentationRange() {
			return null;
		}
		
		
		public boolean hasModelChildren(final Filter filter) {
			return false;
		}
		
		public final List<? extends IRLangSourceElement> getModelChildren(final Filter filter) {
			return NO_R_SOURCE_CHILDREN;
		}
		
		public boolean hasSourceChildren(final Filter filter) {
			return false;
		}
		
		public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
			return NO_R_SOURCE_CHILDREN;
		}
		
	}
	
	
	private final ISourceStructElement fParent;
	private ElementAccess fAccess;
	int fType;
	int fOccurrenceCount;
	
	
	public RSourceElementByElementAccess(final IRLangSourceElement parent, final int elementType, final ElementAccess defAccess) {
		fParent = parent;
		fType = elementType;
		setAccess(defAccess);
	}
	
	
	protected void setAccess(final ElementAccess access) {
		if (access != null) {
			access.fModelElement = this;
			fAccess = access;
		}
	}
	
	public final String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	public final ElementAccess getAccess() {
		return fAccess;
	}
	
	public boolean include(final IModelElement element) {
		return (element == this);
	}
	
	public final IRElement getModelParent() {
		final List<? extends IRElement> elements = fAccess.getFrame().getModelElements();
		for (final IRElement element : elements) {
			if (element.hasModelChildren(this)) {
				return element;
			}
		}
		return null;
	}
	
	public final ISourceStructElement getSourceParent() {
		return fParent;
	}
	
	public final ISourceUnit getSourceUnit() {
		return fParent.getSourceUnit();
	}
	
	public final int getElementType() {
		return fType;
	}
	
	public final RElementName getElementName() {
		return fAccess;
	}
	
	public final String getId() {
		final String name = getElementName().getDisplayName();
		final StringBuilder sb = new StringBuilder(name.length() + 10);
		sb.append(Integer.toHexString(fType & MASK_C2));
		sb.append(':');
		sb.append(name);
		sb.append('#');
		sb.append(fOccurrenceCount);
		return sb.toString();
	}
	
	public final boolean exists() {
		return fParent.exists();
	}
	
	public final boolean isReadOnly() {
		return fParent.isReadOnly();
	}
	
	
	public final IRegion getSourceRange() {
		return fAccess.getNode();
	}
	
	public final IRegion getNameSourceRange() {
		RElementAccess access = fAccess;
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
	
	
	public Object getAdapter(final Class required) {
		if (IAstNode.class.equals(required)) {
			return fAccess.getNode();
		}
		if (RElementAccess.class.equals(required)) {
			return fAccess;
		}
		return null;
	}
	
	
	@Override
	public int hashCode() {
		return (fType & MASK_C2) * getElementName().hashCode() + fOccurrenceCount;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RSourceElementByElementAccess)) {
			return false;
		}
		final RSourceElementByElementAccess other = (RSourceElementByElementAccess) obj;
		return ((fType & MASK_C2) == (other.fType & MASK_C2))
				&& (fOccurrenceCount == other.fOccurrenceCount)
				&& ( ((fType & MASK_C1) == C1_SOURCE) || (getSourceParent().equals(other.getSourceParent())) )
				&& (getElementName().equals(other.getElementName()));
	}
	
}
