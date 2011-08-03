// BlockFuncTable
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave;


/**
 * 
 * <p> Object representing a linear interpolation block. </p>
 *
 * <p> Despite misleading name, the actual table is stored as a
 *     FuncTable block and is referenced by this Block.</p>
 *
 * @author Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 **/

import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.io.Writer;
import java.io.IOException;

/**
 *
 * <p>  The Function Table Block represents a nonlinear, multidimensional function. </p>
 *
 **/

public class BlockFuncTable extends Block
{
    /**
     *  if true, have no local breakpoint IDs - only varIDs
     */

    boolean simple;

    /**
     *  ID of gridded data table
     */

    String gtID;

    /**
     *  points to function table definition stored elsewhere
     */

    FuncTable functionTableDef;

    /**
     *  description of function
     */

    String description;

    /**
     *  name of function definition ("unnamed" for simple functions)
     */

    String functionDefName;

    /**
     * Default namespace of parent <code>&lt;root&gt;</code> element
     */
    
    Namespace ns;


    /**
     *
     * <p> Constructor for BlockFuncTable </p>
     *
     * @param function  Top-level Element function definition
     * @param m <code>Model</code> to which we attach
     * @throws <code>IOException</code>
     *
     **/

    public BlockFuncTable( Element function, Model m) throws IOException
    { 
	// Save our name, generate default scalar input and output ports.
	super(function.getAttributeValue("name"), "function table", 3, m);

	// Save XML namespace of element
	this.ns = function.getNamespace();

	// Parse description, if any
	if (function.getChild("description",this.ns) != null)
	    this.description = function.getChild("description",this.ns).getTextTrim();
	else
	    this.description = "No description";

	// default values for function defn name
	this.functionDefName = "unnamed";

	// Functions contain either a functionDefn or dependentVarPts

	// Get name of function definition, if any
	if (function.getChild("functionDefn",this.ns) != null)

	    // Here when function defn found, not simple table
	    // but may have a griddedTableDef, a griddedTableRef,  or an obsolete griddedTable.
	    {
		this.simple = false;
		this.functionDefName = function.getChild("functionDefn",this.ns).getAttributeValue("name");
		if( this.functionDefName == null ) {
		    // create automatic, unique name
		    this.functionDefName = "auto_fn_" + this.ourModel.getNumBlocks();
		}
		this.setName(this.functionDefName);	// substitute function name (but why?)

		// find our table info - one of these should be non-null
		Element gt  = function.getChild("functionDefn",this.ns).getChild("griddedTable",this.ns);
		Element gtd = function.getChild("functionDefn",this.ns).getChild("griddedTableDef",this.ns);
		Element gtr = function.getChild("functionDefn",this.ns).getChild("griddedTableRef",this.ns);
		
		// look for actual function table
		if (gt != null) {
		    String internalID = this.getName() + "_internal_table";
		    this.functionTableDef = new FuncTable( internalID, gt, m );	// griddedTable
		} else if (gtd != null) {
		    this.functionTableDef = new FuncTable( gtd, m );	// griddedTableDef
		} else if (gtr != null) {
		    this.gtID = gtr.getAttributeValue("gtID");
		    // find previously defined table
		    this.functionTableDef = this.ourModel.getTableByID( this.gtID );
		    if (this.functionTableDef == null) { // not found
			System.err.println("Error: function table definition " + this.functionDefName 
					   + " could not locate previous definition for table "
					   + this.gtID);
			System.exit(0);
		    }
		} else {	// error
		    System.err.println("Error: function table definition " + this.functionDefName 
				       + " has no gridded table information (def or ref).");
		    System.exit(0);
		}

		// register ourself with the table
		this.functionTableDef.register( this );

		// Parse and record variable IDs
		parseVarIDsFromFunctionElement(function);
	    }

	else

	    // Here with simple table
	    {
		this.simple = true;
		if (function.getChild("dependentVarPts",this.ns) == null) {
		    System.err.println("Bad DAVE-ML syntax in function '" + this.myName + "'" );
		    System.err.println("  Found neither functionDefn nor dependentVarPts. Should have one or other.");
		    System.exit(0);
		} else
		    parseSimpleFunction( function );
	    }

	// Hook up to output signal
	hookUpOutput(function);

	// Hook up to breakpoint output signal or create them if necessary; also set each dimension
	hookUpInputs();

    }


