/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;
import junit.framework.TestCase;

/**
 *
 * @author ebjackso
 */
public class BlockLimiterTest extends TestCase {

    private Model model;
    private BlockInput   inputBlk;
    private BlockLimiter symLimBlk;
    private BlockLimiter hiLimBlk;
    private BlockLimiter loLimBlk;
    private BlockLimiter noLimBlk;
    private BlockLimiter swapLimBlk;
    private Signal symOut, hiOut, loOut, noOut, swapOut;
    private Signal inputSignal;
    private StringWriter _writer;
    private BlockLimiter blockArray[];
    private VectorInfoArrayList inputVector;
    private VectorInfoArrayList outputVector;
    private VectorInfo input;
    final private double eps = 1e-6;
    private double inputValue[] = {-1000.0, 0.0, 1000.0};
    private double expectedValue[][] = {
    //  symLim    hiLim    loLim    noLim  swapped
        { -2.0, -1000.0,    -2.0, -1000.0, -2.0 }, // input = -1000.0
        {  0.0,     0.0,     0.0,     0.0,  0.0 }, // input =     0.0
        { +2.0,    +2.0, +1000.0, +1000.0, +2.0 }};// input = +1000.0

    
    public BlockLimiterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _writer = new StringWriter();
        assert(_writer != null);
        
        model = new Model(3,3);
        assert(model != null);
        
        // create single input signal
        inputSignal = new Signal("unlimited signal", "input", "furlongs", 3, model);
        assert(inputSignal != null);
        
        // create and hook up input block to signal
        inputBlk = new BlockInput(inputSignal, model);
        assert(inputBlk != null);

        // create five output signals; one for each limiter block being tested
        symOut = new Signal("symmetric limiter output", "symLim", "furlongs", 1, model);
        assert(symOut != null);
        
        hiOut  = new Signal("upper limiter output", "hiLim", "furlongs", 1, model);
        assert(hiOut != null);
        
        loOut  = new Signal("lower limiter output", "loLim", "furlongs", 1, model);
        assert(loOut != null);
        
        noOut  = new Signal("no limiter output", "noLim", "furlongs", 1, model);
        assert(noOut != null);
        
        swapOut  = new Signal("swapped limiter output", "swapLim", "furlongs", 1, model);
        assert(swapOut != null);
        
        // now create the five limiters to test and hook up to output signals
        symLimBlk = new BlockLimiter(inputSignal, model, -2.0, 2.0);
        assert(symLimBlk != null);
        symLimBlk.addOutput(symOut);

        hiLimBlk = new BlockLimiter(inputSignal, model, Double.NEGATIVE_INFINITY, 2.0);
        assert(hiLimBlk != null);
        hiLimBlk.addOutput(hiOut);

        loLimBlk = new BlockLimiter(inputSignal, model, -2.0, Double.POSITIVE_INFINITY);
        assert(loLimBlk != null);
        loLimBlk.addOutput(loOut);

        noLimBlk = new BlockLimiter(inputSignal, model, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        assert(noLimBlk != null);
        noLimBlk.addOutput(noOut);

        swapLimBlk = new BlockLimiter(inputSignal, model, +2.0, -2.0);
        assert(swapLimBlk != null);
        swapLimBlk.addOutput(swapOut);

        // put tested limiters in an array for convenience
        blockArray = new BlockLimiter[5];
        blockArray[0] = symLimBlk;
        blockArray[1] = hiLimBlk;
        blockArray[2] = loLimBlk;
        blockArray[3] = noLimBlk;
        blockArray[4] = swapLimBlk;

        // get the input vector; confirm we have a single input
        inputVector = model.getInputVector();
        assert(inputVector != null);
        assertEquals(1, inputVector.size());

        input = inputVector.get(0);
        assert(input != null);

        // generate the output blocks
        model.hookUpIO();
        
        // sort the model
        model.initialize();

        // get the output vector; confirm there are five
        outputVector = model.getOutputVector();
        assert(outputVector != null);
        assertEquals(5, outputVector.size());

}

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getValue method, of class BlockLimiter.
     */
    public void testGetValue() {
        for( int i=0; i < 3; i++ ) { // cycle through input values
            input.setValue(inputValue[i]);
            try {
                model.cycle();
            } catch( DAVEException e) {
                String msg = e.getMessage();
            }
            for( int j=0; j < outputVector.size(); j++) {
                double value = outputVector.get(j).getValue();
                assertEquals( expectedValue[i][j], value, eps);
            }
        }
    }

