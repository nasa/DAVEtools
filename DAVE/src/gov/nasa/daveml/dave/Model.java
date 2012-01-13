// Model
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 *
 * Object representing top-level model; owns all blocks and signals
 *
 * @author 031211 {@link <a href="mailto:bruce.jackson@nasa.gov">Bruce Jackson</a>}
 *
 **/

public class Model
{
    /**
     *  name of model
     */

    protected String ourName;

    /**
     *  signals in order of construction
     */

    SignalArrayList signals;

    /**
     *  blocks in order of construction
     */

    BlockArrayList blocks;

    /**
     *  maps bpIDs to breakpoint sets
     */

    Map<String, BreakpointSet> breakpointSets;

    /**
     *  reusable function tables (griddedTableDefs)
     */

    Map<String, FuncTable> tables;

    /**
     *  list of breakpoint blocks  -- do we really use this?
     */

    Map<String, BlockBP> bpBlocks;

    /**
     *  blocks in order of execution
     */

    BlockArrayList executeOrder;

    /**
     *  list of input blocks
     */

    BlockArrayList inputBlocks;

    /**
     *  list of output blocks
     */

    BlockArrayList outputBlocks;

    /**
     *  input vector (if null, not yet established)
     */

    VectorInfoArrayList inputVec;
    
    /**
     *  output vector (if null, not yet established)
     */

    VectorInfoArrayList outputVec;

    /**
     *  indicates if we've set up our execution order, etc.
     */

    boolean initialized;

    /**
     *  if true, allow objects to report things
     */

    boolean verbose;

    /**
     *  serves as master clock		
     */

    int cycleCounter;
    
    /**
     * Code output dialect
     */
    
    int codeDialect;
    
    /**
     * Code generation dialects
     */
    
    public static final int DT_FORTRAN = 0;
    public static final int DT_ANSI_C  = 1;

    /**
     * private return codes
     */

    private static final int exit_success = 0;
    private static final int exit_failure = 1;
    
    /**
     *
     * <p> Constructor for Model </p>
     *
     * @param numBlocks  Estimated number of blocks expected
     * @param numSignals Estimated number of signals expected
     *
     **/

    public Model(int numBlocks, int numSignals)
    {
	this.signals = new SignalArrayList(numSignals);
	this.blocks  = new  BlockArrayList(numBlocks );
	this.breakpointSets = new HashMap<String, BreakpointSet>();
	this.tables = new HashMap<String, FuncTable>();
	this.bpBlocks = new HashMap<String, BlockBP>();
	this.executeOrder = new BlockArrayList(numBlocks);
	this.inputBlocks = new BlockArrayList(numBlocks);
	this.outputBlocks = new BlockArrayList(numBlocks);
	this.initialized = false;
	this.verbose = false;
	this.cycleCounter = 0;
	this.ourName = "untitled";
        this.codeDialect = DT_ANSI_C;
    }


    /**
     *
     * <p> Constructor for Model with default number of signals and blocks </p>
     *
     **/

    public Model()
    {
	this(20, 20);
    }

    /**
     *
     * <p> Sets name of this <code>Model</code> </p>
     *
     * @param theName <code>String</code> containing new name of <code>Model</code>
     *
     **/

    public void setName(String theName)
    {
	this.ourName = theName;
    }


    /**
     * 
     * <p> Returns the name of this <code>Model</code> </p>
     *
     **/

    public String getName() { return this.ourName; }


    /**
     *
     * <p> Returns cycleCounter </p>
     *
     **/

    public int getCycleCounter() { return this.cycleCounter; }


    /**
     *
     * <p> Resets cycleCounter </p>
     *
     **/

    public void resetCycleCounter() { this.cycleCounter = 0; }


    /**
     *
     * Increments cycleCounter 
     *
     **/
    
    public void incrementCycleCounter() { this.cycleCounter++; }
     

    /**
     *
     * Sets verbose flag for model and all <code>Blocks</code> and <code>Signals</code>.
     *
     **/

