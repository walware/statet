package org.rosuda.JRclient;

import java.util.*;

/** representation of R-eXpressions in Java

    @version $Id: REXP.java,v 1.17 2004/08/01 20:31:52 urbaneks Exp $
*/
public class REXP extends Object {
    /** xpression type: NULL */
    public static final int XT_NULL=0;
    /** xpression type: integer */
    public static final int XT_INT=1;
    /** xpression type: double */
    public static final int XT_DOUBLE=2;
    /** xpression type: String */
    public static final int XT_STR=3;
    /** xpression type: language construct (currently content is same as list) */
    public static final int XT_LANG=4;
    /** xpression type: symbol (content is symbol name: String) */
    public static final int XT_SYM=5;
    /** xpression type: RBool */    
    public static final int XT_BOOL=6;
    /** xpression type: Vector */
    public static final int XT_VECTOR=16;
    /** xpression type: RList */
    public static final int XT_LIST=17;
    /** xpression type: closure (there is no java class for that type (yet?). currently the body of the closure is stored in the content part of the REXP. Please note that this may change in the future!) */
    public static final int XT_CLOS=18;
    /** xpression type: int[] */
    public static final int XT_ARRAY_INT=32;
    /** xpression type: double[] */
    public static final int XT_ARRAY_DOUBLE=33;
    /** xpression type: String[] (currently not used, Vector is used instead) */
    public static final int XT_ARRAY_STR=34;
    /** internal use only! this constant should never appear in a REXP */
    public static final int XT_ARRAY_BOOL_UA=35;
    /** xpression type: RBool[] */
    public static final int XT_ARRAY_BOOL=36;
    /** xpression type: unknown; no assumptions can be made about the content */
    public static final int XT_UNKNOWN=48;

    /** xpression type: RFactor; this XT is internally generated (ergo is does not come from Rsrv.h) to support RFactor class which is built from XT_ARRAY_INT */
    public static final int XT_FACTOR=127; 

    /** xpression type */
    int Xt;
    /** attribute xpression or <code>null</code> if none */
    REXP attr;
    /** content of the xpression - its object type is dependent of {@link #Xt} */
    Object cont;

    /** cached binary length; valid only if positive */
    long cachedBinaryLength=-1;
    
    /** construct a new, empty (NULL) expression w/o attribute */
    public REXP() { Xt=0; attr=null; cont=null; }

    /** construct a new xpression of type t and content o, but no attribute
	@param t xpression type (XT_...)
	@param o content */
    public REXP(int t, Object o) {
	Xt=t; cont=o; attr=null;
    }

    /** construct a new xpression of type t, content o and attribute at
	@param t xpression type
	@param o content
	@param at attribute */
    public REXP(int t, Object o, REXP at) {
	Xt=t; cont=o; attr=at;
    }

    /** construct a new xpression of type XT_ARRAY_DOUBLE and content val
        @param val array of doubles to store in the REXP */
    public REXP(double[] val) {
        this(XT_ARRAY_DOUBLE,val);
    }

    /** construct a new xpression of type XT_ARRAY_INT and content val
        @param val array of integers to store in the REXP */
    public REXP(int[] val) {
        this(XT_ARRAY_INT,val);
    }
    
    /** construct a new xpression of type XT_ARRAY_INT and content val
      @param val array of integers to store in the REXP */
    public REXP(String[] val) {
      this(XT_ARRAY_STR,val);
    }
    
    /** get attribute of the REXP. In R every object can have attached attribute xpression. Some more complex structures such as classes are built that way.
        @return attribute xpression or <code>null</code> if there is none associated */
    public REXP getAttribute() {
        return attr;
    }

    /** get raw content. Use as... methods to retrieve contents of known type.
        @return content of the REXP */
    public Object getContent() {
        return cont;
    }

    /** get xpression type (see XT_.. constants) of the content. It defines the type of the content object.
        @return xpression type */
    public int getType() {
        return Xt;
    }
    
