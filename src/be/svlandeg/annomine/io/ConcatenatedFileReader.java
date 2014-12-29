package be.svlandeg.annomine.io;

import be.svlandeg.annomine.algorithms.TextMapping;
import be.svlandeg.annomine.data.Description;
import be.svlandeg.annomine.data.DescriptionsList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * This custom reader reads various DescriptionsLists from one file. The batches should be in one big file, ordered by query gene, as the file is read only once consecutively.
 * @author Sofie Van Landeghem
 */
public class ConcatenatedFileReader extends Reader
{
	
    protected int row_counter;  // remembers where in the file we were reading

    /**
     * Create a reader object that will read Description batches from tab-delimited files.
     * 
     * @param tm the object that deals with text variation
     * @param evalue_col the column number that specifies the e-value (start counting columns from 0!)
     * @param score_col the column number that specifies the score (start counting columns from 0!)
     * @param description_col the column number that specifies the description itself (start counting columns from 0!)
     * @param query_col the column number that specifies the query gene (start counting columns from 0!)
     */
    public ConcatenatedFileReader(TextMapping tm, int evalue_col, int score_col, int description_col, int query_col)
    {
        super(tm, evalue_col, score_col, description_col, query_col);
        row_counter = 0;
    }

    @Override
    public Description readDescription(String line, boolean substrings, boolean switchorder) throws IllegalArgumentException
    {
        Description dl = null;
        if (score_col >= 0)
        {
            dl = new Input(tm).readScoredFromTab(line, score_col, description_col, query_col, substrings, switchorder);
        }
        else if (evalue_col >= 0)
        {
            dl = new Input(tm).readEvaluedFromTab(line, evalue_col, description_col, query_col, substrings, switchorder);
        }
        else
        {
            dl = new Input(tm).readFromTab(line, description_col, query_col, substrings, switchorder);
        }
        return dl;
    }

    /**
     * Read a description batch from a certain file.
     * 
     * @param file the input file
     * @param substrings the first time this should be true: the converted versions of all substrings will then be stored in the TextMapping object
     * @param switchorder decides whether the order of the words in the description line can be moved around
     * @param newline the newline character, used when printing the error message
     * @return a description batch
     * @throws IOException when the input file could not be read properly
     * @throws IllegalArgumentException when an invalid column index is encountered
     */
    public DescriptionsList readNextDescription(File file, boolean substrings, boolean switchorder, String newline) throws IOException
    {
        String filename = file.getName();

        BufferedReader reader = null;
        int counter = 0;
        DescriptionsList dl = null;
        try
        {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();

            while (line != null)
            {
                if (!line.toLowerCase().startsWith("querylocus"))
                {
                    if (counter >= row_counter)
                    {
                        Description d = readDescription(line, substrings, switchorder);
                        String thisquery = d.getQuery();
                        if (dl == null)
                        {
                            dl = new DescriptionsList(thisquery);
                        }
                        String query = dl.getName();
                        if (query != null && !query.equals(thisquery))
                        {
                            // we've reached the next query
                            row_counter = counter;
                            reader.close();
                            return dl;
                        }
                        dl.addDescription(d);
                    }
                }
                counter++;
                line = reader.readLine();
            }
            reader.close();
        }
        catch (IOException e)
        {
        	reader.close();
            System.out.println("Error: Couldn't read " + filename + newline);
            System.out.println(e.getMessage() + newline);
            throw (e);
        }
        row_counter = counter;
        return dl;
    }
}