/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import static de.walware.statet.r.core.model.ArgsDefinition.CLASS_NAME;
import static de.walware.statet.r.core.model.ArgsDefinition.HELP_TOPIC_NAME;
import static de.walware.statet.r.core.model.ArgsDefinition.METHOD_NAME;
import static de.walware.statet.r.core.model.ArgsDefinition.METHOD_OBJ;
import static de.walware.statet.r.core.model.ArgsDefinition.NAME_AS_STRING;
import static de.walware.statet.r.core.model.ArgsDefinition.NAME_AS_SYMBOL;
import static de.walware.statet.r.core.model.ArgsDefinition.PACKAGE_NAME;
import static de.walware.statet.r.core.model.ArgsDefinition.UNSPECIFIC_NAME;
import static de.walware.statet.r.core.model.ArgsDefinition.UNSPECIFIC_OBJ;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImSet;


public class RCoreFunctions {
	
	
	public static final RCoreFunctions DEFAULT= new RCoreFunctions();
	
	
	public static final String BASE_PACKAGE_NAME= "base";
	
	public static final String BASE_ASSIGN_NAME= "assign";
	public final ArgsDefinition BASE_ASSIGN_args;
	
	public static final String BASE_REMOVE_NAME= "remove";
	public static final String BASE_REMOVE_ALIAS_RM= "rm";
	public final ArgsDefinition BASE_REMOVE_args;
	
	public static final String BASE_EXISTS_NAME= "exists";
	public final ArgsDefinition BASE_EXISTS_args; 
	
	public static final String BASE_GET_NAME= "get";
	public final ArgsDefinition BASE_GET_args;
	
	public static final String BASE_SAVE_NAME= "save";
	public final ArgsDefinition BASE_SAVE_args;
	
	public static final String BASE_CALL_NAME= "call";
	public final ArgsDefinition BASE_CALL_args;
	
	public static final String BASE_DOCALL_NAME= "do.call";
	public final ArgsDefinition BASE_DOCALL_args;
	
	
	public static final String BASE_LIBRARY_NAME= "library";
	public final ArgsDefinition BASE_LIBRARY_args;
	
	public static final String BASE_REQUIRE_NAME= "require";
	public final ArgsDefinition BASE_REQUIRE_args;
	
	public static final String BASE_GLOBALENV_NAME= "globalenv";
	public final ArgsDefinition BASE_GLOBALENV_args;
	
	public static final String BASE_TOPENV_NAME= "topenv";
	public final ArgsDefinition BASE_TOPENV_args;
	
	public static final String BASE_GETNAMESPACE_NAME= "getNamespace";
	public final ArgsDefinition BASE_GETNAMESPACE_args;
	
	public static final String BASE_GETNAMESPACENAME_NAME= "getNamespaceName";
	public final ArgsDefinition BASE_GETNAMESPACENAME_args;
	
	public static final String BASE_GETNAMESPACEVERSION_NAME= "getNamespaceVersion";
	public final ArgsDefinition BASE_GETNAMESPACEVERSION_args;
	
	public static final String BASE_GETNAMESPACEEXPORTS_NAME= "getNamespaceExports";
	public final ArgsDefinition BASE_GETNAMESPACEEXPORTS_args;
	
	public static final String BASE_GETNAMESPACEIMPORTS_NAME= "getNamespaceImports";
	public final ArgsDefinition BASE_GETNAMESPACEIMPORTS_args;
	
	public static final String BASE_GETNAMESPACEUSERS_NAME= "getNamespaceUsers";
	public final ArgsDefinition BASE_GETNAMESPACEUSERS_args;
	
	public static final String BASE_GETEXPORTEDVALUE_NAME= "getExportedValue";
	public final ArgsDefinition BASE_GETEXPORTEDVALUE_args;
	
	public static final String BASE_ATTACHNAMESPACE_NAME= "attachNamespace";
	public final ArgsDefinition BASE_ATTACHNAMESPACE_args;
	
	public static final String BASE_LOADNAMESPACE_NAME= "loadNamespace";
	public final ArgsDefinition BASE_LOADNAMESPACE_args;
	
	public static final String BASE_REQUIRENAMESPACE_NAME= "requireNamespace";
	public final ArgsDefinition BASE_REQUIRENAMESPACE_args;
	
	public static final String BASE_ISNAMESPACELOADED_NAME= "isNamespaceLoaded";
	public final ArgsDefinition BASE_ISNAMESPACELOADED_args;
	
	public static final String BASE_UNLOADNAMESPACE_NAME= "unloadNamespace";
	public final ArgsDefinition BASE_UNLOADNAMESPACE_args;
	
	
	public static final String BASE_C_NAME= "c";
	public final ArgsDefinition BASE_C_args;
	
