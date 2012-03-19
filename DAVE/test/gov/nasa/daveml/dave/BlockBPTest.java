package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;
import junit.framework.TestCase;

public class BlockBPTest extends TestCase {

    protected Model _model;
    protected BreakpointSet _bps;
    protected BlockBP _bpb;
    protected Signal _inSig;
    protected Signal _outSig;
    protected BlockMathConstant _sourceBlock;
    private StringWriter _writer;
    private final Double EPS = 0.000001;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _writer = new StringWriter();
        _model = new Model(3, 3);
        _inSig = new Signal("inSig", _model);
        _outSig = new Signal("outSig", _model);
        _sourceBlock = new BlockMathConstant("32.15", _model);

        // hook source block to input signal
        _sourceBlock.addOutput(_inSig);

        // create breakpointset
        try {
            _bps = new BreakpointSet("alpha", "ALPHA1",
                    "-10. -5. 0 5 10 15. 20. 25. 30 35 40 45.",
                    " Alpha breakpoints for basic and damping aero tables ", _model);
        } catch (DAVEException e) {
            fail("Exception in testBlockBPTest.setUp() call to create a BreakpointSet( element, model) : "
                    + e.getMessage());
        }

        // create breakpoint block
        try {
//  BlockBP( String bpID, String ourName, Signal inSignal, Signal outSignal, Model m)
            _bpb = new BlockBP("ALPHA1", "theBPblock", _inSig, _outSig, _model);
        } catch (DAVEException e) {
            fail("Exception in testBlockBPTest.testBlockPB() in call to create a BlockBP: "
                    + e.getMessage());
        }
    }

    public void testBlockBP() {
        assertNotNull(_bpb);
    }

    public void testUpdate() {

        // try with nominal value
        tryBPBupdate();
        assertEquals(8.43, _bpb.getValue(), EPS);

        // try another input value
        _sourceBlock.setValue(0.0);
        tryBPBupdate();
        assertEquals(2.00, _bpb.getValue(), EPS);

        // go off the bottom edge
        _sourceBlock.setValue(-20.0);
        tryBPBupdate();
        assertEquals(0.00, _bpb.getValue(), EPS);

        // go off the top edge
        _sourceBlock.setValue(200.0);
        tryBPBupdate();
        assertEquals(11.00, _bpb.getValue(), EPS);

    }

    public void testGetBPID() {
        assertEquals("ALPHA1", _bpb.getBPID());
    }

    public void testLength() {
        assertEquals(12, _bpb.length());
    }

    public void testGetBPset() {
        assertEquals(_bps, _bpb.getBPset());
    }

    public void testDescribeSelfFileWriter() {
        try {
            _bpb.describeSelf(_writer);
        } catch (IOException e) {
            assertTrue(false);
        }
        assertEquals("Block \"theBPblock\" has one input (inSig), one output (outSig),"
                + " value [NaN] and is a breakpoint block with 12 breakpoints.",
                _writer.toString());
    }

    protected void tryBPBupdate() {
        try {
            _bpb.update();
        } catch (DAVEException e) {
            fail("Exception in testBlockBPTest.testUpdate() in call to update a BlockBP: "
                    + e.getMessage());
        }
    }
}
