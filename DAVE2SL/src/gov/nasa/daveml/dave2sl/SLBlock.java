// SLBlock.java
//  
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@larc.nasa.gov>
//  Visit <http://daveml.org> for more info
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.


package gov.nasa.daveml.dave2sl;

import gov.nasa.daveml.dave.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;

/**
 *
 * Encases a {@link gov.nasa.daveml.dave.Block} to give it magical Simulink powers.
 *
 * <p>
 * These methods provide unique capabilities for writing Simulink blocks.
 *
 * <p>
 * Modification history:
 *  <ul>
 *   <li>040227 Written</li>
 *   <li>060901 Rewritten to use 'add_block', 'add_line', etc instead of raw MDL.
 *  </ul>
 *
 * @author Bruce Jackson &lt;bruce.jackson@nasa.gov&gt;
 * @version 0.9
 *
 **/

public class SLBlock
{
    /**
     *  the diagram we belong to
     */

    SLDiagram ourDiagram;

    /**
     *  block we're hiding
     */

    Block block;

    /**
     *  our children blocks
     */

    BlockArrayList children;

    /**
     *  lowest descendent's row
     */

    int rowDepthOfChildren;

    /**
     *  layout information
     */

    int myRow;

    /**
     *     for diagram model
     */

    int myCol;

    /**
     *  height of block
     */

    int mdlHeight;

    /**
     *  width of block
     */

    int mdlWidth;

    /**
     *  adjustment to block width
     */

    int xPad;

    /**
     *  adjustment to block height
     */

    int yPad;

    /**
     *  list of tables already written
     */

    HashSet<String> writtenTables;

    /**
     *  set if user asked for us to be chatty
     */

    boolean verboseFlag;

    /**
     *
     * Basic constructor
     *
     **/

    public SLBlock()
    {
        this.myRow = 0;
        this.myCol = 0;
        this.children = new BlockArrayList(10);
        this.rowDepthOfChildren = 0;
        this.writtenTables = new HashSet<String>(10);
        this.verboseFlag = false;
    }


    /**
     *
     * Constructor that encases a {@link gov.nasa.daveml.dave.Block}
     *
     * @param diagram our parent diagram
     * @param b the <code>Block</code> to encase
     *
     **/

    public SLBlock( SLDiagram diagram, Block b )
    {
        this();
        this.ourDiagram = diagram;      // record who we belong to
        this.block = b;                 // record our embedded DAVE Block
        // set MDLwidth, height based on type of block
        // default values
        mdlHeight = 30;
        mdlWidth  = 30;
        xPad = 0;
        yPad = 0;
        if        (b instanceof BlockBP)            { mdlHeight=52; mdlWidth=50; xPad=0; yPad=0;
        } else if (b instanceof BlockFuncTable)     { mdlHeight=50; mdlWidth=50; xPad=0; yPad=0;
        } else if (b instanceof BlockInput)         { mdlHeight=14; mdlWidth=30; xPad=0; yPad=8;
        } else if (b instanceof BlockOutput)        { mdlHeight=14; mdlWidth=30; xPad=0; yPad=0;
        } else if (b instanceof BlockMathConstant)  { mdlHeight=30; mdlWidth=60; xPad=0; yPad=0;
        }

        // Adjust height for blocks with more than one input
        // Assumes all these simple blocks have a single output; otherwise would need
        // to look at some combination of inputs & outputs to assure 10-pixel min spacing

        int numInputs = this.block.numInputs();
        if (numInputs > 1) {
            int minHt = 24 + 15*(numInputs-1);
            if (minHt > mdlHeight)
                mdlHeight = minHt;
        }
    }


    /**
     *
     * Creates necessary Matlab commands to add our block
     *
     * @param writer Instance of the SLFileWriter class
     *
     **/

