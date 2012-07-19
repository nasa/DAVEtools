//  SLFileWriter.java
//  
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave2sl;

import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;

/**
 *
 * Provides special output functions for writing Simulink files.
 *
 * @author Bruce Jackson, NASA Langley
 *
 * <ul>
 *  <li>020419 E. B. Jackson</li>
 *  <li>040225 Modified for 0.4</li>
 * </ul>
 *
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * @version 0.9
 *
 **/

public class SLFileWriter extends FileWriter
{

	/**
	 * Current amount of indentation in output file
	 */

	int indent;

	/**
	 * Our parent diagram
	 */

	SLDiagram diagram;

	/**
	 * Name of embedded subsystem
	 */

	String subsysName;

	/**
	 *  Height of subsystem block
	 */

	int subsysHeight;

	/**
	 *  Grid spacing preference of Simulink
	 */

	int gridSpacing = 5;

	/**
	 *  Distance from top of window to top input/output line of subsystem block
	 */

	int subsysTopLine = 65;  // needs to be evenly divisible by gridSpacing

	/**
	 *  Vertical spacing between I/O ports on top-level diagram on side with greater number of ports
	 */

	int greaterNumPortSpacing = 30;

	/**
	 *  Vertical spacing between I/O ports on top-level diagram on
	 *  side with lesser number of ports
	 */

	int lesserNumPortSpacing = 30;

	/**
	 *  Vertical offset of first I/O line on top-level diagram on side
	 *  with greater number of ports, +down
	 */

	int greaterNumPortVertOffset;

	/**
	 *  Vertical offset of first I/O line on top-level diagram on side
	 *  with fewer number of ports, +down
	 */

	int lesserNumPortVertOffset;

	/**
	 *  location of vertical centerline of system in window
	 */

	int subsysVertCL = 300;

	/**
	 *  width of subsystem block
	 */

	int subsysWidth = 180;

	/**
	 *  whitespace around subsystem block
	 */

	int subsysPad = 50;

	/**
	 *
	 *  Constructor for SLFileWriter; derived from FileWriter
	 *  but specialized to write Simulink-model creation scripts.
	 *
	 * @param fileName Name of file to open
	 *
	 */

	public SLFileWriter(SLDiagram theDiagram, String fileName) throws IOException
	{
		super(fileName);
		this.diagram = theDiagram;
		this.indent = 0;
		this.subsysName = "unknown";
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
		int i;
		for(i = 0; i< this.indent; i++)
			super.write(" ");
		super.write( cbuf + "\n" );
	}

	/**
	 *
	 * Generates add_block() line within subsystem for arbitrary block
	 * 
	 * @param protoPath   Name of simulink block type
	 * @param blockName   Caption of block
	 * @param paramString Parameter values, such as position
	 *
	 */

	public void addBlock( String protoPath, String blockName, String paramString ) 
	throws IOException {

		String cmdline = "add_block('" + protoPath + "','" + 
		this.subsysName + "/" + blockName + "'";
		if (paramString.length() > 0) 
			cmdline = cmdline + "," + paramString;
		cmdline = cmdline + ");";
		this.writeln(cmdline);
	}


	/**
	 *
	 * Generates add_block() line within subsystem for built-in block
	 * 
	 * @param builtInType Name of simulink block type
	 * @param blockName   Caption of block
	 * @param paramString Parameter values, such as position
	 *
	 */

	public void addBuiltInBlock( String builtInType, String blockName, String paramString ) 
	throws IOException {

		String protoType = "built-in/" + builtInType;
		this.addBlock( protoType, blockName, paramString );
	}


	/**
	 *
	 * Generates set_param() command for given subsystem block
	 *
	 * @param blockName  Caption of block
	 * @param paramName  Name of parameter to set
	 * @param paramValue Value of parameter to set
	 *
	 */

	public void setParam( String blockName, String paramName, String paramValue )
	throws IOException {
		this.writeln("set_param('" + this.subsysName + "/" + blockName + "','"
				+ paramName + "'," + paramValue + ");");
	}


