package uz.radar.wiut.radar.models;

import java.io.Serializable;

public class LocationObject implements Serializable {
    private int id;
    private String name;
    private double lattitude;
    private double longitude;

    public LocationObject(int id, String name, double lattitude, double longitude) {
        this.id = id;
        this.name = name;
        this.lattitude = lattitude;
        this.longitude = longitude;
    }

    public LocationObject() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
