package gov.nasa.daveml.dave;

import junit.framework.*;
import org.jdom.Element;

/**
 * First JUnit test I wrote.
 * 
 * This has a bunch of tests that should distributed to other
 * test routines; this module should test the common methods 
 * to all blocks.
 *
 * 040220 Bruce Jackson <mailto:bruce.jackson@nasa.gov>
 * 2010-05-08 Slightly rewritten
 *
 **/

public class GeneralBlockTest extends TestCase {

    Model _simple;
    Model _complexMath;
    Signal mySignal;
    BlockMathConstant myConstBlock;

    @Override
    protected void setUp() {

	_simple = new Model(3,3);

	// create a constant input block in separate model
	Model constModel = new Model(2,2);
	myConstBlock = new BlockMathConstant( "-3.45", constModel );

	// create a simple abs value function from input such as
	//  <variableDef name="absbeta" varID="absbeta" units="deg">
	//    <description>
	//       Absolute value of angle-of-sideslip, deg.
	//    </description>
	//    <calculation>
	//      <math>
	//        <apply><abs/><ci>beta</ci></apply>
	//      </math>
	//    </calculation>
	//  </variableDef>

	Element theValue = new Element("ci");	// add numeric constant
	theValue.addContent( "beta" );

	Element absElement = new Element("abs");

	Element applyElement = new Element("apply");
	applyElement.addContent( absElement );
	applyElement.addContent( theValue );

	Element mathElement = new Element("math");
	mathElement.addContent( applyElement );

	Element calcElement = new Element("calculation");
	calcElement.addContent( mathElement );

	Element descElement = new Element("description");
	descElement.addContent( "Absolute value of angle-of-sideslip, deg." );

	java.util.ArrayList<Element> list = new java.util.ArrayList<Element>(3);
	list.add( descElement );
	list.add( calcElement );

	Element varDefElement = new Element("variableDef");
	varDefElement.setContent( list );
	varDefElement.setAttribute( "name", "absbeta" );
	varDefElement.setAttribute( "varID", "absbeta" );
	varDefElement.setAttribute( "units", "deg" );

	mySignal = null;
        try {
            mySignal = new Signal( varDefElement, _simple );
        } catch (DAVEException e) {
            fail("Unexpected exception while creating 'mySignal' in " +
                    "GeneralBlockTest.setUp(): " + e.getLocalizedMessage() );
        }
        assertNotNull( mySignal );

	// need to create the input signal beta - modify prev varDef element
	varDefElement.removeChild( "calculation" );
	descElement.addContent( "beta input signal" );
	varDefElement.setAttribute( "name", "beta" );
	varDefElement.setAttribute( "varID", "beta" );

	Signal ignored;
        ignored = null;
        try {
            ignored = new Signal( varDefElement, _simple );
        } catch (DAVEException e) {
             fail("Unexpected exception while creating 'ignored' in " +
                    "GeneralBlockTest: " + e.getLocalizedMessage() );
        }
        assertNotNull( ignored );
	_simple.wireBlocks();
	_simple.hookUpIO();

    }

    public static Test suite() {
	return new TestSuite( GeneralBlockTest.class );
    }

    public void testConstBlock() {
	assertEquals( -3.45, myConstBlock.getValue() );
    }

    public void testSignal() {
	assertEquals( "absbeta", mySignal.getName() );
	assertEquals( "deg",     mySignal.getUnits() );
    }

    public void testRenameOutVarID() {
        Block upstreamBlock = mySignal.getSource();
        assertTrue( upstreamBlock != null );
        assertEquals( "absbeta", upstreamBlock.getOutputVarID() );
        mySignal.setVarID("newName");
        upstreamBlock.renameOutVarID();
        assertEquals( "newName", upstreamBlock.getOutputVarID() );
    }

    public void testModel() {
//	if (false) {
//	    try {
//		gov.nasa.daveml.dave.TreeFileWriter writer 
//		    = new gov.nasa.daveml.dave.TreeFileWriter("test.txt");
//		writer.describe(_simple);
//		writer.close();
//	    } catch (java.io.IOException e ) {
//	    }
//	}
	assertEquals( 3, _simple.getNumBlocks() ); // should be input, abs, 1 output
	assertEquals( 0, _simple.getNumTables() );
	assertEquals( 2, _simple.getNumSignals() ); // should be input-abs, abs-output
    }

    /*
      public void testAbsBlock() {
      double input = -5;
      fail("Not yet implemented.");
      }
    */

