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
class OtisModelWriter extends OtisWriter {
    
    BlockArrayList inputBlocks;
    BlockArrayList outputBlocks;
    int nextCalVarNumber;
    String CLdef;
    String CDdef;
    String Cmdef;
    
    static final int MAX_COMMENT_LENGTH = 72;

    public OtisModelWriter(Model theModel, String sourceFileName) throws IOException {
        super( theModel, sourceFileName );
        inputBlocks = new BlockArrayList(10);
        outputBlocks = new BlockArrayList(10);
        nextCalVarNumber = 1;
        CLdef = null;
        CDdef = null;
        Cmdef = null;
    }
    
    public void writeln( String cbuf ) throws IOException {
        super.write( cbuf + "\n" );
    }
    
    
    public void writeModel(BlockArrayList sortedBlocks, String modelName) throws IOException {
        CodeAndVarNames cvn = new CodeAndVarNames();
        Iterator<Block> blkIt;
        Block blk;
        ourModel.setCodeDialect(Model.DT_FORTRAN);
        
        // Write aero intermediate calculations
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
//                    if (id.equals("BSPAN")) {
//                        System.out.println("Found " + id);
//                    }

            // If we output a 'derived' signal, don't generate code;
            // such a signal-generating block was inserted for Simulink
            // realization

            Signal outSig = blk.getOutput();
            if (outSig != null)
                if (outSig.isDerived()) {
//                  System.out.println("Skipping block " + blk.getName() + " as a derived block");
                    skip = true;// don't emit code at this point
                }

            if (blk instanceof BlockBP ) {          // ignore this block
                // skip code generation
            } else if (blk instanceof BlockInput)  { // collect for start
                inputBlocks.add(blk);
                translateInputBlockVarID(blk);
//              System.out.println("Putting block " + blk.getName() + " into input block list");
                skip = true;
            } else if (blk instanceof BlockOutput) { // collect for end
                outputBlocks.add(blk);
                translateOutputBlockVarID(blk);
//              System.out.println("Putting block " + blk.getName() + " into output block list");
                skip = true;
            // if source block is a BlockFuncTable, generate the table call
            } else if (blk instanceof BlockFuncTable) {
                cvn.appendCode( this.generateTableCall( (BlockFuncTable) blk));

            } else { // otherwise generate equation variables and code
                if (!skip)
                    cvn.append( blk.genCode() );
            }

        } // end of while (blkIt.hasNext()) loop


//            // Now write the declarations - sorted with no duplicates
//            codeDeclarations = "";
//            ArrayList<String> varNames = cvn.getVarNames();
//            ArrayList<String> uniqueNames = new ArrayList<String>(new HashSet( varNames ));
//            Collections.sort(uniqueNames, String.CASE_INSENSITIVE_ORDER);
//            Iterator<String> varIt = uniqueNames.iterator();
//            while (varIt.hasNext()) {
//                codeDeclarations += indent + "REAL " + varIt.next() + "\n";
//            }
//            
        // Write initial part of $OTISIN
        // including input XPARS and CALs
        writeModelHeader(modelName);

        // followed by the body (algorithms)
        writeAsOtisCalc(cvn.getCode());

        // write the $PHASEIN section including aero outputs CL and CD
        writeModelFooter();

