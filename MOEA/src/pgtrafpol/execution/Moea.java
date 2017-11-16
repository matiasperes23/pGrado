/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pgtrafpol.execution;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.SynchronizedRandomGenerator;
import org.moeaframework.Analyzer;
import org.moeaframework.algorithm.MOEAD;
import org.moeaframework.algorithm.PeriodicAction;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.spi.AlgorithmFactory;
/**
 *
 * @author Germán_Ruiz
 */
public class Moea {
    
    private static final String project_name = "pgTrafPol";
    private static String instance_path, results_root, configuration_name;
    private static String results_dir, results_logics_dir;
    private static String tmp_output_dir, tmp_tl_logics_dir, tmp_sumo_output_dir;
    private static final int seeds = 30;
    
    public static void executeParallelAlgorithm(String algorithm, 
            Properties properties, 
            List<NondominatedPopulation> results,
            List<Long> executionTimes)
    {
        int cores = Runtime.getRuntime().availableProcessors();
        if(cores > 24)
            cores = 24;

        Executor executor = new Executor()
                .withProblemClass(GenotipoEvaluator.class,
                        tmp_tl_logics_dir, tmp_sumo_output_dir)
                .withAlgorithm(algorithm)
                .withProperties(properties)                  
                .distributeOn(cores);

        System.out.println("Empieza ejecucion de " + seeds + " seeds. Tiempos de ejecución: ");
        for(int s = 0;s < seeds; s++)
        {
            long startTime = System.nanoTime();

            // ---*--- //
            NondominatedPopulation result = executor.run();
            // ---*--- //
            
            long endTime = System.nanoTime();
            executionTimes.add(TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS));
            System.out.println(executionTimes.get(s));

            results.add(result);
            // Imprimo resultados de la ejecucion numero s+1
            //printApproximationSet(s+1, result);

            // Limpio directorios temporales
            try
            {       
                FileUtils.cleanDirectory(new File(tmp_tl_logics_dir));
                FileUtils.cleanDirectory(new File(tmp_sumo_output_dir));
            } 
            catch(Exception ex)
            {
                System.out.println("Main: Excepcion al limpiar directorios termporales - seed " + s+1 + ":");
                System.out.println(ex.toString());
            } 
        }
    }
    
    public static void executeIslandModelAlgorithm(String algorithm, 
            Properties properties, 
            List<NondominatedPopulation> results,
            List<Long> executionTimes,
            String max_evaluations)
    {
        int cores = Runtime.getRuntime().availableProcessors();
            final int numberOfIslands = cores < seeds ? cores : seeds;
            final int actionFrequency = 200;      
            final int maxEvaluationsInt = Integer.parseInt(max_evaluations);

            // need to use a synchronized random number generator
            // instead of the default     
            PRNG.setRandom(new RandomAdaptor(
                    new SynchronizedRandomGenerator(
                            new MersenneTwister())));
            
            final Map<Thread, MOEAD> islands = new HashMap<Thread, MOEAD>();

            for (int i = 0; i < numberOfIslands; i++) 
            {                
                // Creacion de carpetas temporales
                String seed_tmp_tl_logics_dir = tmp_tl_logics_dir + "/" + i;
                String seed_tmp_sumo_output_dir = tmp_sumo_output_dir + "/" + i;
            
                try 
                {
                    File f_tl_logics_dir    = new File(seed_tmp_tl_logics_dir);
                    File f_sumo_output_dir  = new File(seed_tmp_sumo_output_dir);

                    f_tl_logics_dir.mkdirs();
                    f_sumo_output_dir.mkdirs();
                } 
                catch (Exception ex) 
                {
                    System.out.println("Main: Excepcion al crear directorio de Seed:");
                    System.out.println(ex.toString());
                    exit(-1);
                }

                org.moeaframework.core.Problem problemEval = new GenotipoEvaluator(seed_tmp_tl_logics_dir, seed_tmp_sumo_output_dir);
                final MOEAD moead = (MOEAD) AlgorithmFactory.getInstance().getAlgorithm(
                        "MOEAD",
                        properties,
                        problemEval);

                // create a periodic action for handling migration events
                final PeriodicAction migration = new PeriodicAction(
                        moead, 
                        actionFrequency, 
                        PeriodicAction.FrequencyType.EVALUATIONS) 
                {
                    @Override
                    public void doAction() 
                    {
                        Thread thisThread = Thread.currentThread();
                        GenotipoEvaluator genotipo_evaluator = (GenotipoEvaluator)islands.get(thisThread).getProblem();
                        
                        // Limpio directorios temporales
                        try 
                        {
                            FileUtils.cleanDirectory(new File(genotipo_evaluator.getTempLogicsDir()));
                            FileUtils.cleanDirectory(new File(genotipo_evaluator.getTempSumoOutputDir()));
                        } 
                        catch (Exception ex) 
                        {
                            System.out.println("MoeaIslands: Excepcion al limpiar directorios termporales:");
                            System.out.println(genotipo_evaluator.getTempLogicsDir() + ", " + genotipo_evaluator.getTempSumoOutputDir());
                            System.out.println(ex.toString());
                        }
                    }
                };
                
                // start each algorithm its own thread so they run concurrently
                Thread thread = new Thread() {

                    @Override
                    public void run() { 
                        while (migration.getNumberOfEvaluations() < maxEvaluationsInt) {
                            migration.step();
                        }
                    }
                };
                
                islands.put(thread, moead);
            }
            
            System.out.println("Empieza ejecucion de " + seeds + " seeds...");
            long startTime = System.nanoTime();
            for (Thread thread : islands.keySet()) {
                thread.start();
            }

            // wait for all threads to finish and aggregate the result
            int i = 0;
            NondominatedPopulation result;
            for (Thread thread : islands.keySet()) {
                try {
                    thread.join();
                    long endTime = System.nanoTime();
                    result = islands.get(thread).getResult();
                    executionTimes.add(TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS));
                    System.out.println(executionTimes.get(i++));

                    // Imprimo resultados de la ejecucion numero s+1
                    //printApproximationSet(i, result);

                    results.add(result);
                } catch (InterruptedException e) {
                    System.out.println("Thread " + thread.getId() + " was interrupted!");
                }
            }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        checkArguments(args);    
        
        instance_path           = args[0]; // Directorio del escenario SUMO o instancia.
        results_root            = args[1]; // Directorio Raiz donde copiar los resultados de la ejecucion - Sin "/" al final.
        configuration_name      = args[2]; // Nombre de la configuracion - Ver "Cronograma de Ejecuciones".     
        String algorithm        = args[3];
        String max_evaluations  = args[4];
        String population_size  = args[5];

        createTemporalFolders();
        Problem problem = Problem.getProblem();
        problem.Populate(instance_path);
        
        // Creacion de carpeta de resultados
        createResultsFolders();
        
        System.out.println("CONFIGURATION NAME: " + configuration_name);
        System.out.println("ALGORITHM:          " + algorithm);
        System.out.println("maxEvaluations:     " + max_evaluations);
        System.out.println("populationSize:     " + population_size);
        
        Properties props = new Properties();
        props.setProperty("maxEvaluations", max_evaluations);
        props.setProperty("populationSize", population_size);
        
        List<NondominatedPopulation> results = new ArrayList<NondominatedPopulation>();
        List<Long> executionTimes = new ArrayList<Long>();
        
        if ("NSGAII".equals(algorithm))
        {
            String crossoverRate    = args[6];
            String mutationRate     = args[7];
            
            props.setProperty("sbx.rate", crossoverRate); 
            props.setProperty("pm.rate", mutationRate);
            
            System.out.println("sbx.rate:           " + crossoverRate);
            System.out.println("pm.rate:            " + mutationRate);
            
            executeParallelAlgorithm(algorithm, props, results, executionTimes);
        }
        else if ("GDE3".equals(algorithm))
        {
            String deCrossoverRate  = args[6];
            String deStepSize       = args[7]; 
            
            props.setProperty("de.crossoverRate", deCrossoverRate);
            props.setProperty("de.stepSize", deStepSize);
            
            System.out.println("de.crossoverRate:   " + deCrossoverRate);
            System.out.println("de.stepSize:        " + deStepSize);
            
            executeParallelAlgorithm(algorithm, props, results, executionTimes);
        }
        else if ("MOEAD".equals(algorithm))
        {
            String deCrossoverRate  = args[6];
            String mutationRate     = args[7];
            
            props.setProperty("de.crossoverRate", deCrossoverRate);
            props.setProperty("pm.rate", mutationRate);
            props.setProperty("neighborhoodSize", "0.2"); // 20% de la poblacion
            
            System.out.println("deCrossoverRate:    " + deCrossoverRate);
            System.out.println("mutationRate:       " + mutationRate);
            
            executeIslandModelAlgorithm(algorithm, props, results, executionTimes, max_evaluations);
        }
        else if("eNSGAII".equals(algorithm))
        {
            String sbx_rate       = args[6];
            String pm_rate        = args[7];
            String epsilon_string = args[8];
            
            Double epsilon = Double.parseDouble(epsilon_string);
            
            Double e1 = 10000*epsilon;
            Double e2 = 100000*epsilon;
            Double e3 = 1000*epsilon;
            Double e4 = 100*epsilon;
            Double e5 = 1000*epsilon;
            Double e6 = 1.0;
            Double e7 = 100*epsilon;
            
            props.setProperty("sbx.rate", sbx_rate); 
            props.setProperty("pm.rate",  pm_rate);
            props.setProperty("epsilon", e1.toString() + ", " 
                    + e2.toString() + ", " 
                    + e3.toString() + ", " 
                    + e4.toString() + ", "
                    + e5.toString() + ", "
                    + e6.toString() + ", "
                    + e7.toString());
            
            //props.setProperty("epsilon", "10000, 100000, 1000, 100, 1000, 1, 100");
            //props.setProperty("epsilon", "1000, 10000, 100, 10, 100, 1, 10");
            //props.setProperty("epsilon", "10000, 100000, 1000, 10, 100, 1, 10");
            
            System.out.println("sbx.rate:           " + sbx_rate);
            System.out.println("pm.rate:            " + pm_rate);
            System.out.println("epsilon:            " + epsilon);
 
            executeParallelAlgorithm(algorithm, props, results, executionTimes);
        }

        // Al terminar la ejecucion de todas las seeds elimino las carpetas temporales
        deleteTemporalFolders();

        Analyzer analyzer = new Analyzer()
                .withProblemClass(GenotipoEvaluator.class,
                        tmp_tl_logics_dir, tmp_sumo_output_dir)
                .includeAllMetrics()
                .showAll();
        
        analyzer.addAll(configuration_name, results);
        try 
        {
            // Imprimo resultados en unico archivo para posterior analisis
            analyzer.saveAs(configuration_name, new File(results_dir + "/" + configuration_name + ".set"));
        } 
        catch (IOException ex) 
        {
            System.out.println("Main: Excepcion al guardar resultados con Analyzer:");
            System.out.println(ex.toString());
        }
        
        // Imprimo tiempos de ejecucion en unico archivo para posterior analisis
        printExecutionTimes(executionTimes);
        System.out.println("---*---");
    }
    
    private static void printArgumentsUse() 
    {
        System.out.println("Uso: moea "
                + "[directorio_instancia] "
                + "[directorio_resultados] "
                + "[nombre_configuracion] "
                + "[nombre_algoritmo] "
                + "[max_evaluations] "
                + "[population_size] "
                + "[param1]...[paramN]"
        );
    }

    private static void checkArguments(String[] args) 
    {
        if(args.length < 3)
        {
            System.out.println("Error: Uso incorrecto de argumentos.");
            printArgumentsUse();
            exit(-1);
        }
        
        String algorithm = args[3];
                
        if("NSGAII".equals(algorithm) || "GDE3".equals(algorithm) 
                || "MOEAD".equals(algorithm))
        {
            if(args.length != 8)
            {
                System.out.println("Error: Numero incorrecto de argumentos.");
                printArgumentsUse();
                exit(-1);
            }
        }
        else if("eNSGAII".equals(algorithm))
        {
            if(args.length != 9)
            {
                System.out.println("Error: Numero incorrecto de argumentos.");
                printArgumentsUse();
                exit(-1);
            }
        }
        else
        {
            System.out.println("Error: Algoritmo Desconocido.");
            exit(-1);
        }    
    }
    
    // Creacion de carpetas temporales
    private static void createResultsFolders()
    {
        results_dir = results_root + "/" + configuration_name;
        
        String results_dir_aux = results_dir;
        int i = 2;
        while(new File(results_dir_aux).isDirectory()) {
            results_dir_aux = results_dir + " (" + i++ + ")";
        }
        results_dir = results_dir_aux;
        
        
        results_logics_dir = results_dir + "/best-tl-logics";
        
        try
        {
            File f_results_dir = new File(results_dir);
            File f_results_logics_dir = new File(results_logics_dir);
            f_results_dir.mkdirs();
            f_results_logics_dir.mkdirs();
            
            System.out.println("Resultados en:      " + f_results_dir);
        } 
        catch(Exception ex)
        {
            System.out.println("Main: Excepcion al crear directorios:");
            System.out.println(ex.toString());
            exit(-1);
        }  
    }
    
    private static void createTemporalFolders()
    {
        tmp_output_dir   = System.getProperty("java.io.tmpdir") + "/" + project_name + "/" + "tmp_" + configuration_name;

        // Primero se intenta borrar la carpeta temporal
        try
        {       
            FileUtils.deleteDirectory(new File(tmp_output_dir));
        } 
        catch(Exception ex)
        {
            // Si no se puede borrar la carpeta temporal se crea otra con otro nombre
            while(new File(tmp_output_dir).isDirectory()) {
                tmp_output_dir = tmp_output_dir + "_" + (int)(Math.random() * 10);
            }
        }  
        tmp_tl_logics_dir       = tmp_output_dir + "/tl_logics";
        tmp_sumo_output_dir     = tmp_output_dir + "/output";
        
        try
        {        
            File f_tl_logics_dir    = new File(tmp_tl_logics_dir);
            File f_sumo_output_dir  = new File(tmp_sumo_output_dir);  
            
            f_tl_logics_dir.mkdirs();
            f_sumo_output_dir.mkdirs();
        } 
        catch(Exception ex)
        {
            System.out.println("GenotipoEvaluator: Excepcion al crear directorios:");
            System.out.println(ex.toString());
            exit(-1);
        }  
    }
    // * //
    
    private static void deleteTemporalFolders()
    {
        try
        {       
            FileUtils.deleteDirectory(new File(tmp_output_dir));
        } 
        catch(Exception ex)
        {
            System.out.println("Main: Excepcion al borrar directorio temporal:");
            System.out.println(ex.toString());
        }
    }
    
    // Precondicion: La carpeta de resultados 'results_dir' esta creada.
    public static void printApproximationSet(int execution_number, NondominatedPopulation result)
    {
        String aproxSetFile = results_dir + "/" + configuration_name + "_EJ_" + execution_number + ".set";
        try
        {
            PrintWriter aprox_set_file = new PrintWriter(aproxSetFile, "UTF-8");
            int sol = 1;
            for (Solution solution : result)
            {  
                aprox_set_file.println(solution.getObjective(0)
                        + " " + solution.getObjective(1)
                        + " " + solution.getObjective(2)
                        + " " + solution.getObjective(3)
                        + " " + solution.getObjective(4)
                        + " " + solution.getObjective(5)
                        + " " + solution.getObjective(6)
                );
                //Creo ademas los tl_logics files de cada soluicion
                String tl_logic_path = results_logics_dir + "/" + "best-tll-ej-" + execution_number + "-" + sol++ + ".add.xml";
                Problem.getProblem().CreateTLFile(EncodingUtils.getInt(solution), tl_logic_path);
            }
            aprox_set_file.println('#');
            aprox_set_file.close();
        }
        catch(IOException ex){
            System.out.println("Main: Exepcion al imprimir Set de Aproximacion Ej: " + execution_number + ".");
            System.out.println(ex.toString());
        }
    }

    private static void printExecutionTimes(List<Long> executionTimes) 
    {        
        String exec_times_file_path = results_dir + "/" + configuration_name + ".times";
        try
        {
            PrintWriter exec_times_file = new PrintWriter(exec_times_file_path, "UTF-8");
            
            for (Long execTime : executionTimes) 
            {    
                exec_times_file.println(execTime);  
            }
            
            exec_times_file.println('#');
            exec_times_file.close();
        }
        catch(IOException ex)
        {
            System.out.println("Main: Exepcion al imprimir tiempos de ejecucion:");
            System.out.println(ex.toString());
        }
        
    }
   
}
