package jakepalanca.bubblechart;

import jakepalanca.bubblechart.Bubble;
import jakepalanca.bubblechart.Quadtree;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * The {@code BubbleChartSim} class simulates a force-directed layout using quadtree-based proximity checks.
 */
public class BubbleChartSim {

    private List<Bubble> bubbles;
    private Quadtree quadtree;
    private double width;
    private double height;

    private double baseRepulsionStrength = 1500;
    private double attractionStrength = 0.001;
    private double gravitationalPull = 0.001;
    private double damping = 0.53;

    private double endRepulsionStrength;
    private double endAttractionStrength;

    private double minDimension;
    private static final int MAX_ITERATIONS = 1000;

    private static final Logger LOGGER = Logger.getLogger(BubbleChartSim.class.getName());

    public BubbleChartSim(double width, double height) {
        this.width = width;
        this.height = height;
        this.bubbles = new ArrayList<>();
        this.minDimension = Math.min(width, height) * 0.9; // 90% of the smallest dimension
        this.quadtree = new Quadtree(0, new Rectangle2D.Double(0, 0, width, height));  // Initialize quadtree
    }

    public void addBubble(Bubble bubble) {
        double offset = Math.random() * 10 - 5;  // Random offset between -5 and 5
        bubble.setX(width / 2 + offset);
        bubble.setY(height / 2 + offset);
        bubbles.add(bubble);
        quadtree.insert(bubble);  // Insert bubble into the quadtree
        adjustBubbleSizes();
    }

    public void removeBubble(UUID uuid) {
        bubbles.removeIf(b -> b.getUuid().equals(uuid));
        quadtree.clear();
        for (Bubble bubble : bubbles) {
            quadtree.insert(bubble);  // Reinsert all bubbles after removal
        }
        adjustBubbleSizes();
    }

    private void adjustBubbleSizes() {
        boolean overlapDetected = true;
        boolean borderIntersection = true;
        int retries = 0;

        while ((overlapDetected || borderIntersection) && retries < MAX_ITERATIONS) {
            retries++;
            double totalRadiusRatio = bubbles.stream().mapToDouble(Bubble::getSize).sum();

            for (Bubble bubble : bubbles) {
                double proportionalSize = (bubble.getSize() / totalRadiusRatio) * minDimension;
                proportionalSize = Math.min(proportionalSize, minDimension * 0.45);
                bubble.setRadius(proportionalSize);
                bubble.setMass(Math.PI * Math.pow(bubble.getRadius(), 2));  // Set mass based on area
            }

            applyDynamicForces();
            overlapDetected = checkForOverlap();
            borderIntersection = checkForBorderIntersection();

            if (overlapDetected || borderIntersection) {
                reduceBubbleSizes();
            }
        }
    }

    private void applyDynamicForces() {
        quadtree.clear();  // Clear and reinsert bubbles into the quadtree for every frame
        for (Bubble bubble : bubbles) {
            quadtree.insert(bubble);
        }

        for (Bubble bubble : bubbles) {
            applyAttraction(bubble);
            applyDynamicRepulsionAndAttraction(bubble);
        }
    }

