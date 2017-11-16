/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.alternative;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pgtrafpol.analysis.SimpleExecutor;
import pgtrafpol.execution.Logic;
import pgtrafpol.execution.Problem;
/**
 *
 * @author bdi
 */
public class Alternative 
{
    static String tmp_tl_logics_dir, tmp_sumo_output_dir;
    static Problem problem;
    static String instances_root, instance_path, results_root;
    static String sumo_cfg_path;
    
    public static double calcularDistancia(String idActual, String idAnterior, NodeList listOfEdges)
    {
        double distancia = 0;
        boolean flag = false;
        for (int s = 0; s < listOfEdges.getLength() && !flag; s++) 
        {
            Node firstEdge = listOfEdges.item(s);
            Node from = firstEdge.getAttributes().getNamedItem("from");
            if (from != null && from.getNodeValue().equals(idAnterior)) 
            {
                if (firstEdge.getAttributes().getNamedItem("to").getNodeValue().equals(idActual)) 
                {
                    NodeList lanes = firstEdge.getChildNodes();
                    for (int h = 0; h < lanes.getLength(); h++) 
                    {
                        Node lane = lanes.item(h);
                        if (lane.getNodeName().equals("lane")) 
                        {
                            distancia = Double.parseDouble(lane.getAttributes().getNamedItem("length").getNodeValue());
                            flag = true;
                            break;
                        }
                    }
                }
            }
        }
        if(distancia == 0)
        {
            if(idActual.equals("917606391") && idAnterior.equals("917543762"))
            {
                distancia = 91.97;
            }      
            else if(idActual.equals("917580765") && idAnterior.equals("1893973257"))
            {
                distancia = 96.53;
            } 
            else if(idActual.equals("917414594") && idAnterior.equals("917490607"))
            {
                distancia = 96.53;
            }
            else
            {
                System.out.println("Distancia 0: " + idActual + " - " + idAnterior);
                return distancia;
            }
        } 
        return distancia;
    }
    
    static int indexAvenidas(String id, List<ArrayList<String>> avenidas)
    {
        for(ArrayList<String> avenida : avenidas)
        {
            if(avenida.contains(id))
                return avenida.indexOf(id);
        }
        return -1;
    }
    
    static ArrayList<String> avenidaContenedor(String id, List<ArrayList<String>> avenidas)
    {
        for(ArrayList<String> avenida : avenidas)
        {
            if(avenida.contains(id))
                return avenida;
        }
        return null;
    }
    
