package be.svlandeg.annomine.io;

/**
 * Class that can clean the output descriptions by removing open-ended parts in braces, strange punctuation bits, etc.
 * 
 * @author Sofie Van Landeghem
 */
public class Postprocess
{

	/**
	 * Remove parts of the string which contain unmatched brace, such as a beginning part with a closing brace, or an ending part with an open brace.
	 * This is done for the pairs () and [] and {}
	 * 
	 * @param line the original input line
	 * @return a copied version of the input, but with open-ended parts removed
	 */
	public String removeUnmatchedBraces(String line)
	{
		String result = new Postprocess().removeOpenBraces(line);
		String result2 = new Postprocess().removeCloseBraces(result);
		return result2;
	}
	
	/**
	 * Remove parts at the end of the string with an open brace, { or [ or (
	 * This is done iteratively, until no more open-ended parts are found.
	 * 
	 * @param line the original input line
	 * @return a copied version of the input, but with open-ended parts removed
	 */
	protected String removeOpenBraces(String line)
	{
		boolean tocheck = true;
		String convertedline = line;
		while (tocheck)
		{
			String statue = convertedline;
			tocheck = false;
			convertedline = removeOpenBraces(convertedline, "(", ")");
			convertedline = removeOpenBraces(convertedline, "{", "}");
			convertedline = removeOpenBraces(convertedline, "[", "]");
			if (!statue.equals(convertedline)) // something changed --> do the loop again
			{
				tocheck = true;
			}
		}
		return convertedline;
	}

	/**
	 * Remove the last part of the line where an open brace occurs, if there is NO corresponding closing brace after it.
	 * 
	 * @param line the original input line
	 * @param openbrace the character representing an open brace, e.g. [
	 * @param closebrace the character representing a corresponding closed brace, e.g. ]
	 * @return a copied version of the input, but with the open-ended part removed
	 */
	private String removeOpenBraces(String line, String openbrace, String closebrace)
	{
		String convertedline = line;
		int openindex = convertedline.lastIndexOf(openbrace);
		int closeindex = convertedline.lastIndexOf(closebrace);
		if (openindex >= 0 && openindex > closeindex)
		{
			// remove the last bits, after (and including) the unmatched opening brace
			convertedline = convertedline.substring(0, openindex).trim();
		}
		return convertedline;
	}

	/**
	 * Remove parts at the beginning of the string with an unmatched closing brace, } or ] or )
	 * This is done iteratively, until no more unmatched parts are found.
	 * 
	 * @param line the original input line
	 * @return a copied version of the input, but with the open-ended parts removed
	 */
	protected String removeCloseBraces(String line)
	{
		boolean tocheck = true;
		String convertedline = line;
		while (tocheck)
		{
			String statue = convertedline;
			tocheck = false;
			convertedline = removeCloseBraces(convertedline, "(", ")");
			convertedline = removeCloseBraces(convertedline, "{", "}");
			convertedline = removeCloseBraces(convertedline, "[", "]");
			if (!statue.equals(convertedline)) // something changed --> do the loop again
			{
				tocheck = true;
			}
		}
		return convertedline;
	}

	/**
	 * Remove the first part of the line before a closed brace, if there is NO corresponding opening brace before it.
	 * 
	 * @param line the original input line
	 * @param openbrace the character representing an open brace, e.g. [
	 * @param closebrace the character representing a corresponding closed brace, e.g. ]
	 * @return a copied version of the input, but with the open-ended part removed
	 */
	private String removeCloseBraces(String line, String openbrace, String closebrace)
	{
		String convertedline = line;
		int openindex = convertedline.indexOf(openbrace);
		if (openindex < 0)	
		{
			// there is no open brace: put artificially at the end of the string
			openindex = line.length();
		}
		int closeindex = convertedline.indexOf(closebrace);
		if (closeindex >= 0 && closeindex < openindex)
		{
			// remove the first bits, before (and including) the unmatched closing brace
			convertedline = convertedline.substring(closeindex + 1).trim();
		}
		return convertedline;
	}

	/**
	 * Remove starting and ending punctuation.
	 * This is done iteratively, until no more punctuation is at the start or end of the line.
	 * 
	 * @param line the original input line
	 * @return a copied version of the input, but with starting/trailing punctuation removed
	 */
	public String removePunctuation(String line)
	{
		boolean tocheck = true;
		String convertedline = line;
		while (tocheck)
		{
			tocheck = false;
			String statue = convertedline;
			convertedline = removeStartingPunctuation(convertedline);
			convertedline = removeEndingPunctuation(convertedline);
			if (!statue.equals(convertedline)) // something changed --> do the loop again
			{
				tocheck = true;
			}
		}
		return convertedline;
	}

	/**
	 * Remove punctuation at the start of the line: , or ; or . or : or -
	 */
	private String removeStartingPunctuation(String line)
	{
		if (line.startsWith(",") || line.startsWith(";") || line.startsWith(".") || line.startsWith(":") || line.startsWith("-"))
		{
			return line.substring(1).trim();
		}
		return line;
	}

	/**
	 * Remove punctuation at the end of the line: , or ; or . or : or -
	 */
	private String removeEndingPunctuation(String line)
	{
		if (line.endsWith(",") || line.endsWith(";") || line.endsWith(".") || line.endsWith(":") || line.endsWith("-"))
		{
			return line.substring(0, line.length() - 1).trim();
		}
		return line;
	}

	/**
	 * Convert strange "meta" characters to their originales, such as "PLUS" to +
	 * See also {@link Preprocess#convertToMeta(String)}
	 * 
	 * @param line the original input line
	 * @return a copied version of the input, but with the meta characters reverted back to their original forms
	 */
	public String convertFromMeta(String line)
	{
		String convertedline = line.replace(Preprocess.plusMeta, "+");
		convertedline = convertedline.replace(Preprocess.hyphenMeta, "-");
		return convertedline;
	}

}
