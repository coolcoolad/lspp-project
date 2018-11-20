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
 * 在逻辑大小为1*1的地图上生成n个加油站
 */
public class PoiGenerator {
    private static ArrayList<Poi> generate(int poiNum) {
        ArrayList<Poi> poiArr = new ArrayList<>();
        Random rand = new Random();
        for(int i=0; i < poiNum; i++) {
            double x = rand.nextDouble();
            double y = rand.nextDouble();
            Poi poi = new Poi("gas station"+i,x,y);
            while (poiArr.contains(poi)){
                poi.locX = rand.nextDouble();
                poi.locY = rand.nextDouble();
            }
            poiArr.add(poi);
        }
        return poiArr;
    }

    public static ArrayList<Poi> loadFromFile() {
        return Utils.loadObjectFromFile(ConstantValues.poiArrObjPath);
    }

    public static void main(String[] args) {
        try {
            int num = Integer.valueOf(args[0]);
            ArrayList<Poi> poiArr = PoiGenerator.generate(num);
            Utils.saveObjectToFile(poiArr, "poiArr1.obj");
            LogView.addLog("generate poiArr ok!");
        } catch (Exception ex) {ex.printStackTrace();}
    }
}