    /** parses byte buffer for binary representation of xpressions - read one xpression slot (descends recursively for aggregated xpressions such as lists, vectors etc.)
	@param x xpression object to store the parsed xpression in
	@param buf buffer containing the binary representation
	@param o offset in the buffer to start at
        @return position just behind the parsed xpression. Can be use for successive calls to {@link #parseREXP} if more than one expression is stored in the binary array. */
    public static int parseREXP(REXP x, byte[] buf, int o) {
	int xl=Rtalk.getLen(buf,o);
	boolean hasAtt=((buf[o]&128)!=0);
        boolean isLong=((buf[o]&64)!=0);
	int xt=(int)(buf[o]&63);
        //System.out.println("parseREXP: type="+xt+", len="+xl+", hasAtt="+hasAtt+", isLong="+isLong);
        if (isLong) o+=4;
        o+=4;
	int eox=o+xl;
	
	x.Xt=xt; x.attr=null;
	if (hasAtt) o=parseREXP(x.attr=new REXP(),buf,o);
	if (xt==XT_NULL) {
	    x.cont=null; return o;
	};
	if (xt==XT_DOUBLE) {
	    long lr=Rtalk.getLong(buf,o);
	    x.cont=new Double(Double.longBitsToDouble(lr));
	    o+=8;
	    if (o!=eox) {
		System.out.println("Warning: double SEXP size mismatch\n");
		o=eox;
	    };
	    return o;
	}
	if (xt==XT_ARRAY_DOUBLE) {
	    int as=(eox-o)/8,i=0;
	    double[] d=new double[as];
	    while (o<eox) {
		d[i]=Double.longBitsToDouble(Rtalk.getLong(buf,o));
		o+=8;
		i++;
	    };
	    if (o!=eox) {
		System.out.println("Warning: double array SEXP size mismatch\n");
		o=eox;
	    };
	    x.cont=d;
	    return o;
	};
	if (xt==XT_BOOL) {
	    x.cont=new RBool(buf[o]); o++;
	    if (o!=eox) {
                if (eox!=o+3) // o+3 could happen if the result was aligned (1 byte data + 3 bytes padding)
                    System.out.println("Warning: bool SEXP size mismatch\n");
		o=eox;
	    };
	    return o;
	};
	if (xt==XT_ARRAY_BOOL_UA) {
	    int as=(eox-o), i=0;
            x.Xt=XT_ARRAY_BOOL; // XT_ARRAY_BOOL_UA is only old transport type for XT_ARRAY_BOOL
	    RBool[] d=new RBool[as];
	    while(o<eox) {
		d[i]=new RBool(buf[o]);
		i++; o++;
	    };
	    x.cont=d;
	    return o;
	};
        if (xt==XT_ARRAY_BOOL) {
            int as=Rtalk.getInt(buf,o);
            o+=4;
            int i=0;
            RBool[] d=new RBool[as];
            while(o<eox && i<as) {
                d[i]=new RBool(buf[o]);
                i++; o++;
            }
	    // skip the padding
	    while ((i&3)!=0) { i++; o++; };
            x.cont=d;
            return o;
        };
        if (xt==XT_INT) {
	    x.cont=new Integer(Rtalk.getInt(buf,o));
	    o+=4;
	    if (o!=eox) {
		System.out.println("Warning: int SEXP size mismatch\n");
		o=eox;
	    };
	    return o;
	}
	if (xt==XT_ARRAY_INT) {
	    int as=(eox-o)/4,i=0;
	    int[] d=new int[as];
	    while (o<eox) {
		d[i]=Rtalk.getInt(buf,o);
		o+=4;
		i++;
	    };
	    if (o!=eox) {
		System.out.println("Warning: int array SEXP size mismatch\n");
		o=eox;
	    };
	    x.cont=d;
	    // hack for lists - special lists attached to int are factors
	    if (x.attr!=null && x.attr.Xt==XT_LIST && x.attr.cont!=null &&
		((RList)x.attr.cont).head!=null &&
		((RList)x.attr.cont).body!=null &&
		((RList)x.attr.cont).head.cont!=null &&
		((RList)x.attr.cont).body.cont!=null &&
		((RList)x.attr.cont).head.Xt==XT_VECTOR &&
		((RList)x.attr.cont).body.Xt==XT_LIST &&
		((RList)((RList)x.attr.cont).body.cont).head!=null &&
		((RList)((RList)x.attr.cont).body.cont).head.Xt==XT_STR &&
		((String)((RList)((RList)x.attr.cont).body.cont).head.cont).compareTo("factor")==0) {
		RFactor f=new RFactor(d,(Vector)((RList)x.attr.cont).head.cont);
		x.cont=f;
		x.Xt=XT_FACTOR;
		x.attr=null;
	    };
	    return o;
	};
	if (xt==XT_VECTOR) {
	    Vector v=new Vector();
	    while(o<eox) {
		REXP xx=new REXP();
		o=parseREXP(xx,buf,o);
		v.addElement(xx);
	    };
	    if (o!=eox) {
		System.out.println("Warning: int vector SEXP size mismatch\n");
		o=eox;
	    };
	    x.cont=v;
	    // fixup for lists since they're stored as attributes of vectors
	    if (x.attr!=null && x.attr.Xt==XT_LIST && x.attr.cont!=null) {
		RList l=new RList();
		l.head=((RList)x.attr.cont).head;
		l.body=new REXP(XT_VECTOR,v);
		x.cont=l;
		x.Xt=XT_LIST; x.attr=x.attr.attr;
		// one more hack: we're de-vectorizing strings if alone
		// so we should invert that in case of list heads
		if (l.head.Xt==XT_STR) {
		    Vector sv=new Vector();
		    sv.addElement(l.head);
		    l.head=new REXP(XT_VECTOR,sv,l.head.attr);
		    l.head.attr=null;
		};
	    };
	    return o;
	};
	if (xt==XT_STR) {
	    int i=o;
	    while (buf[i]!=0 && i<eox) i++;
	    try {
		x.cont=new String(buf,o,i-o,Rconnection.transferCharset);
	    } catch(Exception e) {
		System.out.println("unable to convert string\n");
		x.cont=null;
	    };
	    o=eox;
	    return o;
	};
	if (xt==XT_LIST || xt==XT_LANG) {
	    RList rl=new RList();
	    rl.head=new REXP();
	    rl.body=new REXP();
	    rl.tag=null;
	    o=parseREXP(rl.head,buf,o); // CAR
	    o=parseREXP(rl.body,buf,o); // CDR
	    if (o!=eox) {
		// if there is more data then it's presumably the TAG entry
		rl.tag=new REXP();
		o=parseREXP(rl.tag,buf,o);
		if (o!=eox) {
		    System.out.println("Warning: list SEXP size mismatch\n");
		    o=eox;
		}
	    };
	    x.cont=rl;
	    return o;
	};

	if (xt==XT_SYM) {
	    REXP sym=new REXP();
	    o=parseREXP(sym,buf,o); // PRINTNAME that's all we will use
	    String s=null;
	    if (sym.Xt==XT_STR) s=(String)sym.cont; else s=sym.toString();
	    x.cont=s; // content of a symbol is its printname string (so far)
	    o=eox;
	    return o;
	}

	if (xt==XT_CLOS) {
	    REXP form=new REXP();
	    REXP body=new REXP();
	    o=parseREXP(form,buf,o);
	    o=parseREXP(body,buf,o);
	    if (o!=eox) {
		System.out.println("Warning: closure SEXP size mismatch\n");
		o=eox;
	    }
            /* curently closures are not coded into their own objects, basically due to lack of demand. */
	    x.cont=body;
	    return o;
	}

	if (xt==XT_UNKNOWN) {
	    x.cont=new Integer(Rtalk.getInt(buf,o));
	    o=eox;
	    return o;
	}

	x.cont=null;
	o=eox;
	System.out.println("unhandled type: "+xt);
	return o;
    }

