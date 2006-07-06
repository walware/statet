/*
 * $Header: /home/cvs/jakarta-commons/primitives/src/java/org/apache/commons/collections/primitives/DoubleCollections.java,v 1.1 2003/10/29 19:39:12 rwaldhoff Exp $
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
 * USE, DATA, OR PROFITS; OR BUSINESS DoubleERRUPTION) HOWEVER CAUSED AND
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

import org.apache.commons.collections.primitives.decorators.UnmodifiableDoubleIterator;
import org.apache.commons.collections.primitives.decorators.UnmodifiableDoubleList;
import org.apache.commons.collections.primitives.decorators.UnmodifiableDoubleListIterator;

/**
 * This class consists exclusively of static methods that operate on or
 * return DoubleCollections.
 * <p>
 * The methods of this class all throw a NullPoDoubleerException if the 
 * provided collection is null.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/10/29 19:39:12 $
 * 
 * @author Rodney Waldhoff 
 */
public final class DoubleCollections {

    /**
     * Returns an unmodifiable DoubleList containing only the specified element.
     * @param value the single value
     * @return an unmodifiable DoubleList containing only the specified element.
     */    
    public static DoubleList singletonDoubleList(double value) {
        // TODO: a specialized implementation of DoubleList may be more performant
        DoubleList list = new ArrayDoubleList(1);
        list.add(value);
        return UnmodifiableDoubleList.wrap(list);
    }

    /**
     * Returns an unmodifiable DoubleIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable DoubleIterator containing only the specified element.
     */    
    public static DoubleIterator singletonDoubleIterator(double value) {
        return singletonDoubleList(value).iterator();
    }

    /**
     * Returns an unmodifiable DoubleListIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable DoubleListIterator containing only the specified element.
     */    
    public static DoubleListIterator singletonDoubleListIterator(double value) {
        return singletonDoubleList(value).listIterator();
    }

    /**
     * Returns an unmodifiable version of the given non-null DoubleList.
     * @param list the non-null DoubleList to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null DoubleList
     * @throws NullPoDoubleerException if the given DoubleList is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableDoubleList#wrap
     */    
    public static DoubleList unmodifiableDoubleList(DoubleList list) throws NullPointerException {
        if(null == list) {
            throw new NullPointerException();
        }
        return UnmodifiableDoubleList.wrap(list);
    }
    
    /**
     * Returns an unmodifiable version of the given non-null DoubleIterator.
     * @param iter the non-null DoubleIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null DoubleIterator
     * @throws NullPoDoubleerException if the given DoubleIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableDoubleIterator#wrap
     */    
    public static DoubleIterator unmodifiableDoubleIterator(DoubleIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableDoubleIterator.wrap(iter);
    }
        
    /**
     * Returns an unmodifiable version of the given non-null DoubleListIterator.
     * @param iter the non-null DoubleListIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null DoubleListIterator
     * @throws NullPoDoubleerException if the given DoubleListIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableDoubleListIterator#wrap
     */    
    public static DoubleListIterator unmodifiableDoubleListIterator(DoubleListIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableDoubleListIterator.wrap(iter);
    }
    
    /**
     * Returns an unmodifiable, empty DoubleList.
     * @return an unmodifiable, empty DoubleList.
     * @see #EMPTY_DOUBLE_LIST
     */    
    public static DoubleList getEmptyDoubleList() {
        return EMPTY_DOUBLE_LIST;
    }
    
    /**
     * Returns an unmodifiable, empty DoubleIterator
     * @return an unmodifiable, empty DoubleIterator.
     * @see #EMPTY_DOUBLE_ITERATOR
     */    
    public static DoubleIterator getEmptyDoubleIterator() {
        return EMPTY_DOUBLE_ITERATOR;
    }
    
    /**
     * Returns an unmodifiable, empty DoubleListIterator
     * @return an unmodifiable, empty DoubleListIterator.
     * @see #EMPTY_DOUBLE_LIST_ITERATOR
     */    
    public static DoubleListIterator getEmptyDoubleListIterator() {
        return EMPTY_DOUBLE_LIST_ITERATOR;
    }    

    /**
     * An unmodifiable, empty DoubleList
     * @see #getEmptyDoubleList
     */    
    public static final DoubleList EMPTY_DOUBLE_LIST = unmodifiableDoubleList(new ArrayDoubleList(0));

    /**
     * An unmodifiable, empty DoubleIterator
     * @see #getEmptyDoubleIterator
     */    
    public static final DoubleIterator EMPTY_DOUBLE_ITERATOR = unmodifiableDoubleIterator(EMPTY_DOUBLE_LIST.iterator());

    /**
     * An unmodifiable, empty DoubleListIterator
     * @see #getEmptyDoubleListIterator
     */    
    public static final DoubleListIterator EMPTY_DOUBLE_LIST_ITERATOR = unmodifiableDoubleListIterator(EMPTY_DOUBLE_LIST.listIterator());
}
