package com.example.window;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
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
    private XYChart.Series<String, Number> series;
    private final Random random = new Random();
    private final Queue<String> timeLabels = new LinkedList<>();
    private static final int MAX_DATA_POINTS = 20;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Real-Time Chart");

        BorderPane root = new BorderPane();

        // Чат справа
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefWidth(200);
        root.setRight(chatArea);

        // График в центре
        NumberAxis yAxis = new NumberAxis();
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Price");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        series = new XYChart.Series<>();
        lineChart.getData().add(series);
        root.setCenter(lineChart);

        // Таймер обновления графика
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateChart()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Нижняя панель
        HBox bottomPanel = new HBox(10);
        TextField textField1 = new TextField();
        TextField textField2 = new TextField();
        bottomPanel.getChildren().addAll(textField1, textField2);
        for (int i = 1; i <= 5; i++) {
            bottomPanel.getChildren().add(new Button("Button " + i));
        }
        root.setBottom(bottomPanel);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    private void updateChart() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        series.getData().add(new XYChart.Data<>(currentTime, 10 + random.nextInt(50)));
        timeLabels.add(currentTime);

        if (series.getData().size() > MAX_DATA_POINTS) {
            series.getData().remove(0);
            timeLabels.poll();
        }
    }
}

