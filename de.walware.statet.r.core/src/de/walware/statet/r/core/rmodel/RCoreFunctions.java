/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rmodel;

import static de.walware.statet.r.core.rmodel.ArgsDefinition.CLASSNAME_STRING;

import java.util.HashMap;
import java.util.Random;


public class RCoreFunctions {
	
	
	public static final RCoreFunctions DEFAULT = new RCoreFunctions();
	
	
	public static final String BASE_ASSIGN_NAME = "assign";
	public static final int BASE_ASSIGN_ID =                      1;
	public final ArgsDefinition BASE_ASSIGN_args;
	public final int BASE_ASSIGN_arg_x;
	public final int BASE_ASSIGN_arg_value;
	
	public static final String BASE_REMOVE_NAME = "remove";
	public static final String BASE_REMOVE_ALIAS_RM = "rm";
	public static final int BASE_REMOVE_ID =                      2;
	public final ArgsDefinition BASE_REMOVE_args;
	public final int BASE_REMOVE_arg_x;
	
	public static final String BASE_EXISTS_NAME = "exists";
	public static final int BASE_EXISTS_ID =                      3;
	public final ArgsDefinition BASE_EXISTS_args; 
	public final int BASE_EXISTS_arg_x;
	
	public static final String BASE_GET_NAME = "get";
	public static final int BASE_GET_ID =                         4;
	public final ArgsDefinition BASE_GET_args;
	public final int BASE_GET_arg_x;
	
	public static final String BASE_SAVE_NAME = "save";
	public static final int BASE_SAVE_ID =                        5;
	public final ArgsDefinition BASE_SAVE_args;
	public final int BASE_SAVE_arg_x;
	
	public static final String BASE_CALL_NAME = "call";
	public static final int BASE_CALL_ID =                        6;
	public final ArgsDefinition BASE_CALL_args;
	public final int BASE_CALL_arg_name;
	
	
	public static final String METHODS_SETGENERIC_NAME = "setGeneric";
	public static final int METHODS_SETGENERIC_ID =              10;
	public final ArgsDefinition METHODS_SETGENERIC_args;
	public final int METHODS_SETGENERIC_arg_f;
	
	public static final String METHODS_SETGROUPGENERIC_NAME = "setGroupGeneric";
	public static final int METHODS_SETGROUPGENERIC_ID =         11;
	public final ArgsDefinition METHODS_SETGROUPGENERIC_args;
	public final int METHODS_SETGROUPGENERIC_arg_f;
	
	public static final String METHODS_REMOVEGENERIC_NAME = "removeGeneric";
	public static final int METHODS_REMOVEGENERIC_ID =           12;
	public final ArgsDefinition METHODS_REMOVEGENERIC_args;
	public final int METHODS_REMOVEGENERIC_arg_f;
	
	public static final String METHODS_ISGENERIC_NAME = "isGeneric";
	public static final int METHODS_ISGENERIC_ID =               13;
	public final ArgsDefinition METHODS_ISGENERIC_args;
	public final int METHODS_ISGENERIC_arg_f;
	
	public static final String METHODS_ISGROUP_NAME = "isGroup";
	public static final int METHODS_ISGROUP_ID =                 14;
	public final ArgsDefinition METHODS_ISGROUP_args;
	public final int METHODS_ISGROUP_arg_f;
	
	public static final String METHODS_SIGNATURE_NAME = "signature";
	public static final int METHODS_SIGNATURE_ID =               19;
	public final ArgsDefinition METHODS_SIGNATURE_args;
	
	
	public static final String METHODS_SETCLASS_NAME = "setClass";
	public static final int METHODS_SETCLASS_ID =                20;
	public final ArgsDefinition METHODS_SETCLASS_args;
	public final int METHODS_SETCLASS_arg_class;
	
	public static final String METHODS_SETIS_NAME = "setIs";
	public static final int METHODS_SETIS_ID =                   21;
	public final ArgsDefinition METHODS_SETIS_args;
	public final int METHODS_SETIS_arg_class;
	public final int METHODS_SETIS_arg_classToExtend;
	
	public static final String METHODS_REMOVECLASS_NAME = "removeClass";
	public static final int METHODS_REMOVECLASS_ID =             22;
	public final ArgsDefinition METHODS_REMOVECLASS_args;
	public final int METHODS_REMOVECLASS_arg_class;
	
