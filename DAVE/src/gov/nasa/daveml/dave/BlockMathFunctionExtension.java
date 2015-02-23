// BlockMathFunctionExtension
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.
//
// 070223 Added to DAVE

package gov.nasa.daveml.dave;

/**
 *
 * DAVE-ML extensions to MathML 2 functions
 *
 * @author 070223 Bruce Jackson <mailto:bruce.jackson@nasa.gov>
 * @since version 0.8 / rev 193
 *
 **/

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;


/**
 *
 * <p> The MathExtension block represents an official DAVE-ML extension to MathML </p>
 * @since version 0.8 / rev 193
 **/

public class BlockMathFunctionExtension extends BlockMathFunction
{

    /**
     * Defined supported functions, for speed of execution
     * see BlockMathFunction for MathML2 list
     * @since version 0.8 / rev 193
     **/

    private static final int ATAN2 = 11;        // double-digits implies two inputs
    private static final String DAVEML_EXT_URL = "http://daveml.org/function_spaces.html";

    /**
     *
     * <p> Constructor for Math Extension Block <p>
     *
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing "apply" element
     * @param m         The parent <code>Model</code>
     * @since version 0.8 / rev 193
     *
     **/

    @SuppressWarnings("unchecked")
    public BlockMathFunctionExtension( Element applyElement, Model m )
    {
        // Initialize superblock elements
        super(m);  // hooks us up to model

        // Here with applyElement pointing to something like:
        //    <apply>
        //      <csymbol definitionURL="http://daveml.org/function_spaces.html#atan2"
        //               encoding="text">
        //        atan2
        //      </csymbol>
        //      <ci>y_arg</ci>
        //      <cn>x_arg</cn>
        //    </apply>

        // Parse parts of the Apply element
        List<Element> kids = applyElement.getChildren();
        Iterator<Element> ikid = kids.iterator();

        // first element should be 'csymbol'; DON'T use for name
        Element csymbol = ikid.next();
        if( csymbol == null ) {
            System.err.println("Error - BlockMathFunctionExtension constructor couldn't locate " +
                               "csymbol child element.");
            System.exit(0);
        }

        String csName = csymbol.getName();
        if( csName == null ) {
            System.err.println("Error - BlockMathFunctionExtension constructor couldn't locate " +
                               "csymbol element name.");
            System.exit(0);
        }
        if( !csName.equals("csymbol") ) {
            System.err.println("Error - BlockMathFunctionExtension constructor called with wrong" +
                               " child element - expected 'csymbol' but found" + csName);
            System.exit(0);
        }
        int csSize = csymbol.getContentSize();
        if( csSize != 1 ) {
            System.err.println("Error - BlockMathFunctionExtension constructor called with " +
                               csSize + " elements - expected a single element.");
            System.exit(0);
        }

        // check the definition URL attribute - should start with 'http://daveml.org/function_spaces'
        String defURL   = csymbol.getAttributeValue("definitionURL");
        if( defURL == null ) {
            System.err.println("Warning - required csymbol 'definitionURL' attribute missing; assumed valid");
        } else {
            if( !defURL.startsWith(DAVEML_EXT_URL) ) {
                System.err.println("Error - required csymbol 'definitionURL' unrecognized.");
                System.exit(0);
            }
        }

        // check the encoding attribute - should be 'text'
        String encoding = csymbol.getAttributeValue("encoding");
        if( encoding == null ) {
            System.err.println("Warning - required csymbol 'encoding' attribute missing; assumed 'text'");
        } else {
            if( !encoding.equals("text") ) {
                System.err.println("Error - required csymbol 'encoding' unrecognized.");
                System.exit(0);
            }
        }
                
        // get the actual name of the extension function
        this.funcType = csymbol.getTextTrim();
        if( this.funcType == null ) {
            System.err.println("Error - Couldn't retrieve child text (function extension name)\n" +
                               "of csymbol element.");
            System.exit(0);
        }
        this.setName( this.funcType + "_" + m.getNumBlocks() );
        
        // take appropriate action based on type
        if(this.funcType.equals("atan2")) {
            if( defURL != null ) {
                if (!defURL.equals(DAVEML_EXT_URL + "#" + this.funcType) ) {
                    System.err.println("Warning - csymbol 'definitionURL' attribute incomplete; assumed valid");
                }
            }
            this.op = ATAN2;
            this.myType = "two-argument arctangent function";
        } else {
            System.err.println("Error - BlockMathFunctionExtension constructor called with" +
                               " unknown type element:" + this.funcType);
            System.exit(0);
        }
        this.genInputsFromApply(ikid, 1);
        //System.out.println("    BlockMathFunctionExtension constructor: " + this.getName() + " created.");
    }

    /**
     * Change extended function type (calls superclass setFunction if not "atan2")
     * @param functionType String with name of function
     * @throws gov.nasa.daveml.dave.DAVEException
     * @since 0.9
     */
    @Override
    public void setFunction(String functionType) throws DAVEException {
    	if (functionType == null ? this.funcType != null : !functionType.equals(this.funcType)) {
    		super.setFunction(functionType);
        }
     }


    /**
     *
     * <p> Generates description of self </p>
     *
     * @param writer FileWriter on which to generate description
     * @throws IOException
     * @since version 0.8 / rev 193
     **/

    public void describeSelf(FileWriter writer) throws IOException
    {
        super.describeSelf(writer);
        writer.write(" and is a DAVE-ML math extension block.");
    }


    /**
     *
     * <p> Update our output value </p>
     * @throws DAVEException
     * @since version 0.8 / rev 193
     *
     **/

    @Override
    public void update() throws DAVEException
    {
        int requiredNumInputs;
        Iterator<Signal> theInputs;
        Signal theInput;
        double[] theInputValue;
        int index;

        boolean verbose = this.isVerbose();

        if (verbose) {
            System.out.println();
            System.out.println("Method update() called for math " + 
                    this.myType + " block '" + this.getName() + "'");
        }
        
        // Check to see if correct number of inputs
        if (this.inputs.size() < 1) {
            throw new DAVEException("Math " + this.myType + " block " + 
                    this.myName + " has no input.");
        }

        // check type of operation to see if have required number of inputs
        requiredNumInputs = 1;
        if (this.op > 9) { requiredNumInputs = 2; }

        if (this.inputs.size() > requiredNumInputs) {
            throw new DAVEException("Math function block " + this.myName + 
                    " has too many inputs.");
        }

        // see if the input variable(s) is(are) ready
        index = 0;
        theInputValue = new double[2];
        theInputs = this.inputs.iterator();
        while (theInputs.hasNext()) {
            theInput = theInputs.next();
            if (!theInput.sourceReady()) {
                if (verbose) {
                    System.out.println(" Upstream signal '" + 
                            theInput.getName() + "' is not ready.");
                }
                return;
            } else {
                theInputValue[index] = theInput.sourceValue();
                if (verbose) {
                    System.out.println(" Input #" + index + " value is " + 
                            theInputValue[index]);
                }
            }
            index++;
        }

        // Calculate our output
        this.value = Double.NaN;

        switch (this.op) {
        case ATAN2:
            this.value = Math.atan2(theInputValue[0],theInputValue[1]); break;
        }

        if (this.value == Double.NaN) {
            throw new DAVEException("Unrecognized operator " + this.funcType + 
                    " in block " + this.getName());
        }
        // record current cycle counter
        resultsCycleCount = ourModel.getCycleCounter();
    }
}
