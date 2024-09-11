package jakepalanca.bubblechart;

import java.util.ArrayList;
import java.util.List;

public class KnapsackSolver {
    private List<Bubble> allBubbles;
    private double maxWidth;
    private double maxHeight;

    public KnapsackSolver(List<Bubble> allBubbles, double maxWidth, double maxHeight) {
        this.allBubbles = allBubbles;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public List<Bubble> solve() {
        List<Bubble> selectedBubbles = new ArrayList<>();
        double totalArea = maxWidth * maxHeight;
        double currentArea = 0;

        // Sort bubbles by radius ratio to ensure proportional distribution
        allBubbles.sort((b1, b2) -> Double.compare(b2.getRadiusRatio(), b1.getRadiusRatio()));

        for (Bubble bubble : allBubbles) {
            // Calculate the bubble area based on its radius ratio
            double bubbleArea = Math.PI * Math.pow(bubble.getRadius(), 2);

            // Check if adding this bubble will exceed the total available area
            if (currentArea + bubbleArea <= totalArea) {
                selectedBubbles.add(bubble);
                currentArea += bubbleArea;  // Accumulate the area used
            }
        }

        return selectedBubbles;  // Return the selected subset of bubbles
    }
}

