package be.svlandeg.annomine.data;

import java.util.HashSet;
import java.util.Set;

/**
 * A collection of description batches forms a dataset.
 * @author Sofie Van Landeghem
 */
public class Dataset
{

    /** The internal list of DescriptionList objects */
    protected Set<DescriptionsList> lists;

    /**
     * Create a new dataset using a particular set of batches.
     * @param lists the description batches which make up this dataset
     */
    public Dataset(Set<DescriptionsList> lists)
    {
        this.lists = lists;
    }

    /**
     * Create a new dataset.
     * An empty set of description batches is created internally.
     */
    public Dataset()
    {
        this(new HashSet<DescriptionsList>());
    }

    /**
     * Add a descriptions batch to the internal set of batches
     * @param list a new description batch which will be added to this dataset
     */
    public void addList(DescriptionsList list)
    {
        lists.add(list);
    }

    /**
     * Return the collection of description batches
     * @return all description batches in this dataset
     */
    public Set<DescriptionsList> getLists()
    {
        return lists;
    }

    @Override
    public String toString()
    {
        return "Dataset of " + lists.size() + " lists";
    }

}
