// BlockInput
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
 * <p> Object representing an input block </p>
 *
 * <p> 031215 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 **/
import java.io.IOException;
import java.io.Writer;

/**
 *
 * <p>  The Input Block represents an input to the system </p>
 *
 **/
public class BlockInput extends Block {

    /**
     *  units of measure for downstream signal
     */
    String units;

    /**
     *
     * <p> Constructor for input Block <p>
     *
     * @param theSignal Downstream <code>Signal</code> with which to connect
     * @param m <code>Model</code> we're part of
     *
     **/
    public BlockInput(Signal theSignal, Model m) {
        // Initialize superblock elements
        super(theSignal.getName(), "input", 0, m);

        // record our units
        this.units = theSignal.getUnits();

        // hook up to downstream signal
        try {
            this.addOutput(theSignal);
        } catch (DAVEException e) {
            System.err.println("Unexpected error: new Input block '" + this.getName()
                    + "' unable to hook up to downstream signal ");
            System.exit(0);
        }
//System.out.println("    BlockInput constructor: " + myName + " as input " + seqNumber);
    }

    /**
     * <p> Accepts a new input value </p>
     *
     * @param theValue a <code>double</code> value which represents the new input value
     *
     */
    public void setInputValue(double theValue) {
        this.value = theValue;
    }

    /**
     * <p> Returns the units of measure of the input signal </p>
     *
     **/
    public String getUnits() {
        return this.units;
    }

    /**
     *
     * Returns our sequence number (1-based) such as input 1, input 2, etc.
     *
     **/
    public int getSeqNumber() {
        BlockArrayList modelInputs = this.ourModel.getInputBlocks();
        return modelInputs.indexOf(this) + 1;
    }

    
    /**
     * Common input documentation scheme for all code types
     * @return string with description of input signal
     */
    @Override
    public CodeAndVarNames genCode() {
        CodeAndVarNames cvn = new CodeAndVarNames();
        String code = "";
        Signal theSignal = this.getOutput();
        if (theSignal != null) {
            cvn.addVarName(outVarID);
            code += outVarID;
            if (theSignal.isStdAIAA()) 
                code += " (" + theSignal.getName() + ")";
            code += " is a model input";
            if (units.equalsIgnoreCase("nd"))
                code += " and is non-dimensional.";
            else
                code += " with units \'" + units + "\'";
        }
        cvn.appendCode(this.wrapComment(code));
        return cvn;
    }

    /**
     *
     * <p> Generates description of self </p>
     *
     * @throws <code>IOException</code>
     **/
    @Override
    public void describeSelf(Writer writer) throws IOException {
        super.describeSelf(writer);
        writer.write(" (" + units + ")");
    }

    /**
     *
     * <p> Implements update() method </p>
     * @throws DAVEException
     *
     **/
    @Override
    public void update() throws DAVEException {
        if (isVerbose()) {
            System.out.println();
            System.out.println("Method update() called for input block '" + this.getName() + "'");
        }

        // input blocks are always assumed ready
        this.resultsCycleCount = ourModel.getCycleCounter();
    }
}
