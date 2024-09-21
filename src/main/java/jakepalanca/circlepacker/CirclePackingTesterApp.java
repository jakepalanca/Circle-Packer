package jakepalanca.circlepacker;

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
 * Main application to test the circle packing functionality using JavaFX.
 * The app allows users to input dimensions for a rectangle, add bubbles (circles),
 * and then optimize their packing using a packing algorithm.
 * The user can also test predefined edge cases for circle packing scenarios.
 */
public class CirclePackingTesterApp extends Application {

    /**
     * Represents a bubble in the UI. Implements the Packable interface to provide
     * necessary attributes for circle packing, such as radius ratio, radius, and coordinates.
     */
    public static class Bubble implements Packable {
        private final UUID id;
        private double radiusRatio;
        private double radius;
        private double x;
        private double y;
        private final Color color;

        /**
         * Constructs a new Bubble with the specified radius ratio and color.
         *
         * @param radiusRatio the ratio of the bubble's radius
         * @param color       the color of the bubble
         */
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

        /**
         * Returns the color of the bubble.
         *
         * @return the color of the bubble
         */
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

    // Details labels for displaying packing results
    private Label computationTimeLabel;
    private Label iterationsLabel;
    private Label overlapsExistLabel;
    private Label totalOverlapAreaLabel;
    private Label adjustmentsMadeLabel;
    private Label chartDimensionsLabel;

    // Chart instance that holds the bubbles
    private Chart chart;

    // Canvas dimensions
    private final double canvasWidth = 500;
    private final double canvasHeight = 500;

    // Maximum number of iterations for the packing algorithm
    private final int maxIterations = 1000;

    /**
     * The main entry point for the application.
     * This method is called by the JavaFX runtime to launch the application.
     *
     * @param primaryStage the primary window for this application
     */
    @Override
    public void start(Stage primaryStage) {
        // Show the initial dimension input window
        showDimensionInputWindow(primaryStage);
    }

    /**
     * Displays a window that prompts the user to input the width and height
     * for the rectangle used in the circle packing algorithm.
     *
     * @param primaryStage the main stage of the application
     */
    private void showDimensionInputWindow(Stage primaryStage) {
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
                    chart = new Chart(inputWidth, inputHeight);
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
        inputBox.getChildren().addAll(new Label("Enter the dimensions of the rectangle:"),
                widthInput, heightInput, continueButton);

        Scene inputScene = new Scene(inputBox);
        primaryStage.setTitle("Set Rectangle Dimensions");
        primaryStage.setScene(inputScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Sets up the main application window, including the canvas for visualizing
     * the packing result, input fields, and control buttons.
     *
     * @param primaryStage the main stage of the application
     */
    private void showMainApplication(Stage primaryStage) {
        initializeComponents();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox controlBox = createControlBox();
        canvas = new Canvas(canvasWidth, canvasHeight);
        canvas.setStyle("-fx-background-color: lightgray;");
        StackPane canvasPane = new StackPane(canvas);
        canvasPane.setPadding(new Insets(25));
        canvasPane.setStyle("-fx-border-color: black;");

        VBox rightBox = createRightBox();

        root.setLeft(controlBox);
        root.setCenter(canvasPane);
        root.setRight(rightBox);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Circle Packing Tester");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.show();

        clearCanvas();
    }

    /**
     * Creates a control box containing input fields for radius ratios,
     * buttons for adding bubbles, optimizing, and resetting the chart.
     *
     * @return the control box as a VBox layout
     */
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

        controlBox.getChildren().addAll(new Label("Add Bubble:"),
                radiusRatioField, addButton, radiusListLabel, radiusListView, optimizeButton, resetButton);

        return controlBox;
    }

    /**
     * Creates the right box containing edge case buttons and details about the
     * packing result (such as computation time, iterations, and overlap).
     *
     * @return the right box as a VBox layout
     */
    private VBox createRightBox() {
        VBox rightBox = new VBox(20);
        rightBox.setPadding(new Insets(0, 10, 0, 10));
        rightBox.setAlignment(Pos.TOP_LEFT);

        Label edgeCasesLabel = new Label("Edge Cases:");
        ScrollPane edgeCasesPane = createEdgeCasesPane();

        Label detailsLabel = new Label("Details:");
        GridPane detailsGrid = createDetailsGrid();

        rightBox.getChildren().addAll(edgeCasesLabel, edgeCasesPane, detailsLabel, detailsGrid);

        return rightBox;
    }

    /**
     * Creates a grid pane to display details about the packing result.
     *
     * @return the grid pane containing the details
     */
    private GridPane createDetailsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 0));

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

    /**
     * Creates a scroll pane containing buttons for different edge cases
     * of circle packing, such as single bubble, many small bubbles, and random ratios.
     *
     * @return the scroll pane containing edge case buttons
     */
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

    /**
     * Defines a list of predefined edge cases for circle packing, such as
     * single bubble, two equal bubbles, many small bubbles, etc.
     *
     * @return a list of edge cases
     */
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