    public void makeVerbose() 
    { 
	this.verbose = true;
	if (signals != null) {
	    Iterator<?> it = signals.iterator();
	    while (it.hasNext()) {
		Signal s = (Signal) it.next();
		s.makeVerbose();
	    }
	}
	if (blocks != null) {
	    Iterator<?> it = blocks.iterator();
	    while (it.hasNext()) {
		Block b = (Block) it.next();
		b.makeVerbose();
	    }
	}
    }


    /**
     * <p> unsets verbose flag </p>
     **/

    public void silence() 
    { 
	this.verbose = false;
	if (signals != null) {
	    Iterator<?> it = signals.iterator();
	    while (it.hasNext()) {
		Signal s = (Signal) it.next();
		s.silence();
	    }
	}
	if (blocks != null) {
	    Iterator<?> it = blocks.iterator();
	    while (it.hasNext()) {
		Block b = (Block) it.next();
		b.silence();
	    }
	}
    }

    
    /**
     * Return status of verbose flag
     **/

    public boolean isVerbose() { return this.verbose; }


    /**
     *
     * <p> Access signals list </p>
     *
     **/

    public SignalArrayList getSignals() { return signals; }


    /**
     *
     * <p> Access blocks list </p>
     *
     **/

    public BlockArrayList getBlocks() { return blocks; }


    /**
     *
     * Return number of input blocks
     *
     **/

    public int getNumInputBlocks() { return inputBlocks.size(); }


    /**
     *
     * Return input block list
     *
     **/

    public BlockArrayList getInputBlocks() { return inputBlocks; }


    /**
     *
     * Return number of output blocks
     *
     **/

    public int getNumOutputBlocks() { return outputBlocks.size(); }


    /**
     *
     * Return output block list
     *
     **/

    public BlockArrayList getOutputBlocks() { return outputBlocks; }

    /**
     *
     * Return code dialect
     * 
     **/
    
    public int getCodeDialect() { return codeDialect; }
    
    /**
     * 
     * Set emitted code dialect
     * 
     */
    
    public void setCodeDialect( int dialect ) { codeDialect = dialect; }

    /**
     *
     * <p> Add block </p>
     *
     * @param newBlock <code>Block</code> to be added to blocks list
     *
     **/

    public void add( Block newBlock ) 
    { 
	if (this.verbose) {
	    System.out.println("Adding " + newBlock.getType() + " block '" 
			       + newBlock.getName() + "' to model.");
	    newBlock.makeVerbose();
	}
	blocks.add(newBlock);
	if (newBlock instanceof BlockInput) {
	    inputBlocks.add(newBlock);
	    if (this.verbose)
		System.out.println("size of inputBlocks list is " + inputBlocks.size() );
	}
	if (newBlock instanceof BlockOutput)
	    outputBlocks.add(newBlock);
    }



    /**
     *
     * <p> Add signal </p>
     *
     * @param newSignal <code>Signal</code> to be added to signals list
     *
     **/

    public void add( Signal newSignal ) 
    { 
	if (this.verbose) {
	    System.out.println("Adding signal '" + newSignal.getName() + "' to model.");
	    newSignal.makeVerbose();
	}	
	signals.add(newSignal); 
    }


    /**
     *
     * <p> Register breakpoint set definition </p>
     *
     * @param newBPSet <code>BreakpointSet</code> to be added to bpID list
     *
     **/

    public void register( BreakpointSet newBPSet )
    { 
	int oldSize = 0;
	int setSize = 0;

	if (this.verbose) {
	    System.out.println("Registering breakpoint set " + newBPSet.getName() 
			       + " (" + newBPSet.getBPID() + ") with model.");
	    oldSize = breakpointSets.size();
	}

	breakpointSets.put(newBPSet.getBPID(), newBPSet); // hash using ID field for easy retrieval

	// check to see if it got stored
	if (this.verbose) {
	    String key = newBPSet.getBPID();
	    boolean success = breakpointSets.containsKey( key );
	    setSize = breakpointSets.size();
	    System.out.println("breakpoint set map size is now " + setSize);
	    if (success && (setSize>oldSize))
		System.out.println("...successfully registered to key '" + key + "'");
	    else
		System.out.println("...appears to be duplicate; proceeding");
	}
    }


    /**
     *
     * <p> Look up a breakpoint set by its ID </p>
     *
     * @param bpID <code>String</code> with ID of BreakpointSet
     * @return BreakpointSet matching ID, or null
     *
     **/

