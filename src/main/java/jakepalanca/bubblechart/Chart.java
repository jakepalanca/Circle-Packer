package jakepalanca.bubblechart;

import java.util.*;

/**
 * Class representing a chart that manages packable objects and optimizes their placement.
 */
public class Chart {

    private double width;
    private double height;
    private Map<UUID, Packable> packables;

    public Chart(double width, double height) {
        this.width = width;
        this.height = height;
        this.packables = new HashMap<>();
    }

    /**
     * Adds a packable object to the chart.
     *
     * @param packable The packable object to add.
     */
    public void addPackable(Packable packable) {
        packables.put(packable.getId(), packable);
    }

    /**
     * Removes a packable object from the chart by its UUID.
     *
     * @param id The UUID of the packable object to remove.
     */
    public void removePackable(UUID id) {
        packables.remove(id);
    }

    /**
     * Optimizes the placement of packable objects within the chart dimensions.
     *
     * @param maxIterations Maximum number of iterations for the optimization algorithm.
     * @return PackingResult containing optimization details.
     */
    public PackingResult<Packable> optimize(int maxIterations) {
        List<Packable> packableList = new ArrayList<>(packables.values());
        return Packing.packCircles(width, height, packableList, maxIterations);
    }

    /**
     * Gets all packable objects in the chart.
     *
     * @return Collection of packable objects.
     */
    public Collection<Packable> getPackables() {
        return packables.values();
    }

    // Getters and setters for width and height if needed
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
