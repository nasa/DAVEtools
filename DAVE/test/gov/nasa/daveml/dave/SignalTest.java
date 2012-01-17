package gov.nasa.daveml.dave;

import org.jdom.Element;
import junit.framework.*;
import java.util.ArrayList;
import java.util.Iterator;



/**
 * Tests the Signal object
 *
 * 060913 Bruce Jackson, NASA Langley <mailto:bruce.jackson@nasa.gov>
 *
 **/

public class SignalTest extends TestCase {

    private Signal _ds; // default signal constructor
    private Signal _ss; // simple signal constructor
    private Signal _fs; // full signal constructor
    private Signal _cs; // signal from var def with calculation
    private Block  _bc; // upstream constant block
    private Block  _bd; // downstream output block
    private Model  _m;  // parent model for simple system
    private final double eps = 1e-6;

    @Override
    protected void setUp() {
        _m  = new Model(3,3);
        _ds = new Signal();
        _ss = new Signal( "new signal", _m );
        _fs = new Signal( "full signal", "fulsig", "deg", 4, _m );
        _fs.setLowerLimit( -3.45 );
        _fs.setUpperLimit(  67.8 );
        _bc = new BlockMathConstant( "-3.45", _m );
        _bd = new BlockOutput( _ss, _m );

    }  // end of setup

    public void testToValidId() {
        String input    = "what is the Matter";
        String expected = "what_is_the_Matter";
        assertTrue( Signal.toValidId(input).equals(expected));
        input    = "where_is_this";
        expected = "where_is_this";
        assertTrue( Signal.toValidId(input).equals(expected));
    }

    public void testDefaultCtor() {
        assertTrue( (_ds.getName().equals("unnamed")));
        try {
            assertTrue( _ds.sourceReady() ); // should fail; no block
            fail("Expected exception.");
        } catch ( DAVEException e ) { 
            assertTrue( true );         // should throw exception
        }
        assertTrue( (_ds.getVarID() .equals("unnamed")));
        assertTrue( (_ds.getUnits() == null ? "" == null : _ds.getUnits().equals("")));
        assertTrue(  _ds.getSource() == null );
        assertTrue(  _ds.getSourcePort() == 0 );
        assertFalse( _ds.hasSource() );
        assertFalse( _ds.hasDest() );
        assertFalse( _ds.hasIC() );
        assertFalse( _ds.isInput() );
        assertFalse( _ds.isControl() );
        assertFalse( _ds.isDisturbance() );
        assertFalse( _ds.isState() );
        assertFalse( _ds.isStateDeriv() );
        assertFalse( _ds.isInput() );
        assertFalse( _ds.isOutput() );
        assertFalse( _ds.isStdAIAA() );
        assertFalse( _ds.isDerived() );
        assertFalse( _ds.isDefined() );
        assertFalse( _ds.hasLowerLimit() );
        assertFalse( _ds.hasUpperLimit() );
        assertFalse( _ds.isLimited() );
        assertTrue( Double.isInfinite(_ds.getLowerLimit() ));
        assertTrue( Double.isInfinite(_ds.getUpperLimit() ));
    }

    public void testFullCtor() {
        assertTrue( (_fs.getName() == null ? "full signal" == null : _fs.getName().equals("full signal")));
        assertTrue( (_fs.getVarID() == null ? "fulsig" == null : _fs.getVarID().equals("fulsig")));
        assertTrue( (_fs.getUnits() == null ? "deg" == null : _fs.getUnits().equals("deg")));
        assertTrue(  _fs.getSource() == null );
        assertTrue(  _fs.getSourcePort() == 0 );
        assertFalse( _fs.hasSource() );
        assertFalse( _fs.hasDest() );
        assertFalse( _fs.hasIC() );
        assertFalse( _fs.isInput() );
        assertFalse( _fs.isControl() );
        assertFalse( _fs.isDisturbance() );
        assertFalse( _fs.isState() );
        assertFalse( _fs.isStateDeriv() );
        assertFalse( _fs.isInput() );
        assertFalse( _fs.isOutput() );
        assertFalse( _fs.isStdAIAA() );
        assertFalse( _fs.isDerived() );
        assertFalse( _fs.isDefined() );
        assertTrue(  _fs.hasLowerLimit() );
        assertTrue(  _fs.hasUpperLimit() );
        assertTrue(  _fs.isLimited() );
        assertEquals( -3.45, _fs.getLowerLimit(), eps );
        assertEquals(  67.8, _fs.getUpperLimit(), eps );
    }

    public void testSingleSignal() {
                
        // build a Signal from by tying together existing Blocks (defined in setUp())
        // this test does not use XML snippet

        assertTrue( _bd != null );

        assertTrue( _ss.getName().equals("new signal"));
        try {
            assertTrue( _ss.sourceReady() ); // should fail; no block
            fail("Expected to receive an exception.");
        } catch ( DAVEException e ) { 
            assertTrue( true );
        }

        try { _bc.update(); }
        catch (DAVEException e) {
            fail("Exception thrown while performing update of constant block in testSingleSignal: " + e.getMessage());
        }

        try { 
            _bc.addOutput( _ss );
            assertTrue( _ss.sourceReady() );
        } catch (DAVEException e) {
            fail("Exception thrown while adding output to constant block in testSingleSignal: " + e.getMessage());
        }

        try {
            assertEquals( -3.45, _ss.sourceValue(), 0.0000001 );
        } catch (DAVEException e) {
            fail("Exception thrown while getting value of signal source in testSingleSignal: " + e.getMessage());
        }

        try { _m.initialize(); }
        catch (DAVEException e ) {
            fail("Exception thrown while initializing model in testSingleSignal: " + e.getMessage());
        }

        assertTrue(  _ss.getVarID().equals("new_signal"));
        assertTrue(  _ss.getUnits().equals("unkn"));
        assertTrue(  _ss.getSource() == _bc );
        assertTrue(  _ss.getSourcePort() == 1 );
        assertTrue(  _ss.hasSource() );
        assertTrue(  _ss.hasDest() );
        assertFalse( _ss.hasIC() );
        assertFalse( _ss.isInput() );
        assertFalse( _ss.isControl() );
        assertFalse( _ss.isDisturbance() );
        assertFalse( _ss.isState() );
        assertFalse( _ss.isStateDeriv() );
        assertFalse( _ss.isInput() );
        assertFalse( _ss.isOutput() );
        assertFalse( _ss.isStdAIAA() );
        assertFalse( _ss.isDerived() );
        assertFalse( _ss.isDefined() );
        assertFalse( _ss.hasLowerLimit() );
        assertFalse( _ss.hasUpperLimit() );
        assertFalse( _ss.isLimited() );
        assertTrue( Double.isInfinite(_ss.getLowerLimit() ));
        assertTrue( Double.isInfinite(_ss.getUpperLimit() ));

        BlockArrayList dests = _ss.getDests();
        assertTrue( dests != null );
        assertTrue( dests.size() == 1 );
        Iterator<Block> dbi = dests.iterator();
        assertTrue( dbi.hasNext() );
        Block destBlock = (Block) dbi.next();
        assertTrue( destBlock == _bd );

        ArrayList<Integer> destPorts = _ss.getDestPortNumbers();
        assertTrue( destPorts != null );
        assertTrue( destPorts.size() == 1 );
        Iterator<Integer> dpi = destPorts.iterator();
        assertTrue( dpi.hasNext() );
        Integer destPort = (Integer) dpi.next();
        assertTrue( destPort == 1 );
        
    }
    