    public BreakpointSet getBPSetByID( String bpID )
    {
	if (this.verbose) {
	    System.out.println("Looking up breakpoint set of ID '" + bpID + "'");
	}
	if (this.breakpointSets.containsKey( bpID )) {
	    return breakpointSets.get( bpID );
	} else {
	    System.err.println("Unable to look up breakpoint set ID '" + bpID + "'; was it defined?");
	    System.exit(exit_failure);
	}
	return null;
    }


    /**
     *
     * <p> Register breakpoint block definition </p>
     *
     * @param newBPBlock <code>BlockBP</code> to be added to list
     *
     **/

    public void register( BlockBP newBPBlock )
    { 
	int oldSize = 0;
	int setSize = 0;

	String key = newBPBlock.getName();

	if (this.verbose) {
	    System.out.println("Registering breakpoint block " + key
			       + " with model.");
	    oldSize = bpBlocks.size();
	}

	bpBlocks.put(key, newBPBlock); 	// hash using name for easy retrieval

	// check to see if it got stored
	if (this.verbose) {
	    boolean success = bpBlocks.containsKey( key );
	    setSize = bpBlocks.size();
	    System.out.println("bpBlocks map size is now " + setSize);
	    if (success && (setSize>oldSize))
		System.out.println("...successfully registered to key '" + key + "'");
	    else
		System.out.println("...appears to be duplicate; proceeding");
	}

    }


    /**
     *
     * <p> Look up a breakpoint block by its ID </p>
     *
     * @param bpID <code>String</code> with ID of BlockBP
     * @return BlockBP matching ID, or null
     *
     **/

    public BlockBP getBPBlockByID( String bpID )
    {
	if (this.verbose) {
	    System.out.println("Looking up breakpoint block of ID '" + bpID + "'");
	}
	if (bpBlocks.containsKey( bpID )) {
	    return bpBlocks.get( bpID );
	} else {
	    System.err.println("Unable to look up breakpoint block ID '" + bpID + "'; was it defined?");
	    System.exit(0);
	}
	return null;
    }


    /**
     *
     * <p> Register function table definition </p>
     *
     * @param newTable <code>FuncTable</code> to be added to tables list
     *
     **/

    public void register( FuncTable newTable )
    { 
	int oldSize = 0;
	int setSize = 0;

	String key = newTable.getGTID();
	
	// ensure we have a key of some nature, so there is a unique entry in hash
	// for F-16 model, were getting multiple null tables being overwritten
	// and number of data points was just of last table found
	if (key == null) {
		key = newTable.getName() + "_fakeID";
	}
	
	if (this.verbose) {
	    System.out.println("Registering table " + newTable.getName() + " (" 
			       + key + ") to model.");
	    oldSize = tables.size();
	}

	tables.put(key, newTable); // hash using ID field for easy retrieval

	// check to see if it got stored

	if (this.verbose) {
	    boolean success = tables.containsKey( key );
	    setSize = tables.size();
	    System.out.println("FuncTable map size is now " + setSize);
	    if (success && (setSize>oldSize))
		System.out.println("...successfully registered to key '" + key + "'");
	    else
		System.out.println("...appears to be duplicate; proceeding");
	}
    }


    /**
     *
     * <p> Look up a griddedTableDef by its ID </p>
     *
     * @param gtID <code>String</code> with ID of griddedTableDef
     * @return FuncTable matching ID, or null
     *
     **/

    public FuncTable getTableByID( String gtID )
    {
	if (this.verbose) {
	    System.out.println("Looking up gridded table of ID '" + gtID + "'");
	}
	if (tables.containsKey( gtID )) {
	    return tables.get( gtID );
	} else {
	    System.err.println("Unable to look up gridded table ID '" + gtID + "'; was it defined?");
	    System.exit(0);
	}
	return null;

    }


    /**
     *
     * <p> Return number of blocks </p>
     *
     **/

    public int getNumBlocks() { return blocks.size(); }


    /**
     *
     * <p> Return number of signals </p>
     *
     **/

    public int getNumSignals() { return signals.size(); }


    /**
     *
     * <p> Return number of tables </p>
     *
     **/

    public int getNumTables() { return tables.size(); }


