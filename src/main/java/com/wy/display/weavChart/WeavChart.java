package com.wy.display.weavChart;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/5 12:33
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description: 绘制探测器模型
 *
 * @author: WuYe
 *
 * @create: 2020-03-05 12:33
 **/
public class WeavChart extends Application {
    public void start() throws Exception{
        URL resource = getClass().getResource("../detector/DetectorPaint.fxml");
        Parent root = FXMLLoader.load(resource);
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Ltpc Detector Display");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    @Override
    public void start(Stage primaryStage) throws Exception{
        URL resource = getClass().getResource("../detector/DetectorPaint.fxml");
        Parent root = FXMLLoader.load(resource);
        primaryStage.setTitle("Ltpc Detector Display");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
       launch(args);
    }
}
