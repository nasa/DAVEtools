package gov.nasa.daveml.dave;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.jdom.Element;

import junit.framework.TestCase;

public class BlockMathFunctionTest extends TestCase {
	
	protected Model _model;
	protected StringWriter _writer;
	protected BlockMathFunction _block;
	
	protected final double EPS = 0.00000001;
	
	protected final double COS_0p5 = 0.87758256;
	protected final double SIN_0p5 = 0.47942553;
	protected final double TAN_0p5 = 0.54630249;

	protected void setUp() throws Exception {
		super.setUp();
		_model  = new Model(1,1);
		_writer = new StringWriter();
		instantiateFunction( "cos", "cosine function", 0.5, false );
	}
	
	public void testBlockMathFunction_ctor() {
		
		instantiateFunction( "cos", "cosine function",  0.5, false ); assertEquals( COS_0p5, _block.getValue(), EPS );
		instantiateFunction( "sin", "sine function",    0.5, false ); assertEquals( SIN_0p5, _block.getValue(), EPS );
		instantiateFunction( "tan", "tangent function", 0.5, false ); assertEquals( TAN_0p5, _block.getValue(), EPS );

		instantiateFunction( "cos", "cosine function", -0.5, false ); assertEquals( COS_0p5, _block.getValue(), EPS );
		instantiateFunction( "sin", "sine function",   -0.5, false ); assertEquals(-SIN_0p5, _block.getValue(), EPS );
		instantiateFunction( "tan", "tangent function",-0.5, false ); assertEquals(-TAN_0p5, _block.getValue(), EPS );

		instantiateFunction( "arccos", "arccosine function",  COS_0p5, false ); assertEquals( 0.5, _block.getValue(), EPS );
		instantiateFunction( "arcsin", "arcsine function",    SIN_0p5, false ); assertEquals( 0.5, _block.getValue(), EPS );
		instantiateFunction( "arctan", "arctangent function", TAN_0p5, false ); assertEquals( 0.5, _block.getValue(), EPS );

		instantiateFunction( "arccos", "arccosine function", -COS_0p5, false ); assertEquals( Math.PI-0.5, _block.getValue(), EPS );
		instantiateFunction( "arcsin", "arcsine function",   -SIN_0p5, false ); assertEquals(-0.5, _block.getValue(), EPS );
		instantiateFunction( "arctan", "arctangent function",-TAN_0p5, false ); assertEquals(-0.5, _block.getValue(), EPS );

		instantiateFunction( "power", "power function", 2, 4, false ); assertEquals(16.0, _block.getValue(), EPS );
		instantiateFunction( "power", "power function", 16, 1./4., false ); assertEquals(2.0, _block.getValue(), EPS );

		// should generate exception - power needs to be spelled out
		instantiateFunction( "pow", "power function", 2, 4, true );
	}
	
	public void testUpdate() {
		updateExistingBlock( "cos",  0.5,  COS_0p5 );
		updateExistingBlock( "sin",  0.5,  SIN_0p5 );
		updateExistingBlock( "tan",  0.5,  TAN_0p5 );
		
		updateExistingBlock( "cos",  -0.5,  COS_0p5 );
		updateExistingBlock( "sin",  -0.5, -SIN_0p5 );
		updateExistingBlock( "tan",  -0.5, -TAN_0p5 );
		
		updateExistingBlock( "arccos",  COS_0p5, 0.5 );
		updateExistingBlock( "arcsin",  SIN_0p5, 0.5 );
		updateExistingBlock( "arctan",  TAN_0p5, 0.5 );
		
		updateExistingBlock( "arccos", -COS_0p5, Math.PI-0.5 );
		updateExistingBlock( "arcsin", -SIN_0p5, -0.5 );
		updateExistingBlock( "arctan", -TAN_0p5, -0.5 );
		
		// can't update existing model to add second argument to power function

	}

	public void updateExistingBlock( String func, double arg, double expectedValue) {
		BlockMathConstant constBlk = getUpstreamConstBlk1();
		constBlk.setValue( arg );
		try {
			_block.setFunction( func );
		} catch (DAVEException e) {
			fail("Unexpected exception when calling setFunction() for BlockMathFunction block: "
					+ e.getMessage() );
		}
		try {
			_block.update();
		} catch (DAVEException e) {
			fail("Unexpected exception when calling update() for BlockMathFunction block: "
					+ e.getMessage() );
		}		
		assertEquals(expectedValue, _block.getValue(), EPS );
	}

