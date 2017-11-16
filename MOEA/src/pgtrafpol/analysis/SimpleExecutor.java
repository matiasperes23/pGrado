/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.analysis;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author bdi
 */
public class SimpleExecutor 
{
    
    public static  Map<Integer, Double> executeSimple(String instance_path, String results_path) throws IOException
    {
        double co = 0, co2 = 0, hc = 0, pmx = 0, nox = 0; int cantVeh = 0; double timeLoss = 0; // fuel = 0;

        String tmp_tl_logics_dir    = instance_path + "/real/";
        String sumo_cfg_path        = instance_path + "/red.sumo.cfg";
        String tl_logic_path        = tmp_tl_logics_dir + "/tl-logic.add.xml";
        String output_trips_path    = tmp_tl_logics_dir + "/tripinfo.xml";
        String output_emission_path = tmp_tl_logics_dir + "/emissions.xml";
        
        // Cargar candidato en archivo de SUMO
        try 
        {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "sumo",
                "-c",
                sumo_cfg_path,
                "-a",
                tl_logic_path,
                "--tripinfo-output",
                output_trips_path,
                "--no-warnings"
            );

            Process sumoProcess = processBuilder.start();
            int exitVal = sumoProcess.waitFor();
            

            if (exitVal == 0) //No errors
            {
                sumoProcess.destroy();
                // Leo emision de contaminantes
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                InputStream in1 = new FileInputStream(output_emission_path);
                XMLStreamReader sr1 = inputFactory.createXMLStreamReader(in1);
                sr1.nextTag(); // Advance to "meandata" element
                sr1.nextTag(); // Advance to "interval" element
                sr1.nextTag(); // Advance to "edge" element
                while (sr1.hasNext()) {
                    if (sr1.isStartElement()) {
                        co  += Double.parseDouble(sr1.getAttributeValue(2));
                        co2 += Double.parseDouble(sr1.getAttributeValue(3));
                        hc  += Double.parseDouble(sr1.getAttributeValue(4));
                        pmx += Double.parseDouble(sr1.getAttributeValue(5));
                        nox += Double.parseDouble(sr1.getAttributeValue(6));
                        //fuel += Double.parseDouble(sr1.getAttributeValue(7));
                    }
                  sr1.next();
                }
                
                sr1.close();
                in1.close();

                // Leo cantidad de vehiculos que llegaron a destino y tiempo perdido debido a velocidad baja
                InputStream in2 = new FileInputStream(output_trips_path);
                XMLStreamReader sr2 = inputFactory.createXMLStreamReader(in2);
                sr2.nextTag(); // Advance to "tripinfos" element
                sr2.nextTag(); // Advance to "tripinfo" element

                while (sr2.hasNext()) {
                    if (sr2.isStartElement()) {
                        cantVeh++;
                        timeLoss += Double.parseDouble(sr2.getAttributeValue(13));
                    }
                    sr2.next();
                }
                
                sr2.close();
                in2.close();            
            } 
            else 
            {               
                System.out.println("GenotipoEvaluator - Error de ejecucion de SUMO:");
                
                InputStreamReader isr = new InputStreamReader(sumoProcess.getInputStream());
                BufferedReader buff = new BufferedReader (isr);
                String line;
                while((line = buff.readLine()) != null)
                    System.out.println(line);

                isr = new InputStreamReader(sumoProcess.getErrorStream());
                buff = new BufferedReader (isr);
                while((line = buff.readLine()) != null)
                    System.out.println(line);
                System.out.println();
                
                sumoProcess.destroy();
            }
        } 
        catch (Exception ex) 
        {
            System.out.println("GenotipoEvaluator - Excepcion al evaluar Genotipo:");
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
        
        Map<Integer, Double> objetivosSolReal = new HashMap<Integer, Double>();
        objetivosSolReal.put(0, co);
        objetivosSolReal.put(1, co2);
        objetivosSolReal.put(2, hc);
        objetivosSolReal.put(3, pmx);
        objetivosSolReal.put(4, nox);
        objetivosSolReal.put(5, (double)(-1)*cantVeh);
        objetivosSolReal.put(6, timeLoss);
        
        
        // Imprimo el resultado en un archivo 
        PrintStream ps = null;
        try {
            ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(tmp_tl_logics_dir + "REAL.set"))));
            
            ps.println(co + "\t" + co2 + "\t" + hc + "\t" + pmx + "\t" + nox + "\t" + cantVeh + "\t" + timeLoss);
            ps.println(co/1000 + "\t" + co2/1000 + "\t" + hc/1000 + "\t" + pmx/1000 + "\t" + nox/1000 + "\t" + cantVeh + "\t" + timeLoss);
            double emisions = (co/100 + co2/10000 + hc/10 + pmx + nox/10);
            ps.println(emisions + "\t" + cantVeh + "\t" + timeLoss);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
        
        /*
        System.out.println("CO          " +  co);
        System.out.println("CO2         " +  co2);
        System.out.println("HC          " +  hc);
        System.out.println("PMx         " +  pmx);
        System.out.println("NOx         " +  nox);
        System.out.println();
        
        //double emisions = (co/10 + nox/10);
        System.out.println("Emisions    " + emisions);
        System.out.println();
        System.out.println("CantVeh     " +  cantVeh);
        System.out.println("TimeLoss    " +  timeLoss);
        */
        
        return objetivosSolReal;
    }
    
}
