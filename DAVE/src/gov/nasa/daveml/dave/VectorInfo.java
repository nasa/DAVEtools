// VectorInfo
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave;

/**
 *
 * <p> Object providing information about an input or output to a
 * <code>Model</code> </p>
 *
 * <p> 031227 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 */

/**
 *
 * <p> Object giving variable name and value for any input, output,
 * state, or state derivative in a <code>Model</code> </p>
 *
 **/

public class VectorInfo
{
	/**
	 *  Name of input or output block
	 */

	String  signalName;

	/**
	 *  units of measure
	 */

	String  myUnits;

	/**
	 *  if true, we're an input
	 */

	boolean isInput;

	/**
	 *  source block (if output)
	 */

	Block   source;

	/**
	 *  sink block (if input)
	 */

	Block   sink;

	/**
	 *  input or output value
	 */

	double  value;

	/**
	 *  verification tolerance value
	 */

	double  tolerance;


	/**
	 * <p> Elementary constructor </p>
	 **/

	public VectorInfo()
	{
		this.signalName =  "";
		this.myUnits = "";
		this.source = null;
		this.sink = null;
		this.value = Double.NaN;
		this.tolerance = Double.NaN;
	}


	/**
	 *
	 * <p> Simple constructor </p>
	 *
	 * @param signalName A <code>String</code> containing the name of
	 * the signal to construct
	 * @param units Our units-of-measure
	 * @param blk The source or sink <code>Block</code> we represent
	 * @param isInput If true, we represent an input
	 *
	 **/

	public VectorInfo(String signalName, String units, Block blk, boolean isInput)
	{
		this(); // initialize tol, e.g.
		this.signalName = signalName;
		this.myUnits = units;
		if (isInput) {
                    this.isInput = true;
                    this.sink = blk;
                    this.source = null;
                    if (blk != null) {
                        BlockInput ib;
                        ib = (BlockInput) blk;
                        if (ib.hasIC()) {
                            this.value = ib.getValue();
                        }
                    }
		} else {
			this.isInput = false;
			this.sink = null;
			this.source = blk;
		}
	}


	/**
	 * <p> Set the name of the signal </p>
	 *
	 * @param theName <code>String</code> to use for name
	 *
	 **/

	//private void setName( String theName ) { this.signalName = theName; }


	/**
	 *
	 * <p> Set the units of the signal </p>
	 *
	 * @param theUnits <code>String</code> to use for name
	 *
	 **/

	public void setUnits( String theUnits ) { this.myUnits = theUnits; }


	/**
	 *
	 * Set the value of the signal
	 *
	 * @param theValue value to remember
	 *
	 **/

	public void setValue( double theValue ) { this.value = theValue; }


	/**
	 *
	 * Set the value of the signal
	 *
	 * @param theValue value to remember
	 *
	 **/

	public void setValue( String theValue ) { this.value = Double.parseDouble( theValue ); }


	/**
	 *
	 * <p> Sets the verification tolerance of the signal </p>
	 *
	 * @param theValue <code>double</code> value to remember
	 *
	 **/

	public void setTolerance( double theValue ) { this.tolerance = theValue; }


	/**
	 *
	 * <p> Sets the verification tolerance of the signal </p>
	 *
	 * @param theValue <code>double</code> value to remember
	 *
	 **/

	public void setTolerance( String theValue ) { this.tolerance = Double.parseDouble( theValue ); }


	/**
	 *
	 * <p> Returns signal name as <code>String</code> </p>
	 *
	 **/

	public String getName() { return this.signalName; }

	/**
	 *
	 * <p> Returns variable name as <code>String</code> </p>
	 *
	 **/

	public String getUnits() { return this.myUnits; }


	/**
	 *
	 * <p> Returns the value of this element </p>
	 *
	 **/

	public double getValue() { return this.value; }


	/**
	 *
	 * <p> Returns the verification tolerance of this element </p>
	 *
	 **/

	public double getTolerance() { return this.tolerance; }


	/**
	 *
	 * <p> Indicates if we're an input or not </p>
	 *
	 **/

	public boolean isInput() { return this.isInput; }


	/**
	 *
	 * <p> Returns source block </p>
	 *
	 **/

	public BlockOutput getSource() { return (BlockOutput) this.source; }


	/**
	 *
	 * <p> Returns sink block </p>
	 *
	 **/

	public BlockInput getSink() { return (BlockInput) this.sink; }

}

