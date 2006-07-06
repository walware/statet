/*
 * $Header: /home/cvs/jakarta-commons/primitives/src/java/org/apache/commons/collections/primitives/FloatCollections.java,v 1.1 2003/10/29 20:07:54 rwaldhoff Exp $
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
 * USE, DATA, OR PROFITS; OR BUSINESS FloatERRUPTION) HOWEVER CAUSED AND
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

import org.apache.commons.collections.primitives.decorators.UnmodifiableFloatIterator;
import org.apache.commons.collections.primitives.decorators.UnmodifiableFloatList;
import org.apache.commons.collections.primitives.decorators.UnmodifiableFloatListIterator;

/**
 * This class consists exclusively of static methods that operate on or
 * return FloatCollections.
 * <p>
 * The methods of this class all throw a NullPoFloaterException if the 
 * provided collection is null.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/10/29 20:07:54 $
 * 
 * @author Rodney Waldhoff 
 */
public final class FloatCollections {

    /**
     * Returns an unmodifiable FloatList containing only the specified element.
     * @param value the single value
     * @return an unmodifiable FloatList containing only the specified element.
     */    
    public static FloatList singletonFloatList(float value) {
        // TODO: a specialized implementation of FloatList may be more performant
        FloatList list = new ArrayFloatList(1);
        list.add(value);
        return UnmodifiableFloatList.wrap(list);
    }

    /**
     * Returns an unmodifiable FloatIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable FloatIterator containing only the specified element.
     */    
    public static FloatIterator singletonFloatIterator(float value) {
        return singletonFloatList(value).iterator();
    }

    /**
     * Returns an unmodifiable FloatListIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable FloatListIterator containing only the specified element.
     */    
    public static FloatListIterator singletonFloatListIterator(float value) {
        return singletonFloatList(value).listIterator();
    }

    /**
     * Returns an unmodifiable version of the given non-null FloatList.
     * @param list the non-null FloatList to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null FloatList
     * @throws NullPoFloaterException if the given FloatList is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableFloatList#wrap
     */    
    public static FloatList unmodifiableFloatList(FloatList list) throws NullPointerException {
        if(null == list) {
            throw new NullPointerException();
        }
        return UnmodifiableFloatList.wrap(list);
    }
    
    /**
     * Returns an unmodifiable version of the given non-null FloatIterator.
     * @param iter the non-null FloatIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null FloatIterator
     * @throws NullPoFloaterException if the given FloatIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableFloatIterator#wrap
     */    
    public static FloatIterator unmodifiableFloatIterator(FloatIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableFloatIterator.wrap(iter);
    }
        
    /**
     * Returns an unmodifiable version of the given non-null FloatListIterator.
     * @param iter the non-null FloatListIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null FloatListIterator
     * @throws NullPoFloaterException if the given FloatListIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableFloatListIterator#wrap
     */    
    public static FloatListIterator unmodifiableFloatListIterator(FloatListIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableFloatListIterator.wrap(iter);
    }
    
    /**
     * Returns an unmodifiable, empty FloatList.
     * @return an unmodifiable, empty FloatList.
     * @see #EMPTY_FLOAT_LIST
     */    
    public static FloatList getEmptyFloatList() {
        return EMPTY_FLOAT_LIST;
    }
    
    /**
     * Returns an unmodifiable, empty FloatIterator
     * @return an unmodifiable, empty FloatIterator.
     * @see #EMPTY_FLOAT_ITERATOR
     */    
    public static FloatIterator getEmptyFloatIterator() {
        return EMPTY_FLOAT_ITERATOR;
    }
    
    /**
     * Returns an unmodifiable, empty FloatListIterator
     * @return an unmodifiable, empty FloatListIterator.
     * @see #EMPTY_FLOAT_LIST_ITERATOR
     */    
    public static FloatListIterator getEmptyFloatListIterator() {
        return EMPTY_FLOAT_LIST_ITERATOR;
    }    

    /**
     * An unmodifiable, empty FloatList
     * @see #getEmptyFloatList
     */    
    public static final FloatList EMPTY_FLOAT_LIST = unmodifiableFloatList(new ArrayFloatList(0));

    /**
     * An unmodifiable, empty FloatIterator
     * @see #getEmptyFloatIterator
     */    
    public static final FloatIterator EMPTY_FLOAT_ITERATOR = unmodifiableFloatIterator(EMPTY_FLOAT_LIST.iterator());

    /**
     * An unmodifiable, empty FloatListIterator
     * @see #getEmptyFloatListIterator
     */    
    public static final FloatListIterator EMPTY_FLOAT_LIST_ITERATOR = unmodifiableFloatListIterator(EMPTY_FLOAT_LIST.listIterator());
}