	public static final String BASE_DATAFRAME_NAME= "data.frame";
	public final ArgsDefinition BASE_DATAFRAME_args;
	
	public static final String BASE_USEMETHOD_NAME= "UseMethod";
	public final ArgsDefinition BASE_USEMETHOD_args;
	
	public static final String BASE_NEXTMETHOD_NAME= "NextMethod";
	public final ArgsDefinition BASE_NEXTMETHOD_args;
	
	
	public static final String UTILS_PACKAGE_NAME= "utils";
	
	public static final String UTILS_METHODS_NAME= "methods";
	public final ArgsDefinition UTILS_METHODS_args;
	
	public static final String UTILS_GETS3METHOD_NAME= "getS3method";
	public final ArgsDefinition UTILS_GETS3METHOD_args;
	
	public static final String UTILS_HELP_NAME= "help";
	public final ArgsDefinition UTILS_HELP_args;
	
	
	public static final String METHODS_PACKAGE_NAME= "methods";
	
	public static final String METHODS_SETGENERIC_NAME= "setGeneric";
	public final ArgsDefinition METHODS_SETGENERIC_args;
	
	public static final String METHODS_SETGROUPGENERIC_NAME= "setGroupGeneric";
	public final ArgsDefinition METHODS_SETGROUPGENERIC_args;
	
	public static final String METHODS_REMOVEGENERIC_NAME= "removeGeneric";
	public final ArgsDefinition METHODS_REMOVEGENERIC_args;
	
	public static final String METHODS_ISGENERIC_NAME= "isGeneric";
	public final ArgsDefinition METHODS_ISGENERIC_args;
	
	public static final String METHODS_ISGROUP_NAME= "isGroup";
	public final ArgsDefinition METHODS_ISGROUP_args;
	
	public static final String METHODS_SIGNATURE_NAME= "signature";
	public final ArgsDefinition METHODS_SIGNATURE_args;
	
	
	public static final String METHODS_SETCLASS_NAME= "setClass";
	public final ArgsDefinition METHODS_SETCLASS_args;
	
	public static final String METHODS_SETCLASSUNION_NAME= "setClassUnion";
	public final ArgsDefinition METHODS_SETCLASSUNION_args;
	
	public static final String METHODS_REPRESENTATION_NAME= "representation";
	public final ArgsDefinition METHODS_REPRESENTATION_args;
	
	public static final String METHODS_PROTOTYPE_NAME= "prototype";
	public final ArgsDefinition METHODS_PROTOTYPE_args;
	
	public static final String METHODS_SETIS_NAME= "setIs";
	public final ArgsDefinition METHODS_SETIS_args;
	
	public static final String METHODS_REMOVECLASS_NAME= "removeClass";
	public final ArgsDefinition METHODS_REMOVECLASS_args;
	
	public static final String METHODS_RESETCLASS_NAME= "resetClass";
	public final ArgsDefinition METHODS_RESETCLASS_args;
	
	public static final String METHODS_SETAS_NAME= "setAs";
	public final ArgsDefinition METHODS_SETAS_args;
	
	public static final String METHODS_SETVALIDITY_NAME= "setValidity";
	public final ArgsDefinition METHODS_SETVALIDITY_args;
	
	public static final String METHODS_ISCLASS_NAME= "isClass";
	public final ArgsDefinition METHODS_ISCLASS_args;
	
	public static final String METHODS_EXTENDS_NAME= "extends";
	public final ArgsDefinition METHODS_EXTENDS_args;
	
	public static final String METHODS_GETCLASS_NAME= "getClass";
	public final ArgsDefinition METHODS_GETCLASS_args;
	
	public static final String METHODS_GETCLASSDEF_NAME= "getClassDef";
	public final ArgsDefinition METHODS_GETCLASSDEF_args;
	
	public static final String METHODS_FINDCLASS_NAME= "findClass";
	public final ArgsDefinition METHODS_FINDCLASS_args;
	
	
	public static final String METHODS_NEW_NAME= "new";
	public final ArgsDefinition METHODS_NEW_args;
	
	public static final String METHODS_AS_NAME= "as";
	public final ArgsDefinition METHODS_AS_args;
	
	public static final String METHODS_IS_NAME= "is";
	public final ArgsDefinition METHODS_IS_args;
	
	
	public static final String METHODS_SETMETHOD_NAME= "setMethod";
	public final ArgsDefinition METHODS_SETMETHOD_args;
	
	public static final String METHODS_REMOVEMETHOD_NAME= "removeMethod";
	public final ArgsDefinition METHODS_REMOVEMETHOD_args;
	
