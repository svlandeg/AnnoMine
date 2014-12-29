package be.svlandeg.annomine.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * This class provides functionality to unify the input textual descriptions through lower-casing, stemming, etc.
 * It keeps track of the original lines and their transformed/cleaned versions.
 * 
 * @author Sofie Van Landeghem
 */
public class TextMapping
{
    /** maps the converted line back to the original*/
    protected Map<String, String> origmap;

    /** if set to true, matching will be done case-insensitive */
    protected boolean tolowercase;

    /** if set to true, matching will be done using stemming */
    protected boolean stemming;

    /** if set to true, unknown proteins are renamed to 'conserved hypothetical protein' */
    protected boolean unify_unknowns;
    
    /** Default value for removeEndPunctuation = true */
    protected boolean removeEndPunctuation;
    
    /** Default value for sortTokensAlphabetically = false */
    protected boolean sortTokensAlphabetically;


    /**
     * Create a new TextMapping object. By default, punctuation at the end of a string will be removed.
     * 
     * @param tolowercase if set to true, matching will be done case-insensitive
     * @param stemming whether or not to apply stemming of the input
     * @param unify_unknowns whether or not to unify strings such as 'hypothetical' and 'unknown' protein to the same input string
     */
    public TextMapping(boolean tolowercase, boolean stemming, boolean unify_unknowns)
    {
        this(tolowercase, stemming, unify_unknowns, true, false);
    }
    
    /**
     * Create a new TextMapping object. By default, punctuation at the end of a string will be removed.
     * 
     * @param tolowercase if set to true, matching will be done case-insensitive
     * @param stemming whether or not to apply stemming of the input
     * @param unify_unknowns whether or not to unify strings such as 'hypothetical' and 'unknown' protein to the same input string
     * @param removeEndPunctuation whether or not to remove unwanted punctuation at the end of a string
     * @param sortTokensAlphabetically whether or not to sort the tokens in a string in alphabetical order
     */
    public TextMapping(boolean tolowercase, boolean stemming, boolean unify_unknowns, boolean removeEndPunctuation, boolean sortTokensAlphabetically)
    {
        this.tolowercase = tolowercase;
        this.stemming = stemming;
        this.unify_unknowns = unify_unknowns;
        this.removeEndPunctuation = removeEndPunctuation;
        this.sortTokensAlphabetically = sortTokensAlphabetically;
        clean();
    }

    /**
     * Clear the cache (the list of original lines and their converted/cleaned versions)
     */
    public void clean()
    {
        origmap = new HashMap<String, String>();
    }

    /**
     * Retrieve the original string.
     * 
     * @param converted the converted string
     * @return the original form of a converted string (useful for printing)
     */
    public String retrieveOriginal(String converted)
    {
        return origmap.get(converted);
    }

    /**
     * This method aims to find the most sensible string for printing out of two options, in a consistent (deterministic) fashion.
     * 
     * If there's one with all capitals or all lowercase, and one with regular capitalization, chose the regular one.
     * Otherwise, prefer the string with matching number of opening and closing braces.
     * If all else is undecided, chose the shorter one or the one that lexically comes before the other.
     */
    private String chose(String first, String second)
    {
        if (first.toUpperCase().equals(first) && !second.toUpperCase().equals(second))
        {
            return second;  // prefer the second one that is NOT all upper case
        }
        if (!first.toUpperCase().equals(first) && second.toUpperCase().equals(second))
        {
            return first; // prefer the first one that is NOT all upper case
        }
        if (first.toLowerCase().equals(first) && !second.toLowerCase().equals(second))
        {
            return second;  // prefer the second one that is NOT all lower case
        }
        if (!first.toLowerCase().equals(first) && second.toLowerCase().equals(second))
        {
            return first;   // prefer the first one that is NOT all lower case
        }

        int first_openbraces = first.length() - first.replace("(", "").length();
        int first_closebraces = first.length() - first.replace(")", "").length();
        int second_openbraces = second.length() - second.replace("(", "").length();
        int second_closebraces = second.length() - second.replace(")", "").length();

        if ((first_openbraces == first_closebraces) && (second_openbraces != second_closebraces))
        {
            if (!(first.startsWith("(") && first.endsWith(")")))
            {
                return first;   // prefer the one with equal number of ( and )
            }
        }
        if ((first_openbraces != first_closebraces) && (second_openbraces == second_closebraces))
        {
            if (!(second.startsWith("(") && second.endsWith(")")))
            {
                return second;   // prefer the one with equal number of ( and )
            }
        }

        first_openbraces = first.length() - first.replace("[", "").length();
        first_closebraces = first.length() - first.replace("]", "").length();
        second_openbraces = second.length() - second.replace("[", "").length();
        second_closebraces = second.length() - second.replace("]", "").length();

        if ((first_openbraces == first_closebraces) && (second_openbraces != second_closebraces))
        {
            if (!(first.startsWith("[") && first.endsWith("]")))
            {
                return first;   // prefer the one with equal number of [ and ]
            }
        }
        if ((first_openbraces != first_closebraces) && (second_openbraces == second_closebraces))
        {
            if (!(second.startsWith("[") && second.endsWith("]")))
            {
                return second;   // prefer the one with equal number of [ and ]
            }
        }

        if (first.length() < second.length())
        {
            return first;   // prefer the first one that is smaller
        }
        if (first.compareTo(second) < 0)
        {
            return first;   // prefer the first one that lexically comes before the second one
        }
        return second;
    }

