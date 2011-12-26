/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.daveml.dave2post;

import gov.nasa.daveml.dave.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author ebjackso
 */
class PostTableFileWriter extends FileWriter {
    
    int tableNumber, tableRefNumber;
    Model ourModel;

    public PostTableFileWriter(Model theModel, String tableFileName) throws IOException {
        super( tableFileName );
        ourModel = theModel;
    }
    
    /**
     *
     * Adds newline to the end of each write.
     *
     * @param cbuf String containing text to write
     *
     */

    public void writeln( String cbuf ) throws IOException
    {
//            int i;
//            for(i = 0; i< this.indent; i++)
//                    super.write(" ");
            super.write( cbuf + "\n" );
    }


    
    void generateTableDescription(BlockFuncTable bft) {
        tableNumber     = 0;  // restart table numbering
        tableRefNumber  = 1;  // restart table references (found in multi lines)
        String outVarID = bft.getOutputVarID();
        FuncTable ft    = bft.getFunctionTableDef();
        int[] dims      = ft.getDimensions();
        int numDims     = dims.length;
        String gtID     = ft.getGTID();
        
        try {
            writeln("C  Table" + "'" + gtID + "', dim = " + numDims + ".");
        } catch (IOException ex) {
            Logger.getLogger(PostTableFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (numDims == 1) {
             this.writeMonoTable(bft, 0);
        } else { /*  multi-dimensional table */
            int offset = 0;
            
            this.writeMultiTables(bft, 0); // recurses to write all table indices
            
            for( int i = 1; i < dims[numDims-2]; i++) {
                offset = this.writeMonoTable(bft, offset);
            }
        }
        
    }
    
    private void writeMultiTables(BlockFuncTable bft, int dimLevel) {
        // on entry, dimLevel is offset into dims array (starts at 0)
        FuncTable ft    = bft.getFunctionTableDef();
        String outVarID = bft.getOutputVarID();
        int[] dims      = ft.getDimensions();
        int numDims     = dims.length;
        if (dimLevel >= (numDims-1)) { // last level is written using writeMonoTable
            return;
        } else {
            String inVarID  = bft.getVarID(dimLevel+1); // convert to port (1-based) number
            String bpID     = ft.getBPID(dimLevel+1);
            ArrayList<Double> bps  = ourModel.getBPSetByID(bpID).values();
            Iterator<Double> bpIt  = bps.iterator();
            
            try {
                writeln(outVarID + " = " + outVarID + ", multi, " + inVarID + ", " + tableNumber++ + ", lin_inp, noxt,");
            } catch (IOException ex) {
                Logger.getLogger(PostTableFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            while (bpIt.hasNext()) {
                double breakpointVal = bpIt.next();
                try {
                    writeln("     " + breakpointVal + ", " + tableRefNumber++ + ",");
                } catch (IOException ex) {
                    Logger.getLogger(PostTableFileWriter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            writeMultiTables(bft, dimLevel+1);
        }
    }

    // writes table header and data points for last dimension
    private int writeMonoTable(BlockFuncTable bft, int offset) 
    {
        FuncTable ft    = bft.getFunctionTableDef();
        String outVarID = bft.getOutputVarID();
        int[] dims      = ft.getDimensions();
        int numDims     = dims.length;
        
        String bpID     = ft.getBPID(numDims);
        String inVarID  = bft.getVarID(numDims);
        
        ArrayList<Double> bps  = ourModel.getBPSetByID(bpID).values();
        ArrayList<Double> vals = ft.getValues();
        try {
            write("  " + outVarID + " = " + outVarID + ", monovar, " + inVarID );
            writeln(", " + tableNumber + ", lin_inp, noxt,");
        } catch (IOException ex) {
            Logger.getLogger(PostTableFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

        offset = this.generate1DTable(bps, vals, offset);
        
        tableNumber++;

        return offset;
    }



    private int generate1DTable(ArrayList<Double> bps, ArrayList<Double> vals, int valOffset) {
        int i = 0;
        if (bps.size() > (vals.size() - valOffset)) {
            System.err.println("ERROR - number of remaining values in table " + 
                    (vals.size() - valOffset) + 
                    " is less than the number of breakpoints" +
                    bps.size() );
        } else {
            for (i = 0; i < bps.size(); i++) {
                try {
                    writeln("         " + bps.get(i) + ", " + vals.get(i + valOffset) + ",");
                } catch (IOException ex) {
                    Logger.getLogger(PostTableFileWriter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return valOffset + i;
    }
    
//    private void generate1DTableNumbers(ArrayList<Double> bps, int startTableNumber) {
//        int tabNum = startTableNumber;
//        for (int i = 0; i < bps.size(); i++) {
//            System.out.println("         " + bps.get(i) + ", " + tabNum);
//            tabNum++;
//        }
//    }
//
//    // returns number of points written
//    private int generateSubTables(int[] dims, int dim, BlockFuncTable bft, int valOffset) {
//        FuncTable ft    = bft.getFunctionTableDef();
//        String outVarID = bft.getOutputVarID();
//        String inVarID  = bft.getVarID(dim);
//        String bpID     = ft.getBPID(dim);
//        System.out.print("  " + outVarID + " = " + outVarID + ", multisub, " + inVarID );
//        System.out.println(", " + tableNumber + ", lin_inp, noxt");
//        tableNumber++;
//        BreakpointSet bpset = ourModel.getBPSetByID(bpID);
//        this.generate1DTable(bpset.values(), ft.getValues(), valOffset);
//        return bpset.values().size();
//    }
//    


    
}
