// BlockLimiter
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2011 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.


package gov.nasa.daveml.dave;

/**
 *
 * <p> Object representing a two-sided limiter block </p>
 * <p> 2010-12-15 Bruce Jackson, NASA Langley Research Center
 *     <mailto:bruce.jackson@nasa.gov> </p>
 * @author Bruce Jackson
 *
 **/

import java.io.IOException;
import java.io.Writer;

/**
 *
 * <p>  The Limiter Block provides upper and lower limits to an input signal. </p>
 *
 **/
public class BlockLimiter extends Block {
    /**
     *  units of measure of downstream block
     */

    String units;

    /**
     * lower limit, or -Inf
     */

    Double lowerLim;

    /**
     * upper limit, or +Inf
     */

    Double upperLim;

    /**
     * indicates presence of lower limit (not -Inf)
     */

    boolean hasLowerLim;

    /**
     * indicates presence of valid upper limit (not +Inf)
     */

    boolean hasUpperLim;



    /**
     *
     * <p> Constructor for output Block <p>
     *
     * @param sourceSignal Upstream <code>Signal</code> with which to connect
     * @param m <code>Model</code> we're part of
     * @param lowerLimit <code>String</code> representing the minimum value we can pass (-Infinity means no limit)
     * @param upperLimit <code>String</code> representing the maximum value we can pass (+Infinity means no limit)
     *
     **/

    public BlockLimiter( Signal sourceSignal, Model m, double lowerLimit, double upperLimit )
    {
        // Initialize superblock elements
        super(sourceSignal.getName()+ " limiter", "limiter", 1, m);

        // record our U of M
        this.units = sourceSignal.getUnits();

        // record limits
        lowerLim = lowerLimit;
        upperLim = upperLimit;

        // ensure correct order
        if (lowerLim > upperLim) {
            Double temp = lowerLim;
            lowerLim = upperLim;
            upperLim = temp;
        }

        // set limiting flags
        hasLowerLim = !lowerLim.isInfinite();
        hasUpperLim = !upperLim.isInfinite();

        // hook up to upstream signal
        //System.out.println("    BlockOutput constructor: " + myName + " is hooking up to signal " + sourceSignal.getName());
        sourceSignal.addSink( this, 1 );
        //System.out.println("    BlockOutput constructor: " + myName + " as output " + seqNumber);
    }


    /**
     * <p> Returns the output value </p>
     *
     * <p> This method is distinguished from normal
     * <code>Block.getValue()</code> in that it is public</p>
     *
     **/

    public double getValue()    { return this.value; }


    /**
     * <p> Returns the units of measure of the output signal </p>
     *
     **/

    public String getUnits() { return this.units; }

    /**
     * <p> Generates C algorithm to limit input to output</p>
     */
    
    @Override
    public String genCcode() {
        String code = "";
        String indent = "  ";
        Signal input;
        Signal outputSig = this.getOutput();
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (outputSig != null)
            if (!outputSig.isDerived()) {
//                code = "// Code for variable \"" + outVarID + "\":\n";
                code = code + indent + outVarID + " = ";
            }
        input = inputs.get(0);
        code = code + input.genCcode();
        if (this.hasLowerLimit()) {
            code = code + ";\n";
            code = code + indent + "if ( " + outVarID + " < " + lowerLim.toString() + " ) {\n";
            code = code + indent + indent + outVarID + " = " + lowerLim.toString() + ";\n";
            code = code + indent + "}\n";
        }
        if (this.hasUpperLimit()) {
            if (!this.hasLowerLimit())
                code = code + ";\n";
            code = code + indent + "if ( " + outVarID + " > " + upperLim.toString() + " ) {\n";
            code = code + indent + indent + outVarID + " = " + upperLim.toString() + ";\n";
            code = code + indent + "}\n";
        }
        // if not derived, need trailing semicolon and new line if no limits
        if (outputSig != null)
            if (!outputSig.isDerived())
                if (!this.hasLowerLimit() && !this.hasUpperLimit() )
                    code = code + ";\n";
        return code;
        
    }

    /**
     * <p> Generates FORTRAN algorithm to limit input to output</p>
     */
    
    @Override
    public String genFcode() {
        String code = "";
        String indent = "       ";
        Signal input;
        Signal outputSig = this.getOutput();
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (outputSig != null)
            if (!outputSig.isDerived()) {
//                code = "* Code for variable \"" + outVarID + "\":\n";
                code = code + indent + outVarID + " = ";
            }
        input = inputs.get(0);
        code = code + input.genFcode();
        if (this.hasLowerLimit()) {
            code = code + "\n";
            code = code + indent + "if ( " + outVarID + " .LT. " + lowerLim.toString() + " ) then\n";
            code = code + indent + indent + outVarID + " = " + lowerLim.toString() + "\n";
            code = code + indent + "endif\n";
        }
        if (this.hasUpperLimit()) {
            if (!this.hasLowerLimit())
                code = code + "\n";
            code = code + indent + "if ( " + outVarID + " .GT. " + upperLim.toString() + " ) then\n";
            code = code + indent + indent + outVarID + " = " + upperLim.toString() + "\n";
            code = code + indent + "endif\n";
        }
        // if not derived, need new line if no limits
        if (outputSig != null)
            if (!outputSig.isDerived())
                if (!this.hasLowerLimit() && !this.hasUpperLimit() )
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
        writer.write(" (" + units + ") and is a limiter block with " +
                "a lower limit of " + lowerLim +
                " and an upper limit of " + upperLim + ".");
    }

    /**
     *
     * <p> Implements update() method </p>
     * @throws DAVEException
     *
     **/

    public void update() throws DAVEException
    {
        if (isVerbose()) {
            System.out.println();
            System.out.println("Method update() called for limiter block '" + this.getName() + "'");
        }

        // Check to see if only one input
        if (this.inputs.size() < 1)
            throw new DAVEException(" Limiter block " + this.myName + " has no input.");

        if (this.inputs.size() > 1)
            throw new DAVEException(" Limiter block " + this.myName + " has more than one input.");

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

        // show ourselves up-to-date
        this.resultsCycleCount = ourModel.getCycleCounter();

        // save answer
        this.value = inputValue;
        if (hasLowerLim)
            if( this.value < lowerLim )
                this.value = lowerLim;
        if (hasUpperLim)
            if( this.value > upperLim )
                this.value = upperLim;

    }

    /**
     * Indicates a practical lower limit has been defined
     * @return true if lower limit exists
     */

    public boolean hasLowerLimit() { return hasLowerLim; }


    /**
     * Indicates a practical upper limit has been defined
     * @return true if upper limit exists
     */
    public boolean hasUpperLimit() { return hasUpperLim; }


    /**
     * Returns the value of the lower limit
     * @return lower limit as a double
     */
    public double getLowerLimit() { return lowerLim.doubleValue(); }

    /**
     * Returns the value of the upper limit
     * @return upper limit as a double
     */
    public double getUpperLimit() { return upperLim.doubleValue(); }

}
