// VectorInfoArrayList
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave;

/**
 * <p> Extends ArrayList for VectorInfo objects </p> 
 * <p> A VectorInfo contains external names and values for I/O lists
 * to communicate with other DAVE utilities.</p>
 * <p> 031227 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 */


import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * <p> Extends the ArrayList object for VectorInfo objects </p>
 *
 */

@SuppressWarnings("serial")
public class VectorInfoArrayList extends ArrayList<VectorInfo>
{

    /**
     *
     * <p> Constructor takes no arguments </p>
     *
     **/

    public VectorInfoArrayList() { super(); }

    /**
     *
     * <p> Constructor takes initial capacity estimate </p>
     *
     * @param initialCapacity integer estimate of required slots
     *
     **/

    public VectorInfoArrayList( int initialCapacity ) { super(initialCapacity); }


    /**
     *
     * <p> Constructor converts existing <code>Collection</code> </p>
     *
     * @param c existing <code>Collection</code>
     *
     **/

    public VectorInfoArrayList( Collection<VectorInfo> c ) { super(c); }

 }
