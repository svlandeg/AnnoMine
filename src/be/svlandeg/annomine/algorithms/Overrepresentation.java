package be.svlandeg.annomine.algorithms;

import be.svlandeg.annomine.Environment;
import be.svlandeg.annomine.data.Description;
import be.svlandeg.annomine.data.DescriptionsList;

import com.aliasi.lm.TokenizedLM;
import com.aliasi.lm.UniformBoundaryLM;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ScoredObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * The Overrepresentation class contains the main algorithm to determine significant phrases using the Lingpipe library.
 * Overrepresented terms are modeled as n-grams, with n ranging from a specified minimum to a specified maximum.
 * 
 * @author Sofie Van Landeghem
 */
public class Overrepresentation
{
	/** minimal count to be included in the new results */
	protected int min_count_ngram;

	/** Default value for maximum returned results = 25 */
	public static int max_returned_results = 25;

	/** minimum occurrence count to prune sequence counter */
	public static int min_count_occ = 2;

	/** all new n grams : first this should be calculated, then reported */
	protected SortedMap<Double, Set<String>> all_nGrams;

	/** the biggest possible n-gram for consideration as term */
	protected int max_ngram;

	/** the smallest possible n-gram for consideration as term */
	protected int min_ngram;

	/** if set to true, matching will be done case-insensitive */
	protected boolean lowercase;

	/** The LingPipe tokenizer factory, by default an 'IndoEuropeanTokenizerFactory' */
	protected TokenizerFactory tf;

	/** backgrounds, indexed by the size (n) of the n-gram */
	public TokenizedLM[] backgrounds;

	/**
	 * Create a new Overrepresentation object, specifying the minimum and maximum size (n) of an n-gram, as well as the minimum count an n-gram should have before considering it as a valid output.
	 * 
	 * @param min_count_ngram minimal count to be included in the new results
	 * @param min_ngram the smallest possible n-gram for consideration as term
	 * @param max_ngram the biggest possible n-gram for consideration as term
	 * @param lowercase if set to true, matching will be done case-insensitive
	 */
	public Overrepresentation(int min_count_ngram, int min_ngram, int max_ngram, boolean lowercase)
	{
		this.min_ngram = min_ngram;
		this.max_ngram = max_ngram;
		this.min_count_ngram = min_count_ngram;
		this.lowercase = lowercase;
		backgrounds = new TokenizedLM[max_ngram - min_ngram + 1];
		tf = IndoEuropeanTokenizerFactory.INSTANCE;
		all_nGrams = new TreeMap<Double, Set<String>>();
	}

	/**
	 * Return all overrepresented n-grams as a sorted resultmap. These results should have been calculated by first creating a background model and then calculating the overrepresentation of new terms.
	 * 
	 * @return all resulting n-grams, sorted by score
	 */
	public SortedMap<Double, Set<String>> getAllResults()
	{
		return all_nGrams;
	}

	/**
	 * Clean the previously calculated overrepresented n-grams. This method does not alter (or remove) the background model, which should be done with {@link #cleanBackgroundModel()}!
	 */
	public void cleanAllResults()
	{
		all_nGrams = new TreeMap<Double, Set<String>>();
	}

	/**
	 * Clean the background model. This does not clean the calculated results, which should be done with {@link #cleanAllResults()}!
	 */
	public void cleanBackgroundModel()
	{
		backgrounds = new TokenizedLM[max_ngram - max_ngram + 1];
	}

	/**
	 * Method that determines a background model from a given description list.
	 * 
	 * @param descriptions the descriptions that form the background data
	 */
	public void calculateBackgroundModel(DescriptionsList descriptions)
	{
		Set<DescriptionsList> model_data = new HashSet<DescriptionsList>();
		model_data.add(descriptions);
		calculateBackgroundModel(model_data);
	}

	/**
	 * Method that determines a background model from a given set of description lists.
	 * A model is built for all possible n-grams between min_ngram and max_ngram.
	 * 
	 * @param descriptionLists the description lists that form the background data
	 */
	public void calculateBackgroundModel(Set<DescriptionsList> descriptionLists)
	{
		for (int ngram = max_ngram; ngram > (min_ngram - 1); ngram--)
		{
			int pos = ngram - min_ngram;
			if (backgrounds[pos] == null)
			{
				backgrounds[pos] = new TokenizedLM(tf, ngram, new UniformBoundaryLM(), new UniformBoundaryLM(), ngram);
				backgrounds[pos].sequenceCounter().prune(min_count_occ);
			}
			for (DescriptionsList list : descriptionLists)
			{
				for (Description dl : list.getDescriptions())
				{
					String line = dl.getDescription();
					double weight = dl.getWeight();
					if (weight >= 0)
					{
						int intweight = new Double(weight).intValue();
						backgrounds[pos].train(line, intweight);
					}
				}
			}
		}
	}