    /**
     *
     * Copy constructor.
     *
     **/

    public BlockFuncTable( BlockFuncTable b ) 
    {
	super( b );		// call Block copy constructor
	// copy all BlockFuncTable-specific fields
	this.simple              = b.simple;
	this.gtID                = b.gtID;
	this.functionTableDef    = b.functionTableDef;
	this.description         = b.description;
	this.functionDefName     = b.functionDefName;
    }


    /**
     *
     * Return pointer to function table 
     *
     **/

    public FuncTable getFunctionTableDef() { return this.functionTableDef; }


    /**
     *
     * <p> Parse elements of a simple function (no explicit breakpoint
     * refs or table refs/defs). Must create & hook up own breakpoint objects. </p>
     *
     * @param function JDOM "function" element
     * @throws IOException
     **/

    @SuppressWarnings("unchecked")
	void parseSimpleFunction( Element function ) throws IOException
    {
	// calling method has already confirmed this child exists.
	Element outTable = function.getChild("dependentVarPts",this.ns);

	// get varIDs of input variables
	List<Element>     iVarPts         = function.getChildren("independentVarPts",this.ns);
	Iterator<Element> iVarPtsIterator = iVarPts.iterator();
	
	// create automatic breakpoint name/ID for this input variable
	String funcTableName = "auto_" + this.myName + "_table";

	// create new funcTable from simple table info
	this.functionTableDef = 
	    new FuncTable( funcTableName, funcTableName, outTable.getTextTrim(), "", 
			   iVarPts.size(), ourModel);

	if (this.functionTableDef == null) {
	    System.err.println("Unable to create new FuncTable from simple table");
	    System.exit(0);
	}

	this.functionTableDef.register( this );	// register ourself

	this.outVarID = outTable.getAttributeValue("varID");

	// Since this is a simple table, we need to create unique breakpoint sets

	int i = 1;
	while (iVarPtsIterator.hasNext()) {

	    // get name of independent variable
	    Element iVarPtsElement = iVarPtsIterator.next();
	    String inVarID = iVarPtsElement.getAttributeValue("varID");
	    this.addVarID(i, inVarID);

	    if (this.isVerbose())
		System.out.println("Added input varID '" + inVarID + "' for simple table function "
				   + this.functionDefName);

	    // create automatic breakpoint name/ID for this input variable
	    String bpName = "auto_" + this.myName + "_bpID_" + i;

	    // record fake breakpoint ID for future creation
	    this.functionTableDef.addBPID( i-1, bpName );

            // create new  breakpoint set
	    Element inTable = function.getChild("independentVarPts",this.ns);
	    if (inTable == null) {
		System.err.println("No breakpoint values found in simple function defn of "
				   + this.functionDefName);
		System.exit(0);
	    }

	    String bpDescription = 
		new String("Automatic breakpoint set created from simple table element named " 
			   + this.getName());
	    try {
		new BreakpointSet( bpName, bpName, inTable.getTextTrim(), bpDescription , this.ourModel);
	    } catch (DAVEException e) {
		System.err.println("Unable to create new breakpoint set named '"
				   + bpName + "' to support function '" 
				   + this.getName() + "':");
		System.err.println(e.getMessage());
		System.exit(0);
	    }

	    if (this.isVerbose())
		System.out.println("Created new BreakpointSet named '" + bpName
				   + "' for simple function " + this.functionDefName);

	    i++;	// increment count
	}
	//  tell our table to determine it's dimensionality (from bpIDs previously loaded)

	this.functionTableDef.setDimensions();
    }


    /**
     *
     * <p> Finds and saves a list of independent variable references found in an
     *     <code>function</code> element
     *
     * @param function JDOM "function" element
     *
     **/

    @SuppressWarnings("unchecked")
	protected void parseVarIDsFromFunctionElement( Element function )
    {

	// record input (independent) variable IDs
	List<Element>     iVarRefs        = function.getChildren("independentVarRef",this.ns);
	Iterator<Element> iVarRefIterator = iVarRefs.iterator();

	int i = 0;
	while (iVarRefIterator.hasNext())
	    {
		// get ID of independent variable
		Element iVarRefElement = iVarRefIterator.next();
		String theVarID = iVarRefElement.getAttributeValue("varID");
		this.addVarID(i+1, theVarID);	// must be in order
		if( this.isVerbose())
		    System.out.println("Added varID " + theVarID);

		i++;
	    }

	// record output (dependent) variable IDs
	Element outVar = function.getChild("dependentVarRef",this.ns);
	this.outVarID = outVar.getAttributeValue("varID");
    }


