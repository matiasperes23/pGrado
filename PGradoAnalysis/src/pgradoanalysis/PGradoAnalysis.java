/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgradoanalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.Analyzer;

/**
 *
 * @author bdi
 */
public class PGradoAnalysis {

    private static String instances_root;
    private static String results_root;
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */    
    public static void main(String[] args) throws IOException 
    {
        instances_root  = args[0]; // Directorio Raiz donde se guardan los escenarios SUMO o instancias - Sin "/" al final.
        results_root    = args[1]; // Directorio Raiz donde se guardan los resultados de la ejecucion - Sin "/" al final. 
        
        //instances_root  = "C:/Users/bdi/Google Drive/pGrado/Ejecuciones/Escenarios/";
        //results_root    = "C:/Users/bdi/Google Drive/pGrado/Ejecuciones/Resultados/";
        
        instances_root  = "C:/Prueba/Escenarios/";
        results_root    = "C:/Prueba/Resultados/";
        
        analysisStage1();
        //analysisStage2();
        //analysisStage3();
        
        System.out.println("Fin de Analisis.");
    }
    
    public static void analysisStage1()
    {
        Integer stage = 1;
        String[] instances = {"IC1", "IC2"};
        
        System.out.println("Inicio Analisis Etapa" + stage + "...");
        
        for(String instance : instances)
        {
            System.out.println("Instancia " + instance + "...");
            
            List<Configuration> configurations = new ArrayList();
            configurations.add(new Configuration(stage,instance, "NSGAII", 3));
            configurations.add(new Configuration(stage,instance, "GDE3",   3));
            configurations.add(new Configuration(stage,instance, "MOEAD",  3));

            String reference_set_path = generateReferenceSet(stage, 
                    instance, 
                    configurations);

            analyzeWithReferenceSet(configurations, reference_set_path);
            
            System.out.println("Completado - Instancia " + instance + ".");
        }
    }
    
    public static void analysisStage2()
    {
        Integer stage = 2;
        String[] instances = {"IC1", "IC2", "IC3"};
        
        System.out.println("Inicio Analisis Etapa" + stage + "...");
        
        for(String instance : instances)
        {
            System.out.println("Instancia " + instance + "...");
            
            List<Configuration> configurations = new ArrayList();
            configurations.add(new Configuration(stage,instance, "NSGAII", 9));
            configurations.add(new Configuration(stage,instance, "GDE3",   9));
            configurations.add(new Configuration(stage,instance, "MOEAD",  9));

            String reference_set_path = generateReferenceSet(stage, 
                    instance, 
                    configurations);

            analyzeWithReferenceSet(configurations, reference_set_path);
            
            System.out.println("Completado - Instancia " + instance + ".");
        }
    }
    
    public static void analysisStage3()
    {
        Integer stage = 3;
        String[] instances = {"IC1", "IC2", "IC3"};
        
        System.out.println("Inicio Analisis Etapa" + stage + "...");
        
        for(String instance : instances)
        {
            System.out.println("Instancia " + instance + "...");

            List<Configuration> configurations = new ArrayList();
            configurations.add(new Configuration(2,instance, "NSGAII", 9));
            configurations.add(new Configuration(2,instance, "GDE3", 9));
            configurations.add(new Configuration(2,instance, "MOEAD", 9));
            configurations.add(new Configuration(stage, instance, "ENSGAII", 4));

            String reference_set_path = generateReferenceSet(stage, instance, configurations);
            
            configurations = new ArrayList();
            configurations.add(new Configuration(stage, instance, "ENSGAII", 4));

            analyzeWithReferenceSet(configurations, reference_set_path);
            
            System.out.println("Completado - Instancia " + instance + ".");
        }
    }
    
