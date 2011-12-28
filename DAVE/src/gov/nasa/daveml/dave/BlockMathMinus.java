// BlockMathMinus
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
 * <p> Minus value math function block </p>
 * <p> Was named BlockMathGain, but that isn't supported by mathml2?</p>
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
 * <p> The BlockMathMinus block represents a minus block </p>
 *
 **/

public class BlockMathMinus extends BlockMath
{
    /**
     *
     * <p> Constructor for Minus Block <p>
     *
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing "apply" element
     * @param m		The parent <code>Model</code>
     *
     **/

    @SuppressWarnings("unchecked")
	public BlockMathMinus( Element applyElement, Model m )
    {
	// Initialize superblock elements
	super("pending", "minus", m);

	// Parse parts of the Apply element
	List<Element> kids = applyElement.getChildren();

        // should be either two or three children for unary or binary minus;
        // first is the minus flag element
        if( kids.size() < 2)
            System.err.println("Warning - <apply><minus/></apply> found; need one or two arguments.");

        if (kids.size() > 3)
            System.err.println("Warning - <apply><minus/> construct with more than two arguments; truncating.");

	Iterator<Element> ikid = kids.iterator();

	// first element should be our type; also use for name
	Element first = ikid.next();
	this.setName( first.getName() + "_" + m.getNumBlocks() );
	
	// take appropriate action based on type
	if(!first.getName().equals("minus"))  {
	    System.err.println("Error - BlockMathMinus constructor called with" +
			       " wrong type element.");
	} else
	    genInputsFromApply( ikid, 1 );
    }
    
    
    /**
     * <p> Generate C-code equivalent of our operation</p>
     */
    
    @Override
    public String genCcode() {
        String code = "";
        Signal input0, input1;
        Signal outputSig = this.getOutput();
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (!outputSig.isDerived()) {
            code = "// Code for variable \"" + myName + "\":\n";
            code = code + "  " + myName + " = ";
        }
        input0 = inputs.get(0);
        if (inputs.size() == 1) { // unary minus
            code = code + "-" + input0.genCcode();
        } else { // must be binary minus: input 0 less 1    
            input1 = inputs.get(1);
            code = code + input0.genCcode() + " - " + input1.genCcode();           
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
        Signal input0, input1;
        Signal outputSig = this.getOutput();
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (!outputSig.isDerived()) {
            code = "C Code for variable \"" + myName + "\":\n";
            code = code + "  " + myName + " = ";
        }
        input0 = inputs.get(0);
        if (inputs.size() == 1) { // unary minus
            code = code + "-" + input0.genFcode();
        } else { // must be binary minus: input 0 less 1    
            input1 = inputs.get(1);
            code = code + input0.genCcode() + " - " + input1.genFcode();           
        }
        // if not derived, need new line
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

    @Override
    public void describeSelf(Writer writer) throws IOException
    {
	super.describeSelf(writer);
	writer.write(" and is a Minus block.");
    }

    /**
     *
     * <p> Implements update() method </p>
     * @throws DAVEException
     *
     **/

    @Override
    public void update() throws DAVEException
    {

	if (isVerbose()) {
	    System.out.println();
	    System.out.println("Method update() called for minus block '" + this.getName() + "'");
	}

        int numInputs = this.inputs.size();
	// Check to see if only one input
	if (numInputs < 1)
	    throw new DAVEException("Minus block " + this.myName + " has no input.");

	if (numInputs > 2)
	    throw new DAVEException("Minus block " + this.myName + " has more than two inputs.");

        if (numInputs == 1) { // unary minus block

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

            // Calculate output
            this.value = -inputValue;

        } else { // binary minus block

            // see if both input variables are ready
            Signal minuend = this.inputs.get(0);
            assert(minuend != null);
            if (!minuend.sourceReady()) {
                if (this.isVerbose())
                    System.out.println(" Upstream signal '" + minuend.getName() + "' is not ready.");
                return;
            }

            Signal subtrahend = this.inputs.get(1);
            assert(subtrahend != null);
            if (!subtrahend.sourceReady()) {
                if (this.isVerbose())
                    System.out.println(" Upstream signal '" + subtrahend.getName() + "' is not ready.");
                return;
            }

            // get both input variable values
            double minuendValue = minuend.sourceValue();
            double subtrahendValue = subtrahend.sourceValue();
            if (this.isVerbose())
                System.out.println(" Minuend value is " + minuendValue + ";"
                              + " subtrahend value is " + subtrahendValue);

            // Calculate output
            this.value = minuendValue - subtrahendValue;
        }
	// record current cycle counter
	resultsCycleCount = ourModel.getCycleCounter();

    }
}
