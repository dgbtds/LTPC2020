package com.wy.display.config.readData;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/7 15:17
 */

import com.wy.display.config.readXML.ReadConfig;
import com.wy.model.data.SimpleData;
import com.wy.model.data.SimplifyData;
import com.wy.model.decetor.LtpcChannel;
import com.wy.model.decetor.LtpcDetector;
import com.wy.model.decetor.TrackMetadata;
import com.wy.display.config.detectorTracker.DetectorTracker;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.shape.Rectangle;
import com.wy.model.data.DataSource;
import com.wy.model.data.TimeReference;

import java.io.*;
import java.util.ArrayList;

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:读取数据到对象
 *
 * @author: WuYe
 *
 * @create: 2020-03-07 15:17
 **/
public class ReadData extends ScheduledService<DataSource> {
    private static ArrayList<LtpcChannel> ltpcChannels;
    private final int rightHeader = 0x1EADC0DE;
    public final int rightTailler = 0x5A5A5A5A;
    private int chargeMax=0;
    private int chargeMin=Integer.MAX_VALUE;
    private double progess;
    private  boolean isDone=false;
    private DataInputStream dis;
    private String filePath;

    public ReadData() {
    }

    public ReadData(DataInputStream dis, String filePath) {
        this.dis = dis;
        this.filePath = filePath;
    }

    public static void main(String[] args) throws Exception {
        File file = new File("D:\\bigdata\\LtpcData\\2019.12.26\\10triggerTest.bin");
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        System.out.println("文件大小 ："+file.length());

        //配置
        ReadConfig.setDetectorByXlxs(new File("C:/javaProject/idealProject/LTPC2020-3-4-version2/src/main/resources/detector.xlsx"));
        LtpcDetector ltpcDetector = ReadConfig.getLtpcDetector();
        DetectorTracker.setTrackMetaData(ltpcDetector);

        ReadData readData = new ReadData();
        DataSource dataSource = readData.readDataSource(dis, "D:\\bigdata\\LtpcData\\2019.12.26\\2triggerTest.bin");
        System.out.println(dataSource);
    }
    public DataSource readDataSource(DataInputStream dis, String filepath) throws IOException {

        int triggerCount=1;
        int packageCount=0;
        int length=dis.available();
        DataSource dataSource = new DataSource();
        ArrayList<SimplifyData> sdList = new ArrayList<>();
        dataSource.setFilePath(filepath);

        TimeReference timeReference = readTimeReferencePck(dis);
        if (timeReference==null){return null;}
        short timeTriggerNum = timeReference.getTriggerNumber();
        //触发数据
        do {
            SimpleData simpleData = readSimpleDataPck(dis);
            if (simpleData==null){break;}
            int flag = simpleData.getFlag();
            int i = flag >> 14;
            if ((flag >> 14)==3){
                TimeReference tr = new TimeReference();
                tr.setHeader(simpleData.getHeader());
                tr.setReservedArea1(simpleData.getReservedArea1());
                tr.setTargetBoardAddress(simpleData.getTargetBoardAddress());
                tr.setPacklength(simpleData.getPacklength());
                tr.setReservedArea2(simpleData.getReservedArea2());
                tr.setSourceBoardAddress(simpleData.getSourceBoardAddress());
                tr.setPackageNunmber(simpleData.getPackageNunmber());
                tr.setType(simpleData.getType());
                tr.setFlag(simpleData.getFlag());

                tr.setTriggerNumber(dis.readShort());
                tr.setReservedArea3(dis.readInt());
                tr.setReservedArea4(dis.readShort());
                tr.setExtTimestamp16(dis.readShort());
                tr.setExtTimestamp32(dis.readInt());

                tr.setTailler(dis.readInt());
                if (timeReference.getTailler()!=rightTailler){
                    System.out.println("triggerNum"+tr.getTriggerNumber()+" : Tailler error");
                    break;
                }
                if (tr.getTriggerNumber()==timeTriggerNum){
                    System.out.println("TriggerNumber not change");
                    break;
                }
                else {
                    triggerCount++;
                    timeTriggerNum=tr.getTriggerNumber();
                }
            }
            else {
              processSimpleData(simpleData,sdList);
                packageCount++;
                if (packageCount%100==0){
                    progess=length-dis.available();
                }
            }

        } while (dis.available()>0);
        dataSource.setTriggerCount(triggerCount);
        dataSource.setSdList(sdList);
        dataSource.setChargeMax(chargeMax);
        dataSource.setChargeMin(chargeMin);
        isDone=true;
        return dataSource;
    }
    //处理单个数据包
    private void processSimpleData(SimpleData simpleData, ArrayList<SimplifyData> sdList){
        int flag = simpleData.getFlag();
        int channelId = (flag>>8) &0x3f;
        int channelNum=channelId;
//        int channelNum = getChannelNum(channelId, simpleData.getSourceBoardAddress());


        TrackMetadata singleton = TrackMetadata.getSingleton();
        //通道对应的路径编号
        ArrayList<Integer> trackers=singleton.getChannelNum_trackerNums().get(channelNum);
        if (trackers==null){
            throw new RuntimeException("请先配置参数");
        }
        short[] shorts = simpleData.getSampleData();
        int piece=trackers.size();
        int Size=shorts.length/piece+1;
        int start=0;
        for (int i=0;i<piece;i++) {
            int max = findMax(shorts, start, start+Size*(i+1));
                SimplifyData sd = new SimplifyData();
                sd.setChannelNum(channelNum);
                sd.setTriggerNum(simpleData.getTriggerNumber());
                sd.setTrackerNum(trackers.get(i));
                sd.setCharge((int) shorts[max]);
                sd.setPlaneNum(getPlaneNum(trackers.get(i)));
                sd.setRectangle(getRectangle(channelNum));
                sdList.add(sd);
            start+=Size;
        }
    }
    private int  getChannelNum(int channelId,int sourceBoard){
        if (ltpcChannels!=null){
            LtpcChannel[] ltpcChannelArray = (LtpcChannel[]) ltpcChannels.stream().filter(s -> s.getChannelId() == channelId && s.getSourceBoardNum()==sourceBoard).toArray();
            if (ltpcChannelArray.length>1){
                System.out.println("多个符合");
                return Integer.MAX_VALUE;
            }
            else {
                return ltpcChannelArray[0].getPid();
            }
        }
        System.out.println("未找到");
        return Integer.MAX_VALUE;
    }
    private Rectangle getRectangle(int channleNum){
        return TrackMetadata.getSingleton().getRectangleMap().get(channleNum);
    }
    private int  getPlaneNum(int trackerNum){
        if (trackerNum<=42&&trackerNum>=37){
            return trackerNum-33;
        }
        if (trackerNum<=36&&trackerNum>=1){
            return ((trackerNum-1)%12)+1;
        }
        return 0;
    }
    private int findMax(short[] shorts, int start, int end) {
        int MIndex=start;
        if (end>shorts.length){
            end=shorts.length;
        }
        for(int i=start+1;i<end;i++){
            if(shorts[i]>shorts[MIndex]){
                MIndex=i;
            }
        }
        if (shorts[MIndex]>chargeMax){
            chargeMax=shorts[MIndex];
        }
        if (shorts[MIndex]<chargeMin){
            chargeMin=shorts[MIndex];
        }
        return MIndex;
    }

