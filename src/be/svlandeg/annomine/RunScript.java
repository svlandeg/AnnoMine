package be.svlandeg.annomine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import be.svlandeg.annomine.algorithms.Overrepresentation;
import be.svlandeg.annomine.algorithms.TextMapping;

/**
 * Class to run the statistical overrepresentation method.
 *
 * @author Sofie Van Landeghem
 */
public class RunScript
{

    /**
     * Default value for minimum count ngram = 3
     */
    protected int min_count_ngram = 2;
    /**
     * Default value for minimal n-gram = 2
     */
    protected int min_ngram = 2;
    /**
     * Default value for maximal n-gram = 10
     */
    protected int max_ngram = 10;
    /**
     * Default value for output_cutoff = 0
     */
    protected int output_cutoff = 0;
    /**
     * Default value for input cutoff = 0
     */
    protected int input_cutoff = 0;
    /**
     * Default value for lowercase = true --> case insensitive
     */
    protected boolean lowercase = true;
    /**
     * Default value for stemming = true
     */
    protected boolean stemming = true;
    /**
     * Default value for weights = true
     */
    protected boolean weights = true;
    /**
     * Default value for unify_unknowns = true
     */
    protected boolean unify_unknowns = true;
    /**
     * Default value for normalization = true
     */
    protected boolean normalization = true;
    protected int col_score = -1;
    protected int col_evalue = -1;
    protected int printnr = 1;

    /**
     * Strings should be of format key=value. 
     * Mandatory keys: testdir outputdir
     * Optional keys: traindir/backgrounddir minngram maxngram output_cutoff lowercase mincountngram
     * backgrounddir and traindir refer to the same thing, the term 'traindir' is supported for backwards compatibility
     *
     * @param args the arguments which get parsed to determine the options
     */
    public static void main(String[] args)
    {
    	String newline = Environment.getNewline(); 
        
        if (args.length < 3 || args[0].equalsIgnoreCase("help"))
        {
            new RunScript().printMandatoryString();
            System.out.println(newline);
            new RunScript().printOptionalString();
            return;
        }
        try
        {
            Map<String, String> map = RunScript.parse(args);
            new RunScript().run(map);
        }
        catch (Exception e)
        {
            System.out.println("An error occurred from which the program could not recover : " + newline);
            e.printStackTrace();
            System.out.println(newline);
            System.out.println("This is probably due to misconfiguration of the program." + newline);
            System.out.println("Contact solan AT psb DOT ugent DOT be for help with this issue, quoting the above error message." + newline);
        }
    }

    /**
     * Hard-coded main
     * @param args (unused) arguments
     */
    @SuppressWarnings("unused")
	public static void main_thing(String[] args)
    {
        Map<String, String> map = new HashMap<String, String>();
        
        //map.put("trainfile", "G:/users/solan/fa/training_regular/random_sample_train_100.txt");
        //map.put("testfile", "G:/users/solan/fa/input/toto.txt");
        //map.put("outputfile", "G:/users/solan/fa/output/afkaptest.txt");
        
        map.put("trainfile", "/group/biocomp/users/solan/fa/training_regular/random_sample_train_100.txt");
        //map.put("backgroundfile", "/group/biocomp/users/solan/fa/training_regular/random_sample_train_100.txt");
        map.put("testfile", "/group/biocomp/users/solan/fa/input/toto.txt");
        map.put("outputfile", "/group/biocomp/users/solan/fa/output/afkaptest.txt");
        map.put("col_desc", "12");
        map.put("col_query", "0");
        map.put("col_evalue", "10");
        map.put("printnr", "3");
        map.put("outputcutoff", "160000");
        //map.put("inputperc", "90");
        //map.put("normalization", "false");
        //map.put("minngram", "2");
        new RunScript().run(map);
    }

