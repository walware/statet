/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.core.refactoring;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ltk.core.refactoring.CategorizedTextEditGroup;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;


/**
 * A utility class to provide compatibility with the old
 * text change API of adding text edits directly and auto
 * inserting them into the tree.
 */
public class TextChangeCompatibility {
	
	
	public static void addTextEdit(final TextChange change, final String name, final TextEdit edit) throws MalformedTreeException {
		addTextEdit(change, name, edit, true);
	}
	
	public static void addTextEdit(final TextChange change, final String name, final TextEdit edit, final boolean enable) throws MalformedTreeException {
		Assert.isNotNull(change);
		Assert.isNotNull(name);
		Assert.isNotNull(edit);
		TextEdit root= change.getEdit();
		if (root == null) {
			root= new MultiTextEdit();
			change.setEdit(root);
		}
		insert(root, edit);
		final TextEditChangeGroup group = new TextEditChangeGroup(change, new TextEditGroup(name, edit));
		group.setEnabled(enable);
		change.addTextEditChangeGroup(group);
	}
	
	public static void addTextEdit(final TextChange change, final String name, final TextEdit edit, final GroupCategorySet groupCategories) throws MalformedTreeException {
		Assert.isNotNull(change);
		Assert.isNotNull(name);
		Assert.isNotNull(edit);
		TextEdit root= change.getEdit();
		if (root == null) {
			root= new MultiTextEdit();
			change.setEdit(root);
		}
		insert(root, edit);
		change.addTextEditChangeGroup(new TextEditChangeGroup(
			change,
			new CategorizedTextEditGroup(name, edit, groupCategories)));
	}
	
	
	public static void insert(final TextEdit parent, final TextEdit edit) throws MalformedTreeException {
		if (!parent.hasChildren()) {
			parent.addChild(edit);
			return;
		}
		final TextEdit[] children= parent.getChildren();
		// First dive down to find the right parent.
		for (int i= 0; i < children.length; i++) {
			final TextEdit child= children[i];
			if (covers(child, edit)) {
				insert(child, edit);
				return;
			}
		}
		// We have the right parent. Now check if some of the children have to
		// be moved under the new edit since it is covering it.
		int removed= 0;
		for (int i= 0; i < children.length; i++) {
			final TextEdit child= children[i];
			if (covers(edit, child)) {
				parent.removeChild(i - removed++);
				edit.addChild(child);
			}
		}
		parent.addChild(edit);
	}
	
	
	private static boolean covers(final TextEdit thisEdit, final TextEdit otherEdit) {
		if (thisEdit.getLength() == 0)	// an insertion point can't cover anything
			return false;
		
		final int thisOffset= thisEdit.getOffset();
		final int thisEnd= thisEdit.getExclusiveEnd();
		if (otherEdit.getLength() == 0) {
			final int otherOffset= otherEdit.getOffset();
			return thisOffset < otherOffset && otherOffset < thisEnd;
		} else {
			final int otherOffset= otherEdit.getOffset();
			final int otherEnd= otherEdit.getExclusiveEnd();
			return thisOffset <= otherOffset && otherEnd <= thisEnd;
		}
	}
	
}