	public static final String METHODS_REMOVEMETHODS_NAME= "removeMethods";
	public final ArgsDefinition METHODS_REMOVEMETHODS_args;
	
	public static final String METHODS_EXISTSMETHOD_NAME= "existsMethod";
	public final ArgsDefinition METHODS_EXISTSMETHOD_args;
	
	public static final String METHODS_HASMETHOD_NAME= "hasMethod";
	public final ArgsDefinition METHODS_HASMETHOD_args;
	
	public static final String METHODS_GETMETHOD_NAME= "getMethod";
	public final ArgsDefinition METHODS_GETMETHOD_args;
	
	public static final String METHODS_SELECTMETHOD_NAME= "selectMethod";
	public final ArgsDefinition METHODS_SELECTMETHOD_args;
	
	public static final String METHODS_GETMETHODS_NAME= "getMethods";
	public final ArgsDefinition METHODS_GETMETHODS_args;
	
	public static final String METHODS_FINDMETHOD_NAME= "findMethod";
	public final ArgsDefinition METHODS_FINDMETHOD_args;
	
	public static final String METHODS_DUMPMETHOD_NAME= "dumpMethod";
	public final ArgsDefinition METHODS_DUMPMETHOD_args;
	
	public static final String METHODS_DUMPMETHODS_NAME= "dumpMethods";
	public final ArgsDefinition METHODS_DUMPMETHODS_args;
	
	public static final String METHODS_SLOT_NAME= "slot";
	public final ArgsDefinition METHODS_SLOT_args;
	
	
	private final ImSet<String> packageNames;
	
	private final HashMap<String, ArgsDefinition> nameDefMap= new HashMap<>();
	
