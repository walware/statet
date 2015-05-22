/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.OperationCanceledException;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;
import de.walware.ecommons.ltk.core.model.IWorkspaceSourceUnit;

import de.walware.statet.r.core.model.ArgsBuilder;
import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RCoreFunctions;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.IRSourceConstants;
import de.walware.statet.r.core.rsource.ast.Arithmetic;
import de.walware.statet.r.core.rsource.ast.Assignment;
import de.walware.statet.r.core.rsource.ast.CForLoop;
import de.walware.statet.r.core.rsource.ast.CIfElse;
import de.walware.statet.r.core.rsource.ast.CLoopCommand;
import de.walware.statet.r.core.rsource.ast.CRepeatLoop;
import de.walware.statet.r.core.rsource.ast.CWhileLoop;
import de.walware.statet.r.core.rsource.ast.Dummy;
import de.walware.statet.r.core.rsource.ast.FCall;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.Help;
import de.walware.statet.r.core.rsource.ast.Logical;
import de.walware.statet.r.core.rsource.ast.Model;
import de.walware.statet.r.core.rsource.ast.NSGet;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.NullConst;
import de.walware.statet.r.core.rsource.ast.NumberConst;
import de.walware.statet.r.core.rsource.ast.Power;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAst.ReadedFCallArgs;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RAstVisitor;
import de.walware.statet.r.core.rsource.ast.Relational;
import de.walware.statet.r.core.rsource.ast.Seq;
import de.walware.statet.r.core.rsource.ast.Sign;
import de.walware.statet.r.core.rsource.ast.SourceComponent;
import de.walware.statet.r.core.rsource.ast.Special;
import de.walware.statet.r.core.rsource.ast.StringConst;
import de.walware.statet.r.core.rsource.ast.SubIndexed;
import de.walware.statet.r.core.rsource.ast.SubNamed;
import de.walware.statet.r.core.rsource.ast.Symbol;
import de.walware.statet.r.internal.core.sourcemodel.BuildSourceFrame.ElementAccessList;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RClass;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RClassExt;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RMethod;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RPkgImport;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RSlot;


/**
 * Walks through the AST, analyzes element access, ...
 * 
 * Saves the information in {@link IRModelInfo}
 */
public class SourceAnalyzer extends RAstVisitor {
	
	
	private static final int S_GLOBAL= 0;
	private static final int S_LOCAL= 1;
	private static final int S_SEARCH= 2;
	
	private static final int RETURN_SOURCE_CONTAINTER= 1;
	private static final int RETURN_METHOD_SIGNATURE= 2;
	private static final int RETURN_STRING_ARRAY= 3;
	private static final int REG_CLASS_REPRESENTATION= 4;
	private static final int REG_CLASS_PROTOTYPE= 5;
	
	private static final int[] NO_REQUESTS= { };
	private static final int[] STRING_ARRAY_REQUEST= {
		RETURN_STRING_ARRAY };
	private static final int[] SIGNATURE_REQUESTS= {
		RETURN_METHOD_SIGNATURE, RETURN_STRING_ARRAY };
	private static final int[] REPRESENTATION_REQUEST= {
		REG_CLASS_REPRESENTATION };
	private static final int[] PROTOTYPE_REQUEST= {
		REG_CLASS_PROTOTYPE };
	
	private static final Integer FIRST= Integer.valueOf(0);
	
	
	private static final Comparator<ISourceStructElement> SOURCEELEMENT_SORTER= new Comparator<ISourceStructElement>() {
		@Override
		public int compare(final ISourceStructElement e1, final ISourceStructElement e2) {
			return (e1.getSourceRange().getOffset() - e2.getSourceRange().getOffset());
		}
	};
	
	
	private static class ReturnValue {
		
		final int returnType;
		
		ReturnValue(final int returnType) {
			this.returnType= returnType;
		}
	}
	
	private static class NodeArray extends ReturnValue {
		
		final RAstNode[] array;
		
		NodeArray(final int returnType, final RAstNode[] array) {
			super(returnType);
			this.array= array;
		}
	}
	
	
	private static class SourceElementBuilder extends ReturnValue {
		
		
		private final SourceElementBuilder parent;
		private final IBuildSourceFrameElement element;
		private final List<RSourceElementByElementAccess> children;
		
		private final List<ElementAccess> toCheck;
		private final BuildSourceFrame envir;
		
		
		SourceElementBuilder(final IBuildSourceFrameElement element,
				final SourceElementBuilder parent, final BuildSourceFrame envir) {
			super(RETURN_SOURCE_CONTAINTER);
			this.element= element;
			this.parent= parent;
			this.children= new ArrayList<>();
			this.toCheck= new ArrayList<>();
			this.envir= envir;
		}
		
		
		@Override
		public String toString() {
			final StringBuilder sb= new StringBuilder("SourceAnalyzer$SourceElementBuilder"); //$NON-NLS-1$
			sb.append("\n\tfor= ").append(this.element); //$NON-NLS-1$
			sb.append("\n\tin= ").append(this.envir); //$NON-NLS-1$
			
			return sb.toString();
		}
		
	}
	
	private static class Signature extends ReturnValue {
		
		/** the access objects are not yet registered */
		private final ElementAccess[] argNameAccess;
		private final String[] classNames;
		
		Signature(final ElementAccess[] argNameAccess, final String[] classNames) {
			super(RETURN_METHOD_SIGNATURE);
			this.argNameAccess= argNameAccess;
			this.classNames= classNames;
		}
		
	}
	
	private class RoxygenAdapter implements IRoxygenAnalyzeContext {
		
		
		private RSourceModelInfo fModelInfo;
		private int fCounter;
		
		
		@Override
		public IRModelInfo getModelInfo() {
			return this.fModelInfo;
		}
		
		@Override
		public void createSelfAccess(final IRLangSourceElement element, final RAstNode symbol) {
			final String text= symbol.getText();
			if (text == null) {
				return;
			}
			final RElementAccess elementAccess= ((RSourceElementByElementAccess) element).getAccess();
			if (elementAccess != null
					&& text.equals(elementAccess.getSegmentName())
					&& elementAccess.getNextSegment() == null ) {
				final ElementAccess access= new ElementAccess.Default(symbol.getRParent(), symbol);
				((ElementAccess) elementAccess).fShared.postAdd(access);
				return;
			}
		}
		
		@Override
		public void createNamespaceImportAccess(final RAstNode symbol) {
			final String text= symbol.getText();
			if (text == null) {
				return;
			}
			final ElementAccess access= new ElementAccess.Package(symbol.getRParent(), symbol);
			access.fFlags |= ElementAccess.A_IMPORT;
			this.fModelInfo.fPackageRefs.add(text, access);
		}
		
		@Override
		public void createNamespaceObjectImportAccess(final IRFrameInSource namespace, final RAstNode symbol) {
			final String text= symbol.getText();
			if (text == null) {
				return;
			}
			if (namespace instanceof BuildSourceFrame) {
				final ElementAccess access= new ElementAccess.Default(symbol.getRParent(), symbol);
				
				final BuildSourceFrame namespaceFrame= (BuildSourceFrame) namespace;
				final ElementAccessList namespaceList= namespaceFrame.fData.get(text);
				
				final BuildSourceFrame next= this.fModelInfo.fLocalFrames.values().iterator().next();
				final ElementAccessList defaultList= next.fData.get(text);
				if (defaultList != null && defaultList.isCreated < BuildSourceFrame.CREATED_RESOLVED) {
					next.fData.remove(text);
					if (namespaceList != null) {
						namespaceList.entries.addAll(defaultList.entries);
						for (final ElementAccess defaultAccess : defaultList.entries) {
							defaultAccess.fShared= namespaceList;
						}
						namespaceList.postAdd(access);
					}
					else {
						defaultList.frame= namespaceFrame;
						defaultList.postAdd(access);
						namespaceFrame.fData.put(text, defaultList);
					}
				}
				else {
					if (namespaceList != null) {
						namespaceList.postAdd(access);
					}
					else {
						final ElementAccessList accessList= new BuildSourceFrame.ElementAccessList(text);
						accessList.frame= namespaceFrame;
						accessList.postAdd(access);
						namespaceFrame.fData.put(text, accessList);
					}
				}
			}
		}
		
		@Override
		public IRFrameInSource getNamespaceFrame(final String name) {
			final String id= BuildSourceFrame.createId(IRFrame.PACKAGE, name, -1);
			BuildSourceFrame frame= this.fModelInfo.fNamespaceFrames.get(id);
			if (frame == null) {
				frame= new BuildSourceFrame.DefScope(IRFrame.PACKAGE, id, name, null);
				this.fModelInfo.fNamespaceFrames.put(id, frame);
				return frame;
			}
			return null;
		}
		
		@Override
		public void createSlotAccess(final RClass rClass, final RAstNode symbol) {
			final String text= symbol.getText();
			if (text == null) {
				return;
			}
			final ElementAccessList accessList= rClass.getBuildFrame().fData.get(text);
			if (accessList == null) {
				return;
			}
			final List<? extends IRLangSourceElement> children= rClass.getSourceChildren(null);
			for (final IRLangSourceElement child : children) {
				if (child.getElementType() == IRElement.R_S4SLOT
						&& text.equals(child.getElementName().getSegmentName())) {
					final ElementAccess access= new ElementAccess.Slot(symbol.getRParent(), symbol);
					accessList.postAdd(access);
					return;
				}
			}
		}
		
		@Override
		public void createArgAccess(final RMethod rMethod, final RAstNode symbol) {
			final String text= symbol.getText();
			if (text == null) {
				return;
			}
			final ElementAccessList accessList= rMethod.getBuildFrame().fData.get(text);
			if (accessList == null) {
				return;
			}
			final List<? extends IRLangSourceElement> children= rMethod.getSourceChildren(null);
			for (final IRLangSourceElement child : children) {
				if (child.getElementType() == IRElement.R_ARGUMENT
						&& text.equals(child.getElementName().getSegmentName())) {
					final ElementAccess access= new ElementAccess.Default(symbol.getRParent(), symbol);
					access.fFlags |= ElementAccess.A_ARG;
					accessList.postAdd(access);
					return;
				}
			}
		}
		
		@Override
		public void createRSourceRegion(final RAstNode node) {
			if (!SourceAnalyzer.this.roxygenExamples) {
				this.fCounter= 0;
				cleanup();
				init();
				SourceAnalyzer.this.roxygenExamples= true;
			}
			try {
				final RoxygenRCodeElement element= new RoxygenRCodeElement(this.fModelInfo.getSourceElement(), this.fCounter++, SourceAnalyzer.this.topLevelEnvir, node);
				enterElement(element, SourceAnalyzer.this.topLevelEnvir, node);
				node.acceptInRChildren(SourceAnalyzer.this);
				leaveElement();
			}
			catch (final InvocationTargetException unused) {}
		}
		
		public void update(final RSourceModelInfo modelInfo) {
			this.fModelInfo= modelInfo;
			SourceAnalyzer.this.roxygenAnalyzer.updateModel(SourceAnalyzer.this.roxygenAdapter);
		}
		
	}
	
	
	private IRSourceUnit sourceUnit;
	private List<RChunkBuildElement> chunkElements;
	private AstInfo ast;
	
	private int anonymCount;
	private final ArrayList<String> idComponents= new ArrayList<>(32);
	private LinkedHashMap<String, BuildSourceFrame> frames;
	private Map<String, BuildSourceFrame> dependencyEnvironments;
	private final ArrayList<BuildSourceFrame> currentEnvironments= new ArrayList<>(32);
	private BuildSourceFrame globalEnvir;
	private BuildSourceFrame genericDefaultEnvir;
	private BuildSourceFrame topLevelEnvir;
	private BuildSourceFrame topScope;
	private PackageReferences packageRefs;
	
