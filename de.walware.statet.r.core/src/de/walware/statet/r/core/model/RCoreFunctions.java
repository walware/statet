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

package de.walware.statet.r.core.model;

import static de.walware.statet.r.core.model.ArgsDefinition.CLASS_NAME;
import static de.walware.statet.r.core.model.ArgsDefinition.METHOD_NAME;
import static de.walware.statet.r.core.model.ArgsDefinition.METHOD_OBJ;

import java.util.HashMap;
import java.util.Set;


public class RCoreFunctions {
	
	
	public static final RCoreFunctions DEFAULT = new RCoreFunctions();
	
	
	public static final String BASE_ASSIGN_NAME = "assign";
	public final ArgsDefinition BASE_ASSIGN_args;
	
	public static final String BASE_REMOVE_NAME = "remove";
	public static final String BASE_REMOVE_ALIAS_RM = "rm";
	public final ArgsDefinition BASE_REMOVE_args;
	
	public static final String BASE_EXISTS_NAME = "exists";
	public final ArgsDefinition BASE_EXISTS_args; 
	
	public static final String BASE_GET_NAME = "get";
	public final ArgsDefinition BASE_GET_args;
	
	public static final String BASE_SAVE_NAME = "save";
	public final ArgsDefinition BASE_SAVE_args;
	
	public static final String BASE_CALL_NAME = "call";
	public final ArgsDefinition BASE_CALL_args;
	
	public static final String BASE_LIBRARY_NAME = "library";
	public final ArgsDefinition BASE_LIBRARY_args;
	
	public static final String BASE_REQUIRE_NAME = "require";
	public final ArgsDefinition BASE_REQUIRE_args;
	
	public static final String BASE_GLOBALENV_NAME = "globalenv";
	public final ArgsDefinition BASE_GLOBALENV_args;
	
	public static final String BASE_TOPENV_NAME = "topenv";
	public final ArgsDefinition BASE_TOPENV_args;
	
	public static final String BASE_C_NAME = "c";
	public final ArgsDefinition BASE_C_args; 
	
	public static final String BASE_USEMETHOD_NAME = "UseMethod";
	public final ArgsDefinition BASE_USEMETHOD_args;
	
	public static final String BASE_NEXTMETHOD_NAME = "NextMethod";
	public final ArgsDefinition BASE_NEXTMETHOD_args;
	
	public static final String UTILS_METHODS_NAME = "methods";
	public final ArgsDefinition UTILS_METHODS_args;
	
	public static final String UTILS_GETS3METHOD_NAME = "getS3method";
	public final ArgsDefinition UTILS_GETS3METHOD_args;
	
	
	public static final String METHODS_SETGENERIC_NAME = "setGeneric";
	public final ArgsDefinition METHODS_SETGENERIC_args;
	
	public static final String METHODS_SETGROUPGENERIC_NAME = "setGroupGeneric";
	public final ArgsDefinition METHODS_SETGROUPGENERIC_args;
	
	public static final String METHODS_REMOVEGENERIC_NAME = "removeGeneric";
	public final ArgsDefinition METHODS_REMOVEGENERIC_args;
	
	public static final String METHODS_ISGENERIC_NAME = "isGeneric";
	public final ArgsDefinition METHODS_ISGENERIC_args;
	
	public static final String METHODS_ISGROUP_NAME = "isGroup";
	public final ArgsDefinition METHODS_ISGROUP_args;
	
	public static final String METHODS_SIGNATURE_NAME = "signature";
	public final ArgsDefinition METHODS_SIGNATURE_args;
	
	
	public static final String METHODS_SETCLASS_NAME = "setClass";
	public final ArgsDefinition METHODS_SETCLASS_args;
	
	public static final String METHODS_SETCLASSUNION_NAME = "setClassUnion";
	public final ArgsDefinition METHODS_SETCLASSUNION_args;
	
	public static final String METHODS_REPRESENTATION_NAME = "representation";
	public final ArgsDefinition METHODS_REPRESENTATION_args;
	
