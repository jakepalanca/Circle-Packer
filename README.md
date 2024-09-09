# **BubbleChart**

## **Project Overview**

This project implements a force-directed bubble chart simulation. Its main purpose is to generate positional and size data for bubbles based on scale ratios using a physics simulation. JavaFX is used as the configuration interface to manage the bubbles, adjust physics parameters, and visualize the current state of the simulation. The simulation uses a force-directed layout to prevent bubble overlap through repulsion and attraction forces.

The main goal of the system is to provide accurate bubble positioning and size data, which can then be exported or used for further processing. JavaFX handles all UI interactions and visualization, but the physics simulation is managed internally by the `BubbleChartSim` class.

---
### **Problems & Known Issues**

Despite the current functionality of the application, there are a few issues that still need to be addressed. Below is a list of known problems, along with brief descriptions of each:

---

### **1. Bubbles Not Correctly Positioned After Addition**

**Description**:  
When new bubbles are added to the chart, they are not always positioned correctly. Instead of being placed at the center of the canvas, they may appear in arbitrary or unintended positions. This issue affects how bubbles interact with each other and their visual arrangement within the canvas.

**Potential Cause**:  
This could be related to incorrect initialization of the bubble's position or improper scaling after the initial placement, especially when dealing with variable chart dimensions and proportional scaling logic.

**Resolution Plan**:
- Investigate the logic responsible for positioning bubbles on the canvas after being added.
- Ensure that the initial position is calculated relative to the center of the canvas, taking into account the current canvas size and scale.

---

### **2. Overlap Detection Not Always Accurate**

**Description**:  
The overlap detection mechanism in the simulation is inconsistent, with some bubbles passing the overlap check even when they are visibly intersecting or engulfed by larger bubbles. This results in failed simulations where bubbles are shown to overlap but are reported as non-overlapping.

**Potential Cause**:  
The issue may stem from the physics calculations related to bubble collision detection and repulsion forces, especially with bubbles of different sizes. The current implementation may not handle edge cases effectively, particularly for bubbles near the edges or those with vastly different radii.

**Resolution Plan**:
- Review the force-directed algorithm in `BubbleChartSim.java` to improve the precision of overlap detection.
- Add more redundancy checks, especially for corner cases where one bubble engulfs another.
- Ensure that the repulsion and attraction forces are correctly adjusted based on the size and distance of bubbles.

---

### **3. Canvas Borders Not Properly Enforced**

**Description**:  
Bubbles are able to go beyond the canvas borders, either partially or completely, which leads to bubbles being rendered off-screen. This breaks the visual integrity of the chart and creates an inaccurate representation of the simulation.

**Potential Cause**:  
The current clamping logic within the `renderBubbles()` method may not be functioning correctly, or the scaling factors for bubble size and position may not account for the canvas boundaries appropriately.

**Resolution Plan**:
- Revisit the `clamp()` logic used in the rendering process to ensure that bubbles stay within the canvas boundaries.
- Confirm that both the X and Y positions of bubbles are checked relative to their radius when determining if they exceed canvas borders.

---

### **4. JavaFX Not Properly Representing the Simulation State**

**Description**:  
The JavaFX interface does not always reflect the actual state of the simulation. This includes cases where the bubbles do not scale correctly, the simulation data (such as repulsion and attraction strengths) are not displayed or updated in real-time, and visual elements are not proportional to the input dimensions.

**Potential Cause**:  
The issue may arise from incorrect scaling logic or an outdated rendering process that doesn't dynamically adapt to changes in simulation data or canvas size. The current implementation might also lack sufficient event listeners to update the canvas and controls in response to real-time changes.

**Resolution Plan**:
- Implement dynamic scaling and re-rendering of the canvas each time bubbles are added, removed, or resized.
- Ensure that all simulation properties (such as repulsion, attraction) update both visually and in the simulation data during interaction with the UI.
- Add listeners to react to changes in dimensions or other properties and trigger immediate re-rendering of the canvas.