	public static final String METHODS_RESETCLASS_NAME = "resetClass";
	public static final int METHODS_RESETCLASS_ID =              23;
	public final ArgsDefinition METHODS_RESETCLASS_args;
	public final int METHODS_RESETCLASS_arg_class; // not naming
	
	public static final String METHODS_ISCLASS_NAME = "isClass";
	public static final int METHODS_ISCLASS_ID =                 24;
	public final ArgsDefinition METHODS_ISCLASS_args;
	public final int METHODS_ISCLASS_arg_class;
	
	public static final String METHODS_EXTENDS_NAME = "extends";
	public static final int METHODS_EXTENDS_ID =                 25;
	public final ArgsDefinition METHODS_EXTENDS_args;
	public final int METHODS_EXTENDS_arg_class;
	public final int METHODS_EXTENDS_arg_classToExtend;
	
	public static final String METHODS_GETCLASS_NAME = "getClass";
	public static final int METHODS_GETCLASS_ID =                27;
	public final ArgsDefinition METHODS_GETCLASS_args;
	public final int METHODS_GETCLASS_arg_class;
	
	public static final String METHODS_GETCLASSDEF_NAME = "getClassDef";
	public static final int METHODS_GETCLASSDEF_ID =             28;
	public final ArgsDefinition METHODS_GETCLASSDEF_args;
	public final int METHODS_GETCLASSDEF_arg_class;
	
	public static final String METHODS_FINDCLASS_NAME = "findClass";
	public static final int METHODS_FINDCLASS_ID =               29;
	public final ArgsDefinition METHODS_FINDCLASS_args;
	public final int METHODS_FINDCLASS_arg_class;
	
	
	public static final String METHODS_NEW_NAME = "new";
	public static final int METHODS_NEW_ID =                     30;
	public final ArgsDefinition METHODS_NEW_args;
	public final int METHODS_NEW_arg_class;
	
	public static final String METHODS_AS_NAME = "as";
	public static final int METHODS_AS_ID =                      31;
	public final ArgsDefinition METHODS_AS_args;
	public final int METHODS_AS_arg_class;
	
	public static final String METHODS_IS_NAME = "is";
	public static final int METHODS_IS_ID =                      35;
	public final ArgsDefinition METHODS_IS_args;
	public final int METHODS_IS_arg_class;
	
	
	public static final String METHODS_SETMETHOD_NAME = "setMethod";
	public static final int METHODS_SETMETHOD_ID =               40;
	public final ArgsDefinition METHODS_SETMETHOD_args;
	public final int METHODS_SETMETHOD_arg_f;
	public final int METHODS_SETMETHOD_arg_signature;
	
	public static final String METHODS_REMOVEMETHOD_NAME = "removeMethod";
	public static final int METHODS_REMOVEMETHOD_ID =            41;
	public final ArgsDefinition METHODS_REMOVEMETHOD_args;
	public final int METHODS_REMOVEMETHOD_arg_f;
	public final int METHODS_REMOVEMETHOD_arg_signature;
	
	public static final String METHODS_REMOVEMETHODS_NAME = "removeMethods";
	public static final int METHODS_REMOVEMETHODS_ID =           42;
	public final ArgsDefinition METHODS_REMOVEMETHODS_args;
	public final int METHODS_REMOVEMETHODS_arg_f;
	
	public static final String METHODS_EXISTSMETHOD_NAME = "existsMethod";
	public static final int METHODS_EXISTSMETHOD_ID =            44;
	public final ArgsDefinition METHODS_EXISTSMETHOD_args;
	public final int METHODS_EXISTSMETHOD_arg_f;
	public final int METHODS_EXISTSMETHOD_arg_signature;
	
	public static final String METHODS_HASMETHOD_NAME = "hasMethod";
	public static final int METHODS_HASMETHOD_ID =               45;
	public final ArgsDefinition METHODS_HASMETHOD_args;
	public final int METHODS_HASMETHOD_arg_f;
	public final int METHODS_HASMETHOD_arg_signature;
	
	public static final String METHODS_GETMETHOD_NAME = "getMethod";
	public static final int METHODS_GETMETHOD_ID =               46;
	public final ArgsDefinition METHODS_GETMETHOD_args;
	public final int METHODS_GETMETHOD_arg_f;
	public final int METHODS_GETMETHOD_arg_signature;
	