    private void checkCode( int lang, Signal sig, String id ) {
        CodeAndVarNames cvn;
        _m.setCodeDialect(lang);
        
        // first order element
        sig.clearDerivedFlag();
        cvn = sig.genCode();
        assertFalse( cvn.getVarNames() == null );
        assertEquals( 1, cvn.getVarNames().size() );
        assertTrue( id.equals(cvn.getVarName(0)) );
        assertTrue( id.equals(cvn.getCode()) );
        assertFalse( sig.isDefined() ); // only emitted var name, not def
        sig.clearDefinedFlag();
        
        // derived element - since these are not hooked to any inputs, will 
        // return "" as code and empty varNames listarry
        sig.setDerivedFlag();
        cvn = sig.genCode();
        assertFalse( cvn.getVarNames() == null );
        assertEquals( 0, cvn.getVarNames().size() );
        assertTrue( "".equals(cvn.getCode()) );
        assertFalse( sig.isDefined() );
    }

    private void checkCode( Signal sig, String id ){
        checkCode( Model.DT_ANSI_C, sig, id);
        sig.clearDefinedFlag();
        checkCode( Model.DT_FORTRAN, sig, id);
    }

    public void testGenCode_DefaultSignal() {
        Signal sig = _ds;
        String id = "unnamed";
        checkCode( sig, id );
    }
    
    public void testGenCode_SingleSignal() {
        Signal sig = _ss;
        String id = "new_signal";
        checkCode( sig, id );
   }


    public void testGenCode_FullSignal() {
        Signal sig = _fs;
        String id = "fulsig";
        checkCode( sig, id );
    }

    public void testVerbose() {
        assertFalse( _ss.isVerbose() );
        _ss.makeVerbose();
        assertTrue( _ss.isVerbose() );
        _ss.silence();
        assertFalse( _ss.isVerbose() );
    }

    public void testSetGetLimits() {
        _ds.setLowerLimit("-2.0"); // string argument
        assertTrue(_ds.hasLowerLimit());
        assertFalse(_ds.hasUpperLimit());
        assertTrue(_ds.isLimited());
        assertEquals(-2.0, _ds.getLowerLimit(), eps);

        _ds.setLowerLimit(-4.0); // double argument
        assertTrue(_ds.hasLowerLimit());
        assertFalse(_ds.hasUpperLimit());
        assertTrue(_ds.isLimited());
        assertEquals(-4.0, _ds.getLowerLimit(), eps);

        _ds.setUpperLimit("+15.5");
        assertTrue(_ds.hasLowerLimit());
        assertTrue(_ds.hasUpperLimit());
        assertTrue(_ds.isLimited());
        assertEquals( +15.5, _ds.getUpperLimit(), eps);
        assertEquals( -4.0, _ds.getLowerLimit(), eps);

        _ds.setUpperLimit(15.1);
        assertTrue(_ds.hasLowerLimit());
        assertTrue(_ds.hasUpperLimit());
        assertTrue(_ds.isLimited());
        assertEquals( +15.1, _ds.getUpperLimit(), eps);
        assertEquals( -4.0, _ds.getLowerLimit(), eps);
    }

    public void testClearIsOutputFlag() {
        setFullSignalWithFlags("isOutput", null);
        _fs.clearIsOutputFlag();
        assertFalse( _fs.isOutput() );
    }


    public void testDeepCopy() {
        Signal s2 = new Signal( _fs );
        assertTrue(  s2.getName().equals("full signal"));
        assertTrue(  s2.getVarID().equals("fulsig"));
        assertTrue(  s2.getUnits().equals("deg"));
        assertTrue(  s2.getSource() == null );
        assertTrue(  s2.getSourcePort() == 0 );
        assertFalse( s2.hasSource() );
        assertFalse( s2.hasDest() );
        assertFalse( s2.hasIC() );
        assertFalse( s2.isInput() );
        assertFalse( s2.isControl() );
        assertFalse( s2.isDisturbance() );
        assertFalse( s2.isState() );
        assertFalse( s2.isStateDeriv() );
        assertFalse( s2.isInput() );
        assertFalse( s2.isOutput() );
        assertFalse( s2.isStdAIAA() );
        assertFalse( s2.isDerived() );
        assertTrue(  s2.hasLowerLimit() );
        assertTrue(  s2.hasUpperLimit() );
        assertTrue(  s2.isLimited() );
        assertEquals( -3.45, s2.getLowerLimit(), eps );
        assertEquals(  67.8, s2.getUpperLimit(), eps );
       try {
            assertTrue( _ds.sourceReady() );
            fail("Expected exception.");
        } catch ( DAVEException e ) { 
            assertTrue( true );
        }
    }

