// DAVE.java
//  
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.
package gov.nasa.daveml.dave;

import com.sun.org.apache.xerces.internal.util.XMLCatalogResolver;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 *
 * <b>D</b>igital <b>A</b>erospace <b>V</b>ehicle <b>E</b>xchange <b>M</b>arkup
 * <b>L</b>anguage utility routines. <p> Converts <b>DAVE-ML</b> files into
 * networked {@link Block} &amp;
 * {@link Signal} objects contained within a {@link Model} object. <p>
 * <b>DAVE-ML</b> is being considered as a possible AIAA standard for encoding
 * dynamic flight vehicle models for exchange between simulation tools and
 * facilities in an open-software, facility-neutral manner. <p> More information
 * about DAVE-ML is available at the project website:
 * {@link <a href="http://daveml.org">
 * http://daveml.org</a>} 
 * 
 * <p> Modification history: 
 * <ul> 
 * <li>020419: Written EBJ</li> 
 * <li>031220: Substantially modified for DAVE_tools 0.4</li> 
 * </ul>
 *
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * @version 0.9
 *
 *
 */
public class DAVE {
    // all fields are private unless marked

    /**
     * the version of DAVE in use
     */
    String myVersion;
    /**
     * the model we're focusing on
     */
    Model m;
    /**
     * the default namespace
     */
    Namespace ns;
    /**
     * internal argument counter
     */
    int argNum;
    /**
     * Our base identifier - used in JDOM calls
     */
    String base_uri;
    /**
     * Arguments to invocation
     */
    String args[];
    /**
     * name of the input file
     */
    String inputFileName;
    /**
     * name of the txt output file, if selected for output
     */
    String listFileName;
    /**
     * the name of the input file, less directory and filetype
     */
    String stubName;
    /**
     * Flag set if user asked us to create a listing (.txt) file
     */
    boolean makeListing;
    /**
     * Flag set if user wants to output internal (intermediate) signal values
     */
    boolean createInternalValues;
    /**
     * Flag set if user wants to do repetitive evaluations of model
     */
    boolean evaluate;
    /**
     * Flag set if user wants detailed processing information
     */
    boolean verboseFlag;
    /**
     * Flag set if user wants statistical processing information
     */
    boolean genStatsFlag;
    /**
     * Flag set if no file needs to be processed (e.g. help or verbose flags)
     */
    boolean noProcessingRequired;
    /**
     * Flag set if user has requested a help message
     */
    boolean helpRequested;
    /**
     * Flag set if user has asked to ignore checkcases
     */
    protected boolean ignoreCheckcases;
    /**
     * Internal variable definition count
     */
    int varDefCount;
    /**
     * Internal breakpoint set definition count
     */
    int bpDefCount;
    /**
     * Internal gridded table definition count
     */
    int gtDefCount;
    /**
     * Internal function definition count
     */
    int functionCount;
    /**
     * Internal check-case count
     */
    int checkCaseCount;
    /**
     * InputStream of input file
     */
    InputStream _inputStream;
    /**
     * processing timer start time, in milliseconds
     */
    long parseStartTime;        // ms
    /**
     * processing timer end time, in milliseconds
     */
    long parseStopTime;         // ms
    /**
     * the XML input file root element
     */
    Element root;
    /**
     * checkcase data structure
     */
    CheckData checkcases;
    /**
     * private return codes
     */
    private static final int exit_success = 0;
    private static final int exit_failure = 1;

    /**
     *
     * Simple constructor.
     *
     *
     */
    public DAVE() {
        this.makeListing = false;
        this.createInternalValues = false;
        this.evaluate = false;
        this.verboseFlag = false;
        this.genStatsFlag = false;
        this.noProcessingRequired = false;
	this.ignoreCheckcases = false;
        this.helpRequested = false;
        this.checkCaseCount = 0;
        this.m = new Model(20, 20);

        String date = "2015-03-02";

        // add date (now that we're under git)
        this.myVersion = "0.9.7 (" + date + ")";
    }

    /**
     *
     * Constructor that parses command arguments list.
     *
     * @param args String array containing switches, file name(s)
     *
     *
     */
    public DAVE(String args[]) {
        this();
        this.parseOptions(args);
        if (this.verboseFlag) {
            m.makeVerbose();
        }
        if (this.helpRequested) {
            this.printHelp();
        }
    }

    /**
     * Sets the debug flag.
     *
     */
    public void makeVerbose() {
        this.verboseFlag = true;
        this.m.makeVerbose();
    }

    /**
     * Unsets the debug flag.
     *
     */
    public void silence() {
        this.verboseFlag = false;
        this.m.silence();
    }

    /**
     *
     * Returns the status of the debug flag.
     *
     *
     */
    public boolean isVerbose() {
        return this.verboseFlag;
    }

    /**
     *
     * Returns the version of software
     *
     *
     */
    public String getVersion() {
        return this.myVersion;
    }

    /**
     *
     * Indicates if checkcases were included in file.
     *
     *
     */
    public boolean hasCheckcases() {
        return this.checkcases != null;
    }

    /**
     *
     * Returns the checkcase data.
     *
     *
     */
    public CheckData getCheckcaseData() {
        return this.checkcases;
    }

    /**
     *
     * Sets the input file and stub name strings.
     *
     * <p> This method needed to support packages that extend DAVE.
     *
     * @param fn the new file name
     *
     *
     */
    public void setInputFileName(String fn) {
        this.inputFileName = fn;
        this.stubName = DAVE.toStubName(fn);
    }

    /**
     *
     * Returns input file name string.
     *
     *
     */
    public String getInputFileName() {
        return this.inputFileName;
    }

    /**
     *
     * Returns the stub name string.
     *
     *
     */
    public String getStubName() {
        return this.stubName;
    }

    /**
     *
     * Returns our model.
     *
     * @return Model
     */
    public Model getModel() {
        return this.m;
    }

    /**
     *
     * Sets the 'generate statistics' flag
     *
     **/

    public void setGenStatsFlag() {
        this.genStatsFlag = true;
    }

    /**
     *
     * Returns the genStatsFlag
     *
     * @return the generate stats flag
     *
     */
    public boolean getGenStatsFlag() {
        return this.genStatsFlag;
    }

    /**
     *
     * Stores the input arguments to the command line
     *
     * @param args The input argument strings
     *
     *
     */
    public void setArgs(String args[]) {
        this.args = args;
    }

    /**
     *
     * Returns the input argument array
     *
     *
     */
    public String[] getArgs() {
        return this.args;
    }

    /**
     * Number of command line option switches provided by user
     *
     * @since version 0.8 / rev 193
     */
    public int numCmdLineSwitches() {
        int index;
        int count = 0;

        for (index = 0; index < this.args.length; index++) {
            if (this.args[index].startsWith("-")) {
                count++;
            }
        }
        return count;
    }

    /**
     * Verifies any checkcases provided in XML file.
     *
     */
    public boolean verify() {
        boolean result = true;
        int goodCases = 0;

        if (checkcases == null) { // no checkcases
            return true;
        }

        // run the model with each checkcase
        ArrayList<StaticShot> shots = checkcases.getStaticShots();
        Iterator<StaticShot> shit = shots.iterator();

        if (this.isVerbose()) {
            System.out.println("");
            System.out.println("Verifying " + shots.size() + " check-cases");
            System.out.println("------------------------");
            System.out.println("");
        }

        while (shit.hasNext()) {
            StaticShot shot = shit.next();
            if (this.isVerbose()) {
                System.out.println("");
                System.out.println("Verifying staticShot '" + shot.getName() + "'");
                System.out.println("");
            }
            try {
                VectorInfoArrayList inputVec = m.getInputVector();
                shot.loadInputVector(inputVec);       // throws DAVEException if problem

                m.cycle();      // run model with inputs

                VectorInfoArrayList outputVec = m.getOutputVector();
                if (outputVec == null) {
                    System.err.println(
                            "Null output vector returned from Model.cycle() while verifying.");
                    System.exit(exit_failure);
                }
                boolean matched = shot.checkOutputs(outputVec);
                if (!matched) {
                    System.err.println("Verification error - can't match case '"
                            + shot.getName() + "'.");
                    result = false;
                } else {
                    goodCases++;
		}
		if (matched || this.ignoreCheckcases) {
                    if (this.createInternalValues) {
                        // write internal values to file
                        String checkCaseName = this.stubName + "_"
                                + toFileName(shot.getName()) + ".txt";
                        FileOutputStream fos = new FileOutputStream(checkCaseName);
                        PrintWriter pw = new PrintWriter(fos);
                        m.generateInternalValues(pw);
                        pw.flush();
                        fos.close();
                    }
                }

            } catch (Exception e) {
                System.err.println("Problem performing verification - ");
                System.err.println(e.getMessage());
                System.exit(exit_failure);
            }
        }
        System.out.println("Verified " + goodCases + " of "
                + shots.size() + " embedded checkcases.");
        if (this.createInternalValues) {
	    if (!this.ignoreCheckcases) {
		System.out.println("Wrote internal values for each good checkcase.");
	    } else {
		System.out.println("Wrote internal values for each checkcase.");
	    }
        }
        return result;
    }

    /**
     * Creates new {@link Signal} for each variableDef in model.
     *
     */
    @SuppressWarnings("unchecked") // since Element.getChildren() method returns generic List
    public void parseVariableDefs() {
        Signal dummySig;

        List<Element> variableList = root.getChildren("variableDef", this.ns);
        if (variableList.size() > 0) {
            Iterator<Element> variableIterator = variableList.iterator();
            this.varDefCount = variableList.size();
            if (this.isVerbose()) {
                System.out.println("");
                System.out.println("Parsing " + varDefCount + " variable definitions");
                System.out.println("--------------------------------");
                System.out.println("");
            }
            while (variableIterator.hasNext()) {
                try {
                    dummySig = new Signal(variableIterator.next(), m);
                } catch (DAVEException ex) {
                    System.err.println("Exception thrown while parsing variableDefs: " +
                            ex.getLocalizedMessage());
                    System.err.println("aborting further parsing.");
                    
                }
            }
        }
    }

    /**
     *
     * Finds gridded function table definitions. <p> Create new {@link FuncTable}
     * entities, which can be shared by more than one {@link BlockFuncTable}
     * (should be renamed to
     * <code>BlockFuncLookup</code>).
     *
     *
     */
    @SuppressWarnings("unchecked")
    public void parseTableDefs() {
        FuncTable dummyFuncTable;
        List<Element> gtdList = root.getChildren("griddedTableDef", this.ns);
        if (gtdList.size() > 0) {
            Iterator gtdi = gtdList.iterator();
            this.gtDefCount = gtdList.size();
            if (this.isVerbose()) {
                System.out.println("");
                System.out.println("Parsing " + gtDefCount + " table definitions");
                System.out.println("----------------------------");
                System.out.println("");
            }
            while (gtdi.hasNext()) {
                try {
                    dummyFuncTable = new FuncTable((Element) gtdi.next(), m);
                } catch (IOException e) {
                    System.err.println(
                            "Error in DAVE.parseTableDefs() while looking for griddedTableDefs");
                    System.exit(exit_failure);
                }
            }
        }
    }

    /**
     *
     * Parses any breakpoint definitions (
     * <code>&lt;breakpointDef&gt;</code> elements). <p> Create new {@link BreakpointSet}s
     * for each definition found;
     * {@link BlockBP} creation is deferred until we know what independent
     * variables to use as inputs.
     *
     *
     */
    @SuppressWarnings("unchecked")
    public void parseBreakpointDefs() {
        BreakpointSet ignored;
        List<Element> bpList = root.getChildren("breakpointDef", this.ns);
        if (bpList.size() > 0) {
            Iterator bpIt = bpList.iterator();
            this.bpDefCount = bpList.size();
            if (this.isVerbose()) {
                System.out.println("");
                System.out.println("Parsing " + bpDefCount + " breakpoint definitions");
                System.out.println("---------------------------------");
                System.out.println("");
            }
            while (bpIt.hasNext()) {
                try {
                    ignored = new BreakpointSet((Element) bpIt.next(), m);
                } catch (Exception e) {
                    System.err.println("Error in DAVE.parseBreakpointDefs() :"
                            + e.getMessage());
                    System.exit(exit_failure);
                }
            }
        }
    }

    /**
     *
     * Finds &amp; deals with
     * <code>&lt;function&gt;</code> definitions. <p> These automatically wire
     * themselves to any existing BP output signals, generating new breakpoint
     * output signals if required, e.g. Mach_x_MACH1, (meaning variable 'Mach'
     * normalized by breakpoint set MACH1) and create their own output signal.
     *
     *
     */
    @SuppressWarnings("unchecked")
    public void parseFunctions() {
        BlockFuncTable ignored;
        List<Element> functionList = root.getChildren("function", this.ns);
        if (functionList.size() > 0) {
            Iterator functionIterator = functionList.iterator();
            this.functionCount = functionList.size();
            if (this.isVerbose()) {
                System.out.println("");
                System.out.println("Parsing " + functionCount + " function definitions");
                System.out.println("-------------------------------");
                System.out.println("");
            }
            while (functionIterator.hasNext()) {
                try {
                    ignored = new BlockFuncTable((Element) functionIterator.next(), m);
                } catch (IOException e) {
                    System.err.println("Error in DAVE.parseFunctions() ");
                    System.exit(exit_failure);
                }
            }

        }
    }

    /**
     *
     * Parses checkcase data (
     * <code>&lt;checkData&gt;</code> elements) (if any exist).
     *
     *
     */
    @SuppressWarnings("unchecked")
    public void parseCheckCases() {
        Element checkData = root.getChild("checkData", this.ns);
        if (checkData != null) {
            this.checkcases = new CheckData(checkData.getChildren("staticShot", this.ns));
            this.checkCaseCount = this.checkcases.count();
            if (this.isVerbose()) {
                System.out.println("");
                System.out.println("Parsing " + checkCaseCount + " check-cases");
                System.out.println("---------------------");
                System.out.println("");
            }
        }
    }

    /**
     *
     * Parses whole file of DAVE-ML markup. <p> This method will take the
     * supplied DAVE xml file, and convert that file into lists of {@link Signal}
     * and {@link Block}, as well as {@link BreakpointSet} and {@link FuncTable}
     * objects. <p> The procedure is to locate and create objects for each type
     * of DAVE-ML element, in sequence: <ul> <li><code>&lt;variableDef&gt;</code>s</li> <li><code>&lt;breakpointDef&gt;</code>s</li> <li><code>&lt;griddedTableDef&gt;</code>s</li> <li><code>&lt;function&gt;</code>s</li>
     * </ul> <p> An interesting quirk about building a model: the
     * <code>&lt;function&gt;</code> elements specify a table, one or more
     * breakpoint sets, and the independent variable to be normalized with the
     * particular breakpoint set(s). Thus, only when the function definition is
     * read and parsed can we construct breakpoint blocks ({@link BlockBP}s).
     * So, the function table parsing routine (
     * <code>new</code> {@link
     * BlockFuncTable}) has to do a lot of
     * <code>BlockBP</code> creation and wiring of {@link Signal}s That's why
     * creation of
     * <code>BlockBPs</code> is deferred until creation of
     * <code>BlockFuncTable</code>s.
     *
     * @throws
     * <code>IOException</code> - when errors occur.
     *
     *
     */
    public boolean parseFile()
            throws IOException {
        // Get DAVE XML file's location (in a file: URL)
        File f = new File(this.inputFileName);
        String fullURI = f.toURI().toString();
        this.base_uri = fullURI; // have to keep file name as part of base

        // Dig out model name from file name
        this.m.setName(this.stubName);

        // Hack the start time
        this.parseStartTime = System.currentTimeMillis();

        // Build XML tree

        Document doc = load();

        root = doc.getRootElement();

        // See if there is a default namespace
        this.ns = root.getNamespace();
        if (this.isVerbose()) {
            System.out.println("Root element has '" + this.ns.getURI() + "' default namespace.");
        }

        // Parse into signals and blocks --

        // Find our Signals; create a Signal for each VariableDef
        // found

        parseVariableDefs();

        // Record breakpoint sets by creating a BreakpointSet for
        // every BreakpointDef found

        parseBreakpointDefs();

        // Record tables 

        parseTableDefs();

        // Create lookup functions and associated breakpoint blocks;
        // tie breakpoints & tables together into functions.

        parseFunctions();

        // Hook all the blocks together
        // loop through model, telling each block to wire up its inputs and outputs to predefined signals.

        m.wireBlocks();

        // Create input & output blocks

        m.hookUpIO();

        // Load checkcase data (if any)
        parseCheckCases();

        // Hit the stopwatch again
        this.parseStopTime = System.currentTimeMillis();

        // Sanity check
        boolean result = m.verifyIntegrity();

        // Initialize model
        // This builds the exection order lists; was happening only for models with checkcases
        try {
            m.initialize();
        } catch (DAVEException ex) {
            Logger.getLogger(DAVE.class.getName()).log(Level.SEVERE, null, ex);
        }

        // report results
        if (this.isVerbose()) {
            System.out.println("");
            System.out.println("File parsing complete; model built.");
            System.out.println("===================================");
            System.out.println("");
        }

        return result;
    }

    /**
     * loads the JDOM document from file, resolving any catalog entries for
     * local copies of DAVEfunc.dtd. When on-line, will automatically try to
     * find on-line version (latest version) of DTD. When off-line will look for
     * a catalog.xml or catalog file in the local directory or in
     * /etc/xml/catalog to find a DTD to validate the input file.
     *
     * To use local DTD files for validation, suggest downloading and installing
     * the DAVE-ML and MathML2 DTDs in a 'schemas' subdirectory, then placing
     * the contents below in a 'catalog.xml' file in the same directory as the
     * DAVE-ML model being loaded.
     *
     * @return Document object with the parsed file
     */
// Example catalog.xml file contents
//
// <?xml version="1.0"?>
//  <!-- commented out to prevent network access
//     !DOCTYPE catalog PUBLIC "-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN"
//    "http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd" 
//   -->
// <catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
//   <group prefer="public" xml:base="">
//     <public
//	 publicId="-//AIAA//DTD for Flight Dynamic Models - Functions 2.0//EN"
//	 uri="schemas/DAVEfunc.dtd"/>
//
//     <public
//	 publicId="-//W3C//DTD MathML 2.0//EN"
//	 uri="schemas/mathml2.dtd"/>
//
//    </group>
// </catalog>
    public Document load() throws IOException {
        Document doc = null;
        XMLCatalogResolver cr = null;
        String directory_uri = this.base_uri.substring(0, this.base_uri.lastIndexOf('/'));
        String errorLine = "No error.";
        boolean tryValidationFlag = true;
        int numberOfFailures = 0;
        boolean success = false;
        while (numberOfFailures < 2 && !success) {
            success = true;
            errorLine = "Error when attempting validation...";

            // see if we can open the file first, and deal with any exceptions
            try {
                // open the XML file
                _inputStream = new FileInputStream(this.inputFileName);
                assert (_inputStream != null);
            } catch (IOException e) {
                System.err.println("Error opening file '" + this.inputFileName + "': "
                        + e.getMessage());
                System.exit(-1);
            }

            // now see if we can parse it; try with and without validation
            try {
                // Load XML into JDOM Document
                SAXBuilder builder = new SAXBuilder(tryValidationFlag);
                // must turn off Xerces desire to load external DTD regardless of validation
                builder.setFeature(
                        "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                if (tryValidationFlag) {
                    String[] catalogs;
                    // environment variable XML_CATALOG_FILES trumps properties file
                    String xml_catalog_files = System.getenv("XML_CATALOG_FILES");
                    if (xml_catalog_files == null) {
                        xml_catalog_files = System.getProperty("xml.catalog.files");
                    }
                    if (xml_catalog_files != null) {
                        catalogs = new String[]{xml_catalog_files, "catalog.xml", "catalog", "file:/etc/xml/catalog"};
                    } else {
                        catalogs = new String[]{"catalog.xml", "catalog", "file:/etc/xml/catalog"};
                    }
                    cr = new XMLCatalogResolver(catalogs);


//                    // here to test stuff
//                    
//                    cr.setUseLiteralSystemId(false);
//                    
//                    boolean preferPublic = cr.getPreferPublic();
//                    if (preferPublic)
//                        System.out.println("Prefer public");
//                    else
//                        System.out.println("Prefer system");
//                    
//                    System.out.println("call to resolvePublic for DAVE-ML models gives:");
//                    System.out.println(cr.resolvePublic("-//AIAA//DTD for Flight Dynamic Models - Functions 2.0//EN",
//                            "http://www.daveml.org/DTDs/2p0/DAVEfunc.dtd"));
//                    
//                    System.out.println();
//                    
//                    System.out.println("call to resolvePublic for MathML2 namespace gives:");
//                    System.out.println(cr.resolvePublic("-//W3C//DTD MathML 2.0//EN",
//                            "http://www.w3.org/Math/DTD/mathml2/mathml2.dtd"));
//                            
                } else {
                    cr = null;
                }
                builder.setEntityResolver(cr);
                doc = builder.build(_inputStream, directory_uri);
            } catch (java.io.FileNotFoundException e) {
                errorLine = errorLine + e.getMessage();
                numberOfFailures++;
                success = false;
            } catch (java.net.UnknownHostException e) {
                errorLine = errorLine + " (network unavailable)";
                numberOfFailures++;
                success = false;
             } catch (java.net.ConnectException e) {
                errorLine = errorLine + " (connection timed out)";
                numberOfFailures++;
                success = false;
            } catch (java.io.IOException e) {
                errorLine = errorLine + " (general I/O problem)";
                numberOfFailures++;
                success = false;
            } catch (JDOMException e) {
                errorLine = errorLine + "\n" + e.getMessage();
                numberOfFailures++;
                success = false;
            }
            if (numberOfFailures == 1 && !success) {
                System.err.println(errorLine);
                System.err.println("Proceeding without validation.");
                tryValidationFlag = false;
                _inputStream.close();
            }
        }
        if (!success && numberOfFailures >= 2) {
            _inputStream.close();
            System.err.println(errorLine);
            System.err.println("Unable to load file successfully (tried with and without validation), aborting.");
            System.exit(-1);
        }
//        if (success && this.isVerbose()) {
        if (success) {
            System.out.println("Loaded '" + this.inputFileName + "' successfully, ");
            org.jdom.DocType dt = doc.getDocType();
            switch (numberOfFailures) {
                case 0: // validated against some DTD
                    System.out.print("Validating against '");
                    String catalogURI = cr.resolvePublic(dt.getPublicID(), dt.getSystemID());
                    if (catalogURI == null) {
                        System.out.print(dt.getSystemID());
                    } else {
                        System.out.print(catalogURI);
                    }
                    System.out.println(".'");
                    break;
                case 1: // no validation
                    System.out.println("WITHOUT validation.");
            }
        }

        return doc;
    }

    /**
     *
     * Describes our model.
     *
     * Records information about the {@link Model} on the specified text file
     *
     *
     */
    public void describeSelf() {
        try {
            TreeFileWriter writer = new TreeFileWriter(this.listFileName);
            writer.describe(m);
            writer.close();
        } catch (IOException e) {
            return;
        }
    }

    /**
     *
     * Generates parsing statistics on
     * <code>stdout</code>.
     *
     *
     */
    public void reportStats() {
        System.out.println("File parsing statistics:");
        System.out.println("          Number of variable definitions: " + this.varDefCount);
        System.out.println("        Number of breakpoint definitions: " + this.bpDefCount);
        System.out.println("     Number of gridded table definitions: " + this.gtDefCount);
        System.out.println("          Number of function definitions: " + this.functionCount);
        if (this.checkCaseCount > 0) {
            System.out.println("        Number of check-case definitions: " + this.checkCaseCount);
        }
        System.out.println();
        System.out.println("              Parsing took "
                + (this.parseStopTime - this.parseStartTime) / 1000.0
                + " seconds.");
        System.out.println();

        m.reportStats();        // report implementation stats
    }

    /**
     *
     * Static method that removes path and filetype from pathname.
     *
     * @param inString
     * <code>String</code> containing filename with possible extension
     *
     *
     */
    static String toStubName(String inString) {

        File F = new File(inString);
        String name = F.getName();              // strips pathname from filename

        StringBuilder buf = new StringBuilder(name);
        String stubName;

        stubName = null;

        int dotIndex = buf.length();    // point to last char
        while (dotIndex > 0) {
            dotIndex--;
            if (buf.charAt(dotIndex) == '.') // look for file type sep
            {
                break;
            }
        }

        if (dotIndex > 0) {
            stubName = buf.substring(0, dotIndex);
        }

        return stubName;
    }

    /**
     *
     * Static method that turns punctuated sentence into filename. <p> Creates
     * acceptable filename by <ul> <li> converting whitespace to single
     * underscores</li> <li> removing commas and periods</li> <li> limiting
     * result to 32 chars (arbitrary)</li> </ul>
     *
     * @param inString contains sentence or phrase to convert
     *
     *
     */
    static String toFileName(String inString) {
        int limit = 32; // max file name length

        StringBuilder buf = new StringBuilder(inString);
        StringWriter fileName = new StringWriter(limit);

        for (int i = 0; (i < buf.length()) && (fileName.getBuffer().length() < limit); i++) {
            char c = buf.charAt(i);
            switch (c) {
                case '.':
                case ',':
                case ';':
                case ':':
                case '\'':
                case '"':
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                    break;
                case ' ':   // ignore whitespace (space & tab)
                case '\t':
                    fileName.write('_');
                    break;
                default:
                    fileName.write(c);
            }
        }
        return fileName.toString();
    }

    /**
     * Requests new model inputs from user. <p> Will return -1 if user signals
     * end-of-input (
     * <code>^d</code>)
     *
     * @param inVec A {@link VectorInfoArrayList} listing inputs and default
     * values
     * @return int -1 to quit, 0 to keep going
     * @throws IOException;
     *
     *
     */
    public int loadInputs(VectorInfoArrayList inVec) throws IOException {
        int tokenType;

        // The following mumbo-jumbo necessary to take stdin and parse it
        FileReader frin = new FileReader(FileDescriptor.in);
        StreamTokenizer st = new StreamTokenizer(frin);

        // Tell parser to look for double-precision numbers and EOLs
        // Note - can't use exponential notation; only 0-9, -, .
        st.parseNumbers();
        st.eolIsSignificant(true);

        // Write header
        System.out.println();
        System.out.println(" Specify input values:");

        // Loop for each input block that might need value
        Iterator<VectorInfo> in = inVec.iterator();
        while (in.hasNext()) {
            boolean blankLine = false;
            VectorInfo inVal = in.next();

            // write name, units, default value
            System.out.print("   " + inVal.getName() + " (" + inVal.getUnits() + ") ["
                    + inVal.getValue() + "]  : ");

            // look for number in input stream; abort on EOF; skip to next on EOL
            do {        // look for number or EOL or EOF
                tokenType = st.nextToken();
                // System.out.println("tokenType was " + tokenType);
                if (tokenType == StreamTokenizer.TT_EOF) {
                    return -1;   // quit
                }
                if (tokenType == StreamTokenizer.TT_EOL) {    // skip to next param
                    blankLine = true;
                    break;
                }
            } while (tokenType != StreamTokenizer.TT_NUMBER); // keep looking until number found

            if (!blankLine) {
                // if not empty line, interpret number and save in block
                //              System.out.println("Input value was " + st.nval);
                try {
                    inVal.setValue(st.nval);
                    //              System.out.println("setValue called for " + inVal.getName() + " with value of " + st.nval);
                } catch (NumberFormatException e) {
                    // take no action - leave value as is
                }

                // look for EOL so we can ignore it
                do {
                    tokenType = st.nextToken();
                    //              System.out.println("skipping tokenType " + tokenType );
                    if (tokenType == StreamTokenizer.TT_EOF) {
                        return -1;
                    }
                } while (tokenType != StreamTokenizer.TT_EOL);
            }
        }
        return 0;       // indicate keep going
    }

    /**
     *
     * Reports contents of model output vector to user on
     * <code>stdout</code>.
     *
     * @param outVec A
     * <code>VectorInfoArrayList</code> listing output names and values
     *
     *
     */
    public void listOutputs(VectorInfoArrayList outVec) {
        System.out.println();
        System.out.println("Output values are:");
        Iterator<VectorInfo> it = outVec.iterator();
        while (it.hasNext()) {
            VectorInfo vi = it.next();
            System.out.print("  " + vi.getName() + " = " + vi.getValue());

            // add units if any
            if (vi.getUnits().length() > 0) {
                System.out.println(" (" + vi.getUnits() + ")");
            } else {
                System.out.println();
            }

        }
        System.out.println();
        //      this.describeSelf();
    }

    /**
     *
     * Reports contents of model internals vector to user on
     * <code>stdout</code>.
     *
     * @param outVec A
     * <code>VectorInfoArrayList</code> listing output names and values
     *
     *
     */
    public void listInternals(VectorInfoArrayList outVec) {
        System.out.println();
        System.out.println("Intermediate values are:");
        Iterator<VectorInfo> it = outVec.iterator();
        while (it.hasNext()) {
            VectorInfo vi = it.next();
            System.out.print("  " + vi.getName() + " = " + vi.getValue());

            // add units if any
            if (vi.getUnits().length() > 0) {
                System.out.println(" (" + vi.getUnits() + ")");
            } else {
                System.out.println();
            }

        }
        System.out.println();
        //      this.describeSelf();
    }

    /**
     *
     * Matches each option argument by unique part of command. <p> Looks for any
     * argument that begins with "-" and a unique part of a command option. Sets
     * field
     * <code>argNum</code> with the index of the matched argument, so argument
     * value (if any) can be retrieved. Returns
     * <code>true</code> if option is matched. <p> Expanded to look for
     * fullOptionString if preceded with '--'
     *
     * @param uniqueOptionStart
     * <code>String</code> with unique first part of option
     * @param fullOptionString
     * <code>String</code> for full option name if user insists on providing mor
     * than unique part
     * @return boolean
     *
     *
     */
    protected boolean matchOptionArgs(String uniqueOptionStart, String fullOptionString) {
        boolean matched = false;
        int index;

        //      System.out.println();
        //      System.out.println("Trying to match '-" + uniqueOptionStart + "'...");

        for (index = 0; index < this.args.length; index++) {
            //      System.out.println(" examining argument " + index + ": '"
            //                         + this.args[index] + "'");
            // look for starting '--' for full option string
            if (this.args[index].startsWith("--")) {
                //              System.out.println(" found full option argument: " + this.args[index]);
                if (this.args[index].startsWith("--" + fullOptionString)) {
                    matched = true;
                    this.argNum = index;
                }
            }
            // look for starting '-' to identify single-character argument
            if (this.args[index].startsWith("-")) {
                //              System.out.println(" found option argument: " + this.args[index]);
                if (this.args[index].startsWith("-" + uniqueOptionStart)) {
                    matched = true;
                    this.argNum = index;
                }
            }
        }
        //      System.out.println("Match found = " + matched);
        return matched;
    }

    /**
     * Generate usage information
     *
     * @since version 0.8.1 / rev 397
     *
     */
    private void printHelp() {
        System.out.println("Usage: java DAVE [options] DAVE-ML_document");
        System.out.println("");
        System.out.println("  where options is one or more of the following:");
        System.out.println("");
        System.out.println("    --version      (-v)    print version number and exit");
        System.out.println("    --count        (-c)    count number of elements");
        System.out.println("    --debug        (-d)    generate debugging information");
        System.out.println("    --eval         (-e)    do prompted model I/O evaluation");
        System.out.println("    --list         (-o)    output text description to optional output file");
        System.out.println("    --internal     (-i)    show intermediate results in calcs and checkcases");
	System.out.println("    --no-checkcase (-x)    ignore failing checkcases");
        System.out.println("");
    }

    /**
     * Parses any command-line options given.
     *
     */
    private void parseOptions(String inArgs[]) {
        String exampleUse = "Usage: java DAVE [-v][-c][-d][-e][-h][-x][-i] [-o [Text_output_file]] DAVE-ML_document";
        int numArgs = inArgs.length;

        // Make sure we have at least the input file

        if (numArgs < 1) {
            System.out.println(exampleUse);
            System.out.println("Need at least one argument.");
            System.exit(0);
        }

        // Save arguments into field
        this.setArgs(inArgs);

        // Retrieve input file name
        this.inputFileName = this.args[numArgs - 1];

        // Don't deal with stub name if no filename provided
        if (this.inputFileName.startsWith("-")) {
            numArgs++;  // adjust so we'll look at first argument
        } else {
            // Generate stub file name and list file name
            this.stubName = DAVE.toStubName(this.inputFileName);
            this.listFileName = this.stubName + ".txt";
        }

        // Parse remaining options
        if (numArgs > 1) {
            int parsedArgs = 0;
            if (matchOptionArgs("c", "count")) {
                this.genStatsFlag = true;
                parsedArgs++;
            }
            if (matchOptionArgs("d", "debug")) {
                this.verboseFlag = true;
                parsedArgs++;
            }
            if (matchOptionArgs("e", "eval")) {
                this.evaluate = true;
                parsedArgs++;
            }
            if (matchOptionArgs("i", "internal")) {
                this.createInternalValues = true;
                parsedArgs++;
            }
            if (matchOptionArgs("o", "list")) {
                this.makeListing = true;
                if (numArgs > (this.argNum + 2)) {
                    if (!this.args[this.argNum + 1].startsWith("-")) {
                        this.listFileName = this.args[this.argNum + 1];
                    }
                }
                parsedArgs++;
            }
            if (matchOptionArgs("v", "version")) {
                this.noProcessingRequired = true;
                System.out.println("DAVE version " + getVersion());
                System.exit(0);
            }
            if (matchOptionArgs("x", "no-checkcase")) {
                this.ignoreCheckcases = true;
		parsedArgs++;
		System.out.println("Ignoring checkcases");
            }
            if (matchOptionArgs("h", "help")) {
                this.noProcessingRequired = true;
                this.helpRequested = true;
                parsedArgs++;
            }

            int numSwitches = this.numCmdLineSwitches();

            if (parsedArgs < numSwitches) {
                System.err.print("Unable to understand ");
                if (numSwitches == 1) {
                    System.err.println("and parse option switch '"
                            + this.args[this.argNum] + "'.");
                } else if (numSwitches == 2) {
                    System.err.println("and parse both option switches.");
                } else {
                    System.err.println("and parse all " + numSwitches + " option switches.");
                }
                System.err.println(exampleUse);
                System.exit(0);
            }
        }
    }

    /**
     *
     * Provides a static entry point for running DAVE as standalone utility.
     *
     *
     * @param args
     */
    public static void main(String args[]) {

        boolean success = false;

        DAVE dave = new DAVE(args);

        if (dave.noProcessingRequired) {
            System.exit(exit_success);  // short circuit if just help requested
        }
        try {
            success = dave.parseFile();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // quit now if problems in parsing
        if (!success) {
            System.exit(exit_failure);
        }

        // If checkcase data is included, run quick verification
        if (dave.checkcases != null) {
            if (dave.ignoreCheckcases) {
                System.out.println("(Verification cases(s) ignored.)");
            } else if (!dave.verify()) {
                System.exit(exit_failure);
            }
        }

        // Did user ask for stats?
        if (dave.genStatsFlag) {
            dave.reportStats();         // report parsing stats
        }

        // Did user ask for a listing?
        if (dave.makeListing) {
            dave.describeSelf();
        }

        // Did user ask to perform evalution?
        if (dave.evaluate) {
            while (true) {      // run until ^D entered
                try {
                    VectorInfoArrayList inputVec = dave.m.getInputVector();
                    int eof = dave.loadInputs(inputVec);
                    if (eof == -1) {
                        System.out.println();
                        System.exit(exit_success);
                    }

                    dave.m.cycle();     // run model with inputs

                    VectorInfoArrayList outputVec = dave.m.getOutputVector();
                    if (outputVec == null) {
                        System.err.println(" Null output vector returned from Model.cycle() ");
                        System.exit(exit_failure);
                    }
                    dave.listOutputs(outputVec);
                    
                    if (dave.createInternalValues) {
                        VectorInfoArrayList internalVec = dave.m.getInternalsVector();
                        if (internalVec == null) {
                            System.err.println(" Null internal results vector returned from Model.cycle() ");
                            System.exit(exit_failure);
                        }
                        dave.listInternals(internalVec);
                    }
    
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}
