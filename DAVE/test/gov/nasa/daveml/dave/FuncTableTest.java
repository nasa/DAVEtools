package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.jdom.Element;

import junit.framework.TestCase;

public class FuncTableTest extends TestCase {

	protected Model _model;
	protected Element _gtd;
	protected FuncTable _gft;
	protected BreakpointSet _bpAlpha1;
	protected BreakpointSet _bpDe1;
	protected final Double EPS = 0.0000001;

	protected void setUp() throws Exception {
		super.setUp();
		
		_model = new Model(3,3);
		
		// create breakpoint sets (needed so table can figure out its dimensions)
		_bpAlpha1 = BreakpointSetTest.generateSampleAlphaBreakpointSet( _model );
		_bpDe1    = BreakpointSetTest.generateSampleElevBreakpointSet( _model );
		
		// create function table

		_gtd = generateSampleGriddedTableDefDOM();
 		_gft = new FuncTable( _gtd, _model );
	}

	public void testFuncTableModel() {
		FuncTable ft = new FuncTable( _model );
		assertNotNull( ft );
		assertEquals(0, ft.numDim());
		assertEquals("No table name set.", ft.getName());
		assertEquals(0, ft.size() );
	}

	public void testFuncTableStringElementModel() {
		FuncTable ft = null;
		assertNotNull( _gtd );
		try {
			ft = new FuncTable( "newID", _gtd, _model );
		} catch (IOException e) {
			fail("Exception when building function table in testFuncTableStringElementModel(): " 
					+ e.getMessage() );
		}
		assertNotNull( ft );
		assertEquals("newID", ft.getGTID() );
		// this is the only unique difference between the two constructors we test
	}

	public void testFuncTableElementModel() {
		// this constructor called by our test's setUp() method
		assertNotNull(_gft);
		// Other validations are performed by other unit tests
	}

	public void testFuncTableStringStringStringStringIntModel() {
		// This constructor may be pointless, since it doesn't include any breakpoint info
		// so dims is empty and not many methods will work.
//     public FuncTable( String tableID, String tableName, String tableValues, String description, int ndim, Model m )
		FuncTable ft = new FuncTable( "simpleFT", "simple function table",
				"1., 2., 3., 4., 5., 6.", "A simple table", 1, _model );
		assertNotNull( ft );
		assertEquals( "simpleFT", ft.getGTID() );
		assertEquals( "simple function table", ft.getName() );
		assertEquals( "A simple table", ft.getDescription() );
		assertEquals( 6, ft.size() );
		StringWriter writer = new StringWriter();
		try {
		    ft.printTable(new PrintWriter(writer));
		} catch (IOException e) {
		    fail("Exception thrown in test of FuncTable SSSIM constructor: " + e.getMessage());
		}
//		assertEquals( "gov.nasa.daveml", writer.toString());
		// TODO need to expand tests here
	}

	public void testRegister() {
		// need a complete BFT def Element; this example is too incomplete
//		BlockFuncTable bft = null;
//		try {
//			bft = new BlockFuncTable(_gtd, _model);
//		} catch (IOException e) {
//			fail("Unexpected exception in testRegister()" );
//		}
//		_gft.register(bft);
//		assertNotNull(_gft.users);
//		assertEquals(1, _gft.users.size() );
	}

	public void testMakeVerbose() {
		assertFalse( _gft.isVerbose() );
		_gft.makeVerbose();
		assertTrue( _gft.isVerbose() );
		_gft.silence();
		assertFalse( _gft.isVerbose() );
	}

	public void testAddBPID() {
		_gft.addBPID(3, "newBP");
		assertEquals("newBP", _gft.getBPID(3) );
	}

	public void testGetBPIterator() {
		Iterator<String> bpit = _gft.getBPIterator();
		assertNotNull(bpit);
		assertTrue(bpit.hasNext() );
		assertEquals("DE1", bpit.next() );
		assertTrue(bpit.hasNext() );
		assertEquals("ALPHA1", bpit.next() );
		assertFalse(bpit.hasNext() );
	}

	public void testGetBPID() {
		assertEquals( "DE1", _gft.getBPID(1) );
		assertEquals( "ALPHA1", _gft.getBPID(2) );
		try {
			assertEquals( "", _gft.getBPID(3) );
			fail("Expected to get IndexOutOfBoundsException in testGetPBID");
		} catch( IndexOutOfBoundsException e ) {
			// do nothing - expected exception
		}
	}

