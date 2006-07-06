/*
 * $Header: /home/cvs/jakarta-commons/primitives/src/java/org/apache/commons/collections/primitives/ByteCollections.java,v 1.1 2003/10/29 18:33:11 rwaldhoff Exp $
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
 * USE, DATA, OR PROFITS; OR BUSINESS ByteERRUPTION) HOWEVER CAUSED AND
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

import org.apache.commons.collections.primitives.decorators.UnmodifiableByteIterator;
import org.apache.commons.collections.primitives.decorators.UnmodifiableByteList;
import org.apache.commons.collections.primitives.decorators.UnmodifiableByteListIterator;

/**
 * This class consists exclusively of static methods that operate on or
 * return ByteCollections.
 * <p>
 * The methods of this class all throw a NullPoByteerException if the 
 * provided collection is null.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/10/29 18:33:11 $
 * 
 * @author Rodney Waldhoff 
 */
public final class ByteCollections {

    /**
     * Returns an unmodifiable ByteList containing only the specified element.
     * @param value the single value
     * @return an unmodifiable ByteList containing only the specified element.
     */    
    public static ByteList singletonByteList(byte value) {
        // TODO: a specialized implementation of ByteList may be more performant
        ByteList list = new ArrayByteList(1);
        list.add(value);
        return UnmodifiableByteList.wrap(list);
    }

    /**
     * Returns an unmodifiable ByteIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable ByteIterator containing only the specified element.
     */    
    public static ByteIterator singletonByteIterator(byte value) {
        return singletonByteList(value).iterator();
    }

    /**
     * Returns an unmodifiable ByteListIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable ByteListIterator containing only the specified element.
     */    
    public static ByteListIterator singletonByteListIterator(byte value) {
        return singletonByteList(value).listIterator();
    }

    /**
     * Returns an unmodifiable version of the given non-null ByteList.
     * @param list the non-null ByteList to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null ByteList
     * @throws NullPoByteerException if the given ByteList is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableByteList#wrap
     */    
    public static ByteList unmodifiableByteList(ByteList list) throws NullPointerException {
        if(null == list) {
            throw new NullPointerException();
        }
        return UnmodifiableByteList.wrap(list);
    }
    
    /**
     * Returns an unmodifiable version of the given non-null ByteIterator.
     * @param iter the non-null ByteIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null ByteIterator
     * @throws NullPoByteerException if the given ByteIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableByteIterator#wrap
     */    
    public static ByteIterator unmodifiableByteIterator(ByteIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableByteIterator.wrap(iter);
    }
        
    /**
     * Returns an unmodifiable version of the given non-null ByteListIterator.
     * @param iter the non-null ByteListIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null ByteListIterator
     * @throws NullPoByteerException if the given ByteListIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableByteListIterator#wrap
     */    
    public static ByteListIterator unmodifiableByteListIterator(ByteListIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableByteListIterator.wrap(iter);
    }
    
    /**
     * Returns an unmodifiable, empty ByteList.
     * @return an unmodifiable, empty ByteList.
     * @see #EMPTY_BYTE_LIST
     */    
    public static ByteList getEmptyByteList() {
        return EMPTY_BYTE_LIST;
    }
    
    /**
     * Returns an unmodifiable, empty ByteIterator
     * @return an unmodifiable, empty ByteIterator.
     * @see #EMPTY_BYTE_ITERATOR
     */    
    public static ByteIterator getEmptyByteIterator() {
        return EMPTY_BYTE_ITERATOR;
    }
    
    /**
     * Returns an unmodifiable, empty ByteListIterator
     * @return an unmodifiable, empty ByteListIterator.
     * @see #EMPTY_BYTE_LIST_ITERATOR
     */    
    public static ByteListIterator getEmptyByteListIterator() {
        return EMPTY_BYTE_LIST_ITERATOR;
    }    

    /**
     * An unmodifiable, empty ByteList
     * @see #getEmptyByteList
     */    
    public static final ByteList EMPTY_BYTE_LIST = unmodifiableByteList(new ArrayByteList(0));

    /**
     * An unmodifiable, empty ByteIterator
     * @see #getEmptyByteIterator
     */    
    public static final ByteIterator EMPTY_BYTE_ITERATOR = unmodifiableByteIterator(EMPTY_BYTE_LIST.iterator());

    /**
     * An unmodifiable, empty ByteListIterator
     * @see #getEmptyByteListIterator
     */    
    public static final ByteListIterator EMPTY_BYTE_LIST_ITERATOR = unmodifiableByteListIterator(EMPTY_BYTE_LIST.listIterator());
}
