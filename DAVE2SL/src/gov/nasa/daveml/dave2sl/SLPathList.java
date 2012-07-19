// SLPathList.java
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.daveml.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave2sl;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * An extension to ArrayList object, intended to be an array of SLBranches; each
 * SLBranch is an array of (non-branching) SLLineSegments that run from an output
 * port to an input port.
 *
 * An SLPathList can be condensed into a single SLLineSegment with branching children
 *
 * <p>
 * Modification history:
 *  <ul>
 *   <li>020710 Written</li>
 *   <li>040225 Updated for version 0.5</li>
 *   <li>2010-05-04 Modified to use SLBranches instead of ArrayList of SLLineSegments</li>
 *  </ul>
 *
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * @version 0.9
 *
 **/

@SuppressWarnings("serial")
public class SLPathList extends ArrayList<SLBranch>
{

	/**
	 *
	 * Basic constructor.
	 *
	 **/

	public SLPathList()
	{
		super();
	}


	/**
	 * 
	 * Constructor allowing initial size to be specified.
	 *
	 **/

	public SLPathList( int size )
	{
		super( size );
	}


	/**
	 *
	 * Overrides ArrayList add method, to ensure only <code>ArrayList</code>s are added.
	 *
	 * @param branch the <code>SLBranch</code> to be added
	 *
	 **/

	public boolean add( SLBranch branch )
	{
		super.add(branch);
		return true;
	}


	/**
	 *
	 * Moves objects on indicated <code>SLPathList</code> into this object.
	 *
	 * @param otherList the other 'list.
	 *
	 **/

	public void merge( SLPathList otherList )
	{
		//System.out.println("Merging path list... ");
		//otherList.describe();

		//System.out.println("...into path list... ");
		//this.describe();

		Iterator<SLBranch> it = otherList.iterator();
		while( it.hasNext() )
		{
			this.add( it.next() );
		}

		//System.out.println("Merge complete... ");
		//this.describe();
		//System.out.println();
	}


	/**
	 *
	 * Writes text description to output.
	 *
	 **/

	public void describe()
	{
		System.out.println("...an SLPathList with " + this.size() + " paths:");
		int i = 1;
		Iterator<SLBranch> it = this.iterator();
		while( it.hasNext() )
		{
			System.out.println(" Path " + i + ": ");
			ArrayList<?> a = (ArrayList<?>) it.next();
			Iterator<?> ait = a.iterator();
			while( ait.hasNext() )
			{
				SLLineSegment ls = (SLLineSegment) ait.next();
				ls.describe("  ");
			}
			i++;
		}
		System.out.println();
	}
}
