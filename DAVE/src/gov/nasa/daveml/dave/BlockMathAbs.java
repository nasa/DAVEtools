// BlockMathAbs
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
 * <p> Absolute value math function block </p>
 * <p> 031214 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 **/

import org.jdom.Element;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Iterator;

/**
 *
 * <p>  The Math block represents an absolute value (scalar input) function</p>
 *
 **/

public class BlockMathAbs extends BlockMath
{
    
    /**
     *
     * <p> Constructor for abs Block <p>
     *
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing "apply" element
     * @param m		The parent <code>Model</code>
     *
     **/

    @SuppressWarnings("unchecked")
	public BlockMathAbs( Element applyElement, Model m )
    {
	// Initialize superblock elements
	super("pending", "absolute value", m);

	// Parse parts of the Apply element
	List<Element> kids = applyElement.getChildren();
	Iterator<Element> ikid = kids.iterator();

	// first element should be our type; also use for name
	Element first = ikid.next();
	this.setName( first.getName() + "_" + m.getNumBlocks());
	
	// take appropriate action based on type
	if(!first.getName().equals("abs"))
	    {
		System.err.println("Error - BlockMathAbs constructor called with" +
				   " wrong type element.");
	    }
	else
	    {
		// look for single input
		if(kids.size() > 2)
		    System.err.println("Error - <apply><abs/> only handles single arguments, not " + (kids.size()-1));
		else
		    this.genInputsFromApply(ikid, 1);
	    }

//System.out.println("    BlockMathAbs constructor: " + myName + " created.");
    }


    /**
     *
     * <p> Update our output value </p>
     * @throws DAVEException
     *
     **/

    public void update() throws DAVEException
    {
	if (isVerbose()) {
	    System.out.println();
	    System.out.println("Method update() called for math abs block '" + this.getName() + "'");
	}
	
	// Check to see if only one input
	if (this.inputs.size() < 1)
	    throw new DAVEException("Abs block " + this.myName + " has no input.");

	if (this.inputs.size() > 1)
	    throw new DAVEException("Abs block " + this.myName + " has more than one input.");

	// see if single input variable is ready
	Signal theInput = this.inputs.get(0);
	if (!theInput.sourceReady()) {
	    if (this.isVerbose())
		System.out.println(" Upstream signal '" + theInput.getName() + "' is not ready.");
	    return;
	}

	// get single input variable value
	double inputValue = theInput.sourceValue();
	if (this.isVerbose())
	    System.out.println(" Input value is " + inputValue);

	// record current cycle counter
	resultsCycleCount = ourModel.getCycleCounter();

	// save answer
	this.value = Math.abs(inputValue);
    }

    /**
     *
     * <p> Generates description of self </p>
     *
     * @throws <code>IOException</code>
     **/

    public void describeSelf(Writer writer) throws IOException
    {
	super.describeSelf(writer);
	writer.write(" and is an Absolute Value math block.");
    }

}
