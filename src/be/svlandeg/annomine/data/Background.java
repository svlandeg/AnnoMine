package be.svlandeg.annomine.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Class to create a suitable background .lst file by sampling clean descriptions from NR Prot.
 * @author Sofie Van Landeghem
 */
public class Background
{
    /**
     * Main method that creates a sample of the background file (TODO: currently hard coded file locations)
     * @param args (unused) input parameters
     * @throws java.lang.Exception when the hard-coded location can not be found
     */
    public static void main(String[] args) throws Exception
    {
        File origfile = new File("G:/users/solan/fa/data_klaas_1juli/nrprot_headers_clean.txt");

        //int rows = 30;
        //File sampledfile = new File("G:/users/solan/fa/train_klaas/random_sample_train_" + rows + ".txt");
        //new Background().sampleSequence(origfile, sampledfile, rows);

        double fraction = 1;
        File randomsampledfile = new File("G:/users/solan/fa/train_klaas/random_sample_train_" + fraction + ".txt");
        new Background().sampleRandom(origfile, randomsampledfile, 1/fraction);
    }

    /**
     * Create a sample of the background file by simply copying the first X rows
     * 
     * @param origfile the original file
     * @param sampledfile the output file
     * @param rows the number of rows the new output file should contain
     * @throws java.lang.Exception when an IO error occurs during reading or writing
     */
    public void sampleSequence(File origfile, File sampledfile, int rows) throws Exception
    {
        BufferedReader reader = new BufferedReader(new FileReader(origfile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(sampledfile));
        String line = reader.readLine();
        int count = 0;
        while (line != null && count < rows)
        {
            String cleanline = cleanLine(line);
            writer.write(cleanline);
            writer.newLine();
            writer.flush();
            count++;
            line = reader.readLine();
        }
        reader.close();
        writer.close();
    }

    /**
     * Create a sample of the background model file containing around a certain percentage of the original lines
     * The percentage is implemented by checking a random double against the percentage for each original line.
     * 
     * @param origfile the original file
     * @param sampledfile the output file
     * @param perc the percentage of lines that should be kept from the original input file
     * @throws java.lang.Exception when an IO error occurs during reading or writing
     */
    public void sampleRandom(File origfile, File sampledfile, double perc) throws Exception
    {
        BufferedReader reader = new BufferedReader(new FileReader(origfile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(sampledfile));
        String line = reader.readLine();
        while (line != null)
        {
            if (new Random().nextDouble() <= perc)  // only keep about "perc" amount of all lines
            {
                String cleanline = cleanLine(line);
                writer.write(cleanline);
                writer.newLine();
                writer.flush();
            }
            line = reader.readLine();
        }
        reader.close();
        writer.close();
    }

    /**
     * Clean the line by removing the first few tabs and by removing non-printable characters like control sequences.
     * @param line the original line
     * @return a cleaned version of this line
     */
    private String cleanLine(String line)
    {
        StringTokenizer stok = new StringTokenizer(line, "\t");
        String result = "";
        while (stok.hasMoreTokens())
        {
            result = stok.nextToken();  // only keep last token (after last tab)
        }
        String truncated = result.replaceAll("\\p{Cntrl}", "");     // remove non-printable characters
        return truncated;
    }
}
