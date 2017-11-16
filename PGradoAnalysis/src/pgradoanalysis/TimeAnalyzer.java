/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgradoanalysis;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.moeaframework.util.statistics.KruskalWallisTest;
import org.moeaframework.util.statistics.MannWhitneyUTest;

/**
 *
 * @author bdi
 */
public class TimeAnalyzer {
    
    private double significanceLevel;
    
    private Map<String, double[]> data;
    
    TimeAnalyzer()
    {
        data = new HashMap<>();
        significanceLevel = 0.05;
    }
    
    public void loadAs(String config_name, File result_file) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(result_file));
        List<Double> values = new ArrayList();
        // prime the reader by reading the first line
	String line = reader.readLine();
        
        while ((line != null) && line.startsWith("#")) {
            line = reader.readLine();
        }
        
        // read next entry, terminated by #
        while ((line != null) && !line.startsWith("#")) {
            Double seed_time = Double.parseDouble(line);

            values.add(seed_time);
            
            line = reader.readLine();
        }
        
        if(values.size() > 0)
        {
            double[] results = new double[values.size()];
            int i = 0;
            for(Double d : values)
                results[i++] = d;
            
            data.put(config_name, results);
        }
        
        reader.close();
    }
    
    public void printTimeAnalysis() throws IOException {
        printTimeAnalysis(System.out);
    }
    
    public void printTimeAnalysis(PrintStream ps)
    {
        if (data.isEmpty()){
            return;
	}
        List<String> algorithms = new ArrayList(data.keySet());
        
        Map<String, List<String>> indifferences = new HashMap<>();
        
        for (String algorithm : algorithms) {
            indifferences.put(algorithm, new ArrayList<String>());
        }
        
        
        KruskalWallisTest kwTest = new KruskalWallisTest(data.size());
		
        for (int i = 0; i < algorithms.size(); i++) {
            kwTest.addAll(data.get(algorithms.get(i)),i);
        }
        
        if (!kwTest.test(significanceLevel)) // Se acepta Hipotesis Nula: All populations have equal medians. 
        { 
            for (int i = 0; i < algorithms.size() - 1; i++) {
                for (int j = i + 1; j < algorithms.size(); j++) {
                    indifferences.get(algorithms.get(i))
                            .add(algorithms.get(j));
                    indifferences.get(algorithms.get(j))
                            .add(algorithms.get(i));
                }
            }
        }
        else // Se rechaza Hipotesis Nula
        {
            for (int i = 0; i < algorithms.size() - 1; i++) {
                for (int j = i + 1; j < algorithms.size(); j++) {
                    MannWhitneyUTest mwTest
                            = new MannWhitneyUTest();

                    mwTest.addAll(data.get(algorithms.get(i)), 0);
                    mwTest.addAll(data.get(algorithms.get(j)), 1);

                    if (!mwTest.test(significanceLevel)) {
                        indifferences.get(algorithms.get(i))
                                .add(algorithms.get(j));
                        indifferences.get(algorithms.get(j))
                                .add(algorithms.get(i));
                    }
                }
            }
        }

        Min min = new Min();
        Max max = new Max();
        Median median = new Median();
        StandardDeviation standardDeviation = new StandardDeviation();
        
        try 
        {
            for (String algorithm : data.keySet()) 
            {
                double[] values = data.get(algorithm);

                ps.print(algorithm);
                ps.println(':');
                ps.print("    ");
                ps.print("SeedTimeSeconds");
                ps.print(": ");

                if (values.length == 0) {
                    ps.print("null");
                } else if (values.length == 1) {
                    ps.print(values[0]);
                } else {
                    ps.println();

                    ps.print("        Min: ");
                    ps.println(min.evaluate(values));
                    ps.print("        Median: ");
                    ps.println(median.evaluate(values));
                    ps.print("        Max: ");
                    ps.println(max.evaluate(values));
                    ps.print("        StandardDeviation: ");
                    ps.println(standardDeviation.evaluate(values));
                }
                ps.print("        Count: ");
                ps.print(values.length);

                ps.println();
                ps.print("        Indifferent: ");
                ps.print(indifferences.get(algorithm));
                
                ps.println();
            }
            
            ps.println();
            ps.println("FORMATO HOJA DE CALCULO");
            ps.println();
            for (String algorithm : data.keySet())
            {
                ps.print(algorithm);
                ps.println(':');

                double[] values = data.get(algorithm);

                if (values.length == 0) {
                    ps.print("null");
                } else if (values.length == 1) {
                    ps.print(values[0]);
                } else {
                    ps.println(max.evaluate(values) + "\t" + 
                        median.evaluate(values) + "\t" + 
                        standardDeviation.evaluate(values));
                }
                
                ps.println();
            }
            
            ps.close();
        } 
        catch (Exception ex) 
        {
            System.out.println("Error al imprimir Time Analysis:");
            System.out.println(ex.getMessage());
        }
    }
    
    public void saveTimeAnalysis(File file) throws IOException {
        PrintStream ps = null;

        try {
            ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(
                    file)));

            printTimeAnalysis(ps);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }

    }

}
