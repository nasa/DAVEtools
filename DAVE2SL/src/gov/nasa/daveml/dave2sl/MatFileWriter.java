//  MatFileWriter.java
//
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.


package gov.nasa.daveml.dave2sl;

import gov.nasa.daveml.dave.*;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * Provides special output functions for writing Matlab workspace (.mat) files.
 *
 * <p>
 * Temporarily set to write only Matlab script (.m) files instead.
 *
 * <p>
 * Also generates verification scripts for Simulink models
 *
 * <p>
 * <code>MatFileWriter</code> Implementation Notes:
 *
 * <p>
 * In MATLAB, dimensions higher than two (planes, in Matlab speak)
 * are added after the row, column dimensions. Matlab likes the second
 * dimension to be the one most rapidly changing; I need to verify
 * that we're generating the .m arrays properly; may need to swap 1st
 * and 2nd dimensions.
 *
 * <p>
 * In Simulink lookups, the order of ports into the prelookup
 * table block (said inputs coming from breakpoint blocks) are in the
 * same order as Matlab matrix dimensions, so the inputs should be
 * row, column, plane, hyperplane, etc. These appear to be in inverse
 * order (with possible row/column swap) then we're storing them in
 * DAVE.
 *
 * <p>Modification history:
 * <ul>
 *  <li>020611 Created</li>
 *  <li>040225 Updated for v0.5</li>
 *  <li>040302 Added verify-script-generating methods</li>
 * </ul>
 *
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * @version 0.9
 *
 */

public class MatFileWriter extends FileWriter
{

	/**
	 *  keeps track of how many input/output sets have been written
	 */

	int caseNumberCount;

	/**
	 * the name of the <code>Model</code> we're realizing in Simulink
	 */

	String modelName;

	/**
	 * Our parent/source diagram
	 */

	SLDiagram diagram;

	/**
	 * A list of names of outputs we've discovered is missing
	 */

	ArrayList<String> missingOutputs;

	/**
	 *
	 * Constructor for MatFileWriter; derived from FileWriter but
	 * specialized to write Matlab .mat files. Incomplete - it
	 * currently writes an equivalent script (.m) file that replicates
	 * the workspace of a Matlab workspace (.mat) file.
	 *
	 * @param fileName Name of file to open
	 *
	 */

	public MatFileWriter(SLDiagram diagram, String fileName) throws IOException
	{
		super(fileName);
		this.diagram = diagram;		// record our parent; will need to chat
		this.caseNumberCount = 0;
	}

	/**
	 *
	 * Adds newline to the end of each write.
	 *
	 * @param cbuf String containing text to write
	 *
	 */

	public void writeln( String cbuf ) throws IOException
	{
		super.write( cbuf + "\n" );
	}

	/** 
	 *
	 * Writes the header for Simulink model-data .m file.
	 *
	 */

	public void writeDataHeader( String modelName )
	throws IOException
	{
		this.modelName = modelName;
		this.writeln("%% M file script to provide data for " + this.modelName + ".mdl model");
	}


	/** 
	 *
	 * Writes the footer for Simulink model-data .m file.
	 *
	 */

	public void writeDataFooter()
	throws IOException
	{
		//	writer.writeln("  }");	// not needed
	}


	/**
	 *
	 * Writes the header for the verify-script file
	 *
	 * @param modelName The name of the model.
	 *
	 **/

	public void writeVerifyScriptHeader( String modelName ) 
	{
		this.modelName = modelName;
		try {
			this.writeln("function [success] = " + modelName + "_verify()");
			this.writeln("% Script file to verify implementation of " + modelName + ".xml in Simulink");
			this.writeln("%");
			this.writeln("% Created 02-MAR-04 by DAVE2SL utility");
			this.writeln("%");
			this.writeln("");
			this.writeln("% set filename");
			this.writeln("");
			this.writeln("% Load data file - no longer needed with model workspace storage");
			this.writeln("%" + modelName + "_setup");
			this.writeln("");
			this.writeln("% Set options");
			this.writeln("options = simset('FixedStep',1,'MaxStep',1,'MinStep',1,'Solver','FixedStepDiscrete','SrcWorkspace','current');");
			this.writeln("");
			this.writeln("%");
			this.writeln("% Checkcase data");
			this.writeln("%");
		} catch (IOException e) {
			System.err.println("WARNING: Problems writing header of verification script.");
		}
	}


	/**
	 *
	 * Writes the footer for the verify-script file
	 *
	 **/

