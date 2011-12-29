package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import org.jdom.Element;

import junit.framework.TestCase;

public class BlockMathMinusTest extends TestCase {

    protected Model _model;
    protected Signal _minusAlpha;
    protected Signal _alphaMinusBeta;
    protected Signal _alphaSignal;
    protected Signal _betaSignal;
    protected BlockMathMinus _unaryMinusBlock;
    protected BlockMathMinus _binaryMinusBlock;
    protected BlockMathConstant _constantBetaBlock;
    protected BlockMathConstant _constantAlphaBlock;
    
    @Override
    protected void setUp() {

        // don't need input signal - can create const block and signal in one step later
        _model   = new Model(3,3);

        // build unary minus signal
        // String signalName, String varID, String units, int numConnects, Model m
        _minusAlpha = new Signal( "minusAlpha", "minusAlpha", "deg", 4, _model );

        // build an minus value apply element
        //   <apply>
        //     <minus/>
        //     <ci>alpha</ci>
        //   </apply>

        Element alphaEl = new Element("ci");   // add numeric constant
        alphaEl.addContent( "alpha" );

        Element applyElement = new Element("apply");
        applyElement.addContent( new Element("minus") );
        applyElement.addContent( alphaEl );

        // create unary minus block
        _unaryMinusBlock = new BlockMathMinus( applyElement, _model );

        // add constant-block-fed input signal
        _unaryMinusBlock.addConstInput("-3.45", 1);

        // find and record location of newly-created constant block
        _alphaSignal = _unaryMinusBlock.getInput(0);
        _constantAlphaBlock = (BlockMathConstant) _alphaSignal.getSource();

        // add output signal
        try {
            _unaryMinusBlock.addOutput(_minusAlpha);
        } catch (DAVEException e1) {
            fail("problem adding unary output signal to block in TestBlockMathMinus: "
                    + e1.getMessage());
        }


        // create binary minus signal
        // String signalName, String varID, String units, int numConnects, Model m
        _alphaMinusBeta = new Signal( "alphaMinusBeta", "alphaMinusBeta", "deg", 4, _model );

        // build binary minus signal: alpha - beta
        //   <apply>
        //     <minus/>
        //     <ci>alpha</ci>
        //     <ci>beta</ci>
        //   </apply>

        // add second argument to existing apply element

        Element betaEl = new Element("ci");
        betaEl.addContent( "beta" );

        applyElement.addContent( betaEl );

        // create binary minus block
        _binaryMinusBlock = new BlockMathMinus( applyElement, _model );

        // connect to alpha signal - by convention, the call is to the upstream entity (the signal)
        // so the signal will notify the block of their connection
        _alphaSignal.addSink(_binaryMinusBlock, 1);


        // add constant-block-fed input signal
        _binaryMinusBlock.addConstInput("+3.45", 2); // this is constant value for beta

        // find and record location of newly-created constant block
        _betaSignal = _binaryMinusBlock.getInput(1);
        _constantBetaBlock = (BlockMathConstant) _betaSignal.getSource();

        // add output signal
        try {
            _binaryMinusBlock.addOutput(_alphaMinusBeta);
        } catch (DAVEException e2) {
            fail("problem adding binary output signal to block in TestBlockMathMinus: "
                    + e2.getMessage());
        }

        // probably redundant
        _model.wireBlocks();

        try {
            _model.initialize();
        } catch (DAVEException e3) {
            fail("problem initializing model with BlockMathMinus in TestBlockMathMinus: "
                    + e3.getMessage() );
        }
    }

    public void testGenCcode() {
        String result1 = _unaryMinusBlock.genCcode();
        assertEquals("  minusAlpha = -(-3.45);\n", result1);
        String result2 = _binaryMinusBlock.genCcode();
        assertEquals("  alphaMinusBeta = (-3.45) - (+3.45);\n", result2);
    }

    public void testGenFcode() {
        String result1 = _unaryMinusBlock.genFcode();
        assertEquals("       minusAlpha = -(-3.45)\n", result1);
        String result2 = _binaryMinusBlock.genFcode();
        assertEquals("       alphaMinusBeta = (-3.45) - (+3.45)\n", result2);
    }

