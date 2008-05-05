/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.codegeneration;

import org.eclipse.jface.text.templates.ContextTypeRegistry;

import de.walware.statet.ext.templates.StatextCodeTemplatesContextType;
import de.walware.statet.ext.templates.TemplatesMessages;


public class RCodeTemplatesContextType extends StatextCodeTemplatesContextType {
	
	
/* context types **************************************************************/
	public static final String NEW_RSCIRPTFILE_CONTEXTTYPE = "r_NewRScriptFile_context"; //$NON-NLS-1$
	
/* templates ******************************************************************/
	public static final String NEW_RSCRIPTFILE = "r_NewRScriptFile"; //$NON-NLS-1$
	
//	public static final String COMMENT_SUFFIX= "comment"; //$NON-NLS-1$
//
//	public static final String CATCHBLOCK_ID= CODETEMPLATES_PREFIX + "catchblock"; //$NON-NLS-1$
//	public static final String METHODSTUB_ID= CODETEMPLATES_PREFIX + "methodbody"; //$NON-NLS-1$
//	public static final String NEWTYPE_ID= CODETEMPLATES_PREFIX + "newtype"; //$NON-NLS-1$
//	public static final String CONSTRUCTORSTUB_ID= CODETEMPLATES_PREFIX + "constructorbody"; //$NON-NLS-1$
//	public static final String GETTERSTUB_ID= CODETEMPLATES_PREFIX + "getterbody"; //$NON-NLS-1$
//	public static final String SETTERSTUB_ID= CODETEMPLATES_PREFIX + "setterbody"; //$NON-NLS-1$
//	public static final String FILECOMMENT_ID= CODETEMPLATES_PREFIX + "file" + COMMENT_SUFFIX; //$NON-NLS-1$
//	public static final String TYPECOMMENT_ID= CODETEMPLATES_PREFIX + "type" + COMMENT_SUFFIX; //$NON-NLS-1$
//	public static final String FIELDCOMMENT_ID= CODETEMPLATES_PREFIX + "field" + COMMENT_SUFFIX; //$NON-NLS-1$
//	public static final String METHODCOMMENT_ID= CODETEMPLATES_PREFIX + "method" + COMMENT_SUFFIX; //$NON-NLS-1$
//	public static final String CONSTRUCTORCOMMENT_ID= CODETEMPLATES_PREFIX + "constructor" + COMMENT_SUFFIX; //$NON-NLS-1$
//	public static final String OVERRIDECOMMENT_ID= CODETEMPLATES_PREFIX + "override" + COMMENT_SUFFIX; //$NON-NLS-1$
//	public static final String GETTERCOMMENT_ID= CODETEMPLATES_PREFIX + "getter" + COMMENT_SUFFIX; //$NON-NLS-1$
//	public static final String SETTERCOMMENT_ID= CODETEMPLATES_PREFIX + "setter" + COMMENT_SUFFIX; //$NON-NLS-1$
	
//	public static final String EXCEPTION_TYPE= "exception_type"; //$NON-NLS-1$
//	public static final String EXCEPTION_VAR= "exception_var"; //$NON-NLS-1$
//	public static final String ENCLOSING_METHOD= "enclosing_method"; //$NON-NLS-1$
//	public static final String ENCLOSING_TYPE= "enclosing_type"; //$NON-NLS-1$
//	public static final String BODY_STATEMENT= "body_statement"; //$NON-NLS-1$
//	public static final String FIELD= "field"; //$NON-NLS-1$
//	public static final String FIELD_TYPE= "field_type"; //$NON-NLS-1$
//	public static final String BARE_FIELD_NAME= "bare_field_name"; //$NON-NLS-1$
//
//	public static final String PARAM= "param"; //$NON-NLS-1$
//	public static final String RETURN_TYPE= "return_type"; //$NON-NLS-1$
//	public static final String SEE_TAG= "see_to_overridden"; //$NON-NLS-1$
//
//	public static final String TAGS= "tags"; //$NON-NLS-1$
//
//	public static final String TYPENAME= "type_name"; //$NON-NLS-1$
//	public static final String FILENAME= "file_name"; //$NON-NLS-1$
//	public static final String PACKAGENAME= "package_name"; //$NON-NLS-1$
//	public static final String PROJECTNAME= "project_name"; //$NON-NLS-1$
//
//	public static final String PACKAGE_DECLARATION= "package_declaration"; //$NON-NLS-1$
//	public static final String TYPE_DECLARATION= "type_declaration"; //$NON-NLS-1$
//	public static final String TYPE_COMMENT= "typecomment"; //$NON-NLS-1$
//	public static final String FILE_COMMENT= "filecomment"; //$NON-NLS-1$
	
