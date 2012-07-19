// DAVEException
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave;

/**
 *
 * <p> Exception class for DAVE package </p>
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>} </p>
 * @since DAVE_tools 0.4
 **/

/**
 *
 * <p> Exception class for DAVE package </p>
 *
 **/

@SuppressWarnings("serial")
public class DAVEException extends Exception {
    
    /**
     * <p> Constructor for DAVEException </p>
     *
     **/
    public DAVEException()
    {
	super();
    }

    public DAVEException(String s)
    {
	super(s);
    }

}
