package be.svlandeg.annomine.data;

/**
 * Class that represents one functional description, possible with a weight assigned to it.
 * @author Sofie Van Landeghem
 */
public class Description
{
    
    protected String description;
    protected String query;
    protected double weight;

    /**
     * Constructor with default value for the query (null).
     * 
     * @param description the functional description itself
     * @param weight the weight or score of this description - usually this refers to the sequence-similarity of the query gene
     */
    public Description(String description, double weight)
    {
        this(description, weight, null);
    }

    /**
     * Constructor, specifying the description for a certain query gene, associated with a certain score.
     * 
     * @param description the functional description itself (this object can not be changed later on)
     * @param weight the weight or score of this description - usually this refers to the sequence-similarity of the query gene
     * @param query the quere gene
     */
    public Description(String description, double weight, String query)
    {
        this.description = description;
        this.weight = weight;
        this.query = query;
    }
    
    /**
     * Return the functional description.
     * @return the functional description
     */
    public String getDescription()
    {
        return description;
    }
    
    /**
     * Return the weight or score of this description.
     * @return the weight (score)
     */
    public double getWeight()
    {
        return weight;
    }

    /**
     * Return the query gene.
     * @return the query gene (can be null when unspecified)
     */
    public String getQuery()
    {
        return query;
    }

    /**
     * Set the query gene related to this description.
     * @param query the query gene
     */
    public void setQuery(String query)
    {
        this.query = query;
    }

    /**
     * Set the weight of this description.
     * @param weight the weight or score of this description - usually this refers to the sequence-similarity of the query gene
     */
    public void setWeight(double weight)
    {
        this.weight = weight;
    }
    
    /**
     * Set the actual functional annotation of this description.
     * @param description the functional annotation 
     */
    public void setDescription(String description)
    {
        this.description = description;
    }


}
