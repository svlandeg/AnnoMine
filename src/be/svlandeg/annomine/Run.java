package be.svlandeg.annomine;

import be.svlandeg.annomine.algorithms.Normalization;
import be.svlandeg.annomine.algorithms.Overrepresentation;
import be.svlandeg.annomine.algorithms.TextMapping;
import be.svlandeg.annomine.data.DescriptionsList;
import be.svlandeg.annomine.io.ConcatenatedFileReader;
import be.svlandeg.annomine.io.Output;
import be.svlandeg.annomine.io.SeparateFileReader;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;

/**
 * Run the complete pipeline, depending on the format of input and output.
 * 
 * @author Sofie Van Landeghem
 */
public class Run
{

	protected Overrepresentation op;

	/* the internal TextMapping object that unifies the String input */
	protected TextMapping tm;

	/**
	 * Create a new run object, specifying the classes to calculate overrepresentation and to deal with text input.
	 * 
	 * @param op the object that calculates overrepresentation
	 * @param tm the object that deals with text variation
	 */
	public Run(Overrepresentation op, TextMapping tm)
	{
		this.op = op;
		this.tm = tm;
	}

	/**
	 * Run the pipeline from directories.
	 * 
	 * @param backgrounddir the directory containing the background model
	 * @param testdir the directory containing the test files
	 * @param outputdir the directory to which to write the output
	 * @param weights whether or not to take weights into account
	 * @param perc used to enforce a weight cutoff
	 * @param normalization whether or not to normalize weights
	 * @param printnr the number of required output results per query gene
	 * @param weightCutoff the weight cutoff
	 * @param newline the newline character, used when printing the error message
	 * @param printLog whether or not to print the normalization results to standard output
	 */
	public void runFromDirectories(File backgrounddir, File testdir, String outputdir, boolean weights, int perc, boolean normalization, int printnr, int weightCutoff, String newline, boolean printLog)
	{
		// TODO
		printnr = 1;
		
		boolean switchorder = false;
		op.cleanBackgroundModel();
		if (backgrounddir != null)
		{
			if (printLog)
			{
				System.out.println(newline);
				System.out.println("Building background from " + backgrounddir.getAbsolutePath() + newline);
			}
			// when creating the background model, don't apply any weights
			Set<DescriptionsList> descriptions = new SeparateFileReader(tm, -1, -1, 0, "background").readBatchLists(backgrounddir.getAbsolutePath(), true, switchorder, newline);
			op.calculateBackgroundModel(descriptions);
		}

		iniDirectories(outputdir);

		int score_col = -1;
		int descr_col = 0;
		if (weights)
		{
			score_col = 0;
			descr_col = 1;
		}

		for (File testfile : testdir.listFiles())
		{
			if (printLog)
			{
				System.out.println(newline);
				System.out.println("Testing " + testfile + newline);
			}
			String testname = testfile.getName();
			try
			{
				tm.clean();
				op.cleanAllResults();
				DescriptionsList test = new SeparateFileReader(tm, score_col, -1, descr_col, testname).readList(testfile, true, switchorder, newline);
				if (perc <= 100 && perc > 0)
				{
					test = new Normalization().enforceWeightCutoff(test, perc);
				}
				if (normalization)
				{
					test = new Normalization().linearNormalization(test, printLog);
				}
				boolean allOK = op.calculate(test, switchorder);
				if (!allOK)
				{
					return;
				}
				SortedMap<Double, Set<String>> all_nGrams = op.getAllResults();
				File outputFile = new File(outputdir + "prediction_" + testname);
				new Output(weightCutoff, "").printBestResults(tm, outputFile, null, all_nGrams, false, printnr);
			}
			catch (IOException e)
			{
				System.out.println(" ! Couldn't read " + testname + newline);
				System.out.println(e.getMessage() + newline);
			}
			catch (IllegalArgumentException e)
			{
				System.out.println(e.getMessage() + newline);
			}
		}
	}