    public void createM(SLFileWriter writer, int x, int y ) 
        throws IOException
    {
        if        (this.block instanceof BlockBP)            { writeMforBP(       writer, x, y );
        } else if (this.block instanceof BlockFuncTable)     { writeMforBFT(      writer, x, y );
        } else if (this.block instanceof BlockInput)         { writeMforIn(       writer, x, y );
        } else if (this.block instanceof BlockMathAbs)       { writeMforAbs(      writer, x, y );
        } else if (this.block instanceof BlockMathConstant)  { writeMforConst(    writer, x, y );
        } else if (this.block instanceof BlockMathFunction)  { writeMforFunc(     writer, x, y );
        } else if (this.block instanceof BlockMathMinus)     { writeMforMinus(    writer, x, y );
        } else if (this.block instanceof BlockMathMinmax)    { writeMforMinmax(   writer, x, y );
        } else if (this.block instanceof BlockMathProduct)   { writeMforMult(     writer, x, y );
        } else if (this.block instanceof BlockMathRelation ) { writeMforRelation( writer, x, y );
        } else if (this.block instanceof BlockMathSum)       { writeMforSum(      writer, x, y );
        } else if (this.block instanceof BlockMathSwitch)    { writeMforSwitch(   writer, x, y );
            //          } else if (this.block instanceof BlockNode)          { writeMforNode(     writer, x, y );
        } else if (this.block instanceof BlockOutput)        { writeMforOut(      writer, x, y );
        } else if (this.block instanceof BlockLimiter)       { writeMforLimit(    writer, x, y );
        } else {        // default
            writer.writeln("%%");
            writer.writeln("%%******** Missing Block Type: block \"" + this.getName() + "\" *******");
            writer.writeln("%%    which claims to be of type \"" + this.block.getType() + "\".");
            writer.writeln("display(['Unknown block type: \"" + this.block.getType() + 
                    "\" for block named \"" + this.block.getName() + "\".']);");
            writer.writeln("%%");
            System.err.println("WARNING: unknown block type for block '" + this.getName() + "'.");
        }
    }

    /**
     *
     * Generates add_block command for limiter block
     *
     **/

    public void writeMforLimit( SLFileWriter writer, int x, int y )
            throws IOException {

        BlockLimiter bl = (BlockLimiter) this.getBlock();
        // Map to Simulink symbol for infinite limits
        String lowerLim = "-Inf";
        String upperLim = "Inf";
        if ( bl.hasLowerLimit() )
            lowerLim = Double.toString(bl.getLowerLimit());
        if ( bl.hasUpperLimit() )
            upperLim = Double.toString(bl.getUpperLimit());
        String paramString = "'ShowName','off',"
                + "'LowerLimit','" + lowerLim + "','UpperLimit','" + upperLim + "',"
                + "'AttributesFormatString','%<LowerLimit> to %<UpperLimit>',"
                + this.createPositionString(x,y);
        writer.addBuiltInBlock("Saturate", this.getName(), paramString);
    }

    /**
     *
     * Generates add_block command for breakpoint block
     *
     **/

    public void writeMforBP( SLFileWriter writer, int x, int y )
        throws IOException
    {
        //              BreakpointSet bpSet = ((BlockBP) this.getBlock()).getBPset();
        String blockType = "simulink/Lookup Tables/PreLookup Index Search";
        String paramString =  "'FontSize',10," +  this.createPositionString(x,y);
        writer.addBlock(blockType, this.getName(), paramString);

        writer.writeln("pts_vec = '" +  ourDiagram.model.getName() + "_data." 
                       + MDLNameList.convertToMDLString(this.getName()) + "_pts';");
        String maskValues = "{pts_vec,"
            + "'Binary Search',"                // searchMode
            + "'on',"                           // cacheBpFlag
            + "'off',"                          // outputFlag
            + "'uint32',"                       // IndexTypeFlag
            + "'Linear Extrapolation',";        // extrapMode
        if (this.ourDiagram.getWarnOnClip())
            maskValues += "'Warning'";          // rangeErrorMode
        else
            maskValues += "'None'";
        maskValues += "}";
        writer.setParam(this.getName(),"MaskValues",maskValues);
        writer.writeln("clear pts_vec");
    }


    /**
     *
     * Generates add_block command for function block
     *
     **/

