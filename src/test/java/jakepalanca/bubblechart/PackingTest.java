package jakepalanca.bubblechart;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class PackingTest {

    /**
     * Mock Packable class for testing.
     */
    public static class MockPackable implements Packable {
        private UUID id;
        private double radiusRatio;
        private double radius;
        private double x;
        private double y;

        public MockPackable(double radiusRatio) {
            this.id = UUID.randomUUID();
            this.radiusRatio = radiusRatio;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public double getRadiusRatio() {
            return radiusRatio;
        }

        @Override
        public void setRadiusRatio(double radiusRatio) {
            this.radiusRatio = radiusRatio;
        }

        @Override
        public double getRadius() {
            return radius;
        }

        @Override
        public void setRadius(double radius) {
            this.radius = radius;
        }

        @Override
        public double getX() {
            return x;
        }

        @Override
        public void setX(double x) {
            this.x = x;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public void setY(double y) {
            this.y = y;
        }
    }

    @Test
    public void testSingleBubble() {
        List<MockPackable> bubbles = new ArrayList<>();
        bubbles.add(new MockPackable(1.0));

        PackingResult<Packable> result = Packing.packCircles(500, 500, bubbles, 1000);

        assertEquals(1, result.getPackables().size());
        Packable bubble = result.getPackables().get(0);

        // Bubble should be within bounds
        assertTrue(bubble.getX() >= bubble.getRadius());
        assertTrue(bubble.getX() <= 500 - bubble.getRadius());
        assertTrue(bubble.getY() >= bubble.getRadius());
        assertTrue(bubble.getY() <= 500 - bubble.getRadius());

        // No overlaps should exist
        assertFalse(result.isOverlapsExist());
    }

    @Test
    public void testTwoEqualBubbles() {
        List<MockPackable> bubbles = new ArrayList<>();
        bubbles.add(new MockPackable(1.0));
        bubbles.add(new MockPackable(1.0));

        PackingResult<Packable> result = Packing.packCircles(500, 500, bubbles, 1000);

        // Bubbles should not overlap
        assertFalse(result.isOverlapsExist());

        // Distance between bubbles should be at least sum of radii
        Packable b1 = result.getPackables().get(0);
        Packable b2 = result.getPackables().get(1);

        double dx = b2.getX() - b1.getX();
        double dy = b2.getY() - b1.getY();
        double distance = Math.hypot(dx, dy);
        double minDistance = b1.getRadius() + b2.getRadius();

        assertTrue(distance >= minDistance);
    }

    @Test
    public void testManySmallBubbles() {
        List<MockPackable> bubbles = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            bubbles.add(new MockPackable(0.1));
        }

        PackingResult<Packable> result = Packing.packCircles(500, 500, bubbles, 1000);

        // Check for overlaps
        assertFalse(result.isOverlapsExist());
    }

    @Test
    public void testLargeAndSmallBubbles() {
        List<MockPackable> bubbles = new ArrayList<>();
        bubbles.add(new MockPackable(1.0));
        bubbles.add(new MockPackable(0.5));
        bubbles.add(new MockPackable(0.2));

        PackingResult<Packable> result = Packing.packCircles(500, 500, bubbles, 1000);

        // Check for overlaps
        assertFalse(result.isOverlapsExist());
    }

    @Test
    public void testIdenticalBubbles() {
        List<MockPackable> bubbles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            bubbles.add(new MockPackable(1.0));
        }

        PackingResult<Packable> result = Packing.packCircles(1000, 1000, bubbles, 1000);

        // Check for overlaps
        assertFalse(result.isOverlapsExist());
    }

    @Test
    public void testRandomRatios() {
        List<MockPackable> bubbles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            double ratio = 0.1 + Math.random() * 0.9;
            bubbles.add(new MockPackable(ratio));
        }

        PackingResult<Packable> result = Packing.packCircles(500, 500, bubbles, 1000);

        // Check for overlaps
        assertFalse(result.isOverlapsExist());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeRadiusRatio() {
        List<MockPackable> bubbles = new ArrayList<>();
        bubbles.add(new MockPackable(-1.0));

        Packing.packCircles(500, 500, bubbles, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroRectangleDimensions() {
        List<MockPackable> bubbles = new ArrayList<>();
        bubbles.add(new MockPackable(1.0));

        Packing.packCircles(0, 0, bubbles, 1000);
    }

    @Test
    public void testOverlappingAfterOptimization() {
        // Create bubbles that are likely to overlap
        List<MockPackable> bubbles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            bubbles.add(new MockPackable(100)); // Very large bubbles
        }

        PackingResult<Packable> result = Packing.packCircles(500, 500, bubbles, 1000);

        // Since the bubbles are too large, overlaps may exist
        assertTrue(result.isOverlapsExist());
    }

    @Test
    public void testAdjustmentsMade() {
        List<MockPackable> bubbles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            bubbles.add(new MockPackable(1.0));
        }

        PackingResult<Packable> result = Packing.packCircles(500, 500, bubbles, 1000);

        // Ensure adjustments were made during optimization
        assertTrue(result.getAdjustmentsMade() > 0);
    }

    @Test
    public void testTotalOverlapArea() {
        List<MockPackable> bubbles = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            bubbles.add(new MockPackable(200)); // Large bubbles
        }

        PackingResult<Packable> result = Packing.packCircles(500, 500, bubbles, 1000);

        // Total overlap area should be greater than zero
        assertTrue(result.getTotalOverlapArea() > 0);
    }
}
