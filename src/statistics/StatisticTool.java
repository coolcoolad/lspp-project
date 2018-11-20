package statistics;

import util.ConstantValues;
import util.LogView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by yangjie on 2017/2/6.
 */
public class StatisticTool {
    private static StatisticTool tool = null;
    public int anonymousNum = 0;
    public int userNum = 0;
    private ArrayList<Double> responseTimeArr = new ArrayList<>();
    private ArrayList<Double> communicationArr = new ArrayList<>();
    private ArrayList<Boolean> successFlagArr = new ArrayList<>();
    private StatisticTool(){}
    public void addResponseTime(double time){responseTimeArr.add(time);}
    public void addCommunication(double communication){communicationArr.add(communication);}
    public void addSucessFlag(boolean flag){successFlagArr.add(flag);}
    public double calcAvgResponseTime(){
        if(responseTimeArr.size() == 0)
            throw new IllegalArgumentException("responseTimeArr.size() == 0");
        double sum = 0;
        for(double time: responseTimeArr)
            sum += time;
        return sum / responseTimeArr.size();
    }
    public double calcAvgCommunication() {
        if(communicationArr.size() == 0)
            throw new IllegalArgumentException("communicationArr.size() == 0");
        double sum = 0;
        for(double comm: communicationArr)
            sum += comm;
        return sum / communicationArr.size();
    }
    public double calcSuccessRate() {
        if(successFlagArr.size() == 0)
            throw new IllegalArgumentException("successFlagArr.size() == 0");
        double sum = 0;
        for(boolean flag: successFlagArr)
            if(flag)
                sum += 1;
        return sum / successFlagArr.size();
    }
    public static StatisticTool getInstance(){
        if(tool == null)
            tool = new StatisticTool();
        return tool;
    }
    public static void reset(){tool = null;}
    public void appendToFile(){
        try {
            File file = new File(ConstantValues.statisticsCsvPath);
            boolean flag = file.exists();
            FileWriter fw = new FileWriter(ConstantValues.statisticsCsvPath,true);
            if(!flag)
                fw.write("timestamp,userNum,requestNum,anonymousNum,avgResponseTime,avgCommunication,successRate\n");
            long time = System.currentTimeMillis();
            double avgResponseTime = calcAvgResponseTime();
            double avgCommunication = calcAvgCommunication();
            double successRate = calcSuccessRate();
            fw.write(time+","+userNum+","+successFlagArr.size()+","+anonymousNum+","+avgResponseTime+","+avgCommunication+","+successRate+"\n");
            fw.close();
        } catch (Exception ex){
            LogView.addLog(ex.getMessage());
            ex.printStackTrace();
            HashSet<Integer> aa = new HashSet<>();
            aa.toArray(new Integer[0]);
        }
    }
}
