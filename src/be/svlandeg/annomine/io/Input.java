package be.svlandeg.annomine.io;

import be.svlandeg.annomine.algorithms.TextMapping;
import be.svlandeg.annomine.data.Description;

import java.util.StringTokenizer;

/**
 * Class that reads the input according to the specified parameters
 * @author Sofie Van Landeghem
 */
public class Input
{
    protected double default_score = 1.0;

    protected TextMapping tm;

    public Input(TextMapping tm)
    {
        this.tm = tm;
    }

    /**
     * Read from a tab-delimited file. A description with default score (1.0) will be returned.
     * 
     * @param line the tab-delimited line
     * @param description_col the column number that specifies the description (start counting columns from 0!)
     * @param substrings define whether substrings should also be calculated and inserted in the internal mapping
     * @param switchorder decides whether the order of the words in the description line can be moved around
     * @return the functional description
     * @throws java.lang.IllegalArgumentException when an invalid column index is encountered
     */
    public Description readFromTab(String line, int description_col, boolean substrings, boolean switchorder) throws IllegalArgumentException
    {
        String description = readColumn(line, description_col);
        description = preprocess(description, substrings, switchorder);

        double score = default_score;
        return new Description(description, score);
    }

    /**
     * Read from a tab delimited file. A description with default score (1) will be returned.
     * 
     * @param line the tab-delimited line
     * @param description_col the column number that specifies the description (start counting columns from 0!)
     * @param query_col the column number that specifies the query gene (start counting columns from 0!)
     * @param substrings define whether substrings should also be calculated and inserted in the internal mapping
     * @param switchorder specifies whether the order of the words in the description line can be moved around, or not
     * @return the functional description
     * @throws java.lang.IllegalArgumentException when an invalid column index is encountered
     */
    public Description readFromTab(String line, int description_col, int query_col, boolean substrings, boolean switchorder) throws IllegalArgumentException
    {
        String description = null;
        description = readColumn(line, description_col);
        description = preprocess(description, substrings, switchorder);

        double score = default_score;

        String query = readColumn(line, query_col);

        return new Description(description, score, query);
    }

    /**
     * Read from a tab delimited file, adding scores from a certain column to each description.
     * @param line the tab-delimited line
     * @param score_col the column number that specifies the score (start counting columns from 0!)
     * @param description_col the column number that specifies the description (start counting columns from 0!)
     * @param substrings define whether substrings should also be calculated and inserted in the internal mapping
     * @param switchorder specifies whether the order of the words in the description line can be moved around, or not
     * @return  the functional description
     * @throws java.lang.IllegalArgumentException when an invalid column index is encountered
     */
    public Description readScoredFromTab(String line, int score_col, int description_col, boolean substrings, boolean switchorder) throws IllegalArgumentException
    {
        String description = readColumn(line, description_col);
        description = preprocess(description, substrings, switchorder);

        double score = Integer.parseInt(readColumn(line, score_col).trim());

        return new Description(description, score);
    }

    /**
     * Read from a tab delimited file, calculating scores from the evalue column for each description.
     * @param line the tab-delimited line
     * @param score_col the column number that specifies the score (start counting columns from 0!)
     * @param description_col the column number that specifies the description (start counting columns from 0!)
     * @param substrings define whether substrings should also be calculated and inserted in the internal mapping
     * @param switchorder specifies whether the order of the words in the description line can be moved around, or not
     * @return the functional description
     * @throws java.lang.IllegalArgumentException when an invalid column index is encountered
     */
    public Description readEvaluedFromTab(String line, int score_col, int description_col, boolean substrings, boolean switchorder) throws IllegalArgumentException
    {
        String description = readColumn(line, description_col);
        description = preprocess(description, substrings, switchorder);

        double score = 1 - Integer.parseInt(readColumn(line, score_col).trim());

        return new Description(description, score);
    }

    /**
     * Read from a tab delimited file, adding scores from a certain column to each description.
     * @param line the tab-delimited line
     * @param score_col the column number that specifies the score (start counting columns from 0!)
     * @param description_col the column number that specifies the description (start counting columns from 0!)
     * @param query_col the column number that specifies the query gene (start counting columns from 0!)
     * @param substrings define whether substrings should also be calculated and inserted in the internal mapping
     * @param switchorder specifies whether the order of the words in the description line can be moved around, or not
     * @return the functional description
     * @throws java.lang.IllegalArgumentException when an invalid column index is encountered
     */
    public Description readScoredFromTab(String line, int score_col, int description_col, int query_col, boolean substrings, boolean switchorder) throws IllegalArgumentException
    {
        String description = null;
        description = readColumn(line, description_col);
        description = preprocess(description, substrings, switchorder);

        String stringscore = readColumn(line, score_col).trim();
        Double dscore = Double.parseDouble(stringscore);

        String query = readColumn(line, query_col);

        return new Description(description, dscore, query);
    }

    /**
     * Read from a tab delimited file, calculating scores from the evalue column for each description.
     * @param line the tab-delimited line
     * @param score_col the column number that specifies the score (start counting columns from 0!)
     * @param description_col the column number that specifies the description (start counting columns from 0!)
     * @param query_col the column number that specifies the query gene (start counting columns from 0!)
     * @param substrings define whether substrings should also be calculated and inserted in the internal mapping
     * @param switchorder specifies whether the order of the words in the description line can be moved around, or not
     * @return the functional description
     * @throws java.lang.IllegalArgumentException when an invalid column index is encountered
     */
    public Description readEvaluedFromTab(String line, int score_col, int description_col, int query_col, boolean substrings, boolean switchorder) throws IllegalArgumentException
    {
        String description = null;
        description = readColumn(line, description_col);
        description = preprocess(description, substrings, switchorder);

        double score = default_score;
        String stringscore = readColumn(line, score_col).trim();
        if (Double.parseDouble(stringscore) == 0.0)
        {
            score = 250.0;
        }
        else
        {
            double evalue = Double.parseDouble(stringscore);
            while (evalue < 1)
            {
                evalue *= 10;
                score++;
            }
        }

        String query = readColumn(line, query_col);
        return new Description(description, score, query);
    }

    /**
     * This method checks whether the given col integer is valid and throws an IllegalArgumentException otherwise
     */
    private String readColumn(String line, int col) throws IllegalArgumentException
    {
        StringTokenizer stok = new StringTokenizer(line, "\t");
        int cols = stok.countTokens();
        if (col >= cols)
        {
            throw new IllegalArgumentException("  ! " + col + " is not a valid column (total columns: " + cols + ")");
        }
        int nr = 0;
        while (stok.hasMoreTokens())
        {
            String token = stok.nextToken();
            if (nr == col)
            {
                return token;
            }
            nr++;
        }
        System.out.println(" ! This should never happen");
        System.out.println("   line : " + line);
        System.out.println("   col : " + col);
        return null;
    }

    /**
     * Preprocess a description by removing braces, stopwords, strange words, and possible even switch the order of the words
     */
    private String preprocess(String line, boolean substrings, boolean switchorder)
    {
        Preprocess pp = new Preprocess();
        String convertedline = pp.removeBraces(line);
        convertedline = pp.removeStopWords(convertedline);
        convertedline = pp.convertToMeta(convertedline);
        convertedline = convertedline.trim();
        if (tm != null)
        {
            convertedline = tm.convert(convertedline, substrings, switchorder);
        }
        return convertedline;
    }
}
