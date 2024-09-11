package jakepalanca.bubblechart;

import java.util.ArrayList;
import java.util.List;

public class Chart {
    private double width;
    private double height;
    private List<Bubble> bubbles;

    public Chart(double width, double height) {
        this.width = width;
        this.height = height;
        this.bubbles = new ArrayList<>();
    }

    public void addBubble(double radiusRatio) {
        Bubble bubble = new Bubble(0, radiusRatio);  // Add a bubble with 0 radius, to be recalculated
        bubbles.add(bubble);
        recalculateBubbleRadii();  // Recalculate the sizes after each bubble is added
        solveKnapsackProblem();  // Solve the knapsack problem to pack bubbles effectively
    }

    public void removeBubble() {
        if (!bubbles.isEmpty()) {
            bubbles.remove(bubbles.size() - 1);
            recalculateBubbleRadii();  // Recalculate the sizes after removal
            solveKnapsackProblem();  // Re-solve after removing
        }
    }

    // Dynamically recalculate bubble sizes based on their radius ratios
    public void recalculateBubbleRadii() {
        double totalRatio = bubbles.stream().mapToDouble(Bubble::getRadiusRatio).sum();
        double totalArea = width * height;

        for (Bubble bubble : bubbles) {
            double bubbleArea = (bubble.getRadiusRatio() / totalRatio) * totalArea;
            double newRadius = Math.sqrt(bubbleArea / Math.PI);
            bubble.setRadius(newRadius);
        }
    }

    // Solve the knapsack problem and optimize packing
    public void solveKnapsackProblem() {
        KnapsackSolver knapsackSolver = new KnapsackSolver(bubbles, width, height);  // Call the KnapsackSolver constructor
        List<Bubble> selectedBubbles = knapsackSolver.solve();  // Solve the problem and get selected bubbles
        optimizePacking(selectedBubbles);  // Optimize packing for the selected bubbles
    }

    // Optimize packing for the selected bubbles
    public void optimizePacking(List<Bubble> selectedBubbles) {
        CirclePackingOptimization optimizer = new CirclePackingOptimization(this, selectedBubbles);
        optimizer.optimize();  // Call the optimization logic for packing
    }

    // Getter methods for chart dimensions
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public List<Bubble> getBubbles() {
        return bubbles;
    }
}