    public static void main(String[] args) throws IOException 
    {
        tmp_tl_logics_dir    = "C:/Alternative/temp";
        tmp_sumo_output_dir  = "C:/Alternative/temp";
        instances_root       = "C:/Users/bdi/Desktop/Ejecuciones/Escenarios/";
        results_root         = "C:/Users/bdi/Desktop/Ejecuciones/Resultados/Etapa4/OndaVerde/";
        instance_path        = instances_root + "CENTRO_T/"; // Directorio del escenario SUMO o instancia.
        String tl_logic_path = instance_path  + "tl-logic.add.xml";
        String red_path      = instance_path  + "red.net.xml";
        problem              = Problem.getProblem();      
        problem.Populate(instance_path);
        sumo_cfg_path        = problem.getCfgPath();
        
        List<Genotipo> genotipos = new ArrayList<Genotipo>();
        try 
        {                     
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (new File(red_path));
            NodeList listOfEdges = doc.getElementsByTagName("edge");
            
            List<ArrayList<String>> avenidas = new ArrayList<ArrayList<String>>();
            ArrayList<String> avenida1 = new ArrayList<String>(); 
            avenida1.add("917365340");avenida1.add("917543762");avenida1.add("917606391");avenida1.add("917500859"); 
            avenida1.add("917560069");avenida1.add("917735203");avenida1.add("917533316");avenida1.add("917686942");
            avenida1.add("917370760");
            ArrayList<String> avenida2 = new ArrayList<String>();
            avenida2.add("917545752");avenida2.add("917681291");avenida2.add("917494347");avenida2.add("1893973257");
            avenida2.add("917580765");avenida2.add("917490297");avenida2.add("917322276");avenida2.add("917597065");
            ArrayList<String> avenida3 = new ArrayList<String>();
            avenida3.add("917511160");avenida3.add("917473596");avenida3.add("917664545");avenida3.add("917490607");
            avenida3.add("917414594");avenida3.add("917536880");avenida3.add("917429080");avenida3.add("917422676");
            ArrayList<String> avenida4 = new ArrayList<String>();
            avenida4.add("917370760");avenida4.add("917686942");avenida4.add("917533316");avenida4.add("917735203");
            avenida4.add("917560069");avenida4.add("917500859");avenida4.add("917606391");avenida4.add("917543762");
            avenida4.add("917365340");
            avenidas.add(avenida1);avenidas.add(avenida2);avenidas.add(avenida3);
            List<Integer> verdes = new ArrayList<Integer>();  verdes.add(10); verdes.add(20); verdes.add(30); verdes.add(40); verdes.add(50);
            List<Integer> rojas = new ArrayList<Integer>(); rojas.add(10); rojas.add(20); rojas.add(30); rojas.add(40); rojas.add(50);
            int nTLlogics = problem.getN_tl_logic();
            List<Logic> tls_logic = problem.getTl_logic();
            int varCont = 0;
            Logic logica;
            for (Integer verde : verdes) 
            {
                for (Integer rojo : rojas) 
                {

                    for (int vel = 15; vel <= 35; vel++) 
                    {
                        int[] duraciones = new int[problem.getNumberOfVariables()];
                        double vel_m = (((double) vel) * 1000 / 3600);
                        // la primer logica va así noma
                        for (int i = 0; i < nTLlogics; i++) 
                        {
                            logica = tls_logic.get(i);
                            int indexLogica = indexAvenidas(logica.id, avenidas);
                            ArrayList<String> avenidaContiene = avenidaContenedor(logica.id, avenidas);
                            if (indexLogica >= 0) 
                            {
                                if (indexLogica == 0) 
                                {
                                    duraciones[i * 3 + 0] = 0;
                                    duraciones[i * 3 + 1] = verde;
                                    duraciones[i * 3 + 2] = rojo;
                                } 
                                else 
                                {
                                    double distancia = calcularDistancia(logica.id, avenidaContiene.get(indexLogica - 1), listOfEdges);

                                    if (avenida1.contains(logica.id)) 
                                    {
                                        duraciones[i * 3 + 0] = duraciones[(i - 1) * 3 + 0] + (int) (distancia / vel_m);
                                        duraciones[i * 3 + 1] = verde;
                                        duraciones[i * 3 + 2] = rojo;
                                    } else if (avenida2.contains(logica.id)) 
                                    {
                                        duraciones[i * 3 + 0] = duraciones[(i - 1) * 3 + 0] + (int) (distancia / vel_m);
                                        duraciones[i * 3 + 1] = verde;
                                        duraciones[i * 3 + 2] = rojo;
                                    } else if (avenida3.contains(logica.id)) {
                                        duraciones[i * 3 + 0] = duraciones[(i - 1) * 3 + 0] + (int) (distancia / vel_m);
                                        duraciones[i * 3 + 1] = verde;
                                        duraciones[i * 3 + 2] = rojo;
                                    }
                                }
                            } 
                            else 
                            {
                                duraciones[i * 3 + 0] = 0;
                                duraciones[i * 3 + 1] = logica.phases_durations.get(0);
                                duraciones[i * 3 + 2] = logica.phases_durations.get(2);
                            }

                        }
                        genotipos.add(new Genotipo(duraciones));
                    }
                }
            }
        }
        catch (Exception err) 
        {
            System.out.println(" " + err.getMessage ());
        }
        
        //Evalúo todos los Genotipo
        for(Genotipo gen : genotipos)
        {
            evaluate(gen);
        }
        
        String comparison_path = results_root + "OV_ComparisonResult_Real" + ".txt";
        // Ejecuto configuracion real
        Map<Integer, Double> objetivosSolReal = SimpleExecutor.executeSimple(instance_path,"");
        
        // Guardo comparacion del conjunto de referencia contra configuracion real
        printComparison(new File(comparison_path), genotipos, objetivosSolReal);
        
        String combined_reference_tpg_set_path = results_root
                    + "OV_CombinedReferenceSet_" + "TPG" + ".txt";
        String combined_reference_vd_set_path = results_root
                + "OV_CombinedReferenceSet_" + "VD" + ".txt";
        String reference_set_path = results_root + "OV_ReferenceSet" + ".txt";
        // Guardo Set de Referencia Combinado
        saveCombinedReferenceSetTPG(new File(combined_reference_tpg_set_path),genotipos);
        saveCombinedReferenceSetVD(new File(combined_reference_vd_set_path),genotipos);
        saveReferenceSet(new File(reference_set_path), genotipos);
        
        /******/
        
        List<Genotipo> ges1 = genotipoParetoTPG(genotipos);
        List<Genotipo> ges2 = genotipoParetoVH(genotipos);
        ges1.addAll(ges2);
        List<Genotipo> genotiposDCI = new ArrayList<Genotipo>();
        for (Genotipo g1 : ges1) 
        {
            for(int i = 0; i < problem.getN_tl_logic(); i++)
            {
                int val = g1.phasesDurations[i+1];
                g1.phasesDurations[i+1] = 180;
                genotiposDCI.add(new Genotipo(g1.phasesDurations));
                g1.phasesDurations[i+1] = val;

                val = g1.phasesDurations[i+2];
                g1.phasesDurations[i+2] = 180;
                genotiposDCI.add(new Genotipo(g1.phasesDurations));
                g1.phasesDurations[i+2] = val;
            }
        }
        for(Genotipo gen : genotiposDCI)
        {
            evaluate(gen);
        }
        //genotipos.addAll(genotiposDCI);
        
        String comparison_path2 = results_root + "DCI_ComparisonResult_Real" + ".txt";
        
        // Guardo comparacion del conjunto de referencia contra configuracion real
        printComparison(new File(comparison_path2), genotiposDCI, objetivosSolReal);
        
        String combined_reference_tpg_set_path2 = results_root
                    + "DCI_CombinedReferenceSet_" + "TPG" + ".txt";
        String combined_reference_vd_set_path2 = results_root
                + "DCI_CombinedReferenceSet_" + "VD" + ".txt";
        String reference_set_path2 = results_root + "DCI_ReferenceSet" + ".txt";
        // Guardo Set de Referencia Combinado
        saveCombinedReferenceSetTPG(new File(combined_reference_tpg_set_path2),genotiposDCI);
        saveCombinedReferenceSetVD(new File(combined_reference_vd_set_path2),genotiposDCI);
        saveReferenceSet(new File(reference_set_path2), genotiposDCI);
    }
    