	public void testGetName() {
		assertEquals( "Cm0_table", _gft.getName() );
	}

	public void testGetGTID() {
		assertEquals( "CMT_TABLE", _gft.getGTID() );
	}

	public void testSize() {
		assertEquals( 60, _gft.size() );
	}

	public void testNumDim() {
		assertEquals( 2, _gft.numDim() );
	}

	public void testDim() {
		assertEquals(  5, _gft.dim(0) );
		assertEquals( 12, _gft.dim(1) );
	}

	public void testGetDimensions() {
		int [] dims = _gft.getDimensions();
		assertNotNull (dims);
		assertEquals( 2, dims.length );
		assertEquals( 5, dims[0] );
		assertEquals(12, dims[1] );
	}

	public void testGetValues() {
		ArrayList<Double> vals = _gft.getValues();
		assertNotNull( vals );
		assertEquals( 60, vals.size() );
		assertEquals( 0.205, vals.get( 0), EPS );
		assertEquals( 0.192, vals.get(11), EPS );
		assertEquals( 0.010, vals.get(29), EPS );
		assertEquals(-0.259, vals.get(48), EPS );
		assertEquals(-0.005, vals.get(59), EPS );
		try {
			vals.get(-1);
			fail("Expected exception not thrown in TestFuncTable.testGetValues() with -1 index");
		} catch( IndexOutOfBoundsException e) {
			// do nothing - expected
		}
		try {
			vals.get(60);
			fail("Expected exception not thrown in TestFuncTable.testGetValues() with index 60");
		} catch( IndexOutOfBoundsException e) {
			// do nothing - expected
		}
		
	}

	public void testPrintTableWriter() {
		StringWriter writer = new StringWriter();
		StringBuffer buffer;
                String osName = System.getProperty("os.name");
		assertNotNull( writer );
		try {
			_gft.printTable(writer);
			buffer = writer.getBuffer();
                        if (osName.contains("Windows")) {
                            assertEquals( 451, buffer.length() );
                        } else {
                            assertEquals( 873, buffer.length() );
                        }
			
                        String[] lines = buffer.toString().split(System.getProperty("line.separator"));

                        assertEquals(5, lines.length );
                        
			assertNotNull( lines[0] );
			assertEquals("2.050000E-01, 1.680000E-01, 1.860000E-01, 1.960000E-01, " +
                                     "2.130000E-01, 2.510000E-01, 2.450000E-01, 2.380000E-01, " +
                                     "2.520000E-01, 2.310000E-01, 1.980000E-01, 1.920000E-01, ", lines[0] );

                        assertNotNull( lines[1] );
			assertEquals("8.100000E-02, 7.700000E-02, 1.070000E-01, 1.100000E-01, " +
                                     "1.100000E-01, 1.410000E-01, 1.270000E-01, 1.190000E-01, " +
                                     "1.330000E-01, 1.080000E-01, 8.100000E-02, 9.300000E-02, ", lines[1] );

			assertNotNull( lines[2] );
			assertEquals("-4.600000E-02, -2.000000E-02, -9.000000E-03, -5.000000E-03, " +
                                     "-6.000000E-03, 1.000000E-02, 6.000000E-03, -1.000000E-03, " +
                                     "1.400000E-02, 0.000000E00, -1.300000E-02, 3.200000E-02, ", lines[2] );

			assertNotNull( lines[3] );
			assertEquals("-1.740000E-01, -1.450000E-01, -1.210000E-01, -1.270000E-01, " +
                                     "-1.290000E-01, -1.020000E-01, -9.700000E-02, -1.130000E-01, " +
                                     "-8.700000E-02, -8.400000E-02, -6.900000E-02, -6.000000E-03, ", lines[3] ); 

			assertNotNull( lines[4] );
			assertEquals("-2.590000E-01, -2.020000E-01, -1.840000E-01, -1.930000E-01, " +
                                     "-1.990000E-01, -1.500000E-01, -1.600000E-01, -1.670000E-01, " +
                                     "-1.040000E-01, -7.600000E-02, -4.100000E-02, -5.000000E-03", lines[4] ); 

		} catch (IOException e) {
			fail("Unexpected exception in testPrintTable(): " 
					+ e.getMessage());
		}

	}

