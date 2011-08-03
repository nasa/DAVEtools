package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom.Element;

import junit.framework.TestCase;

public class BlockFuncTableTest extends TestCase {

	protected Model _model;
	protected BreakpointSet _bpAlpha1;
	protected BreakpointSet _bpDe1;
	protected BlockBP _alpha1BpBlock;
	protected BlockBP _de1BpBlock;
	protected Signal _alphaSig;
	protected Signal _deSig;
	protected Signal _alphaIndexOffsetSig;
	protected Signal _deIndexOffsetSig;
	protected Signal _cmtSig;
	protected BlockMathConstant _alphaSourceBlock;
	protected BlockMathConstant _deSourceBlock;
	protected BlockFuncTable _bft;
	private   StringWriter _writer;
	private final Double EPS = 0.000001;

	protected void setUp() throws Exception {
		super.setUp();
		
		_writer        = new StringWriter();
		_model         = new Model(3,3);

		// create a function block from the following XML snippet
		//	  <function name="Basic Cm">
		//	    <description>
		//	      Basic coefficient of pitching-moment as a function of angle of
		//	      attack and elevator
		//	    </description>
		//	    <independentVarRef varID="el" min="-24.0" max="24.0"
		//	                       extrapolate="neither"/>
		//	    <!-- DE breakpoints -->
		//	    <independentVarRef varID="alpha" min="-10.0" max="45.0"
		//	                       extrapolate="neither"/>
		//	    <!-- Alpha breakpoints -->
		//	    <dependentVarRef varID="cmt"/>
		//
		//	    <functionDefn name="Cm0_fn">
		//	      <griddedTableDef name="Cm0_table">
		//	        <breakpointRefs>
		//	          <bpRef bpID="DE1"/>
		//	          <bpRef bpID="ALPHA1"/>
		//	        </breakpointRefs>
		//	        <dataTable>
		//	          <!-- Note: last breakpoint changes most rapidly -->
		//   <!-- DE\ALPHA   -10   -5     0     5    10    15    20    25    30    35    40    45  -->
		//   <!-- ------------------------------------------------------------------------------- -->
		//	 <!-- -24 --> |  .205, .168, .186, .196, .213, .251, .245, .238, .252, .231, .198, .192,
		//	 <!-- -12 --> |  .081, .077, .107, .110, .110, .141, .127, .119, .133, .108, .081, .093,
		//	 <!--   0 --> | -.046,-.020,-.009,-.005,-.006, .010, .006,-.001, .014, .000,-.013, .032,
		//	 <!--  12 --> | -.174,-.145,-.121,-.127,-.129,-.102,-.097,-.113,-.087,-.084,-.069,-.006,
		//	 <!--  24 --> | -.259,-.202,-.184,-.193,-.199,-.150,-.160,-.167,-.104,-.076,-.041,-.005
		//	        </dataTable>
		//	      </griddedTableDef>
		//	    </functionDefn>
		//	  </function>

		_alphaSig      = new Signal("alpha", _model);
		_deSig         = new Signal("el",    _model);
		_cmtSig        = new Signal("cmt", _model);
		
		_alphaSourceBlock = new BlockMathConstant( "12.15", _model);
		_deSourceBlock = new BlockMathConstant( "0.25", _model);
		
		// hook source blocks to input signals
		_alphaSourceBlock.addOutput(_alphaSig);
		_deSourceBlock.addOutput(_deSig);
		
		// create breakpoint sets
		_bpAlpha1 = BreakpointSetTest.generateSampleAlphaBreakpointSet( _model );
		_bpDe1    = BreakpointSetTest.generateSampleElevBreakpointSet( _model );
		
		// create function table & breakpoint blocks

		Element functionElement = generateExampleBlockFuncTableDOM();
 		_bft = new BlockFuncTable( functionElement, _model );
 		
 		// record breakpoint block output signals so we can find upstream BP block
 		_deIndexOffsetSig    = _bft.getInput(0);
 		_alphaIndexOffsetSig = _bft.getInput(1);
 		
 		// record breakpoint blocks for manual updating later
 		_de1BpBlock    = (BlockBP) _deIndexOffsetSig.getSource();
 		_alpha1BpBlock = (BlockBP) _alphaIndexOffsetSig.getSource();
	}

