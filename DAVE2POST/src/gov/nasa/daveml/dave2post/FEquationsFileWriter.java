/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.daveml.dave2post;

import gov.nasa.daveml.dave.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ebjackso
 */
class FEquationsFileWriter extends FileWriter {
    
    Model ourModel;

    public FEquationsFileWriter(Model theModel, String sourceFileName) throws IOException {
        super( sourceFileName );
        ourModel = theModel;
    }
    
    public void writeln( String cbuf ) throws IOException
    {
//            int i;
//            for(i = 0; i< this.indent; i++)
//                    super.write(" ");
        super.write( cbuf + "\n" );
    }


    void generateTableCall(BlockFuncTable bft) {
        String outVarID = bft.getOutputVarID();
        try {
            writeln("      " + outVarID + " = gentab (gs.motbl." + outVarID + "t(1))");
        } catch (IOException ex) {
            Logger.getLogger(FEquationsFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
