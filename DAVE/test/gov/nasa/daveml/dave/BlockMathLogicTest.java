package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom.Element;

import junit.framework.TestCase;

public class BlockMathLogicTest extends TestCase {

    protected Model _model;
    protected Signal _output1Signal;
    protected Signal _output2Signal;
    protected Signal _output3Signal;
    protected Signal _value1Signal;
    protected Signal _value2Signal;
    protected Signal _value3Signal;
    protected String _value1SignalID;
    protected String _value2SignalID;
    protected String _value3SignalID;
    protected BlockMathConstant _value1Block;
    protected BlockMathConstant _value2Block;
    protected BlockMathConstant _value3Block;
    protected BlockMathLogic _block1;
    protected BlockMathLogic _block2;
    protected BlockMathLogic _block3;
    private StringWriter _writer;
    protected String routineName = "TestBlockMathLogic";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String theRoutineName = "TestBlockMathProduct::setUp()";

        // don't need input signal - can create const block and signal in one step later
        _model = new Model(4, 4);
        _writer = new StringWriter();

        // build a relation calculation
        //      <apply>
        //        <or/>
        //        <ci>BOOL_1</ci>
        //        <ci>BOOL_2</ci>
        //      </apply>


        // first, build the upstream constant blocks and signals
        _value1Block = new BlockMathConstant(" 1.0", _model);
        _value2Block = new BlockMathConstant(" 0.0", _model);
        _value3Block = new BlockMathConstant(" 0.0", _model);
        _value1SignalID = "BOOL_1";
        _value2SignalID = "BOOL_2";
        _value3SignalID = "BOOL_3";
        _value1Signal = new Signal("boolean 1", _value1SignalID, "nd", 1, _model);
        _value2Signal = new Signal("boolean 2", _value2SignalID, "nd", 1, _model);
        _value3Signal = new Signal("boolean 3", _value3SignalID, "nd", 1, _model);
        _value1Block.addOutput(_value1Signal);
        _value2Block.addOutput(_value2Signal);
        _value3Block.addOutput(_value3Signal);

        // create downstream signal
        _output1Signal = new Signal("output1Signal", _model);
        _output2Signal = new Signal("output2Signal", _model);
        _output3Signal = new Signal("output3Signal", _model);

        // build JDOM from XML snippet
        Element theValue11 = new Element("ci");	// add numeric constant
        theValue11.addContent("BOOL_1");

        Element theValue21 = new Element("ci");
        theValue21.addContent("BOOL_1");
    
        Element theValue22 = new Element("ci");
        theValue22.addContent("BOOL_2");

        Element theValue31 = new Element("ci");
        theValue31.addContent("BOOL_1");
    
        Element theValue32 = new Element("ci");
        theValue32.addContent("BOOL_2");

        Element theValue33 = new Element("ci");
        theValue33.addContent("BOOL_3");

        Element theLogicalElement1 = new Element("not");
        Element theLogicalElement2 = new Element("or");
        Element theLogicalElement3 = new Element("or");

        Element apply1Element = new Element("apply");
        apply1Element.addContent(theLogicalElement1);
        apply1Element.addContent(theValue11);

        Element apply2Element = new Element("apply");
        apply2Element.addContent(theLogicalElement2);
        apply2Element.addContent(theValue21);
        apply2Element.addContent(theValue22);

        Element apply3Element = new Element("apply");
        apply3Element.addContent(theLogicalElement3);
        apply3Element.addContent(theValue31);
        apply3Element.addContent(theValue32);
        apply3Element.addContent(theValue33);

        // create logic blocks
        _block1 = new BlockMathLogic(apply1Element, _model);
        _block2 = new BlockMathLogic(apply2Element, _model);
        _block3 = new BlockMathLogic(apply3Element, _model);

        // hook up inputs to block
        _block1.addInput(_value1Signal, 1);

	_block2.addInput(_value1Signal, 1);
	_block2.addInput(_value2Signal, 2);

	_block3.addInput(_value1Signal, 1);
	_block3.addInput(_value2Signal, 2);
	_block3.addInput(_value3Signal, 3);

        // hook up outputs to blocks
        _block1.addOutput(_output1Signal);
        _block2.addOutput(_output2Signal);
        _block3.addOutput(_output3Signal);