	public static final String METHODS_SELECTMETHOD_NAME = "selectMethod";
	public static final int METHODS_SELECTMETHOD_ID =            47;
	public final ArgsDefinition METHODS_SELECTMETHOD_args;
	public final int METHODS_SELECTMETHOD_arg_f;
	public final int METHODS_SELECTMETHOD_arg_signature;
	
	public static final String METHODS_GETMETHODS_NAME = "getMethods";
	public static final int METHODS_GETMETHODS_ID =              48;
	public final ArgsDefinition METHODS_GETMETHODS_args;
	public final int METHODS_GETMETHODS_arg_f;
	
	public static final String METHODS_FINDMETHOD_NAME = "findMethod";
	public static final int METHODS_FINDMETHOD_ID =              49;
	public final ArgsDefinition METHODS_FINDMETHOD_args;
	public final int METHODS_FINDMETHOD_arg_f;
	public final int METHODS_FINDMETHOD_arg_signature;
	
	public static final String METHODS_DUMPMETHOD_NAME = "dumpMethod";
	public static final int METHODS_DUMPMETHOD_ID =              50;
	public final ArgsDefinition METHODS_DUMPMETHOD_args;
	public final int METHODS_DUMPMETHOD_arg_f;
	
	public static final String METHODS_DUMPMETHODS_NAME = "dumpMethods";
	public static final int METHODS_DUMPMETHODS_ID =             51;
	public final ArgsDefinition METHODS_DUMPMETHODS_args;
	public final int METHODS_DUMPMETHODS_arg_f;
	
