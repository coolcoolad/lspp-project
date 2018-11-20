package algorithm;

import drawables.Drawable;
import drawables.DrawableSymbol;
import router.RouterGraph;
import statistics.StatisticTool;
import util.LogView;
import util.PointDouble;
import util.PointInt;
import util.Utils;

import java.util.*;

/**
 * Created by yangjie on 2017/2/5.
 */
public class AnonymousGroupAlgorithm {
    private static ArrayList<PointDouble> findAnonymousGroup1(
            DrawableSymbol user,
            HashMap<Integer, ArrayList<DrawableSymbol>> routerClientMap,
            RouterGraph routerGraph,
            HashMap<Long,Boolean> maliciousMap,
            int protectRadius,
            int anonymousNum
    ){
        ArrayList<PointDouble> ans = new ArrayList<>();
        int userChoose = anonymousNum/2;
        int maxHop = anonymousNum;
        int maxHopBak = maxHop;
        Random rand = new Random();
        ArrayList<double[]> routerArr = (ArrayList<double[]>) routerGraph.getNodes().clone();
        while (maxHop -- > 0){
            int curRouter = Utils.findNearestPoint(user.getX(),user.getY(),routerArr);
//            if(Utils.calcDistance(user.getX(),user.getY(),routerArr.get(curRouter)[0],routerArr.get(curRouter)[1])> protectRadius)
//                break;
            ArrayList<DrawableSymbol> connectUser = routerClientMap.get(curRouter);
            routerArr.remove(curRouter);
            if(connectUser == null)
                continue;
            ArrayList<DrawableSymbol> userArr = filterVisibalePoint(connectUser);
//            if(userArr.size() < userChoose)
//                continue;
            for(int i=0; i < userChoose; i++){
                if(userArr.size() == 0)
                    break;
                int choose = rand.nextInt(userArr.size());
                DrawableSymbol cur = userArr.get(choose);
                if(cur.getUid() != user.getUid() && !maliciousMap.get(cur.getUid())){
                    double dist = Utils.calcDistance(user.getX(), user.getY(), cur.getX(), cur.getY());
                    if(dist <= protectRadius)
                        ans.add(new PointDouble(cur.getX(),cur.getY()));
                }
                userArr.remove(choose);
            }
            if(ans.size() >= anonymousNum)
                break;
            try {
                Thread.sleep(50);
            } catch (Exception ex) {
                LogView.addLog(ex.getMessage());
                ex.printStackTrace();
            }
        }
        if(ans.size() < anonymousNum)
            return null;
        StatisticTool.getInstance().addCommunication(maxHopBak - maxHop);
        return ans;
    }

    private static ArrayList<PointDouble> findAnonymousGroup(
            DrawableSymbol user,
            HashMap<Integer, ArrayList<DrawableSymbol>> routerClientMap,
            RouterGraph routerGraph,
            HashMap<Long,Boolean> maliciousMap,
            int protectRadius,
            int anonymousNum
    ){
        ArrayList<PointDouble> ans = new ArrayList<>();
        int userChoose = 2;
        int maxHop = (int)(anonymousNum*1.45);
        //int maxHop = 15;
        int maxHopBak = maxHop;
        Random rand = new Random();
        ArrayList<double[]> routerArr = (ArrayList<double[]>) routerGraph.getNodes().clone();
        while (maxHop -- > 0){
            int curRouter = Utils.findNearestPoint(user.getX(),user.getY(),routerArr);
            ArrayList<DrawableSymbol> connectUser = routerClientMap.get(curRouter);
            routerArr.remove(curRouter);
            if(connectUser == null)
                continue;
            ArrayList<DrawableSymbol> userArr = filterVisibalePoint(connectUser);
            for(int i=0; i < userChoose; i++){
                if(userArr.size() == 0)
                    break;
                int choose = rand.nextInt(userArr.size());
                DrawableSymbol cur = userArr.get(choose);
                if(!maliciousMap.get(cur.getUid())){
                    double dist = Utils.calcDistance(user.getX(), user.getY(), cur.getX(), cur.getY());
                    if(dist > protectRadius)
                        ans.add(new PointDouble(cur.getX(),cur.getY()));
                }
                userArr.remove(choose);
            }
            if(ans.size() >= anonymousNum)
                break;
            try {
                Thread.sleep(20);
            } catch (Exception ex) {
                LogView.addLog(ex.getMessage());
                ex.printStackTrace();
            }
        }
        if(ans.size() < anonymousNum)
            return null;
        StatisticTool.getInstance().addCommunication(maxHopBak - maxHop);
        return ans;
    }

    private static ArrayList<DrawableSymbol> filterVisibalePoint(ArrayList<DrawableSymbol> pointArr){
        ArrayList<DrawableSymbol> visiableArr = new ArrayList<>();
        for(DrawableSymbol point: pointArr)
            if(point.getPresentation().getVisibility())
                visiableArr.add(point);
        return visiableArr;
    }

    public static PointDouble getAdjustedCoord(
            DrawableSymbol user,
            HashMap<Integer, ArrayList<DrawableSymbol>> routerClientMap,
            RouterGraph routerGraph,
            HashMap<Long,Boolean> maliciousMap,
            int protectRadius,
            int anonymousNum
    ){
        List<PointDouble> pointArr = findAnonymousGroup(user,routerClientMap,routerGraph,maliciousMap,
                protectRadius,anonymousNum);
        if(pointArr == null)
            return null;
        double sumX = 0;
        double sumY = 0;
        for(PointDouble point: pointArr){
            sumX += point.X;
            sumY += point.Y;
        }
        return new PointDouble(sumX/anonymousNum,sumY/anonymousNum);
    }
}
