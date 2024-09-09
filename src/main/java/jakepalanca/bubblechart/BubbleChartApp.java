package jakepalanca.bubblechart;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Text;

import javax.swing.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code BubbleChartApp} class is a JavaFX application that visualizes a force-directed bubble chart.
 */
public class BubbleChartApp extends Application {

    /** Logger for recording application events */
    private static final Logger LOGGER = Logger.getLogger(BubbleChartApp.class.getName());

    /** Simulation of the force-directed bubble chart */
    private BubbleChartSim bubbleChart;

    /** Pane used to display bubbles on the canvas */
    private Pane canvasPane;

    /** Initial width of the canvas */
    private double canvasWidth = 800;

    /** Initial height of the canvas */
    private double canvasHeight = 600;

    /** VBox to display the list of bubbles */
    private VBox bubbleListBox;

    /** Text indicator to show overlap status (Pass/Fail) */
    private Text overlapIndicator;

    /** Label to display non-editable chart dimensions */
    private Label chartDimensionsLabel;

    /** Label to display the dynamically updated end repulsion value */
    private Label endRepulsionLabel;

    /** Label to display the dynamically updated end attraction value */
    private Label endAttractionLabel;

    private Canvas canvas;

    @Override
    public void start(Stage primaryStage) {
        // Initialize the BubbleChartSim with default values
        bubbleChart = new BubbleChartSim(canvasWidth, canvasHeight);

        LOGGER.info("Initializing the ForceDirectedBubbleChart");

        LOGGER.info("Setting up the main layout");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Initialize bubbleListBox
        bubbleListBox = new VBox(10);
        bubbleListBox.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(bubbleListBox);
        scrollPane.setPrefWidth(200); // Set fixed width for bubble list
        root.setLeft(scrollPane);

        // Initialize canvasPane
        canvasPane = new Pane();
        canvas = new Canvas(canvasWidth, canvasHeight);
        canvasPane.getChildren().add(canvas);
        canvasPane.setStyle("-fx-background-color: #F0F0F0; -fx-border-color: black; -fx-border-width: 2;");
        root.setCenter(canvasPane);

        // Add the control panel to the right side
        root.setRight(createControlPanel());

        // Set up the scene
        Scene scene = new Scene(root, 1200, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Force-Directed Bubble Chart");
        primaryStage.show();

        // Handle window resizing to adjust the canvas
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            renderBubbles(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight());
        });

        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            renderBubbles(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight());
        });

        // Prompt for dimensions after the UI is initialized
        promptForDimensions();
        updateChartDimensionsInfo(); // Update chart dimensions after user input
    }

    private VBox createControlPanel() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10));

        // Initialize chartDimensionsLabel
        chartDimensionsLabel = new Label();
        chartDimensionsLabel.setStyle("-fx-border-color: black; -fx-padding: 5;");
        updateChartDimensionsInfo();

        // Input for radius ratio
        TextField radiusRatioInput = new TextField();
        radiusRatioInput.setPromptText("Radius Ratio (Default: 1.0)");

        Button addButton = new Button("Add Bubble");
        addButton.setOnAction(e -> {
            try {
                double radiusRatio = radiusRatioInput.getText().isEmpty() ? 1.0 : Double.parseDouble(radiusRatioInput.getText());
                if (radiusRatio <= 0) throw new NumberFormatException("Radius ratio must be positive.");

                // Add bubble at the center of the canvas
                double centerX = canvasWidth / 2;
                double centerY = canvasHeight / 2;
                Bubble bubble = new Bubble(centerX, centerY, radiusRatio);
                bubbleChart.addBubble(bubble);

                refreshBubbleList(); // Refresh the left panel with bubble list
                renderBubbles(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight());
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid Input", "Please enter a valid positive number for the radius ratio.");
            }
        });

        // Physics settings and simulate button (similar to before)
        TextField repulsionInput = new TextField(String.valueOf(bubbleChart.getBaseRepulsionStrength()));
        TextField attractionInput = new TextField(String.valueOf(bubbleChart.getAttractionStrength()));

        Label physicsLabel = new Label("Physics Settings:");
        Label repulsionLabel = new Label("Repulsion Strength (Starting):");
        Label attractionLabel = new Label("Attraction Strength (Starting):");

        endRepulsionLabel = new Label("End Repulsion: N/A");
        endAttractionLabel = new Label("End Attraction: N/A");

        Button updatePhysicsButton = new Button("Update Physics");
        updatePhysicsButton.setOnAction(e -> {
            try {
                double newRepulsionStrength = Double.parseDouble(repulsionInput.getText());
                double newAttractionStrength = Double.parseDouble(attractionInput.getText());

                bubbleChart.setBaseRepulsionStrength(newRepulsionStrength);
                bubbleChart.setAttractionStrength(newAttractionStrength);

                LOGGER.info("Physics updated: Repulsion=" + newRepulsionStrength + ", Attraction=" + newAttractionStrength);
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid Input", "Please enter valid numbers for repulsion and attraction strengths.");
            }
        });

        Button simulateButton = new Button("Simulate Physics");
        overlapIndicator = new Text();
        simulateButton.setOnAction(e -> {
            bubbleChart.simulateUntilStable();
            renderBubbles(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight());

            boolean hasOverlap = bubbleChart.checkForOverlap();
            overlapIndicator.setText(hasOverlap ? "Fail: Bubbles are intersecting or engulfed." : "Pass: No bubbles are overlapping.");
            overlapIndicator.setFill(hasOverlap ? Color.RED : Color.GREEN);

            endRepulsionLabel.setText("End Repulsion: " + bubbleChart.getEndRepulsionStrength());
            endAttractionLabel.setText("End Attraction: " + bubbleChart.getEndAttractionStrength());
        });

        Button resetChartButton = new Button("Reset Chart");
        resetChartButton.setOnAction(e -> promptForDimensions());

        // New buttons for bubble data and simulation data
        Button bubbleDataButton = new Button("Show Bubble Data");
        bubbleDataButton.setOnAction(e -> showBubbleData());

        Button simDataButton = new Button("Show Simulation Data");
        simDataButton.setOnAction(e -> showSimData());

        // Add components to the control panel
        controls.getChildren().addAll(chartDimensionsLabel, radiusRatioInput, addButton, physicsLabel, repulsionLabel, repulsionInput,
                attractionLabel, attractionInput, updatePhysicsButton, endRepulsionLabel, endAttractionLabel, simulateButton,
                overlapIndicator, resetChartButton, bubbleDataButton, simDataButton);

        return controls;
    }

    private void showBubbleData() {
        StringBuilder bubbleData = new StringBuilder();
        for (Bubble bubble : bubbleChart.getBubbles()) {
            bubbleData.append("UUID: ").append(bubble.getUuid())
                    .append(", Radius Ratio: ").append(bubble.getSize())
                    .append(", X: ").append(bubble.getX())
                    .append(", Y: ").append(bubble.getY())
                    .append("\n");
        }

        TextArea textArea = new TextArea(bubbleData.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        showDataDialog("Bubble Data", textArea);
    }

    private void showSimData() {
        String simData = "Base Repulsion Strength: " + bubbleChart.getBaseRepulsionStrength() + "\n"
                + "End Repulsion Strength: " + bubbleChart.getEndRepulsionStrength() + "\n"
                + "Base Attraction Strength: " + bubbleChart.getAttractionStrength() + "\n"
                + "End Attraction Strength: " + bubbleChart.getEndAttractionStrength();

        TextArea textArea = new TextArea(simData);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        showDataDialog("Simulation Data", textArea);
    }

    private void showDataDialog(String title, TextArea content) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void renderBubbles(GraphicsContext gc, double canvasWidth, double canvasHeight) {
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        // We assume the canvasWidth and canvasHeight are set and used for scaling
        double scale = Math.min(canvasWidth / canvasWidth, canvasHeight / canvasHeight); // Adjust scaling relative to the current canvas size

        for (Bubble bubble : bubbleChart.getBubbles()) {
            // Use the bubble's position and radius directly and apply the scale factor
            double bubbleX = bubble.getX() * scale;
            double bubbleY = bubble.getY() * scale;
            double bubbleRadius = bubble.getRadius() * scale;

            // Ensure the bubble doesn't go past the canvas borders
            bubbleX = clamp(bubbleX, bubbleRadius, canvasWidth - bubbleRadius);
            bubbleY = clamp(bubbleY, bubbleRadius, canvasHeight - bubbleRadius);

            // Render all bubbles in blue as requested
            gc.setFill(Color.BLUE);
            gc.fillOval(bubbleX - bubbleRadius, bubbleY - bubbleRadius, bubbleRadius * 2, bubbleRadius * 2);
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private void updateChartDimensionsInfo() {
        chartDimensionsLabel.setText("Chart Dimensions: " + (int) canvasWidth + " x " + (int) canvasHeight);
    }

    private void promptForDimensions() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Set Chart Dimensions");

        TextField widthField = new TextField(String.valueOf((int) canvasWidth));
        TextField heightField = new TextField(String.valueOf((int) canvasHeight));

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(new Label("Width:"), widthField, new Label("Height:"), heightField);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int newWidth = Integer.parseInt(widthField.getText());
                int newHeight = Integer.parseInt(heightField.getText());
                if (newWidth <= 0 || newHeight <= 0) throw new NumberFormatException();

                canvasWidth = newWidth;
                canvasHeight = newHeight;
                canvas.setWidth(canvasWidth);
                canvas.setHeight(canvasHeight);
                bubbleChart.setDimensions(canvasWidth, canvasHeight);
                bubbleChart.getBubbles().clear();
                refreshBubbleList();
                renderBubbles(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight());
                updateChartDimensionsInfo();
                LOGGER.info("Chart dimensions updated to width=" + canvasWidth + " and height=" + canvasHeight);
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid Input", "Please enter valid positive integers for width and height.");
                promptForDimensions();
            }
        }
    }

    private void refreshBubbleList() {
        bubbleListBox.getChildren().clear();

        for (Bubble bubble : bubbleChart.getBubbles()) {
            HBox bubbleItem = new HBox(10);
            Button removeButton = new Button("X");
            removeButton.setOnAction(e -> {
                bubbleChart.removeBubble(bubble.getUuid());
                refreshBubbleList();
                renderBubbles(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight());
            });

            Label bubbleLabel = new Label("UUID: " + bubble.getUuid() + " | Radius Ratio: " + bubble.getSize());
            bubbleItem.getChildren().addAll(removeButton, bubbleLabel);
            bubbleListBox.getChildren().add(bubbleItem);
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
