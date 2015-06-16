package pt.ipleiria.estg.es2.byinvitationonly.Models;

public class Conference {
    private String abbreviation;
    private String fullName;
    private String location;
    private String dates;
    private String logoURL;
    private String website;
    private String callForPapers;
    private float myRating;
    private String firebaseConferenceNode;


    // Constructor for file
    public Conference(String abbreviation, String fullName, String location, String dates,
                      String logoURL, String website, String callForPapers) {
        this.abbreviation = abbreviation;
        this.fullName = fullName;
        this.location = location;
        this.dates = dates;
        this.logoURL = logoURL;
        this.website = website;
        this.callForPapers = callForPapers;
        this.myRating = 0;
        this.firebaseConferenceNode = null;
    }

    // Constructor for Firebase
    public Conference(String abbreviation, String fullName, String location, String dates,
                      String logoURL, String website, String callForPapers, float myRating, String firebaseConferenceNode) {
        this.abbreviation = abbreviation;
        this.fullName = fullName;
        this.location = location;
        this.dates = dates;
        this.logoURL = logoURL;
        this.website = website;
        this.callForPapers = callForPapers;
        this.myRating = myRating;
        if (firebaseConferenceNode.equals("null"))
            this.firebaseConferenceNode = null;
        else
            this.firebaseConferenceNode = firebaseConferenceNode;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLocation() {
        return location;
    }

    public String getDates() {
        return dates;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public String getWebsite() {
        return website;
    }

    public String getCallForPapers() {
        return callForPapers;
    }

    public float getMyRating() {
        return myRating;
    }

    public void setMyRating(float myRating) {
        this.myRating = myRating;
    }

    public String getFirebaseConferenceNode() {
        return firebaseConferenceNode;
    }
}
