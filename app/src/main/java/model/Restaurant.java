package model;

/**
 * Created by Oguz on 21/02/2017.
 */

public class Restaurant {

    private long zomatoId;
    private String name;
    private String thumb;
    private String address;
    private String locality;
    private String city;
    private int zipCode;
    private double lat;
    private double lon;
    private String cuisine;
    private String currency;
    private int priceRating;
    private double rating;

    public Restaurant() {

    }

    public Restaurant(long zomatoId, String name, String thumb, String address, String locality, String city, int zipCode, double lat, double lon, String cuisine, String currency) {
        this.zomatoId = zomatoId;
        this.name = name;
        this.thumb = thumb;
        this.address = address;
        this.locality = locality;
        this.city = city;
        this.zipCode = zipCode;
        this.lat = lat;
        this.lon = lon;
        this.cuisine = cuisine;
        this.currency = currency;
    }


    public Restaurant(long zomatoId, String name, String thumb, double rating, int priceRating) {
        this.zomatoId = zomatoId;
        this.name = name;
        this.thumb = thumb;
        this.rating = rating;
        this.priceRating = priceRating;
    }

    public int getPriceRating() {
        return priceRating;
    }

    public double getRating() {
        return rating;
    }

    public long getZomatoId() {
        return zomatoId;
    }

    public String getName() {
        return name;
    }

    public String getThumb() {
        return thumb;
    }

    public String getAddress() {
        return address;
    }

    public String getLocality() {
        return locality;
    }

    public String getCity() {
        return city;
    }

    public int getZipCode() {
        return zipCode;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getCuisine() {
        return cuisine;
    }

    public String getCurrency() {
        return currency;
    }
}
