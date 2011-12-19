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
            this.generate1DTable(bpset.values(), ft.getValues(), 0);
        } else { /*  multi-dimensional table */
            // generate first layer of multitable
            int dim = 1;
            String bpID = ft.getBPID(dim);
            String inVarID = bft.getVarID(dim);
            System.out.print("  " + outVarID + " = " + outVarID + ", multi, " + inVarID );
            System.out.println(", " + tableNumber + ", lin_inp, noxt");
            tableNumber++;
            BreakpointSet bpset = ourModel.getBPSetByID(bpID);
            this.generate1DTableNumbers(bpset.values(), tableNumber);
            
            // generate subtables
            int offset = 0;
            
            dim++;
            for (int i=1; i<=dims[dim-2]; i++) {
                offset = this.generateSubTables(dims, dim, bft, offset);
            }
        }
    }
    
    private void generate1DTable(ArrayList<Double> bps, ArrayList<Double> vals, int valOffset) {
        if (bps.size() > (vals.size() - valOffset)) {
            System.out.println("ERROR - number of remaining values in table " + 
                    (vals.size() - valOffset) + 
                    " is less than the number of breakpoints" +
                    bps.size() );
        } else {
            for (int i = 0; i < bps.size(); i++) {
                System.out.println("         " + bps.get(i) + ", " + vals.get(i + valOffset));
            }
        }
    }
    
    private void generate1DTableNumbers(ArrayList<Double> bps, int startTableNumber) {
        int tabNum = startTableNumber;
        for (int i = 0; i < bps.size(); i++) {
            System.out.println("         " + bps.get(i) + ", " + tabNum);
            tabNum++;
        }
    }

    // returns number of points written
    private int generateSubTables(int[] dims, int dim, BlockFuncTable bft, int valOffset) {
        FuncTable ft    = bft.getFunctionTableDef();
        String outVarID = bft.getOutputVarID();
        String inVarID  = bft.getVarID(dim);
        String bpID     = ft.getBPID(dim);
        System.out.print("  " + outVarID + " = " + outVarID + ", multisub, " + inVarID );
        System.out.println(", " + tableNumber + ", lin_inp, noxt");
        tableNumber++;
        BreakpointSet bpset = ourModel.getBPSetByID(bpID);
        this.generate1DTable(bpset.values(), ft.getValues(), valOffset);
        return bpset.values().size();
    }
    


    
}
