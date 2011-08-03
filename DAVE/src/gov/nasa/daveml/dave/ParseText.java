// ParseText
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
 * <p> Object to convert string containing list of comma, tab, or
 *      space-separated values into an array. </p>
 * <p> 031214 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 **/

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * <p>  Class to convert a string containing comma, tab, or
 *      space-separated values into an array. </p>
 *
 **/

public class ParseText
{
    /**
     * the stream tokenizer chosen
     */

    protected StreamTokenizer st;

    /**
     * indicates a good number has been read
     */

    protected boolean goodNumber;

    public ParseText(String inputData)
    {
        StringReader sr = new StringReader(inputData);
        st = new StreamTokenizer(sr);
        st.resetSyntax();                       // all chars ignored
        st.whitespaceChars(0x09, 0x2A); // delimiters: HT to *
        st.whitespaceChars(0x2C, 0x2C);    // add comma to delimiters
        st.wordChars(0x2B,0x2B);                // designate '+' as word component
        st.wordChars(0x2D,0x2e);                // designate '-', '.' as word components
        st.wordChars(0x30,0x39);                // designate '0' through '9' as word chars
        st.wordChars(0x45,0x45);                // 'E' is now part of word
        st.wordChars(0x65,0x65);                // as is 'e'

        //                   st.parseNumbers();
        //                   st.eolIsSignificant(false);
        //      System.out.println(inputData);
    }

    /**
     * Returns next floating point number in input string
     * @return next floating point value in input string
     * @throws IOException
     */
    public double next()
        throws IOException
    {
        goodNumber = false;
        double value = Double.NaN;

        do{
            st.nextToken();
            //  return (double) st.nval;
            switch(st.ttype) {
            case StreamTokenizer.TT_NUMBER:     // in present incarnation, should never select
                //              System.out.println("Input number is '" + st.nval + "'; conversion is ");
                value = st.nval;
                goodNumber = true; 
                break;
            case StreamTokenizer.TT_WORD:
                //              System.out.println("Input word is '" + st.sval + "'; conversion is ");
                try {
                    value = Double.parseDouble(st.sval);
                    goodNumber = true;
                } catch (NumberFormatException e) {goodNumber = false;}
                break;
            case StreamTokenizer.TT_EOL:
                //              System.out.println("End of line");
                break;
            case StreamTokenizer.TT_EOF:
                //              System.out.println("End of file");
                break;
            default:
                System.out.println("Ordinary character, value is " + st.ttype);
            }
        }
        while ((!goodNumber) && (st.ttype != StreamTokenizer.TT_EOF));
        return value;
    }

    /**
     * Indicates if end-of-file has been reached in input string
     * @return true if at end-of-file
     */
    public boolean eof()
    {
        return (st.ttype == StreamTokenizer.TT_EOF);
    }

    /**
     * Indicates if previous word read was a valid number
     * @return true if previous number was valid
     */
    public boolean validNumber()
    {
        return ( goodNumber && 
                 (st.ttype != StreamTokenizer.TT_EOF));

    }
    
    /**
     *
     * <p> Converts string of comma-, space- or tab-separated
     * floating-point values to ArrayList of Doubles. </p>
     *
     **/

    public ArrayList<Double> toList() throws IOException
    {
        ArrayList<Double> al = new ArrayList<Double>(10);

        while (!this.eof() )
            {
                Double dbl = new Double(this.next());
                if (this.validNumber())
                    al.add( dbl );
            }

        return al;
    }

    /**
     *
     * <p> Static version of routine toList </p>
     *
     * @param values String containing comma- or whitespace-separated values.
     *
     **/

    static public ArrayList<Double> toList(String values) throws IOException
    {
        ParseText pt = new ParseText(values);
        return pt.toList();
    }

    /** 
     *
     * <p> This class defines a main() method to test the ParseText logic. To
     *  use, run : java ParseText\$Test
     *
     **/

    public static class Test {
        /** Entry point for testing purposes **/
        public static void main(String[] args)
            throws IOException
        {
            ParseText pt;

            if (args.length < 1)
                pt = new ParseText("  ,  1.0, +2.9,3.2,+4       -5.,Z,9.99E99  ");
            else
                pt = new ParseText(args[0]);

            //              while (!pt.eof() )
            //                  {
            //                      System.out.println(pt.next());
            //                  }
            
            ArrayList<Double> theList = pt.toList();

            System.out.println();
            System.out.println("Returned array:");
            System.out.println();
            System.out.print("  [");

            Iterator<Double> listIterator = theList.iterator();

            while(listIterator.hasNext())
                {
                    System.out.print(listIterator.next());
                    if(listIterator.hasNext()) System.out.print(", ");
                }

            System.out.println("]");
        }
    }
}
