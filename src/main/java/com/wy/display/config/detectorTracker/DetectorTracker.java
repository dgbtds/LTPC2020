package com.wy.display.config.detectorTracker;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/5 12:19
 */

import com.wy.model.decetor.LtpcChannel;
import com.wy.model.decetor.LtpcDetector;
import com.wy.model.decetor.TrackMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:探测器路径和通道号对应
 *
 * @author: WuYe
 *
 * @create: 2020-03-05 12:19
 **/
public class DetectorTracker {
    public  static void setTrackMetaData(LtpcDetector ltpcDetector) throws Exception {
        List<LtpcChannel> listChannels = ltpcDetector.getChannels();

        TrackMetadata singleton = TrackMetadata.getSingleton();
        HashMap<Integer, ArrayList<Integer>> channelNum_trackerNums = singleton.getChannelNum_trackerNums();
        HashMap<Integer, ArrayList<Integer>> trackerNum_channelNUms = singleton.getTrackerNum_channelNUms();
        for(int i=0;i<listChannels.size();i++){
            channelNum_trackerNums.put(i,new ArrayList<>());
        }
        for(int i=0;i<TrackMetadata.trackerCount;i++){
            trackerNum_channelNUms.put(i+1,new ArrayList<>());
        }
        //
        listChannels.forEach(
                c->{
                    switch (c.getArea()){
                        case 0:
                            AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,37,6);
                            break;
                        case 1:
                            AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,c.getBoard()+1,3);
                            break;
                        case 2:
                            AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,c.getBoard()+7,3);
                            break;
                        case 3:
                            switch (c.getBoard()){
                                case 0:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,2,3);
                                    break;
                                case 1:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,1,3);
                                    break;
                                case 2:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,4,3);
                                    break;
                                case 3:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,3,3);
                                    break;
                                case 4:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,6,3);
                                    break;
                                case 5:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,5,3);
                                    break;
                                default:
                                    throw new RuntimeException("board error: "+c.getBoard());
                            }
                            break;
                        case 4:
                            switch (c.getBoard()){
                                case 0:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,8,3);
                                    break;
                                case 1:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,7,3);
                                    break;
                                case 2:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,9,3);
                                    break;
                                case 3:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,10,3);
                                    break;
                                case 4:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,12,3);
                                    break;
                                case 5:
                                    AddMap(channelNum_trackerNums,trackerNum_channelNUms,c,11,3);
                                    break;
                                default:
                                    throw new RuntimeException("board error: "+c.getBoard());
                            }
                            break;
                        default:
                            throw new RuntimeException("case error: "+c.getArea());
                    }
                }
        );
    }
    private static void AddMap(HashMap<Integer, ArrayList<Integer>>channelNum_trackerNums ,HashMap<Integer, ArrayList<Integer>> trackerNum_channelNUms, LtpcChannel c, int TrackerNum,int plane){
        AddChannelMap(channelNum_trackerNums,c,TrackerNum,plane);
        AddTrackerMap(trackerNum_channelNUms,c,TrackerNum,plane);
    }

    private static void AddChannelMap(HashMap<Integer, ArrayList<Integer>> channelNum_trackerNums, LtpcChannel c, int TrackerNum,int plane) {
        if (plane==3) {
            ArrayList<Integer> list = channelNum_trackerNums.get(c.getPid());
            for(int i=0;i<plane;i++){
                list.add(TrackerNum+i*12);
            }
            channelNum_trackerNums.put(
                    c.getPid(),list
            );
        } else if(plane==6) {
            ArrayList<Integer> list = channelNum_trackerNums.get(c.getPid());
            for(int i=0;i<plane;i++){
                list.add(TrackerNum+i);
            }
            channelNum_trackerNums.put(
                    c.getPid(),list
            );
        }
    }
    private  static void AddTrackerMap(HashMap<Integer, ArrayList<Integer>> trackerNum_channelNUms, LtpcChannel c, int TrackerNum,int plane) {
        if (plane==3) {
            for(int i=0;i<plane;i++){
                ArrayList<Integer> list = trackerNum_channelNUms.get(TrackerNum + i * 12);
                list.add(c.getPid());
                trackerNum_channelNUms.put(TrackerNum+i*12,list);
            }
        } else if (plane==6){
            for(int i=0;i<plane;i++){
                ArrayList<Integer> list = trackerNum_channelNUms.get(TrackerNum + i );
                list.add(c.getPid());
                trackerNum_channelNUms.put(TrackerNum+i,list);
            }
        }
    }
}
