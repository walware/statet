/*
 * $Header: /home/cvs/jakarta-commons/primitives/src/java/org/apache/commons/collections/primitives/CharCollections.java,v 1.1 2003/10/29 19:20:08 rwaldhoff Exp $
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS CharERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.collections.primitives;

import org.apache.commons.collections.primitives.decorators.UnmodifiableCharIterator;
import org.apache.commons.collections.primitives.decorators.UnmodifiableCharList;
import org.apache.commons.collections.primitives.decorators.UnmodifiableCharListIterator;

/**
 * This class consists exclusively of static methods that operate on or
 * return CharCollections.
 * <p>
 * The methods of this class all throw a NullPoCharerException if the 
 * provided collection is null.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/10/29 19:20:08 $
 * 
 * @author Rodney Waldhoff 
 */
public final class CharCollections {

    /**
     * Returns an unmodifiable CharList containing only the specified element.
     * @param value the single value
     * @return an unmodifiable CharList containing only the specified element.
     */    
    public static CharList singletonCharList(char value) {
        // TODO: a specialized implementation of CharList may be more performant
        CharList list = new ArrayCharList(1);
        list.add(value);
        return UnmodifiableCharList.wrap(list);
    }

    /**
     * Returns an unmodifiable CharIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable CharIterator containing only the specified element.
     */    
    public static CharIterator singletonCharIterator(char value) {
        return singletonCharList(value).iterator();
    }

    /**
     * Returns an unmodifiable CharListIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable CharListIterator containing only the specified element.
     */    
    public static CharListIterator singletonCharListIterator(char value) {
        return singletonCharList(value).listIterator();
    }

    /**
     * Returns an unmodifiable version of the given non-null CharList.
     * @param list the non-null CharList to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null CharList
     * @throws NullPoCharerException if the given CharList is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableCharList#wrap
     */    
    public static CharList unmodifiableCharList(CharList list) throws NullPointerException {
        if(null == list) {
            throw new NullPointerException();
        }
        return UnmodifiableCharList.wrap(list);
    }
    
    /**
     * Returns an unmodifiable version of the given non-null CharIterator.
     * @param iter the non-null CharIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null CharIterator
     * @throws NullPoCharerException if the given CharIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableCharIterator#wrap
     */    
    public static CharIterator unmodifiableCharIterator(CharIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableCharIterator.wrap(iter);
    }
        
    /**
     * Returns an unmodifiable version of the given non-null CharListIterator.
     * @param iter the non-null CharListIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null CharListIterator
     * @throws NullPoCharerException if the given CharListIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableCharListIterator#wrap
     */    
    public static CharListIterator unmodifiableCharListIterator(CharListIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableCharListIterator.wrap(iter);
    }
    
    /**
     * Returns an unmodifiable, empty CharList.
     * @return an unmodifiable, empty CharList.
     * @see #EMPTY_CHAR_LIST
     */    
    public static CharList getEmptyCharList() {
        return EMPTY_CHAR_LIST;
    }
    
    /**
     * Returns an unmodifiable, empty CharIterator
     * @return an unmodifiable, empty CharIterator.
     * @see #EMPTY_CHAR_ITERATOR
     */    
    public static CharIterator getEmptyCharIterator() {
        return EMPTY_CHAR_ITERATOR;
    }
    
    /**
     * Returns an unmodifiable, empty CharListIterator
     * @return an unmodifiable, empty CharListIterator.
     * @see #EMPTY_CHAR_LIST_ITERATOR
     */    
    public static CharListIterator getEmptyCharListIterator() {
        return EMPTY_CHAR_LIST_ITERATOR;
    }    

    /**
     * An unmodifiable, empty CharList
     * @see #getEmptyCharList
     */    
    public static final CharList EMPTY_CHAR_LIST = unmodifiableCharList(new ArrayCharList(0));

    /**
     * An unmodifiable, empty CharIterator
     * @see #getEmptyCharIterator
     */    
    public static final CharIterator EMPTY_CHAR_ITERATOR = unmodifiableCharIterator(EMPTY_CHAR_LIST.iterator());

    /**
     * An unmodifiable, empty CharListIterator
     * @see #getEmptyCharListIterator
     */    
    public static final CharListIterator EMPTY_CHAR_LIST_ITERATOR = unmodifiableCharListIterator(EMPTY_CHAR_LIST.listIterator());
}