	public static void registerContextTypes(final ContextTypeRegistry registry) {
		registry.addContextType(new RCodeTemplatesContextType(NEW_RSCIRPTFILE_CONTEXTTYPE));
		
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.CATCHBLOCK_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.METHODBODY_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.CONSTRUCTORBODY_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.GETTERBODY_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.SETTERBODY_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.NEWTYPE_CONTEXTTYPE));
//
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.FILECOMMENT_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.TYPECOMMENT_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.FIELDCOMMENT_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.METHODCOMMENT_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.CONSTRUCTORCOMMENT_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.OVERRIDECOMMENT_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.GETTERCOMMENT_CONTEXTTYPE));
//		registry.addContextType(new RCodeTemplatesContextTypes(RCodeTemplatesContextTypes.SETTERCOMMENT_CONTEXTTYPE));
	}

//	/**
//	 * Resolver for javadoc tags.
//	 */
//	public static class TagsVariableResolver extends TemplateVariableResolver {
//		public TagsVariableResolver() {
//			super(TAGS,  JavaTemplateMessages.CodeTemplateContextType_variable_description_tags);
//		}
//
//		protected String resolve(TemplateContext context) {
//			return "@"; //$NON-NLS-1$
//		}
//	}
//
//	private boolean fIsComment;
	
	
	public RCodeTemplatesContextType(final String contextName) {
		
		super(contextName);
		
		if (NEW_RSCIRPTFILE_CONTEXTTYPE.equals(contextName)) {
			addRUnitVariables();
			addInitialSelectionResolver();
		}
		
//		if (CATCHBLOCK_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(EXCEPTION_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_exceptiontype));
//			addResolver(new CodeTemplateVariableResolver(EXCEPTION_VAR,  JavaTemplateMessages.CodeTemplateContextType_variable_description_exceptionvar));
//		} else if (METHODBODY_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingtype));
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_METHOD,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingmethod));
//			addResolver(new CodeTemplateVariableResolver(BODY_STATEMENT,  JavaTemplateMessages.CodeTemplateContextType_variable_description_bodystatement));
//		} else if (CONSTRUCTORBODY_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingtype));
//			addResolver(new CodeTemplateVariableResolver(BODY_STATEMENT,  JavaTemplateMessages.CodeTemplateContextType_variable_description_bodystatement));
//		} else if (GETTERBODY_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingtype));
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_METHOD,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingmethod));
//			addResolver(new CodeTemplateVariableResolver(FIELD, JavaTemplateMessages.CodeTemplateContextType_variable_description_getterfieldname));
//		} else if (SETTERBODY_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingtype));
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_METHOD,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingmethod));
//			addResolver(new CodeTemplateVariableResolver(FIELD, JavaTemplateMessages.CodeTemplateContextType_variable_description_getterfieldname));
//			addResolver(new CodeTemplateVariableResolver(PARAM, JavaTemplateMessages.CodeTemplateContextType_variable_description_param));
//		} else if (NEWTYPE_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(TYPENAME,  JavaTemplateMessages.CodeTemplateContextType_variable_description_typename));
//			addResolver(new CodeTemplateVariableResolver(PACKAGE_DECLARATION,  JavaTemplateMessages.CodeTemplateContextType_variable_description_packdeclaration));
//			addResolver(new CodeTemplateVariableResolver(TYPE_DECLARATION,  JavaTemplateMessages.CodeTemplateContextType_variable_description_typedeclaration));
//			addResolver(new CodeTemplateVariableResolver(TYPE_COMMENT,  JavaTemplateMessages.CodeTemplateContextType_variable_description_typecomment));
//			addResolver(new CodeTemplateVariableResolver(FILE_COMMENT,  JavaTemplateMessages.CodeTemplateContextType_variable_description_filecomment));
//			addCompilationUnitVariables();
//		} else if (TYPECOMMENT_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(TYPENAME,  JavaTemplateMessages.CodeTemplateContextType_variable_description_typename));
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingtype));
//			addResolver(new TagsVariableResolver());
//			addCompilationUnitVariables();
//			fIsComment= true;
//		} else if (FILECOMMENT_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(TYPENAME,  JavaTemplateMessages.CodeTemplateContextType_variable_description_typename));
//			addCompilationUnitVariables();
//			fIsComment= true;
//		} else if (FIELDCOMMENT_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(FIELD_TYPE, JavaTemplateMessages.CodeTemplateContextType_variable_description_fieldtype));
//			addResolver(new CodeTemplateVariableResolver(FIELD, JavaTemplateMessages.CodeTemplateContextType_variable_description_fieldname));
//			addCompilationUnitVariables();
//			fIsComment= true;
//		} else if (METHODCOMMENT_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingtype));
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_METHOD,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingmethod));
//			addResolver(new CodeTemplateVariableResolver(RETURN_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_returntype));
//			addResolver(new TagsVariableResolver());
//			addCompilationUnitVariables();
//			fIsComment= true;
//		} else if (OVERRIDECOMMENT_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingtype));
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_METHOD,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingmethod));
//			addResolver(new CodeTemplateVariableResolver(SEE_TAG,  JavaTemplateMessages.CodeTemplateContextType_variable_description_seetag));
//			addResolver(new TagsVariableResolver());
//			addCompilationUnitVariables();
//			fIsComment= true;
//		} else if (CONSTRUCTORCOMMENT_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingtype));
//			addResolver(new TagsVariableResolver());
//			addCompilationUnitVariables();
//			fIsComment= true;
//		} else if (GETTERCOMMENT_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingtype));
//			addResolver(new CodeTemplateVariableResolver(FIELD_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_getterfieldtype));
//			addResolver(new CodeTemplateVariableResolver(FIELD, JavaTemplateMessages.CodeTemplateContextType_variable_description_getterfieldname));
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_METHOD,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingmethod));
//			addResolver(new CodeTemplateVariableResolver(BARE_FIELD_NAME, JavaTemplateMessages.CodeTemplateContextType_variable_description_barefieldname));
//			addCompilationUnitVariables();
//			fIsComment= true;
//		} else if (SETTERCOMMENT_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingtype));
//			addResolver(new CodeTemplateVariableResolver(FIELD_TYPE,  JavaTemplateMessages.CodeTemplateContextType_variable_description_getterfieldtype));
//			addResolver(new CodeTemplateVariableResolver(FIELD, JavaTemplateMessages.CodeTemplateContextType_variable_description_getterfieldname));
//			addResolver(new CodeTemplateVariableResolver(ENCLOSING_METHOD,  JavaTemplateMessages.CodeTemplateContextType_variable_description_enclosingmethod));
//			addResolver(new CodeTemplateVariableResolver(PARAM, JavaTemplateMessages.CodeTemplateContextType_variable_description_param));
//			addResolver(new CodeTemplateVariableResolver(BARE_FIELD_NAME, JavaTemplateMessages.CodeTemplateContextType_variable_description_barefieldname));
//			addCompilationUnitVariables();
//			fIsComment= true;
//		}
	}
	
	private void addRUnitVariables() {
		addResolver(new CodeTemplatesVariableResolver(FILENAME, TemplatesMessages.Templates_Variable_File_description));
//		addResolver(new CodeTemplateVariableResolver(PACKAGENAME, JavaTemplateMessages.CodeTemplateContextType_variable_description_packagename));
//		addResolver(new CodeTemplateVariableResolver(PROJECTNAME, JavaTemplateMessages.CodeTemplateContextType_variable_description_projectname));
	}
	
