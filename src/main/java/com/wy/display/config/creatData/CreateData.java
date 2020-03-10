package com.wy.display.config.creatData;

import com.wy.display.config.readXML.ReadConfig;
import com.wy.model.decetor.LtpcChannel;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author WuYe
 * @vesion 1.0 2019/12/11
 * /
 * /**
 * @program: Ltpc-Moudle
 * @description:
 * @author: WuYe
 * @create: 2019-12-11 13:30
 **/
public class CreateData  {
    private static final int Threshould=100000000;
    private static final int Rounds=10;
    private  static ArrayList<LtpcChannel> ltpcChannels;

    public static void main(String[] args) throws Exception {
        ReadConfig.setDetectorByXlxs(new File("C:\\javaProject\\idealProject\\LTPC2020-3-4-version2\\src\\main\\resources\\detector.xlsx"));
        ltpcChannels = (ArrayList<LtpcChannel>) ReadConfig.getLtpcDetector().getChannels();
        writeLTPC("D:\\bigdata\\LtpcData\\2020_3_9\\3triggerTest.bin",3);
    }
    public static void writeLTPC(String fileName,int trigger) throws Exception {
        File file = new File(fileName);
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
         long External_trigger_timestamp=0xff00ffff0000L;
        if (trigger<1){
            throw new RuntimeException("trigger wrong!!!");
        }
        for(int i=0;i<trigger;i++){
            outEtdPck( dos,i,External_trigger_timestamp);
            for (int j=0;j<ltpcChannels.size();j++) {
                outAdcPck(dos, ltpcChannels.get(j),i,External_trigger_timestamp);
            }
        }
        dos.close();
        System.out.println("size ="+file.length());
    }

    private static void outAdcPck(DataOutputStream dos, LtpcChannel channel, int triggerNum, long external_trigger_timestamp) throws IOException {
        Random random = new Random();
        dos.writeInt(0x1EADC0DE);
        dos.writeInt(0);
        dos.writeInt(channel.getSourceBoardNum()<<16);
        int flag = (0x2<<14) | ((channel.getChannelId()&0x3f)<<8)|0x12;
        dos.writeShort(flag);
        dos.writeShort(0);
        dos.writeInt(0);
        dos.writeShort(triggerNum);
        dos.writeShort((short)(external_trigger_timestamp>>32));
        dos.writeInt((int) (external_trigger_timestamp&0xffffffffL));
        for(int i=0;i<288;i++){
            dos.writeShort(random.nextInt(1000));
        }
        dos.writeInt(0x5A5A5A5A);
    }

    private static void outEtdPck(DataOutputStream dos, int triggerNum, long external_trigger_timestamp) throws IOException {
        byte[]bytes=new byte[64];
        dos.writeInt(0x1EADC0DE);
        dos.writeLong(0L);
        dos.writeShort(0xc012);
        dos.writeShort(triggerNum);
        dos.writeInt(0);
        dos.writeShort(0);
        dos.writeShort((short)(external_trigger_timestamp>>32));
        dos.writeInt((int) (external_trigger_timestamp&0xffffffffL));
        dos.writeInt(0x5A5A5A5A);
    }



}
