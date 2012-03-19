package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom.Element;

import junit.framework.TestCase;

public class BlockMathSwitch3Test extends TestCase {

    protected Model _model;
    private StringWriter _writer;
    protected BlockMathSwitch _block;
    protected BlockMathConstant _inputBlock;
    protected BlockMathConstant _upperLimitBlock;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _model = new Model(3, 3);
        _writer = new StringWriter();

        _block = generateSampleSwitch(_model);

        // retrieve the input blocks
        SignalArrayList theSignals = _model.getSignals();

        Signal inputSignal = theSignals.findByID("ALP_UNLIM");
        assertNotNull(inputSignal);
        Block theBlock = inputSignal.getSource();
        assertNotNull(theBlock);
        assertEquals("constant value", theBlock.myType);
        _inputBlock = (BlockMathConstant) theBlock;
        assertNotNull(_inputBlock);

        inputSignal = theSignals.findByID("ALP_MAX_LIM");
        assertNotNull(inputSignal);
        theBlock = inputSignal.getSource();
        assertNotNull(theBlock);
        assertEquals("constant value", theBlock.myType);
        _upperLimitBlock = (BlockMathConstant) theBlock;
        assertNotNull(_upperLimitBlock);
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

        _upperLimitBlock.setValue(40.0);
        assertEquals( -2.0, checkSwitch( -99. ));
        assertEquals( -1.0, checkSwitch(  -1. ));
        assertEquals(  0.0, checkSwitch(   0. ));
        assertEquals( +1.0, checkSwitch(  +1. ));
        assertEquals( +5.0, checkSwitch(  +5. ));
        assertEquals(+10.0, checkSwitch( +10. ));
        assertEquals(+20.0, checkSwitch( +20. ));
        assertEquals(+30.0, checkSwitch( +30. ));
        assertEquals(+40.0, checkSwitch( +40. ));
        assertEquals(+40.0, checkSwitch( +99. ));


        _upperLimitBlock.setValue(20.0);
        assertEquals( -2.0, checkSwitch( -99. ));
        assertEquals( -1.0, checkSwitch(  -1. ));
        assertEquals(  0.0, checkSwitch(   0. ));
        assertEquals( +1.0, checkSwitch(  +1. ));
        assertEquals( +5.0, checkSwitch(  +5. ));
        assertEquals(+10.0, checkSwitch( +10. ));
        assertEquals(+20.0, checkSwitch( +20. ));
        assertEquals(+20.0, checkSwitch( +30. ));
        assertEquals(+20.0, checkSwitch( +40. ));
        assertEquals(+20.0, checkSwitch( +99. ));

    }

    private double checkSwitch(Double switchVal) {
        String routineName = "TestBlockMathSwitch3.checkSwitch()";

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
            fail("testDescribeSelfWriter of TestBlockMathSwitch3 threw unexpected exception: "
                    + e.getMessage());
        }
        assertEquals("Block \"switch_3\" has three inputs (const_-2_, null, switch_7),"
                + " one output (outputSignal), value [NaN] and is a Switch math block.",
                _writer.toString());
    }

    public static BlockMathSwitch generateSampleSwitch(Model model) {

        // Generate a BlockMathSwitch block (and associated wiring)
        // Adapted for HL-20 aerodynamc model  angle of attack limit function
        // rev J.
        //
        //  <apply>
        //    <piecewise>
        //      <piece>
        //        <cn>-2</cn>
        //        <apply><lt/><ci>ALP_UNLIM</ci><cn>-2</cn></apply>
        //      </piece>
	//      <piece>
	//        <ci>ALP_MAX_LIM</ci>
	//        <apply><gt/><ci>ALP_UNLIM</ci><ci>ALP_MAX_LIM</ci></apply>
	//      </piece>
	//      <otherwise>
	//        <ci>ALP_UNLIM</ci>
	//      </otherwise>
	//    </piecewise>
        //  </apply>


        // create upstream (input) signals
        Block  alpMaxLim         = new BlockMathConstant("40", model);
        Signal alpMaxLimSignal   = new Signal("alpha upper limit",
                                    "ALP_MAX_LIM", "deg", 1, model);

        Block  alpUnlim          = new BlockMathConstant("0", model);
        Signal alpUnlimSignal    = new Signal("unlimited alpha input",
                                    "ALP_UNLIM", "deg", 1, model);

        // create downstream (output) signal
        Signal outputSignal      = new Signal("outputSignal", model);

        // wire up input constant blocks to their respective signal lines
        try {
            alpMaxLim.addOutput(alpMaxLimSignal);
            alpUnlim.addOutput(alpUnlimSignal);
        } catch (DAVEException e) {
            fail("Unexpected exception in TestBlockMathSwitch2.generateSampleSwitch() "
                    + e.getMessage());
        }

        // now build JDOM from XML snippet
        
        Element innerApply1 = new Element("apply");   //     <apply>
        innerApply1.addContent(new Element("lt"));    //       <lt/>
        innerApply1.addContent(new Element("ci")      //
                .addContent("ALP_UNLIM"));            //       <ci>ALP_UNLIM</ci>
        innerApply1.addContent(new Element("cn")      //
                .addContent("-2"));                   //       <cn>-2</cn>
                                                      //     </apply>

        Element innerApply2 = new Element("apply");   //     <apply>
        innerApply2.addContent(new Element("gt"));    //       <gt/>
        innerApply2.addContent(new Element("ci")      //
                .addContent("ALP_UNLIM"));            //       <ci>ALP_UNLIM</ci>
        innerApply2.addContent(new Element("ci")      //
                .addContent("ALP_MAX_LIM"));          //       <ci>ALP_MAX_LIM</ci>
                                                      //     </apply>

        Element piece1 = new Element("piece");        //   <piece>
        piece1.addContent(new Element("cn")
                .addContent("-2"));                   //     <cn>-2</cn>
        piece1.addContent(innerApply1);               //     <apply>
                                                      //       <lt/>
                                                      //       <ci>ALP_UNLIM</ci>
                                                      //       <cn>-2</cn>
                                                      //     </apply>
                                                      //   </piece>

        Element piece2 = new Element("piece");        //   <piece>
        piece2.addContent(new Element("ci")
                .addContent("ALP_MAX_LIM"));          //     <ci>ALP_MAX_LIM</ci>
        piece2.addContent(innerApply2);               //     <apply>
                                                      //       <gt/>
                                                      //       <ci>switchVal</ci>
                                                      //       <cn>1</cn>
                                                      //     </apply>
                                                      //   </piece>

        Element otherwise = new Element("otherwise"); //   <otherwise>
        otherwise.addContent(new Element("ci")
                .addContent("ALP_UNLIM"));            //     <ci>ALP_UNLIM</ci>
                                                      //   </otherwise>

        Element piecewise = new Element("piecewise");
        piecewise.addContent(piece1);
        piecewise.addContent(piece2);
        piecewise.addContent(otherwise);

        Element outerApply = new Element("apply");
        outerApply.addContent(piecewise);

        BlockMathSwitch bms = null;
        bms = new BlockMathSwitch(outerApply, model);

        // inputs get wired in later with model.wireBlocks()

        // hook up output
        try {
            bms.addOutput(outputSignal);
        } catch (DAVEException e) {
            fail("Unexpected exception in hooking up output signal "
                    + "in TestBlockMathSwitch2.generateSampleSwitch: " + e.getMessage());
        }
        return bms;
    }
}