    public void testSignalMarkedAsInput() {
        // test building an input signal from an XML snippet
        setFullSignalWithFlags("isInput",null);
        assertFalse( _fs.hasIC() );
        assertTrue(  _fs.isInput() );
        assertFalse( _fs.isControl() );
        assertFalse( _fs.isDisturbance() );
        assertFalse( _fs.isState() );
        assertFalse( _fs.isStateDeriv() );
        assertFalse( _fs.isOutput() );
        assertFalse( _fs.isStdAIAA() );
 
    }
    public void testSignalMarkedAsControl() {
        // test building a control signal from an XML snippet
        setFullSignalWithFlags("isControl",null);
        assertFalse( _fs.hasIC() );
        assertFalse( _fs.isInput() );
        assertTrue(  _fs.isControl() );
        assertFalse( _fs.isDisturbance() );
        assertFalse( _fs.isState() );
        assertFalse( _fs.isStateDeriv() );
        assertFalse( _fs.isOutput() );
        assertFalse( _fs.isStdAIAA() );
    }
    public void testSignalMarkedAsDisturbance() {
        // test building a disturbance signal from an XML snippet
        setFullSignalWithFlags("isDisturbance",null);
        assertFalse( _fs.hasIC() );
        assertFalse( _fs.isInput() );
        assertFalse( _fs.isControl() );
        assertTrue(  _fs.isDisturbance() );
        assertFalse( _fs.isState() );
        assertFalse( _fs.isStateDeriv() );
        assertFalse( _fs.isOutput() );
        assertFalse( _fs.isStdAIAA() );
    }

    public void testSignalIncorrectlyMarkedAsBothInputAndControl() {
        // test building a mis-configured signal from an XML snippet
        setFullSignalWithFlags("isInput", "isControl");
        assertFalse( _fs.hasIC() );
        assertTrue(  _fs.isInput() ); // should only see first flag
        assertFalse( _fs.isControl() );
        assertFalse( _fs.isDisturbance() );
        assertFalse( _fs.isState() );
        assertFalse( _fs.isStateDeriv() );
        assertFalse( _fs.isOutput() );
        assertFalse( _fs.isStdAIAA() );
    }
    public void testSignalIncorrectlyMarkedAsBothInputAndDisturbance() {
        // test building a mis-configured signal from an XML snippet
        setFullSignalWithFlags("isInput", "isDisturbance");
        assertFalse( _fs.hasIC() );
        assertTrue(  _fs.isInput() ); // should only see first flag
        assertFalse( _fs.isControl() );
        assertFalse( _fs.isDisturbance() );
        assertFalse( _fs.isState() );
        assertFalse( _fs.isStateDeriv() );
        assertFalse( _fs.isOutput() );
        assertFalse( _fs.isStdAIAA() );
    }
    public void testSignalIncorrectlyMarkedAsBothControlAndDisturbance() {
        // test building a mis-configured signal from an XML snippet
        setFullSignalWithFlags("isControl", "isDisturbance");
        assertFalse( _fs.hasIC() );
        assertFalse( _fs.isInput() );
        assertTrue(  _fs.isControl() ); // should only see first flag
        assertFalse( _fs.isDisturbance() );
        assertFalse( _fs.isState() );
        assertFalse( _fs.isStateDeriv() );
        assertFalse( _fs.isOutput() );
        assertFalse( _fs.isStdAIAA() );
   }
    public void testSignalMarkedAsState() {
        // test building a state variable from an XML snippet
        setFullSignalWithFlags("isState", null);
        assertFalse( _fs.hasIC() );
        assertFalse( _fs.isInput() );
        assertFalse( _fs.isControl() );
        assertFalse( _fs.isDisturbance() );
        assertTrue(  _fs.isState() );
        assertFalse( _fs.isStateDeriv() );
        assertFalse( _fs.isOutput() );
        assertFalse( _fs.isStdAIAA() );
    }
    public void testSignalMarkedAsStateDeriv() {
        // test building a state derivative variable from an XML snippet
        setFullSignalWithFlags("isStateDeriv", null);
        assertFalse( _fs.hasIC() );
        assertFalse( _fs.isInput() );
        assertFalse( _fs.isControl() );
        assertFalse( _fs.isDisturbance() );
        assertFalse( _fs.isState() );
        assertTrue(  _fs.isStateDeriv() );
        assertFalse( _fs.isOutput() );
        assertFalse( _fs.isStdAIAA() );
    }
    public void testSignalMarkedAsOutput() {
        // test building an output variable from an XML snippet
        setFullSignalWithFlags("isOutput", null);
        assertFalse( _fs.hasIC() );
        assertFalse( _fs.isInput() );
        assertFalse( _fs.isControl() );
        assertFalse( _fs.isDisturbance() );
        assertFalse( _fs.isState() );
        assertFalse( _fs.isStateDeriv() );
        assertTrue(  _fs.isOutput() );
        assertFalse( _fs.isStdAIAA() );
    }
    public void testSignalMarkedAsStdAIAA() {
        // test building a predefined AIAA variable from an XML snippet
        setFullSignalWithFlags("isStdAIAA", null);
        assertFalse( _fs.hasIC() );
        assertFalse( _fs.isInput() );
        assertFalse( _fs.isControl() );
        assertFalse( _fs.isDisturbance() );
        assertFalse( _fs.isState() );
        assertFalse( _fs.isStateDeriv() );
        assertFalse( _fs.isOutput() );
        assertTrue(  _fs.isStdAIAA() );
    }

    //                                                                                    IC   min   max   output
    public void testSignalWithICBelowMinLimit()     { buildAndTestSignalWithIcPlusLimit(-100.,  -2., null,  -2.); }
    public void testSignalWithICAboveMinLimit()     { buildAndTestSignalWithIcPlusLimit( 100.,  -2., null, 100.); }
    public void testSignalWithICBelowMaxLimit()     { buildAndTestSignalWithIcPlusLimit(   1., null,   2.,   1.); }
    public void testSignalWithICAboveMaxLimit()     { buildAndTestSignalWithIcPlusLimit( 100., null,   2.,   2.); }
    public void testSignalWithICBelowMinMaxLimit()  { buildAndTestSignalWithIcPlusLimit(-100.,  -2.,   2.,  -2.); }
    public void testSignalWithICInsideMinMaxLimit() { buildAndTestSignalWithIcPlusLimit(   1.,  -2.,   2.,   1.); }
    public void testSignalWithICAboveMinMaxLimit()  { buildAndTestSignalWithIcPlusLimit( 100.,  -2.,   2.,   2.); }