---

### **5. Dynamic Features Not Functioning Correctly**

**Description**:  
Certain dynamic features, such as updating physics parameters (repulsion, attraction) and bubble resizing, either do not apply correctly or have no effect on the simulation. For instance, changing the base repulsion strength via the UI does not always update the simulation’s internal state.

**Potential Cause**:  
The binding between the JavaFX UI and the simulation engine might be weak or missing. It’s possible that changes made through the interface are not properly passed on to the `BubbleChartSim` class, meaning the internal physics simulation remains unaffected.

**Resolution Plan**:
- Review the connections between the JavaFX control panel and the `BubbleChartSim` instance to ensure that UI inputs directly influence the simulation.
- Add appropriate logging and debugging to track whether input values are being passed and applied correctly.
- Improve the feedback mechanism in the UI to reflect successful updates to the simulation parameters.

---

### **6. Bubble Sizes Not Properly Scaled Relative to the Canvas**

**Description**:  
When bubbles of varying radius ratios are added, their sizes do not appear to scale correctly relative to the canvas dimensions. Some bubbles may appear much smaller or larger than expected, making the visualization unclear and inconsistent with the physics simulation.

**Potential Cause**:  
This is likely due to improper scaling factors being applied during the rendering process. The relationship between the chart dimensions, bubble radius ratios, and the canvas size needs to be recalibrated to ensure proportional scaling.

**Resolution Plan**:
- Adjust the scaling logic in `renderBubbles()` to ensure that bubble sizes are proportional to both their radius ratios and the canvas dimensions.
- Use the minimum of the horizontal and vertical scaling factors to maintain consistent aspect ratios for the bubbles.

---

### **7. Inconsistent Results for Edge Cases**

**Description**:  
When dealing with edge cases (such as very large or very small radius ratios, or extreme repulsion/attraction values), the simulation may produce unpredictable results. Bubbles may overlap or fail to stabilize within the allowed number of iterations.

**Potential Cause**:  
The current algorithm may not handle extreme values well. For example, very large repulsion strengths might force bubbles off the canvas, while very small attraction strengths may prevent bubbles from interacting meaningfully.

**Resolution Plan**:
- Review the physics algorithm to handle edge cases more gracefully.
- Set upper and lower bounds for certain parameters (e.g., repulsion and attraction strength) to avoid unrealistic or unstable simulation results.
- Add tests specifically for extreme parameter values to ensure consistent results.

---

## **Next Steps**

- Focus on refining the physics simulation to better handle edge cases, overlap detection, and bubble positioning.
- Improve the interaction between JavaFX and the simulation engine to ensure dynamic updates and accurate real-time feedback.
- Implement additional safeguards (e.g., clamping and boundary checks) to ensure the bubbles stay within the canvas and are scaled correctly.

---

## **Core Components**

### **1. BubbleChartApp (JavaFX UI Layer)**

- **Purpose**: Provides a user interface for managing the simulation. This includes the ability to add and remove bubbles, adjust repulsion and attraction strengths, reset the chart dimensions, and simulate physics interactions.

- **Key Features**:
    - **Canvas-based Visualization**: The center panel renders the bubbles based on their calculated positions and sizes.
    - **Left Panel**: Displays the current list of bubbles with their UUID, radius ratios, and size data.
    - **Right Panel (Control Panel)**: Allows users to configure physics properties (repulsion, attraction) and add/remove bubbles.
    - **Reset & Simulate Buttons**: Control the simulation process and reset the canvas size.
    - **Dynamic Resizing**: Bubbles are scaled to fit within the canvas dimensions, ensuring proportional rendering.
    - **Data Export Buttons**:
        - **Bubble Data**: Copies a list of bubble data (UUID, radius ratio, X, Y).
        - **Simulation Data**: Copies the simulation data (repulsion and attraction strengths).

- **Key Files**:
    - `BubbleChartApp.java` - Main JavaFX application for visualization and configuration.
    - `Bubble.java` - Defines individual bubble properties, including size and position.

