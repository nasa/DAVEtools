/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.daveml.dave2otis;

import gov.nasa.daveml.dave.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author ebjackso
 */
class OtisTableWriter extends OtisWriter {
    
    int tableNumber, tableRefNumber;
    int    lineWrapLen; /** max chars per line in tables                    */
    BlockFuncTable bft; /** the model block containing the function table   */
    FuncTable ft;       /** function table currently being written          */
    String outVarID;    /** output variable ID for table result             */
    String outOtisName; /** translation of outVarID into OTIS namespace     */
    String indent;      /** indent for OTIS table lines, if any             */
    int[] dims;         /** vector of table dimensions                      */
    int[] coords;       /** working list of progress along table dimensions */
    int numDims;        /** number of dimensions of table                   */

    public OtisTableWriter(Model theModel, String tableFileName) 
            throws IOException {
        super( theModel, tableFileName );
        indent = "";
        lineWrapLen = 72;
        ft = null;
        outVarID = "";
        outOtisName  = "";  
        dims = null;
        numDims = -1;
        idMap = null;
    }
    
    /**
     *
     * Adds newline to the end of each write.
     *
     * @param cbuf String containing text to write
     *
     */

    private void writeln( String cbuf ) throws IOException {
        super.write( cbuf + "\n" );
    }
    
    /**
     * Adds just a newline
     */
    
    private void writeln() throws IOException {
        this.writeln("");
    }