    private TimeReference readTimeReferencePck(DataInputStream dis) throws IOException {

        int readInt = dis.readInt();
        if ( readInt!= rightHeader) {
            System.out.println("TimeReference header error");
            if (!findRightHeader(dis, readInt) ) {
                System.out.println("TimeReference no header error");
                return null;
            }
        }
        TimeReference timeReference = new TimeReference();
        timeReference.setHeader(readInt);
        timeReference.setReservedArea1(dis.readByte());
        timeReference.setTargetBoardAddress(dis.readByte());
        timeReference.setPacklength(dis.readShort());
        timeReference.setReservedArea2(dis.readByte());
        timeReference.setSourceBoardAddress(dis.readByte());
        timeReference.setPackageNunmber(dis.readByte());
        timeReference.setType(dis.readByte());

        int flag = dis.readUnsignedShort();
        timeReference.setFlag(flag);
        if ( (flag >> 14)!=3)
        { System.out.println("TimeReference flag error");return null; }

        timeReference.setTriggerNumber(dis.readShort());
        timeReference.setReservedArea3(dis.readInt());
        timeReference.setReservedArea4(dis.readShort());
        timeReference.setExtTimestamp16(dis.readShort());
        timeReference.setExtTimestamp32(dis.readInt());

        timeReference.setTailler(dis.readInt());
        if (timeReference.getTailler()!=rightTailler) { System.out.println("TimeReference Tailler error");return null; }
        return timeReference;
    }
    private SimpleData readSimpleDataPck(DataInputStream dis) throws IOException {
        int readInt = dis.readInt();
        if ( readInt!= rightHeader) {
            System.out.println("SimpleData header error");
            if (!findRightHeader(dis, readInt) ) {
                System.out.println("SimpleData no header error");
                return null;
            }
        }
        SimpleData simpleData = new SimpleData();
        simpleData.setHeader(readInt);
        simpleData.setReservedArea1(dis.readByte());
        simpleData.setTargetBoardAddress(dis.readByte());
        simpleData.setPacklength(dis.readShort());
        simpleData.setReservedArea2(dis.readByte());
        simpleData.setSourceBoardAddress(dis.readByte());
        simpleData.setPackageNunmber(dis.readByte());
        simpleData.setType(dis.readByte());

        int flag = dis.readUnsignedShort();
        simpleData.setFlag(flag);
        if ((flag >> 14)==3) { return simpleData; }
        if ((flag >> 14)!=2) { System.out.println("SimpleData flag error");return null; }

        simpleData.setPackageNumber(dis.readShort());
        simpleData.setTriggerSource(dis.readInt());
        simpleData.setTriggerNumber(dis.readShort());
        simpleData.setExtTimestamp16(dis.readShort());
        simpleData.setExtTimestamp32(dis.readInt());
        int simpleLength = (flag &0xff) * (16);
        short[] simpleDatas = new short[simpleLength];
        for(int i=0;i<simpleDatas.length;i++){
            simpleDatas[i]=(short) dis.readUnsignedShort();
        }
        simpleData.setSampleData(simpleDatas);

        simpleData.setTailler(dis.readInt());
        if (simpleData.getTailler()!=rightTailler) { System.out.println("SimpleData Tailler error");return null; }
        return simpleData;

    }
    private Boolean findRightHeader (DataInputStream dis,int readInt) throws IOException {
        while (dis.available() > 0) {
            byte b = dis.readByte();
            int read = (readInt << 8) | b;
            if (read == rightHeader) {
                return true;
            }
        }
        return false;
    }