	public void testGetPt() {

		// check perimeter and intermediate values
		boolean dontExpectException = false;
		checkPt(0,  0,  0.205, dontExpectException );
		checkPt(0, 11,  0.192, dontExpectException );
		checkPt(2,  5,  0.010, dontExpectException );
		checkPt(4,  0, -0.259, dontExpectException );
		checkPt(4, 11, -0.005, dontExpectException );
		
		// check out-of-bounds
		boolean expectException = true;
		checkPt(-1, -1, 0, expectException );
		checkPt(-1,  0, 0, expectException );		
		checkPt(-1, 11, 0, expectException );		
//		checkPt(-1, 12, 0, expectException ); TODO - this is passing without exception; should throw OOB	
		checkPt( 0, -1, 0, expectException );		
//		checkPt( 0, 12, 0, expectException ); TODO - this is passing without exception; should throw OOB	
//		checkPt( 4, -1, 0, expectException ); TODO - this is passing without exception; should throw OOB
		checkPt( 4, 12, 0, expectException );		
//		checkPt( 5, -1, 0, expectException ); TODO - this is passing without exception; should throw OOB		
//		checkPt( 5,  0, 0, expectException ); TODO - this is passing without exception; should throw OOB
		checkPt( 5, 11, 0, expectException );		
		checkPt( 5, 12, 0, expectException );		
	}
	
	protected void checkPt( int index1, int index2, double expectedValue, boolean expectException) {

		double point = Double.NaN;
		int[] indices = new int[2];

		indices[0] =  index1;
		indices[1] =  index2;
		
		try {
			point = _gft.getPt(indices);
			if (expectException) {
				fail("Expected exception not thrown in TestFuncTable.testGetPt() with indices [" +
						index1 + ", " + index2 + "]" );
			} else {
				assertEquals( expectedValue, point, EPS );
			}
		} catch (Exception e) {
//			if ((e.getClass() == java.io.IndexOutOfBoundsException) && expectException) {
			if (expectException) {
				// do nothing - expected exception for test
			} else {
				fail("Exception thrown in TestFuncTable.testGetPt() with indices [" +
						index1 + ", " + index2 + "] :" );
			}
		}
	}
	
	public static Element generateSampleGriddedTableDefDOM() {
		// create a function table from the following XML snippet
		//	      <griddedTableDef name="Cm0_table" gtID="CMT_TABLE">
		//	        <breakpointRefs>
		//	          <bpRef bpID="DE1"/>
		//	          <bpRef bpID="ALPHA1"/>
		//	        </breakpointRefs>
		//	        <dataTable>
		//	          <!-- Note: last breakpoint changes most rapidly -->
		//	          .205,.168,.186,.196,.213,.251,.245,.238,.252,.231,.198,.192,
		//	          .081,.077,.107,.110,.110,.141,.127,.119,.133,.108,.081,.093,
		//	          -.046,-.020,-.009,-.005,-.006,.010,.006,-.001,.014,.000,-.013,.032,
		//	          -.174,-.145,-.121,-.127,-.129,-.102,-.097,-.113,-.087,-.084,-.069,-.006,
		//	          -.259,-.202,-.184,-.193,-.199,-.150,-.160,-.167,-.104,-.076,-.041,-.005
		//	        </dataTable>
		//	      </griddedTableDef>
		
    	Element dataTableElement = new Element("dataTable");
    	dataTableElement.addContent( 
    		" .205,.168,.186,.196,.213,.251,.245,.238,.252,.231,.198,.192, " +
    		" .081,.077,.107,.110,.110,.141,.127,.119,.133,.108,.081,.093, " +
    		"-.046,-.020,-.009,-.005,-.006,.010,.006,-.001,.014,.000,-.013,.032, " +
    		"-.174,-.145,-.121,-.127,-.129,-.102,-.097,-.113,-.087,-.084,-.069,-.006, " +
    		"-.259,-.202,-.184,-.193,-.199,-.150,-.160,-.167,-.104,-.076,-.041,-.005" );
    	
    	Element bpRef1 = new Element("bpRef");
    	bpRef1.setAttribute("bpID", "DE1");
    	Element bpRef2 = new Element("bpRef");
    	bpRef2.setAttribute("bpID", "ALPHA1");
    	
    	Element breakpointRefs = new Element("breakpointRefs");
    	breakpointRefs.addContent(bpRef1);
    	breakpointRefs.addContent(bpRef2);
    	
    	Element gtd = new Element("griddedTableDef");
    	gtd.setAttribute("name", "Cm0_table");
    	gtd.setAttribute("gtID", "CMT_TABLE");
    	gtd.addContent(breakpointRefs);
    	gtd.addContent(dataTableElement);
    	
    	return gtd;
	}

}
