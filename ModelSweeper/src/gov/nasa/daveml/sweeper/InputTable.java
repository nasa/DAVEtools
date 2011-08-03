/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nasa.daveml.sweeper;

import gov.nasa.daveml.dave.CheckData;
import gov.nasa.daveml.dave.DAVE;
import gov.nasa.daveml.dave.DAVEException;
import gov.nasa.daveml.dave.Model;
import gov.nasa.daveml.dave.StaticShot;
import gov.nasa.daveml.dave.VectorInfo;
import gov.nasa.daveml.dave.VectorInfoArrayList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bjax
 */
public class InputTable extends javax.swing.table.AbstractTableModel{

    /**
     * Serialization unique ID
     */

    private static final long serialVersionUID = -40L;

    /**
     * Our model
     */

    Model m;

    /**
     * Array List of input signals (of InputInfo type)
     */
    ArrayList<InputInfo> array;

    /**
     * Parent JFrame we are displayed in
     */

    InputTableJFrame parentJFrame;

    /**
     * ModelSweeper main class we belong to
     */

    ModelSweeperUI uiClass;

    /**
     * Constructor that builds vector of input information for model encoded
     * in the provided DAVE-ML file
     *
     * @param d the input DAVE-ML file (after parsing)
     */
    public InputTable(DAVE d, ModelSweeperUI mainClass) {

        parentJFrame = null;
        uiClass = mainClass;
        InputInfo modelInput;

        // collect information about each input in model

        VectorInfoArrayList vec = null;
        m = d.getModel();
        if (m != null) {
            try {
                vec = m.getInputVector();
            } catch (DAVEException ex) {
                Logger.getLogger(InputTableJFrame.class.getName()).
                        log(Level.SEVERE, null, ex);
                vec = null;
            }
        }

        if (vec == null) {
            System.exit(0);
        }

        // add in elements of array
        array = new ArrayList<InputInfo>(vec.size());
        ListIterator it = vec.listIterator();
        int index = 0;
        while (it.hasNext()) {
            VectorInfo signal = (VectorInfo) it.next();
            array.add(new InputInfo(signal));
        }

        // then, if any checkcases are present, scan them
        // to find checkcase min & max

        if (d.hasCheckcases()) {
            setMaxMinToInf(); // set min to +Inf and max to -Inf
            CheckData cd = d.getCheckcaseData();
            ArrayList<StaticShot> ssArray = cd.getStaticShots();
            Iterator ssIt = ssArray.iterator();

            // cycle through the checkcases
            while (ssIt.hasNext()) {
                StaticShot ss = (StaticShot) ssIt.next();
                VectorInfoArrayList ssInputVectorInfo = ss.getInputs();
                Iterator inputIt = ssInputVectorInfo.iterator();

                // cycle through the signals in the checkcase
                while (inputIt.hasNext()) {
                    VectorInfo ccSignal = (VectorInfo) inputIt.next();
                    String inputName = ccSignal.getName();
                    double inputValue = ccSignal.getValue();
                    // now try to find a matching
                    boolean matched = false;
                    Iterator sigIt = array.iterator();

                    //  cycle through the model inputs to find
                    modelInput = null;
                    while (sigIt.hasNext()) {
                        modelInput = (InputInfo) sigIt.next();
                        if (modelInput.getName() == null ? inputName == null :
                            modelInput.getName().equals(inputName)) {
                            matched = true;
                            break;
                        }
                    }
                    if (matched) {
                        modelInput.setMinMax(inputValue); // adjust max/min
                    }
                }
            }

            // finally set default to halfway of min-max (only if checkcases
            // are present)
            Iterator sigIt = array.iterator();
            while (sigIt.hasNext()) {
                modelInput = (InputInfo) sigIt.next();
                modelInput.setNominal(); // set default to half
            }
        }
    }

    /**
     * Sets min and max fields to +Inf and -Inf, respectively
     * in anticipation of obtaining these from the checkcase data
     */
    private void setMaxMinToInf() {
        Iterator it = array.iterator();
        while (it.hasNext()) {
            InputInfo input = (InputInfo) it.next();
            input.setMax(Double.NEGATIVE_INFINITY);
            input.setMin(Double.POSITIVE_INFINITY);
        }
    }

    /**
     * Returns the minimum value to sweep for a selected input.
     *
     * The default minimum value is set to the smallest such value encountered
     * in any included checkcase. The user may have interactively set another
     * minimum value.
     *
     * If no checkcases were included and the user has not provided another
     * value, the initial default value is 0 which may cause div-by-zero errors.
     *
     * @param offset 0-based input selector (offset from first entry in array)
     * @return minimum value, either the default or (previously) user-specified
     */
    public double getMinVal(int offset) {
        InputInfo input = array.get(offset);
        return input.getMin();
    }

    /**
     * Returns the maximum value to sweep for a selected input.
     *
     * The default maximum value is set to the largest such value encountered in
     * any included checkcase. The user may have interactively set another
     * maximum value.
     *
     * If no checkcases were included and the user has not provided another
     * value, the initial default value is 0 which may cause div-by-zero errors.
     *
     * @param offset 0-based input selector (offset from first entry in array)
     * @return maximum value, either from default or (previously) user-specified
     */
    public double getMaxVal(int offset) {
        InputInfo input = array.get(offset);
        return input.getMax();
    }

