/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.daveml.dave2post;

import gov.nasa.daveml.dave.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

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
    
    
    public void writeFcode() throws IOException {
        CodeAndVarNames cvn = new CodeAndVarNames();
        BlockArrayList sortedBlocks;
        BlockArrayList outputBlocks = new BlockArrayList(10);
        Iterator<Block> blkIt;
        Block blk;
        String codeBody = "";
        String codeDeclarations = "";
        ourModel.setCodeDialect(Model.DT_FORTRAN);
        try {
            sortedBlocks = ourModel.getSortedBlocks();
            if (sortedBlocks.isEmpty()) {
                System.err.println(
                        "Warning: Order of execution could not be determined" +
                        " (sorted block execution list empty).");
            }
            blkIt = sortedBlocks.iterator();
            while (blkIt.hasNext()) {
                blk = blkIt.next();
                boolean skip = false;
                
                // Mark 'derived' limiter and switch blocks as 'underived'
                // These blocks were inserted during parsing a <variableDef>
                // element and in essence create a new variable, previously undefined.
                // They are marked as 'derived' because they don't appear in the original MathML
                // We need to treat them as an original ('underived') variable
                // so the logic gets expressed as separate lines, not within 
                // a parenthetical expression.
                
                if (blk instanceof BlockLimiter || blk instanceof BlockMathSwitch )
                    blk.getOutput().clearDerivedFlag();
                
//                // debugging section
//                String id = blk.getOutputVarID();
//                if (id != null)
//                    if (id.equals("VRW")) {
//                        System.out.println("Found " + id);
//                    }

                // If we output a 'derived' signal, don't generate code;
                // such a signal-generating block was inserted for Simulink
                // realization
                
                Signal outSig = blk.getOutput();
                if (outSig != null)
                    if (outSig.isDerived())
                        skip = true;// don't emit code at this point
               
                if (blk instanceof BlockBP ) {          // ignore this block
                    // skip code generation
                } else if (blk instanceof BlockOutput) { // collect for end
                    outputBlocks.add(blk);
                // if source block is a BlockFuncTable, generate the table call
                } else if (blk instanceof BlockFuncTable) {
                    cvn.appendCode( this.generateTableCall( (BlockFuncTable) blk));

                } else { // otherwise generate equation variables and code
                    if (!skip)
                        cvn.append( blk.genCode() );
                }
                    
            } // end of while (blkIt.hasNext()) loop
            
            // write information about model outputs
            blkIt = outputBlocks.iterator();
            while (blkIt.hasNext()) {
                cvn.append(blkIt.next().genCode());
                codeBody = cvn.getCode();
            }
            
            // Now write the declarations - sorted with no duplicates
            codeDeclarations = "";
            ArrayList<String> varNames = cvn.getVarNames();
            ArrayList<String> uniqueNames = new ArrayList<String>(new HashSet( varNames ));
            Collections.sort(uniqueNames, String.CASE_INSENSITIVE_ORDER);
            Iterator<String> varIt = uniqueNames.iterator();
            while (varIt.hasNext()) {
                codeDeclarations += indent + "REAL " + varIt.next() + "\n";
            }
            write( codeDeclarations );
            
            // followed by the body (algorithms)
            write( codeBody );
            
            // write the STOP and END statements
            writeln( indent + "STOP");
            writeln( indent + "END");
        
        } catch (DAVEException ex) {
            System.err.println("Warning: Order of execution could not be determined (sorted varID list null).");
        }
    }


    private String generateTableCall(BlockFuncTable bft) {
        String outVarID = bft.getOutputVarID();
        return indent + outVarID + " = gentab (gs.motbl." + outVarID + "t(1))\n";
    }
    
}
