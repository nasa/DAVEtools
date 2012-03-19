README.txt for DAVE_tools directory
Bruce Jackson, NASA Langley Research Center
<mailto:bruce.jackson@nasa.gov?subject=DAVEtools_0.9>

                       ***************************
                       **                       **
                       ** DAVEtools version 0.9 **
                       **                       **
                       ***************************

INTRODUCTION:

This directory contains Java source files (.java) and an archive (.jar
file) for three tools to manipulate dynamic aerospace vehicle exchange
markup language (DAVE-ML) files.

This utility builds and runs under Java SE 6 under Mac OS X
10.6.x; I haven't tested it with later versions of Java or other
operating systems. DAVEtools 0.9.1 requires Java SE 6 (1.6.0) or
higher for its inclusion of the XMLCatalogResolver class.

Both tools use both JDOM (www.jdom.org), a Java-based XML
parser built on SAX technology, and Xerces, a Java-based XML
parser. JDOM version 1.1.1 and xerces version 2.9.0 are included in
the DAVE/vendor/lib directory and are embedded in the compiled
DAVEtools.jar file. DAVEtools have not been tested with later releases
of JDOM or Xerces.

The first tool, 'dave,' generates a Java-based model of the DAVE-ML
described model, and provides some simple utility features.

Syntax:

     java -jar DAVE/DAVE.jar [switches] filename.dml
 (or java -cp DAVEtools.jar gov.nasa.daveml.dave.DAVE [switches] filename.dml)

The basic 'dave' utility is fairly immature and uninteresting at this
stage, since it can only:

  o Parse a DAVE-ML static model (DAVEfunc) into an internal Model
    object,

  o Automatically run embedded check-cases against that model,

  o Provide information about the model (-count),

  o Generate a listing file (-list) of the blocks and signals
    involved, and
  
  o Generate output vectors (-eval) for an arbitrary input vector.

  o Generate internal variable information (-internal) for debugging.

'dave' is intended to be a building block for more useful programs to
translate DAVE-ML into other formats.


The second utility, 'dave2sl,' builds on dave to creates Simulink
representations of DAVE-ML models.

Syntax:

     java -jar DAVEtools.jar [switches] <filename.dml>
 (or java -cp  DAVEtools.jar gov.nasa.daveml.dave2sl.DAVE2SL \
                             [switches] <filename.dml> )

where [switches] can be any combination of

 -c[ount]	provide information about the model

 -d[ebug]	provide some debugging text as it runs

 -w[arnruntime] generates Matlab warnings when breakpoint values are
		clipped

 -l[ib]		generates Library instead of Model

 -e[nabled]	generates a subsystem with an Enable port; outputs
		forced to zero when subsystem is disabled.


dave2sl will create (if successful) two or three output files:

  filename_create.m a Matlab script that builds 'filename.mdl'
  filename_setup.m  a Matlab script that loads data for model
  filename_verify.m a Matlab script that verifies the model

The verification script will be created only if the DAVE-ML file
contains check case data for the model. 

To create the equivalent Simulink model, invoke the 'create' script
from any Matlab/Simulink (Release 2009a or later) prompt; it will use
the other two files to create and verify the model. At this point the
'create' script can be discarded; the 'verify' script can be run
again, and the 'setup' script should be retained.


The third utility, ModelSweeper, 


     java -jar ModelSweeper.jar 
 (or java -cp  DAVEtools.jar gov.nasa.dave.sweeper.ModelSweeperUI)

allows one to select a DAVE-ML file and then visualize any output
signal as a function of two inputs in a 3D grid.


INSTALLATION:

This package distribution include two open-source Java tools: JDOM
(www.jdom.org), and Xerces (xerces.apache.org). This version of
DAVEtools has been tested with the included jdom-1.1.1 and
xerces-2.9.0 distributions.

DAVEtools.jar has a default entry point that invokes the
'dave2sl' DAVE-ML to Simulink translator. The simpler 'dave' program
can be chosen by invoking slightly different syntax; see the two
command-line invocations above.

To build a new copy of DAVEtools, you'll need the Ant build system;
download same from http://ant.apache.org .

Installation procedure

1) Un-zip the DAVEtools archive into some convenient location on your
   Java-capable system. This will create a directory tree named
   something like 'DAVE_tools_0.9.0-56cd78g'.

2) Change directory (cd) to the DAVEtools top-level directory.

3) Run 'ant' from the command line to build the DAVEtools.jar file.

4) Alternatively, you can run 'ant doc' to create the HTML
   documentation tree (subsequently found in the 'doc'
   subdirectory).

5) If you're feeling lucky, try running the unit tests - you'll have
   to obtain and install a copy of 'junit'. Then run 'ant test'.
   These tests are not yet complete. 

