package com.wy.display.weavChart;
/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/5 12:34
 */

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.TextField;


/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-03-05 12:34
 **/
public class WeavChartController {
    @FXML
    private TextField waveformInfo;
    @FXML
    private LineChart<Number,Number> simpleWaveform;
    @FXML
    private NumberAxis timeX;
    @FXML
    private NumberAxis chargeY;
    @FXML
    private void initialize() throws Exception {

    }

}

