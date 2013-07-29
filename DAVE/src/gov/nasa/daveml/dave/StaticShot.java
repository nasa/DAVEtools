// StaticShot
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
 * Serves as wrapper for static trim shots
 *
 * @author 040202 Bruce Jackson <mailto:bruce.jackson@nasa.gov>
 *
 **/

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * 
 * A class to bundle input and output vectors for checkcases found in DAVE-ML files
 * 
 * @since DAVE_tools 0.4
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * <p> 040202: Written, EBJ
 *
 **/

public class StaticShot
{

    /**
     * the name of this static shot
     */

    String scenario;

    /**
     * list of input signal names, in block order
     */

    VectorInfoArrayList inputs;

    /**
     * list of output signal names, in order
     */

    VectorInfoArrayList outputs;
    
 
    /** 
     *
     * Constructor to built from JDOM Elements
     *
     **/

    @SuppressWarnings("unchecked")
        public StaticShot( Element staticShot )
    {
        this.scenario = staticShot.getAttributeValue("name");
        //      System.out.println("Found static shot named '" + this.scenario + "'");

        // Fetch default namespace
        Namespace ss_ns = staticShot.getNamespace();

        // parse input signals
        Element checkInputs = staticShot.getChild("checkInputs",ss_ns);
        if (checkInputs == null) {
            System.err.println("Error reading checkcase data.");
            System.exit(0);
        }
        List<Element> theInputs = checkInputs.getChildren("signal",ss_ns);
        //      System.out.println("found " + theInputs.size() + " inputs.");
        this.inputs = new VectorInfoArrayList( theInputs.size() );
        Iterator<Element> itin = theInputs.iterator();
        while (itin.hasNext()) {
            //      System.out.print("+");
            Element theSignal = itin.next();
            // units may be missing if non-dimensional
            Element theUnitsElement = theSignal.getChild("signalUnits",ss_ns);
            String theUnits = "";
            if (theUnitsElement != null) {
                theUnits = theUnitsElement.getTextTrim();
            }
            Element theSignalName = theSignal.getChild("signalName",ss_ns);
            if (theSignalName == null) {
                System.err.println("Error reading checkcase data: no signal name found.");
                System.exit(0);
            }
            VectorInfo signal = new VectorInfo( theSignalName.getTextTrim(),
                                                theUnits, null, true);
            Element theSignalValue = theSignal.getChild("signalValue",ss_ns);
            if (theSignalValue == null) {
                System.err.println("Error reading checkcase data: no signal value found for signal '" 
                                   + theSignalName.getTextTrim() + "'.");
                System.exit(0);
            }
            signal.setValue( theSignal.getChild("signalValue",ss_ns).getTextTrim() );
            this.inputs.add(signal);
        }           

        // parse output values
        Element checkOutputs = staticShot.getChild("checkOutputs",ss_ns);
        if (checkInputs == null) {
            System.err.println("Error reading checkcase data.");
            System.exit(0);
        }
        List theOutputs = checkOutputs.getChildren("signal",ss_ns);
        //      System.out.println("found " + theOutputs.size() + " outputs.");
        this.outputs = new VectorInfoArrayList( theOutputs.size() );
        int outCount = 0;
        Iterator itout = theOutputs.iterator();
        while (itout.hasNext()) {
            outCount++;
            Element theSignal = (Element) itout.next();
            try {
                VectorInfo signal = new VectorInfo( theSignal.getChild("signalName",ss_ns).getTextTrim(),
                                                    null, null, false);
                signal.setValue(     theSignal.getChild("signalValue",ss_ns).getTextTrim() );
                signal.setTolerance( theSignal.getChild("tol",ss_ns).getTextTrim() );
                Element units =      theSignal.getChild("signalUnits",ss_ns);
                if (units == null) {
                    signal.setUnits("");
                } else {
                    signal.setUnits( units.getTextTrim() );
                }
                this.outputs.add(signal);
            } catch (NullPointerException e) {
                System.err.println("Warning: can't find element 'signalName' in 'checkOutputs' element number "
                                   + outCount + " in scenario '" + this.scenario + "'; check capitalization.");
            }
            //      System.out.print("+");
        }
    }


    /**
     *
     * Returns name of scenario associated with this shot
     *
     **/

    public String getName() { return this.scenario; }


    /**
     *
     * Returns input vector information
     *
     **/

    public VectorInfoArrayList getInputs() { return this.inputs; }


    /**
     *
     * Returns output vector information
     *
     **/

    public VectorInfoArrayList getOutputs() { return this.outputs; }


    /**
     *
     * Load the supplied input vector while verifying units
     *
     * @param inVec input vector from model
     *
     * @throws DAVEException if names, numbers, or units don't match
     **/

