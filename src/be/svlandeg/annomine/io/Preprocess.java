package be.svlandeg.annomine.io;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import be.svlandeg.annomine.data.Description;
import be.svlandeg.annomine.data.DescriptionsList;

/**
 * Class to preprocess the input descriptions
 * @author Sofie Van Landeghem
 */
public class Preprocess
{
	
	public static String hyphenMeta = "hyphen";
	public static String plusMeta = "plus";
	
	/**
	 * Remove the last part of the line which ends in [] 
	 * This removes meta data such as organism names
	 * 
	 * @param line the original line
	 * @return a copied version of the line, but the last part between [] removed
	 */
    public String removeBraces(String line)
    {
        String convertedline = line.trim();
        int index = convertedline.lastIndexOf("[");
        if (index > 0 && line.endsWith("]"))
        {
            convertedline = convertedline.substring(0, index);
        }
        return convertedline.trim();
    }

    /**
     * Convert meaningful punctuation symbols such as + to their "meta" variants, such as "PLUS". This is needed to run the algorithm in an optimal fashion.
     * See also {@link Postprocess#convertFromMeta(String)}
     * 
     * @param line the original line
     * @return a copied version of the line, but with meaningfull punctuation symbols converted to meta variants
     */
    public String convertToMeta(String line)
    {
        String convertedline = line.replace("+", plusMeta);
        convertedline = convertedline.replace("-", hyphenMeta);
        return convertedline;
    }
    
    /**
     * Convert different versions of spelling strings such as 'co expression', 'co-expression' and 'coexpression'.
     * This is done by directly altering the description string of each entry in the descriptionslist.
     * 
     * @param dl the original descriptions
     */
    public void unifyLanguage(DescriptionsList dl)
    {
    	Map<String, String> conversions = new HashMap<String, String>();
    	for (Description desc : dl.getDescriptions())
    	{
    		String line = desc.getDescription();
	    	String convertedline = line;
	        int hyphenIndex = convertedline.indexOf(hyphenMeta);
	        while (hyphenIndex >= 0)
	        {
	        	int previousSpace = Math.max(convertedline.lastIndexOf(" ", hyphenIndex), 0);
	        	
	        	int nextSpace = convertedline.indexOf(" ", hyphenIndex+1);
	        	if (nextSpace < 0)
	        	{
	        		nextSpace = convertedline.length();
	        	}
	        	
	        	String hyphenedWord = convertedline.substring(previousSpace, nextSpace).trim();
	        	
	        	// we don't want hyphens in the beginning or end of a word ...
	        	while (hyphenedWord.startsWith(hyphenMeta))
	        	{
	        		hyphenedWord = hyphenedWord.substring(hyphenMeta.length()).trim();
	        	}
	        	while (hyphenedWord.endsWith(hyphenMeta))
	        	{
	        		hyphenedWord = hyphenedWord.substring(0, hyphenedWord.length() - hyphenMeta.length()).trim();
	        	}
	        	
	        	// after trailing hyphens are gone, if we still have some in the middle, we will introduce them also in other strings for uniformity
	        	if (hyphenedWord.contains(hyphenMeta))
	        	{
	        		String spacedWord = hyphenedWord.replace(hyphenMeta, " ");
	        		String concatWord = hyphenedWord.replace(hyphenMeta, "");
	        		
	        		conversions.put(spacedWord, hyphenedWord);
	        		conversions.put(concatWord, hyphenedWord);
	        	}
	        	
	        	hyphenIndex = convertedline.indexOf('-', hyphenIndex+1);
	        }
    	}
    	for (Description desc : dl.getDescriptions())
    	{
    		String line = desc.getDescription();
    		for (String c : conversions.keySet())
        	{
        		line = line.replace(c, conversions.get(c));
        	}
    		desc.setDescription(line);
    	}
    }

    /**
     * Remove stop words and meta information such as 'predicted:' and 'RecName' from the input string.
     * These words are considered unnecessary for determining a consensus functional description.
     * 
     * @param line the original line
     * @return a copied version of the line, but with uninformative information removed
     */
    public String removeStopWords(String line)
    {
        String convertedline = line;
        boolean go = true;
        while (go)
        {
            go = false;
            for (String startStopword : getStartStopWords(true))
            {
                if (convertedline.toLowerCase().startsWith(startStopword))
                {
                    int length = startStopword.length();
                    convertedline = convertedline.substring(length);
                    go = true;      // something changed : check all words again!
                }
            }
            if (convertedline.contains("RecName"))
            {
                int shortindex = convertedline.indexOf("Short=");
                if (shortindex < 0)
                {
                    shortindex = convertedline.length();
                }
                int altindex = convertedline.indexOf("AltName:");
                if (altindex < 0)
                {
                    altindex = convertedline.length();
                }
                int flagsindex = convertedline.indexOf("Flags:");
                if (flagsindex < 0)
                {
                    flagsindex = convertedline.length();
                }

                int firstindex = Math.min(shortindex, altindex);
                firstindex = Math.min(firstindex, flagsindex);

                if (firstindex > 0 && firstindex < convertedline.length())
                {
                    convertedline = convertedline.substring(0, firstindex);
                }
            }
            for (String removeword : getRemoveWords(false))
                {
                    if (convertedline.contains(removeword))
                    {
                        go = true;
                        convertedline = convertedline.replace(removeword, "").trim();
                    }
                }
        }
        return convertedline.trim();
    }

    /**
     * Return the list of possible stop words at the beginning of a line. 
     * If the toLowerCase parameter is true, all words will be returned in lower-case form.
     */
    private Set<String> getStartStopWords(boolean toLowerCase)
    {
        Set<String> words = new HashSet<String>();
        words.add("predicted:");
        if (toLowerCase)
        {
        	return toLowerCase(words);
        }
        return words;
    }

    /**
     * Return the list of possible stop words at the end of a line.
     * If the toLowerCase is true, all words will be returned in lower-case form.
     */
    private Set<String> getRemoveWords(boolean toLowerCase)
    {
        Set<String> words = new HashSet<String>();
        words.add("RecName:");
        words.add("Full=");
        words.add("Short=");
        words.add("AltName:");
        words.add("Full=");
        words.add("(ISS)");
        if (toLowerCase)
        {
        	return toLowerCase(words);
        }
        return words;
    }
    
    /**
     * Transform a set of strings with mixed casing to all lower-case strings.
     */
    private Set<String> toLowerCase(Set<String> originals)
    {
    	Set<String> converted = new HashSet<String>();
    	for (String s : originals)
    	{
    		converted.add(s.toLowerCase());
    	}
    	return converted;
    }

}