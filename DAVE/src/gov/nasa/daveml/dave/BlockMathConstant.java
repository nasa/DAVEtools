// BlockMathConstant
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
 * <p> Object representing a constant value in a model </p>
 * <p> 031214 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 */

import java.io.IOException;
import java.io.Writer;

/**
 *
 * <p>  The constant block represents a constant value source </p>
 *
 **/

public class BlockMathConstant extends BlockMath
{
    /**
     * The value of this constant
     */

    String stringValue;

    /**
     *
     * <p> Constructor for constant value Block <p>
     *
     * @param constantValue <code>String</code> containing value of constant
     *
     **/

    public BlockMathConstant( String constantValue, Model m )
    {
	// Initialize superblock elements
	super("const_" + m.getNumBlocks(), "constant value", m);
	this.setValue(constantValue);
    }


    /**
     * 
     * Returns our numeric value
     *
     **/

    public double getValueAsDouble() { return this.value; }


    /**
     *
     * Returns our string value
     *
     **/

    public String getValueAsString() { return this.stringValue; }


    /**
     * Sets our value from a string
     */
    
    public final void setValue( String newValue ) {
    	this.stringValue = newValue;

    	try {
    	    this.value = Double.parseDouble(stringValue);
    	} catch (java.lang.NumberFormatException e) {
    	    System.err.println("Encountered content-number element <cn> containing a string ('"
    			       + newValue + "') that cannot be "
    			       + "cannot be converted into a number; mislabeled "
    			       + "content-identifier element <ci>? DAVE unable to continue.");
    	    System.exit(0);
    	}
    }
    
    /**
     * Sets our value from a Double
     */
    
    public void setValue( Double newValue ) {
    	this.value = newValue;
    	this.stringValue = newValue.toString();
    }
    
    /**
     * <p> Generate C-code equivalent of our constant</p>
     */
    
    @Override
    public CodeAndVarNames genCode() {
        CodeAndVarNames cvn = new CodeAndVarNames();
        Signal outputSig = this.getOutput();
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (!outputSig.isDerived()) {
//            code = "// Code for variable \"" + outVarID + "\":\n";
            cvn.appendCode(indent() + outVarID + " = ");
            cvn.addVarName(outVarID);
        }
        
        cvn.appendCode(this.getValueAsString());
        
        // if not derived, need trailing semicolon and new line
        if (!outputSig.isDerived())
            cvn.appendCode(endLine());
        return cvn;
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
	writer.write(" and is a Constant Value math block.");
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
	    System.out.println("Method update() called for constant block '" + this.getName() + "'");
	}
	
	// Check to see if no input
	if (this.inputs.size() > 0)
	    throw new DAVEException("Constant block " + this.myName + " has more than zero inputs.");

	// record current cycle counter
	resultsCycleCount = ourModel.getCycleCounter();
    }
    
    /**
    *
    * Indicates if all results are up-to-date.
    * Constant blocks are always ready!
    *
    **/

    @Override
   public boolean isReady()
   {
	return true;
   }
}
