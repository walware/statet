/*
 * $Header: /home/cvs/jakarta-commons/primitives/src/java/org/apache/commons/collections/primitives/decorators/BaseUnmodifiableShortList.java,v 1.1 2003/10/29 18:57:15 rwaldhoff Exp $
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
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
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

package org.apache.commons.collections.primitives.decorators;

import org.apache.commons.collections.primitives.ShortCollection;
import org.apache.commons.collections.primitives.ShortIterator;
import org.apache.commons.collections.primitives.ShortList;
import org.apache.commons.collections.primitives.ShortListIterator;

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.1 $ $Date: 2003/10/29 18:57:15 $
 * 
 * @author Rodney Waldhoff 
 */
abstract class BaseUnmodifiableShortList extends BaseProxyShortList {

    public final void add(int index, short element) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean addAll(int index, ShortCollection collection) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final short removeElementAt(int index) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final short set(int index, short element) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean add(short element) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean addAll(ShortCollection c) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final void clear() {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean removeAll(ShortCollection c) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean removeElement(short element) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean retainAll(ShortCollection c) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }    
    
    public final ShortList subList(int fromIndex, int toIndex) {
        return UnmodifiableShortList.wrap(getProxiedList().subList(fromIndex,toIndex));
    }

    public final ShortIterator iterator() {
        return UnmodifiableShortIterator.wrap(getProxiedList().iterator());
    }
    
    public ShortListIterator listIterator() {
        return UnmodifiableShortListIterator.wrap(getProxiedList().listIterator());
    }

    public ShortListIterator listIterator(int index) {
        return UnmodifiableShortListIterator.wrap(getProxiedList().listIterator(index));
    }

}
