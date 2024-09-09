package jakepalanca.bubblechart;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * The {@code Bubble} class represents a single bubble in the bubble chart simulation.
 * Each bubble has a unique identifier, position (x, y), velocity (vx, vy), radius, size, and mass.
 * The bubble can have forces applied to it which affect its velocity and position.
 * The mass is calculated based on the area of the bubble (π * r^2).
 */
public class Bubble {

    private static final Logger LOGGER = Logger.getLogger(BubbleChartApp.class.getName());

    /** Unique identifier for the bubble */
    private final UUID uuid;

    /** X and Y coordinates representing the position of the bubble */
    private double x, y;

    /** X and Y velocities of the bubble */
    private double vx, vy;

    /** Radius of the bubble, proportional to its size */
    private double radius;

    /** Size factor of the bubble (used initially to set the radius) */
    private double size;

    /** Mass of the bubble, proportional to the area of the bubble (π * r^2) */
    private double mass;

    /** Damping factor to slow down movement, simulating resistance */
    private static final double DAMPING = 0.98;

    /**
     * Constructs a {@code Bubble} object with an initial position and size.
     * The mass is calculated based on the area of the bubble.
     *
     * @param x     the initial X coordinate of the bubble
     * @param y     the initial Y coordinate of the bubble
     * @param size  the size factor used to calculate the radius and mass
     */
    public Bubble(double x, double y, double size) {
        this.uuid = UUID.randomUUID(); // Generate a unique identifier for each bubble
        this.x = x;
        this.y = y;
        this.size = size;
        this.radius = size; // Initially set the radius equal to the size
        updateMass(); // Calculate mass based on the initial radius
        this.vx = 0; // Initial velocity in the X direction
        this.vy = 0; // Initial velocity in the Y direction
    }

    /**
     * Returns the unique UUID of the bubble.
     *
     * @return the UUID of the bubble
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns the X coordinate of the bubble's position.
     *
     * @return the X coordinate of the bubble
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the X coordinate of the bubble's position.
     *
     * @param x the new X coordinate of the bubble
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the Y coordinate of the bubble's position.
     *
     * @return the Y coordinate of the bubble
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the Y coordinate of the bubble's position.
     *
     * @param y the new Y coordinate of the bubble
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns the radius of the bubble.
     *
     * @return the radius of the bubble
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the bubble.
     * The mass is recalculated whenever the radius changes.
     *
     * @param radius the new radius of the bubble
     */
    public void setRadius(double radius) {
        this.radius = radius;
        updateMass(); // Update mass based on the new radius
    }

    /**
     * Returns the size (initially used to set the radius) of the bubble.
     *
     * @return the size of the bubble
     */
    public double getSize() {
        return size;
    }

    /**
     * Sets the size of the bubble, updating the radius and mass accordingly.
     *
     * @param size the new size of the bubble
     */
    public void setSize(double size) {
        this.size = size;
        this.radius = size; // Adjust radius based on size
        updateMass(); // Update mass based on the new radius
    }

    /**
     * Updates the mass of the bubble based on the radius.
     * The mass is proportional to the area of the bubble (π * r^2).
     */
    public void updateMass() {
        this.mass = Math.PI * Math.pow(this.radius, 2); // Mass is proportional to area
    }

    /**
     * Returns the mass of the bubble.
     *
     * @return the mass of the bubble
     */
    public double getMass() {
        return mass;
    }

    /**
     * Applies a force to the bubble, adjusting its velocity based on the force applied and its mass.
     * This follows Newton's second law: F = ma, where acceleration (a) = F / m.
     *
     * @param fx the force applied in the X direction
     * @param fy the force applied in the Y direction
     */
    public void applyForce(double fx, double fy) {
        this.vx += fx / mass;  // Apply force in the X direction
        this.vy += fy / mass;  // Apply force in the Y direction
    }

    /**
     * Updates the position of the bubble based on its current velocity.
     * The velocity is also reduced by a damping factor to simulate resistance or friction.
     *
     * @param damping the damping factor to slow down the bubble's movement
     */
    public void updatePosition(double damping, double chartWidth, double chartHeight) {
        // Validate velocity and position
        if (Double.isNaN(vx) || Double.isNaN(vy)) {
            vx = 0;
            vy = 0;
        }
        if (Double.isNaN(x) || Double.isNaN(y)) {
            x = chartWidth / 2;
            y = chartHeight / 2;
        }

        this.vx *= damping;
        this.vy *= damping;

        this.x += this.vx;
        this.y += this.vy;

        LOGGER.info(String.format("Bubble %s updated position to (%.2f, %.2f), Velocity: (%.2f, %.2f), Damping: %.2f",
                uuid, x, y, vx, vy, damping));
    }


    /**
     * Checks if the bubble is still moving by examining its velocity.
     * If the velocity in both X and Y directions is below a small threshold, the bubble is considered stationary.
     *
     * @return {@code true} if the bubble is moving, {@code false} otherwise
     */
    public boolean isMoving() {
        return Math.abs(vx) > 0.01 || Math.abs(vy) > 0.01; // A small threshold to consider the bubble moving
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }
}