	private final Map<String, ArgsDefinition> nameDefMapIm= Collections.unmodifiableMap(nameDefMap);
	
	
	protected RCoreFunctions() {
		this.packageNames= ImCollections.newSet(
				BASE_PACKAGE_NAME,
				UTILS_PACKAGE_NAME,
				METHODS_PACKAGE_NAME );
		
		this.BASE_ASSIGN_args= createBaseAssign();
		this.nameDefMap.put(BASE_ASSIGN_NAME, this.BASE_ASSIGN_args);
		
		this.BASE_REMOVE_args= createBaseRemove();
		this.nameDefMap.put(BASE_REMOVE_NAME, this.BASE_REMOVE_args);
		this.nameDefMap.put(BASE_REMOVE_ALIAS_RM, this.BASE_REMOVE_args);
		
		this.BASE_EXISTS_args= createBaseExists();
		this.nameDefMap.put(BASE_EXISTS_NAME, this.BASE_EXISTS_args);
		
		this.BASE_GET_args= createBaseGet();
		this.nameDefMap.put(BASE_GET_NAME, this.BASE_GET_args);
		
		this.BASE_SAVE_args= createBaseSave();
		this.nameDefMap.put(BASE_SAVE_NAME, this.BASE_SAVE_args);
		
		this.BASE_CALL_args= createBaseCall();
		this.nameDefMap.put(BASE_CALL_NAME, this.BASE_CALL_args);
		
		this.BASE_DOCALL_args= createBaseDoCall();
		this.nameDefMap.put(BASE_DOCALL_NAME, this.BASE_DOCALL_args);
		
		
		this.BASE_LIBRARY_args= createBaseLibrary();
		this.nameDefMap.put(BASE_LIBRARY_NAME, this.BASE_LIBRARY_args);
		
		this.BASE_REQUIRE_args= createBaseRequire();
		this.nameDefMap.put(BASE_REQUIRE_NAME, this.BASE_REQUIRE_args);
		
		this.BASE_GLOBALENV_args= createBaseGlobalenv();
		this.nameDefMap.put(BASE_GLOBALENV_NAME, this.BASE_GLOBALENV_args);
		
		this.BASE_TOPENV_args= createBaseTopenv();
		this.nameDefMap.put(BASE_TOPENV_NAME, this.BASE_TOPENV_args);
		
		this.BASE_GETNAMESPACE_args= createBaseGetNamespace();
		this.nameDefMap.put(BASE_GETNAMESPACE_NAME, this.BASE_GETNAMESPACE_args);
		
		this.BASE_GETNAMESPACENAME_args= createBaseGetNamespaceName();
		this.nameDefMap.put(BASE_GETNAMESPACENAME_NAME, this.BASE_GETNAMESPACENAME_args);
		
		this.BASE_GETNAMESPACEVERSION_args= createBaseGetNamespaceVersion();
		this.nameDefMap.put(BASE_GETNAMESPACEVERSION_NAME, this.BASE_GETNAMESPACEVERSION_args);
		
		this.BASE_GETNAMESPACEEXPORTS_args= createBaseGetNamespaceExports();
		this.nameDefMap.put(BASE_GETNAMESPACEEXPORTS_NAME, this.BASE_GETNAMESPACEEXPORTS_args);
		
		this.BASE_GETNAMESPACEIMPORTS_args= createBaseGetNamespaceImports();
		this.nameDefMap.put(BASE_GETNAMESPACEIMPORTS_NAME, this.BASE_GETNAMESPACEIMPORTS_args);
		
		this.BASE_GETNAMESPACEUSERS_args= createBaseGetNamespaceUsers();
		this.nameDefMap.put(BASE_GETNAMESPACEUSERS_NAME, this.BASE_GETNAMESPACEUSERS_args);
		
		this.BASE_GETEXPORTEDVALUE_args= createBaseGetExportedValue();
		this.nameDefMap.put(BASE_GETEXPORTEDVALUE_NAME, this.BASE_GETEXPORTEDVALUE_args);
		
		this.BASE_ATTACHNAMESPACE_args= createBaseAttachNamespace();
		this.nameDefMap.put(BASE_ATTACHNAMESPACE_NAME, this.BASE_ATTACHNAMESPACE_args);
		
		this.BASE_LOADNAMESPACE_args= createBaseLoadNamespace();
		this.nameDefMap.put(BASE_LOADNAMESPACE_NAME, this.BASE_LOADNAMESPACE_args);
		
		this.BASE_REQUIRENAMESPACE_args= createBaseRequireNamespace();
		this.nameDefMap.put(BASE_REQUIRENAMESPACE_NAME, this.BASE_REQUIRENAMESPACE_args);
		
		this.BASE_ISNAMESPACELOADED_args= createBaseIsNamespaceLoaded();
		this.nameDefMap.put(BASE_ISNAMESPACELOADED_NAME, this.BASE_ISNAMESPACELOADED_args);
		
		this.BASE_UNLOADNAMESPACE_args= createBaseUnloadNamespace();
		this.nameDefMap.put(BASE_UNLOADNAMESPACE_NAME, this.BASE_UNLOADNAMESPACE_args);
		
		
		this.BASE_C_args= createBaseC();
		this.nameDefMap.put(BASE_C_NAME, this.BASE_C_args);
		
		this.BASE_DATAFRAME_args= createBaseC();
		this.nameDefMap.put(BASE_DATAFRAME_NAME, this.BASE_C_args);
		
		this.BASE_USEMETHOD_args= createBaseUseMethod();
		this.nameDefMap.put(BASE_USEMETHOD_NAME, this.BASE_USEMETHOD_args);
		
		this.BASE_NEXTMETHOD_args= createBaseNextMethod();
		this.nameDefMap.put(BASE_NEXTMETHOD_NAME, this.BASE_NEXTMETHOD_args);
		
		
		this.UTILS_METHODS_args= createUtilsMethods();
		this.nameDefMap.put(UTILS_METHODS_NAME, this.UTILS_METHODS_args);
		
		this.UTILS_GETS3METHOD_args= createUtilsGetS3Method();
		this.nameDefMap.put(UTILS_GETS3METHOD_NAME, this.UTILS_GETS3METHOD_args);
		
		this.UTILS_HELP_args= createUtilsHelp();
		this.nameDefMap.put(UTILS_HELP_NAME, this.UTILS_HELP_args);
		
		
		this.METHODS_SETGENERIC_args= createMethodsSetGeneric();
		this.nameDefMap.put(METHODS_SETGENERIC_NAME, this.METHODS_SETGENERIC_args);
		
		this.METHODS_SETGROUPGENERIC_args= createMethodsSetGroupGeneric();
		this.nameDefMap.put(METHODS_SETGROUPGENERIC_NAME, this.METHODS_SETGROUPGENERIC_args);
		
		this.METHODS_REMOVEGENERIC_args= createMethodsRemoveGeneric();
		this.nameDefMap.put(METHODS_REMOVEGENERIC_NAME, this.METHODS_REMOVEGENERIC_args);
		
		this.METHODS_ISGENERIC_args= createMethodsIsGeneric();
		this.nameDefMap.put(METHODS_ISGENERIC_NAME, this.METHODS_ISGENERIC_args);
		
		this.METHODS_ISGROUP_args= createMethodsIsGroup();
		this.nameDefMap.put(METHODS_ISGROUP_NAME, this.METHODS_ISGROUP_args);
		
		this.METHODS_SIGNATURE_args= createMethodsSignature();
		this.nameDefMap.put(METHODS_SIGNATURE_NAME, this.METHODS_SIGNATURE_args);
		
		this.METHODS_SETCLASS_args= createMethodsSetClass();
		this.nameDefMap.put(METHODS_SETCLASS_NAME, this.METHODS_SETCLASS_args);
		
		this.METHODS_SETCLASSUNION_args= createMethodsSetClassUnion();
		this.nameDefMap.put(METHODS_SETCLASSUNION_NAME, this.METHODS_SETCLASSUNION_args);
		
		this.METHODS_REPRESENTATION_args= createMethodsRepresentation();
		this.nameDefMap.put(METHODS_REPRESENTATION_NAME, this.METHODS_REPRESENTATION_args);
		
		this.METHODS_PROTOTYPE_args= createMethodsPrototype();
		this.nameDefMap.put(METHODS_PROTOTYPE_NAME, this.METHODS_PROTOTYPE_args);
		
		this.METHODS_SETIS_args= createMethodsSetIs();
		this.nameDefMap.put(METHODS_SETIS_NAME, this.METHODS_SETIS_args);
		
		this.METHODS_REMOVECLASS_args= createMethodsRemoveClass();
		this.nameDefMap.put(METHODS_REMOVECLASS_NAME, this.METHODS_REMOVECLASS_args);
		
		this.METHODS_RESETCLASS_args= createMethodsResetClass();
		this.nameDefMap.put(METHODS_RESETCLASS_NAME, this.METHODS_RESETCLASS_args);
		
		this.METHODS_SETAS_args= createMethodsSetAs();
		this.nameDefMap.put(METHODS_SETAS_NAME, this.METHODS_SETAS_args);
		
		this.METHODS_SETVALIDITY_args= createMethodsSetValidity();
		this.nameDefMap.put(METHODS_SETVALIDITY_NAME, this.METHODS_SETVALIDITY_args);
		
		this.METHODS_ISCLASS_args= createMethodsIsClass();
		this.nameDefMap.put(METHODS_ISCLASS_NAME, this.METHODS_ISCLASS_args);
		
		this.METHODS_EXTENDS_args= createMethodsExtends();
		this.nameDefMap.put(METHODS_EXTENDS_NAME, this.METHODS_EXTENDS_args);
		
		this.METHODS_GETCLASS_args= createMethodsGetClass();
		this.nameDefMap.put(METHODS_GETCLASS_NAME, this.METHODS_GETCLASS_args);
		
		this.METHODS_GETCLASSDEF_args= createMethodsGetClassDef();
		this.nameDefMap.put(METHODS_GETCLASSDEF_NAME, this.METHODS_GETCLASSDEF_args);
		
		this.METHODS_FINDCLASS_args= createMethodsFindClass();
		this.nameDefMap.put(METHODS_FINDCLASS_NAME, this.METHODS_FINDCLASS_args);
		
		this.METHODS_NEW_args= createMethodsNew();
		this.nameDefMap.put(METHODS_NEW_NAME, this.METHODS_NEW_args);
		
		this.METHODS_AS_args= createMethodsAs();
		this.nameDefMap.put(METHODS_AS_NAME, this.METHODS_AS_args);
		
		this.METHODS_IS_args= createMethodsIs();
		this.nameDefMap.put(METHODS_IS_NAME, this.METHODS_IS_args);
		
		this.METHODS_SETMETHOD_args= createMethodsSetMethod();
		this.nameDefMap.put(METHODS_SETMETHOD_NAME, this.METHODS_SETMETHOD_args);
		
		this.METHODS_REMOVEMETHOD_args= createMethodsRemoveMethod();
		this.nameDefMap.put(METHODS_REMOVEMETHOD_NAME, this.METHODS_REMOVEMETHOD_args);
		
		this.METHODS_REMOVEMETHODS_args= createMethodsRemoveMethods();
		this.nameDefMap.put(METHODS_REMOVEMETHODS_NAME, this.METHODS_REMOVEMETHODS_args);
		
		this.METHODS_EXISTSMETHOD_args= createMethodsExistsMethod();
		this.nameDefMap.put(METHODS_EXISTSMETHOD_NAME, this.METHODS_EXISTSMETHOD_args);
		
		this.METHODS_HASMETHOD_args= createMethodsHasMethod();
		this.nameDefMap.put(METHODS_HASMETHOD_NAME, this.METHODS_HASMETHOD_args);
		
		this.METHODS_GETMETHOD_args= createMethodsGetMethod();
		this.nameDefMap.put(METHODS_GETMETHOD_NAME, this.METHODS_GETMETHOD_args);
		
		this.METHODS_SELECTMETHOD_args= createMethodsSelectMethod();
		this.nameDefMap.put(METHODS_SELECTMETHOD_NAME, this.METHODS_SELECTMETHOD_args);
		
		this.METHODS_GETMETHODS_args= createMethodsGetMethods();
		this.nameDefMap.put(METHODS_GETMETHODS_NAME, this.METHODS_GETMETHODS_args);
		
		this.METHODS_FINDMETHOD_args= createMethodsFindMethod();
		this.nameDefMap.put(METHODS_FINDMETHOD_NAME, this.METHODS_FINDMETHOD_args);
		
		this.METHODS_DUMPMETHOD_args= createMethodsDumpMethod();
		this.nameDefMap.put(METHODS_DUMPMETHOD_NAME, this.METHODS_DUMPMETHOD_args);
		
		this.METHODS_DUMPMETHODS_args= createMethodsDumpMethods();
		this.nameDefMap.put(METHODS_DUMPMETHODS_NAME, this.METHODS_DUMPMETHODS_args);
		
		this.METHODS_SLOT_args= createMethodsSlot();
		this.nameDefMap.put(METHODS_SLOT_NAME, this.METHODS_SLOT_args);
	}
	
	
	protected ArgsDefinition createBaseAssign() {
		return new ArgsBuilder()
				.add("x", UNSPECIFIC_NAME | NAME_AS_STRING)
				.add("value", "pos", "envir", "inherits", "immediate")
				.build();
	}
	
