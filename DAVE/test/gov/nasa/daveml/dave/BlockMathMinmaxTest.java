package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import org.jdom.Element;

import junit.framework.TestCase;

// added 2011-07-26 Bruce Jackson
public class BlockMathMinmaxTest extends TestCase {

    protected Model modelMax, modelMin;
    private final double EPS = 0.000001;
    protected BlockMathMinmax minBlock, maxBlock;

    @Override
    protected void setUp() throws Exception {

        String routineName = "BlockMathMinmaxTest::setUp()";

        // create empty models
        modelMin = new Model(3, 3);
        modelMax = new Model(3, 3);

        // don't need input signal - can create const block and signal in one step later
        minBlock = setUpExtremaModel("min", routineName, modelMin);
        maxBlock = setUpExtremaModel("max", routineName, modelMax);
    }

    private BlockMathMinmax setUpExtremaModel(String op, String routineName, Model _model) throws Exception {

        Signal _outputSignal;
        Signal _value1Signal;
        Signal _value2Signal;
        String _value1SignalID;
        String _value2SignalID;
        BlockInput _input1Block;
        BlockInput _input2Block;
        BlockOutput _outputBlock;
        BlockMathMinmax _block;

        VectorInfoArrayList _inputVector;
        VectorInfoArrayList _outputVector;


        // build an max selector algorithm
        //      <apply>
        //        <max/>
        //        <ci>PB</ci>
        //        <ci>BSPAN</ci>
        //      </apply>             <!-- max(PB,BSPAN) -->

        // first, build the input blocks and signals
        _value1SignalID = "PB";
        _value2SignalID = "BSPAN";
        _value1Signal = new Signal("PB", _value1SignalID, "d_s", 1, _model);
        _value2Signal = new Signal("BSPAN", _value2SignalID, "ft", 1, _model);
        _input1Block = new BlockInput(_value1Signal, _model);
        _input2Block = new BlockInput(_value2Signal, _model);

        // create downstream signal
        _outputSignal = new Signal("outputSignal", _model);
        _outputBlock = new BlockOutput(_outputSignal, _model);

        // build JDOM from XML snippet
        Element theValue1 = new Element("ci");	// add numeric constant
        theValue1.addContent("PB");

        Element theValue2 = new Element("ci");
        theValue2.addContent("BSPAN");

        Element theMaxElement = new Element(op);

        Element applyElement = new Element("apply");
        applyElement.addContent(theMaxElement);
        applyElement.addContent(theValue1);
        applyElement.addContent(theValue2);

        // create extrema block
        _block = new BlockMathMinmax(applyElement, _model);

        // hook up inputs to block
        _block.addInput(_value1Signal, 1);
        _block.addInput(_value2Signal, 2);

        // hook up output to block
        _block.addOutput(_outputSignal);

        try {
            _model.initialize();
        } catch (DAVEException e) {
            fail("problem initializing model in " + routineName);
        }

        _inputVector = _model.getInputVector();
        assert (_inputVector != null);
        assertEquals(2, _inputVector.size());

        VectorInfo _input1 = _inputVector.get(0);
        assert (_input1 != null);
        _input1.setValue("2.0");

        VectorInfo _input2 = _inputVector.get(1);
        assert (_input2 != null);
        _input2.setValue("-3.5");

        _model.hookUpIO();

        _outputVector = _model.getOutputVector();
        assert (_outputVector != null);
//        assertEquals(1, _outputVector.size());

        try {
            _model.cycle();
        } catch (Exception e) {
            fail("Unexpected exception in " + routineName
                    + ".setUp for op " + op + ": " + e.getMessage());
        }
        return _block;
    }

    public void testDescribeSelfWriter() {

        testDescribeSelf("max", maxBlock, "2.0");
        testDescribeSelf("min", minBlock, "-3.5");
    }

    private void testDescribeSelf(String op, BlockMathMinmax block,
            String valString) {

        StringWriter writer = new StringWriter();
        try {
            block.describeSelf(writer);
        } catch (IOException e) {
            fail("testDescribeSelfWriter of BlockMathMinmaxTest threw unexpected exception: "
                    + e.getMessage());
        }
        assertEquals("Block \"" + op + "_4\" has two inputs (PB, BSPAN),"
                + " one output (outputSignal), value [" + valString + "] and is a extrema math block.",
                writer.toString());
    }

    public void testGetValue() {
        assertEquals(2.0, maxBlock.getValue(), EPS);
        assertEquals(-3.5, minBlock.getValue(), EPS);
    }

    public void testIsReady() {
        assertTrue(maxBlock.isReady());
        assertTrue(minBlock.isReady());
    }