    public void loadInputVector( VectorInfoArrayList inVec )
        throws DAVEException
    {

        VectorInfo modelSig = null;
        VectorInfo checkSig;

        // compare vector lengths
        if (inVec.size() != this.inputs.size()) {
            throw new DAVEException("Input vector length for checkcase '" + this.getName()
                                    + "' (" + this.inputs.size() 
                                    + ") doesn't match length of model's input vector ("
                                    + inVec.size() + ").");
        }

        // for each checkcase signal, find matching signal name from model
        Iterator<VectorInfo> checkIt = this.inputs.iterator();

        // loop for each checkcase signal
        while (checkIt.hasNext()) {
            checkSig = checkIt.next();
            String sigName = checkSig.getName();

            // find matching entry in model's vector
            boolean matched = false;
            Iterator<VectorInfo> modelIt = inVec.iterator();
            while (!matched) {
                if (!modelIt.hasNext()) {
                    throw new DAVEException("Could not find input signal '" + sigName 
                                            + "' in model's inputs.");
                }
                modelSig = modelIt.next();
                if(checkSig.getName().equals( modelSig.getName())) {
                    matched = true;
                }
            }

            // here with matching name - compare units
            if (!checkSig.getUnits().equals( modelSig.getUnits())) {
                throw new DAVEException("Mismatched units - in checkcase '" + this.getName()
                                        + "' for input signal '" + sigName + "', checkcase specifies '"
                                        + checkSig.getUnits() + "' but model expects '"
                                        + modelSig.getUnits() + "'.");
            }
            // make sure it's an input
            if (!modelSig.isInput()) {
                throw new DAVEException("Found non-input signal '" + modelSig.getName() 
                                        + "' in model's input vector.");
            }
            // set value
            modelSig.setValue( checkSig.getValue() );
        }
    }

    
    /**
     * Wrapper which directs error messages to standard out
     */
    
    public boolean checkOutputs( VectorInfoArrayList outVec )
    	throws DAVEException {
    	return checkOutputs( outVec, System.out );
    }


    /**
     *
     * Compares the supplied outputs to see if within tolerances
     *
     * @param outVec output vector from model
     *
     * @throws DAVEException if vector length or names don't match
     *
     **/

    public boolean checkOutputs( VectorInfoArrayList outVec, PrintStream out )
        throws DAVEException
    {
        boolean comparison  = true;
        VectorInfo modelSig = null;
        VectorInfo checkSig;

        // compare vector lengths
        if (outVec.size() < this.outputs.size()) {
            throw new DAVEException("Output vector length for checkcase '" + this.getName()
                                    + "' (" + this.outputs.size() 
                                    + ") is bigger than the length of model's output vector ("
                                    + outVec.size() + ").");
        }
        // for each checkcase signal, find matching signal name from model
        Iterator<VectorInfo> checkIt = this.outputs.iterator();

        // loop for each checkcase signal

        while (checkIt.hasNext()) {
            checkSig = checkIt.next();
            String sigName = checkSig.getName();

            // find matching entry in model's vector
            boolean matched = false;
            Iterator<VectorInfo> modelIt = outVec.iterator();
            while (!matched) {
                if (!modelIt.hasNext()) {
                    throw new DAVEException("Could not find output signal '" + 
                            sigName + "' in model's outputs.");
                }
                modelSig = modelIt.next();
                if(checkSig.getName().equals( modelSig.getName())) {
                    matched = true;
                }
            }

            // here with matching name - compare units
            if (!checkSig.getUnits().equals( modelSig.getUnits())) {
                throw new DAVEException("Mismatched units - in checkcase '" + this.getName()
                                        + "' for output signal '" + sigName + "', checkcase specifies '"
                                        + checkSig.getUnits() + "' but model expects '"
                                        + modelSig.getUnits() + "'.");
            }
            // make sure it's an output
            if (modelSig.isInput()) {
                throw new DAVEException("Found input signal '" + modelSig.getName() 
                                        + "' in model's output vector.");
            }
            // compare values; see if within tolerance
            double actual   = modelSig.getValue();
            double expected = checkSig.getValue();
            // first make sure neither actual or expect is Not-A-Number
            if (Double.isNaN(actual)) {
                if (!Double.isNaN(expected)) {
                    comparison = false;
                    out.println(); // blank line
                    out.println("For output '" + sigName +
                        "': encountered unexpected not-a-number (NaN) value.");
                }
            }
            if (Double.isNaN(expected)) {
                if (!Double.isNaN(actual)) {
                    comparison = false;
                    out.println(); // blank line
                    out.println("For output '" + sigName +
                        "': expected not-a-number (NaN) value but found "
                        + actual + ".");
                }
            }
            if (Double.isNaN(expected) && Double.isNaN(actual)) {
                // good to go
            } else {
                double diff = Math.abs(actual - expected);
                double tol = Math.abs(checkSig.getTolerance());
                if (diff > tol) {
                    comparison = false;
                    out.println(); // blank line
                    out.println("For output '" + sigName + "': expected " + expected);
                    out.println(" but found " + actual + "; difference is " + diff);
                    out.println(" which is greater than allowed tolerance of " + tol + ".");
                }
            }
        }
        return comparison;
    }
}

