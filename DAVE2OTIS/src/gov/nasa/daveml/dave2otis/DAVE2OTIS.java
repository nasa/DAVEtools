// DAVE2OTIS.java
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.
//
//  since @0.9.4
//
//  Main class file of DAVE-ML to OTIS4 utility

package gov.nasa.daveml.dave2otis;

import gov.nasa.daveml.dave.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Top-level driver for converting DAVE models into OTIS4 table and code
 * <p>
 * Utility program to convert <b>D</b>igital <b>A</b>erospace
 * <b>V</b>ehicle <b>E</b>xchange <b>M</b>arkup <b>L</b>anguage
 * (<b>DAVE-ML</b>) models into OTIS4 source files
 *<p>
 * <b>DAVE-ML</b> is part of AIAA standard S-119 for
 * encoding dynamic flight vehicle models for exchange between
 * simulation tools and facilities in an open-software,
 * facility-neutral manner.
 * <p>
 * More information about DAVE-ML is available at the project website:
 * {@link <a href="http://daveml.org">  http://daveml.org</a>} 
 *<p> 
 * Modification history: 
 * <ul>
 *  <li>2012-01-18: Written EBJ</li>
 * </ul>
 *
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * @version 0.9
 *
 **/
public class DAVE2OTIS extends DAVE {

    /**
     *  Name of the _table.pos2 (table) file
     */
    String tableFileName;
    /**
     *  Name of the _table.c (equations) file
     */
    String modelFileName;
//    /**
//     *  indicates user asked for Warn on clip at run time for table lookups
//     */
//    boolean warnRunTimeFlag;
//    /**
//     *  indicates user asked for a library instead of a model creation
//     */
//    boolean makeLib;
//    /**
//     *  indicates user asked resulting system to have an enable input
//     */
//    boolean makeEnabledSubSys;

    /**
     * Basic constructor
     **/
    public DAVE2OTIS() {
        super();	// calls non-parsing DAVE constructor which
        // initializes flags

//        this.warnRunTimeFlag = false;
//        this.makeLib = false;
//        this.makeEnabledSubSys = false;
    }

    /**
     *
     * Command-line argument-parsing constructor
     *
     **/
    public DAVE2OTIS(String[] args) {
        this();		// calls non-parsing DAVE constructor which
        // initializes DAVE flags and creates Model
        // placeholder; also initializes DAVE2OTIS
        // option flags

        this.parseOptions(args);	// parse DAVE2OTIS-specific arguments

        if (this.isVerbose()) {
            this.getModel().makeVerbose();	// set model to be verbose
        }
    }

//    /**
//     *
//     * Returns the "makeLib" flag
//     *
//     **/
//    public boolean makeLib() {
//        return this.makeLib;
//    }

    /**
     *
     * Sets the input, stub, MAT and MDL file names.
     *
     * <p>
     * Overrides method in DAVE.
     *
     * @param fn the input file name to use
     *
     **/
    @Override
    public void setInputFileName(String fn) {
        super.setInputFileName(fn);	// sets stub and file name fields
        this.tableFileName = this.getStubName() + ".dat";
        this.modelFileName = this.getStubName() + ".inl";
    }

    /**
     *
     * Parse command-line options
     *
     **/
    private void parseOptions(String inArgs[]) {
        String exampleUse = "Usage: java DAVE2OTIS [-v|--version] | [-c|--count] [-d|--debug] DAVE-ML_document";
        int numArgs = inArgs.length;

        // Save arguments into field
        this.setArgs(inArgs);

        // Parse any options

        if (numArgs > 0) {
            int parsedArgs = 0;
            if (this.matchOptionArgs("c", "count")) {
                this.setGenStatsFlag();
                parsedArgs++;
            }
            if (this.matchOptionArgs("d", "debug")) {
                this.makeVerbose();
                parsedArgs++;
            }
            if (matchOptionArgs("v", "version")) {
                System.out.println("DAVE2OTIS version " + getVersion());
                System.exit(0);
            }
            if (parsedArgs < (numArgs - 1)) {
                if (numArgs == 2) {
                    //		    	String[] theArgs = getArgs();
                    System.err.println("Unable to understand and parse option switch '"
                            + getArgs()[2] + "'.");
                } else {
                    System.err.println("Unable to understand and parse all "
                            + (numArgs - 1) + " argument(s).");
                }
                System.err.println(exampleUse);
                System.exit(0);
            }
        } else {
            // Make sure we have at least the input file
            System.out.println(exampleUse);
            System.out.println("Need at least one argument.");
            System.exit(0);
        }

        // Retrieve input file name
        this.setInputFileName(inArgs[numArgs - 1]);
    }

    /**
     *
     * Calls each block in DAVE model to generate appropriate m-script
     *
     **/
    public void createModel() throws IOException {
        Model theModel = this.getModel();

        // create our output files (two: one with equations, one with data)
        File file = new File(this.getStubName());
        String modelName = file.getName();	// removes path

        // select only CL and CD outputs
        
        theModel.clearSelections();
        
        if (!theModel.selectOutputByName("totalCoefficientOfLift")) {
            System.err.println( "Error: lift coefficient not found");
            System.exit(1);
        }
        
        if (!theModel.selectOutputByName("totalCoefficientOfDrag")) {
            System.err.println( "Error: drag coefficient not found");
            System.exit(1);
        }
        
        // create two file writers
        OtisTableWriter tableWriter = new OtisTableWriter(theModel, this.tableFileName);
        OtisModelWriter modelWriter = new OtisModelWriter(theModel, this.modelFileName);

        // generate contents
        
        modelWriter.writeModel( modelName );
        
        // find the function blocks
        BlockArrayList blocks = null;
        try {
            blocks = theModel.getSelectedBlocks();
            if (blocks == null) {
                System.err.println(
                        "Error: Order of execution could not be determined" +
                        " (sorted block execution list null).");
                System.exit(1);
            }
            if (blocks.isEmpty()) {
                System.err.println(
                        "Error: Order of execution could not be determined" +
                        " (sorted block execution list empty).");
                System.exit(1);
            }        
        
            Iterator<Block> it = blocks.iterator();
            while(it.hasNext()) {
                Block blk = it.next();
                if (blk instanceof BlockFuncTable) {
                    BlockFuncTable bft = (BlockFuncTable) blk;
                    tableWriter.generateTableDescription( bft );
                }
            }   
        
        } catch (DAVEException ex) {
            Logger.getLogger(DAVE2OTIS.class.getName()).log(Level.SEVERE, null, ex);
        }


        // Write footers
//        mdlWriter.writeSLFooter(this.getVersion(), modelName);
//        matWriter.writeDataFooter();

        // Close the files
        tableWriter.close();
        modelWriter.close();

    }

    /**
     *
     * Creates script to verify created model
     *
     **/
//    public void createVerifyScript() {
//
//        MatFileWriter scriptWriter;
//
//        String modelName = this.getModel().getName();
//
//        String scriptFileName = modelName + "_verify.m";
//        try {
//            scriptWriter = new MatFileWriter(this.myDiagram, scriptFileName);
//        } catch (IOException e) {
//            System.err.println("WARNING: Unable to create verification script.");
//            return;
//        }
//
//        scriptWriter.writeVerifyScriptHeader(modelName);
//
//        CheckData cd = this.getCheckcaseData();
//        ArrayList<StaticShot> al = cd.getStaticShots();
//
//        Iterator<StaticShot> it = al.iterator();
//        while (it.hasNext()) {
//            StaticShot ss = it.next();
//            scriptWriter.writeCheckCaseFromStaticShot(ss);
//        }
//
//        scriptWriter.writeVerifyScriptFooter();
//
//        // close the file
//        try {
//            scriptWriter.close();
//        } catch (IOException e) {
//            System.err.println("WARNING: Unable to successfully close verification script.");
//        }
//
//    }

    /**
     *
     * Main routine for DAVE-ML-to-OTIS utility
     *
     **/
    public static void main(String args[]) {

        boolean success = false;

        // calls DAVE constructor to initialize new model; parses
        // DAVE2OTIS-specific arguments.
        DAVE2OTIS dave2otis = new DAVE2OTIS(args);

        // Have DAVE utility parse the file and build internal Model
        try {
            System.out.println("Parsing input file...");
            success = dave2otis.parseFile();	// instantiated in
            // gov.nasa.daveml.DAVE
            // superclass
        } catch (Exception e) {
            System.err.println("ERROR in parsing input file: " + e.getMessage());
        }

        // quit now if problems in parsing
        if (!success) {
            System.out.println("Parsing of input file failed; no Simulink creation script will be generated.");
            System.exit(1);
        }

        System.out.println("Parsing successful.");

        // If checkcase data is included, run quick verification of internal Model
        try {
            if (dave2otis.hasCheckcases()) {
                System.out.println("Running verification of internal model...");
                if (!dave2otis.verify()) {
                    System.out.println("");
                    System.out.println("Verification failed; no OTIS source files will be generated.");
                    System.exit(1);
                }
            }
        } catch (NoSuchMethodError e) {
            System.err.println("The DAVE.jar file appears to be out-of-date; couldn't find hasCheckcases() method.");
            System.exit(1);
        }

        // Did user ask for stats?
        try {
            if (dave2otis.getGenStatsFlag()) {
                dave2otis.reportStats();		// report parsing stats using default DAVE routine
            }
        } catch (NoSuchMethodError e) {
            System.err.println("The DAVE.jar file appears to be out-of-date; couldn't find getGenStatsFlag() method.");
            System.exit(1);
        }

        // Create creation and verification m-script files
        System.out.println("Creating OTIS4 input deck and model source...");
        try {
            dave2otis.createModel();
        } catch (IOException e) {
            return;
        }
//        if (dave2sl.makeLib()) {
//            System.out.println("Simulink library written.");
//        } else {
//            System.out.println("Simulink model written.");
//        }

        // Generate verification script
//        if (dave2otis.hasCheckcases()) {
//            System.out.println("Checkcase data found, generating verification script...");
//            dave2otis.createVerifyScript();
//            System.out.println("Verification script written.");
//        }

        System.out.println("Wrote OTIS4 source files for "
                + dave2otis.getStubName() + ".");
    }
}
