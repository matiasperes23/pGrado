/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bdi
 */
public class Configuration {
    
    public Integer stage;
    public String instance;
    public String algorithm;
    public Integer parameterCombinations;
    
    public Configuration(Integer stage, String instance, String algorithm, 
            Integer parameterCombinations)
    {
        this.stage                   = stage;
        this.instance                = instance;
        this.algorithm               = algorithm;
        this.parameterCombinations   = parameterCombinations;
    }
    
    public String getAlgorithm()
    {
        return algorithm;
    }
    
    public String getName(Integer combination)
    {
        if(combination == 0)
            return "E" + stage + "_" + instance + "_" + algorithm;
        else if(combination > 0 && combination <= parameterCombinations)
            return "E" + stage + "_" + instance + "_" + algorithm + "_P" + combination;
        else
            return null;
    }
    
    public List<String> getNames()
    {
        List<String> names = new ArrayList();
        if(parameterCombinations>0)
        {
            for(Integer i = 1; i <= parameterCombinations; i++)
            {
                names.add(this.getName(i));
            }
        }
        else
        {
            names.add(this.getName(0));
        }
        return names;
    }
}