    //                                                                                              A    B    min   max   output
    public void testSignalWithCalcBelowMinLimit()     { buildAndTestModelWithCalcAndMinMaxLimits( -0.5, 23.5,  -1., null, -1.   ); }
    public void testSignalWithCalcAboveMinLimit()     { buildAndTestModelWithCalcAndMinMaxLimits(  0.0, 23.5,  -1., null,  0.   ); }
    public void testSignalWithCalcBelowMaxLimit()     { buildAndTestModelWithCalcAndMinMaxLimits(  0.5,  0.1, null,  +1.,  0.05 ); }
    public void testSignalWithCalcAboveMaxLimit()     { buildAndTestModelWithCalcAndMinMaxLimits( +0.5, 23.5, null,  +1., +1.   ); }
    public void testSignalWithCalcBelowMinMaxLimit()  { buildAndTestModelWithCalcAndMinMaxLimits( -0.5, 23.5,  -1.,  +1., -1.   ); }
    public void testSignalWithCalcInsideMinMaxLimit() { buildAndTestModelWithCalcAndMinMaxLimits( -0.5,  0.1,  -1.,  +1., -0.05 ); }
    public void testSignalWithCalcAboveMinMaxLimit()  { buildAndTestModelWithCalcAndMinMaxLimits( +0.5, 23.5,  -1.,  +1., +1.   ); }
/*
 *  These are functionally equivalent to the testSignalWithIC... tests above; not repeated
 *  public void testSignalWithInputBelowMinLimit() { fail("test not yet implemented"); }
 *  public void testSignalWithInputAboveMinLimit() { fail("test not yet implemented"); }
 *  public void testSignalWithInputBelowMaxLimit() { fail("test not yet implemented"); }
 *  public void testSignalWithInputAboveMaxLimit() { fail("test not yet implemented"); }
 *  public void testSignalWithInputBelowMinMaxLimit() { fail("test not yet implemented"); }
 *  public void testSignalWithInputInsideMinMaxLimit() { fail("test not yet implemented"); }
 *  public void testSignalWithInputAboveMinMaxLimit() { fail("test not yet implemented"); }
 */

    public void testSignalElementWithNumericConstant_ctor() {

        // test building an abs value network from the XML snippet
                
        // delete existing Signals and Blocks
        _ds = null;
        _ss = null;
        _fs = null;
        _bc = null;
        _bd = null;
                
        // create a simple calculated vardef to test the Signal methods
        //   <variableDef name="calcVar" varID="dv" units="deg">
        //     <description>
        //       My description
        //     </description>
        //     <calculation>
        //       <math>
        //         <apply><abs/><cn>-6.78</cn></apply>
        //       </math>
        //     </calculation>
        //   </variableDef>

        Element theDesc1 = new Element("description");
        theDesc1.addContent("My description");

        Element theValue = new Element("cn");
        theValue.addContent( "-6.78" );

        Element absElement = new Element("abs");

        Element applyElement = new Element("apply");
        applyElement.addContent( absElement );
        applyElement.addContent( theValue );

        Element mathElement = new Element("math");
        mathElement.addContent( applyElement );

        Element calcElement = new Element("calculation");
        calcElement.addContent( mathElement );

        Element calcVarDef; // varDef with calculation component

        calcVarDef = new Element("variableDef");
        calcVarDef.setAttribute( "name", "calcVar" );
        calcVarDef.setAttribute( "varID", "dv" );
        calcVarDef.setAttribute( "units", "deg" );
        calcVarDef.addContent( theDesc1 );
        calcVarDef.addContent( calcElement );
                
        // use new model empty of blocks and signals
        Model m = new Model(3,3);
                
        Signal theSignal = new Signal(calcVarDef, m); // this creates single upstream BlockMathAbs named "abs_1"
        assertTrue(  theSignal != null );
        assertTrue(  theSignal.hasSource() );
        assertFalse( theSignal.hasDest() );
        assertFalse( theSignal.hasIC() );

        // verify information about newly-created block
        Block abs_1 = theSignal.getSource();
        assertTrue(                  abs_1 != null );
        assertEquals( "abs_1",       abs_1.getName() );
        assertEquals( 1,             abs_1.numInputs() );
        assertEquals( 1,             abs_1.numVarIDs() );
        assertTrue(                  abs_1.allInputsConnected() );
        assertEquals( "const_1",     abs_1.getVarID(1) );
        assertEquals( "dv",          abs_1.getOutputVarID() );
        assertEquals( theSignal,     abs_1.getOutput() );
        assertTrue(                  abs_1.outputConnected() );

        // verify information about the Model
        assertTrue( m != null );
        assertEquals( 2, m.getNumBlocks() );
        assertEquals( 2, m.getNumSignals() );
        assertEquals( 0, m.getNumInputBlocks() );
        assertEquals( 0, m.getNumOutputBlocks() );

        try { m.initialize(); }
        catch (DAVEException e ) {
            fail("Exception thrown while initializing model in testSingleSignal_ctor: " + e.getMessage());
        }
                
        try {
            //      assertTrue( theSignal.sourceReady() );
            assertEquals( 6.78, theSignal.sourceValue(), 0.0000001 );
        } catch (DAVEException e) {
            fail("Exception thrown while getting value of signal source in testSingleElement_ctor: " 
                 + e.getMessage());
        }
        
        m.setCodeDialect(Model.DT_ANSI_C);
        assertTrue( theSignal.genCode().getCode().equals("dv") );

    }

