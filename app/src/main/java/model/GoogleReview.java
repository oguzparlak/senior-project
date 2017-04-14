package model;

/**
 * Created by Oguz on 14/04/2017.
 */

public class GoogleReview {

    private String author;
    private double rating;
    private String timeDescription;
    private String text;

    public GoogleReview(String author, double rating, String timeDescription, String text) {
        this.author = author;
        this.rating = rating;
        this.timeDescription = timeDescription;
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public double getRating() {
        return rating;
    }

    public String getTimeDescription() {
        return timeDescription;
    }

    public String getText() {
        return text;
    }
}
