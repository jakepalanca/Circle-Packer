package jakepalanca.bubblechart;

import java.util.List;
import java.util.Random;

/**
 * Class containing methods for packing circles within a rectangle.
 */
public class Packing {

    public static PackingResult<Packable> packCircles(double width, double height, List<? extends Packable> circles, int maxIterations) {
        // Validate rectangle dimensions
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Rectangle dimensions must be positive.");
        }

        // Calculate total area and desired packing density
        double rectangleArea = width * height;
        double desiredPackingDensity = 0.8; // Adjust as needed
        double maxTotalCircleArea = rectangleArea * desiredPackingDensity;

        // Calculate total radius ratio
        double totalRadiusRatio = 0;
        for (Packable circle : circles) {
            if (circle.getRadiusRatio() <= 0) {
                throw new IllegalArgumentException("All circle radius ratios must be positive.");
            }
            totalRadiusRatio += circle.getRadiusRatio();
        }

        // Initialize radii and positions
        Random rand = new Random();
        for (Packable circle : circles) {
            double circleArea = (circle.getRadiusRatio() / totalRadiusRatio) * maxTotalCircleArea;
            double radius = Math.sqrt(circleArea / Math.PI);
            circle.setRadius(radius);

            // Initial random placement within bounds
            double x = radius + rand.nextDouble() * (width - 2 * radius);
            double y = radius + rand.nextDouble() * (height - 2 * radius);
            circle.setX(x);
            circle.setY(y);
        }

        // Optimize positions
        long startTime = System.currentTimeMillis();
        int iterations = optimizeCircles(width, height, circles, maxIterations);
        long computationTime = System.currentTimeMillis() - startTime;

        // Adjust positions to ensure circles are within bounds
        adjustPositionsToFit(circles, width, height);

        // Check for overlaps and compute total overlap area
        boolean overlapsExist = false;
        double totalOverlapArea = 0;
        for (int i = 0; i < circles.size(); i++) {
            Packable c1 = circles.get(i);
            for (int j = i + 1; j < circles.size(); j++) {
                Packable c2 = circles.get(j);
                double overlapArea = calculateOverlapArea(c1, c2);
                if (overlapArea > 0) {
                    overlapsExist = true;
                    totalOverlapArea += overlapArea;
                }
            }
        }

        int adjustmentsMade = iterations; // Using iterations as adjustments for simplicity

