/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.daveml.dave2post;

import gov.nasa.daveml.dave.*;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author ebjackso
 */
class CEquationsFileWriter extends FileWriter {
    
    Model ourModel;

    public CEquationsFileWriter(Model theModel, String sourceFileName) throws IOException {
        super( sourceFileName );
        ourModel = theModel;
    }

    void generateTableCall(BlockFuncTable bft) {
        System.out.println("      clf4 = gentab (gs.motbl.clf3t(1))");
    }
    
}
