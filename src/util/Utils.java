package util;

import generator2.DataGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by yangjie on 2017/1/9.
 */
class Ele <T extends Comparable<T>> implements Comparable<Ele<T>>{
    public int index;
    public T data;
    public Ele(int index, T data){
        this.index = index;
        this.data = data;
    }

    @Override
    public int compareTo(Ele<T> o) {
        return this.data.compareTo(o.data);
    }
}

public class Utils {
    public static <T extends Comparable<T>> int[] sortIndex(T[] arr, boolean reverse){
        ArrayList<Ele<T>> eleArr = new ArrayList<>(arr.length);
        for(int i=0; i < arr.length; i++)
            eleArr.add(new Ele<T>(i,arr[i]));
        Collections.sort(eleArr);
        if(reverse)
            Collections.reverse(eleArr);
        int[] indexArr = new int[arr.length];
        for(int i=0; i < arr.length; i++)
            indexArr[i] = eleArr.get(i).index;
        return indexArr;
    }

    public static double calcDistance(double x0, double y0, double x1, double y1) {
        return Math.sqrt(Math.pow(x0-x1,2) + Math.pow(y0-y1,2));
    }

    public static <T extends Serializable> void saveObjectToFile(T obj, String filePath){
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(obj);
            objectOut.close();
            fileOut.close();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static <T extends Serializable> T loadObjectFromFile(String filePath){
        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            T obj = (T)objectIn.readObject();
            objectIn.close();
            fileIn.close();
            return obj;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static void swap(int[] arr, int x, int y){
        int t = arr[x];
        arr[x] = arr[y];
        arr[y] = t;
    }

    public static int[] sampleIndex(int size, int num){
        if(size < num)
            throw new IllegalArgumentException("size < num");
        int[] idxArr = new int[size];
        for(int i=0; i < size; i++)
            idxArr[i] = i;
        int[] locArr = new int[num];
        Random rand = new Random();
        for(int i=0; i < num; i++) {
            int select = rand.nextInt(size - i);
            locArr[i] = idxArr[select];
            swap(idxArr,size-1-i,select);
        }
        return locArr;
    }

    public static int findNearestPoint(double x, double y, ArrayList<double[]> pointArr){
        double dist = Double.MAX_VALUE;
        int nearestIdx = -1;
        for(int i=0; i < pointArr.size(); i++){
            double cur = Utils.calcDistance(x,y,pointArr.get(i)[0],pointArr.get(i)[1]);
            if(dist > cur){
                nearestIdx = i;
                dist = cur;
            }
        }
        return nearestIdx;
    }
}
