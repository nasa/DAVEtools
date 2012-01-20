/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.daveml.dave2otis;

import gov.nasa.daveml.dave.Model;

import gov.nasa.daveml.dave.Signal;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds common methods for both equation and table writers
 * @author ebjackso
 */
public abstract class OtisWriter extends FileWriter {
    Map<String,String> idMap; /** mapping of Std AIAA names to OTIS names   */
        Model  ourModel;

    public OtisWriter(Model theModel, String tableFileName) 
            throws IOException {
        super( tableFileName );
        ourModel = theModel;
    }
    /**
     * Translates variable identified by varID into OTIS ABLOCK name
     * if available.
     * 
     * Uses the varID to fetch the variable; if the variable is a standard AIAA 
     * variable, and there is a matching OTIS variable name, it returns the OTIS
     * name.
     * 
     * @param varID variable ID to translate into OTIS name
     */
    
    protected String translate( String varID ) {
        
        String output = varID;
        if (idMap == null)
            this.setupMap(); // initialize the mapping of AIAA -> OTIS varnames
        
        // find variable (signal) definition in source XML
        Signal signal = ourModel.getSignals().findByID(varID);
        
        // if standard, do lookup in map
        if( signal.isStdAIAA() ) {
            String aiaaName = this.getAIAAName( signal );
            String otisName = idMap.get(aiaaName);
            if (otisName != null) {
                output = otisName;
            }
        }
        return output;
    }

    /**
     * Build the map from Standard AIAA (S-119 defined) variable names to OTIS
     */
    private void setupMap() {
        idMap = new HashMap<String, String>();
        //         AIAA Standard Name_units  OTIS
        idMap.put("angleOfAttack_rad"      , "ALPHA" );
        idMap.put("angleOfAttack_deg"      , "ALPHAD");
        idMap.put("angleOfSideslip_rad"    , "BETA"  );
        idMap.put("angleOfSideslip_deg"    , "BETAD" );
        idMap.put("totalCoefficientOfDrag" , "CD"    );
        idMap.put("totalCoefficientOfLift" , "CL"    );
        idMap.put("mach"                   , "MACH"  );
        idMap.put("eulerAngle_rad_Roll"    , "PHIG"  );
        idMap.put("eulerAngle_deg_Roll"    , "PHIGD" );
        idMap.put("eulerAngle_rad_Yaw"     , "PSIG"  );
        idMap.put("eulerAngle_deg_Yaw"     , "PSIGD" );
        idMap.put("dynamicPressure_lbf_ft2", "Q"     );
        idMap.put("eulerAngle_rad_Pitch"   , "THETG" );
        idMap.put("eulerAngle_deg_Pitch"   , "THETGD");
        idMap.put("trueAirspeed_ft_s"      , "VEL"   );
        // TODO - needs expansion - above for proof-of-concept
    }

    
    /**
     * Convert signal name (pseudo-AIAA name) into true AIAA name
     * that potentially has both units and/or axis id appended
     * @param signal
     * @return 
     */
    private String getAIAAName(Signal signal) {
        String varName  = signal.getName();
        String units    = signal.getUnits();
        String aiaaName = varName;
        String axisName = "";

        // now deal with eulerAngle_deg_Roll; scalar AIAA name would 
        // be coded as eulerAngle_Roll; must insert units ahead of _Roll
        int underbar = varName.indexOf("_");
        int varNameLen = varName.length();

        if (underbar > 0) {
            axisName = varName.substring(underbar,(varNameLen-underbar));
            aiaaName = varName.substring(underbar);
        }
        if (!units.equalsIgnoreCase("nd")) {
            aiaaName = varName + "_" + units;
        }
        aiaaName += axisName; // add _Roll, e.g.
        
        return aiaaName;
    }
    
}