    public static void alternativoAguada(String[] args) throws IOException 
    {
        tmp_tl_logics_dir   = "C:/Alternative/temp";
        tmp_sumo_output_dir = "C:/Alternative/temp";
        instances_root      = "C:/Users/bdi/Desktop/Ejecuciones/Escenarios/";
        results_root        = "C:/Users/bdi/Desktop/Ejecuciones/Resultados/Etapa4/OndaVerde/";
        instance_path       = instances_root + "GF_N/"; // Directorio del escenario SUMO o instancia.
        String tl_logic_path = instance_path + "tl-logic.add.xml";
        String red_path = instance_path + "red.net.xml";
        problem             = Problem.getProblem();      
        problem.Populate(instance_path);
        sumo_cfg_path       = problem.getCfgPath();
        
        List<Genotipo> genotipos = new ArrayList<Genotipo>();
        try 
        {                     
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (new File(red_path));
            NodeList listOfEdges = doc.getElementsByTagName("edge");
            
            List<Integer> verdes = new ArrayList<Integer>();  verdes.add(10); verdes.add(20); verdes.add(30); verdes.add(40); verdes.add(50);
            List<Integer> rojas = new ArrayList<Integer>(); rojas.add(10); rojas.add(20); rojas.add(30); rojas.add(40); rojas.add(50);
            int nTLlogics = problem.getN_tl_logic();
            List<Logic> tls_logic = problem.getTl_logic();
            int varCont = 0;
            Logic logica;
            for(Integer verde : verdes)
            {
            for(Integer rojo : rojas)
            {    
           
            for(int vel = 15; vel <= 45; vel++)
            {
                int[] duraciones = new int[problem.getNumberOfVariables()];
                double vel_m = (((double)vel)*1000/3600);
                // la primer logica va así noma
                for (int i = 0; i < nTLlogics; i++) 
                {         
                    logica = tls_logic.get(i);

                    if(i == 0)
                    {
                        duraciones[i*3+0] = 0;
                        duraciones[i*3+1] = verde;
                        duraciones[i*3+2] = rojo;
                    }
                    else
                    {
                        // Calculo la distancia al id anterior
                        double distancia = 0;
                        String idActual = logica.id;
                        String idAnterior = tls_logic.get(i-1).id;
                        boolean flag = false;
                        for(int s=0; s<listOfEdges.getLength() && !flag; s++)
                        {
                            Node firstEdge = listOfEdges.item(s);
                            Node from = firstEdge.getAttributes().getNamedItem("from");
                            if(from != null && from.getNodeValue().equals(idAnterior))
                            {
                                if(firstEdge.getAttributes().getNamedItem("to").getNodeValue().equals(idActual))
                                {
                                    NodeList lanes = firstEdge.getChildNodes();
                                    for(int h=0; h<lanes.getLength(); h++)
                                    {
                                        Node lane = lanes.item(h);
                                        if (lane.getNodeName().equals("lane"))
                                        {
                                            distancia = Double.parseDouble(lane.getAttributes().getNamedItem("length").getNodeValue());
                                            flag = true;
                                            break;
                                        }
                                    }
                                    
                                }
                            }
                        }
                        duraciones[i*3+0] = duraciones[(i-1)*3+0] + (int)(distancia/vel_m);
                        duraciones[i*3+1] = verde;
                        duraciones[i*3+2] = rojo;
                    }
                }
                genotipos.add(new Genotipo(duraciones));
                int[] duracionesInversas = new int[problem.getNumberOfVariables()];
                for (int i = 0; i < nTLlogics; i++) 
                {
                    duracionesInversas[i*3+0] = duracionesInversas[((nTLlogics-1)-i)*3+0];
                    duracionesInversas[i*3+1] = verde;
                    duracionesInversas[i*3+2] = rojo;
                }
                genotipos.add(new Genotipo(duracionesInversas));
            }
            }
            }
        }
        catch (Exception err) 
        {
            System.out.println(" " + err.getMessage ());
        }
        
        //Evalúo todos los Genotipo
        for(Genotipo gen : genotipos)
        {
            evaluate(gen);
        }
        
        List<double[]> soluciones = new ArrayList<double[]>();
        
        String comparison_path = results_root + "ComparisonResult_Real" + ".txt";
        // Ejecuto configuracion real
        Map<Integer, Double> objetivosSolReal = SimpleExecutor.executeSimple(instance_path,"");
        
        // Guardo comparacion del conjunto de referencia contra configuracion real
        printComparison(new File(comparison_path), genotipos, objetivosSolReal);
        
        String combined_reference_tpg_set_path = results_root
                    + "CombinedReferenceSet_" + "TPG" + ".txt";
        String combined_reference_vd_set_path = results_root
                + "CombinedReferenceSet_" + "VD" + ".txt";
        String reference_set_path = results_root + "ReferenceSet" + ".txt";
        // Guardo Set de Referencia Combinado
        saveCombinedReferenceSetTPG(new File(combined_reference_tpg_set_path),genotipos);
        saveCombinedReferenceSetVD(new File(combined_reference_vd_set_path),genotipos);
        saveReferenceSet(new File(reference_set_path), genotipos);
    }
    
