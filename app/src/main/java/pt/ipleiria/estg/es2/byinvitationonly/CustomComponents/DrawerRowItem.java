package pt.ipleiria.estg.es2.byinvitationonly.CustomComponents;


public class DrawerRowItem {
    private String title;
    private int imageId;


    public DrawerRowItem(String title, int imageId) {
        this.title = title;
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageId() {
        return imageId;
    }
}
