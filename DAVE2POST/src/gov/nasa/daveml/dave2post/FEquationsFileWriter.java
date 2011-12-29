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
class FEquationsFileWriter extends FileWriter {
    
    Model ourModel;
    
    String indent;

    public FEquationsFileWriter(Model theModel, String sourceFileName) throws IOException {
        super( sourceFileName );
        ourModel = theModel;
        indent = "       ";
    }
    
    public void writeln( String cbuf ) throws IOException
    {
//            int i;
//            for(i = 0; i< this.indent; i++)
//                    super.write(" ");
        super.write( cbuf + "\n" );
    }
    
    
    public void writeEquations() throws IOException {
        BlockArrayList sortedBlocks;
        try {
            sortedBlocks = ourModel.getSortedBlocks();
            if (sortedBlocks.isEmpty()) {
                System.err.println(
                        "Warning: Order of execution could not be determined" +
                        " (sorted block execution list empty).");
            }
            Iterator<Block> blkIt = sortedBlocks.iterator();
            while (blkIt.hasNext()) {
                Block blk = blkIt.next();
                boolean skip = false;
                
                // debugging section
                String id = blk.getOutputVarID();
                if (id != null)
                    if (id.equals("VRW")) {
                        System.out.println("Found " + id);
                        blk.getOutput().clearDerivedFlag();
                    }

                // If we output a 'derived' signal, don't generate code;
                // such a signal-generating block was inserted for Simulink
                // realization
                
                Signal outSig = blk.getOutput();
                if (outSig != null)
                    if (outSig.isDerived())
                        skip = true;// don't emit code at this point
               
                if (blk instanceof BlockInput            // ignore these blocks
                        || blk instanceof BlockOutput
                        || blk instanceof BlockBP) {
                    // skip code generation
                // if source block is a BlockFuncTable, generate the table call
                } else if (blk instanceof BlockFuncTable) {
                    this.generateTableCall( (BlockFuncTable) blk);

                } else { // otherwise generate equation code
                    String code = blk.genFcode();
//                    System.out.println(code);
                    if (!skip) 
                        write(code);
                }
                    
            } // end of while (blkIt.hasNext()) loop
        
        } catch (DAVEException ex) {
            System.err.println("Warning: Order of exection could not be determined (sorted varID list null).");
        }
    }


    private void generateTableCall(BlockFuncTable bft) {
        String outVarID = bft.getOutputVarID();
        try {
            writeln(indent + outVarID + " = gentab (gs.motbl." + outVarID + "t(1))");
        } catch (IOException ex) {
            Logger.getLogger(FEquationsFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
