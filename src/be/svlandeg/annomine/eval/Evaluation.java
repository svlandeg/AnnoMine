package be.svlandeg.annomine.eval;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import be.svlandeg.annomine.data.Description;

/**
 * Class that comparse the manual annotations to the computationally generated ones, 
 * and produces performance measures from that comparison.
 * 
 * @author Sofie Van Landeghem
 */
public class Evaluation
{

	private static boolean printAffixes = true;
	private static boolean printSubs = true;
	private static boolean printDifferences = true;

	private enum Comparison
	{
		EQUAL, EQUAL_AFFIX, LONGER, SHORTER, DIFFERENT
	};

	/**
	 * Main method that runs the evaluation
	 * 
	 * @param manuals the manual annotations, mapped by query gene
	 * @param comp_notraining the predictions, mapped by query gene
	 * @throws IOException when an IO error occurs
	 */
	public void eval(Map<String, List<String>> manuals, Map<String, Set<Description>> comp_notraining) throws IOException
	{
		/* We only evaluate those predictions that are in the manual gold standard, 
		 * excluding for instance test instance during the training phase */
		Set<String> allQueries = new HashSet<String>();
		allQueries.addAll(manuals.keySet());
		
		int TP = 0;
		int TN = 0;
		int FN = 0;
		
		int countShorter = 0;
		int countLonger = 0;
		int countDifferent = 0;
		
		int countFP_NegativeGold = 0;
		int countNextManualConsulted = 0;

		Map<String, Integer> equals_affix = new HashMap<String, Integer>();
		Map<String, Integer> shorters = new HashMap<String, Integer>();
		Map<String, Integer> longers = new HashMap<String, Integer>();
		Map<String, Integer> differences = new HashMap<String, Integer>();

		for (String query : allQueries)
		{
			List<String> ms = manuals.get(query);
			Set<Description> cs = comp_notraining.get(query);

			if (ms != null && ms.size() >= 1 && cs != null && cs.size() >= 1)
			{
				// take the first one of each (TODO: generalize)
				Iterator<String> mIt = ms.iterator();
				String m = mIt.next();
				String c = cs.iterator().next().getDescription();
				
				Comparison result = compareCleaned(m, c);
				
				String cleanM = clean(m);
				String cleanC = clean(c);
				Comparison cleanResult = compareCleaned(cleanM, cleanC);

				/* In case we don't get a good comparison with the first manual hit, check the next one */
				boolean firstTime = true;
				while (cleanResult.equals(Comparison.DIFFERENT) && mIt.hasNext())
				{
					m = mIt.next();
					cleanM = clean(m);
					result = compareCleaned(m, c);
					cleanResult = compareCleaned(cleanM, cleanC);
					if (firstTime)
					{
						countNextManualConsulted++;
						firstTime = false;
					}
				}
				if (isUndefined(c) && isUndefined(m))
				{
					TN++;
				}
				else if (isUndefined(c) && !isUndefined(m))
				{
					// we predicted undefined function, but manual annotation had one
					FN++;
				}
				else if (!isUndefined(c) && isUndefined(m))
				{
					// we predicted a function, but manual annotation says its undefined
					countFP_NegativeGold++;
				}
				else if (result.equals(Comparison.EQUAL))
				{
					TP++;
				}
				else if (cleanResult.equals(Comparison.EQUAL))
				{
					TP++;
					
					String affix = m + " <-> " + c;

					if (!equals_affix.containsKey(affix))
					{
						equals_affix.put(affix, 0);
					}
					equals_affix.put(affix, 1 + equals_affix.get(affix));
				}
				else if (cleanResult.equals(Comparison.SHORTER))
				{
					countShorter++;

					String shorter = m + " -> " + c;

					if (!shorters.containsKey(shorter))
					{
						shorters.put(shorter, 0);
					}
					shorters.put(shorter, 1 + shorters.get(shorter));
				}
				else if (cleanResult.equals(Comparison.LONGER))
				{
					countLonger++;
					String longer = m + " <- " + c;

					if (!longers.containsKey(longer))
					{
						longers.put(longer, 0);
					}
					longers.put(longer, 1 + longers.get(longer));
				}
				else if (cleanResult.equals(Comparison.DIFFERENT))
				{
					countDifferent++;
					String difference = m + " <-> " + c;

					if (!differences.containsKey(difference))
					{
						differences.put(difference, 0);
					}
					differences.put(difference, 1 + differences.get(difference));
				}

			}
			else
			{
				System.out.println("Found something odd for " + query);
				System.out.println("ms : " + ms + " - cs : " + cs);
				System.out.println("");
			}
		}
		
		if (printAffixes)
		{
			System.out.println("");
			System.out.println(" Equal except for affixes: manual <-> predicted:");
			for (String e : equals_affix.keySet())
			{
				System.out.println("  " + e + " -> " + equals_affix.get(e) + " cases");
			}
		}
		
		if (printSubs)
		{
			System.out.println("");
			System.out.println(" Shorter: m  ->  c:");
			for (String shorter : shorters.keySet())
			{
				System.out.println("  " + shorter + " -> " + shorters.get(shorter) + " cases");
			}

			System.out.println("");
			System.out.println(" Longer: m  <-  c:");
			for (String longer : longers.keySet())
			{
				System.out.println("  " + longer + " -> " + longers.get(longer) + " cases");
			}
		}

		if (printDifferences)
		{
			System.out.println("");
			System.out.println(" Differences manual <-> predicted:");
			for (String difference : differences.keySet())
			{
				System.out.println("  " + difference + " -> " + differences.get(difference) + " cases");
			}
		}
		
		int different = FN + countFP_NegativeGold + countDifferent;
		int total = TP + TN + countShorter + countLonger + different;

		int goldNegatives = TN + countFP_NegativeGold;
		int goldPositives = total - goldNegatives;

		int FP = countFP_NegativeGold + countDifferent + countShorter + countLonger;

		int predictedPositives = (TP + FP);
		int predictedNegatives = (TN + FN);

		double precision = (double) 100 * TP / predictedPositives;
		double recall = (double) 100 * TP / goldPositives;
		double F = 2 * precision * recall / (precision + recall);
		
		double specificity = 100 * TN / goldNegatives;

		System.out.println("");
		System.out.println(" Performance");
		System.out.println("  Predicted positives: " + predictedPositives);
		System.out.println("  Predicted negatives: " + predictedNegatives);
		System.out.println("  Gold positives: " + goldPositives);
		System.out.println("  Gold negatives: " + goldNegatives);
		System.out.println("");
		System.out.println("  TP: " + TP);
		System.out.println("  FP: " + FP + " (" + countShorter + " shorter, " + countLonger + " longer, " + countDifferent + " probably wrong functions and " + countFP_NegativeGold + " invented function)");
		System.out.println("  FN: " + FN + " (missing function)");
		System.out.println("  TN: " + TN);
		System.out.println("");
		System.out.println("  MINIMAL precision/TPR: " + TP + "/" + predictedPositives + " = " + precision + "%");
		System.out.println("  MINIMAL recall/sensitivity: " + TP + "/" + goldPositives + " = " + recall + "%");
		System.out.println("  MINIMAL F-score: " + F + "%");
		System.out.println("  specificity/TNR: " + TN + "/" + goldNegatives + " = " + specificity + "%");

		NumberFormat formatter = new DecimalFormat("#0,00"); 
		
		System.out.println("");
		System.out.println("  tp tn fn fp shorter longer different invented precision recall F specificity");
		System.out.println("  " + TP + "\t" + TN + "\t" + FN + "\t" + FP + "\t" + "\t" + 
				countShorter + "\t" + countLonger + "\t" + countDifferent + "\t" + countFP_NegativeGold + "\t" + "\t" + 
				formatter.format(100*precision) + "\t" + formatter.format(100*recall) + "\t" + formatter.format(100*F) + "\t" + formatter.format(100*specificity));
	}