	protected BlockMathConstant getUpstreamConstBlk(int index) {
		Signal s = _block.getInput(index-1);
		assertNotNull( s );
		Block sourceBlk = s.getSource();
		assertNotNull( sourceBlk );
		assertEquals( "constant value", sourceBlk.getType() );
		return (BlockMathConstant) sourceBlk;
	}
	
	protected BlockMathConstant getUpstreamConstBlk1() {
		return this.getUpstreamConstBlk(1);
	}

	protected BlockMathConstant getUpstreamConstBlk2() {
		return this.getUpstreamConstBlk(2);
	}

	public void testDescribeSelfWriter() {
		try {
			_block.describeSelf(_writer);
		} catch (IOException e) {
			fail("testDescribeSelfWriter of TestBlockMathSum threw unexpected exception: " 
					+ e.getMessage() );
		}
		assertEquals( "Block \"cos_1\" has one input (const_0.5_)," +
				" NO OUTPUTS, value [0.8775825618903728] and is a Function math block.", 
				_writer.toString() );
	}

	protected void instantiateFunction( 
			String func, String name, double argument, boolean expectException ) {
		ArrayList<Double> args = new ArrayList<Double>();
		args.add(argument);
		instantiateFunction( func, name, args, expectException );
	}
	
	protected void instantiateFunction( 
			String func, String name, double argument1, double argument2, boolean expectException) {
		ArrayList<Double> args = new ArrayList<Double>();
		args.add(argument1);
		args.add(argument2);
		instantiateFunction( func, name, args, expectException );
	}
	
	protected void instantiateFunction( 
			String func, String name, ArrayList<Double> args, boolean expectException ) {
		instantiateFunction( func, name, null, null, null, null, null, args, expectException );
	}

	protected void instantiateFunction( 
			String func, String name, 
			String funcAttributeName1, String funcAttributeValue1,
			String funcAttributeName2, String funcAttributeValue2,
			String extendedFunctionName,
			ArrayList<Double> args, boolean expectException ) {

		Element argElement;

		// build a JDOM doclet like this:
		//
		//  <apply>
		//    <funcName/>
		//    <cn>arg1</cn>
		//    ...
		//    <cn>argN</cn>
		//  </apply>
		
		Element applyElement = new Element( "apply" );
		Element funcElement  = new Element( func );
		applyElement.addContent( funcElement );
		
		// if we have valid function attributes, apply them
		if (funcAttributeName1 != null) {
			funcElement.setAttribute(funcAttributeName1, funcAttributeValue1);
			if (funcAttributeName2 != null) {
				funcElement.setAttribute(funcAttributeName2, funcAttributeValue2);
			}
			if (extendedFunctionName != null) {
				funcElement.addContent(extendedFunctionName);
			}
		}
		
		Iterator<Double> it = args.iterator();
		while (it.hasNext() ) {
			argElement = new Element( "cn" );
			argElement.addContent( it.next().toString() );
			applyElement.addContent( argElement );
		}
		
		_block = null;
		try {
			if (func == "csymbol") {
				_block = new BlockMathFunctionExtension( applyElement, _model );
			} else {
				_block = new BlockMathFunction( applyElement, _model );
			}
			if (expectException)
				fail("Expected exception didn't happen in call to BlockMathFunction constructor");
		} catch (DAVEException e) {
			if (expectException) {
				// do nothing - expected
			} else 
				fail("Unexpected exception in call to BlockMathFunction from instantiateFunction: "
						+ e.getMessage() );	
		}
		
		if (!expectException) { // proceed with more tests if no expected exceptions
			assertNotNull(_block);
			assertEquals( name, _block.getType() );
			if (extendedFunctionName == null)
				assertEquals( func, _block.getFuncType() );
			else
				assertEquals( extendedFunctionName, _block.getFuncType() );
			assertEquals( Double.NaN, _block.getValue() );
			try {
				_model.initialize();
			} catch (DAVEException e) {
				fail("Unexpected exception in TestBlockMathFunction.instantiateFunction() " +
						"when calling model.initialize(): " 
						+ e.getMessage());
			}
		}
	}
}