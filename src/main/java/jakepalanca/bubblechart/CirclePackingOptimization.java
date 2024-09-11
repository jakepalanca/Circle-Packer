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

        // Loop through each pair of bubbles
        for (int i = 0; i < bubbles.size(); i++) {
            Bubble bubbleI = bubbles.get(i);
            double x_i = point[2 * i];
            double y_i = point[2 * i + 1];
            double radius_i = bubbleI.getRadius();

            // Check if bubbleI violates boundary constraints
            if (x_i - radius_i < 0 || x_i + radius_i > width || y_i - radius_i < 0 || y_i + radius_i > height) {
                penalty += 1000;  // Apply a large penalty for boundary violation
            }

            // Now compare with all other bubbles to check for overlap
            for (int j = i + 1; j < bubbles.size(); j++) {
                Bubble bubbleJ = bubbles.get(j);
                double x_j = point[2 * j];
                double y_j = point[2 * j + 1];
                double radius_j = bubbleJ.getRadius();

                // Calculate the distance between bubble i and bubble j
                double distSquared = Math.pow(x_i - x_j, 2) + Math.pow(y_i - y_j, 2);
                double minDistSquared = Math.pow(radius_i + radius_j, 2);

                // Check if the bubbles overlap (distSquared should be greater than or equal to minDistSquared)
                if (distSquared < minDistSquared) {
                    penalty += Math.pow(minDistSquared - distSquared, 2);  // Penalty increases as overlap increases
                }
            }
        }

        return penalty;
    }


    private double[] initializeGreedy() {
        double[] startPoint = new double[bubbles.size() * 2];

        // Distribute the bubbles in a grid-like pattern as initial guess to avoid overlap
        int cols = (int) Math.ceil(Math.sqrt(bubbles.size()));  // Number of columns in grid
        double xIncrement = chart.getWidth() / (cols + 1);
        double yIncrement = chart.getHeight() / (cols + 1);

        for (int i = 0; i < bubbles.size(); i++) {
            int row = i / cols;
            int col = i % cols;

            startPoint[2 * i] = xIncrement * (col + 1);  // x position
            startPoint[2 * i + 1] = yIncrement * (row + 1);  // y position
        }
        return startPoint;
    }

}