    /** Calculates the length of the binary representation of the REXP including all headers. This is the amount of memory necessary to store the REXP via {@link #getBinaryRepresentation}.
        <p>Please note that currently only XT_[ARRAY_]INT, XT_[ARRAY_]DOUBLE and XT_[ARRAY_]STR are supported! All other types will return 4 which is the size of the header.
        @return length of the REXP including headers (4 or 8 bytes)*/
    public int getBinaryLength() {
	int l=0;
	switch (Xt) {
	case XT_INT: l=4; break;
	case XT_DOUBLE: l=8; break;
	case XT_STR: l=(cont==null)?1:((String)cont).length()+1; break;
	case XT_ARRAY_INT: l=(cont==null)?0:((int[])cont).length*4; break;
	case XT_ARRAY_DOUBLE: l=(cont==null)?0:((double[])cont).length*8; break;
	case XT_ARRAY_STR:
	  if (cachedBinaryLength<0) { // if there's no cache, we have to count..
	    if (cont==null) cachedBinaryLength=4; else {
	      String sa[]=(String[])cont;
	      int i=0, io=0;
	      while (i<sa.length) {
		if (sa[i]!=null) {
		  try {
		    byte b[]=sa[i].getBytes(Rconnection.transferCharset);
		    io+=b.length;
		    b=null;
		  } catch (java.io.UnsupportedEncodingException uex) {
		    // FIXME: we should so something ... so far we hope noone's gonna mess with the encoding
		  }
		}
		io++;
		i++;
	      }
	      while ((io&3)!=0) io++;
	      cachedBinaryLength=io+4;
	      if (cachedBinaryLength>0xfffff0)
		cachedBinaryLength+=4;
	    }
	  }
	  return (int)cachedBinaryLength;
	} // switch
        if (l>0xfffff0) l+=4; // large data need 4 more bytes
	return l+4;
    }