	/**
	 *
	 * Generates add_line() within subsystem for given line
	 * 
	 * @param fromBlock   Name of simulink block source
	 * @param fromPort    Number of source block's port
	 * @param toBlock     Name of simulink block destination
	 * @param toPort      Number of destination block's port
	 * @param name        Name of signal to label
	 *
	 */

	public void addLine( String fromBlock, int fromPort, String toBlock, int toPort,
			String name ) throws IOException {

		String cmdline = "h=add_line('" + this.subsysName + "','" +
		fromBlock + "/" + fromPort + "','" +
		toBlock   + "/" + toPort   + "','autorouting','on');";
		this.writeln(cmdline);
		this.writeln("set_param(h,'Name','" + name + "');");
	}


	/** 
	 *
	 * Writes the header for a Simulink representation.  
	 * <p> 
	 * We create the 'create' M-script code that opens an
	 * appropriately sized model window and inserts an appropriately
	 * sized subsystem for further population. We also write some
	 * header documentation into the 'create' m-script. We then write
	 * the m-script command to load both the simulink library and run
	 * the setup script. If necessary, we embed an enable block in the
	 * newly-created subsystem.
	 *
	 * <p> The vertical sizing is a bit tricky: the rectangle that
	 * gets drawn is actually somewhat smaller than what is specified
	 * in the appropriate <code>Position</code> MDL directive. A
	 * subsystem block that is required to be 305 pixels high actually
	 * is drawn 275 pixels high - does this position include the title
	 * block?
	 *
	 * <p>It appears the right way is to fix the top line on the
	 * closest vertical grid line divisible by 5, then allow 12-14
	 * vertical spacing above top line and below bottom line.
	 *
	 * @param modelName Name of model being implemented
	 *
	 */