    public void testAllInputsReady() {
        assertTrue(maxBlock.allInputsReady());
        assertTrue(minBlock.allInputsReady());
    }

    public void testMakeVerbose() {
        assertFalse(maxBlock.isVerbose());
        maxBlock.makeVerbose();
        assertTrue(maxBlock.isVerbose());
        maxBlock.silence();
        assertFalse(maxBlock.isVerbose());
    }

    public void testGetName() {
        assertEquals("max_4", maxBlock.getName());
        assertEquals("min_4", minBlock.getName());
    }

    public void testGetType() {
        assertEquals("maximum selector", maxBlock.getType());
        assertEquals("minimum selector", minBlock.getType());
    }

    public void testGetVarID() {
        assertEquals("PB", maxBlock.getVarID(1));
        assertEquals("BSPAN", maxBlock.getVarID(2));
    }

    public void testGetVarIDIterator() {
        String name;
        Iterator<String> it = maxBlock.getVarIDIterator();
        assertTrue(it.hasNext());
        name = it.next();
        assertEquals("PB", name);
        assertTrue(it.hasNext());
        name = it.next();
        assertEquals("BSPAN", name);
        assertFalse(it.hasNext());
    }

    public void testGetOutputVarID() {
        assertEquals("outputSignal", maxBlock.getOutputVarID());
        assertEquals("outputSignal", minBlock.getOutputVarID());
    }

    public void testSetName() {
        maxBlock.setName("fart");
        assertEquals("fart", maxBlock.getName());
    }

    public void testNumInputs() {
        assertEquals(2, maxBlock.numInputs());
        assertEquals(2, minBlock.numInputs());
    }

    public void testNumVarIDs() {
        assertEquals(2, maxBlock.numVarIDs());
        assertEquals(2, minBlock.numVarIDs());
    }

    public void testUpdate() {

        String routineName = "BlockMathMinmaxTest::testUpdate()";

        checkMax(modelMax, 0., 0., routineName);
        checkMax(modelMax, 0., -1., routineName);
        checkMax(modelMax, 0., -1.01, routineName);
        checkMax(modelMax, 0., 1.01, routineName);
        checkMax(modelMax, -9., -1., routineName);

        checkMin(modelMin, 0., 0., routineName);
        checkMin(modelMin, 0., -1., routineName);
        checkMin(modelMin, 0., -1.01, routineName);
        checkMin(modelMin, 0., 1.01, routineName);
        checkMin(modelMin, -9., -1., routineName);
    }

    private void checkMax(Model model, Double n1, Double n2, String callingName) {

        String routineName = callingName + "::checkMax()";
        VectorInfoArrayList inputs = null;

        try {
            inputs = model.getInputVector();

            // set operand values
            Iterator<VectorInfo> it = inputs.iterator();
            VectorInfo input1 = it.next();
            VectorInfo input2 = it.next();

            input1.setValue(n1);
            input2.setValue(n2);

            // run model
            try {
                model.cycle();
            } catch (Exception e) {
                fail("Unexpected exception in " + routineName
                        + " for max(" + n1.toString()
                        + ", " + n2.toString() + "]): " + e.getMessage());
            }

            // check result
            VectorInfoArrayList outputs = model.getOutputVector();
            it = outputs.iterator();
            VectorInfo output = it.next();

            assertEquals(Math.max(n1, n2), output.getValue());

        } catch (DAVEException e) {
            fail("error when trying to obtain VectorInfoArrayList in "
                    + routineName + ": " + e.getMessage());
        }

    }
    
        private void checkMin(Model model, Double n1, Double n2, String callingName) {

        String routineName = callingName + "::checkMin()";
        VectorInfoArrayList inputs = null;

        try {
            inputs = model.getInputVector();

            // set operand values
            Iterator<VectorInfo> it = inputs.iterator();
            VectorInfo input1 = it.next();
            VectorInfo input2 = it.next();

            input1.setValue(n1);
            input2.setValue(n2);

            // run model
            try {
                model.cycle();
            } catch (Exception e) {
                fail("Unexpected exception in " + routineName
                        + " for min(" + n1.toString()
                        + ", " + n2.toString() + "]): " + e.getMessage());
            }

            // check result
            VectorInfoArrayList outputs = model.getOutputVector();
            it = outputs.iterator();
            VectorInfo output = it.next();

            assertEquals(Math.min(n1, n2), output.getValue());

        } catch (DAVEException e) {
            fail("error when trying to obtain VectorInfoArrayList in "
                    + routineName + ": " + e.getMessage());
        }

    }
}