6) Also you can try to run the DAVE2SL validation tests ('ant
   app-test').  These tests, which will exercise the DAVE-ML to
   Simulink converter dave2sl, require both a GNU-compatible 'make'
   and a recent Matlab(r)/Simulink(r) installation to be available
   from the command line.

================================================

V 0.9.2: Corrected bug in DAVE that didn't recognize use of binary
        minus in MathML2 calculations; fixed bug whereby anonymous
        tables shared the same matlab setup data; corrected sense of
        return codes from DAVE and DAVE2SL. Improved integration test
        framework. Corrected bug in ModelSweeper that caused program
        to crash if no checkcases were defined.

V 0.9.1: Added support for minValue and maxValue attributes of
        variableDef as allowed in DAVE-ML 2.0 RC 4; fixed error
        reporting in creation script generation (aborts were returning
        exit status 0 but now report 1).

V 0.9.0: Changed to support the default DAVE-ML namespace (xmlns);
	added entity refs to make Eclipse IDE happy; changed for using
	'build' subdirectories to 'bin' to make Eclipse convention;
	fleshed out ant build.xml file to rely less on makefiles (but
	a makefile is still needed to run the app_tests of DAVE2SL).

	Corrected the inability of dave to parse MathML 'piecewise'
	elements with more than one 'piece.'

 	Distribution format switched to zip from tar to be more PC
 	friendly.

	Build system switched to Ant from Make (but some hybrid make
	functionality exists, to some version of make is still
	required).

	Source code management system now uses 'git' instead of
	Subversion.

	Now producing a single DAVEtools.jar that includes all
	products for convenience, including dave, dave2sl, modelsweeper,
	xerces and jdom .jar file contents. Slightly different syntax
	used to invoke DAVE2SL (default jar entry point) and DAVE.

Summary of changes since DAVEtools v 0.8.0:

V 0.8.1a: Added support for MathML logical operations (not, and, or,
          xor); switched to ant for build instead of make

Summary of changes since DAVEtools v 0.7.2:

V 0.8.0b: Incorporated NASA open-source license agreement, copyright info

V 0.8.0a: added 'csymbol' element extension support for 'atan2';
	  now checking input arguments for -c|--count forms in DAVE.

         Added support for an immediate <ci> and <cn> sub-elements in a
         <math> element; this is valid MathML but was not supported in
         DAVEtools until now. This allows direct assignment of a
         variable (in the <variableDef> element to either another
         variable (using the content identifier, <ci>, element) or a
         constant numeric value (using the content number, <cn>,
         element). 

	 Verification tests that fail now gives more details on which
	 output parameters fail to compare.

Summary of changes since DAVEtools v 0.6.0 (not released):

V 0.7.2: Developed 'dist' makefile target.

V 0.7.1: Moved data into model workspace instead of base workspace.

V 0.7.0: Now generating 'create' script instead of .mdl directly to
	work around bug in Simulink interpreter regarding order of
	output ports. (rev 75)

Summary of changes since DAVEtools v 0.5.0:

V 0.6.0: Mostly updates to documentation; no major changes.

V 0.5.7: Breakpoint vectors are now written into the same structure as
	 function tables, with _pts appended to name of BP block.

V 0.5.6: Table contents are now written into structure via setup
         script with model-unique name to support multiple models with
         similar structure.

V 0.5.5: Now writes Simulink 6.2 (R14SP2) models

V 0.5.4: No change; moved to new SVN repository

V 0.5.3: Removed xerces as a necessary separate package, and removed
JDOM b-9 from the distribution, per legal beagles. This required
modifying DAVE.java to pass in a base URI for the input file.

V 0.5.2: Wrote Theory of Operation (package.html) for DAVE2SL in
javadoc. Added switches to DAVE2SL for library, enabled subsystem,
version 4 or 5 Simulink, and clip warnings on/off. Fixed bug in sum
block. Some javadoc improvements.

V 0.5.1: Generate Simulink as a subsystem for cleanliness. Corrected
spacing bug in Simulink diagram; adjusted table lookup blocks so
multiple input ports have at least 15 pixel spacing.

BUGS:

The resulting Simulink(r) model, found inside the clean top-level
subsystem diagram, is really ugly (but seems to work).

The 'extrapolate' and 'interpolate' attributes of independentVarPts
and independentVarRef elements are ignored; all lookup tables
generated in dave2sl are linear and are clipped to the input
breakpoint set.

MathML presentation markup is not supported. Only the following
subset of all MathML content markup elements are supported:

  abs
  and
  arccos
  arcsin
  arctan
  cos
  divide
  eq
  geq
  gt
  leq
  lt
  minus
  neq
  not
  or
  piecewise
  plus
  power
  quotient
  sin
  tan
  times
  xor

The following additional function definition is supported, which is
NOT part of MathML 2.0:

  atan2

An error message is returned if other MathML markup elements are
detected.
