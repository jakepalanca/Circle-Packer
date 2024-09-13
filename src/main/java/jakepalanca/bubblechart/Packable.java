package jakepalanca.bubblechart;

import java.util.UUID;

/**
 * Interface representing an object that can be used in the circle packing algorithm.
 */
public interface Packable {

    /**
     * Gets the unique identifier of the packable object.
     *
     * @return the UUID
     */
    UUID getId();

    /**
     * Gets the radius ratio of the circle.
     *
     * @return the radius ratio
     */
    double getRadiusRatio();

    /**
     * Sets the radius ratio of the circle.
     *
     * @param radiusRatio the new radius ratio
     */
    void setRadiusRatio(double radiusRatio);

    /**
     * Gets the actual radius of the circle after optimization.
     *
     * @return the radius
     */
    double getRadius();

    /**
     * Sets the actual radius of the circle after optimization.
     *
     * @param radius the new radius
     */
    void setRadius(double radius);

    /**
     * Gets the x-coordinate of the circle's center.
     *
     * @return the x-coordinate
     */
    double getX();

    /**
     * Sets the x-coordinate of the circle's center.
     *
     * @param x the new x-coordinate
     */
    void setX(double x);

    /**
     * Gets the y-coordinate of the circle's center.
     *
     * @return the y-coordinate
     */
    double getY();

    /**
     * Sets the y-coordinate of the circle's center.
     *
     * @param y the new y-coordinate
     */
    void setY(double y);
}
