/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.execution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author Germán_Ruiz
 */


// Singleton Class
public class Problem 
{
    private final int numberOfObjectives = 7; // CO, CO2, HC, PMx, NOx, VehDestino, TimeLoss
    private final int yellow_duration = 4;
    private final int red_green_min_duration = 5;
    private final int red_green_max_duration = 60;
    
    private static Problem problem = null;
    private int n_tl_logic;
    private int phasesCount;
    private List<Logic> tl_logic;
    private int simulation_time;
    private String path;
    private boolean populated = false;
    private int numberOfVariables;

    protected Problem() 
    {
        this.n_tl_logic = 0;
        this.phasesCount = 0;
        this.tl_logic = new ArrayList<Logic>();
        this.numberOfVariables = 0;
    }

    public static Problem getProblem() 
    {
        if (problem == null) {
            problem = new Problem();
        }
        return problem;
    }
    
    public int getPhasesCount() {
        return phasesCount;
    }

    public int getN_tl_logic() {
        return n_tl_logic;
    }

    public int getSimulationTime() {
        return simulation_time;
    }

    public List<Logic> getTl_logic() {
        return tl_logic;
    }

    public String getPath() {
        return path;
    }

    public String getLoopPath() {
        return path + "loop.sh";
    }

    public String getCfgPath() {
        return path + "red.sumo.cfg";
    }

    public boolean populated() {
        return populated;
    }

    public int getNumberOfVariables() {
        return numberOfVariables;
    }

    public int getNumberOfObjectives() {
        return numberOfObjectives;
    }
    
    public int getRedGreenMinDuration() {
        return red_green_min_duration;
    }

    public int getRedGreenMaxDuration() {
        return red_green_max_duration;
    }

    public int Populate(String problemPath) 
    {
        this.n_tl_logic = 0;
        this.tl_logic = new ArrayList<Logic>();

        if (!problemPath.endsWith("/")) {
            problemPath += "/";
        }
        path = problemPath;

        phasesCount = 0;
        String tl_logic_path = path + "tl-logic.add.xml";
        try 
        {
            BufferedReader tl_file = new BufferedReader(new FileReader(tl_logic_path));
            String line;
            while ((line = tl_file.readLine()) != null) {
                if (line.contains("<tlLogic")) {
                    // Nuva Logica
                    Logic tl = new Logic();
                    tl.n_phases = 0;
                    tl.n_red_green_phases = 0;
                    tl.n_yellow_phases = 0;

                    tl.phases = new ArrayList<String>();
                    tl.phases_durations = new ArrayList<Integer>();
                    String[] components = line.split("\\s+"); //Splitting by spaces
                    for (String component : components) {
                        component = component.replace("\"", "");
                        if (component.startsWith("id=")) {
                            tl.id = component.split("=")[1];
                            break;
                        }
                    }

                    line = tl_file.readLine();
                    while ((line.contains("<phase"))) {
                        String[] components_aux = line.split("\\s+"); //Splitting by spaces                            
                        for (String component : components_aux) {
                            component = component.replace("\"", "");
                            if (component.startsWith("duration=")) {
                                String duration = component.split("=")[1];
                                Integer dur = Integer.parseInt(duration);
                                tl.phases_durations.add(dur);
                            }
                            if (component.startsWith("state=")) {
                                String state = component.split("=")[1];
                                state = state.substring(0, state.length() - 2);
                                tl.phases.add(state);
                                tl.n_phases++;
                                // Si tiene una luz amarilla no se cuenta como variable
                                // Las luces amarillas tienen duracion fija
                                if (!state.contains("y")) {
                                    phasesCount++;
                                    tl.n_red_green_phases++;
                                } else {
                                    tl.phases_durations.set(tl.phases_durations.size() - 1, 4); //Por las dudas
                                    tl.n_yellow_phases++;
                                }
                                break;
                            }
                        }
                        line = tl_file.readLine();
                    }
                    //Adding the tl logic
                    tl.max_offset_duration = tl.n_yellow_phases*yellow_duration + tl.n_red_green_phases*red_green_max_duration;
                    this.tl_logic.add(tl);
                }
            }
            tl_file.close();
        } 
        catch (IOException e) 
        {
            System.out.println("Problem: Excepcion al popular problema:");
            System.out.println(e.toString());
            e.printStackTrace();
        }

        try 
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            //Here we do the actual parsing
            Document doc = builder.parse(new File(this.getCfgPath()));

            this.simulation_time = Integer.parseInt(((Element) doc.getElementsByTagName("end").item(0)).getAttribute("value"));
        } 
        catch (Exception e) 
        {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        n_tl_logic = this.tl_logic.size();
        populated = true;
        numberOfVariables = this.n_tl_logic + phasesCount;
        return numberOfVariables;
    }

    public void CreateTLFile(int[] genotipo, String tl_logic_path) 
    {
        CreateTLFile(genotipo, tl_logic_path, false, "");
    }

    public void CreateTLFile(int[] genotipo, String tl_logic_path, String emissions_output_path) 
    {
        CreateTLFile(genotipo, tl_logic_path, true, emissions_output_path);
    }

    public void CreateTLFile(int[] genotipo, String tl_logic_path, Boolean emissions_add, String emissions_output_path) 
    {
        try
        {
            PrintWriter tl_file = new PrintWriter(tl_logic_path);
            int cont = 0;
            
            tl_file.println("<additional>");

            if (emissions_add) 
            {
                tl_file.println("   <edgeData id=\"dump_" + simulation_time + "\" type=\"emissions\" freq=\"" + simulation_time + "\" file=\"" + emissions_output_path + "\"/>");
            }

            for (int i = 0; i < n_tl_logic; i++) 
            {
                tl_file.println("    <tlLogic id=\"" + tl_logic.get(i).id + "\" type=\"static\" programID=\"1\" offset=\"" + (genotipo[cont++]) + "\">");
                for (int j = 0; j < tl_logic.get(i).n_phases; j++) 
                {
                    String states = tl_logic.get(i).phases.get(j);
                    if (states.contains("y")) //Para amarillas duracion de 4 seg
                    {
                        tl_file.println("        <phase duration=\"" + yellow_duration + "\" state=\"" + states + "\"/>");
                    } 
                    else 
                    {
                        tl_file.println("        <phase duration=\"" + genotipo[cont++] + "\" state=\"" + states + "\"/>");
                    }

                }
                tl_file.println("    </tlLogic>");
            }
            tl_file.println("</additional>");
            tl_file.close();
        } 
        catch (Exception ex) 
        {
            System.out.println("Problem - Error al crear TLFile: " + tl_logic_path);
            System.out.println(ex.toString()); 
        }
    }

}
