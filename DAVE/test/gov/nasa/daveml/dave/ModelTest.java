package gov.nasa.daveml.dave;

import junit.framework.*;


/**
 * Tests the Model object
 *
 * 060913 Bruce Jackson, NASA Langley <mailto:bruce.jackson@nasa.gov>
 *
 **/

public class ModelTest extends TestCase {

    Model _simple;

    protected void setUp() {

	_simple = new Model(3,3);

    }

    public void testName() {
	assertTrue( _simple.getName() == "untitled" );
	_simple.setName("test name");
	assertTrue( _simple.getName() == "test name" );
    }

    public void testCycleCounter() {
	assertTrue( _simple.getCycleCounter() == 0 );
	_simple.incrementCycleCounter();
	assertTrue( _simple.getCycleCounter() == 1 );
	_simple.resetCycleCounter();
	assertTrue( _simple.getCycleCounter() == 0 );
    }

    public void testVerboseFlag() {
	assertFalse( _simple.isVerbose() );
	_simple.makeVerbose();
	assertTrue( _simple.isVerbose() );
    }

    public void testAddBlocks() {
	Signal s = new Signal("sig", _simple);
	assertTrue( _simple.getNumInputBlocks() == 0 );
	assertTrue( _simple.getNumOutputBlocks() == 0 );

	/* BlockInput iblk = */ new BlockInput(s, _simple);
	assertTrue( _simple.getNumInputBlocks() == 1 );
	assertTrue( _simple.getNumOutputBlocks() == 0 );

	/* BlockOutput oblk = */ new BlockOutput(s, _simple);
	assertTrue( _simple.getNumInputBlocks() == 1 );
	assertTrue( _simple.getNumOutputBlocks() == 1 );
    }

    public static Test suite() {
	return new TestSuite( ModelTest.class );
    }

    public static void main (String[] args) {
	junit.textui.TestRunner.run(suite());
    }
    
}
