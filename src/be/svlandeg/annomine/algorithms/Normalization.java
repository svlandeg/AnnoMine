package be.svlandeg.annomine.algorithms;

import be.svlandeg.annomine.data.Description;
import be.svlandeg.annomine.data.DescriptionsList;

/**
 * This class is designed to deal with weighting issues: weight cutoff and weight normalization methods.
 *
 * @author Sofie Van Landeghem
 */
public class Normalization
{

	/**
	 * Enforce a weight cutoff on a description list: each weight needs to be at least above a percentage (e.g. 80%) of the maximum weight in the list. 
	 * 
	 * @param dl the original description list
	 * @param perc the cutoff percentage
	 * @return a copy of the original list, keeping only the top best scored descriptions
	 */
    public DescriptionsList enforceWeightCutoff(DescriptionsList dl, int perc)
    {
        double maxweight = getMaxWeight(dl);
        double allowed = maxweight * perc / 100;
        DescriptionsList dl2 = new DescriptionsList(dl.getName());
        for (Description d : dl.getDescriptions())
        {
            double thisweight = d.getWeight();
            if (thisweight >= allowed)
            {
                dl2.addDescription(d);
            }
        }
        System.out.println("   weight cutoff (" + perc + "%): " + dl.
                getDescriptions().size() + " --> " + dl2.getDescriptions().
                size());
        return dl2;
    }

    /**
     * Renormalize all weights in a description list. 
	 * 
     * @param dl the original description list
     * @param printLog whether or not to print the weight normalization result to the standard output
     * @return a copy of the original list, but with all descriptions normalized
     */
    public DescriptionsList linearNormalization(DescriptionsList dl, boolean printLog)
    {
        int totalweight = getTotalWeight(dl);
        int nr = dl.getDescriptions().size();
        DescriptionsList dl2 = new DescriptionsList(dl.getName());
        double factor = (double) nr / totalweight;
        for (Description d : dl.getDescriptions())
        {
            double thisweight = d.getWeight();
            double newweight = Math.round((long) (thisweight * factor));
            if (newweight > 0)
            {
                d.setWeight(newweight);
                dl2.addDescription(d);
            }
        }
        if (printLog)
        {
            System.out.println("   weights normalized:  " + dl.getDescriptions().size() + " --> " + dl2.getDescriptions().size());
        }
        return dl2;
    }

    /**
     * Retrieve the maximum weight occurring in a description list.
     */
    private double getMaxWeight(DescriptionsList dl)
    {
        double maxweight = 0;
        for (Description d : dl.getDescriptions())
        {
            maxweight = Math.max(maxweight, d.getWeight());
        }
        return maxweight;
    }

    /**
     * Retrieve the total weight scores (summed) in the description list.
     */
    private int getTotalWeight(DescriptionsList dl)
    {
        int totalweight = 0;
        for (Description d : dl.getDescriptions())
        {
            totalweight += d.getWeight();
        }
        return totalweight;
    }
}