    public void testSignalElementWithBlockConstant_ctor() {

        // test building an abs value network from the XML snippet
                
        // delete existing Signals and Blocks
        _ds = null;
        _ss = null;
        _fs = null;
        _bc = null;
        _bd = null;
                
        // create a simple calculated vardef to test the Signal methods
        // define upstream constant
        //   <variableDef name="beta" varID="beta" units="deg" initialValue="-6.78"/>
        //   <variableDef name="calcVar" varID="dv" units="deg">
        //     <description>
        //       My description
        //     </description>
        //     <calculation>
        //       <math>
        //         <apply><abs/><ci>beta</ci></apply>
        //       </math>
        //     </calculation>
        //   </variableDef>

        Element theDesc1 = new Element("description");
        theDesc1.addContent("My description");

        Element theValue = new Element("ci");
        theValue.addContent( "beta" );

        Element absElement = new Element("abs");

        Element applyElement = new Element("apply");
        applyElement.addContent( absElement );
        applyElement.addContent( theValue );

        Element mathElement = new Element("math");
        mathElement.addContent( applyElement );

        Element calcElement = new Element("calculation");
        calcElement.addContent( mathElement );

        Element calcVarDef; // varDef with calculation component
        Element constVarDef; // varDef for upstream constant

        calcVarDef = new Element("variableDef");
        calcVarDef.setAttribute( "name", "calcVar" );
        calcVarDef.setAttribute( "varID", "dv" );
        calcVarDef.setAttribute( "units", "deg" );
        calcVarDef.addContent( theDesc1 );
        calcVarDef.addContent( calcElement );
                
        // use new model empty of blocks and signals
        Model m = new Model(3,3);
                
        // define the constant value
        constVarDef = new Element( "variableDef" );
        constVarDef.setAttribute(  "name", "beta" );
        constVarDef.setAttribute(  "varID", "beta" );
        constVarDef.setAttribute(  "units", "deg" );
        constVarDef.setAttribute( "initialValue", "-6.78" );
                
        // this wire will lead from a const block that is created by model.hookUpIO()
        Signal beta = new Signal( constVarDef, m);
        assertTrue(  beta != null );
        assertFalse( beta.hasSource() );
        assertFalse( beta.hasDest() );
        assertTrue(  beta.hasIC() );
                
        // this creates single upstream BlockMathAbs named "abs_1"
        Signal dv = new Signal( calcVarDef, m ); 
        assertTrue(  dv != null );
        assertTrue(  dv.hasSource() );
        assertFalse( dv.hasDest() );
        assertFalse( dv.hasIC() );

        // verify information about newly-created block
        Block abs_1 = dv.getSource();
        assertTrue(            abs_1 != null );
        assertEquals( "abs_1", abs_1.getName() );
        assertEquals( 1,       abs_1.numInputs() );
        assertEquals( 1,       abs_1.numVarIDs() );
        assertFalse(           abs_1.allInputsConnected() );
        assertEquals( "beta",  abs_1.getVarID(1) );
        assertEquals( "dv",    abs_1.getOutputVarID() );
        assertEquals( dv,      abs_1.getOutput() );
        assertTrue(            abs_1.outputConnected() );
                
        // verify information about the Model
        assertTrue( m != null );
        assertEquals( 1, m.getNumBlocks() );
        assertEquals( 2, m.getNumSignals() );
        assertEquals( 0, m.getNumInputBlocks() );
        assertEquals( 0, m.getNumOutputBlocks() );

        // at this point, Model "m" has one block (BlockMathAbs "abs_1") and two Signals (dv and beta)
        // Signal "dv" is connected to output of BlockMathAbs "abs_1" but has no output
        // Signal "beta" is not connected to anything (input or output block) but has an IC value
        // Block "abs_1" is connected to output Signal "dv" but is not yet connected to input Signal "beta"
        //     but "abs_1" knows the varID of its single input is "beta"
        //
        //                    +------------------+  
        //       beta         |                  |   dv
        //     --------       | beta  abs_1   dv +--------
        //      -6.78         |                  |
        //                    +------------------+
        //
                                
        m.wireBlocks();  // this should connect beta as input to BMA
        //
        //                    +------------------+  
        //       beta         |                  |   dv
        //     -------------->| beta  abs_1   dv +--------
        //      -6.78         |                  |
        //                    +------------------+
        //
                
        // check input signal attributes for changes
        assertTrue(  beta != null );
        assertFalse( beta.hasSource() );
        assertTrue(  beta.hasDest() );    // this should have changed
        assertTrue(  beta.hasIC() );
                
        // check output signal attributes for changes
        assertTrue(  dv != null );
        assertTrue(  dv.hasSource() );
        assertFalse( dv.hasDest() );
        assertFalse( dv.hasIC() );
                
        // check BMA for changes
        assertTrue(            abs_1 != null );
        assertEquals( "abs_1", abs_1.getName() );
        assertEquals( 1,       abs_1.numInputs() );
        assertEquals( 1,       abs_1.numVarIDs() );
        assertTrue(            abs_1.allInputsConnected() );  // this should have changed
        assertEquals( "beta",  abs_1.getVarID(1) );
        assertEquals( "dv",    abs_1.getOutputVarID() );
        assertEquals( dv,      abs_1.getOutput() );
        assertTrue(            abs_1.outputConnected() );

        // check Model for changes
        assertTrue( m != null );
        assertEquals( 1, m.getNumBlocks() );
        assertEquals( 2, m.getNumSignals() );
        assertEquals( 0, m.getNumInputBlocks() );
        assertEquals( 0, m.getNumOutputBlocks() );

        // now create new BlockMathConstant "const_-6.78_" and hook it to "beta" signal
        // this step also creates an output port for dv to hook to
        m.hookUpIO();
                
        //
        //   +--------------+       +------------------+      +---------+
        //   |              | beta  |                  |  dv  |         |
        //   |  const  beta +------>| beta  abs_1   dv +----->|  output |
        //   |  -6.78       | -6.78 |                  |      |         |
        //   +--------------+       +------------------+      +---------+
        //
                
        // check input signal attributes for changes
        assertTrue(  beta != null );
        assertTrue(  beta.hasSource() );   // this should have changed
        assertTrue(  beta.hasDest() );
        assertTrue(  beta.hasIC() );
                
        // check output signal attributes for changes
        assertTrue(  dv != null );
        assertTrue(  dv.hasSource() );
        assertTrue(  dv.hasDest() );      // this should have changed
        assertFalse( dv.hasIC() );
                
        // check BMA for changes
        assertTrue(            abs_1 != null );
        assertEquals( "abs_1", abs_1.getName() );
        assertEquals( 1,       abs_1.numInputs() );
        assertEquals( 1,       abs_1.numVarIDs() );
        assertTrue(            abs_1.allInputsConnected() );
        assertEquals( "beta",  abs_1.getVarID(1) );
        assertEquals( "dv",    abs_1.getOutputVarID() );
        assertEquals( dv,      abs_1.getOutput() );
        assertTrue(            abs_1.outputConnected() );

        // check Model for changes
        assertTrue( m != null );
        assertEquals( 3, m.getNumBlocks() );       // this should have changed
        assertEquals( 2, m.getNumSignals() );
        assertEquals( 0, m.getNumInputBlocks() );
        assertEquals( 1, m.getNumOutputBlocks() ); // this should have changed
                
        try { m.initialize(); }
        catch (DAVEException e ) {
            fail("Exception thrown while initializing model in testSingleSignal_ctor: " + e.getMessage());
        }
                
        try {
            assertTrue( dv.sourceReady() );
            assertEquals( 6.78, dv.sourceValue(), 0.0000001 );
        } catch (DAVEException e) {
            fail("Exception thrown while getting value of signal source in testSingleElement_ctor: " 
                 + e.getMessage());
        }
        
        m.setCodeDialect(Model.DT_ANSI_C);
        assertTrue( beta.genCode().getCode().equals("beta") );
        assertTrue( dv.genCode().getCode().equals("dv") );
    }

