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
        indent = "      ";
    }
    
    public void writeln( String cbuf ) throws IOException
    {
//            int i;
//            for(i = 0; i< this.indent; i++)
//                    super.write(" ");
        super.write( cbuf + "\n" );
    }
    
    
   public void writeEquations() throws IOException {
       ArrayList<String> sortedVarIDs;
       try {
           sortedVarIDs = ourModel.getSortedVarIDs();
           if (sortedVarIDs.isEmpty())
               System.err.println("Warning: Order of exection could not be determined (sorted varID list empty).");
           Iterator<String> varID = sortedVarIDs.iterator();
           while (varID.hasNext()) {
               String theVarID = varID.next();
               Block blk = ourModel.getBlockByOutputVarID(theVarID);
               if (blk == null)
                   writeln("C Warning: can't find block with output ID \"" + theVarID + "\"");
               else
                   writeln(blk.genFcode());
           }
       } catch (DAVEException ex) {
           System.err.println("Warning: Order of exection could not be determined (sorted varID list null).");
       }
   }


    void generateTableCall(BlockFuncTable bft) {
        String outVarID = bft.getOutputVarID();
        try {
            writeln(indent + outVarID + " = gentab (gs.motbl." + outVarID + "t(1))");
        } catch (IOException ex) {
            Logger.getLogger(FEquationsFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
