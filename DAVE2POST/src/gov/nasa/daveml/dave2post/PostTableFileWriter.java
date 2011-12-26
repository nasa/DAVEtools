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
    String indent; // indent for table values

    public PostTableFileWriter(Model theModel, String tableFileName) throws IOException {
        super( tableFileName );
        ourModel = theModel;
        indent = "         ";
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
                write("  " + outVarID + "t = " + outVarID + "t, multi, " + inVarID);
                writeln(", " + tableNumber++ + ", lin_inp, noxt,");
            } catch (IOException ex) {
                Logger.getLogger(PostTableFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            while (bpIt.hasNext()) {
                double breakpointVal = bpIt.next();
                try {
                    writeln(indent + breakpointVal + ", " + tableRefNumber++ + ",");
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
            write("  " + outVarID + "t = " + outVarID + "t, monovar, " + inVarID );
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
                    writeln(indent + bps.get(i) + ", " + vals.get(i + valOffset) + ",");
                } catch (IOException ex) {
                    Logger.getLogger(PostTableFileWriter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return valOffset + i;
    }

}
