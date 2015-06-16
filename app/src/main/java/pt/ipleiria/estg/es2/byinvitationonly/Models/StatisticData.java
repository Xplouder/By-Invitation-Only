package pt.ipleiria.estg.es2.byinvitationonly.Models;


public class StatisticData {

    private String title;
    private String numRatings;
    private float averageRating;

    public StatisticData(String title, String numRatings, float averageRating) {
        this.title = title;
        this.numRatings = numRatings;
        this.averageRating = averageRating;
    }

    public String getTitle() {
        return title;
    }

    public String getNumRatings() {
        return numRatings;
    }

    public float getAverageRating() {
        return averageRating;
    }
}
