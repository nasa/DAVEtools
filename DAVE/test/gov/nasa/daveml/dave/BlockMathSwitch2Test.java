package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom.Element;

import junit.framework.TestCase;

public class BlockMathSwitch2Test extends TestCase {

    protected Model _model;
    private StringWriter _writer;
    protected BlockMathSwitch _block;
    protected BlockMathConstant _inputBlock;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _model = new Model(3, 3);
        _writer = new StringWriter();

        _block = generateSampleSwitch(_model);

        // retrieve the input block
        SignalArrayList theSignals = _model.getSignals();
        Signal inputSignal = theSignals.findByID("switchVal");
        assertNotNull(inputSignal);
        Block theBlock = inputSignal.getSource();
        assertNotNull(theBlock);
        assertEquals("constant value", theBlock.myType);
        _inputBlock = (BlockMathConstant) theBlock;
        assertNotNull(_inputBlock);
    }

    public void testUpdate() {
        assertNotNull(_block);
        String routineName = "TestBlockMathSwitch2::testUpdate()";

        _model.wireBlocks();

        // must call this routine before cycling (but can ignore returned object)
        try {
            _model.getInputVector();
        } catch (DAVEException e) {
            fail("error when trying to obtain VectorInfoArrayList in "
                    + routineName + ": " + e.getMessage());
        }

        try {
            _model.initialize();
        } catch (DAVEException e) {
            fail("error when trying to initialize model in "
                    + routineName + ": " + e.getMessage());
        }

        assertEquals(-1.0, checkSwitch(-99.));
        assertEquals(-1.0, checkSwitch(-1.));
        assertEquals(-0.5, checkSwitch(-0.5));
        assertEquals(-0.000001, checkSwitch(-0.000001));
        assertEquals(-0.0, checkSwitch(-0.0));
        assertEquals( 0.0, checkSwitch( 0.0));
        assertEquals( 0.0, checkSwitch(+0.0));
        assertEquals(+0.000001, checkSwitch(+0.000001));
        assertEquals(+0.5, checkSwitch(+0.5));
        assertEquals(+1.0, checkSwitch(+1.));
        assertEquals(+1.0, checkSwitch(+99.));
    }

    private double checkSwitch(Double switchVal) {
        String routineName = "TestBlockMathSwitch2.checkSwitch()";

        // set operand values
        _inputBlock.setValue(switchVal);

        // run model
        try {
            _model.cycle();
        } catch (Exception e) {
            fail("Unexpected exception in " + routineName
                    + " for [" + switchVal.toString() + "]: " + e.getMessage());
        }

        // check result
        return _block.getValue();
    }

    public void testDescribeSelfWriter() {
        try {
            _block.describeSelf(_writer);
        } catch (IOException e) {
            fail("testDescribeSelfWriter of TestBlockMathSwitch2 threw unexpected exception: "
                    + e.getMessage());
        }
        assertEquals("Block \"switch_2\" has three inputs (const_-1_, null, switch_6),"
                + " one output (outputSignal), value [NaN] and is a Switch math block.",
                _writer.toString());
    }

    public static BlockMathSwitch generateSampleSwitch(Model model) {

        // Generate a BlockMathSwitch block (and associated wiring) for
        // a switch that outputs -1 if switchVal is less than -1, 
        //     +1 if switchVal is greater than +1 and 'switchVal' otherwise
        //        <apply>
        //          <piecewise>
        //            <piece>
        //              <cn>-1</ci>
        //              <apply>
        //                <lt/>
        //                <ci>switchVal</ci>
        //                <cn>-1</cn>
        //              </apply>
        //            </piece>
        //            <piece>
        //              <cn>+1</ci>
        //              <apply>
        //                <gt/>
        //                <ci>switchVal</ci>
        //                <cn>+1</cn>
        //              </apply>
        //            </piece>
        //            <otherwise>
        //              <ci>switchVal</ci>
        //            </otherwise>
        //          </piecewise>
        //        </apply>

        Block swValueBlock;
        String swValueSignalID;
        Signal swValueSignal;
        Signal outputSignal;

        swValueBlock = new BlockMathConstant("99", model);
        swValueSignalID = "switchVal";
        swValueSignal = new Signal("switch value", swValueSignalID, "nd", 1, model);
        try {
            swValueBlock.addOutput(swValueSignal);
        } catch (DAVEException e) {
            fail("Unexpected exception in TestBlockMathSwitch2.generateSampleSwitch() "
                    + e.getMessage());
        }

        // create downstream signal
        outputSignal = new Signal("outputSignal", model);

        // build JDOM from XML snippet

        Element innerApply1 = new Element("apply");   //     <apply>
        innerApply1.addContent(new Element("lt"));    //       <lt/>
        innerApply1.addContent(new Element("ci")
                .addContent("switchVal"));            //       <ci>switchVal</ci>
        innerApply1.addContent(new Element("cn")
                .addContent("-1"));                   //       <cn>-1</cn>
                                                      //     </apply>

        Element innerApply2 = new Element("apply");   //     <apply>
        innerApply2.addContent(new Element("gt"));    //       <gt/>
        innerApply2.addContent(new Element("ci")
                .addContent("switchVal"));            //       <ci>switchVal</ci>
        innerApply2.addContent(new Element("cn")
                .addContent("1"));                    //       <cn>1</cn>
                                                      //     </apply>

        Element piece1 = new Element("piece");        //   <piece>
        piece1.addContent(new Element("cn")
                .addContent("-1"));                   //     <cn>-1</cn>
        piece1.addContent(innerApply1);               //     <apply>
                                                      //       <lt/>
                                                      //       <ci>switchVal</ci>
                                                      //       <cn>-1</cn>
                                                      //     </apply>
                                                      //   </piece>

        Element piece2 = new Element("piece");        //   <piece>
        piece2.addContent(new Element("cn")
                .addContent("1"));                    //     <cn>1</cn>
        piece2.addContent(innerApply2);               //     <apply>
                                                      //       <gt/>
                                                      //       <ci>switchVal</ci>
                                                      //       <cn>1</cn>
                                                      //     </apply>
                                                      //   </piece>

        Element otherwise = new Element("otherwise"); //   <otherwise>
        otherwise.addContent(new Element("ci")
                .addContent("switchVal"));            //     <ci>switchVal</ci>
                                                      //   </otherwise>

        Element piecewise = new Element("piecewise");
        piecewise.addContent(piece1);
        piecewise.addContent(piece2);
        piecewise.addContent(otherwise);

        Element outerApply = new Element("apply");
        outerApply.addContent(piecewise);

        BlockMathSwitch bms = null;
        bms = new BlockMathSwitch(outerApply, model);
        try {
            bms.addOutput(outputSignal);
        } catch (DAVEException e) {
            fail("Unexpected exception in hooking up output signal "
                    + "in TestBlockMathSwitch2.generateSampleSwitch: " + e.getMessage());
        }
        return bms;
    }
}
