package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom.Element;

import junit.framework.TestCase;

public class BlockMathRelationTest extends TestCase {

    protected Model _model;
    protected Signal _outputSignal;
    protected Signal _value1Signal;
    protected Signal _value2Signal;
    protected String _value1SignalID;
    protected String _value2SignalID;
    protected BlockMathConstant _value1Block;
    protected BlockMathConstant _value2Block;
    protected BlockMathRelation _block;
    private StringWriter _writer;
    protected String routineName = "TestBlockMathRelation";
    private final double EPS = 0.000001;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String theRoutineName = "TestBlockMathProduct::setUp()";

        // don't need input signal - can create const block and signal in one step later
        _model = new Model(3, 3);
        _writer = new StringWriter();

        // build a relation calculation
        //      <apply>
        //        <lt/>
        //        <ci>ALP_UNLIM</ci>
        //        <ci>ALP_LIMIT</ci>
        //      </apply>


        // first, build the upstream constant blocks and signals
        _value1Block = new BlockMathConstant("-3.5", _model);
        _value2Block = new BlockMathConstant(" 2.0", _model);
        _value1SignalID = "ALP_UNLIM";
        _value2SignalID = "ALP_LIMIT";
        _value1Signal = new Signal("unlimited alpha", _value1SignalID, "d_s", 1, _model);
        _value2Signal = new Signal("alpha limit", _value2SignalID, "ft", 1, _model);
        _value1Block.addOutput(_value1Signal);
        _value2Block.addOutput(_value2Signal);

        // create downstream signal
        _outputSignal = new Signal("outputSignal", _model);

        // build JDOM from XML snippet
        Element theValue1 = new Element("ci");	// add numeric constant
        theValue1.addContent("ALP_UNLIM");

        Element theValue2 = new Element("ci");
        theValue2.addContent("ALP_LIMIT");

        Element theTimesElement = new Element("lt");

        Element applyElement = new Element("apply");
        applyElement.addContent(theTimesElement);
        applyElement.addContent(theValue1);
        applyElement.addContent(theValue2);

        // create product block
        _block = new BlockMathRelation(applyElement, _model);

        // hook up inputs to block
        _block.addInput(_value1Signal, 1);
        _block.addInput(_value2Signal, 2);

        // hook up output to block
        _block.addOutput(_outputSignal);

