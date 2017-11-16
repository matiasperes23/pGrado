/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.execution;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

/**
 *
 * @author Germán_Ruiz
 */
public class GenotipoEvaluator extends AbstractProblem implements Serializable {

    //private static final long serialVersionUID = 5790638151819130066L;
    
    private Problem problem;
    private String problem_path, sumo_cfg_path;
    private String tmp_tl_logics_dir, tmp_sumo_output_dir;
    
    public String getTempLogicsDir()
    {
        return tmp_tl_logics_dir;
    }
    
    public String getTempSumoOutputDir()
    {
        return tmp_sumo_output_dir;
    }

    public GenotipoEvaluator(String tmp_tl_logics_dir, String tmp_sumo_output_dir) 
    {
        // Numero de variables de decision y numero de objetivos
        super(Problem.getProblem().getNumberOfVariables(), Problem.getProblem().getNumberOfObjectives());  
        problem         = Problem.getProblem();
        sumo_cfg_path   = problem.getCfgPath();
        this.tmp_tl_logics_dir = tmp_tl_logics_dir;
        this.tmp_sumo_output_dir = tmp_sumo_output_dir;
    }

    @Override
    public void evaluate(Solution solution) 
    {
        double co = 0, co2 = 0, hc = 0, pmx = 0, nox = 0; int cantVeh = 0; double timeLoss = 0; // fuel = 0;

        String id_exec              = "-" + UUID.randomUUID();
        String tl_logic_path        = tmp_tl_logics_dir   + "/" + "tl"        + id_exec + ".add.xml";
        String output_trips_path    = tmp_sumo_output_dir + "/" + "tripinfo"  + id_exec + ".xml";
        String output_emission_path = tmp_sumo_output_dir + "/" + "emissions" + id_exec + ".xml";
        
        // Cargar candidato en archivo de SUMO
        problem.CreateTLFile(EncodingUtils.getInt(solution), tl_logic_path, output_emission_path);

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
        
        // BORRANDO ARCHIVOS TEMPORALES
        // SUSTITUIDO POR VACIADO DE CARPETAS TEMPORALES
        /*
        try
        {
            Process deleteProcess = Runtime.getRuntime().exec("rm " + output_emission_path + " " + output_trips_path + " " + tl_logic_path);
            deleteProcess.waitFor();
            deleteProcess.destroy();
        } 
        catch (Exception ex) 
        {
            System.out.println("GenotipoEvaluator - Excepcion al borrar archivos de ejecucion:");
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
        */
        
        solution.setObjective(0, co);
        solution.setObjective(1, co2);
        solution.setObjective(2, hc);
        solution.setObjective(3, pmx);
        solution.setObjective(4, nox);
        solution.setObjective(5, -cantVeh); // Niego para maximizar cantVeh
        solution.setObjective(6, timeLoss);
    }
    
    @Override
    public Solution newSolution() {
        Solution solution = new Solution(numberOfVariables, numberOfObjectives);

        int nTLlogics = this.problem.getN_tl_logic();
        List<Logic> tls_logic = this.problem.getTl_logic();
        int varCont = 0;
        Logic logica;
        for (int i = 0; i < nTLlogics; i++) 
        {         
            logica = tls_logic.get(i);
            
            // Offset
            solution.setVariable(varCont, EncodingUtils.newInt(0, logica.max_offset_duration)); 
            varCont++;

            // Luces Verdes, Rojas
            for(int j = 0; j < logica.n_red_green_phases; j++)
            {
                // Se asignan duraciones en el intervalo [5,60] segundos para verdes y rojas
                // Para amarillas se usa duracion fija de 4 seg, no se consideran en las variables
                solution.setVariable(varCont, EncodingUtils.newInt(problem.getRedGreenMinDuration(), problem.getRedGreenMaxDuration()));
                varCont++;
            }
        }
        
        return solution;
    }

}
