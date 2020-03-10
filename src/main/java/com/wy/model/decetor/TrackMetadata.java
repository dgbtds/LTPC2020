package com.wy.model.decetor;

import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author WuYe
 * @vesion 1.0 2019/12/26
 * /
 * /**
 * @program: Ltpc-Moudle
 * @description:通道和激光路径的相互对应
 * @author: WuYe
 * @create: 2019-12-26 15:08
 **/
public class TrackMetadata {
    public static final int trackerCount=42;
    private HashMap<Integer,ArrayList<Integer>> channelNum_trackerNums;
    private HashMap<Integer, ArrayList<Integer>> trackerNum_channelNUms;
    private HashMap<Integer, ArrayList<Integer>> trackerNum_PlaneNums;
    private HashMap<Integer, Rectangle> rectangleMap;
    private static TrackMetadata trackMetadata;
    private TrackMetadata(){
        this.channelNum_trackerNums=new HashMap<>();
        this.trackerNum_channelNUms=new HashMap<>();
    }
    public static TrackMetadata getSingleton(){
        if (trackMetadata==null){
            trackMetadata= new TrackMetadata();
        }
        return trackMetadata;
    }

    public HashMap<Integer, Rectangle> getRectangleMap() {
        return rectangleMap;
    }

    public void setRectangleMap(HashMap<Integer, Rectangle> rectangleMap) {
        this.rectangleMap = rectangleMap;
    }

    public HashMap<Integer, ArrayList<Integer>> getTrackerNum_PlaneNums() {
        return trackerNum_PlaneNums;
    }

    public void setTrackerNum_PlaneNums(HashMap<Integer, ArrayList<Integer>> trackerNum_PlaneNums) {
        this.trackerNum_PlaneNums = trackerNum_PlaneNums;
    }

    public HashMap<Integer, ArrayList<Integer>> getChannelNum_trackerNums() {
        return channelNum_trackerNums;
    }

    public void setChannelNum_trackerNums(HashMap<Integer, ArrayList<Integer>> channelNum_trackerNums) {
        this.channelNum_trackerNums = channelNum_trackerNums;
    }

    public HashMap<Integer, ArrayList<Integer>> getTrackerNum_channelNUms() {
        return trackerNum_channelNUms;
    }

    public void setTrackerNum_channelNUms(HashMap<Integer, ArrayList<Integer>> trackerNum_channelNUms) {
        this.trackerNum_channelNUms = trackerNum_channelNUms;
    }

    public  String GetString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n------通道序号--对应--路径序号--- : "+channelNum_trackerNums.size()+"\n\n");
        channelNum_trackerNums.forEach(
                (k,v)->{
                    stringBuilder.append("通道序号 : "+k+" 路径序号 : ");
                    v.forEach(i->{
                        stringBuilder.append(i+" ");
                    });
                    stringBuilder.append("\n");
                }
        );
        return stringBuilder.toString();
    }
}