	public static final String METHODS_PROTOTYPE_NAME = "prototype";
	public final ArgsDefinition METHODS_PROTOTYPE_args;
	
	public static final String METHODS_SETIS_NAME = "setIs";
	public final ArgsDefinition METHODS_SETIS_args;
	
	public static final String METHODS_REMOVECLASS_NAME = "removeClass";
	public final ArgsDefinition METHODS_REMOVECLASS_args;
	
	public static final String METHODS_RESETCLASS_NAME = "resetClass";
	public final ArgsDefinition METHODS_RESETCLASS_args;
	
	
	public static final String METHODS_ISCLASS_NAME = "isClass";
	public final ArgsDefinition METHODS_ISCLASS_args;
	
	public static final String METHODS_EXTENDS_NAME = "extends";
	public final ArgsDefinition METHODS_EXTENDS_args;
	
	public static final String METHODS_GETCLASS_NAME = "getClass";
	public final ArgsDefinition METHODS_GETCLASS_args;
	
	public static final String METHODS_GETCLASSDEF_NAME = "getClassDef";
	public final ArgsDefinition METHODS_GETCLASSDEF_args;
	
	public static final String METHODS_FINDCLASS_NAME = "findClass";
	public final ArgsDefinition METHODS_FINDCLASS_args;
	
	
	public static final String METHODS_NEW_NAME = "new";
	public final ArgsDefinition METHODS_NEW_args;
	
	public static final String METHODS_AS_NAME = "as";
	public final ArgsDefinition METHODS_AS_args;
	
	public static final String METHODS_IS_NAME = "is";
	public final ArgsDefinition METHODS_IS_args;
	
	
	public static final String METHODS_SETMETHOD_NAME = "setMethod";
	public final ArgsDefinition METHODS_SETMETHOD_args;
	
	public static final String METHODS_REMOVEMETHOD_NAME = "removeMethod";
	public final ArgsDefinition METHODS_REMOVEMETHOD_args;
	
	public static final String METHODS_REMOVEMETHODS_NAME = "removeMethods";
	public final ArgsDefinition METHODS_REMOVEMETHODS_args;
	
	public static final String METHODS_EXISTSMETHOD_NAME = "existsMethod";
	public final ArgsDefinition METHODS_EXISTSMETHOD_args;
	
	public static final String METHODS_HASMETHOD_NAME = "hasMethod";
	public final ArgsDefinition METHODS_HASMETHOD_args;
	
	public static final String METHODS_GETMETHOD_NAME = "getMethod";
	public final ArgsDefinition METHODS_GETMETHOD_args;
	
	public static final String METHODS_SELECTMETHOD_NAME = "selectMethod";
	public final ArgsDefinition METHODS_SELECTMETHOD_args;
	
	public static final String METHODS_GETMETHODS_NAME = "getMethods";
	public final ArgsDefinition METHODS_GETMETHODS_args;
	
	public static final String METHODS_FINDMETHOD_NAME = "findMethod";
	public final ArgsDefinition METHODS_FINDMETHOD_args;
	
	public static final String METHODS_DUMPMETHOD_NAME = "dumpMethod";
	public final ArgsDefinition METHODS_DUMPMETHOD_args;
	
	public static final String METHODS_DUMPMETHODS_NAME = "dumpMethods";
	public final ArgsDefinition METHODS_DUMPMETHODS_args;
	
