package poi;

import util.ConstantValues;
import util.LogView;
import util.Utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by yangjie on 2017/1/9.
 */

/**
 * 在逻辑大小为1*1的地图上生成n个poi
 */
public class PoiGenerator {
    private static ArrayList<Poi> generate(int poiNum, int type) {
        ArrayList<Poi> poiArr = new ArrayList<>();
        Random rand = new Random();
        for(int i=0; i < poiNum; i++) {
            double x = rand.nextDouble();
            double y = rand.nextDouble();
            Poi poi = new Poi("poi"+i,x,y);
            poi.type = type;
            while (poiArr.contains(poi)){
                poi.locX = rand.nextDouble();
                poi.locY = rand.nextDouble();
            }
            poiArr.add(poi);
        }
        return poiArr;
    }

    public static void main(String[] args) {
        try {
            int num = Integer.valueOf(args[0]);
            ArrayList<Poi> poiArr = new ArrayList<>();
            for (int i=0; i < 5; i++) {
                ArrayList<Poi> arr = PoiGenerator.generate(num, i);
                poiArr.addAll(arr);
            }
            Utils.saveObjectToFile(poiArr, ConstantValues.poiArrObjPath);
            LogView.addLog("generate poiArr ok!");
        } catch (Exception ex) {ex.printStackTrace();}
    }
}