	public void writeVerifyScriptFooter()
	{
		try {
			this.writeln("");
			this.writeln("num_cases = " + this.caseNumberCount + ";");
			this.writeln("");
			this.writeln("fprintf('Running %d verification cases for %s:\\n', num_cases, '" + this.modelName + "');");
			this.writeln("");
			this.writeln("outcome = 0;");
			this.writeln("");
			this.writeln("for i=1:num_cases");
			this.writeln("   UT = [0 checkcase{i}.u];");
			this.writeln("   y_good = checkcase{i}.y;");
			this.writeln("   y_tol = checkcase{i}.tol;");
			this.writeln("   [t,x,y] = sim('" + this.modelName + "',[0 0],options,UT);");
			this.writeln("   outcome(i) = (length(y_good) == sum(abs(y-y_good)<y_tol));");
			this.writeln("");
			this.writeln("   if outcome(i) == 0");
			this.writeln("      fprintf(' Case %s FAILED.\\n', checkcase{i}.name);");
			this.writeln("   else");
			this.writeln("      fprintf(' Case %d passed...\\n', i);");
			this.writeln("   end");
			this.writeln("end");
			this.writeln("");
			this.writeln("if sum(outcome) == num_cases");
			this.writeln("   success = 1;");
			this.writeln("   fprintf('\\nAll cases passed: model \"" + this.modelName + "\" verified.\\n');");
			this.writeln("else");
			this.writeln("   success = 0;");
			this.writeln("   fprintf('%f%% (%d of %d) FAILED. \"" + this.modelName + "\" not verified!\\n', ...");
			this.writeln("             100*(num_cases-sum(outcome))/num_cases, ...");
			this.writeln("             num_cases-sum(outcome), num_cases);");
			this.writeln("end");
			this.writeln("");

		} catch (IOException e) {
			System.err.println("WARNING: Problems writing header of verification script.");
		}
	}


	/**
	 *
	 * Writes the checkcase-data loading routines for each static shot
	 * into the verify-script file
	 *
	 * @param ss StaticShot to encode
	 *
	 **/

	public void writeCheckCaseFromStaticShot( StaticShot ss )
	{
		Iterator<VectorInfo> it;
		Iterator<String> nameit;
		boolean found;

		// checkcase info
		VectorInfo vi = null;
		VectorInfoArrayList inputs = ss.getInputs();
		VectorInfoArrayList outputs = ss.getOutputs();

		// simulink block info
		ArrayList<String> modelInputNames = this.diagram.getInputNames();
		ArrayList<String> modelOutputNames = this.diagram.getOutputNames();

		String caseName = ss.getName();

		try {
			this.caseNumberCount++;

			this.writeln("");
			this.writeln("% Checkcase " + this.caseNumberCount + " - " + caseName);
			this.writeln("");

			this.writeln("checkcase{" + this.caseNumberCount + "}.name = '" + caseName + "';");
			this.writeln("");

			this.writeln("checkcase{" + this.caseNumberCount + "}.u = [");

			// match vector components to names in order
			nameit = modelInputNames.iterator();
			while (nameit.hasNext()) {
				String name = nameit.next();
				found = false;
				it = inputs.iterator();
				while (it.hasNext()) {
					vi = it.next();
					String equivMDLname = MDLNameList.convertToMDLString( vi.getName() );
					if (name.equals(equivMDLname)) {
						found = true;
						this.writeln( vi.getValue() + "	% " + vi.getName());
					}
				}
				if (!found) {
					System.err.println("Warning: unable to locate input '" + name +
					"' in the model's checkcase data.");
					System.err.println("Unable to correctly generate " +
					"input vector for verification script.");
				}
			}

			this.writeln("     ]';");
			this.writeln("");

			// match vector components to names in order
			nameit = modelOutputNames.iterator();
			VectorInfoArrayList outputInfo = new VectorInfoArrayList( modelOutputNames.size() );
			while (nameit.hasNext()) {
				String name = nameit.next();
				found = false;
				it = outputs.iterator();
				while (it.hasNext()) {	// look for matching input variable name
					vi = it.next();
					String equivMDLname = MDLNameList.convertToMDLString( vi.getName() );
					if (name.equals(equivMDLname)) {
						found = true;
						outputInfo.add(vi);
					}
				}
				if (!found) {	// record placeholder with infinite tolerance
					vi = new VectorInfo( name, "", null, false );
					vi.setTolerance( Double.POSITIVE_INFINITY );
					outputInfo.add(vi);
					// has this one been encountered before?
					if (this.missingOutputs == null) {
						this.missingOutputs = new ArrayList<String>(10);
					}
					if (!this.missingOutputs.contains( name )) {
						// no - print warning
						System.err.println("Warning: unable to locate output '" + name +
						"' in the model's checkcase data.");
						System.err.println("This output will be ignored for verification in all cases.");
						this.missingOutputs.add( name );
					}
				}
			}

			// write output vector

			this.writeln("checkcase{" + this.caseNumberCount + "}.y = [");
			it = outputInfo.iterator();
			while( it.hasNext() ) {
				vi = it.next();
				if (vi.getTolerance() != Double.POSITIVE_INFINITY) {
					this.writeln( vi.getValue() + "	% " + vi.getName());
				} else {
					this.writeln( 0 + "	% " + vi.getName() + " (missing from verification data)");
				}
			}
			this.writeln("     ]';");
			this.writeln("");

			// write tolerance vector

			this.writeln("checkcase{" + this.caseNumberCount + "}.tol = [");
			it = outputInfo.iterator();
			while (it.hasNext()) {
				vi = it.next();
				if (vi.getTolerance() != Double.POSITIVE_INFINITY) {
					this.writeln( vi.getTolerance() + "	% " + vi.getName());
				} else {
					this.writeln( "Inf	% " + vi.getName() + " (missing from verification data)");
				}
			}
			this.writeln("     ]';");
			this.writeln("");

		} catch (IOException e) {
			System.err.println("WARNING: Problem writing checkcase data to verify script.");
		}
	}


