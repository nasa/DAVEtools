package gov.nasa.daveml.dave;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jdom.Element;

import junit.framework.TestCase;

public class StaticShotTest extends TestCase {

	protected StaticShot _ss;
	protected final double EPS = 0.00000001;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		_ss = new StaticShot(genStaticShotElement());
	}

	public void testStaticShot() {
		assertNotNull(_ss);
		// TODO more tests
	}

	public void testGetName() {
		assertEquals("Nominal",_ss.getName());
	}

	public void testGetInputs() {
		VectorInfoArrayList inputs = _ss.getInputs();
		assertNotNull( inputs );
		assertEquals(3, inputs.size() );
		checkSignal(inputs, 0, "trueAirspeed"   , "f_s", 300.0 );
		checkSignal(inputs, 1, "angleOfAttack"  , "deg",   5.0 );
		checkSignal(inputs, 2, "angleOfSideslip", "deg",   0.0 );
	}

	public void testGetOutputs() {		
		VectorInfoArrayList outputs = _ss.getOutputs();
		assertNotNull( outputs );
		assertEquals(3, outputs.size() );
		checkSignal(outputs, 0, "aeroBodyForceCoefficient_X", "nd", -0.004, 1.0e-6 );
		checkSignal(outputs, 1, "aeroBodyForceCoefficient_Y", "nd",  0.000, 1.0e-6 );
		checkSignal(outputs, 2, "aeroBodyForceCoefficient_Z", "nd", -0.416, 1.0e-6 );
	}

	public void testLoadInputVector() {
		
		// build fake model's input vector
		VectorInfoArrayList inVec = new VectorInfoArrayList(3);
		
		VectorInfo airspeed = new VectorInfo("trueAirspeed", "f_s", null, true);
		airspeed.setValue(300.);
		inVec.add(airspeed);

		VectorInfo alpha = new VectorInfo("angleOfAttack", "deg", null, true);
		alpha.setValue(5.);
		inVec.add(alpha);

		// should get length miscompare exception
		try {
			_ss.loadInputVector(inVec);
			fail("Expected exception NOT thrown in testLoadInputVector.");
		} catch (DAVEException e) {
			String expectedMessage = 
				"Input vector length for checkcase 'Nominal' (3) "
				+ "doesn't match length of model's input vector (2).";
			assertEquals(expectedMessage, e.getMessage());
		}
		
		VectorInfo beta = new VectorInfo("angleOfSideslip", "deg", null, true);
		beta.setValue(0.);
		inVec.add(beta);

		// check nominal inputs
		try {
			_ss.loadInputVector(inVec);
		} catch (DAVEException e) {
			fail("Unexpected exception in testLoadInputVector: " + e.getMessage() );
		}
		
		String bad_units = "rrr";
		beta.setUnits(bad_units);
		
		// should get units miscompare exception
		try {
			_ss.loadInputVector(inVec);
			fail("Expected exception NOT thrown in testLoadInputVector.");
		} catch (DAVEException e) {
			String expectedMessage = 
				"Mismatched units - in checkcase 'Nominal' for input signal "
				+ "'angleOfSideslip', checkcase specifies 'deg' but model expects '"
				+ bad_units + "'.";
			assertEquals(expectedMessage, e.getMessage());
		}
		
		// change back to correct
		beta.setUnits("deg");
		try {
			_ss.loadInputVector(inVec);
		} catch (DAVEException e) {
			fail("Unexpected exception in testLoadInputVector: " + e.getMessage() );
		}
		
		alpha.signalName = "fart";
		
		// should get missing input exception
		try {
			_ss.loadInputVector(inVec);
			fail("Expected exception NOT thrown in testLoadInputVector.");
		} catch (DAVEException e) {
			String expectedMessage = 
				"Could not find input signal 'angleOfAttack' in model's inputs.";
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	public void testCheckOutputs() {

		ByteArrayOutputStream myStream = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(myStream);

		// build fake model's output vector
		VectorInfoArrayList outVec = new VectorInfoArrayList(3);
		
		VectorInfo X = new VectorInfo("aeroBodyForceCoefficient_X", "nd", null, false);
		X.setValue(-0.004);
		X.setTolerance(1.0e-6);
		outVec.add(X);

		VectorInfo Y = new VectorInfo("aeroBodyForceCoefficient_Y", "nd", null, false);
		Y.setValue( 0.000);
		Y.setTolerance(1.0e-6);
		outVec.add(Y);

		// should get length miscompare exception
		try {
			_ss.checkOutputs(outVec);
			fail("Expected exception NOT thrown in testLoadInputVector.");
		} catch (DAVEException e) {
			String expectedMessage = 
				"Output vector length for checkcase 'Nominal' (3) "
				+ "is bigger than the length of model's output vector (2).";
			assertEquals(expectedMessage, e.getMessage());
		}
		
		// add missing output
		
		VectorInfo Z = new VectorInfo("aeroBodyForceCoefficient_Z", "nd", null, false);
		Z.setValue(-0.416);
		Z.setTolerance(1.0e-6);
		outVec.add(Z);

		// check nominal outputs
		try {
			_ss.checkOutputs(outVec);
		} catch (DAVEException e) {
			fail("Unexpected exception in testCheckOutputs: " + e.getMessage() );
		}

		// bad name case
		
		X.signalName = "xxx";
			
		// should get missing input exception
		try {
			_ss.checkOutputs(outVec);
			fail("Expected exception NOT thrown in testLoadInputVector.");
		} catch (DAVEException e) {
			String expectedMessage = 
				"Could not find output signal 'aeroBodyForceCoefficient_X' in model's outputs.";
			assertEquals(expectedMessage, e.getMessage());
		}
		
		// change back to correct name
		
		X.signalName = "aeroBodyForceCoefficient_X";

		// check nominal outputs again
		try {
			_ss.checkOutputs(outVec);
		} catch (DAVEException e) {
			fail("Unexpected exception in testCheckOutputs: " + e.getMessage() );
		}
		
		// mess up units
		
		String bad_units = "rtss";
		Z.setUnits(bad_units);

		// should get units miscompare exception
		try {
			_ss.checkOutputs(outVec);
			fail("Expected exception NOT thrown in testLoadInputVector.");
		} catch (DAVEException e) {
			String expectedMessage = 
				"Mismatched units - in checkcase 'Nominal' for output signal "
				+ "'aeroBodyForceCoefficient_Z', checkcase specifies 'nd' but model expects '"
				+ bad_units + "'.";
			assertEquals(expectedMessage, e.getMessage());
		}
		
		// correct and check
		Z.setUnits("nd");

		// check nominal outputs again
		try {
			_ss.checkOutputs(outVec);
		} catch (DAVEException e) {
			fail("Unexpected exception in testCheckOutputs: " + e.getMessage() );
		}
		
		// check for within tolerance output
		double tol = Y.getTolerance();
		double nomval = Y.getValue();
		Y.setValue(nomval + 0.99*tol);
		
		// check nominal outputs again
		try {
			_ss.checkOutputs(outVec);
		} catch (DAVEException e) {
			fail("Unexpected exception in testCheckOutputs: " + e.getMessage() );
		}
		
		// now make it 1% out of tolerance
		Y.setValue(nomval + 1.01*tol);
		try {
			_ss.checkOutputs(outVec, ps);
		} catch (DAVEException e) {
			fail("Unexpected exception in testCheckOutputs: " + e.getMessage() );
		}
		
		// should get out-of-tolerance message on standard out
		String errMsg; 
		String expectedMsg;
		errMsg = myStream.toString();
		expectedMsg =
			"For output 'aeroBodyForceCoefficient_Y': expected 0.0\n" +
			" but found 1.0099999999999999E-6; difference is 1.0099999999999999E-6\n" +
			" which is greater than allowed tolerance of 1.0E-6.\n";
		assertEquals(expectedMsg, errMsg);
		
		myStream = new ByteArrayOutputStream();  // clear output buffer
		ps = new PrintStream(myStream);
		
		// now make it 1% out of tolerance
		Y.setValue(nomval - 1.01*tol);
		try {
			_ss.checkOutputs(outVec, ps);
		} catch (DAVEException e) {
			fail("Unexpected exception in testCheckOutputs: " + e.getMessage() );
		}
		// should get out-of-tolerance message on standard out
		errMsg = myStream.toString();
		expectedMsg =
			"For output 'aeroBodyForceCoefficient_Y': expected 0.0\n" +
			" but found -1.0099999999999999E-6; difference is 1.0099999999999999E-6\n" +
			" which is greater than allowed tolerance of 1.0E-6.\n";
		assertEquals(expectedMsg, errMsg);
	}
	
    protected Element genStaticShotElement() {
    	Element staticShot = new Element("staticShot");      // <staticShot name="Nominal" refID="NOTE1">
    	staticShot.setAttribute("name", "Nominal");
    	staticShot.setAttribute("refID","NOTE1");
    	
    	Element checkInputs = 
    		new Element("checkInputs");                      //   <checkInputs>
    	
    	Element inputSignal1 =                               //     <signal>
    		buildSignalElement("trueAirspeed",               //       <signalName>trueAirspeed</signalName>
    					       "f_s",                        //       <signalUnits>f_s</signalUnits>
    					       " 300.000");                  //       <signalValue> 300.000</signalValue>
        checkInputs.addContent(inputSignal1);                //      </signal>          
                                                             
        Element inputSignal2 =                               //      <signal>
            buildSignalElement("angleOfAttack",              //        <signalName>angleOfAttack</signalName>
                               "deg",                        //        <signalUnits>deg</signalUnits>
                               " 5.000");                    //        <signalValue> 5.000</signalValue>
        checkInputs.addContent(inputSignal2);                //      </signal>
        
        Element inputSignal3 =                               //      <signal>
            buildSignalElement("angleOfSideslip",            //        <signalName>angleOfSideslip</signalName>
                               "deg",                        //        <signalUnits>deg</signalUnits>
                               " 0.000");                    //        <signalValue> 0.000</signalValue>
        checkInputs.addContent(inputSignal3);                //      </signal>
        
    	staticShot.addContent( checkInputs );                //    </checkInputs>
    	
    	
    	// internalValues are actually ignored and not recorded. 
    	// Here for realism.
    	
        Element internalValues = 
        	new Element("internalValues");                   //    <internalValues>
        
        Element internalSignal1 =                            //      <signal>
            buildSignalElement("vt", "300.0");               //        <varID>vt</varID>
                                                             //        <signalValue>300.0</signalValue>
        internalValues.addContent(internalSignal1);          //      </signal>
        
        Element internalSignal2 =                            //      <signal>
            buildSignalElement("alpha", "5.0");              //        <varID>alpha</varID>
                                                             //        <signalValue>5.0</signalValue>
        internalValues.addContent(internalSignal2);          //      </signal>
        
        Element internalSignal3 =                            //      <signal>
            buildSignalElement("beta", "0.0");               //        <varID>beta</varID>
                                                             //        <signalValue>0.0</signalValue>
        internalValues.addContent(internalSignal3);          //      </signal>
        
        staticShot.addContent(internalValues);               //    </internalValues>
        
        
        Element checkOutputs = new Element("checkOutputs");  //    <checkOutputs>
        
        Element outputSignal1 =                              //      <signal>
        	buildSignalElement("aeroBodyForceCoefficient_X", //        <signalName>aeroBodyForceCoefficient_X</signalName>
                               "nd",                         //        <signalUnits>nd</signalUnits>
                               "-0.00400000000000",          //        <signalValue>-0.00400000000000</signalValue>
                               "0.000001");                  //        <tol>0.000001</tol>
        checkOutputs.addContent(outputSignal1);              //      </signal>
        
        Element outputSignal2 =                              //      <signal>
        	buildSignalElement("aeroBodyForceCoefficient_Y", //        <signalName>aeroBodyForceCoefficient_Y</signalName>
                               "nd",                         //        <signalUnits>nd</signalUnits>
                               " 0.00000000000000",          //        <signalValue> 0.00000000000000</signalValue>
                               "0.000001");                  //        <tol>0.000001</tol>
        checkOutputs.addContent(outputSignal2);              //      </signal>
        
        Element outputSignal3 =                              //      <signal>
        	buildSignalElement("aeroBodyForceCoefficient_Z", //        <signalName>aeroBodyForceCoefficient_Z</signalName>
                               "nd",                         //        <signalUnits>nd</signalUnits>
                               "-0.41600000000000",          //        <signalValue>-0.41600000000000</signalValue>
                               "0.000001");                  //        <tol>0.000001</tol>
        checkOutputs.addContent(outputSignal3);              //      </signal>
        
        staticShot.addContent(checkOutputs);                 //    </checkOutputs>
        
    	return staticShot;                                   //  </staticShot>
                
    }
    
    // input signal element builder
    protected Element buildSignalElement( String name, String units, String value ) {
		Element signal = new Element("signal");
		signal.addContent(new Element("signalName" ).addContent(name ));
		signal.addContent(new Element("signalUnits").addContent(units));
		signal.addContent(new Element("signalValue").addContent(value));
		return signal;
	}

    // internal signal element builder
    protected Element buildSignalElement( String varID, String value ) {
		Element signal = new Element("signal");
		signal.addContent(new Element("varID"      ).addContent(varID ));
		signal.addContent(new Element("signalValue").addContent(value));
		return signal;
	}
    
    // output signal element builder
    protected Element buildSignalElement( String name, String units, String value, String tol ) {
		Element signal = buildSignalElement( name, units, value );
		signal.addContent(new Element("tol" ).addContent(tol ));
		return signal;
	}

    protected void checkSignal( VectorInfoArrayList signals,
    		int index, String name, String units, double value ) {
    	checkSignal( signals, index, name, units, value, Double.NaN);
    }
    
    protected void checkSignal( VectorInfoArrayList signals,
    		int index, String name, String units, 
    		double value, double tolerance) {
    	
    	VectorInfo signal;
    	
    	assertNotNull(signals);
    	signal = signals.get( index     );
		assertNotNull(           signal );
		assertEquals( name,      signal.getName()           );
		assertEquals( units,     signal.getUnits()          );
		assertEquals( value,     signal.getValue(), EPS     );
		assertEquals( tolerance, signal.getTolerance(), EPS );

    }

}
