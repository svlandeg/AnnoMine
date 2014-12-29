package be.svlandeg.annomine.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Class that simulates baseline-predictors for the evaluation data
 * 
 * @author Sofie Van Landeghem
 */
public class Baseline
{

	/**
	 * Calculate a simple baseline: taking the first (best) blast hit
	 * 
	 * @param location_descriptions the original descriptions
	 * @param location_outputFile the location where to write the baseline results
	 * @param avoidHypothetical whether or not to try and find the first non-hypothetical hit
	 * @throws IOException when an IO error occurs
	 */
	public void calculateBaseline(String location_descriptions, String location_outputFile, boolean avoidHypothetical) throws IOException
	{
		String currentQuery = null;
		boolean writtenCurrent = false;

		BufferedWriter writer = new BufferedWriter(new FileWriter(location_outputFile));

		BufferedReader reader = new BufferedReader(new FileReader(location_descriptions));
		String line = reader.readLine();
		while (line != null)
		{
			StringTokenizer stok = new StringTokenizer(line, "\t");
			String newQuery = stok.nextToken().trim();
			String id = stok.nextToken().trim();
			Double weight = Double.parseDouble(stok.nextToken());
			String description = cleanBaselineHit(stok.nextToken());
			
			boolean isHypothetical = Evaluation.isUndefined(description);
			boolean isFirst = currentQuery == null || ! newQuery.equals(currentQuery);
			
			if (isFirst)
			{
				// we didn't write the previous query, so this means it's hypothetical anyway
				if (! writtenCurrent && currentQuery != null)
				{
					writer.append(currentQuery + "\t" + 1.0 + "\t" + "hypothetical protein");
					writer.newLine();
					writer.flush();
					writtenCurrent = true;
				}
				writtenCurrent = false;
			}
			
			if (! writtenCurrent)
			{
				// we just write the first one
				if (! avoidHypothetical)
				{
					writer.append(newQuery + "\t" + weight + "\t" + description);
					writer.newLine();
					writer.flush();
					writtenCurrent = true;
				}
				
				// avoiding hypotheticals, we write this function if it's not a hypothetical
				else if (! isHypothetical)
				{
					writer.append(newQuery + "\t" + weight + "\t" + description);
					writer.newLine();
					writer.flush();
					writtenCurrent = true;
				}
			}
			currentQuery = newQuery;
			line = reader.readLine();
		}
		reader.close();

		writer.flush();
		writer.close();
	}

	/**
	 * (minimally) clean the blast hit, by removing the organism between []
	 * 
	 * This already goes a little into the actual algorithm of AnnoMine ... is this still a baseline?!
	 */
	private String cleanBaselineHit(String orig)
	{
		int open = orig.lastIndexOf("[");
		int close = orig.lastIndexOf("]");

		String result = new String(orig);

		if (open >= 0 && close >= 0 && open < close)
		{
			result = orig.substring(0, open);
			result += orig.substring(close + 1); 
		}
		return result.trim();
	}
	
	public static void main(String[] args) throws IOException
	{
		//boolean avoidHypothetical = false;
		//String file = Dataset.baseline_besthit;
		
		boolean avoidHypothetical = true;
		String file = Dataset.baseline_besthit_nohypothetical;
	
		Baseline bl = new Baseline();
		bl.calculateBaseline(Dataset.compDir + Dataset.orig_descriptions, Dataset.compDir + file, avoidHypothetical);
	}
}
