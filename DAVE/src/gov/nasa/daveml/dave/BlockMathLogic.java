// BlockMathLogic
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
 * <p> Logical operator math function block </p>
 * <p> 2015-02-13 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 **/

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;


/**
 *
 * <p>  The MathLogic block represents an Logical operator (scalar input) function</p>
 *
 **/

public class BlockMathLogic extends BlockMath
{
    /**
     * Defined logics, for speed of execution
     **/

    private static final int UNK = 0;
    private static final int OR  = 1;
    private static final int AND = 2;
    private static final int NOT = 3;

    /**
     * Which algebraic logic we're testing
     */

    String logicOp;

    /**
     * Which algebraic logic we're testing, using encoded value from internal table
     */

    int logic;
    
    /**
     *
     * <p> Constructor for logic Block <p>
     *
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing "apply" element
     * @param m         The parent <code>Model</code>
     *
     **/

    @SuppressWarnings("unchecked")
    public BlockMathLogic( Element applyElement, Model m )
    {
        // Initialize superblock elements
        super("pending", "logic", m);

        this.logic = UNK;

        // Parse parts of the Apply element
        List<Element> kids = applyElement.getChildren();
        Iterator<Element> ikid = kids.iterator();

        // first element should be our type; also use for name
        Element first = ikid.next();
        this.logicOp = first.getName();
        this.setName( this.logicOp + "_" + m.getNumBlocks() );
        
        // take appropriate action based on type
        if(!validateLogic()) {
            System.err.println("Error - BlockMathLogic constructor called with" +
                                   " unknown logic operator " + this.logicOp);
        } else {
            //System.out.println("   BlockMathLogic constructor called with " + kids.size() + "elements.");
	    this.genInputsFromApply(ikid, 1);
        }

        //System.out.println("    BlockMathLogic constructor: " + this.getName() + " created.");
    }


    /**
     *
     * <p> Determines if string is acceptable MathML logic </p>
     *
     * @return <code>boolean</code> true if is a logic
     *
     **/

    protected final boolean validateLogic( )
    {
    	this.logic = UNK;
    	
        if (this.logicOp.equals("or" )) { this.logic = OR;   }
        if (this.logicOp.equals("and")) { this.logic = AND;  }
        if (this.logicOp.equals("not")) { this.logic = NOT;  }

        return (this.logic != UNK);
    }


    /**
     * <p> Generate code equivalent of a logic test </p>
     * @return String with automatically generated code
     */
    
    @Override
    public CodeAndVarNames genCode() {
        CodeAndVarNames cvn = new CodeAndVarNames();
        Iterator<Signal> inputSig = inputs.iterator();
        Signal outputSig = this.getOutput();
        
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (!outputSig.isDerived()) {
            cvn.appendCode(indent() + outVarID + " = ");
            cvn.addVarName(outVarID);
        }
        
        if (inputs == null) {
            cvn.appendCode(errorComment(
                    "in BlockMathLogic genCode(): encountered null input list."));
            return cvn;
        }
        
        if (inputs.size() < 1) {
            cvn.appendCode(errorComment(
                    "in BlockMathLogic genCode(): encountered input list" +
                    " with no input elements."));
            return cvn;
        }
        if ((inputs.size() > 1) && (this.logic == NOT))  {
            cvn.appendCode(errorComment(
                    "in BlockMathLogic genCode(): encountered input list" +
                    " with more than one elements for NOT operator."));
            return cvn;
        }

        int dialect = ourModel.getCodeDialect();
        Signal arg1 = inputSig.next();

	// Generate "!( arg1 )"
	
	if (logic == NOT) {
	    switch(dialect) {
	    case Model.DT_ANSI_C:
		cvn.appendCode(" !"); break;
	    case Model.DT_FORTRAN:
		cvn.appendCode(" .NOT."); break;
	    }
	    cvn.append(arg1.genCode());
	    
	} else {

	    // Generate "( arg 1 ) || ( arg 2 ) [ || ( arg 3 ) ] "
	    // or use && instead of || if AND
	    cvn.append(arg1.genCode());
	    while (inputSig.hasNext()) {
		switch(dialect) {
		case Model.DT_ANSI_C:
		    switch (logic) {
		    case OR:  cvn.appendCode(" || " ); break;
		    case AND: cvn.appendCode(" && "); break;
		    }
		    break;
		case Model.DT_FORTRAN:
		    switch (logic) {
		    case OR:  cvn.appendCode(" .OR. "); break;
		    case AND: cvn.appendCode(" .AND. "); break;
		    }
		}
		cvn.append(inputSig.next().genCode());
            }
        }
        
        // if not derived, need new line
        if (!outputSig.isDerived()) {
            cvn.appendCode(this.endLine());
        }
        return cvn;
    }


    /**
     *
     * <p> Generates description of self </p>
     *
     * @throws IOException
     **/

    @Override
    public void describeSelf(Writer writer) throws IOException
    {
        super.describeSelf(writer);
        writer.write(" and is a Logical Operator math block.");
    }

    /**
     * Sets logic operator from String
     * @param logicString String with desired operator
     * @throws DAVEException 
     */
    
    public void setLogicOp( String logicString ) throws DAVEException {
    	this.logicOp = logicString.toLowerCase();
    	if (!this.validateLogic()) {
    		throw new DAVEException("Unrecognized logic string: " + logicString );
    	}
    }

    /**
     *
     * Returns logic value
     *
     * @return boolean result
     **/

    public boolean getBoolValue() {
	return this.value != 0.0;
    }
	    

    /**
     *
     * Returns logic operator as String
     *
     * @return String with char representation of logic operator
     **/

    public String getLogicOp() { return this.logicOp; }


    /**
     *
     * <p> Implements update() method </p>
     * @throws DAVEException
     *
     **/

    @Override
    public void update() throws DAVEException
    {
        int numInputs;
        Iterator<Signal> theInputs;
        Signal theInput;
        boolean[] theInputValue;
        int index = 0;

        boolean verbose = this.isVerbose();

        if (verbose) {
            System.out.println();
            System.out.println("Entering update method for function '" + this.getName() + "'");
        }

        numInputs = this.inputs.size();

        // allocate memory for the input values
        theInputValue = new boolean[numInputs];
        
	switch (logic) {
	case NOT:
	    if (numInputs != 1) {
		throw new DAVEException("Number of inputs to NOT logical block '" + this.getName() +
					"' wrong - should be only one, found " + numInputs + ".");
	    }
	    break;
	case AND:
	case OR:
	    if (numInputs < 2) {
		throw new DAVEException("Number of inputs to n-ary logical block '" + this.getName() +
					"' wrong - should be more than one, found " + numInputs + ".");
	    }
	    break;
	}

        // see if each input variable is ready
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
                theInputValue[index] = theInput.sourceValue() != 0.0;
            }
            index++;
        }

        // Calculate our output
        boolean boolValue = theInputValue[0];
	if (logic == NOT) {
	    boolValue = !theInputValue[0];
	} else {
	    for(int i = 1; i < numInputs; i++) {
                if (logic == AND) {
                    boolValue = boolValue && theInputValue[i];
                }
                if (logic == OR) {
                    boolValue = boolValue || theInputValue[i];
                }
            }
	}
        this.value = boolValue ? 1.0 : 0.0;
        // record current cycle counter
        resultsCycleCount = ourModel.getCycleCounter();
    }
}
