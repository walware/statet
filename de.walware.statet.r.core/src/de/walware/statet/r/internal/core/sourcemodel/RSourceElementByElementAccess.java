/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRClass;
import de.walware.statet.r.core.model.IRClassExtension;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.IRPackageLoad;
import de.walware.statet.r.core.model.IRSlot;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.DocuComment;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.RAst;


abstract class RSourceElementByElementAccess
		implements IRLangSourceElement, IModelElement.Filter {
	
	
	static final class RPkgImport extends RSourceElementByElementAccess implements IRPackageLoad {
		
		
		public RPkgImport(final IRLangSourceElement parent, final ElementAccess access) {
			super(parent, IRElement.R_PACKAGE_LOAD, access);
		}
		
		
		@Override
		public IRegion getDocumentationRange() {
			return null;
		}
		
		
		@Override
		public boolean hasModelChildren(final IModelElement.Filter filter) {
			return false;
		}
		
		@Override
		public final List<? extends IRLangSourceElement> getModelChildren(final IModelElement.Filter filter) {
			return RSourceElements.NO_R_SOURCE_CHILDREN;
		}
		
		@Override
		public boolean hasSourceChildren(final IModelElement.Filter filter) {
			return false;
		}
		
		@Override
		public List<? extends IRLangSourceElement> getSourceChildren(final IModelElement.Filter filter) {
			return RSourceElements.NO_R_SOURCE_CHILDREN;
		}
		
	}
	
	static final class RMethod extends RSourceElementByElementAccess implements IRMethod, IBuildSourceFrameElement {
		
		
		private List<? extends IRLangSourceElement> fSourceChildrenProtected = RSourceElements.NO_R_SOURCE_CHILDREN;
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
		
		void complete(final AnonymousAccess defAccess, final ArgsDefinition args) {
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
		
		@Override
		public void setSourceChildren(final List<? extends IRLangSourceElement> children) {
			fSourceChildrenProtected = children;
		}
		
		@Override
		public BuildSourceFrame getBuildFrame() {
			return fEnvir;
		}
		
		
		@Override
		public IRegion getDocumentationRange() {
			return fDocu;
		}
		
		public FDef getFDefNode() {
			return fFDefNode;
		}
		
		@Override
		public ArgsDefinition getArgsDefinition() {
			return fArgs;
		}
		
		
		@Override
		public boolean hasModelChildren(final IModelElement.Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.hasChildren(fModelChildrenProtected, filter);
		}
		
		@Override
		public List<? extends IRLangSourceElement> getModelChildren(final IModelElement.Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.getChildren(fModelChildrenProtected, filter);
		}
		
		@Override
		public boolean hasSourceChildren(final IModelElement.Filter filter) {
			return RSourceElements.hasChildren(fSourceChildrenProtected, filter);
		}
		
		@Override
		public List<? extends IRLangSourceElement> getSourceChildren(final IModelElement.Filter filter) {
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
		
		
		private static final List<String> NO_PARENTS = Collections.emptyList();
		
		
		private List<? extends IRLangSourceElement> fSourceChildrenProtected = RSourceElements.NO_R_SOURCE_CHILDREN;
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
		
		@Override
		public void setSourceChildren(final List<? extends IRLangSourceElement> children) {
			fSourceChildrenProtected = children;
		}
		
		@Override
		public BuildSourceFrame getBuildFrame() {
			return fEnvir;
		}
		
		
		@Override
		public DocuComment getDocumentationRange() {
			return fDocu;
		}
		
		@Override
		public List<String> getExtendedClassNames() {
			return fSuperClassesTypeNamesProtected;
		}
		
		
		@Override
		public boolean hasModelChildren(final IModelElement.Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.hasChildren(fModelChildrenProtected, filter);
		}
		
		@Override
		public List<? extends IRLangSourceElement> getModelChildren(final IModelElement.Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.getChildren(fModelChildrenProtected, filter);
		}
		
		@Override
		public boolean hasSourceChildren(final IModelElement.Filter filter) {
			return RSourceElements.hasChildren(fSourceChildrenProtected, filter);
		}
		
		@Override
		public List<? extends IRLangSourceElement> getSourceChildren(final IModelElement.Filter filter) {
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
		
		
		private List<? extends IRLangSourceElement> fSourceChildrenProtected = RSourceElements.NO_R_SOURCE_CHILDREN;
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
		
		@Override
		public void setSourceChildren(final List<? extends IRLangSourceElement> children) {
			fSourceChildrenProtected = children;
		}
		
		@Override
		public BuildSourceFrame getBuildFrame() {
			return fEnvir;
		}
		
		
		@Override
		public String getExtCommand() {
			return fExtCommand;
		}
		
		@Override
		public String getExtTypeName() {
			return fExtTypeName;
		}
		
		@Override
		public IRegion getDocumentationRange() {
			return null;
		}
		
		
		@Override
		public boolean hasModelChildren(final IModelElement.Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.hasChildren(fModelChildrenProtected, filter);
		}
		
		@Override
		public List<? extends IRLangSourceElement> getModelChildren(final IModelElement.Filter filter) {
			if (fModelChildrenProtected == null) {
				fModelChildrenProtected = fEnvir.getModelChildren(this);
			}
			return RSourceElements.getChildren(fModelChildrenProtected, filter);
		}
		
		@Override
		public boolean hasSourceChildren(final IModelElement.Filter filter) {
			return RSourceElements.hasChildren(fSourceChildrenProtected, filter);
		}
		
		@Override
		public List<? extends IRLangSourceElement> getSourceChildren(final IModelElement.Filter filter) {
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
		
		
		@Override
		public DocuComment getDocumentationRange() {
			return fDocu;
		}
		
		
		@Override
		public boolean hasModelChildren(final IModelElement.Filter filter) {
			return false;
		}
		
		@Override
		public final List<? extends IRLangSourceElement> getModelChildren(final IModelElement.Filter filter) {
			return RSourceElements.NO_R_SOURCE_CHILDREN;
		}
		
		@Override
		public boolean hasSourceChildren(final IModelElement.Filter filter) {
			return false;
		}
		
		@Override
		public List<? extends IRLangSourceElement> getSourceChildren(final IModelElement.Filter filter) {
			return RSourceElements.NO_R_SOURCE_CHILDREN;
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
		
		
		@Override
		public String getTypeName() {
			return fTypeName;
		}
		
		@Override
		public IRegion getDocumentationRange() {
			return null;
		}
		
		
		@Override
		public boolean hasModelChildren(final IModelElement.Filter filter) {
			return false;
		}
		
		@Override
		public final List<? extends IRLangSourceElement> getModelChildren(final IModelElement.Filter filter) {
			return RSourceElements.NO_R_SOURCE_CHILDREN;
		}
		
		@Override
		public boolean hasSourceChildren(final IModelElement.Filter filter) {
			return false;
		}
		
		@Override
		public List<? extends IRLangSourceElement> getSourceChildren(final IModelElement.Filter filter) {
			return RSourceElements.NO_R_SOURCE_CHILDREN;
		}
		
	}
	
	
	private final IRLangSourceElement fParent;
	private RElementAccess fAccess;
	int fType;
	int fOccurrenceCount;
	
	
	public RSourceElementByElementAccess(final IRLangSourceElement parent, final int elementType, final ElementAccess defAccess) {
		fParent = parent;
		fType = elementType;
		setAccess(defAccess);
	}
	
	
	protected void setAccess(final AnonymousAccess access) {
		if (access != null) {
			fAccess = access;
		}
	}
	
	protected void setAccess(final ElementAccess access) {
		if (access != null) {
			access.fModelElement = this;
			fAccess = access;
		}
	}
	
	@Override
	public final String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	public final RElementAccess getAccess() {
		return fAccess;
	}
	
	@Override
	public boolean include(final IModelElement element) {
		return (element == this);
	}
	
	@Override
	public final IRElement getModelParent() {
		final List<? extends IRElement> elements = fAccess.getFrame().getModelElements();
		for (final IRElement element : elements) {
			if (element.hasModelChildren(this)) {
				return element;
			}
		}
		return null;
	}
	
	@Override
	public final ISourceStructElement getSourceParent() {
		return fParent;
	}
	
	@Override
	public final IRSourceUnit getSourceUnit() {
		return fParent.getSourceUnit();
	}
	
	@Override
	public final int getElementType() {
		return fType;
	}
	
	@Override
	public final RElementName getElementName() {
		return fAccess;
	}
	
	@Override
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
	
	@Override
	public final boolean exists() {
		return fParent.exists();
	}
	
	@Override
	public final boolean isReadOnly() {
		return fParent.isReadOnly();
	}
	
	
	@Override
	public final IRegion getSourceRange() {
		return fAccess.getNode();
	}
	
	@Override
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
	
	
	@Override
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
