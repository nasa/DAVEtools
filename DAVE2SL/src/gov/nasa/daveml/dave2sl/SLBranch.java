package gov.nasa.daveml.dave2sl;

import java.util.ArrayList;

/**
*
* Single branch of an SLSignal, composed of a non-branching sequence of SLLineSegments.
*
* <p>
* The SLBranch contains a list of SLLineSegments that run from the output port of 
* one block to the input port of another block.
* </p>
*
*<p> 
* Modification history: 
* <ul>
*  <li>2010-05-04: Written to simplify SLSignal and SLDiagram classes</li>
* </ul>
*
* @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
* @since 0.9
* @version 0.9
*
**/

public class SLBranch extends ArrayList<SLLineSegment>
{
	/*
	 * Set the initial number of segments
	 */
	public SLBranch(int i) {
		super(i);
	}

	/**
	 * Not sure why Eclipse wants this to be present
	 */
	private static final long serialVersionUID = -1954668026239970685L;

}