    /** Stores the REXP in its binary (ready-to-send) representation including header into a buffer and returns the index of the byte behind the REXP.
        <p>Please note that currently only XT_[ARRAY_]INT, XT_[ARRAY_]DOUBLE and XT_[ARRAY_]STR are supported! All other types will be stored as SEXP of the length 0 without any contents.
        @param buf buffer to store the REXP binary into
        @param off offset of the first byte where to store the REXP
        @return the offset of the first byte behind the stored REXP */
    public int getBinaryRepresentation(byte[] buf, int off) {
	int myl=getBinaryLength();
        boolean isLarge=(myl>0xfffff0);
        Rtalk.setHdr(Xt,myl-(isLarge?8:4),buf,off);
        off+=(isLarge?8:4);
	switch (Xt) {
	case XT_INT: Rtalk.setInt(asInt(),buf,off); break;
	case XT_DOUBLE: Rtalk.setLong(Double.doubleToLongBits(asDouble()),buf,off); break;
	case XT_ARRAY_INT:
	    if (cont!=null) {
		int ia[]=(int[])cont;
		int i=0, io=off;
		while(i<ia.length) {
		    Rtalk.setInt(ia[i++],buf,io); io+=4;
		}
	    }
	    break;
	case XT_ARRAY_DOUBLE:
	    if (cont!=null) {
		double da[]=(double[])cont;
		int i=0, io=off;
		while(i<da.length) {
		    Rtalk.setLong(Double.doubleToLongBits(da[i++]),buf,io); io+=8;
		}
	    }
	    break;
	case XT_ARRAY_STR:
	  if (cont!=null) {
	    String sa[]=(String[])cont;
	    int i=0, io=off;
	    while (i<sa.length) {
	      if (sa[i]!=null) {
		try {
		  byte b[]=sa[i].getBytes(Rconnection.transferCharset);
		  System.arraycopy(b,0,buf,io,b.length);
		  io+=b.length;
		  b=null;
		} catch (java.io.UnsupportedEncodingException uex) {
		  // FIXME: we should so something ... so far we hope noone's gonna mess with the encoding
		}
	      }
	      buf[io++]=0;
	      i++;
	    }
	    i=io-off;
	    while ((i&3)!=0) { buf[io++]=1; i++; } // padding if necessary..
	  }
	}
	return off+myl;
    }