	public static final String METHODS_SLOT_NAME = "slot";
	public final ArgsDefinition METHODS_SLOT_args;
	
	
	public final HashMap<String, ArgsDefinition> fNameDefMap = new HashMap<String, ArgsDefinition>();
	
	
	protected RCoreFunctions() {
		BASE_ASSIGN_args = createBaseAssign();
		fNameDefMap.put(BASE_ASSIGN_NAME, BASE_ASSIGN_args);
		
		BASE_REMOVE_args = createBaseRemove();
		fNameDefMap.put(BASE_REMOVE_NAME, BASE_REMOVE_args);
		fNameDefMap.put(BASE_REMOVE_ALIAS_RM, BASE_REMOVE_args);
		
		BASE_EXISTS_args = createBaseExists();
		fNameDefMap.put(BASE_EXISTS_NAME, BASE_EXISTS_args);
		
		BASE_GET_args = createBaseGet();
		fNameDefMap.put(BASE_GET_NAME, BASE_GET_args);
		
		BASE_SAVE_args = createBaseSave();
		fNameDefMap.put(BASE_SAVE_NAME, BASE_SAVE_args);
		
		BASE_CALL_args = createBaseCall();
		fNameDefMap.put(BASE_CALL_NAME, BASE_CALL_args);
		
		BASE_LIBRARY_args = createBaseLibrary();
		fNameDefMap.put(BASE_LIBRARY_NAME, BASE_LIBRARY_args);
		
		BASE_REQUIRE_args = createBaseRequire();
		fNameDefMap.put(BASE_REQUIRE_NAME, BASE_REQUIRE_args);
		
		BASE_GLOBALENV_args = createBaseGlobalenv();
		fNameDefMap.put(BASE_GLOBALENV_NAME, BASE_GLOBALENV_args);
		
		BASE_TOPENV_args = createBaseTopenv();
		fNameDefMap.put(BASE_TOPENV_NAME, BASE_TOPENV_args);
		
		BASE_USEMETHOD_args = createBaseUseMethod();
		fNameDefMap.put(BASE_USEMETHOD_NAME, BASE_USEMETHOD_args);
		
		BASE_NEXTMETHOD_args = createBaseNextMethod();
		fNameDefMap.put(BASE_NEXTMETHOD_NAME, BASE_NEXTMETHOD_args);
		
		BASE_C_args = createBaseC();
		fNameDefMap.put(BASE_C_NAME, BASE_C_args);
		
		
		UTILS_METHODS_args = createUtilsMethods();
		fNameDefMap.put(UTILS_METHODS_NAME, UTILS_METHODS_args);
		
		UTILS_GETS3METHOD_args = createUtilsGetS3Method();
		fNameDefMap.put(UTILS_GETS3METHOD_NAME, UTILS_GETS3METHOD_args);
		
		
		METHODS_SETGENERIC_args = createMethodsSetGeneric();
		fNameDefMap.put(METHODS_SETGENERIC_NAME, METHODS_SETGENERIC_args);
		
		METHODS_SETGROUPGENERIC_args = createMethodsSetGroupGeneric();
		fNameDefMap.put(METHODS_SETGROUPGENERIC_NAME, METHODS_SETGROUPGENERIC_args);
		
		METHODS_REMOVEGENERIC_args = createMethodsRemoveGeneric();
		fNameDefMap.put(METHODS_REMOVEGENERIC_NAME, METHODS_REMOVEGENERIC_args);
		
		METHODS_ISGENERIC_args = createMethodsIsGeneric();
		fNameDefMap.put(METHODS_ISGENERIC_NAME, METHODS_ISGENERIC_args);
		
		METHODS_ISGROUP_args = createMethodsIsGroup();
		fNameDefMap.put(METHODS_ISGROUP_NAME, METHODS_ISGROUP_args);
		
		METHODS_SIGNATURE_args = createMethodsSignature();
		fNameDefMap.put(METHODS_SIGNATURE_NAME, METHODS_SIGNATURE_args);
		
		METHODS_SETCLASS_args = createMethodsSetClass();
		fNameDefMap.put(METHODS_SETCLASS_NAME, METHODS_SETCLASS_args);
		
		METHODS_SETCLASSUNION_args = createMethodsSetClassUnion();
		fNameDefMap.put(METHODS_SETCLASSUNION_NAME, METHODS_SETCLASSUNION_args);
		
		METHODS_REPRESENTATION_args = createMethodsRepresentation();
		fNameDefMap.put(METHODS_REPRESENTATION_NAME, METHODS_REPRESENTATION_args);
		
		METHODS_PROTOTYPE_args = createMethodsPrototype();
		fNameDefMap.put(METHODS_PROTOTYPE_NAME, METHODS_PROTOTYPE_args);
		
		METHODS_SETIS_args = createMethodsSetIs();
		fNameDefMap.put(METHODS_SETIS_NAME, METHODS_SETIS_args);
		
		METHODS_REMOVECLASS_args = createMethodsRemoveClass();
		fNameDefMap.put(METHODS_REMOVECLASS_NAME, METHODS_REMOVECLASS_args);
		
		METHODS_RESETCLASS_args = createMethodsRemoveClass();
		fNameDefMap.put(METHODS_RESETCLASS_NAME, METHODS_RESETCLASS_args);
		
		METHODS_ISCLASS_args = createMethodsIsClass();
		fNameDefMap.put(METHODS_ISCLASS_NAME, METHODS_ISCLASS_args);
		
		METHODS_EXTENDS_args = createMethodsExtends();
		fNameDefMap.put(METHODS_EXTENDS_NAME, METHODS_EXTENDS_args);
		
		METHODS_GETCLASS_args = createMethodsGetClass();
		fNameDefMap.put(METHODS_GETCLASS_NAME, METHODS_GETCLASS_args);
		
		METHODS_GETCLASSDEF_args = createMethodsGetClassDef();
		fNameDefMap.put(METHODS_GETCLASSDEF_NAME, METHODS_GETCLASSDEF_args);
		
		METHODS_FINDCLASS_args = createMethodsFindClass();
		fNameDefMap.put(METHODS_FINDCLASS_NAME, METHODS_FINDCLASS_args);
		
		METHODS_NEW_args = createMethodsNew();
		fNameDefMap.put(METHODS_NEW_NAME, METHODS_NEW_args);
		
		METHODS_AS_args = createMethodsAs();
		fNameDefMap.put(METHODS_AS_NAME, METHODS_AS_args);
		
		METHODS_IS_args = createMethodsIs();
		fNameDefMap.put(METHODS_IS_NAME, METHODS_IS_args);
		
		METHODS_SETMETHOD_args = createMethodsSetMethod();
		fNameDefMap.put(METHODS_SETMETHOD_NAME, METHODS_SETMETHOD_args);
		
		METHODS_REMOVEMETHOD_args = createMethodsRemoveMethod();
		fNameDefMap.put(METHODS_REMOVEMETHOD_NAME, METHODS_REMOVEMETHOD_args);
		
		METHODS_REMOVEMETHODS_args = createMethodsRemoveMethods();
		fNameDefMap.put(METHODS_REMOVEMETHODS_NAME, METHODS_REMOVEMETHODS_args);
		
		METHODS_EXISTSMETHOD_args = createMethodsExistsMethod();
		fNameDefMap.put(METHODS_EXISTSMETHOD_NAME, METHODS_EXISTSMETHOD_args);
		
		METHODS_HASMETHOD_args = createMethodsHasMethod();
		fNameDefMap.put(METHODS_HASMETHOD_NAME, METHODS_HASMETHOD_args);
		
		METHODS_GETMETHOD_args = createMethodsGetMethod();
		fNameDefMap.put(METHODS_GETMETHOD_NAME, METHODS_GETMETHOD_args);
		
		METHODS_SELECTMETHOD_args = createMethodsSelectMethod();
		fNameDefMap.put(METHODS_SELECTMETHOD_NAME, METHODS_SELECTMETHOD_args);
		
		METHODS_GETMETHODS_args = createMethodsGetMethods();
		fNameDefMap.put(METHODS_GETMETHODS_NAME, METHODS_GETMETHODS_args);
		
		METHODS_FINDMETHOD_args = createMethodsFindMethod();
		fNameDefMap.put(METHODS_FINDMETHOD_NAME, METHODS_FINDMETHOD_args);
		
		METHODS_DUMPMETHOD_args = createMethodsDumpMethod();
		fNameDefMap.put(METHODS_DUMPMETHOD_NAME, METHODS_DUMPMETHOD_args);
		
		METHODS_DUMPMETHODS_args = createMethodsDumpMethods();
		fNameDefMap.put(METHODS_DUMPMETHODS_NAME, METHODS_DUMPMETHODS_args);
		
		METHODS_SLOT_args = createMethodsSlot();
		fNameDefMap.put(METHODS_SLOT_NAME, METHODS_SLOT_args);
	}
	
	
	ArgsDefinition createBaseAssign() {
		return new ArgsDefinition(
				"x", "value", "pos", "envir", "inherits", "immediate");
	}
	
