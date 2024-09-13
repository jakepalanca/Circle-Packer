package jakepalanca.bubblechart;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Main application to test circle packing functionality using JavaFX.
 * This is meant to test the CirclePacking algorithm with a GUI.
 */
public class CirclePackingTesterApp extends Application {

    /**
     * Class to represent each bubble in the UI with radius ratio, color, and coordinates.
     */
    public static class Bubble implements Packable {
        private UUID id;
        private double radiusRatio;
        private double radius;
        private double x;
        private double y;
        private final Color color;

        public Bubble(double radiusRatio, Color color) {
            this.id = UUID.randomUUID();
            this.radiusRatio = radiusRatio;
            this.color = color;
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

        public Color getColor() {
            return color;
        }

        @Override
        public String toString() {
            return "Radius Ratio: " + radiusRatio;
        }
    }

    // UI Components
    private ListView<Bubble> radiusListView;
    private Canvas canvas;
    private TextField radiusRatioField;
    private Button addButton;
    private Button optimizeButton;
    private Button resetButton;

    // Details labels
    private Label computationTimeLabel;
    private Label iterationsLabel;
    private Label overlapsExistLabel;
    private Label totalOverlapAreaLabel;
    private Label adjustmentsMadeLabel;
    private Label chartDimensionsLabel;

    // Chart instance
    private Chart chart;

    // Canvas dimensions
    private final double canvasWidth = 500;
    private final double canvasHeight = 500;

    // Maximum number of iterations for the packing algorithm
    private final int maxIterations = 1000;

    @Override
    public void start(Stage primaryStage) {
        // Show the initial dimension input window
        showDimensionInputWindow(primaryStage);
    }

    // Display the initial window to input dimensions
    private void showDimensionInputWindow(Stage primaryStage) {
        // Create TextFields for width and height
        TextField widthInput = new TextField();
        widthInput.setPromptText("Width (min 100)");

        TextField heightInput = new TextField();
        heightInput.setPromptText("Height (min 100)");

        Button continueButton = new Button("Continue");
        continueButton.setOnAction(e -> {
            try {
                double inputWidth = Double.parseDouble(widthInput.getText().trim());
                double inputHeight = Double.parseDouble(heightInput.getText().trim());
                if (inputWidth >= 100 && inputHeight >= 100) {
                    // Initialize the chart
                    chart = new Chart(inputWidth, inputHeight);
                    // Proceed to main application
                    showMainApplication(primaryStage);
                } else {
                    showAlert("Width and height must be at least 100.");
                }
            } catch (NumberFormatException ex) {
                showAlert("Please enter valid numeric values for width and height.");
            }
        });

        VBox inputBox = new VBox(10);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(20));
        inputBox.getChildren().addAll(
                new Label("Enter the dimensions of the rectangle:"),
                widthInput,
                heightInput,
                continueButton
        );

        Scene inputScene = new Scene(inputBox);
        primaryStage.setTitle("Set Rectangle Dimensions");
        primaryStage.setScene(inputScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // Set up the main application window
    private void showMainApplication(Stage primaryStage) {
        // Initialize UI components
        initializeComponents();

        // Set up the layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Left: Radius ratio input and list
        VBox controlBox = createControlBox();

        // Center: Canvas for drawing
        canvas = new Canvas(canvasWidth, canvasHeight);
        canvas.setStyle("-fx-background-color: lightgray;");
        StackPane canvasPane = new StackPane(canvas);
        canvasPane.setPadding(new Insets(25)); // Add padding around canvas
        canvasPane.setStyle("-fx-border-color: black;");

        // Right: Edge cases buttons and details
        VBox rightBox = createRightBox();

        // Add components to the root layout
        root.setLeft(controlBox);
        root.setCenter(canvasPane);
        root.setRight(rightBox);

        // Create and set the scene
        Scene scene = new Scene(root);
        primaryStage.setTitle("Circle Packing Tester");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.show();

        // Clear the canvas
        clearCanvas();
    }

    // Create the right box with edge cases and details
    private VBox createRightBox() {
        VBox rightBox = new VBox(20);
        rightBox.setPadding(new Insets(0, 10, 0, 10)); // Added padding
        rightBox.setAlignment(Pos.TOP_LEFT);

        Label edgeCasesLabel = new Label("Edge Cases:");
        ScrollPane edgeCasesPane = createEdgeCasesPane();

        Label detailsLabel = new Label("Details:");
        GridPane detailsGrid = createDetailsGrid();

        rightBox.getChildren().addAll(edgeCasesLabel, edgeCasesPane, detailsLabel, detailsGrid);

        return rightBox;
    }

    // Create the details grid
    private GridPane createDetailsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 0)); // Added padding