    public void testSignalElementWithCalculation() {
        buildAndTestModelWithCalcAndMinMaxLimits( 0.5, 23.5, null, null, 0.5*23.5 );
    }


    // useful common code

    private void  buildAndTestModelWithCalcAndMinMaxLimits(
            Double a, Double b, Double min, Double max, Double expectedValue ) {

                // test building a calculated value from an XML snippet
        Model m = buildModelWithCalculationATimeBAndMinMaxLimits( a, b, min, max);

        try { m.initialize(); }
        catch (DAVEException e ) {
            fail("Exception thrown while initializing model in buildAndTestModelWithCalcAndMinMaxLimits: "
                    + e.getMessage());
        }

        SignalArrayList sal = m.getSignals();
        Signal dv = sal.findByID("dv");

        assertNotNull(dv);

        // look at source of signal; this is the same as the model's output value
        try {
            assertTrue( dv.sourceReady() );
            assertEquals( expectedValue, dv.sourceValue(), 0.0000001 );
        } catch (DAVEException e) {
            fail("Exception thrown while getting value of signal source in buildAndTestModelWithCalcAndMinMaxLimits: "
                 + e.getMessage());
        }

    }

    private Model buildModelWithCalculationATimeBAndMinMaxLimits(
            Double a, Double b, Double min, Double max) {

        boolean calcIsLimited = false;

        // create a simple calculated vardef to test the Signal methods
        // define upstream variables

        //   <variableDef name="a" varID="A" units="r_s" initialValue={a}/>
        //   <variableDef name="b" varID="B" units="ft"  initialValue={b}/>

        // define signal of interest

        //   <variableDef name="calcVar" varID="dv" units="ft_s"
        //                           [minValue={min}] [maxValue={max}] >
        //     <description>
        //       My description
        //     </description>
        //     <calculation>
        //       <math>
        //         <apply>
        //           <times/>
        //           <ci>A</ci>
        //           <ci>B</ci>
        //         </apply>             <!-- (A*B) -->
        //       </math>
        //     </calculation>
        //   </variableDef>

        // build up XML equivalent JDOM tree

        Element theDesc1 = new Element("description");
        theDesc1.addContent("My description");

        Element firstArg = new Element("ci");
        firstArg.addContent( "A" );

        Element secondArg = new Element("ci");
        secondArg.addContent( "B" );

        Element timesElement = new Element("times");

        Element applyElement = new Element("apply");
        applyElement.addContent( timesElement );
        applyElement.addContent( firstArg );
        applyElement.addContent( secondArg );

        Element mathElement = new Element("math");
        mathElement.addContent( applyElement );

        Element calcElement = new Element("calculation");
        calcElement.addContent( mathElement );

        Element calcVarDef; // varDef with calculation component
        Element arg1VarDef; // varDef for upstream constant 1
        Element arg2VarDef; // varDef for upstream constant 2

        calcVarDef = new Element("variableDef");
        calcVarDef.setAttribute( "name", "calcVar" );
        calcVarDef.setAttribute( "varID", "dv" );
        calcVarDef.setAttribute( "units", "deg" );
        calcVarDef.addContent( theDesc1 );
        calcVarDef.addContent( calcElement );
        if (min != null) {
            calcVarDef.setAttribute( "minValue", min.toString() );
            calcIsLimited = true;
        }
        if (max != null) {
            calcVarDef.setAttribute( "maxValue", max.toString() );
            calcIsLimited = true;
        }

        // use new model empty of blocks and signals
        Model m = new Model(3,3);

        // define the first constant value
        arg1VarDef = new Element( "variableDef" );
        arg1VarDef.setAttribute(  "name", "a" );
        arg1VarDef.setAttribute(  "varID", "A" );
        arg1VarDef.setAttribute(  "units", "r_s" );
        arg1VarDef.setAttribute(  "initialValue", a.toString() );

        // this wire will lead from a const block that is created by model.hookUpIO()
        Signal aSig = new Signal( arg1VarDef, m);
        assertTrue(  aSig != null );
        assertFalse( aSig.hasSource() );
        assertFalse( aSig.hasDest() );
        assertTrue(  aSig.hasIC() );

        // define the second constant value
        arg2VarDef = new Element( "variableDef" );
        arg2VarDef.setAttribute(  "name", "b" );
        arg2VarDef.setAttribute(  "varID", "B" );
        arg2VarDef.setAttribute(  "units", "ft" );
        arg2VarDef.setAttribute(  "initialValue", b.toString() );

        // this wire will lead from a const block that is created by model.hookUpIO()
        Signal bSig = new Signal( arg2VarDef, m);
        assertTrue(  bSig != null );
        assertFalse( bSig.hasSource() );
        assertFalse( bSig.hasDest() );
        assertTrue(  bSig.hasIC() );

        // this creates single upstream BlockMathProduct named "times_1"
        Signal dv = new Signal( calcVarDef, m );
        assertTrue(  dv != null );
        assertTrue(  dv.hasSource() );
        assertFalse( dv.hasDest() );
        assertFalse( dv.hasIC() );

        // verify information about newly-created block
        Block times_1 = dv.getSource();
        assertTrue(              times_1 != null );
        assertEquals( "times_1", times_1.getName() );
        assertEquals( 2,         times_1.numInputs() );
        assertEquals( 2,         times_1.numVarIDs() );
        assertFalse(             times_1.allInputsConnected() );
        assertEquals( "A",       times_1.getVarID(1) );
        assertEquals( "B",       times_1.getVarID(2) );
        assertEquals( "dv",      times_1.getOutputVarID() );
        assertEquals( dv,        times_1.getOutput() );
        assertTrue(              times_1.outputConnected() );

        // verify information about the Model
        assertTrue( m != null );
        assertEquals( 1, m.getNumBlocks() );
        assertEquals( 3, m.getNumSignals() );
        assertEquals( 0, m.getNumInputBlocks() );
        assertEquals( 0, m.getNumOutputBlocks() );

        // at this point, Model "m" has one block (BlockMathTimes "times_1") and three Signals (dv, A and B)
        // Signal "dv" is connected to output of BlockMathTimes "times_1" but has no output
        // Neither signal "A" nor "B" is not connected to anything but both have IC values
        // Block "times_1" is connected to output Signal "dv" but is not yet connected to
        //     either input Signal, but it knows the varID of its two inputs
        //
        //         A
        //     ---------
        //        0.5         +-----------------+
        //                    | A               |   dv
        //                    |     times_1  dv +--------
        //                    | B               |
        //         B          +-----------------+
        //     ---------
        //       23.50
        //

        m.wireBlocks();  // this should connect inputs to block dv
        //
        //         A
        //     -----------+
        //        0.5     |   +-----------------+
        //                +-->| A               |   dv
        //                    |     times_1  dv +--------
        //                +-->| B               |
        //         B      |   +-----------------+
        //     -----------+
        //       23.50
        //

        // check input signals attributes for changes
        assertTrue(  aSig != null );
        assertFalse( aSig.hasSource() );
        assertTrue(  aSig.hasDest() );   // this should have changed
        assertTrue(  aSig.hasIC() );

        assertTrue(  bSig != null );
        assertFalse( bSig.hasSource() );
        assertTrue(  bSig.hasDest() );   // this should have changed
        assertTrue(  bSig.hasIC() );

        // check output signal attributes for changes
        assertTrue(  dv != null );
        assertTrue(  dv.hasSource() );
        assertFalse( dv.hasDest() );
        assertFalse( dv.hasIC() );

        // check calculation block for changes
        assertTrue(              times_1 != null );
        assertEquals( "times_1", times_1.getName() );
        assertEquals( 2,         times_1.numInputs() );
        assertEquals( 2,         times_1.numVarIDs() );
        assertTrue(              times_1.allInputsConnected() );  // this should have changed
        assertEquals( "A",       times_1.getVarID(1) );
        assertEquals( "B",       times_1.getVarID(2) );
        assertEquals( "dv",      times_1.getOutputVarID() );
        assertEquals( dv,        times_1.getOutput() );
        assertTrue(              times_1.outputConnected() );

        // check Model for changes
        assertTrue( m != null );
        assertEquals( 1, m.getNumBlocks() );
        assertEquals( 3, m.getNumSignals() );
        assertEquals( 0, m.getNumInputBlocks() );
        assertEquals( 0, m.getNumOutputBlocks() );

        // now create two new BlockMathConstant blocks and hook them to the
        //     A and B signals
        // this step also creates an output port for dv to hook to
        // if a limiter is defined for calcVar, a limiter will be inserted
        m.hookUpIO();

        //   Without limiter:
        //
        //   +----------+
        //   |          | A
        //   | const  A +-------+
        //   |   0.5    | 0.5   |   +----------------+     +--------+
        //   +----------+       +-->| A              | dv  |        |
        //                          |   times_1  dv  +---->| output |
        //   +----------+       +-->| B              |     |        |
        //   |          | B     |   +----------------+     +--------+
        //   | const  B +-------+
        //   | 23.50    | 23.50
        //   +----------+
        //

        //   With limiter:
        //
        //   +----------+
        //   |          | A
        //   | const  A +-------+
        //   |   0.5    | 0.5   |   +----------------+ dv_   +---------+     +--------+
        //   +----------+       +-->| A              | unlim |         | dv  |        |
        //                          |   times_1 dv_  +------>| limiter +---->| output |
        //   +----------+       +-->| B        unlim |       |         |     |        |
        //   |          | B     |   +----------------+       +---------+     +--------+
        //   | const  B +-------+
        //   | 23.50    | 23.50
        //   +----------+
        //

        // check input signal attributes for changes
        // check input signals attributes for changes
        assertTrue(  aSig != null );
        assertTrue(  aSig.hasSource() );   // this should have changed
        assertTrue(  aSig.hasDest() );
        assertTrue(  aSig.hasIC() );

        assertTrue(  bSig != null );
        assertTrue(  bSig.hasSource() );     // this should have changed
        assertTrue(  bSig.hasDest() );
        assertTrue(  bSig.hasIC() );

        // check output signal attributes for changes
        assertTrue(  dv != null );
        assertTrue(  dv.hasSource() );
        assertTrue(  dv.hasDest() );      // this should have changed
        assertFalse( dv.hasIC() );

        // check calculation block for changes
        assertTrue(              times_1 != null );
        assertEquals( "times_1", times_1.getName() );
        assertEquals( 2,         times_1.numInputs() );
        assertEquals( 2,         times_1.numVarIDs() );
        assertTrue(              times_1.allInputsConnected() );  // this should have changed
        assertEquals( "A",       times_1.getVarID(1) );
        assertEquals( "B",       times_1.getVarID(2) );
        if ( calcIsLimited ) {
            assertEquals( "dv_unlim",times_1.getOutputVarID() );
        } else {
            assertEquals( "dv",      times_1.getOutputVarID() );
            assertEquals( dv,        times_1.getOutput() );
        }
        assertTrue(              times_1.outputConnected() );

        // check Model for changes
        assertTrue( m != null );
        if ( calcIsLimited ) {
            // limiter block and output signal have been inserted
            assertEquals( 5, m.getNumBlocks() );       // this should have changed
            assertEquals( 4, m.getNumSignals() );
        } else {
            assertEquals( 4, m.getNumBlocks() );       // this should have changed
            assertEquals( 3, m.getNumSignals() );
        }
        assertEquals( 0, m.getNumInputBlocks() );
        assertEquals( 1, m.getNumOutputBlocks() ); // this should have changed

        return m;
    }

