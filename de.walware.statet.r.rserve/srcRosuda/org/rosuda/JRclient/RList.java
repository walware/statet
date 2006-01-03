package org.rosuda.JRclient;

import java.util.*;

/** implementation of R-lists<br>
    This is rather preliminary and may change in future since it's not really proper.
    The point is that the parser tries to interpret lists to be of the form entry=value,
    where entry is stored in the "head" part, and value is stored in the "body" part.
    Then using {@link #at(String)} it is possible to fetch "body" for a specific "head".
    The terminology used is partly from hash fields - "keys" are the elements in "head"
    and values are in "body" (see {@link #keys}).
    <p>
    On the other hand, R uses lists to store complex internal structures, which are not
    parsed according to the structure - in that case "head" and "body" have to be evaluated
    separately according to their meaning in that context.

    @version $Id: RList.java,v 1.4 2003/07/30 23:03:52 starsoft Exp $
*/
public class RList extends Object {
    /** xpressions containing head and body. 
	The terminology is a bit misleading - head corresponds to CAR, body to CDR and finally tag is TAG. */
    public REXP head, body, tag;
    /** usual assumption is that both head and body are xpressions containing vectors.
	In such case the actual content objects (Vectors) are cached for key/value access. */
    Vector h,b;

    /** constructs an empty list */
    public RList() { head=body=tag=null; }
    
    /** constructs an initialized list
	@param h head xpression
	@param b body xpression */
    public RList(REXP h, REXP b) { head=h; body=b; tag=null; }
    
    /** get head xpression (CAR)
	@return head xpression */
    public REXP getHead() { return head; }
    
    /** get body xpression (CDR)
	@return body xpression */
    public REXP getBody() { return body; }

    /** get tag xpression
	@return tag xpression */
    public REXP getTag() { return tag; }

    /** internal function that updates cached vectors
        @return <code>true</code> if both expressions are vectors and of the same length */
    boolean updateVec() {
	if (head==null||body==null||
	    head.Xt!=REXP.XT_VECTOR||body.Xt!=REXP.XT_VECTOR)
	    return false;
	h=(Vector)head.cont;
	b=(Vector)body.cont;
	return (h.size()==b.size());
    }

    /** get xpression given a key
	@param v key
	@return xpression which corresponds to the given key or
	        <code>null</code> if list is not standartized or key not found */
    public REXP at(String v) {
	if (!updateVec()) return null;
	for (int i=0;i<h.size();i++) {
	    REXP r=(REXP)h.elementAt(i);
	    if (r!=null && r.Xt==REXP.XT_STR && ((String)r.cont).compareTo(v)==0)
		return (REXP)b.elementAt(i);
	}
	return null;
    }

    /** get element at the specified position
	@param i index
	@return xpression at the index or <code>null</code> if list is not standartized or
	        if index out of bounds */
    public REXP at(int i) {
	if (!updateVec()) return null;
	return (i>=0 && i<b.size())?(REXP)b.elementAt(i):null;
    }

    /** returns all keys of the list
	@return array containing all keys or <code>null</code> if list is not standartized */
    public String[] keys() {
	if (!updateVec()) return null;
	String[] k=new String[h.size()];
	for(int i=0;i<h.size();i++) {
	    REXP r=(REXP)h.elementAt(i);
	    k[i]=(r==null||r.Xt!=REXP.XT_STR)?null:(String)r.cont;
	};
	return k;
    }
}
