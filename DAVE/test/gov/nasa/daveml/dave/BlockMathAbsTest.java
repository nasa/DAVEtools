package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;

import junit.framework.TestCase;

public class BlockMathAbsTest extends TestCase {

    protected Model _model;
    protected BlockMathAbs _block;
    private StringWriter _writer;
    protected Signal _sig, _outSig;
    protected BlockMathConstant _const;

    @Override
    protected void setUp() {

        // don't need input signal - can create const block and signal in one step later
        _model = new Model(3, 3);
        _writer = new StringWriter();
        

        // build an absolute value apply element
        //   <apply>
        //     <abs/>
        //     <ci>beta</ci>
        //   </apply>

        Element ciElement = new Element("ci");	// add numeric constant
        ciElement.addContent("beta");

        Element absElement = new Element("abs");

        Element applyElement = new Element("apply");
        applyElement.addContent(absElement);
        applyElement.addContent(ciElement);

        // create abs block
        _block = new BlockMathAbs(applyElement, _model);

        // create upstream input (const block and signal)
        _const = new BlockMathConstant("-3.45", _model);
        Signal sig = new Signal("beta", _model);
        
        // create output block
        _outSig = new Signal("absBeta", _model);


        // wire into block under test
        _block.addInput(sig, 1);
        try {
            _block.addOutput(_outSig);
        } catch (DAVEException e1) {
            fail("Exception when adding output signal in TestBlockMathAbs.setUp(): " + 
                    e1.getMessage());
        }
        try {
            _const.addOutput(sig);
        } catch (DAVEException e2) {
            fail("Exception when hooking up constant block in TestBlockMathAbs.setUp(): " + 
                    e2.getMessage());
        }
    }

    public void testBlockMathAbsCtor() {
        Model m = new Model(3, 3);

        // create an abs block from following snippet
        //      <apply><abs/><ci>fart</ci></apply>

        Element theValue = new Element("ci");
        theValue.addContent("fart");

        Element absElement = new Element("abs");

        Element applyElement = new Element("apply");
        applyElement.addContent(absElement);
        applyElement.addContent(theValue);

        Block theBlock = new BlockMathAbs(applyElement, m);

        assertTrue(theBlock != null);
        assertEquals("abs_1", theBlock.myName);
        assertEquals("abs_1", theBlock.getName());
        assertEquals("absolute value", theBlock.myType);
        assertEquals("absolute value", theBlock.getType());
        assertEquals(1, theBlock.numInputs());
        assertEquals(1, theBlock.numVarIDs());
        assertEquals("fart", theBlock.getVarID(1));
        assertFalse(theBlock.isReady());
    }

    public void testUpdate() {
        _const.setValue(4.56);
        try {
            _model.getInputVector();
        } catch (DAVEException e1) {
            fail("error when trying to obtain VectorInfoArrayList in TestBlockMathAbs::testUpdate(): "
                    + e1.getMessage());
        }
        try {
            _block.update();
        } catch (DAVEException e) {
            // TODO Auto-generated catch block
            fail("error when trying to update testing block in TestBlockMathAbs::testUpdate(): "
                    + e.getMessage());
        }
        assertEquals(4.56, _block.getValue(), 0.000001);
    }

    public void testGenCcode() {
        _model.setCodeDialect(Model.DT_ANSI_C);
        CodeAndVarNames result = _block.genCode();
        assertEquals("  absBeta = fabs( beta );\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("absBeta", result.getVarName(0));
        assertEquals("beta", result.getVarName(1));
    }

    public void testGenFcode() {
        _model.setCodeDialect(Model.DT_FORTRAN);
        CodeAndVarNames result = _block.genCode();
        assertEquals("       absBeta = ABS( beta )\n", result.getCode());
        assertEquals(2, result.getVarNames().size());
        assertEquals("absBeta", result.getVarName(0));
        assertEquals("beta", result.getVarName(1));
    }

    public void testDescribeSelf() {
        try {
            _block.describeSelf(_writer);
        } catch (IOException e) {
            assertTrue(false);
            e.printStackTrace();
        }
        assertEquals("Block \"abs_1\" has one input (beta), one output (absBeta),"
                + " value [NaN] and is an Absolute Value math block.",
                _writer.toString());
    }

    public void testGetValue() {
        try {
            _block.update();
        } catch (DAVEException e) {
            fail("error in testGetValue unit test of TestBlockMathAbs: " + e.getMessage());
        }
        assertEquals(3.45, _block.getValue(), 0.000001);
    }

    public void testIsReady() {
        assertFalse(_block.isReady());
        try {
            _block.update();
        } catch (DAVEException e) {
            fail("error in testIsReady unit test of TestBlockMathAbs: " + e.getMessage());
        }
        assertTrue(_block.isReady());
    }

    public void testAllInputsReady() {
        assertTrue(_block.allInputsReady());
    }

    public void testMakeVerbose() {
        assertFalse(_block.isVerbose());
        _block.makeVerbose();
        assertTrue(_block.isVerbose());
        _block.silence();
        assertFalse(_block.isVerbose());
    }

    public void testGetModel() {
        assertEquals(_model.getName(), _block.getModel().getName());
    }

    public void testGetSetName() {
        assertEquals("abs_1", _block.getName());
        _block.setName("fart");
        assertEquals("fart", _block.getName());
    }

    public void testGetType() {
        assertEquals("absolute value", _block.getType());
    }

    public void testGetVarID() {
        assertEquals("beta", _block.getVarID(1));
    }

    public void testGetVarIDIterator() {
        Iterator<String> it = _block.getVarIDIterator();
        assertTrue(it.hasNext());
        String s = it.next();
        assertEquals("beta", s);
        assertFalse(it.hasNext());
    }

    public void testGetOutput() {
        Signal s = _block.getOutput();
        assertEquals(_outSig, s);
    }

    public void getGetInputIterator() {
        Iterator<Signal> it = _block.getInputIterator();
        assertTrue(it.hasNext());
        Signal s = it.next();
        assertEquals(_sig, s);
        assertFalse(it.hasNext());
    }

    public void testGetOutputVarID() {
        assertEquals("absBeta", _block.getOutputVarID());
    }

    public void testNumInputs() {
        assertEquals(1, _block.numInputs());
    }

    public void testNumVarIDs() {
        assertEquals(1, _block.numVarIDs());
    }
}