    /**
     * Calls each block and tells it to hook up inputs &amp; outputs
     **/

    public void wireBlocks()
    {
	Iterator<Block> it = blocks.iterator();
        int numBlocks = blocks.size();
        if (numBlocks > 0 && this.isVerbose()) {
            System.out.println("");
            System.out.println("Wiring " + numBlocks + " blocks together");
            System.out.println("--------------------");
            System.out.println("");
        }
	while (it.hasNext()) {
	    Block b = it.next();
	    try {
		b.hookUp();
	    } catch (DAVEException e) {
		System.err.println(e.getMessage());
		System.err.println("Block '" + b.getName() + "' unable to hook up to i/o signals... missing varDef?");
		System.exit(0);
	    }
	}
    }


    /**
     * Creates any missing constant (or input) and output blocks (and any
     * required connector Signals). Also sets up any necessary limiters on Signals
     **/

    public void hookUpIO()
    {
        Signal theSignal;
	Iterator<Signal> it;
        ArrayList<Signal> modifiedSignals = new ArrayList<Signal>(0);

	// iterate for all signals - find blocks with missing inputs or outputs
	it = this.signals.iterator();
        Block ignored;

        if (this.isVerbose()) {
            System.out.println("");
            System.out.println("Creating any necessary I/O connections");
            System.out.println("--------------------------------------");
            System.out.println("");
        }

	while (it.hasNext()) {
	    theSignal = it.next();
	    
	    if (this.isVerbose())
		System.out.println("checking signal '" + theSignal.getName() + "'");
	    // check for missing source
	    if (!theSignal.hasSource()) {
		if (this.isVerbose())
		    System.out.println("...signal has no source.");
		// if an initial condition is defined, create a constant value input
                // unless it has been flagged as an input, control or disturbance
		if( theSignal.hasIC()     &&
                        !( theSignal.isInput() || theSignal.isControl() ||
                           theSignal.isDisturbance() )
                  ) {
		    Block b = new BlockMathConstant( theSignal.getIC(), this);
		    try {
			b.addOutput( theSignal );
		    } catch (DAVEException e) {
			System.err.println(" Error adding new IC block to signal.");
			System.exit(0);
		    } 
		} else {
		    // otherwise, create an input block and attach
		    ignored = new BlockInput( theSignal, this );
		    if (this.isVerbose())
			System.out.println("...created new input block");
		}
	    }

            if( theSignal.isLimited() )
                setUpLimiterFor( theSignal, modifiedSignals );

	    // Connect an output block to any signal that has either no destination
	    // or is flagged as an output.
	    if ( !theSignal.hasDest() || theSignal.isOutput() )
		ignored = new BlockOutput( theSignal, this );
        } // end of while (it.hasNext()) over model's signals

        // add to model any newly-created signals (representing outputs from any new limiters)
        it = modifiedSignals.iterator();
        while (it.hasNext()) {
            this.add(it.next());
        }
    }

