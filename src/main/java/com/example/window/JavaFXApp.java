package com.example.window;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JavaFXApp extends Application {
    private Timeline timeline;
    private XYChart.Series<String, Number> series;
    private XYChart.Series<String, Number> horizontalLine;
    private XYChart.Series<String, Number> arrowsSeries;
    private final Random random = new Random();

    private final LinkedHashMap<String, XYChart.Data<String, Number>> allData = new LinkedHashMap<>();

    private static final int MAX_VISIBLE_POINTS = 20;
    private double currentHorizontalLineValue = 30;
    private int scrollOffset = 0;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Real-Time Chart");
        BorderPane root = new BorderPane();

        // --- График ---
        NumberAxis yAxis = new NumberAxis();
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Price");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        series = new XYChart.Series<>();
        horizontalLine = new XYChart.Series<>();
        arrowsSeries = new XYChart.Series<>();
        lineChart.getData().addAll(series, horizontalLine, arrowsSeries);
        lineChart.setMaxHeight(500);

        // --- Масштабирование колесиком ---
        lineChart.setOnScroll(event -> {
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            yAxis.setAutoRanging(false);
            yAxis.setUpperBound(yAxis.getUpperBound() * zoomFactor);
            yAxis.setLowerBound(yAxis.getLowerBound() * zoomFactor);
        });

        // --- Прокрутка графика мышью ---
        final double[] dragStartX = {0};
        lineChart.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                dragStartX[0] = event.getX();
            }
        });

        lineChart.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                double deltaX = event.getX() - dragStartX[0];
                if (Math.abs(deltaX) > 5) {
                    scrollOffset += deltaX > 0 ? -1 : 1;
                    updateVisibleData();
                    dragStartX[0] = event.getX();
                }
            }
        });

        // --- Панель кнопок ---
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(10));
        buttonPanel.setStyle("-fx-background-color: lightgray;");

        for (int i = 1; i <= 5; i++) {
            Button button = new Button("Button " + i);
            button.setOnAction(e -> {
                String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                double price = 10 + random.nextInt(50);
                drawArrows(currentTime, price); // Теперь метод существует!
            });
            buttonPanel.getChildren().add(button);
        }

        // --- Слайдер скорости ---
        Slider speedSlider = new Slider(0, 2, 1);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(1);
        speedSlider.setBlockIncrement(0.1);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 0) {
                timeline.stop();
            } else {
                timeline.setRate(newVal.doubleValue());
                timeline.play();
            }
        });

        // --- Панель слайдера и текстовых полей ---
        HBox sliderPanel = new HBox(10);
        sliderPanel.setPadding(new Insets(10));
        sliderPanel.getChildren().addAll(new Label("Speed: "), speedSlider);

        for (int i = 1; i <= 5; i++) {
            TextField textField = new TextField();
            textField.setPrefWidth(80);
            textField.setPromptText("Field " + i);
            sliderPanel.getChildren().add(textField);
        }

        // --- Панель ботов по горизонтали ---
        HBox botsPanel = new HBox(10);
        botsPanel.setPadding(new Insets(10));
        botsPanel.setStyle("-fx-border-color: black; -fx-padding: 5;");

        for (int i = 1; i <= 4; i++) {
            VBox botBox = new VBox(5);
            botBox.setStyle("-fx-border-color: black; -fx-padding: 5;");
            botBox.getChildren().add(new Label("Bot " + i));

            TextField minPrice = new TextField();
            TextField maxPrice = new TextField();
            TextField share = new TextField();
            TextField leverage = new TextField();
            minPrice.setPromptText("Min Price");
            maxPrice.setPromptText("Max Price");
            share.setPromptText("Share");
            leverage.setPromptText("Leverage");

            Label entryPrice = new Label("Entry: -");
            Label liquidationPrice = new Label("Liquidation: -");
            Label takeProfit = new Label("Take Profit: -");
            Label stopLoss = new Label("Stop Loss: -");
            Label currentBalance = new Label("Balance: -");

            botBox.getChildren().addAll(minPrice, maxPrice, share, leverage,
                    entryPrice, liquidationPrice, takeProfit, stopLoss, currentBalance);
            botsPanel.getChildren().add(botBox);
        }

        // --- Центр: график + кнопки + слайдер + боты ---
        VBox centerPanel = new VBox(10);
        centerPanel.getChildren().addAll(lineChart, buttonPanel, sliderPanel, botsPanel);
        root.setCenter(centerPanel);

        // --- Запуск обновления ---
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateChart()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.setScene(new Scene(root, 1920, 1080));
        primaryStage.show();
    }

    private void drawArrows(String time, double price) {
        XYChart.Data<String, Number> redArrow = new XYChart.Data<>(time, price + 2);
        redArrow.setNode(createArrowNode("🔻", "red"));

        XYChart.Data<String, Number> greenArrow = new XYChart.Data<>(time, price - 2);
        greenArrow.setNode(createArrowNode("🔺", "green"));

        arrowsSeries.getData().addAll(redArrow, greenArrow);
    }

    private StackPane createArrowNode(String arrow, String color) {
        Label label = new Label(arrow);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px;");
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(label);
        return stackPane;
    }

    private void updateChart() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        double price = 10 + random.nextInt(50);

        if (allData.containsKey(currentTime)) {
            allData.remove(currentTime);
        }

        XYChart.Data<String, Number> newData = new XYChart.Data<>(currentTime, price);
        allData.put(currentTime, newData);

        updateVisibleData();
    }

    private void updateVisibleData() {
        series.getData().clear();
        horizontalLine.getData().clear();
        arrowsSeries.getData().clear();

        int start = Math.max(0, allData.size() - MAX_VISIBLE_POINTS - scrollOffset);
        int end = Math.min(allData.size(), start + MAX_VISIBLE_POINTS);

        List<String> keys = new ArrayList<>(allData.keySet());

        for (int i = start; i < end; i++) {
            String time = keys.get(i);
            XYChart.Data<String, Number> data = allData.get(time);
            series.getData().add(new XYChart.Data<>(time, data.getYValue()));
            horizontalLine.getData().add(new XYChart.Data<>(time, currentHorizontalLineValue));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
