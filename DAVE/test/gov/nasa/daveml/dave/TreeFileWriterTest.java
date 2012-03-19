package gov.nasa.daveml.dave;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

public class TreeFileWriterTest extends TestCase {
	
	Model _m;
	TreeFileWriter _w;
	String _fileName;

	protected void setUp() throws Exception {
		super.setUp();
		_m = genSampleModel();
		_fileName = "tempFile.txt";
		_w = new TreeFileWriter( _fileName );
	}

	public void testTreeFileWriter() {
		assertNotNull( _w );
	}

	public void testDescribe() {
		try {
			_w.describe(_m);
		} catch (IOException e) {
			fail("Unexpected exception thrown in describe(): " + e.getMessage() );
		}
		
		// close and reopen file
		try {
			_w.close();
		} catch (IOException e) {
			fail("Unexpected exception thrown in close(): " + e.getMessage() );
		}
		
		FileReader f = null;
		try {
			f = new FileReader(_fileName);
		} catch (FileNotFoundException e) {
			fail("Unexpected exception thrown in creating FileReader: " + e.getMessage() );
		}
		
		assertNotNull(f);
		char[] buf = new char[1000];
		try {
			f.read(buf);
		} catch (IOException e) {
			fail("Unexpected exception thrown in reading file: " + e.getMessage() );
		}
		String contents = new String(buf);
		int end = contents.lastIndexOf('\n');  // funny chars at end of string
		String expectedMessage = 
			"Contents of model:\n" +
			"\n" +
			"Number of signals: 1\n" +
			"\n" +
			"1 Signal \"theSig\" (unkn) [theSig] connects outport 1 of block const_3.4_ " +
			"to inport 1 of block theSig.\n" +
			"\n" +
			"Number of blocks: 2\n" +
			"\n" +
			"1 Block \"const_3.4_\" has NO INPUTS, one output (theSig), value [3.4] " +
			"and is a Constant Value math block.\n" +
			"2 Block \"theSig\" has one input (theSig), NO OUTPUTS, value [NaN] (unkn) " +
			"and is an output block.";
		
		assertEquals( expectedMessage, contents.substring(0, end ));
	}
	
	protected Model genSampleModel() {
		Model m = new Model(3,3);
		Block constBlk = new BlockMathConstant( "3.4", m );
		Signal sig = new Signal("theSig", m);
		try {
			constBlk.addOutput(sig);
		} catch (DAVEException e) {
			fail("Unexpected exception thrown in adding signal to const block in genSampleModel(): " 
					+ e.getMessage() );
		}
		new BlockOutput(sig, m);
		return m;
	}

}
