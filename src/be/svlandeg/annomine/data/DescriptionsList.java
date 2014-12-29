package be.svlandeg.annomine.data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents one named batch of functional descriptions.
 * @author Sofie Van Landeghem
 */
public class DescriptionsList
{

    /** A list of descriptions, usually of homologues genes */
    protected List<Description> descriptions;
    
    /** The name of this list */
    protected String name;

    /**
     * Provide a name and a list of descriptions.
     * @param name the name of this batch	
     * @param descriptions the actual descriptions
     */
    public DescriptionsList(String name, List<Description> descriptions)
    {
        this.name = name;
        this.descriptions = descriptions;
    }

    /**
     * Provide a name, an empty list of descriptions is created internally.
     * @param name the name of this batch	
     */
    public DescriptionsList(String name)
    {
        this(name, new ArrayList<Description>());
    }

    /**
     * Add a description to the internal list.
     * @param description one functional description
     */
    public void addDescription(Description description)
    {
        descriptions.add(description);
    }

    /**
     * Return the list of known descriptions.
     * @return the batch of functional descriptions (can be empty, but not null)
     */
    public List<Description> getDescriptions()
    {
        return descriptions;
    }

    /**
     * Return the name of this list. This should not be used as unique identifier!
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return toShortString();
    }

    /**
     * Retrieve a short string to print for this list.
     * Includes the name and the number of descriptions in the batch.
     * 
     * @return a short textual description of this batch
     */
    public String toShortString()
    {
        String result = "Descriptionslist : " + name;
        result += " - " + descriptions.size() + " descriptions in total";
        return result;
    }

    /**
     * Retrieve a long string to print for this list.
     * Includes the name the number of descriptions in the batch.
     * Finally, all descriptions are printed, one on each row.
     * 
     * @return a detailed textual description of this batch
     */
    public String toLongString()
    {
        String result = toShortString();
        for (Description d : descriptions)
        {
            result += " \n " + d;
        }
        return result;
    }
}