    /**
     * Top-level entry point; pass in each table to be written into OTIS format.
     * This top-level stores information from the provided <code>BlockFuncTable</code>
     * into local fields for convenience.
     */
    void generateTableDescription() {
        ft          = bft.getFunctionTableDef();
        outVarID    = bft.getOutputVarID();
        outOtisName = outVarID;
        dims        = ft.getDimensions();
        numDims     = dims.length;
        try {               
            this.writeTable();
        } catch (IOException ex) {
            Logger.getLogger(OtisTableWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Private routine that actually generates the OTIS output in several parts.
     * @throws IOException 
     */
    
    private void writeTable() throws IOException {
        
        // write header
        
        writeln("* Data set name");
        writeln(indent + outVarID);
        writeln("* Table name");
        writeln(indent + outOtisName);
        writeln("* Data set scale factor");
        writeln(indent + "1.0");
        
        // add comment line(s)
        writeln("*============================");
        String descr = bft.getDescription();
        this.writeTextComment( normalize(descr) );
        writeln("*============================");
       
        writeln("* Number of terms");
        writeln(indent + "1");
        writeln("* Number of tabular coefficients");
        writeln(indent + "NCOEF1");
        write(  indent + "* this term is a function of ");
        if (numDims == 1)
            write("one thing: ");
        else
            write(numDims + " things: ");
        for (int dim = numDims; dim >= 1; dim-- ) {
            write( bft.getVarID(dim) );
            if (dim > 2)
                write( ", " );
            if (dim == 2)
                write( " and ");
        }
        
        writeln();
        writeln(indent + numDims);
        
        // Generate the independent variables listing in OTIS
        this.writeIndependentValues();
        
        // Generate the table of dependent values, with headers separating
        // each dimension
        
        this.writeDependentValues();
        
        // put two empty comment lines after each table
        
        writeln("*");
        writeln("*-----------------------------------------------------");
    }
    
    
    /**
     * Emit the breakpoint (independent values) vectors
     */

    private void writeIndependentValues() throws IOException {
        
    // note that we work backwards in writing independent values
    // as OTIS's convention is first breakpoint varies fastest
    // whereas DAVE-ML the last dimension varies fastest

        for (int dim = numDims; dim >= 1; dim-- ) {

            String inVarID    = bft.getVarID(dim); // convert to port (1-based) number
            String inOtisName = inVarID; // translation should have been done previously
            String bpID       = ft.getBPID(dim);
            ArrayList<Double> bps  = ourModel.getBPSetByID(bpID).values();
            Iterator<Double> bpIt  = bps.iterator();

            writeln(indent + inOtisName);
            writeln(indent + "* number of " + inOtisName + "s");
            writeln(indent + bps.size());
            writeln(indent + "* " + inOtisName + " values");
            String origIndent = indent;
            indent = indent + "      "; // add six spaces

            String buffer = indent;

            while (bpIt.hasNext()) {
                double breakpointVal = bpIt.next();
                String testBuffer = buffer + breakpointVal + "  ";
                if (testBuffer.length() > lineWrapLen) {
                    writeln(buffer);
                    buffer = indent + breakpointVal + "  ";
                } else {
                    buffer = testBuffer;
                }
            }
            writeln(buffer);
            indent = origIndent;
        }
    }
    
    /**
     * Emit the table of dependent values in OTIS format
     */
    
    private void writeDependentValues() throws IOException {
        // WRITE TABLE VALUES (DEPENDENT VALUES)

        // these are in the stored order, since we wrote breakpoints 
        // in reverse order
        ArrayList<Double> pts = ft.getValues();
        Iterator<Double> ptIt = pts.iterator();

        // duplicate the dims array (vector of integers giving table dimension)
        coords = dims.clone();

        // zero out the array
        for (int i = 0; i < coords.length; i++) {
            coords[i] = 0;
        }

        // write top-level header
        write(  indent + "* " + outOtisName + " values");
        if (numDims > 1)
            writeln(  " for various " + bft.getVarID(numDims) );
        else
            writeln();

        // recursively write comment header and last bit of table
        writeIndepValsWithHdr(ptIt, 1);
    }

    
    /**
     * Called by writeDependentValues(), this recursive routines writes comments 
     * documenting the current coordinates corresponding to outer dimensions 
     * followed current inner-most vector of dependent values (DV) from the data
     * table.
     * 
     * The <code>dim</code> parameter is the offset (0-based) into the 
     * <code>coords[]</code> vector, which is a list of current table coordinates
     * (we don't make use of the last entry since we emit that dimension all at 
     * once).
     * 
     * If called with <code>dim</code> equal to an value of something less 
     * than the next-to-last dimension offset (0-based), this routine 
     * emits a single comment line giving that coordinate value, and calls 
     * itself  with <code>dim</code> incremented to point to the next outer-most 
     * entry of <code>coords[]</code>.
     * 
     * If called with <code>dim</code> equal to the offset to the next-to-last
     * entry in <code>coords[]</code>, we generate the DV points corresponding 
     * to the final dimension of entries corresponding to the table coordinates
     * specified in <code>coords[]</code>.
     * 
     * If the table is f(Mach, alpha, deflection) then this emits something like
     * 
     *   * for Mach = 0.8
     *   * for alpha = 10.3
     *   ...appropriate points from table separated by spaces...
     * 
     * @param ptIt  An <code>Iterator</code> pointing to next DV point
     * @param dim   The current offset into the vector of dimensions
     * @throws IOException 
     */
    
    private void writeIndepValsWithHdr(Iterator<Double> ptIt, int dim ) 
            throws IOException {
        
        if (dim < numDims) {
            String bpID = ft.getBPID(dim);
            ArrayList<Double> bps = ourModel.getBPSetByID(bpID).values();
            for (int i = 0; i < bps.size(); i++ ) {
                coords[dim] = i; // increment along this breakpoint dimension
                double bpVal = bps.get(coords[dim]);
                writeln(indent + "* for " + bft.getVarID(dim) + 
                        " = " + bpVal + " ");
                writeIndepValsWithHdr(ptIt, dim+1);
            }
            
        } else { // last dimension shows final-dimension vector at given coordinates

            String lastBpID = ft.getBPID(numDims);
            int numLastBps = ourModel.getBPSetByID(lastBpID).values().size();
            
            String buffer = indent + "  ";
            for (int i = 0; i < numLastBps; i++) {
                double val = ptIt.next();
                String testBuffer = buffer + val + "  ";
                if (testBuffer.length() > lineWrapLen) {
                    writeln(buffer);
                    buffer = indent + "  " + val + "  ";
                } else {
                    buffer = testBuffer;
                }
            }
            writeln(buffer);
        }
    }
    

    /**
     * Returns input string reformatted as OTIS comment line
     * @param description 
     */

    private void writeTextComment(String input) throws IOException {
        
        String buffer = "";
        String testBuffer;
        if (input != null) {
            input = normalize(input);
            String[] word = input.split(" "); // split into words
            buffer = indent + "*";
            for (int i = 0; i < word.length; i++) {
                testBuffer = buffer + " " + word[i];
                if (testBuffer.length() > lineWrapLen) {
                    writeln(buffer);
                    buffer = indent + "* " + word[i];
                } else {
                    buffer = testBuffer;
                }
            }
        }
        writeln(buffer);
    }

    void writeTables( BlockArrayList blocks ) {
        
        Iterator<Block> it = blocks.iterator();
        while(it.hasNext()) {
            Block blk = it.next();
            if (blk instanceof BlockFuncTable) {
                bft = (BlockFuncTable) blk;
                this.generateTableDescription();
            }
        }   
    }
    
}
