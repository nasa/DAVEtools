/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nasa.daveml.sweeper;

import gov.nasa.daveml.dave.VectorInfo;

/**
 * Wrapper class to add nominal, max and min values to model inputs.
 * @author Bruce Jackson, NASA Langley Research Center, bruce.jackson@nasa.gov
 */
public class InputInfo {

    /**
     * The original, encapsulated DAVE VectorInfo, object
     */
    VectorInfo orig;

    /**
     * Current minimum of sweep range for this signal
     */
    double min;

    /**
     * Current maximum of sweep range for this signal
     */
    double max;

    /**
     * Current default value for signal while not being swept
     */
    double nominal;

    /**
     * Constructor for InputInfo object, which encapsulates the original DAVE
     * VectorInfo about an input signal; adds min, max and nominal value
     * attributes
     *
     * @param aVectorInfo The initial DAVE-ML input signal information object
     */
    public InputInfo( VectorInfo aVectorInfo ) {
        orig = aVectorInfo;
        min = 0.;
        max = 0.;
        nominal = 0.;
    }

    /**
     * Returns the name of the encapsulated input signal
     * @return name of input signal
     */
    public String getName() {
        return orig.getName();
    }

    /**
     * Returns the units-of-measure of the encapsulated input signal
     * @return units of the input signal
     */
    public String getUnits() {
        return orig.getUnits();
    }

    /**
     * Returns the current minimum value for the signal
     * @return minimum value of signal
     */
    double getMin() {
//        System.out.println("Returning min value of " + min + " for " + orig.getName());
        return min;
    }

    /**
     * Returns the current maximum value for the signal
     * @return maximum value of the signal
     */
    double getMax() {
//        System.out.println("Returning max value of " + max + " for " + orig.getName());
        return max;
    }

    /**
     * Returns the nominal value for the signal
     * @return nominal value of the signal
     */
    double getNominal() {
        return nominal;
    }

    /**
     * Sets a new minimum value for an input signal
     * @param newMin new minimum value for signal
     */
    void setMin(double newMin) {
        min = newMin;
//        System.out.println("Set new min value (" + newMin + ") for " + orig.getName());
    }

    /**
     * Sets the new maximum value for an input signal
     * @param newMax new maximum value for the signal
     */
    void setMax(double newMax) {
        max = newMax;
//        System.out.println("Set new max value (" + newMax + ") for " + orig.getName());
    }

    /**
     * Adjusts the minimum or maximum value of the signal so that the range
     * includes the provided signal
     * @param newVal Value to use to adjust the signal range, if necessary
     */
    void setMinMax( double newVal ) {
        if (newVal > max) {
//            System.out.println("Set new max value (" + newVal + ") for " + orig.getName());
            max = newVal;
        }
        if (newVal < min) {
//            System.out.println("Set new min value (" + newVal + ") for " + orig.getName());
            min = newVal;
        }
    }

    /**
     * Sets the new nominal value for an input signal
     * @param newNominal the new nominal value for the signal
     */
    void setNominal( double newNominal ) {
        nominal = newNominal;
    }

    /**
     * Sets the new nominal value of an input signal to be the midrange between
     * current min and max values
     */
    void setNominal() {
        // if no argument set to halfway between min and max
        nominal = 0.5*(min + max);
    }
}
