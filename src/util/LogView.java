package util;

import generator2.DataGenerator;

/**
 * Created by yangjie on 2017/2/1.
 */
public class LogView {
    private static DataGenerator frame_ = null;
    public static void init(DataGenerator frame){frame_ = frame;}
    public static void addLog(String log){frame_.addToLogView(log);}
}