        return new PackingResult<>((List<Packable>) circles, computationTime, iterations, overlapsExist, totalOverlapArea, adjustmentsMade);
    }

    private static void adjustPositionsToFit(List<? extends Packable> circles, double width, double height) {
        for (Packable circle : circles) {
            // Adjust positions to keep circles within bounds
            double x = circle.getX();
            double y = circle.getY();
            double radius = circle.getRadius();

            x = Math.max(radius, Math.min(x, width - radius));
            y = Math.max(radius, Math.min(y, height - radius));

            circle.setX(x);
            circle.setY(y);
        }
    }
    /**
     * Optimizes circle positions and sizes to minimize overlaps.
     *
     * @param width          The width of the rectangle.
     * @param height         The height of the rectangle.
     * @param circles        List of circles.
     * @param maxIterations  Maximum number of iterations.
     * @return Number of iterations performed.
     */
    private static int optimizeCircles(double width, double height, List<? extends Packable> circles, int maxIterations) {
        int iteration = 0;
        boolean hasOverlaps = true;
        Random rand = new Random();

        while (iteration < maxIterations && hasOverlaps) {
            hasOverlaps = false;

            for (int i = 0; i < circles.size(); i++) {
                Packable c1 = circles.get(i);

                for (int j = i + 1; j < circles.size(); j++) {
                    Packable c2 = circles.get(j);

                    double dx = c2.getX() - c1.getX();
                    double dy = c2.getY() - c1.getY();
                    double distance = Math.hypot(dx, dy);
                    double minDistance = c1.getRadius() + c2.getRadius();

                    if (distance < minDistance) {
                        hasOverlaps = true;
                        double overlap = minDistance - distance;

                        if (distance == 0) {
                            // Assign a small random shift to avoid division by zero
                            double angle = rand.nextDouble() * 2 * Math.PI;
                            dx = Math.cos(angle);
                            dy = Math.sin(angle);
                            distance = 0.001; // Small value to prevent division by zero
                        }

                        // Adjust positions to resolve overlap
                        double shiftX = (dx / distance) * (overlap / 2);
                        double shiftY = (dy / distance) * (overlap / 2);

                        c1.setX(c1.getX() - shiftX);
                        c1.setY(c1.getY() - shiftY);
                        c2.setX(c2.getX() + shiftX);
                        c2.setY(c2.getY() + shiftY);

                        // Keep circles within bounds
                        keepCircleWithinBounds(c1, width, height);
                        keepCircleWithinBounds(c2, width, height);
                    }
                }
            }

            iteration++;
        }

        // Final adjustment: shrink circles if necessary
        adjustSizesToFit(circles, width, height);

        return iteration;
    }

    /**
     * Adjusts circle sizes to fit within the rectangle without overlaps.
     *
     * @param circles  List of circles.
     * @param width    The width of the rectangle.
     * @param height   The height of the rectangle.
     */
    private static void adjustSizesToFit(List<? extends Packable> circles, double width, double height) {
        boolean resized;
        do {
            resized = false;

            for (int i = 0; i < circles.size(); i++) {
                Packable c1 = circles.get(i);

                // Check if circle is out of bounds
                if (c1.getX() - c1.getRadius() < 0 ||
                        c1.getX() + c1.getRadius() > width ||
                        c1.getY() - c1.getRadius() < 0 ||
                        c1.getY() + c1.getRadius() > height) {

                    // Reduce size
                    c1.setRadius(c1.getRadius() * 0.95);
                    resized = true;
                }

                // Check for overlaps
                for (int j = i + 1; j < circles.size(); j++) {
                    Packable c2 = circles.get(j);

                    double dx = c2.getX() - c1.getX();
                    double dy = c2.getY() - c1.getY();
                    double distance = Math.hypot(dx, dy);
                    double minDistance = c1.getRadius() + c2.getRadius();

                    if (distance < minDistance) {
                        // Reduce sizes
                        c1.setRadius(c1.getRadius() * 0.95);
                        c2.setRadius(c2.getRadius() * 0.95);
                        resized = true;
                    }
                }
            }
        } while (resized);
    }

    /**
     * Keeps a circle within the bounds of the rectangle.
     *
     * @param circle  The circle to adjust.
     * @param width   The width of the rectangle.
     * @param height  The height of the rectangle.
     */
    private static void keepCircleWithinBounds(Packable circle, double width, double height) {
        double x = circle.getX();
        double y = circle.getY();
        double radius = circle.getRadius();

        x = Math.max(radius, Math.min(x, width - radius));
        y = Math.max(radius, Math.min(y, height - radius));

        circle.setX(x);
        circle.setY(y);
    }

    /**
     * Calculates the overlap area between two circles.
     *
     * @param c1 First circle.
     * @param c2 Second circle.
     * @return Overlap area.
     */
    private static double calculateOverlapArea(Packable c1, Packable c2) {
        double dx = c2.getX() - c1.getX();
        double dy = c2.getY() - c1.getY();
        double distance = Math.hypot(dx, dy);

        // No overlap
        if (distance >= c1.getRadius() + c2.getRadius()) {
            return 0;
        }

        // One circle is completely inside the other
        if (distance <= Math.abs(c1.getRadius() - c2.getRadius())) {
            double smallerRadius = Math.min(c1.getRadius(), c2.getRadius());
            return Math.PI * smallerRadius * smallerRadius;
        }

        // Partial overlap
        double r1 = c1.getRadius();
        double r2 = c2.getRadius();

        double part1 = r1 * r1 * Math.acos((distance * distance + r1 * r1 - r2 * r2) / (2 * distance * r1));
        double part2 = r2 * r2 * Math.acos((distance * distance + r2 * r2 - r1 * r1) / (2 * distance * r2));
        double part3 = 0.5 * Math.sqrt((-distance + r1 + r2) * (distance + r1 - r2) * (distance - r1 + r2) * (distance + r1 + r2));

        return part1 + part2 - part3;
    }
}
