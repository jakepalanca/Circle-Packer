package jakepalanca.bubblechart;

import java.util.UUID;

public class Bubble {
    private double radius;
    private double radiusRatio;  // Ratio that defines the size relative to others
    private double x;
    private double y;
    private final String id;

    public Bubble(double radius, double radiusRatio) {
        this.radius = radius;
        this.radiusRatio = radiusRatio;
        this.id = UUID.randomUUID().toString();  // Assign unique ID
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double newRadius) {
        this.radius = newRadius;
    }

    public double getRadiusRatio() {
        return radiusRatio;
    }

    public void setRadiusRatio(double radiusRatio) {
        this.radiusRatio = radiusRatio;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getId() {
        return id;
    }
}