	/**
	 * Define whether a manual annotation or computational prediction is a 'hypothetical protein'
	 * @param description
	 * @return
	 */
	protected static boolean isUndefined(String description)
	{
		String lowerDescription = description.toLowerCase();
		boolean isUndefined = false;

		if (lowerDescription.equals("hypothetical protein"))
		{
			isUndefined = true;
		}
		else if (lowerDescription.equals("uncharacterized protein"))
		{
			isUndefined = true;
		}
		else if (lowerDescription.equals("predicted protein"))
		{
			isUndefined = true;
		}
		else if (lowerDescription.equals("conserved unknown protein"))
		{
			isUndefined = true;
		}
		else if (lowerDescription.equals("conserved hypothetical protein"))
		{
			isUndefined = true;
		}
		else if (lowerDescription.equals("unnamed protein product"))
		{
			isUndefined = true;
		}
		else if (lowerDescription.startsWith("hypothetical protein") || lowerDescription.startsWith("uncharacterized protein"))
		{
			StringTokenizer stok = new StringTokenizer(description, " ");

			// "hypothetical protein XXX"
			if (stok.countTokens() == 3)
			{
				isUndefined = true;
			}
		}

		return isUndefined;
	}

	// TODO: move to AnnoMine core!
	private String clean(String orig)
	{
		String result = new String(orig).toLowerCase().trim();
		
		boolean go = true;
		
		while (go)
		{
			int startLength = result.trim().length();
			
			if (result.startsWith("uncharacterized"))
			{
				result = result.substring(15).trim();
			}
			if (result.startsWith("predicted:"))
			{
				result = result.substring(10).trim();
			}
			if (result.startsWith("predicted"))
			{
				result = result.substring(9).trim();
			}
			if (result.startsWith("probable"))
			{
				result = result.substring(8).trim();
			}
			if (result.startsWith("putative"))
			{
				result = result.substring(8).trim();
			}
			
			if (result.endsWith(", putative"))
			{
				result = result.substring(0, result.length() - 10).trim();
			}
			if (result.endsWith("(ISS)"))
			{
				result = result.substring(0, result.length() - 5).trim();
			}
			if (result.endsWith("protein"))
			{
				result = result.substring(0, result.length() - 7).trim();
			}
			if (result.endsWith("homolog"))
			{
				result = result.substring(0, result.length() - 7).trim();
			}
			if (result.endsWith("isoform x1"))
			{
				result = result.substring(0, result.length() - 10).trim();
			}
			if (result.endsWith("isoform x2"))
			{
				result = result.substring(0, result.length() - 10).trim();
			}
			if (result.endsWith("isoform x3"))
			{
				result = result.substring(0, result.length() - 10).trim();
			}
			if (result.endsWith("isoform"))
			{
				result = result.substring(0, result.length() - 7).trim();
			}
			if (result.endsWith("-like"))
			{
				result = result.substring(0, result.length() - 5).trim();
			}
			go = result.trim().length() != startLength;
		}

		return result;
	}