	/**
	 * Method to calculate the overrepresentation of n-grams within a list of descriptions, using the background model (if built previously).
	 * The results of this method are stored in the all_nGrams object.
	 * 
	 * @param test the input descriptions
	 * @param switchorder whether or not the order of words can be switched
	 * @return whether everything went OK or not
	 */
	public boolean calculate(DescriptionsList test, boolean switchorder)
	{
		all_nGrams = new TreeMap<Double, Set<String>>();

		for (int ngram = max_ngram; ngram > (min_ngram - 1); ngram--)
		{
			boolean allOK = calculate(test, ngram, switchorder);
			if (!allOK)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Method that calculates the overrepresentation of n-grams within a list of descriptions. 
	 * First, a 'foreground' model is built using the descriptions in the list, and their weights. 
	 * If there is a background model previously built, this foreground model is compared by searching for overrepesented, new terms in the foreground. 
	 * If there is no background previously built, the most frequently terms are simply determined from this input alone, which may lead to a bias towards overrepresented but uninformative information.
	 * 
	 * The results of this method are stored in the all_nGrams object. The method returns true when it was able to end succesfully.
	 */
	private boolean calculate(DescriptionsList descriptions, int ngram, boolean switchorder)
	{
		int pos = ngram - min_ngram;
		TokenizedLM foregroundModel = new TokenizedLM(tf, ngram);
		foregroundModel.sequenceCounter().prune(min_count_occ);
		
		for (Description dl : descriptions.getDescriptions())
		{
			String line = dl.getDescription();
			double weight = dl.getWeight();
			int intweight = new Double(weight).intValue();
			if (weight >= 0 && intweight >= 0)
			{
				try
				{
					if (switchorder)
					{
						Set<String> substrings = getSubstrings(line, ngram);

						for (String ss : substrings)
						{
							foregroundModel.train(ss, intweight);
						}
					}
					else
					{
						foregroundModel.train(line, intweight);
					}
				}
				catch (Exception e)
				{
					String newline = Environment.getNewline(); 
					System.out.println("An error occurred when applying weights. Are you sure you specified the correct column? " + newline);
					System.out.println("If the problem persists, contact solan AT psb DOT ugent DOT be for help with this issue." + newline);
					return false;
				}
			}
		}

		if (pos >= 0 && pos < backgrounds.length && backgrounds[pos] != null)
		{
			SortedSet<ScoredObject<String[]>> newTerms = foregroundModel.newTermSet(ngram, min_count_ngram, max_returned_results, backgrounds[pos]);
			report(newTerms);
		}
		else
		{
			SortedSet<ScoredObject<String[]>> terms = foregroundModel.frequentTermSet(ngram, max_returned_results);
			report(terms);
		}
		return true;

	}
	
	/**
	 * Return all n-grams in a certain line, specifying the n parameter. The tokens in the n-grams are internally sorted alphabetically.
	 * This way, the same n-gram is recognised irrespective of the internal word order (e.g. 'signalling enzyme' and 'enzyme, signalling')
	 */
	private Set<String> getSubstrings(String line, int ngram)
	{
		Set<String> substrings = new HashSet<String>();
		
		// store all tokens in a list
		StringTokenizer stok = new StringTokenizer(line, " ");
		List<String> tokens = new ArrayList<String>();
		while (stok.hasMoreTokens())
		{
			tokens.add(stok.nextToken());
		}
		int totaltokens = tokens.size();

		// go through the list of tokens using a window of 'ngram' long
		for (int start = 0; start < totaltokens - ngram + 1; start++)
		{
			// within the window, store the tokens in a sorted set, then concatenate them again using a space
			SortedSet<String> substring = new TreeSet<String>();
			for (int j = start; j < start + ngram; j++)
			{
				substring.add(tokens.get(j));
			}
			if (!substring.isEmpty())
			{
				String string = "";
				for (String ss : substring)
				{
					string += ss + " ";
				}
				substrings.add(string.trim());
			}
		}
		return substrings;
	}

	/**
	 * Add these overrepresented ngrams to the collection of results, stored in all_nGrams
	 */
	private void report(SortedSet<ScoredObject<String[]>> nGrams)
	{
		for (ScoredObject<String[]> nGram : nGrams)
		{
			double score = nGram.score();
			String[] toks = nGram.getObject();
			String tok = "";
			for (String t : toks)
			{
				tok += t + " ";
			}
			tok = tok.trim();

			score = score * (-1);
			if (score < 0)
			{
				if (!all_nGrams.containsKey(score))
				{
					all_nGrams.put(score, new HashSet<String>());
				}
				all_nGrams.get(score).add(tok);
			}
		}
	}
}
