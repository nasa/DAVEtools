// SLSignal.java
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave2sl;

import gov.nasa.daveml.dave.Block;
import gov.nasa.daveml.dave.BlockArrayList;
import gov.nasa.daveml.dave.Signal;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;


/**
 *
 * An extension to the DAVE Signal object that includes Simulink
 * diagram routing information.
 *
 * Note: many methods are no longer used; we let Matlab figure out the
 * routing for these automatically generated lines. They are retained
 * in case we ever want to try to clean up the autorouted lines. The
 * 'createAddLine' method is called to generate lines using Simulink's
 * 'add_line' method; one 'add_line' is generated for each
 * outport-inport pairing, so our branching and routing is ignored.
 *
 * <p>
 * Modification history:
 *  <ul>
 *   <li>020621 Written</li>
 *   <li>040227 Updated for version 0.5</li>
 *   <li>060901 Rewritten to use 'add_block', 'add_line', etc instead of raw MDL.
 *  </ul>
 *
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * @version 0.9
 *
 **/

public class SLSignal extends Signal
{
	/**
	 *  our parent diagram
	 */

	SLDiagram parentDiagram;

	/**
	 *  starting cell
	 */

	SLCell sourceCell;

	/**
	 *  defined if only one destination that is adjacent
	 */

	SLCell destCell;

	/**
	 *  temporary set of ArrayLists of SLLineSegments leading to each destination
	 *  Question: Each element may contain single SLLineSegments, or an array of SLLineSegments?
	 */

	ArrayList<SLBranch> branches;

	/**
	 *  branching tree of SLLineSegments, filled in with call to singlePath() method
	 */

	SLLineSegment path;

	/**
	 *
	 * Elementary constructor
	 *
	 **/

	public SLSignal()
	{
		super();
		if (this.isVerbose())
			System.out.println("SLSignal basic constructor called.");
		this.parentDiagram = null;
		this.sourceCell = null;
		this.branches = new ArrayList<SLBranch>(10);
		this.path = null;
	}


	/**
	 *
	 * Expanding copy constructor
	 *
	 * @param oldSignal Signal to convert to SLSignal
	 * @param theParentDiagram The Diagram we belong to
	 *
	 **/

	public SLSignal( Signal oldSignal, SLDiagram theParentDiagram )
	{
		super( oldSignal );	// copy constructor for Signal
		if (this.isVerbose())
			System.out.println("SLSignal copy constructor called with old signal " + oldSignal.getName());    
		this.parentDiagram = theParentDiagram;

		// copy over most fields
		if(this.getSource() != null) {
			Block b = this.getSource();
			SLBlock slb = (SLBlock) b.getMask();
			if (slb == null) {
				System.err.println("ERROR: Unexpected null mask for block '" + b.getName()
						+ "' in constructing downstream SLSignal!");
				System.exit(0);
			}
			this.sourceCell = this.parentDiagram.getCell( slb );
		} else {
			System.err.println("WARNING: no source for signal " + this.getName());
		}
		this.branches = new ArrayList<SLBranch>(10);
		this.path = null;
	}


	/**
	 *
	 * Returns parent diagram of signal
	 *
	 * @return the parent {@link SLDiagram}
	 *
	 **/

	public SLDiagram getDiagram() { return this.parentDiagram; }



	/**
	 * 
	 * Creates text description of all branches.
	 *
	 * <p> 
	 * An {@link SLSignal} has two possible descriptions of
	 * routing information: on the first pass (by calling the
	 * initialRoute method) the "branches" <code>ArrayList</code>
	 * field will be filled out with individual, non-branching paths
	 * from the output port to each destination. Following the second
	 * pass (by calling the makeBranchingRoute method) the branches
	 * are merged into a signal branching tree structure starting with
	 * the <code>SLLineSegment</code> found in the "path" field.
	 *
	 **/

	public void describeBranches()
	{

		if (!this.isVerbose())
			return;

		// write header information

		System.out.println();

		if (this.path == null)	// use old branching information
		{
			if (this.branches == null)
			{
				System.err.println("No branching information found in signal '" + this.getName() + "'!");
				return;
			}
			else
			{
				System.out.print("  Signal '" + getName() );
				if (this.branches.size() <= 1)
				{
					System.out.println("' has one path: from block '" + this.sourceCell.getBlock().getName() +
							"' at location [" + this.sourceCell.getRowIndex() + "," +
							this.sourceCell.getColIndex() + "] ");
					if (this.destCell != null)
						System.out.println("   to adjacent block '" + this.destCell.getBlock().getName() +
								"' at location [" + this.destCell.getRowIndex() + "," +
								this.destCell.getColIndex() + "]");
				}
				else
				{
					System.out.println("' has " + this.branches.size() + " branches: from block '" + this.sourceCell.getBlock().getName() +
							"' at location [" + this.sourceCell.getRowIndex() + "," +
							this.sourceCell.getColIndex() + "] ");
				}

				// write routing information

				Iterator<SLBranch> ib = this.branches.iterator();
				while (ib.hasNext())
				{
					SLLineSegment ls = null;
					SLBranch list = ib.next();
					Iterator<SLLineSegment> iline = list.iterator();

					while (iline.hasNext())			// scan ahead...
						ls = iline.next();	// to find destination

					SLCell destination = ls.getDestCell();
					System.out.println("   to block '"   + destination.getBlock().getName() +
							"' at location [" + destination.getRowIndex() + "," +
							destination.getColIndex() + "] via");

					iline = list.iterator();	// start over at beginning of path
					boolean first = true;
					while (iline.hasNext())
					{
						if(first)
						{
							first = false;
							System.out.print("     ");
						}
						else
							System.out.print("     then ");

						ls = iline.next();

						ls.describe();
					}
				}
			}
		}	// end of null "path" field
		else
		{	// use branching structure found in "path" field
			if (this.isVerbose())
				this.path.describe();
		}
	}


	/**
	 * 
	 * Creates text description of path (branching tree)
	 *
	 **/

	public void describePath()
	{

		if (!this.isVerbose())
			return;

		System.out.println();
		System.out.print("  Signal '" + getName() + "' runs from block '" + this.sourceCell.getBlock().getName() +
				"' at location [" + this.sourceCell.getRowIndex() + "," +
				this.sourceCell.getColIndex() + "] ");
		if(path == null)
		{
			System.out.println(" to adjacent block '" + this.destCell.getBlock().getName() + 
					"' at location [" + this.destCell.getRowIndex() + "," +
					this.destCell.getColIndex() + "]. ");
		}
		else
		{
			System.out.println(" to " + this.getDests().size() + " different destination(s) as follows: ");
			this.path.describe("    ");
		}
	}


	/**
	 *
	 * Writes signal wiring for Simulink representation.
	 *
	 * @param writer Instance of the SLFileWriter class
	 *
	 */

	public void createAddLine(SLFileWriter writer) throws IOException
	{

		// for now, just hook up I/O with autorouting to single destination

		BlockArrayList dests = this.getDests();
		ArrayList<Integer> destPorts = this.getDestPortNumbers();
		Iterator<Block> id = dests.iterator();
		Iterator<Integer> ip = destPorts.iterator();
		while (id.hasNext()) {
			Integer destPort = ip.next();
			Block destBlock  = id.next();

			writer.addLine( this.getSource().getName(),
					this.getSourcePort(),
					destBlock.getName(),
					destPort, this.getName());
		}

	}	// end of createAddLine

}