    public void writeMforBFT( SLFileWriter writer, int x, int y )
        throws IOException
    {
        BlockFuncTable bft = (BlockFuncTable) this.getBlock();
        bft.reorderInputsForMDL();
        FuncTable ft = bft.getFunctionTableDef();
        int numDim = ft.numDim();
        String blockType = "simulink/Lookup Tables/Interpolation (n-D) using PreLookup";
        String paramString =  "'FontSize',10," + this.createPositionString(x,y);
        writer.addBlock(blockType, this.getName(), paramString);

        String tableName = ourDiagram.model.getName() + "_data." 
            + MDLNameList.convertToMDLString(ft.getGTID());
        String maskValues = "{";
        if (numDim > 4)
            maskValues += "'More...'";          // numDimsPopupSelect
        else
            maskValues += "'" + numDim + "',";
        maskValues += "'" + numDim + "',"       // explicitNuumDims
            + "'" + tableName + "',"                    // table
            + "'Linear',"                       // interpMethod
            + "'None - Clip',";                 // extrapMethod
        if (this.ourDiagram.getWarnOnClip())
            maskValues += "'Warning',";         // rangeErrorMode
        else
            maskValues += "'None',";
        maskValues += "'0'}";                   // NumSelectionDims
        writer.setParam(this.getName(),"MaskValues",maskValues);
    }

    /**
     *
     * Generates add_block command for input block
     *
     **/

    public void writeMforIn( SLFileWriter writer, int x, int y )
        throws IOException
    {
        BlockInput bin = (BlockInput) this.getBlock();
        int seqNumber = bin.getSeqNumber();
        String blockName = this.getName();
        ourDiagram.addInput( seqNumber, blockName );
        writer.addBuiltInBlock("Inport", blockName, this.createPositionString(x, y));
    }


    /**
     *
     * Generates add_block command for output block
     *
     **/

    public void writeMforOut( SLFileWriter writer, int x, int y )
        throws IOException
    {
        BlockOutput bout = (BlockOutput) this.getBlock();
        int seqNumber = bout.getSeqNumber();
        String blockName = this.getName();
        ourDiagram.addOutput( seqNumber, blockName );
        writer.addBuiltInBlock("Outport", blockName, this.createPositionString(x,y));
    }


    /**
     *
     * Generates add_block command for MathAbs block
     *
     **/

    public void writeMforAbs( SLFileWriter writer, int x, int y )
        throws IOException
    {
        writer.addBuiltInBlock("Abs", this.getName(), this.createPositionString(x,y));
    }


    /**
     *
     * Generates add_block command for constant block
     *
     **/

    public void writeMforConst( SLFileWriter writer, int x, int y )
        throws IOException
    {
        BlockMathConstant bconst = (BlockMathConstant) this.getBlock();
        String myValue = bconst.getValueAsString();
        String paramString = "'ShowName','off','Value','" + myValue + "',"
            + this.createPositionString(x,y);
        writer.addBuiltInBlock("Constant", this.getName(), paramString);
    }


    /**
     *
     * Generates add_block command for MathFunc block
     *
     **/

    public void writeMforFunc( SLFileWriter writer, int x, int y )
        throws IOException
    {
        BlockMathFunction bmf = (BlockMathFunction) this.getBlock();
        String funcType = bmf.getFuncType();
        String blockType;
        String operatorType;
        if(funcType.equals("power")) {
            blockType = "Math";
            operatorType = "pow";
        } else if (funcType.equals("floor")) {
            blockType = "Rounding";
            operatorType = "floor";
        } else if (funcType.equals("ceiling")) {
            blockType = "Rounding";
            operatorType = "ceil";
        } else {
            blockType = "Trigonometry";
            operatorType = funcType;
            // map MathML2 operators to Simulink function names
            if(funcType.equals("arcsin"))
                operatorType = "asin";
            if(funcType.equals("arccos"))
                operatorType = "acos";
            if(funcType.equals("arctan"))
                operatorType = "atan";
        }

        //      String paramString = "'Inputs'," + this.getBlock().numInputs() + ","
        String paramString = ""
            + "'Operator','" + operatorType + "'," + this.createPositionString(x,y);
        writer.addBuiltInBlock(blockType, this.getName(), paramString);
    }

