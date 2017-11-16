/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.trafficgenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.exit;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.moeaframework.Analyzer;
 

/**
 *
 * @author bdi
 */
public class TrafficGenerator 
{
    private static final double simulation_time = 480;
    private static String manual_counts_path, od_routes_path, 
            results_root, instance_name, configuration_name;
    private static Map<String,String> counts_edges = new HashMap<String,String>();

    public static void main(String[] args)
    {

        if(args.length == 5)
        {
            manual_counts_path  = args[0];
            od_routes_path      = args[1];
            results_root        = args[2];
            instance_name       = args[3];
            configuration_name  = args[4];
        }
        else
        {
            manual_counts_path  = "C:/Users/bdi/Desktop/TrafficGenerator/conteos-tarde.xml";
            od_routes_path      = "C:/Users/bdi/Desktop/TrafficGenerator/gralflores-rutas-od.rou.xml";
            results_root        = "C:/Users/bdi/Desktop/TrafficGenerator/Traffic/";
            instance_name       = "gralflores";
            configuration_name  = "GF-M";
        }

        // Cargo rutas OD de od_routes_path
        List<OD> od_routes = loadODRoutes();
        
        // Cargo conteos manuales
        Map<String,Flow> manual_counts = loadManualCounts();

        // Ejecuto
        NondominatedPopulation result = new Executor()
                .withProblemClass(ODMatrix.class, od_routes, manual_counts)
                .withAlgorithm("NSGAII")      
                .withMaxEvaluations(10000000)
                .withProperty("populationSize", 500)
                .distributeOnAllCores()
                .run();
        
        int sol = 1;
        for (Solution solution : result) 
        {
            System.out.println("Solution " + sol + ":");
            System.out.println(solution.getObjective(0) + " " 
                    + solution.getObjective(1) + " " 
                    + solution.getObjective(2) + " " 
                    + solution.getObjective(3)
            );
            System.out.println();
            
            printSolutionTraffic(solution, sol, od_routes);
            printMatrix(solution, sol++, od_routes);
            break;
        }   
        
        Analyzer analyzer = new Analyzer()
                .withProblemClass(ODMatrix.class, od_routes, manual_counts)
                .includeAllMetrics()
                .showAll();
        
        analyzer.add(configuration_name, result);
        try 
        {
            // Imprimo resultados en unico archivo para posterior analisis
            analyzer.saveAs(configuration_name, new File(results_root + "/" + configuration_name + ".set"));
        } 
        catch (IOException ex) 
        {
            System.out.println("Main: Excepcion al guardar resultados con Analyzer:");
            System.out.println(ex.toString());
        }
    }

