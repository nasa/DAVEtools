package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.*;


/**
 * A Constant Block test routine
 *
 * 081217 Bruce Jackson <mailto:bruce.jackson@nasa.gov>
 *
 **/

public class BlockMathConstantTest extends TestCase {

    protected Model _model;
    protected BlockMathConstant _block;
    protected Signal _outSig;
    protected StringWriter _writer;

    @Override
    protected void setUp() {
    	_model = new Model();
        _outSig = new Signal("constantOutput", _model);
    	_block = new BlockMathConstant( "-3.45", _model);
        try {
            _block.addOutput(_outSig);
        } catch (DAVEException ex) {
            Logger.getLogger(BlockMathConstantTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    	_writer = new StringWriter();
    }

    public void testGetValue() {
	assertTrue( _block.getValue() == -3.45 );
   }
    
    public void testSetValueString() {
    	_block.setValue("4.56");
    	assertEquals( 4.56, _block.getValue() );
    }

    public void testSetValueDouble() {
    	_block.setValue(-5.67);
    	assertEquals( -5.67, _block.getValue() );
    }

    public void testUpdate() {
        _block.setValue( 4.56 );
        try {
                _model.getInputVector();
        } catch (DAVEException e1) {
                fail("error when trying to obtain VectorInfoArrayList in TestBlockMathAbs::testUpdate()");
        }
        try {
                _model.cycle();
        } catch (DAVEException e) {
                fail("error when trying to cycle model in TestBlockMathAbs::testUpdate()");
        }
        assertEquals( 4.56, _block.getValue(), 0.000001 );
    }

    public void testGenCcode() {
        _model.setCodeDialect(Model.DT_ANSI_C);
        CodeAndVarNames result = _block.genCode();
        assertEquals("  constantOutput = -3.45;\n", result.getCode());
        assertEquals(1, result.getVarNames().size());
        assertEquals("constantOutput", result.getVarName(0));
    }

    public void testGenFcode() {
        _model.setCodeDialect(Model.DT_FORTRAN);
        CodeAndVarNames result = _block.genCode();
        assertEquals("       constantOutput = -3.45\n", result.getCode());
        assertEquals(1, result.getVarNames().size());
        assertEquals("constantOutput", result.getVarName(0));
    }

    public void testDescribeSelf() {
            try {
                    _block.describeSelf(_writer);
            } catch (IOException e) {
                    assertTrue(false);
            }
            assertEquals( "Block \"const_-3.45_\" has NO INPUTS, one output (constantOutput)," +
                            " value [-3.45] and is a Constant Value math block.", 
                            _writer.toString() );
    }

    public void testIsReady() {
            assertTrue(_block.isReady() );
    }

    public void testAllInputsReady() {
            assertTrue(_block.allInputsReady() );
    }

    public void testMakeVerbose() {
            assertFalse( _block.isVerbose() );
            _block.makeVerbose();
            assertTrue( _block.isVerbose() );
            _block.silence();
            assertFalse( _block.isVerbose() );
    }

    public void testGetModel() {
            assertEquals( _model.getName(), _block.getModel().getName() );
    }

    public void testGetSetName() {
            assertEquals( "const_-3.45_", _block.getName() );
            _block.setName("fart");
            assertEquals( "fart", _block.getName() );
    }

    public void testGetType() {
            assertEquals( "constant value", _block.getType() );
    }

    public void testGetVarIDIterator() {
            Iterator<String> it = _block.getVarIDIterator();
            assertFalse(it.hasNext());
    }

    public void testGetOutput() {
            Signal s = _block.getOutput();
            assertEquals( _outSig, s );
    }

    public void getGetInputIterator() {
            Iterator<Signal> it = _block.getInputIterator();
            assertFalse(it.hasNext());
    }

    public void testGetOutputVarID() {
            assertEquals( _outSig.getVarID(), _block.getOutputVarID() );
    }

    public void testNumInputs() {
            assertEquals(0, _block.numInputs() );
    }

    public void testNumVarIDs() {
            assertEquals(0, _block.numVarIDs() );
    }

    public static Test suite() {
        return new TestSuite( BlockMathConstantTest.class );
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
}
