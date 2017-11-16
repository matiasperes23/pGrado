/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgradoanalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;


/**
 * @author Germán_Ruiz
 */

/* saves each tl_logic with ID, Nº OF PHASES, and STATES of PHASES*/
class Logic {

    public String id;
    public int n_phases;
    public int n_yellow_phases;
    public int n_red_green_phases;
    public List<String> phases;
    public List<Integer> phases_durations;
    public int max_offset_duration;
}

// Singleton Class
public class PGradoProblem  extends AbstractProblem implements Serializable
{
    private static final int nObjectives = 7; // CO, CO2, HC, PMx, NOx, VehDestino, TimeLoss
    private static final int yellow_duration = 4;
    private static final int red_green_max_duration = 60;
    private static final int red_green_min_duration = 5;
    
    private static int n_tl_logic;
    private static int phasesCount;
    private static List<Logic> tl_logic;
    private static String path;
    private static int nVariables;

    public PGradoProblem(String instance_path) 
    {
        super(populate(instance_path), nObjectives);
    }
    
    @Override
    public void evaluate(Solution solution) 
    {}

     @Override
    public Solution newSolution() {
        Solution solution = new Solution(numberOfVariables, numberOfObjectives);
        int varCont = 0;
        Logic logica;
        for (int i = 0; i < n_tl_logic; i++) 
        {         
            logica = tl_logic.get(i);
            
            // Offset
            solution.setVariable(varCont, EncodingUtils.newInt(0, logica.max_offset_duration)); 
            varCont++;

            // Luces Verdes, Rojas
            for(int j = 0; j < logica.n_red_green_phases; j++)
            {
                // Se asignan duraciones en el intervalo [5,60] segundos para verdes y rojas
                // Para amarillas se usa duracion fija de 4 seg, no se consideran en las variables
                solution.setVariable(varCont, EncodingUtils.newInt(red_green_min_duration, red_green_max_duration));
                varCont++;
            }
        }
        return solution;
    }

    public static int populate(String instance_path) 
    {
        n_tl_logic = 0;
        tl_logic = new ArrayList();

        if (!instance_path.endsWith("/")) {
            instance_path += "/";
        }
        path = instance_path;

        phasesCount = 0;
        String tl_logic_path = path + "tl-logic.add.xml";
        try(BufferedReader tl_file = new BufferedReader(new FileReader(tl_logic_path)))
        {
            String line;
            while ((line = tl_file.readLine()) != null) {
                if (line.contains("<tlLogic")) {
                    // Nuva Logica
                    Logic tl = new Logic();
                    tl.n_phases = 0;
                    tl.n_red_green_phases = 0;
                    tl.n_yellow_phases = 0;

                    tl.phases = new ArrayList();
                    tl.phases_durations = new ArrayList();
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
                    tl_logic.add(tl);
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

        n_tl_logic = tl_logic.size();
        nVariables = n_tl_logic + phasesCount;
        return nVariables;
    }

}