	public void writeSLHeader(String modelName )
	throws IOException
	{
		int lesserNumPorts;
		int greaterNumPorts;
		int windowVertOffset = 200;
		int windowHorzOffset =  70;

		this.subsysName = modelName + "/" + modelName;

		// set height of block to have min spacing between inputs and outputs

		if (this.diagram.getNumOutputs() > this.diagram.getNumInputs()) {
			greaterNumPorts = this.diagram.getNumOutputs();
			lesserNumPorts  = this.diagram.getNumInputs();
		} else {
			greaterNumPorts = this.diagram.getNumInputs();
			lesserNumPorts  = this.diagram.getNumOutputs();
		}

		//	subsysHeight = (int) (((double)numPorts - 0.5)*(double)greaterNumPortSpacing);
		subsysHeight = greaterNumPorts * greaterNumPortSpacing; // this leaves 1/2 portSpace above and below

		int totalGreaterNumPortVertSpace = greaterNumPortSpacing*(greaterNumPorts - 1);
		this.greaterNumPortVertOffset = (subsysHeight - totalGreaterNumPortVertSpace)/2;

		// set spacing & offset for smaller number of ports
		lesserNumPortSpacing = this.gridSpacing*
		(int) (Math.floor(( this.subsysHeight/
				((double)(lesserNumPorts)-0.5) )/
				(double)this.gridSpacing));

		int totalLesserNumPortVertSpace = lesserNumPortSpacing*(lesserNumPorts - 1);
		this.lesserNumPortVertOffset = (subsysHeight - totalLesserNumPortVertSpace)/2;

		// adjust to closest grid divisible by this.gridSpacing
		int topOfBox = subsysTopLine - greaterNumPortVertOffset;
		int lesserNumPortFirstLineVertPos = topOfBox + this.lesserNumPortVertOffset;
		float gridPos = (float)lesserNumPortFirstLineVertPos/(float)this.gridSpacing;
		int intGridPos = Math.round(gridPos);
		this.lesserNumPortVertOffset = intGridPos*this.gridSpacing - topOfBox;

		// write some header documentation
		this.writeln("%");
		this.writeln("% Invoke this script to create the corresponding model");
		this.writeln("%");

		// write creation script header - create and open the new system
		this.writeln("");
		this.writeln("% Create the system (either model or library) and open it");
		this.writeln("");
		this.write("new_system('" + modelName + "'");
		if (this.diagram.getLibFlag()) {
			this.writeln(",'Library');");
		} else {
			this.writeln(",'Model');");
		}
		this.writeln("load_system('simulink');");

		// Open the parent diagram and adjust size
		this.writeln("open_system('" + modelName + "');");
		this.writeln("set_param('" + modelName + "','Location', ["
				+  windowHorzOffset + ", "
				+  windowVertOffset + ", "
				+ (windowHorzOffset + 2*subsysVertCL) + ", "
				+ (windowVertOffset + subsysHeight + 2*subsysPad) + "]);");
		// removes annoying partially-drawmn window header in X11
		this.writeln("% pause(0.1); - disabled with R2006b/7.3; causes typeahead buffer overflow"); 

		// execute the setup script (which will be written and available by then)
		this.writeln("");
		this.writeln("% Execute the setup script to create the data structure");
		this.writeln("% in the model workspace for clarity");
		this.writeln("");

		// Move parameter data to model workspace, so it's stored with
		// the model and we don't have to carry around setup file
		this.writeln("mws = get_param('" + modelName + "','modelworkspace');");
		this.writeln("if ~exist('mws')");
		this.writeln("  error('Unable to open model workspace - aborting');");
		this.writeln("end");
		this.writeln("mws.clear;");
		this.writeln("mws.evalin('" + modelName + "_data','" 
				+ modelName + "_setup');");
		this.writeln("clear mws;");

		// create a subsystem block with proper dimensions
		this.writeln("");
		this.writeln("% Create the subsystem with proper size to accomodate in/out ports");
		this.writeln("");
		this.write("add_block('built-in/subsystem','" + this.subsysName + "'");
		this.writeln(",'Position',["
				+ (subsysVertCL - subsysWidth/2) + ", " +  topOfBox + ", " 
				+ (subsysVertCL + subsysWidth/2) + ", " + (topOfBox + subsysHeight)
				+ "]);");

		// add enable block inside subsystem if requested
		if (this.diagram.getEnabledFlag()) {
			this.writeln("");
			this.writeln("% add a single enable block inside subsystem");
			this.writeln("");
			this.write("add_block('built-in/EnablePort','" + this.subsysName + "/Enable',");
			this.writeln("'Position',[15, 15, 35, 35]);");
		}

		this.writeln("");
		this.writeln("% Flesh out subsystem with internal blocks");
		this.writeln("");

		this.indent = 4;
	}


	/** 
	 *
	 *  Writes the footer for a Simulink representation. This includes
	 *  the inports and outports used for verification testing and the
	 *  annotation that writes the version number of DAVEtools that
	 *  generated the Simulink block.
	 *
	 * @param version String with version number to embed
	 * @param modelName Name of model being implemented
	 *
	 */

