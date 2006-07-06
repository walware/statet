/*
 * $Header: /home/cvs/jakarta-commons/primitives/src/java/org/apache/commons/collections/primitives/LongCollections.java,v 1.1 2003/10/27 23:46:10 rwaldhoff Exp $
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
 * USE, DATA, OR PROFITS; OR BUSINESS LongERRUPTION) HOWEVER CAUSED AND
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

import org.apache.commons.collections.primitives.decorators.UnmodifiableLongIterator;
import org.apache.commons.collections.primitives.decorators.UnmodifiableLongList;
import org.apache.commons.collections.primitives.decorators.UnmodifiableLongListIterator;

/**
 * This class consists exclusively of static methods that operate on or
 * return LongCollections.
 * <p>
 * The methods of this class all throw a NullPoLongerException if the 
 * provided collection is null.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/10/27 23:46:10 $
 * 
 * @author Rodney Waldhoff 
 */
public final class LongCollections {

    /**
     * Returns an unmodifiable LongList containing only the specified element.
     * @param value the single value
     * @return an unmodifiable LongList containing only the specified element.
     */    
    public static LongList singletonLongList(long value) {
        // TODO: a specialized implementation of LongList may be more performant
        LongList list = new ArrayLongList(1);
        list.add(value);
        return UnmodifiableLongList.wrap(list);
    }

    /**
     * Returns an unmodifiable LongIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable LongIterator containing only the specified element.
     */    
    public static LongIterator singletonLongIterator(long value) {
        return singletonLongList(value).iterator();
    }

    /**
     * Returns an unmodifiable LongListIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable LongListIterator containing only the specified element.
     */    
    public static LongListIterator singletonLongListIterator(long value) {
        return singletonLongList(value).listIterator();
    }

    /**
     * Returns an unmodifiable version of the given non-null LongList.
     * @param list the non-null LongList to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null LongList
     * @throws NullPoLongerException if the given LongList is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableLongList#wrap
     */    
    public static LongList unmodifiableLongList(LongList list) throws NullPointerException {
        if(null == list) {
            throw new NullPointerException();
        }
        return UnmodifiableLongList.wrap(list);
    }
    
    /**
     * Returns an unmodifiable version of the given non-null LongIterator.
     * @param iter the non-null LongIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null LongIterator
     * @throws NullPoLongerException if the given LongIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableLongIterator#wrap
     */    
    public static LongIterator unmodifiableLongIterator(LongIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableLongIterator.wrap(iter);
    }
        
    /**
     * Returns an unmodifiable version of the given non-null LongListIterator.
     * @param iter the non-null LongListIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null LongListIterator
     * @throws NullPoLongerException if the given LongListIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableLongListIterator#wrap
     */    
    public static LongListIterator unmodifiableLongListIterator(LongListIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableLongListIterator.wrap(iter);
    }
    
    /**
     * Returns an unmodifiable, empty LongList.
     * @return an unmodifiable, empty LongList.
     * @see #EMPTY_LONG_LIST
     */    
    public static LongList getEmptyLongList() {
        return EMPTY_LONG_LIST;
    }
    
    /**
     * Returns an unmodifiable, empty LongIterator
     * @return an unmodifiable, empty LongIterator.
     * @see #EMPTY_LONG_ITERATOR
     */    
    public static LongIterator getEmptyLongIterator() {
        return EMPTY_LONG_ITERATOR;
    }
    
    /**
     * Returns an unmodifiable, empty LongListIterator
     * @return an unmodifiable, empty LongListIterator.
     * @see #EMPTY_LONG_LIST_ITERATOR
     */    
    public static LongListIterator getEmptyLongListIterator() {
        return EMPTY_LONG_LIST_ITERATOR;
    }    

    /**
     * An unmodifiable, empty LongList
     * @see #getEmptyLongList
     */    
    public static final LongList EMPTY_LONG_LIST = unmodifiableLongList(new ArrayLongList(0));

    /**
     * An unmodifiable, empty LongIterator
     * @see #getEmptyLongIterator
     */    
    public static final LongIterator EMPTY_LONG_ITERATOR = unmodifiableLongIterator(EMPTY_LONG_LIST.iterator());

    /**
     * An unmodifiable, empty LongListIterator
     * @see #getEmptyLongListIterator
     */    
    public static final LongListIterator EMPTY_LONG_LIST_ITERATOR = unmodifiableLongListIterator(EMPTY_LONG_LIST.listIterator());
}
