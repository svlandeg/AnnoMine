package be.svlandeg.annomine.io;

import be.svlandeg.annomine.algorithms.TextMapping;
import be.svlandeg.annomine.data.Description;

/**
 * This custom reader reads various DescriptionsLists, each from a different file.
 * TODO: what is this reader used for?!
 * 
 * @author Sofie Van Landeghem
 */
public class SeparateFileReader extends Reader
{
	
    public String name;
    

    /**
     * Create a reader object that will read Description batches from a certain directory, all containing seperate files
     * 
     * @param tm the object that deals with text variation
     * @param evalue_col the column number that specifies the e-value (start counting columns from 0!)
     * @param score_col the column number that specifies the score (start counting columns from 0!)
     * @param description_col the column number that specifies the description itself (start counting columns from 0!)
     * @param name the name of the query gene
     */
    public SeparateFileReader(TextMapping tm, int evalue_col, int score_col, int description_col, String name)
    {
        super(tm, evalue_col, score_col, description_col);
        this.name = name;
    }

    @Override
    public Description readDescription(String line, boolean substrings, boolean switchorder) throws IllegalArgumentException
    {
        Description dl = null;
        if (score_col >= 0)
        {
            dl = new Input(tm).readScoredFromTab(line, score_col, description_col, substrings, switchorder);
        }
        else if (evalue_col >= 0)
        {
            dl = new Input(tm).readEvaluedFromTab(line, evalue_col, description_col, query_col, substrings, switchorder);
        }
        else
        {
            dl = new Input(tm).readFromTab(line, description_col, substrings, switchorder);
        }
        dl.setQuery(name);
        return dl;
    }
}