    public void testDescribeSelf() {

        StringWriter writer  = new StringWriter();

        try {
            _unaryMinusBlock.describeSelf(writer);
        } catch (IOException e1) {
            fail("Unexpected exception in BlockMathMinusTest::testDescribeSelf with unaryMinusBlock: "
                    + e1.getMessage() );
        }
        assertEquals( "Block \"minus_1\" has one input (const_-3.45_)," +
        		" one output (minusAlpha), value [3.45] and is a Minus block.",
                writer.toString() );

        // clear old results
        writer = new StringWriter();

        try {
            _binaryMinusBlock.describeSelf(writer);
        } catch (IOException e2) {
            fail("Unexpected exception in BlockMathMinusTest::testDescribeSelf with binaryMinusBlock: "
                    + e2.getMessage() );
        }
        assertEquals( "Block \"minus_3\" has two inputs (const_-3.45_, const_+3.45_)," +
        		" one output (alphaMinusBeta), value [-6.9] and is a Minus block.",
                writer.toString() );
    }

    public void testGetValue() {
        assertEquals( 3.45,  _unaryMinusBlock.getValue(), 0.000001 );
        assertEquals(-6.90, _binaryMinusBlock.getValue(), 0.000001 );
    }

    public void testIsReady() {
        assertTrue( _unaryMinusBlock.isReady() );
        assertTrue(_binaryMinusBlock.isReady() );
    }

    public void testAllInputsReady() {
        assertTrue( _unaryMinusBlock.allInputsReady() );
        assertTrue(_binaryMinusBlock.allInputsReady() );
    }

    public void testMakeVerbose() {
        assertFalse( _unaryMinusBlock.isVerbose() );
        _unaryMinusBlock.makeVerbose();
        assertTrue( _unaryMinusBlock.isVerbose() );
        _unaryMinusBlock.silence();
        assertFalse( _unaryMinusBlock.isVerbose() );
    }

    public void testGetModels() {
        assertEquals( _model.getName(),  _unaryMinusBlock.getModel().getName() );
        assertEquals( _model.getName(), _binaryMinusBlock.getModel().getName() );
    }

    public void testGetSetName() {
        assertEquals( "minus_1", _unaryMinusBlock.getName() );
        _unaryMinusBlock.setName("fart");
        assertEquals( "fart", _unaryMinusBlock.getName() );
    }

    public void testGetType() {
        assertEquals( "minus",  _unaryMinusBlock.getType() );
        assertEquals( "minus", _binaryMinusBlock.getType() );
    }

    public void testGetVarID() {
        assertEquals("alpha",  _unaryMinusBlock.getVarID(1));
        assertEquals("alpha", _binaryMinusBlock.getVarID(1));
        assertEquals("beta", _binaryMinusBlock.getVarID(2));
    }

    public void testGetVarIDIterator() {
        Iterator<String> it;
        String s;

        it = _unaryMinusBlock.getVarIDIterator();
        assertTrue(it.hasNext());
        s = it.next();
        assertEquals( "alpha", s);
        assertFalse(it.hasNext());

        it = _binaryMinusBlock.getVarIDIterator();
        assertTrue(it.hasNext());
        s = it.next();
        assertEquals( "alpha", s);
        assertTrue(it.hasNext() );
        s = it.next();
        assertEquals( "beta", s);
        assertFalse(it.hasNext());
}

    public void testGetOutput() {
        Signal s;

        s = _unaryMinusBlock.getOutput();
        assertEquals( _minusAlpha, s );

        s = _binaryMinusBlock.getOutput();
        assertEquals( _alphaMinusBeta, s );
    }

    public void getGetInputIterator() {
        Iterator<Signal> it;
        Signal s;

        it = _unaryMinusBlock.getInputIterator();
        assertTrue( it.hasNext() );
        s = it.next();
        assertEquals( _alphaSignal, s );
        assertFalse( it.hasNext() );

        it = _binaryMinusBlock.getInputIterator();
        assertTrue(it.hasNext());
        s = it.next();
        assertEquals( _alphaSignal, s );
        assertTrue( it.hasNext() );
        s = it.next();
        assertEquals( _betaSignal, s );
        assertFalse( it.hasNext() );
    }

    public void testGetOutputVarID() {
        assertEquals( "minusAlpha", _unaryMinusBlock.getOutputVarID() );
        assertEquals( "alphaMinusBeta", _binaryMinusBlock.getOutputVarID() );
    }

    public void testNumInputs() {
        assertEquals(1, _unaryMinusBlock.numInputs() );
        assertEquals(2, _binaryMinusBlock.numInputs() );
    }

    public void testNumVarIDs() {
        assertEquals(1, _unaryMinusBlock.numVarIDs() );
        assertEquals(2, _binaryMinusBlock.numVarIDs() );
    }

}