	public void testUpdate() {
		try {
			_model.initialize();
		} catch (DAVEException e1) {
			fail("Model initialize method threw an exception in TestBlockFuncTable unit test: " + e1.getMessage());
		}
		assertTrue( _bft.isReady() );
		try {
			_bft.update();
		} catch (DAVEException e2) {
			fail("Update method threw an exception in TestBlockFuncTable unit test: " + e2.getMessage());
		}
		assertEquals(-0.0015839583, _bft.getValue(), EPS);
		
		//             alpha,  de,   cmt
		// try corners
		checkFuncValue( -10., -24.,  0.205 );
		checkFuncValue(  45., -24.,  0.192 );
		checkFuncValue( -10.,  24., -0.259 );
		checkFuncValue(  45.,  24., -0.005 );
		
		// try mid-value
		checkFuncValue(   0.,   0., -0.009 );

		// build-up to arbitrary
		checkFuncValue(  5.0,   0., -0.005   );
		checkFuncValue( 10.0,   0., -0.006   );
		checkFuncValue(  5.0,  12., -0.127   );
		checkFuncValue( 10.0,  12., -0.129   );
		checkFuncValue(  7.1,   0., -0.00542 );
		checkFuncValue(  7.1,  12., -0.12784 );
		
		// try arbitrary values
		checkFuncValue(  7.1, 5.43, -0.06081505 );
	}
	
	private void checkFuncValue( Double alpha, Double de, Double expectedValue ) {
		_alphaSourceBlock.setValue(alpha);
		_deSourceBlock.setValue(de);
		try {
			_alpha1BpBlock.update();
		} catch (DAVEException e1) {
			fail("alpha BreakpointBlock.update() method threw an exception in TestBlockFuncTable unit test: " + e1.getMessage());
		}
		try {
			_de1BpBlock.update();
		} catch (DAVEException e2) {
			fail("de BreakpointBlock.update() method threw an exception in TestBlockFuncTable unit test: " + e2.getMessage());
		}
		try {
			_bft.update();
		} catch (DAVEException e3) {
			fail("Update method threw an exception in TestBlockFuncTable unit test: " + e3.getMessage());
		}
		assertEquals(expectedValue, _bft.getValue(), EPS);
	}

	public void testBlockFuncTableElementModel() {
		// this constructor is called in setUp()
		assertNotNull( _bft );
		assertEquals(    2, _bft.numInputs() );
		assertEquals("cmt", _bft.getOutputVarID() );
	}

	public void testBlockFuncTableBlockFuncTable() {
		// TODO
	}

	public void testGetFunctionTableDef() {
		FuncTable gft = _bft.getFunctionTableDef();
		assertNotNull( gft );
		// TODO - more tests
	}

	public void testParseSimpleFunction() {
		// TODO 
	}