	public static final String METHODS_SLOT_NAME = "slot";
	public static final int METHODS_SLOT_ID =                    60;
	public final ArgsDefinition METHODS_SLOT_args;
	public final int METHODS_SLOT_arg_object;
	public final int METHODS_SLOT_arg_slot;
	
	
	public final HashMap<String, ArgsDefinition> fNameDefMap = new HashMap<String, ArgsDefinition>();
	
	
	protected RCoreFunctions() {
		BASE_ASSIGN_args = createBaseAssign();
		BASE_ASSIGN_arg_x = BASE_ASSIGN_args.indexOf("x");
		BASE_ASSIGN_arg_value = BASE_ASSIGN_args.indexOf("value");
		fNameDefMap.put(BASE_ASSIGN_NAME, BASE_ASSIGN_args);
		
		BASE_REMOVE_args = createBaseRemove();
		BASE_REMOVE_arg_x = BASE_REMOVE_args.indexOf("x");
		fNameDefMap.put(BASE_REMOVE_NAME, BASE_REMOVE_args);
		fNameDefMap.put(BASE_REMOVE_ALIAS_RM, BASE_REMOVE_args);
		
		BASE_EXISTS_args = createBaseExists();
		BASE_EXISTS_arg_x = BASE_EXISTS_args.indexOf("x");
		fNameDefMap.put(BASE_EXISTS_NAME, BASE_EXISTS_args);
		
		BASE_GET_args = createBaseGet();
		BASE_GET_arg_x = BASE_GET_args.indexOf("x");
		fNameDefMap.put(BASE_GET_NAME, BASE_GET_args);
		
		BASE_SAVE_args = createBaseSave();
		BASE_SAVE_arg_x = BASE_SAVE_args.indexOf("x");
		fNameDefMap.put(BASE_SAVE_NAME, BASE_SAVE_args);
		
		BASE_CALL_args = createBaseCall();
		BASE_CALL_arg_name = BASE_CALL_args.indexOf("name");
		fNameDefMap.put(BASE_CALL_NAME, BASE_CALL_args);
		
		METHODS_SETGENERIC_args = createMethodsSetGeneric();
		METHODS_SETGENERIC_arg_f = METHODS_SETGENERIC_args.indexOf("name");
		fNameDefMap.put(METHODS_SETGENERIC_NAME, METHODS_SETGENERIC_args);
		
		METHODS_SETGROUPGENERIC_args = createMethodsSetGroupGeneric();
		METHODS_SETGROUPGENERIC_arg_f = METHODS_SETGROUPGENERIC_args.indexOf("name");
		fNameDefMap.put(METHODS_SETGROUPGENERIC_NAME, METHODS_SETGROUPGENERIC_args);
		
		METHODS_REMOVEGENERIC_args = createMethodsRemoveGeneric();
		METHODS_REMOVEGENERIC_arg_f = METHODS_REMOVEGENERIC_args.indexOf("f");
		fNameDefMap.put(METHODS_REMOVEGENERIC_NAME, METHODS_REMOVEGENERIC_args);
		
		METHODS_ISGENERIC_args = createMethodsIsGeneric();
		METHODS_ISGENERIC_arg_f = METHODS_ISGENERIC_args.indexOf("f");
		fNameDefMap.put(METHODS_ISGENERIC_NAME, METHODS_ISGENERIC_args);
		
		METHODS_ISGROUP_args = createMethodsIsGroup();
		METHODS_ISGROUP_arg_f = METHODS_ISGROUP_args.indexOf("f");
		fNameDefMap.put(METHODS_ISGROUP_NAME, METHODS_ISGROUP_args);
		
		METHODS_SIGNATURE_args = createMethodsSignature();
		fNameDefMap.put(METHODS_SIGNATURE_NAME, METHODS_SIGNATURE_args);
		
		METHODS_SETCLASS_args = createMethodsSetClass();
		METHODS_SETCLASS_arg_class = METHODS_SETCLASS_args.indexOf("Class");
		fNameDefMap.put(METHODS_SETCLASS_NAME, METHODS_SETCLASS_args);
		
		METHODS_SETIS_args = createMethodsSetIs();
		METHODS_SETIS_arg_class = METHODS_SETIS_args.indexOf("class1");
		METHODS_SETIS_arg_classToExtend = METHODS_SETIS_args.indexOf("class2");
		fNameDefMap.put(METHODS_SETIS_NAME, METHODS_SETIS_args);
		
		METHODS_REMOVECLASS_args = createMethodsRemoveClass();
		METHODS_REMOVECLASS_arg_class = METHODS_REMOVECLASS_args.indexOf("Class");
		fNameDefMap.put(METHODS_REMOVECLASS_NAME, METHODS_REMOVECLASS_args);
		
		METHODS_RESETCLASS_args = createMethodsRemoveClass();
		METHODS_RESETCLASS_arg_class = METHODS_RESETCLASS_args.indexOf("Class");
		fNameDefMap.put(METHODS_RESETCLASS_NAME, METHODS_RESETCLASS_args);
		
		METHODS_ISCLASS_args = createMethodsIsClass();
		METHODS_ISCLASS_arg_class = METHODS_ISCLASS_args.indexOf("Class");
		fNameDefMap.put(METHODS_ISCLASS_NAME, METHODS_ISCLASS_args);
		
		METHODS_EXTENDS_args = createMethodsExtends();
		METHODS_EXTENDS_arg_class = METHODS_EXTENDS_args.indexOf("class1");
		METHODS_EXTENDS_arg_classToExtend = METHODS_EXTENDS_args.indexOf("class2");
		fNameDefMap.put(METHODS_EXTENDS_NAME, METHODS_EXTENDS_args);
		
		METHODS_GETCLASS_args = createMethodsGetClass();
		METHODS_GETCLASS_arg_class = METHODS_GETCLASS_args.indexOf("Class");
		fNameDefMap.put(METHODS_GETCLASS_NAME, METHODS_GETCLASS_args);
		
		METHODS_GETCLASSDEF_args = createMethodsGetClassDef();
		METHODS_GETCLASSDEF_arg_class = METHODS_GETCLASSDEF_args.indexOf("Class");
		fNameDefMap.put(METHODS_GETCLASSDEF_NAME, METHODS_GETCLASSDEF_args);
		
		METHODS_FINDCLASS_args = createMethodsFindClass();
		METHODS_FINDCLASS_arg_class = METHODS_FINDCLASS_args.indexOf("Class");
		fNameDefMap.put(METHODS_FINDCLASS_NAME, METHODS_FINDCLASS_args);
		
		METHODS_NEW_args = createMethodsNew();
		METHODS_NEW_arg_class = METHODS_NEW_args.indexOf("Class");
		fNameDefMap.put(METHODS_NEW_NAME, METHODS_NEW_args);
		
		METHODS_AS_args = createMethodsAs();
		METHODS_AS_arg_class = METHODS_AS_args.indexOf("Class");
		fNameDefMap.put(METHODS_AS_NAME, METHODS_AS_args);
		
		METHODS_IS_args = createMethodsIs();
		METHODS_IS_arg_class = METHODS_IS_args.indexOf("Class");
		fNameDefMap.put(METHODS_IS_NAME, METHODS_IS_args);
		
		METHODS_SETMETHOD_args = createMethodsSetMethod();
		METHODS_SETMETHOD_arg_f = METHODS_SETMETHOD_args.indexOf("f");
		METHODS_SETMETHOD_arg_signature = METHODS_SETMETHOD_args.indexOf("signature");
		fNameDefMap.put(METHODS_SETMETHOD_NAME, METHODS_SETMETHOD_args);
		
		METHODS_REMOVEMETHOD_args = createMethodsRemoveMethod();
		METHODS_REMOVEMETHOD_arg_f = METHODS_REMOVEMETHOD_args.indexOf("f");
		METHODS_REMOVEMETHOD_arg_signature = METHODS_REMOVEMETHOD_args.indexOf("signature");
		fNameDefMap.put(METHODS_REMOVEMETHOD_NAME, METHODS_REMOVEMETHOD_args);
		
		METHODS_REMOVEMETHODS_args = createMethodsRemoveMethods();
		METHODS_REMOVEMETHODS_arg_f = METHODS_REMOVEMETHODS_args.indexOf("f");
		fNameDefMap.put(METHODS_REMOVEMETHODS_NAME, METHODS_REMOVEMETHODS_args);
		
		METHODS_EXISTSMETHOD_args = createMethodsExistsMethod();
		METHODS_EXISTSMETHOD_arg_f = METHODS_EXISTSMETHOD_args.indexOf("f");
		METHODS_EXISTSMETHOD_arg_signature = METHODS_EXISTSMETHOD_args.indexOf("signature");
		fNameDefMap.put(METHODS_EXISTSMETHOD_NAME, METHODS_EXISTSMETHOD_args);
		
		METHODS_HASMETHOD_args = createMethodsHasMethod();
		METHODS_HASMETHOD_arg_f = METHODS_HASMETHOD_args.indexOf("f");
		METHODS_HASMETHOD_arg_signature = METHODS_HASMETHOD_args.indexOf("signature");
		fNameDefMap.put(METHODS_HASMETHOD_NAME, METHODS_HASMETHOD_args);
		
		METHODS_GETMETHOD_args = createMethodsGetMethod();
		METHODS_GETMETHOD_arg_f = METHODS_GETMETHOD_args.indexOf("f");
		METHODS_GETMETHOD_arg_signature = METHODS_GETMETHOD_args.indexOf("signature");
		fNameDefMap.put(METHODS_GETMETHOD_NAME, METHODS_GETMETHOD_args);
		
		METHODS_SELECTMETHOD_args = createMethodsSelectMethod();
		METHODS_SELECTMETHOD_arg_f = METHODS_SELECTMETHOD_args.indexOf("f");
		METHODS_SELECTMETHOD_arg_signature = METHODS_SELECTMETHOD_args.indexOf("signature");
		fNameDefMap.put(METHODS_SELECTMETHOD_NAME, METHODS_SELECTMETHOD_args);
		
		METHODS_GETMETHODS_args = createMethodsGetMethods();
		METHODS_GETMETHODS_arg_f = METHODS_GETMETHODS_args.indexOf("f");
		fNameDefMap.put(METHODS_GETMETHODS_NAME, METHODS_GETMETHODS_args);
		
		METHODS_FINDMETHOD_args = createMethodsFindMethod();
		METHODS_FINDMETHOD_arg_f = METHODS_FINDMETHOD_args.indexOf("f");
		METHODS_FINDMETHOD_arg_signature = METHODS_FINDMETHOD_args.indexOf("signature");
		fNameDefMap.put(METHODS_FINDMETHOD_NAME, METHODS_FINDMETHOD_args);
		
		METHODS_DUMPMETHOD_args = createMethodsDumpMethod();
		METHODS_DUMPMETHOD_arg_f = METHODS_DUMPMETHOD_args.indexOf("f");
		fNameDefMap.put(METHODS_DUMPMETHOD_NAME, METHODS_DUMPMETHOD_args);
		
		METHODS_DUMPMETHODS_args = createMethodsDumpMethods();
		METHODS_DUMPMETHODS_arg_f = METHODS_DUMPMETHODS_args.indexOf("f");
		fNameDefMap.put(METHODS_DUMPMETHODS_NAME, METHODS_DUMPMETHODS_args);
		
		METHODS_SLOT_args = createMethodsSlot();
		METHODS_SLOT_arg_object = METHODS_SLOT_args.indexOf("object");
		METHODS_SLOT_arg_slot = METHODS_SLOT_args.indexOf("name");
		fNameDefMap.put(METHODS_SLOT_NAME, METHODS_SLOT_args);
	}
	
	
	ArgsDefinition createBaseAssign() {
		return new ArgsDefinition(BASE_ASSIGN_ID,
				"x", "value", "pos", "envir", "inherits", "immediate");
	}
	
