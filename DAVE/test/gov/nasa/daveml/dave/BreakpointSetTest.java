package gov.nasa.daveml.dave;

import java.util.ArrayList;

import org.jdom.Element;

import junit.framework.TestCase;

public class BreakpointSetTest extends TestCase {
	
	protected Model _model;
	protected BreakpointSet _bps;
	protected BlockBP _bpb;
	
	private final Double EPS = 0.000001;

	protected void setUp() throws Exception {
		super.setUp();
		_model = new Model(3,3);
	}

	public void testBreakpointSetModel() {
		_bps = new BreakpointSet( _model );
		assertEquals( "", _bps.getName() );
		assertEquals( "", _bps.getBPID() );
		assertEquals( 0, _bps.length() );
		assertFalse( _bps.isVerbose() );
		assertNull( _bps.values() );
	}

	public void testBreakpointSetElementModel() {
		// implement this breakpoint definition
		// 
		//   <breakpointDef name="alpha" bpID="ALPHA1" units="deg">
		//     <description> Alpha breakpoints for basic and damping aero tables </description>
		//	   <bpVals>-10., -5., 0., 5., 10., 15., 20., 25., 30., 35., 40., 45.</bpVals>
		//   </breakpointDef>
		// 
		
    	Element bpValElement = new Element("bpVals");
    	bpValElement.addContent( "-10., -5., 0., 5., 10., 15., 20., 25., 30., 35., 40., 45." );
    	
    	Element descriptionElement = new Element("description");
    	descriptionElement.addContent( " Alpha breakpoints for basic and damping aero tables " );

    	Element bpDefElement = new Element("breakpointDef");
    	bpDefElement.setAttribute("name", "alpha");
    	bpDefElement.setAttribute("bpID", "ALPHA1");
    	bpDefElement.setAttribute("units", "deg");
    	bpDefElement.addContent(descriptionElement);
    	bpDefElement.addContent(bpValElement);
    	
		try {
			_bps = new BreakpointSet( bpDefElement, _model );
		} catch (DAVEException e) {
			fail("Exception in testBreakpointSetElementModel() call to BreakpointSet( element, model) : " +
				e.getMessage() );
		}
		
		checkBPS();
		
		// note how leading, trailing whitespace has been trimmed
		assertEquals( "Alpha breakpoints for basic and damping aero tables",
				_bps.myDescription );
	}

	public void testBreakpointSetStringStringStringStringModel() {
		
		// commas, trailing decimals are optional
		try {
			_bps = new BreakpointSet( "alpha", "ALPHA1", 
					"-10. -5. 0 5 10 15. 20. 25. 30 35 40 45.",
					" Alpha breakpoints for basic and damping aero tables ", _model );
		} catch (DAVEException e) {
			fail("Exception in testBreakpointSetElementModel() call to BreakpointSet( element, model) : " +
				e.getMessage() );
		}
		
		checkBPS();
		
		// FIXME leading, trailing whitespace should be trimmed
		assertEquals( " Alpha breakpoints for basic and damping aero tables ",
				_bps.myDescription );
	}
	
	public void testRegister() {
		_bps = new BreakpointSet( _model );
		_bps.register(_bpb);
		assertNotNull( _bps.users );
		BlockBP bpb = (BlockBP) _bps.users.get(0);
		assertEquals( _bpb, bpb );
	}

	public void testIsVerbose() {
		_bps = new BreakpointSet( _model );
		assertFalse( _bps.isVerbose() );
		_bps.makeVerbose();
		assertTrue( _bps.isVerbose() );
		_bps.silence();
		assertFalse( _bps.isVerbose() );
	}

	private void checkBPS() {
		assertNotNull( _bps );
		assertEquals( "alpha", _bps.getName() );
		assertEquals( "ALPHA1", _bps.getBPID() );
		assertEquals( 12, _bps.length() );
		assertFalse( _bps.isVerbose() );
		assertNotNull( _bps.values() );
		ArrayList<Double> vals = _bps.values();
		assertNotNull( vals );
		assertEquals( 12, vals.size() );
		checkValues( vals );
	}
		

	private void checkValues( ArrayList<Double> vals ) {
		assertEquals( -10., vals.get( 0), EPS );
		assertEquals(  -5., vals.get( 1), EPS );
		assertEquals(   0., vals.get( 2), EPS );
		assertEquals(   5., vals.get( 3), EPS );
		assertEquals(  10., vals.get( 4), EPS );
		assertEquals(  15., vals.get( 5), EPS );
		assertEquals(  20., vals.get( 6), EPS );
		assertEquals(  25., vals.get( 7), EPS );
		assertEquals(  30., vals.get( 8), EPS );
		assertEquals(  35., vals.get( 9), EPS );
		assertEquals(  40., vals.get(10), EPS );
		assertEquals(  45., vals.get(11), EPS );	
	}

	public static BreakpointSet generateSampleAlphaBreakpointSet( Model model ) {
		BreakpointSet bpAlpha = null;
		try {
			bpAlpha = new BreakpointSet( "alpha", "ALPHA1", 
					"-10. -5. 0 5 10 15. 20. 25. 30 35 40 45.",
					" Alpha breakpoints for basic and damping aero tables ", model );
		} catch (DAVEException e) {
			fail("Exception in TestFuncTable.setUp() call to create ALPHA1 BreakpointSet : " +
					e.getMessage() );
		}
		return bpAlpha;
	}

	public static BreakpointSet generateSampleElevBreakpointSet( Model model ) {
		BreakpointSet bpDE = null;
		try {
			bpDE = new BreakpointSet( "el", "DE1", 
					"-24, -12., 0, 12., 24.",
					" Elevator angle breakpoints ", model );
		} catch (DAVEException e) {
			fail("Exception in TestFuncTable.setUp() call to create DE1 BreakpointSet : " +
					e.getMessage() );
		}
		return bpDE;
	}
}
