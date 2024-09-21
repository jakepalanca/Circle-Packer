package jakepalanca.circlepacker;

import java.util.*;

/**
 * Represents a chart that manages packable objects (such as circles) and optimizes their placement
 * within specified dimensions. The optimization ensures minimal overlap and efficient use of space.
 * <p>
 * This class provides methods to add, remove, and retrieve packable objects, as well as perform
 * an optimization to arrange the objects within the chart dimensions using a packing algorithm.
 * </p>
 */
public class Chart {

    private final double width;
    private final double height;
    private final Map<UUID, Packable> packables;

    /**
     * Constructs a new Chart with the specified dimensions.
     *
     * @param width  the width of the chart
     * @param height the height of the chart
     */
    public Chart(double width, double height) {
        this.width = width;
        this.height = height;
        this.packables = new HashMap<>();
    }

    /**
     * Adds a packable object to the chart.
     * Each packable object is identified by its unique ID (UUID).
     *
     * @param packable the packable object to add to the chart
     * @throws IllegalArgumentException if the packable is null
     */
    public void addPackable(Packable packable) {
        if (packable == null) {
            throw new IllegalArgumentException("Packable cannot be null");
        }
        packables.put(packable.getId(), packable);
    }

    /**
     * Removes a packable object from the chart by its unique identifier (UUID).
     *
     * @param id the UUID of the packable object to remove
     * @throws NoSuchElementException if no packable object is found with the provided UUID
     */
    public void removePackable(UUID id) {
        if (!packables.containsKey(id)) {
            throw new NoSuchElementException("Packable with UUID " + id + " not found");
        }
        packables.remove(id);
    }

    /**
     * Optimizes the placement of all packable objects within the chart.
     * The optimization algorithm attempts to minimize overlap and improve the layout of the objects
     * within the given chart dimensions.
     *
     * @param maxIterations the maximum number of iterations allowed for the optimization process
     * @return a {@link PackingResult} containing details of the optimized arrangement
     */
    public PackingResult<Packable> optimize(int maxIterations) {
        List<Packable> packableList = new ArrayList<>(packables.values());
        return Packing.packCircles(width, height, packableList, maxIterations);
    }

    /**
     * Retrieves all the packable objects currently in the chart.
     *
     * @return a collection of all packable objects in the chart
     */
    public Collection<Packable> getPackables() {
        return packables.values();
    }

    /**
     * Returns the width of the chart.
     *
     * @return the width of the chart
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the height of the chart.
     *
     * @return the height of the chart
     */
    public double getHeight() {
        return height;
    }
}