    public void setUpLimiterFor(Signal theSignal,
            ArrayList<Signal> modifiedSignals) {
        Block ignored;


            // Deal with internal signal limits by inserting limit blocks:

            // 1) duplicate current signal; schedule to add to model when iteration is complete
            // 2) append "_unlim" to original signal name & varID
            // 3) create downstream limit block(s)
            // 4) create downstream signal with old name; add to model
            // 5) change source varID of any destination blocks to downstream (limited) signal
            // 6) change destination varID of any source blocks to new limited signal
            // 7) hook current signal to limit block

                // start with duplication
                // new signal will share all features & destination blocks & ports, limits, flags
                Signal limitedSignal = new Signal( theSignal );
                // this.add(limitedSignal);     // wait until iteration is done to add new limited signal to model
                boolean OK = modifiedSignals.add(limitedSignal);
                assert(OK);
                limitedSignal.setDerivedFlag(); // mark as automatically generated variable

                // record original metadata for signal in case of later failure
                BlockArrayList origDests = (BlockArrayList) theSignal.getDests().clone();
                @SuppressWarnings("unchecked")
                ArrayList<Integer> origPorts = (ArrayList<Integer>) theSignal.getDestPortNumbers().clone();
                String origName = theSignal.getName();
                String origVarID = theSignal.getVarID();

                // change name & varID of original signal; clear outputs
                theSignal.setName(  theSignal.getName()  + "_unlim" );
                theSignal.setVarID( theSignal.getVarID() + "_unlim" );
                theSignal.removeDestBlocks();   // break downstream connections

                // change output varID of source block to append "_unlim"
                theSignal.getSource().renameOutVarID();

                // this sets up the new limit block, and hooks the upstream signal as it's input
                BlockLimiter limiter = new BlockLimiter( theSignal, this,
                        theSignal.getLowerLimit(), theSignal.getUpperLimit() );

                // replace the source for original (unlimited) signals' destination
                // blocks to be the newly-created limited signal
                // Follows convention for calling signal with downstream block
                Iterator<Block> it = origDests.iterator();
                while (it.hasNext()) {
                    Block b = it.next();
                    b.replaceInput( theSignal, limitedSignal);
                }

                // following convention, tell limiter of its new output to perform hookup
                try {
                    limiter.addOutput(limitedSignal);
                } catch (DAVEException ex) {
                    System.err.println("Unable to insert limiter for variable " + origName );
                    // restore original signal to previous condition
                    theSignal.dests     = origDests;
                    theSignal.destPorts = origPorts;
                    theSignal.setName(    origName  );
                    theSignal.setVarID(   origVarID );
                    // BUG: new limitedSignal has been added to modifiedSignals list
                }

                // here on success
                theSignal.clearIsOutputFlag();  // no longer use this as an output, if set

                // now clear the original signal's limits
                theSignal.setLowerLimit(Double.NEGATIVE_INFINITY);
                theSignal.setUpperLimit(Double.POSITIVE_INFINITY);

                // set up an output block for the new signal if required
                if( !limitedSignal.hasDest() || limitedSignal.isOutput() )
                    ignored = new BlockOutput( limitedSignal, this );

                return;
            }

    
    /**
     * Verify model integrity
     *
     * Returns true if no disconnects found
     *
     **/

    public boolean verifyIntegrity()
    {
	boolean missing;
	boolean OK;
	
	OK = true;

	// look for blocks with missing inputs & outputs, hookup if possible or gen error
	if (this.isVerbose()) {
            System.out.println("");
	    System.out.println("Checking integrity of model");
            System.out.println("---------------------------");
            System.out.println("");
        }

	Iterator<Block> blockIterator = this.getBlocks().iterator();
	while( blockIterator.hasNext()) {

	    Block b = blockIterator.next();
	    missing = b.verifyInputs();
	    if( missing ) {
		System.err.println(" Block " + b.getName() + " has missing input(s).");
		OK = false;
	    }

	    missing = b.verifyOutputs();
	    if( missing ) {
		System.err.println(" Block " + b.getName() + " has missing output(s).");
		OK = false;
	    }
	}

	// look at all signals to make sure they have both sources and destination blocks

	Iterator<Signal> signalIterator = this.getSignals().iterator();
	while ( signalIterator.hasNext()) {
	    
	    Signal s = signalIterator.next();
	    missing = !s.hasSource();
	    if( missing ) {
		System.err.println(" Signal " + s.getName() + " has no source block.");
		OK = false;
	    }
	    missing = !s.hasDest();
	    if( missing ) {
		System.err.println(" Signal " + s.getName() + " has no destination block.");
		OK = false;
	    }
	}

	return OK;
    }


    /**
     *
     * Writes values of all internal signals to an output file.
     * <p>
     * Only emits those signals corresponding to varDefs in the
     * DAVE-ML input file for clarity.
     *
     * @param out Where to put the output.
     *
     **/

    public void generateInternalValues( PrintWriter out ) {
	Iterator<?> it = this.signals.iterator();
	// make sure there are signals to evaluate
	if (it != null)
	    if ( it.hasNext() ) {
		// write header stuff
		out.println("      <internalValues>");
		// write lines of values
		while (it.hasNext()) {
		    Signal s = (Signal) it.next();
		    // ignore automatically-generated signals
		    // should just print out signals from varDefs
		    if (!s.isDerived()) {
			out.print("	<signal> <varID>");
			out.print( s.getVarID());
			out.print("</varID> <signalValue>");
			try {
			    out.print( s.sourceValue());
			} catch (DAVEException e) {
			    System.err.println(e.getMessage());
			    System.err.println("Error - wiring problem discovered when printing checkcase internal values.");
			    System.exit(0);
			}
			out.print("</signalValue> </signal>");
			out.println();
		    }
		}
		// write closing tags
		out.println("      </internalValues>");
	    }
    }


