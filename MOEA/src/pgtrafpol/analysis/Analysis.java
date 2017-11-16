/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pgtrafpol.execution.GenotipoEvaluator;
import pgtrafpol.execution.Problem;


/**
 *
 * @author bdi
 */
public class Analysis {

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
        
        instances_root  = "C:/Users/bdi/Desktop/Ejecuciones/Escenarios/";
        results_root    = "C:/Users/bdi/Desktop/Ejecuciones/Resultados/";
        
        //analysisTest();
        //analysisStage1();
        //analysisStage2();
        //analysisStage3();
        analysisStage4();
        //analysisCicos1();
        
        
        System.out.println("Fin de Analisis.");
    }
    
    public static void analysisStage4()
    {
        Integer stage = 4;
        String[] instances = {"CENTRO_M", "CENTRO_T", "CENTRO_N"};
        
        System.out.println("Inicio Analisis Etapa 4...");
        
        for(String instance : instances)
        {
            System.out.println("Instancia " + instance + "...");
            
            List<Configuration> configurations = new ArrayList();
            configurations.add(new Configuration(stage,instance, "NSGAII",  0));
            configurations.add(new Configuration(stage,instance, "GDE3",    0));
            configurations.add(new Configuration(stage,instance, "MOEAD",   0));
            configurations.add(new Configuration(stage,instance, "ENSGAII", 0));
            
            //generarLogicsFile(instance);
            //individualComparisonReferenceSet(stage, instance, configurations);
            
            //comparisonReferenceSet(stage, instance, configurations);
            
            //printCombinedReferenceSet(stage, instance, configurations);
            printWholeCombinedReferenceSet(stage, instance, configurations);
            
            /*
            String reference_set_path = generateReferenceSet(stage, 
                    instance,
                    configurations);

            analyzeWithReferenceSet(configurations, reference_set_path);
            */
            System.out.println("Completado - Instancia " + instance + ".");
        }
    }
    
    public static void generarLogicsFile(String instance)
    {
        //String gtype = "96.26406051805789 37.46149476916004 22.182866127005305 17.16908688490413 48.95421807445866 22.06861874131346 95.04142415584445 49.90058741812948 21.215317280254435 9.100937752693312 37.572607742863454 60.54003359957158 1.7462668272208135 55.619338495364346 5.0 57.49907671569542 32.77039523446357 20.66366714546895 6.410221749602345 45.17112918309314 5.0 56.37461495851351 35.10089213782751 5.0 58.10644790115994 48.38708386981999 16.225961281288043 111.07314924080761 10.834366554108602 40.29795317214959 115.21916895934251 32.579261690775915 12.74911228134974 62.82029331025033 47.986633641169064 16.581660953037403 99.67001922889624 40.342277483098236 16.814204066748168 89.60165052894568 51.67266960750988 19.994908310000653";
        //String gtype = "55.99622926225394 36.04081972815608 27.486071496642865 115.09492617452892 39.41116421089974 24.104580606220928 61.82993988848842 5.0 41.4329238512823 5.124880989563831 0.0 5.0 21.28298652441201 20.959061344223457 34.46207754232757 6.604581535334915 24.331879198694494 360.23647023873633 5.0 5.0 6.3344392675641155 7.638021902541935 5.0 5.0";
        //String gtype = "74.35841715768125 40.908181534042086 18.144170399888505 55.1741798991496 29.359581234528715 16.057029200576057 153.97422703632517 6.217477286080921 60.99999999999999 5.181939336062243 33.912867363100204 49.75216451105892 31.480620841635165 166.19457395147617 5.0 5.0 12.969154367083453 263.4792038486779 8.71556453434339 5.732474761279763 5.586285592589856 5.373271907588927 7.773723416413764 10.521834542192625";
        String gtype = "10.511682936194823 30.458032642317598 9.199870726541041 85.34471269879157 36.650049077107 9.779820206538105 82.08328983952875 22.392810929700172 16.912891342045963 25.3665191625156 15.441728555141662 41.663018056716325 82.6750853084826 13.304873106477114 39.32048446516157 79.12619679327413 30.021571649516382 29.693112838004076 73.77086744822647 21.76319307533018 32.57230031755102 42.86226781971418 15.638001777573427 58.94818279600592 115.09852555143127 36.42249893083367 27.269045161033635 37.05577116735625 56.8435255724918 34.81898724495768 57.39065249560055 37.23176786468264 21.556825049319418 87.80528570808413 12.010544036825195 49.77320679369311 94.64029184455902 53.152105444992415 5.729556622130209 73.48045626780018 13.482628876162659 15.433946181876223 36.02671586854375 37.08657076930701 45.20158983842654 38.303961549384056 40.619502152602294 27.547705277701073 20.8590722136205 14.679652207703793 7.988651037367115 38.28183684684669 15.559507300527011 36.389421791505235 6.843864722537447 20.13629607799038 52.52855330234786 59.65748991669131 19.272155436164336 18.942249678833946 73.75318726304211 45.07560687989205 25.04241652142648 15.52080558168094 12.544028393149947 42.31590455160288 98.78509588220074 25.946741514999232 44.577004366392956 90.59093902363337 18.868241839993782 35.32815792718617 105.94399794639354 17.50648395746097 28.466730762009895 113.5444718756112 43.08123461278671 39.22247244930741 35.673803295170146 12.581455246208078 37.483346622714194 17.714184396806957 29.44210599638479 60.638691905257616 101.35238118920043 17.224089916250144 14.789473695905684 47.05788661706987 49.385410866631126 17.511115842381344 69.8107886480678 5.308322238966158 18.403470410644044 55.84893057127784 40.52471558170981 38.24807314625252 47.61725838312323 19.536820348221312 6.797622039932845 69.30204479259505 47.88475192232429 12.793770677898367 26.22760698600836 12.18963321455144 31.547901924283224 35.33126667256943 17.499480175914716 17.246416170757204 96.9253005488385 21.71156164353418 48.085031203323794 36.276897666120824 6.13013641018018 6.214737045499766 17.652087403522266 22.891354209809705 15.459408272626879 38.61587882457448 24.54334321375984 14.679075870569225 48.70970269654657 31.413216673400914 42.71832218566996";
        String[] genes = gtype.split(" ");
        int[] phasesDurations = new int[genes.length];
        for(int i = 0; i<genes.length; i++)
        {
            phasesDurations[i] = (int)(Double.parseDouble(genes[i]));
        }
        
        Problem problem = Problem.getProblem();     
        String instance_path = instances_root + instance + "/";
        problem.Populate(instance_path);
        String tl_logic_path = "C:/Users/bdi/Desktop/" + instance + ".ttl.xml";
        problem.CreateTLFile(phasesDurations, tl_logic_path, "");
        
    }
    
    public static void analysisCicos1()
    {
        Integer stage = 4;
        String[] instances = {"CENTRO_M"};
        
        System.out.println("Inicio Analisis Etapa 4...");
        
        for(String instance : instances)
        {
            System.out.println("Instancia " + instance + "...");
            
            List<Configuration> configurations = new ArrayList();
            configurations.add(new Configuration(stage,instance, "NSGAII",  1));
            //configurations.add(new Configuration(stage,instance, "GDE3",    1));
            //configurations.add(new Configuration(stage,instance, "MOEAD",   1));
            //configurations.add(new Configuration(stage,instance, "ENSGAII", 1));
            
            //String reference_set_path = comparisonReferenceSet(stage, 
            //        instance, 
            //        configurations);

            String reference_set_path = generateReferenceSet(stage, 
                    instance, 
                    configurations);
            
            analyzeWithReferenceSet(configurations, reference_set_path);
            
            System.out.println("Completado - Instancia " + instance + ".");
        }
    }
    
    public static void analysisTest()
    {
        instances_root  = "C:/Prueba/Escenarios/";
        results_root    = "C:/Prueba/Resultados/";
        
        Integer stage = 3;
        String[] instances = {"IC1"};
        
        System.out.println("Inicio Analisis Etapa" + stage + "...");
        
        for(String instance : instances)
        {
            System.out.println("Instancia " + instance + "...");
            
            List<Configuration> configurations = new ArrayList();
            configurations.add(new Configuration(stage,instance, "NSGAII",  1));
            configurations.add(new Configuration(stage,instance, "ENSGAII", 4));

            String reference_set_path = generateReferenceSet(stage, 
                    instance, 
                    configurations);

            analyzeWithReferenceSet(configurations, reference_set_path);
            
            System.out.println("Completado - Instancia " + instance + ".");
        }
    }
    
    public static void analysisStage1()
    {
        Integer stage = 1;
        String[] instances = {"IC1", "IC2", "IC3"};
        
        System.out.println("Inicio Analisis Etapa 1...");
        
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
        
        System.out.println("Inicio Analisis Etapa 2...");
        
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
        
        System.out.println("Inicio Analisis Etapa 3...");
        
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
        // Cada configuracion dentro de la lista de configuraciones es comparada
        // contra el conjunto de referencia.
        for(Configuration config : configurations)
        {
            String results_path = results_root 
                    + "Etapa" + config.stage + "/"
                    + config.instance + "/";
            
            String instance_path = instances_root + config.instance + "/";
            Problem problem = Problem.getProblem();
            problem.Populate(instance_path);
            
            NewAnalyzer algorithm_analyzer = new NewAnalyzer();
            algorithm_analyzer.withProblemClass(GenotipoEvaluator.class, 
                            "C:/temp", "C:/temp");
            algorithm_analyzer.includeAllMetrics();
            algorithm_analyzer.showAll();
            
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
                    + "E" + config.stage + "_" 
                    + config.instance + ".txt";
            String algorith_time_analysis_result_path = results_path + config.algorithm 
                    + "_TimeAnalysisResult_" 
                    + "E" + config.stage + "_" 
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
        Problem problem = Problem.getProblem();
        problem.Populate(instance_path);
        
        NewAnalyzer analyzer = new NewAnalyzer();
        analyzer.withProblemClass(GenotipoEvaluator.class, "C:/temp", "C:/temp");
        analyzer.includeAllMetrics();
        analyzer.showAll();
        
        TimeAnalyzer time_analyzer = new TimeAnalyzer();
        
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
                    time_analyzer.loadAs(config_name, new File(results_path
                            + config_name + "/" 
                            + config_name + ".times"));
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
                + "AnalysisReferenceSet_" + "E" + stage + "_" + instance + ".txt";
        String analysis_result_path = results_path 
                + "AnalysisResult_" + "E" + stage + "_" + instance + ".txt";
        String time_analysis_result_path = results_path 
                + "TimeAnalysisResult_" + "E" + stage + "_" + instance + ".txt";
        try 
        {
            // Guardo Set de Referencia
            analyzer.saveReferenceSet(new File(reference_set_path));
            // Guardo Analisis Completo
            analyzer.saveAnalysis(new File(analysis_result_path));
            
            time_analyzer.saveTimeAnalysis(new File(time_analysis_result_path));
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
    
    public static void printCombinedReferenceSet(Integer stage, // Etapa Principal
            String instance, // Instancia Principal
            List<Configuration> configurations)
    {
        for (Configuration config : configurations) 
        {
            String instance_path = instances_root + instance + "/";
            Problem problem = Problem.getProblem();
            problem.Populate(instance_path);

            NewAnalyzer analyzer = new NewAnalyzer();
            analyzer.withProblemClass(GenotipoEvaluator.class, "C:/temp", "C:/temp");
            analyzer.includeAllMetrics();
            analyzer.showAll();

            TimeAnalyzer time_analyzer = new TimeAnalyzer();

            String _results_path = results_root
                    + "Etapa" + config.stage + "/"
                    + config.instance + "/";
            for (String config_name : config.getNames()) {
                try {
                    analyzer.loadAs(config_name, new File(_results_path
                            + config_name + "/"
                            + config_name + ".set"));
                    time_analyzer.loadAs(config_name, new File(_results_path
                            + config_name + "/"
                            + config_name + ".times"));
                } catch (Exception ex) {
                    System.out.println("Main: Excepcion al cargar configuracion " + config_name + ":");
                    System.out.println(ex.toString());
                }
            }

            String results_path = results_root
                    + "Etapa" + stage + "/"
                    + instance + "/";
            //analyzer.printAnalysis();
            String reference_set_path = results_path
                    + config.getAlgorithm() + "_" + "ReferenceSet_" + "E" + stage + "_" + instance + ".txt";
            String combined_reference_tpg_set_path = results_path
                    + config.getAlgorithm() + "_" + "CombinedReferenceSet_" + "E" + stage + "_" + instance + "_" + "TPG" + ".txt";
            String combined_reference_vd_set_path = results_path
                    + config.getAlgorithm() + "_" + "CombinedReferenceSet_" + "E" + stage + "_" + instance + "_" + "VD" + ".txt";
            try {
                // Guardo Set de Referencia 
                analyzer.saveReferenceSet(new File(reference_set_path));
                
                // Guardo Set de Referencia Combinado
                analyzer.saveCombinedReferenceSetTPG(new File(combined_reference_tpg_set_path));
                analyzer.saveCombinedReferenceSetVD(new File(combined_reference_vd_set_path));
            } 
            catch (IOException ex) 
            {
                System.out.println("Main: Excepcion al generar set de referencia - "
                        + "Etapa " + stage + " - Instancia "
                        + instance + ":");
                System.out.println(ex.toString());
            }
        }
    }
    
    public static void printWholeCombinedReferenceSet(Integer stage, // Etapa Principal
            String instance, // Instancia Principal
            List<Configuration> configurations)
    {
        
            String instance_path = instances_root + instance + "/";
            Problem problem = Problem.getProblem();
            problem.Populate(instance_path);

            NewAnalyzer analyzer = new NewAnalyzer();
            analyzer.withProblemClass(GenotipoEvaluator.class, "C:/temp", "C:/temp");
            analyzer.includeAllMetrics();
            analyzer.showAll();

            TimeAnalyzer time_analyzer = new TimeAnalyzer();
            for (Configuration config : configurations) 
            {
                String _results_path = results_root
                        + "Etapa" + config.stage + "/"
                        + config.instance + "/";
                for (String config_name : config.getNames()) {
                    try {
                        analyzer.loadAs(config_name, new File(_results_path
                                + config_name + "/"
                                + config_name + ".set"));
                        time_analyzer.loadAs(config_name, new File(_results_path
                                + config_name + "/"
                                + config_name + ".times"));
                    } catch (Exception ex) {
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
                    + "WHOLE" + "_" + "ReferenceSet_" + "E" + stage + "_" + instance + ".txt";
            String combined_reference_tpg_set_path = results_path
                    + "WHOLE" + "_" + "CombinedReferenceSet_" + "E" + stage + "_" + instance + "_" + "TPG" + ".txt";
            String combined_reference_vd_set_path = results_path
                    + "WHOLE" + "_" + "CombinedReferenceSet_" + "E" + stage + "_" + instance + "_" + "VD" + ".txt";
            try {
                // Guardo Set de Referencia 
                analyzer.saveReferenceSet(new File(reference_set_path));
                
                // Guardo Set de Referencia Combinado
                analyzer.saveCombinedReferenceSetTPG(new File(combined_reference_tpg_set_path));
                analyzer.saveCombinedReferenceSetVD(new File(combined_reference_vd_set_path));
            } 
            catch (IOException ex) 
            {
                System.out.println("Main: Excepcion al generar set de referencia - "
                        + "Etapa " + stage + " - Instancia "
                        + instance + ":");
                System.out.println(ex.toString());
            }
        
    }
    
    
    public static void comparisonReferenceSet(Integer stage, // Etapa Principal
            String instance, // Instancia Principal
            List<Configuration> configurations)
    {
        for(Configuration config : configurations)
        {
        String instance_path = instances_root + instance + "/";
        Problem problem = Problem.getProblem();
        problem.Populate(instance_path);
        
        NewAnalyzer analyzer = new NewAnalyzer();
        analyzer.withProblemClass(GenotipoEvaluator.class, "C:/temp", "C:/temp");
        analyzer.includeAllMetrics();
        analyzer.showAll();
        
        TimeAnalyzer time_analyzer = new TimeAnalyzer();
        
        
        String _results_path = results_root 
                + "Etapa" + config.stage + "/"
                + config.instance + "/";
        for(String config_name : config.getNames())
        {
            try 
            {
                analyzer.loadAs(config_name, new File(_results_path
                        + config_name + "/" 
                        + config_name + ".set"));
                time_analyzer.loadAs(config_name, new File(_results_path
                        + config_name + "/" 
                        + config_name + ".times"));
            } 
            catch (Exception ex) 
            {
                System.out.println("Main: Excepcion al cargar configuracion " + config_name + ":");
                System.out.println(ex.toString());
            }
        }
        
        
        String results_path = results_root 
                + "Etapa" + stage + "/" 
                + instance + "/";
        //analyzer.printAnalysis();
        String reference_set_path = results_path 
                + "AnalysisReferenceSet_" + "E" + stage + "_" + instance + ".txt";
        String comparison_path = results_path 
                + config.getAlgorithm() + "_" + "ComparisonResult_" + "E" + stage + "_" + instance + ".txt";
        String time_analysis_result_path = results_path 
                + "TimeAnalysisResult_" + "E" + stage + "_" + instance + ".txt";
        try 
        {
            // Ejecuto configuracion real
             Map<Integer, Double> objetivosSolReal = SimpleExecutor.executeSimple(instance_path,results_path);
            
             // Guardo comparacion del conjunto de referencia contra configuracion real
             analyzer.saveComparison(new File(comparison_path),objetivosSolReal);
             
            
            // Guardo Set de Referencia
            //analyzer.saveReferenceSet(new File(reference_set_path));
            // Guardo Analisis Completo
            
            
            //time_analyzer.saveTimeAnalysis(new File(time_analysis_result_path));
        } 
        catch (IOException ex) 
        {
            System.out.println("Main: Excepcion al generar set de referencia - " 
                    + "Etapa " + stage + " - Instancia " 
                    + instance + ":");
            System.out.println(ex.toString());
        }
        
        }
    }
    
    public static void individualComparisonReferenceSet(Integer stage, // Etapa Principal
            String instance, // Instancia Principal
            List<Configuration> configurations)
    {
        for(Configuration config : configurations)
        {
            String instance_path = instances_root + instance + "/";
            Problem problem = Problem.getProblem();
            problem.Populate(instance_path);

            NewAnalyzer analyzer = new NewAnalyzer();
            analyzer.withProblemClass(GenotipoEvaluator.class, "C:/temp", "C:/temp");
            analyzer.includeAllMetrics();
            analyzer.showAll();

            TimeAnalyzer time_analyzer = new TimeAnalyzer();

            String _results_path = results_root 
                    + "Etapa" + config.stage + "/"
                    + config.instance + "/";
            for(String config_name : config.getNames())
            {
                try 
                {
                    analyzer.loadAs(config_name, new File(_results_path
                            + config_name + "/" 
                            + config_name + ".set"));
                    time_analyzer.loadAs(config_name, new File(_results_path
                            + config_name + "/" 
                            + config_name + ".times"));
                } 
                catch (Exception ex) 
                {
                    System.out.println("Main: Excepcion al cargar configuracion " + config_name + ":");
                    System.out.println(ex.toString());
                }
            }

            String results_path = results_root 
                    + "Etapa" + stage + "/" 
                    + instance + "/";
            //analyzer.printAnalysis();
            String comparison_path = results_path 
                    + config.getAlgorithm() + "Individual" + "_" + "ComparisonResult_" + "E" + stage + "_" + instance + ".txt";
            try 
            {
                // Ejecuto configuracion real
                 Map<Integer, Double> objetivosSolReal = SimpleExecutor.executeSimple(instance_path,results_path);

                 // Guardo comparacion del conjunto de referencia contra configuracion real
                 analyzer.saveIndividualComparison(new File(comparison_path),objetivosSolReal,config.getAlgorithm());

            } 
            catch (IOException ex) 
            {
                System.out.println("Main: Excepcion al generar set de referencia - " 
                        + "Etapa " + stage + " - Instancia " 
                        + instance + ":");
                System.out.println(ex.toString());
            }
        }
    }
    
}
