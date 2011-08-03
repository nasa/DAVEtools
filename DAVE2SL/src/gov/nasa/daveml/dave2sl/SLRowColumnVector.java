// SLRowColumnVector.java
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave2sl;

import java.util.Vector;


/**
 *
 * A vector of {@link SLCell}s and an associated cable tray.
 *
 * <p>
 * An SLRowColumnVector contains a Vector for the blocks in the
 * row or column and an ArrayList with signals that are carried below
 * the row or to the left of the column in a virtual cable tray.
 *
 * <p>
 * Modification history:
 *  <ul>
 *   <li>020620 Written</li>
 *   <li>040227 Updated for version 0.5</li>
 *  </ul>
 *
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * @version 0.9
 *
 **/

public class SLRowColumnVector
{
	/**
	 *  our row/col of cells
	 */

	Vector<SLCell> cells;

	/**
	 *  cable tray carrying thru signals
	 */

	SLCableTray cableTray;

	/**
	 *  if true, we're a row
	 */

	boolean isRow;

	/**
	 *  are we chatty while we work?
	 */

	boolean verboseFlag;

	/**
	 *
	 * SLRowColumnVector constructor with number of columns specified
	 *
	 * @param numCells Number of cells to allocate (can be grown)
	 * @param asRow <code>Boolean</code> signifying if we are to be row or col
	 *
	 **/

	public SLRowColumnVector( int numCells, boolean asRow )
	{
		int numTrays = 5;
		this.cells = new Vector<SLCell>( numCells );
		this.cells.setSize( numCells );	// fill will null objects
		this.cableTray = new SLCableTray( numTrays ); 
		this.isRow = asRow;
		this.verboseFlag = false;
	}



	/**
	 *
	 * Registers cell at specified offset
	 *
	 * @param offset offset amount (index)
	 *
	 **/

	public void set( int offset, SLCell cell )
	{
		if(offset > this.cells.size())
			this.cells.setSize( offset );	// may have one extra with this logic
		this.cells.set( offset, cell );
		if( this.isRow )
			cell.setRow(this);
		else
			cell.setCol(this);
	}


	/**
	 *
	 * Gets cell at specified offset
	 *
	 * @param offset 0-based offset value
	 *
	 **/

	public SLCell get( int offset )
	{
		if(offset > this.cells.size())
			return null;
		return this.cells.get( offset );
	}


	/**
	 *
	 * Returns index (offset) of specified cell
	 *
	 * @param theCell <code>SLCell</code> to find index of 
	 *
	 **/

	public int getOffset( SLCell theCell )
	{
		return cells.indexOf( theCell );
	}


	/**
	 *
	 * Returns width of column or height of row, including cable tray
	 *
	 **/

	public int getSize()
	{
		int theSize = this.getSizeNoTray() + this.cableTray.getSize();
		//    if( isRow )
		//  	System.out.print("  height of row returned as ");
		//    else
		//  	System.out.print("  width of col returned as ");
		//      System.out.println(theSize);
		return theSize;
	}


	/**
	 *
	 * Returns width of column or height of row, not including cable tray
	 *
	 **/

	public int getSizeNoTray()
	{
		int mySize = 0;
		int cellSize = 0;
		for( int i = 0; i < cells.size(); i++)
		{
			// loop through Vector of cells; return max cell width (if col) or height (if row)
			SLCell theCell = cells.get(i);
			if (theCell != null)
			{
				if (this.isRow)	// we're a Row of cells - find tallest cell
					cellSize = (cells.get(i)).getMinHeight();
				else			// We're a column - remember widest cell
					cellSize = (cells.get(i)).getMinWidth();
				if( cellSize > mySize ) mySize = cellSize;	// remember largest dimension
				//System.out.println("     cell " + i + " size is " + cellSize)
			}
		}
		return mySize;
	}


	public SLCableTray getTray() { return this.cableTray; }

	public Integer addToTray( SLSignal theSignal )
	{
		this.cableTray.add( theSignal );
		int theOffset = this.cableTray.indexOf( theSignal );
		return new Integer( theOffset );
	}


	/**
	 *
	 * Set verbose flag
	 *
	 **/

	public void makeVerbose() { this.verboseFlag = true; }


	/**
	 *
	 * Indicates status of verbose flag
	 *
	 **/

	public boolean isVerbose() { return this.verboseFlag; }


	/**
	 *
	 * Clears the verbose flag
	 *
	 **/

	public void silence() { this.verboseFlag = false; }

}
