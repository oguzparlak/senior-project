package model;

import java.io.Serializable;

/**
 * Created by Oguz on 14/04/2017.
 */

public class Review implements Serializable {

    private String author;
    private double rating;
    private String timeDescription;
    private String text;
    private String source;

    public Review(String source, String author, double rating, String timeDescription, String text) {
        this.source = source;
        this.author = author;
        this.rating = rating;
        this.timeDescription = timeDescription;
        this.text = text;
    }

    public Review() {

    }

    public String getSource() {
        return source;
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
