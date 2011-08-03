// SLCell.java
//  
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave2sl;

/**
 *
 * Object representing the cubbyhole for a block in a Simulink diagram.
 *
 * <p>
 * Width and height are actually dictated by our row and
 * column. The SLCell acts as mediator between embedded block (which
 * has a width and height) and the Row/Column parent (who can
 * provide row and height).
 *
 * <p>
 * Modification history:
 *  <ul>
 *   <li>020624 Written</li>
 *   <li>040225 Updated for version 0.5</li>
 *  </ul>
 *
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * @version 0.9
 *
 **/

public class SLCell
{
	/**
	 * our parent diagram
	 */

	SLDiagram myParent;

	/**
	 * the occupying <code>SLBlock</code> contained in this cell
	 */

	SLBlock myBlock;

	/**
	 * Which row I belong to
	 */

	SLRowColumnVector myRow;

	/**
	 * Which column I belong to
	 */

	SLRowColumnVector myCol;

	/**
	 *
	 * Base constructor
	 *
	 **/

	public SLCell()
	{
		myParent = null;
		myBlock = null;
	}

	/**
	 *
	 * Constructor that records block and diagram.
	 *
	 * @param b {@link SLBlock} to store in this cell.
	 * @param theDiagram Our parent {@link SLDiagram}.
	 *
	 **/

	public SLCell( SLBlock b, SLDiagram theDiagram )
	{
		this();
		this.myBlock = b;
		this.myParent = theDiagram;
	}


	/**
	 *
	 * Returns the block contained herein.
	 *
	 **/

	public SLBlock getBlock() { return this.myBlock; }


	/**
	 *
	 * Returns the row index (why is this reversed? may be bug)
	 *
	 **/

	public int getRowIndex()  { return myCol.getOffset( this ); }


	/**
	 *
	 * Returns the column index (why is this reversed?)
	 *
	 **/

	public int getColIndex()  { return myRow.getOffset( this ); }


	/**
	 *
	 * Returns the row {@link SLRowColumnVector} to which cell belongs
	 *
	 **/

	public SLRowColumnVector getRow() { return this.myRow; }


	/**
	 *
	 * Returns the column {@link SLRowColumnVector} to which cell belongs
	 *
	 **/

	public SLRowColumnVector getCol() { return this.myCol; }


	/**
	 *
	 * Returns the <code>SLCell</code> adjacent to us in our column
	 *
	 **/

	public SLCell previousCellInColumn() 
	{
		return myParent.getCell(this.getRowIndex()-1, this.getColIndex());
	}


	/**
	 *
	 * Returns the <code>SLCell</code> adjacent to us in our row
	 *
	 **/

	public SLCell previousCellInRow()
	{
		return myParent.getCell(this.getRowIndex(), this.getColIndex()-1);
	}


	/**
	 *
	 * Returns the <code>SLCell</code> adjacent to us in our column
	 *
	 **/
	public SLCell nextCellInColumn()
	{
		return myParent.getCell(this.getRowIndex()+1, this.getColIndex());
	}


	/**
	 *
	 * Returns the <code>SLCell</code> adjacent to us in our row
	 *
	 **/

	public SLCell nextCellInRow()
	{
		return myParent.getCell(this.getRowIndex(), this.getColIndex()+1);
	}


	/**
	 *
	 * Assigns this SLCell to a given row.
	 *
	 * @param theRow the row to which we are assigned
	 *
	 **/

	public void setRow( SLRowColumnVector theRow ) { this.myRow = theRow; }


	/**
	 *
	 * Assigns this SLCell to a given column.
	 *
	 * @param theCol the column to which we are assigned
	 *
	 **/

	public void setCol( SLRowColumnVector theCol ) {this.myCol = theCol; }


	/**
	 *
	 * Returns desired minimum width, based on specified margin around
	 * encapsulated block.
	 *
	 * <p>Note we use only a single pad for height, but double pad for
	 * width.
	 *
	 **/

	public int getMinWidth()
	{
		return this.myBlock.getMDLWidth() + 2*this.myParent.getPadding();
	}


	/**
	 *
	 * Returns desired minimum height, based on specified margin
	 * around encapsulated block. 
	 *
	 * <p>Note we use only a single pad for height, but double pad for
	 * width.
	 *
	 **/

	public int getMinHeight()
	{
		return this.myBlock.getMDLHeight() + this.myParent.getPadding();
	}


	/**
	 *
	 * Returns our column's width.
	 *
	 **/

	public int getWidth()  { return myCol.getSize(); }


	/**
	 *
	 * Returns our row's height.
	 *
	 **/

	public int getHeight() { return myRow.getSize(); }


	/**
	 *
	 * Returns distance from input or output port to edge of cell,
	 * based on size of block and cell itself.
	 *
	 **/

	public int distToEdge()
	{

		//      System.out.println("for block '" + this.myBlock.getName() + "':");
		//      System.out.println("    Col width   " + this.getWidth());
		//      System.out.println("  Block width  -" + this.myBlock.getMdlWidth());
		//      System.out.println("               ----------");
		//      System.out.println("    difference  " + (this.getWidth() - this.myBlock.getMdlWidth()));
		//      System.out.println(" half of diff   " + (this.getWidth() - this.myBlock.getMdlWidth())/2);
		//      System.out.println();

		return (this.getWidth() - this.myBlock.getMDLWidth())/2;
	}
}