        try {
            _model.initialize();
        } catch (DAVEException e) {
            fail("problem initializing model in " + theRoutineName
                    + ": " + e.getMessage());
        }
    }

    public final void testGetSetValidateUpdateRelation() {

        // Before calling update, need to ask model for input vector (ignore result)

        try { // need to call before cycling model
            _model.getInputVector();
        } catch (DAVEException e1) {
            fail("error when trying to obtain VectorInfoArrayList in " + routineName);
        }

        assertFalse(checkRelation(4.5, "lt", 2.0, false));
        assertEquals("lt", _block.getRelationOp());
        assertFalse(checkRelation(4.5, "leq", 2.0, false));
        assertEquals("leq", _block.getRelationOp());
        assertFalse(checkRelation(4.5, "eq", 2.0, false));
        assertEquals("eq", _block.getRelationOp());
        assertTrue(checkRelation(4.5, "geq", 2.0, false));
        assertEquals("geq", _block.getRelationOp());
        assertTrue(checkRelation(4.5, "gt", 2.0, false));
        assertEquals("gt", _block.getRelationOp());

        assertTrue(checkRelation(-4.5, "lt", 2.0, false));
        assertEquals("lt", _block.getRelationOp());
        assertTrue(checkRelation(-4.5, "leq", 2.0, false));
        assertEquals("leq", _block.getRelationOp());
        assertFalse(checkRelation(-4.5, "eq", 2.0, false));
        assertEquals("eq", _block.getRelationOp());
        assertFalse(checkRelation(-4.5, "geq", 2.0, false));
        assertEquals("geq", _block.getRelationOp());
        assertFalse(checkRelation(-4.5, "gt", 2.0, false));
        assertEquals("gt", _block.getRelationOp());

        assertFalse(checkRelation(2.0, "lt", 2.0, false));
        assertEquals("lt", _block.getRelationOp());
        assertTrue(checkRelation(2.0, "leq", 2.0, false));
        assertEquals("leq", _block.getRelationOp());
        assertTrue(checkRelation(2.0, "eq", 2.0, false));
        assertEquals("eq", _block.getRelationOp());
        assertTrue(checkRelation(2.0, "geq", 2.0, false));
        assertEquals("geq", _block.getRelationOp());
        assertFalse(checkRelation(2.0, "gt", 2.0, false));
        assertEquals("gt", _block.getRelationOp());

        assertFalse(checkRelation(4.5, "LT", 2.0, false));
        assertEquals("lt", _block.getRelationOp());
        assertFalse(checkRelation(4.5, "LEQ", 2.0, false));
        assertEquals("leq", _block.getRelationOp());
        assertFalse(checkRelation(4.5, "EQ", 2.0, false));
        assertEquals("eq", _block.getRelationOp());
        assertTrue(checkRelation(4.5, "GEQ", 2.0, false));
        assertEquals("geq", _block.getRelationOp());
        assertTrue(checkRelation(4.5, "GT", 2.0, false));
        assertEquals("gt", _block.getRelationOp());

        assertTrue(checkRelation(-4.5, "LT", 2.0, false));
        assertEquals("lt", _block.getRelationOp());
        assertTrue(checkRelation(-4.5, "LEQ", 2.0, false));
        assertEquals("leq", _block.getRelationOp());
        assertFalse(checkRelation(-4.5, "EQ", 2.0, false));
        assertEquals("eq", _block.getRelationOp());
        assertFalse(checkRelation(-4.5, "GEQ", 2.0, false));
        assertEquals("geq", _block.getRelationOp());
        assertFalse(checkRelation(-4.5, "GT", 2.0, false));
        assertEquals("gt", _block.getRelationOp());

        assertFalse(checkRelation(2.0, "LT", 2.0, false));
        assertEquals("lt", _block.getRelationOp());
        assertTrue(checkRelation(2.0, "LEQ", 2.0, false));
        assertEquals("leq", _block.getRelationOp());
        assertTrue(checkRelation(2.0, "EQ", 2.0, false));
        assertEquals("eq", _block.getRelationOp());
        assertTrue(checkRelation(2.0, "GEQ", 2.0, false));
        assertEquals("geq", _block.getRelationOp());
        assertFalse(checkRelation(2.0, "GT", 2.0, false));
        assertEquals("gt", _block.getRelationOp());

        assertFalse(checkRelation(2.0, "le", 2.0, true));
        assertFalse(checkRelation(2.0, "XX", 2.0, true));
        assertFalse(checkRelation(2.0, "--", 2.0, true));

    }

    public final void testBlockMathRelation() {
        assertNotNull(_block);
        assertEquals(1, _block.getValue(), EPS);
    }

    public void testGenCcode() {
        CodeAndVarNames cvn;
        String expected;
        _model.setCodeDialect(Model.DT_ANSI_C);
        _block.getOutput().setDerivedFlag();
        cvn = _block.genCode();
        expected = "ALP_UNLIM < ALP_LIMIT";
        assertEquals(expected, cvn.getCode());
        assertEquals(2, cvn.getVarNames().size());
        assertEquals("ALP_UNLIM", cvn.getVarName(0));
        assertEquals("ALP_LIMIT", cvn.getVarName(1));
        
        _block.getOutput().clearDerivedFlag();
        cvn = _block.genCode();
        expected = "  outputSignal = " + expected + ";\n";
        assertEquals(expected, cvn.getCode());
        assertEquals(3, cvn.getVarNames().size());
        assertEquals("outputSignal", cvn.getVarName(0));
        assertEquals("ALP_UNLIM",    cvn.getVarName(1));
        assertEquals("ALP_LIMIT",    cvn.getVarName(2));
    }
    
    public void testGenFcode() {
        CodeAndVarNames cvn;
        String expected;
        _model.setCodeDialect(Model.DT_FORTRAN);
        _block.getOutput().setDerivedFlag();
        cvn = _block.genCode();
        expected = "ALP_UNLIM .LT. ALP_LIMIT";
        assertEquals(expected, cvn.getCode());
        assertEquals(2, cvn.getVarNames().size());
        assertEquals("ALP_UNLIM", cvn.getVarName(0));
        assertEquals("ALP_LIMIT", cvn.getVarName(1));
        
        _block.getOutput().clearDerivedFlag();
        cvn = _block.genCode();
        expected = "       outputSignal = " + expected + "\n";
        assertEquals(expected, cvn.getCode());
        assertEquals(3, cvn.getVarNames().size());
        assertEquals("outputSignal", cvn.getVarName(0));
        assertEquals("ALP_UNLIM",    cvn.getVarName(1));
        assertEquals("ALP_LIMIT",    cvn.getVarName(2));
    }
    
    public void testDescribeSelfFileWriter() {
        try {
            _block.describeSelf(_writer);
        } catch (IOException e) {
            assertTrue(false);
        }
        assertEquals("Block \"lt_3\" has two inputs (unlimited alpha, alpha limit),"
                + " one output (outputSignal), value [1.0] and is a Relational Operator math block.",
                _writer.toString());
    }

    private boolean checkRelation(Double n1, String relation, Double n2, boolean expectException) {
        // set operand values
        _value1Block.setValue(n1);
        _value2Block.setValue(n2);

        // set relationship test
        try {
            _block.setRelationOp(relation);
        } catch (DAVEException e1) {
            if (!expectException) {
                fail("Unexpected exception in " + routineName
                        + ".checkRelation for [" + n1.toString()
                        + " " + relation + " " + n2.toString() + "]: " + e1.getMessage());
            }
        }

        // run model
        try {
            _model.cycle();
        } catch (DAVEException e) {
            fail("Unexpected exception in " + routineName
                    + ".checkRelation for [" + n1.toString()
                    + " " + relation + " " + n2.toString() + "]: " + e.getMessage());
        }

        // check result
        return (_block.getValue() == 1);
    }
}