    /**
     *
     * <p> Using specified output (dependent) variable name, look for
     * such a declared variable. If not found, create appropriate
     * one. </p>
     *
     * @param function JDOM function Element
     *
     **/

    protected String hookUpOutput( Element function )
    {

	// Parse and discover dependent variable ID

	Element depVar      = function.getChild("dependentVarRef",this.ns);
	if (depVar == null)
	    depVar = function.getChild("dependentVarPts",this.ns);	// simple table
	String depVarID   = depVar.getAttributeValue("varID");

	Iterator<Signal> sigIterator = this.ourModel.getSignals().iterator();
	boolean depVarSignalFound = false;
	Signal dVsig = null;

	// Look for existing variable definition (signal)

	while (sigIterator.hasNext()) {
	    // look for matching explicit signal
	    dVsig = sigIterator.next();
	    if (depVarID.equals(dVsig.getVarID())) {
		depVarSignalFound = true;
		break;
	    }
	}

	/*
	// if not found, make our own
	if (!depVarSignalFound) {
	    // create and connect to new signal
	    dVsig = new Signal( depVarName, depVarName, "unkn", 10, m );
	}
	*/

	// if not found, complain
	if (!depVarSignalFound) {
	    System.err.println("Unable to locate output signal with ID '" + depVarID
			       + "' for Function block '" + this.getName() + "'.");
	    System.exit(0);
	}

	try {
	    this.addOutput(dVsig);	// connect to new or existing signal
	} catch (DAVEException e) {
	    System.err.println("Unexpected error: new Function block '" + this.getName() 
			       + "' is unable to hook up to output signal ID '"
			       + depVarID + "':");
	    System.err.println(e.getMessage());
	    System.exit(0);
	}

	return dVsig.getName();
    }


    /**
     *
     * <p> Create a new breakpoint block and associated
     * index-and-weight signal to serve as an input to this block. </p>
     *
     **/

    protected void createAndHookUpIWPath( String bpID, String varID, 
					 String iwSignalID, int portNum ) {

	Signal connector = null;	// signal wire to join Breakpoint to Function

	// Look at predeclared signals to match breakpoint input varID
	// to get name & units
	if (this.isVerbose())
	    System.out.print("Looking for input signal named '" + varID + "'...");			

	Signal theBPInputSignal = ourModel.getSignals().findByID( varID );

	if( theBPInputSignal != null ) {
	    if (this.isVerbose())
		System.out.println(" found it.");
	    // create and connect to new intermediate signal
	    String connectorName	= theBPInputSignal.getName() + "_by_" + bpID;
	    String units		= theBPInputSignal.getUnits();
	    if (this.isVerbose())
		System.out.println("Creating new index-and-weights signal named '" 
				   + connectorName + "' with a varID of '"
				   + iwSignalID + "' and units of '" + units + "'");
	    connector = new Signal( connectorName, iwSignalID, units, 2, ourModel );
	    connector.setAutoFlag();	// flag as an automatic variable
	    connector.addSink( this, portNum+1 );	// hook up to new signal
	} else {
	    // else block - error
	    if( this.isVerbose())
		System.out.println(" DIDN'T FIND IT!! - ERROR!");
			
	    System.err.println("Error: in BlockFuncTable.createAndHookUpIWPath() for Function block '" 
			       + this.getName() + "', can't find independent (input) variable with ID '"
			       + iwSignalID + "'.");
	    System.exit(0);
	}

	// Create new breakpoint block to generate the index-and-weights signal

	try {
	    new BlockBP( bpID, bpID, theBPInputSignal, connector, ourModel );
	} catch (DAVEException e) {
	    System.err.println("BlockFuncTable.createAndHookUpIWPath: in hooking up Function block '"
			       + this.getName() + "':");
	    System.err.println(e.getMessage());
	    System.exit(0);
	}
    }


    /**
     *
     * <p> Hook up to specified breakpoint blocks. Note that a
     *  breakpoint vector can be used by more than function block, for
     *  example, left and right aileron deflections may use same
     *  breakpoint values but be normalized by different values when
     *  running. Therefore, we create a unique block name for the
     *  breakpoint block that combines the breakpoint set name with
     *  the independent value name. this assures we are free to reuse
     *  an offset-and-index (normalized breakpoint) when they have the
     *  same combined name. </p>
     *
     **/