	/**
	 * Run the pipeline from files.
	 * 
	 * @param backgroundfile the file containing the background model
	 * @param testfile the file containing the test data
	 * @param outputfile the file to which to write the output
	 * @param evalue_col the column number that specifies the e-value (start counting columns from 0!)
	 * @param score_col the column number that specifies the score (start counting columns from 0!)
	 * @param description_col the column number that specifies the description itself (start counting columns from 0!)
	 * @param query_col the column number that specifies the query gene (start counting columns from 0!)
	 * @param perc used to enforce a weight cutoff
	 * @param normalization whether or not to normalize weights
	 * @param printnr the number of required output results per query gene
	 * @param weightCutoff the weight cutoff
	 * @param newline the newline character, used when printing the error message
	 * @param printLog whether or not to print the normalization results to standard output
	 */
	public void runFromFiles(File backgroundfile, File testfile, File outputfile, int evalue_col, int score_col, int description_col, int query_col, int perc, boolean normalization, int printnr, int weightCutoff, String newline, boolean printLog)
	{
		boolean switchorder = false;
		op.cleanBackgroundModel();
		if (backgroundfile != null)
		{
			String backgroundname = backgroundfile.getName();
			if (printLog)
			{
				System.out.println(newline);
				System.out.println("Building background from: " + backgroundfile + newline);
			}
			// when creating the background model, don't apply any weights
			try
			{
				DescriptionsList background_dl = new SeparateFileReader(tm, -1, -1, 0, "background").readList(backgroundfile, true, switchorder, newline);
				op.calculateBackgroundModel(background_dl);
			}
			catch (IOException e)
			{
				System.out.println(" ! Couldn't read background file" + backgroundname + newline);
				System.out.println(e.getMessage() + newline);
				op.cleanBackgroundModel();
				return;
			}
			catch (IllegalArgumentException e)
			{
				System.out.println(e.getMessage() + newline);
				op.cleanBackgroundModel();
			}
		}
		else if (printLog)
		{
			System.out.println(newline);
			System.out.println("no background model " + newline);
		}

		if (printLog)
		{
			System.out.println(newline);
			System.out.println("testing " + testfile + newline);
		}

		ConcatenatedFileReader reader = new ConcatenatedFileReader(tm, evalue_col, score_col, description_col, query_col);
		iniFile(outputfile, newline);
		int i = 0;

		boolean append = false;
		try
		{
			tm.clean();
			DescriptionsList dl = reader.readNextDescription(testfile, true, switchorder, newline);
			while (dl != null)
			{
				if (i % 25 == 0)
				{
					if (printLog)
					{
						System.out.println(newline);
						System.out.println("  Cleaning up memory" + newline);
						System.out.println(newline);
					}
					System.gc();

				}
				String query = dl.getName();
				if (query != null && !query.toLowerCase().equals("querylocus"))
				{
					op.cleanAllResults();
					if (printLog)
					{
						System.out.println("  calculating test batch for query " + dl.getName() + newline);
					}
					if (perc <= 100 && perc > 0)
					{
						dl = new Normalization().enforceWeightCutoff(dl, perc);
					}
					if (normalization)
					{
						dl = new Normalization().linearNormalization(dl, printLog);
					}
					boolean allOK = op.calculate(dl, switchorder);
					if (!allOK)
					{
						return;
					}
					SortedMap<Double, Set<String>> all_nGrams = op.getAllResults();

					//new Output().printResults(tm, outputfile, query, all_nGrams, append);  //create a new file the first time, then append the rest
					if (outputfile == null)
					{
						newline = " <br />"; // we're printing to the browser
					}
					new Output(weightCutoff, newline).printBestResults(tm, outputfile, query, all_nGrams, append, printnr); //create a new file the first time, then append the rest
					append = true;
					tm.clean();
					dl = reader.readNextDescription(testfile, true, switchorder, newline);
					if (printLog)
					{
						System.out.println(newline);
					}
					i++;
					all_nGrams = null;
				}
			}
		}
		catch (IOException e)
		{
			System.out.println("Error: Couldn't write results." + newline);
			System.out.println(" " + e.getMessage() + newline);
		}
		if (printLog)
		{
			System.out.println("DONE " + newline);
		}

	}

	/**
	 * Create the output file and its parent locations, if necessary
	 */
	private void iniFile(File outputfile, String newline)
	{
		// make sure the outputdir is an empty folder
		if (outputfile != null)
		{
			if (outputfile.exists())
			{
				System.out.println(" ! Outputfile existed --> overwriting" + newline);
			}
			else
			{
				new File(outputfile.getParent()).mkdirs();
			}
		}
	}

	/**
	 * Create the output directory and its parent locations, if necessary
	 */
	private void iniDirectories(String outputdir)
	{
		// make sure the outputdir is an empty folder
		if (new File(outputdir).exists())
		{
			for (File f : new File(outputdir).listFiles())
			{
				f.delete();
			}
		}
		else
		{
			new File(outputdir).mkdirs();
		}
	}
}
