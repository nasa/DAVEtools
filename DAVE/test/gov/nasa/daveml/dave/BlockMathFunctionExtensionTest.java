package gov.nasa.daveml.dave;

import java.io.IOException;
import java.util.ArrayList;

public class BlockMathFunctionExtensionTest extends BlockMathFunctionTest {
        
    // Tests the extensions to MathML function space. ATAN2 is sole example thus far:
    //  <apply>
    //    <csymbol definitionURL="http://daveml.org/function_spaces.html#atan2" encoding="text">
    //      atan2
    //    </csymbol>
    //    <ci>0.5</ci>
    //    <ci>0.6</ci>
    //  </apply>

    final double piOver4 = Math.PI/4.0;

    protected void setUp() throws Exception {
        super.setUp();
        instantiateAtan2Function( 0.5, 0.5 );
    }
        
    public void testBlockMathFunction_ctor() {

        instantiateAtan2Function(  0.0,  0.5 ); assertEquals(          0.0, _block.getValue(), EPS );
        instantiateAtan2Function(  0.5,  0.5 ); assertEquals(   1.*piOver4, _block.getValue(), EPS );
        instantiateAtan2Function(  0.5,  0.0 ); assertEquals(   2.*piOver4, _block.getValue(), EPS );
        instantiateAtan2Function(  0.5, -0.5 ); assertEquals(   3.*piOver4, _block.getValue(), EPS );
        instantiateAtan2Function(  0.0, -0.5 ); assertEquals(   4.*piOver4, _block.getValue(), EPS );
        instantiateAtan2Function( -0.5,  0.5 ); assertEquals(  -1.*piOver4, _block.getValue(), EPS );
        instantiateAtan2Function( -0.5,  0.0 ); assertEquals(  -2.*piOver4, _block.getValue(), EPS );
        instantiateAtan2Function( -0.5, -0.5 ); assertEquals(  -3.*piOver4, _block.getValue(), EPS );

	// should be NaN
        instantiateAtan2Function(  0.0,  0.0 ); assertEquals(          0.0, _block.getValue(), EPS );
    }
        
    public void testUpdate() {

        updateExistingBlock( "atan2",  0.0,  0.5,         0.0 );
        updateExistingBlock( "atan2",  0.5,  0.5,  1.*piOver4 );
        updateExistingBlock( "atan2",  0.5,  0.0,  2.*piOver4 );
        updateExistingBlock( "atan2",  0.5, -0.5,  3.*piOver4 );
        updateExistingBlock( "atan2",  0.0, -0.5,  4.*piOver4 );
        updateExistingBlock( "atan2", -0.5,  0.5, -1.*piOver4 );
        updateExistingBlock( "atan2", -0.5,  0.0, -2.*piOver4 );
        updateExistingBlock( "atan2", -0.5, -0.5, -3.*piOver4 );
                
        updateExistingBlock( "atan2",  0.0,  0.0,         0.0 ); // should be NaN
    }

    public void updateExistingBlock( String func, double arg1, double arg2, double expectedValue) {
        BlockMathConstant constBlk = null;
        constBlk = getUpstreamConstBlk1();
        constBlk.setValue( arg1 );
        constBlk = getUpstreamConstBlk2();
        constBlk.setValue( arg2 );
        try {
            _block.setFunction( func );
        } catch (DAVEException e) {
            fail("Unexpected exception when calling setFunction() for BlockMathFunction block: "
                 + e.getMessage() );
        }
        try {
            _block.update();
        } catch (DAVEException e) {
            fail("Unexpected exception when calling update() for BlockMathFunction block: "
                 + e.getMessage() );
        }               
        assertEquals(expectedValue, _block.getValue(), EPS );
    }


    protected void instantiateAtan2Function( double argument1, double argument2 ) {
        String csymbol    = "csymbol";
        String attrName1  = "definitionURL";
        String attrValue1 ="http://daveml.org/function_spaces.html#atan2";
        String attrName2  = "encoding";
        String attrValue2 = "text";
        String extFuncName = "atan2";
        ArrayList<Double> args = new ArrayList<Double>();
        boolean expectException = false;
        args.add(argument1);
        args.add(argument2);
        instantiateFunction( csymbol, "two-argument arctangent function", 
                             attrName1, attrValue1, attrName2, attrValue2, extFuncName,
                             args, expectException );
    }

    public void testDescribeSelfWriter() {
        try {
            _block.describeSelf(_writer);
        } catch (IOException e) {
            fail("testDescribeSelfWriter of TestBlockMathSum threw unexpected exception: " 
                 + e.getMessage() );
        }
        assertEquals( "Block \"atan2_1\" has two inputs (const_0.5_, const_0.5_)," +
                      " NO OUTPUTS, value [0.7853981633974483] and is a Function math block.", 
                      _writer.toString() );
    }

}