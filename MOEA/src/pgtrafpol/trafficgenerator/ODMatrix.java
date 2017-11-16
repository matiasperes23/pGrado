/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.trafficgenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

/**
 *
 * @author bdi
 */
public class ODMatrix  extends AbstractProblem 
{
    private static int nObjectives = 4;
    private  List<OD> od_routes;
    private Map<String,Flow> manual_counts;

    public ODMatrix(List<OD> od_routes, Map<String,Flow> manual_counts) 
    {
        super(od_routes.size()*4, nObjectives);
        this.od_routes = od_routes;
        this.manual_counts = manual_counts;
    }

    @Override
    public void evaluate(Solution solution) 
    {
        int[] matrixOD = EncodingUtils.getInt(solution);
        Map<String,Flow> sol_flow = new HashMap<String,Flow>();
        
        // Calculo del flujo en cada edge con la matriz solucion
        for(int i = 0; i < numberOfVariables; i=i+4)
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
        
        int light_diff = 0;
        int car_diff = 0;
        int heavy_diff = 0;
        int bus_diff = 0;
                
        for(String edge : manual_counts.keySet())
        {
            Flow counts_edge_flow = manual_counts.get(edge);
            if(sol_flow.containsKey(edge))
            {
                Flow sol_edge_flow = sol_flow.get(edge);     
                light_diff  += Math.abs(counts_edge_flow.light - sol_edge_flow.light);
                car_diff    += Math.abs(counts_edge_flow.car   - sol_edge_flow.car);
                heavy_diff  += Math.abs(counts_edge_flow.heavy - sol_edge_flow.heavy);
                bus_diff    += Math.abs(counts_edge_flow.bus   - sol_edge_flow.bus);
            }
            else
            {
                light_diff  += counts_edge_flow.light;
                car_diff    += counts_edge_flow.car;
                heavy_diff  += counts_edge_flow.heavy;
                bus_diff    += counts_edge_flow.bus;
            }
        }
        
        solution.setObjective(0, light_diff);
        solution.setObjective(1, car_diff);
        solution.setObjective(2, heavy_diff);
        solution.setObjective(3, bus_diff);
    }

    @Override
    public Solution newSolution() 
    {
        Solution new_solution = new Solution(numberOfVariables, numberOfObjectives);
        
        for(int i = 0; i < numberOfVariables; i=i+4)
        {
            // Light
            new_solution.setVariable(i+0, EncodingUtils.newInt(0, 100));
            // Car
            new_solution.setVariable(i+1, EncodingUtils.newInt(0, 300));
            // Heavy
            new_solution.setVariable(i+2, EncodingUtils.newInt(0, 25));
            // Bus
            new_solution.setVariable(i+3, EncodingUtils.newInt(0, 50));
        }
        
        return new_solution;        
    }

    
}