    /**
     *
     * Generates add_block command for minus block
     *
     **/

    public void writeMforMinus( SLFileWriter writer, int x, int y )
        throws IOException
    {
        String paramString;
        int numArgs = this.block.numInputs();
        if (numArgs == 1) {
            paramString = "'Gain','-1'," + this.createPositionString(x,y);
            writer.addBuiltInBlock("Gain", this.getName(), paramString);
        }
        if (numArgs == 2) {
            paramString = "'IconShape','rectangular','Inputs','+-',";
            paramString += this.createPositionString(x,y);
            writer.addBuiltInBlock("Sum", this.getName(), paramString);
        }
    }

    /**
     *
     * Generates add_block command for Min-max block
     *
     **/

    public void writeMforMinmax( SLFileWriter writer, int x, int y )
        throws IOException
    {
        String paramString;
        BlockMathMinmax bmm = (BlockMathMinmax) this.block;
        int numArgs = bmm.numInputs();
        if (numArgs == 1) {
            paramString = "'Gain','1'," + this.createPositionString(x,y);
            writer.addBuiltInBlock("Gain", this.getName(), paramString);
        }
        if (numArgs >= 2) {
//            paramString = "'Ports',[" + numArgs + ",1],";
            paramString = this.createPositionString(x,y);
            paramString += ",'Function','" + bmm.getFuncType() + "'";
            paramString += ",'Inputs', '" + numArgs + "'";
            writer.addBuiltInBlock("MinMax", this.getName(), paramString);
        }
    }

    /**
     *
     * Generates add_block command for product block
     *
     **/

    public void writeMforMult( SLFileWriter writer, int x, int y )
        throws IOException
    {
        BlockMathProduct bmp = (BlockMathProduct) this.getBlock();
        String blockType = bmp.getBlockType();
        String inputs;
        if(blockType.equals("times"))
            inputs = "'" + this.getBlock().numInputs() + "'";
        else
            inputs = "'*/'";
        String paramString = "'Multiplication','Element-wise(.*)',"
            + "'SaturateOnIntegerOverflow','on','Inputs'," + inputs 
            + "," + this.createPositionString(x,y);
        writer.addBuiltInBlock("Product", this.getName(), paramString);
    }


    /**
     *
     * Generates add_block command for MathRelation block
     *
     **/

    public void writeMforRelation( SLFileWriter writer, int x, int y )
        throws IOException
    {
        BlockMathRelation bmr = (BlockMathRelation) this.getBlock();
        String relationOp = bmr.getRelationOp();
        String paramString = this.createPositionString(x,y) + ",'Operator',";
        if(relationOp.equals("lt"))  paramString += "'<'";
        if(relationOp.equals("leq")) paramString += "'<='";
        if(relationOp.equals("eq"))  paramString += "'=='";
        if(relationOp.equals("geq")) paramString += "'>='";
        if(relationOp.equals("gt"))  paramString += "'>'";
        if(relationOp.equals("neq")) paramString += "~='";
        writer.addBuiltInBlock("RelationalOperator", this.getName(), paramString);
    }


    /**
     *
     * Generates add_block command for MathSum block
     *
     **/

    public void writeMforSum( SLFileWriter writer, int x, int y )
        throws IOException
    {
        String signs = "";
        for( int i = 0; i< this.getBlock().numInputs(); i++ ) 
            signs = signs+"+";
        String paramString = "'IconShape','rectangular','Inputs','" + signs + "',";
        paramString += this.createPositionString(x,y);
        writer.addBuiltInBlock("Sum", this.getName(), paramString);
    }


    /**
     *
     * Generates add_block command for node (gain = 1) block
     *
     **/

    public void writeMforNode( SLFileWriter writer, int x, int y )
        throws IOException
    {
        String paramString = "'Gain','1',";
        paramString += this.createPositionString(x,y);
        writer.addBuiltInBlock("Gain", this.getName(), paramString);
    }


