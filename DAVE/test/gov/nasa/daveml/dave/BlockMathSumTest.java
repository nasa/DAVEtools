package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import org.jdom.Element;

import junit.framework.TestCase;

public class BlockMathSumTest extends TestCase {

	protected Model _model;
	protected Signal _outputSignal;
	protected Signal _value1Signal;
	protected Signal _value2Signal;
	protected String _value1SignalID;
	protected String _value2SignalID;
	protected BlockMathConstant _value1Block;
	protected BlockMathConstant _value2Block;
	protected BlockMathSum _block;
	private   StringWriter _writer;
	
	private final double EPS = 0.000001;
	
    @Override
    protected void setUp() throws Exception {
    	    	
       	String routineName = "TestBlockMathProduct::setUp()";
        
    	// don't need input signal - can create const block and signal in one step later
    	_model   = new Model(3,3);
		_writer  = new StringWriter();
		   	
    	// build an product calculation
		//      <apply>
		//        <plus/>
		//        <ci>PB</ci>
		//        <ci>BSPAN</ci>
		//      </apply>             <!-- (PB*BSPAN) -->
		
		// first, build the upstream constant blocks and signals
		_value1Block = new BlockMathConstant( "-3.5", _model );
		_value2Block = new BlockMathConstant( " 2.0", _model );
		_value1SignalID = "PB";
		_value2SignalID = "BSPAN";
		_value1Signal = new Signal("PB",    _value1SignalID, "d_s", 1, _model);
		_value2Signal = new Signal("BSPAN", _value2SignalID, "ft", 1, _model);
		_value1Block.addOutput(_value1Signal);
		_value2Block.addOutput(_value2Signal);
		
		// create downstream signal
		_outputSignal = new Signal("outputSignal", _model);

		// build JDOM from XML snippet
    	Element theValue1 = new Element("ci");	// add numeric constant
    	theValue1.addContent( "PB" );
    	
    	Element theValue2 = new Element("ci");
    	theValue2.addContent( "BSPAN" );

    	Element thePlusElement = new Element("plus");

    	Element applyElement = new Element("apply");
    	applyElement.addContent( thePlusElement );
       	applyElement.addContent( theValue1 );
       	applyElement.addContent( theValue2 );
   	
    	// create product block
		_block     = new BlockMathSum( applyElement, _model );
				
		// hook up inputs to block
		_block.addInput(_value1Signal,1);
		_block.addInput(_value2Signal,2);
		
		// hook up output to block
		_block.addOutput(_outputSignal);
				
		try {
			_model.initialize();
		} catch (DAVEException e) {
			fail("problem initializing model in " + routineName);
		}
    }
    
    public void testGenCcode() {
        _model.setCodeDialect(Model.DT_ANSI_C);
        CodeAndVarNames result = _block.genCode();
        assertEquals("  outputSignal = PB + BSPAN;\n", result.getCode());
        this.checkVarNames(result);
    }

    public void testGenFcode() {
        _model.setCodeDialect(Model.DT_FORTRAN);
        CodeAndVarNames result = _block.genCode();
        assertEquals("       outputSignal = PB + BSPAN\n", result.getCode());
        this.checkVarNames(result);
    }
    
    public void checkVarNames( CodeAndVarNames result ) {
        assertEquals(3, result.getVarNames().size());
        assertEquals("outputSignal", result.getVarName(0));
        assertEquals("PB",           result.getVarName(1));
        assertEquals("BSPAN",        result.getVarName(2));
    }

    public void testDescribeSelfWriter() {
        try {
                _block.describeSelf(_writer);
        } catch (IOException e) {
                fail("testDescribeSelfWriter of TestBlockMathSum threw unexpected exception: " 
                                + e.getMessage() );
        }
        assertEquals( "Block \"plus_3\" has two inputs (PB, BSPAN)," +
                        " one output (outputSignal), value [-1.5] and is a Sum math block.", 
                        _writer.toString() );
    }

    public void testGetValue() {
            assertEquals( -1.5, _block.getValue(), EPS );
    }

    public void testIsReady() {
            assertTrue( _block.isReady() );
    }

    public void testAllInputsReady() {
            assertTrue( _block.allInputsReady() );
    }

    public void testMakeVerbose() {
        assertFalse( _block.isVerbose() );
        _block.makeVerbose();
        assertTrue( _block.isVerbose() );
        _block.silence();
        assertFalse( _block.isVerbose() );
    }

	public void testGetName() {
		assertEquals( "plus_3", _block.getName() );
	}

	public void testGetType() {
		assertEquals( "summing", _block.getType() );
	}

	public void testGetVarID() {
		assertEquals( "PB", _block.getVarID(1) );
		assertEquals( "BSPAN", _block.getVarID(2) );
	}

	public void testGetInputIterator() {
		Signal s;
		Iterator<Signal> it = _block.getInputIterator();
		assertTrue( it.hasNext() );
		s = it.next();
		assertEquals( _value1Signal, s );
		assertTrue( it.hasNext() );
		s = it.next();
		assertEquals( _value2Signal, s );
		assertFalse( it.hasNext() );
	}

	public void testGetOutput() {
		assertEquals( _outputSignal, _block.getOutput() );
	}

	public void testGetVarIDIterator() {
		String name;
		Iterator<String> it = _block.getVarIDIterator();
		assertTrue( it.hasNext() );
		name = it.next();
		assertEquals( "PB", name );
		assertTrue( it.hasNext() );
		name = it.next();
		assertEquals( "BSPAN", name);
		assertFalse( it.hasNext() );
	}

	public void testGetOutputVarID() {
		assertEquals( "outputSignal", _block.getOutputVarID() );
	}

	public void testSetName() {
		_block.setName("fart");
		assertEquals( "fart", _block.getName() );
	}

	public void testNumInputs() {
		assertEquals(2, _block.numInputs() );
	}

	public void testNumVarIDs() {
		assertEquals(2, _block.numVarIDs() );
	}

    public void testUpdate() {
    	String routineName = new String( "TestBlockMathProduct::testUpdate()" );
 
    	// must call this routine before cycling (but can ignore returned object)
    	try {
            _model.getInputVector();
        } catch (DAVEException e) {
            fail("error when trying to obtain VectorInfoArrayList in " 
            		+ routineName + ": " + e.getMessage() );
        }
        
        checkSum( 0.,  0.  );
        checkSum( 0., -1.  );
        checkSum( 0., -1.01);
        checkSum( 0.,  1.01);
        checkSum(-9., -1.  );
    }

    private void checkSum( Double n1, Double n2 ) {
    	String routineName = new String( "TestBlockMathSum" );

    	// set operand values
    	_value1Block.setValue( n1 );
    	_value2Block.setValue( n2 );
    	
		// run model
    	try {
    		_model.cycle();
    	} catch (Exception e) {
			fail("Unexpected exception in " + routineName +
					".checkSum for ["+ n1.toString() + 
					" + " + n2.toString() + "]: " + e.getMessage() );
    	}
    	
    	// check result
    	assertEquals( n1+n2, _block.getValue() );
    }

}
