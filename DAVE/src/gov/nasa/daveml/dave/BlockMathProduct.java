// BlockMathProduct
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
 * <p> Multiplication math function block </p>
 * <p> 031211 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 **/

import org.jdom.Element;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Iterator;

/**
 *
 * <p> The MathProduct block represents a scalar multiply/divide block </p>
 *
 **/

public class BlockMathProduct extends BlockMath
{

    /**
     *  should be either "times" or "quotient" or "divide"
     */

    String blockType;

    // quotient/divide blocks have only two inputs and calculate #1/#2.
    // product block inputs are multiplied together.
    
    /**
     *
     * <p> Constructor for Product Block <p>
     *
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing "apply" element
     * @param m		The parent <code>Model</code>
     *
     **/

    @SuppressWarnings("unchecked")
	public BlockMathProduct( Element applyElement, Model m )
    {
	// Initialize superblock elements
	super("pending", "product", m);

	// Parse parts of the Apply element
	List<Element> kids = applyElement.getChildren();
	Iterator<Element> ikid = kids.iterator();

	// first element should be our type; also use for part of name
	Element first = ikid.next();
	this.blockType = first.getName();
	this.setName( blockType + "_" + m.getNumBlocks() );
	
	// take appropriate action based on type
	if(blockType.equals("times"))
	  {
	      this.genInputsFromApply(ikid, 1);
	  }
	else if (blockType.equals("quotient"))
	    {
		if(kids.size() != 3)
		    System.err.println("Error - <apply><quotient/> only handles 2 arguments, not " + (kids.size()-1));
		else
		    this.genInputsFromApply(ikid, 1);
	    }
	else if (blockType.equals("divide"))
	    {
		if(kids.size() != 3)
		    System.err.println("Error - <apply><divide/> only handles 2 arguments, not " + (kids.size()-1));
		else
		    this.genInputsFromApply(ikid, 1);
	    }
	else
	  {
	    System.err.println("Error - BlockMathProduct constructor called with" +
			       " type element:" + blockType);
	  }

//System.out.println("    BlockMathProduct constructor: " + this.getName() + " created.");
    }


    /**
     *
     * Returns our block type
     *
     **/

    public String getBlockType() { return this.blockType; }


    /**
     * <p> Generate C-code equivalent of our operation</p>
     */
    
    @Override
    public String genCcode() {
        String code = "";
        Iterator<Signal> inputSig = inputs.iterator();
        Signal outputSig = this.getOutput();
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (!outputSig.isDerived()) {
//            code = "// Code for variable \"" + outVarID + "\":\n";
            code = code + "  " + outVarID + " = ";
        }
        while (inputSig.hasNext()) {
            Signal inSig = inputSig.next();
            code = code + inSig.genCcode();
            if (inputSig.hasNext()) {
                code = code + "*";
            }
        }
        // if not derived, need trailing semicolon and new line
        if (!outputSig.isDerived())
            code = code + ";\n";
        return code;
    }


    /**
     * <p> Generate FORTRAN code equivalent of our operation</p>
     */
    
    @Override
    public String genFcode() {
        String code = "";
        String indent = "       ";
        Iterator<Signal> inputSig = inputs.iterator();
        Signal outputSig = this.getOutput();
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (!outputSig.isDerived())
            code = code + indent + outVarID + " = ";
        while (inputSig.hasNext()) {
            Signal inSig = inputSig.next();
            code = code + inSig.genFcode();
            if (inputSig.hasNext())
                code = code + "*";
        }
        // if not derived, need newline
        if (!outputSig.isDerived())
            code = code + "\n";
        return code;
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
	writer.write(" and is a Product math block.");
    }

    /**
     *
     * <p> Implements update() method </p>
     * @throws DAVEException
     *
     **/

    public void update() throws DAVEException
    {
	Iterator<Signal> theInputs;
	double[] inputVals;
	Signal theInput;

	int index = 0;

	boolean verbose = this.isVerbose();

	if (verbose) {
	    System.out.println();
	    System.out.println("Method update() called for product block '" + this.getName() + "'");
	}
	
	// Check to see if inputs are ready
	theInputs = this.inputs.iterator();
	inputVals = new double[this.inputs.size()];

	while (theInputs.hasNext()) {
	    theInput = theInputs.next();
	    if (!theInput.sourceReady()) {
		if (verbose)
		    System.out.println(" Upstream signal '" + theInput.getName() + "' is not ready.");
		return;
	    } else {
		inputVals[index] = theInput.sourceValue();
		if (verbose)
		    System.out.println(" Input #" + index + " value is " + inputVals[index]);
	    }
	    index++;
	}

	if( this.blockType.equals("times")) {
	    this.value = 1.0;
	    for(int i = 0; i<inputVals.length; i++)
		this.value *= inputVals[i];
	} else {
	    this.value = inputVals[0]/inputVals[1];
	}

	// record current cycle counter
	resultsCycleCount = ourModel.getCycleCounter();

    }
}