    private void applyDynamicRepulsionAndAttraction(Bubble bubble) {
        List<Bubble> nearbyBubbles = quadtree.retrieve(new ArrayList<>(), bubble);  // Get nearby bubbles

        for (Bubble other : nearbyBubbles) {
            if (bubble != other) {
                double dx = bubble.getX() - other.getX();
                double dy = bubble.getY() - other.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 1e-6) {
                    distance = 1e-6;  // Avoid zero or near-zero distances to prevent NaN values
                }

                double minDistance = bubble.getRadius() + other.getRadius();
                double maxDistance = minDimension;

                // Repulsion calculation
                if (distance < minDistance) {
                    double overlap = minDistance - distance;
                    endRepulsionStrength = baseRepulsionStrength * (overlap / minDistance);  // Scale repulsion to overlap
                    bubble.applyForce((dx / distance) * endRepulsionStrength, (dy / distance) * endRepulsionStrength);

                    LOGGER.info(String.format("Repulsion: Bubble %s applying repulsion force (%.2f, %.2f) to Bubble %s. Distance: %.2f, Overlap: %.2f, Force: %.2f",
                            bubble.getUuid(), dx / distance * endRepulsionStrength, dy / distance * endRepulsionStrength, other.getUuid(), distance, overlap, endRepulsionStrength));
                }

                // Attraction calculation
                else if (distance > maxDistance) {
                    double distanceBeyondMax = distance - maxDistance;
                    endAttractionStrength = attractionStrength + distanceBeyondMax * 0.01;
                    bubble.applyForce(-(dx / distance) * endAttractionStrength, -(dy / distance) * endAttractionStrength);

                    LOGGER.info(String.format("Attraction: Bubble %s applying attraction force (%.2f, %.2f) to Bubble %s. Distance: %.2f, Distance beyond max: %.2f, Force: %.2f",
                            bubble.getUuid(), -(dx / distance) * endAttractionStrength, -(dy / distance) * endAttractionStrength, other.getUuid(), distance, distanceBeyondMax, endAttractionStrength));
                }
            }
        }
    }

    private void applyAttraction(Bubble bubble) {
        double centerX = width / 2;
        double centerY = height / 2;
        double dx = centerX - bubble.getX();
        double dy = centerY - bubble.getY();

        double distanceFromCenter = Math.sqrt(dx * dx + dy * dy);
        if (distanceFromCenter < 1e-6) {
            distanceFromCenter = 1e-6;  // Prevent zero distance from center
        }

        double gravityStrength = gravitationalPull * distanceFromCenter;

        bubble.applyForce(dx * gravityStrength, dy * gravityStrength);

        LOGGER.info(String.format("Dynamic Gravity: Bubble %s pulling to center with force (%.2f, %.2f). Distance from center: %.2f, Gravity strength: %.2f",
                bubble.getUuid(), dx * gravityStrength, dy * gravityStrength, distanceFromCenter, gravityStrength));
    }

    private void reduceBubbleSizes() {
        for (Bubble bubble : bubbles) {
            bubble.setRadius(bubble.getRadius() * 0.95);
        }
    }

    private boolean checkForBorderIntersection() {
        boolean intersectionDetected = false;
        for (Bubble bubble : bubbles) {
            if (bubble.getX() - bubble.getRadius() < 0 || bubble.getX() + bubble.getRadius() > width ||
                    bubble.getY() - bubble.getRadius() < 0 || bubble.getY() + bubble.getRadius() > height) {
                intersectionDetected = true;
                LOGGER.warning(String.format("Border intersection detected for Bubble %s at position (%.2f, %.2f).",
                        bubble.getUuid(), bubble.getX(), bubble.getY()));
                bubble.setX(Math.max(bubble.getRadius(), Math.min(width - bubble.getRadius(), bubble.getX())));
                bubble.setY(Math.max(bubble.getRadius(), Math.min(height - bubble.getRadius(), bubble.getY())));
            }
        }
        return intersectionDetected;
    }

    boolean checkForOverlap() {
        boolean overlapDetected = false;
        for (int i = 0; i < bubbles.size(); i++) {
            for (int j = i + 1; j < bubbles.size(); j++) {
                Bubble bubble1 = bubbles.get(i);
                Bubble bubble2 = bubbles.get(j);
                double dx = bubble2.getX() - bubble1.getX();
                double dy = bubble2.getY() - bubble1.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                double sumOfRadii = bubble1.getRadius() + bubble2.getRadius();

                if (distance < sumOfRadii) {
                    overlapDetected = true;
                    LOGGER.warning(String.format("Overlap detected between Bubble %s and Bubble %s. Distance: %.2f, Combined Radius: %.2f",
                            bubble1.getUuid(), bubble2.getUuid(), distance, sumOfRadii));
                }
            }
        }
        return overlapDetected;
    }

    public void simulateUntilStable() {
        boolean bubblesMoving = true;
        int iteration = 0;
        while (bubblesMoving && iteration < MAX_ITERATIONS) {
            LOGGER.info(String.format("Iteration %d: Simulating forces", iteration));
            applyDynamicForces();
            bubblesMoving = false;

            for (Bubble bubble : bubbles) {
                bubble.updatePosition(damping, width, height);
                if (bubble.isMoving()) {
                    bubblesMoving = true;
                    LOGGER.info(String.format("Bubble %s is still moving. Position (%.2f, %.2f), Velocity (%.2f, %.2f)",
                            bubble.getUuid(), bubble.getX(), bubble.getY(), bubble.getVx(), bubble.getVy()));
                }
            }

            iteration++;
        }
        LOGGER.info("Simulation ended after " + iteration + " iterations.");
    }

    public double getEndRepulsionStrength() {
        return endRepulsionStrength;
    }

    public double getEndAttractionStrength() {
        return endAttractionStrength;
    }

    public void setBaseRepulsionStrength(double strength) {
        this.baseRepulsionStrength = strength;
    }

    public void setAttractionStrength(double strength) {
        this.attractionStrength = strength;
    }

    public void setDimensions(double width, double height) {
        this.width = width;
        this.height = height;
        this.minDimension = Math.min(width, height) * 0.9;
    }

    public List<Bubble> getBubbles() {
        return bubbles;
    }

    public double getBaseRepulsionStrength() {
        return baseRepulsionStrength;
    }

    public double getAttractionStrength() {
        return attractionStrength;
    }
}