    /**
     * Returns the nominal value for a selected input to hold when sweeping
     * other inputs.
     *
     * The default nominal value is set to halfway between the
     * minimum and maximum values encountered in any included checkcase. The
     * user may have interactively specify a preferred nominal value.
     *
     * If no checkcases were included and the user has not provided another
     * value, the initial default values is 0 which may case div-by-zero errors.
     *
     * @param offset 0-based input selector (offset from first entry in array)
     * @return nominal value, either from default or (previously) user-specified
     */
    public double getNomVal(int offset) {
        InputInfo input = array.get(offset);
//        System.out.println("Returning nominal value of " +
//                            input.getNominal() + " for " + input.getName());
        return input.getNominal();
    }

    /**
     * Sets the minimum value to use when sweeping the selected input.
     *
     * The default minimum value is set to the smallest such value encountered
     * in any included checkcase. The user may have interactively set another
     * minimum value.
     *
     * If no checkcases were included and the user has not provided another
     * value, the initial default value is 0 which may cause div-by-zero errors.
     *
     * @param offset 0-based input selector (offset from first entry in array)
     * @param minVal new minimum value to record
     */
    public void setMinVal(int offset, double minVal) {
        InputInfo input = array.get(offset);
        input.setMin(minVal);
    }

    /**
     * Sets the maximum value to use when sweeping the selected input.
     *
     * The default maximum value is set to the largest such value encountered
     * in any included checkcase. The user may have interactively set another
     * maximum value.
     *
     * If no checkcases were included and the user has not provided another
     * value, the initial default value is 0 which may cause div-by-zero errors.
     *
     * @param offset 0-based input selector (offset from first entry in array)
     * @param maxVal new maximum value to record
     */
    public void setMaxVal(int offset, double maxVal) {
        InputInfo input = array.get(offset);
        input.setMax(maxVal);
    }

    /**
     * Sets the nominal value to hold when performing sweeps with other inputs.
     *
     * The default nominal value is set to halfway between the minimum and
     * maximum values encountered for this input in any included checkcase.
     * The user may have interactively set another nominal value.
     *
     * If no checkcases were included and the user has not provided another
     * value, the initial default value is 0 which may cause div-by-zero errors.
     *
     * @param offset 0-based input selector (offset from first entry in array)
     * @param nomVal new nominal value to record
     */
    public void setNomVal(int offset, double nomVal) {
        InputInfo input = array.get(offset);
        input.setNominal(nomVal);
    }

    /**
     * Loads the provided input vector with nominal values for each input in the
     * model, in preparation for sweeping the model.
     *
     * Inputs being swept will be have their values modified later during the
     * sweep.
     *
     * @param inputVec an VectorInfoArrayList (model input vector) to be set to
     * nominal values.
     */

    public void loadNominalValues(VectorInfoArrayList inputVec) {
        // load nominal values into the input vector
        Iterator it = inputVec.iterator();
        int i = 0;
        while (it.hasNext()) {
            VectorInfo vi = (VectorInfo) it.next();
            vi.setValue(this.getNomVal(i));
            i++;
        }
    }

    public String getName( int offset ) {
        InputInfo input = array.get(offset);
        return input.getName();
    }

    public String getUnits( int offset ) {
        InputInfo input = array.get(offset);
        return input.getUnits();
    }

    public int getRowCount() {
        return array.size();
    }

    public int getColumnCount() {
        return 5;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex > 1)
            return true;
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        String answer = "";

        switch (columnIndex) {
            case 0:
                answer = getName( rowIndex );
                break;
            case 1:
                answer = getUnits( rowIndex );
                break;
            case 2:
                answer = new Double(getNomVal( rowIndex )).toString();
                break;
            case 3:
                answer = new Double(getMinVal( rowIndex )).toString();
                break;
            case 4:
                answer = new Double(getMaxVal( rowIndex )).toString();
                break;
        }
        return answer;
    }

    @Override
    public String getColumnName( int columnIndex ) {
        String answer = "";

        switch (columnIndex) {
            case 0:
                answer = "Signal Name";
                break;
            case 1:
                answer = "Units";
                break;
            case 2:
                answer = "Nominal Value";
                break;
            case 3:
                answer = "Minimum Value";
                break;
            case 4:
                answer = "Maximum Value";
                break;
        }
        return answer;
    }

    @Override
    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex) {
//        System.out.println("setValueAt called at (" + rowIndex + ", " + columnIndex + ")");
        double val = Double.parseDouble( (String) aValue );
        switch (columnIndex) {
            case 2:
                setNomVal( rowIndex, val );
                break;
            case 3:
                setMinVal( rowIndex, val );
                break;
            case 4:
                setMaxVal( rowIndex, val );
                break;
        }
        uiClass.updateInputInfo(rowIndex);
    }

    void setJFrame(InputTableJFrame inputTableJFrame) {
        parentJFrame = inputTableJFrame;
        parentJFrame.setModelName(m.getName());
    }

}