	/**
	 * Compare two strings and return the 'level of equality'
	 */
	private Comparison compareCleaned(String gold, String prediction)
	{
		if (gold.equals(prediction))
		{
			return Comparison.EQUAL;
		}
		if (gold.indexOf(prediction) >= 0)
		{
			return Comparison.SHORTER;
		}
		if (gold.indexOf(prediction) >= 0)
		{
			return Comparison.SHORTER;
		}

		String editGold = gold.replace("-", " ");
		editGold = editGold.replace(",", "");

		String editPrediction = prediction.replace("-", " ");
		editPrediction = editPrediction.replace(",", "");

		if (editGold.equals(editPrediction))
		{
			return Comparison.EQUAL;
		}

		if (editGold.indexOf(editPrediction) >= 0)
		{
			return Comparison.SHORTER;
		}
		if (editPrediction.indexOf(editGold) >= 0)
		{
			return Comparison.LONGER;
		}
		return Comparison.DIFFERENT;
	}

	
	/**
	 * Main method
	 * @param args - not used
	 * @throws IOException when an IO error occurs
	 */
	public static void main(String[] args) throws IOException
	{
		AnnotationsReader ar = new AnnotationsReader();
		//String location = Dataset.compDir + Dataset.training_name;
		String location = Dataset.compDir + Dataset.testing_name; 
		Map<String, List<String>> manuals = ar.readManualAnnotations(location, false);

		// NO TRAINING

		//String description = "AnnoMine no training";
		//Map<String, Set<Description>> comp = ar.readComputationalAnnotations(Dataset.compDir + Dataset.annomine_notraining);

		//String description = "AnnoMine no training and no weights";
		//Map<String, Set<Description>> comp = ar.readComputationalAnnotations(Dataset.compDir + Dataset.annomine_notraining_noweights);

		// WITH TRAINING

		//String description = "AnnoMine small training";
		//Map<String, Set<Description>> comp = ar.readComputationalAnnotations(Dataset.compDir + Dataset.annomine_smalltraining);

		//String description = "AnnoMine small training and no weights";
		//Map<String, Set<Description>> comp = ar.readComputationalAnnotations(Dataset.compDir + Dataset.annomine_smalltraining_noweights);

		//String description = "AnnoMine medium training";
		//Map<String, Set<Description>> comp = ar.readComputationalAnnotations(Dataset.compDir + Dataset.annomine_mediumtraining);

		//String description = "AnnoMine medium training and no weights";
		//Map<String, Set<Description>> comp = ar.readComputationalAnnotations(Dataset.compDir + Dataset.annomine_mediumtraining_noweights);

		// BASELINES

		//String description = "best hit baseline";
		//Map<String, Set<Description>> comp = ar.readComputationalAnnotations(Dataset.compDir + Dataset.baseline_besthit);
		
		String description = "best hit baseline, avoiding hypotheticals";
		Map<String, Set<Description>> comp = ar.readComputationalAnnotations(Dataset.compDir + Dataset.baseline_besthit_nohypothetical);

		System.out.println("Running evaluation of " + location + " against " + description + " (" + comp.keySet().size() + " prediction against " + manuals.keySet().size() + " manuals).");

		new Evaluation().eval(manuals, comp);

		System.out.println("");
		System.out.println("Done! ");
	}

}