    /** returns human-readable name of the xpression type as string. Arrays are denoted by a trailing asterisk (*).
	@param xt xpression type
	@return name of the xpression type */
    public static String xtName(int xt) {
	if (xt==XT_NULL) return "NULL";
	if (xt==XT_INT) return "INT";
	if (xt==XT_STR) return "STRING";
	if (xt==XT_DOUBLE) return "REAL";
	if (xt==XT_BOOL) return "BOOL";
	if (xt==XT_ARRAY_INT) return "INT*";
	if (xt==XT_ARRAY_STR) return "STRING*";
	if (xt==XT_ARRAY_DOUBLE) return "REAL*";
	if (xt==XT_ARRAY_BOOL) return "BOOL*";
	if (xt==XT_SYM) return "SYMBOL";
	if (xt==XT_LANG) return "LANG";
	if (xt==XT_LIST) return "LIST";
	if (xt==XT_CLOS) return "CLOS";
	if (xt==XT_VECTOR) return "VECTOR";
	if (xt==XT_FACTOR) return "FACTOR";
	if (xt==XT_UNKNOWN) return "UNKNOWN";
	return "<unknown "+xt+">";
    }	

    /** get content of the REXP as string (if it is one)
        @return string content or <code>null</code> if the REXP is no string */
    public String asString() {
        return (Xt==XT_STR)?(String)cont:null;
    }

    /** get content of the REXP as int (if it is one)
        @return int content or 0 if the REXP is no integer */
    public int asInt() {
        if (Xt==XT_ARRAY_INT) {
            int i[]=(int[])cont;
            if (i!=null && i.length>0) return i[0];
        }
        return (Xt==XT_INT)?((Integer)cont).intValue():0;
    }

    /** get content of the REXP as double (if it is one)
        @return double content or 0.0 if the REXP is no double */
    public double asDouble() {
        if (Xt==XT_ARRAY_DOUBLE) {
            double d[]=(double[])cont;
            if (d!=null && d.length>0) return d[0];
        }
        return (Xt==XT_DOUBLE)?((Double)cont).doubleValue():0.0;
    }

    /** get content of the REXP as {@link Vector} (if it is one)
        @return Vector content or <code>null</code> if the REXP is no Vector */
    public Vector asVector() {
        return (Xt==XT_VECTOR)?(Vector)cont:null;
    }

    /** get content of the REXP as {@link RFactor} (if it is one)
        @return {@link RFactor} content or <code>null</code> if the REXP is no factor */
    public RFactor asFactor() {
        return (Xt==XT_FACTOR)?(RFactor)cont:null;
    }

    /** get content of the REXP as {@link RList} (if it is one)
        @return {@link RList} content or <code>null</code> if the REXP is no list */
    public RList asList() {
        return (Xt==XT_LIST)?(RList)cont:null;
    }

    /** get content of the REXP as {@link RBool} (if it is one)
        @return {@link RBool} content or <code>null</code> if the REXP is no logical value */
    public RBool asBool() {
        return (Xt==XT_BOOL)?(RBool)cont:null;
    }

    /** get content of the REXP as an array of doubles. Array of integers, single double and single integer are automatically converted into such an array if necessary.
        @return double[] content or <code>null</code> if the REXP is not a array of doubles or integers */
    public double[] asDoubleArray() {
        if (Xt==XT_ARRAY_DOUBLE) return (double[])cont;
        if (Xt==XT_DOUBLE) {
            double[] d=new double[1]; d[0]=asDouble(); return d;
        }
        if (Xt==XT_INT) {
            double[] d=new double[1]; d[0]=((Integer)cont).doubleValue(); return d;
        }
        if (Xt==XT_ARRAY_INT) {
            int[] i=asIntArray();
            if (i==null) return null;
            double[] d=new double[i.length];
            int j=0;
            while (j<i.length) {
                d[j]=(double)i[j]; j++;
            }
            return d;
        }
        return null;
    }

    /** get content of the REXP as an array of integers. Unlike {@link #asDoubleArray} <u>NO</u> automatic conversion is done if the content is not an array of the correct type, because there is no canonical representation of doubles as integers. A single integer is returned as an array of the length 1.
        @return double[] content or <code>null</code> if the REXP is not a array of  integers */
    public int[] asIntArray() {
        if (Xt==XT_ARRAY_INT) return (int[])cont;
        if (Xt==XT_INT) {
            int[] i=new int[1]; i[0]=asInt(); return i;
        }
        return null;
    }