	ArgsDefinition createBaseRemove() {
		return new ArgsDefinition(BASE_REMOVE_ID,
				"...", "list", "pos", "envir", "inherits");
	}
	
	ArgsDefinition createBaseExists() {
		return new ArgsDefinition(BASE_EXISTS_ID,
				"x", "where", "envir", "frame", "mode", "inherits");
	}
	
	ArgsDefinition createBaseGet() {
		return new ArgsDefinition(BASE_GET_ID,
				"x", "pos", "envir", "mode", "inherits");
	}
	
	ArgsDefinition createBaseSave() {
		return new ArgsDefinition(BASE_SAVE_ID,
				"...", "list", "ascii", "version", "envir", "compress", "eval.promises");
	}
	
	ArgsDefinition createBaseCall() {
		return new ArgsDefinition(BASE_CALL_ID,
				"name", "...");
	}
	
	ArgsDefinition createMethodsSetGeneric() {
		return new ArgsDefinition(METHODS_SETGENERIC_ID,
				"name", "def", "group", "valueClass", "where", "package" , 
				"signature" , "useAsDefault" , "genericFunction");
	}
	
	ArgsDefinition createMethodsSetGroupGeneric() {
		return new ArgsDefinition(METHODS_SETGROUPGENERIC_ID,
				"name", "def", "group", "valueClass", "knownMembers", 
				"where", "package");
	}
	