    public double getProgess() {
        return progess;
    }

    public void setProgess(double progess) {
        this.progess = progess;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    @Override
    protected Task<DataSource> createTask() {
        return new Task<DataSource>() {
            @Override
            protected DataSource call() throws Exception {
                int triggerCount=1;
                int packageCount=0;
                int length=dis.available();
                DataSource dataSource = new DataSource();
                ArrayList<SimplifyData> sdList = new ArrayList<>();
                dataSource.setFilePath(filePath);

                TimeReference timeReference = readTimeReferencePck(dis);
                if (timeReference==null){return null;}
                short timeTriggerNum = timeReference.getTriggerNumber();
                //触发数据
                do {
                    SimpleData simpleData = readSimpleDataPck(dis);
                    if (simpleData==null){break;}
                    int flag = simpleData.getFlag();
                    int i = flag >> 14;
                    if ((flag >> 14)==3){
                        TimeReference tr = new TimeReference();
                        tr.setHeader(simpleData.getHeader());
                        tr.setReservedArea1(simpleData.getReservedArea1());
                        tr.setTargetBoardAddress(simpleData.getTargetBoardAddress());
                        tr.setPacklength(simpleData.getPacklength());
                        tr.setReservedArea2(simpleData.getReservedArea2());
                        tr.setSourceBoardAddress(simpleData.getSourceBoardAddress());
                        tr.setPackageNunmber(simpleData.getPackageNunmber());
                        tr.setType(simpleData.getType());
                        tr.setFlag(simpleData.getFlag());

                        tr.setTriggerNumber(dis.readShort());
                        tr.setReservedArea3(dis.readInt());
                        tr.setReservedArea4(dis.readShort());
                        tr.setExtTimestamp16(dis.readShort());
                        tr.setExtTimestamp32(dis.readInt());

                        tr.setTailler(dis.readInt());
                        if (timeReference.getTailler()!=rightTailler){
                            System.out.println("triggerNum"+tr.getTriggerNumber()+" : Tailler error");
                            break;
                        }
                        if (tr.getTriggerNumber()==timeTriggerNum){
                            System.out.println("TriggerNumber not change");
                            break;
                        }
                        else {
                            triggerCount++;
                            timeTriggerNum=tr.getTriggerNumber();
                        }
                    }
                    else {
                        processSimpleData(simpleData,sdList);
                        packageCount++;
                            progess=length-dis.available();
                            updateProgress(progess,length);
                    }

                } while (dis.available()>0);
                dataSource.setTriggerCount(triggerCount);
                dataSource.setSdList(sdList);
                dataSource.setChargeMax(chargeMax);
                dataSource.setChargeMin(chargeMin);
                isDone=true;
                return dataSource;
            }
        };
    }

    public static ArrayList<LtpcChannel> getLtpcChannels() {
        return ltpcChannels;
    }

    public static void setLtpcChannels(ArrayList<LtpcChannel> ltpcChannels) {
        ReadData.ltpcChannels = ltpcChannels;
    }
}