	ArgsDefinition createBaseRemove() {
		return new ArgsDefinition(
				"...", "list", "pos", "envir", "inherits");
	}
	
	ArgsDefinition createBaseExists() {
		return new ArgsDefinition(
				"x", "where", "envir", "frame", "mode", "inherits");
	}
	
	ArgsDefinition createBaseGet() {
		return new ArgsDefinition(
				"x", "pos", "envir", "mode", "inherits");
	}
	
	ArgsDefinition createBaseSave() {
		return new ArgsDefinition(
				"...", "list", "ascii", "version", "envir", "compress", "eval.promises");
	}
	
	ArgsDefinition createBaseCall() {
		return new ArgsDefinition(
				"name", "...");
	}
	
	ArgsDefinition createBaseLibrary() {
		return new ArgsDefinition(
				"package", "help", "pos", "lib.loc", "character.only", 
				"logical.return", "warn.conflicts", "keep.source",
				"verbose", "version");
	}
	
	ArgsDefinition createBaseRequire() {
		return new ArgsDefinition(
				"package", "lib.loc", "quietly", "warn.conflicts", "keep.source", 
				"character.only", "version", "save");
	}
	
	ArgsDefinition createBaseGlobalenv() {
		return new ArgsDefinition();
	}
	
	ArgsDefinition createBaseTopenv() {
		return new ArgsDefinition(
				"envir", "matchThisEnv");
	}
	
	
	ArgsDefinition createBaseC() {
		return new ArgsBuilder()
				.add("...")
				.add("recursive")
				.toDef();
	}
	
