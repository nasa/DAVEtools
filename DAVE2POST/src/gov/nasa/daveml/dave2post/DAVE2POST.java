// DAVE2POST.java
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
//  Main class file of DAVE-ML to POST II utility

package gov.nasa.daveml.dave2post;

import gov.nasa.daveml.dave.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * Top-level driver for converting DAVE models into POST II table and code
 * <p>
 * Utility program to convert <b>D</b>igital <b>A</b>erospace
 * <b>V</b>ehicle <b>E</b>xchange <b>M</b>arkup <b>L</b>anguage
 * (<b>DAVE-ML</b>) models into POST II source files
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
 *  <li>2011-12-19: Written EBJ</li>
 * </ul>
 *
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * @version 0.9
 *
 **/
public class DAVE2POST extends DAVE {

    /**
     *  Name of the _table.pos2 (table) file
     */
    String tableFileName;
    /**
     *  Name of the _table.c (equations) file
     */
    String sourceFileName;
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
    public DAVE2POST() {
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
    public DAVE2POST(String[] args) {
        this();		// calls non-parsing DAVE constructor which
        // initializes DAVE flags and creates Model
        // placeholder; also initializes DAVE2POST
        // option flags

        this.parseOptions(args);	// parse DAVE2POST-specific arguments

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
        this.tableFileName = this.getStubName() + ".pos2";
        this.sourceFileName = this.getStubName() + ".f";
    }

    /**
     *
     * Parse command-line options
     *
     **/
    private void parseOptions(String inArgs[]) {
        String exampleUse = "Usage: java DAVE2POST [-v|--version] | [-c|--count] [-d|--debug] DAVE-ML_document";
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
//            if (this.matchOptionArgs("w", "warnruntime")) {
//                this.warnRunTimeFlag = true;
//                parsedArgs++;
//            }
//            if (this.matchOptionArgs("l", "lib")) {
//                this.makeLib = true;
//                parsedArgs++;
//            }
//            if (this.matchOptionArgs("e", "enabled")) {
//                this.makeEnabledSubSys = true;
//                parsedArgs++;
//            }
            if (matchOptionArgs("v", "version")) {
                System.out.println("DAVE2POST version " + getVersion());
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
//        // create Diagram (rough layout for Simulink model)
        Model theModel = this.getModel();
//        this.myDiagram = new SLDiagram(this.getModel());
//
//        // set options for diagram
//        if (this.warnRunTimeFlag) {
//            this.myDiagram.setWarnOnClip();
//        }
//
//        if (this.makeLib) {
//            this.myDiagram.setLibFlag();
//        }
//
//        if (this.makeEnabledSubSys) {
//            this.myDiagram.setEnabledFlag();
//        }

        // create our output files (two: one with equations, one with data)
        File file = new File(this.getStubName());
        String modelName = file.getName();	// removes path

        // script file for now
        PostTableFileWriter tableWriter = new PostTableFileWriter(theModel, this.tableFileName);
        FEquationsFileWriter equationWriter = new FEquationsFileWriter(theModel, this.sourceFileName);

        // Write headers
//        mdlWriter.writeSLHeader(modelName);
//        matWriter.writeDataHeader(modelName);

        // generate contents
        
        equationWriter.writeFcode();
        
        // find the function blocks
        BlockArrayList blocks = theModel.getBlocks();
        Iterator<Block> it = blocks.iterator();
        while(it.hasNext()) {
            Block blk = it.next();
            if (blk instanceof BlockFuncTable) {
                BlockFuncTable bft = (BlockFuncTable) blk;
                tableWriter.generateTableDescription( bft );
            }
        }   

        // Write footers
//        mdlWriter.writeSLFooter(this.getVersion(), modelName);
//        matWriter.writeDataFooter();

        // Close the files
        tableWriter.close();
        equationWriter.close();

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
     * Main routine for DAVE-ML-to-POST utility
     *
     **/
    public static void main(String args[]) {

        boolean success = false;

        // calls DAVE constructor to initialize new model; parses
        // DAVE2POST-specific arguments.
        DAVE2POST dave2post = new DAVE2POST(args);

        // Have DAVE utility parse the file and build internal Model
        try {
            System.out.println("Parsing input file...");
            success = dave2post.parseFile();	// instantiated in
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
            if (dave2post.hasCheckcases()) {
                System.out.println("Running verification of internal model...");
                if (!dave2post.verify()) {
                    System.out.println("");
                    System.out.println("Verification failed; no POST source files will be generated.");
                    System.exit(1);
                }
            }
        } catch (NoSuchMethodError e) {
            System.err.println("The DAVE.jar file appears to be out-of-date; couldn't find hasCheckcases() method.");
            System.exit(1);
        }

        // Did user ask for stats?
        try {
            if (dave2post.getGenStatsFlag()) {
                dave2post.reportStats();		// report parsing stats using default DAVE routine
            }
        } catch (NoSuchMethodError e) {
            System.err.println("The DAVE.jar file appears to be out-of-date; couldn't find getGenStatsFlag() method.");
            System.exit(1);
        }

        // Create creation and verification m-script files
        System.out.println("Creating POST-II input deck and model source...");
        try {
            dave2post.createModel();
        } catch (IOException e) {
            return;
        }
//        if (dave2sl.makeLib()) {
//            System.out.println("Simulink library written.");
//        } else {
//            System.out.println("Simulink model written.");
//        }

        // Generate verification script
//        if (dave2post.hasCheckcases()) {
//            System.out.println("Checkcase data found, generating verification script...");
//            dave2post.createVerifyScript();
//            System.out.println("Verification script written.");
//        }

        System.out.println("Wrote POST II source files for "
                + dave2post.getStubName() + ".");
    }
}
