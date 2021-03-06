package com.wy.display.config;

import com.wy.Main;
import com.wy.display.config.readData.ReadData;
import com.wy.display.config.readXML.ReadConfig;
import com.wy.model.data.SimplifyData;
import com.wy.model.decetor.LtpcChannel;
import com.wy.model.decetor.LtpcDetector;
import com.wy.model.decetor.TrackMetadata;
import com.wy.display.config.detectorTracker.DetectorTracker;
import com.wy.display.detector.DetectorPaintController;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.wy.model.data.DataSource;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dgbtds
 */
public class ConfigController {
    public static Color[] colors = {
            Color.WHITE, Color.LIGHTGRAY, Color.GRAY, Color.DARKGRAY,
            Color.BLACK, Color.RED, Color.PINK, Color.ORANGE,
            Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.BLUE
    };
    @FXML
    private TextField channelNum;
    @FXML
    private TextField trackerNum;
    @FXML
    private TextField planeNum;
    @FXML
    private TextField triggerNum;
    @FXML
    private Button image;
    @FXML
    private ProgressBar fileProgressBar;
    @FXML
    private TextArea ConfigLog;
    private  DataSource dataSource;
    private LtpcDetector ltpcDetector;

    private boolean isConfiged=false;
    @FXML
    private void initialize(){
        ConfigLog.setFont(Font.font(20));
        fileProgressBar.setProgress(0);
        Tooltip triggerTip = new Tooltip("填写触发号,必填");
        Tooltip planeTip = new Tooltip("填写平面号，必填");
        Tooltip trackerTip = new Tooltip("填写路径号，可不选");
        Tooltip channelTip = new Tooltip("填写通道号，可不填");
        Tooltip imageTip = new Tooltip("打开参考图片");
        triggerNum.setTooltip(triggerTip);
        trackerNum.setTooltip(trackerTip);
        planeNum.setTooltip(planeTip);
        channelNum.setTooltip(channelTip);
        image.setTooltip(imageTip);

        setIntegerFormatter(triggerNum);
        setIntegerFormatter(trackerNum);
        setIntegerFormatter(planeNum);
        setIntegerFormatter(channelNum);

    }
    private void setIntegerFormatter(TextField t){
        TextFormatter<Integer> integerTextFormatter = new TextFormatter<>(
                change -> {
                    String text = change.getControlNewText();
                    if (text.matches("\\d*\\.?\\d*")) {
                        return change;
                    } else {
                        return null;
                    }
                }
        );
        t.setTextFormatter(integerTextFormatter);
    }
    @FXML
    private void controlButtonAction() throws Exception {
        FileChooser fileChooser = FileChooseBuild();
        fileChooser.setTitle("选择配置文件");
        fileChooser.setInitialDirectory(new File("C:/javaProject/idealProject/LTPC2020-3-4-version2/src/main/resources"));
        File file = fileChooser.showOpenDialog(new Stage());
        if (file!=null) {
            ConfigLog.setStyle("-fx-text-fill:green");
            ConfigLog.setText("---------初始化配置文件--------- \n Path: "+file.getAbsolutePath()+"\n\n");
            ReadConfig.setDetectorByXlxs(file);
            ltpcDetector = ReadConfig.getLtpcDetector();
            String s = ltpcDetector.toString();
            ConfigLog.appendText(s);

            DetectorTracker.setTrackMetaData(ltpcDetector);
            ConfigLog.appendText( TrackMetadata.getSingleton().GetString());
            DetectorPaintController.setChannels(ltpcDetector.getChannels());
            Main.showDetector();
            isConfiged=true;
        }
        else {
            ConfigLog.setStyle("-fx-text-fill:pink");
            ConfigLog.setText("未选择配置文件");
        }
    }
    @FXML
    private void imageAction(){
        AnchorPane anchorPane = new AnchorPane();
        ImageView imageView = new ImageView("com/wy/DetectorInfo.png");
        anchorPane.getChildren().add(imageView);
        Stage stage = new Stage();
        stage.setTitle("参考图片");
        stage.setWidth(1300);
        stage.setHeight(900);
        stage.setScene(new Scene(anchorPane));
        stage.show();
    }
    @FXML
    private void FileOpenAction() throws IOException {
        if (isConfiged) {
            FileChooser fileChooser = FileChooseBuild();
            fileChooser.setInitialDirectory( new File("D:\\bigdata\\LtpcData\\2019.12.26"));
            File file = fileChooser.showOpenDialog(new Stage());
            DataInputStream dis = new DataInputStream(new FileInputStream(file));

            ReadData readData = new ReadData(dis, file.getPath());
            ReadData.setLtpcChannels((ArrayList<LtpcChannel>) ltpcDetector.getChannels());
            fileProgressBar.progressProperty().bind(readData.progressProperty());
            readData.start();

            readData.setOnSucceeded(event -> {
                dataSource = readData.getValue();
                if (dataSource!=null){
                    ConfigLog.setStyle("-fx-text-fill:blue");
                    ConfigLog.appendText(dataSource.toString());
                }
                fileProgressBar.progressProperty().unbind();
                fileProgressBar.setProgress(1);

            });

        }
        else {
            ConfigLog.setStyle("-fx-text-fill:red");
            ConfigLog.appendText("请先配置参数文件");
        }
    }
    public static FileChooser FileChooseBuild(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有类型","*.*"),
                new FileChooser.ExtensionFilter("Excel类型","*.xlsx"),
                new FileChooser.ExtensionFilter("数据类型","*.bin")
        );
        return fileChooser;
    }
    @FXML
    private void ColorButtonAction(){

        VBox ColorBlock = new VBox();
        ColorBlock.setPrefHeight(560);
        ColorBlock.setPrefWidth(120);
        ColorBlock.setBorder(new Border(
                new BorderStroke(Color.DODGERBLUE, BorderStrokeStyle.SOLID,new CornerRadii(10), BorderWidths.DEFAULT)
        ));

        double wdith = ColorBlock.getPrefWidth();
        double height = ColorBlock.getPrefHeight();
        int count = colors.length + 1;
        for(int i=0;i<colors.length;i++){
            Rectangle rectangle = new Rectangle(0, (double) height  / (double)count, wdith, (double) height  / (double)count);
            rectangle.setFill(colors[colors.length-1-i]);
            rectangle.setStyle("-fx-stroke-type:outside");
            ColorBlock.getChildren().add(rectangle);
        }
        Label label = new Label("颜色标尺");
        label.setPrefWidth(wdith);
        label.setFont(Font.font(20));
        label.setPrefHeight((double)height/(double)count);
        ColorBlock.getChildren().add(label);

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("  颜色标尺 ");
        stage.setScene(new Scene(ColorBlock));
        stage.show();
    }
    @FXML
    private void fillButtonAction() {

        String triggerS = triggerNum.getText();
        String planeS = planeNum.getText();
        String trackerS = trackerNum.getText();
        String channelS = channelNum.getText();
        Integer trigger = null, plane = null, tracker = null, channel=null;

        try {
            if (!triggerS.equals("")) {
                trigger = Integer.valueOf(triggerS);
            } else {
                ConfigLog.appendText("触发序号必须填写");
                return;
            }

            if (!planeS.equals("") ) {
                plane = Integer.valueOf(planeS);
            } else {
                ConfigLog.appendText("平面序号必须填写");
                return;
            }

            if (!trackerS.equals("")) {
                tracker = Integer.valueOf(trackerS);
            } else {
                tracker = null;
            }

            if (!channelS.equals("")) {
                channel = Integer.valueOf(channelS);
            } else {
                channel = null;
            }

        } catch (NumberFormatException e) {
            ConfigLog.appendText(e.getMessage());
        }
        if (dataSource==null){
            ConfigLog.appendText("没有数据源");
            return;
        }

        DetectorPaintController.setDataSource(dataSource);
        ArrayList<SimplifyData> sdList = dataSource.getSdList();
        Integer finalTrigger = trigger;
        Integer finalPlane = plane;
        Stream<SimplifyData> simplifyDataStream = sdList.stream()
                .filter(s -> s.getTriggerNum() == finalTrigger).filter(s -> s.getTriggerNum() == finalTrigger)
                .filter(s -> s.getPlaneNum() == finalPlane);
        if (tracker!=null){
            Integer finalTracker = tracker;
            Stream<SimplifyData> simplifyDataStream1 = simplifyDataStream.filter(s -> s.getTrackerNum() == finalTracker);
            if (channel!=null){
                Integer finalChannel = channel;
                Stream<SimplifyData> simplifyDataStream2 = simplifyDataStream1.filter(s -> s.getTrackerNum() == finalChannel);
                ArrayList<SimplifyData> collect = (ArrayList<SimplifyData>) simplifyDataStream2.collect(Collectors.toList());
                DetectorPaintController.setSdList(collect);
                DetectorPaintController.fillRect();
            }
            else {
                ArrayList<SimplifyData> collect = (ArrayList<SimplifyData>) simplifyDataStream1.collect(Collectors.toList());
                DetectorPaintController.setSdList(collect);
                DetectorPaintController.fillRect();
            }
        }
        else {
            ArrayList<SimplifyData> collect = (ArrayList<SimplifyData>) simplifyDataStream.collect(Collectors.toList());
            DetectorPaintController.setSdList(collect);
            DetectorPaintController.fillRect();
        }
    }

}