//	/*
//	 * @see org.eclipse.jdt.internal.corext.template.ContextType#validateVariables(org.eclipse.jdt.internal.corext.template.TemplateVariable[])
//	 */
//	protected void validateVariables(TemplateVariable[] variables) throws TemplateException {
//		ArrayList required=  new ArrayList(5);
//		String contextName= getId();
//		if (NEWTYPE_CONTEXTTYPE.equals(contextName)) {
//			required.add(PACKAGE_DECLARATION);
//			required.add(TYPE_DECLARATION);
//		}
//		for (int i= 0; i < variables.length; i++) {
//			String type= variables[i].getType();
//			if (getResolver(type) == null) {
//				throw new TemplateException(Messages.format(JavaTemplateMessages.CodeTemplateContextType_validate_unknownvariable, type));
//			}
//			required.remove(type);
//		}
//		if (!required.isEmpty()) {
//			String missing= (String) required.get(0);
//			throw new TemplateException(Messages.format(JavaTemplateMessages.CodeTemplateContextType_validate_missingvariable, missing));
//		}
//		super.validateVariables(variables);
//	}
//
//
//
//	/* (non-Javadoc)
//	 * @see org.eclipse.jdt.internal.corext.template.ContextType#createContext()
//	 */
//	public TemplateContext createContext() {
//		return null;
//	}
	

//	/*
//	 * @see org.eclipse.jdt.internal.corext.template.ContextType#validate(java.lang.String)
//	 */
//	public void validate(String pattern) throws TemplateException {
//		super.validate(pattern);
//		if (fIsComment) {
//			if (!isValidComment(pattern)) {
//				throw new TemplateException(JavaTemplateMessages.CodeTemplateContextType_validate_invalidcomment);
//			}
//		}
//	}
//
//
//	private boolean isValidComment(String template) {
//		IScanner scanner= ToolFactory.createScanner(true, false, false, false);
//		scanner.setSource(template.toCharArray());
//		try {
//			int next= scanner.getNextToken();
//			while (next == ITerminalSymbols.TokenNameCOMMENT_LINE || next == ITerminalSymbols.TokenNameCOMMENT_JAVADOC || next == ITerminalSymbols.TokenNameCOMMENT_BLOCK) {
//				next= scanner.getNextToken();
//			}
//			return next == ITerminalSymbols.TokenNameEOF;
//		} catch (InvalidInputException e) {
//		}
//		return false;
//	}
	
}
