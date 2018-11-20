package router;

/**
 * Created by yangjie on 2017/1/8.
 */

import util.ConstantValues;
import util.LogView;
import util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class RouterGenerator {
    private static RouterGraph graph = null;
    private static final double offset = 0.34;

    /**
     * 随机在逻辑大小为1*1的地图上生成n个路由
     * @param routerNum
     */
    private static void generateNodes(int routerNum) {
        Random rand = new Random();
        //生成n个点，坐标随机
        for(int i=0; i < routerNum; i++){
            double x = rand.nextDouble();
            double y = rand.nextDouble();
            while (!graph.addNode(x, y)){
                x = rand.nextDouble();
                y = rand.nextDouble();
            }
        }
//        for(int i=0; i < routerNum; i++){
//            double x = Math.abs(rand.nextGaussian()/6+0.5);
//            double y = Math.abs(rand.nextGaussian()/6+0.5);
//            x = Math.min(1,x);
//            y = Math.min(1,y);
//            while (!graph.addNode(x, y)){
//                x = Math.abs(rand.nextGaussian());
//                y = Math.abs(rand.nextGaussian());
//                x = Math.min(1,x);
//                y = Math.min(1,y);
//            }
//        }
    }

    /**
     * 使用九宫格，生成星型路由网络
     */
    private static void generateEdges() {
        //计算九宫格每个格子的中心
        Double[][] centerArr = {
                {0.25,0.25},{0.25,0.5},{0.25,0.75},
                {0.5,0.25},{0.5,0.5},{0.5,0.75},
                {0.75,0.25},{0.75,0.5},{0.75,0.75},
        };
//        double centerX = offset/2;
//        double centerY = offset/2;
//        for(int i=0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                centerArr[i * 3 + j][0] = centerX;
//                centerArr[i * 3 + j][1] = centerY;
//                centerY += offset;
//            }
//            centerX += offset;
//            centerY = offset/2;
//        }
        //找出距离格子中心最近的点，作为每个格子的二级路由
        int[] routerLevelTwoArr = new int[9];
        ArrayList<double[]> nodeArr = graph.getNodes();
        for(int i=0; i < 9; i++)
            routerLevelTwoArr[i] = Utils.findNearestPoint(centerArr[i][0], centerArr[i][1], nodeArr);
        //三级路由连接到二级路由
        for(int i=0; i < nodeArr.size(); i++){
            int index = getIndex(nodeArr.get(i)[0], nodeArr.get(i)[1]);
            if(i != routerLevelTwoArr[index])
                graph.addEdge(i, routerLevelTwoArr[index]);
        }
        //把二级路由连接到一级路由
        for(int i=0; i < 9; i++)
            if(routerLevelTwoArr[i] != routerLevelTwoArr[4] && !graph.edgeIsExist(routerLevelTwoArr[i],routerLevelTwoArr[4]))
                graph.addEdge(routerLevelTwoArr[i],routerLevelTwoArr[4]);
    }

    private static int getIndex(double x, double y) {
        double left = 0;
        double right = offset;
        double top = 0;
        double down = offset;
        for(int i=0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (y >= left && y < right && x >= top && x < down)
                    return i * 3 + j;
                left += offset;
                right += offset;
            }
            top += offset;
            down += offset;
            left = 0;
            right = offset;
        }
        throw new IllegalArgumentException();
    }

    /**
     * 给每个结点生成[a,b)个与最近结点的连接
     * @param a
     * @param b
     */
    private static void generateEdges1(int a,int b) {
        Random rand = new Random();
        int routerNum = graph.getNodes().size();
        Collections.sort(new ArrayList<Integer>());
        //给每个结点生成[a,b)个与最近结点的连接
        Double[][] fullGraph = new Double[routerNum][routerNum];
        for(int i=0; i < routerNum; i++)
            for(int j=0; j < routerNum; j++)
                fullGraph[i][j] = Utils.calcDistance(graph.getNode(i)[0], graph.getNode(i)[1],
                        graph.getNode(j)[0], graph.getNode(j)[1]);
        for(int i=0; i < routerNum; i++){
            int num = rand.nextInt(b-a)+a+1;
            int[] indexArr = Utils.sortIndex(fullGraph[i],false);
            for(int j=0; j < num; j++) {
                if(graph.getNodeEdges(i).size() >= num)
                    break;
                if(indexArr[j] == i || graph.edgeIsExist(i,indexArr[j])
                        || graph.getNodeEdges(indexArr[j]).size() >= num)
                    continue;
                graph.addEdge(i, indexArr[j]);
                graph.addEdge(indexArr[j],i);
            }
        }
    }

    private static RouterGraph generate(int routerNum, int a, int b){
        graph = new RouterGraph();
        generateNodes(routerNum);
        generateEdges();
        return graph;
    }

    public static RouterGraph loadFromFile(){
        return Utils.loadObjectFromFile(ConstantValues.routerGraphObjPath);
    }

    public static void main(String[] args) {
        try {
            int num = Integer.valueOf(args[0]);
            RouterGraph graph = RouterGenerator.generate(num, 2, 6);
            Utils.saveObjectToFile(graph, ConstantValues.routerGraphObjPath);
            LogView.addLog("generate routerGraph ok!");
        } catch (Exception ex){ex.printStackTrace();}
    }
}
