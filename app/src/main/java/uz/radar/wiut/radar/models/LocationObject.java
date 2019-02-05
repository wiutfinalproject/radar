package uz.radar.wiut.radar.models;

import java.io.Serializable;

public class LocationObject implements Serializable {
    private int id;
    private String name;
    private double lattitude;
    private double longitude;
    private boolean isNear = false;

    public LocationObject(int id, String name, double lattitude, double longitude, boolean isNear) {
        this.id = id;
        this.name = name;
        this.lattitude = lattitude;
        this.longitude = longitude;
        this.isNear = isNear;
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

    public boolean isNear() {
        return isNear;
    }

    public void setNear(boolean near) {
        isNear = near;
    }
}
