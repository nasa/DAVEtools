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
    private Signal signal;
    private StringWriter _writer;
    private BlockLimiter blockArray[];
    private VectorInfoArrayList inputVector;
//    private VectorInfoArrayList outputVector;
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

        signal = new Signal("unlimited signal", model);
        assert(signal != null);
        signal.setUnits("furlongs");

        inputBlk = new BlockInput(signal, model);
        assert(inputBlk != null);

        symLimBlk = new BlockLimiter(signal, model, -2.0, 2.0);
        assert(symLimBlk != null);

        hiLimBlk = new BlockLimiter(signal, model, Double.NEGATIVE_INFINITY, 2.0);
        assert(hiLimBlk != null);

        loLimBlk = new BlockLimiter(signal, model, -2.0, Double.POSITIVE_INFINITY);
        assert(loLimBlk != null);

        noLimBlk = new BlockLimiter(signal, model, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        assert(noLimBlk != null);

        swapLimBlk = new BlockLimiter(signal, model, +2.0, -2.0);
        assert(swapLimBlk != null);

        blockArray = new BlockLimiter[5];
        blockArray[0] = symLimBlk;
        blockArray[1] = hiLimBlk;
        blockArray[2] = loLimBlk;
        blockArray[3] = noLimBlk;
        blockArray[4] = swapLimBlk;

        inputVector = model.getInputVector();
        assert(inputVector != null);
        assertEquals(1, inputVector.size());

        input = inputVector.get(0);
        assert(input != null);

        model.hookUpIO();

//        outputVector = model.getOutputVector();
//        assert(outputVector != null);
//        assertEquals(5, outputVector.size());


}

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getValue method, of class BlockLimiter.
     */
    public void testGetValue() {
        BlockLimiter blk;
        for( int i=0; i < 3; i++ ) { // cycle through input values
            input.setValue(inputValue[i]);
            try {
                model.cycle();
            } catch( DAVEException e) {
                String msg = e.getMessage();
            }
            for( int j=0; j < blockArray.length; j++) {
                blk = blockArray[j];
                double value = blk.getValue();
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
            assertEquals( "Block \"unlimited signal limiter\" has one input (unlimited signal), NO OUTPUTS," +
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