	ArgsDefinition createBaseUseMethod() {
		return new ArgsBuilder()
				.add("generic", METHOD_NAME)
				.add("object")
				.toDef();
	}
	
	ArgsDefinition createBaseNextMethod() {
		return new ArgsBuilder()
				.add("name", METHOD_NAME)
				.add("object", "...")
				.toDef();
	}
	
	ArgsDefinition createUtilsMethods() {
		return new ArgsBuilder()
				.add("generic.function", METHOD_OBJ | METHOD_NAME)
				.add("class", CLASS_NAME)
				.toDef();
	}
	
	ArgsDefinition createUtilsGetS3Method() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("class", CLASS_NAME)
				.add("optional")
				.toDef();
	}
	
	ArgsDefinition createMethodsSetGeneric() {
		return new ArgsDefinition(
				"name", "def", "group", "valueClass", "where", "package" , 
				"signature" , "useAsDefault" , "genericFunction");
	}
	
	ArgsDefinition createMethodsSetGroupGeneric() {
		return new ArgsDefinition(
				"name", "def", "group", "valueClass", "knownMembers", 
				"where", "package");
	}
	
	ArgsDefinition createMethodsRemoveGeneric() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("where")
				.toDef();
	}
	
	ArgsDefinition createMethodsIsGeneric() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("where", "fdef", "getName")
				.toDef();
	}
	
	ArgsDefinition createMethodsIsGroup() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("where", "fdef")
				.toDef();
	}
	
	ArgsDefinition createMethodsSignature() {
		return new ArgsDefinition(
				"...");
	}
	
	ArgsDefinition createMethodsSetClass() {
		return new ArgsBuilder()
			.add("Class", CLASS_NAME)
			.add("representation", "prototype", "contains", "validity", 
				"access", "where", "version", "sealed", "package")
			.toDef();
	}
	
	ArgsDefinition createMethodsSetClassUnion() {
		return new ArgsBuilder()
			.add("name", CLASS_NAME)
			.add("members", "where")
			.toDef();
	}
	
	ArgsDefinition createMethodsRepresentation() {
		return new ArgsDefinition(
				"...");
	}
	
	ArgsDefinition createMethodsPrototype() {
		return new ArgsDefinition(
				"...");
	}
	
	ArgsDefinition createMethodsSetIs() {
		return new ArgsDefinition(
				"class1", "class2", "test", "coerce", "replace", "by", "where", 
				"classDef", "extensionObject", "doComplete");
	}
	
	ArgsDefinition createMethodsRemoveClass() {
		return new ArgsBuilder()
				.add("Class", CLASS_NAME)
				.add("where")
				.toDef();
	}
	
	ArgsDefinition createMethodsResetClass() {
		return new ArgsBuilder()
				.add("Class") // no naming
				.add("classDef", "where")
				.toDef();
	}
	
	ArgsDefinition createMethodsIsClass() {
		return new ArgsBuilder()
				.add("Class", CLASS_NAME)
				.add("formal", "where")
				.toDef();
	}
	
	ArgsDefinition createMethodsGetClass() {
		return new ArgsDefinition(
				"Class", ".Force", "where");
	}
	
	ArgsDefinition createMethodsGetClassDef() {
		return new ArgsDefinition(
				"Class", "where", "package");
	}
	
	ArgsDefinition createMethodsFindClass() {
		return new ArgsDefinition(
				"Class", "where", "unique");
	}
	
	ArgsDefinition createMethodsExtends() {
		return new ArgsBuilder()
				.add("class1", CLASS_NAME)
				.add("class2", CLASS_NAME)
				.add("maybe", "fullInfo")
				.toDef();
	}
	
	ArgsDefinition createMethodsNew() {
		return new ArgsBuilder()
				.add("Class", CLASS_NAME)
				.add("...")
				.toDef();
	}
	
	ArgsDefinition createMethodsAs() {
		return new ArgsBuilder()
				.add("object")
				.add("Class", CLASS_NAME)
				.add("strict", "ext")
				.toDef();
	}
	
	ArgsDefinition createMethodsIs() {
		return new ArgsBuilder()
				.add("object")
				.add("class2", CLASS_NAME)
				.toDef();
	}
	
	ArgsDefinition createMethodsSetMethod() {
		return new ArgsDefinition(
				"f", "signature", "definition", "where", "valueClass", "sealed");
	}
	
	ArgsDefinition createMethodsRemoveMethod() {
		return new ArgsDefinition(
				"f", "signature", "where");
	}
	
	ArgsDefinition createMethodsRemoveMethods() {
		return new ArgsDefinition(
				"f", "where", "all");
	}
	
	ArgsDefinition createMethodsExistsMethod() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("signature", "where")
				.toDef();
	}
	
	ArgsDefinition createMethodsHasMethod() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("signature", "where")
				.toDef();
	}
	
	ArgsDefinition createMethodsGetMethod() {
		return new ArgsDefinition(
				"f", "signature", "where", "optional", "mlist", "fdef");
	}
	
	ArgsDefinition createMethodsSelectMethod() {
		return new ArgsDefinition(
				"f", "signature", "optional", "useInherited", "mlist", "fdef", "verbose");
	}
	
	ArgsDefinition createMethodsGetMethods() {
		return new ArgsDefinition(
				"f", "where");
	}
	
	ArgsDefinition createMethodsFindMethod() {
		return new ArgsDefinition(
				"f", "signature", "where");
	}
	
	ArgsDefinition createMethodsDumpMethod() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("signature", "file", "where", "def")
				.toDef();
	}
	
	ArgsDefinition createMethodsDumpMethods() {
		return new ArgsBuilder()
				.add("f", METHOD_NAME)
				.add("file", "signature", "methods", "where")
				.toDef();
	}
	
	ArgsDefinition createMethodsSlot() {
		return new ArgsDefinition(
				"object", "name", "check");
	}
	
	
	public Set<String> getKnownFunctions() {
		return fNameDefMap.keySet();
	}
	public ArgsDefinition getArgs(final String name) {
		return fNameDefMap.get(name);
	}
	
	
}