	protected ArgsDefinition createBaseRemove() {
		return new ArgsBuilder()
				.add("...", UNSPECIFIC_NAME | NAME_AS_STRING | NAME_AS_SYMBOL)
				.add("list", "pos", "envir", "inherits")
				.build();
	}
	
	protected ArgsDefinition createBaseExists() {
		return new ArgsBuilder()
				.add("x", UNSPECIFIC_NAME | NAME_AS_STRING)
				.add("where", "envir", "frame", "mode", "inherits")
				.build();
	}
	
	protected ArgsDefinition createBaseGet() {
		return new ArgsBuilder()
				.add("x", UNSPECIFIC_NAME | NAME_AS_STRING)
				.add("pos", "envir", "mode", "inherits")
				.build();
	}
	
	protected ArgsDefinition createBaseGet0() {
		return new ArgsBuilder()
				.add("x", UNSPECIFIC_NAME | NAME_AS_STRING)
				.add("envir", "mode", "inherits", "ifnotfound")
				.build();
	}
	
	protected ArgsDefinition createBaseSave() {
		return new ArgsBuilder()
				.add("...", UNSPECIFIC_NAME | NAME_AS_STRING | NAME_AS_SYMBOL)
				.add("list", UNSPECIFIC_NAME | NAME_AS_STRING)
				.add("file", "ascii", "version", "envir", "compress", "compress_level",
						"eval.promises", "precheck" )
				.build();
	}
	