    /**
     * Remove punctuation marks that should not be at the end of a string, such as , or [ 
     * This method runs until all such marks are removed from the input line, and then returns the cleaned version.
     */
    private String stripRemovalsAtEnd(String line)
    {
        String result = line;
        Set<String> puncts = endRemovals();
        
        // check whether the last character is an unwanted punctuation symbol
        String last = result.substring(result.length() - 1, result.length());
        while (puncts.contains(last) && result.length() > 0)
        {
            result = result.substring(0, result.length() - 1);
            if (result.length() > 0)
            {
                last = result.substring(result.length() - 1, result.length());
            }
        }
        return result;
    }
    
    /**
     * This method sorts the tokens in a line in alphabetical order
     */
	private String getSortedSubstring(String line)
    {
        SortedSet<String> substrings = new TreeSet<String>();
        StringTokenizer stok = new StringTokenizer(line, " ");
        while (stok.hasMoreTokens())
        {
        	substrings.add(stok.nextToken());
        }

        String ssubstring = "";
        for (String ss : substrings)
        {
        	ssubstring += ss.trim() + " ";
        }
        
        return ssubstring.trim();
    }

    /**
     * Return the set of symbols that delimit words, and are thus ignored when comparing strings/tokens, because we only compare tokens.
     * TODO: if these need to be length 1, make them characters!
     */
    private Set<String> wordDelimiters()
    {
        Set<String> puncts = new HashSet<String>();
        puncts.add(".");
        puncts.add(",");
        puncts.add(";");
        puncts.add(":");
        puncts.add("/");
        
        puncts.add("|");
        //puncts.add("_");
        puncts.add("[");
        puncts.add("]");
        puncts.add("{");
        puncts.add("}");
        puncts.add("(");
        puncts.add(")");
        
        puncts.add("?");
        puncts.add("!");
        puncts.add("\'");
        puncts.add("&");
        puncts.add("*");
        puncts.add("=");
        puncts.add(" ");
        puncts.add("\t");
        
        /* 
         * These punctuation marks are considered to be too informative for the strings themselves
         * They are preserved with the methods Preprocess.convertToMeta and Postprocess.convertFromMeta
         * 
         * puncts.add("+");
         * puncts.add("-");		// this would break things like "ATP-dependent"
         */

        return puncts;
    }
    
    /**
     * Return the set of symbols that can be used within strings, but not at the beginning of end (in which case they become a delimiter)
     */
    private Set<Character> intermediatePunctuation()
    {
        Set<Character> puncts = new HashSet<Character>();
        puncts.add('_');
        return puncts;
    }

    /**
     * Return the set of symbols that should not be at the end of a string.
     * TODO: if these need to be length 1, make them characters!
     */
    private Set<String> endRemovals()
    {
        Set<String> puncts = new HashSet<String>();
        puncts.add(".");
        puncts.add(",");
        puncts.add(";");
        puncts.add(":");
        puncts.add("/");
        puncts.add("-");
        puncts.add("|");
        puncts.add("_");
        puncts.add("[");
        puncts.add("{");
        puncts.add("(");
        puncts.add("+");
        puncts.add("?");
        puncts.add("!");
        puncts.add("\'");
        puncts.add("&");
        puncts.add("*");
        puncts.add("=");
        puncts.add(" ");
        puncts.add("\t");

        return puncts;
    }

    /**
     * Retrieve the actual content of a tab-delimited line, removing the first token before the first (and only) tab.
     * 
     * @param line the original line
     * @return the second column (after the first and only tab)
     */
    public String getLine(String line)
    {
        StringTokenizer stok = new StringTokenizer(line, "\t");
        stok.nextToken();
        return stok.nextToken();
    }

    /**
     * Parse the next tab-delimited token as an integer and interpret as the weight.
     * 
     * @param line the original line
     * @return the weight of this description (which is on the first tab-delimited token)
     */
    public int getWeight(String line)
    {
        StringTokenizer stok = new StringTokenizer(line, "\t");
        return Integer.parseInt(stok.nextToken());
    }

