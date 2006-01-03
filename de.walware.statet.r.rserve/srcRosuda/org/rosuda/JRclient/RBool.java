package org.rosuda.JRclient;

/** Implementation of tri-state logical data type in R.
    The three states are TRUE, FALSE and NA. To obtain truly boolean
    value, you'll need to use {@link #isTRUE} or {@link #isFALSE} since there is
    no canonical representation of RBool in boolean

    @version $Id: RBool.java,v 1.2 2003/07/30 23:03:52 starsoft Exp $
*/
public class RBool extends Object {
    int val;

    public RBool(boolean b) {
	val=(b)?1:0;
    };
    public RBool(RBool r) {
	val=r.val;
    };
    public RBool(int i) { /* 0=FALSE, 2=NA, anything else = TRUE */
	val=(i==0||i==2)?i:1;
    };

    public boolean isNA() { return (val==2); };
    public boolean isTRUE() { return (val==1); };
    public boolean isFALSE() { return (val==0); };

    public String toString() { return (val==0)?"FALSE":((val==2)?"NA":"TRUE"); };
}