    protected void hookUpInputs()
    {
	int portCount = 0;

	// Parse and discover independent variable IDs

	Iterator<String> iVarIDIterator = this.getVarIDIterator();
	Iterator<String> bpIDIterator   = this.functionTableDef.getBPIterator();
	String signalVarID = null;

	if( this.isVerbose())
	    System.out.println("In BlockFuncTable.hookUpInputs() method for BFT "
			       + this.myName );

	while (bpIDIterator.hasNext()) {

	    // get name of signal associated with this breakpoint

	    String bpID = bpIDIterator.next();
	    if( this.isVerbose())
		System.out.print(" Looking for varID corresponding to bpID '" 
				 + bpID + "'");
	    if( !iVarIDIterator.hasNext() ) {
		System.err.println("BlockFuncTable.hookUpInputs(): Unexpected end of VarID Array in Function block");
		System.err.println("'" + this.getName() + "' while looking for bpID '" + bpID + "'.");
		System.err.println("Check to make sure the function definition has the same independent variables");
		System.err.println("as the tabel definition.");
		System.exit(0);
	    } else {
		// get corresponding independent variable ID
		signalVarID = iVarIDIterator.next();
		if (this.isVerbose())
		    System.out.println("; found corresponding varID '" 
				       + signalVarID + "'");
	    }

	    // combine independent variable ID with breakpoint ID
	    // "index-and-weight" signal

	    String iwSignalID = signalVarID + "_x_" + bpID;
	    if (this.isVerbose())
		System.out.println(" now looking for combined signal '" 
				   + iwSignalID + "'");

	    // look for existing signal from previously built breakpoint block

	    Signal theSignal = ourModel.getSignals().findByID( iwSignalID );
	    if (theSignal != null) {
		theSignal.addSink( this, portCount+1 );	// does double link
		if (this.isVerbose())
		    System.out.println(" found combined signal '" 
				       + iwSignalID + "'; added to port " 
				       + (portCount+1));
	    } else {
		// Signal not found, create it and it's upstream breakpoint block
		if (this.isVerbose())
		    System.out.println(" signal '" + iwSignalID 
				       + "'not found; creating it");

		createAndHookUpIWPath( bpID, signalVarID, iwSignalID, portCount );
	    }
	    portCount++;
	}
    }


    /**
     *
     * Reorders input signal assignments to match Simulink MDL
     * order (utilty method only needed for Simulink model generation)
     *
     * <p>
     * Put in this class due to need to access lots of internals.
     *
     **/

    public void reorderInputsForMDL() {

	// If only one or two inputs, no need to change anything

	if (this.numInputs() > 2) {
	
	    // move 1,   2,   3, ... n-2, n-1, n to 
	    //      n, n-1, n-2, ...   3,   1, 2
	    // note first two are in reverse order
	    // This is due to Matlab order requirements
	    
	    // loop through inputs
	    Iterator<Signal> theInputsIterator = this.getInputIterator();

	    while ( theInputsIterator.hasNext() ) {
		Signal theInput = theInputsIterator.next();

		// find our index into the signal's destination
		// ports
		BlockArrayList destBlocks = theInput.getDests();
		ArrayList<Integer> destPorts = theInput.getDestPortNumbers();

		// Look for ourself in the destBlocks & record
		// corresponding port index

		Iterator<Block> destBlockIterator = destBlocks.iterator();
		Iterator<Integer> destPortIterator  = destPorts.iterator();
		int thePortIndex = 0;
		while( destBlockIterator.hasNext() ) {
		    Block destBlock  = destBlockIterator.next();
		    Integer destPort = destPortIterator.next();

		    if (destBlock == this) {

			// set value for new index

			int oldPortNumber = destPort.intValue();
			int newPortNumber = (this.numInputs() + 1) - oldPortNumber;
			if(oldPortNumber == (this.numInputs()-1)) newPortNumber = 1;
			if(oldPortNumber == (this.numInputs()  )) newPortNumber = 2;
			theInput.setPortNumber( thePortIndex, newPortNumber );
		    }
		    thePortIndex++;
		}
	    }
	}
    }


    /** 
     *
     * <p> This method directs output to designated Writer </p>
     *
     * @param writer <code>Writer</code> to receive values
     * @throws <code>IOException</code>
     *
     **/