    /**
     * Convert a string to the new variant: remove punctuation, apply lowercase/stemming if needed, etc.
     * 
     * @param line the original line
     * @param substrings define whether substrings should also be calculated and inserted in the internal mapping
     * @param switchorder decides whether the order of the words in the description line can be moved around
     * @return a cleaned copy of the original line
     */
    public String convert(String line, boolean substrings, boolean switchorder)
    {
    	// remove all punctuation that can be in the middle, but not at the beginning of end of a string
    	// we do this by looking at cases where such punctuation occurs with a space, and in those cases, only keep the space
    	Set<Character> punct_intermediate = intermediatePunctuation();
        for (Character punct : punct_intermediate)
        {
        	line.replace(punct + " ", " ");
        	line.replace(" " + punct, " ");
        }
    	
    	
        Set<String> punct_delimiters = wordDelimiters();
        String splitstring = "";
        for (String delimit : punct_delimiters)
        {
            splitstring += delimit;
        }
        StringTokenizer stok = new StringTokenizer(line, splitstring);
        String convertedlinesave = "";
        String convertedlinesub = "";
        SortedSet<String> tokens_sorted = new TreeSet<String>();
        List<String> tokens_listed = new ArrayList<String>();
        while (stok.hasMoreTokens())
        {
            String token = stok.nextToken();
            if (stemming)
            {
                token = new Stemmer().stem(token);
            }
            if (tolowercase)
            {
                token = token.toLowerCase();
            } 
            tokens_sorted.add(token);
            tokens_listed.add(token);

        }
        if (switchorder)
        {
            for (String tok : tokens_sorted)
            {
                convertedlinesave += tok + " ";
            }
        }
        else
        {
            for (String tok : tokens_listed)
            {
                convertedlinesave += tok + " ";
            }
        }

        for (String tok : tokens_listed)
        {
            convertedlinesub += tok + " ";
        }

        convertedlinesub = convertedlinesub.trim();
        convertedlinesave = convertedlinesave.trim();
        add(line, convertedlinesave);
        if (substrings)
        {
            addSubstrings(line, switchorder);
        }
        return convertedlinesub;
    }

    /**
     * Check whether an input string contains words such as "hypothetical protein" or "predicted protein", or whether it equals to "unknown" or "protein" (matched lowercase).
     * In these cases, return the string "conserved unknown protein" to ensure more homogeneity of the output results
     * 
     * @param input the input string
     * @return the output string, which is either equal to the input, or the string "conserved unknown protein" if the input refers to a generic description
     */
    public String convertHypothetical(String input)
    {
        String lowercaseinput = input.toLowerCase().trim();
        if (lowercaseinput.contains("hypothetical protein") || lowercaseinput.equals("protein")
                || lowercaseinput.contains("predicted protein") || lowercaseinput.equals("unknown protein")
                || lowercaseinput.equals("unnamed protein") || lowercaseinput.contains("conserved unknown protein")
                || lowercaseinput.equals("family protein") || lowercaseinput.equals("unknown")
                || lowercaseinput.equals("unnamed protein product"))
        {
            return "conserved unknown protein";
        }
        return input;
    }

    /**
     * Add a line to this text mapping object, by unifying the hypothetical protein descriptions and keeping the mapping to this input string and its converted version.
     */
    private void add(String line, String convertedline)
    {
    	if (unify_unknowns)
        {
            line = convertHypothetical(line);
        }
        if (!origmap.containsKey(convertedline))
        {
            origmap.put(convertedline, line);
        }
        else
        {
            String newstring = chose(origmap.get(convertedline), line);
            origmap.put(convertedline, newstring);
        }
    }

    /**
     * Add the substrings of a specific line to the mapping.
     * Various parameters deal with the preprocessing of the line, such as allowing the order of the words to switch or removing unwanted punctuation at the end.
     */
    private void addSubstrings(String fullline, boolean switchorder)
    {
        Set<String> removals = wordDelimiters();
        String splitstring = "";
        for (String remove : removals)
        {
            splitstring += remove;
        }
        StringTokenizer stok = new StringTokenizer(fullline, splitstring, true);
        List<String> tokens = new ArrayList<String>();
        String token = null;
        while (stok.hasMoreTokens() || token != null)
        {
            if (token == null)
            {
                token = stok.nextToken();
            }
            String word = token;
            token = null;
            boolean go = true;
            while (stok.hasMoreTokens() && go)
            {
                token = stok.nextToken();
                if (removals.contains(token))
                {
                    word += token;
                    token = null;
                }
                else
                {
                    go = false;
                }
            }
            tokens.add(word);
        }
        int totaltokens = tokens.size();
        for (int ngram = 1; ngram <= totaltokens; ngram++)
        {
            for (int start = 0; start < totaltokens - ngram + 1; start++)
            {
                String substring = "";
                for (int j = start; j < start + ngram; j++)
                {
                    substring += tokens.get(j);
                }
                if (removeEndPunctuation)
                {
                	substring = stripRemovalsAtEnd(substring.trim());
                }
                if (sortTokensAlphabetically)
                {
                	substring = getSortedSubstring(substring);
                }
                if (substring != null && !substring.isEmpty())
                {
                    convert(substring, false, switchorder);
                }
            }
        }
    }
    
}
