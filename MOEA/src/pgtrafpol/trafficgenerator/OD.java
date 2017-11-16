/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.trafficgenerator;

/**
 *
 * @author bdi
 */
public class OD 
{
    public String[] route;
    
    OD(String[] route)
    {
        this.route = route;
    }
    
    String Origin()
    {
        return route[0];
    }
    
    String Destination()
    {
        return route[route.length-1];
    }
}
