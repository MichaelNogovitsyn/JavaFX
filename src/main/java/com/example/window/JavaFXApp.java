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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class JavaFXApp extends Application {
    private Timeline timeline;
    private XYChart.Series<String, Number> series;
    private XYChart.Series<String, Number> horizontalLine;
    private XYChart.Series<String, Number> arrowsSeries;
    private final Random random = new Random();
    private final Queue<String> timeLabels = new LinkedList<>();
    private static final int MAX_DATA_POINTS = 20;
    private double currentHorizontalLineValue = 30;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Real-Time Chart");
        BorderPane root = new BorderPane();

        // Панель ботов слева
        VBox botsPanel = new VBox(10);
        botsPanel.setPrefWidth(140);
        botsPanel.setPrefHeight(600); // Ограничение высоты
        botsPanel.getChildren().add(new Label("Bots"));
        for (int i = 1; i <= 4; i++) {
            VBox botBox = new VBox(5);
            botBox.setStyle("-fx-border-color: black; -fx-padding: 5;");
            botBox.getChildren().add(new Label("Bot " + i));

            TextField minPrice = new TextField();
            minPrice.setMinHeight(30);
            minPrice.setPromptText("Min Price");
            TextField maxPrice = new TextField();
            maxPrice.setMinHeight(30);
            maxPrice.setPromptText("Max Price");
            TextField share = new TextField();
            share.setMinHeight(30);
            share.setPromptText("Share");
            TextField leverage = new TextField();
            leverage.setMinHeight(30);
            leverage.setPromptText("Leverage");

            Label entryPrice = new Label("Entry Price: -");
            Label liquidationPrice = new Label("Liquidation Price: -");
            Label takeProfit = new Label("Take Profit: -");
            Label stopLoss = new Label("Stop Loss: -");
            Label currentBalance = new Label("Balance: -");

            botBox.getChildren().addAll(minPrice, maxPrice, share, leverage,
                    entryPrice, liquidationPrice, takeProfit, stopLoss, currentBalance);
            botsPanel.getChildren().add(botBox);
        }
        root.setLeft(botsPanel);

        // Слайдер для регулировки скорости обновления графика
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

        // График в центре
        NumberAxis yAxis = new NumberAxis();
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Price");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        series = new XYChart.Series<>();
        horizontalLine = new XYChart.Series<>();
        arrowsSeries = new XYChart.Series<>();
        lineChart.getData().addAll(series, horizontalLine, arrowsSeries);
        lineChart.setMaxHeight(650); // Ограничение высоты графика

        // Масштабирование графика при прокрутке после клика
        lineChart.setOnMouseClicked(e -> lineChart.requestFocus());
        lineChart.setOnScroll(event -> {
            if (lineChart.isFocused()) {
                double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
                yAxis.setAutoRanging(false);
                yAxis.setUpperBound(yAxis.getUpperBound() * zoomFactor);
                yAxis.setLowerBound(yAxis.getLowerBound() * zoomFactor);
            }
        });

        // Панель с кнопками (размещена под графиком)
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(10));
        buttonPanel.setStyle("-fx-background-color: lightgray;");

        for (int i = 1; i <= 5; i++) {
            Button button = new Button("Button " + i);
            int index = i;
            button.setOnAction(e -> {
                String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                double price = 10 + random.nextInt(50);
                drawArrows(currentTime, price);
                System.out.println("Button " + index + " clicked: Arrows added at " + price);
            });
            buttonPanel.getChildren().add(button);
        }

        // Центр: график + кнопки
        VBox centerPanel = new VBox(10);
        centerPanel.getChildren().addAll(lineChart, buttonPanel);
        root.setCenter(centerPanel);

        // Правая панель со слайдером
        VBox rightPanel = new VBox(10);
        rightPanel.setPrefHeight(600);
        rightPanel.getChildren().addAll(speedSlider, new Label("Speed Control"));
        root.setRight(rightPanel);

        // Запуск обновления графика
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateChart()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.setScene(new Scene(root, 1920, 1080));
        primaryStage.show();
    }

    private void updateChart() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        double price = 10 + random.nextInt(50);

        series.getData().add(new XYChart.Data<>(currentTime, price));
        horizontalLine.getData().add(new XYChart.Data<>(currentTime, currentHorizontalLineValue));
        arrowsSeries.getData().add(new XYChart.Data<>(currentTime, price)); // Добавляем стрелки

        timeLabels.add(currentTime);

        // Удаляем старые точки только если их количество превышает лимит
        if (series.getData().size() > MAX_DATA_POINTS) {
            if (!series.getData().isEmpty()) series.getData().remove(0);
            if (!horizontalLine.getData().isEmpty()) horizontalLine.getData().remove(0);
            if (!arrowsSeries.getData().isEmpty()) arrowsSeries.getData().remove(0);
            timeLabels.poll();
        }
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

    public static void main(String[] args) {
        launch(args);
    }
}