        int row = 0;

        grid.add(new Label("Chart Dimensions:"), 0, row);
        chartDimensionsLabel = new Label();
        grid.add(chartDimensionsLabel, 1, row++);

        grid.add(new Label("Computation Time:"), 0, row);
        computationTimeLabel = new Label();
        grid.add(computationTimeLabel, 1, row++);

        grid.add(new Label("Iterations:"), 0, row);
        iterationsLabel = new Label();
        grid.add(iterationsLabel, 1, row++);

        grid.add(new Label("Overlaps Exist:"), 0, row);
        overlapsExistLabel = new Label();
        grid.add(overlapsExistLabel, 1, row++);

        grid.add(new Label("Total Overlap Area:"), 0, row);
        totalOverlapAreaLabel = new Label();
        grid.add(totalOverlapAreaLabel, 1, row++);

        grid.add(new Label("Adjustments Made:"), 0, row);
        adjustmentsMadeLabel = new Label();
        grid.add(adjustmentsMadeLabel, 1, row++);

        return grid;
    }

    // Create the control box on the left side
    private VBox createControlBox() {
        VBox controlBox = new VBox(10);
        controlBox.setPadding(new Insets(0, 10, 0, 0));
        controlBox.setAlignment(Pos.TOP_LEFT);

        Label radiusListLabel = new Label("Radius Ratios:");
        radiusListView = new ListView<>();
        radiusListView.setPrefWidth(200);
        radiusListView.setPlaceholder(new Label("No bubbles added"));
        radiusListView.setCellFactory(param -> new BubbleCell());

        radiusRatioField = new TextField();
        radiusRatioField.setPromptText("Enter radius ratio (e.g., 0.5)");

        addButton = new Button("Add Bubble");
        addButton.setOnAction(e -> addBubble());

        optimizeButton = new Button("Optimize & Draw");
        optimizeButton.setOnAction(e -> optimizeAndDraw());

        resetButton = new Button("Reset Chart");
        resetButton.setOnAction(e -> resetChart());

        controlBox.getChildren().addAll(
                new Label("Add Bubble:"),
                radiusRatioField,
                addButton,
                radiusListLabel,
                radiusListView,
                optimizeButton,
                resetButton
        );

        return controlBox;
    }

    // Create the edge cases pane with buttons
    private ScrollPane createEdgeCasesPane() {
        VBox edgeCasesBox = new VBox(10);
        edgeCasesBox.setPadding(new Insets(5));
        edgeCasesBox.setAlignment(Pos.TOP_LEFT);

        // Define edge cases
        List<EdgeCase> edgeCases = createEdgeCases();

        for (EdgeCase edgeCase : edgeCases) {
            Button button = new Button(edgeCase.getName());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> loadEdgeCase(edgeCase));
            edgeCasesBox.getChildren().add(button);
        }

        ScrollPane scrollPane = new ScrollPane(edgeCasesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200); // Adjust as needed

        return scrollPane;
    }

    // Define edge cases
    private List<EdgeCase> createEdgeCases() {
        List<EdgeCase> edgeCases = new ArrayList<>();

        // Edge case 1: Single Bubble
        edgeCases.add(new EdgeCase("Single Bubble", List.of(
                new Bubble(1.0, Color.BLUE)
        )));

        // Edge case 2: Two Equal Bubbles
        edgeCases.add(new EdgeCase("Two Equal Bubbles", List.of(
                new Bubble(1.0, Color.RED),
                new Bubble(1.0, Color.GREEN)
        )));

        // Edge case 3: Many Small Bubbles
        List<Bubble> smallBubbles = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            smallBubbles.add(new Bubble(0.1, generateColor(i)));
        }
        edgeCases.add(new EdgeCase("Many Small Bubbles", smallBubbles));

        // Edge case 4: Large and Small Bubbles
        edgeCases.add(new EdgeCase("Large and Small Bubbles", List.of(
                new Bubble(1.0, Color.ORANGE),
                new Bubble(0.5, Color.PURPLE),
                new Bubble(0.2, Color.BROWN)
        )));

        // Edge case 5: Identical Bubbles (50 bubbles of ratio 1.0)
        List<Bubble> identicalBubbles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            identicalBubbles.add(new Bubble(1.0, generateColor(i)));
        }
        edgeCases.add(new EdgeCase("50 Identical Bubbles", identicalBubbles));

        // Edge case 6: Random Ratios (20 bubbles with random ratios)
        List<Bubble> randomBubbles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            double ratio = 0.1 + Math.random() * 0.9; // Ratios between 0.1 and 1.0
            randomBubbles.add(new Bubble(ratio, generateColor(i)));
        }
        edgeCases.add(new EdgeCase("Random Ratios", randomBubbles));

        // Add more edge cases as needed

        return edgeCases;
    }

    // Load an edge case
    private void loadEdgeCase(EdgeCase edgeCase) {
        // Clear previous bubbles
        chart.getPackables().clear();
        radiusListView.getItems().clear();

        // Add edge case bubbles to the list
        for (Bubble bubble : edgeCase.getBubbles()) {
            chart.addPackable(bubble);
        }
        radiusListView.getItems().addAll(edgeCase.getBubbles());

        // Automatically optimize and draw the bubbles
        optimizeAndDraw();
    }

    // Optimize and draw the circles
    private void optimizeAndDraw() {
        if (chart.getPackables().isEmpty()) {
            showAlert("No bubbles to optimize. Please add bubbles or select an edge case.");
            return;
        }

        // Perform the circle packing
        PackingResult<Packable> result = null;
        try {
            result = chart.optimize(maxIterations);
        } catch (IllegalArgumentException e) {
            showAlert("Error: " + e.getMessage());
            return;
        }

        // Draw the packed circles
        clearCanvas();
        drawPackedCircles(result.getPackables());

        // Update details
        updateDetails(result);
    }

    // Update the details interface
    private void updateDetails(PackingResult<Packable> result) {
        chartDimensionsLabel.setText((int) chart.getWidth() + " x " + (int) chart.getHeight());
        computationTimeLabel.setText(result.getComputationTime() + " ms");
        iterationsLabel.setText(String.valueOf(result.getIterations()));
        overlapsExistLabel.setText(result.isOverlapsExist() ? "Yes" : "No");
        totalOverlapAreaLabel.setText(String.format("%.2f",
                result.getTotalOverlapArea()));
        adjustmentsMadeLabel.setText(String.valueOf(result.getAdjustmentsMade()));
    }

    // Add a bubble to the list
    private void addBubble() {
        try {
            double ratio = Double.parseDouble(radiusRatioField.getText().trim());
            if (ratio > 0) {
                // Generate a color
                Color color = generateColor(radiusListView.getItems().size());
                Bubble bubble = new Bubble(ratio, color);
                chart.addPackable(bubble);
                radiusListView.getItems().add(bubble);
                radiusRatioField.clear();
            } else {
                showAlert("Radius ratio must be a positive number.");
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid input. Please enter a numeric value for radius ratio.");
        }
    }

    // Reset the chart and clear all data
    private void resetChart() {
        chart.getPackables().clear();
        radiusListView.getItems().clear();
        clearCanvas();
        clearDetails();

        // Go back to the dimension input window
        Stage stage = (Stage) canvas.getScene().getWindow();
        showDimensionInputWindow(stage);
    }

    // Clear the canvas
    private void clearCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        // Calculate scale and offsets
        double scaleX = (canvasWidth - 50) / chart.getWidth();
        double scaleY = (canvasHeight - 50) / chart.getHeight();
        double scale = Math.min(scaleX, scaleY);
        scale = Math.max(scale, 0.1); // Ensure scale is not too small

        double scaledWidth = chart.getWidth() * scale;
        double scaledHeight = chart.getHeight() * scale;
        double offsetX = (canvasWidth - scaledWidth) / 2;
        double offsetY = (canvasHeight - scaledHeight) / 2;

        // Draw the rectangle (representing the bounding box)
        gc.setStroke(Color.BLACK);
        gc.strokeRect(offsetX, offsetY, scaledWidth, scaledHeight);
    }

    // Clear details
    private void clearDetails() {
        chartDimensionsLabel.setText("");
        computationTimeLabel.setText("");
        iterationsLabel.setText("");
        overlapsExistLabel.setText("");
        totalOverlapAreaLabel.setText("");
        adjustmentsMadeLabel.setText("");
    }

    // Draw the packed circles on the canvas
    private void drawPackedCircles(Collection<Packable> packables) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double scaleX = (canvasWidth - 50) / chart.getWidth();
        double scaleY = (canvasHeight - 50) / chart.getHeight();
        double scale = Math.min(scaleX, scaleY);
        scale = Math.max(scale, 0.1); // Ensure scale is not too small

        double scaledWidth = chart.getWidth() * scale;
        double scaledHeight = chart.getHeight() * scale;
        double offsetX = (canvasWidth - scaledWidth) / 2;
        double offsetY = (canvasHeight - scaledHeight) / 2;

        for (Packable p : packables) {
            Bubble bubble = (Bubble) p;
            Color color = bubble.getColor();

            // Scale and offset circle positions and radius
            double scaledX = bubble.getX() * scale + offsetX;
            double scaledY = bubble.getY() * scale + offsetY;
            double scaledRadius = bubble.getRadius() * scale;

            gc.setFill(color);
            gc.fillOval(scaledX - scaledRadius,
                    scaledY - scaledRadius,
                    scaledRadius * 2, scaledRadius * 2);

            gc.setStroke(Color.BLACK); // Draw circle outline
            gc.strokeOval(scaledX - scaledRadius,
                    scaledY - scaledRadius,
                    scaledRadius * 2, scaledRadius * 2);
        }
    }

    // Generate differentiable colors
    private Color generateColor(int index) {
        // Use HSB color model, vary hue
        double hue = (index * 40) % 360; // Change hue every time
        double saturation = 0.7;
        double brightness = 0.6;
        return Color.hsb(hue, saturation, brightness);
    }

    // Show an alert dialog with a message
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR,
                message, ButtonType.OK);
        alert.showAndWait();
    }

    // Initialize UI components
    private void initializeComponents() {
        // Implement if needed
    }

    // Main method to launch the application
    public static void main(String[] args) {
        launch(args);
    }

    // EdgeCase class to hold predefined bubble lists
    private static class EdgeCase {
        private final String name;
        private final List<Bubble> bubbles;

        public EdgeCase(String name, List<Bubble> bubbles) {
            this.name = name;
            this.bubbles = bubbles;
        }

        public String getName() {
            return name;
        }

        public List<Bubble> getBubbles() {
            return bubbles;
        }
    }

    // Custom ListCell for the ListView to include a delete button and color matching
    private class BubbleCell extends ListCell<Bubble> {
        HBox hbox = new HBox(5);
        Button deleteButton = new Button("Delete");
        Label label = new Label();

        public BubbleCell() {
            super();
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.getChildren().addAll(deleteButton, label);

            deleteButton.setOnAction(e -> {
                Bubble bubble = getItem();
                chart.removePackable(bubble.getId());
                getListView().getItems().remove(bubble);
            });
        }

        @Override
        protected void updateItem(Bubble bubble, boolean empty) {
            super.updateItem(bubble, empty);
            if (empty || bubble == null) {
                setText(null);
                setGraphic(null);
            } else {
                label.setText("Radius Ratio: " + bubble.getRadiusRatio());
                label.setTextFill(bubble.getColor());
                setGraphic(hbox);
            }
        }
    }
}
