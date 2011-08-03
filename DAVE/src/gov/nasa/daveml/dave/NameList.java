// NameList
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

//
// 020514 EBJ
//

package gov.nasa.daveml.dave;

/**
 *
 * <p> Creates and manages unique object names, such as guaranteeing
 *     unique block names for Simulink MDL file. Not to be confused with XML and 
 *     JDOM Namespace object which relate to unique names of XML elements; this 
 *     object ensures block and signal names are unique within a model.</p>
 * <p> 031211 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 * <p> 2010-05-04 Renamed from NameSpace to NameList to avoid confusion with JDOM Namespace</p>
 *
 */

import java.util.ArrayList;

/**
 *
 * <p> NameList represents a list of strings in use. It provides
 * methods to add, delete, and create unique names based on an initial
 * namespace. </p>
 *
 **/

@SuppressWarnings("serial")
public class NameList extends ArrayList<String>
{
    public NameList()  {super();}
    public NameList(int initialCapacity) { super(initialCapacity); }

    /**
     *
     * <p> Changes name to meet namespace requirements, but does not ensure uniqueness </p>
     *
     * Currently is a placeholder for more sophisticated logic
     * 
     * @param s <code>String</code> with proposed name
     * @return String
     *
     **/

    public String  fixName( String s) { return s; }	// returns acceptable filtered version of name


    /**
     *
     * <p> Returns <code>boolean</code> which indicates if supplied name is unique to namespace </p>
     *
     * @param s <code>String</code> with candidate name
     * @return boolean indicating if <b>s</b> is unique or not
     *
     **/

    public boolean isUnique(String s) { return (this.indexOf(s) == -1); }


    /**
     *
     * <p> Changes name to meet namespace requirements, and appends
     * integer until unique name is created. Returns acceptable,
     * unique name</p>
     *
     * @param s <code>String</code> with candidate name
     * @return String containing unique-ified <b>s</b> name
     *
     **/

    public String addUnique(String s)
    {
	String name = this.fixName(s);	// perform any unique filtering
	if (this.isUnique(s)) 
	    super.add(s);
	else
	    {
//System.out.print("-->Non-unique name " + s + " found; changed to ");
		int suffix = 1;
		while( !this.isUnique(s + suffix) ) suffix++;
		name = s + suffix;
		super.add(name);
//System.out.println(name);
	    }
	return name;
    }
}