    public void testMathFunc() {

	Model mathFuncModel = new Model(10, 10);

	// create input variable

	Element input = new Element("variableDef");
	input.setAttribute("name",  "X");
	input.setAttribute("varID", "X");
	input.setAttribute("units", "" );

	Signal ignored;
        ignored = null;
        try {
            ignored = new Signal( input, mathFuncModel );
        } catch (DAVEException e) {
             fail("Unexpected exception while creating 'ignored' in " +
                    "GeneralBlockTest.testMathFunc(): " + e.getLocalizedMessage() );
        }
        assertNotNull( ignored );
	// create output calculation

//<variableDef name="y" varID="Y" units="">
//  <calculation>
//    <math>                     <!-- in the following comments, target of buildup is innermost [] brackets          -->
//      <apply>                  <!-- Y = [  ?                                                                     ] -->
// 	  <plus/>                <!-- Y = [  ?  + ?                                                                ] -->
// 	  <apply>                <!-- Y = [ [?] + ?                                                                ] -->
// 	    <minus/>             <!-- Y = [-[?] + ?                                                                ] -->
// 	    <ci>X</ci>           <!-- Y = [-[X] + ?                                                                ] -->
// 	  </apply>               <!-- Y = [ -X  + ?                                                                ] -->
// 	  <apply>                <!-- Y = [ -X  + [       ?                                                       ]] -->
// 	    <pow/>		 <!-- Y = [ -X  + [ ? ^                  ?                                        ]] -->
// 	    <ci>X</ci>	         <!-- Y = [ -X  + [ X ^                  ?                                        ]] -->
// 	    <apply>              <!-- Y = [ -X  + [ X ^ [                ?                                       ]]] -->
// 	      <divide/>          <!-- Y = [ -X  + [ X ^ [       ?        /                   ?                   ]]] -->
// 	      <apply>            <!-- Y = [ -X  + [ X ^ [[      ?       ]/                   ?                   ]]] -->
// 		<arcsin/>          <!-- Y = [ -X  + [ X ^ [[asin(    ?   )]/                   ?                   ]]] -->
// 		<apply>          <!-- Y = [ -X  + [ X ^ [[asin([   ?  ])]/                   ?                   ]]] -->
// 		  <sin/>         <!-- Y = [ -X  + [ X ^ [[asin([sin[?]])]/                   ?                   ]]] -->
// 		  <ci>X</ci>     <!-- Y = [ -X  + [ X ^ [[asin([sin(X)])]/                   ?                   ]]] -->
// 		</apply>         <!-- Y = [ -X  + [ X ^ [[asin( sin(X) )]/                   ?                   ]]] -->
// 	      </apply>           <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) /                   ?                   ]]] -->
// 	      <apply>            <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [                 ?                  ]]]] -->
// 		<mult/>          <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [        ?        *        ?         ]]]] -->
// 		<apply>          <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [ [      ?       ]*        ?         ]]]] -->
// 		  <arccos/>        <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [ [acos(   ?    )]*        ?         ]]]] -->
// 		  <apply>        <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [ [acos([  ?   ])]*        ?         ]]]] -->
// 		    <cos/>       <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [ [acos([cos(?)])]*        ?         ]]]] -->
// 		    <ci>X</ci>   <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [ [acos([cos(X)])]*        ?         ]]]] -->
// 		  </apply>       <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [ [acos( cos(X) )]*        ?         ]]]] -->
// 		</apply>         <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [  acos( cos(X) ) *        ?         ]]]] -->
// 		<apply>          <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [  acos( cos(X) ) * [      ?       ] ]]]] -->
// 		  <tan/>         <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [  acos( cos(X) ) * [tan(    ?    )] ]]]] -->
// 		  <apply>        <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [  acos( cos(X) ) * [tan([   ?   ])] ]]]] -->
// 		    <arctan/>      <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [  acos( cos(X) ) * [tan([atan(?)])] ]]]] -->
// 		    <ci>X</ci>   <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [  acos( cos(X) ) * [tan([atan(X)])] ]]]] -->
// 		  </apply>       <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [  acos( cos(X) ) * [tan( atan(X) )] ]]]] -->
// 		</apply>         <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / [  acos( cos(X) ) *  tan( atan(X) )  ]]]] -->
// 	      </apply>           <!-- Y = [ -X  + [ X ^ [ asin( sin(X) ) / (  acos( cos(X) ) *  tan( atan(X) )  )]]] -->
// 	    </apply>             <!-- Y = [ -X  + [ X ^ ( asin( sin(X) ) / (  acos( cos(X) ) *  tan( atan(X) )  ))]] -->
// 	  </apply>               <!-- Y = [ -X  +   X ^ ( asin( sin(X) ) / (  acos( cos(X) ) *  tan( atan(X) )  )))] -->
//      </apply>                 <!-- Y =   -X  +   X ^ ( asin( sin(X) ) / (  acos( cos(X) ) *  tan( atan(X) )  )))  -->
//    </math>                    <!--               /       asin(sin(X))        \        /  X  \           1         -->
//  </calculation>               <!-- Y = -X + X ^ | - - - - - - - - - - - - -  | =  X^ | - - - | - X = X^(-)-X      -->
//</variableDef>                 <!--               \ acos(cos(X))*tan(atan(X)) /        \X * X/           X         -->

	// build tan(atan(X))

	Element X1 = new Element("ci");
	X1.addContent("X");

	Element aTanX = new Element("apply");
	aTanX.addContent( new Element("arctan") );
	aTanX.addContent( X1 );

	Element tanATanX = new Element("apply");
	tanATanX.addContent( new Element("tan") );
	tanATanX.addContent( aTanX );

	// build acos(cos(X))

	Element X2 = new Element("ci");
	X2.addContent("X");

	Element cosX = new Element("apply");
	cosX.addContent( new Element("cos") );
	cosX.addContent( X2 );

	Element aCosCosX = new Element("apply");
	aCosCosX.addContent( new Element("arccos"));
	aCosCosX.addContent( cosX );

	// build denom = acos(cos(X))*tan(atan(X))

	Element denom = new Element("apply");
	denom.addContent( new Element("times") );
	denom.addContent( aCosCosX );
	denom.addContent( tanATanX );

	// build asin(sin(X))

	Element X3 = new Element("ci");
	X3.addContent("X");

	Element sinX = new Element("apply");
	sinX.addContent( new Element("sin") );
	sinX.addContent( X3 );

	Element aSinSinX = new Element("apply");
	aSinSinX.addContent( new Element("arcsin") );
	aSinSinX.addContent( sinX );

	// build trig = asin(sin(X))/denom

	Element trig = new Element("apply");
	trig.addContent( new Element("divide") );
	trig.addContent( aSinSinX );
	trig.addContent( denom );

	// build X^trig

	Element X4 = new Element("ci");
	X4.addContent("X");

	Element xToTheTrigPwr = new Element("apply");
	xToTheTrigPwr.addContent( new Element("power") );
	xToTheTrigPwr.addContent( X4 );
	xToTheTrigPwr.addContent( trig );

	// finally, build X^trig-X

	Element X5 = new Element("ci");
	X5.addContent("X");

	Element negX = new Element("apply");
	negX.addContent( new Element("minus") );
	negX.addContent( X5 );

	Element xToTheTrigPwrMinusX = new Element("apply");
	xToTheTrigPwrMinusX.addContent( new Element("plus") );
	xToTheTrigPwrMinusX.addContent( xToTheTrigPwr );
	xToTheTrigPwrMinusX.addContent( negX );

	// create the single variable that nests it all

	Element math = new Element("math");
	math.addContent( xToTheTrigPwrMinusX );

	Element calc = new Element("calculation");
	calc.addContent( math );

	Element output = new Element("variableDef");
	output.addContent( calc );
	output.setAttribute( "name",  "Y" );
	output.setAttribute( "varID", "Y" );
	output.setAttribute( "units", ""  );

	Signal ignored2;
        ignored2 = null;
        try {
            ignored2 = new Signal( output, mathFuncModel );
        } catch (DAVEException e) {
             fail("Unexpected exception while creating 'ignored2' in " +
                    "GeneralBlockTest.testMathFunc(): " + e.getLocalizedMessage() );
        }
        assertNotNull( ignored2 );
	mathFuncModel.wireBlocks();
	mathFuncModel.hookUpIO();

	// now, try some values

	VectorInfoArrayList inputs = null;
	VectorInfoArrayList outputs = null;
	try {
	    inputs  = mathFuncModel.getInputVector();
	    outputs = mathFuncModel.getOutputVector();
	} catch (DAVEException e) {
	    fail("Exception thrown: " + e.getMessage());
	}

	assertNotNull(inputs);
	assertEquals(1, inputs.size() );

	assertNotNull(outputs);
	assertEquals(1, outputs.size() );

	VectorInfo theInput = (VectorInfo) inputs.get(0);
	assertNotNull( theInput );
	assertEquals( "X", theInput.getName() );

	VectorInfo theOutput = (VectorInfo) outputs.get(0);
	assertNotNull( theOutput );
	assertEquals( "Y", theOutput.getName() );

	// set input value;

	theInput.setValue(0.5);

	// run model

	try {
//	    mathFuncModel.makeVerbose();
	    mathFuncModel.cycle();
	} catch (DAVEException e) {
	    fail("Exception thrown: " + e.getMessage());
	}

	// left for example; this model only has two signals - X & Y

// 	try {
// 	    FileOutputStream fos = new FileOutputStream( "debug_complex_model.xml" );
// 	    PrintWriter pw = new PrintWriter( fos );
// 	    mathFuncModel.generateInternalValues( pw );
// 	    pw.flush();
// 	    fos.close();
// 	} catch (Exception e) {
// 	    System.out.println("Exception: " + e.getMessage());
// 	    e.printStackTrace();
//      }

	// check output value

	assertEquals( -0.25, theOutput.getValue(), 0.0000000001 );

	try {

	    // try some other legitimate values

	    theInput.setValue(+1);
	    mathFuncModel.cycle();
	    assertEquals( 0, theOutput.getValue(), 0.0000000001 );

	    theInput.setValue(+2);
	    mathFuncModel.cycle();
	    assertEquals( -0.7812534, theOutput.getValue(), 0.0000001 );

	    theInput.setValue(+3);
	    mathFuncModel.cycle();
	    assertEquals( -1.9825658, theOutput.getValue(), 0.0000001 );

	} catch (DAVEException e) {
	    fail("Exception thrown: " + e.getMessage());
	}


    }

    public static void main (String[] args) {
	junit.textui.TestRunner.run(suite());
    }
    
}
