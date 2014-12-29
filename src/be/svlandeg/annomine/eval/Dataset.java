package be.svlandeg.annomine.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * This dataset reads the manual annotations and randomly picks a training set and a test set.
 * This ensures that the evaluation algorithm and the optimal parameter settings can be set on the training set,
 * and the test set can be used to obtain an objective performance measure of the tool as a whole.
 * 
 * @author Sofie Van Landeghem
 */
public class Dataset
{
	
	// TODO: hard-coded paths are used because these evaluations are only run once for the performance assessment in the publication

	private int trainingSize = 125;

	public static String all_manual_annotations = "C:\\Users\\Sofie\\Documents\\phd\\annomine-brhel\\dataset\\manual_func_annotations.txt";
	public static String compDir = "C:\\Users\\Sofie\\Documents\\phd\\annomine-brhel\\dataset\\sofie-annomine\\";
	
	//public static String all_manual_annotations = "X:\\groups\\group_biocomp\\projects\\annomine\\brhel\\dataset\\manual_func_annotations.txt";
	//public static String compDir = "X:\\groups\\group_biocomp\\projects\\annomine\\brhel\\dataset\\sofie-annomine\\";
	
	public static String annomine_notraining = "annomine_notraining.tab";
	public static String annomine_notraining_noweights = "annomine_notraining_noweights.tab";
	
	public static String annomine_smalltraining = "annomine_smalltraining.tab";
	public static String annomine_smalltraining_noweights = "annomine_smalltraining_noweights.tab";
	public static String annomine_mediumtraining = "annomine_mediumtraining.tab";
	public static String annomine_mediumtraining_noweights = "annomine_mediumtraining_noweights.tab";
	
	public static String baseline_besthit = "baseline_besthit.tab";
	public static String baseline_besthit_nohypothetical = "baseline_besthit_nohypo.tab";
	
	public static String orig_descriptions = "dataset1_PROT_vs_nrprot.topfuncdesc";
	
	public static String training_name = "gold_training.tab";
	public static String testing_name = "gold_testing.tab";

	/**
	 * Clean the dataset by removing those manual annotations which read "ERROR"
	 * @param dataset the original dataset
	 * @return the clean dataset
	 */
	public Map<String, List<String>> clean(Map<String, List<String>> dataset)
	{
		Map<String, List<String>> cleanDataset = new HashMap<String, List<String>>();
		for (String query : dataset.keySet())
		{
			List<String> annotations = dataset.get(query);
			if (annotations.size() == 1 && annotations.get(0).equals("ERROR"))
			{
				/* do nothing - this entry is ignored. The query gene did not have BLAST hits and is thus removed from further evaluation */
				System.out.println(" discarded " + query);
			}
			else
			{
				cleanDataset.put(query, annotations);
			}
		}
		return cleanDataset;
	}

	/**
	 * Define the training and test split of the (cleaned!) dataset
	 * @param dataset the original (cleaned but otherwise complete) dataset
	 * @throws IOException when an IO error occurs
	 */
	public void defineTrainingSplit(Map<String, List<String>> dataset) throws IOException
	{
		Set<String> testQueries = new HashSet<String>(dataset.keySet());
		Set<String> trainingQueries = new HashSet<String>();

		Random rand = new Random();
		while (trainingQueries.size() < trainingSize)
		{
			int randomIndex = rand.nextInt(testQueries.size());
			String query = new ArrayList<String>(testQueries).get(randomIndex);
			trainingQueries.add(query);
			testQueries.remove(query);
		}

		System.out.println(" found " + trainingQueries.size() + " training queries ");
		System.out.println(" found " + testQueries.size() + " test queries ");

		write(trainingQueries, dataset, compDir+training_name);
		write(testQueries, dataset, compDir+testing_name);
	}

	/** 
	 * Write a specific set of query genes to a file, with their original annotations
	 */
	private void write(Set<String> queries, Map<String, List<String>> dataset, String location) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(location)));
		for (String query : queries)
		{
			writer.append(query);
			for (String annotation : dataset.get(query))
			{
				writer.append("\t");
				writer.append(annotation);
			}
			writer.newLine();
			writer.flush();
		}
		writer.flush();
		writer.close();
	}

	/**
	 * Main method: fetch the original data, clean it by discarding non-blast-hits, and then divide it into training and testing
	 * Currently disabled in order not to generate novel train/test splits by accident (in other words, to ensure a stable training set)
	 * 
	 * @param args - not used
	 * @throws IOException when an IO error occurs
	 */
	public static void main_disabled(String[] args) throws IOException
	{
		AnnotationsReader ar = new AnnotationsReader();
		Dataset ds = new Dataset();

		System.out.println("Reading and cleaning the original manual annoations");
		Map<String, List<String>> dataset = ar.readManualAnnotations(Dataset.all_manual_annotations, true);
		Map<String, List<String>> cleanDataset = ds.clean(dataset);

		System.out.println(" ");
		System.out.println("Defining the training and test split");
		ds.defineTrainingSplit(cleanDataset);
		
		System.out.println(" ");
		System.out.println("Done!");
	}
}