    public static void analyzeWithReferenceSet(List<Configuration> configurations, 
            String reference_set_path)
    {        
        for(Configuration config : configurations)
        {
            String results_path = results_root 
                    + "Etapa" + config.stage + "/"
                    + config.instance + "/";
            
            //NEW 
            
            NewAnalyzer algorithm_analyzer = new NewAnalyzer();
            algorithm_analyzer.withProblemClass(PGradoProblem.class, instances_root + config.instance + "/");
            algorithm_analyzer.includeAllMetrics();
            algorithm_analyzer.showAll();
            
            /*
            Analyzer algorithm_analyzer = new Analyzer()
                    .withProblemClass(GenotipoEvaluator.class, 
                            "C:/temp", "C:/temp")
                    .includeAllMetrics()
                    .showAll();
            */
            
            algorithm_analyzer.withReferenceSet(new File(reference_set_path));
            
            TimeAnalyzer time_analyzer = new TimeAnalyzer();

            for(String config_name : config.getNames())
            {
                try 
                {                    
                    algorithm_analyzer.loadAs(config_name, new File(results_path
                            + config_name + "/"
                            + config_name + ".set"));
                    time_analyzer.loadAs(config_name, new File(results_path
                            + config_name + "/" 
                            + config_name + ".times"));
                } 
                catch (IOException ex) 
                {
                    System.out.println("analyze: Excepcion al cargar set " 
                            + config_name + " - " 
                            + "Etapa " + config.stage + " - "
                            + "Instancia " + config.instance + ":");
                    System.out.println(ex.toString());
                }                   
            }
            
            String algorith_analysis_result_path = results_path + config.algorithm 
                    + "_AnalysisResult_" 
                    + config.stage + "_" 
                    + config.instance + ".txt";
            String algorith_time_analysis_result_path = results_path + config.algorithm 
                    + "_TimeAnalysisResult_" 
                    + config.stage + "_" 
                    + config.instance + ".txt";
            
            try 
            {
                algorithm_analyzer.saveAnalysis(new File(algorith_analysis_result_path));
                time_analyzer.saveTimeAnalysis(new File(algorith_time_analysis_result_path));
            } 
            catch (IOException ex) 
            {
                System.out.println("analyze: Excepcion al guardar analisis " 
                            + "Algoritmo " + config.algorithm + " - " 
                            + "Etapa " + config.stage + " - "
                            + "Instancia " + config.instance + ":");
                    System.out.println(ex.toString());
            }
        }
    }
    
    public static String generateReferenceSet(Integer stage, // Etapa Principal
            String instance, // Instancia Principal
            List<Configuration> configurations)
    {
        String instance_path = instances_root + instance + "/";
        
        // NEW
        
        NewAnalyzer analyzer = new NewAnalyzer();
        analyzer.withProblemClass(PGradoProblem.class, instance_path);
        analyzer.includeAllMetrics();
        analyzer.showAll();
        
        /*
        Problem problem = Problem.getProblem();
        problem.Populate(instance_path);
        
        Analyzer analyzer = new Analyzer()
            .withProblemClass(GenotipoEvaluator.class, 
                    "C:/temp", "C:/temp")
            .includeAllMetrics()
            .showAll();
        */
        for (Configuration config : configurations) 
        {
            String results_path = results_root 
                    + "Etapa" + config.stage + "/"
                    + config.instance + "/";
            for(String config_name : config.getNames())
            {
                try 
                {
                    analyzer.loadAs(config_name, new File(results_path
                            + config_name + "/" 
                            + config_name + ".set"));
                } 
                catch (Exception ex) 
                {
                    System.out.println("Main: Excepcion al cargar configuracion " + config_name + ":");
                    System.out.println(ex.toString());
                }
            }
        }
        
        String results_path = results_root 
                + "Etapa" + stage + "/" 
                + instance + "/";
        //analyzer.printAnalysis();
        String reference_set_path = results_path 
                + "AnalysisReferenceSet_" + stage + "_" + instance + ".txt";
        String analysis_result_path = results_path 
                + "AnalysisResult_" + stage + "_" + instance + ".txt";
        try 
        {
            // Guardo Set de Referencia
            analyzer.saveReferenceSet(new File(reference_set_path));
            // Guardo Analisis Completo
            analyzer.saveAnalysis(new File(analysis_result_path));
        } 
        catch (IOException ex) 
        {
            System.out.println("Main: Excepcion al generar set de referencia - " 
                    + "Etapa " + stage + " - Instancia " 
                    + instance + ":");
            System.out.println(ex.toString());
        }
        
        return reference_set_path;
    }
}
