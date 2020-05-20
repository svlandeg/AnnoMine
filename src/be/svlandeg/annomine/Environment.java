package be.svlandeg.annomine;


/**
 * This class contains a few static parameters that can be adjusted before compilation and distribution of the source code, such as the contact help email address and the way to print newline characters.
 * 
 * @author Sofie Van Landeghem
 */
public class Environment
{
	
	/**
	 * Return the email address for help questions.
	 * @return the contact email address of the AnnoMine developers
	 */
	public static String getHelpEmail()
	{
		return "This tool is not supported anymore";
	}
	
	/**
	 * Return the way a newline should be printed *within* a system.out.println statement (already contains a newline normally!)
	 * @return the newline character, e.g. an html break for online usage of this tool
	 */
	public static String getNewline()
	{
		return "";				// normal usage - newlines are usually included in the system.out.println statement (TODO: revise this?)
		//return " <br />";			// online usage
	}
	
	/**
	 * Return whether or not to print extensive logs
	 * @return whether or not to print extensive logs
	 */
	public static boolean getPrintLog()
	{
		return true;
	}
}