### **2. BubbleChartSim (Physics Engine Layer)**

- **Purpose**: Handles the actual physics simulation. It computes the positions of bubbles using a force-directed algorithm, ensuring that no two bubbles overlap through repulsion and attraction forces.

- **Key Features**:
    - **Repulsion & Attraction Mechanism**: Dynamically prevents overlap of bubbles by simulating forces between them.
    - **Simulation Loop**: Iterates until the system stabilizes or meets the set iteration limit (1000 iterations).
    - **Bubble Management**: Allows adding and removing bubbles, with each bubble’s size determined by the radius ratio.

- **Key Files**:
    - `BubbleChartSim.java` - Core physics engine for calculating bubble positions and resolving overlaps.
    - `Bubble.java` - Represents individual bubbles used by the simulation, with properties like size, position, and unique identifier (UUID).

---

## **Contributing Guidelines**

### **1. Setting Up the Project**

- Clone the repository.
- Ensure JavaFX is set up correctly in your development environment (IntelliJ, Eclipse, etc.).
- Run the `BubbleChartApp` class to start the JavaFX interface and simulation.

### **2. Understanding the Code Structure**

- **BubbleChartApp.java**: Focuses on the user interface and rendering of bubbles.
- **BubbleChartSim.java**: Handles all the physics logic. Most contributions related to the simulation should focus on this file.
- **Bubble.java**: Represents a single bubble. Make sure to respect the UUID system for tracking bubbles.

### **3. Key Areas for Contribution**

- **Physics Improvements**: If you’re working on optimizing the physics simulation, focus on the `BubbleChartSim` class.
    - Areas for improvement: Better handling of edge cases, optimizing for performance, improving the force calculations.

- **UI Enhancements**: Contributions to the JavaFX configuration should focus on `BubbleChartApp`. Consider adding new features like export to file, advanced bubble controls, etc.

- **Logging & Debugging**: We are logging the simulation states at various points. If you add new functionality, ensure it logs information appropriately (use `Logger`).

### **4. Key Interactions to Consider**

- **Add Bubble**: When a bubble is added via the UI, it should be initialized at the center of the canvas, with a default or user-defined radius ratio.
- **Remove Bubble**: When removing a bubble, ensure the UUID is used to identify and delete it.
- **Simulation Stability**: When running simulations, ensure that the physics simulation calculates the new positions, and render the results in JavaFX without overlapping.

### **5. Testing**

- Test any changes made to the physics simulation thoroughly. Use edge cases such as:
    - Bubbles with vastly different sizes.
    - Bubbles on the edges of the canvas.
    - Large number of bubbles (performance testing).
- For UI changes, verify that the canvas scales correctly and that bubbles don't overlap or exceed canvas bounds.

---

## **Usage Guide**

1. **Adding Bubbles**:
    - Enter a radius ratio in the provided input box on the right.
    - Click **"Add Bubble"** to add a new bubble to the chart.
    - The bubble will be positioned in the center of the canvas.

2. **Adjusting Physics Settings**:
    - Modify the **Repulsion** and **Attraction** fields as needed.
    - Click **"Update Physics"** to apply the new settings.

3. **Simulating Physics**:
    - Click the **"Simulate Physics"** button to run the physics simulation.
    - Check the **Pass/Fail** indicator to verify if bubbles are overlapping.

4. **Exporting Data**:
    - Click **"Copy Bubble Data"** to export a list of bubble UUIDs, positions, and radius ratios.
    - Click **"Copy Simulation Data"** to export the base and end repulsion/attraction strengths.

---

## **Future Plans & Improvements**

- Add more advanced configuration options in the JavaFX UI, like mass, friction, or time steps.
- Provide the ability to save and load simulation states from a file.
- Optimize performance for larger datasets.
- Improve the scaling algorithm for edge cases where bubbles have extreme differences in size.

---

## **Questions & Support**

For any questions or contributions, please reach out to the project maintainer.

--- 
