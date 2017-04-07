package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Oguz on 21/02/2017.
 */

public class Restaurant {

    private String uid;
    private String name;
    private String address;
    private List<String> cuisines;
    private List<String> photos;
    private String phoneNumber;
    private String rating;
    private String reviewCount;
    private Map<String, String> specs;
    private Map<String, Boolean> stars = new HashMap<>();


    public Restaurant(String name, String address, List<String> cuisines, List<String> photos, String phoneNumber, String rating, String reviewCount, Map<String, String> specs) {
        this.name = name;
        this.address = address;
        this.cuisines = cuisines;
        this.photos = photos;
        this.phoneNumber = phoneNumber;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.specs = specs;
    }

    public Restaurant() {
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, Boolean> getStars() {
        return stars;
    }

    public String getAddress() {
        return address;
    }

    public List<String> getCuisines() {
        return cuisines;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRating() {
        return rating;
    }

    public String getReviewCount() {
        return reviewCount;
    }

    public Map<String, String> getSpecs() {
        return specs;
    }
}
