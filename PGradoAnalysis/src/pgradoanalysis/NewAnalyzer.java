/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgradoanalysis;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.moeaframework.Analyzer;
import org.moeaframework.analysis.sensitivity.ResultEntry;
import org.moeaframework.analysis.sensitivity.ResultFileReader;
import org.moeaframework.analysis.sensitivity.ResultFileWriter;
import org.moeaframework.core.Indicator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Problem;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.indicator.AdditiveEpsilonIndicator;
import org.moeaframework.core.indicator.Contribution;
import org.moeaframework.core.indicator.GenerationalDistance;
import org.moeaframework.core.indicator.Hypervolume;
import org.moeaframework.core.indicator.InvertedGenerationalDistance;
import org.moeaframework.core.indicator.MaximumParetoFrontError;
import org.moeaframework.core.indicator.Spacing;
import org.moeaframework.core.spi.ProblemFactory;
import org.moeaframework.util.io.FileUtils;
import org.moeaframework.util.statistics.KruskalWallisTest;
import org.moeaframework.util.statistics.MannWhitneyUTest;

/**
 *
 * @author bdi
 */
public class NewAnalyzer extends Analyzer{
    
    /**
	 * {@code true} if the hypervolume metric is to be computed; {@code false}
	 * otherwise.
	 */
	private boolean includeHypervolume;
	
	/**
	 * {@code true} if the generational distance metric is to be computed; 
	 * {@code false} otherwise.
	 */
	private boolean includeGenerationalDistance;
	
	/**
	 * {@code true} if the inverted generational distance metric is to be 
	 * computed; {@code false} otherwise.
	 */
	private boolean includeInvertedGenerationalDistance;
	
	/**
	 * {@code true} if the additive &epsilon;-indicator metric is to be 
	 * computed; {@code false} otherwise.
	 */
	private boolean includeAdditiveEpsilonIndicator;
	
	/**
	 * {@code true} if the spacing metric is to be computed; {@code false}
	 * otherwise.
	 */
	private boolean includeSpacing;
	
	/**
	 * {@code true} if the maximum Pareto front error metric is to be 
	 * computed; {@code false} otherwise.
	 */
	private boolean includeMaximumParetoFrontError;
	
	/**
	 * {@code true} if the contribution of each approximation set to the
	 * reference set is to be computed; {@code false} otherwise.
	 */
	private boolean includeContribution;
	
	/**
	 * {@code true} if the individual values for each seed are shown;
	 * {@code false} otherwise.
	 */
	private boolean showIndividualValues;
	
	/**
	 * {@code true} if the metric values for the aggregate approximation set 
	 * (across all seeds) is to be calculated; {@code false} otherwise.
	 */
	private boolean showAggregate;
	
	/**
	 * {@code true} if the statistical significance of all metrics is to be
	 * calculated; {@code false} otherwise.  If {@code true}, it is necessary
	 * to record multiple seeds for each entry.
	 */
	private boolean showStatisticalSignificance;
	
	/**
	 * The level of significance used when testing the statistical significance
	 * of observed differences in the medians.
	 */
	private double significanceLevel;
	
	/**
	 * The {@link UnivariateStatistic}s used during the analysis.  If none are
	 * specified by the user, then {@link Min}, {@link Median} and {@link Max}
	 * are used.
	 */
	private List<UnivariateStatistic> statistics;
	
	/**
	 * The collection of end-of-run approximation sets.
	 */
	private Map<String, List<NondominatedPopulation>> data;
        
        private PGradoProblem pGradoProblem;
        
        public NewAnalyzer()
        {
            super();
		
            significanceLevel = 0.05;
            statistics = new ArrayList();
            data = new HashMap();
        }
        
        @Override
	public NewAnalyzer withProblemClass(Class<?> problemClass, 
			Object... problemArguments) {
		super.withProblemClass(problemClass, problemArguments);
            
            this.problemClass = problemClass;
        
            this.problemArguments = problemArguments;
            return this;
	}

        
        Class<?> problemClass;
        Object[] problemArguments;

