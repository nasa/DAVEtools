package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;

public class BlockOutputTest extends TestCase {
	
	private Model _model;
	private String _signalID;
	private Signal _signal;
	private BlockMathConstant _constBlock;
	private BlockOutput _block;
	
	protected StringWriter _writer;
	
	private final double EPS = 0.00000001;

	protected void setUp() throws Exception {
		super.setUp();
		
		_writer = new StringWriter();

		_model = new Model(4,4);
		_signalID = new String("outputValue");
		_signal = new Signal(_signalID, _model);
		_constBlock = new BlockMathConstant("3.45", _model);
		_constBlock.addOutput(_signal);
		assertNotNull(_constBlock);
		_model.hookUpIO();
		assertTrue(_signal.hasDest() );
		BlockArrayList destBlocks = _signal.getDests();
		assertNotNull ( destBlocks );
		assertEquals( 1, destBlocks.size() );
		Block destBlock = destBlocks.get(0);
		assertNotNull( destBlock );
		assertEquals( "output", destBlock.getType() );
		_block = (BlockOutput) destBlock;
		assertEquals( Double.NaN, _block.getValue() );
		try {
			_model.initialize();
		} catch (DAVEException e) {
			fail("Unexpected exception in TestBlockOutput.testGetValue() wen calling model.initialize(): " 
					+ e.getMessage());
		}
	}

	public void testGetValue() {
		assertEquals( 3.45, _block.getValue(), EPS );
	}

	public void testUpdate() {
		try {
			_model.getInputVector();
		} catch (DAVEException e) {
			fail("Unexpected exception in TestBlockOutput.testGetValue() wen calling model.getInputVector(): " 
					+ e.getMessage());
		}
		try {
			_model.cycle();
		} catch (DAVEException e) {
			fail("Unexpected exception in TestBlockOutput.testGetValue() wen calling model.cycle(): " 
					+ e.getMessage());
		}
		assertEquals( 3.45, _block.getValue(), EPS );
		_constBlock.setValue("-999.345");
		try {
			_model.cycle();
		} catch (DAVEException e) {
			fail("Unexpected exception in TestBlockOutput.testGetValue() wen calling model.cycle(): " 
					+ e.getMessage());
			assertEquals( -999.345, _block.getValue(), EPS );
		}
	}
        
        public void testGenCcode() {
            String code = _block.genCcode();
            assertEquals("// outputValue is a model output with units 'unkn'\n", code);
        }

        public void testGenFcode() {
            String code = _block.genFcode();
            assertEquals("* outputValue is a model output with units 'unkn'\n", code);
        }

	public void testGetUnits() {
		assertEquals("unkn", _block.getUnits() );
	}

	public void testGetSeqNumber() {
		assertEquals(1, _block.getSeqNumber() );
	}

	public void testDescribeSelfWriter() {
		try {
			_block.describeSelf(_writer);
		} catch (IOException e) {
			fail("testDescribeSelfWriter of TestBlockMathSum threw unexpected exception: " 
					+ e.getMessage() );
		}
		assertEquals( "Block \"outputValue\" has one input (outputValue)," +
				" NO OUTPUTS, value [3.45] (unkn) and is an output block.", 
				_writer.toString() );
	}

}