	ArgsDefinition createMethodsRemoveGeneric() {
		return new ArgsDefinition(METHODS_REMOVEGENERIC_ID,
				"f", "where");
	}
	
	ArgsDefinition createMethodsIsGeneric() {
		return new ArgsDefinition(METHODS_ISGENERIC_ID,
				"f", "where", "fdef", "getName");
	}
	
	ArgsDefinition createMethodsIsGroup() {
		return new ArgsDefinition(METHODS_ISGROUP_ID,
				"f", "where", "fdef");
	}
	
	ArgsDefinition createMethodsSignature() {
		return new ArgsDefinition(METHODS_SIGNATURE_ID,
				"...");
	}
	
	ArgsDefinition createMethodsSetClass() {
		return new ArgsBuilder(METHODS_SETCLASS_ID)
			.add("Class", CLASSNAME_STRING)
			.add("representation", "prototype", "contains", "validity", 
				"access", "where", "version", "sealed", "package").toDef();
	}
	
	ArgsDefinition createMethodsSetIs() {
		return new ArgsDefinition(METHODS_SETIS_ID,
				"class1", "class2", "test", "coerce", "replace", "by", "where", 
				"classDef", "extensionObject", "doComplete");
	}
	
	ArgsDefinition createMethodsRemoveClass() {
		return new ArgsDefinition(METHODS_REMOVECLASS_ID,
				"Class", "where");
	}
	
	ArgsDefinition createMethodsResetClass() {
		return new ArgsDefinition(METHODS_RESETCLASS_ID,
				"Class", "classDef", "where");
	}
	
	ArgsDefinition createMethodsIsClass() {
		return new ArgsDefinition(METHODS_ISCLASS_ID,
				"Class", "formal", "where");
	}
	
	ArgsDefinition createMethodsExtends() {
		return new ArgsDefinition(METHODS_EXTENDS_ID,
				"class1", "class2", "maybe", "fullInfo");
	}
	
	ArgsDefinition createMethodsGetClass() {
		return new ArgsDefinition(METHODS_GETCLASS_ID,
				"Class", ".Force", "where");
	}
	
	ArgsDefinition createMethodsGetClassDef() {
		return new ArgsDefinition(METHODS_GETCLASSDEF_ID,
				"Class", "where", "package");
	}
	
	ArgsDefinition createMethodsFindClass() {
		return new ArgsDefinition(METHODS_FINDCLASS_ID,
				"Class", "where", "unique");
	}
	
	ArgsDefinition createMethodsNew() {
		return new ArgsDefinition(METHODS_NEW_ID,
				"Class", "...");
	}
	
	ArgsDefinition createMethodsAs() {
		return new ArgsDefinition(METHODS_AS_ID,
				"object", "Class", "strict", "ext");
	}
	