	public Problem getProblemInstance() {
		if (problemClass != null) {
			try {
				return (Problem)ConstructorUtils.invokeConstructor(problemClass,
						problemArguments);
			} catch (Exception ex) {
				Logger.getLogger(NewAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			}
                }
                return null;
	}
        
	
	@Override
	public Analyzer withEpsilon(double... epsilon) {
		return (Analyzer)super.withEpsilon(epsilon);
	}
        
        private File referenceSetFile;
	
	@Override
	public Analyzer withReferenceSet(File referenceSetFile) {
		this.referenceSetFile = referenceSetFile;
                return this;
	}
	
	/**
	 * Enables the evaluation of the hypervolume metric.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer includeHypervolume() {
		includeHypervolume = true;
		
		return this;
	}
	
	/**
	 * Enables the evaluation of the generational distance metric.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer includeGenerationalDistance() {
		includeGenerationalDistance = true;
		
		return this;
	}
	
	/**
	 * Enables the evaluation of the inverted generational distance metric.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer includeInvertedGenerationalDistance() {
		includeInvertedGenerationalDistance = true;
		
		return this;
	}
	
	/**
	 * Enables the evaluation of the additive &epsilon;-indicator metric.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer includeAdditiveEpsilonIndicator() {
		includeAdditiveEpsilonIndicator = true;
		
		return this;
	}

	/**
	 * Enables the evaluation of the maximum Pareto front error metric.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer includeMaximumParetoFrontError() {
		includeMaximumParetoFrontError = true;
		
		return this;
	}
	
	/**
	 * Enables the evaluation of the spacing metric.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer includeSpacing() {
		includeSpacing = true;
		
		return this;
	}
	
	/**
	 * Enables the evaluation of the contribution metric.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer includeContribution() {
		includeContribution = true;
		
		return this;
	}
	
	/**
	 * Enables the evaluation of all metrics.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer includeAllMetrics() {
		includeHypervolume();
		includeGenerationalDistance();
		includeInvertedGenerationalDistance();
		includeAdditiveEpsilonIndicator();
		includeMaximumParetoFrontError();
		includeSpacing();
		includeContribution();
		
		return this;
	}
	
	/**
	 * Enables the output of all analysis results.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer showAll() {
		showIndividualValues();
		showAggregate();
		showStatisticalSignificance();
		
		return this;
	}
	
	/**
	 * Enables the output of individual metric values for each seed.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer showIndividualValues() {
		showIndividualValues = true;
		
		return this;
	}
	
	/**
	 * Enables the output of the metric value of the aggregate approximation
	 * set, produced by merging all individual seeds.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer showAggregate() {
		showAggregate = true;
		
		return this;
	}
	
	/**
	 * Enables the output of statistical significance tests.  If enabled, it is
	 * necessary to record multiple seeds for each entry.
	 * 
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer showStatisticalSignificance() {
		showStatisticalSignificance = true;
		
		return this;
	}
	
	/**
	 * Specifies the {@link UnivariateStatistic}s calculated during the 
	 * analysis.  If none are specified by the user, then {@link Min}, 
	 * {@link Median} and {@link Max} are used.
	 * 
	 * @param statistic the statistic to calculate
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer showStatistic(UnivariateStatistic statistic) {
		statistics.add(statistic);
		
		return this;
	}
	
	/**
	 * Sets the level of significance used when testing the statistical 
	 * significance of observed differences in the medians.  Commonly used
	 * levels of significance are {@code 0.05} and {@code 0.01}.
	 * 
	 * @param significanceLevel the level of significance
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer withSignifianceLevel(double significanceLevel) {
		this.significanceLevel = significanceLevel;
		
		return this;
	}
	
	/**
	 * Adds the collection of new samples with the specified name.
	 * 
	 * @param name the name of these samples
	 * @param results the approximation sets
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer addAll(String name, Collection<NondominatedPopulation> results) {
		for (NondominatedPopulation result : results) {
			add(name, result);
		}
		
		return this;
	}
	
	/**
	 * Adds a new sample with the specified name.  If multiple samples are
	 * added using the same name, each sample is treated as an individual
	 * seed.  Analyses can be performed on both the individual seeds and
	 * aggregates of the seeds.
	 * 
	 * @param name the name of this sample
	 * @param result the approximation set
	 * @return a reference to this analyzer
	 */
        @Override
	public Analyzer add(String name, NondominatedPopulation result) {
		List<NondominatedPopulation> list = data.get(name);
		
		if (list == null) {
			list = new ArrayList<NondominatedPopulation>();
			data.put(name, list);
		}
		
		list.add(result);
		
		return this;
	}
	
	/**
	 * Saves all data stored in this analyzer, which can subsequently be read
	 * using {@link #loadData(File, String, String)} with matching arguments.
	 * 
	 * @param directory the directory in which the data is stored
	 * @param prefix the prefix for filenames
	 * @param suffix the suffix (extension) for filenames
	 * @return a reference to this analyzer
	 * @throws IOException if an I/O error occurred
	 */
        @Override
	public Analyzer saveData(File directory, String prefix, String suffix) 
	throws IOException {
		FileUtils.mkdir(directory);

		for (String algorithm : data.keySet()) {
			saveAs(algorithm, new File(directory, prefix + algorithm + 
					suffix));
		}
		
		return this;
	}
	
	/**
	 * Loads data into this analyzer, which was previously saved using
	 * {@link #saveData(File, String, String)} with matching arguments.
	 * 
	 * @param directory the directory in which the data is stored
	 * @param prefix the prefix for filenames
	 * @param suffix the suffix (extension) for filenames
	 * @return a reference to this analyzer
	 * @throws IOException if an I/O error occurred
	 */
        @Override
	public Analyzer loadData(File directory, String prefix, String suffix) 
	throws IOException {
		for (File file : directory.listFiles()) {
			String filename = file.getName();

			if (filename.startsWith(prefix) && filename.endsWith(suffix)) {
				String name = filename.substring(prefix.length(), 
						filename.length()-suffix.length());

				loadAs(name, file);
			}
		}
		
		return this;
	}
	
	/**
	 * Loads the samples stored in a result file using {@link ResultFileReader}.
	 * 
	 * @param name the name of the samples
	 * @param resultFile the result file to load
	 * @return a reference to this analyzer
	 * @throws IOException if an I/O error occurred
	 */
        @Override
	public Analyzer loadAs(String name, File resultFile) throws IOException {
		Problem problem = null;
		ResultFileReader reader = null;
		
		try {
			problem = getProblemInstance();

			try {
				reader = new ResultFileReader(problem, resultFile);
						
				while (reader.hasNext()) {
					add(name, reader.next().getPopulation());
				}
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} finally {
			if (problem != null) {
				problem.close();
			}
		}
		
		return this;
	}
	
	/**
	 * Saves the samples to a result file using {@link ResultFileWriter}.  If
	 * {@code name} is {@code null}, the reference set is saved.  Otherwise,
	 * the approximation sets for the named entries are saved.
	 * 
	 * @param name the name of the samples
	 * @param resultFile the result file to which the data is saved
	 * @return a reference to this analyzer
	 * @throws IOException if an I/O error occurred
	 */
        @Override
	public Analyzer saveAs(String name, File resultFile) throws IOException {
		Problem problem = null;
		ResultFileWriter writer = null;
		
		try {
			problem = getProblemInstance();
			
			//delete the file to avoid appending
			FileUtils.delete(resultFile);

			try {
				writer = new ResultFileWriter(problem, resultFile);
				
				if (name == null) {
					writer.append(new ResultEntry(getReferenceSet()));
				} else {
					for (NondominatedPopulation result : data.get(name)) {
						writer.append(new ResultEntry(result));
					}
				}
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		} finally {
			if (problem != null) {
				problem.close();
			}
		}
		
		return this;
	}
	
	/**
	 * Saves the analysis of all data recorded in this analyzer to the
	 * specified file.
	 * 
	 * @param file the file to which the analysis is saved
	 * @return a reference to this analyzer
	 * @throws IOException if an I/O error occurred
	 */
        @Override
	public Analyzer saveAnalysis(File file) throws IOException {
		PrintStream ps = null;
		
		try {
			ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(
					file)));
			
			printAnalysis(ps);
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
		
		return this;
	}
	
	/**
	 * Prints the analysis of all data recorded in this analyzer to standard
	 * output.
	 * 
	 * @return a reference to this analyzer
	 * @throws IOException if an I/O error occurred
	 */
        @Override
	public Analyzer printAnalysis() throws IOException {
		printAnalysis(System.out);
		
		return this;
	}
	
	/**
	 * Saves the reference set to the specified file.
	 * 
	 * @param file the file to which the reference set is saved
	 * @return a reference to this analyzer
	 * @see #getReferenceSet()
	 * @throws IOException if an I/O error occurred
	 */
        @Override
	public Analyzer saveReferenceSet(File file) throws IOException {
		PopulationIO.writeObjectives(file, getReferenceSet());
		
		return this;
	}
	
	/**
	 * Returns the reference set used by this analyzer.  The reference set is
	 * generated as follows:
	 * <ol>
	 *   <li>If {@link #withReferenceSet(File)} has been set, the contents of 
	 *       the reference set file are returned;
	 *   <li>If the problem factory provides a reference set via the
	 *       {@link ProblemFactory#getReferenceSet(String)} method, this
	 *       reference set is returned;
	 *   <li>Otherwise, the reference set is aggregated from all individual 
	 *       approximation sets.
	 * </ol>
	 * 
	 * @return the reference set used by this analyzer
	 * @throws IllegalArgumentException if the reference set could not be loaded
	 */
        @Override
	public NondominatedPopulation getReferenceSet() {
		try 
                {
                    NondominatedPopulation referenceSet = newArchive();

                    if (referenceSetFile == null) 
                    {
                        throw new IllegalArgumentException("no reference set available");

                    } else {
                            try {
                                    referenceSet.addAll(PopulationIO.readObjectives(referenceSetFile));
                            } catch (IOException e) {
                                    throw new IllegalArgumentException(
                                                    "unable to load reference set", e);
                            }
                    }

                    return referenceSet;
		} 
                catch (IllegalArgumentException e) 
                {
			if (referenceSetFile == null) {
				//return the combination of all approximation sets
				NondominatedPopulation referenceSet = newArchive();
				
				for (List<NondominatedPopulation> entry : data.values()) {
					for (NondominatedPopulation set : entry) {
						referenceSet.addAll(set);
					}
				}
				
				return referenceSet;
			} else {
				throw e;
			}
		}
	}
        
        NondominatedPopulation newArchive() {
            return new NondominatedPopulation(new ParetoDominanceComparator());
	}
        
        /**
	 * Prints the analysis of all data recorded in this analyzer.  
	 * 
	 * @param ps the stream to which the analysis is written
	 * @return a reference to this analyzer
	 * @throws IOException if an I/O error occurred
	 */
        @Override
	public NewAnalyzer printAnalysis(PrintStream ps) throws IOException {
		if (data.isEmpty()) {
			return this;
		}
		
		Problem problem = null;
		
		try {			
                        problem = getProblemInstance();
                        
			//instantiate the reference set
			NondominatedPopulation referenceSet = getReferenceSet();
			
			//setup the quality indicators
			List<Indicator> indicators = new ArrayList<Indicator>();
			
			if (includeHypervolume) {
				indicators.add(new Hypervolume(problem, referenceSet));
			}
			
			if (includeGenerationalDistance) {
				indicators.add(new GenerationalDistance(problem, referenceSet));
			}
			
			if (includeInvertedGenerationalDistance) {
				indicators.add(new InvertedGenerationalDistance(problem, 
						referenceSet));
			}
			
			if (includeAdditiveEpsilonIndicator) {
				indicators.add(new AdditiveEpsilonIndicator(problem, 
						referenceSet));
			}
			
			if (includeMaximumParetoFrontError) {
				indicators.add(new MaximumParetoFrontError(problem, 
						referenceSet));
			}
			
			if (includeSpacing) {
				indicators.add(new Spacing(problem));
			}
			
			if (includeContribution) {
					indicators.add(new Contribution(referenceSet));
			}
			
			if (indicators.isEmpty()) {
				System.err.println("no indicators selected");
				return this;
			}
			
			//generate the aggregate sets
			Map<String, NondominatedPopulation> aggregateSets =
					new HashMap<String, NondominatedPopulation>();
			
			if (showAggregate) {
				for (String algorithm : data.keySet()) {
					NondominatedPopulation aggregateSet = new NondominatedPopulation(new ParetoDominanceComparator());
					
					for (NondominatedPopulation set : data.get(algorithm)) {
						aggregateSet.addAll(set);
					}
					
					aggregateSets.put(algorithm, aggregateSet);
				}
			}
			
			//precompute the individual seed metrics, as they are used both
			//for descriptive statistics and statistical significance tests
			Map<String, Map<Indicator, double[]>> metrics = 
					new HashMap<String, Map<Indicator, double[]>>();
			
			for (String algorithm : data.keySet()) {
				Map<Indicator, double[]> entry = 
						new HashMap<Indicator, double[]>();
				
				for (Indicator indicator : indicators) {
					List<NondominatedPopulation> sets = data.get(algorithm);
					double[] values = new double[sets.size()];
					
					for (int i=0; i<sets.size(); i++) {
						values[i] = indicator.evaluate(sets.get(i));
					}
					
					entry.put(indicator, values);
				}
				
				metrics.put(algorithm, entry);
			}
			
			//precompute the statistical significance of the medians
			Map<Indicator, Map<String, List<String>>> indifferences =
					new HashMap<Indicator, Map<String, List<String>>>();
			
			if (showStatisticalSignificance) {
				List<String> algorithms = new ArrayList<String>(
						metrics.keySet());
				
				//initialize the storage
				for (Indicator indicator : indicators) {
					HashMap<String, List<String>> entry = 
							new HashMap<String, List<String>>();
					
					for (String algorithm : algorithms) {
						entry.put(algorithm, new ArrayList<String>());
					}
					
					indifferences.put(indicator, entry);
				}
				
				for (Indicator indicator : indicators) {
					//insufficient number of samples, skip test
					if (algorithms.size() < 2) {
						continue;
					}
					
					KruskalWallisTest kwTest = new KruskalWallisTest(
							algorithms.size());
					
					for (int i=0; i<algorithms.size(); i++) {
						kwTest.addAll(metrics.get(algorithms.get(i))
								.get(indicator), i);
					}
					
					try {
						if (!kwTest.test(significanceLevel)) {
							for (int i=0; i<algorithms.size()-1; i++) {
								for (int j=i+1; j<algorithms.size(); j++) {
									indifferences.get(indicator)
											.get(algorithms.get(i))
											.add(algorithms.get(j));
									indifferences.get(indicator)
											.get(algorithms.get(j))
											.add(algorithms.get(i));
								}
							}
						} else {
							for (int i=0; i<algorithms.size()-1; i++) {
								for (int j=i+1; j<algorithms.size(); j++) {
									MannWhitneyUTest mwTest = 
											new MannWhitneyUTest();
									
									mwTest.addAll(metrics.get(algorithms.get(i))
											.get(indicator), 0);
									mwTest.addAll(metrics.get(algorithms.get(j))
											.get(indicator), 1);
									
									if (!mwTest.test(significanceLevel)) {
										indifferences.get(indicator)
												.get(algorithms.get(i))
												.add(algorithms.get(j));
										indifferences.get(indicator)
												.get(algorithms.get(j))
												.add(algorithms.get(i));
									}
								}
							}
						}
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}
			}
			
			//print the results
			Min min = new Min();
			Max max = new Max();
			Median median = new Median();
			StandardDeviation standardDeviation = new StandardDeviation();
                        
			for (String algorithm : metrics.keySet()) {
				ps.print(algorithm);
				ps.println(':');
				
				for (Indicator indicator : indicators) {
					double[] values = metrics.get(algorithm).get(indicator);
					
					ps.print("    ");
					ps.print(indicator.getClass().getSimpleName());
					ps.print(": ");
					
					if (values.length == 0) {
						ps.print("null");
					} else if (values.length == 1) {
						ps.print(values[0]);
					} else {
						ps.println();
						
						if (showAggregate) {
							ps.print("        Aggregate: ");
							ps.println(indicator.evaluate(
									aggregateSets.get(algorithm)));
						}
						
						if (statistics.isEmpty()) {
							ps.print("        Min: ");
							ps.println(min.evaluate(values));
							ps.print("        Median: ");
							ps.println(median.evaluate(values));
							ps.print("        Max: ");
							ps.println(max.evaluate(values));
                                                        ps.print("        StandardDeviation: ");
							ps.println(standardDeviation.evaluate(values));
						} else {
							for (UnivariateStatistic statistic : statistics) {
								ps.print("        ");
								ps.print(statistic.getClass().getSimpleName());
								ps.print(": ");
								ps.println(statistic.evaluate(values));
							}
						}
						
						ps.print("        Count: ");
						ps.print(values.length);
						
						if (showStatisticalSignificance) {
							ps.println();
							ps.print("        Indifferent: ");
							ps.print(indifferences.get(indicator)
									.get(algorithm));
						}
						
						if (showIndividualValues) {
							ps.println();
							ps.print("        Values: ");
							ps.print(Arrays.toString(values));
						}
					}
					
					ps.println();
				}            
			}
                        ps.println();
                        ps.println("FORMATO HOJA DE CALCULO");
                        ps.println();
                        for (String algorithm : metrics.keySet()) 
                        {
                            ps.print(algorithm);
                            ps.println(':');
                            
                            for (Indicator indicator : indicators) 
                            {
                                double[] values = metrics.get(algorithm).get(indicator);
                                
                                if (values.length == 0) {
                                    ps.print("null");
				} else if (values.length == 1) {
                                    ps.print(values[0]);
				} else {
                                    
                                    ps.println(max.evaluate(values) + "\t" + 
                                            median.evaluate(values) + "\t" + 
                                            standardDeviation.evaluate(values));
                                }
                            }
                            ps.println();
                        }
                        
		} finally {
			if (problem != null) {
				problem.close();
			}
		}
		
		return this;
	}
    
}