        // write the STOP and END statements
//            writeln( indent + "STOP");
//            writeln( indent + "END");
        
    }


    private String generateTableCall(BlockFuncTable bft) {
        String outVarID = bft.getOutputVarID();
        return outVarID + "=TAB(" + outVarID + ")\n";
    }

    void writeModelHeader(String modelName) {
        try {
            writeln("! " + modelName + " -- explicit propagation in 2 DOF");
            writeln("$OTISIN");
            writeln("!");
            writeln("! Phase sequence");
            writeln("phase_nam='glide',");
            writeln("!");
            writeln("expint_flg=T,");
            writeln("! ");
            writeln("! Output");
            writeln("outblk_nam='DEFAULT','GCR','ALPHAD','CL','CD','MACH','LOD',");
            writeln("!");
            writeln("! Printer plots");
            writeln("pp_xnam='GCR',");
            writeln("pp_ynam='ALT',");
            writeln("!===========================");
            writeln("! Model inputs and constants");
            writeln("!===========================");
            writeln("!");
            
            int xparNumber = 1;
            Iterator<Block> blkIt = inputBlocks.iterator();
            while (blkIt.hasNext()) {
                Block inputBlk = blkIt.next();
                Signal theSignal = inputBlk.getOutput();
                if (!theSignal.isMarked()) { // skip over OTIS-defined signals
                    writeln("! " + theSignal.getVarID().trim() + " (" + 
                            theSignal.getName().trim() + "):");
                    String description = theSignal.getDescription().trim();

                    // wrap comments, breaking at spaces
                    while (description.length() > (MAX_COMMENT_LENGTH-4)) {
                        int finalSpace = description.lastIndexOf(" ");
                        writeln("!   " + description.substring(0, finalSpace));
                        description = description.substring(finalSpace+1);
                    }
                    writeln("!   " + description);
                    writeln("!");
                    String icVal = "0.0";
                    if (theSignal.hasIC())
                        icVal = theSignal.getIC();
                    writeln("xpar(" + xparNumber + ")=" + icVal );
                    writeln("!");
                    xparNumber++;
                }
            }
            
            writeln("!");
            writeln("! Map xpars into aero input variable names");
            writeln("!");
            
            blkIt = inputBlocks.iterator();
            while (blkIt.hasNext()) {
                Block inputBlk = blkIt.next();
                Signal theSignal = inputBlk.getOutput();
                if (!theSignal.isMarked()) { // skip over OTIS-defined signals
                    writeln("cal(" + nextCalVarNumber + ")='DEF=XPAR(" +
                            nextCalVarNumber + "):NAM=" + 
                            theSignal.getVarID().trim() + ":LOC=10',");
                    nextCalVarNumber++;
                }
            }

            writeln("!");

        } catch (IOException ex) {
            System.err.println("Encountered problem while writing OTIS model header");
            Logger.getLogger(OtisModelWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void writeAsOtisCalc(String codeBody) throws IOException {

        writeln("!===========================");
        writeln("!      Model equations");
        writeln("!===========================");
        writeln("!");
            
        String lines[] = null;
        try {
            lines = convertIfBlocks( codeBody );
        } catch (DAVEException ex) {
            Logger.getLogger(OtisModelWriter.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Unable to decipher if block - aborting.");
            System.exit(1);
        }
        int numLines = lines.length;
        for (int i = 0; i < numLines; i++) {
            String equationParts[] = lines[i].split("=");
           
            if (equationParts.length != 2) {
                    System.err.println("Unable to encode in two parts: " + 
                            lines[i]);
            } else {
                boolean skip = false;
                String varName = equationParts[0];
                String rhs     = equationParts[1];
                // exclude output variables
                if ( varName.equals("CL") ) {
                    CLdef = rhs;
                    skip = true;
                }
                if ( varName.equals("CD") ) {
                    CDdef = rhs;
                    skip = true;
                }
                if ( varName.equals("CM") ) {
                    Cmdef = rhs;
                    skip = true;
                }
                if ( !skip ) {
                    writeln("cal(" + nextCalVarNumber + ")='NAM=" +
                            varName + ":def=" + rhs + ":LOC=10',");
                    nextCalVarNumber += 1;
                }
            }
        }
        writeln("!");
        writeln("!=== end of model code ===");
        writeln("!");
    }

    private String[] convertIfBlocks(String codeBody) throws DAVEException {
        String line[] = codeBody.replace(" ","").split("\n");
        ArrayList<String> outLines = new ArrayList<String>(50);
        String parts[];
        String newLine = "";
        int numLines = line.length;
        for (int i = 0; i < (numLines-3); i++) {
            if ((line[i+1].startsWith("IF")) &&
                    ( line[i+3].startsWith("ENDIF"))) {
                // convert into single OTIS IF(x, y, z)
                // line[i]   is equation part y, with variable assignment
                // line[i+1] is x: IF((a.GT.b))THEN
                // line[i+2] is equation part z, with variable assignment
                // line[i+3] should be ENDIF

                // deal with line i (part y)
                parts = line[i].split("=");
                if (parts.length != 2) 
                    throw new DAVEException( "line " + i + 
                        ": unrecognized grammar in line preceding IF statement: " 
                        + line[i]);
                String varname = parts[0];
                String y = parts[1];

                // deal with line i+1
                parts = line[i+1].split("\\."); // matches dot "."
                if (parts.length != 3) 
                    throw new DAVEException("line " + (i+1) + 
                            ":unrecognized grammar in IF statement: " +
                            line[i+1]);
                String a    = parts[0].replace("IF((", "");
                String test = parts[1];
                String b    = parts[2].replace("))THEN","");
                String relTest = "><";
                if (test.equals("LT")) relTest = "<";
                if (test.equals("LE")) relTest = "<=";
                if (test.equals("EQ")) relTest = "==";
                if (test.equals("GE")) relTest = ">=";
                if (test.equals("GT")) relTest = ">";
                if (test.equals("NE")) relTest = "!=";

                String x = a + relTest + b;

                newLine +=  x + "," + y;


                // deal with line i+2: z
                parts = line[i+2].split("=");
                if (parts.length != 2) throw new DAVEException("line " + (i+2) + 
                        ": unrecognized grammar in line inside IF statement: " +
                        line[i+2]);
                String z = parts[1];

                newLine = varname + "=IF(" + x + "," + y + "," + z + ")";
                outLines.add(newLine);
                i = i + 3;
            } else {  // no following IF line
                outLines.add( line[i] );
            }
        } // end of loop
        outLines.add( line[numLines-3] ); // add last three lines as they ...
        outLines.add( line[numLines-2] ); // ... can't contain an IF block
        outLines.add( line[numLines-1] );
        return outLines.toArray(new String[0]);
    }

    private void writeModelFooter() {
        try {
            writeln("$END");
            writeln("!");
            writeln("$PHASIN");
            writeln("!");
            writeln("ph_nam='glide', ");
            writeln("ph_type='E',");
            writeln("ph_title='HL-20 glide problem',");
            writeln("!");
            writeln("! Planet model");
            writeln("cb_flat=0.,");
            writeln("cb_rot=0.,");
            writeln("atmos='index=1',");
            writeln("! vehicle model");
            writeln("sref='def=286.45',");
            writeln("! Sum coefficients");
            
            // write output coefficients
            int aeroNumber = 1;
            if (CLdef != null) {
                writeln("aero(" + aeroNumber + ")='type=CL,def=" + CLdef + "',");
                aeroNumber++;
            }
            if (CLdef != null) {
                writeln("aero(" + aeroNumber + ")='type=CD,def=" + CDdef + "',");
                aeroNumber++;
            }
            if (Cmdef != null) {
                writeln("aero(" + aeroNumber + ")='type=CM,def=" + Cmdef + "',");
                aeroNumber++;
            }
            

            
//            writeln("aero(1)='type=CL,def=CL0+CLWFL+CLWFR+CLBFUL+CLBFUR+CLBFLL+CLBFLR+CLRUD',");
//            writeln("aero(2)='type=CD,def=CD0+CDWFL+CDWFR+CDBFUL+CDBFUR+CDBFLL+CDBFLR+CDRUD',");
//            writeln("!   aero(3)='type=CM,def=TAB(CM0A0)+ALPHAD*(TAB(CM0A1)+ALPHAD*((TAB(CM0A2))+ALPHAD*TAB(CM0A3)))',");
            writeln("!");
            writeln("");
            writeln("! equations of motion");
            writeln("eqm_type=2,");
            writeln("!");
            writeln("! input  coordinates");
            writeln("ic_type=2,");
            writeln("! states (vfps, psideg, gamd, alt_ft, lon_deg, lat_d, weight_lbs");
            writeln("state(1)='ic_val=   323.215',");
            writeln("state(2)='ic_val=    0.',");
            writeln("state(3)='ic_val=    0.',");
            writeln("state(4)='ic_val= 10000.',");
            writeln("state(5)='ic_val=    0.', ");
            writeln("state(6)='ic_val=    0.',");
            writeln("state(7)='ic_val=  19100.',");
            writeln("!");
            writeln("control(1)='nam=ALPHA,def=.1745',");
            writeln("! ");
            writeln("tp='val=100.',   ");
            writeln("!");
            writeln("! stopping conditions");
            writeln("stp_nam='ALT',");
            writeln("stp_val=   0.,   ");
            writeln("!");
            writeln("! Print interval");
            writeln("outblk_dt=1.,");
            writeln("xintg_dt=.1D0,");
            writeln("!  ");
            writeln("$END");
        } catch (IOException ex) {
            Logger.getLogger(OtisModelWriter.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Encountered problem while writing OTIS model footer");
        }
    }

    private void translateInputBlockVarID(Block blk) {
        // change the varID for the signal fed by this input block
        String dmlVarID = blk.getOutputVarID();
        if (this.needsTranslation(dmlVarID)) {
            
            // get new varID
            String otisVarID = this.translate(dmlVarID);
            
            // change input block's output signal varID; this will 
            // change varIDs for source block and all dest block ports
            Signal sig = blk.getOutput();
            sig.mark(); // mark as an OTIS input variable
            sig.setVarID(otisVarID);
        }
                    
    }

    private void translateOutputBlockVarID(Block blk) {
        // change the varID for the upstream Signal that feeds this block
        Signal inputSig = blk.getInput(0);
        String dmlVarID = inputSig.getVarID();
        if (this.needsTranslation(dmlVarID)) {
            
            // get new varID
            String otisVarID = this.translate(dmlVarID);
            
            // change input block's output signal varID; this will 
            // change varIDs for source block and all dest block ports
            inputSig.mark(); // mark for model writing code
            inputSig.setVarID(otisVarID);
        }
                    
    }

}