	protected ArgsDefinition createBaseCall() {
		return new ArgsBuilder()
				.add("name", UNSPECIFIC_NAME | NAME_AS_STRING)
				.add("...")
				.build();
	}
	
	protected ArgsDefinition createBaseDoCall() {
		return new ArgsBuilder()
				.add("what", UNSPECIFIC_NAME | NAME_AS_STRING | UNSPECIFIC_OBJ)
				.add("args", "quote", "envir")
				.build();
	}
	
	protected ArgsDefinition createBaseLibrary() {
		return new ArgsBuilder()
				.add("package", PACKAGE_NAME | NAME_AS_STRING | NAME_AS_SYMBOL)
				.add("help", PACKAGE_NAME | NAME_AS_STRING | NAME_AS_SYMBOL)
				.add("pos", "lib.loc", "character.only", "logical.return", "warn.conflicts",
						"quietly", "verbose" )
				.build();
	}
	
	protected ArgsDefinition createBaseRequire() {
		return new ArgsBuilder()
				.add("package", PACKAGE_NAME | NAME_AS_STRING | NAME_AS_SYMBOL)
				.add("lib.loc", "quietly", "warn.conflicts", "character.only")
				.build();
	}
	
	protected ArgsDefinition createBaseGlobalenv() {
		return new ArgsDefinition();
	}
	
	protected ArgsDefinition createBaseTopenv() {
		return new ArgsDefinition(
				"envir", "matchThisEnv");
	}
	
	protected ArgsDefinition createBaseGetNamespace() {
		return new ArgsBuilder()
				.add("ns", PACKAGE_NAME | NAME_AS_STRING | NAME_AS_SYMBOL)
				.build();
	}
	
	protected ArgsDefinition createBaseGetNamespaceName() {
		return new ArgsBuilder()
				.add("ns", PACKAGE_NAME | NAME_AS_STRING)
				.build();
	}
	
	protected ArgsDefinition createBaseGetNamespaceVersion() {
		return new ArgsBuilder()
				.add("ns", PACKAGE_NAME | NAME_AS_STRING)
				.build();
	}
	
