// BlockMathSwitch
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
 * <p> Switch element math function block </p>
 * <p> 031211 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 **/
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Iterator;

/**
 *
 * <p>  This Math block represents a switch function </p>
 *
 **/
public class BlockMathSwitch extends BlockMath {

    /**
     *
     * <p> Constructor for switch Block <p>
     *
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing "apply" element
     * @param m         The parent <code>Model</code>
     *
     **/
    @SuppressWarnings("unchecked")
    public BlockMathSwitch(Element applyElement, Model m) {
        // Initialize superblock elements
        super("pending", "switch", m);

        // Parse parts of the Apply element
        Namespace mathml = Namespace.getNamespace("", "http://www.w3.org/1998/Math/MathML");
        Element piecewise = applyElement.getChild("piecewise", mathml);

        // second chance if not qualified
        if (piecewise == null) {
            piecewise = applyElement.getChild("piecewise");
        }

        // take appropriate action based on type
        if (piecewise == null) {
            List<Element> elist = applyElement.getChildren();
            Iterator<Element> elisti = elist.iterator();
            Element el = elisti.next();
            System.err.println("Error - BlockMathSwitch constructor called with"
                    + " wrong type element. Expected <piecewise>, found <"
                    + el.getQualifiedName() + ">.");
        } else {
            // set name of ourself - will be "uniquified" later
            this.setName("switch" + "_" + m.getNumBlocks());

            // look for two children: <piece> and <otherwise>
            List<Element> kids = piecewise.getChildren();
            if (kids.size() < 2) {
                System.err.println("Error - expected at least two child elements (one <piece> and one <otherwise> element); found only " + kids.size()
                        + " elements total.");
            } else {
                // Get the children that describe switch
                List<Element> pieces = piecewise.getChildren("piece", mathml);
                Element otherwise = piecewise.getChild("otherwise", mathml);

                // check they are not null
                if (pieces.isEmpty()) {
                    pieces = piecewise.getChildren("piece");  // 2nd chance
                }
                if (pieces.isEmpty()) {
                    System.err.println("Error - <piecewise> element contained no <piece> elements");
                    return;
                }
                if (otherwise == null) {
                    otherwise = piecewise.getChild("otherwise"); // 2nd chance
                }
                if (otherwise == null) {
                    System.err.println("Error - <piecewise> element contained no <otherwise> element");
                    return;
                }

                // look at first piece
                Element piece = pieces.get(0);
                List<Element> pieceChildren = piece.getChildren();
                if (pieceChildren.size() != 2) {
                    System.err.println("Error - <piece> element can only have two children - found "
                            + pieces.size() + " instead.");
                    return;
                }
                List<Element> other = otherwise.getChildren();
                if (other.size() != 1) {
                    System.err.println("Error - <otherwise> element needs one child - found "
                            + other.size() + " instead.");
                    return;
                }
                // process like:                  this.genInputsFromApply(ikid);
                // piece has two children: output 1 and condition it is used, and
                // otherwise has default condition.
                // Input 1 is passed if condition is true
                this.genInputsFromApply(pieceChildren.iterator(), 1);        // input 1 & condition (input 2)

                if (pieces.size() <= 1) {
                    // if last piece, then input 3 is the <otherwise> child
                    this.genInputsFromApply(other.iterator(), 3); // input 3 (otherwise)
                } else {
                    // if not, then recurse after removing the first <piece> element
                    boolean childRemoved = this.removeFirstPiece(piecewise);
                    if (!childRemoved)
                        throw new AssertionError(
                            "Unable to remove first child from <piecewise> "
                                + "while trying to build an upstream switch for " + this.getName());
                    BlockMathSwitch bms = new BlockMathSwitch(applyElement, m);
                    this.addInput(bms, 3);
                }
            }
        }

        //System.out.println("    BlockMathSwitch constructor: " + myName + " created.");
    }

    /**
     *
     * <p> Generates description of self </p>
     *
     * @throws <code>IOException</code>
     **/
    public void describeSelf(Writer writer) throws IOException {
        super.describeSelf(writer);
        writer.write(" and is a Switch math block.");
    }

    /**
     *
     * <p> Implements update() method </p>
     * <p> Passes input 0 if input 1 > 0.; otherwise passes input 2 to output </p>
     * @throws DAVEException
     *
     **/
    public void update() throws DAVEException {
        int numInputs;
        Iterator<Signal> theInputs;
        Signal theInput;
        double[] theInputValue;
        int index = 0;
        int requiredInputs = 3;

        boolean verbose = this.isVerbose();

        if (verbose) {
            System.out.println();
            System.out.println("Entering update method for switch '" + this.getName() + "'");
        }

        // sanity check to see if we have exact number of inputs
        numInputs = this.inputs.size();
        if (numInputs != requiredInputs) {
            throw new DAVEException("Number of inputs to '" + this.getName()
                    + "' wrong - should be " + requiredInputs + ".");
        }

        // allocate memory for the input values
        theInputValue = new double[requiredInputs];

        // see if each input variable is ready
        theInputs = this.inputs.iterator();

        while (theInputs.hasNext()) {
            theInput = theInputs.next();
            if (!theInput.sourceReady()) {
                if (verbose) {
                    System.out.println(" Upstream signal '" + theInput.getName() + "' is not ready.");
                }
                return;
            } else {
                theInputValue[index] = theInput.sourceValue();
            }
            index++;
        }

        // Calculate our output

        this.value = theInputValue[2];
        if (Math.abs(theInputValue[1]) > 0.0001) // choose input 3 if input 2 non-zero
        {
            this.value = theInputValue[0];
        }

        // record current cycle counter
        resultsCycleCount = ourModel.getCycleCounter();
    }

    private boolean removeFirstPiece(Element piecewise) {
        List kids = piecewise.getChildren();
        Iterator it = kids.iterator();
        boolean foundPiece = false;
        boolean removedPiece = false;
        while ((!foundPiece) && (it.hasNext())) {
            Element el = (Element) it.next();
            foundPiece = (el.getName() == null ? "piece" == null : el.getName().equals("piece"));
        }
        if (foundPiece) {
            it.remove();
        }
        return foundPiece;
    }
}
