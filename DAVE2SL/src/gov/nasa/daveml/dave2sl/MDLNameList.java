// MDLNameList.java
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave2sl;

import gov.nasa.daveml.dave.NameList;

/**
 *
 * Creates and manages unique names of things with valid MDL identifiers.
 *
 * <p>
 * MDLNameList represents a list of strings in use. It provides
 * methods to add, delete, and create unique names based on an initial
 * names list. It guarantees MDL-compatible names, changing the
 * original if necessary.
 *
 * <p>
 * Modification history:
 *  <ul>
 *   <li>020514 Written</li>
 *   <li>040227 Updated for version 0.5</li>
 *   <li>2010-05-04 Renamed to NameList from NameSpace for clarity</li>
 *  </ul>
 *
 * @author Bruce Jackson {@link <mailto:bruce.jackson@nasa.gov>}
 * @version 0.9
 *
 **/

@SuppressWarnings("serial")
public class MDLNameList extends NameList
{
	/**
	 *
	 * Normal constructor.
	 *
	 **/

	public MDLNameList()  {super();}


	/**
	 *
	 * Constructor that sets initial array capacity.
	 *
	 * @param initialCapacity Estimate of initial capacity
	 *
	 **/

	public MDLNameList(int initialCapacity) { super(initialCapacity); }


	/**
	 *
	 * Static method that converts supplied <code>String</code> to
	 * acceptable Simulink block name.
	 *
	 * <p>
	 * Note that the manual implies all UNICODE is acceptable but I've
	 * found it best to remove punctuation, parens and * brackets, and
	 * convert whitespace into underscores. Returns acceptable name as
	 * <code>String</code> but does not guarantee uniqueness or enter
	 * the name into namespace.
	 *
	 * @param s <code>String</code> containing the original name
	 * @return String
	 *
	 **/

	public static String convertToMDLString(String s)
	{	
		//System.out.println("convertToMDLString called for " + s);
		// replace any whitespace with underbars
		// remove any parentheses or brackets
		// remove any punctation or math operators

		StringBuffer sb = new StringBuffer(s);
		for( int i = 0; i < s.length(); i++) {
			switch (sb.charAt(i)) {
			case ' ':
			case '-':
			case ':':
			case '/':
			case '\\':
			case '%':
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case '*':
			case '&':
			case '+':
			case ',':
			case '.':
			case '#':
			case ';':
			case '!':
			case '`':
			case '~':
			case '^':
				sb.setCharAt(i,'_');
			        break;
			default:
			}

		}
		return sb.toString();
	}


	/**
	 *
	 * Converts supplied name <code>String</code> into a
	 * Simulink-acceptable name, as described in class method
	 * <code>convertToMDLString</code>
	 *
	 * @param s <code>String</code> containing candidate name
	 * @return String
	 *
	 **/

	public String fixName(String s)  { return convertToMDLString(s); }

}