        return edgeCases;
    }

    /**
     * Loads a predefined edge case and automatically optimizes and draws the bubbles.
     *
     * @param edgeCase the edge case to load
     */
    private void loadEdgeCase(EdgeCase edgeCase) {
        chart.getPackables().clear();
        radiusListView.getItems().clear();

        for (Bubble bubble : edgeCase.getBubbles()) {
            chart.addPackable(bubble);
        }
        radiusListView.getItems().addAll(edgeCase.getBubbles());

        optimizeAndDraw();
    }

    /**
     * Optimizes the placement of bubbles in the chart and draws them on the canvas.
     * Displays the result of the packing, such as computation time and overlaps.
     */
    private void optimizeAndDraw() {
        if (chart.getPackables().isEmpty()) {
            showAlert("No bubbles to optimize. Please add bubbles or select an edge case.");
            return;
        }

        PackingResult<Packable> result = null;
        try {
            result = chart.optimize(maxIterations);
        } catch (IllegalArgumentException e) {
            showAlert("Error: " + e.getMessage());
            return;
        }

        clearCanvas();
        drawPackedCircles(result.getPackables());

        updateDetails(result);
    }

    /**
     * Updates the details section with information from the packing result, such as
     * computation time, iterations, and total overlap area.
     *
     * @param result the result of the packing operation
     */
    private void updateDetails(PackingResult<Packable> result) {
        chartDimensionsLabel.setText((int) chart.getWidth() + " x " + (int) chart.getHeight());
        computationTimeLabel.setText(result.getComputationTime() + " ms");
        iterationsLabel.setText(String.valueOf(result.getIterations()));
        overlapsExistLabel.setText(result.isOverlapsExist() ? "Yes" : "No");
        totalOverlapAreaLabel.setText(String.format("%.2f",
                result.getTotalOverlapArea()));
        adjustmentsMadeLabel.setText(String.valueOf(result.getAdjustmentsMade()));
    }

    /**
     * Adds a new bubble with the specified radius ratio and a generated color to the chart.
     * If the input is invalid, an error alert is shown.
     */
    private void addBubble() {
        try {
            double ratio = Double.parseDouble(radiusRatioField.getText().trim());
            if (ratio > 0) {
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

    /**
     * Resets the chart, clears all bubbles and the canvas, and returns to the initial dimension input window.
     */
    private void resetChart() {
        chart.getPackables().clear();
        radiusListView.getItems().clear();
        clearCanvas();
        clearDetails();

        Stage stage = (Stage) canvas.getScene().getWindow();
        showDimensionInputWindow(stage);
    }

    /**
     * Clears the canvas by erasing all drawn bubbles and draws the bounding rectangle of the chart.
     */
    private void clearCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        double scaleX = (canvasWidth - 50) / chart.getWidth();
        double scaleY = (canvasHeight - 50) / chart.getHeight();
        double scale = Math.min(scaleX, scaleY);
        scale = Math.max(scale, 0.1); // Ensure scale is not too small

        double scaledWidth = chart.getWidth() * scale;
        double scaledHeight = chart.getHeight() * scale;
        double offsetX = (canvasWidth - scaledWidth) / 2;
        double offsetY = (canvasHeight - scaledHeight) / 2;

        gc.setStroke(Color.BLACK);
        gc.strokeRect(offsetX, offsetY, scaledWidth, scaledHeight);
    }

    /**
     * Clears the details section, resetting the displayed information about the packing result.
     */
    private void clearDetails() {
        chartDimensionsLabel.setText("");
        computationTimeLabel.setText("");
        iterationsLabel.setText("");
        overlapsExistLabel.setText("");
        totalOverlapAreaLabel.setText("");
        adjustmentsMadeLabel.setText("");
    }

    /**
     * Draws the packed circles on the canvas, scaling their positions and radii according to the canvas dimensions.
     *
     * @param packables the collection of packed bubbles to be drawn
     */
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

            double scaledX = bubble.getX() * scale + offsetX;
            double scaledY = bubble.getY() * scale + offsetY;
            double scaledRadius = bubble.getRadius() * scale;

            gc.setFill(color);
            gc.fillOval(scaledX - scaledRadius, scaledY - scaledRadius, scaledRadius * 2, scaledRadius * 2);

            gc.setStroke(Color.BLACK);
            gc.strokeOval(scaledX - scaledRadius, scaledY - scaledRadius, scaledRadius * 2, scaledRadius * 2);
        }
    }

    /**
     * Generates a color based on the index to differentiate the bubbles.
     * Uses the HSB color model with varying hues.
     *
     * @param index the index of the bubble
     * @return a unique color for the bubble
     */
    private Color generateColor(int index) {
        double hue = (index * 40) % 360; // Change hue every time
        double saturation = 0.7;
        double brightness = 0.6;
        return Color.hsb(hue, saturation, brightness);
    }

    /**
     * Shows an alert dialog with the specified message.
     *
     * @param message the message to be displayed in the alert
     */
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Initializes UI components. Can be used to set up additional event handlers or properties.
     */
    private void initializeComponents() {
        // Implement if needed
    }

    /**
     * The main method to launch the application. Called by the JavaFX runtime.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Represents an edge case for circle packing. An edge case includes a name
     * and a list of bubbles with predefined ratios and colors.
     */
    private static class EdgeCase {
        private final String name;
        private final List<Bubble> bubbles;

        /**
         * Constructs an edge case with the specified name and list of bubbles.
         *
         * @param name    the name of the edge case
         * @param bubbles the list of bubbles for this edge case
         */
        public EdgeCase(String name, List<Bubble> bubbles) {
            this.name = name;
            this.bubbles = bubbles;
        }

        /**
         * Returns the name of the edge case.
         *
         * @return the name of the edge case
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the list of bubbles for this edge case.
         *
         * @return the list of bubbles
         */
        public List<Bubble> getBubbles() {
            return bubbles;
        }
    }

    /**
     * Custom ListCell for displaying a bubble in the ListView. Each ListCell contains
     * the bubble's radius ratio and a delete button to remove it from the chart.
     */
    private class BubbleCell extends ListCell<Bubble> {
        HBox hbox = new HBox(5);
        Button deleteButton = new Button("Delete");
        Label label = new Label();

        /**
         * Constructs a new BubbleCell. The delete button allows the user to remove
         * a bubble from the chart and the ListView.
         */
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