    /**
     * Test of getUnits method, of class BlockLimiter.
     */
    public void testGetUnits() {
        BlockLimiter blk;
        for( int j=0; j < blockArray.length; j++) {
            blk = blockArray[j];
            assertEquals( "furlongs", blk.getUnits());
        }
    }
    
    /** 
     * Test code generator
     */
    
    public void testGenCcode() {
        BlockLimiter blk;
        model.setCodeDialect(Model.DT_ANSI_C);
        CodeAndVarNames result = new CodeAndVarNames();

        // check symmetric limiter code
        blk = blockArray[0];
        result = blk.genCode();
        assertEquals(
                "  symLim = input;\n" +
                "  if ( symLim < -2.0 ) {\n" +
                "    symLim = -2.0;\n" +
                "  }\n" +
                "  if ( symLim > 2.0 ) {\n" +
                "    symLim = 2.0;\n" +
                "  }\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("symLim", result.getVarName(0));
        assertEquals("input", result.getVarName(1));
        
        // check upper limiter code
        blk = blockArray[1];
        result = blk.genCode();
        assertEquals(
                "  hiLim = input;\n" +
                "  if ( hiLim > 2.0 ) {\n" +
                "    hiLim = 2.0;\n" +
                "  }\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("hiLim", result.getVarName(0));
        assertEquals("input", result.getVarName(1));
        
        // check lower limiter code
        blk = blockArray[2];
        result = blk.genCode();
        assertEquals(
                "  loLim = input;\n" +
                "  if ( loLim < -2.0 ) {\n" +
                "    loLim = -2.0;\n" +
                "  }\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("loLim", result.getVarName(0));
        assertEquals("input", result.getVarName(1));
        
        // check no limiter code
        blk = blockArray[3];
        result = blk.genCode();
        assertEquals(
                "  noLim = input;\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("noLim", result.getVarName(0));
        assertEquals("input", result.getVarName(1));
        
        // check swapped limits limiter code
        blk = blockArray[4];
        result = blk.genCode();
        assertEquals(
                "  swapLim = input;\n" +
                "  if ( swapLim < -2.0 ) {\n" +
                "    swapLim = -2.0;\n" +
                "  }\n" +
                "  if ( swapLim > 2.0 ) {\n" +
                "    swapLim = 2.0;\n" +
                "  }\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("swapLim", result.getVarName(0));
        assertEquals("input", result.getVarName(1));
     }



    public void testGenFcode() {
        BlockLimiter blk;
        model.setCodeDialect(Model.DT_FORTRAN);
        CodeAndVarNames result = new CodeAndVarNames();
        
        // check symmetric limiter code
        blk = blockArray[0];
        result = blk.genCode();
        assertEquals(
                "       symLim = input\n" +
                "       IF( symLim .LT. -2.0 ) THEN\n" +
                "         symLim = -2.0\n" +
                "       ENDIF\n" +
                "       IF( symLim .GT. 2.0 ) THEN\n" +
                "         symLim = 2.0\n" +
                "       ENDIF\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("symLim", result.getVarName(0));
        assertEquals("input", result.getVarName(1));
        
        // check upper limiter code
        blk = blockArray[1];
        result = blk.genCode();
        assertEquals(
                "       hiLim = input\n" +
                "       IF( hiLim .GT. 2.0 ) THEN\n" +
                "         hiLim = 2.0\n" +
                "       ENDIF\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("hiLim", result.getVarName(0));
        assertEquals("input", result.getVarName(1));
       
        // check lower limiter code
        blk = blockArray[2];
        result = blk.genCode();
        assertEquals(
                "       loLim = input\n" +
                "       IF( loLim .LT. -2.0 ) THEN\n" +
                "         loLim = -2.0\n" +
                "       ENDIF\n", result.getCode());
        
        // check no limiter code
        blk = blockArray[3];
        result = blk.genCode();
        assertEquals(
                "       noLim = input\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("noLim", result.getVarName(0));
        assertEquals("input", result.getVarName(1));
        
        // check swapped limits limiter code
        blk = blockArray[4];
        result = blk.genCode();
        assertEquals(
                "       swapLim = input\n" +
                "       IF( swapLim .LT. -2.0 ) THEN\n" +
                "         swapLim = -2.0\n" +
                "       ENDIF\n" +
                "       IF( swapLim .GT. 2.0 ) THEN\n" +
                "         swapLim = 2.0\n" +
                "       ENDIF\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("swapLim", result.getVarName(0));
        assertEquals("input", result.getVarName(1));

     }



    /**
     * Test of describeSelf method, of class BlockLimiter.
     */
    public void testDescribeSelf() throws Exception {
        BlockLimiter blk;
        for( int i=0; i < blockArray.length; i++) {
            blk = blockArray[i];
            _writer = new StringWriter(); // clear any previous description
            String testString = _writer.toString();
            try {
                blk.describeSelf(_writer);
            } catch (IOException e) {
                assertTrue(false);
            }
            assertEquals( "Block \"unlimited signal limiter\" has one input (unlimited signal), one output (" +
                        blk.getOutput().getName() + ")," +
                        " value [NaN] (furlongs) and is a limiter block with a lower limit of " +
                        Double.toString( blk.getLowerLimit() ) + " and an upper limit of " +
                        Double.toString( blk.getUpperLimit() ) + ".",
                        _writer.toString() );
            }
    }

    /**
     * Test of hasLowerLimit method, of class BlockLimiter.
     */
    public void testHasLowerLimit() {
        assert(  symLimBlk.hasLowerLimit() );
        assert(  !hiLimBlk.hasLowerLimit() );
        assert(   loLimBlk.hasLowerLimit() );
        assert(  !noLimBlk.hasLowerLimit() );
        assert( swapLimBlk.hasLowerLimit() );
    }

    /**
     * Test of hasUpperLimit method, of class BlockLimiter.
     */
    public void testHasUpperLimit() {
        assert(  symLimBlk.hasUpperLimit() );
        assert(   hiLimBlk.hasUpperLimit() );
        assert(  !loLimBlk.hasUpperLimit() );
        assert(  !noLimBlk.hasUpperLimit() );
        assert( swapLimBlk.hasUpperLimit() );
    }

    /**
     * Test of getLowerLimit method, of class BlockLimiter.
     */
    public void testGetLowerLimit() {
        assertEquals(     -2.0,  symLimBlk.getLowerLimit(), eps);
        assert( Double.isInfinite(hiLimBlk.getLowerLimit()));
        assertEquals(     -2.0,   loLimBlk.getLowerLimit(), eps);
        assert( Double.isInfinite(noLimBlk.getLowerLimit()));
        assertEquals(     -2.0, swapLimBlk.getLowerLimit(), eps);
    }

    /**
     * Test of getUpperLimit method, of class BlockLimiter.
     */
    public void testGetUpperLimit() {
        assertEquals(     +2.0,  symLimBlk.getUpperLimit(), eps);
        assertEquals(     +2.0,   hiLimBlk.getUpperLimit(), eps);
        assert( Double.isInfinite(loLimBlk.getUpperLimit()));
        assert( Double.isInfinite(noLimBlk.getUpperLimit()));
        assertEquals(     +2.0, swapLimBlk.getUpperLimit(), eps);
    }

}
