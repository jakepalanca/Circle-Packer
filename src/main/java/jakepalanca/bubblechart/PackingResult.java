package jakepalanca.bubblechart;

import java.util.List;

/**
 * Class representing the result of the packing algorithm.
 *
 * @param <T> Type of objects implementing Packable.
 */
class PackingResult<T extends Packable> {
    private final List<T> packables;
    private final long computationTime; // in milliseconds
    private final int iterations;
    private final boolean overlapsExist;
    private final double totalOverlapArea;
    private final int adjustmentsMade;

    public PackingResult(List<T> packables, long computationTime, int iterations, boolean overlapsExist, double totalOverlapArea, int adjustmentsMade) {
        this.packables = packables;
        this.computationTime = computationTime;
        this.iterations = iterations;
        this.overlapsExist = overlapsExist;
        this.totalOverlapArea = totalOverlapArea;
        this.adjustmentsMade = adjustmentsMade;
    }

    public List<T> getPackables() {
        return packables;
    }

    public long getComputationTime() {
        return computationTime;
    }

    public int getIterations() {
        return iterations;
    }

    public boolean isOverlapsExist() {
        return overlapsExist;
    }

    public double getTotalOverlapArea() {
        return totalOverlapArea;
    }

    public int getAdjustmentsMade() {
        return adjustmentsMade;
    }
}
