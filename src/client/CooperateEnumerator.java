package client;

/**
 * Created by yangjie on 2017/1/11.
 */

import algorithm.AnonymousGroupAlgorithm;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import drawables.DrawableSymbol;
import router.RouterGenerator;
import router.RouterGraph;
import statistics.StatisticTool;
import util.LogView;
import util.PointDouble;
import util.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.alibaba.fastjson.JSON;

/**
 * 先找最近的路由
 * 计算临近路由和用户的中心，找中心离用户最近的路由
 */
public class CooperateEnumerator {
    private static double requestRate = 0.02;
    private static ArrayList<DrawableSymbol> pointArr = null;
    private static HashMap<Long,Boolean> maliciousMap = new HashMap<>();
    private static HashSet<DrawableSymbol> requestSet = new HashSet<>();
    private static double mapWidth_ = 0;
    private static double mapHeight_ = 0;
    private static RouterGraph routerGraph = null;
    private static HashMap<Integer,ArrayList<DrawableSymbol>> routerClientMap = new HashMap<>();
    public static double getMapWidth(){return mapWidth_;}
    public static double getMapHeight() {return mapHeight_;}
    public static ArrayList<DrawableSymbol> getPointArr() {return pointArr;}
    public static Boolean isRequestPoint(DrawableSymbol point) {return requestSet.contains(point);}
    public static void init(ArrayList<DrawableSymbol> arr,int time,int maliciousNum,
                            double mapWidth,double mapHeight) {
        pointArr = arr;
        mapWidth_ = mapWidth;
        mapHeight_ = mapHeight;
        //用uid统计用户数目
        for(DrawableSymbol point: arr)
            maliciousMap.put(point.getUid(),false);
        int userNum = maliciousMap.size();
        //根据时间计算请求的数目，并采样
        int requestTotal = (int)(arr.size() * requestRate);
        int[] idxArr = Utils.sampleIndex(arr.size(),requestTotal);
        for(int idx: idxArr)
            requestSet.add(arr.get(idx));
        //采样指定数目的用户作为恶意用户
        if(maliciousNum > userNum)
            throw new IllegalArgumentException("maliciousNum > userNum");
        idxArr = Utils.sampleIndex(userNum,maliciousNum);
        Arrays.sort(idxArr);
        int i=0;
        int j=0;
        for(Long key: maliciousMap.keySet()){
            if(j >= idxArr.length)
                break;
            if(i == idxArr[j]) {
                maliciousMap.put(key, true);
                j++;
            }
            i++;
        }
        initRouterGraph();
        initRouterConnectMap();
    }

    private static void initRouterGraph() {
        routerGraph = RouterGenerator.loadFromFile();
        ArrayList<double[]> nodeArr = routerGraph.getNodes();
        for(double[] node: nodeArr){
            node[0] *= getMapWidth();
            node[1] *= getMapHeight();
        }
    }

    private static boolean userSendRequest(long uid, int locX,int locY,int num){
        try {
            String url = "http://127.0.0.1:8080/getNearbyPoi?keyword=kw_&locX=x_&locY=y_&num=n_";
            url = url.replace("kw_","test");
            url = url.replace("x_",locX*1.0/mapWidth_+"");
            url = url.replace("y_",locY*1.0/mapHeight_+"");
            url = url.replace("n_",num+"");
            String jsonStr = sendRequest(url);
            JSONArray poiArr = (JSONArray) JSON.parse(jsonStr);
            JSONObject poi = (JSONObject) poiArr.get(0);
            int x = (int)(Double.valueOf(poi.get("locX").toString()) * mapWidth_);
            int y = (int)(Double.valueOf(poi.get("locY").toString()) * mapHeight_);
            //LogView.addLog("--"+poi.get("name")+","+x+","+y);
            return true;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public static void usersSendRequests(int poiNum, int anonymousNum, int protectRadius) {
        for(DrawableSymbol point: pointArr){
            if(!maliciousMap.get(point.getUid()) && requestSet.contains(point) && point.getPresentation().getVisibility()){
                long start = System.currentTimeMillis();
                PointDouble loc = AnonymousGroupAlgorithm.getAdjustedCoord(point,routerClientMap,routerGraph,
                        maliciousMap,protectRadius,anonymousNum);
                //userSendRequest(point.getUid(),point.getX(),point.getY(),poiNum);
                if(loc != null) {
                    long end = System.currentTimeMillis();
                    boolean flag = userSendRequest(point.getUid(), (int) loc.X, (int) loc.Y, poiNum);
                    StatisticTool.getInstance().addSucessFlag(flag);
                    if(flag) {
                        LogView.addLog("user "+point.getUid()+" send a request: "+loc.X+","+loc.Y);
                        double time = (end - start) / 1000.0;
                        StatisticTool.getInstance().addResponseTime(time);
                    }
                }
                else
                    StatisticTool.getInstance().addSucessFlag(false);
            }
        }
    }

    private static String sendRequest(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                while (true){
                    String line = br.readLine();
                    if(line == null)
                        break;
                    sb.append(line);
                }
                return sb.toString();
            }
            return null;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    private static void initRouterConnectMap() {
        //遍历每一个用户位置，找离该用户位置最近的路由
        ArrayList<double[]> nodeArr = routerGraph.getNodes();
        for(DrawableSymbol point: pointArr){
            int nearest = Utils.findNearestPoint(point.getX(), point.getY(), nodeArr);
            if(!routerClientMap.containsKey(nearest))
                routerClientMap.put(nearest,new ArrayList<>());
            routerClientMap.get(nearest).add(point);
        }
    }
}
