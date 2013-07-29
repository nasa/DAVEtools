// BlockArrayList
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave;

/**
 *
 * <p>Extends ArrayList for Block objects </p>
 * <p> 031211 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 */

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * <p> The <code>BlockArrayList</code> extends the
 * <code>ArrayList</code> object to allow searching for specific types
 * of <code>Block</code>s. </p>
 *
 **/

@SuppressWarnings("serial")
public class BlockArrayList extends ArrayList<Block>
{

    /**
     *
     * <p> Constructor takes no arguments </p>
     *
     **/

    public BlockArrayList() { super(); }

    /**
     *
     * <p> Constructor takes initial capacity estimate </p>
     *
     * @param initialCapacity integer estimate of required slots
     *
     **/

    public BlockArrayList( int initialCapacity ) { super(initialCapacity); }


    /**
     *
     * <p> Constructor converts existing Collection </p>
     *
     * @param c existing Collection
     *
     **/

    public BlockArrayList( Collection<Block> c ) { super(c); }

}
