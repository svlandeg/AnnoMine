package be.svlandeg.annomine.io;

import be.svlandeg.annomine.algorithms.TextMapping;
import be.svlandeg.annomine.data.Description;
import be.svlandeg.annomine.data.DescriptionsList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Reads a DescriptionsList from file, or a list of file names
 * @author Sofie Van Landeghem
 */
public abstract class Reader
{
	
    protected TextMapping tm;

    protected int evalue_col;
    protected int score_col;
    protected int description_col;
    protected int query_col;

    /**
     * Constructor with default values for col_evalue, col_score and query_col (all -1).
     * 
     * @param tm the object that deals with text variation
     * @param description_col the column number that specifies the description itself (start counting columns from 0!)
     */
    public Reader(TextMapping tm, int description_col)
    {
        this(tm, -1, -1, description_col, -1);
    }

    /**
     * Constructor with default value for query_col (-1).
     * 
     * @param tm the object that deals with text variation
     * @param evalue_col the column number that specifies the e-value (start counting columns from 0!)
     * @param score_col the column number that specifies the score (start counting columns from 0!)
     * @param description_col the column number that specifies the description itself (start counting columns from 0!)
     */
    public Reader(TextMapping tm, int evalue_col, int score_col, int description_col)
    {
        this(tm, evalue_col, score_col, description_col, -1);
    }

    /**
     * Create a reader object that will read Description batches from a certain directory.
     * 
     * @param tm the object that deals with text variation
     * @param evalue_col the column number that specifies the e-value (start counting columns from 0!)
     * @param score_col the column number that specifies the score (start counting columns from 0!)
     * @param description_col the column number that specifies the description itself (start counting columns from 0!)
     * @param query_col the column number that specifies the query gene (start counting columns from 0!)
     */
    public Reader(TextMapping tm, int evalue_col, int score_col, int description_col, int query_col)
    {
        this.tm = tm;
        this.evalue_col = evalue_col;
        this.score_col = score_col;
        this.description_col = description_col;
        this.query_col = query_col;
    }

    /**
     * Read a description batch from a certain file. 
     * 
     * @param file the file containing the descriptions
     * @param firsttime the first time, the converted versions of all substrings are stored in the TextMapping object
     * @param switchorder decides whether the order of the words in the description line can be moved around
     * @param newline the newline character, used when printing the error message
     * @return a description batch, or null when an IO error occurred (i.e. the file doesn't exist or couldn't be read)
     * @throws IOException when the input file could not be read properly
     * @throws IllegalArgumentException when an invalid column index is encountered
     */
    public DescriptionsList readList(File file, boolean firsttime, boolean switchorder, String newline) throws IOException, IllegalArgumentException
    {
        String filename = file.getName();
        DescriptionsList dl = new DescriptionsList(filename);
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null)
            {
                dl.addDescription(readDescription(line, firsttime, switchorder));
                line = reader.readLine();
            }
            reader.close();
        }
        catch (IOException e)
        {
            System.out.println("Error: Couldn't read " + filename + newline);
            System.out.println(e.getMessage() + newline);
            reader.close();
            throw (e);
        }
        return dl;
    }

    /**
     * Abstract method: read a functional description from one tab-delimited line.
     *
     * @param line the tab-delimted line
     * @param substrings define whether substrings should also be calculated and inserted in the internal mapping
     * @param switchorder decides whether the order of the words in the description line can be moved around
     * @return the functional description
     * @throws IllegalArgumentException when an invalid column index is encountered
     */
    public abstract Description readDescription(String line, boolean substrings, boolean switchorder);

    /**
     * Read a batch of descriptions from an input directory.
     * 
     * @param dir the input directory
     * @param substrings define whether substrings should also be calculated and inserted in the internal mapping
     * @param switchorder decides whether the order of the words in the description line can be moved around
     * @param newline the newline character, used when printing the error message
     * @return the batch of descriptions
     */
    public Set<DescriptionsList> readBatchLists(String dir, boolean substrings, boolean switchorder, String newline)
    {
        Set<DescriptionsList> descriptions = new HashSet<DescriptionsList>();

        for (File f : new File(dir).listFiles())
        {
            try
            {
                DescriptionsList dl = readList(f, substrings, switchorder, newline);
                descriptions.add(dl);
            }
            catch (IOException e)
            {
                // simply don't add the dl to the list
            }
            catch (IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
            }
        }
        return descriptions;
    }

    /**
     * Read a list of file names from a certain file.
     * 
     * @param file the input file
     * @return the list of file names contained in the input file, or null when an IO error occurred
     */
    public Set<String> readFiles(File file)
    {
        Set<String> set = new HashSet<String>();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null)
            {
                set.add(line);
                line = reader.readLine();
            }
            reader.close();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            try{
            	reader.close();
            }
            catch (IOException e2)
            {}
            set = null;
        }
        return set;
    }
}