    /**
     *
     * Generates add_block command for switch block
     *
     **/

    public void writeMforSwitch( SLFileWriter writer, int x, int y )
        throws IOException
    {
        String paramString = "'Threshold','0.5',";
        paramString += this.createPositionString(x,y);
        writer.addBuiltInBlock("Switch", this.getName(), paramString);
    }


    /**
     *
     * <p> Creates Position parameter setting string to properly position the block with padding </p>
     *
     * @param x int containing centerpoint horizontal position relative to upper left corner of diagram
     * @param y int containing centerpoint vertical offset relative to upper left corner of diagram
     *
     **/

    String createPositionString( int x, int y )
        throws IOException
    {
        int x0 = x - this.mdlWidth/2 + this.xPad;
        int y0 = y - this.mdlHeight/2 + this.yPad;

        String paramLine = "'Position',[" + x0 + "," 
            + y0 + "," + (x0+this.mdlWidth) + "," + (y0+this.mdlHeight) + "]";
        return paramLine;
    }


    /**
     *
     * Creates necessary m-script lines to save our data.
     *
     * @param writer <code>MatFileWriter</code> to receive data
     *
     **/

    public void writeMat(MatFileWriter writer)
        throws IOException
    {
        if (this.block instanceof BlockFuncTable) {
            BlockFuncTable bft = (BlockFuncTable) this.block;
            FuncTable ft = bft.getFunctionTableDef();
            String tableName = MDLNameList.convertToMDLString(ft.getGTID());

            // see if already written - only one copy allowed with same name
            if (!this.writtenTables.contains( tableName )) {
                writer.writeln("%% Block \"" + this.getName() + "\" wishes to save the following data:");
                writer.writeMatrix( ourDiagram.model.getName() + "_data." + tableName, 
                                    ft.getValues(),
                                    ft.getDimensions());
                this.writtenTables.add( tableName );
            } else {
                writer.writeln("%% Block \"" + this.getName() + "\" did not duplicate '" + tableName + "'.");
            }         
        } else if (this.block instanceof BlockBP) {
            BreakpointSet bpSet = ((BlockBP) this.getBlock()).getBPset();
            writer.writeln("%% Block \"" + this.getName() + "\" wishes to save the following breakpoint vector:");
            writer.writeln( ourDiagram.model.getName() + "_data." 
                            + MDLNameList.convertToMDLString(this.getName())+ "_pts = ["
                            + bpSet.values() + "];");

        } else {
            writer.writeln("%% Block \"" + this.getName() + "\" has no data to save.");
        }

    }


    /**
     *
     * Returns row number (1-based)
     *
     **/

    public int getRow()    { return this.myRow; }


    /**
     *
     * Returns col number (1-based)
     *
     **/

    public int getCol()   { return this.myCol; }


    /**
     *
     * Returns the name of the encased Block
     *
     **/

    public String getName() { return this.block.getName(); }

    /**
     *
     * Returns the width of the desired Simulink block.
     *
     **/

    public int getMDLWidth() { return this.mdlWidth; }


    /**
     *
     * Returns the height of the desired Simulink block.
     *
     **/

    public int getMDLHeight() { return this.mdlHeight; }


    /**
     *
     * Set verbose flag for us and block
     *
     **/

    public void makeVerbose() 
    { 
        this.verboseFlag = true; 
        if (this.block != null )
            this.block.makeVerbose();
    }


    /**
     *
     * Indicates status of verbose flag
     *
     **/

    public boolean isVerbose() { return this.verboseFlag; }


    /**
     *
     * Clears the verbose flag
     *
     **/

    public void silence() 
    { 
        this.verboseFlag = false; 
        if (this.block != null)
            this.block.silence();
    }


    /**
     *
     * Return encapsulated {@link gov.nasa.daveml.dave.Block}
     *
     **/

    public Block getBlock() { return this.block; }


    /**
     *
     * Calls the main findChildren routine with a starter prefix string.
     *
     **/

    public void findChildren() { this.findChildren(" "); }