    /**
     *
     * <p> Performs model initialization: </p>
     * <ol>
     *   <li>Resets cycle counter</li>
     *   <li>Determines execution order of blocks</li>
     * </ol>
     * @throws DAVEException
     *
     **/

    public void initialize() throws DAVEException 
    {
        if (!this.initialized) { // only do this once
            
	StringWriter strwriter = null;// used only when verbose, so we can call
				// describeSelf(writer)
	if (this.isVerbose()) {
	    System.out.println();
	    System.out.println("Method initialize() called for model '" 
			       + this.getName() + "'");
            System.out.println();
	    strwriter = new StringWriter();
	    if (strwriter == null)
		System.err.println("Unable to create FileWriter for 'out'");
	}

	// get duplicate list of blocks; find which ones are ready now
	BlockArrayList blks = (BlockArrayList) blocks.clone();
	ListIterator<Block> candidates;	// this type of iterator allows for simul. mods.
	Iterator<Block> blockIterator;
	Block b;

	boolean progress = true;
	int blocksNotReady;
	int oldBlocksNotReady = Integer.MAX_VALUE;

	while (!blks.isEmpty() && progress) {
	    // tell any non-ready blocks to update
	    blocksNotReady = 0;
	    blockIterator = blks.iterator();
		
	    if (this.verbose) {
                System.out.println("");
		System.out.println("Telling " + blks.size() + " non-ready blocks to update.");
                System.out.println("");
            }
	    
	    while (blockIterator.hasNext()) {
		b = blockIterator.next();
		if (!b.isReady())
		    try {
			blocksNotReady++;
			b.update();
		    } catch (DAVEException e) {
			System.out.print(" Block '" + b.getName() + "':");
			System.out.println(" threw a DAVE Exception: " + e.getMessage());
//			e.printStackTrace();
			System.out.println(" Continuing...");
		    } catch (RuntimeException e) {
			System.out.print(" Block '" + b.getName() + "':");
			System.out.println(" threw a non-DAVE exception: ");
                        System.out.println(e.getMessage() + ". Quitting...");
			throw e;
		    }
	    }

	    // removing ready blocks

	    if (this.verbose) {
                System.out.println("");
		System.out.println("Moving ready blocks to execution list");
                System.out.println("");
            }

            // blks is a cloned list of all previously non-ready model blocks
            // will search to see if any are ready
	    candidates = blks.listIterator();
	    while (candidates.hasNext()) {
		b = candidates.next();
		// check each one to see if ready
		if (this.verbose) {
		    System.out.print("Checking to see if block '" +
				     b.getName() + "' is ready...");
                }
		if (b.isReady()) {
		    if (this.verbose) {
			System.out.println(" yes. Adding to execution list.");
                    }
		    // blocks that are ready are added to executeOrder list and taken off this one
		    executeOrder.add( b );
		    candidates.remove();	// removes current block
		} else {
		    if (this.verbose)
			System.out.println(" no.");
                }
	    }

            // Number of not ready blocks should shrink each pass through loop
	    progress = blocksNotReady < oldBlocksNotReady;
	    oldBlocksNotReady = blocksNotReady;

            // if not, we'll kick out of loop and whine.
	}

        // Here on empty block list OR lack of progress
	// Check to see if success or if algebraic loop detected
        String errorMsg = "";
	if (!blks.isEmpty()) {
            errorMsg = "Possible algebraic loop detected; unable to satisfy inputs for ";
            errorMsg = errorMsg + blks.size() + " blocks.";
	    throw new DAVEException(errorMsg);
        }

	// Otherwise, believe we have success
	this.initialized = true;
	
	if (this.verbose) {
            System.out.println("");
	    System.out.println("Model '" + this.getName() + "' is initialized. Order is:");
            System.out.println("");
        }
	try {
	    int i = 1;
	    blockIterator = executeOrder.iterator();
	    while (blockIterator.hasNext()) {
		b = blockIterator.next();
		if (this.verbose) {
		    strwriter.write(i + " ");
		    b.describeSelf(strwriter);
		    strwriter.write("\n");
		}
		i++;
	    }
	    if (this.verbose) System.out.print(strwriter.toString());
	} catch (Exception e) {
	    System.err.println(e.getMessage());
            System.exit(exit_failure);
	}

        if (this.isVerbose()) {
            System.out.println("");
            System.out.println("Model initialized and execution order established.");
            System.out.println("");
        }
        }

    }