    public static void saveReferenceSet(File file, List<Genotipo> genotipos) throws IOException 
    {
        List<double[]> soluciones = new ArrayList<double[]>();
        BufferedWriter writer = null;
        try 
        {
            writer = new BufferedWriter(new FileWriter(file));
            for (Genotipo solution : genotipos) 
            {
                writer.write(Double.toString(solution.getObjective(0)));
                writer.write("\t");
                writer.write(Double.toString(solution.getObjective(1)));
                writer.write("\t");
                writer.write(Double.toString(solution.getObjective(2)));
                writer.write("\t");
                writer.write(Double.toString(solution.getObjective(3)));
                writer.write("\t");
                writer.write(Double.toString(solution.getObjective(4)));
                writer.write("\t");
                writer.write(Double.toString(solution.getObjective(5)));
                writer.write("\t");
                writer.write(Double.toString(solution.getObjective(6)));
                writer.newLine();
            }
        } 
        finally 
        {
            if (writer != null) {
                writer.close();
            }
        }
    }
    
    public static List<Genotipo> genotipoParetoVH(List<Genotipo> genotipos)
    {
        List<Genotipo> paretoGenotipos = new ArrayList<Genotipo>();
        List<double[]> soluciones = new ArrayList<double[]>();
        for (Genotipo solution : genotipos) 
        {
            Double emisiones = solution.getObjective(0) / 100;
            emisiones += solution.getObjective(1) / 10000;
            emisiones += solution.getObjective(2) / 10;
            emisiones += solution.getObjective(3);
            emisiones += solution.getObjective(4) / 10;
            solution.sumaEmisiones = emisiones;
            Double cantVeh  = solution.getObjective(5);
            //Double timeLost = solution.getObjective(6);
            boolean flag = false;
            
            for (Genotipo gen : paretoGenotipos) 
            {
                if (gen.sumaEmisiones <= emisiones && gen.getObjective(5) >= cantVeh) //&& sol[1]>cantVeh sol[2]<timeLost
                {
                    flag = true;
                }
            }
            
            if (!flag) 
            {
                paretoGenotipos.add(solution);
                
                for (Iterator<Genotipo> iterator = paretoGenotipos.iterator(); iterator.hasNext();) 
                {
                    Genotipo sol = iterator.next();    
                    if (sol.sumaEmisiones > emisiones && sol.getObjective(5) < cantVeh) 
                    {
                        iterator.remove();
                    }
                }
            }
        }  
        return paretoGenotipos;
    }
    
