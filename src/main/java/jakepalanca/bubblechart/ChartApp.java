package jakepalanca.bubblechart;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class ChartApp extends Application {
    private Chart chart;
    private Pane chartPane;
    private ScrollPane scrollPane;
    private double canvasWidth = 500;
    private double canvasHeight = 500;

    @Override
    public void start(Stage primaryStage) {
        chart = new Chart(canvasWidth, canvasHeight);
        openChartWindow();
        openConfigWindow();
    }

    private void openChartWindow() {
        chartPane = new Pane();
        chartPane.setPrefSize(canvasWidth, canvasHeight);
        scrollPane = new ScrollPane(chartPane);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        Scene chartScene = new Scene(scrollPane, canvasWidth, canvasHeight);
        Stage chartStage = new Stage();
        chartStage.setTitle("Bubble Chart");
        chartStage.setScene(chartScene);
        chartStage.show();
    }

    private void openConfigWindow() {
        Pane configPane = new Pane();

        Label radiusRatioLabel = new Label("Radius Ratio:");
        TextField radiusRatioField = new TextField("0.1");
        Button addBubbleButton = new Button("Add Bubble");
        Button removeBubbleButton = new Button("Remove Bubble");

        radiusRatioLabel.setLayoutX(20);
        radiusRatioLabel.setLayoutY(20);
        radiusRatioField.setLayoutX(120);
        radiusRatioField.setLayoutY(20);
        addBubbleButton.setLayoutX(20);
        addBubbleButton.setLayoutY(60);
        removeBubbleButton.setLayoutX(120);
        removeBubbleButton.setLayoutY(60);

        addBubbleButton.setOnAction(e -> {
            double radiusRatio = Double.parseDouble(radiusRatioField.getText());
            if (chart != null) {
                chart.addBubble(radiusRatio);
                updateDisplay();
            }
        });

        removeBubbleButton.setOnAction(e -> {
            if (chart != null) {
                chart.removeBubble();
                updateDisplay();
            }
        });

        configPane.getChildren().addAll(radiusRatioLabel, radiusRatioField, addBubbleButton, removeBubbleButton);
        Scene configScene = new Scene(configPane, 300, 150);
        Stage configStage = new Stage();
        configStage.setTitle("Bubble Chart Configuration");
        configStage.setScene(configScene);
        configStage.show();
    }

    private void updateDisplay() {
        chartPane.getChildren().clear();
        double scaleFactorX = canvasWidth / chart.getWidth();
        double scaleFactorY = canvasHeight / chart.getHeight();
        double scaleFactor = Math.min(scaleFactorX, scaleFactorY);

        for (Bubble bubble : chart.getBubbles()) {
            Circle circle = new Circle(bubble.getX() * scaleFactor, bubble.getY() * scaleFactor, bubble.getRadius() * scaleFactor);
            circle.setFill(Color.BLUE);
            chartPane.getChildren().add(circle);
        }

        chartPane.setMinSize(chart.getWidth() * scaleFactor, chart.getHeight() * scaleFactor);
        chartPane.setMaxSize(chart.getWidth() * scaleFactor, chart.getHeight() * scaleFactor);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
