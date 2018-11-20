package router;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by yangjie on 2017/1/8.
 */
public class RouterGraph implements Serializable {
    private ArrayList<double[]> nodes = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> edges = new ArrayList<>();

    public ArrayList<ArrayList<Integer>> getEdges() {return edges;}
    public ArrayList<double[]> getNodes() {return nodes;}

    private boolean nodeIsExist(double x,double y){
        for(double[] loc: nodes)
            if(loc[0] == x && loc[1] == y)
                return true;
        return false;
    }

    public double[] getNode(int index){ return nodes.get(index); }

    public boolean addNode(double x,double y){
        if(nodeIsExist(x,y))
            return false;
        nodes.add(new double[]{x,y});
        edges.add(new ArrayList<>());
        return true;
    }

    public boolean edgeIsExist(int nodeA, int nodeB) {
        return edges.get(nodeA).contains(nodeB);
    }

    public ArrayList<Integer> getNodeEdges(int node){
        return edges.get(node);
    }

    public void addEdge(int nodeA, int nodeB) {
        if(edgeIsExist(nodeA, nodeB))
            throw new IllegalArgumentException("edgeIsExist(nodeA, nodeB)");
        edges.get(nodeA).add(nodeB);
    }
}