    /**
     * <p> Builds &amp; returns the current values of output blocks </p>
     *
     * @throws DAVEException
     **/


    public VectorInfoArrayList getOutputVector() throws DAVEException

    {
	if (this.outputVec == null) {
	    this.outputVec = new VectorInfoArrayList();
	    Iterator<?> outBlks = this.outputBlocks.iterator();
	    while (outBlks.hasNext()) {
		Block theBlk = (Block) outBlks.next();
		if (theBlk == null) 
		    throw new DAVEException("Unexpected null found in outputBlocks list.");
		if (!(theBlk instanceof BlockOutput)) 
		    throw new DAVEException("Non-output block found in outputBlocks list.");
		String theName = theBlk.getName();
		String theUnits = ((BlockOutput) theBlk).getUnits();
		double theValue = theBlk.getValue();
		VectorInfo vi = new VectorInfo(theName, theUnits, theBlk, false);
		vi.setValue( theValue );
		this.outputVec.add( vi );
	    }
	}
	return this.outputVec;
    }


    /**
     * <p> Builds &amp; returns the current values of input blocks </p>
     *
     * @throws DAVEException
     **/

    public VectorInfoArrayList getInputVector() throws DAVEException

    {
	if (this.inputVec == null) {
	    this.inputVec = new VectorInfoArrayList();
	    Iterator<?> inBlks = this.inputBlocks.iterator();
	    while (inBlks.hasNext()) {
		Block theBlk = (Block) inBlks.next();
		if (theBlk == null) 
		    throw new DAVEException("Null inputBlocks found.");
		if (!(theBlk instanceof BlockInput)) 
		    throw new DAVEException("Non-input block found in inputBlocks list.");
		String inName = theBlk.getName();
		String inUnits = ((BlockInput) theBlk).getUnits();
		VectorInfo vi = new VectorInfo(inName, inUnits, theBlk, true);
		this.inputVec.add( vi );
	    }
	}
	return this.inputVec;
    }


    /**
     *
     * <p> Cycle the model. </p>
     *
     * <p> The calling routine should complete the input vector (by
     * providing values for each element of the input vector obtained
     * by calling the <code>getInputVector()</code> method. It updates the
     * values of the output vector (obtained by calling <code>Model.getOutputVector()</code> </p>
     *
     * @throws DAVEException
     *
     **/