    public static List<Genotipo> genotipoParetoTPG(List<Genotipo> genotipos)
    {
        List<Genotipo> paretoGenotipos = new ArrayList<Genotipo>();
        for (Genotipo solution : genotipos) 
        {
            Double emisiones = solution.getObjective(0) / 100;
            emisiones += solution.getObjective(1) / 10000;
            emisiones += solution.getObjective(2) / 10;
            emisiones += solution.getObjective(3);
            emisiones += solution.getObjective(4) / 10;
            solution.sumaEmisiones = emisiones;
            //Double cantVeh = solution.getObjective(5);
            Double timeLost = solution.getObjective(6);
            boolean flag = false;
            for (Genotipo gen : paretoGenotipos) 
            {
                if (gen.sumaEmisiones <= emisiones && gen.getObjective(6) <= timeLost) //&& sol[1]>cantVeh sol[2]<timeLost
                {
                    flag = true;
                }
            }
                
            if (!flag) 
            {
                paretoGenotipos.add(solution);
                
                for (Iterator<Genotipo> iterator = paretoGenotipos.iterator(); iterator.hasNext();) 
                {
                    Genotipo sol = iterator.next();    
                    if (sol.sumaEmisiones > emisiones && sol.getObjective(6) > timeLost) 
                    {
                        iterator.remove();
                    }
                }
            }
        }  
        return paretoGenotipos;
    }
    
