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

        // Нижняя панель
        HBox bottomPanel = new HBox(10);
        TextField textField1 = new TextField();
        TextField textField2 = new TextField();
        Button updateLineButton = new Button("Update Line");

        updateLineButton.setOnAction(e -> {
            try {
                currentHorizontalLineValue = Double.parseDouble(textField1.getText());
            } catch (NumberFormatException ex) {
                textField1.setText("Invalid");
            }
        });

        bottomPanel.getChildren().addAll(textField1, textField2, updateLineButton);
        for (int i = 1; i <= 5; i++) {
            bottomPanel.getChildren().add(new Button("Button " + i));
        }
        // Перемещение слайдера в правую панель
        VBox rightPanel = new VBox(10);
        rightPanel.getChildren().addAll(speedSlider, new Label("Speed Control"));
        root.setRight(rightPanel);

        bottomPanel.setPadding(new Insets(10, 10, 20, 10));
        root.setBottom(bottomPanel);

        // График в центре
        NumberAxis yAxis = new NumberAxis();
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Price");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        series = new XYChart.Series<>();
        horizontalLine = new XYChart.Series<>();
        lineChart.getData().addAll(series, horizontalLine);
        lineChart.setMaxHeight(800);

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

        // Перемещение графика при зажатой левой кнопке мыши
        final double[] dragStartY = {0};
        lineChart.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                dragStartY[0] = event.getY();
            }
        });

        lineChart.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                double deltaY = event.getY() - dragStartY[0];
                double shift = (yAxis.getUpperBound() - yAxis.getLowerBound()) * deltaY / lineChart.getHeight();
                yAxis.setAutoRanging(false);
                yAxis.setUpperBound(yAxis.getUpperBound() - shift);
                yAxis.setLowerBound(yAxis.getLowerBound() - shift);
                dragStartY[0] = event.getY();
            }
        });

        root.setCenter(lineChart);

        // Запуск обновления графика
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateChart()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.setScene(new Scene(root, 1920, 1080));
        primaryStage.show();
    }

    private void updateChart() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        series.getData().add(new XYChart.Data<>(currentTime, 10 + random.nextInt(50)));
        horizontalLine.getData().add(new XYChart.Data<>(currentTime, currentHorizontalLineValue));
        timeLabels.add(currentTime);

        if (series.getData().size() > MAX_DATA_POINTS) {
            series.getData().remove(0);
            horizontalLine.getData().remove(0);
            timeLabels.poll();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
