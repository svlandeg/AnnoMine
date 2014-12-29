package be.svlandeg.annomine.io;

import be.svlandeg.annomine.algorithms.TextMapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.SortedMap;

/**
 * Class that prints the output according to specified parameters.
 * 
 * @author Sofie Van Landeghem
 */
public class Output
{

	/**
	 * results should have scores/weights at least higher than this cutoff
	 */
	protected int weightCutoff;
	private boolean debug = false;
	private String newline;

	/**
	 * Create a new output object with a certain weight cutoff and a newline character for printing
	 * 
	 * @param weightCutoff the weight cutoff
	 * @param newline the a newline character for printing
	 */
	public Output(int weightCutoff, String newline)
	{
		this.weightCutoff = weightCutoff;
		this.newline = newline;
	}

	/**
	 * Print all the n-grams in the collection, either to an output fill or to the standard output stream.
	 * Don't print the ones with scores less than the cutoff, or with scores (removefactor) times lower than the highest score n-gram.
	 * 
	 * @param tm the object that deals with text variation
	 * @param outputfile the file to which to write the output, or null when it should be printed to System.out
	 * @param query put null if you don't want the query to be printed
	 * @param all_nGrams the n-grams that need to be printed
	 * @param append if the output file is not null, this parameter decides whether the results are appended in the file, or the file is erased first
	 * @param nr specifies the maximum number of results printen
	 * @throws IOException when a problem occurs writing the results to the output stream
	 */
	public void printBestResults(TextMapping tm, File outputfile, String query, SortedMap<Double, Set<String>> all_nGrams, boolean append, int nr) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		if (outputfile != null)
		{
			System.out.println("  printing to " + outputfile + newline);
			writer = new BufferedWriter(new FileWriter(outputfile, append));
		}

		if (debug)
		{
			System.out.println(" Results:" + newline);
			System.out.println("  cutoff : " + weightCutoff + newline);
			System.out.println(newline);
		}
		if (all_nGrams.isEmpty())
		{
			System.out.println("   no results" + newline);
			writer.close();
			return;
		}
		//if (d > cutoff)
		int written = 0;
		for (double d : all_nGrams.keySet())
		{
			Set<String> ngrams = all_nGrams.get(d);
			d = d * (-1);
			if (debug)
			{
				System.out.println(newline);
				System.out.println("d : " + d + newline);
			}
			if (d >= weightCutoff)
			{
				for (String s : ngrams)
				{
					if (debug)
					{
						System.out.println("s : " + s + newline);
					}
					if (written < nr)
					{
						String orig = tm.retrieveOriginal(s);
						if (debug)
						{
							System.out.println("orig : " + orig + newline);
						}
						if (orig != null)
						{
							String postorig = new Postprocess().removeUnmatchedBraces(orig);
							postorig = new Postprocess().convertFromMeta(orig);
							if (debug)
							{
								System.out.println("post : " + postorig + newline);
							}
							if (query != null && postorig.length() > 2)
							{
								postorig = new Postprocess().removePunctuation(
										postorig);
								if (debug)
								{
									System.out.println("post2 : " + postorig + newline);
								}
								if (!isStrangePunctuation(postorig))
								{
									writer.write(query + "\t");
									writer.write(d + "\t" + postorig + newline);
									writer.newLine();
									writer.flush();
									written++;
									if (debug)
									{
										System.out.println("written!" + newline);
									}
								}
								if (debug)
								{
									System.out.println(newline);
								}
							}
							else
							{
								if (debug)
								{
									System.out.println("deleted!" + newline);
								}
							}
						}
						else
						{
							System.out.println("   ! could not find a textual mapping for '" + s + "'" + newline);
						}
					}
				}
			}
		}
		if (written == 0) // output this when no other hit was found
		{
			writer.write(query + "\t");
			writer.write((weightCutoff + 1) + "\t" + "conserved unknown protein" + newline);
			writer.newLine();
			writer.flush();
			written++;
		}
		writer.close();
		if (debug)
		{
			System.out.println(newline);
		}
	}

	/**
	 * Check whether the punctuation of this n-gram makes sense. If not, return false (and the n-gram will not be printed).
	 */
	private boolean isStrangePunctuation(String orig)
	{
		int comma = orig.indexOf(",");
		if (comma > -1 && comma < 3)
		{
			return true;
		}
		comma = orig.indexOf(";");
		if (comma > -1 && comma < 3)
		{
			return true;
		}
		return false;
	}
}
