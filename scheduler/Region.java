package scheduler;

/**
 * Region is used to store values from the region table in the Database so they can be worked with and
 * filtered through without having to connect to the database again.
 */
public class Region {

    private final String name;
    private final int divisionID;
    private final int countryID;

    public Region(String name, int divisionID, int countryID){
        this.name = name;
        this.countryID = countryID;
        this.divisionID = divisionID;
    }

    public String getName(){return name;}
    public int getDivisionID(){return divisionID;}
    public int getCountryID(){return countryID;}

    /**
     * Overriding toString so that when filtering through a list by ID numbers it will put the name of the region
     * into the list.
     * @return the name of the Region.
     */
    @Override
    public String toString(){
        return getName();
    }
}
