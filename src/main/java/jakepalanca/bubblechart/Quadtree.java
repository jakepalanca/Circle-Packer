package jakepalanca.bubblechart;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Quadtree for efficiently partitioning 2D space and managing proximity checks.
 */
public class Quadtree {

    private int MAX_OBJECTS = 10;    // Max bubbles in a quadtree node before splitting
    private int MAX_LEVELS = 5;      // Max depth of the quadtree

    private int level;
    private List<Bubble> objects;
    private Rectangle2D bounds;
    private Quadtree[] nodes;

    public Quadtree(int level, Rectangle2D bounds) {
        this.level = level;
        this.objects = new ArrayList<>();
        this.bounds = bounds;
        this.nodes = new Quadtree[4];  // 4 children nodes
    }

    /**
     * Clears the quadtree, removes all objects.
     */
    public void clear() {
        objects.clear();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                nodes[i].clear();
                nodes[i] = null;
            }
        }
    }

    /**
     * Splits the node into 4 subnodes.
     */
    private void split() {
        double subWidth = bounds.getWidth() / 2;
        double subHeight = bounds.getHeight() / 2;
        double x = bounds.getX();
        double y = bounds.getY();

        nodes[0] = new Quadtree(level + 1, new Rectangle2D.Double(x + subWidth, y, subWidth, subHeight));
        nodes[1] = new Quadtree(level + 1, new Rectangle2D.Double(x, y, subWidth, subHeight));
        nodes[2] = new Quadtree(level + 1, new Rectangle2D.Double(x, y + subHeight, subWidth, subHeight));
        nodes[3] = new Quadtree(level + 1, new Rectangle2D.Double(x + subWidth, y + subHeight, subWidth, subHeight));
    }

    /**
     * Returns the index of the subnode that the object should be inserted into.
     */
    private int getIndex(Bubble bubble) {
        int index = -1;
        double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
        double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);

        boolean topQuadrant = (bubble.getY() < horizontalMidpoint && bubble.getY() + bubble.getRadius() < horizontalMidpoint);
        boolean bottomQuadrant = (bubble.getY() > horizontalMidpoint);

        if (bubble.getX() < verticalMidpoint && bubble.getX() + bubble.getRadius() < verticalMidpoint) {
            if (topQuadrant) {
                index = 1;
            } else if (bottomQuadrant) {
                index = 2;
            }
        } else if (bubble.getX() > verticalMidpoint) {
            if (topQuadrant) {
                index = 0;
            } else if (bottomQuadrant) {
                index = 3;
            }
        }

        return index;
    }

    /**
     * Inserts a bubble into the quadtree.
     */
    public void insert(Bubble bubble) {
        if (nodes[0] != null) {
            int index = getIndex(bubble);
            if (index != -1) {
                nodes[index].insert(bubble);
                return;
            }
        }

        objects.add(bubble);

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) {
                split();
            }

            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                } else {
                    i++;
                }
            }
        }
    }

    /**
     * Retrieves all objects that could collide with the given object.
     */
    public List<Bubble> retrieve(List<Bubble> returnObjects, Bubble bubble) {
        int index = getIndex(bubble);
        if (index != -1 && nodes[0] != null) {
            nodes[index].retrieve(returnObjects, bubble);
        }

        returnObjects.addAll(objects);

        return returnObjects;
    }
}