	private final LinkedList<RAstNode> argValueToIgnore= new LinkedList<>();
	private int[] request= NO_REQUESTS;
	private Object returnValue;
	
	private final ArrayList<SourceElementBuilder> sourceContainerBuilders= new ArrayList<>();
	private SourceElementBuilder currentSourceContainerBuilder;
	
	private RCoreFunctions configuredRDef;
	private final Map<String, IFCallAnalyzer> fCallAnalyzers= new HashMap<>();
	private IFCallAnalyzer fCallFallback;
	private final IFCallAnalyzer fCallNoAnalysis= new IFCallAnalyzer() {
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
	};
	
	private final RoxygenAnalyzer roxygenAnalyzer;
	private final RoxygenAdapter roxygenAdapter;
	private boolean roxygenExamples;
	
	private final HashMap<String, Integer> commonNames= new HashMap<>();
	private final HashMap<String, Integer> classNames= new HashMap<>();
	private final HashMap<String, Integer> importNames= new HashMap<>();
	
	
	public SourceAnalyzer() {
		configure(RCoreFunctions.DEFAULT);
		this.roxygenAnalyzer= new RoxygenAnalyzer();
		this.roxygenAdapter= new RoxygenAdapter();
	}
	
	public void configure(final RCoreFunctions rdef) {
		this.configuredRDef= rdef;
		this.fCallAnalyzers.clear();
		IFCallAnalyzer analyzer;
		
		this.fCallAnalyzers.put(RCoreFunctions.BASE_ASSIGN_NAME, new BaseAssign(rdef));
		analyzer= new BaseRemove(rdef);
		this.fCallAnalyzers.put(RCoreFunctions.BASE_REMOVE_NAME, analyzer);
		this.fCallAnalyzers.put(RCoreFunctions.BASE_REMOVE_ALIAS_RM, analyzer);
		this.fCallAnalyzers.put(RCoreFunctions.BASE_EXISTS_NAME,
				new BaseExists(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.BASE_GET_NAME, 
				new BaseGet(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.BASE_SAVE_NAME,
				new BaseSave(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.BASE_CALL_NAME,
				new BaseCall(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.BASE_DOCALL_NAME,
				new BaseDoCall(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.BASE_LIBRARY_NAME,
				new BaseLibrary(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.BASE_REQUIRE_NAME,
				new BaseRequire(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.BASE_GLOBALENV_NAME,
				new BaseGlobalenv(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.BASE_TOPENV_NAME,
				new BaseTopenv(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.BASE_C_NAME,
				new BaseC(rdef));
		
		this.fCallAnalyzers.put(RCoreFunctions.BASE_USEMETHOD_NAME,
				new CommonDefBased(rdef.BASE_USEMETHOD_args));
		this.fCallAnalyzers.put(RCoreFunctions.BASE_NEXTMETHOD_NAME,
				new CommonDefBased(rdef.BASE_NEXTMETHOD_args));
		this.fCallAnalyzers.put(RCoreFunctions.UTILS_METHODS_NAME,
				new CommonDefBased(rdef.UTILS_METHODS_args));
		
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SETGENERIC_NAME,
				new MethodsSetGeneric(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SETGROUPGENERIC_NAME,
				new MethodsSetGroupGeneric(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_REMOVEGENERIC_NAME,
				new MethodsRemoveGeneric(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_ISGENERIC_NAME,
				new CommonDefBased(rdef.METHODS_ISGENERIC_args));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_ISGROUP_NAME,
				new CommonDefBased(rdef.METHODS_ISGROUP_args));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SIGNATURE_NAME,
				new MethodsSignature(rdef));
		
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SETCLASS_NAME,
				new MethodsSetClass(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SETCLASSUNION_NAME,
				new MethodsSetClassUnion(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_REPRESENTATION_NAME,
				new MethodsRepresentation(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_PROTOTYPE_NAME,
				new MethodsPrototype(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SETIS_NAME,
				new MethodsSetIs(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_REMOVECLASS_NAME,
				new MethodsRemoveClass(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_RESETCLASS_NAME,
				this.fCallNoAnalysis);
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SETAS_NAME,
				new MethodsSetAs(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SETVALIDITY_NAME,
				new MethodsSetValidity(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_ISCLASS_NAME,
				new CommonDefBased(rdef.METHODS_ISCLASS_args));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_GETCLASS_NAME,
				new MethodsGetClass(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_GETCLASSDEF_NAME,
				new MethodsGetClassDef(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_FINDCLASS_NAME,
				new MethodsFindClass(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_EXTENDS_NAME,
				new CommonDefBased(rdef.METHODS_EXTENDS_args));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_IS_NAME,
				new CommonDefBased(rdef.METHODS_IS_args));
		
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_NEW_NAME,
				new MethodsNew(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_AS_NAME,
				new MethodsAs(rdef));
		
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SETMETHOD_NAME,
				new MethodsSetMethod(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_REMOVEMETHOD_NAME,
				new MethodsRemoveMethod(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_REMOVEMETHODS_NAME,
				new MethodsRemoveMethods(rdef));
		
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_HASMETHOD_NAME,
				new CommonDefBased(rdef.METHODS_HASMETHOD_args));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_EXISTSMETHOD_NAME,
				new CommonDefBased(rdef.METHODS_EXISTSMETHOD_args));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_GETMETHOD_NAME,
				new MethodsGetMethod(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SELECTMETHOD_NAME,
				new MethodsSelectMethod(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_GETMETHODS_NAME,
				new MethodsGetMethods(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_FINDMETHOD_NAME,
				new MethodsFindMethod(rdef));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_DUMPMETHOD_NAME,
				new CommonDefBased(rdef.METHODS_DUMPMETHOD_args));
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_DUMPMETHODS_NAME,
				new CommonDefBased(rdef.METHODS_DUMPMETHOD_args));
		
		this.fCallAnalyzers.put(RCoreFunctions.METHODS_SLOT_NAME,
				new MethodsSlot(rdef));
		
		// DEBUG
//		final Set<String> test= new HashSet<String>();
//		test.addAll(rdef.getKnownFunctions());
//		test.removeAll(fFCallAnalyzers.keySet());
//		System.out.println("nonregistered RCoreFunctions: " + test.toString());
		
		this.fCallFallback= new NoDefFallback();
	}
	
	
	public IRModelInfo createModel(final IRSourceUnit u, final AstInfo ast) {
		if (!(ast.root instanceof SourceComponent)) {
			throw new IllegalArgumentException("ast"); //$NON-NLS-1$
		}
		final SourceComponent root= (SourceComponent) ast.root;
		this.anonymCount= 0;
		this.sourceUnit= u;
		
		try {
			init();
			
			final RSourceUnitElement fileElement= new RSourceUnitElement(this.sourceUnit, this.topLevelEnvir, root);
			enterElement(fileElement, this.topLevelEnvir, root);
			root.acceptInRChildren(this);
			leaveElement();
			
			finish();
			this.ast= new AstInfo(AstInfo.LEVEL_MODEL_DEFAULT, ast);
			final RSourceModelInfo modelInfo= new RSourceModelInfo(this.ast, this.frames, this.topLevelEnvir,
					this.packageRefs, this.dependencyEnvironments, fileElement );
			
			this.roxygenExamples= false;
			this.roxygenAdapter.update(modelInfo);
			if (this.roxygenExamples) {
				finish();
				for (final Iterator<Entry<String, ElementAccessList>> iter= this.topLevelEnvir.fData.entrySet().iterator(); iter.hasNext(); ) {
					final Entry<String, ElementAccessList> entry= iter.next();
					final String name= entry.getKey();
					final ElementAccessList docuList= entry.getValue();
					if (docuList.isCreated == BuildSourceFrame.CREATED_NO) {
						iter.remove();
						final ElementAccessList modelList= modelInfo.fTopFrame.fData.get(name);
						if (modelList != null) {
							for (final ElementAccess access : docuList.entries) {
								access.fShared= modelList;
							}
							modelList.entries.addAll(docuList.entries);
						}
						else {
							docuList.frame= modelInfo.fTopFrame;
							modelInfo.fTopFrame.fData.put(name, docuList);
						}
					}
				}
				for (final Iterator<Entry<String, ElementAccessList>> iter= this.packageRefs.fData.entrySet().iterator(); iter.hasNext(); ) {
					final Entry<String, ElementAccessList> entry= iter.next();
					final String name= entry.getKey();
					final ElementAccessList docuList= entry.getValue();
					if (docuList.isCreated == BuildSourceFrame.CREATED_NO) {
						iter.remove();
						final ElementAccessList modelList= modelInfo.fPackageRefs.fData.get(name);
						if (modelList != null) {
							for (final ElementAccess access : docuList.entries) {
								access.fShared= modelList;
							}
							modelList.entries.addAll(docuList.entries);
						}
						else {
							docuList.frame= modelInfo.fTopFrame;
							modelInfo.fPackageRefs.fData.put(name, docuList);
						}
					}
				}
			}
			
			return modelInfo;
		}
		catch (final OperationCanceledException e) {}
		catch (final InvocationTargetException e) {}
		finally {
			cleanup();
			this.sourceUnit= null;
		}
		return null;
	}
	
	
	public void beginChunkSession(final IRSourceUnit su, final AstInfo ast) {
		this.anonymCount= 0;
		this.sourceUnit= su;
		this.ast= ast;
		if (this.chunkElements == null) {
			this.chunkElements= new ArrayList<>();
		}
		
		init();
	}
	
	public void processChunk(final RChunkBuildElement element, final List<SourceComponent> rootNodes) {
		try {
			this.chunkElements.add(element);
			for (final SourceComponent sourceComponent : rootNodes) {
				element.fEnvir= this.topLevelEnvir;
				enterElement(element, this.topLevelEnvir, sourceComponent);
				sourceComponent.acceptInRChildren(this);
				leaveElement();
			}
		}
		catch (final OperationCanceledException e) {}
		catch (final InvocationTargetException e) {}
	}
	
	public void processInlineNode(final SourceComponent rootNode) {
		try {
			rootNode.acceptInRChildren(this);
		}
		catch (final OperationCanceledException e) {}
		catch (final InvocationTargetException e) {}
	}
	
	public IRModelInfo stopChunkSession() {
		try {
			finish();
			
			final RSourceModelInfo modelInfo= new RSourceModelInfo(this.ast, this.frames,
					this.topLevelEnvir, this.packageRefs, this.dependencyEnvironments,
					new CompositeSourceElement(this.sourceUnit, this.topLevelEnvir,
							this.chunkElements, this.ast.root ));
			return modelInfo;
		}
		finally {
			cleanup();
			this.sourceUnit= null;
			this.chunkElements.clear();
		}
	}
	
	
	private void init() {
		this.frames= new LinkedHashMap<>();
		this.dependencyEnvironments= new HashMap<>();
		final String projId= (this.sourceUnit instanceof IWorkspaceSourceUnit) ?
				((IWorkspaceSourceUnit) this.sourceUnit).getResource().getProject().getName() :
				"<noproject:"+this.sourceUnit.getElementName(); //$NON-NLS-1$
		
		final BuildSourceFrame fileEnvir= new BuildSourceFrame.DefScope(IRFrame.PROJECT,
				BuildSourceFrame.createId(IRFrame.PROJECT, projId, 0), null,
				new BuildSourceFrame[0] ); // ref projects
		
		this.currentEnvironments.add(fileEnvir);
		this.genericDefaultEnvir= this.topLevelEnvir= this.globalEnvir= fileEnvir;
		this.packageRefs= new PackageReferences();
		this.topScope= this.currentEnvironments.get(this.currentEnvironments.size()-1);
		
		this.idComponents.add(projId);
	}
	
	private void finish() {
		for (final BuildSourceFrame si : this.dependencyEnvironments.values()) {
			si.runLateResolve(false);
		}
		this.topLevelEnvir.fParents= ConstArrayList.concat(this.dependencyEnvironments.values().toArray(), this.topLevelEnvir.fParents);
		for (final BuildSourceFrame si : this.frames.values()) {
			si.runLateResolve(false);
		}
		
		final HashMap<String, Integer> commonNames= this.commonNames;
		final HashMap<String, Integer> classNames= this.classNames;
		final HashMap<String, Integer> importNames= this.importNames;
		int anonymous= 0;
		try {
			for (final SourceElementBuilder seb : this.sourceContainerBuilders) {
				if (seb.element.getElementName() == null
						&& seb.element instanceof RSourceElementByElementAccess.RMethod) {
					final RSourceElementByElementAccess.RMethod element= (RSourceElementByElementAccess.RMethod) seb.element;
					element.fOccurrenceCount= anonymous++;
					registerAnonFunctionElement(element, seb.envir);
				}
				for (final RSourceElementByElementAccess element : seb.children) {
					final String name= element.getElementName().getDisplayName();
					final HashMap<String, Integer> names;
					switch (element.fType & IModelElement.MASK_C1) {
					case IModelElement.C1_CLASS:
						names= classNames;
						break;
					case IModelElement.C1_IMPORT:
						names= importNames;
						break;
					default:
						names= commonNames;
						break;
					}
					final Integer occ= names.get(name);
					if (occ == null) {
						names.put(name, FIRST);
					}
					else {
						names.put(name, Integer.valueOf(
								(element.fOccurrenceCount= occ + 1) ));
					}
				}
				for (final ElementAccess access : seb.toCheck) {
					if (seb.envir == access.getFrame()) {
						if (commonNames.containsKey(access.getSegmentName())) {
							continue;
						}
						commonNames.put(access.getSegmentName(), null);
						seb.children.add(new RSourceElementByElementAccess.RVariable(seb.element,
								(seb.envir != this.topLevelEnvir) ? IRElement.R_GENERAL_LOCAL_VARIABLE : IRElement.R_GENERAL_VARIABLE, access));
					}
					else {
	//					seb.children.add(new RSourceElementFromElementAccess.RVariable(seb.element,
	//							IRLangElement.R_COMMON_VARIABLE, access));
					}
				}
				
				final RSourceElementByElementAccess[] finalChildren= seb.children.toArray(new RSourceElementByElementAccess[seb.children.size()]);
				Arrays.sort(finalChildren, SOURCEELEMENT_SORTER);
				if (finalChildren.length > 0) {
					seb.element.setSourceChildren(new ConstArrayList<IRLangSourceElement>(finalChildren));
				}
				
				commonNames.clear();
				classNames.clear();
				importNames.clear();
			}
			
			if (this.chunkElements != null && !this.chunkElements.isEmpty()) {
				final HashMap<String, Integer> names= commonNames;
				for (final RChunkBuildElement element : this.chunkElements) {
					final String name= element.getElementName().getDisplayName();
					final Integer occ= names.get(name);
					if (occ == null) {
						names.put(name, FIRST);
					}
					else {
						names.put(name, Integer.valueOf(
								(element.fOccurrenceCount= occ + 1) ));
					}
				}
			}
		}
		finally {
			commonNames.clear();
			classNames.clear();
			importNames.clear();
		}
	}
	
	private void cleanup() {
		clean(this.currentEnvironments);
		clean(this.idComponents);
		this.argValueToIgnore.clear();
		clean(this.sourceContainerBuilders);
		
		this.ast= null;
		this.genericDefaultEnvir= null;
		this.globalEnvir= null;
		this.packageRefs= null;
		this.topLevelEnvir= null;
		this.frames= null;
		this.dependencyEnvironments= null;
		
		this.returnValue= null;
		this.currentSourceContainerBuilder= null;
	}
	
	private void clean(final ArrayList<?> list) {
		if (list.size() > 2048) {
			list.clear();
			list.trimToSize();
			list.ensureCapacity(1024);
		}
		else {
			list.clear();
		}
	}
	
	
	private BuildSourceFrame getPkgEnvir(final String name) {
		final String id= BuildSourceFrame.createId(IRFrame.PACKAGE, name, ++this.anonymCount);
		BuildSourceFrame envir= this.dependencyEnvironments.get(id);
		if (envir == null) {
			envir= new BuildSourceFrame.DefScope(IRFrame.PACKAGE, id, name, new BuildSourceFrame[0]);
			this.dependencyEnvironments.put(id, envir);
		}
		return envir;
	}
	
	private void registerInEnvir(final int search, final String name, final ElementAccess access) {
		if (access.fShared != null) {
			return;
		}
		switch (search) {
		case S_LOCAL:
			this.topScope.add(name, access);
			return;
		case S_GLOBAL:
			this.globalEnvir.add(name, access);
			return;
		case S_SEARCH:
			this.topScope.addLateResolve(name, access);
			return;
		default:
			throw new IllegalArgumentException("Illegal mode"); //$NON-NLS-1$
		}
	}
	
	private ElementAccess registerSimpleClassAccessInEnvir(final RAstNode refNode, final RAstNode nameNode) {
		final ElementAccess access= new ElementAccess.Class(refNode);
		access.fFlags= ElementAccess.A_READ;
		access.fNameNode= nameNode;
		this.genericDefaultEnvir.addClass(nameNode.getText(), access);
		
		return access;
	}
	
	protected final void enterElement(final IBuildSourceFrameElement element, final BuildSourceFrame envir, final RAstNode node) {
		this.currentSourceContainerBuilder= new SourceElementBuilder(element, this.currentSourceContainerBuilder, envir);
		envir.addFrameElement(element);
		this.frames.put(envir.getFrameId(), envir);
		node.addAttachment(envir);
		this.sourceContainerBuilders.add(this.currentSourceContainerBuilder);
	}
	
	protected final void addEnvirInsteadOfElement(final BuildSourceFrame envir, final RAstNode node) {
		this.frames.put(envir.getFrameId(), envir);
		node.addAttachment(envir);
	}
	
	protected final void leaveElement() {
		this.currentSourceContainerBuilder= this.currentSourceContainerBuilder.parent;
	}
	
	private Object registerSourceElement(final Object value, final ElementAccess access) {
		if (value instanceof RSourceElementByElementAccess) {
			final RSourceElementByElementAccess element= (RSourceElementByElementAccess) value;
			if ((element.getElementType() & IModelElement.MASK_C1) == IModelElement.C1_METHOD) {
				registerFunctionElement((RMethod) value, element.getElementType(), access, null);
				return null;
			}
			
			element.setAccess(access);
			this.currentSourceContainerBuilder.children.add(element);
			access.getNode().addAttachment(element);
			return null;
		}
		else if (access.getSegmentName() != null && access.getType() == RElementName.MAIN_DEFAULT && access.fNextSegment == null) {
			this.currentSourceContainerBuilder.toCheck.add(access);
		}
		return value;
	}
	
	private void registerAnonFunctionElement(final RMethod rMethod, final IRFrame frame) {
		final AnonymousAccess access= new AnonymousAccess(rMethod.getFDefNode(), frame);
		rMethod.complete(access, createMethodArgDef(rMethod.getFDefNode(), null));
		access.getNode().addAttachment(rMethod);
	}
	
	private void registerFunctionElement(final RMethod rMethod, int type,
			final ElementAccess access, final Signature sig) {
		if (rMethod.getElementType() == IRElement.R_COMMON_FUNCTION) {
			final IRFrame frame= access.getFrame();
			if (frame != null && (frame.getFrameType() == IRFrame.FUNCTION || frame.getFrameType() == IRFrame.CLASS)) {
				// make sure it is marked as local
				type |= 0x1;
			}
		}
		rMethod.complete(type, access, createMethodArgDef(rMethod.getFDefNode(), sig));
		if (sig != null && sig.argNameAccess != null) {
			final BuildSourceFrame buildFrame= rMethod.getBuildFrame();
			for (int i= 0; i < sig.argNameAccess.length; i++) {
				if (sig.argNameAccess[i] != null) {
					buildFrame.add(sig.argNameAccess[i].fNameNode.getText(), sig.argNameAccess[i]);
				}
			}
		}
		
		access.fFlags |= ElementAccess.A_FUNC;
		this.currentSourceContainerBuilder.children.add(rMethod);
		access.getNode().addAttachment(rMethod);
	}
	
	private void registerFunctionElement(final RMethod rMethod) {
		this.currentSourceContainerBuilder.children.add(rMethod);
		rMethod.getAccess().getNode().addAttachment(rMethod);
	}
	
	
	private void registerClassElement(final RClass rClass) {
		this.currentSourceContainerBuilder.children.add(rClass);
		rClass.getAccess().getNode().addAttachment(rClass);
	}
	
	private void registerClassExtElement(final RClassExt rClassExt) {
		this.currentSourceContainerBuilder.children.add(rClassExt);
		rClassExt.getAccess().getNode().addAttachment(rClassExt);
	}
	
	private boolean isRequested(final int requestId) {
		for (int i= 0; i < this.request.length; i++) {
			if (this.request[i] == requestId) {
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public void visit(final SourceComponent node) throws InvocationTargetException {
		throw new IllegalArgumentException();
	}
	
	@Override
	public void visit(final Assignment node) throws InvocationTargetException {
		// Value
		this.returnValue= null;
		node.getSourceChild().acceptInR(this);
		final Object returnValue= this.returnValue;
		
		if (node.getNodeType() == NodeType.A_COLON) {
			this.returnValue= null;
			return;
		}
		
		// TODO add direct support for fcall() <- source
		final RAstNode target= node.getTargetChild();
		final ElementAccess access= new ElementAccess.Default(node);
		access.fFlags= ElementAccess.A_WRITE;
		
		final String name= resolveElementName(target, access, true);
		if (name != null || returnValue instanceof RSourceElementByElementAccess) {
			// Resolve
			int mode;
			if (access.getNextSegment() == null) {
				switch (node.getOperator(0)) {
				case ARROW_LEFT_D:
				case ARROW_RIGHT_D:
					mode= S_SEARCH;
					break;
				default:
					mode= S_LOCAL;
					break;
				}
			}
			else {
				mode= S_SEARCH;
			}
			registerInEnvir(mode, name, access);
			
			this.returnValue= registerSourceElement(returnValue, access);
		}
		else {
			this.returnValue= null;
		}
	}
	
	@Override
	public void visit(final CForLoop node) throws InvocationTargetException {
		final Symbol symbol= node.getVarChild();
		final ElementAccess access= new ElementAccess.Default(symbol);
		access.fFlags= ElementAccess.A_WRITE;
		final String name= resolveElementName(symbol, access, false);
		if (name != null) {
			registerInEnvir(S_LOCAL, name, access);
		}
		this.request= NO_REQUESTS;
		node.getCondChild().acceptInR(this);
		node.getContChild().acceptInR(this);
		
		this.returnValue= null;
	}
	
	
	@Override
	public void visit(final FDef node) throws InvocationTargetException {
		final BuildSourceFrame envir= new BuildSourceFrame.DefScope(IRFrame.FUNCTION,
				BuildSourceFrame.createId(IRFrame.FUNCTION, null, ++this.anonymCount),
				null, new BuildSourceFrame[] { this.topScope } );
		this.currentEnvironments.add(envir);
		this.topScope= envir;
		
		final RMethod rMethod;
		if (this.currentSourceContainerBuilder != null) {
			rMethod= new RMethod(this.currentSourceContainerBuilder.element, envir, node);
			enterElement(rMethod, envir, node);
		}
		else {
			rMethod= null;
			addEnvirInsteadOfElement(envir, node);
		}
		
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		if (rMethod != null) {
			leaveElement();
		}
		
		this.currentEnvironments.remove(envir);
		this.topScope= this.currentEnvironments.get(this.currentEnvironments.size()-1);
		
		this.returnValue= rMethod;
	}
	
	@Override
	public void visit(final FDef.Arg node) throws InvocationTargetException {
		final RAstNode nameNode= node.getNameChild();
		if ((nameNode.getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0) {
			final ElementAccess access= new ElementAccess.Default(node);
			access.fFlags= ElementAccess.A_WRITE | ElementAccess.A_ARG;
			access.fNameNode= nameNode;
			registerInEnvir(S_LOCAL, nameNode.getText(), access);
			
			if (this.currentSourceContainerBuilder != null) {
				this.currentSourceContainerBuilder.children.add(new RSourceElementByElementAccess.RVariable(
						this.currentSourceContainerBuilder.element, IRElement.R_ARGUMENT, access));
			}
		}
		
		if (node.hasDefault()) {
			node.getDefaultChild().acceptInR(this);
		}
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final FCall node) throws InvocationTargetException {
		// Resolve
		final RAstNode ref= node.getRefChild();
		final ElementAccess access= new ElementAccess.Default(node, ref);
		access.fFlags= ElementAccess.A_CALL | ElementAccess.A_FUNC;
		
		final String name= resolveElementName(node.getRefChild(), access, true);
		if (name != null) {
			registerInEnvir(S_SEARCH, name, access);
		}
		final boolean write;
		final RAstNode parent= node.getRParent();
		switch ((parent != null) ? parent.getNodeType() : NodeType.DUMMY) {
		case A_LEFT:
		case A_RIGHT:
		case A_EQUALS:
			write= (((Assignment) parent).getTargetChild() == node);
			break;
		default:
			write= false;
			break;
		}
		
		IFCallAnalyzer specialist= null;
		if (name != null) {
			specialist= this.fCallAnalyzers.get(name);
		}
		if (specialist == null) {
			specialist= this.fCallFallback;
		}
		specialist.visit(node, write);
	}
	
	@Override
	public void visit(final FCall.Arg node) throws InvocationTargetException {
		final RAstNode valueNode= node.getValueChild();
		if (valueNode != null) {
			if (!this.argValueToIgnore.remove(valueNode)) {
				valueNode.acceptInR(this);
			}
		}
		
		this.returnValue= null;
	}
	
	public RMethod visitAndCheckValue(final FCall.Arg node, final String name) throws InvocationTargetException {
		assert (name != null);
		if (node != null) {
			final RAstNode valueNode= node.getValueChild();
			if (valueNode != null) {
				valueNode.acceptInR(this);
				this.argValueToIgnore.add(valueNode);
				if (this.returnValue instanceof RMethod) {
					final RMethod rMethod= (RMethod) this.returnValue;
					
					final ElementAccess access= new ElementAccess.Default(node);
					access.fFlags= ElementAccess.A_WRITE | ElementAccess.A_FUNC;
					access.fNameNode= node.getNameChild();
					this.currentSourceContainerBuilder.envir.addRunResolve(name, access);
					
					registerFunctionElement(rMethod, IRElement.R_COMMON_LOCAL_FUNCTION, access, null);
					this.returnValue= null;
					return rMethod;
				}
				else {
					this.returnValue= null;
				}
			}
		}
		return null;
	}
	
	@Override
	public void visit(final SubIndexed.Arg node) throws InvocationTargetException {
		final RAstNode valueNode= node.getValueChild();
		if (valueNode != null) {
			valueNode.acceptInR(this);
		}
	}
	
	@Override
	public void visit(final NSGet node) throws InvocationTargetException {
		final ElementAccess access= new ElementAccess.Default(node);
		access.fFlags= ElementAccess.A_READ;
		final String name= resolveElementName(node, access);
		if (name != null) {
			registerInEnvir(S_LOCAL, name, access);
		}
		
		this.returnValue= access;
	}
	
	@Override
	public void visit(final Symbol node) throws InvocationTargetException {
		final ElementAccess access= new ElementAccess.Default(node);
		access.fFlags= ElementAccess.A_READ;
		final String name= resolveElementName(node, access);
		if (name != null) {
			registerInEnvir(S_SEARCH, name, access);
			
			if (name.equals(".GlobalEnv")) {
				this.returnValue= this.globalEnvir;
				return;
			}
		}
		
		this.returnValue= access;
	}
	
	@Override
	public void visit(final SubNamed node) throws InvocationTargetException {
		final ElementAccess access= new ElementAccess.Default(node);
		access.fFlags= ElementAccess.A_READ;
		final String name= resolvePartName(node, access);
		if (name != null) {
			registerInEnvir(S_SEARCH, name, access);
		}
		
		this.returnValue= access;
	}
	
	@Override
	public void visit(final SubIndexed node) throws InvocationTargetException {
		final ElementAccess access= new ElementAccess.Default(node);
		access.fFlags= ElementAccess.A_READ;
		final String name= resolveElementName(node, access);
		if (name != null) {
			registerInEnvir(S_SEARCH, name, access);
		}
		
		this.returnValue= access;
	}
	
	@Override
	public void visit(final Model node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		this.returnValue= node;
	}
	
	
	@Override
	public void visit(final Help node) throws InvocationTargetException {
		this.returnValue= null;
	}
	
	@Override
	public void visit(final StringConst node) throws InvocationTargetException {
		for (int i= 0; i < this.request.length; i++) {
			if (this.request[i] == RETURN_STRING_ARRAY) {
				this.returnValue= new NodeArray(RETURN_STRING_ARRAY, new StringConst[] { node });
				return;
			}
		}
		this.returnValue= node;
	}
	
	@Override
	public void visit(final NumberConst node) throws InvocationTargetException {
		this.returnValue= null;
	}
	
	@Override
	public void visit(final NullConst node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	
	@Override
	public void visit(final Special node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final Sign node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final Power node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final Arithmetic node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final Seq node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final Relational node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final Logical node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final CIfElse node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final CRepeatLoop node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final CWhileLoop node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final CLoopCommand node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	@Override
	public void visit(final Dummy node) throws InvocationTargetException {
		this.request= NO_REQUESTS;
		node.acceptInRChildren(this);
		
		this.returnValue= null;
	}
	
	
	private String resolveElementName(final RAstNode node, final ElementAccess access,
			final boolean allowString) throws InvocationTargetException {
		switch (node.getNodeType()) {
		case SYMBOL:
			return resolveElementName((Symbol) node, access);
		case STRING_CONST:
			if (allowString && ((node.getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0)) {
				access.fNameNode= node;
				return node.getText();
			}
			return null;
		case SUB_INDEXED_S:
		case SUB_INDEXED_D:
			return resolveElementName((SubIndexed) node, access);
		case SUB_NAMED_PART:
			return resolvePartName((SubNamed) node, access);
		case SUB_NAMED_SLOT:
			return resolveSlotName((SubNamed) node, access);
		case NS_GET:
		case NS_GET_INT:
			return resolveElementName((NSGet) node, access);
		default:
			break;
		}
		if (node == access.fFullNode) {
			node.acceptInRChildren(this);
		}
		else {
			node.acceptInR(this);
		}
		return null;
	}
	
	private String resolveElementName(final Symbol node, final ElementAccess access) {
		if ((node.getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0) {
			access.fNameNode= node;
			return node.getText();
		}
		return null;
	}
	
	private String resolveElementName(final SubIndexed node, final ElementAccess access)
			throws InvocationTargetException {
		final RAstNode child= node.getRefChild();
		final String name= resolveElementName(child, access, false);
		node.getArgsChild().acceptInR(this);
		if (name != null) {
			access.fFlags |= ElementAccess.A_SUB;
			access.appendSubElement((node.getOperator(0) == RTerminal.SUB_INDEXED_D_OPEN) ? 
					new SubIndexedDElementAccess(access, node) : new SubIndexedSElementAccess(access, node));
			return name;
		}
		return null;
	}
	
	private String resolvePartName(final SubNamed node, final ElementAccess access)
			throws InvocationTargetException {
		final RAstNode child= node.getRefChild();
		final String name= resolveElementName(child, access, false);
		if (name != null) {
			access.fFlags |= ElementAccess.A_SUB;
			access.appendSubElement(new SubNamedPartSyntacticElementAccess(access, node));
			return name;
		}
		return null;
	}
	
	private String resolveSlotName(final SubNamed node, final ElementAccess access)
			throws InvocationTargetException {
		final RAstNode child= node.getRefChild();
		final String name= resolveElementName(child, access, false);
		if (name != null) {
			access.fFlags |= ElementAccess.A_SUB;
			access.appendSubElement(new SubNamedSlotSyntacticElementAccess(access, node));
			return name;
		}
		return null;
	}
	
	private String resolveElementName(final NSGet node, final ElementAccess access) {
		final RAstNode namespaceChild= node.getNamespaceChild();
		String namespaceName= null;
		if (isValidPackageName(namespaceChild)) {
			namespaceName= namespaceChild.getText();
			final ElementAccess packageAccess= new ElementAccess.Package(access.fFullNode, namespaceChild);
			this.packageRefs.add(namespaceName, packageAccess);
		}
		// register explicit
		BuildSourceFrame envir;
		if (namespaceName != null &&
				((node.getElementChild().getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0)) {
			envir= getPkgEnvir(namespaceName);
		}
		else {
			envir= this.topScope;
		}
		access.fNameNode= node.getElementChild();
		final String name= access.fNameNode.getText();
		envir.add(name, access);
		return null; // prevent registering in top env
	}
	
	private boolean isValidPackageName(final RAstNode node) {
		switch (node.getNodeType()) {
		case SYMBOL:
			return ((node.getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0);
		case STRING_CONST:
			// TODO check
			return true;
		default:
			return false;
		}
	}
	
	
	protected static interface IFCallAnalyzer {
		
		public void visit(FCall node, boolean assignment) throws InvocationTargetException;
		
	}
	
	protected class CommonVarNamedRead implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_name;
		private final int argIdx_scope;
		
		protected CommonVarNamedRead(final ArgsDefinition argsDef, final String nameArgName, final String scopeArgName) {
			this.argsDef= argsDef;
			this.argIdx_name= this.argsDef.indexOf(nameArgName);
			this.argIdx_scope= this.argsDef.indexOf(scopeArgName);
		}
		
		@Override
		public final void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode nameValue= args.getArgValueNode(this.argIdx_name);
			
			if (nameValue != null && nameValue.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= ElementAccess.A_READ;
				access.fNameNode= nameValue;
				final BuildSourceFrame envir= readScopeArgs(args.getArgValueNode(this.argIdx_scope), SourceAnalyzer.this.topScope);
				if (evalBoolean(args.getArgValueNode("inherits"), false)) {
					envir.addLateResolve(nameValue.getText(), access);
				}
				else {
					envir.add(nameValue.getText(), access);
				}
				
				SourceAnalyzer.this.argValueToIgnore.add(nameValue);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class CommonDefBased implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		
		public CommonDefBased(final ArgsDefinition argsDef) {
			this.argsDef= argsDef;
		}
		
		@Override
		public final void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			
			ITER_ARGS: for (int i= 0; i < args.allocatedArgs.length; i++) {
				final RAstNode argValue= args.getArgValueNode(i);
				if (argValue != null) {
					if ((this.argsDef.get(i).type & ArgsDefinition.METHOD_NAME) != 0
							&& argValue.getNodeType() == NodeType.STRING_CONST) {
						final ElementAccess access= new ElementAccess.Default(node);
						access.fFlags= ElementAccess.A_READ | ElementAccess.A_FUNC;
						access.fNameNode= argValue;
						SourceAnalyzer.this.genericDefaultEnvir.addLateResolve(argValue.getText(), access);
						
						SourceAnalyzer.this.argValueToIgnore.add(argValue);
						continue ITER_ARGS;
					}
					if ((this.argsDef.get(i).type & ArgsDefinition.CLASS_NAME) != 0
							&& argValue.getNodeType() == NodeType.STRING_CONST) {
						registerSimpleClassAccessInEnvir(node, argValue);
						
						SourceAnalyzer.this.argValueToIgnore.add(argValue);
						continue ITER_ARGS;
					}
					if ((this.argsDef.get(i).type & ArgsDefinition.UNSPECIFIC_NAME) != 0
							&& argValue.getNodeType() == NodeType.STRING_CONST) {
						final ElementAccess access= new ElementAccess.Default(node);
						access.fFlags= ElementAccess.A_READ;
						access.fNameNode= argValue;
						SourceAnalyzer.this.topScope.addLateResolve(argValue.getText(), access);
						
						SourceAnalyzer.this.argValueToIgnore.add(argValue);
						continue ITER_ARGS;
					}
				}
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	
	protected final class BaseAssign implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_x;
		private final int argIdx_value;
		
		
		public BaseAssign(final RCoreFunctions rdef) {
			this.argsDef= rdef.BASE_ASSIGN_args;
			this.argIdx_x= this.argsDef.indexOf("x");
			this.argIdx_value= this.argsDef.indexOf("value");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.returnValue= null;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode xNode= args.getArgValueNode(this.argIdx_x);
			final RAstNode valueNode= args.getArgValueNode(this.argIdx_value);
			
			Object returnValue= null;
			if (valueNode != null) {
				valueNode.acceptInR(SourceAnalyzer.this);
				returnValue= SourceAnalyzer.this.returnValue;
				SourceAnalyzer.this.returnValue= null;
				SourceAnalyzer.this.argValueToIgnore.add(valueNode);
			}
			if (xNode != null && xNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= ElementAccess.A_WRITE;
				access.fNameNode= xNode;
				
				final BuildSourceFrame envir= readScopeArgs(args.getArgValueNode("pos"), SourceAnalyzer.this.topScope);
				if (evalBoolean(args.getArgValueNode("inherits"), false)) {
					envir.addLateResolve(xNode.getText(), access);
				}
				else {
					envir.add(xNode.getText(), access);
				}
				
				returnValue= registerSourceElement(returnValue, access);
			}
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= returnValue;
		}
		
	}
	
	protected final class BaseRemove implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		
		public BaseRemove(final RCoreFunctions rdef) {
			this.argsDef= rdef.BASE_REMOVE_args;
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			
			if (args.ellipsisArgs.length > 0) {
				for (int i= 0; i < args.ellipsisArgs.length; i++) {
					final FCall.Arg argNode= args.ellipsisArgs[i];
					if (argNode.hasValue()) {
						final RAstNode valueNode= argNode.getValueChild();
						switch (valueNode.getNodeType()) {
						case SYMBOL:
						case STRING_CONST:
							final ElementAccess access= new ElementAccess.Default(node);
							access.fFlags= ElementAccess.A_DELETE;
							access.fNameNode= valueNode;
							final BuildSourceFrame envir= readScopeArgs(args.getArgValueNode("pos"), SourceAnalyzer.this.topScope);
							if (evalBoolean(args.getArgValueNode("inherits"), false)) {
								envir.addLateResolve(valueNode.getText(), access);
							}
							else {
								envir.add(valueNode.getText(), access);
							}
							
							SourceAnalyzer.this.argValueToIgnore.add(valueNode);
							break;
						default:
							break;
						}
					}
				}
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class BaseExists extends CommonVarNamedRead {
		
		public BaseExists(final RCoreFunctions rdef) {
			super(rdef.BASE_EXISTS_args, "x", "where");
		}
		
	}
	
	protected final class BaseGet implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_x;
		private final int argIdx_scope;
		
		public BaseGet(final RCoreFunctions rdef) {
			this.argsDef= rdef.BASE_GET_args;
			this.argIdx_x= this.argsDef.indexOf("x");
			this.argIdx_scope= this.argsDef.indexOf("pos");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode xNode= args.getArgValueNode(this.argIdx_x);
			
			if (xNode != null && xNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= ElementAccess.A_READ;
				access.fNameNode= xNode;
				final BuildSourceFrame envir= readScopeArgs(args.getArgValueNode(this.argIdx_scope), SourceAnalyzer.this.topScope);
				if (evalBoolean(args.getArgValueNode("inherits"), true)) {
					envir.addLateResolve(xNode.getText(), access);
				}
				else {
					envir.add(xNode.getText(), access);
				}
				
				SourceAnalyzer.this.argValueToIgnore.add(xNode);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class BaseSave implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		
		public BaseSave(final RCoreFunctions rdef) {
			this.argsDef= rdef.BASE_SAVE_args;
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			
			if (args.ellipsisArgs.length > 0) {
				for (int i= 0; i < args.ellipsisArgs.length; i++) {
					final FCall.Arg argNode= args.ellipsisArgs[i];
					if (argNode.hasValue()) {
						final RAstNode valueNode= argNode.getValueChild();
						switch (valueNode.getNodeType()) {
						case SYMBOL:
						case STRING_CONST:
							final ElementAccess access= new ElementAccess.Default(node);
							access.fFlags= ElementAccess.A_READ;
							access.fNameNode= valueNode;
							SourceAnalyzer.this.topScope.addLateResolve(valueNode.getText(), access);
							
							SourceAnalyzer.this.argValueToIgnore.add(valueNode);
							break;
						default:
							break;
						}
					}
				}
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class BaseCall implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_fName;
		
		public BaseCall(final RCoreFunctions rdef) {
			this.argsDef= rdef.BASE_CALL_args;
			this.argIdx_fName= this.argsDef.indexOf("name");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode nameNode= args.getArgValueNode(this.argIdx_fName);
			
			if (nameNode != null && nameNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= ElementAccess.A_READ | ElementAccess.A_FUNC;
				access.fNameNode= nameNode;
				SourceAnalyzer.this.topScope.addLateResolve(nameNode.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(nameNode);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class BaseDoCall implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_fName;
		
		public BaseDoCall(final RCoreFunctions rdef) {
			this.argsDef= rdef.BASE_DOCALL_args;
			this.argIdx_fName= this.argsDef.indexOf("what");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode nameNode= args.getArgValueNode(this.argIdx_fName);
			
			if (nameNode != null) {
				final ElementAccess access= new ElementAccess.Default(node, nameNode);
				access.fFlags= ElementAccess.A_CALL | ElementAccess.A_FUNC;
				final String name= resolveElementName(nameNode, access, true);
				if (name != null) {
					SourceAnalyzer.this.topScope.addLateResolve(name, access);
				}
				SourceAnalyzer.this.argValueToIgnore.add(nameNode);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	private abstract class BaseCommonPackageLoad implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_packageName;
		private final int argIdx_stringOnly;
		
		public BaseCommonPackageLoad(final ArgsDefinition argsDef) {
			this.argsDef= argsDef;
			this.argIdx_packageName= this.argsDef.indexOf("package");
			this.argIdx_stringOnly= this.argsDef.indexOf("character.only");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode nameValue= args.getArgValueNode(this.argIdx_packageName);
			if (nameValue != null 
					&& (nameValue.getNodeType() == NodeType.STRING_CONST
						|| (!evalBoolean(args.getArgNode(this.argIdx_stringOnly), false) && nameValue.getNodeType() == NodeType.SYMBOL))
					&& isValidPackageName(nameValue)) {
				final String packageName= nameValue.getText();
				final ElementAccess access= new ElementAccess.Package(
						node, nameValue);
				access.fFlags |= ElementAccess.A_IMPORT;
				SourceAnalyzer.this.packageRefs.add(packageName, access);
				if (SourceAnalyzer.this.currentSourceContainerBuilder != null) {
					final RPkgImport rImport= new RPkgImport(SourceAnalyzer.this.currentSourceContainerBuilder.element, access);
					SourceAnalyzer.this.currentSourceContainerBuilder.children.add(rImport);
				}
				
				final BuildSourceFrame envir= getPkgEnvir(packageName);
				if (!SourceAnalyzer.this.globalEnvir.fParents.contains(envir)) {
					SourceAnalyzer.this.globalEnvir.fParents= ConstArrayList.concat(envir, SourceAnalyzer.this.globalEnvir.fParents);
				}
				
				SourceAnalyzer.this.argValueToIgnore.add(nameValue);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= SourceAnalyzer.this.globalEnvir;
		}
		
	}
	
	protected final class BaseLibrary extends BaseCommonPackageLoad {
		
		public BaseLibrary(final RCoreFunctions rdef) {
			super(rdef.BASE_LIBRARY_args);
		}
		
	}
	
	protected final class BaseRequire extends BaseCommonPackageLoad {
		
		public BaseRequire(final RCoreFunctions rdef) {
			super(rdef.BASE_REQUIRE_args);
		}
		
	}
	
	
	protected final class BaseGlobalenv implements IFCallAnalyzer {
		
		public BaseGlobalenv(final RCoreFunctions rdef) {
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= SourceAnalyzer.this.globalEnvir;
		}
		
	}
	
	protected final class BaseTopenv implements IFCallAnalyzer {
		
		public BaseTopenv(final RCoreFunctions rdef) {
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
//			final RAstNode envir= resolveEnvir(argValues, this.argsDef);
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= SourceAnalyzer.this.topLevelEnvir;
		}
		
	}
	
	protected final class BaseC implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		
		public BaseC(final RCoreFunctions rdef) {
			this.argsDef= rdef.BASE_C_args;
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			Object returnValue= null;
			REQUEST: for (int i= 0; i < SourceAnalyzer.this.request.length; i++) {
				if (SourceAnalyzer.this.request[i] == RETURN_STRING_ARRAY) {
					final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
					final RAstNode[] array= new RAstNode[args.ellipsisArgs.length];
					for (int j= 0; j < array.length; j++) {
						final FCall.Arg argNode= args.ellipsisArgs[j];
						if (argNode.hasValue()) {
							final RAstNode valueNode= argNode.getValueChild();
							if (valueNode.getNodeType() == NodeType.STRING_CONST) {
								array[j]= valueNode;
								SourceAnalyzer.this.argValueToIgnore.add(valueNode);
							}
							else {
								break REQUEST;
							}
						}
					}
					returnValue= new NodeArray(RETURN_STRING_ARRAY, array);
					break REQUEST;
				}
			}
			SourceAnalyzer.this.request= NO_REQUESTS;
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= returnValue;
		}
		
	}
	
	private abstract class MethodsCommonSetGeneric implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_fName;
		private final int argIdx_def;
		private final int argIdx_useAsDefault;
		private final int argIdx_genericFunction;
		private final int argIdx_signature;
		
		protected MethodsCommonSetGeneric(final ArgsDefinition argsDef) {
			this.argsDef= argsDef;
			this.argIdx_fName= this.argsDef.indexOf("name");
			this.argIdx_def= this.argsDef.indexOf("def");
			this.argIdx_useAsDefault= this.argsDef.indexOf("useAsDefault");
			this.argIdx_genericFunction= this.argsDef.indexOf("genericFunction");
			this.argIdx_signature= this.argsDef.indexOf("signature");
		}
		
		@Override
		public final void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode fNameNode= args.getArgValueNode(this.argIdx_fName);
			
			if (fNameNode != null && fNameNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= ElementAccess.A_WRITE | ElementAccess.A_FUNC;
				access.fNameNode= fNameNode;
				SourceAnalyzer.this.topLevelEnvir.add(fNameNode.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(fNameNode);
				
				final BuildSourceFrame envir= new BuildSourceFrame.RunScope(IRFrame.FUNCTION, BuildSourceFrame.createId(IRFrame.FUNCTION, access.getSegmentName(), ++SourceAnalyzer.this.anonymCount), SourceAnalyzer.this.topScope);
				if (SourceAnalyzer.this.currentSourceContainerBuilder != null) {
					final RMethod rMethod= new RMethod(SourceAnalyzer.this.currentSourceContainerBuilder.element, 
							IRElement.R_GENERIC_FUNCTION, access, envir);
					registerFunctionElement(rMethod);
					
					enterElement(rMethod, envir, node);
					
					final RMethod defMethod= visitAndCheckValue(args.getArgNode(this.argIdx_def), "def");
					final RMethod defaultMethod= visitAndCheckValue(args.getArgNode(this.argIdx_useAsDefault), "useAsDefault");
					visitAndCheckValue(args.getArgNode(this.argIdx_genericFunction), "genericFunction");
					
					final RAstNode signatureValue= args.getArgValueNode(this.argIdx_signature);
					RAstNode[] signatureArgNodes= null;
					if (signatureValue != null) {
						SourceAnalyzer.this.request= STRING_ARRAY_REQUEST;
						SourceAnalyzer.this.returnValue= null;
						signatureValue.acceptInR(SourceAnalyzer.this);
						SourceAnalyzer.this.argValueToIgnore.add(signatureValue);
						if (SourceAnalyzer.this.returnValue instanceof ReturnValue && ((ReturnValue) SourceAnalyzer.this.returnValue).returnType == RETURN_STRING_ARRAY) {
							signatureArgNodes= ((NodeArray) SourceAnalyzer.this.returnValue).array;
						}
						SourceAnalyzer.this.returnValue= null;
					}
					
					ArgsDefinition baseDef= null;
					ArgsDefinition methodDef;
					if (defMethod != null) {
						baseDef= defMethod.getArgsDefinition();
					}
					if (defaultMethod != null && (baseDef == null || baseDef.size() == 0)) {
						baseDef= defaultMethod.getArgsDefinition();
					}
					if (baseDef != null && baseDef.size() > 0) {
						final ArgsBuilder argsBuilder= new ArgsBuilder();
						// we copy the names
						if (signatureArgNodes != null) { // explicit
							ARGS: for (int i= 0; i < baseDef.size(); i++) {
								final String name= baseDef.get(i).name;
								if (name != null) {
									for (int j= 0; j < signatureArgNodes.length; j++) {
										if (name.equals(signatureArgNodes[j].getText())) {
											argsBuilder.add(name, 0, "<?>");
											continue ARGS;
										}
									}
									argsBuilder.add(name, 0, "\u2014");
									continue ARGS;
								}
								argsBuilder.add(name);
								continue ARGS;
							}
						}
						else if (baseDef.size() == 1 && "...".equals(baseDef.get(0).name)) {
							argsBuilder.add("...", 0, "<?>");
						}
						else {
							ARGS: for (int i= 0; i < baseDef.size(); i++) {
								final String name= baseDef.get(i).name;
								if (name != null) {
									if (!name.equals("...")) {
										argsBuilder.add(name, 0, "<?>");
										continue ARGS;
									}
									argsBuilder.add(name, 0, "\u2014");
									continue ARGS;
								}
								argsBuilder.add(name);
								continue ARGS;
							}
						}
						methodDef= argsBuilder.toDef();
					}
					else {
						methodDef= new ArgsDefinition();
					}
					rMethod.complete(methodDef);
					
					node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
					
					leaveElement();
				}
				else {
					addEnvirInsteadOfElement(envir, node);
					
					node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
				}
			}
			else {
				node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			}
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class MethodsSetGeneric extends MethodsCommonSetGeneric {
		
		public MethodsSetGeneric(final RCoreFunctions rdef) {
			super(rdef.METHODS_SETGENERIC_args);
		}
		
	}
	
	protected final class MethodsSetGroupGeneric extends MethodsCommonSetGeneric {
		
		public MethodsSetGroupGeneric(final RCoreFunctions rdef) {
			super(rdef.METHODS_SETGROUPGENERIC_args);
		}
		
	}
	
	protected final class MethodsRemoveGeneric implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_fName;
		
		public MethodsRemoveGeneric(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_SETGROUPGENERIC_args;
			this.argIdx_fName= this.argsDef.indexOf("f");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode fNameNode= args.getArgValueNode(this.argIdx_fName);
			
			if (fNameNode != null && fNameNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= ElementAccess.A_DELETE | ElementAccess.A_FUNC;
				access.fNameNode= fNameNode;
				SourceAnalyzer.this.genericDefaultEnvir.add(fNameNode.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(fNameNode);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class MethodsSignature implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		
		public MethodsSignature(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_SIGNATURE_args;
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			Object returnValue= null;
			
			if (args.ellipsisArgs.length > 0) {
				final ElementAccess[] argNameNodes= new ElementAccess[args.ellipsisArgs.length];
				final String[] classNames= new String[args.ellipsisArgs.length];
				for (int i= 0; i < args.ellipsisArgs.length; i++) {
					final FCall.Arg arg= args.ellipsisArgs[i];
					if (arg.hasName() && arg.getNameChild().getText() != null) {
						argNameNodes[i]= new ElementAccess.Default(node, arg.getNameChild());
						argNameNodes[i].fFlags= ElementAccess.A_ARG;
					}
					if (arg.hasValue()) {
						final RAstNode value= arg.getValueChild();
						if (value.getNodeType() == NodeType.STRING_CONST) {
							classNames[i]= value.getText();
							registerSimpleClassAccessInEnvir(node, value);
							SourceAnalyzer.this.argValueToIgnore.add(value);
						}
					}
				}
				returnValue= new Signature(argNameNodes, classNames);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= returnValue;
		}
		
	}
	
	protected final class MethodsSetClass implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_className;
		private final int argIdx_superClasses;
		private final int argIdx_representation;
		private final int argIdx_prototype;
		
		public MethodsSetClass(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_SETCLASS_args;
			this.argIdx_className= this.argsDef.indexOf("Class");
			this.argIdx_superClasses= this.argsDef.indexOf("contains");
			this.argIdx_representation= this.argsDef.indexOf("representation");
			this.argIdx_prototype= this.argsDef.indexOf("prototype");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			
			final ElementAccess access= new ElementAccess.Class(node);
			access.fFlags= ElementAccess.A_WRITE;
			String name;
			final RAstNode classNameValue= args.getArgValueNode(this.argIdx_className);
			if (classNameValue != null && classNameValue.getNodeType() == NodeType.STRING_CONST) {
				name= classNameValue.getText();
				access.fNameNode= classNameValue;
				SourceAnalyzer.this.argValueToIgnore.add(classNameValue);
			}
			else {
				name= null;
			}
			SourceAnalyzer.this.genericDefaultEnvir.addClass(name, access);
			
			final BuildSourceFrame envir= new BuildSourceFrame.RunScope(IRFrame.CLASS, BuildSourceFrame.createId(IRFrame.CLASS, access.getSegmentName(), ++SourceAnalyzer.this.anonymCount), 
					SourceAnalyzer.this.topScope);
			
			if (SourceAnalyzer.this.currentSourceContainerBuilder != null) {
				final RClass rClass= new RSourceElementByElementAccess.RClass(SourceAnalyzer.this.currentSourceContainerBuilder.element, access, envir);
				registerClassElement(rClass);
				enterElement(rClass, envir, node);
				
				final RAstNode representationValue= args.getArgValueNode(this.argIdx_representation);
				if (representationValue != null) {
					SourceAnalyzer.this.request= REPRESENTATION_REQUEST;
					representationValue.acceptInR(SourceAnalyzer.this);
					
					SourceAnalyzer.this.argValueToIgnore.add(representationValue);
					SourceAnalyzer.this.request= NO_REQUESTS;
				}
				
				final RAstNode superClasses= args.getArgValueNode(this.argIdx_superClasses);
				if (superClasses != null) {
					SourceAnalyzer.this.request= STRING_ARRAY_REQUEST;
					SourceAnalyzer.this.returnValue= null;
					superClasses.acceptInR(SourceAnalyzer.this);
					SourceAnalyzer.this.argValueToIgnore.add(superClasses);
					if (SourceAnalyzer.this.returnValue instanceof ReturnValue && ((ReturnValue) SourceAnalyzer.this.returnValue).returnType == RETURN_STRING_ARRAY) {
						final RAstNode refNode= args.allocatedArgs[this.argIdx_superClasses];
						final RAstNode[] superClassNameNodes= ((NodeArray) SourceAnalyzer.this.returnValue).array;
						final String[] names= new String[superClassNameNodes.length];
						for (int i= 0; i < superClassNameNodes.length; i++) {
							final ElementAccess superClassAccess= registerSimpleClassAccessInEnvir(refNode, superClassNameNodes[i]);
							names[i]= superClassAccess.getSegmentName();
						}
						rClass.addSuperClasses(names);
					}
					SourceAnalyzer.this.request= NO_REQUESTS;
					SourceAnalyzer.this.returnValue= null;
				}
				
				final RAstNode prototypeValue= args.getArgValueNode(this.argIdx_prototype);
				if (prototypeValue != null) {
					SourceAnalyzer.this.request= PROTOTYPE_REQUEST;
					prototypeValue.acceptInR(SourceAnalyzer.this);
					
					SourceAnalyzer.this.argValueToIgnore.add(prototypeValue);
					SourceAnalyzer.this.request= NO_REQUESTS;
				}
				
				node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
				
				leaveElement();
			}
			else {
				addEnvirInsteadOfElement(envir, node);
				
				node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			}
			
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class MethodsSetClassUnion implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_className;
		private final int argIdx_superClassNames;
		
		public MethodsSetClassUnion(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_SETCLASSUNION_args;
			this.argIdx_className= this.argsDef.indexOf("name");
			this.argIdx_superClassNames= this.argsDef.indexOf("members");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode classNameValue= args.getArgValueNode(this.argIdx_className);
			final RAstNode superClassNamesValue= args.getArgValueNode(this.argIdx_superClassNames);
			
			final ElementAccess access= new ElementAccess.Class(node);
			final String name;
			access.fFlags= ElementAccess.A_WRITE;
			if (classNameValue != null && classNameValue.getNodeType() == NodeType.STRING_CONST) {
				name= classNameValue.getText();
				access.fNameNode= classNameValue;
				SourceAnalyzer.this.argValueToIgnore.add(classNameValue);
			}
			else {
				name= null;
			}
			SourceAnalyzer.this.genericDefaultEnvir.addClass(name, access);
			
			final BuildSourceFrame envir= new BuildSourceFrame.RunScope(IRFrame.CLASS, BuildSourceFrame.createId(IRFrame.CLASS, access.getSegmentName(), ++SourceAnalyzer.this.anonymCount), 
					SourceAnalyzer.this.topScope);
			if (SourceAnalyzer.this.currentSourceContainerBuilder != null) {
				final RClass rClass= new RSourceElementByElementAccess.RClass(SourceAnalyzer.this.currentSourceContainerBuilder.element, access, envir);
				registerClassElement(rClass);
				enterElement(rClass, envir, node);
				
				if (superClassNamesValue != null) {
					SourceAnalyzer.this.request= STRING_ARRAY_REQUEST;
					SourceAnalyzer.this.returnValue= null;
					superClassNamesValue.acceptInR(SourceAnalyzer.this);
					SourceAnalyzer.this.argValueToIgnore.add(superClassNamesValue);
					if (SourceAnalyzer.this.returnValue instanceof ReturnValue && ((ReturnValue) SourceAnalyzer.this.returnValue).returnType == RETURN_STRING_ARRAY) {
						final RAstNode refNode= args.allocatedArgs[this.argIdx_superClassNames];
						final RAstNode[] superClassNameNodes= ((NodeArray) SourceAnalyzer.this.returnValue).array;
						final String[] names= new String[superClassNameNodes.length];
						for (int i= 0; i < superClassNameNodes.length; i++) {
							final ElementAccess superClassAccess= registerSimpleClassAccessInEnvir(refNode, superClassNameNodes[i]);
							names[i]= superClassAccess.getSegmentName();
						}
						rClass.addSuperClasses(names);
					}
					SourceAnalyzer.this.request= NO_REQUESTS;
					SourceAnalyzer.this.returnValue= null;
				}
				
				node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
				
				leaveElement();
			}
			else {
				addEnvirInsteadOfElement(envir, node);
				
				node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			}
			
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class MethodsRepresentation implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		
		public MethodsRepresentation(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_REPRESENTATION_args;
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			final boolean requested= (SourceAnalyzer.this.request == REPRESENTATION_REQUEST // || isRequested(REG_CLASS_REPRESENTATION)
					&& SourceAnalyzer.this.currentSourceContainerBuilder.element.getElementType() == IRElement.R_S4CLASS);
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			
			if (args.ellipsisArgs.length > 0) {
				final RSourceElementByElementAccess.RClass rClass= requested ?
								(RSourceElementByElementAccess.RClass) SourceAnalyzer.this.currentSourceContainerBuilder.element : null;
				final String[] superClassNames= new String[args.ellipsisArgs.length];
				
				for (int i= 0; i < args.ellipsisArgs.length; i++) {
					final FCall.Arg arg= args.ellipsisArgs[i];
					if (arg.hasName()) { // slot
						final RAstNode nameNode= arg.getNameChild();
						RSlot slot= null;
						if (rClass != null) {
							final ElementAccess.Slot access= new ElementAccess.Slot(arg);
							access.fFlags= ElementAccess.A_WRITE;
							access.fNameNode= nameNode;
							SourceAnalyzer.this.currentSourceContainerBuilder.envir.addRunResolve(nameNode.getText(), access);
							slot= new RSourceElementByElementAccess.RSlot(rClass, access);
							SourceAnalyzer.this.currentSourceContainerBuilder.children.add(slot);
						}
						if (arg.hasValue()) {
							final RAstNode valueNode= arg.getValueChild();
							if (valueNode.getNodeType() == NodeType.STRING_CONST) {
								registerSimpleClassAccessInEnvir(arg, valueNode);
								if (slot != null) {
									slot.completeType(valueNode.getText());
								}
								SourceAnalyzer.this.argValueToIgnore.add(valueNode);
							}
						}
					}
					else { // superclasses (like setClass arg contains)
						if (arg.hasValue()) {
							final RAstNode value= arg.getValueChild();
							if (value.getNodeType() == NodeType.STRING_CONST) {
								registerSimpleClassAccessInEnvir(arg, value);
								if (rClass != null) {
									superClassNames[i]= value.getText();
								}
								SourceAnalyzer.this.argValueToIgnore.add(value);
							}
						}
					}
				}
				if (rClass != null) {
					rClass.addSuperClasses(superClassNames);
				}
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class MethodsPrototype implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		
		public MethodsPrototype(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_PROTOTYPE_args;
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			final boolean requested= (SourceAnalyzer.this.request == PROTOTYPE_REQUEST // || isRequested(REG_CLASS_REPRESENTATION)
					&& SourceAnalyzer.this.currentSourceContainerBuilder.element.getElementType() == IRElement.R_S4CLASS);
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			
			if (args.ellipsisArgs.length > 0) {
				final RSourceElementByElementAccess.RClass classDef= requested ?
						(RSourceElementByElementAccess.RClass) SourceAnalyzer.this.currentSourceContainerBuilder.element : null;
				for (int i= 0; i < args.ellipsisArgs.length; i++) {
					final FCall.Arg arg= args.ellipsisArgs[i];
					if (arg.hasName()) { // slot
						final RAstNode slotNameNode= arg.getNameChild();
						final String slotName= slotNameNode.getText();
						RSlot slot= null;
						if (classDef != null && slotName != null) {
							final ElementAccess.Slot access= new ElementAccess.Slot(arg);
							access.fFlags= ElementAccess.A_WRITE;
							access.fNameNode= slotNameNode;
							SourceAnalyzer.this.currentSourceContainerBuilder.envir.addRunResolve(slotName, access);
							for (final RSourceElementByElementAccess child : SourceAnalyzer.this.currentSourceContainerBuilder.children) {
								if (child.getElementType() == IRElement.R_S4SLOT
										&& slotName.equals(child.getElementName().getSegmentName()) ) {
									slot= (RSlot) child;
									break;
								}
							}
						}
						if (arg.hasValue()) {
//							final RAstNode valueNode= arg.getValueChild();
//							if (slot != null) {
//								slot.fPrototypeCode= value.toString();
//							}
						}
					}
//					else { // data
//					}
				}
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class MethodsSetIs implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_className;
		private final int argIdx_classToExtendName;
		private final int argIdx_testF;
		private final int argIdx_coerceF;
		private final int argIdx_replaceF;
		
		public MethodsSetIs(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_SETIS_args;
			this.argIdx_className= this.argsDef.indexOf("class1");
			this.argIdx_classToExtendName= this.argsDef.indexOf("class2");
			this.argIdx_testF= this.argsDef.indexOf("test");
			this.argIdx_coerceF= this.argsDef.indexOf("coerce");
			this.argIdx_replaceF= this.argsDef.indexOf("replace");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode classNameNode= args.getArgValueNode(this.argIdx_className);
			final RAstNode cToExtendNameNode= args.getArgValueNode(this.argIdx_classToExtendName);
			RClassExt rClassExt= null;
			BuildSourceFrame envir= null;
			
			if (classNameNode != null && classNameNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Class(node);
				access.fFlags= ElementAccess.A_WRITE;
				access.fNameNode= classNameNode;
				SourceAnalyzer.this.genericDefaultEnvir.addClass(classNameNode.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(classNameNode);
				
				envir= new BuildSourceFrame.RunScope(IRFrame.FUNCTION,
						BuildSourceFrame.createId(IRFrame.FUNCTION, access.getSegmentName(), ++SourceAnalyzer.this.anonymCount),
						SourceAnalyzer.this.topScope );
				if (SourceAnalyzer.this.currentSourceContainerBuilder != null) {
					rClassExt= new RClassExt(SourceAnalyzer.this.currentSourceContainerBuilder.element, access, envir, "setIs");
					registerClassExtElement(rClassExt);
				}
				else {
					node.addAttachment(envir);
				}
			}
			if (cToExtendNameNode != null && cToExtendNameNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Class(node);
				access.fFlags= ElementAccess.A_READ;
				access.fNameNode= cToExtendNameNode;
				SourceAnalyzer.this.genericDefaultEnvir.addClass(cToExtendNameNode.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(cToExtendNameNode);
				
				if (rClassExt != null) {
					rClassExt.complete(classNameNode.getText());
				}
			}
			
			if (rClassExt != null) {
				enterElement(rClassExt, envir, node);
				
				visitAndCheckValue(args.allocatedArgs[this.argIdx_testF], "test");
				visitAndCheckValue(args.allocatedArgs[this.argIdx_coerceF], "coerce");
				visitAndCheckValue(args.allocatedArgs[this.argIdx_replaceF], "replace");
				
				node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
				
				leaveElement();
			}
			else {
				node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			}
			SourceAnalyzer.this.returnValue= null;
			return;
		}
		
	}
	
	
	protected final class MethodsRemoveClass implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_className;
		
		public MethodsRemoveClass(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_REMOVECLASS_args;
			this.argIdx_className= this.argsDef.indexOf("Class");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode classNameNode= args.getArgValueNode(this.argIdx_className);
			
			if (classNameNode != null && classNameNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Class(node);
				access.fFlags= ElementAccess.A_DELETE;
				access.fNameNode= classNameNode;
				SourceAnalyzer.this.genericDefaultEnvir.addClass(classNameNode.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(classNameNode);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
			return;
		}
		
	}
	
	protected final class MethodsSetAs implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_className;
		private final int argIdx_toClass;
		
		public MethodsSetAs(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_SETAS_args;
			this.argIdx_className= this.argsDef.indexOf("from");
			this.argIdx_toClass= this.argsDef.indexOf("to");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode classNameNode= args.getArgValueNode(this.argIdx_className);
			final RAstNode toClassNode= args.getArgValueNode(this.argIdx_toClass);
			
			if (classNameNode != null && classNameNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Class(node);
				access.fFlags= ElementAccess.A_WRITE;
				access.fNameNode= classNameNode;
				SourceAnalyzer.this.genericDefaultEnvir.addClass(classNameNode.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(classNameNode);
			}
			if (toClassNode != null && toClassNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Class(node);
				access.fFlags= ElementAccess.A_READ;
				access.fNameNode= toClassNode;
				SourceAnalyzer.this.genericDefaultEnvir.addClass(toClassNode.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(toClassNode);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
			return;
		}
		
	}
	
	protected final class MethodsSetValidity implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_className;
		
		public MethodsSetValidity(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_SETVALIDITY_args;
			this.argIdx_className= this.argsDef.indexOf("Class");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode classNameNode= args.getArgValueNode(this.argIdx_className);
			
			if (classNameNode != null && classNameNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Class(node);
				access.fFlags= ElementAccess.A_WRITE;
				access.fNameNode= classNameNode;
				SourceAnalyzer.this.genericDefaultEnvir.addClass(classNameNode.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(classNameNode);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
			return;
		}
		
	}
	
	private abstract class MethodsCommonClassRead implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_className;
		
		protected MethodsCommonClassRead(final ArgsDefinition argsDef, final String classNameNodeName) {
			this.argsDef= argsDef;
			this.argIdx_className= this.argsDef.indexOf(classNameNodeName);
		}
		
		@Override
		public final void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode classNameNode= args.getArgValueNode(this.argIdx_className);
			
			if (classNameNode != null && classNameNode.getNodeType() == NodeType.STRING_CONST) {
				registerSimpleClassAccessInEnvir(node, classNameNode);
				SourceAnalyzer.this.argValueToIgnore.add(classNameNode);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class MethodsGetClass extends MethodsCommonClassRead {
		
		public MethodsGetClass(final RCoreFunctions rdef) {
			super(rdef.METHODS_GETCLASS_args, "Class");
		}
		
	}
	
	protected final class MethodsGetClassDef extends MethodsCommonClassRead {
		
		public MethodsGetClassDef(final RCoreFunctions rdef) {
			super(rdef.METHODS_GETCLASSDEF_args, "Class");
		}
		
	}
	
	protected final class MethodsFindClass extends MethodsCommonClassRead {
		
		public MethodsFindClass(final RCoreFunctions rdef) {
			super(rdef.METHODS_FINDCLASS_args, "Class");
		}
		
	}
	
	protected final class MethodsNew extends MethodsCommonClassRead {
		
		public MethodsNew(final RCoreFunctions rdef) {
			super(rdef.METHODS_NEW_args, "Class");
		}
		
	}
	
	protected final class MethodsAs extends MethodsCommonClassRead {
		
		public MethodsAs(final RCoreFunctions rdef) {
			super(rdef.METHODS_AS_args, "Class");
		}
		
	}
	
	protected final class MethodsSetMethod implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_fName;
		private final int argIdx_signature;
		private final int argIdx_fDef;
		
		public MethodsSetMethod(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_SETMETHOD_args;
			this.argIdx_fName= this.argsDef.indexOf("f");
			this.argIdx_signature= this.argsDef.indexOf("signature");
			this.argIdx_fDef= this.argsDef.indexOf("definition");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode fNameArg= args.getArgValueNode(this.argIdx_fName);
			final RAstNode fDefArg= args.getArgValueNode(this.argIdx_fDef);
			
			if (fNameArg != null && fNameArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= ElementAccess.A_WRITE | ElementAccess.A_FUNC;
				access.fNameNode= fNameArg;
				SourceAnalyzer.this.genericDefaultEnvir.add(fNameArg.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(fNameArg);
				
				final Signature sig= readSignature(node, args.getArgValueNode(this.argIdx_signature));
				SourceAnalyzer.this.returnValue= null;
				if (fDefArg != null) {
					fDefArg.acceptInR(SourceAnalyzer.this);
					SourceAnalyzer.this.argValueToIgnore.add(fDefArg);
				}
				RMethod rMethod;
				if (SourceAnalyzer.this.returnValue instanceof RMethod) {
					rMethod= (RMethod) SourceAnalyzer.this.returnValue;
					registerFunctionElement(rMethod, IRElement.R_S4METHOD, access, sig);
				}
				else {
					final BuildSourceFrame envir= new BuildSourceFrame.DefScope(IRFrame.FUNCTION,
							BuildSourceFrame.createId(IRFrame.FUNCTION, access.getSegmentName(), ++SourceAnalyzer.this.anonymCount),
							access.getSegmentName(), new BuildSourceFrame[] { SourceAnalyzer.this.topLevelEnvir } );
					if (SourceAnalyzer.this.currentSourceContainerBuilder != null) {
						rMethod= new RMethod(SourceAnalyzer.this.currentSourceContainerBuilder.element, IRElement.R_S4METHOD, access, envir);
						enterElement(rMethod, envir, node);
						leaveElement();
						registerFunctionElement(rMethod, IRElement.R_S4METHOD, access, sig);
					}
					else {
						addEnvirInsteadOfElement(envir, node);
					}
				}
				SourceAnalyzer.this.returnValue= null;
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class MethodsRemoveMethod implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_fName;
		private final int argIdx_signature;
		
		public MethodsRemoveMethod(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_REMOVEMETHOD_args;
			this.argIdx_fName= this.argsDef.indexOf("f");
			this.argIdx_signature= this.argsDef.indexOf("signature");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode fNameArg= args.getArgValueNode(this.argIdx_fName);
			
			if (fNameArg != null && fNameArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= ElementAccess.A_DELETE | ElementAccess.A_FUNC;
				access.fNameNode= fNameArg;
				SourceAnalyzer.this.genericDefaultEnvir.addLateResolve(fNameArg.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(fNameArg);
				
//				final Signature sig= readSignature(node, args.getArgValueNode(this.argIdx_signature));
//				fReturnValue= null;
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class MethodsRemoveMethods implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_fName;
		
		public MethodsRemoveMethods(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_REMOVEMETHODS_args;
			this.argIdx_fName= this.argsDef.indexOf("f");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode fNameArg= args.getArgValueNode(this.argIdx_fName);
			
			if (fNameArg != null && fNameArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= ElementAccess.A_DELETE | ElementAccess.A_FUNC;
				access.fNameNode= fNameArg;
				SourceAnalyzer.this.genericDefaultEnvir.addLateResolve(fNameArg.getText(), access);
				
				SourceAnalyzer.this.argValueToIgnore.add(fNameArg);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	private class MethodsCommonMethodRead implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_fName;
		
		protected MethodsCommonMethodRead(final ArgsDefinition argsDef, final String fNameNodeName) {
			this.argsDef= argsDef;
			this.argIdx_fName= this.argsDef.indexOf(fNameNodeName);
		}
		
		@Override
		public final void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode fNameNode= args.getArgValueNode(this.argIdx_fName);
			
			if (fNameNode != null && fNameNode.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= ElementAccess.A_READ | ElementAccess.A_FUNC;
				access.fNameNode= fNameNode;
				SourceAnalyzer.this.genericDefaultEnvir.add(fNameNode.getText(), access);
				SourceAnalyzer.this.argValueToIgnore.add(fNameNode);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	protected final class MethodsGetMethod extends MethodsCommonMethodRead {
		
		public MethodsGetMethod(final RCoreFunctions rdef) {
			super(rdef.METHODS_GETMETHOD_args, "f");
		}
		
	}
	
	protected final class MethodsSelectMethod extends MethodsCommonMethodRead {
		
		public MethodsSelectMethod(final RCoreFunctions rdef) {
			super(rdef.METHODS_SELECTMETHOD_args, "f");
		}
		
	}
	
	protected final class MethodsGetMethods extends MethodsCommonMethodRead {
		
		public MethodsGetMethods(final RCoreFunctions rdef) {
			super(rdef.METHODS_GETMETHODS_args, "f");
		}
		
	}
	
	protected final class MethodsFindMethod extends MethodsCommonMethodRead {
		
		public MethodsFindMethod(final RCoreFunctions rdef) {
			super(rdef.METHODS_FINDMETHOD_args, "f");
		}
		
	}
	
	protected final class MethodsSlot implements IFCallAnalyzer {
		
		private final ArgsDefinition argsDef;
		private final int argIdx_object;
		private final int argIdx_slotName;
		
		
		public MethodsSlot(final RCoreFunctions rdef) {
			this.argsDef= rdef.METHODS_SLOT_args;
			this.argIdx_object= this.argsDef.indexOf("object");
			this.argIdx_slotName= this.argsDef.indexOf("name");
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			final ReadedFCallArgs args= RAst.readArgs(node.getArgsChild(), this.argsDef);
			final RAstNode objectArg= args.getArgValueNode(this.argIdx_object);
			final RAstNode slotArg= args.getArgValueNode(this.argIdx_slotName);
			
			if (objectArg != null && objectArg.getNodeType() == NodeType.SYMBOL) {
				final ElementAccess access= new ElementAccess.Default(node);
				access.fFlags= (assignment) ?
						(ElementAccess.A_WRITE | ElementAccess.A_SUB) :
						(ElementAccess.A_READ | ElementAccess.A_SUB);
				access.fNameNode= objectArg;
				SourceAnalyzer.this.argValueToIgnore.add(objectArg);
				
				if (slotArg != null && slotArg.getNodeType() == NodeType.STRING_CONST) {
					access.fNextSegment= new SubNamedSlotSemanticElementAccess(access, slotArg);
					SourceAnalyzer.this.argValueToIgnore.add(slotArg);
				}
				
				SourceAnalyzer.this.topScope.addLateResolve(objectArg.getText(), access);
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
	
	protected final class NoDefFallback implements IFCallAnalyzer {
		
		public NoDefFallback() {
		}
		
		@Override
		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
			SourceAnalyzer.this.request= NO_REQUESTS;
			
			final FCall.Args args= node.getArgsChild();
			if (args.getChildCount() > 0 && assignment) {
				final FCall.Arg firstArg= args.getChild(0);
				final RAstNode argName= firstArg.getNameChild();
				final RAstNode argValue= firstArg.getValueChild();
				if (firstArg.hasValue()
						&& (!firstArg.hasName() || argName.getText().equals("x"))) { 
					final ElementAccess access= new ElementAccess.Default(node);
					access.fFlags= ElementAccess.A_WRITE;
					final String mainName= resolveElementName(argValue, access, false);
					SourceAnalyzer.this.argValueToIgnore.add(argValue);
					if (mainName != null) {
						registerInEnvir(S_SEARCH, mainName, access);
					}
				}
			}
			
			node.getArgsChild().acceptInRChildren(SourceAnalyzer.this);
			SourceAnalyzer.this.returnValue= null;
		}
		
	}
	
//	protected final class Template1 implements IFCallAnalyzer {
//		
//		public Template1(final RCoreFunctions rdef) {
//		}
//		
//		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
//		}
//		
//	}
	
//	protected final class Template2 implements IFCallAnalyzer {
//		
//		private final ArgsDefinition argsDef;
//		
//		public Template2(final RCoreFunctions rdef) {
//			this.argsDef= rdef.;
//		}
//		
//		public void visit(final FCall node, final boolean assignment) throws InvocationTargetException {
//		}
//		
//	}
	
	private Signature readSignature(final RAstNode refNode, final RAstNode sigNode) throws InvocationTargetException {
		if (sigNode != null) {
			this.request= SIGNATURE_REQUESTS;
			this.returnValue= null;
			sigNode.acceptInR(SourceAnalyzer.this);
			this.argValueToIgnore.add(sigNode);
			if (this.returnValue instanceof ReturnValue) {
				final ReturnValue value= (ReturnValue) this.returnValue;
				this.returnValue= null;
				if (value.returnType == RETURN_METHOD_SIGNATURE) {
					return (Signature) value;
				}
				if (value.returnType == RETURN_STRING_ARRAY) {
					final RAstNode[] nodes= ((NodeArray) value).array;
					final String[] classNames= new String[nodes.length];
					for (int i= 0; i < nodes.length; i++) {
						registerSimpleClassAccessInEnvir(refNode, nodes[i]);
						classNames[i]= nodes[i].getText();
					}
					return new Signature(null, classNames);
				}
			}
			else {
				this.returnValue= null;
				return null;
			}
		}
		return null;
	}
	
	public static ArgsDefinition createMethodArgDef(final FDef fdefNode, final Signature sig) {
		final FDef.Args argList= (fdefNode != null) ? fdefNode.getArgsChild() : null;
		final ArgsBuilder b= new ArgsBuilder();
		if (argList != null) {
			final int n= argList.getChildCount();
			if (sig != null && sig.classNames != null) {
				ITER_ARGS: for (int i= 0; i < n; i++) {
					final String argName= argList.getChild(i).getNameChild().getText();
					if (argName != null && sig.argNameAccess != null) {
						for (int j= 0; j < sig.argNameAccess.length; j++) {
							if (sig.argNameAccess[j] != null
									&& argName.equals(sig.argNameAccess[j].fNameNode.getText())) {
								b.add(argName, 0, sig.classNames[j]);
								continue ITER_ARGS;
							}
						}
						continue ITER_ARGS;
					}
					else if (i < sig.classNames.length) {
						b.add(argName, 0, sig.classNames[i]);
						continue ITER_ARGS;
					}
					else {
						b.add(argName, 0, null);
						continue ITER_ARGS;
					}
				}
			}
			else { // (sig == null || sigClasses == null)
				ITER_ARGS: for (int i= 0; i < n; i++) {
					final String argName= argList.getChild(i).getNameChild().getText();
					b.add(argName, 0, null);
					continue ITER_ARGS;
				}
			}
		}
		else { // (argList == null)
			if (sig != null && sig.argNameAccess != null && sig.classNames != null) {
				ITER_ARGS: for (int i= 0; i < sig.argNameAccess.length; i++) {
					if (sig.argNameAccess[i] != null) {
						b.add(sig.argNameAccess[i].fNameNode.getText(), 0, sig.classNames[i]);
						continue ITER_ARGS;
					}
					else {
						break ITER_ARGS;
					}
				}
			}
		}
		return b.toDef();
	}
	
	private boolean evalBoolean(final RAstNode valueNode, final boolean defaultValue) {
		if (valueNode != null && valueNode.getNodeType() == NodeType.NUM_CONST) {
			if (valueNode.getOperator(0) == RTerminal.TRUE) {
				return true;
			}
			if (valueNode.getOperator(0) == RTerminal.FALSE) {
				return false;
			}
		}
		return defaultValue;
	}
	
	private BuildSourceFrame readScopeArgs(final RAstNode pos, final BuildSourceFrame defaultScope) throws InvocationTargetException {
		this.returnValue= null;
		BuildSourceFrame envir= null;
		if (pos != null) {
			switch (pos.getNodeType()) {
			case NUM_CONST:
				if (pos.getText().equals("1")) { // search pos
					envir= this.globalEnvir;
					break;
				}
				break;
			case STRING_CONST: // search name
				if (pos.getText().equals(".GlobalEnv")) {
					envir= this.globalEnvir;
					break;
				}
				if (pos.getText().startsWith("package:")) {
					envir= getPkgEnvir(pos.getText().substring(8));
					break;
				}
				break;
			default:
				// check for environment
				pos.acceptInR(SourceAnalyzer.this);
				if (this.returnValue instanceof BuildSourceFrame) {
					envir= (BuildSourceFrame) this.returnValue;
					break;
				}
				break;
			}
			this.argValueToIgnore.add(pos);
		}
		if (envir != null) {
			return envir;
		}
		return defaultScope;
	}
	
}
