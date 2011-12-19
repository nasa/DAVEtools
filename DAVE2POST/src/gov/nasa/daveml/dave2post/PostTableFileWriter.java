/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.daveml.dave2post;

import gov.nasa.daveml.dave.*;
import java.util.ArrayList;


/**
 *
 * @author ebjackso
 */
class PostTableFileWriter {
    
    int tableNumber;
    Model ourModel;

    public PostTableFileWriter(Model theModel, String tableFileName) {
        tableNumber = 0;
        ourModel = theModel;
    }

    void generateTableDescription(BlockFuncTable bft) {
        String outVarID = bft.getOutputVarID();

        FuncTable ft = bft.getFunctionTableDef();
        int[] dims = ft.getDimensions();
        int numDims = dims.length;
        String gtID = ft.getGTID();
        System.out.println("C  Table" + "'" + gtID + "', dim = " + numDims + ".");
        if (numDims == 1) {
            String bpID = ft.getBPID(numDims);
            String inVarID = bft.getVarID(numDims);
            System.out.print("  " + outVarID + " = " + outVarID + ", monovar, " + inVarID );
            System.out.println(", " + tableNumber + ", lin_inp, noxt");
            tableNumber++;
            BreakpointSet bpset = ourModel.getBPSetByID(bpID);
            this.generate1DTable(bpset.values(), ft.getValues());
        } else { /*  multi-dimensional table */
            // generate first layer of multitable
            String bpID = ft.getBPID(1);
            String inVarID = bft.getVarID(1);
            System.out.print("  " + outVarID + " = " + outVarID + ", multi, " + inVarID );
            System.out.println(", " + tableNumber + ", lin_inp, noxt");
            tableNumber++;
            BreakpointSet bpset = ourModel.getBPSetByID(bpID);
            this.generate1DTableNumbers(bpset.values(), tableNumber);
        }
    }
    
    void generate1DTable(ArrayList<Double> bps, ArrayList<Double> vals) {
        if (bps.size() != vals.size()) {
            System.out.println("ERROR - table size of 1D table doesn't match number of breakpoints");
        } else {
            for (int i = 0; i < bps.size(); i++) {
                System.out.println("         " + bps.get(i) + ", " + vals.get(i));
            }
        }
    }
    
        void generate1DTableNumbers(ArrayList<Double> bps, int startTableNumber) {
            int tabNum = startTableNumber;
            for (int i = 0; i < bps.size(); i++) {
                System.out.println("         " + bps.get(i) + ", " + tabNum);
                tabNum++;
            }
    }

    
}
