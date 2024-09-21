package jakepalanca.circlepacker;

import java.util.List;

/**
 * Represents the result of the circle packing algorithm. This class contains details about
 * the outcome of the packing process, such as the final arrangement of packable objects,
 * computation time, number of iterations, and overlap information.
 *
 * @param <T> The type of objects that implement {@link Packable}.
 */
public class PackingResult<T extends Packable> {

    private final List<T> packables;
    private final long computationTime; // in milliseconds
    private final int iterations;
    private final boolean overlapsExist;
    private final double totalOverlapArea;
    private final int adjustmentsMade;

    /**
     * Constructs a new PackingResult with the given details.
     *
     * @param packables        the list of packed objects
     * @param computationTime  the time taken to perform the packing algorithm, in milliseconds
     * @param iterations       the number of iterations performed during the packing process
     * @param overlapsExist    whether any overlaps between objects still exist after packing
     * @param totalOverlapArea the total area of overlaps between objects, if any
     * @param adjustmentsMade  the number of adjustments made during the packing process
     */
    public PackingResult(List<T> packables, long computationTime, int iterations, boolean overlapsExist, double totalOverlapArea, int adjustmentsMade) {
        this.packables = packables;
        this.computationTime = computationTime;
        this.iterations = iterations;
        this.overlapsExist = overlapsExist;
        this.totalOverlapArea = totalOverlapArea;
        this.adjustmentsMade = adjustmentsMade;
    }

    /**
     * Returns the list of packed objects.
     *
     * @return the list of packables
     */
    public List<T> getPackables() {
        return packables;
    }

    /**
     * Returns the time taken for the packing computation in milliseconds.
     *
     * @return the computation time in milliseconds
     */
    public long getComputationTime() {
        return computationTime;
    }

    /**
     * Returns the number of iterations performed during the packing process.
     *
     * @return the number of iterations
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Returns whether any overlaps still exist between the packed objects after the optimization.
     *
     * @return {@code true} if overlaps exist, {@code false} otherwise
     */
    public boolean isOverlapsExist() {
        return overlapsExist;
    }

    /**
     * Returns the total area of overlaps between objects.
     *
     * @return the total overlap area
     */
    public double getTotalOverlapArea() {
        return totalOverlapArea;
    }

    /**
     * Returns the number of adjustments made during the packing process.
     *
     * @return the number of adjustments made
     */
    public int getAdjustmentsMade() {
        return adjustmentsMade;
    }
}