    public static void saveCombinedReferenceSetVD(File file, List<Genotipo> genotipos) throws IOException 
    {
        List<double[]> soluciones = new ArrayList<double[]>();
        BufferedWriter writer = null;
        try 
        {
            writer = new BufferedWriter(new FileWriter(file));
            for (Genotipo solution : genotipos) 
            {
                double[] candidate = new double[3];
                Double emisiones = solution.getObjective(0) / 100;
                emisiones += solution.getObjective(1) / 10000;
                emisiones += solution.getObjective(2) / 10;
                emisiones += solution.getObjective(3);
                emisiones += solution.getObjective(4) / 10;

                Double cantVeh  = solution.getObjective(5);
                Double timeLost = solution.getObjective(6);
                boolean flag = false;
                for (double[] sol : soluciones) {
                    if (sol[0] < emisiones && sol[1] > cantVeh) {
                        flag = true;
                    }
                }
                if (!flag) {
                    candidate[0] = emisiones;
                    candidate[1] = cantVeh;
                    candidate[2] = timeLost;
                    soluciones.add(candidate);

                    for (Iterator<double[]> iterator = soluciones.iterator(); iterator.hasNext();) {
                        double[] sol = iterator.next();
                        if (sol[0] > emisiones && sol[1] < cantVeh) {
                            iterator.remove();
                        }
                    }
                }
            }
            for (double[] sol : soluciones) {
                writer.write(Double.toString(sol[0]));
                writer.write("\t");
                writer.write(Double.toString(sol[1]));
                writer.write("\t");
                writer.write(Double.toString(sol[2]));
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    
    public static void saveCombinedReferenceSetTPG(File file, List<Genotipo> genotipos) throws IOException {
        List<double[]> soluciones = new ArrayList<double[]>();
        BufferedWriter writer = null;
        try 
        {
            writer = new BufferedWriter(new FileWriter(file));

            for (Genotipo solution : genotipos) 
            {
                double[] candidate = new double[3];
                Double emisiones = solution.getObjective(0) / 100;
                emisiones += solution.getObjective(1) / 10000;
                emisiones += solution.getObjective(2) / 10;
                emisiones += solution.getObjective(3);
                emisiones += solution.getObjective(4) / 10;

                Double cantVeh = solution.getObjective(5);
                Double timeLost = solution.getObjective(6);
                boolean flag = false;
                for (double[] sol : soluciones) {
                    if (sol[0] < emisiones && sol[2] < timeLost) //&& sol[1]>cantVeh sol[2]<timeLost
                    {
                        flag = true;
                    }
                }

                if (!flag) 
                {
                    candidate[0] = emisiones;
                    candidate[1] = cantVeh;
                    candidate[2] = timeLost;
                    soluciones.add(candidate);

                    for (Iterator<double[]> iterator = soluciones.iterator(); iterator.hasNext();) 
                    {
                        double[] sol = iterator.next();
                        if (sol[0] > emisiones && sol[2] > timeLost) //&& sol[1]>cantVeh sol[2]<timeLost   
                        {
                            iterator.remove();
                        }
                    }
                }
            }

            for (double[] sol : soluciones) 
            {
                writer.write(Double.toString(sol[0]));
                writer.write("\t");
                writer.write(Double.toString(sol[1]));
                writer.write("\t");
                writer.write(Double.toString(sol[2]));
                writer.newLine();
            }

        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    
    public static void printComparison(File file, List<Genotipo> genotipos, Map<Integer, Double> objetivosSolReal) throws IOException {

        PrintStream ps = null;

        try {
            ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(
                    file)));

            Map<Integer, double[]> diferencia = new HashMap<Integer, double[]>();

            Map<Integer, String> relObjetivos = new HashMap<Integer, String>();
            relObjetivos.put(0, "CO");
            relObjetivos.put(1, "CO2");
            relObjetivos.put(2, "HC");
            relObjetivos.put(3, "PMx");
            relObjetivos.put(4, "NOx");
            relObjetivos.put(5, "cantVeh");
            relObjetivos.put(6, "timeLoss");

            for (Integer obj : objetivosSolReal.keySet()) {

                double[] values = new double[genotipos.size()];
                if (obj != 5) {
                    for (int i = 0; i < genotipos.size(); i++) {
                        values[i] = ((objetivosSolReal.get(obj)) - (genotipos.get(i).getObjective(obj))) / (objetivosSolReal.get(obj));
                    }
                } else {
                    for (int i = 0; i < genotipos.size(); i++) {
                        values[i] = ((((double)(-1))*(genotipos.get(i).getObjective(obj))) - (objetivosSolReal.get(obj))) / (objetivosSolReal.get(obj));
                    }
                }
                diferencia.put(obj, values);
            }

            //print the results
            Min min = new Min();
            Max max = new Max();
            Mean mean = new Mean();
            StandardDeviation standardDeviation = new StandardDeviation();

            ps.println("Min: ");
            for (Integer obj : objetivosSolReal.keySet()) {
                ps.println(min.evaluate(diferencia.get(obj)));
            }
            ps.println("Mean: ");
            for (Integer obj : objetivosSolReal.keySet()) {
                ps.println(mean.evaluate(diferencia.get(obj)));
            }
            ps.println("Max: ");
            for (Integer obj : objetivosSolReal.keySet()) {
                ps.println(max.evaluate(diferencia.get(obj)));
            }
            ps.println("SD: ");
            for (Integer obj : objetivosSolReal.keySet()) {
                ps.println(standardDeviation.evaluate(diferencia.get(obj)) * 100);
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
    
    static void evaluate(Genotipo genotipo)
    {
        double co = 0, co2 = 0, hc = 0, pmx = 0, nox = 0; int cantVeh = 0; double timeLoss = 0; // fuel = 0;

        String id_exec              = "-" + UUID.randomUUID();
        String tl_logic_path        = tmp_tl_logics_dir   + "/" + "tl"        + id_exec + ".add.xml";
        String output_trips_path    = tmp_sumo_output_dir + "/" + "tripinfo"  + id_exec + ".xml";
        String output_emission_path = tmp_sumo_output_dir + "/" + "emissions" + id_exec + ".xml";
        
        // Cargar candidato en archivo de SUMO
        problem.CreateTLFile(genotipo.phasesDurations, tl_logic_path, output_emission_path);
        
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
        
        genotipo.setObjective(0, co);
        genotipo.setObjective(1, co2);
        genotipo.setObjective(2, hc);
        genotipo.setObjective(3, pmx);
        genotipo.setObjective(4, nox);
        genotipo.setObjective(5, cantVeh); // Niego para maximizar cantVeh
        genotipo.setObjective(6, timeLoss);
    
    }
    
}
