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
public class Flow 
{
    public int light, car, heavy, bus;
    
    Flow(int l, int c, int h, int b)
    {
        light = l; car = c; heavy = h; bus = b;
    }
    
}