	public void writeSLFooter( String version, String modelName )
	throws IOException
	{
		int count;
		int topOfPort;
		ArrayList<String> names;
		String name;
		Iterator<String> it;
		int vertSpace;
		int portWidth  = 30;  // size of In/Out port oval
		int portHeight = 14;  // height of I/O port oval
		int extraPad   = 40;  // extra horizontal whitespace between I/O port and subsystem
		int inportX    = this.subsysVertCL - (this.subsysWidth/2 + subsysPad + portWidth + extraPad);
		int outportX   = this.subsysVertCL + (this.subsysWidth/2 + subsysPad + extraPad);
		SimpleDateFormat timeStamp = new SimpleDateFormat("EE MMM d HH:mm:ss yyyy");

		// Correct port numbers for outputs
		this.writeln("");
		this.writeln("% Correct output numbers");
		this.writeln("");
		names = this.diagram.getOutputNames();
		it = names.iterator();
		count = 0;
		while (it.hasNext()) {
			count++;
			name = it.next();
			this.writeln("set_param('" + this.subsysName + "/" + 
					name + "','Port','" + count + "');");
		}

		this.indent=0;

		if (!this.diagram.getLibFlag()) {

			// write inports & lines
			this.writeln("");
			this.writeln("% Top-level inports and lines");
			this.writeln("");

			if (this.diagram.getNumOutputs() < this.diagram.getNumInputs()) {
				vertSpace = this.greaterNumPortSpacing;
				topOfPort = subsysTopLine - portHeight/2;
			} else {
				vertSpace = this.lesserNumPortSpacing;
				topOfPort = subsysTopLine - portHeight/2 + lesserNumPortVertOffset - greaterNumPortVertOffset;
			}

			count = 0;
			names = this.diagram.getInputNames();
			it = names.iterator();
			while (it.hasNext()) {
				count++;
				name = it.next();
				this.write("add_block('built-in/Inport','" + modelName + "/" + name + "','Position',[");
				this.writeln(inportX + ", " + topOfPort  + ", " + (inportX+portWidth) 
						+ ", " + (topOfPort + portHeight) + "]);");
				this.writeln("add_line('" + modelName + "','" + name + "/1','" + 
						modelName + "/" + count + "');");

				topOfPort = topOfPort+vertSpace;
			}

			// write outports & lines
			this.writeln("");
			this.writeln("% Top-level outports and lines");
			this.writeln("");

			if (this.diagram.getNumOutputs() > this.diagram.getNumInputs()) {
				vertSpace = this.greaterNumPortSpacing;
				topOfPort = subsysTopLine - portHeight/2;
			} else {
				vertSpace = this.lesserNumPortSpacing;
				topOfPort = subsysTopLine - portHeight/2 + lesserNumPortVertOffset - greaterNumPortVertOffset;
			}

			count = 0;
			names = this.diagram.getOutputNames();
			it = names.iterator();
			while (it.hasNext()) {
				count++;
				name = it.next();
				this.write("add_block('built-in/Outport','" + modelName + "/" + name + "','Position',[");
				this.writeln(outportX + ", " + topOfPort  + ", " + (outportX+portWidth) 
						+ ", " + (topOfPort + portHeight) + "]);");

				this.writeln("add_line('" + modelName + "','" + modelName + "/" + count +
						"','" + name + "/1');");
				topOfPort = topOfPort + vertSpace;
			}
		}

		this.writeln("");
		this.writeln("% Add annotation");
		this.writeln("");
		this.writeln("add_block('built-in/Note','" + modelName + "/Auto-generated by DAVE2SL version " + version + "','Position',[" + subsysVertCL + ", 22]);");

		this.writeln("");
		this.writeln("clear h;  % remove handle used for naming lines");
		this.writeln("");

		this.writeln("% Pause so window can be drawn prior to verification");
		this.writeln("");
		this.writeln("% pause(0.5); - disabled with R2006b/7.3; causes typeahead buffer overflow");

		// Run the verification script, if it exists; save file if
		// model verifies or if verification script doesn't exist

		this.writeln("if exist('" + modelName + "_verify') == 2");
		this.writeln("  if " + modelName + "_verify");
		this.writeln("    save_system('" + modelName + "','" + modelName + "');");
		this.writeln("    fprintf('\\n\"" + modelName 
				+ "\" model verified and saved.\\n')");
		this.writeln("  else");
		this.writeln("    fprintf('\\n\"" + modelName 
				+ "\" model NOT VERIFIED; model has NOT been saved.\\n')");
		this.writeln("  end");
		this.writeln("else");
		this.writeln("  save_system('" + modelName + "','" + modelName + "');");
		this.writeln("  fprintf('\\n\"" + modelName + "\" model saved.\\n')");
		this.writeln("end");
		this.writeln("");
		this.writeln("% " + modelName 
				+ " model-building script was auto-generated ");
		this.writeln("% by DAVE2SL version " + version);
		this.writeln("% on " + timeStamp.format( new Date() ) + ".");
	}
}