    public void cycle() throws DAVEException
    {
        if (this.isVerbose()) {
            System.out.println("");
            System.out.println("Cycle() function called for model '" + this.getName() + "'");
            System.out.println("");
        }
	// check to see if inputVec is null
	if (this.inputVec == null)
	    throw new DAVEException("Input vector is null. Did you obtain one from getInputVector() first?");
	
	// check to see if it has the proper number of elements
	if (this.inputVec.size() != inputBlocks.size())
	    throw new DAVEException("Input vector length (" + inputVec.size()
				    + ") does not match number of input blocks (" + inputBlocks.size());

	// unload the input vector into the input blocks - complain if block refs are nulls
	Iterator<VectorInfo> inp = this.inputVec.iterator();
	while (inp.hasNext()) {
	    VectorInfo vi = inp.next();
	    if (vi == null) 
		throw new DAVEException("Unexpected found null in input vector array list.");
	    BlockInput theBlk = vi.getSink();
	    double theValue = vi.getValue();
	    theBlk.setInputValue( theValue );
	}

	// make sure the model is initialized - this also cycles the model
	if (!this.initialized)
	    try {
                if (this.isVerbose()) {
                    System.out.println("");
                    System.out.println("Model execution order not yet set; calling initialize()");
                    System.out.println("-------------------------------------------------------");
                    System.out.println("");
                }
		this.initialize();
	    } catch (Exception e) {
		System.err.println(e.getMessage());
		System.exit(exit_failure);
	    }
	else
	    { 		// cycle the model once if it is
		Iterator<?> theBlocks = executeOrder.iterator();
		while (theBlocks.hasNext()) {
		    Block theBlock = (Block) theBlocks.next();
		    theBlock.update();
		}
	    }

	// build and return the output vector from output block values
	
	// if we need to create an output vector, take extra steps & complain if null
	if (this.outputVec == null) {

	    this.outputVec = new VectorInfoArrayList(); // overwrites any existing one; no big deal

	    if (this.outputVec == null)
		throw new DAVEException("Unable to allocate space for output vector.");

	    Iterator<?> outBlks = this.outputBlocks.iterator();
	    while (outBlks.hasNext()) {
		Block theBlk = (Block) outBlks.next();
		if (theBlk == null)
		    throw new DAVEException("Null found in outputBlocks unexpectedly.");
		if (!(theBlk instanceof BlockOutput)) 
		    throw new DAVEException("Non-output block found in outputBlocks.");
		String theName = theBlk.getName();
		String theUnits = ((BlockOutput) theBlk).getUnits();
		double theValue = theBlk.getValue();
		// create an output record
		VectorInfo vi = new VectorInfo(theName, theUnits, theBlk, false);
		if (vi == null)
		    throw new DAVEException("Unable to allocate memory for output vector element");
		vi.setValue( theValue );
		this.outputVec.add( vi );
	    }
	} else { 
	    // have existing output vector; can speed things up
	    if (this.outputBlocks.size() != this.outputVec.size())
		throw new DAVEException("Somehow ended up with mismatched output vector.");
	    Iterator<?> outBlks = this.outputBlocks.iterator();
	    Iterator<?> outVI   = this.outputVec.iterator();
	    while (outBlks.hasNext()) {
		BlockOutput theBlk = (BlockOutput) outBlks.next();
		VectorInfo  vi  = (VectorInfo) outVI.next();
		double theValue = theBlk.getValue();
		vi.setValue( theValue );
	    }
	}
    }

    /**
     *
     * Reports number of things on output
     *
     **/

    public void reportStats()
    {
	// sum the number of data points found
	int total = 0;
	if (!tables.isEmpty()) {
	    Set<String> tableKeySet = tables.keySet();	// get all the keys
	    Iterator<String> tableKeyIt = tableKeySet.iterator();
	    while (tableKeyIt.hasNext()) {
		String tableKey = tableKeyIt.next();
		FuncTable theTable = this.tables.get( tableKey );
		total = total + theTable.size();
	    }
	}
	System.out.println("Implementation statistics:");
	System.out.println(" Number of function interpolation tables: " + this.tables.size());
	System.out.println("               Number of breakpoint sets: " + this.breakpointSets.size());
	System.out.println("                   Number of data points: " + total);
	System.out.println();
	System.out.println("                  Number of signal lines: " + this.signals.size());
	System.out.println("                        Number of blocks: " + this.blocks.size());
	System.out.println("                        Number of inputs: " + this.inputBlocks.size());
	System.out.println("                       Number of outputs: " + this.outputBlocks.size());

    }
    
    /**
     * Returns the blocks in execution sorted order; available only after call to initialize()
     * @return BlockArrayList of sorted blocks, in appropriate order of execution
     */
    
    public BlockArrayList getSortedBlocks() throws DAVEException {
        if (executeOrder == null) {
            throw new DAVEException("Execution order not yet determined");
        }
        return executeOrder;
    }
    
    /**
     * Return the block that represents the final math operation for the indicated
     * variable.
     * @param theVarID identifies the variable whose source block is sought
     * @return the source block, or null if not found
     */
    public Block getBlockByOutputVarID(String theVarID) {
        // easiest way is to find single with proper ID, then
        // find source block.
        Block theBlock = null;
        Signal theSignal = signals.findByID(theVarID);
        if (theSignal != null) {
            theBlock = theSignal.getSource();
        }
        return theBlock;
    }

}