    public static List<OD> loadODRoutes()
    {
        try
        {
            List<OD> od_routes = new ArrayList<OD>();
            File od_routes_file = new File(od_routes_path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document od_routes_doc = dBuilder.parse(od_routes_file);
            od_routes_doc.getDocumentElement().normalize();
            
            NodeList nList = od_routes_doc.getElementsByTagName("route");
            
            for (int temp = 0; temp < nList.getLength(); temp++) 
            {
                if (nList.item(temp).getNodeType() == Node.ELEMENT_NODE) 
                {
                    Element eElement = (Element)nList.item(temp);
                    od_routes.add(new OD(eElement.getAttribute("edges").split(" ")));
                }
                
            }
            return od_routes;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Map<String,Flow> loadManualCounts()
    {
        try
        {
            Map<String,Flow> manual_counts = new HashMap<String,Flow>();
            File manual_counts_file = new File(manual_counts_path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document manual_counts_doc = dBuilder.parse(manual_counts_file);
            manual_counts_doc.getDocumentElement().normalize();
            
            NodeList nMaps = manual_counts_doc.getElementsByTagName("mapa");
            NodeList nConteosMapa = null;
            for (int temp = 0; temp < nMaps.getLength(); temp++) 
            {
                Element eMap = (Element) nMaps.item(temp);
                if (eMap.getAttribute("id").equals(instance_name)) 
                {
                    nConteosMapa = eMap.getChildNodes();
                    break;
                }
            }
            
            if(nConteosMapa == null)
                exit(-1);

            for (int temp = 0; temp < nConteosMapa.getLength(); temp++) 
            {
                if (nConteosMapa.item(temp).getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nConteosMapa.item(temp);
                    counts_edges.put(eElement.getAttribute("edge"), eElement.getAttribute("id"));
                    if(eElement.hasChildNodes())
                    {
                        manual_counts.put(eElement.getAttribute("edge"), new Flow(
                                Integer.parseInt(eElement.getElementsByTagName("light").item(0).getTextContent()), 
                                Integer.parseInt(eElement.getElementsByTagName("car").item(0).getTextContent()), 
                                Integer.parseInt(eElement.getElementsByTagName("heavy").item(0).getTextContent()), 
                                Integer.parseInt(eElement.getElementsByTagName("bus").item(0).getTextContent())
                        ));
                    }
                }    
            }
            return manual_counts;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }
    }
    
    static void printSolutionTraffic(Solution solution, int sol_id, List<OD> od_routes)
    {
        String output_file = results_root + configuration_name + sol_id + ".rou.xml";
        int[] matrixOD = EncodingUtils.getInt(solution);
        int vehicle_id = 0;
        Random rand = new Random();
        try 
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("routes");
            doc.appendChild(rootElement);
            
            Element vTypeLDV = doc.createElement("vType");
            vTypeLDV.setAttribute("id", "typeLDV");
            vTypeLDV.setAttribute("vClass", "motorcycle");
            vTypeLDV.setAttribute("emissionClass", "HBEFA3/LDV");
            vTypeLDV.setAttribute("guiShape", "motorcycle");
            vTypeLDV.setAttribute("color", "0,255,255");
            rootElement.appendChild(vTypeLDV);
            
            Element vTypePC = doc.createElement("vType");
            vTypePC.setAttribute("id", "typePC");
            vTypePC.setAttribute("vClass", "passenger");
            vTypePC.setAttribute("emissionClass", "HBEFA3/PC");
            vTypePC.setAttribute("guiShape", "passenger");
            vTypePC.setAttribute("color", "255,255,0");
            rootElement.appendChild(vTypePC);
            
            Element vTypeHDV = doc.createElement("vType");
            vTypeHDV.setAttribute("id", "typeHDV");
            vTypeHDV.setAttribute("vClass", "trailer");
            vTypeHDV.setAttribute("emissionClass", "HBEFA3/HDV");
            vTypeHDV.setAttribute("guiShape", "truck/trailer");
            vTypeHDV.setAttribute("color", "255,0,0");
            rootElement.appendChild(vTypeHDV);
            
            Element vTypeBus = doc.createElement("vType");
            vTypeBus.setAttribute("id", "typeBus");
            vTypeBus.setAttribute("vClass", "bus");
            vTypeBus.setAttribute("emissionClass", "HBEFA3/Bus");
            vTypeBus.setAttribute("guiShape", "bus");
            vTypeBus.setAttribute("color", "255,69,0");
            rootElement.appendChild(vTypeBus);
            
            List<Element> vehicle_nodes = new ArrayList<Element>();
            
            // Para cada OD
            for(int i = 0; i < matrixOD.length; i=i+4)
            {
                int od = i/4;
                // Para cada tipo
                for(int t = 0; t < 4; t++)
                {
                    if(matrixOD[i+t]>0)
                    {
                        Double repetition_rate = simulation_time/matrixOD[i+t];
                        //Double time = 0.0;
                        Double time =  randTime();
                        for(int j = 0; j < matrixOD[i+t]; j++)
                        {
                            // Imprimo vehiculo con tipo t y salida time 
                            Element vehicle = doc.createElement("vehicle");
                            vehicle.setAttribute("id", Integer.toString(vehicle_id));
                            vehicle.setAttribute("depart", String.format("%.2f", time));
                            vehicle.setAttribute("departLane", "best");
                            vehicle.setAttribute("departPos", "random");
                            vehicle.setAttribute("departSpeed", "max");
                            vehicle.setAttribute("type", vehicleType(t));
                            //rootElement.appendChild(vehicle);
                            vehicle_nodes.add(vehicle);
                            
                            Element route = doc.createElement("route");
                            route.setAttribute("edges", StringUtils.join(od_routes.get(od).route," "));
                            vehicle.appendChild(route);
                            
                            vehicle_id++;
                            //time += repetition_rate;
                            time =  randTime();
                        } 
                    }
                }
            }
            
            Collections.sort(vehicle_nodes, new Comparator<Element>() {
                @Override
                public int compare(Element e1, Element e2) {
                    return Double.compare(Double.parseDouble(e1.getAttribute("depart")),
                            Double.parseDouble(e2.getAttribute("depart"))
                    );
                }
            });
            
            for(Element elem : vehicle_nodes)
                rootElement.appendChild(elem);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(output_file));
            transformer.transform(source, result);
        }
        catch (Exception ex) {
            ex.printStackTrace();
	} 
    }
    
    static void printMatrix(Solution solution, int sol_id, List<OD> od_routes)
    {
        String output_file = results_root + configuration_name + sol_id + "-matrix.txt";
        int[] matrixOD = EncodingUtils.getInt(solution);
        
        
        SortedSet<String> edgesOrigenTree  = new TreeSet();
        SortedSet<String> edgesDestinoTree = new TreeSet();
        
        // Determino origenes y destinos
        for(OD od : od_routes)
        {
            edgesOrigenTree.add(od.Origin());
            edgesDestinoTree.add(od.Destination());
        }  
        List<String> edgesOrigen = new ArrayList<String>(edgesOrigenTree);
        List<String> edgesDestino = new ArrayList<String>(edgesDestinoTree);
        int cantOrigenes = edgesOrigen.size();
        int cantDestinos = edgesDestino.size();
        
        // Creo matrices para cada tipo de vehiculo
        List<int[][]> matrices = new ArrayList(); 
        matrices.add(new int[cantOrigenes][cantDestinos]);
        matrices.add(new int[cantOrigenes][cantDestinos]);
        matrices.add(new int[cantOrigenes][cantDestinos]);
        matrices.add(new int[cantOrigenes][cantDestinos]);
        // Relleno con 0's dado que no existen caminos entre todo par OD
        for(int i = 0; i < cantOrigenes; i++)
        {
            for(int j = 0; j < cantDestinos; j++)
            {
                for(int t = 0; t < 4; t++)
                {
                    matrices.get(t)[i][j] = 0;
                }
            }
        }
        
        
        // Relleno las matrices con los datos de la solucion matrixOD
        for(int i = 0; i < matrixOD.length; i=i+4)
        {
            OD od = od_routes.get(i/4);
            int origenIndex  = edgesOrigen.indexOf(od.Origin());
            int destinoIndex = edgesDestino.indexOf(od.Destination());
                 
            for(int t = 0; t < 4; t++)
            {
                matrices.get(t)[origenIndex][destinoIndex] = matrixOD[i+t];
            }
        }            
        
        // Imprimo en el archivo
        try 
        {
            PrintWriter writer = new PrintWriter(output_file, "UTF-8");
 
            // Para cada tipo imprimo una matriz
            writer.println("TIPOS: typeLDV = Livianos, typePC = Auto de " +
                    "Pasajeros, typeHDV = Camión Pesado, typeBus = Ómnibus");
            writer.println("");
            for(int t = 0; t < 4; t++)
            {
                writer.println("");
                writer.println("MATRIZ TIPO " + vehicleType(t) + ":");
                writer.println("");
                
                // Imprimo cabezal de destinos
                writer.print("O\\D");
                for(int j = 0; j < cantDestinos; j++)
                {
                    writer.print("\t" + counts_edges.get(edgesDestino.get(j)));
                }
                writer.println("");
                
                for(int i = 0; i < cantOrigenes; i++)
                {
                    // Imprimo una fila
                    writer.print(counts_edges.get(edgesOrigen.get(i)));
                    for(int j = 0; j < cantDestinos; j++)
                    {
                        writer.print("\t" + matrices.get(t)[i][j]);
                    }
                    writer.println("");
                }
            }
            
            // Imprimo Flujos
            writer.println("Flujo de vehículos por minuto: (Link\\Flujo)");
            writer.println("");
            writer.println("L\\F" + "\t" + "Light" + "\t" + "Car" + "\t" + "Heavy" + "\t" + "Bus");
            Map<String,Flow> sol_flow = flowDerivation(matrixOD,od_routes);   
            for(String edge : sol_flow.keySet())
            {   
                if(counts_edges.containsKey(edge))
                {
                    Flow flow = sol_flow.get(edge);
                    writer.println(counts_edges.get(edge) + "\t" + (flow.light/8.0) 
                            + "\t" + (flow.car/8.0) 
                            + "\t" + (flow.heavy/8.0) 
                            + "\t" + (flow.bus/8.0));
                    
                }
            }
            writer.close();
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
    }
    
    public static Map<String,Flow> flowDerivation(int[] matrixOD, List<OD> od_routes)
    {
        Map<String,Flow> sol_flow = new HashMap<String,Flow>();
        
        // Calculo del flujo en cada edge con la matriz solucion
        for(int i = 0; i < od_routes.size()*4; i=i+4)
        {
            int od = i/4;
            
            for(String s : od_routes.get(od).route)
            {
                if(!sol_flow.containsKey(s))
                {
                    sol_flow.put(s, 
                            new Flow(matrixOD[i+0], 
                                     matrixOD[i+1], 
                                     matrixOD[i+2], 
                                     matrixOD[i+3])
                    );
                }
                else
                {
                    Flow flow = sol_flow.get(s);
                    flow.light += matrixOD[i+0];
                    flow.car   += matrixOD[i+1];
                    flow.heavy += matrixOD[i+2];
                    flow.bus   += matrixOD[i+3];
                }
            }
        }
        return sol_flow;
    }
    
    public static double randTime() 
    {
        return (Math.random() * (simulation_time));
    }
    
    private static String vehicleType(int t)
    {
        if (t == 0) {
            return "typeLDV";
        } else if (t == 1) {
            return "typePC";
        } else if (t == 2) {
            return "typeHDV";
        } else {
            return "typeBus";
        }
    }
    
}