	protected ArgsDefinition createBaseGetNamespaceExports() {
		return new ArgsBuilder()
				.add("ns", PACKAGE_NAME | NAME_AS_STRING)
				.build();
	}
	
	protected ArgsDefinition createBaseGetNamespaceImports() {
		return new ArgsBuilder()
				.add("ns", PACKAGE_NAME | NAME_AS_STRING)
				.build();
	}
	
	protected ArgsDefinition createBaseGetNamespaceUsers() {
		return new ArgsBuilder()
				.add("ns", PACKAGE_NAME | NAME_AS_STRING)
				.build();
	}
	
	protected ArgsDefinition createBaseGetExportedValue() {
		return new ArgsBuilder()
				.add("ns", PACKAGE_NAME | NAME_AS_STRING)
				.add("name")
				.build();
	}
	
	protected ArgsDefinition createBaseAttachNamespace() {
		return new ArgsBuilder()
				.add("ns", PACKAGE_NAME | NAME_AS_STRING)
				.add("pos", "depends")
				.build();
	}
	
	protected ArgsDefinition createBaseLoadNamespace() {
		return new ArgsBuilder()
				.add("package", PACKAGE_NAME | NAME_AS_STRING)
				.add("lib.loc", "keep.source", "partial", "versionCheck")
				.build();
	}
	
	protected ArgsDefinition createBaseRequireNamespace() {
		return new ArgsBuilder()
				.add("package", PACKAGE_NAME | NAME_AS_STRING)
				.add("...", "quietly")
				.build();
	}
	
	protected ArgsDefinition createBaseIsNamespaceLoaded() {
		return new ArgsBuilder()
				.add("name", PACKAGE_NAME | NAME_AS_STRING | NAME_AS_SYMBOL)
				.build();
	}
	
	protected ArgsDefinition createBaseUnloadNamespace() {
		return new ArgsBuilder()
				.add("ns", PACKAGE_NAME | NAME_AS_STRING)
				.build();
	}
	
	
	protected ArgsDefinition createBaseC() {
		return new ArgsBuilder()
				.add("...")
				.add("recursive")
				.build();
	}
	
	protected ArgsDefinition createBaseDataFrame() {
		return new ArgsBuilder()
				.add("...")
				.add("row.names")
				.add("check.rows")
				.add("check.names")
				.add("stringsAsFactors")
				.build();
	}
	
	protected ArgsDefinition createBaseUseMethod() {
		return new ArgsBuilder()
				.add("generic", METHOD_NAME)
				.add("object")
				.build();
	}
	
	protected ArgsDefinition createBaseNextMethod() {
		return new ArgsBuilder()
				.add("name", METHOD_NAME)
				.add("object", "...")
				.build();
	}
	
	protected ArgsDefinition createUtilsMethods() {
		return new ArgsBuilder()
				.add("generic.function", METHOD_OBJ | METHOD_NAME)
				.add("class", CLASS_NAME)
				.build();
	}
	