    private void buildAndTestSignalWithIcPlusLimit(
            Double ic, Double min, Double max,
            Double expectedValue ) {

        _m = new Model(3,3); // create a fresh, empty model
        VectorInfoArrayList outputs = null;
        VectorInfoArrayList inputs = null;
        String icString = null;
        String minString = null;
        String maxString = null;
        if (ic != null)
            icString = ic.toString();
        if (min != null)
            minString = min.toString();
        if (max != null)
            maxString = max.toString();
        setFullSignalWithICMinMax(icString, minString, maxString);
        // the original signal now has a limiter block downstream of it
        BlockArrayList dests = _fs.getDests();
        assertTrue( dests != null );
        assertEquals( 1, dests.size() );
        Block blk = dests.get(0);
        assertTrue( blk != null );
        assertTrue( blk instanceof BlockLimiter );
        BlockLimiter limBlk = (BlockLimiter) blk;
        assertTrue( (min == null) ^ limBlk.hasLowerLimit() );
        assertTrue( (max == null) ^ limBlk.hasUpperLimit() );
        if (min != null)
            assertEquals(min, limBlk.getLowerLimit(), eps);
        if (max != null)
            assertEquals(max, limBlk.getUpperLimit(), eps);

        try {
            outputs = _m.getOutputVector();
            inputs  = _m.getInputVector();
        } catch (DAVEException e) {
            fail("Exception thrown while trying to get I/O vectors from model:"
                        + e.getMessage() );
        }
        assertEquals(1, outputs.size());
        assertEquals(0, inputs.size());
        VectorInfo output = outputs.get(0);
        try {

            _m.cycle();
        } catch (DAVEException e) {
            fail("Exception thrown while trying to get output vector from model:"
                        + e.getMessage() );
        }
        assertEquals( expectedValue, output.getValue(), eps );
    }