	/**
	 *
	 * Writes out a matrix specification to the file. This is the
	 * entry point for the private, recursive method of the same name.
	 *
	 * @param arrayName A <code>String</code> containing the name of the array to write
	 * @param table     A <code>ArrayList</code> containing the data as a long vector
	 * @param dims	An array of <code>int</code> containing the dimensions of the table
	 *
	 */

	public void writeMatrix( String arrayName, ArrayList<Double> table, int[] dims )
	throws IOException
	{
		this.writeMatrix( arrayName, table, dims, 0, "" );
	}



	/** 
	 *
	 * Recursively writes out matlab matrix initialization
	 * script. This recursive version is not available for
	 * higher-level routines to call.
	 *
	 * @param arrayName A <code>String</code> containing the name of the array to write
	 * @param table     A <code>ArrayList</code> containing the data as a long vector
	 * @param dims	An array of <code>int</code> containing the dimensions of the table
	 * @param startIndex An <code>int</code> with the offset into the table
	 * @param higherDims A <code>String</code> containing fragments of dimension specificiers
	 *
	 **/

	private int writeMatrix( String arrayName, ArrayList<Double> table, int[] dims, int startIndex, String higherDims )
	throws IOException
	{

		int offset;
		int i, j;

		// System.out.println("writeMatrix called recursively, index = " + startIndex + 
		//		   "; dims.length = " + dims.length + "; dims[0] = " + dims[0]);

		switch (dims.length)
		{
		case 0:	// shouldn't happen
			return 0;
		case 1:
			if(higherDims.length() == 0)
				this.write(arrayName + " = [\n");
			for ( i = 0; i < dims[0]; i++)
			{
				Double theValue = table.get(i+startIndex);
				this.write( theValue.toString() );
				if( i < dims[0]-1) this.write(", ");
			}
			if(higherDims.length() == 0)
				this.write("];\n");
			return i;
		case 2:
			if(higherDims.length() == 0)
				this.write(arrayName + " = [\n");
			else
				this.write(arrayName + "(:,:" + higherDims + " = [\n");
			for ( i = 0; i < dims[0]; i++)
			{
				int[] newDims = new int[1];
				newDims[0] = dims[1];
				offset = writeMatrix( arrayName, table, newDims, startIndex, " " );
				if( i < dims[0]-1) 
					this.write(",\n");
				startIndex = startIndex + offset;
			}
			this.write("];\n");
			return startIndex;
		case 3:
			/* Here we have to write multiple 2D array assignments */
			for ( i = 0; i < dims[0]; i++ )
			{
				int[] newDims = new int[2];
				newDims[0] = dims[1];
				newDims[1] = dims[2];
				String hDims = new String("," + (i+1) + higherDims + ")");
				offset = writeMatrix( arrayName, table, newDims, startIndex, hDims );
				startIndex = offset;

			}
			return startIndex;
		default:
			/* For dimensions higher than 3; only change is termination of hDims string */
			for ( i = 0; i < dims[0]; i++ )
			{
				int[] newDims = new int[dims.length-1];
				for( j = 0; j < (dims.length-1); j++)
					newDims[j] = dims[j+1];
				String hDims = new String("," + (i+1) + higherDims );
				offset = writeMatrix( arrayName, table, newDims, startIndex, hDims );
				startIndex = offset;
			}
			return startIndex;
		}
	}
}
