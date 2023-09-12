package scheduler;

/**
 * Custom class made to store the results for appointment total reports to easily display them onto the tables.
 */
public class ReportResult {
    private final String sort;
    private final Integer total;

    public ReportResult(String sort, int total){
        this.sort = sort;
        this.total = total;
    }

    public Integer getTotal() {
        return total;
    }

    public String getSort() {
        return sort;
    }
}