	ArgsDefinition createMethodsIs() {
		return new ArgsDefinition(METHODS_IS_ID,
				"object", "class2");
	}
	
	ArgsDefinition createMethodsSetMethod() {
		return new ArgsDefinition(METHODS_SETMETHOD_ID,
				"f", "signature", "definition", "where", "valueClass", "sealed");
	}
	
	ArgsDefinition createMethodsRemoveMethod() {
		return new ArgsDefinition(METHODS_REMOVEMETHOD_ID,
				"f", "signature", "where");
	}
	
	ArgsDefinition createMethodsRemoveMethods() {
		return new ArgsDefinition(METHODS_REMOVEMETHOD_ID,
				"f", "where", "all");
	}
	
	ArgsDefinition createMethodsExistsMethod() {
		return new ArgsDefinition(METHODS_EXISTSMETHOD_ID,
				"f", "signature", "where");
	}
	
	ArgsDefinition createMethodsHasMethod() {
		return new ArgsDefinition(METHODS_HASMETHOD_ID,
				"f", "signature", "where");
	}
	
	ArgsDefinition createMethodsGetMethod() {
		return new ArgsDefinition(METHODS_GETMETHOD_ID,
				"f", "signature", "where", "optional", "mlist", "fdef");
	}
	
	ArgsDefinition createMethodsSelectMethod() {
		return new ArgsDefinition(METHODS_SELECTMETHOD_ID,
				"f", "signature", "optional", "useInherited", "mlist", "fdef", "verbose");
	}
	
	ArgsDefinition createMethodsGetMethods() {
		return new ArgsDefinition(METHODS_GETMETHODS_ID,
				"f", "where");
	}
	
	ArgsDefinition createMethodsFindMethod() {
		return new ArgsDefinition(METHODS_FINDMETHOD_ID,
				"f", "signature", "where");
	}
	
	ArgsDefinition createMethodsDumpMethod() {
		return new ArgsDefinition(METHODS_DUMPMETHOD_ID,
				"f", "signature", "file", "where", "def");
	}
	
	ArgsDefinition createMethodsDumpMethods() {
		return new ArgsDefinition(METHODS_DUMPMETHODS_ID,
				"f", "file", "signature", "methods", "where");
	}
	
	ArgsDefinition createMethodsSlot() {
		return new ArgsDefinition(METHODS_SLOT_ID,
				"object", "name", "check");
	}
	
	
	protected void checkUniqueId() {
		final int testId = new Random().nextInt();
		switch(testId) {
		case BASE_ASSIGN_ID:
		case BASE_REMOVE_ID:
		case BASE_EXISTS_ID:
		case BASE_GET_ID:
		case BASE_SAVE_ID:
		case BASE_CALL_ID:
		case METHODS_SETGENERIC_ID:
		case METHODS_REMOVEGENERIC_ID:
		case METHODS_ISGENERIC_ID:
		case METHODS_ISGROUP_ID:
		case METHODS_SETGROUPGENERIC_ID:
		case METHODS_SETCLASS_ID:
		case METHODS_SETIS_ID:
		case METHODS_REMOVECLASS_ID:
		case METHODS_RESETCLASS_ID:
		case METHODS_ISCLASS_ID:
		case METHODS_EXTENDS_ID:
		case METHODS_NEW_ID:
		case METHODS_AS_ID:
		case METHODS_IS_ID:
		case METHODS_GETCLASS_ID:
		case METHODS_GETCLASSDEF_ID:
		case METHODS_FINDCLASS_ID:
		case METHODS_SIGNATURE_ID:
		case METHODS_SETMETHOD_ID:
		case METHODS_REMOVEMETHOD_ID:
		case METHODS_REMOVEMETHODS_ID:
		case METHODS_EXISTSMETHOD_ID:
		case METHODS_HASMETHOD_ID:
		case METHODS_GETMETHOD_ID:
		case METHODS_SELECTMETHOD_ID:
		case METHODS_GETMETHODS_ID:
		case METHODS_FINDMETHOD_ID:
		case METHODS_DUMPMETHOD_ID:
		case METHODS_DUMPMETHODS_ID:
		case METHODS_SLOT_ID:
			System.out.print("Id matched: " + testId);
		}
	}
	
	public ArgsDefinition getArgs(final String name) {
		return fNameDefMap.get(name);
	}
	
	
}
