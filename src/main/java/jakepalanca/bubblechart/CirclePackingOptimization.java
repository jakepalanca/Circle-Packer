package jakepalanca.bubblechart;

import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import java.util.List;
import java.util.logging.Logger;

public class CirclePackingOptimization {
    private static final Logger logger = Logger.getLogger(CirclePackingOptimization.class.getName());
    private final Chart chart;
    private final List<Bubble> bubbles;

    public CirclePackingOptimization(Chart chart, List<Bubble> selectedBubbles) {
        this.chart = chart;
        this.bubbles = selectedBubbles;
    }

    public void optimize() {
        if (bubbles.isEmpty()) {
            logger.warning("No bubbles to optimize.");
            return;
        }

        // Position each bubble by solving the packing problem
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(bubbles.size() * 2 + 2);
        double[] startPoint = initializeGreedy();
        int dimension = 2 * bubbles.size();  // Dimension for x, y coordinates for each bubble

        // Set lower and upper bounds for each bubble
        double[] lowerBounds = new double[dimension];
        double[] upperBounds = new double[dimension];

        for (int i = 0; i < bubbles.size(); i++) {
            Bubble bubble = bubbles.get(i);
            double radius = bubble.getRadius();

            // Ensure bubbles stay within the bounds of the rectangle
            lowerBounds[2 * i] = radius;                   // x >= radius
            upperBounds[2 * i] = chart.getWidth() - radius; // x <= width - radius

            lowerBounds[2 * i + 1] = radius;                // y >= radius
            upperBounds[2 * i + 1] = chart.getHeight() - radius; // y <= height - radius
        }

        try {
            PointValuePair result = optimizer.optimize(
                    new MaxEval(5000),
                    new ObjectiveFunction(point -> calculateObjectiveFunction(point)),
                    GoalType.MINIMIZE,
                    new InitialGuess(startPoint),
                    new SimpleBounds(lowerBounds, upperBounds)  // Ensure bounds are properly set
            );

            // Update bubble positions with optimized values
            double[] optimizedPoints = result.getPoint();
            for (int i = 0; i < bubbles.size(); i++) {
                bubbles.get(i).setPosition(optimizedPoints[2 * i], optimizedPoints[2 * i + 1]);
            }

        } catch (Exception e) {
            logger.severe("Optimization failed: " + e.getMessage());
        }
    }
    private double calculateObjectiveFunction(double[] point) {
        double penalty = 0.0;

        // Rectangle dimensions
        double width = chart.getWidth();
        double height = chart.getHeight();

        double totalBubbleArea = 0;

        // Loop through each bubble to check for boundary violations and overlaps
        for (int i = 0; i < bubbles.size(); i++) {
            Bubble bubbleI = bubbles.get(i);
            double x_i = point[2 * i];
            double y_i = point[2 * i + 1];
            double radius_i = bubbleI.getRadius();

            // Calculate total area of the bubble
            totalBubbleArea += Math.PI * radius_i * radius_i;

            // Penalize boundary violations (much larger penalty for exceeding borders)
            if (x_i - radius_i < 0 || x_i + radius_i > width || y_i - radius_i < 0 || y_i + radius_i > height) {
                penalty += 5000;  // Large penalty for boundary violation
            }

            // Check for overlaps with other bubbles
            for (int j = i + 1; j < bubbles.size(); j++) {
                Bubble bubbleJ = bubbles.get(j);
                double x_j = point[2 * j];
                double y_j = point[2 * j + 1];
                double radius_j = bubbleJ.getRadius();

                // Calculate the distance between bubble i and bubble j
                double distSquared = Math.pow(x_i - x_j, 2) + Math.pow(y_i - y_j, 2);
                double minDistSquared = Math.pow(radius_i + radius_j, 2);

                // Check if the bubbles overlap
                if (distSquared < minDistSquared) {
                    penalty += Math.pow(minDistSquared - distSquared, 2);  // Penalty increases as overlap increases
                }
            }
        }

        // Light penalty for unused space to encourage maximizing bubble sizes
        double totalArea = width * height;
        penalty += 0.01 * (totalArea - totalBubbleArea);

        return penalty;
    }


    private double[] initializeGreedy() {
        double[] startPoint = new double[bubbles.size() * 2];

        // Number of columns in grid-like pattern
        int cols = (int) Math.ceil(Math.sqrt(bubbles.size()));
        double xIncrement = (chart.getWidth() - getMaxBubbleRadius() * 2) / (cols + 1);
        double yIncrement = (chart.getHeight() - getMaxBubbleRadius() * 2) / (cols + 1);

        for (int i = 0; i < bubbles.size(); i++) {
            int row = i / cols;
            int col = i % cols;

            Bubble bubble = bubbles.get(i);
            double radius = bubble.getRadius();

            // Ensure initial placement accounts for radius and avoids border
            startPoint[2 * i] = Math.max(radius, Math.min(chart.getWidth() - radius, xIncrement * (col + 1)));
            startPoint[2 * i + 1] = Math.max(radius, Math.min(chart.getHeight() - radius, yIncrement * (row + 1)));
        }

        return startPoint;
    }

    private double getMaxBubbleRadius() {
        return bubbles.stream().mapToDouble(Bubble::getRadius).max().orElse(0);
    }


}