        try {
            _model.initialize();
        } catch (DAVEException e) {
            fail("problem initializing model in " + theRoutineName
                    + ": " + e.getMessage());
        }
    }

    public final void testGetSetValidateUpdateLogic() {

        // Before calling update, need to ask model for input vector (ignore result)

        try { // need to call before cycling model
            _model.getInputVector();
        } catch (DAVEException e1) {
            fail("error when trying to obtain VectorInfoArrayList in " + routineName);
        }

        assertTrue(checkLogic("or",  false, false, false));        assertEquals("or",  _block2.getLogicOp());
        assertTrue(checkLogic("or",  false, true , true ));        assertEquals("or",  _block2.getLogicOp());
        assertTrue(checkLogic("or",  true , false, true ));        assertEquals("or",  _block2.getLogicOp());
        assertTrue(checkLogic("or",  true , true , true ));        assertEquals("or",  _block2.getLogicOp());

        assertTrue(checkLogic("or",  false, false, false, false)); assertEquals("or",  _block3.getLogicOp());
        assertTrue(checkLogic("or",  false, false, true , true )); assertEquals("or",  _block3.getLogicOp());
        assertTrue(checkLogic("or",  false, true , false, true )); assertEquals("or",  _block3.getLogicOp());
        assertTrue(checkLogic("or",  false, true , true , true )); assertEquals("or",  _block3.getLogicOp());
        assertTrue(checkLogic("or",  true , false, false, true )); assertEquals("or",  _block3.getLogicOp());
        assertTrue(checkLogic("or",  true , false, true , true )); assertEquals("or",  _block3.getLogicOp());
        assertTrue(checkLogic("or",  true , true , false, true )); assertEquals("or",  _block3.getLogicOp());
        assertTrue(checkLogic("or",  true , true , true , true )); assertEquals("or",  _block3.getLogicOp());


        assertTrue(checkLogic("and", false, false, false));        assertEquals("and", _block2.getLogicOp());
        assertTrue(checkLogic("and", false, true , false));        assertEquals("and", _block2.getLogicOp());
        assertTrue(checkLogic("and", true , false, false));        assertEquals("and", _block2.getLogicOp());
        assertTrue(checkLogic("and", true , true , true ));        assertEquals("and", _block2.getLogicOp());

        assertTrue(checkLogic("and", false, false, false, false)); assertEquals("and", _block3.getLogicOp());
        assertTrue(checkLogic("and", false, false, true , false)); assertEquals("and", _block3.getLogicOp());
        assertTrue(checkLogic("and", false, true , false, false)); assertEquals("and", _block3.getLogicOp());
        assertTrue(checkLogic("and", false, true , true , false)); assertEquals("and", _block3.getLogicOp());
        assertTrue(checkLogic("and", true , false, false, false)); assertEquals("and", _block3.getLogicOp());
        assertTrue(checkLogic("and", true , false, true , false)); assertEquals("and", _block3.getLogicOp());
        assertTrue(checkLogic("and", true , true , false, false)); assertEquals("and", _block3.getLogicOp());
        assertTrue(checkLogic("and", true , true , true , true )); assertEquals("and", _block3.getLogicOp());

        assertTrue(checkLogic("not", true , false )); assertEquals("not", _block1.getLogicOp());
        assertTrue(checkLogic("not", false, true  )); assertEquals("not", _block1.getLogicOp());

    }

    public final void testBlockMathLogic() {
        assertNotNull(_block1);
        assertNotNull(_block2);
        assertNotNull(_block3);
        assertTrue(_block2.getValue() == 1.0 );
    }

    public void testGenCcode() {
        CodeAndVarNames cvn;
        String expected;
        _model.setCodeDialect(Model.DT_ANSI_C);
        _block2.getOutput().setDerivedFlag();
        cvn = _block2.genCode();
        expected = "BOOL_1 || BOOL_2";
        assertEquals(expected, cvn.getCode());
        assertEquals(2, cvn.getVarNames().size());
        assertEquals("BOOL_1", cvn.getVarName(0));
        assertEquals("BOOL_2", cvn.getVarName(1));
        
        _block2.getOutput().clearDerivedFlag();
        cvn = _block2.genCode();
        expected = "  output2Signal = " + expected + ";\n";
        assertEquals(expected, cvn.getCode());
        assertEquals(3, cvn.getVarNames().size());
        assertEquals("output2Signal", cvn.getVarName(0));
        assertEquals("BOOL_1",    cvn.getVarName(1));
        assertEquals("BOOL_2",    cvn.getVarName(2));
    }
    
    public void testGenFcode() {
        CodeAndVarNames cvn;
        String expected;
        _model.setCodeDialect(Model.DT_FORTRAN);
        _block2.getOutput().setDerivedFlag();
        cvn = _block2.genCode();
        expected = "BOOL_1 .OR. BOOL_2";
        assertEquals(expected, cvn.getCode());
        assertEquals(2, cvn.getVarNames().size());
        assertEquals("BOOL_1", cvn.getVarName(0));
        assertEquals("BOOL_2", cvn.getVarName(1));
        
        _block2.getOutput().clearDerivedFlag();
        cvn = _block2.genCode();
        expected = "       output2Signal = " + expected + "\n";
        assertEquals(expected, cvn.getCode());
        assertEquals(3, cvn.getVarNames().size());
        assertEquals("output2Signal", cvn.getVarName(0));
        assertEquals("BOOL_1",    cvn.getVarName(1));
        assertEquals("BOOL_2",    cvn.getVarName(2));
    }
    
    public void testDescribeSelfFileWriter() {
        try {
            _block2.describeSelf(_writer);
        } catch (IOException e) {
            assertTrue(false);
        }
        assertEquals("Block \"or_5\" has two inputs (boolean 1, boolean 2),"
                + " one output (output2Signal), value [1.0] and is a Logical Operator math block.",
                _writer.toString());
    }

    private boolean checkLogic(String relation, Boolean n1, boolean expectedResult) {
	boolean expectException = false;
        // set operand values
        _value1Block.setValue(n1 ? 1.0 : 0.0);

        // set relationship test
        try {
            _block1.setLogicOp(relation);
        } catch (DAVEException e1) {
            if (!expectException) {
                fail("Unexpected exception in " + routineName
                    + ".checkLogic for [" + relation 
                    + " " + n1.toString() + "]: " + e1.getMessage());
            }
        }

        // run model
        try {
            _model.cycle();
        } catch (DAVEException e) {
            fail("Unexpected exception in " + routineName
                    + ".checkLogic for [" + relation 
                    + " " + n1.toString() + "]: " + e.getMessage());
        }

        // check result
        return (_block1.getBoolValue() == expectedResult );
    }

    private boolean checkLogic(String relation, Boolean n1, Boolean n2, boolean expectedResult ) {
	boolean expectException = false;
        // set operand values
        _value1Block.setValue(n1 ? 1.0 : 0.0);
        _value2Block.setValue(n2 ? 1.0 : 0.0);

        // set relationship test
        try {
            _block2.setLogicOp(relation);
        } catch (DAVEException e1) {
            if (!expectException) {
                fail("Unexpected exception in " + routineName
                    + ".checkLogic for [" + relation 
                    + " " + n1.toString() + " " + n2.toString() + "]: " + e1.getMessage());
            }
        }

        // run model
        try {
            _model.cycle();
        } catch (DAVEException e) {
            fail("Unexpected exception in " + routineName
                    + ".checkLogic for [" + relation 
                    + " " + n1.toString() + " " + n2.toString() + "]: " + e.getMessage());
        }

        // check result
        return (_block2.getBoolValue() == expectedResult );
    }

    private boolean checkLogic(String relation, Boolean n1, Boolean n2, Boolean n3, boolean expectedResult ) {
	boolean expectException = false;
        // set operand values
        _value1Block.setValue(n1 ? 1.0 : 0.0);
        _value2Block.setValue(n2 ? 1.0 : 0.0);
        _value3Block.setValue(n3 ? 1.0 : 0.0);

        // set relationship test
        try {
            _block3.setLogicOp(relation);
        } catch (DAVEException e1) {
            if (!expectException) {
                fail("Unexpected exception in " + routineName
		     + ".checkLogic for [" + relation 
		     + " " + n1.toString() + " " + n2.toString() + " " + n3.toString() + "]: " + e1.getMessage());
            }
        }

        // run model
        try {
            _model.cycle();
        } catch (DAVEException e) {
            fail("Unexpected exception in " + routineName
		     + ".checkLogic for [" + relation 
		     + " " + n1.toString() + " " + n2.toString() + " " + n3.toString() + "]: " + e.getMessage());
        }

        // check result
        return (_block3.getBoolValue() == expectedResult );
    }
}
