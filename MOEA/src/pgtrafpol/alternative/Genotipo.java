/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.alternative;

/**
 *
 * @author bdi
 */
public class Genotipo 
{
    private static int GenotipoCount = 0;
    
    public final int id;
    public final int[] phasesDurations;
    public final int length;    
    public double co;
    public double co2;
    public double hc;
    public double pmx;
    public double nox;
    public double vd;
    public double tpg;
    public double[] objetivos;
    public double sumaEmisiones;
    
    public Genotipo(int[] phasesDurations)
    {
        this.phasesDurations = phasesDurations.clone(); //Copia limpia
        this.length = phasesDurations.length;
        objetivos = new double[7];
        this.id = GenotipoCount++;
    }
    
    public void setObjective(int i, double obj)
    {
        objetivos[i] = obj;
    }
    
    public double getObjective(int i)
    {
        return objetivos[i];
    }
    
    public void setParameters(double co, double co2, double hc, 
            double pmx, double nox, double vd, double tpg)
    {     
        this.co     = co;
        this.co2    = co2;
        this.hc     = hc;
        this.pmx    = pmx;
        this.nox    = nox;
        this.vd     = vd;
        this.tpg    = tpg;
    }
    
}