    private void setFullSignalWithFlags( String flagValue1, String flagValue2 ) {

        // delete existing Full Signal
        _fs = null;

        // create a simple vardef to see if flag is correctly set
        //   <variableDef name="theVar" varID="dv" units="deg">
        //     <description>
        //       My description
        //     </description>
        //     <SOMEFLAG/>
        //   </variableDef>

        Element theDesc1 = new Element("description");
        theDesc1.addContent("My description");
        Element calcVarDef; // varDef with calculation component
        Element flag1 = new Element(flagValue1);
        Element flag2 = null;
        if (flagValue2 != null)
            flag2 = new Element(flagValue2);

        calcVarDef = new Element("variableDef");
        calcVarDef.setAttribute( "name", "theVar" );
        calcVarDef.setAttribute( "varID", "dv" );
        calcVarDef.setAttribute( "units", "deg" );
        calcVarDef.addContent( theDesc1 );
        calcVarDef.addContent( flag1 );
        if (flagValue2 != null)
            calcVarDef.addContent( flag2 );

        _fs = new Signal(calcVarDef, _m); // this creates single upstream BlockMathAbs named "abs_1"

        // test all but flag values
        assertEquals( "theVar", _fs.getName()  );
        assertEquals( "dv",     _fs.getVarID() );
        assertEquals( "deg",    _fs.getUnits() );
        assertTrue(  _fs.getSource() == null );
        assertTrue(  _fs.getSourcePort() == 0 );
        assertFalse( _fs.hasSource() );
        assertFalse( _fs.hasDest() );
        assertFalse( _fs.isDerived() );
    }

    private void setFullSignalWithICMinMax( String ICValue, String minValue, String maxValue ) {

        // delete existing Full Signal
        _fs = null;

        // create a simple vardef to see if IC and limits are correctly set
        //   <variableDef name="theVar" varID="dv" units="deg" [IC=#{ICValue}]
        //                [minValue=#{minValue} [maxValue=#{maxValue}]>
        //     <description>
        //       My description
        //     </description>
        //   </variableDef>

        Element theDesc1 = new Element("description");
        theDesc1.addContent("My description");
        Element varDef; // varDef with calculation component

        varDef = new Element("variableDef");
        varDef.setAttribute( "name", "theVar" );
        varDef.setAttribute( "varID", "dv" );
        varDef.setAttribute( "units", "deg" );
        if(ICValue != null)
            varDef.setAttribute("initialValue", ICValue );
        if(minValue != null)
            varDef.setAttribute("minValue", minValue);
        if(maxValue != null)
            varDef.setAttribute("maxValue", maxValue);
        varDef.addContent( theDesc1 );
        varDef.addContent( new Element("isOutput") );

        // this will create a const block and an output block after calling model.hookUpIO()
        _fs = new Signal(varDef, _m); // this creates single upstream BlockMathAbs named "abs_1"

        // test all but flag values
        assertEquals( "theVar", _fs.getName()  );
        assertEquals( "dv",     _fs.getVarID() );
        assertEquals( "deg",    _fs.getUnits() );
        assertTrue(  _fs.getSource() == null );
        assertTrue(  _fs.getSourcePort() == 0 );
        assertFalse( _fs.hasSource() );
        assertFalse( _fs.hasDest() );
        assertFalse( _fs.isDerived() );

        _m.hookUpIO(); // creates & connects a constant block to an output block
            try { _m.initialize(); }
        catch (DAVEException e ) {
            fail("Exception thrown while initializing model: " + e.getMessage());
        }
    }

    public static Test suite() {
        return new TestSuite( SignalTest.class );
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }


}