    /** returns the content of the REXP as a matrix of doubles (2D-array: m[rows][cols]). This is the same form as used by popular math packages for Java, such as JAMA. This means that following leads to desired results:<br>
        <code>Matrix m=new Matrix(c.eval("matrix(c(1,2,3,4,5,6),2,3)").asDoubleMatrix());</code>
        @return 2D array of doubles in the form double[rows][cols] or <code>null</code> if the contents is no 2-dimensional matrix of doubles */
    public double[][] asDoubleMatrix() {
        if (Xt!=XT_ARRAY_DOUBLE || attr==null || attr.Xt!=XT_LIST) return null;
        REXP dim=attr.asList().getHead();
        if (dim==null || dim.Xt!=XT_ARRAY_INT) return null; // we need dimension attr
        int[] ds=dim.asIntArray();
        if (ds==null || ds.length!=2) return null; // matrix must be 2-dimensional
        int m=ds[0], n=ds[1];
        double[][] r=new double[m][n];
        double[] ct=asDoubleArray();
        if (ct==null) return null;
        // R stores matrices as matrix(c(1,2,3,4),2,2) = col1:(1,2), col2:(3,4)
        // we need to copy everything, since we create 2d array from 1d array
        int i=0,k=0;
        while (i<n) {
            int j=0;
            while (j<m) {
                r[j++][i]=ct[k++];
            }
            i++;
        }
        return r;
    }

    /** this is just an alias for {@link #asDoubleMatrix()}. */
    public double[][] asMatrix() {
        return asDoubleMatrix();
    }
    
    /** displayable contents of the expression. The expression is traversed recursively if aggregation types are used (Vector, List, etc.)
        @return String descriptive representation of the xpression */
    public String toString() {
	StringBuffer sb=
	    new StringBuffer("["+xtName(Xt)+" ");
	if (attr!=null) sb.append("\nattr="+attr+"\n ");
	if (Xt==XT_DOUBLE) sb.append((Double)cont);
	if (Xt==XT_INT) sb.append((Integer)cont);
	if (Xt==XT_BOOL) sb.append((RBool)cont);
	if (Xt==XT_FACTOR) sb.append((RFactor)cont);
	if (Xt==XT_ARRAY_DOUBLE) {
	    double[] d=(double[])cont;
	    sb.append("(");
	    for(int i=0; i<d.length; i++) {
		sb.append(d[i]);
		if (i<d.length-1) sb.append(", ");
                if (i==99) {
                    sb.append("... ("+(d.length-100)+" more values follow)");
                    break;
                }
	    };
	    sb.append(")");
	};
	if (Xt==XT_ARRAY_INT) {
	    int[] d=(int[])cont;
	    sb.append("(");
	    for(int i=0; i<d.length; i++) {
		sb.append(d[i]);
		if (i<d.length-1) sb.append(", ");
                if (i==99) {
                    sb.append("... ("+(d.length-100)+" more values follow)");
                    break;
                }
            };
	    sb.append(")");
	};
	if (Xt==XT_ARRAY_BOOL) {
	    RBool[] d=(RBool[])cont;
	    sb.append("(");
	    for(int i=0; i<d.length; i++) {
		sb.append(d[i]);
		if (i<d.length-1) sb.append(", ");
	    };
	    sb.append(")");
	};
	if (Xt==XT_VECTOR) {
	    Vector v=(Vector)cont;
	    sb.append("(");
	    for(int i=0; i<v.size(); i++) {
		sb.append(((REXP)v.elementAt(i)).toString());
		if (i<v.size()-1) sb.append(", ");
	    };
	    sb.append(")");
	};
	if (Xt==XT_STR) {
	    sb.append("\"");
	    sb.append((String)cont);
	    sb.append("\"");
	};
	if (Xt==XT_SYM) {
	    sb.append((String)cont);
	};
	if (Xt==XT_LIST || Xt==XT_LANG) {
	    RList l=(RList)cont;
	    sb.append(l.head); sb.append(" <-> ");
	    sb.append(l.body);
	};
	if (Xt==XT_UNKNOWN) sb.append((Integer)cont);
	sb.append("]");
	return sb.toString();
    };
}   
