//  TreeFileWriter.java
//
//    Provides special output functions for writing text file representations
//      of XML-derived DAVE networks.
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.
//
//    020419 E. B. Jackson <bruce.jackson@nasa.gov>
//

package gov.nasa.daveml.dave;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class TreeFileWriter extends FileWriter
{
    /**
     *
     * <p> Constructor for TreeFileWriter; derived from FileWriter
     *  but specialized to write Simulink .mdl files 
     *
     * @param fileName Name of file to open
     * @throws IOException
     *
     */

    public TreeFileWriter(String fileName) throws IOException
    {
        super(fileName);
    }

    /**
     *
     * <p> Addeds newline to the end of each write </p>
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
     * <p> Writes newline </p>
     *
     */

    public void writeln() throws IOException
    {
        super.write( "\n" );
    }

    /**
     *
     * <p> This method writes out contents of DAVE network </p>
     *
     **/

    public void describe( Model m ) throws IOException
    {
        this.writeln("Contents of model:");
        this.writeln();

        // List inputs

        this.writeln("Number of inputs: " + m.getNumInputBlocks());
        this.writeln();

        int i = 1;
        Iterator<VectorInfo> inputIterator;
        try {
            inputIterator = m.getInputVector().iterator();
            while (inputIterator.hasNext()) {
                VectorInfo input;
                input = inputIterator.next();
                this.write(i + " ");
                this.write(input.getName());
                this.writeln(" (" + input.getUnits() + ")");
                i++;
            }
        } catch (DAVEException ex) {
            this.writeln("DAVEException trying to get input vector");
        }
        this.writeln();

        // List outputs
        this.writeln("Number of outputs: " + m.getNumOutputBlocks());
        this.writeln();
        i = 1;
        Iterator<VectorInfo> outputIterator;
        try {
            outputIterator = m.getOutputVector().iterator();
            while (outputIterator.hasNext()) {
                VectorInfo output;
                output = outputIterator.next();
                this.write(i + " ");
                this.write(output.getName());
                this.writeln(" (" + output.getUnits() + ")");
                i++;
            }
        } catch (DAVEException ex) {
            this.writeln("DAVEException trying to get output vector");
        }
        this.writeln();

        this.writeln("Number of signals: " + m.getNumSignals());
        this.writeln();

        // List signals & info
        i = 1;
        Iterator<Signal> signalIterator = m.getSignals().iterator();
        while (signalIterator.hasNext()) {
            write(i + " ");
            (signalIterator.next()).describeSelf( (FileWriter) this );
            this.writeln();
            i++;
        }

        this.writeln();
        this.writeln("Number of blocks: " + m.getNumBlocks());
        this.writeln();

        // List blocks & info
        i = 1;
        Iterator<Block> blockIterator = m.getBlocks().iterator();
        while (blockIterator.hasNext()) {
            write(i + " ");
            (blockIterator.next()).describeSelf( (FileWriter) this );
            this.writeln();
            i++;
        }
    }
}