    /**
     *
     * Should only run after network complete. Searches downstream
     * signal and gets list of all immediate children.
     *
     **/

    private void findChildren(String prefix)
    {
        Signal sig = null;
        BlockArrayList blks;
        Block kid;

        // Only run if haven't before, since children shouldn't change

        if ( children.size() == 0 ) {
            if (this.isVerbose()) {
                System.out.println("Finding children of block '" + this.block.getName() + "'...");
            }
            // iterate through downstream signals to get next level blocks
            sig = this.block.getOutput();       // our output signal
            if (sig != null) {
                blks = sig.getDests();          // our signal's destination blocks
                if( blks == null ) {
                    System.err.println("ERROR: block '" + this.block.getName() + "' has an dangling output!");
                    System.exit(0);
                } else {
                    for ( Iterator<Block> iBlk = blks.iterator(); iBlk.hasNext(); ) {
                        kid = iBlk.next();
                        children.add(kid);
                        SLBlock kidSLBlock = (SLBlock) kid.getMask();
                        if (kidSLBlock == null) {
                            System.err.println("Block " + getName() + " has no Simulink block assigned!");
                            System.exit(0);
                        }
                        kidSLBlock.findChildren(prefix+ " ");   // this may be redundant
                    }
                }
            }
        }
    }


    /**
     *
     * Assigns this block to a row and a column (1-based).
     *
     * <p>
     * Should only run after children found. If we are assigned a
     * deeper column than we had before, tell all children to go 1
     * deeper; find out their depth in rows and set next child below
     * them. Returns lowest row of any child.
     *
     * @param minimumRow (1-based) value of our min row. 
     * @param minimumCol (1-based) value of our min column. Inports usually row 1.
     * @param prefix <code>String</code> containing offset prefix for writing diagnostics
     *
     **/

    public int setPosition( int minimumRow, int minimumCol, String prefix )
    {
        Block kid = null;

        //System.out.print(prefix + "Setting position of " + myName + " and kids. Original row was " + this.myRow);     
        if (this.myRow == 0)  this.myRow = minimumRow;          // don't go lower, but...
        if (minimumCol > this.myCol)  this.myCol = minimumCol;  // ... can slide right

        this.rowDepthOfChildren = myRow;

        //System.out.println("; now set to " + this.myRow);     

        int offset = 0;
        for ( Iterator<?> ikid = children.iterator(); ikid.hasNext(); )
            {
                kid = (Block) ikid.next();
                //System.out.println(prefix + "Calling kid named " + kid.getName() + " with offset " + offset);
                SLBlock kidSLBlock = (SLBlock) kid.getMask();
                if (kidSLBlock == null) {
                    System.err.println("Unexpected null mask found for block '" + kid.getName() + "'.");
                    System.exit(0);
                }
                int returnedRow =
                    kidSLBlock.setPosition( this.rowDepthOfChildren + offset, this.myCol + 1, (prefix + " ") );
                if ( this.rowDepthOfChildren < returnedRow ) this.rowDepthOfChildren = returnedRow;

                //System.out.println(prefix + "row depth of children set to " + this.rowDepthOfChildren);
                offset = 1;     // after first iteration, step down each time
            }

        //System.out.println(prefix + "Position of block " + myName + ": (" + myRow + "," + myCol + "); returning depth " + this.rowDepthOfChildren);

        if ( myRow > this.rowDepthOfChildren )
            return myRow;
        else
            return this.rowDepthOfChildren;
    }


    /**
     *
     * Sets position with a default indentation of single space, if not specified
     *
     * @param minRow (1-based) value of our min row. 
     * @param minCol (1-based) value of our min column. Inports usually row 1.
     *
     **/

    public int setPosition( int minRow, int minCol ) { return setPosition( minRow, minCol, " "); }


    /**
     *
     * Sets value for row and column, overwriting previous values.
     *
     * @param theRow (1-based) value of our new row.
     * @param theCol (1-based) value of our new column.
     * 
     **/

