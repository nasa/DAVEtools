// BlockMathSum
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
 * <p> Summing math function block </p>
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
 * <p> The MathSum block represents a scalar summer </p>
 *
 **/

public class BlockMathSum extends BlockMath
{
    
    /**
     *
     * <p> Constructor for Sum Block <p>
     *
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing "apply" element
     * @param m         The parent <code>Model</code>
     *
     **/

    @SuppressWarnings("unchecked")
    public BlockMathSum( Element applyElement, Model m )
    {
        // Initialize superblock elements
        super("pending", "summing", m);

        // Parse parts of the Apply element
        List<Element> kids = applyElement.getChildren();
        Iterator<Element> ikid = kids.iterator();

        // first element should be our type; also use for name
        Element first = ikid.next();
        String blockType = first.getName();
        this.setName( blockType + "_" + m.getNumBlocks() );
        
        // take appropriate action based on type
        if(!blockType.equals("plus"))
            {
                System.err.println("Error - BlockMathSum constructor called with" +
                                   " type element:" + blockType);
            }
        else
            {
                //System.out.println("   BlockMathSum constructor called with " + kids.size() + "elements.");
                this.genInputsFromApply(ikid, 1);
            }

        //System.out.println("    BlockMathSum constructor: " + this.getName() + " created.");
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
        writer.write(" and is a Sum math block.");
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
        int index;

        boolean verbose = this.isVerbose();

        if (verbose) {
            System.out.println();
            System.out.println("Method update() called for summing block '" + this.getName() + "'");
        }
        
        // Check to see if inputs are ready
        theInputs = this.inputs.iterator();
        inputVals = new double[this.inputs.size()];

        index = 0;
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

        this.value = 0.0;
        for(int i = 0; i<inputVals.length; i++)
            this.value += inputVals[i];

        // record current cycle counter
        resultsCycleCount = ourModel.getCycleCounter();

    }
}
