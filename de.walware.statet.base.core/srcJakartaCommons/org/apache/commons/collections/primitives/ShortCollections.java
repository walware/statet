/*
 * $Header: /home/cvs/jakarta-commons/primitives/src/java/org/apache/commons/collections/primitives/ShortCollections.java,v 1.1 2003/10/29 18:57:15 rwaldhoff Exp $
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
 * USE, DATA, OR PROFITS; OR BUSINESS ShortERRUPTION) HOWEVER CAUSED AND
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

import org.apache.commons.collections.primitives.decorators.UnmodifiableShortIterator;
import org.apache.commons.collections.primitives.decorators.UnmodifiableShortList;
import org.apache.commons.collections.primitives.decorators.UnmodifiableShortListIterator;

/**
 * This class consists exclusively of static methods that operate on or
 * return ShortCollections.
 * <p>
 * The methods of this class all throw a NullPoShorterException if the 
 * provided collection is null.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/10/29 18:57:15 $
 * 
 * @author Rodney Waldhoff 
 */
public final class ShortCollections {

    /**
     * Returns an unmodifiable ShortList containing only the specified element.
     * @param value the single value
     * @return an unmodifiable ShortList containing only the specified element.
     */    
    public static ShortList singletonShortList(short value) {
        // TODO: a specialized implementation of ShortList may be more performant
        ShortList list = new ArrayShortList(1);
        list.add(value);
        return UnmodifiableShortList.wrap(list);
    }

    /**
     * Returns an unmodifiable ShortIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable ShortIterator containing only the specified element.
     */    
    public static ShortIterator singletonShortIterator(short value) {
        return singletonShortList(value).iterator();
    }

    /**
     * Returns an unmodifiable ShortListIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable ShortListIterator containing only the specified element.
     */    
    public static ShortListIterator singletonShortListIterator(short value) {
        return singletonShortList(value).listIterator();
    }

    /**
     * Returns an unmodifiable version of the given non-null ShortList.
     * @param list the non-null ShortList to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null ShortList
     * @throws NullPoShorterException if the given ShortList is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableShortList#wrap
     */    
    public static ShortList unmodifiableShortList(ShortList list) throws NullPointerException {
        if(null == list) {
            throw new NullPointerException();
        }
        return UnmodifiableShortList.wrap(list);
    }
    
    /**
     * Returns an unmodifiable version of the given non-null ShortIterator.
     * @param iter the non-null ShortIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null ShortIterator
     * @throws NullPoShorterException if the given ShortIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableShortIterator#wrap
     */    
    public static ShortIterator unmodifiableShortIterator(ShortIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableShortIterator.wrap(iter);
    }
        
    /**
     * Returns an unmodifiable version of the given non-null ShortListIterator.
     * @param iter the non-null ShortListIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null ShortListIterator
     * @throws NullPoShorterException if the given ShortListIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableShortListIterator#wrap
     */    
    public static ShortListIterator unmodifiableShortListIterator(ShortListIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableShortListIterator.wrap(iter);
    }
    
    /**
     * Returns an unmodifiable, empty ShortList.
     * @return an unmodifiable, empty ShortList.
     * @see #EMPTY_SHORT_LIST
     */    
    public static ShortList getEmptyShortList() {
        return EMPTY_SHORT_LIST;
    }
    
    /**
     * Returns an unmodifiable, empty ShortIterator
     * @return an unmodifiable, empty ShortIterator.
     * @see #EMPTY_SHORT_ITERATOR
     */    
    public static ShortIterator getEmptyShortIterator() {
        return EMPTY_SHORT_ITERATOR;
    }
    
    /**
     * Returns an unmodifiable, empty ShortListIterator
     * @return an unmodifiable, empty ShortListIterator.
     * @see #EMPTY_SHORT_LIST_ITERATOR
     */    
    public static ShortListIterator getEmptyShortListIterator() {
        return EMPTY_SHORT_LIST_ITERATOR;
    }    

    /**
     * An unmodifiable, empty ShortList
     * @see #getEmptyShortList
     */    
    public static final ShortList EMPTY_SHORT_LIST = unmodifiableShortList(new ArrayShortList(0));

    /**
     * An unmodifiable, empty ShortIterator
     * @see #getEmptyShortIterator
     */    
    public static final ShortIterator EMPTY_SHORT_ITERATOR = unmodifiableShortIterator(EMPTY_SHORT_LIST.iterator());

    /**
     * An unmodifiable, empty ShortListIterator
     * @see #getEmptyShortListIterator
     */    
    public static final ShortListIterator EMPTY_SHORT_LIST_ITERATOR = unmodifiableShortListIterator(EMPTY_SHORT_LIST.listIterator());
}