    public void setRowCol( int theRow, int theCol ) 
    { 
        //      System.out.println("  >>> Block " + getName() + " position updated from [" +
        //                         myRow + "," + myCol + "] to [" +
        //                         theRow + "," + theCol + "].");
        myRow = theRow; 
        myCol = theCol; 
        return; 
    }


    /**
     *
     * Recurse through children to find farthest column.
     *
     **/

    public int findChildrensFarthestColumn()
    {
        Block kid = null;

        int farthest = this.myCol;
        for (Iterator<Block> ikid = children.iterator(); ikid.hasNext(); )
            {
                kid = ikid.next();
                SLBlock kidSLBlock = (SLBlock) kid.getMask();
                if (kidSLBlock == null) {
                    System.err.println("Unexpected null mask found for block '" + kid.getName() + "'.");
                    System.exit(0);
                }
                int kidsCol = kidSLBlock.findChildrensFarthestColumn();
                if(kidsCol > farthest) farthest = kidsCol;
            }
        return farthest;
    }


    /**
     *
     * Recurse through children to find deepest row.
     *
     **/

    public int findChildrensDeepestRow()
    {
        Block kid = null;

        int deepest = this.myRow;
        for(Iterator<Block> ikid = children.iterator(); ikid.hasNext(); )
            {
                kid = ikid.next();
                SLBlock kidSLBlock = (SLBlock) kid.getMask();
                if (kidSLBlock == null) {
                    System.err.println("Unexpected null mask found for block '" + kid.getName() + "'.");
                    System.exit(0);
                }
                int kidsRow = kidSLBlock.findChildrensDeepestRow();
                if(kidsRow > deepest) deepest = kidsRow;
            }
        return deepest;
    }

    /** 
     *
     * <p> Recursively prints table values </p>
     *
     * @param table the <code>ArrayList</code> representing the values in the table
     * @param dims a vector of <code>int</code>s containing the
     *        dimensions of the table represented by <code>table</code>
     * @param startIndex a zero-based offset into the <code>dims</code> vector 
     *        showing which dimension to operate on (to support recursion)
     *
     **/

    public static int printTable( Writer writer, 
				  ArrayList<Double> table, 
				  int[] dims, int startIndex)
        throws IOException {

        int offset;
	int i;

        switch (dims.length) {
	case 0:     // shouldn't happen
	    return 0;
	case 1:
	    for ( i = 0; i < dims[0]; i++) {
		Double theValue = table.get(i+startIndex);
		writer.write( theValue.toString() );
		if( i < dims[0]-1) writer.write(", ");
	    }
	    return i;
	case 2:
	    for ( i = 0; i < dims[0]; i++) {
		int[] newDims = new int[1];
		newDims[0] = dims[1];
		offset = printTable( writer, table, newDims, startIndex );
		if( i < dims[0]-1) writer.write(", ");
		writer.write("\n");
		startIndex = startIndex + offset;
	    }
	    return startIndex;
	default:
	    for ( i = 0; i < dims[0]; i++ ) {
		int[] newDims = new int[1];
		newDims[0] = dims[1];
		//System.out.println(" For dimension " + dims.length + " layer " + i);
		//System.out.println();
		offset = printTable( writer, table, newDims, startIndex );
		if( i < dims[0]-1) writer.write(", ");
		writer.write("\n");
		startIndex = startIndex + offset;
	    }
	    return startIndex;
	}
    }

    /** 
     *
     * <p> This method calls recursive table value printer method </p>
     *
     **/

    public static void printTable( ArrayList<Double> table, int[] dims)
        throws IOException {

        // point to System.out
        OutputStreamWriter osw = new OutputStreamWriter(System.out);
        printTable(osw, table, dims, 0);
    }

    /** 
     *
     * <p> This method directs output to designated Writer </p>
     *
     * @param writer <code>Writer</code> to receive values
     * @param table <code>ArrayList</code> of values to print
     * @param dims array of integers representing dimensions of table
     *
     **/

    public static void printTable( Writer writer, ArrayList<Double> table, int[] dims)
        throws IOException {
        printTable(writer, table, dims, 0);
    }
}