    public void printTable( Writer writer )
	throws IOException
    {
    	// just a wrapper function for more elementary FuncTable method of same name
    	FuncTable gft = this.getFunctionTableDef();
    	if (gft != null)
    		gft.printTable(writer);
    }


    /**
     *
     * <p> Generates written description of current instance on output stream </p>
     *
     * @param writer FileWriter instance to write to
     * @throws <code>IOException</code>
     *
     **/

    public void describeSelf(Writer writer) throws IOException
    {
	super.describeSelf(writer);
	writer.write(" and is a function table block with " 
		     + this.functionTableDef.size() + " table points.");
    }


    /**
     *
     * <p> Implements update() method </p>
     * @throws DAVEException
     *
     **/

    public void update() throws DAVEException
    {
	int numInputs;
	Iterator<Signal> theInputs;
	Signal theInput;
	double[] iwv;	// index and weights vector
	int[]    iv;	// index vector

	int index = 0;
	boolean ready = true;

	boolean verbose = this.isVerbose();

	if (verbose) {
	    System.out.println();
	    System.out.println("Entering update method for function '" + this.getName() + "'");
	}

	// sanity check to see if number of inputs matches our dimensionality
	numInputs = this.inputs.size();
	if (numInputs != this.functionTableDef.numDim())
	    throw new DAVEException("Number of inputs doesn't match function dimensions in '" 
				    + this.getName() + "'");

	// see if each input variable is ready
	theInputs = this.inputs.iterator();
	iwv = new double[numInputs];	// index and weights vector
	iv  = new int[numInputs];	// index vector
	if (verbose)
	    System.out.println(" Allocated index-and-weights vector of size " + this.inputs.size());

	// Here to do table lookup
	while (theInputs.hasNext()) {
	    theInput = theInputs.next();
	    if (!theInput.sourceReady()) {
		ready = false;
		if (verbose)
		    System.out.println(" Upstream signal '" + theInput.getName() + "' is not ready.");
		iwv[index] = 0.0;
		iv[index] = 0;
	    } else {
		iwv[index] = theInput.sourceValue();
		iv[index] = (int) theInput.sourceValue();
		if (verbose)
		    System.out.println(" Input # " + index + " value is " + iwv[index]);
	    }
	    index++;
	}
	if (!ready) return;

	// At this point we have the index-and-weights vector in iwv.
	// Call recursive interpolation routine
	this.value = this.interpolate( iwv, iv, numInputs );

	if (verbose)
	    System.out.println(" Interpolate returned value " + this.value);

	// record current cycle counter
	resultsCycleCount = ourModel.getCycleCounter();

    }

    
    /**
     * <p> Performs interpolation based on normalized breakpoint vector </p>
     *
     **/

    private double interpolate( double[] index_and_weights, int[] indices, int interpDimension )
    {
	int select;
	double weight;
	double a;
	double b;
	double value;
	boolean verbose = this.isVerbose();

	if (verbose) {
	    System.out.println();
	    System.out.println("Entering interpolate method for function '" + this.myName + 
			       "' with interpDimension set to " + interpDimension);
	}
	select = indices.length - interpDimension;	// 3, 2, 1, 0

	// Check for index at upper limit; adjust so it uses lower bp
	if (indices[ select ] >= (this.functionTableDef.dim(select)-1)) {	// 0-index adjust 
	    indices[ select ]--;
	    weight = 1.0;
	    if (verbose)
		System.out.println(" Adjusted index/weight at end of dim " + interpDimension);
	} else 
	    weight = index_and_weights[ select ] - (double) indices[ select ];

	if (verbose)
	    System.out.println(" For dim " + interpDimension + " and select = " + select 
			       + " have index of " + indices[ select ] + " and weight of " 
			       + weight);
	if (interpDimension == 1) {
	    a = this.functionTableDef.getPt( indices );
	    indices[ select ]++;
	    b = this.functionTableDef.getPt( indices );
	    indices[ select ]--;
	} else {
	    a = this.interpolate( index_and_weights, indices, interpDimension-1 );
	    indices[ select ]++;
	    b = this.interpolate( index_and_weights, indices, interpDimension-1 );
	    indices[ select ]--;
	}
	value = a + (b-a)*weight;
	if (verbose) System.out.println(" Interpolated between " + a + " and " + b + " is value " + value);
	return value;
    }
}

