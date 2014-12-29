package be.svlandeg.annomine.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import be.svlandeg.annomine.data.Description;

/**
 * Class that reads the manual annotations as well as computationally generated ones, from file.
 * 
 * @author Sofie Van Landeghem
 */
public class AnnotationsReader
{


	/**
	 * Read the manual annotations from file and return them mapped by the query gene.
	 * The manual annotations are not scored/weighted, but there may be more than one per query, in which case they are assumed to be ordered.
	 * 
	 * @param location the location of the manual annotations
	 * @param skipHeader whether or not to skip the first line in the file (if it contains a header line)
	 * 
	 * @return the map of query gene to functional annotation - or multiple (ordered) annotations
	 * @throws IOException when an IO error occurs
	 */
	public Map<String, List<String>> readManualAnnotations(String location, boolean skipHeader) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(new File(location)));
		String line = reader.readLine();

		if (skipHeader)
		{
			line = reader.readLine();
		}

		Map<String, List<String>> results = new HashMap<String, List<String>>();

		while (line != null)
		{
			StringTokenizer stok = new StringTokenizer(line, "\t");
			String protein_id = stok.nextToken();
			String annotation = stok.nextToken();

			if (!results.containsKey(protein_id))
			{
				results.put(protein_id, new ArrayList<String>());
			}
			results.get(protein_id).add(annotation);
			
			/* There may be more manual annotations per line */
			while (stok.hasMoreTokens())
			{
				results.get(protein_id).add(stok.nextToken());
			}
			
			line = reader.readLine();
		}

		reader.close();
		return results;
	}

	/**
	 * Read the computational annotations, produced by AnnoMine, from file and return them mapped by the query gene
	 * 
	 * @param location the file location
	 * @return the map of query gene to functional annotation - or multiple (ordered) annotations
	 * @throws IOException when an IO error occurs
	 */
	public Map<String, Set<Description>> readComputationalAnnotations(String location) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(new File(location)));
		String line = reader.readLine();

		Map<String, Set<Description>> results = new HashMap<String, Set<Description>>();

		while (line != null)
		{
			StringTokenizer stok = new StringTokenizer(line, "\t");
			String protein_id = stok.nextToken();
			Double score = Double.parseDouble(stok.nextToken());
			String annotation = stok.nextToken();

			Description d = new Description(annotation, score, protein_id);

			if (!results.containsKey(protein_id))
			{
				results.put(protein_id, new HashSet<Description>());
			}
			results.get(protein_id).add(d);
			line = reader.readLine();
		}

		reader.close();
		return results;
	}

}
