// BlockMathFunction
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.
//
// 040201 Added trig functions
// 070223 Added csymbol extensions

package gov.nasa.daveml.dave;

/**
 *
 * Multipurpose function block
 *
 * @author 031214 Bruce Jackson <mailto:bruce.jackson@nasa.gov>
 *
 **/

import org.jdom.Element;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Iterator;


/**
 *
 * <p> The MathFunction block represents a scalar exponentiation block </p>
 *
 **/

public class BlockMathFunction extends BlockMath
{

    /**
     * Defined supported functions, for speed of execution
     **/

    private static final int UNK  = 0;
    private static final int SIN  = 1;
    private static final int COS  = 2;
    private static final int TAN  = 3;
    private static final int ARCSIN = 4;
    private static final int ARCCOS = 5;
    private static final int ARCTAN = 6;
    private static final int POW  = 10; // double-digits implies two inputs

    String funcType;   // can be several types: "pow" is current only one (??)
    int op;             // can be several ints: 10 = POWER

    /**
     *
     * <p> Constructor for Function Block <p>
     *
     **/

    public BlockMathFunction( )
    {
        super();
        this.funcType = null;
        this.op = UNK;
    }

    /**
     *
     * <p> Constructor for Function Block <p>
     *
     * @param m         The parent <code>Model</code>
     *
     **/

    public BlockMathFunction( Model m )
    {
        super(m);
        this.funcType = null;
        this.op = UNK;
    }

    /**
     *
     * <p> Constructor for Function Block <p>
     *
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing "apply" element
     * @param m         The parent <code>Model</code>
     * @throws DAVEException 
     *
     **/

    @SuppressWarnings("unchecked")
        public BlockMathFunction( Element applyElement, Model m ) throws DAVEException
    {
        // Initialize superblock elements
        super("pending", "function", m);

        // Initialize function as unknown
        this.op = UNK;

        // Parse parts of the Apply element
        List<Element> kids = applyElement.getChildren();
        Iterator<Element> ikid = kids.iterator();

        // first element should be our type; also use for name
        Element first = ikid.next();
        this.setFunction( first.getName () );
        
        this.genInputsFromApply(ikid, 1);
        //System.out.println("    BlockMathFunction constructor: " + this.getName() + " created.");
    }


    public void setFunction(String functionType) throws DAVEException {
    	this.funcType = functionType;
        this.setName( funcType + "_" + this.ourModel.getNumBlocks() );
        
        // take appropriate action based on type
        if(funcType.equals("power")) {
            this.op = POW;
            this.myType = "power function";
        } else if (funcType.equals("sin")) {
            this.op = SIN;
            this.myType = "sine function";
        } else if (funcType.equals("cos")) {
            this.op = COS;
            this.myType = "cosine function";
        } else if (funcType.equals("tan")) {
            this.op = TAN;
            this.myType = "tangent function";
        } else if (funcType.equals("arcsin")) {
            this.op = ARCSIN;
            this.myType = "arcsine function";
        } else if (funcType.equals("arccos")) {
            this.op = ARCCOS;
            this.myType = "arccosine function";
        } else if (funcType.equals("arctan")) {
            this.op = ARCTAN;
            this.myType = "arctangent function";
        } else 
           throw new DAVEException("Unrecognized operator " + this.funcType 
        		   + " in call to BlockMathFunction.setFunction() method." );
    }

	/**
     *
     * Returns type string
     *
     **/

    public String getFuncType() { return funcType; }

 
    /**
     *
     * <p> Generates description of self </p>
     *
     * @throws <code>IOException</code>
     **/

    public void describeSelf(Writer writer) throws IOException
    {
        super.describeSelf(writer);
        writer.write(" and is a Function math block.");
    }


    /**
     *
     * <p> Update our output value </p>
     * @throws DAVEException
     *
     **/

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
            System.out.println("Method update() called for math " + this.myType + " block '" + this.getName() + "'");
        }
        
        // Check to see if correct number of inputs
        if (this.inputs.size() < 1)
            throw new DAVEException("Math " + this.myType + " block " + this.myName + " has no input.");

        // check type of operation to see if have required number of inputs
        requiredNumInputs = 1;
        if (this.op > 9) requiredNumInputs = 2;

        if (this.inputs.size() > requiredNumInputs)
            throw new DAVEException("Math function block " + this.myName + " has too many inputs.");

        // see if the input variable(s) is(are) ready
        index = 0;
        theInputValue = new double[2];
        theInputs = this.inputs.iterator();
        while (theInputs.hasNext()) {
            theInput = theInputs.next();
            if (!theInput.sourceReady()) {
                if (verbose)
                    System.out.println(" Upstream signal '" + theInput.getName() + "' is not ready.");
                return;
            } else {
                theInputValue[index] = theInput.sourceValue();
                if (verbose)
                    System.out.println(" Input #" + index + " value is " + theInputValue[index]);
            }
            index++;
        }

        // Calculate our output
        this.value = Double.NaN;

        switch (this.op) {
        case POW:
            this.value = Math.pow(theInputValue[0],theInputValue[1]); break;
        case SIN:
            this.value = Math.sin(theInputValue[0]); break;
        case COS:
            this.value = Math.cos(theInputValue[0]); break;
        case TAN:
            this.value = Math.tan(theInputValue[0]); break;
        case ARCSIN:
            this.value = Math.asin(theInputValue[0]); break;
        case ARCCOS:
            this.value = Math.acos(theInputValue[0]); break;
        case ARCTAN:
            this.value = Math.atan(theInputValue[0]); break;
        }

        if (this.value == Double.NaN)
            throw new DAVEException("Unrecognized operator " + this.funcType + " in block " + this.getName());

        // record current cycle counter
        resultsCycleCount = ourModel.getCycleCounter();
    }
}