    /**
     * Hard-coded main
     * @param args (unused) arguments
     */
    @SuppressWarnings("unused")
    public static void main_testyalin(String[] args)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("trainfile", "G:/users/solan/fa/training_small/random_sample_train_1000.txt");
        //map.put("backgroundfile", "G:/users/solan/fa/training_small/random_sample_train_1000.txt");
        map.put("testfile", "G:/users/solan/fa/input/yalin.tab");
        map.put("outputfile", "G:/users/solan/fa/output/outputya.txt");
        map.put("col_desc", "12");
        map.put("col_query", "0");
        map.put("col_evalue", "10");
        map.put("printnr", "1");
        new RunScript().run(map);
    }

    /**
     * Hard-coded main
     * @param args (unused) arguments
     */
    @SuppressWarnings("unused")
    public static void main0(String[] args)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("traindir", "/group/biocomp/users/solan/fa/training_small/");
        //map.put("backgrounddir", "/group/biocomp/users/solan/fa/training_small/");
        map.put("testdir", "/group/biocomp/users/solan/fa/testing/");
        map.put("outputdir", "/group/biocomp/users/solan/fa/output/");
        new RunScript().run(map);
    }
    
    /**
	 * Print the parameters of the method. These are parameters set in the constructor ("adjustable") and the default, static ones.
	 */
	public void printParameters()
	{
		String newline = Environment.getNewline(); 
		System.out.println(newline);
		System.out.println("Run parameters:" + newline);
		System.out.print("  lowercase=" + lowercase + newline);
		System.out.print("  stemming=" + stemming + newline);
		System.out.print("  normalization=" + normalization + newline);
		System.out.print("  min_ngram=" + min_ngram + newline);
		System.out.print("  max_ngram=" + max_ngram + newline);
		System.out.print("  min_count_ngram=" + min_count_ngram + newline);
		System.out.println(newline);

		//System.out.println("Default parameters:" + newline);
		//System.out.print("  max_returned_results=" + Overrepresentation.max_returned_results + newline);
		//System.out.print("  mincountocc=" + min_count_occ + newline);
		//System.out.println(newline);
	}

    /**
     * Run with the parameters parsed. Return null and print a message when the mandatory arguments are not included. All parameters are printed after initialization.
     *
     * @param map the map of settings parameters
     */
    public void run(Map<String, String> map)
    {
    	Boolean printLog = Environment.getPrintLog();
    	String newline = Environment.getNewline(); 
        setParameters(map);
        Overrepresentation op = new Overrepresentation(min_count_ngram, min_ngram, max_ngram, lowercase);
        if (printLog)
        {
            printParameters();
        }
        TextMapping tm = new TextMapping(lowercase, stemming, unify_unknowns);

        if (map.containsKey("testdir"))
        {
            if (!map.containsKey("outputdir"))
            {
                printMandatoryString();
                return;
            }
            File backgrounddir = null;
            if (map.containsKey("traindir"))
            {
            	backgrounddir = new File(map.get("traindir"));
            }
            if (map.containsKey("backgrounddir"))
            {
            	backgrounddir = new File(map.get("backgrounddir"));
            }
            File testdir = new File(map.get("testdir"));

            String outputdir = map.get("outputdir");

            new Run(op, tm).runFromDirectories(backgrounddir, testdir, outputdir, weights, input_cutoff, normalization, printnr, output_cutoff, newline, printLog);
        }
        else if (map.containsKey("testfile"))
        {
            File outputfile = null;

            if (!map.containsKey("outputfile"))
            {
                if (printLog)
                {
                    System.out.println("Printing output to screen" + newline);
                }
            }
            else
            {
                outputfile = new File(map.get("outputfile"));
            }
            File backgroundfile = null;
            if (map.containsKey("trainfile"))
            {
            	backgroundfile = new File(map.get("trainfile"));
            }
            if (map.containsKey("backgroundfile"))
            {
            	backgroundfile = new File(map.get("backgroundfile"));
            }
            File testfile = new File(map.get("testfile"));

            if (!map.containsKey("col_desc") || !map.containsKey("col_query"))
            {
                printMandatoryString();
                return;
            }
            int col_desc = Integer.parseInt(map.get("col_desc"));
            int col_query = Integer.parseInt(map.get("col_query"));

            if (map.containsKey("col_weights") || (col_evalue >= 0 && col_score >= 0))
            {
                printWeightString();
                return;
            }

            if (col_desc >= 0 && col_query >= 0 && col_desc != col_query)
            {
                new Run(op, tm).runFromFiles(backgroundfile, testfile, outputfile, col_evalue, col_score, col_desc, col_query, input_cutoff, normalization, printnr, output_cutoff, newline, printLog);
            }
            else
            {
                printMandatoryString();
                return;
            }
        }
        else
        {
            printMandatoryString();
            return;
        }

    }

    /**
     * Set the fields of this class using the (key,value) mapping
     */
    private void setParameters(Map<String, String> map)
    {
        if (map.containsKey("col_score"))
        {
            String newcol_weights = map.get("col_score");
            Integer newcol_weightsint = null;
            try
            {
                newcol_weightsint = Integer.parseInt(newcol_weights);
            }
            catch (Exception e)
            {
                newcol_weightsint = null;
            }
            if (newcol_weightsint != null && newcol_weightsint > 0)
            {
                col_score = newcol_weightsint;
            }
        }

        if (map.containsKey("col_evalue"))
        {
            String newcol_weights = map.get("col_evalue");
            Integer newcol_weightsint = null;
            try
            {
                newcol_weightsint = Integer.parseInt(newcol_weights);
            }
            catch (Exception e)
            {
                newcol_weightsint = null;
            }
            if (newcol_weightsint != null && newcol_weightsint > 0)
            {
                col_evalue = newcol_weightsint;
            }
        }

        if (map.containsKey("printnr"))
        {
            String newcol_weights = map.get("printnr");
            Integer newcol_weightsint = null;
            try
            {
                newcol_weightsint = Integer.parseInt(newcol_weights);
            }
            catch (Exception e)
            {
                newcol_weightsint = null;
            }
            if (newcol_weightsint != null && newcol_weightsint > 0)
            {
                printnr = newcol_weightsint;
            }
        }

        if (map.containsKey("minngram"))
        {
            String newmin = map.get("minngram");
            Integer newminint = null;
            try
            {
                newminint = Integer.parseInt(newmin);
            }
            catch (Exception e)
            {
                newminint = null;
            }
            if (newminint != null && newminint > 0)
            {
            	min_ngram = newminint;
            }
        }

        if (map.containsKey("maxngram"))
        {
            String newmax = map.get("maxngram");
            Integer newmaxint = null;
            try
            {
                newmaxint = Integer.parseInt(newmax);
            }
            catch (Exception e)
            {
                newmaxint = null;
            }
            if (newmaxint != null && newmaxint >= min_ngram)
            {
            	max_ngram = newmaxint;
            }
        }

        if (map.containsKey("outputcutoff"))
        {
            String newcut = map.get("outputcutoff");
            Integer newcutint = null;
            try
            {
                newcutint = Integer.parseInt(newcut);
            }
            catch (Exception e)
            {
                newcutint = null;
            }
            if (newcutint != null && newcutint >= 0)
            {
                output_cutoff = newcutint;
            }
        }

        if (map.containsKey("inputperc"))
        {
            String newcut = map.get("inputperc");
            Integer newcutint = null;
            try
            {
                newcutint = Integer.parseInt(newcut);
            }
            catch (Exception e)
            {
                newcutint = null;
            }
            if (newcutint != null && newcutint >= 0)
            {
                input_cutoff = newcutint;
            }
        }

        if (map.containsKey("lowercase"))
        {
            String newcase = map.get("lowercase");
            Boolean newcaseboolean = null;
            try
            {
                newcaseboolean = Boolean.parseBoolean(newcase);
            }
            catch (Exception e)
            {
                newcaseboolean = null;
            }
            if (newcaseboolean != null)
            {
                lowercase = newcaseboolean;
            }
        }

        if (map.containsKey("stemming"))
        {
            String newcase = map.get("stemming");
            Boolean newcaseboolean = null;
            try
            {
                newcaseboolean = Boolean.parseBoolean(newcase);
            }
            catch (Exception e)
            {
                newcaseboolean = null;
            }
            if (newcaseboolean != null)
            {
                stemming = newcaseboolean;
            }
        }

        if (map.containsKey("weights"))
        {
            String newweights = map.get("weights");
            Boolean newweightsboolean = null;
            try
            {
                newweightsboolean = Boolean.parseBoolean(newweights);
            }
            catch (Exception e)
            {
                newweightsboolean = null;
            }
            if (newweightsboolean != null)
            {
                weights = newweightsboolean;
            }
        }

        if (map.containsKey("normalization"))
        {
            String newnormalization = map.get("normalization");
            Boolean newnormalizationboolean = null;
            try
            {
                newnormalizationboolean = Boolean.parseBoolean(newnormalization);
            }
            catch (Exception e)
            {
                newnormalizationboolean = null;
            }
            if (newnormalizationboolean != null)
            {
                normalization = newnormalizationboolean;
            }
        }

        if (map.containsKey("unify_unknowns"))
        {
            String newnormalization = map.get("unify_unknowns");
            Boolean newunifyboolean = null;
            try
            {
                newunifyboolean = Boolean.parseBoolean(newnormalization);
            }
            catch (Exception e)
            {
            	newunifyboolean = null;
            }
            if (newunifyboolean != null)
            {
                unify_unknowns = newunifyboolean;
            }
        }

        if (map.containsKey("mincountngram"))
        {
            String newmin_count_ngramstring = map.get("mincountngram");
            Integer newmin_count_ngram = null;
            try
            {
                newmin_count_ngram = Integer.parseInt(newmin_count_ngramstring);
            }
            catch (Exception e)
            {
                newmin_count_ngram = null;
            }
            if (newmin_count_ngram != null && newmin_count_ngram > 0)
            {
                min_count_ngram = newmin_count_ngram;
            }
        }
    }

    /**
     * A print out of the mandatory arguments
     */
    private void printMandatoryString()
    {
    	String newline = Environment.getNewline(); 
        System.out.println(newline);
        System.out.println(" > Mandatory arguments:" + newline);
        System.out.println(" > option 1. testdir= outputdir=" + newline);
        System.out.println(" > option 2. testfile= outputfile= col_desc= col_query=" + newline);
    }

    /**
     * A print out of the mandatory arguments
     */
    private void printWeightString()
    {
    	String newline = Environment.getNewline(); 
        System.out.println(newline);
        System.out.println(" > Weight arguments:" + newline);
        System.out.println(" > Either put col_evalue= OR col_score=" + newline);
        System.out.println(" > DON'T use col_weights= !" + newline);
    }

    /**
     * A print out of the optional arguments inputperc
     */
    private void printOptionalString()
    {
    	String newline = Environment.getNewline(); 
        System.out.println(" > Optional arguments:" + newline);
        System.out.println("  > min_ngram (default " + min_ngram + ") and max_ngram (default " + max_ngram + ") determine the sizes of possible n-grams." + newline);
        System.out.println("  > outputcutoff (default " + output_cutoff + ") determines the minimal output cutoff-value for prediction scores." + newline);
        System.out.println("  > inputperc (default none) determines the input percentage cutoff-value for weights." + newline);
        System.out.println("  > printnr (default " + printnr + ") determines the number of results printed (max:25)." + newline);
        System.out.println("  > lowercase (default " + lowercase + ") : set to 'false' if case matters." + newline);
        System.out.println("  > stemming (default " + lowercase + ") : set to 'false' if you don't want stemming." + newline);
        System.out.println("  > normalization (default " + normalization + ") : set to 'false' if the weights should not be normalized." + newline);
        System.out.println("  > unify_unknowns (default " + unify_unknowns + ") : set to 'false' if you don't want hypothetical and unnamed proteins to default to 'conserved unknown protein'." + newline);
        System.out.println("  > option 1. weights (default " + weights + ") : set to 'true' when the test files include weights." + newline);
        //System.out.println("  > mincountngram (default " + min_count_ngram + ") determines the minimal count that an n-gram should appear in the test file." + newline);
        System.out.println("  > option 2. col_evalue= OR col_score= : set to >= 0 to specify the correct column" + newline);
        System.out.println("  > option 1. backgrounddir (default none)" + newline);
        System.out.println("  > option 2. backgroundfile (default none)" + newline);
        System.out.println("  > Insensible values will be discarded and set to their default values." + newline);
    }

    /**
     * Parse the arguments of the main method into a (key,value) map from the format key=value.
     */
    private static Map<String, String> parse(String[] args)
    {
        Map<String, String> map = new HashMap<String, String>();
        for (String parameter : args)
        {
            int limit = parameter.indexOf("=");
            if (limit >= 0)
            {
                String name = parameter.substring(0, limit);
                String value = parameter.substring(limit + 1);
                map.put(name, value);
            }
        }
        return map;
    }
}