	protected ArgsDefinition createUtilsGetS3Method() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("class", CLASS_NAME)
				.add("optional")
				.build();
	}
	
	protected ArgsDefinition createUtilsHelp() {
		return new ArgsBuilder()
				.add("topic", HELP_TOPIC_NAME | NAME_AS_STRING | NAME_AS_SYMBOL)
				.add("package", PACKAGE_NAME | NAME_AS_STRING | NAME_AS_SYMBOL)
				.add("lib.loc", "verbose", "try.all.packages", "help_type")
				.build();
	}
	
	
	protected ArgsDefinition createMethodsSetGeneric() {
		return new ArgsDefinition(
				"name", "def", "group", "valueClass", "where", "package" , 
				"signature" , "useAsDefault" , "genericFunction");
	}
	
	protected ArgsDefinition createMethodsSetGroupGeneric() {
		return new ArgsDefinition(
				"name", "def", "group", "valueClass", "knownMembers", 
				"where", "package");
	}
	
	protected ArgsDefinition createMethodsRemoveGeneric() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("where")
				.build();
	}
	
	protected ArgsDefinition createMethodsIsGeneric() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("where", "fdef", "getName")
				.build();
	}
	
	protected ArgsDefinition createMethodsIsGroup() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("where", "fdef")
				.build();
	}
	
	protected ArgsDefinition createMethodsSignature() {
		return new ArgsDefinition(
				"...");
	}
	
	protected ArgsDefinition createMethodsSetClass() {
		return new ArgsBuilder()
			.add("Class", CLASS_NAME)
			.add("representation", "prototype", "contains", "validity", 
				"access", "where", "version", "sealed", "package")
			.build();
	}
	
	protected ArgsDefinition createMethodsSetClassUnion() {
		return new ArgsBuilder()
			.add("name", CLASS_NAME)
			.add("members", "where")
			.build();
	}
	
	protected ArgsDefinition createMethodsRepresentation() {
		return new ArgsDefinition(
				"...");
	}
	
	protected ArgsDefinition createMethodsPrototype() {
		return new ArgsDefinition(
				"...");
	}
	
	protected ArgsDefinition createMethodsSetIs() {
		return new ArgsDefinition(
				"class1", "class2", "test", "coerce", "replace", "by", "where", 
				"classDef", "extensionObject", "doComplete");
	}
	
	protected ArgsDefinition createMethodsRemoveClass() {
		return new ArgsBuilder()
				.add("Class", CLASS_NAME)
				.add("where")
				.build();
	}
	
	protected ArgsDefinition createMethodsResetClass() {
		return new ArgsBuilder()
				.add("Class") // no naming
				.add("classDef", "where")
				.build();
	}
	
	protected ArgsDefinition createMethodsSetAs() {
		return new ArgsBuilder()
				.add("from", CLASS_NAME)
				.add("to", CLASS_NAME)
				.add("def", "replace", "where")
				.build();
	}
	
	protected ArgsDefinition createMethodsSetValidity() {
		return new ArgsBuilder()
				.add("Class", CLASS_NAME) // no naming
				.add("method", "where")
				.build();
	}
	
	protected ArgsDefinition createMethodsIsClass() {
		return new ArgsBuilder()
				.add("Class", CLASS_NAME)
				.add("formal", "where")
				.build();
	}
	
	protected ArgsDefinition createMethodsGetClass() {
		return new ArgsDefinition(
				"Class", ".Force", "where");
	}
	
	protected ArgsDefinition createMethodsGetClassDef() {
		return new ArgsDefinition(
				"Class", "where", "package");
	}
	
	protected ArgsDefinition createMethodsFindClass() {
		return new ArgsDefinition(
				"Class", "where", "unique");
	}
	
	protected ArgsDefinition createMethodsExtends() {
		return new ArgsBuilder()
				.add("class1", CLASS_NAME)
				.add("class2", CLASS_NAME)
				.add("maybe", "fullInfo")
				.build();
	}
	
	protected ArgsDefinition createMethodsNew() {
		return new ArgsBuilder()
				.add("Class", CLASS_NAME)
				.add("...")
				.build();
	}
	
	protected ArgsDefinition createMethodsAs() {
		return new ArgsBuilder()
				.add("object")
				.add("Class", CLASS_NAME)
				.add("strict", "ext")
				.build();
	}
	
	protected ArgsDefinition createMethodsIs() {
		return new ArgsBuilder()
				.add("object")
				.add("class2", CLASS_NAME)
				.build();
	}
	
	protected ArgsDefinition createMethodsSetMethod() {
		return new ArgsDefinition(
				"f", "signature", "definition", "where", "valueClass", "sealed");
	}
	
	protected ArgsDefinition createMethodsRemoveMethod() {
		return new ArgsDefinition(
				"f", "signature", "where");
	}
	
	protected ArgsDefinition createMethodsRemoveMethods() {
		return new ArgsDefinition(
				"f", "where", "all");
	}
	
	protected ArgsDefinition createMethodsExistsMethod() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("signature", "where")
				.build();
	}
	
	protected ArgsDefinition createMethodsHasMethod() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("signature", "where")
				.build();
	}
	
	protected ArgsDefinition createMethodsGetMethod() {
		return new ArgsDefinition(
				"f", "signature", "where", "optional", "mlist", "fdef");
	}
	
	protected ArgsDefinition createMethodsSelectMethod() {
		return new ArgsDefinition(
				"f", "signature", "optional", "useInherited", "mlist", "fdef", "verbose");
	}
	
	protected ArgsDefinition createMethodsGetMethods() {
		return new ArgsDefinition(
				"f", "where");
	}
	
	protected ArgsDefinition createMethodsFindMethod() {
		return new ArgsDefinition(
				"f", "signature", "where");
	}
	
	protected ArgsDefinition createMethodsDumpMethod() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("signature", "file", "where", "def")
				.build();
	}
	
	protected ArgsDefinition createMethodsDumpMethods() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("file", "signature", "methods", "where")
				.build();
	}
	
	protected ArgsDefinition createMethodsSlot() {
		return new ArgsDefinition(
				"object", "name", "check");
	}
	
	
	public ImSet<String> getPackageNames() {
		return this.packageNames;
	}
	
	public Set<String> getKnownFunctions() {
		return this.nameDefMapIm.keySet();
	}
	
	public ArgsDefinition getArgs(final String name) {
		return this.nameDefMap.get(name);
	}
	
}