	public void testPrintTableWriter() {
		StringWriter writer = new StringWriter();
		StringBuffer buffer;
		String substr;
		assertNotNull( writer );
		try {
			_bft.printTable(writer);
			buffer = writer.getBuffer();
			assertEquals( 453, buffer.length() );
			
			substr = buffer.substring(0,85);
			assertNotNull( substr );
			assertEquals("0.205, 0.168, 0.186, 0.196, 0.213, 0.251, 0.245, 0.238, 0.252, 0.231, 0.198, 0.192, \n", substr );

			substr = buffer.substring(85,168);
			assertNotNull( substr );
			assertEquals("0.081, 0.077, 0.107, 0.11, 0.11, 0.141, 0.127, 0.119, 0.133, 0.108, 0.081, 0.093, \n", substr );

			substr = buffer.substring(168,261);
			assertNotNull( substr );
			assertEquals("-0.046, -0.02, -0.0090, -0.0050, -0.0060, 0.01, 0.0060, -0.0010, 0.014, 0.0, -0.013, 0.032, \n", substr );

			substr = buffer.substring(261,359);
			assertNotNull( substr );
			assertEquals("-0.174, -0.145, -0.121, -0.127, -0.129, -0.102, -0.097, -0.113, -0.087, -0.084, -0.069, -0.0060, \n", substr ); 

			substr = buffer.substring(359,453);
			assertNotNull( substr );
			assertEquals("-0.259, -0.202, -0.184, -0.193, -0.199, -0.15, -0.16, -0.167, -0.104, -0.076, -0.041, -0.0050\n", substr ); 

		} catch (IOException e) {
			fail("Unexpected exception in testPrintTable(): " 
					+ e.getMessage());
		}

	}

	public void testDescribeSelf() {
		try {
			_bft.describeSelf(_writer);
		} catch (IOException e) {
			fail("Exception in testDescribeSelf() unit test of TestBlockFuncTable: " + e.getMessage() );
		}
		String str = _writer.toString();
		assertNotNull( str );
		assertEquals( 141, str.length() );
		assertEquals( "Block \"Cm0_fn\" has two inputs (el_by_DE1, alpha_by_ALPHA1), " +
				"one output (cmt), value [NaN] and is a function table block with 60 table points.", str );
	}
	
	public static Element generateExampleBlockFuncTableDOM() {
		// create a function block from the following XML snippet
		//	  <function name="Basic Cm">
		//	    <description>
		//	      Basic coefficient of pitching-moment as a function of angle of
		//	      attack and elevator
		//	    </description>
		//	    <independentVarRef varID="el" min="-24.0" max="24.0"
		//	                       extrapolate="neither"/>
		//	    <!-- DE breakpoints -->
		//	    <independentVarRef varID="alpha" min="-10.0" max="45.0"
		//	                       extrapolate="neither"/>
		//	    <!-- Alpha breakpoints -->
		//	    <dependentVarRef varID="cmt"/>
		//
		//	    <functionDefn name="Cm0_fn">
		//	      <griddedTableDef name="Cm0_table">
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
		//	    </functionDefn>
		//	  </function>
		
    	Element griddedTableDef = FuncTableTest.generateSampleGriddedTableDefDOM();
    	
    	Element funcDefn = new Element("functionDefn");
    	funcDefn.setAttribute("name", "Cm0_fn");
    	funcDefn.addContent(griddedTableDef);
    	
    	Element iVarRef1 = new Element("independentVarRef");
    	iVarRef1.setAttribute("varID", "el");
    	iVarRef1.setAttribute("min", "-24.0");
    	iVarRef1.setAttribute("max", "24.0");
    	iVarRef1.setAttribute("extrapolate", "neither");

    	Element iVarRef2 = new Element("independentVarRef");
    	iVarRef2.setAttribute("varID", "alpha");
    	iVarRef2.setAttribute("min", "-10.0");
    	iVarRef2.setAttribute("max", " 45.0");
    	iVarRef2.setAttribute("extrapolate", "neither");
    	
    	Element dVarRef = new Element("dependentVarRef");
    	dVarRef.setAttribute("varID", "cmt");
    	
	   	Element descriptionElement = new Element("description");
	   	descriptionElement.addContent("Basic coefficient of pitching-moment as a function of angle of " +
			      "attack and elevator");
		
	   	Element functionElement = new Element("function");
	   	functionElement.setAttribute("name", "Basic Cm");
	   	functionElement.addContent(descriptionElement);
	   	functionElement.addContent(iVarRef1);
	   	functionElement.addContent(iVarRef2);
	   	functionElement.addContent(dVarRef);
	   	functionElement.addContent(funcDefn);
	   	
	   	return functionElement;
	}

}
